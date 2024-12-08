//CalenarViewModel.kt
package com.example.doordonot.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarViewModel : ViewModel() {
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

    private val _selectedDate = MutableStateFlow(
        SelectedDate(
            year = formatter.format(Date()).split("-").first().toInt(),
            month = formatter.format(Date()).split("-")[1].toInt(),
            day = formatter.format(Date()).split("-").last().toInt()
        )
    )//현재 선택된 날짜(변경 O)
    val selectedDate: StateFlow<SelectedDate> = _selectedDate//선택된 날짜 읽기 전용

    fun onDateSelected(year: Int, month: Int, day: Int) {
        viewModelScope.launch {
            _selectedDate.value = SelectedDate(year, month, day)
        }
    }
}