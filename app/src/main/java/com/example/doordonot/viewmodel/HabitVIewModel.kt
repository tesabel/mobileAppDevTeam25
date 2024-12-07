//// HabitViewModel.kt
//
//package com.example.doordonot.viewmodel
//
//import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxScope
//import androidx.compose.foundation.layout.size
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.CompositionLocalProvider
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.compositionLocalOf
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.input.pointer.consumeAllChanges
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.layout.boundsInWindow
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.IntSize
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewmodel.compose.viewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import java.time.LocalDate
//import com.example.doordonot.ui.Habit
//
//class HabitViewModel : ViewModel() {
//    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
//    val habits: StateFlow<List<Habit>> get() = _habits
//
//    fun addHabit(habit: Habit) {
//        _habits.value = _habits.value + habit
//    }
//
//    fun isHabitCheckedOnDate(habit: Habit, date: LocalDate): Boolean {
//        return habit.successDates.contains(date)
//    }
//
//    fun toggleHabitCheck(habit: Habit, date: LocalDate) {
//        if (habit.successDates.contains(date)) {
//            habit.successDates.remove(date)
//        } else {
//            habit.successDates.add(date)
//        }
//        updateHabitStatus(habit)
//    }
//
//    fun setHabitCheck(habit: Habit, date: LocalDate, isChecked: Boolean) {
//        if (isChecked) {
//            if (!habit.successDates.contains(date)) {
//                habit.successDates.add(date)
//            }
//        } else {
//            habit.successDates.remove(date)
//        }
//        updateHabitStatus(habit)
//    }
//
//    fun getTotalDays(habit: Habit): Int {
//        return habit.successDates.size
//    }
//
//    fun getConsecutiveDays(habit: Habit): Int {
//        // 연속 일수 계산 로직 구현 필요
//        return 0
//    }
//
//    private fun updateHabitStatus(habit: Habit) {
//        val totalDays = getTotalDays(habit)
//        if (!habit.isMaintained && totalDays >= habit.transitionDays) {
//            habit.isMaintained = true
//            // _habits.value = _habits.value.toList() // 필요 시 활성화
//        }
//    }
//
//    //드롭했을 때
//    fun updateHabit(updatedHabit: Habit) {
//        _habits.value = _habits.value.map { habit ->
//            //name 말고 id로
//            if (habit.name == updatedHabit.name) updatedHabit else habit
//        }
//    }
//}

