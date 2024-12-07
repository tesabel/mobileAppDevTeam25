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

    // 습관 추가
    fun addHabit(habit: Habit, userId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            habitRepository.addHabit(habit, userId) { success ->
                if (success) {
                    println("HabitViewModel: Habit added successfully.")
                    loadHabits(userId)
                    onComplete()
                } else {
                    println("HabitViewModel: Failed to add habit.")
                    _errorMessage.value = "습관 등록에 실패했습니다."
                }
            }
        }
    }

    // 습관 목록 가져오기
    fun loadHabits(userId: String) {
        viewModelScope.launch {
            habitRepository.getHabits(userId) { habitsList ->
                println("HabitViewModel: Loaded habits: $habitsList")
                _habits.value = habitsList
                // 오늘 날짜의 DailyStatus 초기화
                habitRepository.initializeTodayDailyStatuses(userId) { success ->
                    if (success) {
                        println("HabitViewModel: 오늘 날짜의 DailyStatus 초기화 성공")
                        // streak 업데이트를 위해 습관 다시 로드
                        habitRepository.getHabits(userId) { updatedHabits ->
                            println("HabitViewModel: Reloaded habits after initializing DailyStatus.")
                            _habits.value = updatedHabits
                        }
                    } else {
                        println("HabitViewModel: 오늘 날짜의 습관 상태 초기화에 실패했습니다.")
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
                    println("HabitViewModel: Found habit: $habit")
                    _currentHabit.value = habit
                } else {
                    println("HabitViewModel: 해당 습관 정보를 찾을 수 없습니다.")
                    _errorMessage.value = "해당 습관 정보를 찾을 수 없습니다."
                }
            }
        }
    }

    // 특정 습관의 DailyStatus 로드
    fun loadDailyStatuses(habitId: String, userId: String) {
        viewModelScope.launch {
            habitRepository.getDailyStatuses(habitId, userId) { statuses ->
                println("HabitViewModel: Loaded dailyStatuses: $statuses")
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
                    println("HabitViewModel: Retrieved DailyStatus: $status")
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
                    println("HabitViewModel: Failed to retrieve or initialize DailyStatus.")
                    onResult(null)
                }
            }
        }
    }

    // 특정 날짜의 DailyStatus isChecked 값을 토글하는 함수
    fun toggleDailyStatus(
        habitId: String,
        userId: String,
        date: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            println("HabitViewModel: Toggling DailyStatus for habitId: $habitId, date: $date")
            habitRepository.toggleDailyStatus(habitId, userId, date) { success ->
                if (success) {
                    println("HabitViewModel: toggleDailyStatus succeeded, reloading dailyStatuses.")
                    // 연속 성공 횟수 업데이트
                    loadDailyStatuses(habitId, userId)
                } else {
                    println("HabitViewModel: toggleDailyStatus failed.")
                }
                onComplete(success)
            }
        }
    }

    // 습관 상태 업데이트
    fun updateDailyStatus(habitId: String, userId: String, dailyStatus: DailyStatus) {
        viewModelScope.launch {
            habitRepository.updateDailyStatus(habitId, userId, dailyStatus) { success ->
                if (success) {
                    println("HabitViewModel: updateDailyStatus succeeded, reloading dailyStatuses.")
                    // 상태 업데이트 성공 시 다시 로드
                    loadDailyStatuses(habitId, userId)
                } else {
                    println("HabitViewModel: updateDailyStatus failed.")
                    _errorMessage.value = "습관 상태 업데이트에 실패했습니다."
                }
            }
        }
    }
}