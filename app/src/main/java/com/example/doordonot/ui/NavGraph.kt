// NavGraph.kt

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doordonot.ui.CalendarPage
import com.example.doordonot.ui.HabitManagementPage
import com.example.doordonot.ui.LoginPage
import com.example.doordonot.ui.MakeHabitPage
import com.example.doordonot.viewmodel.HabitViewModel

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val viewModel: HabitViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginPage(navController, viewModel) }
        composable("calendar") { CalendarPage(navController, viewModel) }
        composable("habit_management") { HabitManagementPage(navController, viewModel) }
        composable("make_habit") { MakeHabitPage(navController, viewModel) }
    }
}
