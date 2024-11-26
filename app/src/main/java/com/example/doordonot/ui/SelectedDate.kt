package com.example.doordonot.ui

import java.time.LocalDate

data class SelectedDate(val year: Int, val month: Int, val day: Int) {
    fun toLocalDate(): LocalDate = LocalDate.of(year, month, day)
}
