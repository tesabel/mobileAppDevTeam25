

package com.example.doordonot.calendar

import java.time.LocalDate

data class SelectedDate(val year: Int, val month: Int, val day: Int){
    fun toLocalDate(): LocalDate = LocalDate.of(year, month, day)//java LocalDate로 변환
    fun toStringDate():String = "$year-$month-$day"//String로 변환
}
