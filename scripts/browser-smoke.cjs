const fs = require('node:fs');
const { chromium } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright');
const url = process.env.E2E_URL || 'http://127.0.0.1:5173';
const output = 'docs/reports/browser-e2e-latest/results.json';

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({ acceptDownloads: true });
  page.setDefaultTimeout(15000);
  let passed = 0;
  try {
    await page.goto(url + '/login');
    await page.getByLabel('Email').fill(process.env.E2E_EMAIL);
    await page.getByLabel('Password').fill(process.env.E2E_PASSWORD);
    await page.getByRole('button', { name: 'Sign in' }).click();
    await page.getByRole('heading', { name: 'Dashboard' }).waitFor(); passed++;

    await page.getByRole('link', { name: 'Surgeries' }).click();
    await page.getByRole('heading', { name: 'Surgeries' }).waitFor(); passed++;

    await page.getByRole('link', { name: 'Emergencies' }).click();
    await page.getByRole('heading', { name: 'Emergencies' }).waitFor();
    await page.getByRole('button', { name: 'Impact' }).first().click();
    await page.getByRole('heading', { name: 'Impact analysis' }).waitFor();
    await page.getByText('Mohan Verma').first().waitFor(); passed++;

    await page.getByRole('link', { name: 'Reports' }).click();
    await page.getByRole('heading', { name: 'Reports' }).waitFor();
    const [csv] = await Promise.all([
      page.waitForEvent('download'),
      page.getByRole('button', { name: 'Export surgery list CSV' }).click(),
    ]);
    if (!csv.suggestedFilename().endsWith('.csv')) throw new Error('No CSV export');
    passed++;
    fs.mkdirSync('docs/reports/browser-e2e-latest', { recursive: true });
    fs.writeFileSync(output, JSON.stringify({ passed, failed: 0, verdict: 'PASS' }, null, 2));
    console.log('Browser smoke PASS: ' + passed + ' checks');
  } catch (err) {
    fs.mkdirSync('docs/reports/browser-e2e-latest', { recursive: true });
    await page.screenshot({ path: 'docs/reports/browser-e2e-latest/failure.png' }).catch(() => {});
    fs.writeFileSync(output, JSON.stringify({ passed, failed: 1, verdict: 'FAIL', error: String(err) }, null, 2));
    console.error(err);
    process.exitCode = 1;
  } finally { await browser.close(); }
})().catch(err => { console.error(err); process.exitCode = 1; });
