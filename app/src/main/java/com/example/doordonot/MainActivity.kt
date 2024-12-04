package com.example.doordonot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.doordonot.ui.theme.DoOrDonotTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.example.doordonot.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase 초기화
        FirebaseApp.initializeApp(this)

        // Firestore 인스턴스 가져오기
        val db = FirebaseFirestore.getInstance()

        setContent {
            DoOrDonotTheme {
                NavGraph()
            }
        }
    }
}