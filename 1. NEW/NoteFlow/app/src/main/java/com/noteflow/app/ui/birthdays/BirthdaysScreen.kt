package com.noteflow.app.ui.birthdays

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.noteflow.app.data.Birthday
import com.noteflow.app.ui.ViewModelFactory
import java.util.Calendar

private val MONTHS_RU = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)

/** Contact data read from the system Contacts app after picking one. */
private data class PickedContact(
    val id: String?,
    val name: String,
    val photoUri: String?,
    val month: Int?,
    val day: Int?,
    val year: Int?
)

@Composable
fun BirthdaysScreen() {
    val context = LocalContext.current
    val viewModel: BirthdaysViewModel = viewModel(factory = ViewModelFactory(context))
    val birthdays by viewModel.birthdays.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var pendingContact by remember { mutableStateOf<PickedContact?>(null) }

    val contactPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri: Uri? ->
        if (uri != null) {
            pendingContact = readContact(context, uri)
        } else {
            pendingContact = null
        }
        showAddDialog = true
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) contactPicker.launch(null) else {
            // No contacts permission — fall back to fully manual entry.
            pendingContact = null
            showAddDialog = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (birthdays.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Пока нет ни одного дня рождения", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)) {
                items(birthdays, key = { it.id }) { birthday ->
                    BirthdayRow(birthday, onDelete = { viewModel.delete(birthday) })
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                    PackageManager.PERMISSION_GRANTED
                if (granted) contactPicker.launch(null) else permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Добавить день рождения")
        }
    }

    if (showAddDialog) {
        AddBirthdayDialog(
            initial = pendingContact,
            onConfirm = { birthday ->
                viewModel.add(birthday)
                showAddDialog = false
                pendingContact = null
            },
            onDismiss = {
                showAddDialog = false
                pendingContact = null
            }
        )
    }
}

@Composable
private fun BirthdayRow(birthday: Birthday, onDelete: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (birthday.photoUri != null) {
                AsyncImage(
                    model = birthday.photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Icon(Icons.Default.Cake, contentDescription = null)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(birthday.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${birthday.day} ${MONTHS_RU[birthday.month - 1]}" + (birthday.year?.let { ", $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить")
        }
    }
}

/** Reads display name, photo and — if present — the TYPE_BIRTHDAY event from a picked contact. */
private fun readContact(context: android.content.Context, uri: Uri): PickedContact {
    var name = "Без имени"
    var contactId: String? = null
    var photoUri: String? = null

    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val photoIdx = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
            if (idIdx >= 0) contactId = cursor.getString(idIdx)
            if (nameIdx >= 0) cursor.getString(nameIdx)?.let { name = it }
            if (photoIdx >= 0) photoUri = cursor.getString(photoIdx)
        }
    }

    var month: Int? = null
    var day: Int? = null
    var year: Int? = null

    contactId?.let { id ->
        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(id, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE)
                val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else -1
                if (type == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) {
                    val dateIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)
                    val raw = if (dateIdx >= 0) cursor.getString(dateIdx) else null
                    raw?.let { d ->
                        // Format is "yyyy-MM-dd", or "--MM-dd" when the contact has no year set.
                        if (d.startsWith("--")) {
                            val parts = d.removePrefix("--").split("-")
                            if (parts.size == 2) {
                                month = parts[0].toIntOrNull()
                                day = parts[1].toIntOrNull()
                            }
                        } else {
                            val parts = d.split("-")
                            if (parts.size == 3) {
                                year = parts[0].toIntOrNull()
                                month = parts[1].toIntOrNull()
                                day = parts[2].toIntOrNull()
                            }
                        }
                    }
                }
            }
        }
    }

    return PickedContact(contactId, name, photoUri, month, day, year)
}

@Composable
private fun AddBirthdayDialog(initial: PickedContact?, onConfirm: (Birthday) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val cal = remember { Calendar.getInstance() }
    var month by remember { mutableStateOf(initial?.month ?: (cal.get(Calendar.MONTH) + 1)) }
    var day by remember { mutableStateOf(initial?.day ?: cal.get(Calendar.DAY_OF_MONTH)) }
    var name by remember { mutableStateOf(initial?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый день рождения") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        android.app.DatePickerDialog(
                            context,
                            { _, _, pickedMonth, pickedDay ->
                                month = pickedMonth + 1
                                day = pickedDay
                            },
                            cal.get(Calendar.YEAR), month - 1, day
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Дата: $day ${MONTHS_RU[month - 1]}")
                }
                if (initial?.month != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Дата рождения найдена в контакте",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    Birthday(
                        contactId = initial?.id,
                        name = name.ifBlank { "Без имени" },
                        photoUri = initial?.photoUri,
                        month = month,
                        day = day,
                        year = initial?.year
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
