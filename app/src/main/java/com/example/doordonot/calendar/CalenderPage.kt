// CalendarPage.kt

package com.example.doordonot.calendar

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.model.Habit
import com.example.doordonot.model.HabitDisplay
import com.example.doordonot.model.HabitType
import com.example.doordonot.ui.components.BottomNavigationBar
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.Locale
@Composable
fun CalendarPage(
    habitViewModel: HabitViewModel = viewModel(),
    calendarViewModel: CalendarViewModel = viewModel(),
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val selectedDate by calendarViewModel.selectedDate.collectAsState()

    val selectedDateHabitDisplays by habitViewModel.selectedDateHabitDisplays.collectAsState()
    val formingHabits = selectedDateHabitDisplays.filter { it.habit.type == HabitType.FORMING }
    val maintainHabits = selectedDateHabitDisplays.filter { it.habit.type == HabitType.MAINTAIN }

    val errorMessage by habitViewModel.errorMessage.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    Scaffold(
        topBar = { TopBar(title = "캘린더") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("make_habit") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "습관 추가")
            }
        },bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        if (currentUser != null)
        { LaunchedEffect(selectedDate, currentUser!!.uid) {
            // 선택된 날짜에 해당하는 습관 데이터 로드
            habitViewModel.loadHabitsForDate(currentUser!!.uid, selectedDate.toLocalDate())
        }}
        Column(modifier = Modifier.padding(innerPadding)) {
            // 캘린더 컴포넌트
            Calendar(calendarViewModel = calendarViewModel)

            // 선택된 날짜 정보
            Text(
                text = "선택된 날짜: ${selectedDate.year}-${selectedDate.month}-${selectedDate.day}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )

            // 에러 메시지 표시
            errorMessage.takeIf { it.isNotEmpty() }?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 형성 중인 습관 섹션
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "형성 중인 습관",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
                            .background(
                                Color(248, 84, 83, 255)
                            )
                    )
                    if (formingHabits.isEmpty()) {
                        Text(
                            text = "선택된 날짜에 형성 중인 습관이 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp).fillMaxWidth().align(Alignment.CenterHorizontally)
                        ) {
                            items(formingHabits) { habitDisplay ->
                                HabitDisplayCard(habitDisplay)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 유지 중인 습관 섹션
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "유지 중인 습관",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
                            .background(
                            Color(13, 146, 244)
                        )
                    )
                    if (maintainHabits.isEmpty()) {
                        Text(
                            text = "선택된 날짜에 유지 중인 습관이 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp).fillMaxWidth().align(Alignment.CenterHorizontally)
                        ) {
                            items(maintainHabits) { habitDisplay ->
                                HabitDisplayCard(habitDisplay)
                            }
                        }
                    }
                }
            }


        }
    }
}

@Composable
fun Calendar(calendarViewModel: CalendarViewModel) {
    val selectedDate by calendarViewModel.selectedDate.collectAsState()

    AndroidView(
        factory = { CalendarView(it) },
        modifier = Modifier.fillMaxWidth()
    ) { calendarView ->
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        val selectedDateStr = "${selectedDate.year}-${selectedDate.month}-${selectedDate.day}"
        calendarView.date = formatter.parse(selectedDateStr)!!.time

        calendarView.setOnDateChangeListener { _, year, month, day ->
            calendarViewModel.onDateSelected(year, month + 1, day)
        }
    }
}



// 변경 부분 시작: 새로 추가할 HabitDisplayCard
@Composable
fun HabitDisplayCard(habitDisplay: HabitDisplay) {
    Card(
        modifier = Modifier
            .width(200.dp) // 카드의 고정된 가로 크기
            .height(120.dp)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = habitDisplay.habit.name, style = MaterialTheme.typography.titleLarge)
            //Text(text = "카테고리: ${habitDisplay.habit.category}", style = MaterialTheme.typography.bodyMedium)
            Column (modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally))
            {
                Text(text = "연속 성공 일수: ${habitDisplay.habit.streak}", style = MaterialTheme.typography.bodySmall)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "오늘의 체크 상태:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    // 체크박스 클릭 불가능하도록 enabled = false
                    Checkbox(
                        checked = habitDisplay.isChecked,
                        onCheckedChange = null,
                        enabled = false
                    )
                }
            }


        }
    }
}

