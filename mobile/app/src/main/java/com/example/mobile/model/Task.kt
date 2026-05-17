package com.example.mobile.model

data class Task(
    val id: Long = 0,
    val title: String = "",
    val description: String? = null,
    val priority: String = "MEDIUM",   // HIGH, MEDIUM, LOW
    val status: String = "TODO",       // TODO, IN_PROGRESS, PENDING, COMPLETED
    val dueDate: String? = null,
    val groupId: Long? = null,
    val assignedUsers: List<AssignedUser>? = null,
    val createdAt: String? = null
)

data class AssignedUser(
    val id: Long = 0,
    val fullName: String? = null,
    val email: String? = null
)

data class TaskRequest(
    val title: String,
    val description: String?,
    val priority: String,
    val status: String,
    val dueDate: String?,
    val groupId: Long?,
    val assignedUserIds: List<Long>
)
