package com.noteflow.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

/** How long a deleted note stays in the trash before being purged forever. */
private val TRASH_RETENTION_MS = TimeUnit.DAYS.toMillis(7)

class NotesRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val noteDao = db.noteDao()
    private val labelDao = db.labelDao()

    fun activeNotes(): Flow<List<Note>> = noteDao.observeActiveNotes()
    fun archivedNotes(): Flow<List<Note>> = noteDao.observeArchivedNotes()
    fun trashedNotes(): Flow<List<Note>> = noteDao.observeTrashedNotes()
    fun labels(): Flow<List<Label>> = labelDao.observeLabels()
    fun notesByLabel(label: String): Flow<List<Note>> = noteDao.observeNotesByLabel(label)
    fun search(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
    fun observeNote(id: Long): Flow<Note?> = noteDao.observeNote(id)

    /** One-shot full read of a note plus its checklist/images/labels — used by the editor screen. */
    suspend fun loadFullNote(noteId: Long): NoteWithExtras? {
        val note = noteDao.getNoteById(noteId) ?: return null
        return NoteWithExtras(
            note = note,
            checklist = noteDao.getChecklist(noteId),
            images = noteDao.getImages(noteId),
            labels = noteDao.getLabelsForNote(noteId)
        )
    }

    suspend fun saveNote(
        note: Note,
        checklist: List<ChecklistItem> = emptyList(),
        images: List<NoteImage> = emptyList(),
        labels: List<String> = emptyList()
    ): Long {
        val id = if (note.id == 0L) noteDao.insertNote(note) else {
            noteDao.updateNote(note); note.id
        }

        noteDao.clearChecklist(id)
        if (checklist.isNotEmpty()) noteDao.insertChecklistItems(checklist.map { it.copy(noteId = id) })

        noteDao.clearImages(id)
        if (images.isNotEmpty()) noteDao.insertImages(images.map { it.copy(noteId = id) })

        noteDao.clearNoteLabels(id)
        labels.forEach { labelDao.insertLabel(Label(it)) }
        if (labels.isNotEmpty()) {
            noteDao.insertNoteLabels(labels.map { NoteLabelCrossRef(id, it) })
        }
        return id
    }

    suspend fun moveToTrash(note: Note) =
        noteDao.updateNote(note.copy(inTrash = true, deletedAt = System.currentTimeMillis(), pinned = false))

    suspend fun restoreFromTrash(note: Note) =
        noteDao.updateNote(note.copy(inTrash = false, deletedAt = null))

    suspend fun deleteForever(note: Note) = noteDao.hardDeleteNote(note.id)

    /** Call periodically (e.g. on app start) to purge notes older than the retention window. */
    suspend fun purgeExpiredTrash() {
        val cutoff = System.currentTimeMillis() - TRASH_RETENTION_MS
        noteDao.getAllNotesOnce()
            .filter { it.inTrash && (it.deletedAt ?: 0L) < cutoff }
            .forEach { noteDao.hardDeleteNote(it.id) }
    }

    suspend fun setPinned(note: Note, pinned: Boolean) = noteDao.updateNote(note.copy(pinned = pinned))
    suspend fun setArchived(note: Note, archived: Boolean) = noteDao.updateNote(note.copy(archived = archived))
    suspend fun setColor(note: Note, color: String) = noteDao.updateNote(note.copy(color = color))
    suspend fun setReminder(note: Note, atMillis: Long?) = noteDao.updateNote(note.copy(reminderAt = atMillis))

    suspend fun renameLabel(oldName: String, newName: String) {
        labelDao.insertLabel(Label(newName))
        labelDao.renameLabelRefs(oldName, newName)
        labelDao.deleteLabel(oldName)
    }

    suspend fun deleteLabel(name: String) = labelDao.deleteLabel(name)

    /** Full snapshot used by BackupManager / ExportManager. */
    suspend fun allNotesForBackup(): List<NoteWithExtras> =
        noteDao.getAllNotesOnce().filter { !it.inTrash }.mapNotNull { loadFullNote(it.id) }
}
