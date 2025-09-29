# SLOs & SLIs


## Objectives
- API P95 latency < 200 ms (reads)
- Error rate < 0.1%
- MTTR < 30 min


## Indicators (examples)
- `http_server_request_duration_seconds` → P95 (PromQL)
- Error ratio of 5xx over total
- DLQ depth for messaging


## Alert Hints
- P95 above threshold for 5 min
- Error rate > 0.1% for 5 min
- DLQ > 0
