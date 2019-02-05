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
import java.util.*

class DetailsActivity : AppCompatActivity() {
    private lateinit var task: Task
    private lateinit var taskViewModel: TasksViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_layout)
        val item = intent.getParcelableExtra<Task>("item")
        val conflict = intent.getStringExtra("conflictValue") ?: ""
        intentUI(item, conflict)
        taskViewModel = ViewModelProviders.of(this, TasksViewModel.Factory(application))
            .get(TasksViewModel::class.java)
    }

    private fun intentUI(item: Task?, conflict: String) {
        item?.also {
            findViewById<TextView>(R.id.textViewId).text = item.id.toString()
            findViewById<TextView>(R.id.textViewText).text = item.text
            findViewById<TextView>(R.id.textViewTextData).text = Date(item.updated).toString()
            findViewById<TextView>(R.id.textViewTextStatus).text = item.status
            task = item
            if (conflict == "") {
                findViewById<TextView>(R.id.textViewConflict).visibility = View.GONE
                findViewById<TextView>(R.id.textViewConflictValue).visibility = View.GONE
                findViewById<TextView>(R.id.textViewConflictValue).text = ""
            } else {
                findViewById<TextView>(R.id.textViewConflictValue).text = conflict
            }

        }

    }

    override fun onBackPressed() {
        val text = findViewById<EditText>(R.id.textViewText).text.toString()
        val textConflict = findViewById<TextView>(R.id.textViewConflictValue).text.toString()
        task.text = text
        val resultIntent = Intent()
        resultIntent.putExtra("item", task)
        resultIntent.putExtra("conflictValue", textConflict)
        setResult(Activity.RESULT_OK, resultIntent)
        this@DetailsActivity.finish()
        super.onBackPressed()
    }
}