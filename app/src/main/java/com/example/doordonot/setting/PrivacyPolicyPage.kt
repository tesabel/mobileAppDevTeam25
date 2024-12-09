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
fun PrivacyPolicyPage(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("개인정보 처리방침") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = """
                    본 앱은 사용자의 습관 데이터를 안전하게 관리하고, 더 나은 사용자 경험을 제공하기 위해 Firebase Firestore를 사용합니다.
                    수집된 개인정보는 사용자 인증을 위한 이메일 주소 및 비밀번호, 습관 목록 및 진행 상태 등으로 구성됩니다.
                    이러한 정보는 오직 사용자의 습관 추적 및 관리를 목적으로만 사용되며, 제3자에게는 제공되지 않습니다.
                    데이터 보안을 위해 Firebase의 최신 보안 기술을 적용하고 있으며, 사용자는 언제든지 자신의 데이터를 조회, 수정, 삭제할 수 있습니다.
                    본 앱은 법적 요구사항을 준수하며, 개인정보 보호에 최선을 다하고 있습니다.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyLarge
            )
            // 실제 개인정보 처리방침 내용을 여기에 작성
        }
    }
}