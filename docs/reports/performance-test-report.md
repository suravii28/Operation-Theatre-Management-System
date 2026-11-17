# OTMS Performance Test Report

## TC-Perf-01: API Performance

Tool: Grafana k6 v2.4.0
Endpoint: GET /api/surgeries
Concurrent virtual users: 10
Test duration: 60 seconds

| Metric | Result |
|---|---:|
| Total HTTP requests | 591 |
| Successful checks | 590/590 |
| Failed HTTP requests | 0% |
| Average response time | 16.95 ms |
| Median response time | 13.33 ms |
| P90 response time | 29.54 ms |
| P95 response time | 37.15 ms |
| Maximum response time | 257.39 ms |

### Acceptance criteria

- P90 response time <= 3000 ms: PASS (29.54 ms).
- Successful checks > 90%: PASS (100%).
- HTTP request failure rate: 0%.

## TC-Conc-01: Concurrent Booking

Endpoint: POST /api/surgeries
Tool: Grafana k6 v2.4.0
Virtual users: 2
Scenario: Two requests for the same theatre, surgeon and time slot.

### Initial test result

| Virtual user | HTTP status | Result |
|---|---:|---|
| VU 1 | 201 Created | Booking accepted |
| VU 2 | 409 Conflict | Booking rejected |

Result: PASS for the initial test. Exactly one conflicting booking was accepted.

### Repeatability note

A subsequent run returned HTTP 409 for both requests because the earlier successful booking remained in the database. The test slot must be reset or changed before repeating the concurrency test.

## Scope and limitations

The performance test measured the authenticated surgery-list endpoint only. It does not establish performance characteristics for every API endpoint.

The concurrency test provides evidence for the tested conflicting booking scenario. Further repeated tests with clean test data are recommended.

## Conclusion

Both acceptance criteria were met in the recorded initial test runs:
- TC-Perf-01: PASS.
- TC-Conc-01: PASS (initial run).
