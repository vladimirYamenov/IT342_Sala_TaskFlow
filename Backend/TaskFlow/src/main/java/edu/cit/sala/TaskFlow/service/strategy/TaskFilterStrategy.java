package edu.cit.sala.TaskFlow.service.strategy;

import edu.cit.sala.TaskFlow.entity.Task;

import java.util.List;

/**
 * Strategy Pattern - Strategy interface.
 * Defines a contract for filtering tasks.
 * Different implementations handle different filter combinations.
 */
public interface TaskFilterStrategy {

    List<Task> filter(Long scopeId, String status, String priority);
}
