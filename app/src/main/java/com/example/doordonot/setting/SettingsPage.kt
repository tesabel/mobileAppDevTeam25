package com.example.doordonot.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doordonot.ui.components.BottomNavigationBar
import com.example.doordonot.ui.components.TopBar
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsPage(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopBar(title = "설정") },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_habit") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "습관 추가")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 로그아웃
            Text(
                text = "로그아웃",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true }
                    .padding(vertical = 16.dp)
            )
            Divider()

            // 회원 탈퇴
            Text(
                text = "회원 탈퇴",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteAccountDialog = true }
                    .padding(vertical = 16.dp)
            )
            Divider()

            // 이용약관
            Text(
                text = "이용약관",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("terms_of_service") }
                    .padding(vertical = 16.dp)
            )
            Divider()

            // 개인정보 처리방침
            Text(
                text = "개인정보 처리방침",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("privacy_policy") }
                    .padding(vertical = 16.dp)
            )
            Divider()
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
                                    // 오류 처리
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