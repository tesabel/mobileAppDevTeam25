// HabitDisplay.kt (새로 추가)
// 변경 부분 시작: 습관과 체크 상태를 함께 관리할 데이터 클래스
package com.example.doordonot.model

data class HabitDisplay(
    val habit: Habit,
    val isChecked: Boolean
)
// 변경 부분 끝