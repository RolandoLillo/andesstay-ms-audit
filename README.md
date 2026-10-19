# AndesStay MS Audit

Microservicio de auditoría para la plataforma **AndesStay**. Expone un timeline de eventos de auditoría y un panel de métricas dinámicas para los roles **Auditor** y **Admin**.

## Tecnologías

- Java 17
- Spring Boot 3.4.x
- Spring Data JPA
- PostgreSQL (compartido `andesstay_db`)
- Lombok
- Docker

## Puerto

`8084`

## Endpoints

### Health Check

```
GET /api/audit/health
```

```bash
curl http://localhost:8084/api/audit/health
```

Respuesta:

```json
{
  "status": "UP",
  "service": "andesstay-ms-audit"
}
```

### Timeline de Auditoría

```
GET /api/audit/events?from=&to=&limit=
```

Query params opcionales:
- `from` — ISO-8601 (ej. `2026-10-01T00:00:00Z`)
- `to` — ISO-8601 (ej. `2026-10-31T23:59:59Z`)
- `limit` — default `50`, máximo `200`

```bash
curl "http://localhost:8084/api/audit/events?limit=50"
```

Respuesta:

```json
{
  "items": [
    {
      "id": 1,
      "occurredAt": "2026-10-18T21:00:00Z",
      "actor": "operador@andesstay.local",
      "action": "RESERVATION_STATUS_CHANGED",
      "resourceType": "RESERVATION",
      "resourceId": "42",
      "detail": "CREADA → CONFIRMADA",
      "severity": "INFO"
    }
  ]
}
```

### Métricas del Panel de Auditoría

```
GET /api/audit/metrics
```

```bash
curl http://localhost:8084/api/audit/metrics
```

Respuesta:

```json
{
  "asOf": "2026-10-18T21:30:00Z",
  "occupationPercent": 84.0,
  "reservationsToday": 26,
  "checkInsToday": 9,
  "incidentsOpen": 3,
  "incidentsNeedReview": 1,
  "completenessPercent": 96.0,
  "availabilityPercent": 95.0,
  "paymentsConfirmed": 22,
  "cancellations": 2,
  "risks": [
    { "label": "Pago pendiente", "status": "22 casos", "severity": "INFO" },
    { "label": "Cancelaciones", "status": "2 casos", "severity": "WARN" },
    { "label": "Incidencias abiertas", "status": "3 casos", "severity": "WARN" },
    { "label": "Servicio de lavandería", "status": "Sin novedad", "severity": "INFO" }
  ]
}
```

## Fórmulas de Métricas

Las métricas se calculan consultando las tablas `reservations` y `audit_event` en la BD compartida `andesstay_db`.

| Métrica | Fórmula |
|---------|---------|
| `reservationsToday` | `COUNT(*) FROM reservations WHERE check_in_date = CURRENT_DATE` |
| `checkInsToday` | `COUNT(*) FROM reservations WHERE status = 'EN_ESTADIA'` |
| `paymentsConfirmed` | `COUNT(*) FROM reservations WHERE status = 'CONFIRMADA'` |
| `cancellations` | `COUNT(*) FROM reservations WHERE status = 'CANCELADA'` |
| `completenessPercent` | `(COUNT(*) WHERE customer_user_id, unit_id>0, check_in_date, check_out_date, amount NOT NULL / COUNT(*) FROM reservations) * 100` |
| `occupationPercent` | `((paymentsConfirmed + checkInsToday) / COUNT(*) FROM reservations) * 100` |
| `availabilityPercent` | `100 - occupationPercent` |
| `incidentsOpen` | `COUNT(*) FROM audit_event WHERE severity IN ('WARN','ERROR') AND status = 'OPEN'` |
| `incidentsNeedReview` | `COUNT(*) FROM audit_event WHERE severity IN ('WARN','ERROR') AND status = 'NEED_REVIEW'` |

Los porcentajes se redondean a 1 decimal.

## Seed de Datos

El archivo `src/main/resources/data.sql` inserta 20 registros iniciales en `audit_event` al arrancar el servicio (solo si la tabla está vacía).

## Ejecución Local

```bash
./mvnw spring-boot:run
```

Con Docker:

```bash
docker build -t andesstay/ms-audit:latest .
docker run -p 8084:8084 -e DB_URL=jdbc:postgresql://host:5432/andesstay_db -e DB_USERNAME=postgres -e DB_PASSWORD=postgres andesstay/ms-audit:latest
```

## Autorización (BFF)

El BFF (`andesstay-ms-bff`) debe enrutar `GET /api/audit/**` hacia `http://ms-audit:8084/api/audit/**` con la regla de seguridad: **únicamente roles `Auditor` y `Admin`**. Cualquier token con rol `Cliente` u `Operador` debe recibir `403 Forbidden`.

Ejemplo con token de Auditor:

```bash
curl -H "Authorization: Bearer <token-auditor>" http://localhost:8080/api/audit/metrics
```

Respuesta: `200 OK`.

Ejemplo con token de Cliente:

```bash
curl -H "Authorization: Bearer <token-cliente>" http://localhost:8080/api/audit/metrics
```

Respuesta: `403 Forbidden`.
