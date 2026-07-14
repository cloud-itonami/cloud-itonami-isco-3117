# Governance

This project is part of the cloud-itonami initiative and follows the governance structure of the parent organization.

## Decision Making

- Core design decisions are made by the project maintainers.
- Significant changes (API changes, scope revisions, new features) are discussed in issues or pull requests before implementation.
- All changes must pass the test suite and code review.

## Scope Boundaries

This actor supports mining and metallurgical technicians in field data collection and inspection workflows. The following are **permanently outside this actor's scope**:

- Extraction or blasting decisions
- Ore-grade assessment for mining targeting
- Production quota or target-setting
- Mine-safety authority determinations
- Equipment operation sequencing

Any proposal attempting to move these decisions into this actor will be rejected at the governor level (hard block, no override path).

## Releasing

Releases follow semantic versioning. New versions are published via GitHub Releases.
