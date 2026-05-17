package com.example.mobile.API

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    // Call ApiClient.init(context) once in LoginActivity after login succeeds
    private var token: String? = null

    fun setToken(t: String?) { token = t }
    fun getToken(): String? = token

    private fun buildRetrofit(): Retrofit {
        val authInterceptor = Interceptor { chain ->
            val req = chain.request().newBuilder().apply {
                token?.let { addHeader("Authorization", "Bearer $it") }
            }.build()
            chain.proceed(req)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private val retrofit by lazy { buildRetrofit() }

    val authService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val taskService: TaskApiService by lazy { retrofit.create(TaskApiService::class.java) }
    val groupService: GroupApiService by lazy { retrofit.create(GroupApiService::class.java) }
}
