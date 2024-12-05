package com.example.doordonot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.HabitViewModel

@Composable
fun MakeHabitPage(
    navController: NavController,
    viewModel: HabitViewModel = HabitViewModel(),
    authViewModel: AuthViewModel
) {
    Scaffold(
        topBar = { TopBar(title = "습관 추가") }
    ) { padding ->
        var habitName by remember { mutableStateOf("") }
        var selectedHabitType by remember { mutableStateOf("형성중인 습관") }

        val habitTypes = listOf("형성중인 습관", "유지중인 습관")
        val habitTypeColors = mapOf(
            "형성중인 습관" to Color(248, 84, 83),
            "유지중인 습관" to Color(13, 146, 244)
        )

        var isCategoryExpanded by remember { mutableStateOf(false) }
        val categories = listOf("금지", "운동", "공부")
        var selectedCategories by remember { mutableStateOf(setOf<String>()) }

        var transitionDays by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 습관 이름 입력
            Text(text = "습관 이름", style = MaterialTheme.typography.titleMedium)
            TextField(
                value = habitName,
                onValueChange = { habitName = it },
                placeholder = { Text("습관 이름을 입력하세요") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 습관 유형 선택
            Text(text = "습관 유형", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                habitTypes.forEach { habitType ->
                    val isSelected = selectedHabitType == habitType
                    val backgroundColor = habitTypeColors[habitType] ?: Color.Gray

                    Card(
                        modifier = Modifier
                            .width(150.dp)
                            .padding(8.dp)
                            .clickable {
                                selectedHabitType = habitType
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = backgroundColor
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = habitType, color = Color.White)
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                enabled = false,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color.White,
                                    uncheckedColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 카테고리 분류 드롭다운
            Text(text = "카테고리 분류", style = MaterialTheme.typography.titleMedium)
            Box {
                Text(
                    text = if (selectedCategories.isEmpty()) "카테고리 선택" else selectedCategories.joinToString(", "),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCategoryExpanded = !isCategoryExpanded }
                        .background(Color.LightGray)
                        .padding(8.dp)
                )
                DropdownMenu(
                    expanded = isCategoryExpanded,
                    onDismissRequest = { isCategoryExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                if (selectedCategories.contains(category)) {
                                    selectedCategories = selectedCategories - category
                                } else {
                                    selectedCategories = selectedCategories + category
                                }
                            },
                            leadingIcon = {
                                Checkbox(
                                    checked = selectedCategories.contains(category),
                                    onCheckedChange = null
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 유지 중인 습관 전환 일수 입력
            if (selectedHabitType == "형성중인 습관") {
                Text(text = "유지 중인 습관 전환 일수", style = MaterialTheme.typography.titleMedium)
                TextField(
                    value = transitionDays,
                    onValueChange = { transitionDays = it.filter { char -> char.isDigit() } },
                    placeholder = { Text("일수를 입력하세요") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 저장 버튼
//            Button(
//                onClick = {
//                    val habit = Habit(
//                        name = habitName,
//                        categories = selectedCategories.toList(),
//                        isMaintained = selectedHabitType == "유지중인 습관",
//                        transitionDays = transitionDays.toIntOrNull() ?: 0
//                    )
//                    viewModel.addHabit(habit)
//                    navController.navigate("habit_management")
//                },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("습관 추가")
//            }
        }
    }
}
