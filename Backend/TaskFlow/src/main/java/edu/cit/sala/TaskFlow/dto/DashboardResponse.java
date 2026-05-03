package edu.cit.sala.TaskFlow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Facade Pattern - Unified response for the Dashboard.
 * Aggregates data from multiple subsystems (tasks, groups) into one response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long pendingTasks;
    private List<TaskResponse> recentTasks;
    private List<GroupResponse> groups;
}
