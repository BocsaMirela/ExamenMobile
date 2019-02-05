package com.example.examenTasks.viewModel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.widget.Toast
import com.example.examenTasks.POJO.Task
import com.example.examenTasks.dao.TaskDatabase
import com.example.examenTasks.dao.TaskRepository
import com.example.examenTasks.manager.TaskManager
import org.json.JSONObject

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TasksViewModel(application: Application) : ViewModel() {

    private val noteManager = TaskManager()
    private val noteRepository: TaskRepository =
        TaskRepository.getInstance(TaskDatabase.getAppDatabase(application).noteDAO())

    var items: MutableLiveData<List<TaskViewModel>> = MutableLiveData()

    fun getTasksFromServer(): Call<List<Task>> {
        return noteManager.getAllTasksServer()
    }

    fun deleteAllDB(): Boolean {
        return noteRepository.deleteAllTask()
    }

    fun updateTaskServer(task: Task): Call<Task> {
        return noteManager.updateTaskServer(task)
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TasksViewModel(application) as T
        }
    }

    fun getAllTasks(): List<Task> {
        return noteRepository.getAllTasks()
    }

    fun addTask(task: Task): Long {
        return noteRepository.addTask(task)
    }

    fun deleteTask(task: Task) {
        return noteRepository.deleteTask(task)
    }

    fun updateTask(task: Task) {
        noteRepository.updateTask(task)
    }
}
