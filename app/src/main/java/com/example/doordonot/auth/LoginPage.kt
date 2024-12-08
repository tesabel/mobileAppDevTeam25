//auth/LoginPage

package com.example.doordonot.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.doordonot.ui.components.TopBar

@Composable
fun LoginPage(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val showDateAlert by authViewModel.showDateAlert.collectAsState()

    // 로그인 성공 후 날짜알림(AlertDialog)
    if (showDateAlert != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("반갑습니다!") },
            text = { Text(showDateAlert!!) },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.onDateAlertDismissed()
                    // 알림을 닫고 캘린더로 이동
                    navController.navigate("calendar") {
                        popUpTo("login") { inclusive = true }
                    }
                }) {
                    Text("확인")
                }
            }
        )
    }

    Scaffold(
        topBar = { TopBar(title = "로그인") }
    ) { padding ->
        val email by authViewModel.email.collectAsState()
        val password by authViewModel.password.collectAsState()
        val errorMessage by authViewModel.errorMessage.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // 이메일 입력 필드
            TextField(
                value = email,
                onValueChange = { authViewModel.onEmailChange(it) },
                label = { Text("이메일") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 비밀번호 입력 필드
            TextField(
                value = password,
                onValueChange = { authViewModel.onPasswordChange(it) },
                label = { Text("비밀번호") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 에러 메시지 표시
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 로그인 버튼
            Button(
                onClick = {
                    authViewModel.login {
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