package com.example.examenTasks.manager

import com.example.examenTasks.POJO.Task
import com.example.examenTasks.POJO.ServerResponse
import com.example.examenTasks.api.API
import com.example.examenTasks.api.RetrofitFactory
import org.json.JSONObject
import retrofit2.Call

class TaskManager {
    private val api = RetrofitFactory()
        .getRetrofitInstance().create(API::class.java)

    fun getAllTasksServer(): Call<List<Task>> {
        return api.getTasks()
    }

    fun getAllTasksServerUpdated(m: Long): Call<List<Task>> {
        return api.getTasksPaginated(m)
    }

    fun addTaskServer(event: Task): Call<Task> {
        return api.addTask(event)
    }

    fun deleteTaskServer(id: Int): Call<Task> {
        return api.deleteTask(id)
    }

    fun updateTaskServer(event: Task): Call<Task> {
        return api.updateTask(event.id, event)
    }


}