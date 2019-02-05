package com.example.examenTasks.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.examenTasks.POJO.Task
import com.example.examenTasks.R
import com.example.examenTasks.Utils.OnClickInterface
import com.example.examenTasks.viewModel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TaskAdapter : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    private var taskViewModelsList: List<TaskViewModel> = ArrayList()
    var task: List<Task> = ArrayList()
    private val TITLE = 0
    private val LOAD_MORE = 1
    var clickListener: OnClickInterface? = null


    fun setTasksList(list: List<TaskViewModel>) {
        taskViewModelsList = list
    }

    fun getTasksList(): List<TaskViewModel> {
        return taskViewModelsList
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val view = layoutInflater.inflate(R.layout.row_layout, viewGroup, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return taskViewModelsList.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, p: Int) {
        val event = taskViewModelsList[p]
        viewHolder.txtDescription.text = event.task.text
        viewHolder.txtVersion.text = "Version: " + event.task.version.toString()
        viewHolder.txtNotSent.text = event.status

    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {


        var txtDescription: TextView = view.findViewById(R.id.chocolateDescription)
        var txtVersion: TextView = view.findViewById(R.id.chocolateVersion)
        var txtNotSent: TextView = view.findViewById(R.id.chocolateResult)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.onClick(view, adapterPosition); }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position < itemCount) {
            TITLE
        } else {
            LOAD_MORE
        }
    }

    fun setOnClickListener(clickInterface: OnClickInterface) {
        clickListener = clickInterface
    }
}