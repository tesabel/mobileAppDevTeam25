// com.example.doordonot.viewmodel.HabitViewModel.kt
package com.example.doordonot.viewmodel

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

    private val _dailyStatuses = MutableStateFlow<List<DailyStatus>>(emptyList())
    val dailyStatuses: StateFlow<List<DailyStatus>> = _dailyStatuses

    private val _currentHabit = MutableStateFlow<Habit?>(null)
    val currentHabit: StateFlow<Habit?> = _currentHabit

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
                // 오늘 날짜의 DailyStatus 초기화
                habitRepository.initializeTodayDailyStatuses(userId) { success ->
                    if (success) {
                        println("오늘 날짜의 DailyStatus 초기화 성공")
                        // streak 업데이트를 위해 습관 다시 로드
                        habitRepository.getHabits(userId) { updatedHabits ->
                            _habits.value = updatedHabits
                        }
                    } else {
                        _errorMessage.value = "오늘 날짜의 습관 상태 초기화에 실패했습니다."
                    }
                }
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

    // 날짜별 습관 상태 불러오기
    fun loadDailyStatuses(habitId: String, userId: String) {
        viewModelScope.launch {
            habitRepository.getDailyStatuses(habitId, userId) { statuses ->
                _dailyStatuses.value = statuses
            }
        }
    }

    // 특정 날짜의 습관 상태 가져오기 또는 초기화
    fun getOrInitializeDailyStatus(
        habitId: String,
        userId: String,
        date: String,
        onResult: (DailyStatus?) -> Unit
    ) {
        viewModelScope.launch {
            habitRepository.getOrInitializeDailyStatus(habitId, userId, date) { status ->
                if (status != null) {
                    // 기존 상태 업데이트
                    val updatedStatuses = _dailyStatuses.value.toMutableList()
                    val index = updatedStatuses.indexOfFirst { it.date == date }
                    if (index != -1) {
                        updatedStatuses[index] = status
                    } else {
                        updatedStatuses.add(status)
                    }
                    _dailyStatuses.value = updatedStatuses
                    onResult(status)
                } else {
                    onResult(null)
                }
            }
        }
    }

    // 특정 습관 로드
    fun loadHabit(habitId: String, userId: String) {
        viewModelScope.launch {
            habitRepository.getHabits(userId) { habitsList ->
                val habit = habitsList.find { it.id == habitId }
                _currentHabit.value = habit
            }
        }
    }
}