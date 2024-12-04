package com.example.doordonot.habit

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.model.Habit
import com.example.doordonot.model.HabitType
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.HabitViewModel

@Composable
fun AddHabitPage(
    navController: NavController,
    habitViewModel: HabitViewModel,
    authViewModel: AuthViewModel
) {
    val user by authViewModel.currentUser.collectAsState()

    // user가 null이 아닐 때만 UI를 표시
    user?.let { currentUser ->
        var habitName by remember { mutableStateOf("") }
        var habitCategory by remember { mutableStateOf("") }
        var habitType by remember { mutableStateOf(HabitType.FORMING) }
        var errorMessage by remember { mutableStateOf("") }

        Scaffold(
            topBar = { TopBar(title = "습관 등록") }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // 습관 이름 입력 필드
                TextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("습관 이름") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 카테고리 입력 필드
                TextField(
                    value = habitCategory,
                    onValueChange = { habitCategory = it },
                    label = { Text("카테고리") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 습관 타입 선택 라디오 버튼
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = habitType == HabitType.FORMING,
                        onClick = { habitType = HabitType.FORMING }
                    )
                    Text("형성 중")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = habitType == HabitType.MAINTAIN,
                        onClick = { habitType = HabitType.MAINTAIN }
                    )
                    Text("유지 중")
                }
                Spacer(modifier = Modifier.height(8.dp))

                // 에러 메시지 표시
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 습관 등록 버튼
                Button(
                    onClick = {
                        if (habitName.isBlank() || habitCategory.isBlank()) {
                            errorMessage = "모든 필드를 입력해주세요."
                        } else {
                            val habit = Habit(
                                name = habitName,
                                category = habitCategory,
                                type = habitType,
                                streak = if (habitType == HabitType.MAINTAIN) 1 else 0 // 유지 중인 습관은 기본적으로 1
                            )
                            habitViewModel.addHabit(habit, currentUser.uid) {
                                Log.d("AddHabitPage", "Habit added successfully, navigating to habit_management")
                                navController.navigate("habit_management") {
                                    popUpTo("add_habit") { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("등록")
                }
            }
        }
    }

    // user가 null인 경우 로그인 페이지로 이동
    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("add_habit") { inclusive = true }
            }
        }
    }
}