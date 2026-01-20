# Task Project

This is a Spring Boot application with PostgreSQL, fully containerized using Docker and Docker Compose.

---

## Prerequisites

- Docker Desktop installed  
- Docker Compose (comes with Docker Desktop)  
- JDK 21 (for building the project locally)  
- Gradle installed (or use the wrapper `./gradlew`)  

---

## Setup & Run



### 1️⃣ Clone project
bash
git clone https://github.com/Kamala03/springboot-task-app.git
cd springboot-task-app

### 2️⃣ Build the application
./gradlew clean build


### 3️⃣ Build Docker images
docker-compose build


### 4️⃣ Run the application
docker-compose up



