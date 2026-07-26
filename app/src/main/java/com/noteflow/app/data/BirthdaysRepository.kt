package com.noteflow.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class BirthdaysRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).birthdayDao()

    fun observeAll(): Flow<List<Birthday>> = dao.observeAll()
    suspend fun add(birthday: Birthday): Long = dao.insert(birthday)
    suspend fun update(birthday: Birthday) = dao.update(birthday)
    suspend fun delete(birthday: Birthday) = dao.delete(birthday)
    suspend fun getAllOnce(): List<Birthday> = dao.getAllOnce()
    suspend fun getById(id: Long): Birthday? = dao.getById(id)
}
