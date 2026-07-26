package com.noteflow.app.ui.editor

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteflow.app.data.*
import com.noteflow.app.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditorState(
    val id: Long = 0,
    val type: NoteType = NoteType.NOTE,
    val title: String = "",
    val body: String = "",
    val color: String = "Default",
    val pinned: Boolean = false,
    val checklist: List<ChecklistItem> = emptyList(),
    val images: List<NoteImage> = emptyList(),
    val labels: List<String> = emptyList(),
    val reminderAt: Long? = null,
    val loaded: Boolean = false
)

class EditorViewModel(
    private val repository: NotesRepository,
    private val appContext: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    fun load(noteId: Long, forceListType: Boolean = false) {
        if (noteId == 0L) {
            // Brand-new note: use the user's configured default background instead of pure white.
            _state.value = if (forceListType) {
                EditorState(
                    type = NoteType.LIST,
                    color = settingsRepository.defaultNoteColor.value,
                    checklist = listOf(ChecklistItem(noteId = 0, position = 0)),
                    loaded = true
                )
            } else {
                EditorState(color = settingsRepository.defaultNoteColor.value, loaded = true)
            }
            return
        }
        viewModelScope.launch {
            val full = repository.loadFullNote(noteId)
            if (full != null) {
                _state.value = EditorState(
                    id = full.note.id,
                    type = full.note.type,
                    title = full.note.title,
                    body = full.note.body,
                    color = full.note.color,
                    pinned = full.note.pinned,
                    checklist = full.checklist,
                    images = full.images,
                    labels = full.labels,
                    reminderAt = full.note.reminderAt,
                    loaded = true
                )
            } else {
                _state.value = EditorState(loaded = true)
            }
        }
    }

    fun updateTitle(title: String) { _state.value = _state.value.copy(title = title) }
    fun updateBody(body: String) { _state.value = _state.value.copy(body = body) }
    fun updateColor(color: String) { _state.value = _state.value.copy(color = color) }
    fun updateLabels(labels: List<String>) { _state.value = _state.value.copy(labels = labels) }
    fun updateChecklist(items: List<ChecklistItem>) { _state.value = _state.value.copy(checklist = items) }
    fun updateReminder(atMillis: Long?) { _state.value = _state.value.copy(reminderAt = atMillis) }
    fun addImage(uri: String) {
        val next = _state.value.images.toMutableList()
        next.add(NoteImage(noteId = _state.value.id, position = next.size, uri = uri))
        _state.value = _state.value.copy(images = next)
    }
    fun removeImage(uri: String) {
        _state.value = _state.value.copy(images = _state.value.images.filterNot { it.uri == uri })
    }
    fun switchType(type: NoteType) { _state.value = _state.value.copy(type = type) }

    /** Persists the note and (re)schedules or cancels its reminder as needed. Returns the saved note id. */
    fun save(onSaved: (Long) -> Unit = {}) = viewModelScope.launch {
        val s = _state.value
        if (s.title.isBlank() && s.body.isBlank() && s.checklist.isEmpty()) {
            onSaved(s.id) // nothing to persist, but the caller (e.g. the back button) must still proceed
            return@launch
        }

        val note = Note(
            id = s.id,
            type = s.type,
            title = s.title,
            body = s.body,
            color = s.color,
            pinned = s.pinned,
            reminderAt = s.reminderAt,
            modifiedAt = System.currentTimeMillis()
        )
        val id = repository.saveNote(note, s.checklist, s.images, s.labels)
        val savedNote = note.copy(id = id)
        if (s.reminderAt != null) ReminderScheduler.schedule(appContext, savedNote) else ReminderScheduler.cancel(appContext, id)
        onSaved(id)
    }
}
