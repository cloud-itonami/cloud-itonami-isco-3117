# Security Policy

## Reporting Security Issues

If you discover a security vulnerability, please email security@cloud-itonami.org instead of using the GitHub issue tracker. We will work with you to address the issue promptly.

## Security Considerations

This actor is designed with the following security properties:

1. **Immutable Audit Trail**: All operations are logged to an append-only ledger, regardless of outcome. This provides full traceability.

2. **Governor Enforcement**: A separate, independent Governor layer enforces safety constraints. No proposal can bypass the Governor's verdict.

3. **No Direct Writes**: The advisor can only propose; the Store can only be modified through the committed decision path. No direct writes from advisors.

4. **Hard Invariants**: Scope-exclusion ops (extraction, blasting, ore-grading, etc.) are permanently hard-blocked at the governor level — no configuration or override path.

5. **Human-in-the-Loop**: Safety-critical operations (hazard flagging, low-confidence proposals) always escalate to human review before commitment.

## Best Practices

- Always keep dependencies up to date.
- Run the full test suite before deploying.
- Review audit logs regularly.
- Follow principle of least privilege for actor integrations.
