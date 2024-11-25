// NavGraph.kt

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doordonot.ui.CalendarPage
import com.example.doordonot.ui.HabitManagementPage
import com.example.doordonot.ui.LoginPage
import com.example.doordonot.ui.MakeHabitPage
import com.example.doordonot.ui.SignUpPage
import com.example.doordonot.viewmodel.HabitViewModel

// NavGraph.kt

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val viewModel: HabitViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginPage(navController) }
        composable("signup") { SignUpPage(navController) }
        composable("calendar") { CalendarPage(navController, viewModel) }
        composable("habit_management") { HabitManagementPage(navController, viewModel) }
        composable("make_habit") { MakeHabitPage(navController, viewModel) }
    }
}
