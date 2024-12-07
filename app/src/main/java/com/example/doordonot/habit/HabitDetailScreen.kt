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

    // 유저나 habitId가 유효하지 않으면 리스트 화면으로 이동
    if (user == null || habitId.isBlank()) {
        LaunchedEffect(Unit) {
            println("Invalid user or habitId, navigate back to list.")
            navController.navigate("habit_list") {
                popUpTo("habit_detail") { inclusive = true }
            }
        }
        return
    }

    // 유저와 habitId가 유효하므로 Firestore에서 해당 습관 로드
    LaunchedEffect(habitId, user!!.uid) {
        println("Loading habit detail for habitId: $habitId, user: ${user!!.uid}")
        habitViewModel.loadHabit(habitId, user!!.uid)
    }

    Scaffold(
        topBar = {
            TopBarWithBackButton(
                title = "습관 상세",
                onBackClick = {
                    println("Back button pressed, navigating back...")
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
                println("Habit loaded: ${currentHabit!!.name}, preparing UI...")
                HabitDetailContent(
                    habit = currentHabit,
                    habitViewModel = habitViewModel,
                    userId = user!!.uid
                )
            } else {
                // currentHabit가 아직 null이면 로딩 스피너 표시
                println("Habit not loaded yet, showing spinner.")
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
fun HabitDetailContent(habit: Habit?, habitViewModel: HabitViewModel, userId: String) {
    // habit가 null이 아닐 때만 진행
    if (habit == null) {
        println("HabitDetailContent called with null habit, showing message.")
        Text("습관 정보를 불러올 수 없습니다.")
        return
    }

    var isChecked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val currentDate = getCurrentDate()

    // currentHabit가 로딩된 이후 상태를 초기화하는 LaunchedEffect
    LaunchedEffect(habit.id) {
        println("Fetching dailyStatus for habit: ${habit.id}, date: $currentDate")
        habitViewModel.getOrInitializeDailyStatus(
            habit.id,
            userId,
            currentDate
        ) { dailyStatus ->
            if (dailyStatus != null) {
                isChecked = dailyStatus.isChecked
            } else {
                println("DailyStatus is null, initializing with default false.")
                isChecked = false
            }
            isLoading = false
        }
    }

    println("Rendering HabitDetailContent for habit: ${habit.name}, isChecked: $isChecked, isLoading: $isLoading")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "습관 이름: ${habit.name}", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "카테고리: ${habit.category}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "연속 성공: ${habit.streak}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "타입: ${habit.type.name}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "오늘 체크 여부", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
            }

            Checkbox(
                checked = isChecked,
                enabled = !isLoading,
                onCheckedChange = { checked ->
                    println("Checkbox clicked: $checked, toggling dailyStatus.")
                    isChecked = checked
                    habitViewModel.toggleDailyStatus(
                        habitId = habit.id,
                        userId = userId,
                        date = currentDate
                    ) { success ->
                        if (!success) {
                            println("Failed to toggleDailyStatus, rolling back checkbox state.")
                            isChecked = !checked
                        } else {
                            println("toggleDailyStatus succeeded, state updated.")
                        }
                    }
                }
            )
        }
    }
}

// Helper 함수: 현재 날짜 가져오기
fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}