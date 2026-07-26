package com.noteflow.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.noteflow.app.BuildConfig
import com.noteflow.app.NoteFlowApp
import com.noteflow.app.R
import com.noteflow.app.backup.BackupManager
import com.noteflow.app.data.AppLanguage
import com.noteflow.app.data.NoteTextColor
import com.noteflow.app.data.ThemeMode
import com.noteflow.app.ui.theme.DefaultNoteBackgroundOptions
import com.noteflow.app.ui.theme.NoteColorPalette
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val backupManager = remember { BackupManager(context) }
    val settingsRepository = remember { (context.applicationContext as NoteFlowApp).settingsRepository }
    val scope = rememberCoroutineScope()

    val themeMode by settingsRepository.themeMode.collectAsState()
    val textColor by settingsRepository.textColor.collectAsState()
    val defaultNoteColor by settingsRepository.defaultNoteColor.collectAsState()
    val language by settingsRepository.language.collectAsState()

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { scope.launch { backupManager.restoreBackup(it) } }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_title)) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
        )
    }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SectionTitle(stringResource(R.string.settings_language))
            SingleChoiceSegmentedButtonRow(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                AppLanguage.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = language == option,
                        onClick = {
                            settingsRepository.setLanguage(option)
                            (context as? android.app.Activity)?.recreate()
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, AppLanguage.entries.size)
                    ) { Text(option.nativeLabel) }
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionTitle(stringResource(R.string.settings_theme))
            SingleChoiceSegmentedButtonRow(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                SegmentedButton(
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { settingsRepository.setThemeMode(ThemeMode.LIGHT) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text(stringResource(R.string.settings_theme_light)) }
                SegmentedButton(
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { settingsRepository.setThemeMode(ThemeMode.DARK) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text(stringResource(R.string.settings_theme_dark)) }
            }

            Spacer(Modifier.height(24.dp))

            SectionTitle(stringResource(R.string.settings_text_color))
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                NoteTextColor.entries.forEach { option ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ColorSwatch(
                            color = option.color,
                            selected = option == textColor,
                            onClick = { settingsRepository.setTextColor(option) },
                            showBorder = option == NoteTextColor.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(option.labelRes), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionTitle(stringResource(R.string.settings_default_bg))
            Text(
                stringResource(R.string.settings_default_bg_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                DefaultNoteBackgroundOptions.forEach { key ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ColorSwatch(
                            color = NoteColorPalette.getValue(key),
                            selected = key == defaultNoteColor,
                            onClick = { settingsRepository.setDefaultNoteColor(key) },
                            showBorder = true
                        )
                        Spacer(Modifier.height(4.dp))
                        val label = when (key) {
                            "White" -> stringResource(R.string.color_bg_white)
                            "Black" -> stringResource(R.string.color_bg_black)
                            else -> key
                        }
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionTitle(stringResource(R.string.settings_backup_section))
            SettingsRow(stringResource(R.string.settings_backup_create), stringResource(R.string.settings_backup_create_sub)) {
                scope.launch {
                    val uri = backupManager.createBackup()
                    val shareTitle = context.getString(R.string.settings_backup_share_title)
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "application/zip"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, shareTitle))
                }
            }
            SettingsRow(stringResource(R.string.settings_backup_restore), stringResource(R.string.settings_backup_restore_sub)) {
                restoreLauncher.launch("application/zip")
            }

            Spacer(Modifier.height(24.dp))

            SectionTitle(stringResource(R.string.settings_about))
            Text(
                stringResource(R.string.settings_about_body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.settings_about_dev),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Text(
                stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit, showBorder: Boolean = false) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (showBorder) Modifier.border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White
            )
        }
    }
}

private fun Color.luminance(): Float = (0.299f * red + 0.587f * green + 0.114f * blue)

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }
}
