package com.example.examenTasks

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.examenTasks.POJO.Task
import com.example.examenTasks.viewModel.TaskViewModel
import com.example.examenTasks.viewModel.TasksViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailsActivity : AppCompatActivity() {
    private lateinit var task: Task
    private lateinit var taskViewModel: TasksViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_layout)
        val item = intent.getParcelableExtra<Task>("item")
        intentUI(item)
        taskViewModel = ViewModelProviders.of(this, TasksViewModel.Factory(application))
            .get(TasksViewModel::class.java)
    }

    private fun intentUI(item: Task?) {
        item?.also {
            findViewById<TextView>(R.id.textViewId).text = item.id.toString()
            findViewById<TextView>(R.id.textViewText).text = item.text
            findViewById<TextView>(R.id.textViewConflictVersion).text = item.version.toString()
            task = item

        }

    }

    fun onUpdate(v: View) {
        val text = findViewById<EditText>(R.id.textViewText).text.toString()
        val textConflict = findViewById<TextView>(R.id.textViewConflictVersion).text.toString()
        task.text = text
        task.version = textConflict.toInt()
//        taskViewModel.updateTask(task)

        taskViewModel.updateTaskServer(task).enqueue(object : Callback<Task> {
            override fun onFailure(call: Call<Task>, t: Throwable) {
                val resultIntent = Intent()
                resultIntent.putExtra("item", task)
                resultIntent.putExtra("ok", false)
                setResult(Activity.RESULT_OK, resultIntent)
                this@DetailsActivity.finish()
            }

            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                when {
                    response.code() == 200 -> {
                        val taskR = response.body()!!
                        val resultIntent = Intent()
                        resultIntent.putExtra("item", taskR)
                        resultIntent.putExtra("ok", true)
                        setResult(Activity.RESULT_OK, resultIntent)
                        this@DetailsActivity.finish()
                    }
                    response.code() == 409 -> {
                        getDataFromServer(task.id)


                    }
                }
            }
        })

    }

    private fun createDialogErrorMessage(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.create()
        builder.show()
    }

    private fun getDataFromServer(idOld: Int) {
        taskViewModel.getTasksFromServer().enqueue(object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                response.body()?.also {
                    val item = it.find { it -> it.id == idOld }
                    val newValue = item!!.text
                    createDialogErrorMessage("Version Conflict: New Value $newValue ")
                    findViewById<TextView>(R.id.textViewText).text = newValue
                    findViewById<TextView>(R.id.textViewConflictVersion).text = item.version.toString()
                }
            }

            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
            }
        })

    }
}