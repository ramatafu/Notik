package com.noteflow.app.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteflow.app.data.Note
import com.noteflow.app.data.NotesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class NotesTab { ACTIVE, ARCHIVED, TRASH, BIRTHDAYS }

class NotesViewModel(private val repository: NotesRepository) : ViewModel() {

    private val _tab = MutableStateFlow(NotesTab.ACTIVE)
    val tab: StateFlow<NotesTab> = _tab

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _labelFilter = MutableStateFlow<String?>(null)
    val labelFilter: StateFlow<String?> = _labelFilter

    // Bumped every time the list screen becomes visible again (see NotesListScreen's
    // ON_RESUME observer). Room's Flow already re-queries reactively on its own, but
    // this guarantees a fresh read the moment you come back from the editor, closing
    // any possible window where a just-saved note could show up stale.
    private val _refreshTick = MutableStateFlow(0)
    fun refresh() { _refreshTick.value++ }

    val labels = repository.labels().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount = repository.activeCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val archivedCount = repository.archivedCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val trashedCount = repository.trashedCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = combine(_tab, _query, _labelFilter, _refreshTick) { tab, query, label, _ ->
        Triple(tab, query, label)
    }.flatMapLatest { (tab, query, label) ->
        when {
            tab == NotesTab.BIRTHDAYS -> flowOf(emptyList())
            query.isNotBlank() -> repository.search(query)
            label != null -> repository.notesByLabel(label)
            tab == NotesTab.ACTIVE -> repository.activeNotes()
            tab == NotesTab.ARCHIVED -> repository.archivedNotes()
            else -> repository.trashedNotes()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: NotesTab) { _labelFilter.value = null; _tab.value = tab }
    fun setQuery(q: String) { _query.value = q }
    fun filterByLabel(label: String?) { _labelFilter.value = label }

    fun togglePin(note: Note) = viewModelScope.launch { repository.setPinned(note, !note.pinned) }

    /** Pinned notes are protected from archiving/trashing until unpinned first. */
    fun archive(note: Note) {
        if (note.pinned) return
        viewModelScope.launch { repository.setArchived(note, true) }
    }
    fun unarchive(note: Note) = viewModelScope.launch { repository.setArchived(note, false) }

    fun moveToTrash(note: Note) {
        if (note.pinned) return
        viewModelScope.launch { repository.moveToTrash(note) }
    }
    fun restore(note: Note) = viewModelScope.launch { repository.restoreFromTrash(note) }

    fun deleteForever(note: Note, onDone: () -> Unit = {}) = viewModelScope.launch {
        repository.deleteForever(note)
        onDone()
    }

    fun emptyTrash() = viewModelScope.launch { repository.emptyTrash() }
}
