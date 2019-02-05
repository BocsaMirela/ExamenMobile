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
        viewHolder.txtStatus.text = event.task.status
        viewHolder.txtData.text = formatDate(Date(event.task.updated))
        viewHolder.txtConflict.text = event.conflict
        viewHolder.txtVersion.text = "Version: " + event.task.version.toString()

    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {


        var txtDescription: TextView = view.findViewById(R.id.chocolateDescription)
        var txtData: TextView = view.findViewById(R.id.chocolateData)
        var txtStatus: TextView = view.findViewById(R.id.chocolateStatus)
        var txtConflict: TextView = view.findViewById(R.id.chocolateResult)
        var txtVersion: TextView = view.findViewById(R.id.chocolateVersion)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.onClick(view, adapterPosition); }

    }

    private fun formatDate(date: Date): String {
        val fmt = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
//        return fmt.parse(string)
        return fmt.format(date)
    }

    fun removeItem(item: Task) {
        (taskViewModelsList as ArrayList<Task>).remove(item)
        notifyDataSetChanged()
    }

    fun insertItem(item: Task) {
        (taskViewModelsList as ArrayList<Task>).add(item)
        notifyDataSetChanged()
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