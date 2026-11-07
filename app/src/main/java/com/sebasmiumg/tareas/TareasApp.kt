package com.sebasmiumg.tareas

import android.app.Application
import com.sebasmiumg.tareas.data.AppDatabase
import com.sebasmiumg.tareas.data.TaskRepository

class TareasApp : Application() {

    val repository: TaskRepository by lazy {
        TaskRepository(AppDatabase.getInstance(this).taskDao())
    }
}
