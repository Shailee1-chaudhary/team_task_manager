# 🚀 Team Task Manager

A full-stack web application built with **Java Spring Boot** that allows teams to create projects, assign tasks, track progress, and visualize work on a **Kanban board** — all with **role-based access control (Admin/Member)**.

## 📋 Key Features

- **Authentication** — Signup & Login with JWT-based security
- **Project Management** — Admins create projects and manage team members
- **Task Management** — Create, assign, update status, set priorities, story points & due dates
- **Task Identifiers** — Every task gets a unique number (TSK_1, TSK_2, …) visible across all views
- **7 Task Statuses** — To Do, In Progress, Blocked, Code Review, QA Testing, QA Testing Failed, Done
- **Kanban Board** — Visual board view with columns per status, accessible to all users
- **Progress Notes** — Add timestamped progress updates/comments on tasks
- **Dashboard** — Overview of task counts by status, overdue items, and recent activity
- **Role-Based Access Control** — Admin and Member roles with granular permission enforcement
- **Input Validation** — Client-side & server-side validation with descriptive error messages

## 🖥️ Pages

| Page | URL | Description |
|------|-----|-------------|
| Login | `/login.html` | User authentication |
| Signup | `/signup.html` | New user registration |
| Dashboard | `/dashboard.html` | Stats, recent tasks, project overview |
| Projects | `/projects.html` | Project list, create (admin), manage members |
| My Tasks | `/tasks.html` | Task list with filters, create/edit/delete tasks |
| Board | `/board.html` | Kanban board with all tasks grouped by status |

## 🛠️ Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Backend     | Java 17, Spring Boot 3.2            |
| Security    | Spring Security + JWT (jjwt 0.12)   |
| Database    | H2 (dev) / MySQL (production)       |
| ORM         | Spring Data JPA / Hibernate         |
| Frontend    | Vanilla HTML/CSS/JavaScript         |
| Build       | Maven                               |
| Validation  | Jakarta Bean Validation              |

## ⚙️ Requirements

- Java 17+
- Maven 3.8+
- MySQL 8+ (for production) or H2 (embedded, for development)

## 🚀 Getting Started

### 1. Clone the repository

git clone https://github.com/your-username/team-task-manager.git
cd team-task-manager


### 2. Configure the application

**For Development (H2 — default):**
No additional configuration needed. H2 in-memory database is used by default.

**For Production (MySQL):**
Set the following environment variables:

export DATABASE_URL=jdbc:mysql://localhost:3306/taskmanagerdb
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=your_password
export DATABASE_DRIVER=com.mysql.cj.jdbc.Driver
export HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect
export JWT_SECRET=your-256-bit-secret-key-here


### 3. Build & Run

mvn clean install
mvn spring-boot:run


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
| POST   | `/api/projects`                       | Create project     | Admin only       |
| GET    | `/api/projects`                       | Get all projects   | Authenticated    |
| GET    | `/api/projects/{id}`                  | Get project by ID  | Project member   |
| PUT    | `/api/projects/{id}`                  | Update project     | Owner/Admin      |
| DELETE | `/api/projects/{id}`                  | Delete project     | Owner/Admin      |
| POST   | `/api/projects/{id}/members/{userId}` | Add member         | Owner/Admin      |
| DELETE | `/api/projects/{id}/members/{userId}` | Remove member      | Owner/Admin      |

### Tasks
| Method | Endpoint                          | Description           | Access         |
|--------|-----------------------------------|-----------------------|----------------|
| POST   | `/api/tasks`                      | Create task           | Project member |
| GET    | `/api/tasks/all`                  | Get all tasks (board) | Authenticated  |
| GET    | `/api/tasks/project/{id}`         | Get tasks by project  | Project member |
| GET    | `/api/tasks/my-tasks`             | Get my assigned tasks | Authenticated  |
| GET    | `/api/tasks/{id}`                 | Get task by ID        | Project member |
| PUT    | `/api/tasks/{id}`                 | Update task           | Project member |
| PATCH  | `/api/tasks/{id}/status`          | Update task status    | Project member |
| DELETE | `/api/tasks/{id}`                 | Delete task           | Owner/Admin    |

### Progress Notes (Comments)
| Method | Endpoint                              | Description            | Access         |
|--------|---------------------------------------|------------------------|----------------|
| GET    | `/api/tasks/{id}/progress`            | Get progress notes     | Project member |
| POST   | `/api/tasks/{id}/progress`            | Add progress note      | Project member |
| DELETE | `/api/tasks/{id}/progress/{noteId}`   | Delete progress note   | Author/Admin   |

### Dashboard
| Method | Endpoint          | Description       | Access        |
|--------|-------------------|--------------------|---------------|
| GET    | `/api/dashboard`  | Get dashboard data | Authenticated |

## 📊 Task Statuses

| Status | Description |
|--------|-------------|
| `TODO` | Task is pending, not yet started |
| `IN_PROGRESS` | Task is actively being worked on |
| `BLOCKED` | Task is blocked by a dependency or issue |
| `CODE_REVIEW` | Code is written and awaiting review |
| `QA_TESTING` | Task is being tested by QA |
| `QA_TESTING_FAILED` | QA testing found issues, needs rework |
| `DONE` | Task is complete |

## 📦 API Request/Response Examples

### Signup

POST /api/auth/signup
{
  "name": "Shailee Chaudhary",
  "email": "shailee@gmail.com",
  "password": "password123@",
  "role": "ADMIN"
}


### Login

POST /api/auth/login
{
  "email": "shailee@gmail.com",
  "password": "password123@"
}


### Create Project (Admin only)

POST /api/projects
Authorization: Bearer <token>
{
  "name": "Code Review",
  "description": "Code Review"
}


### Create Task

POST /api/tasks
Authorization: Bearer <token>
{
  "title": "Implement login page",
  "description": "Create the login UI",
  "status": "TODO",
  "priority": "HIGH",
  "storyPoints": 5,
  "dueDate": "2026-06-01",
  "projectId": 1,
  "assigneeId": 2
}


### Update Task Status

PATCH /api/tasks/1/status
Authorization: Bearer <token>
{
  "status": "CODE_REVIEW"
}


### Add Progress Note

POST /api/tasks/1/progress
Authorization: Bearer <token>
{
  "content": "Completed the API integration, moving to testing"
}


## 🔐 Role-Based Access Control

| Feature                  | Admin | Member (Owner) | Member |
|--------------------------|-------|----------------|--------|
| Create Project           | ✅     | ❌              | ❌      |
| Update/Delete Project    | ✅     | ✅              | ❌      |
| Add/Remove Members       | ✅     | ✅              | ❌      |
| Create Task              | ✅     | ✅              | ✅      |
| Update Task              | ✅     | ✅              | ✅      |
| Delete Task              | ✅     | ✅              | ❌      |
| Reassign Task            | ✅     | ✅              | ❌      |
| View Board (all tasks)   | ✅     | ✅              | ✅      |
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


src/main/java/com/teamtask/
├── TeamTaskManagerApplication.java
├── controller/
│   ├── AuthController.java
│   ├── CommentController.java
│   ├── DashboardController.java
│   ├── HomeController.java
│   ├── ProjectController.java
│   ├── TaskController.java
│   └── UserController.java
├── dto/
│   ├── ApiResponse.java
│   ├── AuthResponse.java
│   ├── CommentRequest.java
│   ├── CommentResponse.java
│   ├── DashboardResponse.java
│   ├── LoginRequest.java
│   ├── SignupRequest.java
│   ├── ProjectRequest.java
│   ├── ProjectResponse.java
│   ├── TaskRequest.java
│   ├── TaskResponse.java
│   └── UserSummary.java
├── entity/
│   ├── Comment.java
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
│   ├── CommentRepository.java
│   ├── ProjectRepository.java
│   ├── TaskRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtil.java
│   └── SecurityConfig.java
└── service/
    ├── AuthService.java
    ├── CommentService.java
    ├── CustomUserDetailsService.java
    ├── DashboardService.java
    ├── ProjectService.java
    └── TaskService.java

src/main/resources/static/
├── index.html
├── login.html
├── signup.html
├── dashboard.html
├── projects.html
├── tasks.html
├── board.html
├── css/
│   └── style.css
└── js/
    └── api.js


## 📄 License

This project is open source.
