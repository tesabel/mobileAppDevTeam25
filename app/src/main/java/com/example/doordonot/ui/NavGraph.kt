// NavGraph.kt

package com.example.doordonot

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doordonot.auth.LoginPage
import com.example.doordonot.auth.SignUpPage
import com.example.doordonot.habit.AddHabitPage
import com.example.doordonot.setting.PrivacyPolicyPage
import com.example.doordonot.setting.SettingsPage
import com.example.doordonot.setting.TermsOfServicePage
import com.example.doordonot.viewmodel.CalendarViewModel
import com.example.doordonot.viewmodel.HabitViewModel
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.habit.HabitDetailScreen
import com.example.doordonot.habit.HabitListScreen
import com.example.doordonot.ui.CalendarPage
import com.example.doordonot.ui.HabitManagementPage
import com.example.doordonot.ui.MakeHabitPage

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    // ViewModel 인스턴스 생성
    val habitViewModel: HabitViewModel = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginPage(navController, authViewModel) }
        composable("signup") { SignUpPage(navController, authViewModel) }
        composable("calendar") { CalendarPage(viewModel = habitViewModel, calendarViewModel, navController, authViewModel) }
        composable("habit_management") { HabitManagementPage(navController, habitViewModel, authViewModel) }
        composable("make_habit") { MakeHabitPage(navController, habitViewModel, authViewModel) }
        // 설정 페이지 추가
        composable("settings") { SettingsPage(navController, authViewModel, habitViewModel) }
        // 이용약관 페이지 추가
        composable("terms_of_service") { TermsOfServicePage(navController) }
        // 개인정보 처리방침 페이지 추가
        composable("privacy_policy") { PrivacyPolicyPage(navController) }
        // 습관 등록 페이지 추가
        composable("add_habit") { AddHabitPage(navController, habitViewModel, authViewModel) }
        // 기존 습관 목록 페이지 추가
        composable("habit_list") {
            HabitListScreen(navController, habitViewModel, authViewModel)
        }

        composable("habit_detail/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
            HabitDetailScreen(navController = navController, habitId = habitId)
        }
    }
}