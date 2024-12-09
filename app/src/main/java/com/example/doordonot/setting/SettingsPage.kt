package com.example.doordonot.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.ui.components.BottomNavigationBar
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.HabitViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.doordonot.notification.NotificationViewModel

@Composable
fun SettingsPage(
    navController: NavController,
    authViewModel: AuthViewModel,
    habitViewModel: HabitViewModel,
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // 알림 활성화 상태 관리 (실제 앱에서는 ViewModel에서 상태를 관리하거나 DataStore에 저장 가능)
    var isNotificationEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Scaffold(
        topBar = { TopBar(title = "설정") },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 알림 On/Off 스위치 추가
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isNotificationEnabled = !isNotificationEnabled
                        notificationViewModel.setDailyReminder(context, isNotificationEnabled)
                    }
                    .padding(vertical = 16.dp)
            ) {
                Text(text = "알림 활성화", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isNotificationEnabled,
                    onCheckedChange = {
                        isNotificationEnabled = it
                        notificationViewModel.setDailyReminder(context, it)
                    }
                )
            }
            Divider()

            // 기존 버튼들
            SettingsButton(
                text = "로그아웃",
                icon = Icons.Default.Logout,
                onClick = { showLogoutDialog = true }
            )
            Divider()

            SettingsButton(
                text = "회원 탈퇴",
                icon = Icons.Default.Logout, // 적절한 아이콘으로 변경 가능
                onClick = { showDeleteAccountDialog = true }
            )
            Divider()

            SettingsButton(
                text = "이용약관",
                icon = Icons.Default.List, // 적절한 아이콘으로 변경 가능
                onClick = { navController.navigate("terms_of_service") }
            )
            Divider()

            SettingsButton(
                text = "개인정보 처리방침",
                icon = Icons.Default.List, // 적절한 아이콘으로 변경 가능
                onClick = { navController.navigate("privacy_policy") }
            )
            Divider()

//            // 새로운 습관 확인 버튼 추가
//            SettingsButton(
//                text = "습관 목록 확인",
//                icon = Icons.Default.List, // 적절한 아이콘으로 변경 가능
//                onClick = { navController.navigate("habit_list") }
//            )
//            Divider()
        }

        // 로그아웃 확인 다이얼로그
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("로그아웃") },
                text = { Text("정말로 로그아웃 하시겠습니까?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo("settings") { inclusive = true }
                            }
                            showLogoutDialog = false
                        }
                    ) {
                        Text("예")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("아니오")
                    }
                }
            )
        }

        // 회원 탈퇴 확인 다이얼로그
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                title = { Text("회원 탈퇴") },
                text = { Text("정말로 회원 탈퇴 하시겠습니까?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val user = auth.currentUser
                            user?.delete()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("login") {
                                        popUpTo("settings") { inclusive = true }
                                    }
                                } else {
                                    // 오류 처리 (예: 토스트 메시지)
                                }
                            }
                            showDeleteAccountDialog = false
                        }
                    ) {
                        Text("예")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAccountDialog = false }) {
                        Text("아니오")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}