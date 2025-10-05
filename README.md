# Enrollment System Microservices

A small, educational microservices-based enrollment system that demonstrates a multi-service architecture using Spring Boot. The system is split into three services:

- `students-service` — manages student records (MySQL + Spring Data JPA)
- `courses-service` — manages courses (Postgres + Spring WebFlux + Reactive Repositories)
- `enrollments-service` — manages enrollments (MongoDB + Spring WebFlux), enriches enrollment records by calling student and course services

This repository includes `docker-compose` to run the services and databases locally for development and demonstration.

## Features

- CRUD APIs for students, courses and enrollments
- Reactive services using Spring WebFlux (courses and enrollments)
- Synchronous service for students (Spring Data JPA + MySQL)
- Inter-service calls (WebClient / RestTemplate) to enrich enrollment entities
- Validation and global exception handling with meaningful HTTP errors
- Preloaded sample data and SQL scripts for quick setup
- Docker Compose to orchestrate services and databases

## Tech stack

- Java 17+
- Spring Boot (WebFlux and MVC)
- Spring Data JPA (students-service)
- Spring Data Reactive (courses-service and enrollments-service)
- MongoDB, PostgreSQL, MySQL
- MapStruct (DTO mapping)
- Gradle (multi-project build)
- Docker, docker-compose

## Prerequisites

- Docker and Docker Compose (for the easiest local setup)
- Java 17+ (if running services locally without Docker)
- Gradle (or use the included Gradle wrapper)

## Quick start with Docker (recommended)

1. From the repository root, start the full stack:

```powershell
docker-compose up --build
```

2. Services will be available on host ports:
- Enrollments API: http://localhost:8081/api/v1/enrollments
- Students API: http://localhost:8082/api/v1/students
- Courses API: http://localhost:8083/api/v1/courses

3. Database admin UIs:
- phpMyAdmin (MySQL): http://localhost:5013
- pgAdmin (Postgres): http://localhost:9000
- mongo-express (MongoDB): http://localhost:8091

Notes:
- The compose file maps the containers to host ports and provides initial SQL/Mongo sample data.
- If you need to reset DB volumes used by Docker, stop the compose stack and remove volumes created in the `data/` folder.

## Run locally (without Docker)

Each service is a separate Spring Boot application. You can run them individually from an IDE or the command line.

1. Start the database(s) you need (MySQL, Postgres, MongoDB) locally and set the appropriate connection properties in each service's `src/main/resources/application.yml` or `application.properties` (or use the `docker` spring profile provided in the repo).

2. Run each service:
- `courses-service`
- `students-service`
- `enrollments-service`

Example (using the Gradle wrapper in each service directory):

```powershell
# from repository root - runs gradle task for the module
.\gradlew :students-service:bootRun
.\gradlew :courses-service:bootRun
.\gradlew :enrollments-service:bootRun
```

Make sure each service port (default container port 8080) is not conflicting on your host. The docker-compose file maps internal 8080 to different external ports for local use.

## API Examples

Use curl, httpie or Postman to interact with APIs.

Get all enrollments (stream):
```powershell
curl http://localhost:8081/api/v1/enrollments
```

Create an enrollment:
```powershell
curl -X POST http://localhost:8081/api/v1/enrollments -H "Content-Type: application/json" -d @- <<'JSON'
{
	"enrollmentYear": 2024,
	"semester": "FALL",
	"studentId": "c3540a89-cb47-4c96-888e-ff96708db4d8",
	"courseId": "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223"
}
JSON
```

Get a course by id:
```powershell
curl http://localhost:8083/api/v1/courses/<courseId>
```

Get a student by studentId:
```powershell
curl http://localhost:8082/api/v1/students/<studentId>
```

## Project structure

- `courses-service/` — Reactive course microservice (Postgres)
	- `src/main/java/...presentationlayer` — REST controllers and DTOs
	- `src/main/java/...businesslayer` — services and business logic
	- `src/main/java/...dataaccesslayer` — repository & entity
	- `src/main/resources` — application config and SQL scripts
- `students-service/` — Student microservice (MySQL, synchronous)
	- similar structure but uses Spring Data JPA and MapStruct mapping
- `enrollments-service/` — Enrollment microservice (MongoDB, reactive)
	- calls students-service and courses-service to enrich records
- `docker-compose.yml` — local orchestration for services and DBs
- `data/` — DB init data and volumes

## Error handling and validation

- Controllers validate input and return meaningful errors using a global controller advice.
- `ApplicationExceptions` centralizes reactive error creation (used by controllers/clients for reactive flows).
- `RequestValidator` classes validate body content (present in courses and enrollments services).

## Contributing

- Fork the repository and open a branch per feature/fix.
- Run unit and integration tests for the module you change.
- Follow the existing code style and patterns (services are split by domain).
- If adding new API endpoints, add matching tests (unit and integration) and documentation in the README.

## Testing

- Each service contains tests under `src/test/java`. Run tests using Gradle:
```powershell
.\gradlew test
# or for a specific module
.\gradlew :enrollments-service:test
