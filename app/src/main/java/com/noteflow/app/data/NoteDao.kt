package com.noteflow.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/** A note plus everything that belongs to it, assembled for the UI/export layers. */
data class NoteWithExtras(
    @Embedded val note: Note,
    val checklist: List<ChecklistItem>,
    val images: List<NoteImage>,
    val labels: List<String>
)

@Dao
interface NoteDao {

    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun hardDeleteNote(noteId: Long)

    @Query("DELETE FROM notes WHERE inTrash = 1")
    suspend fun deleteAllTrashed()

    @Query("SELECT * FROM notes WHERE inTrash = 0 AND archived = 0 ORDER BY pinned DESC, modifiedAt DESC")
    fun observeActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE archived = 1 AND inTrash = 0 ORDER BY modifiedAt DESC")
    fun observeArchivedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE inTrash = 1 ORDER BY deletedAt DESC")
    fun observeTrashedNotes(): Flow<List<Note>>

    @Query("SELECT COUNT(*) FROM notes WHERE inTrash = 0 AND archived = 0")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE archived = 1 AND inTrash = 0")
    fun observeArchivedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE inTrash = 1")
    fun observeTrashedCount(): Flow<Int>

    @Query(
        """SELECT * FROM notes WHERE inTrash = 0 AND
           (title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%')
           ORDER BY pinned DESC, modifiedAt DESC"""
    )
    fun searchNotes(query: String): Flow<List<Note>>

    @Query(
        """SELECT notes.* FROM notes
           INNER JOIN note_label_cross_ref ON notes.id = note_label_cross_ref.noteId
           WHERE note_label_cross_ref.labelName = :label AND inTrash = 0
           ORDER BY pinned DESC, modifiedAt DESC"""
    )
    fun observeNotesByLabel(label: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observeNote(noteId: Long): Flow<Note?>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): Note?

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesOnce(): List<Note>

    @Query("SELECT * FROM notes WHERE reminderAt IS NOT NULL AND inTrash = 0")
    suspend fun notesWithReminders(): List<Note>

    // --- checklist items ---
    @Query("DELETE FROM checklist_items WHERE noteId = :noteId")
    suspend fun clearChecklist(noteId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItem>)

    @Query("SELECT * FROM checklist_items WHERE noteId = :noteId ORDER BY position")
    suspend fun getChecklist(noteId: Long): List<ChecklistItem>

    // --- images ---
    @Query("DELETE FROM note_images WHERE noteId = :noteId")
    suspend fun clearImages(noteId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<NoteImage>)

    @Query("SELECT * FROM note_images WHERE noteId = :noteId ORDER BY position")
    suspend fun getImages(noteId: Long): List<NoteImage>

    // --- labels on a note ---
    @Query("DELETE FROM note_label_cross_ref WHERE noteId = :noteId")
    suspend fun clearNoteLabels(noteId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteLabels(refs: List<NoteLabelCrossRef>)

    @Query("SELECT labelName FROM note_label_cross_ref WHERE noteId = :noteId")
    suspend fun getLabelsForNote(noteId: Long): List<String>
}

@Dao
interface LabelDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabel(label: Label)

    @Query("DELETE FROM labels WHERE name = :name")
    suspend fun deleteLabel(name: String)

    @Query("UPDATE note_label_cross_ref SET labelName = :newName WHERE labelName = :oldName")
    suspend fun renameLabelRefs(oldName: String, newName: String)

    @Query("SELECT * FROM labels ORDER BY name")
    fun observeLabels(): Flow<List<Label>>
}
