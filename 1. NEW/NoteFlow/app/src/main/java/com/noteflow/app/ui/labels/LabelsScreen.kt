package com.noteflow.app.ui.labels

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noteflow.app.ui.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: LabelsViewModel = viewModel(factory = ViewModelFactory(context))
    val labels by viewModel.labels.collectAsState()
    var editing by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Лейблы") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }
        )
    }) { padding ->
        LazyColumnLabels(padding) {
            labels.forEach { label ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = { editing = label.name; editValue = label.name }) {
                        Icon(Icons.Default.Edit, contentDescription = "Переименовать")
                    }
                    IconButton(onClick = { viewModel.delete(label.name) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            }
        }
    }

    editing?.let { oldName ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Переименовать лейбл") },
            text = { OutlinedTextField(value = editValue, onValueChange = { editValue = it }) },
            confirmButton = {
                TextButton(onClick = { viewModel.rename(oldName, editValue); editing = null }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun LazyColumnLabels(padding: androidx.compose.foundation.layout.PaddingValues, content: @Composable () -> Unit) {
    Column(Modifier.padding(padding)) { content() }
}
