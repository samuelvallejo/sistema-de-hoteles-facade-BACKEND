---
name: hotel-backend-facade
description: Implement and maintain the Spring Boot backend for the hotel Facade workshop, including availability, prices, services, check-in and invoices. Use for changes in sistema-de-hoteles-facade-BACKEND.
---

# Hotel backend Facade

Read `docs/requirements.md` from the repository root for domain decisions and `docs/api-contract.md` for transport fields. Inspect the existing facade and affected tests before editing.

Keep the REST controller dependent only on HotelFacade. Implement coordination in the facade and specialized behavior in the five independent subsystems. The supplied PDF describes Facade as a simplified interface over independent subsystems; naming a class Facade without enforcing this dependency direction does not satisfy the workshop.

Preserve `[checkInDate, checkOutDate)` bookings, monetary arithmetic in BigDecimal, server-calculated totals, and the three reservation states. Creating a reservation must atomically check and claim availability. Checkout must generate the final invoice, revoke access and release the room under the same coordination lock. Do not return mutable collections from in-memory state.

Maintain HIGH x1.5 / LOW x1 rates through the two pricing strategies. Factories create rooms and priced service lines. The invoice builder records base nights, high-season surcharge and every selected service; subtotal equals total because no taxes or discounts are specified.

For errors distinguish malformed data (400), absent resources (404), and unavailable rooms or invalid lifecycle operations (409). Keep API and invoice messages Spanish, code identifiers English, and preserve the workshop routes.

Run the Maven wrapper with `-B verify`. When changing lifecycle or availability, verify the complete HTTP flow and the competing-reservations test. When changing dependencies between packages, run the architecture tests. Update the contract and domain decisions if behavior intentionally changes.

Prepare configuration using PORT and CORS_ALLOWED_ORIGINS without deploying. GitHub publication is authorized for this backend; Railway/Vercel deployment requires a later user request.
