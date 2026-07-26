package com.noteflow.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.noteflow.app.data.BirthdaysRepository
import com.noteflow.app.data.NotesRepository
import com.noteflow.app.data.SettingsRepository
import com.noteflow.app.reminder.BirthdayScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteFlowApp : Application() {

    lateinit var repository: NotesRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var birthdaysRepository: BirthdaysRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = NotesRepository(this)
        settingsRepository = SettingsRepository.getInstance(this)
        birthdaysRepository = BirthdaysRepository(this)
        createNotificationChannels()

        CoroutineScope(Dispatchers.IO).launch {
            // Purge trash older than 7 days on each cold start.
            repository.purgeExpiredTrash()
            // Re-arm birthday reminders in case they were cleared (mirrors note reminders in BootReceiver).
            BirthdayScheduler.rescheduleAll(this@NoteFlowApp, birthdaysRepository.getAllOnce())
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(REMINDER_CHANNEL_ID, getString(R.string.reminder_channel_name), NotificationManager.IMPORTANCE_HIGH)
            )
            manager.createNotificationChannel(
                NotificationChannel(BIRTHDAY_CHANNEL_ID, getString(R.string.birthday_channel_name), NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "reminders"
        const val BIRTHDAY_CHANNEL_ID = "birthdays"
    }
}
