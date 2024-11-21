package com.example.doordonot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
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
        var errorMessage by remember { mutableStateOf("") }

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
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "아이디와 비밀번호를 모두 입력해주세요."
                    } else if (!isValidCredentials(username, password)) {
                        errorMessage = "유효하지 않은 아이디 또는 비밀번호입니다."
                    } else {
                        errorMessage = ""
                        navController.navigate("calendar")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("로그인")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 회원가입 페이지로 이동
            TextButton(
                onClick = { navController.navigate("signup") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("회원가입")
            }
        }
    }
}

fun isValidCredentials(username: String, password: String): Boolean {
    return username == "test" && password == "1234"
}