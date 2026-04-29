package com.rnandresy.lol.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rnandresy.lol.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CH_MESSAGES = "askip_messages"
        const val CH_POSTS    = "askip_posts"
        private var msgId  = 1000
        private var postId = 2000
    }

    fun createChannels() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CH_MESSAGES, "Messages privés", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Nouveaux messages reçus"
                enableVibration(true)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_POSTS, "Nouveaux posts", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Activité du fil Askip"
            }
        )
    }

    private fun hasPermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        else true

    private fun tapIntent() = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun showMessageNotification(sender: String, body: String) {
        if (!hasPermission()) return
        runCatching {
            NotificationManagerCompat.from(context).notify(msgId++,
                NotificationCompat.Builder(context, CH_MESSAGES)
                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                    .setContentTitle("💬 $sender")
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(tapIntent())
                    .build()
            )
        }
    }

    fun showPostNotification(username: String, content: String) {
        if (!hasPermission()) return
        runCatching {
            NotificationManagerCompat.from(context).notify(postId++,
                NotificationCompat.Builder(context, CH_POSTS)
                    .setSmallIcon(android.R.drawable.ic_menu_share)
                    .setContentTitle("🔥 $username a posté")
                    .setContentText(content)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(tapIntent())
                    .build()
            )
        }
    }
}