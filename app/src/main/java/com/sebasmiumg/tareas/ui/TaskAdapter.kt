package com.sebasmiumg.tareas.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sebasmiumg.tareas.R
import com.sebasmiumg.tareas.data.Task
import com.sebasmiumg.tareas.databinding.ItemTaskBinding

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onCompletedChange: (Task, Boolean) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding, onTaskClick, onCompletedChange)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(
        private val binding: ItemTaskBinding,
        private val onTaskClick: (Task) -> Unit,
        private val onCompletedChange: (Task, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.textDescription.text = task.description
            binding.textStatus.setText(
                if (task.isCompleted) R.string.status_completed else R.string.status_pending
            )

            // Una tarea completada se ve tachada y atenuada.
            val dimmed = if (task.isCompleted) COMPLETED_ALPHA else 1f
            binding.textDescription.setStrikeThrough(task.isCompleted)
            binding.textDescription.alpha = dimmed
            binding.textStatus.alpha = dimmed

            // Asignar isChecked por código no dispara el click, así que no hay llamadas duplicadas.
            binding.checkboxCompleted.isChecked = task.isCompleted
            binding.checkboxCompleted.setOnClickListener {
                onCompletedChange(task, binding.checkboxCompleted.isChecked)
            }
            binding.root.setOnClickListener { onTaskClick(task) }
        }
    }

    private object TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }

    private companion object {
        const val COMPLETED_ALPHA = 0.6f
    }
}
