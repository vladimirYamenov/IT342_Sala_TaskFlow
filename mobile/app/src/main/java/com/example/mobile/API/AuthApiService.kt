package com.example.mobile.API

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.mobile.model.RegisterRequest
import com.example.mobile.model.AuthResponse
import com.example.mobile.model.LoginRequest
import okhttp3.ResponseBody


interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}