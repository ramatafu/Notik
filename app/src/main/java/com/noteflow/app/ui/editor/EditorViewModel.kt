package com.noteflow.app.ui.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteflow.app.data.*
import com.noteflow.app.reminder.ReminderScheduler
import com.noteflow.app.security.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class EditorState(
    val id: Long = 0,
    val type: NoteType = NoteType.NOTE,
    val title: String = "",
    val body: String = "",
    val color: String = "Default",
    val pinned: Boolean = false,
    // Carried through from the loaded note and re-included on every save — otherwise
    // saving (which happens on every simple back-navigation) would silently reset a
    // note back to "active", pulling it out of the archive or trash.
    val archived: Boolean = false,
    val inTrash: Boolean = false,
    val deletedAt: Long? = null,
    val checklist: List<ChecklistItem> = emptyList(),
    val images: List<NoteImage> = emptyList(),
    val labels: List<String> = emptyList(),
    val reminderAt: Long? = null,
    val passwordHash: String? = null,
    val passwordSalt: String? = null,
    // Session-only: true once the correct password has been entered this time
    // around. Always true for notes with no password. Resets every time you
    // navigate away and back into a locked note.
    val unlocked: Boolean = true,
    val loaded: Boolean = false
) {
    val isLocked: Boolean get() = passwordHash != null
}

class EditorViewModel(
    private val repository: NotesRepository,
    private val appContext: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    fun load(noteId: Long, forceListType: Boolean = false) {
        if (noteId == 0L) {
            _state.value = if (forceListType) {
                EditorState(
                    type = NoteType.LIST,
                    checklist = listOf(ChecklistItem(noteId = 0, position = 0)),
                    loaded = true
                )
            } else {
                EditorState(loaded = true)
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
                    archived = full.note.archived,
                    inTrash = full.note.inTrash,
                    deletedAt = full.note.deletedAt,
                    checklist = full.checklist,
                    images = full.images,
                    labels = full.labels,
                    reminderAt = full.note.reminderAt,
                    passwordHash = full.note.passwordHash,
                    passwordSalt = full.note.passwordSalt,
                    unlocked = full.note.passwordHash == null,
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

    /**
     * Copies the picked image into the app's own private storage before storing a
     * reference to it. The URI handed to us by the system picker is often only
     * readable for a short-lived grant — after the process dies (e.g. the user
     * leaves the app), that grant can be revoked and the image would fail to load.
     * Copying it locally makes the attachment permanent regardless of that.
     */
    fun addImage(sourceUri: Uri) = viewModelScope.launch {
        val localUri = withContext(Dispatchers.IO) { copyImageToLocalStorage(sourceUri) } ?: return@launch
        val next = _state.value.images.toMutableList()
        next.add(NoteImage(noteId = _state.value.id, position = next.size, uri = localUri))
        _state.value = _state.value.copy(images = next)
    }

    fun removeImage(uri: String) {
        _state.value = _state.value.copy(images = _state.value.images.filterNot { it.uri == uri })
        // Best-effort cleanup of the local copy; harmless if it's already gone.
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { Uri.parse(uri).path?.let { File(it).delete() } }
        }
    }

    private fun copyImageToLocalStorage(sourceUri: Uri): String? = try {
        val imagesDir = File(appContext.filesDir, "note_images").apply { mkdirs() }
        val mimeExtension = appContext.contentResolver.getType(sourceUri)?.substringAfterLast('/')?.takeIf { it.length in 2..4 } ?: "jpg"
        val destFile = File(imagesDir, "${UUID.randomUUID()}.$mimeExtension")
        appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        }
        Uri.fromFile(destFile).toString()
    } catch (e: Exception) {
        null
    }

    fun switchType(type: NoteType) { _state.value = _state.value.copy(type = type) }

    /** Checks a candidate password against the stored hash. Unlocks the session state on success. */
    fun unlock(password: String): Boolean {
        val s = _state.value
        val salt = s.passwordSalt
        val hash = s.passwordHash
        val ok = hash != null && salt != null && PasswordUtils.verify(password, salt, hash)
        if (ok) _state.value = s.copy(unlocked = true)
        return ok
    }

    /** Sets (or replaces) this note's password and persists immediately — no length limit. */
    fun setPassword(password: String) = viewModelScope.launch {
        val salt = PasswordUtils.generateSalt()
        val hash = PasswordUtils.hash(password, salt)
        _state.value = _state.value.copy(passwordHash = hash, passwordSalt = salt, unlocked = true)
        persistNow()
    }

    /** Removes the password protection from this note and persists immediately. */
    fun removePassword() = viewModelScope.launch {
        _state.value = _state.value.copy(passwordHash = null, passwordSalt = null, unlocked = true)
        persistNow()
    }

    /** Persists the note and (re)schedules or cancels its reminder as needed. Returns the saved note id. */
    fun save(onSaved: (Long) -> Unit = {}) = viewModelScope.launch {
        val s = _state.value
        if (s.title.isBlank() && s.body.isBlank() && s.checklist.isEmpty()) {
            onSaved(s.id) // nothing to persist, but the caller (e.g. the back button) must still proceed
            return@launch
        }

        val id = persistNow()
        onSaved(id)
    }

    /** Builds a Note from the current state and writes it through the repository. Always includes
     *  the password fields and the archived/trash state, so a regular save() never accidentally
     *  wipes a note's lock or silently pulls it back out of the archive/trash. */
    private suspend fun persistNow(): Long {
        val s = _state.value
        val note = Note(
            id = s.id,
            type = s.type,
            title = s.title,
            body = s.body,
            color = s.color,
            pinned = s.pinned,
            archived = s.archived,
            inTrash = s.inTrash,
            deletedAt = s.deletedAt,
            reminderAt = s.reminderAt,
            modifiedAt = System.currentTimeMillis(),
            passwordHash = s.passwordHash,
            passwordSalt = s.passwordSalt
        )
        val id = repository.saveNote(note, s.checklist, s.images, s.labels)
        val savedNote = note.copy(id = id)
        _state.value = _state.value.copy(id = id)
        if (s.reminderAt != null) ReminderScheduler.schedule(appContext, savedNote) else ReminderScheduler.cancel(appContext, id)
        return id
    }
}
