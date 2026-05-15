# 🚀 Team Task Manager

A full-stack web application built with **Java Spring Boot** that allows users to create projects, assign tasks, and track progress with **role-based access control (Admin/Member)**.

## 📋 Key Features

- **Authentication** — Signup & Login with JWT-based security
- **Project & Team Management** — Create projects, add/remove team members
- **Task Management** — Create, assign, update status & track tasks
- **Dashboard** — Overview of tasks, status counts, and overdue items
- **Role-Based Access Control** — Admin and Member roles with permission enforcement

## 🛠️ Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Backend     | Java 17, Spring Boot 3.2            |
| Security    | Spring Security + JWT (jjwt 0.12)   |
| Database    | H2 (dev) / MySQL (production)       |
| ORM         | Spring Data JPA / Hibernate         |
| Build       | Maven                               |
| Validation  | Jakarta Bean Validation              |

## ⚙️ Requirements

- Java 17+
- Maven 3.8+
- MySQL 8+ (for production) or H2 (embedded, for development)

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/your-username/team-task-manager.git
cd team-task-manager
```

### 2. Configure the application

**For Development (H2 — default):**
No additional configuration needed. H2 in-memory database is used by default.

**For Production (MySQL):**
Set the following environment variables:
```bash
export DATABASE_URL=jdbc:mysql://localhost:3306/taskmanagerdb
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=your_password
export DATABASE_DRIVER=com.mysql.cj.jdbc.Driver
export HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect
export JWT_SECRET=your-256-bit-secret-key-here
```

### 3. Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

### 4. H2 Console (Dev)
Access at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:taskmanagerdb`
- Username: `sa`
- Password: (empty)

## 📡 API Endpoints

### Public
| Method | Endpoint          | Description          | Access  |
|--------|-------------------|----------------------|---------|
| GET    | `/`               | API info & welcome   | Public  |
| GET    | `/api/health`     | Health check         | Public  |

### Authentication
| Method | Endpoint          | Description        | Access  |
|--------|-------------------|--------------------|---------|
| POST   | `/api/auth/signup` | Register new user  | Public  |
| POST   | `/api/auth/login`  | Login              | Public  |

### Users
| Method | Endpoint          | Description            | Access       |
|--------|-------------------|------------------------|--------------|
| GET    | `/api/users/me`   | Get current user       | Authenticated|
| GET    | `/api/users`      | Get all users          | Admin only   |
| GET    | `/api/users/all`  | Get users for assignment| Authenticated|

### Projects
| Method | Endpoint                              | Description        | Access           |
|--------|---------------------------------------|--------------------|------------------|
| POST   | `/api/projects`                       | Create project     | Authenticated    |
| GET    | `/api/projects`                       | Get all projects   | Authenticated    |
| GET    | `/api/projects/{id}`                  | Get project by ID  | Project member   |
| PUT    | `/api/projects/{id}`                  | Update project     | Owner/Admin      |
| DELETE | `/api/projects/{id}`                  | Delete project     | Owner/Admin      |
| POST   | `/api/projects/{id}/members/{userId}` | Add member         | Owner/Admin      |
| DELETE | `/api/projects/{id}/members/{userId}` | Remove member      | Owner/Admin      |

### Tasks
| Method | Endpoint                      | Description           | Access         |
|--------|-------------------------------|-----------------------|----------------|
| POST   | `/api/tasks`                  | Create task           | Project member |
| GET    | `/api/tasks/project/{id}`     | Get tasks by project  | Project member |
| GET    | `/api/tasks/my-tasks`         | Get my assigned tasks | Authenticated  |
| GET    | `/api/tasks/{id}`             | Get task by ID        | Project member |
| PUT    | `/api/tasks/{id}`             | Update task           | Project member |
| PATCH  | `/api/tasks/{id}/status`      | Update task status    | Project member |
| DELETE | `/api/tasks/{id}`             | Delete task           | Owner/Admin    |

### Dashboard
| Method | Endpoint          | Description       | Access        |
|--------|-------------------|--------------------|---------------|
| GET    | `/api/dashboard`  | Get dashboard data | Authenticated |

## 📦 API Request/Response Examples

### Signup
```json
POST /api/auth/signup
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "ADMIN"
}
```

### Login
```json
POST /api/auth/login
{
  "email": "john@example.com",
  "password": "password123"
}
```

### Create Project
```json
POST /api/projects
Authorization: Bearer <token>
{
  "name": "My Project",
  "description": "Project description"
}
```

### Create Task
```json
POST /api/tasks
Authorization: Bearer <token>
{
  "title": "Implement login page",
  "description": "Create the login UI",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-06-01",
  "projectId": 1,
  "assigneeId": 2
}
```

### Update Task Status
```json
PATCH /api/tasks/1/status
Authorization: Bearer <token>
{
  "status": "IN_PROGRESS"
}
```

## 🔐 Role-Based Access Control

| Feature                  | Admin | Member (Owner) | Member |
|--------------------------|-------|----------------|--------|
| Create Project           | ✅     | ✅              | ✅      |
| Update/Delete Project    | ✅     | ✅              | ❌      |
| Add/Remove Members       | ✅     | ✅              | ❌      |
| Create Task              | ✅     | ✅              | ✅      |
| Update Task              | ✅     | ✅              | ✅      |
| Delete Task              | ✅     | ✅              | ❌      |
| Reassign Task            | ✅     | ✅              | ❌      |
| View All Users           | ✅     | ❌              | ❌      |
| View Dashboard           | ✅     | ✅              | ✅      |

## 🌐 Deployment (Railway)

1. Push code to GitHub
2. Connect to [Railway](https://railway.app)
3. Add a MySQL service
4. Set environment variables:
   - `DATABASE_URL`
   - `DATABASE_USERNAME`
   - `DATABASE_PASSWORD`
   - `DATABASE_DRIVER=com.mysql.cj.jdbc.Driver`
   - `HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect`
   - `JWT_SECRET`
   - `PORT=8080`
5. Deploy!

## 📂 Project Structure

```
src/main/java/com/teamtask/
├── TeamTaskManagerApplication.java
├── controller/
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── HomeController.java
│   ├── ProjectController.java
│   ├── TaskController.java
│   └── UserController.java
├── dto/
│   ├── ApiResponse.java
│   ├── AuthResponse.java
│   ├── DashboardResponse.java
│   ├── LoginRequest.java
│   ├── SignupRequest.java
│   ├── ProjectRequest.java
│   ├── ProjectResponse.java
│   ├── TaskRequest.java
│   ├── TaskResponse.java
│   └── UserSummary.java
├── entity/
│   ├── Project.java
│   ├── Role.java
│   ├── Task.java
│   ├── TaskPriority.java
│   ├── TaskStatus.java
│   └── User.java
├── exception/
│   ├── AccessDeniedException.java
│   ├── BadRequestException.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── ProjectRepository.java
│   ├── TaskRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtil.java
│   └── SecurityConfig.java
└── service/
    ├── AuthService.java
    ├── CustomUserDetailsService.java
    ├── DashboardService.java
    ├── ProjectService.java
    └── TaskService.java
```

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
