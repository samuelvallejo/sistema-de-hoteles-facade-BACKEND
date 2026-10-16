# Coordinación con el frontend

- Repositorio: https://github.com/samuelvallejo/sistema-de-hoteles-facade-FRONTEND
- Revisión más reciente: 16 de octubre de 2026, rama `main`, commit `8ff474f60b5536617534fe937f88e15a25dadfa0`.
- Resultado: aplicación React/Vite con cliente HTTP que usa `VITE_API_URL` y las seis rutas del taller documentadas aquí. Los campos de consulta y las rutas coinciden; el cliente no llama al endpoint de estado raíz.
- Cambio actual: se añadió `GET /` como comprobación de salud del backend. No modifica el contrato consumido por el frontend.
- El frontend se mantiene en su repositorio independiente; no se modificó su código de aplicación.

Antes de futuras modificaciones del backend:

1. Ejecutar `git status --short --branch` en ambos repositorios.
2. En el frontend ejecutar `git fetch origin`, comprobar la rama real con `git ls-remote --heads origin` y revisar los nuevos commits sin sobrescribir cambios locales.
3. Si aparece código, localizar el cliente HTTP, VITE_API_URL y las pantallas de disponibilidad, reserva y factura. Comparar con el contrato vigente.
4. Mantener los seis paths del taller, el envoltorio message/data y los campos ingleses. Actualizar documentación y pruebas si el cambio es deliberado.
5. Registrar aquí el SHA efectivamente revisado y las diferencias relevantes.

Esto describe una revisión durante el trabajo; no hay un monitor o tarea automática ejecutándose en segundo plano.
