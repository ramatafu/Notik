package com.noteflow.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteflow.app.data.Note
import com.noteflow.app.data.NotesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.Calendar

/** A year+month pair (month is 0-based, matching java.util.Calendar.MONTH). */
data class YearMonth(val year: Int, val month: Int) {
    fun next(): YearMonth = if (month == 11) YearMonth(year + 1, 0) else YearMonth(year, month + 1)
    fun prev(): YearMonth = if (month == 0) YearMonth(year - 1, 11) else YearMonth(year, month - 1)
}

/** Normalizes a year/month/day to local midnight, epoch millis — the canonical key used to link a note to a day. */
fun dateKey(year: Int, month: Int, day: Int): Long =
    Calendar.getInstance().apply {
        set(year, month, day, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

class CalendarViewModel(private val repository: NotesRepository) : ViewModel() {

    private val _yearMonth = MutableStateFlow(
        Calendar.getInstance().let { YearMonth(it.get(Calendar.YEAR), it.get(Calendar.MONTH)) }
    )
    val yearMonth: StateFlow<YearMonth> = _yearMonth

    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate: StateFlow<Long?> = _selectedDate

    /** Every distinct day (local midnight) that has at least one non-trashed note, for showing a dot in the grid. */
    val datesWithNotes: StateFlow<Set<Long>> = repository.calendarDates()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    val notesForSelectedDate: StateFlow<List<Note>> = _selectedDate
        .flatMapLatest { date -> if (date == null) flowOf(emptyList()) else repository.notesForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun nextMonth() { _yearMonth.value = _yearMonth.value.next() }
    fun prevMonth() { _yearMonth.value = _yearMonth.value.prev() }
    fun goToToday() {
        val c = Calendar.getInstance()
        _yearMonth.value = YearMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
    }

    fun selectDate(date: Long?) { _selectedDate.value = date }
}
