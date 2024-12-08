//model/Habit

package com.example.doordonot.model

// model/Habit.kt
data class Habit(
    val id: String = "",
    val name: String = "",
    val type: HabitType = HabitType.FORMING,
    val category: String = "",
    val streak: Int = 0,
    // 1. successDates 필드 추가: 성공한 날짜를 저장하는 리스트
    val successDates: List<String> = emptyList(),
    // 1. totalSuccessCount 필드 추가: 총 성공 일수
    val totalSuccessCount: Int = 0
)


enum class HabitType {
    MAINTAIN,
    FORMING
}