package com.noteflow.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.noteflow.app.data.Note

object ReminderScheduler {

    private fun pendingIntent(context: Context, noteId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_NOTE_ID, noteId)
        }
        return PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedule(context: Context, note: Note) {
        val triggerAt = note.reminderAt ?: return cancel(context, note.id)
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent(context, note.id)
        )
    }

    fun cancel(context: Context, noteId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent(context, noteId))
    }

    /** Re-arms every stored reminder — called from BootReceiver after a device restart. */
    suspend fun rescheduleAll(context: Context, notes: List<Note>) {
        notes.filter { it.reminderAt != null && it.reminderAt!! > System.currentTimeMillis() }
            .forEach { schedule(context, it) }
    }
}
