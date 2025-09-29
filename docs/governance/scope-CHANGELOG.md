# Scope CHANGELOG

## 2025-09-29 — Bank attribution + multi-currency balances
**Summary**  
- Add bank identifier on every transaction (FR-10).
- Maintain running balances in EUR and COP (FR-11).

**Impacts**
- Contracts: +`bankId` in Transaction resource (backward-compatible).
- Data: new `bank` table and `bank_id` FK in `transaction`.
- Security/Privacy: no new PII; ensure bank names are tenant-scoped.
- Operations: FX cache must cover USD/EUR/COP by date.

**Actions**
- ADR-005 created to justify schema & projection approach.
- Migrations added (V5__bank_and_tx_fk.sql).
- OpenAPI updated: `/v1/transactions` includes `bankId`.
