package com.noteflow.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.noteflow.app.data.ThemeMode
import com.noteflow.app.ui.navigation.NoteFlowNavGraph
import com.noteflow.app.ui.theme.NoteFlowTheme

// AppCompatActivity (not plain ComponentActivity) is required here: it's the only
// Activity base class AppCompatDelegate.setApplicationLocales() knows how to
// automatically recreate when the in-app language switch is used — see
// ui/settings/SettingsScreen.kt for the other half of this fix.
class MainActivity : AppCompatActivity() {

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
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Status bar icons (clock, battery, signal, etc.): black in dark theme,
            // white in light theme, per explicit request. isAppearanceLightStatusBars
            // is named from the OS's point of view (true = a light bar background,
            // so it picks DARK icons for contrast) — hence it's set to darkTheme here.
            //
            // Also forces the Activity's night mode to match our in-app theme toggle
            // (not the phone's system setting) — this is what makes native dialogs
            // like the reminder/birthday date & time pickers pick up values-night/
            // themes.xml correctly, instead of following the phone's own light/dark
            // setting regardless of what's chosen in our Settings screen.
            val view = LocalView.current
            SideEffect {
                AppCompatDelegate.setDefaultNightMode(
                    if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = darkTheme
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
