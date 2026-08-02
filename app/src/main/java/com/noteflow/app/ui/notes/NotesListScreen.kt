package com.noteflow.app.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noteflow.app.NoteFlowApp
import com.noteflow.app.R
import com.noteflow.app.data.Note
import com.noteflow.app.ui.ViewModelFactory
import com.noteflow.app.ui.birthdays.BirthdaysScreen
import com.noteflow.app.ui.editor.parseMarkupToAnnotatedString
import com.noteflow.app.ui.theme.colorForKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onOpenNote: (Long, Boolean) -> Unit,
    onOpenLabels: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBooks: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(context))
    val notes by viewModel.notes.collectAsState()
    val tab by viewModel.tab.collectAsState()
    val query by viewModel.query.collectAsState()
    val labels by viewModel.labels.collectAsState()
    val labelFilter by viewModel.labelFilter.collectAsState()
    val activeCount by viewModel.activeCount.collectAsState()
    val archivedCount by viewModel.archivedCount.collectAsState()
    val trashedCount by viewModel.trashedCount.collectAsState()
    val settingsRepository = remember { (context.applicationContext as NoteFlowApp).settingsRepository }
    val booksEnabled by settingsRepository.booksEnabled.collectAsState()
    var drawerOpen by remember { mutableStateOf(false) }
    var showClearTrashDialog by remember { mutableStateOf(false) }

    // This route's own Lifecycle goes through ON_RESUME every time it becomes the
    // active destination again (e.g. popping back from the editor) — even though the
    // NotesViewModel instance itself survives the whole time. Forcing a refresh here
    // guarantees the grid always reflects the latest saved note, closing any possible
    // window where a just-edited note could still show stale content.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val activeLabel = stringResource(R.string.notes_tab_active)
    val archivedLabel = stringResource(R.string.notes_tab_archived)
    val trashLabel = stringResource(R.string.notes_tab_trash)
    val birthdaysLabel = stringResource(R.string.notes_tab_birthdays)

    val currentSectionTitle = when (tab) {
        NotesTab.ACTIVE -> stringResource(R.string.tab_with_count, activeLabel, activeCount)
        NotesTab.ARCHIVED -> stringResource(R.string.tab_with_count, archivedLabel, archivedCount)
        NotesTab.TRASH -> stringResource(R.string.tab_with_count, trashLabel, trashedCount)
        NotesTab.BIRTHDAYS -> birthdaysLabel
    }

    Scaffold(
        floatingActionButton = {
            if (tab == NotesTab.ACTIVE) {
                Column(horizontalAlignment = Alignment.End) {
                    ExtendedFloatingActionButton(
                        onClick = { onOpenNote(0, true) },
                        icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                        text = { Text(stringResource(R.string.fab_new_list)) }
                    )
                    Spacer(Modifier.height(12.dp))
                    ExtendedFloatingActionButton(
                        onClick = { onOpenNote(0, false) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text(stringResource(R.string.fab_new_note)) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {

            // Top row: hamburger menu (now also holds the section switcher) + current
            // section name on the left, app name on the right.
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { drawerOpen = true }) {
                    Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.notes_menu_cd))
                }
                Text(
                    currentSectionTitle,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            if (tab != NotesTab.BIRTHDAYS) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = viewModel::setQuery,
                        placeholder = { Text(stringResource(R.string.notes_search_placeholder)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = if (tab == NotesTab.TRASH) 8.dp else 16.dp)
                    )
                    if (tab == NotesTab.TRASH && notes.isNotEmpty()) {
                        TextButton(onClick = { showClearTrashDialog = true }, modifier = Modifier.padding(end = 8.dp)) {
                            Text(stringResource(R.string.clear_trash), color = com.noteflow.app.ui.theme.AccentRed)
                        }
                    }
                }
            }

            if (tab == NotesTab.BIRTHDAYS) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    BirthdaysScreen()
                }
            } else {
                if (labelFilter != null) {
                    AssistChip(
                        onClick = { viewModel.filterByLabel(null) },
                        label = { Text("${stringResource(R.string.menu_labels_title)}: $labelFilter  ✕") },
                        modifier = Modifier.padding(8.dp)
                    )
                }

                if (notes.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.notes_empty), color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onOpenNote(note.id, note.type == com.noteflow.app.data.NoteType.LIST) },
                                onTogglePin = { viewModel.togglePin(note) },
                                onArchive = { if (tab == NotesTab.ARCHIVED) viewModel.unarchive(note) else viewModel.archive(note) },
                                onTrash = { if (tab == NotesTab.TRASH) viewModel.restore(note) else viewModel.moveToTrash(note) },
                                onDeleteForever = { viewModel.deleteForever(note) },
                                inTrash = tab == NotesTab.TRASH
                            )
                        }
                    }
                }
            }
        }
    }

    if (drawerOpen) {
        ModalBottomSheet(onDismissRequest = { drawerOpen = false }) {
            Column(Modifier.padding(16.dp).padding(bottom = 64.dp)) {

                // Group 1: section switcher — used to be tabs on the main screen, now lives here.
                listOf(
                    Triple(NotesTab.ACTIVE, activeLabel, activeCount),
                    Triple(NotesTab.ARCHIVED, archivedLabel, archivedCount),
                    Triple(NotesTab.TRASH, trashLabel, trashedCount)
                ).forEach { (sectionTab, label, count) ->
                    MenuTabRow(
                        label = stringResource(R.string.tab_with_count, label, count),
                        selected = tab == sectionTab
                    ) {
                        viewModel.selectTab(sectionTab)
                        drawerOpen = false
                    }
                }

                Divider(Modifier.padding(vertical = 8.dp))

                // Group 2: birthdays, on its own.
                MenuTabRow(label = birthdaysLabel, selected = tab == NotesTab.BIRTHDAYS) {
                    viewModel.selectTab(NotesTab.BIRTHDAYS)
                    drawerOpen = false
                }

                Divider(Modifier.padding(vertical = 8.dp))

                // Group 3: labels.
                Text(stringResource(R.string.menu_labels_title), style = MaterialTheme.typography.titleMedium)
                labels.forEach { label ->
                    Text(
                        label.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.filterByLabel(label.name)
                                viewModel.selectTab(NotesTab.ACTIVE)
                                drawerOpen = false
                            }
                            .padding(vertical = 12.dp)
                    )
                }
                Text(
                    stringResource(R.string.menu_manage_labels),
                    modifier = Modifier.fillMaxWidth().clickable { drawerOpen = false; onOpenLabels() }.padding(vertical = 12.dp)
                )

                if (booksEnabled) {
                    Divider(Modifier.padding(vertical = 8.dp))

                    // Group 3b: experimental "Books" feature, only shown when enabled in Settings.
                    Text(
                        stringResource(R.string.label_books),
                        modifier = Modifier.fillMaxWidth().clickable { drawerOpen = false; onOpenBooks() }.padding(vertical = 12.dp)
                    )
                }

                Divider(Modifier.padding(vertical = 8.dp))

                // Group 4: settings.
                Text(
                    stringResource(R.string.menu_settings),
                    modifier = Modifier.fillMaxWidth().clickable { drawerOpen = false; onOpenSettings() }.padding(vertical = 12.dp)
                )
            }
        }
    }

    if (showClearTrashDialog) {
        AlertDialog(
            onDismissRequest = { showClearTrashDialog = false },
            title = { Text(stringResource(R.string.clear_trash_confirm_title)) },
            text = { Text(stringResource(R.string.clear_trash_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.emptyTrash()
                    showClearTrashDialog = false
                }) { Text(stringResource(R.string.clear_trash_confirm_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearTrashDialog = false }) { Text(stringResource(R.string.dialog_cancel)) }
            }
        )
    }
}

@Composable
private fun MenuTabRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) com.noteflow.app.ui.theme.AccentRed else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onArchive: () -> Unit,
    onTrash: () -> Unit,
    onDeleteForever: () -> Unit,
    inTrash: Boolean
) {
    val isDark = com.noteflow.app.ui.theme.LocalIsDarkTheme.current
    val cardBackground = if (isDark) MaterialTheme.colorScheme.surface else colorForKey(note.color)
    val cardModifier = Modifier
        .clip(RoundedCornerShape(16.dp))
        .background(cardBackground)
        .border(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color.Black,
            RoundedCornerShape(16.dp)
        )
        .clickable(onClick = onClick)
        .padding(12.dp)

    Column(modifier = cardModifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                note.title.ifBlank { "Без названия" },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (note.passwordHash != null) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Защищена паролем",
                    modifier = Modifier.size(16.dp).padding(end = if (note.pinned) 4.dp else 0.dp)
                )
            }
            if (note.pinned) Icon(Icons.Default.PushPin, contentDescription = "Закреплено", modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(2.dp))
        Text(
            relativeTimeRu(note.modifiedAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(6.dp))
        if (note.passwordHash != null) {
            Text(
                "Содержимое скрыто",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        } else {
            Text(
                parseMarkupToAnnotatedString(note.body),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (!inTrash) {
                IconButton(onClick = onTogglePin, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.PushPin, contentDescription = null, tint = if (note.pinned) com.noteflow.app.ui.theme.AccentRed else Color.Gray)
                }
                IconButton(onClick = onArchive, enabled = !note.pinned, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.Archive,
                        contentDescription = "Архивировать",
                        tint = if (note.pinned) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else LocalContentColor.current
                    )
                }
                IconButton(onClick = onTrash, enabled = !note.pinned, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = if (note.pinned) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else LocalContentColor.current
                    )
                }
            } else {
                IconButton(onClick = onTrash, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Restore, contentDescription = "Восстановить")
                }
                IconButton(onClick = onDeleteForever, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Удалить навсегда")
                }
            }
        }
    }
}
