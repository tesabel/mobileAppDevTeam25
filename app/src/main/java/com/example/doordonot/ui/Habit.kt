// Habit.kt

package com.example.doordonot.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.time.LocalDate

data class Habit(
    val name: String,
    val categories: List<String>,
    var isMaintained: Boolean,
    val transitionDays: Int,
    val successDates: MutableList<LocalDate> = mutableStateListOf()
)
