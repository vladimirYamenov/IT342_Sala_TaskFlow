# TaskFlow Web Application

A collaborative task management web application built with React.

## Features

- **Task Management** — Create, read, update, and delete tasks with priority levels (High/Medium/Low), status tracking (To Do/In Progress/Completed/Pending), due dates, and search/filter functionality.
- **Group Collaboration** — Create groups, manage members (Admin/Member roles), and view group-specific tasks.
- **File Management** — Upload and manage images (JPEG, PNG, GIF, WebP) and PDF files (up to 10MB).
- **Dashboard** — Overview with task statistics, recent tasks, overdue alerts, and group summaries.
- **User Authentication** — Secure JWT-based login/registration with input validation.
- **Toast Notifications** — Real-time success/error/info messages for all actions.

## Tech Stack

- **Frontend:** React 19, React Router 7
- **Backend:** Spring Boot 3.3.5 (Java 17)
- **Database:** MySQL (taskflow_db)
- **Auth:** JWT + BCrypt

## Getting Started

### Prerequisites
- Node.js 18+
- Backend server running on `http://localhost:8080`

### Installation

```bash
cd web
npm install
npm start
```

The app runs at `http://localhost:3000` and proxies API requests to `http://localhost:8080`.

## API Endpoints Used

| Feature | Method | Endpoint |
|---------|--------|----------|
| Register | POST | `/api/auth/register` |
| Login | POST | `/api/auth/login` |
| Get Profile | GET | `/api/auth/me` |
| List Tasks | GET | `/api/tasks` |
| Create Task | POST | `/api/tasks` |
| Update Task | PUT | `/api/tasks/:id` |
| Delete Task | DELETE | `/api/tasks/:id` |
| List Groups | GET | `/api/groups` |
| Create Group | POST | `/api/groups` |
| Add Member | POST | `/api/groups/:id/members` |
| Remove Member | DELETE | `/api/groups/:id/members/:userId` |
| Group Tasks | GET | `/api/groups/:id/tasks` |
| Upload File | POST | `/api/files` |
| List Files | GET | `/api/files` |
| Delete File | DELETE | `/api/files/:id` |
| Download | GET | `/api/files/:id/download` |

## Database Tables

- `users` — User accounts (id, email, password, fullName, role)
- `tasks` — Task records (id, title, description, priority, status, dueDate, user_id, group_id)
- `groups` — Group definitions (id, name, createdAt)
- `group_members` — Group membership (id, group_id, user_id, role)
- `files` — Uploaded files (id, fileName, fileType, filePath, fileSize, task_id, user_id)
