# ADR-005: Bank attribution & multi-currency balances

## Status
Accepted — 2025-09-29

## Context
We must (a) identify originating bank per transaction and (b) expose balances in account currency plus EUR and COP using historical FX.

## Decision
- Introduce `bank` entity (tenant-scoped) and add `bank_id` to `transaction`.
- Compute balances on read using historical FX at txn date; cache converted day rates.
- Keep projections simple (SQL view + app-layer hydration); avoid precomputed snapshots initially.

## Consequences
- Adds one FK and indexes (`transaction(tenant_id, bank_id, booking_date)`).
- Read cost increases slightly; mitigated by FX/day cache.
- Future snapshotting remains possible without breaking API.
