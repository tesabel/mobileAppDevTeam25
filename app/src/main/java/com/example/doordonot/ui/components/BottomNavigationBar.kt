package com.example.doordonot.ui.components

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.doordonot.R

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = colorResource(id = R.color.blue),
        contentColor = Color.White
    ) {
        // "캘린더" 탭
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "캘린더",
                    tint = if (currentRoute == "calendar") colorResource(id = R.color.navy) else Color.White
                )
            },
            label = {
                Text(
                    text = "캘린더",
                    color = if (currentRoute == "calendar") colorResource(id = R.color.navy) else Color.White
                )
            },
            selected = currentRoute == "calendar",
            onClick = { navController.navigate("calendar") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorResource(id = R.color.navy),
                unselectedIconColor = Color.White,
                selectedTextColor = colorResource(id = R.color.navy),
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent   // 배경색을 없앰
            ),
            alwaysShowLabel = true
        )

        // "습관 관리" 탭
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "습관 관리",
                    tint = if (currentRoute == "habit_management") colorResource(id = R.color.navy) else Color.White
                )
            },
            label = {
                Text(
                    text = "습관 관리",
                    color = if (currentRoute == "habit_management") colorResource(id = R.color.navy) else Color.White
                )
            },
            selected = currentRoute == "habit_management",
            onClick = { navController.navigate("habit_management") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorResource(id = R.color.navy),
                unselectedIconColor = Color.White,
                selectedTextColor = colorResource(id = R.color.navy),
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent   // 배경색을 없앰
            ),
            alwaysShowLabel = true
        )

        // "설정" 탭
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "설정",
                    tint = if (currentRoute == "settings") colorResource(id = R.color.navy) else Color.White
                )
            },
            label = {
                Text(
                    text = "설정",
                    color = if (currentRoute == "settings") colorResource(id = R.color.navy) else Color.White
                )
            },
            selected = currentRoute == "settings",
            onClick = { navController.navigate("settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorResource(id = R.color.navy),
                unselectedIconColor = Color.White,
                selectedTextColor = colorResource(id = R.color.navy),
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent   // 배경색을 없앰
            ),
            alwaysShowLabel = true

        )
    }
}
