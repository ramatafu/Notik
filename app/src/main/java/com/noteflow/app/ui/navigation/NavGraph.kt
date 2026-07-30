package com.noteflow.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.noteflow.app.ui.books.BooksScreen
import com.noteflow.app.ui.editor.NoteEditorScreen
import com.noteflow.app.ui.labels.LabelsScreen
import com.noteflow.app.ui.notes.NotesListScreen
import com.noteflow.app.ui.settings.SettingsScreen

object Routes {
    const val LIST = "list"
    const val EDITOR = "editor/{noteId}/{isList}"
    const val LABELS = "labels"
    const val SETTINGS = "settings"
    const val BOOKS = "books"
    fun editor(noteId: Long, isList: Boolean = false) = "editor/$noteId/$isList"
}

@Composable
fun NoteFlowNavGraph(startNoteId: Long? = null) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            NotesListScreen(
                onOpenNote = { id, isList -> navController.navigate(Routes.editor(id, isList)) },
                onOpenLabels = { navController.navigate(Routes.LABELS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenBooks = { navController.navigate(Routes.BOOKS) }
            )
        }
        composable(
            route = Routes.EDITOR,
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType },
                navArgument("isList") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            val isList = backStackEntry.arguments?.getBoolean("isList") ?: false
            NoteEditorScreen(noteId = noteId, forceListType = isList, onBack = { navController.popBackStack() })
        }
        composable(Routes.LABELS) {
            LabelsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.BOOKS) {
            BooksScreen(onBack = { navController.popBackStack() })
        }
    }

    // Deep link from widget/notification: jump straight to a note once, on first composition.
    startNoteId?.let { id ->
        androidx.compose.runtime.LaunchedEffect(id) {
            navController.navigate(Routes.editor(id))
        }
    }
}
