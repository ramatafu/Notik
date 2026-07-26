package com.noteflow.app.ui.birthdays

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteflow.app.data.Birthday
import com.noteflow.app.data.BirthdaysRepository
import com.noteflow.app.reminder.BirthdayScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BirthdaysViewModel(
    private val repository: BirthdaysRepository,
    private val appContext: Context
) : ViewModel() {

    val birthdays = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(birthday: Birthday) = viewModelScope.launch {
        val id = repository.add(birthday)
        BirthdayScheduler.schedule(appContext, birthday.copy(id = id))
    }

    fun delete(birthday: Birthday) = viewModelScope.launch {
        BirthdayScheduler.cancel(appContext, birthday.id)
        repository.delete(birthday)
    }
}
