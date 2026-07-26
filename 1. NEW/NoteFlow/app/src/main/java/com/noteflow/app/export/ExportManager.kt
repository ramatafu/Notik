package com.noteflow.app.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.noteflow.app.data.NoteWithExtras
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class ExportFormat { TXT, JSON, HTML, PDF }

/**
 * Renders a single note to the requested file format inside the app's cache
 * "exports" folder and returns a content:// Uri suitable for sharing.
 */
class ExportManager(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun export(note: NoteWithExtras, format: ExportFormat): android.net.Uri {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeTitle = note.note.title.ifBlank { "untitled" }.replace(Regex("[^A-Za-z0-9а-яА-Я _-]"), "_")
        val file = when (format) {
            ExportFormat.TXT -> File(dir, "$safeTitle.txt").also { writeTxt(it, note) }
            ExportFormat.JSON -> File(dir, "$safeTitle.json").also { writeJson(it, note) }
            ExportFormat.HTML -> File(dir, "$safeTitle.html").also { writeHtml(it, note) }
            ExportFormat.PDF -> File(dir, "$safeTitle.pdf").also { writePdf(it, note) }
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun bodyAsPlainText(body: String): String =
        body.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("`(.*?)`"), "$1")
            .replace(Regex("~~(.*?)~~"), "$1")

    private fun writeTxt(file: File, note: NoteWithExtras) {
        file.writeText(buildString {
            appendLine(note.note.title)
            appendLine()
            if (note.checklist.isNotEmpty()) {
                note.checklist.forEach { appendLine("${if (it.checked) "[x]" else "[ ]"} ${it.text}") }
            } else {
                appendLine(bodyAsPlainText(note.note.body))
            }
        })
    }

    private fun writeJson(file: File, note: NoteWithExtras) {
        val json = JSONObject().apply {
            put("title", note.note.title)
            put("body", note.note.body)
            put("type", note.note.type.name)
            put("color", note.note.color)
            put("pinned", note.note.pinned)
            put("createdAt", note.note.createdAt)
            put("modifiedAt", note.note.modifiedAt)
            put("labels", JSONArray(note.labels))
            put("checklist", JSONArray(note.checklist.map {
                JSONObject().apply { put("text", it.text); put("checked", it.checked) }
            }))
            put("images", JSONArray(note.images.map { it.uri }))
        }
        file.writeText(json.toString(2))
    }

    private fun writeHtml(file: File, note: NoteWithExtras) {
        val htmlBody = note.note.body
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
            .replace(Regex("\\*(.*?)\\*"), "<i>$1</i>")
            .replace(Regex("`(.*?)`"), "<code>$1</code>")
            .replace(Regex("~~(.*?)~~"), "<s>$1</s>")
            .replace("\n", "<br>")

        val checklistHtml = if (note.checklist.isNotEmpty()) {
            buildString {
                append("<ul style=\"list-style:none;padding-left:0\">")
                note.checklist.forEach {
                    append("<li>${if (it.checked) "☑" else "☐"} ${it.text}</li>")
                }
                append("</ul>")
            }
        } else ""

        file.writeText(
            """
            <!DOCTYPE html><html><head><meta charset="utf-8">
            <title>${note.note.title}</title></head><body>
            <h1>${note.note.title}</h1>
            <p style="color:#888">${dateFormat.format(Date(note.note.modifiedAt))}</p>
            <div>$htmlBody</div>
            $checklistHtml
            </body></html>
            """.trimIndent()
        )
    }

    private fun writePdf(file: File, note: NoteWithExtras) {
        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
        var canvas = page.canvas
        val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 12f }

        var y = 40f
        canvas.drawText(note.note.title, 40f, y, titlePaint)
        y += 30f

        val lines = bodyAsPlainText(note.note.body).split("\n") +
            note.checklist.map { "${if (it.checked) "[x]" else "[ ]"} ${it.text}" }

        for (line in lines) {
            // wrap long lines manually at ~90 chars for an A4-ish page
            line.chunked(90).forEach { chunk ->
                if (y > pageHeight - 40f) {
                    pdf.finishPage(page)
                    page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdf.pages.size + 1).create())
                    canvas = page.canvas
                    y = 40f
                }
                canvas.drawText(chunk, 40f, y, bodyPaint)
                y += 18f
            }
        }
        pdf.finishPage(page)
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
    }
}
