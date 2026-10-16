# Coordinación con el frontend

- Repositorio: https://github.com/samuelvallejo/sistema-de-hoteles-facade-FRONTEND
- Inspección inicial: 16 de octubre de 2026.
- Resultado: repositorio vacío, sin commits ni cliente HTTP existente. No existe SHA inicial que registrar.
- Contrato propuesto e implementado: `docs/api-contract.md` y `docs/openapi.json` de este backend.
- Se prepararon instrucciones y una skill de integración en una copia local separada del repositorio frontend. No se implementó la interfaz ni se publicó contenido del frontend.

Antes de futuras modificaciones del backend:

1. Ejecutar `git status --short --branch` en ambos repositorios.
2. En el frontend ejecutar `git fetch origin`, comprobar la rama real con `git ls-remote --heads origin` y revisar los nuevos commits sin sobrescribir cambios locales.
3. Si aparece código, localizar el cliente HTTP, VITE_API_URL y las pantallas de disponibilidad, reserva y factura. Comparar con el contrato vigente.
4. Mantener los seis paths del taller, el envoltorio message/data y los campos ingleses. Actualizar documentación y pruebas si el cambio es deliberado.
5. Registrar aquí el SHA efectivamente revisado y las diferencias relevantes.

Esto describe una revisión durante el trabajo; no hay un monitor o tarea automática ejecutándose en segundo plano.
