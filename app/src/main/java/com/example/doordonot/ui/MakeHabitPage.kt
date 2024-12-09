package com.example.doordonot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.model.Habit
import com.example.doordonot.model.HabitType
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeHabitPage(
    navController: NavController,
    viewModel: HabitViewModel,
    authViewModel: AuthViewModel
) {
    val user by authViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = { TopBar(title = "습관 추가", navController = navController) }
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
        var selectedCategory by remember { mutableStateOf<String?>(null) }

        var transitionDays by remember { mutableStateOf("") }

        // 확인 버튼 활성화 조건 체크
        val isForming = (selectedHabitType == "형성중인 습관")
        val isHabitNameValid = habitName.isNotBlank()
        val isCategorySelected = (selectedCategory != null)
        val isTransitionValid = if (isForming) transitionDays.isNotBlank() && transitionDays.toIntOrNull() != null else true

        val isConfirmEnabled = isHabitNameValid && isCategorySelected && isTransitionValid

        user?.let { currentUser ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // 습관 이름 입력
                Text(text = "습관 이름", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    placeholder = { Text("습관 이름을 입력하세요") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = Color.Gray,
                        unfocusedLabelColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(16.dp) // 둥근 모서리
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 습관 유형 선택
                Text(text = "습관 유형", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    habitTypes.forEach { habitType ->
                        val isSelected = (selectedHabitType == habitType)
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

                Spacer(modifier = Modifier.height(20.dp))

                // 카테고리 분류 (단일 선택)
                Text(text = "카테고리 분류", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    Text(
                        text = selectedCategory ?: "카테고리 선택",
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
                                    selectedCategory = category
                                    isCategoryExpanded = false
                                },
                                leadingIcon = {
                                    RadioButton(
                                        selected = (selectedCategory == category),
                                        onClick = {
                                            selectedCategory = category
                                            isCategoryExpanded = false
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 유지 중인 습관 전환 일수 입력 (형성중인 습관일 때만)
                if (isForming) {
                    Text(text = "유지 중인 습관 전환 일수", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = transitionDays,
                        onValueChange = { transitionDays = it.filter { char -> char.isDigit() } },
                        placeholder = { Text("일수를 입력하세요") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(16.dp) // 둥근 모서리
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 확인 버튼
                Button(
                    onClick = {
                        val categoryString = selectedCategory ?: ""
                        val habitType = if (isForming) HabitType.FORMING else HabitType.MAINTAIN
                        val streak = if (habitType == HabitType.MAINTAIN) 1 else 0

                        val habit = Habit(
                            name = habitName,
                            category = categoryString,
                            type = habitType,
                            streak = streak
                        )

                        viewModel.addHabit(habit, currentUser.uid) {
                            navController.navigate("habit_management") {
                                popUpTo("make_habit") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isConfirmEnabled
                ) {
                    Text("확인")
                }
            }
        }

        // user가 null이면 로그인 페이지로 이동
        if (user == null) {
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo("make_habit") { inclusive = true }
                }
            }
        }
    }
}
