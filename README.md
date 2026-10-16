# Sistema de Reservas de Hotel — Backend

Backend académico con Spring Boot 3.5.16, Java 17+ y Maven. Implementa el patrón Facade del PDF suministrado: un controlador REST habla exclusivamente con HotelFacade y este coordina cinco subsistemas independientes. El estado se conserva únicamente en memoria.

Repositorio backend: https://github.com/samuelvallejo/sistema-de-hoteles-facade-BACKEND

Repositorio frontend: https://github.com/samuelvallejo/sistema-de-hoteles-facade-FRONTEND

Esta entrega implementa el backend. El frontend está pendiente. No se ha desplegado en Railway ni Vercel; las instrucciones de despliegue se incluyen para la siguiente etapa.

## Estructura

```text
.agents/skills/
  hotel-backend-facade/SKILL.md
  hotel-api-sync/SKILL.md
.mvn/wrapper/maven-wrapper.properties
docs/
  api-contract.md
  openapi.json
  requests.http
  requirements.md
  frontend-sync.md
  validation.md
src/main/java/com/hotel/
  HotelApplication.java
  config/          WebConfig
  controller/      HotelController
  dto/             Request/response DTOs and response envelopes
  exception/       HotelException, GlobalExceptionHandler
  facade/          HotelFacade
  factory/         RoomFactory, AdditionalServiceFactory
  model/           Immutable domain records and enums, Invoice.Builder
  service/         Five subsystems and StayRules
  strategy/        PricingStrategy, HighSeasonPricingStrategy, LowSeasonPricingStrategy
src/main/resources/application.properties
src/test/AGENTS.md
src/test/java/com/hotel/
  HotelApiTest.java
  ConcurrencyTest.java
  ArchitectureTest.java
  RateServiceTest.java
AGENTS.md
Dockerfile
.dockerignore
.env.example
pom.xml
mvnw
mvnw.cmd
```

## Ejecutar localmente

Instalar JDK 17 o superior y configurar JAVA_HOME. No hace falta instalar Maven: el wrapper lo descarga. La primera compilación requiere conexión a Maven Central.

PowerShell, desde la raíz de este repositorio:

```powershell
.\mvnw.cmd -B verify
$env:PORT = '8080'
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173'
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
chmod +x mvnw
./mvnw -B verify
PORT=8080 CORS_ALLOWED_ORIGINS=http://localhost:5173 ./mvnw spring-boot:run
```

La API estará en `http://localhost:8080/api/hotel`. También se puede ejecutar el empaquetado con `java -jar target/hotel-facade-1.0.0.jar`. `.env.example` documenta las variables; Spring Boot no carga un archivo `.env` por sí mismo. Exportar las variables en el shell o configurarlas en el proveedor.

Para varios orígenes CORS, separar orígenes exactos por comas, sin barra final. Por ejemplo: `http://localhost:5173,https://hotel.example.com`. No se acepta `*`.

Docker, si se tiene instalado:

```bash
docker build -t hotel-facade .
docker run --rm -p 8080:8080 -e PORT=8080 -e CORS_ALLOWED_ORIGINS=http://localhost:5173 hotel-facade
```

## Ejemplos de los seis endpoints

Los siguientes comandos usan curl con sintaxis Bash. En PowerShell se puede usar `curl.exe`, colocar los JSON en archivos y enviarlos con `--data-binary @archivo.json`. También se pueden ejecutar los ejemplos de `docs/requests.http` en un cliente HTTP compatible. Reemplazar el UUID de ejemplo por el `data.id` recibido al crear la reserva.

Consultar disponibilidad y cotización de tres noches en temporada alta:

```bash
curl 'http://localhost:8080/api/hotel/disponibilidad?checkInDate=2030-06-01&checkOutDate=2030-06-04&roomType=SINGLE&season=HIGH'
```

Crear reserva:

```bash
curl -i -X POST http://localhost:8080/api/hotel/reservar \
  -H 'Content-Type: application/json' \
  -d '{"roomId":1,"checkInDate":"2030-06-01","checkOutDate":"2030-06-04","season":"HIGH","guest":{"name":"Ana Pérez","document":"123456","email":"ana@example.com","phone":"+57 3001234567"}}'
```

Agregar desayuno (15 USD por noche):

```bash
curl -X POST http://localhost:8080/api/hotel/servicios/00000000-0000-0000-0000-000000000000 \
  -H 'Content-Type: application/json' -d '{"serviceType":"BREAKFAST"}'
```

Hacer check-in, obtener llave UUID y consultar reserva:

```bash
curl -X PUT http://localhost:8080/api/hotel/checkin/00000000-0000-0000-0000-000000000000
curl http://localhost:8080/api/hotel/reserva/00000000-0000-0000-0000-000000000000
```

Hacer check-out y obtener la factura final en `data.invoice`:

```bash
curl -X PUT http://localhost:8080/api/hotel/checkout/00000000-0000-0000-0000-000000000000
```

Para SINGLE por tres noches HIGH: base 300 + recargo 150 = 450 USD. Con SPA (50), BREAKFAST (45) y TRANSFER (30), la factura es 575 USD. La factura permanece consultable después del checkout; la llave se revoca y queda en null.

Cada éxito devuelve `message` y `data`. Los errores devuelven `timestamp`, `status`, `code`, `message`, `path` y `errors`. Consultar [contrato completo](docs/api-contract.md) y [OpenAPI](docs/openapi.json) para campos, valores nulos y estados HTTP.

## Arquitectura y patrones

```text
React frontend (separate repository)
              |
        HotelController
              |
         HotelFacade
              |
  +-----------+-----------+----------------+------------+
  |           |           |                |            |
RoomService RateService AdditionalServiceService BillingService AccessService
  |           |           |                |
RoomFactory PricingStrategy AdditionalServiceFactory Invoice.Builder
              |
      HighSeason / LowSeason
```

- **Facade:** HotelFacade es el punto de entrada del controlador y coordina reserva, extras, check-in y check-out. Los subsistemas no se llaman entre sí.
- **Strategy:** HighSeasonPricingStrategy y LowSeasonPricingStrategy calculan la tarifa nocturna, seleccionadas por RateService.
- **Factory:** RoomFactory crea el inventario de 6 sencillas, 6 dobles y 3 suites; AdditionalServiceFactory construye extras con precios y cantidades.
- **Builder:** Invoice.Builder construye las líneas de alojamiento, recargo y extras, y calcula el subtotal y total.

Las operaciones de la fachada se sincronizan para proteger las transacciones compuestas en memoria. Se usan snapshots inmutables y DTOs separados. La documentación de [decisiones del dominio](docs/requirements.md) explica temporadas, moneda, duplicados, fechas y alcance de la simulación.

## Pruebas

`./mvnw -B verify` o `.\mvnw.cmd -B verify` ejecuta pruebas del flujo REST completo, validaciones y errores en español, filtros, disponibilidad y solapamientos, estados, llaves, factura, servicios, CORS, tarifas de los tres tipos en ambas temporadas, concurrencia y límites arquitectónicos con ArchUnit. Los reportes están en `target/surefire-reports`. No requieren servicios externos.

Resultado de la entrega: 16 pruebas aprobadas y los seis endpoints comprobados con el JAR en ejecución. Detalles y límites en [registro de validación](docs/validation.md).

## Coordinación con el frontend y agentes

El frontend estaba vacío al inspeccionarlo. La disponibilidad ya incluye el precio calculado por el servidor para la vista previa. React debe configurar `VITE_API_URL=http://localhost:8080`, traducir enums y leer `data` de las respuestas. Consultar [registro de sincronización](docs/frontend-sync.md) antes de alterar el contrato.

`AGENTS.md` y `src/test/AGENTS.md` fijan el alcance, arquitectura, idiomas y verificaciones. Las skills versionadas en `.agents/skills` cubren implementación de backend y coordinación de API; están incluidas en este repositorio para futuras sesiones.

## Despliegue futuro: Railway y Vercel

Preparación documentada; no se ha ejecutado ningún despliegue.

Backend en Railway:

1. Cuando se autorice desplegar, crear un proyecto y conectar este repositorio GitHub a un servicio.
2. Seleccionar la raíz del repositorio y el Dockerfile incluido. Su etapa de compilación ejecuta `mvn -B verify` y la imagen final ejecuta el JAR con un usuario sin privilegios.
3. Mantener una sola réplica. Railway proporciona PORT; la aplicación lo lee directamente.
4. Configurar CORS_ALLOWED_ORIGINS con el origen HTTPS exacto del frontend Vercel. Puede incluir localhost separado por coma durante pruebas.
5. Generar el dominio público del backend y comprobar GET `/api/hotel/disponibilidad?checkInDate=2030-01-01&checkOutDate=2030-01-02` (200 y 15 habitaciones tras un arranque limpio).

Frontend en Vercel, una vez implementado en su propio repositorio:

1. Importar sistema-de-hoteles-facade-FRONTEND y seleccionar el preset Vite.
2. Usar `npm ci` si existe package-lock.json, build `npm run build`, salida `dist` y raíz del repositorio frontend.
3. Configurar VITE_API_URL con el origen HTTPS del backend Railway, sin `/api/hotel` ni barra final. Al ser Vite, cambiar esta variable requiere reconstruir el frontend.
4. Publicar el frontend y registrar su origen final en CORS_ALLOWED_ORIGINS del backend.
5. Probar desde el navegador disponibilidad → reserva → extras → check-in → check-out → factura. Si se añade React Router, configurar el fallback de SPA en el repositorio frontend.

El taller requiere memoria: reiniciar o redesplegar elimina reservas y facturas. Cada réplica tendría su propio estado; por eso se usa una instancia. La llave es simulada, no existe procesamiento de pagos ni autenticación de usuarios y este backend académico no constituye un sistema comercial de reservas.

Referencia de compatibilidad de Java/Maven: [documentación oficial Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html).
