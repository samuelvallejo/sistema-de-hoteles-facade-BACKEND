# Backend test guidance

- Exercise observable domain behavior: both seasons and all room types; date overlaps and adjoining stays; Spanish validation errors; check-in/out state transitions; service amounts; immutable invoice; key revocation and room release.
- Use MockMvc for the real HTTP/JSON contract and ArchUnit for controller/facade and subsystem boundaries.
- Concurrency tests must coordinate competing calls with a latch and bounded waits; assert exactly one reservation wins for the same room and stay.
- Isolate dates and room IDs between tests. Tests must not require GitHub, Railway, Vercel, a database or external network access at runtime.
