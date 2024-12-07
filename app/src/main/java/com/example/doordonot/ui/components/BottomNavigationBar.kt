package com.example.doordonot.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        // "습관 추가" 버튼 삭제함
        NavigationBarItem(
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = "캘린더") },
            label = { Text("캘린더") },
            selected = false,
            onClick = { navController.navigate("calendar") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "습관 관리") },
            label = { Text("습관 관리") },
            selected = false,
            onClick = { navController.navigate("habit_management") }
        )
        // 설정 탭
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "설정") },
            label = { Text("설정") },
            selected = false,
            onClick = { navController.navigate("settings") }
        )
    }
}
