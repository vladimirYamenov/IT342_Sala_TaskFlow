package edu.cit.sala.TaskFlow;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.sala.TaskFlow.feature.auth.LoginRequest;
import edu.cit.sala.TaskFlow.feature.auth.RegisterRequest;
import edu.cit.sala.TaskFlow.feature.auth.UserRepository;
import edu.cit.sala.TaskFlow.feature.task.TaskRepository;
import edu.cit.sala.TaskFlow.feature.task.TaskRequest;
import edu.cit.sala.TaskFlow.feature.group.GroupRepository;
import edu.cit.sala.TaskFlow.feature.group.GroupRequest;
import edu.cit.sala.TaskFlow.feature.group.AddMemberRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Full Regression Tests for TaskFlow
 *
 * Covers: Authentication (TC-01 to TC-07),
 *         Task Management (TC-08 to TC-12),
 *         Group Management (TC-13 to TC-16)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskFlowApplicationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    GroupRepository groupRepository;

    // Prevent actual email sending during tests
    @MockBean
    JavaMailSender javaMailSender;

    // Shared state across ordered tests
    static String jwtToken;
    static String secondUserToken;
    static Long createdTaskId;
    static Long createdGroupId;

    // =========================================================================
    // Context loads sanity check
    // =========================================================================

    @Test
    @Order(0)
    @DisplayName("TC-00: Spring application context loads successfully")
    void contextLoads() {
        // If this runs, the context loaded correctly
    }

    // =========================================================================
    // Authentication Tests (TC-01 to TC-07)
    // =========================================================================

    @Test
    @Order(1)
    @DisplayName("TC-01: Register with valid data returns 200 and JWT token")
    void tc01_registerValidData() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test User");
        req.setEmail("testuser@example.com");
        req.setPassword("password123");
        req.setConfirmPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("testuser@example.com")))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(body).get("token").asText();
    }

    @Test
    @Order(2)
    @DisplayName("TC-02: Register with duplicate email returns 409 Conflict")
    void tc02_registerDuplicateEmail() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test User");
        req.setEmail("testuser@example.com");
        req.setPassword("password123");
        req.setConfirmPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("TC-03: Register with password mismatch returns 400 Bad Request")
    void tc03_registerPasswordMismatch() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Another User");
        req.setEmail("other@example.com");
        req.setPassword("password123");
        req.setConfirmPassword("different456");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("TC-04: Login with valid credentials returns 200 and JWT token")
    void tc04_loginValidCredentials() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("testuser@example.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @Order(5)
    @DisplayName("TC-05: Login with wrong password returns 401 Unauthorized")
    void tc05_loginWrongPassword() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("testuser@example.com");
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    @DisplayName("TC-06: Login with unknown email returns 401 Unauthorized")
    void tc06_loginUnknownEmail() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@example.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    @DisplayName("TC-07: Access protected route without token returns 403 Forbidden")
    void tc07_protectedRouteWithoutToken() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Task Management Tests (TC-08 to TC-12)
    // =========================================================================

    @Test
    @Order(8)
    @DisplayName("TC-08: Create task with valid data returns 200")
    void tc08_createTask() throws Exception {
        TaskRequest req = new TaskRequest();
        req.setTitle("Write regression tests");
        req.setPriority("HIGH");
        req.setStatus("TODO");

        MvcResult result = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Write regression tests")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        createdTaskId = objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    @Order(9)
    @DisplayName("TC-09: Get all tasks for authenticated user returns 200 and list")
    void tc09_getAllTasks() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(10)
    @DisplayName("TC-10: Update existing task returns 200 with updated fields")
    void tc10_updateTask() throws Exception {
        TaskRequest req = new TaskRequest();
        req.setTitle("Write regression tests - UPDATED");
        req.setPriority("MEDIUM");
        req.setStatus("IN_PROGRESS");

        mockMvc.perform(put("/api/tasks/" + createdTaskId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Write regression tests - UPDATED")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    @Order(11)
    @DisplayName("TC-11: Delete task returns 200")
    void tc11_deleteTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + createdTaskId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(12)
    @DisplayName("TC-12: Second user cannot delete first user's task (403 Forbidden)")
    void tc12_deleteOtherUsersTask() throws Exception {
        // Create a task for user 1
        TaskRequest req = new TaskRequest();
        req.setTitle("User1 private task");
        req.setPriority("LOW");
        req.setStatus("TODO");

        MvcResult result = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        Long taskId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Register second user
        RegisterRequest reg2 = new RegisterRequest();
        reg2.setFullName("Second User");
        reg2.setEmail("second@example.com");
        reg2.setPassword("pass1234");
        reg2.setConfirmPassword("pass1234");

        MvcResult reg2Result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg2)))
                .andExpect(status().isOk())
                .andReturn();
        secondUserToken = objectMapper.readTree(reg2Result.getResponse().getContentAsString()).get("token").asText();

        // Try to delete user1's task as user2
        mockMvc.perform(delete("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isForbidden());

        // Cleanup
        mockMvc.perform(delete("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + jwtToken));
    }

    // =========================================================================
    // Group Management Tests (TC-13 to TC-16)
    // =========================================================================

    @Test
    @Order(13)
    @DisplayName("TC-13: Create group returns 200 with group name")
    void tc13_createGroup() throws Exception {
        GroupRequest req = new GroupRequest();
        req.setName("Dev Team");

        MvcResult result = mockMvc.perform(post("/api/groups")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Dev Team")))
                .andReturn();

        createdGroupId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @Order(14)
    @DisplayName("TC-14: Get user's groups returns 200 and list")
    void tc14_getUserGroups() throws Exception {
        mockMvc.perform(get("/api/groups")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(15)
    @DisplayName("TC-15: Add member to group by email returns 200")
    void tc15_addMemberToGroup() throws Exception {
        AddMemberRequest req = new AddMemberRequest();
        req.setEmail("second@example.com");

        mockMvc.perform(post("/api/groups/" + createdGroupId + "/members")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(16)
    @DisplayName("TC-16: Delete group by owner returns 200")
    void tc16_deleteGroup() throws Exception {
        mockMvc.perform(delete("/api/groups/" + createdGroupId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }
}

