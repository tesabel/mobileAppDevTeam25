// CalendarPage.kt

package com.example.doordonot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doordonot.ui.components.BottomNavigationBar
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.HabitViewModel
import java.time.LocalDate

@Composable
fun CalendarPage(navController: NavController, viewModel: HabitViewModel) {
    Scaffold(
        topBar = { TopBar(title = "캘린더") },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        var selectedDate by remember { mutableStateOf(LocalDate.now()) }
        val habits by viewModel.habits.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 캘린더 부분은 생략 (실제 구현 필요)
            DateSelector(selectedDate) { date ->
                selectedDate = date
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 형성중인 습관 리스트
            Text(
                text = "형성중인 습관",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
            LazyColumn {
                items(habits.filter { !it.isMaintained }) { habit ->
                    HabitItem(habit, viewModel, selectedDate)
                }
            }

            // 유지중인 습관 리스트
            Text(
                text = "유지중인 습관",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )
            LazyColumn {
                items(habits.filter { it.isMaintained }) { habit ->
                    HabitItem(habit, viewModel, selectedDate)
                }
            }
        }
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
}

@Composable
fun DateSelector(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    // 간단한 날짜 선택 UI 예시
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "선택된 날짜: $selectedDate")
        Button(onClick = { onDateSelected(selectedDate.minusDays(1)) }) {
            Text("이전 날짜")
        }
        Button(onClick = { onDateSelected(selectedDate.plusDays(1)) }) {
            Text("다음 날짜")
        }
    }
}
