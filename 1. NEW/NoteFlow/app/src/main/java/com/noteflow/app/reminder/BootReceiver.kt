package com.noteflow.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.noteflow.app.NoteFlowApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val app = context.applicationContext as NoteFlowApp

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notes = app.repository.allNotesForBackup().map { it.note }
                ReminderScheduler.rescheduleAll(context, notes)

                val birthdays = app.birthdaysRepository.getAllOnce()
                BirthdayScheduler.rescheduleAll(context, birthdays)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
