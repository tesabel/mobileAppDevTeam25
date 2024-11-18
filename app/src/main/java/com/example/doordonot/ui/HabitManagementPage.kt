// HabitManagementPage.kt

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
fun HabitManagementPage(navController: NavController, viewModel: HabitViewModel) {
    Scaffold(
        topBar = { TopBar(title = "습관 관리") },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("make_habit") }) {
                Text("+")
            }
        }
    ) { padding ->
        val habits by viewModel.habits.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(text = "형성중인 습관", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
            LazyColumn {
                items(habits.filter { !it.isMaintained }) { habit ->
                    HabitCard(habit, viewModel)
                }
            }

            Text(text = "유지중인 습관", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
            LazyColumn {
                items(habits.filter { it.isMaintained }) { habit ->
                    HabitCard(habit, viewModel)
                }
            }
        }
    }
}

@Composable
fun HabitCard(habit: Habit, viewModel: HabitViewModel) {
    val today = LocalDate.now()
    val isCheckedToday = viewModel.isHabitCheckedOnDate(habit, today)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = habit.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "카테고리: ${habit.categories.joinToString(", ")}")
            Text(text = "총 성공 일수: ${viewModel.getTotalDays(habit)}")
            Text(text = "연속 성공 일수: ${viewModel.getConsecutiveDays(habit)}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = if (habit.isMaintained) "유지중인 습관" else "형성중인 습관")
                Checkbox(
                    checked = isCheckedToday,
                    onCheckedChange = {
                        viewModel.setHabitCheck(habit, today, it)
                    }
                )
            }
        }
    }
}
