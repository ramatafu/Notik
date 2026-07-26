package com.noteflow.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthdayDao {
    @Insert
    suspend fun insert(birthday: Birthday): Long

    @Update
    suspend fun update(birthday: Birthday)

    @Delete
    suspend fun delete(birthday: Birthday)

    @Query("SELECT * FROM birthdays ORDER BY month, day")
    fun observeAll(): Flow<List<Birthday>>

    @Query("SELECT * FROM birthdays")
    suspend fun getAllOnce(): List<Birthday>

    @Query("SELECT * FROM birthdays WHERE id = :id")
    suspend fun getById(id: Long): Birthday?
}
