package com.example.mobile.feature.auth

data class AuthResponse(
    val id: Long?,
    val email: String?,
    val fullName: String?,
    val password: String?
)