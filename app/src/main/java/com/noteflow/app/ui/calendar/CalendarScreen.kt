package com.noteflow.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noteflow.app.data.Note
import com.noteflow.app.ui.ViewModelFactory
import com.noteflow.app.ui.theme.AccentRed
import java.util.Calendar

private val WEEKDAY_LABELS = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
private val MONTH_NAMES = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
)
private val MONTH_NAMES_GENITIVE = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)

/**
 * onOpenNote(noteId, calendarDate) — noteId == 0L means "create a new note for this day".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onOpenNote: (Long, Long) -> Unit) {
    val context = LocalContext.current
    val viewModel: CalendarViewModel = viewModel(factory = ViewModelFactory(context))
    val yearMonth by viewModel.yearMonth.collectAsState()
    val datesWithNotes by viewModel.datesWithNotes.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val notesForSelected by viewModel.notesForSelectedDate.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            IconButton(onClick = { viewModel.prevMonth() }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Предыдущий месяц")
            }
            Text(
                "${MONTH_NAMES[yearMonth.month]} ${yearMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Следующий месяц")
            }
        }
        TextButton(onClick = { viewModel.goToToday() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Сегодня")
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            WEEKDAY_LABELS.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        val weeks = remember(yearMonth) { buildMonthGrid(yearMonth).chunked(7) }
        Column(Modifier.padding(horizontal = 8.dp)) {
            weeks.forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { cell ->
                        DayCell(
                            date = cell,
                            hasNote = cell != null && datesWithNotes.contains(cell),
                            isToday = cell != null && isToday(cell),
                            isSelected = cell != null && cell == selectedDate,
                            onClick = { cell?.let { viewModel.selectDate(it) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    val date = selectedDate
    if (date != null) {
        DayNotesSheet(
            date = date,
            notes = notesForSelected,
            onDismiss = { viewModel.selectDate(null) },
            onOpenNote = { noteId -> onOpenNote(noteId, date) },
            onAddNote = { onOpenNote(0L, date) }
        )
    }
}

@Composable
private fun DayCell(date: Long?, hasNote: Boolean, isToday: Boolean, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .then(
                when {
                    isSelected -> Modifier.background(AccentRed)
                    isToday -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    else -> Modifier
                }
            )
            .then(if (date != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            val day = remember(date) { Calendar.getInstance().apply { timeInMillis = date }.get(Calendar.DAY_OF_MONTH) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    day.toString(),
                    color = if (isSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (hasNote && !isSelected) {
                    Spacer(Modifier.height(2.dp))
                    Box(Modifier.size(4.dp).clip(CircleShape).background(AccentRed))
                }
            }
        }
    }
}

@Composable
private fun DayNotesSheet(date: Long, notes: List<Note>, onDismiss: () -> Unit, onOpenNote: (Long) -> Unit, onAddNote: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp).padding(bottom = 48.dp)) {
            Text(formatFullDate(date), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            if (notes.isEmpty()) {
                Text("На этот день пока нет заметок", color = MaterialTheme.colorScheme.outline)
            } else {
                notes.forEach { note ->
                    Text(
                        note.title.ifBlank { "Без названия" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenNote(note.id) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onAddNote, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Добавить заметку")
            }
        }
    }
}

private fun buildMonthGrid(ym: YearMonth): List<Long?> {
    val cal = Calendar.getInstance().apply {
        set(ym.year, ym.month, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    // Calendar.DAY_OF_WEEK: Sunday=1 .. Saturday=7. We lay the grid out Monday-first.
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val leadingBlanks = (firstDayOfWeek + 5) % 7
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val cells = mutableListOf<Long?>()
    repeat(leadingBlanks) { cells.add(null) }
    for (day in 1..daysInMonth) cells.add(dateKey(ym.year, ym.month, day))
    while (cells.size % 7 != 0) cells.add(null)
    return cells
}

private fun isToday(dateMillis: Long): Boolean {
    val today = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = dateMillis }
    return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
}

private fun formatFullDate(dateMillis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
    return "${cal.get(Calendar.DAY_OF_MONTH)} ${MONTH_NAMES_GENITIVE[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
}
