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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        attachSwipeToDelete()
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

    /** Deslizar una tarea hacia cualquier lado la elimina. */
    private fun attachSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task = adapter.currentList.getOrNull(viewHolder.bindingAdapterPosition) ?: return
                deleteTask(task)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerTasks)
    }

    /** Borra la tarea y ofrece deshacer: restaurarla la vuelve a insertar con el mismo id. */
    private fun deleteTask(task: Task) {
        lifecycleScope.launch {
            repository.deleteTask(task.id)
            Snackbar.make(binding.root, R.string.task_deleted, Snackbar.LENGTH_LONG)
                .setAnchorView(binding.fabAdd)
                .setAction(R.string.undo) {
                    lifecycleScope.launch { repository.restoreTask(task) }
                }
                .show()
        }
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
