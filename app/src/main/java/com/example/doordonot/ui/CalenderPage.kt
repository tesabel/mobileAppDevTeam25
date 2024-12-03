// CalendarPage.kt

package com.example.doordonot.ui

import android.widget.CalendarView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.doordonot.ui.components.BottomNavigationBar
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.CalendarViewModel
import com.example.doordonot.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

@Composable
fun CalendarPage(viewModel: HabitViewModel,
    viewmodel: CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    ,navController: NavController
) {
    Scaffold(
        topBar = { TopBar(title = "캘린더") },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Calendar(navController = navController
                ,viewModel = viewModel,viewmodel = viewmodel
            )
        }
    }
}

@Composable
fun Calendar(
    navController: NavController, viewModel: HabitViewModel, viewmodel: CalendarViewModel
) {
    val selectedDate by viewmodel.selectedDate.collectAsState()
    val habits by viewModel.habits.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { CalendarView(it) }
            ) { calendarView ->
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                val selectedDateStr = "${selectedDate.year}-${selectedDate.month}-${selectedDate.day}"
                calendarView.date = formatter.parse(selectedDateStr)!!.time

                calendarView.setOnDateChangeListener { _, year, month, day ->
                    viewmodel.onDateSelected(year, month + 1, day)
                }
            }
        }
        item {
            Text(
                text = "선택된 날짜: ${selectedDate.year}-${selectedDate.month}-${selectedDate.day}",
                modifier = Modifier.padding(16.dp)
            )

        }
    }

@Composable
fun HabitItem(habit: Habit, viewModel: HabitViewModel, date: LocalDate) {
    val isChecked = viewModel.isHabitCheckedOnDate(habit, date)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = habit.name)
        Checkbox(
            checked = isChecked,
            onCheckedChange = {
                viewModel.setHabitCheck(habit, date, it)
            }
        )
    }
}}
