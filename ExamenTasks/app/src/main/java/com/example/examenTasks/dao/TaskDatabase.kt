package com.example.examenTasks.dao

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.example.examenTasks.POJO.Task

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun noteDAO(): TaskDAO

    companion object {
        private var INSTANCE: TaskDatabase? = null

        fun getAppDatabase(context: Context): TaskDatabase {
            if (INSTANCE == null) {
                INSTANCE =
                        Room.databaseBuilder(context.applicationContext, TaskDatabase::class.java, "tasksBD")
                            .allowMainThreadQueries().build()

            }
            return INSTANCE as TaskDatabase

        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

}