package com.sebasmiumg.tareas.ui

import android.graphics.Paint
import android.widget.TextView

/** Tacha (o quita el tachado de) el texto; se usa para marcar visualmente una tarea completada. */
fun TextView.setStrikeThrough(enabled: Boolean) {
    paintFlags = if (enabled) {
        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}
