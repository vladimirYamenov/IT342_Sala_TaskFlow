package com.example.mobile.API

import com.example.mobile.model.Task
import com.example.mobile.model.TaskRequest
import retrofit2.Response
import retrofit2.http.*

interface TaskApiService {

    @GET("tasks")
    suspend fun getTasks(
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null
    ): Response<List<Task>>

    @POST("tasks")
    suspend fun createTask(@Body request: TaskRequest): Response<Task>

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: Long, @Body request: TaskRequest): Response<Task>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Long): Response<Void>
}
