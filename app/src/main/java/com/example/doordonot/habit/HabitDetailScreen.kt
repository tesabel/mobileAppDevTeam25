// com.example.doordonot.habit.HabitDetailScreen.kt
package com.example.doordonot.habit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.model.DailyStatus
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitDetailScreen(
    navController: NavController,
    habitId: String,
    habitViewModel: HabitViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val user by authViewModel.currentUser.collectAsState()
    val dailyStatuses by habitViewModel.dailyStatuses.collectAsState()
    val currentHabit by habitViewModel.currentHabit.collectAsState()
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }

    user?.let { currentUser ->
        LaunchedEffect(habitId, currentUser.uid) {
            habitViewModel.loadHabit(habitId, currentUser.uid)
            habitViewModel.loadDailyStatuses(habitId, currentUser.uid)
        }

        Scaffold(
            topBar = { TopBar(title = "습관 상세") }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // 습관 상세 정보 표시
                currentHabit?.let { habit ->
                    Text(text = habit.name, style = MaterialTheme.typography.titleLarge)
                    Text(text = "카테고리: ${habit.category}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "연속 성공: ${habit.streak}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "타입: ${habit.type.name}", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 날짜 선택 UI (DatePicker)
                DatePicker(
                    selectedDate = selectedDate,
                    onDateSelected = { date ->
                        selectedDate = date
                        // 날짜 선택 시 DailyStatus 가져오기 또는 초기화
                        habitViewModel.getOrInitializeDailyStatus(
                            habitId = habitId,
                            userId = user!!.uid,
                            date = date
                        ) { status ->
                            if (status == null) {
                                // 오류 처리 (예: 토스트 메시지)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 선택된 날짜의 DailyStatus 표시
                val dailyStatus = dailyStatuses.find { it.date == selectedDate }

                if (dailyStatus != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "날짜: $selectedDate", style = MaterialTheme.typography.titleMedium)
                        Checkbox(
                            checked = dailyStatus.isChecked,
                            onCheckedChange = { isChecked ->
                                val updatedStatus = dailyStatus.copy(isChecked = isChecked)
                                habitViewModel.updateDailyStatus(
                                    habitId = habitId,
                                    userId = user!!.uid,
                                    dailyStatus = updatedStatus
                                )
                            }
                        )
                    }
                } else {
                    Text(text = "날짜별 상태를 불러오는 중...", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 전체 DailyStatus 목록 표시
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                ) {
                    items(dailyStatuses) { status ->
                        DailyStatusItem(status = status, onStatusChange = { isChecked ->
                            val updatedStatus = status.copy(isChecked = isChecked)
                            habitViewModel.updateDailyStatus(
                                habitId = habitId,
                                userId = user!!.uid,
                                dailyStatus = updatedStatus
                            )
                        })
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }

    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("habit_detail") { inclusive = true }
            }
        }
    }
}

@Composable
fun DailyStatusItem(status: DailyStatus, onStatusChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = status.date, style = MaterialTheme.typography.bodyMedium)
        Checkbox(
            checked = status.isChecked,
            onCheckedChange = { isChecked ->
                onStatusChange(isChecked)
            }
        )
    }
}

//@Composable
//fun DatePicker(selectedDate: String, onDateSelected: (String) -> Unit) {
//    var showDialog by remember { mutableStateOf(false) }
//
//    TextField(
//        value = selectedDate,
//        onValueChange = {},
//        label = { Text("날짜 선택") },
//        enabled = false,
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { showDialog = true }
//    )
//    if (showDialog) {
//        val datePickerDialog = android.app.DatePickerDialog(
//            LocalContext.current,
//            { _, year, month, dayOfMonth ->
//                val calendar = Calendar.getInstance().apply {
//                    set(year, month, dayOfMonth)
//                }
//                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                val dateStr = dateFormat.format(calendar.time)
//                onDateSelected(dateStr)
//                showDialog = false
//            },
//            Calendar.getInstance().get(Calendar.YEAR),
//            Calendar.getInstance().get(Calendar.MONTH),
//            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
//        )
//        datePickerDialog.show()
//    }
//}

// Helper 함수: 현재 날짜 가져오기
fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}