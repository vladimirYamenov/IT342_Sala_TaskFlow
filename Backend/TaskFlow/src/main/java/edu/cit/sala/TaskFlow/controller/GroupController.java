package edu.cit.sala.TaskFlow.controller;

import edu.cit.sala.TaskFlow.dto.AddMemberRequest;
import edu.cit.sala.TaskFlow.dto.GroupRequest;
import edu.cit.sala.TaskFlow.dto.GroupResponse;
import edu.cit.sala.TaskFlow.dto.TaskResponse;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.service.GroupService;
import edu.cit.sala.TaskFlow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody GroupRequest request,
                                                     Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        GroupResponse group = groupService.createGroup(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getGroups(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<GroupResponse> groups = groupService.getUserGroups(user.getId());
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long id,
                                                  Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        GroupResponse group = groupService.getGroupById(id, user.getId());
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<GroupResponse> addMember(@PathVariable Long id,
                                                   @RequestBody AddMemberRequest request,
                                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        GroupResponse group = groupService.addMemberByEmail(id, request.getEmail(), user.getId());
        return ResponseEntity.ok(group);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id,
                                             @PathVariable Long userId,
                                             Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        groupService.removeMember(id, userId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskResponse>> getGroupTasks(@PathVariable Long id,
                                                            @RequestParam(required = false) String status,
                                                            @RequestParam(required = false) String priority,
                                                            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<TaskResponse> tasks = taskService.getGroupTasks(id, user.getId(), status, priority);
        return ResponseEntity.ok(tasks);
    }
}
