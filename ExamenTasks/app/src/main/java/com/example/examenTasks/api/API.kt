package com.example.examenTasks.api

import com.example.examenTasks.POJO.Task
import com.example.examenTasks.POJO.ServerResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.DELETE
import retrofit2.http.PUT


interface API {

    @GET(".")
    fun getTasks(): Call<List<Task>>

    @GET(".")
    fun getTasksPaginated(@Query("lastUpdated") m: Long): Call<List<Task>>

    @POST(".")
    fun addTask(@Body Task: Task): Call<Task>

    @PUT("{id}")
    fun updateTask(@Path("id") id: Int, @Body Task: Task): Call<Task>

    @DELETE("{id}")
    fun deleteTask(@Path("id") id: Int): Call<Task>

    @GET("check")
    fun check(): Call<String>

    companion object {
        val BASE_URL = AppResource.BASE_URL
        val IP = AppResource.IP
    }
}
