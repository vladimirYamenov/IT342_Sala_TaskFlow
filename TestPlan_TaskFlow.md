# TaskFlow – Full Regression Test Plan
**Version:** 1.0  
**Branch:** `refactor/vertical-slice-architecture`  
**Date:** 2026  
**Prepared by:** Kirby Sala

---

## 1. Objectives
- Verify that the vertical slice architecture refactoring did not break any existing functionality.
- Validate all major features across Backend (Spring Boot), Web (React), and Mobile (Android/Kotlin).
- Provide evidence of passing automated tests and manual smoke-tests.

---

## 2. Scope

| Layer    | Technology           | Test Type               |
|----------|----------------------|-------------------------|
| Backend  | Spring Boot 3.3 / Java 17 | Unit + Integration (JUnit 5, MockMvc) |
| Web      | React 18             | Component tests (React Testing Library) |
| Mobile   | Android / Kotlin     | Manual smoke test       |

---

## 3. Features Under Test

| Feature ID | Feature Name  | Components Involved                                  |
|------------|---------------|------------------------------------------------------|
| F-01       | Authentication | AuthController, AuthService, User, UserRepository, JwtUtil |
| F-02       | Task Management | TaskController, TaskService, Task, TaskRepository   |
| F-03       | Group Management | GroupController, GroupService, Group, GroupMember  |
| F-04       | File Management | FileController, FileService, FileEntity, FileRepository |
| F-05       | Security & JWT  | SecurityConfig, JwtAuthenticationFilter, JwtUtil    |

---

## 4. Test Cases

### 4.1 Authentication (F-01)

| TC-ID  | Test Case Name              | Precondition      | Steps                                           | Expected Result                         | Type        |
|--------|-----------------------------|-------------------|-------------------------------------------------|-----------------------------------------|-------------|
| TC-01  | Register with valid data     | No existing user  | POST /api/auth/register with name/email/password | 200 OK, returns JWT token              | Integration |
| TC-02  | Register with duplicate email| User exists       | POST /api/auth/register with existing email     | 409 Conflict                            | Integration |
| TC-03  | Register password mismatch   | -                 | POST /api/auth/register, passwords don't match  | 400 Bad Request                         | Integration |
| TC-04  | Login with valid credentials | User registered   | POST /api/auth/login with correct credentials   | 200 OK, returns JWT token              | Integration |
| TC-05  | Login with wrong password    | User registered   | POST /api/auth/login with wrong password        | 401 Unauthorized                        | Integration |
| TC-06  | Login with unknown email     | -                 | POST /api/auth/login with non-existent email    | 401 Unauthorized                        | Integration |
| TC-07  | Access protected route without token | - | GET /api/tasks without Authorization header  | 403 Forbidden                           | Integration |

### 4.2 Task Management (F-02)

| TC-ID  | Test Case Name              | Precondition      | Steps                                           | Expected Result                         | Type        |
|--------|-----------------------------|-------------------|-------------------------------------------------|-----------------------------------------|-------------|
| TC-08  | Create task                 | Authenticated user| POST /api/tasks with title/priority/status      | 200 OK, task created with correct fields| Integration |
| TC-09  | Get all tasks for user      | User has tasks    | GET /api/tasks                                  | 200 OK, list of tasks                   | Integration |
| TC-10  | Update existing task        | Task exists       | PUT /api/tasks/{id} with updated fields         | 200 OK, task updated                    | Integration |
| TC-11  | Delete task                 | Task exists       | DELETE /api/tasks/{id}                          | 200 OK, task removed                    | Integration |
| TC-12  | Access other user's task    | Two users with tasks | DELETE /api/tasks/{otherId} as user A        | 403 Forbidden                           | Integration |

### 4.3 Group Management (F-03)

| TC-ID  | Test Case Name              | Precondition          | Steps                                       | Expected Result                         | Type        |
|--------|-----------------------------|-----------------------|---------------------------------------------|-----------------------------------------|-------------|
| TC-13  | Create group                | Authenticated user    | POST /api/groups with name                  | 200 OK, group created                   | Integration |
| TC-14  | Get user's groups           | User has groups       | GET /api/groups                             | 200 OK, list of groups                  | Integration |
| TC-15  | Add member by email         | Group exists, user exists | POST /api/groups/{id}/members with email | 200 OK, member added                    | Integration |
| TC-16  | Delete group by owner       | Group exists          | DELETE /api/groups/{id}                     | 200 OK, group deleted                   | Integration |

### 4.4 File Management (F-04)

| TC-ID  | Test Case Name              | Precondition          | Steps                                       | Expected Result                         | Type        |
|--------|-----------------------------|-----------------------|---------------------------------------------|-----------------------------------------|-------------|
| TC-17  | Upload file                 | Authenticated user    | POST /api/files (multipart)                 | 201 Created, file metadata returned     | Integration |
| TC-18  | List uploaded files         | Files exist           | GET /api/files                              | 200 OK, list of file metadata           | Integration |
| TC-19  | Delete file                 | File exists           | DELETE /api/files/{id}                      | 204 No Content, file removed            | Integration |

### 4.5 Security (F-05)

| TC-ID  | Test Case Name              | Precondition          | Steps                                       | Expected Result                         | Type        |
|--------|-----------------------------|-----------------------|---------------------------------------------|-----------------------------------------|-------------|
| TC-20  | JWT is validated per request| Valid JWT             | Request with valid JWT                      | 200 OK                                  | Unit        |
| TC-21  | Expired JWT rejected        | Expired JWT           | Request with expired token                  | 403 Forbidden                           | Unit        |
| TC-22  | Malformed JWT rejected      | Bad JWT string        | Request with invalid token string           | 403 Forbidden                           | Unit        |

---

## 5. Test Environment

| Item              | Value                                |
|-------------------|--------------------------------------|
| JDK               | Java 17                              |
| Build Tool        | Maven 3.x (mvnw.cmd)                 |
| Test DB           | H2 In-Memory (test scope)            |
| Test Framework    | JUnit 5, Spring Boot Test, MockMvc   |
| Web               | React 18, React Testing Library      |
| Mobile            | Android Studio (manual smoke)        |

---

## 6. Pass/Fail Criteria

- **Pass:** All JUnit tests execute with `BUILD SUCCESS` from `.\mvnw.cmd test`
- **Pass:** All React component tests pass with `npm test -- --watchAll=false`
- **Fail:** Any compilation error, test failure, or feature regression

---

## 7. Test Execution Schedule

| Phase  | Activity                                | Owner    |
|--------|-----------------------------------------|----------|
| 1      | Run backend JUnit tests (`mvnw test`)   | Dev      |
| 2      | Run web React tests (`npm test`)        | Dev      |
| 3      | Manual Android smoke test               | Dev      |
| 4      | Document results in Regression Report   | Dev      |
