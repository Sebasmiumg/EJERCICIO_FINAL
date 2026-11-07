package com.sebasmiumg.tareas.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.sebasmiumg.tareas.R
import com.sebasmiumg.tareas.TareasApp
import com.sebasmiumg.tareas.databinding.ActivityAddTaskBinding
import kotlinx.coroutines.launch

/** Formulario para crear una tarea. Al guardar, devuelve el resultado a la pantalla principal. */
class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding
    private val repository by lazy { (application as TareasApp).repository }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.inputDescription.doAfterTextChanged { binding.inputDescriptionLayout.error = null }
        binding.buttonSave.setOnClickListener { saveTask() }
    }

    private fun saveTask() {
        val description = binding.inputDescription.text?.toString().orEmpty().trim()
        val notes = binding.inputNotes.text?.toString().orEmpty().trim()

        if (description.isEmpty()) {
            binding.inputDescriptionLayout.error = getString(R.string.error_description_required)
            return
        }

        binding.buttonSave.isEnabled = false
        lifecycleScope.launch {
            val id = repository.addTask(description, notes)
            val result = Intent()
                .putExtra(Extras.TASK_ID, id)
                .putExtra(Extras.TASK_DESCRIPTION, description)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
