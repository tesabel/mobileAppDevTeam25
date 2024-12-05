package com.example.doordonot.model

data class Habit(
    val id: String = "",
    val name: String = "",
    val type: HabitType = HabitType.FORMING,
    val category: String = "",
    val streak: Int = 0
)

enum class HabitType {
    MAINTAIN,
    FORMING
}