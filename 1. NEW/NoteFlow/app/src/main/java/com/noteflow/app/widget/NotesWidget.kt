package com.noteflow.app.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.noteflow.app.MainActivity
import com.noteflow.app.NoteFlowApp

/** Key used to pass the tapped note's id from the widget into MainActivity. */
val NoteIdParamKey = ActionParameters.Key<Long>("noteId")

/**
 * Shows the user's pinned notes (falling back to the most recent ones) and lets
 * them tap a note to open it, or tap "+ New note" to jump straight into the editor.
 */
class NotesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as NoteFlowApp
        val notes = app.repository.allNotesForBackup()
            .map { it.note }
            .sortedWith(compareByDescending<com.noteflow.app.data.Note> { it.pinned }.thenByDescending { it.modifiedAt })
            .take(5)

        val target = ComponentName(context, MainActivity::class.java)

        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize().background(ColorProvider(Color.White)).padding(12.dp)
            ) {
                Text(
                    text = "NoteFlow",
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
                notes.forEach { note ->
                    Text(
                        text = note.title.ifBlank { note.body.take(40) },
                        modifier = GlanceModifier
                            .padding(vertical = 4.dp)
                            .clickable(actionStartActivity(
                                target,
                                actionParametersOf(NoteIdParamKey to note.id)
                            ))
                    )
                }
                Text(
                    text = "+ Новая заметка",
                    modifier = GlanceModifier
                        .padding(top = 8.dp)
                        .clickable(actionStartActivity(
                            target,
                            actionParametersOf(NoteIdParamKey to 0L)
                        ))
                )
            }
        }
    }
}

class NotesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NotesWidget()
}

