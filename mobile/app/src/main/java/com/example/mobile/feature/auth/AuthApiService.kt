package com.example.mobile.feature.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.mobile.feature.auth.RegisterRequest
import com.example.mobile.feature.auth.AuthResponse
import com.example.mobile.feature.auth.LoginRequest
import okhttp3.ResponseBody


interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}