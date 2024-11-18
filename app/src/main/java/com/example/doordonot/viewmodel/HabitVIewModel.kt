// HabitViewModel.kt

package com.example.doordonot.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import com.example.doordonot.ui.Habit

class HabitViewModel : ViewModel() {
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> get() = _habits

    fun addHabit(habit: Habit) {
        _habits.value = _habits.value + habit
    }

    fun isHabitCheckedOnDate(habit: Habit, date: LocalDate): Boolean {
        return habit.successDates.contains(date)
    }

    fun toggleHabitCheck(habit: Habit, date: LocalDate) {
        if (habit.successDates.contains(date)) {
            habit.successDates.remove(date)
        } else {
            habit.successDates.add(date)
        }
        updateHabitStatus(habit)
    }

    fun setHabitCheck(habit: Habit, date: LocalDate, isChecked: Boolean) {
        if (isChecked) {
            if (!habit.successDates.contains(date)) {
                habit.successDates.add(date)
            }
        } else {
            habit.successDates.remove(date)
        }
        updateHabitStatus(habit)
    }

    fun getTotalDays(habit: Habit): Int {
        return habit.successDates.size
    }

    fun getConsecutiveDays(habit: Habit): Int {
        // 연속 일수 계산 로직 구현 필요
        return 0
    }

    private fun updateHabitStatus(habit: Habit) {
        val totalDays = getTotalDays(habit)
        if (!habit.isMaintained && totalDays >= habit.transitionDays) {
            habit.isMaintained = true
            // _habits.value = _habits.value.toList() // 필요 시 활성화
        }
    }
}
