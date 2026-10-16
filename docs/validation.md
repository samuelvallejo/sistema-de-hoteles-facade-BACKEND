# Registro de validación

Validación realizada el 16 de octubre de 2026 en Windows, con JDK 21.0.11, Maven Wrapper 3.9.11 y compilación `release 17`.

- `mvnw.cmd -B verify`: BUILD SUCCESS. 16 pruebas, cero fallos, cero errores, cero omitidas.
- ArchitectureTest: 2 pruebas de límites controlador/fachada y subsistemas independientes.
- ConcurrencyTest: 20 intentos concurrentes por la misma habitación y fechas; exactamente uno obtiene la reserva.
- HotelApiTest: 7 pruebas de flujo HTTP/JSON, factura, estado, disponibilidad, fechas, validaciones, servicios y CORS.
- RateServiceTest: 6 combinaciones de tipo de habitación y temporada.
- JAR arrancado con PORT=18080 y CORS_ALLOWED_ORIGINS=http://localhost:5174: puerto y origen configurables comprobados.
- Prueba HTTP real sobre los seis endpoints: inventario inicial de 15 habitaciones; reserva HIGH por tres noches, tres extras, check-in con UUID, checkout con factura de 575 USD, llave revocada y habitación liberada.
- Solicitud de checkout antes del check-in: 409 con el formato de error documentado.
- OpenAPI 3.1 validado con openapi-spec-validator. Las respuestas HTTP reales se validaron con JSON Schema contra la especificación.
- Skills hotel-backend-facade, hotel-api-sync y hotel-frontend-integration: aprobadas por quick_validate.py de skill-creator.

El proceso local de la prueba HTTP fue detenido al finalizar. Dockerfile preparado, sin construir la imagen porque Docker no está instalado en este entorno. No se ejecutaron despliegues ni se verificó una interfaz frontend, que aún no está implementada.
