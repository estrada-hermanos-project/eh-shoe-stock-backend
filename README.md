# eh-shoe-stock-backend

REST API for inventory control at Estrada Hermanos shoe store.

## Stack

- Java 21
- Maven
- Spring Boot 3.5.16
- Spring Web, Spring Data JPA (Hibernate), Spring Security
- MySQL
- Lombok
- JUnit 5 and Mockito (`spring-boot-starter-test`)
- OpenAPI / Swagger UI (springdoc)

## Requirements

- JDK 21 or higher
- Maven 3.9+
- MySQL 8 (or Docker)

## Configuration

Default values in `src/main/resources/application.yml` can be overridden with environment variables:

| Variable | Default | Description |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP port |
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `eh_shoe_stock` | Database name |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | `root` | Database password |
| `JPA_DDL_AUTO` | `update` | Hibernate `ddl-auto` |

## Run MySQL with Docker

```bash
docker compose up -d
```

## Build and run

```bash
./mvnw clean package
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd clean package
mvnw.cmd spring-boot:run
```

## Useful URLs

- Health: `http://localhost:8080/api/v1/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Tests

```bash
./mvnw test
```
