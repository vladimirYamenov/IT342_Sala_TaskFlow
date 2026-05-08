package edu.cit.sala.TaskFlow.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDate dueDate;
    private Long groupId;
    private List<Long> assignedUserIds;
}
