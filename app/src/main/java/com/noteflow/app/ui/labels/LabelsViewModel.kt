package com.noteflow.app.ui.labels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteflow.app.data.NotesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LabelsViewModel(private val repository: NotesRepository) : ViewModel() {
    val labels = repository.labels().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun rename(oldName: String, newName: String) = viewModelScope.launch {
        if (newName.isNotBlank() && newName != oldName) repository.renameLabel(oldName, newName)
    }

    fun delete(name: String) = viewModelScope.launch { repository.deleteLabel(name) }
}
