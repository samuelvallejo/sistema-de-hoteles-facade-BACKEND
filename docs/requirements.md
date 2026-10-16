# Requisitos y decisiones del taller

Fuente: PDF «Patrón Facade» suministrado por el usuario, páginas 4–5 (interfaz simplificada y subsistemas independientes), 17–20 (caso de hotel), cuatro capturas y texto de requisitos adjunto. El alcance actual solicitado es backend más AGENTS.md/skills; el despliegue queda para una instrucción posterior. La publicación del backend en GitHub sí está autorizada.

| Requisito | Implementación |
| --- | --- |
| Spring Boot, Java 17+, Maven, memoria | pom.xml, colecciones Java; sin base de datos |
| 15 habitaciones | IDs 1–6 SINGLE, 7–12 DOUBLE, 13–15 SUITE; números 101–115 |
| Disponibilidad | Intervalos semiabiertos: entrada incluida, salida excluida; permite estancias contiguas |
| Tarifas | Base por noche: SINGLE 100 USD, DOUBLE 150 USD, SUITE 250 USD |
| Temporada | HIGH multiplica por 1.5; LOW mantiene la base. El cliente selecciona temporada; no se definió calendario |
| Extras | SPA 50 por estancia; BREAKFAST 15 por noche; TRANSFER 30 por estancia |
| Duplicados | Cada extra puede agregarse una vez por reserva; duplicarlo responde 409 |
| Estado | CONFIRMED → CHECKED_IN → CHECKED_OUT; repetir transiciones responde 409 |
| Acceso | UUID emitido al check-in, revocado al check-out; nunca se entrega llave antes del check-in |
| Factura | Base por noche, recargo por noche en alta, cada extra, subtotal y total; sin impuestos ni descuentos inventados |
| Concurrencia | Coordinación sincronizada en HotelFacade y protección del inventario en RoomService |
| Errores | 400 validación, 404 recurso inexistente, 409 conflicto; mensaje en español |
| CORS/puerto | CORS_ALLOWED_ORIGINS configurable; PORT con valor local 8080 |
| Frontend | Repositorio independiente; contrato explícito con cotización en disponibilidad |

La moneda USD se adopta por los importes con signo `$` del taller; no se simula conversión. El desayuno se cobra por noche, equivalente a una mañana por noche reservada. No se cobran penalidades, estancia extra ni late checkout: no se especificaron reglas o importes. El total de checkout incorpora los extras seleccionados.

Para facilitar la demostración, el check-in no se restringe al día real del sistema y las fechas históricas coherentes son válidas. Se factura la duración reservada y el checkout libera la habitación para nuevas reservas, conservando el historial y la factura. Esta simulación no modela ocupación física por reloj ni cancelaciones.

Los identificadores y el código están en inglés. Los mensajes, validaciones y descripciones de factura están en español. Las rutas del PDF se conservan exactamente; el nombre del parámetro interno `reservationId` representa el `{reservaId}` del documento sin modificar la URL real.

Persistencia académica: reiniciar borra todo y recrea el inventario inicial. Una sola instancia debe atender todas las solicitudes. No hay autenticación, cobro bancario ni llaves reales.
