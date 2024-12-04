package com.example.doordonot.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doordonot.model.DailyStatus
import com.example.doordonot.model.Habit
import com.example.doordonot.model.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HabitViewModel(
    private val habitRepository: HabitRepository = HabitRepository()
) : ViewModel() {
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    // 습관 추가
    fun addHabit(habit: Habit, userId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            habitRepository.addHabit(habit, userId) { success ->
                if (success) {
                    loadHabits(userId)
                    onComplete()
                } else {
                    _errorMessage.value = "습관 등록에 실패했습니다."
                }
            }
        }
    }

    // 습관 목록 가져오기
    fun loadHabits(userId: String) {
        viewModelScope.launch {
            habitRepository.getHabits(userId) { habitsList ->
                _habits.value = habitsList
            }
        }
    }

    // 습관 상태 업데이트
    fun updateDailyStatus(habitId: String, userId: String, dailyStatus: DailyStatus) {
        viewModelScope.launch {
            habitRepository.updateDailyStatus(habitId, userId, dailyStatus) { success ->
                if (success) {
                    loadHabits(userId)
                } else {
                    _errorMessage.value = "습관 상태 업데이트에 실패했습니다."
                }
            }
        }
    }
}