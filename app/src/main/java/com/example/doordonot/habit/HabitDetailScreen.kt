import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.model.Habit
import com.example.doordonot.viewmodel.HabitViewModel
import kotlinx.coroutines.launch
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
    val currentHabit by habitViewModel.currentHabit.collectAsState()

    // user와 habitId가 유효하지 않으면 리스트 화면으로 이동
    if (user == null || habitId.isBlank()) {
        LaunchedEffect(Unit) {
            println("HabitDetailScreen: Invalid user or habitId, navigating back to habit_list.")
            navController.navigate("habit_list") {
                popUpTo("habit_detail") { inclusive = true }
            }
        }
        return
    }

    // user가 non-null임을 보장하는 let 블록
    user?.let { nonNullUser ->
        LaunchedEffect(habitId, nonNullUser.uid) {
            println("HabitDetailScreen: Loading habit detail for habitId: $habitId, userId: ${nonNullUser.uid}")
            habitViewModel.loadHabit(habitId, nonNullUser.uid)
        }

        Scaffold(
            topBar = {
                TopBarWithBackButton(
                    title = "습관 상세",
                    onBackClick = {
                        println("HabitDetailScreen: Back button pressed, navigating back.")
                        navController.popBackStack()
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // currentHabit 로딩 완료 여부에 따라 UI 변경
                if (currentHabit != null) {
                    println("HabitDetailScreen: Habit loaded: ${currentHabit!!.name}, preparing UI...")
                    HabitDetailContent(
                        habit = currentHabit!!,
                        habitViewModel = habitViewModel,
                        userId = nonNullUser.uid
                    )
                } else {
                    // currentHabit가 아직 null이면 로딩 스피너 표시
                    println("HabitDetailScreen: Habit not loaded yet, showing spinner.")
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "습관 정보를 불러오는 중...")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithBackButton(title: String, onBackClick: () -> Unit) {
    println("TopBarWithBackButton: $title")
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
        }
    )
}

@Composable
fun HabitDetailContent(
    habit: Habit,
    habitViewModel: HabitViewModel,
    userId: String
) {
    // ViewModel의 DailyStatus 목록을 상태로 관찰
    val dailyStatuses by habitViewModel.currentHabitDailyStatuses.collectAsState()
    val currentDate = getCurrentDate()
    val todayStatus = dailyStatuses.find { it.date == currentDate }

    // 실시간 업데이트 리스너 설정
    LaunchedEffect(habit.id, userId) {
        habitViewModel.observeDailyStatuses(habit.id, userId)
    }

    // DailyStatus 초기화 필요 시 ViewModel 호출
    LaunchedEffect(habit.id, userId, currentDate) {
        if (todayStatus == null) {
            habitViewModel.getOrInitializeDailyStatus(
                habit.id,
                userId,
                currentDate
            ) {}
        }
    }

    // 로딩 상태 및 체크 여부를 ViewModel의 상태로부터 파생
    val isLoading = todayStatus == null
    val isChecked = todayStatus?.isChecked ?: false

    // Snackbar 상태 관리
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "습관 이름: ${habit.name}", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "카테고리: ${habit.category}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "연속 성공: ${habit.streak}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "타입: ${habit.type.name}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "오늘 체크 여부", style = MaterialTheme.typography.titleMedium)

            Row {
                Button(
                    onClick = {
                        println("HabitDetailContent: '완료' 버튼 클릭")
                        habitViewModel.setDailyStatus(
                            habitId = habit.id,
                            userId = userId,
                            date = currentDate,
                            isChecked = true
                        ) { success ->
                            coroutineScope.launch {
                                if (success) {
                                    println("HabitDetailContent: '완료' 설정 성공")
                                    snackbarHostState.showSnackbar("습관 완료로 설정되었습니다.")
                                } else {
                                    println("HabitDetailContent: '완료' 설정 실패")
                                    snackbarHostState.showSnackbar("습관 완료 설정에 실패했습니다.")
                                }
                            }
                        }
                    },
                    enabled = !isLoading && !isChecked, // 이미 완료된 경우 비활성화
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(text = "완료")
                }

                Button(
                    onClick = {
                        println("HabitDetailContent: '실패' 버튼 클릭")
                        habitViewModel.setDailyStatus(
                            habitId = habit.id,
                            userId = userId,
                            date = currentDate,
                            isChecked = false
                        ) { success ->
                            coroutineScope.launch {
                                if (success) {
                                    println("HabitDetailContent: '실패' 설정 성공")
                                    snackbarHostState.showSnackbar("습관 실패로 설정되었습니다.")
                                } else {
                                    println("HabitDetailContent: '실패' 설정 실패")
                                    snackbarHostState.showSnackbar("습관 실패 설정에 실패했습니다.")
                                }
                            }
                        }
                    },
                    enabled = !isLoading && !isChecked, // 아직 완료되지 않은 경우 비활성화
                ) {
                    Text(text = "실패")
                }
            }
        }

        // Snackbar 호스팅
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(top = 16.dp))
    }
}

// Helper 함수: 현재 날짜 가져오기
fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}