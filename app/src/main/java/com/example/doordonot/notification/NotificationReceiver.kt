package com.example.doordonot.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.doordonot.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        showNotification(context)
    }

    private fun showNotification(context: Context) {
        val channelId = "daily_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminder",
                NotificationManager.IMPORTANCE_HIGH // 중요도 높게 설정
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // 기본 시스템 아이콘 사용
            .setContentTitle("습관 알림")
            .setContentText("지금 습관을 체크해보세요!")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 우선순위 높게 설정
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}