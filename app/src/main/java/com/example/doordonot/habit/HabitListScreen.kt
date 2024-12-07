// com.example.doordonot.habit.HabitListScreen.kt
package com.example.doordonot.habit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.model.Habit
import com.example.doordonot.viewmodel.HabitViewModel

@Composable
fun HabitListScreen(
    navController: NavController,
    habitViewModel: HabitViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val habits by habitViewModel.habits.collectAsState()
    val user by authViewModel.currentUser.collectAsState()

    user?.let { currentUser ->
        LaunchedEffect(currentUser.uid) {
            habitViewModel.loadHabits(currentUser.uid)
        }

        Scaffold(
            topBar = { TopBar(title = "습관 목록") },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("make_habit") },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "습관 추가")
                }
            }
        ) { padding ->
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "등록된 습관이 없습니다.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    items(habits) { habit ->
                        HabitItem(habit = habit, onClick = {
                            if (habit.id.isNotBlank()) {
                                navController.navigate("habit_detail/${habit.id}")
                            } else {
                                // Debugging용 로그 추가
                                println("Invalid habit ID: ${habit.id}")
                            }
                        })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // 사용자 정보가 없는 경우 로그인 페이지로 이동
    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("habit_list") { inclusive = true }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = habit.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "카테고리: ${habit.category}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "연속 성공: ${habit.streak}", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = habit.type.name, style = MaterialTheme.typography.bodyMedium)
        }
    }
}