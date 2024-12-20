package com.example.doordonot

import HabitDetailScreen
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.auth.LoginPage
import com.example.doordonot.auth.SignUpPage
import com.example.doordonot.calendar.CalendarPage
import com.example.doordonot.calendar.CalendarViewModel
import com.example.doordonot.habit.AddHabitPage
import com.example.doordonot.habit.HabitListScreen
import com.example.doordonot.setting.PrivacyPolicyPage
import com.example.doordonot.setting.SettingsPage
import com.example.doordonot.setting.TermsOfServicePage
import com.example.doordonot.ui.HabitManagementPage
import com.example.doordonot.ui.MakeHabitPage
import com.example.doordonot.viewmodel.HabitViewModel

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val habitViewModel: HabitViewModel = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginPage(navController, authViewModel) }
        composable("signup") { SignUpPage(navController, authViewModel) }
        composable("calendar") { CalendarPage(habitViewModel, calendarViewModel, navController, authViewModel) }
        composable("habit_management") { HabitManagementPage(navController, habitViewModel, authViewModel) }
        composable("make_habit") { MakeHabitPage(navController, habitViewModel, authViewModel) }
        composable("settings") { SettingsPage(navController, authViewModel, habitViewModel) }
        composable("terms_of_service") { TermsOfServicePage(navController) }
        composable("privacy_policy") { PrivacyPolicyPage(navController) }

        composable("habit_list") { HabitListScreen(navController, habitViewModel, authViewModel) }
        composable("add_habit") { AddHabitPage(navController, habitViewModel, authViewModel) }
        composable("habit_detail/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
            HabitDetailScreen(
                navController = navController,
                habitId = habitId,
                habitViewModel = habitViewModel,
                authViewModel = authViewModel
            )
        }
    }
}
