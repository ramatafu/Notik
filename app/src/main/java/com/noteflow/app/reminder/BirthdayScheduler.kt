package com.noteflow.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.noteflow.app.data.Birthday
import java.util.Calendar

/**
 * Schedules the four reminders requested for each birthday: 7 days before,
 * 3 days before, 1 day before, and on the day itself, all at 10:00 local time.
 */
object BirthdayScheduler {

    private val OFFSET_DAYS = listOf(7, 3, 1, 0)

    private fun nextOccurrence(birthday: Birthday): Calendar {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.MONTH, birthday.month - 1)
            set(Calendar.DAY_OF_MONTH, birthday.day)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (next.before(now)) next.add(Calendar.YEAR, 1)
        return next
    }

    private fun pendingIntent(context: Context, birthdayId: Long, offsetDays: Int): PendingIntent {
        val intent = Intent(context, BirthdayReminderReceiver::class.java).apply {
            putExtra(BirthdayReminderReceiver.EXTRA_BIRTHDAY_ID, birthdayId)
            putExtra(BirthdayReminderReceiver.EXTRA_OFFSET_DAYS, offsetDays)
        }
        val requestCode = (birthdayId * 10 + offsetDays).toInt()
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedule(context: Context, birthday: Birthday) {
        cancel(context, birthday.id)
        val occurrence = nextOccurrence(birthday)
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        OFFSET_DAYS.forEach { offset ->
            val trigger = (occurrence.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -offset) }
            if (trigger.timeInMillis > System.currentTimeMillis()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger.timeInMillis,
                    pendingIntent(context, birthday.id, offset)
                )
            }
        }
    }

    fun cancel(context: Context, birthdayId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        OFFSET_DAYS.forEach { offset -> alarmManager.cancel(pendingIntent(context, birthdayId, offset)) }
    }

    /** Re-arms every stored birthday's reminders — called on app start, same pattern as ReminderScheduler. */
    fun rescheduleAll(context: Context, birthdays: List<Birthday>) {
        birthdays.forEach { schedule(context, it) }
    }
}
