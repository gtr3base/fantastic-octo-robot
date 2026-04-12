# Bank Card Management System

A modern RESTful API for managing bank cards, processing transfers, and handling user accounts. Built with Java, Spring Boot, Spring Security (JWT), and PostgreSQL.

## 🛠 Tech Stack
* **Java 17+**
* **Spring Boot 3.x**
* **Spring Security** (JWT Authentication)
* **Spring Data JPA** (Hibernate)
* **PostgreSQL** (Database)
* **Liquibase** (Database Migrations)
* **Docker & Docker Compose** (Dev Environment)
* **Swagger / OpenAPI 3** (API Documentation)
* **JUnit 5 & Mockito** (Unit Testing)

---

## 🚀 Getting Started

### Prerequisites
Make sure you have the following installed:
* Java JDK 17 or higher
* [Maven](https://maven.apache.org/)
* [Docker Desktop](https://www.docker.com/products/docker-desktop)

### 1. Start the Infrastructure (Database)
I use Docker Compose to spin up PostgreSQL and pgAdmin locally.

```bash
# Run the database in the background
docker compose up -d