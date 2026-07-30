package com.noteflow.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

        /**
         * v1 -> v2: adds the "birthdays" table only. Every other table (notes,
         * checklist_items, note_images, labels, note_label_cross_ref) is left
         * completely untouched, so existing notes survive the app update.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `birthdays` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `contactId` TEXT,
                        `name` TEXT NOT NULL,
                        `photoUri` TEXT,
                        `month` INTEGER NOT NULL,
                        `day` INTEGER NOT NULL,
                        `year` INTEGER
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noteflow.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    // Downgrading (reinstalling an older build over a newer DB) is rare
                    // and not something we promise to preserve — but a normal *upgrade*
                    // never wipes data now that an explicit migration exists above.
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build().also { INSTANCE = it }
            }
    }
}
