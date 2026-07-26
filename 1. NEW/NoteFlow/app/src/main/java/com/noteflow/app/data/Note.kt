package com.noteflow.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteType { NOTE, LIST }

/**
 * Core note entity.
 *
 * [body] holds rich text using a lightweight inline markup so we don't need a
 * separate "spans" table:
 *   **bold**  *italic*  `monospace`  ~~strikethrough~~
 * The UI layer (see ui/editor/RichText.kt) parses this into an AnnotatedString
 * for display/editing and serializes it back on save.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: NoteType = NoteType.NOTE,
    val title: String = "",
    val body: String = "",
    val color: String = "Default",       // one of NoteColors palette keys
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val inTrash: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,         // used to auto-purge trash after 7 days
    val reminderAt: Long? = null         // epoch millis, null = no reminder
)

/** A single checkable line inside a LIST-type note. */
@Entity(tableName = "checklist_items", primaryKeys = ["noteId", "position"])
data class ChecklistItem(
    val noteId: Long,
    val position: Int,
    val text: String = "",
    val checked: Boolean = false
)

/** An image attached to a note (JPG/PNG/WEBP), stored by content URI. */
@Entity(tableName = "note_images", primaryKeys = ["noteId", "position"])
data class NoteImage(
    val noteId: Long,
    val position: Int,
    val uri: String
)

/** A user-defined label, e.g. "Work", "Recipes". */
@Entity(tableName = "labels")
data class Label(
    @PrimaryKey val name: String
)

/** Many-to-many join between notes and labels. */
@Entity(tableName = "note_label_cross_ref", primaryKeys = ["noteId", "labelName"])
data class NoteLabelCrossRef(
    val noteId: Long,
    val labelName: String
)
