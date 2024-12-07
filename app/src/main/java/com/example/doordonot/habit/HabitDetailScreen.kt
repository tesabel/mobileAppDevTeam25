import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment // Alignment import 추가
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.model.Habit
import com.example.doordonot.viewmodel.HabitViewModel


@Composable
fun HabitDetailScreen(
    navController: NavController,
    habitId: String,
    habitViewModel: HabitViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val user by authViewModel.currentUser.collectAsState()
    val currentHabit by habitViewModel.currentHabit.collectAsState()

    if (user == null || habitId.isBlank()) {
        LaunchedEffect(Unit) {
            navController.navigate("habit_list") {
                popUpTo("habit_detail") { inclusive = true }
            }
        }
        return
    }

    LaunchedEffect(habitId, user!!.uid) {
        habitViewModel.loadHabit(habitId, user!!.uid)
    }

    Scaffold(
        topBar = {
            TopBarWithBackButton(
                title = "습관 상세",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (currentHabit != null) {
                HabitDetailContent(habit = currentHabit!!)
            } else {
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
fun HabitDetailContent(habit: Habit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "습관 이름: ${habit.name}", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "카테고리: ${habit.category}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "연속 성공: ${habit.streak}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "타입: ${habit.type.name}", style = MaterialTheme.typography.bodyMedium)
    }
}