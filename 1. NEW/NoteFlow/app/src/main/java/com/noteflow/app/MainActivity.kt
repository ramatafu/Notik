package com.noteflow.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import com.noteflow.app.data.ThemeMode
import com.noteflow.app.ui.navigation.NoteFlowNavGraph
import com.noteflow.app.ui.theme.NoteFlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        // Two sources of a deep link into a specific note:
        // 1) "noteflow://note/{id}" URI — used by reminder notifications
        // 2) a plain "noteId" long extra — used by the home-screen widget
        //    (Glance's actionStartActivity serializes ActionParameters as Intent extras,
        //    keyed by the parameter's name — see widget/NotesWidget.kt: NoteIdParamKey)
        val deepLinkNoteId = intent?.data?.lastPathSegment?.toLongOrNull()
            ?: intent?.getLongExtra("noteId", 0L)?.takeIf { it != 0L }

        setContent {
            val app = application as NoteFlowApp
            val themeMode by app.settingsRepository.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            NoteFlowTheme(darkTheme = darkTheme) {
                NoteFlowNavGraph(startNoteId = deepLinkNoteId?.takeIf { it != 0L })
            }
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
