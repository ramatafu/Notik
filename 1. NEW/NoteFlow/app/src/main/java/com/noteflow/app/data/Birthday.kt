package com.noteflow.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A birthday to remind about. [month]/[day] are always known (1-12 / 1-31);
 * [year] is optional since many contacts only expose month+day.
 */
@Entity(tableName = "birthdays")
data class Birthday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: String? = null,
    val name: String = "",
    val photoUri: String? = null,
    val month: Int,
    val day: Int,
    val year: Int? = null
)
