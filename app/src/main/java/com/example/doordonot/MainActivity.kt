package com.example.doordonot

import NavGraph
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.doordonot.ui.theme.DoOrDonotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoOrDonotTheme {
                NavGraph()
            }
        }
    }
}
