package com.example.examenTasks

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.preference.PreferenceManager
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
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URI
import java.net.URISyntaxException


class MainActivity : AppCompatActivity(), InternetConnectivityListener, OnClickInterface {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var adapter: TaskAdapter
    private lateinit var taskViewModel: TasksViewModel
    private lateinit var loadingTextView: TextView
    private var mWebSocketClient: WebSocketClient? = null
    private var maxM: Long = 0
    private var more: Boolean = true
    private lateinit var mInternetAvailabilityChecker: InternetAvailabilityChecker
    private val UPDATED = 1
    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        InternetAvailabilityChecker.init(this)
        initialize()
        connectWebSocket()
    }

    private fun initialize() {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        editor = pref.edit()
        editor.apply()

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

        populateListFromLocal()

//        internetConnectionStatus()
        getDataFromServer()


        addListenerForConnectionChanged()
    }

    private fun populateListFromLocal() {
        val items = taskViewModel.getAllTasks()
        if (items.isNotEmpty()) {
            val itemsOfTaskViewModels = getTaskViewModel(items)
            adapter.setTasksList(itemsOfTaskViewModels)
            adapter.task = items
            adapter.notifyDataSetChanged()
            maxM = items[0].updated
        } else maxM = 0
    }

    private fun getTaskViewModel(items: List<Task>): List<TaskViewModel> {
        val rez = ArrayList<TaskViewModel>()
        items.forEach { it -> rez.add(TaskViewModel(it, "", "")) }
        return rez
    }

    private fun addListenerForConnectionChanged() {
        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance()
        mInternetAvailabilityChecker.addInternetConnectivityListener(this)
    }

    private fun internetConnectionStatus() {
        if (checkIfIsConnected()) {
            loadingTextView.visibility = View.VISIBLE
            getDataFromServer()
        } else {
            loadingTextView.visibility = View.VISIBLE
            loadingTextView.text = getString(R.string.offline)
            recyclerView.visibility = View.GONE
        }
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
        taskViewModel.getTasksFromServer(maxM).enqueue(object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                response.body()?.also {
                    val items = adapter.task as ArrayList
                    it.forEach { task -> taskViewModel.addTask(task) }
                    var inserted = 0
                    var updated = 0
                    var deleted = 0

                    it.forEach { task ->
                        if (!items.contains(task)) {
                            if (task.status != "deleted") {
                                items.add(task)
                                inserted++
                            } else {
                                taskViewModel.deleteTask(task)
                            }
                        } else {
                            val oldTask = items[items.indexOf(task)]
                            if (task.status == "deleted") {
                                deleted++
                                items.remove(task)
                            } else {
                                updated++
                                items.remove(oldTask)
                                items.add(task)
                            }
                        }
                    }

                    val itemsOfViewModel = getTaskViewModel(items)
                    taskViewModel.items.value = itemsOfViewModel
                    loadingTextView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    Toast.makeText(
                        applicationContext,
                        "$updated tasks were updated \n $inserted tasks were inserted \n" +
                                " $deleted tasks were deleted",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Cannot load data from server",
                    Toast.LENGTH_LONG
                ).show()
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
//                val json = JSONObject(s)
//                Log.e(" json ", json.toString())
//                if (json.has("event")) {
//                    val type = json["event"]
//                    val noteJson = JSONObject(json["task"].toString())
//                    Log.e(" jsonnote ", noteJson.toString())
//                    val task = Task(
//                        noteJson["id"] as Int,
//                        noteJson["text"] as String,
//                        noteJson["status"] as String,
//                        noteJson["updated"] as Long,
//                        noteJson["version"] as Int
//                    )
                val gson = GsonBuilder().create()
                val groupListType = object : TypeToken<WebSocketResponse>() {}.type
                val event = gson.fromJson(s, groupListType) as WebSocketResponse
                val task = event.task
                when (event.event) {
                    "inserted" -> {
                        // add to db
                        taskViewModel.addTask(task)
                        runOnUiThread {
                            val items = adapter.task as ArrayList
                            items.add(task)
                            taskViewModel.items.value = getTaskViewModel(items)
                            Toast.makeText(
                                applicationContext,
                                "1 task was inserted",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    "deleted" -> {
                        //detele from db
                        taskViewModel.deleteTask(task)
                            runOnUiThread {
                                val items = adapter.task as ArrayList
                                items.remove(task)
                                taskViewModel.items.value = getTaskViewModel(items)
                                Toast.makeText(
                                    applicationContext,
                                    "1 task was deleted",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
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


override fun onInternetConnectivityChanged(isConnected: Boolean) {
    if (!isConnected) {
        loadingTextView.visibility = View.VISIBLE
        loadingTextView.text = "OFFLINE"
    } else {
        retry(null)
    }
}

override fun onDestroy() {
    super.onDestroy()
    mInternetAvailabilityChecker
        .removeInternetConnectivityChangeListener(this)
}

private fun checkIfIsConnected(): Boolean {
    val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

override fun onClick(view: View, position: Int) {
    val intent = Intent(this, DetailsActivity::class.java)
    val task = adapter.getTasksList()[position]
    if (task.conflict == "DELETED") {
        val items = adapter.task as ArrayList
        items.remove(task.task)
        taskViewModel.items.value = getTaskViewModel(items)
    } else {
        intent.putExtra("item", task.task)
        intent.putExtra("conflictValue", task.conflictValue)
        startActivityForResult(intent, UPDATED)
    }
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == UPDATED) {
        if (resultCode == Activity.RESULT_OK) {
            data?.apply {
                var task = data.getParcelableExtra<Task>("item")
                var txtConflict = data.getStringExtra("conflictValue")
                task.text = "${task.text} $txtConflict"

                taskViewModel.updateTask(task)
                taskViewModel.updateTask(task)//local
                taskViewModel.updateTaskServer(task).enqueue(object : Callback<Task> {
                    override fun onFailure(call: Call<Task>, t: Throwable) {
                        val items = adapter.task as ArrayList
                        items.remove(task)
                        items.add(task)
                        items.sortBy { it.updated }
                        taskViewModel.items.value = getTaskViewModel(items)
                    }

                    override fun onResponse(call: Call<Task>, response: Response<Task>) {
                        when {
                            response.code() == 200 -> {
                                task = response.body()
                                updateListWhenConflict(task, "", "")
                            }
                            response.code() == 409 -> {
//                                    val json = JSONObject(response.body().toString())
                                val txt = "Conflict value"
                                updateListWhenConflict(task, txt, "CONFLICT")
                            }
                            response.code() == 412 -> updateListWhenConflict(task, "", "DELETED")
                        }
                    }
                })


            }
        }
    }

}

private fun updateListWhenConflict(
    task: Task,
    txt: String,
    conflict: String
) {
    val items = adapter.task as ArrayList
    items.remove(task)
    items.add(task)
    items.sortBy { it.updated }
    val finalItems = getTaskViewModel(items) as ArrayList
    if (txt == "" && conflict == "") {
        taskViewModel.items.value = finalItems
    } else {
        val conflictTask = TaskViewModel(task, conflict, txt)
        finalItems.remove(conflictTask)
        finalItems.add(conflictTask)
        finalItems.sortBy { it.task.updated }
        taskViewModel.items.value = finalItems
    }

}
}
