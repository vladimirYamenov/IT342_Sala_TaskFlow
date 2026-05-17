package com.example.mobile.API

import com.example.mobile.model.*
import com.example.mobile.model.Task
import retrofit2.Response
import retrofit2.http.*

interface GroupApiService {

    @GET("groups")
    suspend fun getGroups(): Response<List<Group>>

    @GET("groups/{id}")
    suspend fun getGroup(@Path("id") id: Long): Response<Group>

    @POST("groups")
    suspend fun createGroup(@Body request: GroupRequest): Response<Group>

    @PUT("groups/{id}")
    suspend fun renameGroup(@Path("id") id: Long, @Body request: RenameGroupRequest): Response<Group>

    @DELETE("groups/{id}")
    suspend fun deleteGroup(@Path("id") id: Long): Response<Void>

    @POST("groups/{id}/members")
    suspend fun addMember(@Path("id") id: Long, @Body request: AddMemberRequest): Response<Void>

    @DELETE("groups/{id}/members/{userId}")
    suspend fun removeMember(@Path("id") id: Long, @Path("userId") userId: Long): Response<Void>

    @GET("groups/{id}/tasks")
    suspend fun getGroupTasks(@Path("id") id: Long): Response<List<Task>>

    // Leave a group you joined/were added to (calls removeMember with your own userId)
    @DELETE("groups/{id}/leave")
    suspend fun leaveGroup(@Path("id") id: Long): Response<Void>
}
