package com.example.examenTasks.dao

import android.arch.persistence.room.*
import com.example.examenTasks.POJO.Task


@Dao
interface TaskDAO {


    @Query("SELECT * FROM tasks")
    fun getTasks(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chocolate: Task): Long

    @Delete
    fun delete(chocolate: Task)

    @Update
    fun update(chocolate: Task)

}
