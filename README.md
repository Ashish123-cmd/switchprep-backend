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
$env:GOOGLE_CLIENT_ID = "your-google-oauth-client-id.apps.googleusercontent.com"
$env:ADMIN_API_KEY = "replace-with-a-local-admin-key"
$env:RESUME_STORAGE_DIRECTORY = "./uploads/resumes"
$env:SPRING_PROFILES_ACTIVE = "fake-ai"
mvn spring-boot:run
```

The API listens on port `8080` by default. Flyway applies migrations automatically. The configured database credentials must match those supplied to Docker Compose.

For a quick integrated UI/API demo without database credentials, run with `SPRING_PROFILES_ACTIVE=local,fake-ai`. The `local` profile uses an in-memory H2 database and disables scheduled ingestion; the `fake-ai` profile avoids OpenAI. Local data is discarded when the backend stops. The default local boards are Lever's `leverdemo` and Ashby's `Ashby`; trigger ingestion manually using `X-Admin-Api-Key: local-ingest-key`.

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
| `GOOGLE_CLIENT_ID` | For Google sign-in | OAuth client ID used to verify Google ID token audiences; `/api/auth/google` returns `500` when it is not set |
| `PUBLIC_API_DOCS` | No | `true` exposes `/swagger-ui` and `/v3/api-docs` without a JWT; defaults to `false` (`true` in the `local` profile) |
| `SIGNUP_DRAFT_TTL_MINUTES` | No | Signup draft lifetime in minutes, defaults to `30` |
| `SIGNUP_DRAFT_CLEANUP_CRON` | No | Cron for deleting expired signup drafts, defaults to `0 */5 * * * *` |
| `SIGNUP_RESUME_RATE_LIMIT_MAX` | No | Resume uploads allowed per client IP per window, defaults to `5` |
| `SIGNUP_RESUME_RATE_LIMIT_WINDOW` | No | Resume upload rate-limit window in minutes, defaults to `10` |
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

## Authentication API

Only the endpoints below can be called without credentials:

| Method | Path | Access | Request | Response |
| --- | --- | --- | --- | --- |
| `POST` | `/api/auth/signup/resume` | Public, rate limited per IP | `multipart/form-data` with a `file` part (PDF or DOCX, max 5 MB) | `201` `{ draftId, parsedProfile, expiresAt }` |
| `POST` | `/api/auth/signup/complete` | Public | `{ draftId, name, email, password }` | `200` `{ token, user }` |
| `POST` | `/api/auth/login` | Public | `{ email, password }` | `200` `{ token, user }` |
| `POST` | `/api/auth/google` | Public | `{ idToken }` | `200` `{ token, user, isNewUser }` |
| `GET` | `/api/auth/me` | Bearer JWT | – | `200` `{ id, name, email, profile: { onboardingCompleted } }` |
| `GET` | `/actuator/health` | Public | – | `200` `{ "status": "UP" }` |

Everything else (profile, preferences, standard answers, jobs, tracker, auto-apply) requires `Authorization: Bearer <token>`. `/api/admin/**` is authenticated with `X-Admin-Api-Key` instead of a JWT.

`user` is always:

```json
{
  "id": "6f0f0c3e-5f70-4f11-9a9f-6b1f0f3f9a10",
  "name": "Alex Morgan",
  "email": "alex.morgan@example.com",
  "profile": { "onboardingCompleted": false }
}
```

`parsedProfile` is the resume parser output, without account fields:

```json
{
  "name": "Alex Morgan",
  "phone": "+1 555 010 1234",
  "location": "Seattle, WA",
  "headline": "Software Engineer",
  "summary": "Software engineer experienced in building reliable web applications.",
  "totalExperienceYears": 5,
  "skills": ["Java", "Spring Boot", "PostgreSQL"],
  "experiences": [
    {
      "company": "Example Company",
      "title": "Software Engineer",
      "startDate": "2021-03-01",
      "endDate": "2024-06-30",
      "description": "Built and maintained backend services."
    }
  ],
  "educations": [
    { "institution": "Example University", "degree": "BS", "field": "Computer Science", "startYear": 2016, "endYear": 2020 }
  ]
}
```

Notes for clients:

- Always send the `draftId` returned by `signup/resume` to `signup/complete`. Drafts expire after `SIGNUP_DRAFT_TTL_MINUTES` and are consumed on completion, so a stale or self-generated id returns `410 SIGNUP_DRAFT_EXPIRED`.
- `experiences` and `educations` in `parsedProfile` do not carry ids; generated ids only exist on `GET /api/profile` after the account is created.
- `startDate` / `endDate` are ISO dates (`yyyy-MM-dd`), while profile editors may submit `yyyy-MM`. Normalize on the client.
- `signup/complete`, `login`, and `google` all return the JWT in `token`; store it and send it as a bearer token on every protected call.

The legacy `POST /api/auth/signup` endpoint has been removed. Signup is now the two-step `signup/resume` → `signup/complete` flow.

## Job Ingestion

Configure one or more board identifiers in `GREENHOUSE_BOARD_TOKENS`, `LEVER_COMPANIES`, and/or `ASHBY_BOARDS`. The scheduled run executes every six hours by default; change `JOB_INGESTION_CRON` or set `JOB_INGESTION_ENABLED=false` to disable it.

To trigger ingestion immediately:

```powershell
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/admin/jobs/ingest" `
  -Headers @{ "X-Admin-Api-Key" = $env:ADMIN_API_KEY }
```

`/api/admin/**` is never anonymous: the request must present the configured `X-Admin-Api-Key` header (a user JWT is not required). A missing or wrong key returns `401 UNAUTHORIZED`.

## API Documentation

Swagger UI is available at [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui). The generated OpenAPI document is at `http://localhost:8080/v3/api-docs`.

Those two paths are **not public by default**: they require the same bearer JWT as the rest of the API. Set `PUBLIC_API_DOCS=true` (the `local` profile already defaults to `true`) to expose them without a token. The generated document declares the `bearerAuth` scheme globally and marks `login`, `signup/resume`, `signup/complete`, and `google` as public.

## Resume Upload With Fake Parser

Start with `SPRING_PROFILES_ACTIVE=fake-ai`, sign up or log in, then send the resume as a multipart field named `file` to `POST /api/profile/resume` with the access token:

```powershell
curl.exe -X POST `
  -H "Authorization: Bearer $token" `
  -F "file=@resume.pdf" `
  "http://localhost:8080/api/profile/resume"
```

Only PDF and DOCX files up to 5 MB are accepted. The API stores the uploaded file and returns sample parsed profile data for review; it does not overwrite the saved profile. The client can save reviewed data with `PUT /api/profile`.

## Google Sign-In

`POST /api/auth/google` accepts a Google ID token and is public. Set `GOOGLE_CLIENT_ID` so the backend can verify the token signature, expiry, issuer, and audience.

```powershell
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/auth/google" `
  -ContentType "application/json" `
  -Body (@{ idToken = $googleIdToken } | ConvertTo-Json)
```

The response contains `token`, `user`, and `isNewUser`. Existing local accounts are linked by matching email; a new account is provisioned when neither the Google subject nor the email matches. Invalid tokens and unverified email addresses return `401`.

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

## Frontend Contract Notes

The shapes above are the backend source of truth for the F1 auth flow. The current
frontend `AuthService` interface needs these changes to match:

1. `getCurrentUser(): Promise<string | null>` → `GET /api/auth/me` returns the full
   user object, so return `Promise<AuthUser | null>` and read `user.email` where the
   UI needs a string.
2. `signIn`, `signInWithGoogle`, and `completeSignup` return `void` today, but the
   API returns `{ token, user }` (`{ token, user, isNewUser }` for Google). Return
   those payloads so the JWT can be stored and the new-user flag can drive
   `/jobs` vs `/onboarding` without an extra request.
3. `parseResumeForSignup(file): Promise<UserProfile>` → `POST /api/auth/signup/resume`
   returns `{ draftId, parsedProfile, expiresAt }`. Return that draft object and map
   `parsedProfile.name` to `fullName`; there is no `email`, no `resume`, and no ids on
   parsed experiences/educations. Parsed dates are `yyyy-MM-dd`, so convert to the
   `yyyy-MM` value used by the month inputs.
4. `SignupPage` currently generates `draftId` with `crypto.randomUUID()`; it must send
   the `draftId` returned by the resume upload, otherwise `/api/auth/signup/complete`
   responds `410 SIGNUP_DRAFT_EXPIRED`.
