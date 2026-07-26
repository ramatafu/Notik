package com.noteflow.app.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.noteflow.app.MainActivity
import com.noteflow.app.NoteFlowApp
import com.noteflow.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BirthdayReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val birthdayId = intent.getLongExtra(EXTRA_BIRTHDAY_ID, -1L)
        val offsetDays = intent.getIntExtra(EXTRA_OFFSET_DAYS, 0)
        if (birthdayId == -1L) return

        val pendingResult = goAsync()
        val app = context.applicationContext as NoteFlowApp

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val birthday = app.birthdaysRepository.getById(birthdayId)
                if (birthday != null) {
                    val whenText = when (offsetDays) {
                        7 -> "через неделю"
                        3 -> "через 3 дня"
                        1 -> "завтра"
                        else -> "сегодня"
                    }
                    showNotification(
                        context, birthdayId, offsetDays,
                        title = "День рождения — ${birthday.name}",
                        body = "У ${birthday.name} день рождения $whenText"
                    )
                    // The "day-of" alarm firing means this occurrence is done — arm next year's set.
                    if (offsetDays == 0) BirthdayScheduler.schedule(context, birthday)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, birthdayId: Long, offsetDays: Int, title: String, body: String) {
        val openIntent = Intent(context, MainActivity::class.java)
        val requestCode = (birthdayId * 10 + offsetDays).toInt()
        val contentIntent = PendingIntent.getActivity(
            context, requestCode, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NoteFlowApp.BIRTHDAY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(requestCode, notification)
    }

    companion object {
        const val EXTRA_BIRTHDAY_ID = "extra_birthday_id"
        const val EXTRA_OFFSET_DAYS = "extra_offset_days"
    }
}
