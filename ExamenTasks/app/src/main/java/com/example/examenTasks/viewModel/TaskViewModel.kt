package com.example.examenTasks.viewModel

import com.example.examenTasks.POJO.Task

class TaskViewModel(val task: Task, val conflict: String, val conflictValue: String) {
    override fun equals(other: Any?): Boolean {
        return (other as TaskViewModel).task.id == this.task.id
    }

}