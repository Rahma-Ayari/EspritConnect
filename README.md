# Esprit Connect Backend

This repository contains the backend API for the Esprit Connect project.

## Current status
- Branch: `Esprit-Connect-Bcakend`
- Framework: Spring Boot 3.2.5
- Java version: 17
- Build system: Maven

## Features
- REST API backend with Spring Boot
- Spring Security authentication and authorization
- JWT support via `io.jsonwebtoken`
- Email support via Spring Mail
- Google login verification with Google API Client
- Two-factor authentication (TOTP)
- Thymeleaf templates support
- OpenAPI / Swagger support via Springdoc
- MySQL runtime database support, H2 for tests
- Resume text extraction using Apache PDFBox and Apache POI
- Activity tracking via Spring AOP

## Project structure
- `pom.xml` — Maven dependencies and build settings
- `src/main/java/` — Java source code
- `src/main/resources/` — application configuration files
- `src/test/java/` — automated tests
- `application-local.properties.example` — local environment example config

## Setup
1. Copy the example config:
   ```bash
   cp application-local.properties.example src/main/resources/application-local.properties
   ```
2. Configure database and mail settings in `src/main/resources/application-local.properties`.
3. Build the project:
   ```bash
   ./mvnw clean package
   ```
4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Notes
- Use a local or remote MySQL database for the application runtime.
- For local development, ensure the `application-local.properties` file contains correct credentials and mail server configuration.
- The backend API should be paired with the Angular frontend in `Angular-EspritConnect-1`.
