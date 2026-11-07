package com.sebasmiumg.tareas.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sebasmiumg.tareas.R
import com.sebasmiumg.tareas.TareasApp
import com.sebasmiumg.tareas.data.Task
import com.sebasmiumg.tareas.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val repository by lazy { (application as TareasApp).repository }
    private val adapter = TaskAdapter(
        onTaskClick = ::openTaskDetail,
        onCompletedChange = ::setTaskCompleted
    )

    private var scrollToTopOnNextUpdate = false

    // Recibe el resultado de AddTaskActivity: el id y la descripción de la tarea recién creada.
    private val addTaskLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val description = result.data?.getStringExtra(Extras.TASK_DESCRIPTION).orEmpty()
                scrollToTopOnNextUpdate = true
                Snackbar.make(binding.root, getString(R.string.task_added, description), Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.fabAdd)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()
        setSupportActionBar(binding.toolbar)

        binding.recyclerTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerTasks.adapter = adapter
        binding.fabAdd.setOnClickListener {
            addTaskLauncher.launch(Intent(this, AddTaskActivity::class.java))
        }

        observeTasks()
    }

    private fun openTaskDetail(task: Task) {
        startActivity(TaskDetailActivity.newIntent(this, task))
    }

    private fun setTaskCompleted(task: Task, completed: Boolean) {
        lifecycleScope.launch { repository.setCompleted(task.id, completed) }
    }

    /** La lista se actualiza sola cada vez que cambia la tabla de tareas en Room. */
    private fun observeTasks() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.tasks.collect { tasks ->
                    adapter.submitList(tasks) {
                        if (scrollToTopOnNextUpdate) {
                            binding.recyclerTasks.scrollToPosition(0)
                            scrollToTopOnNextUpdate = false
                        }
                    }
                    binding.emptyState.isVisible = tasks.isEmpty()
                }
            }
        }
    }
}
