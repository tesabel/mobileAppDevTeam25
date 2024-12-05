package com.example.doordonot.ui

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
import com.example.doordonot.viewmodel.AuthViewModel

@Composable
fun SignUpPage(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    Scaffold(
        topBar = { TopBar(title = "회원가입") }
    ) { padding ->
        val email by authViewModel.email.collectAsState()
        val password by authViewModel.password.collectAsState()
        val confirmPassword by authViewModel.confirmPassword.collectAsState()
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
                label = { Text("비밀번호 (8자 이상)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 비밀번호 확인 입력 필드
            TextField(
                value = confirmPassword,
                onValueChange = { authViewModel.onConfirmPasswordChange(it) },
                label = { Text("비밀번호 확인") },
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

            // 회원가입 버튼
            Button(
                onClick = {
                    authViewModel.signUp {
                        navController.navigate("login") // 회원가입 성공 시 로그인 화면으로 이동
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("회원가입")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 로그인 페이지로 이동 버튼
            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("로그인 페이지로")
            }
        }
    }
}