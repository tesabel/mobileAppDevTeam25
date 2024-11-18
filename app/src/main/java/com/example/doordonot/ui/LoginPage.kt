package com.example.doordonot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.HabitViewModel

@Composable
fun LoginPage(navController: NavController, viewModel: HabitViewModel) {
    Scaffold(
        topBar = { TopBar(title = "로그인") }
    ) { padding ->
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("아이디") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // 로그인 로직을 추가하세요
                    navController.navigate("calendar")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("로그인")
            }
        }
    }
}
