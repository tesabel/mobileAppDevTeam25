package com.example.doordonot.ui

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doordonot.ui.components.TopBar

@Composable
fun PrivacyPolicyPage(navController: NavController) {
    Scaffold(
        topBar = { TopBar(title = "개인정보 처리방침") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "개인정보 처리방침 내용 블라블라.",
                style = MaterialTheme.typography.bodyLarge
            )
            // 실제 개인정보 처리방침 내용을 여기에 작성
        }
    }
}