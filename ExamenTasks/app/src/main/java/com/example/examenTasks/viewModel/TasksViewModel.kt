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

    fun getTasksFromServer(m: Long): Call<List<Task>> {
        return noteManager.getAllTasksServerUpdated(m)
    }


    fun addTaskServer(context: Context, event: Task) {
//        noteManager.addTaskServer(event).enqueue(object : Callback<Task> {
//            override fun onFailure(call: Call<Task>, t: Throwable) {
//                Toast.makeText(
//                    context,
//                    "Something went wrong or no connection! The new event will be saved in your local data",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//
//            override fun onResponse(call: Call<Task>, response: Response<Task>) {
//                Toast.makeText(context, "Add was done", Toast.LENGTH_LONG).show()
//                val listOfItems = items.value as ArrayList<Task>
//                listOfItems.add(event)
//                items.value = listOfItems
//            }
//
//        })
    }

    fun deleteTaskServer(id: Int, context: Context) {
        noteManager.deleteTaskServer(id)
            .enqueue(object : Callback<Task> {
                override fun onFailure(call: Call<Task>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Something went wrong or no connection! Delete from local data",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onResponse(call: Call<Task>, response: Response<Task>) {
                    Toast.makeText(context, "Delete done", Toast.LENGTH_LONG).show()
                }

            })
    }

    fun updateTaskServer(task: Task): Call<Task> {
        return noteManager.updateTaskServer(task)
    }

//    fun updateTaskServer(context: Context, event: Task) {
//        noteManager.updateTaskServer(event).enqueue(object : Callback<Task> {
//            override fun onFailure(call: Call<Task>, t: Throwable) {
//                Toast.makeText(
//                    context,
//                    "Something went wrong or no connection! Update from local data",
//                    Toast.LENGTH_LONG
//                ).show()
//                val listOfItems = items.value as ArrayList<Task>
//                listOfItems.remove(event)
//                listOfItems.add(event)
//                items.value = listOfItems
//            }
//
//            override fun onResponse(call: Call<Task>, response: Response<Task>) {
//                Toast.makeText(context, "Update done", Toast.LENGTH_LONG).show()
//                val listOfItems = items.value as ArrayList<Task>
//                listOfItems.remove(event)
//                listOfItems.add(event)
//                items.value = listOfItems
//
//            }
//
//        })
//    }

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
