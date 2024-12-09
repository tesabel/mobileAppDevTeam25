//HabitViewModel.kt
package com.example.doordonot.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doordonot.model.DailyStatus
import com.example.doordonot.model.Habit
import com.example.doordonot.model.HabitDisplay
import com.example.doordonot.model.HabitRepository
import com.example.doordonot.model.HabitType
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

    // 날짜별 HabitDisplay 리스트로 관리
    private val _selectedDateHabitDisplays = MutableStateFlow<List<HabitDisplay>>(emptyList())
    val selectedDateHabitDisplays: StateFlow<List<HabitDisplay>> = _selectedDateHabitDisplays


    private val _selectedDateHabits = MutableStateFlow<List<Habit>>(emptyList())
    val selectedDateHabits: StateFlow<List<Habit>> = _selectedDateHabits

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
    // 날짜별 습관 로드 시 HabitDisplay 반환
    fun loadHabitsForDate(userId: String, selectedDate: LocalDate) {
        val dateString = selectedDate.toString()
        habitRepository.getHabitsForDate(
            userId = userId,
            selectedDate = dateString,
            onResult = { habitDisplays ->
                _selectedDateHabitDisplays.value = habitDisplays
                _errorMessage.value = ""
            },
            onError = { errorMessage ->
                _selectedDateHabitDisplays.value = emptyList()
                _errorMessage.value = errorMessage
            }
        )
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

    // 특정 날짜의 DailyStatus isChecked 값을 설정하는 함수
    fun setDailyStatus(
        habitId: String,
        userId: String,
        date: String,
        isChecked: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            println("HabitViewModel: Setting DailyStatus for habitId: $habitId, date: $date to isChecked=$isChecked")
            habitRepository.setDailyStatus(habitId, userId, date, isChecked) { success ->
                if (success) {
                    println("HabitViewModel: setDailyStatus succeeded, reloading dailyStatuses.")
                    // 최신 상태를 반영하기 위해 DailyStatus 재로드
                    loadDailyStatuses(habitId, userId)
                } else {
                    println("HabitViewModel: setDailyStatus failed.")
                }
                onComplete(success)
            }
        }
    }

    // 습관 상태 업데이트
    fun updateDailyStatus(
        habitId: String,
        userId: String,
        dailyStatus: DailyStatus,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            habitRepository.updateDailyStatus(habitId, userId, dailyStatus) { success ->
                if (success) {
                    println("HabitViewModel: updateDailyStatus succeeded, reloading dailyStatuses.")
                    loadDailyStatuses(habitId, userId)
                } else {
                    println("HabitViewModel: updateDailyStatus failed.")
                    _errorMessage.value = "습관 상태 업데이트에 실패했습니다."
                }
                onComplete(success) // 콜백 호출
            }
        }
    }


    // 실시간 업데이트 리스너 추가 함수
    fun observeDailyStatuses(habitId: String, userId: String) {
        habitRepository.observeDailyStatuses(habitId, userId) { statuses ->
            _currentHabitDailyStatuses.value = statuses
        }
    }

    //드롭
    fun updateHabitType(
        habitId: String,
        userId: String,
        newType: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                // Repository를 통해 데이터 업데이트
                val isSuccess = habitRepository.updateHabitType(habitId, userId, newType)
                if (isSuccess) {
                    // 업데이트 성공 시 ViewModel 내 상태도 갱신
                    _habits.value = _habits.value.map { habit ->
                        if (habit.id == habitId) habit.copy(type = HabitType.valueOf(newType))
                        else habit
                    }
                }
                onComplete(isSuccess)
            } catch (e: Exception) {
                // 에러 처리
                Log.e("HabitViewModel", "Failed to update habit type", e)
                onComplete(false)
            }
        }
    }


    // 습관 삭제 함수
    fun deleteHabit(habitId: String, userId: String) {
        viewModelScope.launch {
            val isDeleted = habitRepository.deleteHabit(habitId, userId)
            if (isDeleted) {
                loadHabits(userId) // 삭제 후 습관 리스트 재로드
            } else {
                Log.e("HabitViewModel", "습관 삭제에 실패하여 리스트를 새로 로드하지 않음")
            }
        }
    }

}