package com.sebasmiumg.tareas.data

import kotlinx.coroutines.flow.Flow

/** Único punto de acceso a los datos: las pantallas nunca hablan directamente con Room. */
class TaskRepository(private val dao: TaskDao) {

    val tasks: Flow<List<Task>> = dao.observeAll()

    suspend fun getTask(id: Long): Task? = dao.getById(id)

    /** Guarda la tarea y devuelve el id que le asignó la base de datos. */
    suspend fun addTask(description: String, notes: String): Long =
        dao.insert(Task(description = description, notes = notes))

    /** Vuelve a guardar una tarea tal cual era (se usa para deshacer un borrado). */
    suspend fun restoreTask(task: Task) {
        dao.insert(task)
    }

    suspend fun setCompleted(id: Long, completed: Boolean) = dao.setCompleted(id, completed)

    suspend fun deleteTask(id: Long) = dao.deleteById(id)
}
