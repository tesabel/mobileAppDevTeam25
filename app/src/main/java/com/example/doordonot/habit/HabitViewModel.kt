//HabitViewModel.kt
package com.example.doordonot.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doordonot.model.DailyStatus
import com.example.doordonot.model.Habit
import com.example.doordonot.model.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(
    private val habitRepository: HabitRepository = HabitRepository()
) : ViewModel() {
    // 모든 습관 목록
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits

    // 현재 선택된 습관
    private val _currentHabit = MutableStateFlow<Habit?>(null)
    val currentHabit: StateFlow<Habit?> = _currentHabit

    // 특정 습관의 DailyStatus 목록
    private val _currentHabitDailyStatuses = MutableStateFlow<List<DailyStatus>>(emptyList())
    val currentHabitDailyStatuses: StateFlow<List<DailyStatus>> = _currentHabitDailyStatuses

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _selectedDateHabits = MutableStateFlow<List<Habit>>(emptyList())
    val selectedDateHabits: StateFlow<List<Habit>> = _selectedDateHabits

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

    // 특정 습관 로드
    fun loadHabit(habitId: String, userId: String) {
        viewModelScope.launch {
            habitRepository.getHabits(userId) { habitsList ->
                val habit = habitsList.find { it.id == habitId }
                if (habit != null) {
                    _currentHabit.value = habit
                } else {
                    _errorMessage.value = "해당 습관 정보를 찾을 수 없습니다."
                }
            }
        }
    }

    // 특정 습관의 DailyStatus 로드
    fun loadDailyStatuses(habitId: String, userId: String) {
        viewModelScope.launch {
            habitRepository.getDailyStatuses(habitId, userId) { statuses ->
                _currentHabitDailyStatuses.value = statuses
            }
        }
    }

    // 특정 습관의 DailyStatus 가져오기 또는 초기화
    fun getOrInitializeDailyStatus(
        habitId: String,
        userId: String,
        date: String,
        onResult: (DailyStatus?) -> Unit
    ) {
        viewModelScope.launch {
            habitRepository.getOrInitializeDailyStatus(habitId, userId, date) { status ->
                if (status != null) {
                    // 상태 업데이트
                    val updatedStatuses = _currentHabitDailyStatuses.value.toMutableList()
                    val index = updatedStatuses.indexOfFirst { it.date == date }
                    if (index != -1) {
                        updatedStatuses[index] = status
                    } else {
                        updatedStatuses.add(status)
                    }
                    _currentHabitDailyStatuses.value = updatedStatuses
                    onResult(status)
                } else {
                    onResult(null)
                }
            }
        }
    }

    // 습관 상태 업데이트
    fun updateDailyStatus(habitId: String, userId: String, dailyStatus: DailyStatus) {
        viewModelScope.launch {
            habitRepository.updateDailyStatus(habitId, userId, dailyStatus) { success ->
                if (success) {
                    // 상태 업데이트 성공 시 다시 로드
                    loadDailyStatuses(habitId, userId)
                } else {
                    _errorMessage.value = "습관 상태 업데이트에 실패했습니다."
                }
            }
        }
    }
    fun loadHabitsForDate(userId: String, selectedDate: LocalDate) {
        val dateString = selectedDate.toString() // yyyy-MM-dd 형식

        habitRepository.getHabitsForDate(
            userId = userId,
            selectedDate = dateString,
            onResult = { habitsForDate ->
                _selectedDateHabits.value = habitsForDate
            },
            onError = { errorMessage ->
                _errorMessage.value = errorMessage
            }
        )
    }

    fun updateHabitType(habitId: String, userId: String, newType: String) {
        viewModelScope.launch {
            habitRepository.updateHabitType(habitId, newType) { success ->
                if (success) {
                    // If update is successful, refresh the habits list to reflect the changes
                    loadHabits(userId)
                } else {
                    // Handle failure (you can show an error message or log it)
                    Log.e("HabitViewModel", "Failed to update habit type")
                }
            }
        }
    }
}


