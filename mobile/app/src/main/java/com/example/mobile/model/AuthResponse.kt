package com.example.mobile.model

data class AuthResponse(
    val token: String,
    val email: String,
    val fullName: String? = null
)
