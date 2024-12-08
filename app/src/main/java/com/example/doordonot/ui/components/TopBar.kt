package com.example.doordonot.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavController
import com.example.doordonot.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, navController: NavController? = null) {
    TopAppBar(
        colors = TopAppBarColors(
            containerColor = colorResource(id = R.color.blue),
            scrolledContainerColor =  colorResource(id = R.color.blue),
            navigationIconContentColor = colorResource(id = R.color.white),
            titleContentColor = colorResource(id = R.color.white),
            actionIconContentColor=  colorResource(id = R.color.blue),
        ),
        title = {
            Text(
                text = title,
                color = Color.White
            )
        },
        navigationIcon = {
            navController?.let {
                IconButton(onClick = { it.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기", tint = Color.White)
                }
            }
        },

    )
}

