package com.example.examenTasks

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.examenTasks.POJO.Task
import com.example.examenTasks.viewModel.TasksViewModel

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

    fun onUpdate(v:View) {
        val text = findViewById<EditText>(R.id.textViewText).text.toString()
        val textConflict = findViewById<TextView>(R.id.textViewConflictVersion).text.toString()
        task.text = text
        val resultIntent = Intent()
        resultIntent.putExtra("item", task)
        setResult(Activity.RESULT_OK, resultIntent)
        this@DetailsActivity.finish()
        super.onBackPressed()
    }
}