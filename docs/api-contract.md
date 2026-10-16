# Contrato HTTP v1

Base local: `http://localhost:8080`. `Content-Type: application/json` para POST. Fechas ISO `YYYY-MM-DD`; UUID como texto; valores monetarios JSON numéricos en USD con precisión de dos decimales. Las rutas son las del taller y los campos JSON están en inglés. Especificación completa: `openapi.json`.

Éxito: `{ "message": "Mensaje en español.", "data": ... }`.

Error: `{ "timestamp": "2030-01-01T00:00:00Z", "status": 400, "code": "VALIDATION_ERROR", "message": "Revisa los datos del formulario.", "path": "/api/hotel/reservar", "errors": { "guest.email": "El correo no es válido." } }`.

| Método | Ruta | Entrada | Resultado |
| --- | --- | --- | --- |
| GET | / | Sin parámetros | 302, redirección al frontend (`FRONTEND_URL`) |
| GET | /health | Sin parámetros | 200, estado del servicio (`status: UP`) |
| GET | /api/hotel/disponibilidad | query checkInDate, checkOutDate, roomType opcional, season opcional (LOW por defecto) | 200, lista de {room, rate} |
| POST | /api/hotel/reservar | roomId, checkInDate, checkOutDate, season, guest | 201, reserva; cabecera Location |
| POST | /api/hotel/servicios/{reservationId} | {serviceType} | 200, reserva actualizada |
| PUT | /api/hotel/checkin/{reservationId} | Sin cuerpo | 200, reserva con digitalKey |
| PUT | /api/hotel/checkout/{reservationId} | Sin cuerpo | 200, reserva con invoice y digitalKey null |
| GET | /api/hotel/reserva/{reservationId} | Sin cuerpo | 200, reserva actual |

`guest` requiere name, document, email y phone. `roomId` es el ID devuelto por disponibilidad, no el número visible de la habitación. No se envía un precio desde el cliente.

Enums: roomType = SINGLE / DOUBLE / SUITE; season = HIGH / LOW; serviceType = SPA / BREAKFAST / TRANSFER; status = CONFIRMED / CHECKED_IN / CHECKED_OUT. Distinguen mayúsculas/minúsculas. El frontend muestra Sencilla/Doble/Suite, Alta/Baja, Spa/Desayuno/Traslado y sus etiquetas de estado en español.

La reserva contiene id, guest, room, checkInDate, checkOutDate, rate, status, services, servicesTotal, total, digitalKey e invoice. `digitalKey` es null hasta check-in y vuelve a null al checkout. `invoice` es null hasta checkout. `services` comienza vacío. `total` siempre incluye alojamiento y extras actuales.

`room` contiene id, number, type y baseRate. `rate` contiene nights, season, baseNightlyRate, nightlyRate, baseTotal, seasonSurcharge y total (solo alojamiento). En disponibilidad ya se devuelve esa cotización para que la interfaz no replique el cálculo.

Cada extra contiene type, description, quantity, unitPrice y total. La factura contiene id, reservationId, issuedAt (UTC), currency (USD), lines, subtotal y total. Cada línea contiene description en español, quantity, unitPrice y total. El subtotal incluye el recargo y extras; al no existir impuestos, total = subtotal.

400: JSON, enum, UUID, fecha o validación incorrectos. 404: habitación o reserva inexistente. 409: habitación ocupada en el intervalo, transición inválida o servicio duplicado. Los errores del framework también usan el formato documentado. Una solicitud CORS denegada la rechaza el navegador; el cliente no debe depender de un cuerpo JSON en ese caso.

Integración React/Vite:

```javascript
const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const response = await fetch(`${apiUrl}/api/hotel/disponibilidad?${new URLSearchParams({
  checkInDate: '2030-06-01', checkOutDate: '2030-06-04', roomType: 'SINGLE', season: 'HIGH'
})}`);
const payload = await response.json();
if (!response.ok) throw new Error(payload.message);
const availableRooms = payload.data;
```

VITE_API_URL debe ser el origen del backend sin `/api/hotel` ni barra final. No hay cookies ni autenticación. No usar mode no-cors. Ante 409 al reservar, volver a consultar disponibilidad; un resultado previo no bloquea inventario.
