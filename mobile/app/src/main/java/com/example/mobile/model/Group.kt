package com.example.mobile.model

data class Group(
    val id: Long = 0,
    val name: String = "",
    val members: List<GroupMember>? = null
)

data class GroupMember(
    val userId: Long = 0,
    val fullName: String? = null,
    val email: String? = null,
    val role: String? = null
)

data class GroupRequest(val name: String)

data class AddMemberRequest(val email: String)

data class RenameGroupRequest(val name: String)
