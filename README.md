# cloud-itonami-isco-3117

Mining and Metallurgical Technicians (ISCO-08 3117) — independent actor for field data collection, lab testing, and inspection support.

## Scope

This actor supports **mining and metallurgical technicians** in field data collection, sample testing, and inspection workflows:

- **✓ Allowed**: ore/metal sample test and assay data recording, inspection data logging, site visit scheduling, safety concern flagging (human-escalated)
- **✗ Excluded**: extraction/sequencing decisions, blasting authorization, ore-grade assessment for targeting, production quotas, mine-safety authority determinations

## Architecture

Built as a `langgraph-clj` StateGraph actor per ADR-2607011000. One graph run = one field operation request:

```
:intake → :advise → :govern → :decide → :commit (if ok)
                                        → :request-approval (if escalate, human interrupt)
                                        → :hold (if hard violation)
```

### Components

- **Governor** (`mining_metallurgy.governor`): Independent safety/scope layer — enforces hard invariants (project provenance, no direct writes, no operator-class ops) and escalation rules (safety hazards, low confidence).
- **Advisor** (`mining_metallurgy.advisor`): Proposals for test records, inspections, site visits. Mock (deterministic) by default; swappable LLM-backed.
- **Store** (`mining_metallurgy.store`): Protocol-based SSoT for projects, records, audit ledger. MemStore (in-memory) by default.
- **Actor** (`mining_metallurgy.actor`): StateGraph orchestration with checkpoint/resume for human approvals.

### Test & Deploy

```bash
kbb --backend sci test/run_suite.cljk          # Run the suite
kbb --backend sci test/run_suite.cljk --lint   # ... and lint the same sources
```

The suite is **10 tests / 41 assertions**. That sentence is not decoration:
`test/run_suite.cljk` reads it and refuses (exit 2) any run that comes in
under it, so the count cannot go stale without turning the run red.

`kbb -M:test` no longer runs this suite and refuses rather than
answering. The sources are `.kotoba`, which `clojure.tools.namespace` does
not scan, so between 2026-09-10 and 2026-09-11 that command reported
`Ran 0 tests containing 0 assertions. 0 failures, 0 errors.` and exited 0 —
the same value it gave for 8 green tests the commit before. See the header of
`test/run_suite.cljk`.

## License

AGPL-3.0-or-later. See LICENSE.
