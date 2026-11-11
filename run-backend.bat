@echo off
:: Load .env file
for /f "tokens=1* delims== eol=#" %%a in (.env) do (
    set "%%a=%%b"
)
:: Start Spring Boot with local profile
mvn spring-boot:run -Dspring.profiles.active=local