@echo off
setlocal enabledelayedexpansion

REM Load local secrets such as OPENROUTER_API_KEY from the untracked .env file.
if exist .env (
    for /f "tokens=1* delims== eol=#" %%a in (.env) do (
        set "%%a=%%b"
    )
)
if not defined OPENROUTER_API_KEY (
    echo WARNING: OPENROUTER_API_KEY is not set. Add it to .env or set it before running this script.
)
set OPENROUTER_MODEL=openai/gpt-4o-mini
set OPENROUTER_ENDPOINT=https://openrouter.ai/api/v1/chat/completions
set SPRING_PROFILES_ACTIVE=local
set SERVER_PORT=8081
set FRONTEND_ORIGIN=http://localhost:5173
set JWT_SECRET=U3dpdGNobWF0ZS1sb2NhbC1kZXYta2V5LTIwMjYtMDkt
set ADMIN_API_KEY=local-ingest-key
set RESUME_STORAGE_DIRECTORY=./uploads/resumes
set JOB_INGESTION_ENABLED=false
set SPRING_DATASOURCE_URL=jdbc:h2:mem:switchmate;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
set SPRING_DATASOURCE_USERNAME=sa
set SPRING_DATASOURCE_PASSWORD=
set SPRING_FLYWAY_ENABLED=false
set SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop

echo Starting SwitchMate Backend with OpenRouter configured...
echo OPENROUTER_API_KEY is set: YES
echo.

mvn spring-boot:run