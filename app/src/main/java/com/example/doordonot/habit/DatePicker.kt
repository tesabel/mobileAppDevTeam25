// com.example.doordonot.habit.DatePicker.kt
package com.example.doordonot.habit

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DatePicker(selectedDate: String, onDateSelected: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    TextField(
        value = selectedDate,
        onValueChange = {},
        label = { Text("날짜 선택") },
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    )
    if (showDialog) {
        val datePickerDialog = DatePickerDialog(
            LocalContext.current,
            { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateStr = dateFormat.format(calendar.time)
                onDateSelected(dateStr)
                showDialog = false
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}