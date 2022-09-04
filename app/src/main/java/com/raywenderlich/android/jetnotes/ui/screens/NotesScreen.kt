package com.raywenderlich.android.jetnotes.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import com.raywenderlich.android.jetnotes.domain.model.NoteModel
import com.raywenderlich.android.jetnotes.routing.Screen
import com.raywenderlich.android.jetnotes.theme.rwRed
import com.raywenderlich.android.jetnotes.ui.components.AppDrawer
import com.raywenderlich.android.jetnotes.ui.components.Note
import com.raywenderlich.android.jetnotes.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * Home Screen
 */
@Composable
fun NotesScreen(viewModel: MainViewModel) {

    // Observe notes state from MainViewModel
    val notes: List<NoteModel> by viewModel
        .notesNotInTrash
        .observeAsState(listOf())

    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "JetNotes", color = MaterialTheme.colors.onPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = { coroutineScope.launch { scaffoldState.drawerState.open() } }) {
                        Icon(imageVector = Icons.Filled.List, contentDescription = "Drawer button")
                    }
                }
            )
        },
        scaffoldState = scaffoldState,
        drawerContent = {
            AppDrawer(
                currentScreen = Screen.Notes,
                closeDrawerAction = {
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
            )
        },
        content = { paddingValues ->
            if (notes.isNotEmpty()) {
                NotesList(
                    paddingValues = paddingValues,
                    notes = notes,
                    onNoteClick = { viewModel.onNoteClick(it) },
                    onNoteCheckedChange = { viewModel.onNoteCheckedChange(it) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onCreateNewNoteClick() },
                contentColor = MaterialTheme.colors.background,
                backgroundColor = rwRed, // TODO
                content = {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Note Button")
                }
            )
        }
    )
}

@Composable
fun NotesList(
    paddingValues: PaddingValues? = null,
    notes: List<NoteModel>,
    onNoteClick: (NoteModel) -> Unit,
    onNoteCheckedChange: (NoteModel) -> Unit
) {
    LazyColumn {
        items(notes.size) { noteIndex ->
            val note = notes[noteIndex]
            Note(
                note = note,
                onNoteClick = onNoteClick,
                onNoteCheckedChange = onNoteCheckedChange
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesListPreview() {
    NotesList(
        notes = listOf(
            NoteModel(1, "Title 1", "Content 1", null),
            NoteModel(2, "Title 2", "Content 2", false),
            NoteModel(3, "Title 3", "Content 3", true)
        ),
        onNoteClick = {},
        onNoteCheckedChange = {}
    )
}
