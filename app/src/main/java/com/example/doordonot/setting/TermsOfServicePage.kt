package com.example.doordonot.setting

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServicePage(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("이용약관") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    )  { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = """
                    본 앱을 사용함으로써 사용자는 모든 법적 책임을 수락하는 것으로 간주됩니다.
                    사용자는 자신의 계정 정보의 보안을 책임지며, 무단 사용에 대해 즉시 통보해야 합니다.
                    본 앱은 제공되는 기능에 대하여 어떠한 보증도 하지 않으며, 사용 중 발생할 수 있는 손해에 대해 책임지지 않습니다.
                    사용자는 앱을 통해 불법적이거나 유해한 활동을 하지 않을 것을 동의하며, 이러한 행위가 발견될 경우 계정이 정지될 수 있습니다.
                    본 약관은 사전 통보 없이 변경될 수 있으며, 변경된 약관은 앱을 통해 공지됩니다.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyLarge
            )
            // 실제 이용약관 내용을 여기에 작성
        }
    }
}