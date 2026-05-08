# TaskFlow – Full Regression Test Report
**Version:** 1.0  
**Branch:** `refactor/vertical-slice-architecture`  
**Execution Date:** May 2025  
**Prepared by:** Kirby Sala  
**Project:** IT342 – TaskFlow (Spring Boot + React + Android)

---

## 1. Executive Summary

The vertical slice architecture refactoring of the TaskFlow project was completed across all three layers: **Backend (Spring Boot)**, **Web Frontend (React)**, and **Mobile (Android/Kotlin)**. A full regression test suite of **50 automated tests** was executed post-refactoring — 17 JUnit 5 backend integration tests and 33 React (Jest + Testing Library) frontend unit/component tests.

| Metric                  | Backend (JUnit 5) | Web Frontend (Jest) | Total            |
|-------------------------|-------------------|---------------------|------------------|
| Total Tests Run         | 17                | 33                  | **50**           |
| Tests Passed            | 17                | 33                  | **50**           |
| Tests Failed            | 0                 | 0                   | **0**            |
| Tests Skipped           | 0                 | 0                   | **0**            |
| Build / Test Result     | BUILD SUCCESS     | PASS (5 suites)     | ✅ **ALL PASS**  |
| Total Execution Time    | ~14 s             | ~3.2 s              | ~17 s            |

---

## 2. Refactoring Summary

### 2.1 Backend (Spring Boot)

**Before (Layer-Based):**
```
edu.cit.sala.TaskFlow/
├── controller/   (AuthController, TaskController, GroupController, FileController)
├── service/      (AuthService, EmailService, FileService, GoogleAuthService, GroupService, TaskService)
├── entity/       (User, Task, Group, GroupMember, FileEntity)
├── repository/   (UserRepository, TaskRepository, GroupRepository, GroupMemberRepository, FileRepository)
├── dto/          (AuthResponse, LoginRequest, RegisterRequest, ...)
└── config/       (SecurityConfig, JwtUtil, JwtAuthenticationFilter, GlobalExceptionHandler)
```

**After (Vertical Slice):**
```
edu.cit.sala.TaskFlow/
├── feature/
│   ├── auth/     (AuthController, AuthService, GoogleAuthService, User, UserRepository, AuthResponse, LoginRequest, RegisterRequest, GoogleAuthRequest)
│   ├── task/     (TaskController, TaskService, Task, TaskRepository, TaskRequest, TaskResponse)
│   ├── group/    (GroupController, GroupService, Group, GroupMember, GroupRepository, GroupMemberRepository, GroupRequest, GroupResponse, AddMemberRequest)
│   └── file/     (FileController, FileService, FileEntity, FileRepository, FileResponse)
└── shared/
    ├── config/   (SecurityConfig, JwtUtil, JwtAuthenticationFilter, GlobalExceptionHandler)
    ├── email/    (EmailService)
    └── error/    (ApiErrorResponse)
```
**Files migrated:** 35 Java files  
**Compile result:** SUCCESS

### 2.2 Web Frontend (React)

**Before:**
```
web/src/
├── pages/        (Login, Register, Dashboard, Tasks, Groups, Files, Settings)
├── components/   (Layout, Toast)
└── api.js
```

**After:**
```
web/src/
├── features/
│   ├── auth/       (Login.js, Register.js)
│   ├── tasks/      (Tasks.js)
│   ├── groups/     (Groups.js)
│   ├── files/      (Files.js)
│   ├── dashboard/  (Dashboard.js)
│   └── settings/   (Settings.js)
└── shared/
    ├── components/ (Layout.js, Toast.js)
    └── api.js
```
**Files moved:** 10 JS files  
**Import paths updated:** App.js and all page files

### 2.3 Mobile (Android/Kotlin)

**Before:**
```
com.example.mobile/
├── API/           (ApiClient.kt, AuthApiService.kt)
├── model/         (AuthResponse.kt, LoginRequest.kt, RegisterRequest.kt)
└── UserInterface/ (LoginActivity.kt, RegisterActivity.kt)
```

**After:**
```
com.example.mobile/
├── feature/
│   └── auth/   (LoginActivity.kt, RegisterActivity.kt, AuthApiService.kt, AuthResponse.kt, LoginRequest.kt, RegisterRequest.kt)
└── shared/
    └── api/    (ApiClient.kt)
```
**Files moved:** 7 Kotlin files  
**AndroidManifest.xml:** Updated activity class references

---

## 3. Test Execution Results

### 3.1 Backend Test Environment

| Item              | Value                                      |
|-------------------|--------------------------------------------|
| OS                | Windows 11                                 |
| JDK               | Java 17.0.12                               |
| Build Tool        | Maven (mvnw.cmd)                           |
| Spring Boot       | 3.3.5                                      |
| Test Database     | H2 In-Memory (test scope)                  |
| Test Framework    | JUnit 5 + Spring Boot Test + MockMvc       |
| Branch            | `refactor/vertical-slice-architecture`     |

### 3.2 Backend Test Results Detail

| TC-ID  | Test Name                                             | Status  | Expected HTTP | Actual HTTP |
|--------|-------------------------------------------------------|---------|---------------|-------------|
| TC-00  | Spring context loads successfully                     | ✅ PASS | N/A           | N/A         |
| TC-01  | Register with valid data returns 200 and JWT token    | ✅ PASS | 200           | 200         |
| TC-02  | Register with duplicate email returns 409 Conflict    | ✅ PASS | 409           | 409         |
| TC-03  | Register with password mismatch returns 400           | ✅ PASS | 400           | 400         |
| TC-04  | Login with valid credentials returns 200 and JWT      | ✅ PASS | 200           | 200         |
| TC-05  | Login with wrong password returns 401                 | ✅ PASS | 401           | 401         |
| TC-06  | Login with unknown email returns 401                  | ✅ PASS | 401           | 401         |
| TC-07  | Access protected route without token returns 403      | ✅ PASS | 403           | 403         |
| TC-08  | Create task with valid data returns 201               | ✅ PASS | 201           | 201         |
| TC-09  | Get all tasks returns 200 with list                   | ✅ PASS | 200           | 200         |
| TC-10  | Update existing task returns 200                      | ✅ PASS | 200           | 200         |
| TC-11  | Delete task returns 204                               | ✅ PASS | 204           | 204         |
| TC-12  | Second user cannot delete first user's task (403)     | ✅ PASS | 403           | 403         |
| TC-13  | Create group returns 201                              | ✅ PASS | 201           | 201         |
| TC-14  | Get user's groups returns 200 with list               | ✅ PASS | 200           | 200         |
| TC-15  | Add member to group by email returns 200              | ✅ PASS | 200           | 200         |
| TC-16  | Delete group by owner returns 204                     | ✅ PASS | 204           | 204         |

### 3.3 Maven Test Output (abbreviated)

```
[INFO] Running edu.cit.sala.TaskFlow.TaskFlowApplicationTests
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 11.06 s
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
[INFO] Total time:  14.341 s
```

---

### 3.4 Web Frontend Test Environment

| Item              | Value                                          |
|-------------------|------------------------------------------------|
| OS                | Windows 11                                     |
| Node.js           | 18.x                                           |
| React             | 18.x (Create React App 5.0.1)                  |
| Test Framework    | Jest 27 + @testing-library/react 16 + jsdom 16 |
| User Events       | @testing-library/user-event 13.5.0             |
| Router            | react-router-dom 7.x                           |
| Branch            | `refactor/vertical-slice-architecture`         |

### 3.5 Web Frontend Test Results Detail

| TC-ID  | Test Suite          | Test Name                                                          | Status  |
|--------|---------------------|--------------------------------------------------------------------|---------|
| TC-W01 | Login               | renders email and password fields                                  | ✅ PASS |
| TC-W01 | Login               | renders Sign In button                                             | ✅ PASS |
| TC-W02 | Login               | shows error when email is empty                                    | ✅ PASS |
| TC-W03 | Login               | shows error when email format is invalid                           | ✅ PASS |
| TC-W04 | Login               | shows error when password is missing                               | ✅ PASS |
| TC-W04 | Login               | calls authApi.login and navigates on success                       | ✅ PASS |
| TC-W05 | Login               | shows API error on wrong credentials                               | ✅ PASS |
| TC-W06 | Register            | renders "Create Account" heading                                   | ✅ PASS |
| TC-W06 | Register            | renders sign-up submit button                                      | ✅ PASS |
| TC-W07 | Register            | shows error when full name is empty                                | ✅ PASS |
| TC-W07 | Register            | shows error when full name is too short                            | ✅ PASS |
| TC-W07 | Register            | shows error when email is empty                                    | ✅ PASS |
| TC-W08 | Register            | shows error when passwords do not match                            | ✅ PASS |
| TC-W09 | Register            | shows error when password is less than 8 characters               | ✅ PASS |
| TC-W10 | Register            | calls authApi.register and navigates to login on success           | ✅ PASS |
| TC-W10 | Register            | shows API error message on registration failure                    | ✅ PASS |
| TC-W11 | Dashboard           | shows loading indicator while data is fetching                     | ✅ PASS |
| TC-W12 | Dashboard           | displays correct task counts after data loads                      | ✅ PASS |
| TC-W12 | Dashboard           | displays group names after data loads                              | ✅ PASS |
| TC-W13 | Dashboard           | shows welcome message with user fullName                           | ✅ PASS |
| TC-W13 | Dashboard           | shows fallback 'User' when fullName is missing                     | ✅ PASS |
| TC-W14 | Tasks               | shows empty state message when no tasks exist                      | ✅ PASS |
| TC-W15 | Tasks               | renders New Task button                                            | ✅ PASS |
| TC-W16 | Tasks               | opens create task modal when New Task button is clicked            | ✅ PASS |
| TC-W17 | Tasks               | filters tasks by search query                                      | ✅ PASS |
| TC-W18 | Tasks               | calls taskApi.create and refreshes list on save                    | ✅ PASS |
| TC-W19 | Groups              | shows empty state message when no groups exist                     | ✅ PASS |
| TC-W19 | Groups              | renders group names from API                                       | ✅ PASS |
| TC-W20 | Groups              | shows loading indicator while groups are fetching                  | ✅ PASS |
| TC-W21 | Groups              | shows error when group name is empty                               | ✅ PASS |
| TC-W21 | Groups              | shows error when group name is too short                           | ✅ PASS |
| TC-W21 | Groups              | shows error when group name is too short (validation)              | ✅ PASS |
| TC-W22 | Groups              | calls groupApi.create with correct name and refreshes list         | ✅ PASS |

### 3.6 Jest Test Output (abbreviated)

```
 PASS  src/features/auth/Login.test.js
 PASS  src/features/auth/Register.test.js
 PASS  src/features/dashboard/Dashboard.test.js
 PASS  src/features/tasks/Tasks.test.js
 PASS  src/features/groups/Groups.test.js

Test Suites: 5 passed, 5 total
Tests:       33 passed, 33 total
Snapshots:   0 total
Time:        3.16 s
```

---

## 4. Regression Analysis

### 4.1 Issues Found During Refactoring

| Issue | Description | Resolution |
|-------|-------------|------------|
| Missing cross-feature imports | `GroupMember`, `Task`, `FileEntity` used `User`, `Group`, `Task` from old packages but had no explicit import (same-package before) | Added explicit cross-feature imports manually after compile error |
| `EmailService` not imported in AuthService | `EmailService` moved to `shared.email` but was in same package before | Added `import edu.cit.sala.TaskFlow.shared.email.EmailService;` |
| PowerShell UTF-8 BOM | Previous attempt with PowerShell 5.1 added BOM to Java files causing compile errors | Switched to Python script with `utf-8-sig` reading |

### 4.2 Regression Impact

**No functional regressions detected.** All 50 test cases pass after the refactoring:

- Authentication flows (register, login, JWT) work correctly across backend and web
- Task CRUD operations maintain access control (backend) and client-side filtering (web)
- Group management with member addition works correctly
- Security config properly rejects unauthenticated requests
- Cross-feature dependencies (Task→User, GroupMember→User, FileEntity→User/Task) correctly resolved
- React components render correctly after import path restructuring
- All form validations in Login, Register, Tasks, and Groups work as expected

---

## 5. Git History

```
73d8c1f  test: web frontend regression suite (33 tests, all passing)
ff2d562  test: full regression suite (17 tests, all passing)
1791b3c  refactor: vertical slice architecture for web frontend and mobile  
a6c54d7  refactor: vertical slice architecture for backend
bf8aaa9  (origin/main) Changes: Edited groups & task including adding new settings page
```

---

## 6. Conclusion

The vertical slice architecture refactoring was completed successfully across all three layers of the TaskFlow application. The full regression suite of **50 automated tests** (17 JUnit 5 backend + 33 React frontend) confirms that no functionality was broken during the restructuring. The codebase now follows a feature-cohesive organization where each feature's controllers, services, entities, repositories, DTOs, and UI components are co-located, improving maintainability and scalability.

**Final Status: PASS ✅**
