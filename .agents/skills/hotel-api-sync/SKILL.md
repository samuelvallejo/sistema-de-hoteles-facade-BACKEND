---
name: hotel-api-sync
description: Reconcile API changes between the separate hotel Facade backend and React frontend repositories. Use before changing transport fields or integrating changes from sistema-de-hoteles-facade-FRONTEND.
---

# Hotel API synchronization

The backend repository owns `docs/openapi.json` and `docs/api-contract.md`. The frontend repository is https://github.com/samuelvallejo/sistema-de-hoteles-facade-FRONTEND; it is a separate checkout, not a backend subfolder.

Read `docs/frontend-sync.md` for the last inspected revision. Inspect the frontend's remote branch and local status before backend contract work. Fetch remote references when network access is available, and compare with the recorded revision. Preserve uncommitted frontend work; a remote fetch does not require a reset or checkout. An empty repository has no commit to compare: record that fact rather than inventing an API client.

Inspect actual fetch/axios calls, environment configuration and enum handling. Compare URL paths, query names, request body shapes, `message/data` envelopes, error shapes, money, dates and nullable key/invoice fields. Prefer preserving compatibility; document any intentional breaking change and update contract tests with the behavior, not just snapshots of source text.

The six workshop paths cannot be renamed. JSON and query fields use English. The frontend translates enum labels and reads user-visible Spanish messages. The backend is authoritative for quotes and invoice totals. `VITE_API_URL` is the backend origin, without `/api/hotel`.

After a compatible change, update the backend contract, inspect the frontend impact and record its verified revision. Edit or publish the frontend only when the user requests that work. Do not interpret synchronization as an instruction to schedule continuous monitoring or deploy either application.
