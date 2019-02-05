package com.example.examenTasks

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.example.examenTasks.POJO.Task
import com.example.examenTasks.adapters.TaskAdapter
import com.example.examenTasks.viewModel.TasksViewModel
import android.os.Build
import android.widget.TextView
import android.widget.Toast
import com.example.examenTasks.POJO.WebSocketResponse
import com.example.examenTasks.Utils.OnClickInterface
import com.example.examenTasks.api.API
import com.example.examenTasks.viewModel.TaskViewModel
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import com.treebo.internetavailabilitychecker.InternetConnectivityListener
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URI
import java.net.URISyntaxException


class MainActivity : AppCompatActivity(), OnClickInterface {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var adapter: TaskAdapter
    private lateinit var taskViewModel: TasksViewModel
    private lateinit var loadingTextView: TextView
    private var mWebSocketClient: WebSocketClient? = null
    private var more: Boolean = true
    private val UPDATED = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()
        connectWebSocket()
    }

    private fun initialize() {

        taskViewModel = ViewModelProviders.of(this, TasksViewModel.Factory(application))
            .get(TasksViewModel::class.java)

        recyclerView = this.findViewById(R.id.recyclerViewId)
        viewManager = GridLayoutManager(this@MainActivity, 1)
        recyclerView.layoutManager = viewManager
        recyclerView.setHasFixedSize(true)

        adapter = TaskAdapter()
        recyclerView.adapter = adapter
        adapter.setOnClickListener(this)

        loadingTextView = findViewById(R.id.loading)

        observerForLiveData()

//        populateListFromLocal()

//        internetConnectionStatus()
        getDataFromServer()


//        addListenerForConnectionChanged()
    }

    private fun populateListFromLocal() {
        val items = taskViewModel.getAllTasks()
        val itemsOfTaskViewModels = getTaskViewModel(items)
        adapter.setTasksList(itemsOfTaskViewModels)
        adapter.task = items
        adapter.notifyDataSetChanged()
    }

    private fun getTaskViewModel(items: List<Task>): List<TaskViewModel> {
        val rez = ArrayList<TaskViewModel>()
        items.forEach { rez.add(TaskViewModel(it, "")) }
        return rez
    }

    private fun observerForLiveData() {
        taskViewModel.items.observe(this, Observer { events ->
            Log.e(" obs bun ", events?.size.toString())
            events?.also {
                adapter.setTasksList(events)
                adapter.notifyDataSetChanged()
                Log.e(" obs bun", " invisible true ")
            }

        })
    }

    private fun getDataFromServer() {
        taskViewModel.getTasksFromServer().enqueue(object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                response.body()?.also {
                    val items = adapter.task as ArrayList
                    it.forEach { task -> taskViewModel.addTask(task) }

                    val itemsOfViewModel = getTaskViewModel(items)
                    taskViewModel.items.value = itemsOfViewModel
                }
            }

            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                Toast.makeText(applicationContext, "Cannot load data from server", Toast.LENGTH_LONG).show()
                populateListFromLocal()
            }
        })
    }


    fun retry(v: View?) {
        loadingTextView.visibility = View.VISIBLE
        loadingTextView.text = "Loading"
        Log.e("retry ", "yesss")
        more = true
//        currentPage = 1
        getDataFromServer()
    }

    private fun connectWebSocket() {
        val uri: URI
        try {
            uri = URI(API.IP)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }

        mWebSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(serverHandshake: ServerHandshake) {
                Log.i("Websocket", "Opened")
                mWebSocketClient?.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL)
            }

            override fun onMessage(s: String) {
                val gson = GsonBuilder().create()
                val groupListType = object : TypeToken<Task>() {}.type
                val task = gson.fromJson(s, groupListType) as Task

                taskViewModel.addTask(task)

                runOnUiThread {
                    val items = adapter.task as ArrayList
                    items.remove(task)
                    items.add(task)
                    taskViewModel.items.value = getTaskViewModel(items)

                    Toast.makeText(applicationContext, "1 task was updated", Toast.LENGTH_LONG).show()
                }
            }

            override fun onClose(i: Int, s: String, b: Boolean) {
                Log.i("Websocket", "Closed $s")
            }

            override fun onError(e: Exception) {
                Log.i("Websocket", "Error " + e.message)
            }
        }
        (mWebSocketClient as WebSocketClient).connect()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onClick(view: View, position: Int) {
        val intent = Intent(this, DetailsActivity::class.java)
        val task = adapter.getTasksList()[position]

        intent.putExtra("item", task.task)
        startActivityForResult(intent, UPDATED)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UPDATED) {
            if (resultCode == Activity.RESULT_OK) {
                data?.apply {
                    val task = data.getParcelableExtra<Task>("item")
                    taskViewModel.updateTask(task)

                    taskViewModel.updateTaskServer(task).enqueue(object : Callback<Task> {
                        override fun onFailure(call: Call<Task>, t: Throwable) {
                            val items = adapter.task as ArrayList
                            val ind = items.indexOf(task)
                            items.remove(task)
                            items.add(ind, task)

                            val itemsViewModel = getTaskViewModel(items) as ArrayList
                            val taskVM=TaskViewModel(task,"NOT SENT TO SERVER YEST")
                            val ind2=itemsViewModel.indexOf(taskVM)
                            itemsViewModel.remove(taskVM)
                            itemsViewModel.add(ind2,taskVM)

                            taskViewModel.items.value =itemsViewModel
                        }

                        override fun onResponse(call: Call<Task>, response: Response<Task>) {
//                            when {
//                                response.code() == 200 -> {
//                                    task = response.body()
//                                    updateListWhenConflict(task, "", "")
//                                }
//                                response.code() == 409 -> {
////                                    val json = JSONObject(response.body().toString())
//                                    val txt = "Conflict value"
//                                    updateListWhenConflict(task, txt, "CONFLICT")
//                                }
//                                response.code() == 412 -> updateListWhenConflict(task, "", "DELETED")
//                            }
                        }
                    })


                }
            }
        }

    }
}
