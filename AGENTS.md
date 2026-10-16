# Hotel reservation backend

## Scope and source of truth

- Work in this repository for Java/Spring Boot changes. Remote: https://github.com/samuelvallejo/sistema-de-hoteles-facade-BACKEND.
- The separate React/Vite repository is https://github.com/samuelvallejo/sistema-de-hoteles-facade-FRONTEND. Inspect its current branch and changes before changing the API; do not put frontend application code here.
- Read `docs/requirements.md` for the decisions derived from the supplied Facade PDF and workshop. Read `docs/api-contract.md` and `docs/openapi.json` before changing requests or responses.
- Implement backend now. The user authorized publishing this backend to its GitHub repository. Deployment to Railway and Vercel is deferred until the user explicitly requests it. A push to GitHub is not authorization to deploy.

## Architecture and language

- Java 17+, Maven and Spring Boot; state lives only in Java collections. No database, JPA, external cache or persistence layer.
- `HotelController` calls only `HotelFacade`. The facade coordinates RoomService, RateService, AdditionalServiceService, BillingService and AccessService; subsystems must not call one another or the facade.
- Preserve constructor injection, immutable domain snapshots, separate transport DTOs, BigDecimal money, half-open date ranges and synchronized multi-step mutations.
- Source identifiers, filenames and code comments are English. User-visible messages, invoice descriptions and validation messages are Spanish. API enums remain English. The six workshop URL paths remain Spanish exactly as specified.
- Never accept a client price or total as authoritative. Recalculate in the backend. A final invoice must remain stable after checkout.

## Project skills

- Use `.agents/skills/hotel-backend-facade/SKILL.md` for domain or backend implementation work.
- Use `.agents/skills/hotel-api-sync/SKILL.md` when consuming frontend changes or changing the API contract. These skills are repository-scoped; they do not require global installation.

## Verification and handoff

- Windows: `./mvnw.cmd -B verify`; Linux/macOS: `./mvnw -B verify`. Java 17 or newer is required. The wrapper downloads Maven on first use.
- Run the API lifecycle and concurrency tests when changing booking behavior; preserve the ArchUnit checks for the Facade boundary.
- Update contract docs and examples with observable API changes; coordinate compatibility with the separate frontend. Record the inspected frontend revision in `docs/frontend-sync.md`.
- Keep secrets, `.env`, local build outputs and runtime logs out of Git. `.env.example` contains placeholders only.
- Explain that restarting loses all reservations, that multiple instances have separate state, and that this academic API does not implement authentication. Do not claim production deployment or production security.
