# Switchmate Backend

Spring Boot 3.5 / Java 21 API backed by PostgreSQL. Flyway manages the schema; Hibernate validates it at startup.

## Run Locally

Start PostgreSQL from the repository root:

```powershell
docker compose up -d postgres
```

Set the required environment variables in PowerShell, then launch the API:

```powershell
$env:POSTGRES_DB = "switchmate"
$env:POSTGRES_USER = "switchmate"
$env:POSTGRES_PASSWORD = "replace-with-a-local-password"
$env:DB_URL = "jdbc:postgresql://localhost:5432/switchmate"
$env:DB_USERNAME = $env:POSTGRES_USER
$env:DB_PASSWORD = $env:POSTGRES_PASSWORD
$env:FRONTEND_ORIGIN = "http://localhost:5173"
$env:JWT_SECRET = "base64-encoded-random-key-of-at-least-32-bytes"
$env:ADMIN_API_KEY = "replace-with-a-local-admin-key"
$env:RESUME_STORAGE_DIRECTORY = "./uploads/resumes"
$env:SPRING_PROFILES_ACTIVE = "fake-ai"
mvn spring-boot:run
```

The API listens on port `8080` by default. Flyway applies migrations automatically. The configured database credentials must match those supplied to Docker Compose.

## Environment Variables

| Variable | Required | Default / purpose |
| --- | --- | --- |
| `POSTGRES_DB` | For Docker Compose | Database name |
| `POSTGRES_USER` | For Docker Compose | Database user |
| `POSTGRES_PASSWORD` | For Docker Compose | Database password |
| `POSTGRES_PORT` | No | Host port, defaults to `5432` |
| `DB_URL` | Yes | JDBC URL, for example `jdbc:postgresql://localhost:5432/switchmate` |
| `DB_USERNAME` | Yes | Application database user |
| `DB_PASSWORD` | Yes | Application database password |
| `FRONTEND_ORIGIN` | Yes | Allowed browser origin for CORS |
| `JWT_SECRET` | Yes | Base64-encoded signing key with at least 32 random bytes |
| `ADMIN_API_KEY` | For manual ingestion | Key sent as `X-Admin-Api-Key`; empty configuration rejects ingestion requests |
| `SERVER_PORT` | No | HTTP port, defaults to `8080` |
| `SPRING_PROFILES_ACTIVE` | No | Set to `fake-ai` to use the fake resume parser |
| `OPENAI_API_KEY` | For live resume parsing | OpenAI API key; not needed with `fake-ai` |
| `OPENAI_MODEL` | No | OpenAI chat-completions model, defaults to `gpt-4o-mini` |
| `RESUME_STORAGE_DIRECTORY` | No | Local resume storage path, defaults to `./uploads/resumes` |
| `GREENHOUSE_BOARD_TOKENS` | No | Comma-separated Greenhouse board tokens |
| `LEVER_COMPANIES` | No | Comma-separated Lever site/company identifiers |
| `ASHBY_BOARDS` | No | Comma-separated Ashby job-board names |
| `JOB_INGESTION_ENABLED` | No | Scheduled ingestion toggle, defaults to `true` |
| `JOB_INGESTION_CRON` | No | Ingestion cron, defaults to `0 0 */6 * * *` |
| `JOB_SOURCE_CONNECT_TIMEOUT` | No | Job-source connection timeout, defaults to `3s` |
| `JOB_SOURCE_READ_TIMEOUT` | No | Job-source response timeout, defaults to `20s` |
| `JOB_MATCH_SKILL_WEIGHT` | No | Skill matching weight, defaults to `0.7` |
| `JOB_MATCH_EXPERIENCE_WEIGHT` | No | Experience matching weight, defaults to `0.2` |
| `JOB_MATCH_PREFERENCE_WEIGHT` | No | Preference matching weight, defaults to `0.1` |
| `TEST_DB_URL` | With `test` profile | PostgreSQL JDBC URL for tests |
| `TEST_DB_USERNAME` | With `test` profile | Test database user |
| `TEST_DB_PASSWORD` | With `test` profile | Test database password |
| `TEST_FRONTEND_ORIGIN` | With `test` profile | CORS origin for tests |

## Profiles

- Default profile uses `OpenAiResumeParser`; set `OPENAI_API_KEY` to parse uploaded resumes with OpenAI.
- `fake-ai` selects `FakeResumeParser`, which returns fixed sample profile data and does not require an OpenAI key. PDF/DOCX text extraction and file storage still run normally.
- `test` reads `TEST_DB_URL`, `TEST_DB_USERNAME`, `TEST_DB_PASSWORD`, and `TEST_FRONTEND_ORIGIN`. The integration test uses Testcontainers and is skipped when Docker is unavailable.

## Job Ingestion

Configure one or more board identifiers in `GREENHOUSE_BOARD_TOKENS`, `LEVER_COMPANIES`, and/or `ASHBY_BOARDS`. The scheduled run executes every six hours by default; change `JOB_INGESTION_CRON` or set `JOB_INGESTION_ENABLED=false` to disable it.

To trigger ingestion immediately:

```powershell
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/admin/jobs/ingest" `
  -Headers @{ "X-Admin-Api-Key" = $env:ADMIN_API_KEY }
```

## API Documentation

Swagger UI is available at [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui). The generated OpenAPI document is at `http://localhost:8080/v3/api-docs`.

## Resume Upload With Fake Parser

Start with `SPRING_PROFILES_ACTIVE=fake-ai`, sign up or log in, then send the resume as a multipart field named `file` to `POST /api/profile/resume` with the access token:

```powershell
curl.exe -X POST `
  -H "Authorization: Bearer $token" `
  -F "file=@resume.pdf" `
  "http://localhost:8080/api/profile/resume"
```

Only PDF and DOCX files up to 5 MB are accepted. The API stores the uploaded file and returns sample parsed profile data for review; it does not overwrite the saved profile. The client can save reviewed data with `PUT /api/profile`.

## Response Contracts

Successful API endpoints return their resource DTOs directly. Errors use the common envelope:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fieldErrors": [
    { "field": "email", "message": "must be a well-formed email address" }
  ]
}
```

`fieldErrors` is omitted when an error has no field-level details. Authentication and authorization errors use the same envelope.