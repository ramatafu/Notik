package com.noteflow.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Note::class, ChecklistItem::class, NoteImage::class, Label::class, NoteLabelCrossRef::class, Birthday::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun labelDao(): LabelDao
    abstract fun birthdayDao(): BirthdayDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noteflow.db"
                )
                    // Early-stage app, no production migrations to preserve yet —
                    // adding the "birthdays" table just recreates the DB on upgrade.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
