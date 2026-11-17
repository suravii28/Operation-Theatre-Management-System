#!/usr/bin/env node
/**
 * OTMS-NF-001 / TC-PERF-01 performance probe.
 *
 * SRS requirement: "The system shall return normal scheduling and resource
 * availability responses within 3 seconds for at least 90% of requests under
 * normal load" — i.e. 90th percentile latency <= 3000 ms.
 *
 * The script logs in once, resolves the seeded theatre/surgeon IDs, then drives
 * a mixed workload through the representative endpoints:
 *   - GET /api/surgeries        (surgery list / scheduling view)
 *   - GET /api/theatres          (resource availability)
 *   - POST /api/surgeries        (scheduling write path incl. locking)
 *
 * Each POST is given a unique future slot so no two writes conflict, which
 * keeps the run repeatable against a freshly seeded backend.
 *
 * Usage (run against a live backend, e.g. `./mvnw spring-boot:run`):
 *   node scripts/perf-check.js \
 *     [--baseUrl http://localhost:8080] \
 *     [--requests 180] [--concurrency 6] \
 *     [--out docs/reports/<stamp>/perf/results.json]
 *
 * Exits non-zero when any request fails or p90 exceeds the 3s budget, so it can
 * gate CI or report generation.
 */
'use strict';

const args = process.argv.slice(2);

function argValue(name, fallback) {
  const i = args.indexOf(name);
  return i !== -1 && args[i + 1] !== undefined ? args[i + 1] : fallback;
}

const BASE_URL = argValue('--baseUrl', 'http://localhost:8080');
const TOTAL_REQUESTS = Number(argValue('--requests', 180));
const CONCURRENCY = Number(argValue('--concurrency', 6));
const OUT_FILE = argValue('--out', null);
const TARGET_MS = 3000; // OTMS-NF-001 acceptance budget

async function api(method, path, token, body) {
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  // Drain the body so the socket is reusable and latency is end-to-end.
  const text = await res.text();
  return { status: res.status, body: text };
}

function pad(n) {
  return String(n).padStart(2, '0');
}

function slot(i) {
  // Unique future slot per request: 10 one-hour slots per day, 30+ days out,
  // so repeated runs never collide with each other or with seed data.
  const day = 30 + Math.floor(i / 10);
  const hour = 8 + (i % 10);
  const d = new Date();
  d.setDate(d.getDate() + day);
  const date = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  const start = `${date}T${pad(hour)}:00`;
  const endHour = hour + 1 > 23 ? 23 : hour + 1;
  const endMin = hour + 1 > 23 ? 59 : 0;
  return { start, end: `${date}T${pad(endHour)}:${pad(endMin)}` };
}

function percentile(sorted, p) {
  if (sorted.length === 0) return 0;
  const idx = Math.min(sorted.length - 1, Math.ceil((p / 100) * sorted.length) - 1);
  return Math.round(sorted[idx] * 10) / 10;
}

function summarize(samples) {
  const sorted = [...samples].sort((a, b) => a - b);
  return {
    count: sorted.length,
    p50: percentile(sorted, 50),
    p90: percentile(sorted, 90),
    p95: percentile(sorted, 95),
    p99: percentile(sorted, 99),
    max: sorted.length ? Math.round(sorted[sorted.length - 1] * 10) / 10 : 0,
    avg: sorted.length
      ? Math.round(sorted.reduce((s, v) => s + v, 0) / sorted.length)
      : 0,
  };
}

async function main() {
  console.log(`OTMS-NF-001 / TC-PERF-01 performance probe`);
  console.log(`Target: p90 <= ${TARGET_MS} ms | requests: ${TOTAL_REQUESTS} | concurrency: ${CONCURRENCY}`);
  console.log(`Backend: ${BASE_URL}\n`);

  const login = await api('POST', '/api/auth/login', null, {
    email: 'admin@otms.local',
    password: 'Admin123!',
  });
  if (login.status !== 200) {
    console.error(`Login failed (${login.status}). Is the backend running with seed data?`);
    process.exit(1);
  }
  const token = JSON.parse(login.body).token;

  const theatres = JSON.parse((await api('GET', '/api/theatres', token)).body);
  const ot3 = theatres.find((t) => t.name === 'OT-3');
  const staff = JSON.parse((await api('GET', '/api/staff?role=DOCTOR', token)).body);
  const surgeon = staff.find((u) => u.name === 'Dr. Priya Nair');
  if (!ot3 || !surgeon) {
    console.error('Seed data incomplete (missing OT-3 or Dr. Priya Nair).');
    process.exit(1);
  }

  // Warm-up: let JIT, H2 and the connection pool settle before measuring.
  for (let w = 0; w < 6; w++) {
    await api('GET', '/api/surgeries', token);
    await api('GET', '/api/theatres', token);
  }

  const samples = [];
  const failures = [];
  const perKind = { 'GET /api/surgeries': [], 'GET /api/theatres': [], 'POST /api/surgeries': [] };

  let next = 0;
  async function worker() {
    for (;;) {
      const i = next++;
      if (i >= TOTAL_REQUESTS) return;
      const kind = i % 3;
      let method = 'GET';
      let path = '/api/surgeries';
      let body;
      if (kind === 1) {
        path = '/api/theatres';
      } else if (kind === 2) {
        method = 'POST';
        const s = slot(i);
        body = {
          patientName: `Perf Patient ${i}`,
          procedureName: 'Perf probe',
          theatreId: ot3.id,
          leadSurgeonId: surgeon.id,
          startTime: s.start,
          endTime: s.end,
          emergency: false,
          notes: 'TC-PERF-01 load probe',
        };
      }
      const label = `${method} ${path}`;
      const t0 = process.hrtime.bigint();
      try {
        const res = await api(method, path, token, body);
        const ms = Math.round((Number(process.hrtime.bigint() - t0) / 1e6) * 10) / 10;
        samples.push(ms);
        perKind[label].push(ms);
        if (res.status >= 400) {
          failures.push({ request: i, label, status: res.status });
        }
      } catch (err) {
        failures.push({ request: i, label, error: String(err) });
      }
    }
  }

  const startedAt = new Date().toISOString();
  const t0 = Date.now();
  await Promise.all(Array.from({ length: CONCURRENCY }, () => worker()));
  const wallSeconds = (Date.now() - t0) / 1000;

  const overall = summarize(samples);
  const passed =
    failures.length === 0 && samples.length === TOTAL_REQUESTS && overall.p90 <= TARGET_MS;

  const result = {
    requirement: 'OTMS-NF-001 / TC-PERF-01',
    target: 'p90 <= 3000 ms',
    startedAt,
    baseUrl: BASE_URL,
    totalRequests: TOTAL_REQUESTS,
    concurrency: CONCURRENCY,
    completed: samples.length,
    failures: failures.length,
    wallSeconds: Number(wallSeconds.toFixed(1)),
    throughputPerSecond: Number((samples.length / wallSeconds).toFixed(1)),
    overall,
    perEndpoint: Object.fromEntries(
      Object.entries(perKind).map(([label, list]) => [label, summarize(list)]),
    ),
    verdict: passed ? 'PASS' : 'FAIL',
  };

  console.log(`Completed ${samples.length}/${TOTAL_REQUESTS} requests in ${wallSeconds.toFixed(1)}s ` +
    `(${result.throughputPerSecond} req/s)\n`);

  const row = (label, s) =>
    console.log(
      `${label.padEnd(22)} n=${String(s.count).padEnd(4)} p50=${String(s.p50).padEnd(5)} ` +
      `p90=${String(s.p90).padEnd(5)} p95=${String(s.p95).padEnd(5)} p99=${String(s.p99).padEnd(5)} max=${s.max}`,
    );
  row('GET /api/surgeries', result.perEndpoint['GET /api/surgeries']);
  row('GET /api/theatres', result.perEndpoint['GET /api/theatres']);
  row('POST /api/surgeries', result.perEndpoint['POST /api/surgeries']);
  console.log('');
  row('OVERALL', overall);

  if (failures.length) {
    console.log(`\nFailed requests (${failures.length}):`);
    failures.slice(0, 10).forEach((f) =>
      console.log(`  #${f.request} ${f.label} -> ${f.status ?? f.error}`),
    );
    if (failures.length > 10) console.log(`  ... and ${failures.length - 10} more`);
  }

  console.log(`\nVerdict: ${result.verdict} (p90 ${overall.p90} ms vs ${TARGET_MS} ms budget)`);
  if (OUT_FILE) {
    const fs = await import('node:fs');
    const path = await import('node:path');
    fs.mkdirSync(path.dirname(OUT_FILE), { recursive: true });
    fs.writeFileSync(OUT_FILE, JSON.stringify(result, null, 2));
    console.log(`Results written to ${OUT_FILE}`);
  }
  process.exit(passed ? 0 : 1);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
