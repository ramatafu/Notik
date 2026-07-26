package com.noteflow.app.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.noteflow.app.MainActivity
import com.noteflow.app.NoteFlowApp
import com.noteflow.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
        if (noteId == -1L) return

        val pendingResult = goAsync()
        val app = context.applicationContext as NoteFlowApp

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val note = app.repository.loadFullNote(noteId)?.note
                if (note != null && !note.inTrash) {
                    showNotification(context, noteId, note.title.ifBlank { context.getString(R.string.untitled_note) }, note.body)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, noteId: Long, title: String, body: String) {
        val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse("noteflow://note/$noteId"), context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context, noteId.toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NoteFlowApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body.take(120))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(noteId.toInt(), notification)
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }
}
