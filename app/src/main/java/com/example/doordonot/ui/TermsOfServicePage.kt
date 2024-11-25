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
fun TermsOfServicePage(navController: NavController) {
    Scaffold(
        topBar = { TopBar(title = "이용약관") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "이용약관 내용 블라라ㅏㅏ",
                style = MaterialTheme.typography.bodyLarge
            )
            // 실제 이용약관 내용을 여기에 작성
        }
    }
}