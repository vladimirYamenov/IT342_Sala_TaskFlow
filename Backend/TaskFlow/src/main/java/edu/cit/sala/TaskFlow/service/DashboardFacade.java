package edu.cit.sala.TaskFlow.service;

import edu.cit.sala.TaskFlow.dto.DashboardResponse;
import edu.cit.sala.TaskFlow.dto.GroupResponse;
import edu.cit.sala.TaskFlow.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Facade Pattern - Provides a simplified interface to the complex subsystem of
 * TaskService and GroupService.
 *
 * Before: The frontend Dashboard had to make separate API calls to /api/tasks
 * and /api/groups, then aggregate stats (total, completed, in-progress, pending)
 * on the client side.
 *
 * After: DashboardFacade provides a single method that coordinates between
 * TaskService and GroupService, computes statistics server-side, and returns
 * a unified DashboardResponse. This reduces network calls and moves
 * aggregation logic to the backend where it belongs.
 */
@Service
@RequiredArgsConstructor
public class DashboardFacade {

    private final TaskService taskService;
    private final GroupService groupService;

    public DashboardResponse getDashboardData(Long userId) {
        // Delegate to subsystems
        List<TaskResponse> allTasks = taskService.getUserTasks(userId, null, null);
        List<GroupResponse> groups = groupService.getUserGroups(userId);

        // Aggregate statistics
        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();
        long inProgressTasks = allTasks.stream()
                .filter(t -> "IN_PROGRESS".equals(t.getStatus()))
                .count();
        long pendingTasks = allTasks.stream()
                .filter(t -> "TODO".equals(t.getStatus()) || "PENDING".equals(t.getStatus()))
                .count();

        // Get most recent 5 tasks
        List<TaskResponse> recentTasks = allTasks.stream()
                .sorted(Comparator.comparing(TaskResponse::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();

        return DashboardResponse.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .pendingTasks(pendingTasks)
                .recentTasks(recentTasks)
                .groups(groups)
                .build();
    }
}
