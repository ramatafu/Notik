package com.noteflow.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.noteflow.app.NoteFlowApp
import com.noteflow.app.ui.birthdays.BirthdaysViewModel
import com.noteflow.app.ui.calendar.CalendarViewModel
import com.noteflow.app.ui.editor.EditorViewModel
import com.noteflow.app.ui.labels.LabelsViewModel
import com.noteflow.app.ui.notes.NotesViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    private val app = context.applicationContext as NoteFlowApp

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        NotesViewModel::class.java -> NotesViewModel(app.repository) as T
        EditorViewModel::class.java -> EditorViewModel(app.repository, context.applicationContext, app.settingsRepository) as T
        LabelsViewModel::class.java -> LabelsViewModel(app.repository) as T
        BirthdaysViewModel::class.java -> BirthdaysViewModel(app.birthdaysRepository, context.applicationContext) as T
        CalendarViewModel::class.java -> CalendarViewModel(app.repository) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
    }
}
