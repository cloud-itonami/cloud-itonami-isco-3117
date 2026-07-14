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
clojure -M:test              # Run test suite
git push origin main         # CI runs tests, type checks
```

## License

AGPL-3.0-or-later. See LICENSE.
