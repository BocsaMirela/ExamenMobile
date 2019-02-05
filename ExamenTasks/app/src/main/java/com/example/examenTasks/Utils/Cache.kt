package com.example.examenTasks.Utils

import android.support.v4.util.LruCache
import com.example.examenTasks.POJO.Task

class Cache private constructor() {
    val lru: LruCache<Int, List<Task>> = LruCache(1024)
    val lruLastM: LruCache<Int, String> = LruCache(1024)

    companion object {

        private var instance: Cache? = null

        fun getInstance(): Cache {

            if (instance == null) {

                instance = Cache()
            }
            return instance as Cache

        }
    }
}
