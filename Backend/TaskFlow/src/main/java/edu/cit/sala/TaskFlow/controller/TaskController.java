package edu.cit.sala.TaskFlow.controller;

import edu.cit.sala.TaskFlow.dto.TaskRequest;
import edu.cit.sala.TaskFlow.dto.TaskResponse;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request,
                                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TaskResponse task = taskService.createTask(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(@RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String priority,
                                                       Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<TaskResponse> tasks = taskService.getUserTasks(user.getId(), status, priority);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id,
                                                Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TaskResponse task = taskService.getTaskById(id, user.getId());
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                   @RequestBody TaskRequest request,
                                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TaskResponse task = taskService.updateTask(id, request, user.getId());
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
                                           Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        taskService.deleteTask(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
