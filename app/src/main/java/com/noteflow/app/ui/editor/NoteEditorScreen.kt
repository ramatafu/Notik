package com.noteflow.app.ui.editor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.noteflow.app.NoteFlowApp
import com.noteflow.app.data.ChecklistItem
import com.noteflow.app.data.NoteType
import com.noteflow.app.export.ExportFormat
import com.noteflow.app.export.ExportManager
import com.noteflow.app.data.NoteWithExtras
import com.noteflow.app.ui.ViewModelFactory
import com.noteflow.app.ui.theme.NoteColorPalette
import com.noteflow.app.ui.theme.colorForKey
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(noteId: Long, forceListType: Boolean = false, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: EditorViewModel = viewModel(factory = ViewModelFactory(context))
    val state by viewModel.state.collectAsState()
    val settingsRepository = remember { (context.applicationContext as NoteFlowApp).settingsRepository }
    val textColorOption by settingsRepository.textColor.collectAsState()
    val isDark = com.noteflow.app.ui.theme.LocalIsDarkTheme.current
    var showColorPicker by remember { mutableStateOf(false) }
    var showLabelEditor by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var fullscreenImageUri by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(noteId) { viewModel.load(noteId, forceListType) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.addImage(it.toString()) }
    }

    BackHandlerSave(onSave = { viewModel.save { onBack() } })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.type == NoteType.LIST) "Список" else "Заметка") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.save { onBack() } }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { imagePicker.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = "Добавить изображение")
                    }
                    if (!isDark) {
                        IconButton(onClick = { showColorPicker = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "Цвет")
                        }
                    }
                    IconButton(onClick = { showLabelEditor = true }) {
                        Icon(Icons.Default.Label, contentDescription = "Метки")
                    }
                    IconButton(onClick = { showReminderPicker(context) { millis -> viewModel.updateReminder(millis) } }) {
                        Icon(Icons.Default.Alarm, contentDescription = "Напоминание", tint = if (state.reminderAt != null) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Экспорт")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .background(if (isDark) MaterialTheme.colorScheme.background else colorForKey(state.color))
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                placeholder = { Text("Заголовок") },
                textStyle = MaterialTheme.typography.titleLarge.copy(color = textColorOption.color),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    focusedTextColor = textColorOption.color,
                    unfocusedTextColor = textColorOption.color
                )
            )

            FormattingToolbar(
                onBold = { viewModel.updateBody(toggleMarkup(state.body, "**")) },
                onItalic = { viewModel.updateBody(toggleMarkup(state.body, "*")) },
                onMono = { viewModel.updateBody(toggleMarkup(state.body, "`")) },
                onStrike = { viewModel.updateBody(toggleMarkup(state.body, "~~")) },
                onSwitchType = { viewModel.switchType(if (state.type == NoteType.NOTE) NoteType.LIST else NoteType.NOTE) },
                isList = state.type == NoteType.LIST
            )

            if (state.images.isNotEmpty()) {
                LazyRow(Modifier.padding(vertical = 8.dp)) {
                    items(state.images, key = { it.uri }) { image ->
                        Box(Modifier.padding(end = 8.dp)) {
                            AsyncImage(
                                model = image.uri,
                                contentDescription = "Открыть изображение на весь экран",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { fullscreenImageUri = image.uri }
                            )
                            IconButton(onClick = { viewModel.removeImage(image.uri) }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Убрать изображение", tint = Color.White)
                            }
                        }
                    }
                }
            }

            if (state.type == NoteType.LIST) {
                ChecklistEditor(
                    items = state.checklist,
                    onChange = viewModel::updateChecklist,
                    textColor = textColorOption.color
                )
            } else {
                OutlinedTextField(
                    value = state.body,
                    onValueChange = viewModel::updateBody,
                    placeholder = { Text("Заметка…") },
                    textStyle = LocalTextStyle.current.copy(color = textColorOption.color),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        focusedTextColor = textColorOption.color,
                        unfocusedTextColor = textColorOption.color
                    )
                )
            }
        }
    }

    if (showColorPicker) {
        ColorPickerSheet(current = state.color, onPick = { viewModel.updateColor(it); showColorPicker = false }, onDismiss = { showColorPicker = false })
    }
    if (showLabelEditor) {
        LabelPickerSheet(selected = state.labels, onChange = viewModel::updateLabels, onDismiss = { showLabelEditor = false })
    }
    if (showExportMenu) {
        ExportMenuSheet(
            onPick = { format ->
                showExportMenu = false
                scope.launch {
                    val full = NoteWithExtras(
                        note = com.noteflow.app.data.Note(
                            id = state.id, type = state.type, title = state.title, body = state.body,
                            color = state.color, pinned = state.pinned, reminderAt = state.reminderAt
                        ),
                        checklist = state.checklist, images = state.images, labels = state.labels
                    )
                    val uri = ExportManager(context).export(full, format)
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "*/*"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Экспортировать заметку"))
                }
            },
            onDismiss = { showExportMenu = false }
        )
    }

    fullscreenImageUri?.let { uri ->
        FullscreenImageViewer(uri = uri, onDismiss = { fullscreenImageUri = null })
    }
}

@Composable
private fun FullscreenImageViewer(uri: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = uri,
                contentDescription = "Изображение на весь экран",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
            }
        }
    }
}

@Composable
private fun BackHandlerSave(onSave: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onSave)
}

@Composable
private fun FormattingToolbar(
    onBold: () -> Unit, onItalic: () -> Unit, onMono: () -> Unit, onStrike: () -> Unit,
    onSwitchType: () -> Unit, isList: Boolean
) {
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
        IconButton(onClick = onBold) { Icon(Icons.Default.FormatBold, contentDescription = "Жирный") }
        IconButton(onClick = onItalic) { Icon(Icons.Default.FormatItalic, contentDescription = "Курсив") }
        IconButton(onClick = onMono) { Icon(Icons.Default.Code, contentDescription = "Моноширинный") }
        IconButton(onClick = onStrike) { Icon(Icons.Default.FormatStrikethrough, contentDescription = "Зачёркнутый") }
        IconButton(onClick = onSwitchType) {
            Icon(if (isList) Icons.Default.Notes else Icons.Default.Checklist, contentDescription = "Переключить тип")
        }
    }
}

@Composable
private fun ChecklistEditor(items: List<ChecklistItem>, onChange: (List<ChecklistItem>) -> Unit, textColor: Color) {
    Column {
        items.forEachIndexed { index, item ->
            val alpha by animateFloatAsState(targetValue = if (item.checked) 0.5f else 1f, label = "checklistItemAlpha")
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier
                    .animateContentSize()
                    .alpha(alpha)
            ) {
                Checkbox(checked = item.checked, onCheckedChange = { checked ->
                    onChange(items.toMutableList().also { it[index] = item.copy(checked = checked) })
                })
                OutlinedTextField(
                    value = item.text,
                    onValueChange = { text -> onChange(items.toMutableList().also { it[index] = item.copy(text = text) }) },
                    textStyle = LocalTextStyle.current.copy(
                        color = textColor,
                        textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )
                IconButton(onClick = { onChange(items.toMutableList().also { it.removeAt(index) }) }) {
                    Icon(Icons.Default.Close, contentDescription = "Удалить строку")
                }
            }
        }
        TextButton(onClick = { onChange(items + ChecklistItem(noteId = 0, position = items.size)) }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Добавить пункт")
        }
    }
}

@Composable
private fun ColorPickerSheet(current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(Modifier.padding(16.dp).horizontalScroll(rememberScrollState())) {
            NoteColorPalette.forEach { (key, color) ->
                Box(
                    Modifier
                        .padding(end = 12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(if (key == current) Modifier.background(color) else Modifier)
                        .clickable { onPick(key) }
                )
            }
        }
    }
}

@Composable
private fun LabelPickerSheet(selected: List<String>, onChange: (List<String>) -> Unit, onDismiss: () -> Unit) {
    var newLabel by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text("Метки этой заметки", style = MaterialTheme.typography.titleMedium)
            selected.forEach { label ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(label, modifier = Modifier.weight(1f).padding(vertical = 8.dp))
                    IconButton(onClick = { onChange(selected - label) }) { Icon(Icons.Default.Close, contentDescription = null) }
                }
            }
            Row {
                OutlinedTextField(value = newLabel, onValueChange = { newLabel = it }, placeholder = { Text("Новая метка") }, modifier = Modifier.weight(1f))
                IconButton(onClick = { if (newLabel.isNotBlank()) { onChange(selected + newLabel); newLabel = "" } }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun ExportMenuSheet(onPick: (ExportFormat) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            listOf(
                ExportFormat.TXT to "Текст (.txt)",
                ExportFormat.JSON to "JSON (.json)",
                ExportFormat.HTML to "HTML (.html)",
                ExportFormat.PDF to "PDF (.pdf)"
            ).forEach { (format, label) ->
                Text(label, modifier = Modifier.fillMaxWidth().clickable { onPick(format) }.padding(vertical = 12.dp))
            }
        }
    }
}

private fun showReminderPicker(context: android.content.Context, onSet: (Long?) -> Unit) {
    val cal = Calendar.getInstance()
    DatePickerDialog(context, { _, year, month, day ->
        TimePickerDialog(context, { _, hour, minute ->
            cal.set(year, month, day, hour, minute, 0)
            onSet(cal.timeInMillis)
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
}
