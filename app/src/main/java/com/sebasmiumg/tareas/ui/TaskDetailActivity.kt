package com.sebasmiumg.tareas.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sebasmiumg.tareas.R
import com.sebasmiumg.tareas.TareasApp
import com.sebasmiumg.tareas.data.Task
import com.sebasmiumg.tareas.databinding.ActivityTaskDetailBinding
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/** Muestra toda la información de una tarea. Los datos llegan como extras del Intent. */
class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private val repository by lazy { (application as TareasApp).repository }

    private var taskId = NO_ID
    private var isCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()
        binding.toolbar.setNavigationOnClickListener { finish() }

        taskId = intent.getLongExtra(Extras.TASK_ID, NO_ID)
        if (taskId == NO_ID) {
            finish()
            return
        }

        val notes = intent.getStringExtra(Extras.TASK_NOTES).orEmpty()
        val createdAt = intent.getLongExtra(Extras.TASK_CREATED_AT, 0L)
        isCompleted = intent.getBooleanExtra(Extras.TASK_COMPLETED, false)

        binding.textDescription.text = intent.getStringExtra(Extras.TASK_DESCRIPTION).orEmpty()
        binding.textNotes.text = notes.ifBlank { getString(R.string.no_notes) }
        binding.textCreatedAt.text =
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(createdAt))
        binding.buttonToggleCompleted.setOnClickListener { toggleCompleted() }
        renderStatus()
    }

    private fun toggleCompleted() {
        val newState = !isCompleted
        lifecycleScope.launch {
            repository.setCompleted(taskId, newState)
            isCompleted = newState
            renderStatus()
        }
    }

    /** Refleja el estado actual en el texto de estado, el tachado y la etiqueta del botón. */
    private fun renderStatus() {
        binding.textStatus.setText(if (isCompleted) R.string.status_completed else R.string.status_pending)
        binding.textDescription.setStrikeThrough(isCompleted)
        binding.buttonToggleCompleted.setText(
            if (isCompleted) R.string.action_mark_pending else R.string.action_mark_completed
        )
    }

    companion object {
        private const val NO_ID = -1L

        /** Arma el Intent explícito con todos los datos que necesita la pantalla de detalle. */
        fun newIntent(context: Context, task: Task): Intent =
            Intent(context, TaskDetailActivity::class.java)
                .putExtra(Extras.TASK_ID, task.id)
                .putExtra(Extras.TASK_DESCRIPTION, task.description)
                .putExtra(Extras.TASK_NOTES, task.notes)
                .putExtra(Extras.TASK_COMPLETED, task.isCompleted)
                .putExtra(Extras.TASK_CREATED_AT, task.createdAt)
    }
}
