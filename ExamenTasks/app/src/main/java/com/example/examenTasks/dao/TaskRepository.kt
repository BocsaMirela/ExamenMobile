package com.example.examenTasks.dao

import android.os.AsyncTask
import android.util.Log
import com.example.examenTasks.POJO.Task
import java.lang.Exception

class TaskRepository(private val noteDAO: TaskDAO) {

    fun getAllTasks(): List<Task> {
        return LoadAsyncTask(noteDAO).execute().get()
    }

    fun deleteTask(task: Task) {
        DeleteAsyncTask(noteDAO).execute(task)
    }

    fun deleteAllTask():Boolean {
        return DeleteAllAsyncTask(noteDAO).execute().get()
    }


    fun updateTask(task: Task) {
        UpdateAsyncTask(noteDAO).execute(task)
    }
    fun addTask(task: Task): Long {
        return AddAsyncTask(noteDAO).execute(task).get()
    }

    companion object {
        private var instance: TaskRepository? = null

        fun getInstance(noteDAO: TaskDAO): TaskRepository {
            if (instance == null) {
                instance =
                    TaskRepository(noteDAO)
            }
            return instance as TaskRepository
        }
    }

    private class LoadAsyncTask(val noteDAOTask: TaskDAO) :
        AsyncTask<Void, Void, List<Task>>() {
        override fun doInBackground(vararg params: Void?): List<Task> {
            val choco = noteDAOTask.getTasks()
            Log.e(" from db all ", choco.size.toString())
            return choco
        }

    }

    private class AddAsyncTask(val noteDAOTask: TaskDAO) :
        AsyncTask<Task, Void, Long>() {
        override fun doInBackground(vararg params: Task?): Long {
            return try {
                noteDAOTask.insert(params[0]!!)
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }
    }

    private class UpdateAsyncTask(val noteDAOTask: TaskDAO) :
        AsyncTask<Task, Void, Boolean>() {
        override fun doInBackground(vararg params: Task?): Boolean {
            return try {
                noteDAOTask.update(params[0]!!)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private class DeleteAsyncTask(val noteDAOTask: TaskDAO) :
        AsyncTask<Task, Void, Boolean>() {
        override fun doInBackground(vararg params: Task?): Boolean {
            return try {
                noteDAOTask.delete(params[0]!!)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    private class DeleteAllAsyncTask(val noteDAOTask: TaskDAO) :
        AsyncTask<Task, Void, Boolean>() {
        override fun doInBackground(vararg params: Task?): Boolean {
            return try {
                noteDAOTask.deleteAll()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

}
