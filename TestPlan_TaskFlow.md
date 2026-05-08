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

> **Automation Note:** All JUnit 5 backend tests (TC-00 to TC-22) and React Testing Library web tests (TC-W01 to TC-W33) are **automated**. Mobile tests are **manual** smoke tests.

### 4.0 Application Sanity Check

| TC-ID  | Test Case Name              | Precondition      | Steps                                           | Expected Result                         | Type                    |
|--------|-----------------------------|-------------------|-------------------------------------------------|-----------------------------------------|-------------------------|
| TC-00  | Spring context loads        | Application built | Run `.\mvnw.cmd test` (context initialization)  | Spring ApplicationContext loads without errors | Automated (Integration) |

### 4.1 Authentication (F-01)

| TC-ID  | Test Case Name              | Precondition      | Steps                                           | Expected Result                         | Type        |
|--------|-----------------------------|-------------------|-------------------------------------------------|-----------------------------------------|-------------|
| TC-01  | Register with valid data     | No existing user  | POST /api/auth/register with name/email/password | 200 OK, returns JWT token              | Automated (Integration) |
| TC-02  | Register with duplicate email| User exists       | POST /api/auth/register with existing email     | 409 Conflict                            | Automated (Integration) |
| TC-03  | Register password mismatch   | -                 | POST /api/auth/register, passwords don't match  | 400 Bad Request                         | Automated (Integration) |
| TC-04  | Login with valid credentials | User registered   | POST /api/auth/login with correct credentials   | 200 OK, returns JWT token              | Automated (Integration) |
| TC-05  | Login with wrong password    | User registered   | POST /api/auth/login with wrong password        | 401 Unauthorized                        | Automated (Integration) |
| TC-06  | Login with unknown email     | -                 | POST /api/auth/login with non-existent email    | 401 Unauthorized                        | Automated (Integration) |
| TC-07  | Access protected route without token | - | GET /api/tasks without Authorization header  | 403 Forbidden                           | Automated (Integration) |

### 4.2 Task Management (F-02)

| TC-ID  | Test Case Name              | Precondition      | Steps                                           | Expected Result                         | Type        |
|--------|-----------------------------|-------------------|-------------------------------------------------|-----------------------------------------|-------------|
| TC-08  | Create task                 | Authenticated user| POST /api/tasks with title/priority/status      | 200 OK, task created with correct fields| Automated (Integration) |
| TC-09  | Get all tasks for user      | User has tasks    | GET /api/tasks                                  | 200 OK, list of tasks                   | Automated (Integration) |
| TC-10  | Update existing task        | Task exists       | PUT /api/tasks/{id} with updated fields         | 200 OK, task updated                    | Automated (Integration) |
| TC-11  | Delete task                 | Task exists       | DELETE /api/tasks/{id}                          | 200 OK, task removed                    | Automated (Integration) |
| TC-12  | Access other user's task    | Two users with tasks | DELETE /api/tasks/{otherId} as user A        | 403 Forbidden                           | Automated (Integration) |

### 4.3 Group Management (F-03)

| TC-ID  | Test Case Name              | Precondition          | Steps                                       | Expected Result                         | Type        |
|--------|-----------------------------|-----------------------|---------------------------------------------|-----------------------------------------|-------------|
| TC-13  | Create group                | Authenticated user    | POST /api/groups with name                  | 200 OK, group created                   | Automated (Integration) |
| TC-14  | Get user's groups           | User has groups       | GET /api/groups                             | 200 OK, list of groups                  | Automated (Integration) |
| TC-15  | Add member by email         | Group exists, user exists | POST /api/groups/{id}/members with email | 200 OK, member added                    | Automated (Integration) |
| TC-16  | Delete group by owner       | Group exists          | DELETE /api/groups/{id}                     | 200 OK, group deleted                   | Automated (Integration) |

### 4.4 File Management (F-04)

| TC-ID  | Test Case Name              | Precondition          | Steps                                       | Expected Result                         | Type        |
|--------|-----------------------------|-----------------------|---------------------------------------------|-----------------------------------------|-------------|
| TC-17  | Upload file                 | Authenticated user    | POST /api/files (multipart)                 | 201 Created, file metadata returned     | Automated (Integration) |
| TC-18  | List uploaded files         | Files exist           | GET /api/files                              | 200 OK, list of file metadata           | Automated (Integration) |
| TC-19  | Delete file                 | File exists           | DELETE /api/files/{id}                      | 204 No Content, file removed            | Automated (Integration) |

### 4.5 Security (F-05)

| TC-ID  | Test Case Name              | Precondition          | Steps                                       | Expected Result                         | Type        |
|--------|-----------------------------|-----------------------|---------------------------------------------|-----------------------------------------|-------------|
| TC-20  | JWT is validated per request| Valid JWT             | Request with valid JWT                      | 200 OK                                  | Automated (Unit)        |
| TC-21  | Expired JWT rejected        | Expired JWT           | Request with expired token                  | 403 Forbidden                           | Automated (Unit)        |
| TC-22  | Malformed JWT rejected      | Bad JWT string        | Request with invalid token string           | 403 Forbidden                           | Automated (Unit)        |

### 4.6 Web Frontend Component Tests (F-01 – F-05)

| TC-ID  | Test Case Name                                       | Precondition               | Steps                                                   | Expected Result                                    | Type                    |
|--------|------------------------------------------------------|----------------------------|---------------------------------------------------------|----------------------------------------------------|-------------------------|
| TC-W01 | Login – renders email and password fields            | Component mounted          | Render `<Login />`                                      | Email and password inputs present in DOM           | Automated (Component)   |
| TC-W02 | Login – renders Sign In button                       | Component mounted          | Render `<Login />`                                      | Sign In button present in DOM                      | Automated (Component)   |
| TC-W03 | Login – shows error when email is empty              | Component mounted          | Submit form with blank email field                      | Email required error message shown                 | Automated (Component)   |
| TC-W04 | Login – shows error when email format is invalid     | Component mounted          | Enter "notanemail", submit form                         | Invalid email error message shown                  | Automated (Component)   |
| TC-W05 | Login – shows error when password is missing         | Component mounted          | Submit form with blank password field                   | Password required error message shown              | Automated (Component)   |
| TC-W06 | Login – calls authApi.login and navigates on success | `authApi.login` mocked     | Enter valid credentials, click Sign In                  | API called with credentials; navigate() invoked    | Automated (Component)   |
| TC-W07 | Login – shows API error on wrong credentials         | `authApi.login` rejects    | Enter credentials, click Sign In                        | API error message displayed                        | Automated (Component)   |
| TC-W08 | Register – renders "Create Account" heading          | Component mounted          | Render `<Register />`                                   | "Create Account" heading present                   | Automated (Component)   |
| TC-W09 | Register – renders sign-up submit button             | Component mounted          | Render `<Register />`                                   | Submit button rendered                             | Automated (Component)   |
| TC-W10 | Register – shows error when full name is empty       | Component mounted          | Submit form with blank name field                       | Name required error shown                          | Automated (Component)   |
| TC-W11 | Register – shows error when full name is too short   | Component mounted          | Enter 1-char name, submit form                          | Name too short error shown                         | Automated (Component)   |
| TC-W12 | Register – shows error when email is empty           | Component mounted          | Submit form with blank email field                      | Email required error shown                         | Automated (Component)   |
| TC-W13 | Register – shows error when passwords do not match   | Component mounted          | Enter mismatched passwords, submit                      | "Passwords do not match" error shown               | Automated (Component)   |
| TC-W14 | Register – shows error when password < 8 chars       | Component mounted          | Enter 7-char password, submit                           | "Min 8 characters" error shown                     | Automated (Component)   |
| TC-W15 | Register – calls authApi.register on success         | `authApi.register` mocked  | Fill valid form, submit                                 | API called; navigate to /login                     | Automated (Component)   |
| TC-W16 | Register – shows API error on registration failure   | `authApi.register` rejects | Fill form, submit                                       | API error message displayed                        | Automated (Component)   |
| TC-W17 | Dashboard – shows loading indicator                  | API calls pending          | Render `<Dashboard />` before API resolves              | Loading indicator visible                          | Automated (Component)   |
| TC-W18 | Dashboard – displays correct task counts             | `taskApi` returns tasks    | Render `<Dashboard />` after API resolves               | Task counts match mocked data                      | Automated (Component)   |
| TC-W19 | Dashboard – displays group names                     | `groupApi` returns groups  | Render `<Dashboard />` after API resolves               | Group names from mock displayed                    | Automated (Component)   |
| TC-W20 | Dashboard – shows welcome message with fullName      | localStorage has fullName  | Render `<Dashboard />`                                  | "Welcome, {fullName}" displayed                    | Automated (Component)   |
| TC-W21 | Dashboard – shows fallback 'User' when no fullName   | localStorage empty         | Render `<Dashboard />`                                  | "Welcome, User" fallback displayed                 | Automated (Component)   |
| TC-W22 | Tasks – shows empty state when no tasks              | `taskApi` returns []       | Render `<Tasks />`                                      | Empty state message shown                          | Automated (Component)   |
| TC-W23 | Tasks – renders New Task button                      | Component mounted          | Render `<Tasks />`                                      | New Task button present                            | Automated (Component)   |
| TC-W24 | Tasks – opens create modal on button click           | Component mounted          | Click "New Task" button                                 | Create task modal opens                            | Automated (Component)   |
| TC-W25 | Tasks – filters tasks by search query                | `taskApi` returns tasks    | Type search query in search input                       | Only matching tasks shown; others filtered         | Automated (Component)   |
| TC-W26 | Tasks – calls taskApi.create and refreshes list      | `taskApi.create` mocked    | Fill and submit new task form                           | taskApi.create called; task list refreshed         | Automated (Component)   |
| TC-W27 | Groups – shows empty state when no groups            | `groupApi` returns []      | Render `<Groups />`                                     | Empty state message shown                          | Automated (Component)   |
| TC-W28 | Groups – renders group names from API                | `groupApi` returns groups  | Render `<Groups />`                                     | Group names displayed                              | Automated (Component)   |
| TC-W29 | Groups – shows loading indicator                     | API call pending           | Render `<Groups />` before API resolves                 | Loading indicator visible                          | Automated (Component)   |
| TC-W30 | Groups – shows error when group name is empty        | Component mounted          | Submit form with blank name                             | Name required error shown                          | Automated (Component)   |
| TC-W31 | Groups – shows error when group name is too short    | Component mounted          | Enter 1-char name, submit                               | Name too short error shown                         | Automated (Component)   |
| TC-W32 | Groups – shows error when name below min 2 chars     | Component mounted          | Enter name below minimum length, submit                 | Minimum length validation error shown              | Automated (Component)   |
| TC-W33 | Groups – calls groupApi.create and refreshes list    | `groupApi.create` mocked   | Enter valid name, submit                                | groupApi.create called; group list refreshed       | Automated (Component)   |

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

| Phase | Activity                            | Script / Command                                                                          | Owner |
|-------|-------------------------------------|-------------------------------------------------------------------------------------------|-------|
| 1     | Run all backend JUnit 5 tests       | `cd IT342_Sala_TaskFlow/Backend/TaskFlow && .\mvnw.cmd test`                              | Dev   |
| 2     | Run all web React component tests   | `cd IT342_Sala_TaskFlow/web && npm test -- --watchAll=false`                              | Dev   |
| 2a    | Run Login tests only                | `npm test -- --watchAll=false --testPathPattern=Login`                                    | Dev   |
| 2b    | Run Register tests only             | `npm test -- --watchAll=false --testPathPattern=Register`                                 | Dev   |
| 2c    | Run Dashboard tests only            | `npm test -- --watchAll=false --testPathPattern=Dashboard`                                | Dev   |
| 2d    | Run Tasks tests only                | `npm test -- --watchAll=false --testPathPattern=Tasks`                                    | Dev   |
| 2e    | Run Groups tests only               | `npm test -- --watchAll=false --testPathPattern=Groups`                                   | Dev   |
| 3     | Manual Android smoke test           | Launch app on emulator/device; verify login, tasks, and groups features work end-to-end  | Dev   |
| 4     | Document results                    | Update `FullRegressionReport_TaskFlow.md` with pass/fail for each TC-ID                  | Dev   |
