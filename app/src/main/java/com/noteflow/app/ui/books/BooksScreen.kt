package com.noteflow.app.ui.books

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * A minimal, fully offline PDF reader built on Android's built-in PdfRenderer —
 * no network, no third-party library. The user picks a PDF via the system file
 * picker (Storage Access Framework); pages are rendered as bitmaps on demand.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageIndex by remember { mutableStateOf(0) }
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            errorMessage = null
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Not all providers support persistable permissions — fine for this session either way.
            }
            fileName = queryDisplayName(context, uri)
            pageIndex = 0
            pdfUri = uri
        }
    }

    // (Re)opens the PdfRenderer whenever a new file is picked, and always closes it on the way out.
    DisposableEffect(pdfUri) {
        var pfd: ParcelFileDescriptor? = null
        val uri = pdfUri
        if (uri != null) {
            try {
                pfd = context.contentResolver.openFileDescriptor(uri, "r")
                renderer = pfd?.let { PdfRenderer(it) }
                if (renderer == null) errorMessage = "Не удалось открыть файл"
            } catch (e: Exception) {
                errorMessage = "Это не похоже на корректный PDF-файл"
            }
        }
        onDispose {
            renderer?.close()
            renderer = null
            pfd?.close()
        }
    }

    LaunchedEffect(renderer, pageIndex) {
        val r = renderer ?: return@LaunchedEffect
        if (pageIndex !in 0 until r.pageCount) return@LaunchedEffect
        val page = r.openPage(pageIndex)
        val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(AndroidColor.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        pageBitmap = bitmap
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fileName ?: "Книги") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
                actions = {
                    IconButton(onClick = { pickerLauncher.launch(arrayOf("application/pdf")) }) {
                        Icon(Icons.Default.FileOpen, contentDescription = "Открыть PDF")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                errorMessage != null -> {
                    Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                }
                pdfUri == null -> {
                    Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Выберите PDF-файл на устройстве", color = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { pickerLauncher.launch(arrayOf("application/pdf")) }) {
                                Text("Открыть PDF")
                            }
                        }
                    }
                }
                pageBitmap != null -> {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(
                            bitmap = pageBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        )
                    }
                    val totalPages = renderer?.pageCount ?: 0
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (pageIndex > 0) pageIndex-- }, enabled = pageIndex > 0) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Предыдущая страница")
                        }
                        Text("${pageIndex + 1} / $totalPages")
                        IconButton(onClick = { if (pageIndex < totalPages - 1) pageIndex++ }, enabled = pageIndex < totalPages - 1) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Следующая страница")
                        }
                    }
                }
                else -> {
                    Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String? = try {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
    }
} catch (_: Exception) {
    null
}
