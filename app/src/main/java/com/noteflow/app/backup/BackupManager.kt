package com.noteflow.app.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.noteflow.app.data.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Full-database backup: every note (with checklist items, image references and
 * labels) serialized as JSON, zipped for a single shareable file.
 */
class BackupManager(private val context: Context) {

    private val repository = NotesRepository(context)

    suspend fun createBackup(): Uri {
        val notes = repository.allNotesForBackup()
        val root = JSONArray()
        notes.forEach { extras ->
            root.put(JSONObject().apply {
                put("id", extras.note.id)
                put("type", extras.note.type.name)
                put("title", extras.note.title)
                put("body", extras.note.body)
                put("color", extras.note.color)
                put("pinned", extras.note.pinned)
                put("archived", extras.note.archived)
                put("createdAt", extras.note.createdAt)
                put("modifiedAt", extras.note.modifiedAt)
                put("reminderAt", extras.note.reminderAt ?: JSONObject.NULL)
                put("labels", JSONArray(extras.labels))
                put("checklist", JSONArray(extras.checklist.map {
                    JSONObject().apply { put("position", it.position); put("text", it.text); put("checked", it.checked) }
                }))
                put("images", JSONArray(extras.images.map { it.uri }))
            })
        }

        val dir = File(context.cacheDir, "backups").apply { mkdirs() }
        val zipFile = File(dir, "noteflow_backup_${System.currentTimeMillis()}.zip")
        ZipOutputStream(zipFile.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("notes.json"))
            zip.write(root.toString(2).toByteArray())
            zip.closeEntry()
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zipFile)
    }

    /** Restores notes from a previously created backup zip. Existing notes are kept; backup notes are appended. */
    suspend fun restoreBackup(zipUri: Uri) {
        context.contentResolver.openInputStream(zipUri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "notes.json") {
                        val json = zip.readBytes().toString(Charsets.UTF_8)
                        importNotesJson(json)
                    }
                    entry = zip.nextEntry
                }
            }
        }
    }

    private suspend fun importNotesJson(json: String) {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val note = Note(
                type = NoteType.valueOf(obj.getString("type")),
                title = obj.getString("title"),
                body = obj.getString("body"),
                color = obj.getString("color"),
                pinned = obj.getBoolean("pinned"),
                archived = obj.optBoolean("archived", false),
                createdAt = obj.getLong("createdAt"),
                modifiedAt = obj.getLong("modifiedAt"),
                reminderAt = if (obj.isNull("reminderAt")) null else obj.getLong("reminderAt")
            )
            val labels = obj.getJSONArray("labels").let { arr -> (0 until arr.length()).map { arr.getString(it) } }
            val checklist = obj.getJSONArray("checklist").let { arr ->
                (0 until arr.length()).map { idx ->
                    val c = arr.getJSONObject(idx)
                    ChecklistItem(noteId = 0, position = c.getInt("position"), text = c.getString("text"), checked = c.getBoolean("checked"))
                }
            }
            repository.saveNote(note, checklist = checklist, labels = labels)
        }
    }
}
