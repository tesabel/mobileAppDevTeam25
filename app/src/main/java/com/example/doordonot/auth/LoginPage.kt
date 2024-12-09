package com.example.doordonot.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doordonot.R
import com.example.doordonot.ui.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val showDateAlert by authViewModel.showDateAlert.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // 이미 로그인되어 있다면 로그인 페이지를 건너뛰고 바로 캘린더로 이동
    // 단, showDateAlert가 null이 아닐 경우 알림을 먼저 띄운 후 이동
    LaunchedEffect(currentUser) {
        if (currentUser != null && showDateAlert == null) {
            navController.navigate("calendar") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

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

    // 이미 로그인된 상태라면(알림까지 떴다면) UI를 보여줄 필요 없음
    if (currentUser != null && showDateAlert != null) {
        // 알림이 뜰 때까지 잠깐 빈 화면 유지
        Box(modifier = Modifier.fillMaxSize().background(colorResource(id = R.color.white))) {}
        return
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
                .background(colorResource(id = R.color.white))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.image02),
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Fit
            )

            Image(
                painter = painterResource(id = R.drawable.title),
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 25.dp)
                    .height(100.dp),
                contentScale = ContentScale.Fit
            )

            // 이메일 입력 필드
            TextField(
                value = email,
                onValueChange = { authViewModel.onEmailChange(it) },
                label = { Text("이메일") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray
                ),
                shape = RoundedCornerShape(16.dp)
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
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray
                ),
                shape = RoundedCornerShape(16.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

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
