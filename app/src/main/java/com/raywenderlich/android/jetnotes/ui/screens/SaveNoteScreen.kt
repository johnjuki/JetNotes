package com.raywenderlich.android.jetnotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raywenderlich.android.jetnotes.R
import com.raywenderlich.android.jetnotes.domain.model.ColorModel
import com.raywenderlich.android.jetnotes.domain.model.NEW_NOTE_ID
import com.raywenderlich.android.jetnotes.domain.model.NoteModel
import com.raywenderlich.android.jetnotes.routing.JetNotesRouter
import com.raywenderlich.android.jetnotes.routing.Screen
import com.raywenderlich.android.jetnotes.ui.components.NoteColor
import com.raywenderlich.android.jetnotes.util.fromHex
import com.raywenderlich.android.jetnotes.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SaveNoteScreen(viewModel: MainViewModel) {

    // Subscribe SaveNoteScreen() to viewModel.noteEntry’s state
    val noteEntry: NoteModel by viewModel
        .noteEntry
        .observeAsState(NoteModel())

    // Subscribe SaveNoteScreen() to viewModel.colors’s state
    val colors: List<ColorModel> by viewModel
        .colors
        .observeAsState(listOf())

    val bottomDrawerState: BottomDrawerState =
        rememberBottomDrawerState(initialValue = BottomDrawerValue.Closed)

    val coroutineScope = rememberCoroutineScope()

    /**
     * This state represents whether the confirm move note to trash dialog is visible
     */
    val moveNoteToTrashDialogShownState: MutableState<Boolean> =
        rememberSaveable { // TODO: Change to remember() & see the difference
            mutableStateOf(false)
        }
    
    BackHandler {
        if (bottomDrawerState.isOpen) {
            coroutineScope.launch { bottomDrawerState.close() }
        } else {
            JetNotesRouter.navigateTo(Screen.Notes)
        }
    }

    Scaffold(
        topBar = {
            val isEditingMode: Boolean = noteEntry.id != NEW_NOTE_ID
            SaveNoteTopBar(
                isEditingMode = isEditingMode,
                onBackClick = {
                    JetNotesRouter.navigateTo(Screen.Notes)
                },
                onSaveNoteClick = {
                    viewModel.saveNote(noteEntry)
                },
                onOpenColorPickerClick = {
                    coroutineScope.launch { bottomDrawerState.open() }
                },
                onDeleteNoteClick = {
                    moveNoteToTrashDialogShownState.value = true
                }
            )
        },
        content = { paddingValues ->
            paddingValues

            BottomDrawer(
                drawerState = bottomDrawerState,
                drawerContent = {
                    ColorPicker(
                        colors = colors,
                        onColorSelect = { color ->
                            val newNoteEntry = noteEntry.copy(color = color)
                            viewModel.onNoteEntryChange(newNoteEntry)
                            coroutineScope.launch { bottomDrawerState.close() }
                        }
                    )
                },
                content = {
                    SaveNoteContent(
                        note = noteEntry,
                        onNoteChange = { updateNoteEntry ->
                            viewModel.onNoteEntryChange(updateNoteEntry)
                        }
                    )
                }
            )

            if (moveNoteToTrashDialogShownState.value) {
                AlertDialog(
                    onDismissRequest = { moveNoteToTrashDialogShownState.value = false },
                    title = { Text(text = "Move not to trash?") },
                    text = {
                        Text(
                            text = "Are you sure you want to" +
                                    "move this note to trash?"
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.moveNoteToTrash(noteEntry) }) {
                            Text(text = "Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { moveNoteToTrashDialogShownState.value = false }) {
                            Text(text = "Dismiss")
                        }
                    }
                )
            }
        }
    )
}

@Composable
private fun SaveNoteTopBar(
    isEditingMode: Boolean,
    onBackClick: () -> Unit,
    onSaveNoteClick: () -> Unit,
    onOpenColorPickerClick: () -> Unit,
    onDeleteNoteClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(text = "Save Note", color = MaterialTheme.colors.onPrimary)
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Save Note Button",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        },
        actions = {
            // Save note action Icon
            IconButton(onClick = onSaveNoteClick) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save Note",
                    tint = MaterialTheme.colors.onPrimary
                )
            }

            // Open color picker action icon
            IconButton(onClick = onOpenColorPickerClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_color_lens_24),
                    contentDescription = "Open Color Picker Button",
                    tint = MaterialTheme.colors.onPrimary
                )
            }

            // Delete action icon (show only in editing mode)
            if (isEditingMode) {
                IconButton(onClick = onDeleteNoteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Note Button",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
    )
}

// This composable will represent the entire logic of creating & editing notes
@Composable
private fun SaveNoteContent(
    note: NoteModel,
    onNoteChange: (NoteModel) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ContentTextField(
            label = "Title",
            text = note.title,
            onTextChange = { newTitle ->
                onNoteChange.invoke(note.copy(title = newTitle))
            }
        )

        ContentTextField(
            modifier = Modifier
                .heightIn(max = 240.dp)
                .padding(top = 16.dp),
            label = "Body",
            text = note.content,
            onTextChange = { newContent ->
                onNoteChange.invoke(note.copy(content = newContent))
            }
        )

        val canBeCheckedOff: Boolean = note.isCheckedOff != null

        NoteCheckOption(
            isChecked = canBeCheckedOff,
            onCheckedChanged = { canBeCheckedOffNewValue ->
                val isCheckedOff: Boolean? = if (canBeCheckedOffNewValue) false else null

                onNoteChange.invoke(note.copy(isCheckedOff = isCheckedOff))
            }
        )

        PickedColor(color = note.color)
    }
}

@Composable
private fun ContentTextField(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    onTextChange: (String) -> Unit
) {
    TextField(
        value = text,
        onValueChange = onTextChange,
        label = { Text(text = label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.surface)
    )
}

@Composable
private fun PickedColor(color: ColorModel) {
    Row(
        Modifier
            .padding(8.dp)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Picked color",
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        NoteColor(
            color = Color.fromHex(color.hex),
            size = 40.dp,
            border = 1.dp,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
private fun NoteCheckOption(isChecked: Boolean, onCheckedChanged: (Boolean) -> Unit) {
    Row(
        Modifier
            .padding(8.dp)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Can note be checked off?",
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChanged,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun ColorPicker(
    colors: List<ColorModel>,
    onColorSelect: (ColorModel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Color Picker",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(colors.size) { itemIndex ->
                val color = colors[itemIndex]
                ColorItem(color = color, onColorSelect = onColorSelect)
            }
        }
    }
}

@Composable
fun ColorItem(
    color: ColorModel,
    onColorSelect: (ColorModel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onColorSelect(color) }
    ) {
        NoteColor(
            modifier = Modifier.padding(10.dp),
            color = Color.fromHex(color.hex),
            size = 80.dp,
            border = 2.dp
        )
        Text(
            text = color.name,
            fontSize = 22.sp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SaveNoteTopBarPreview() {
//    JetNotesTheme {
//        SaveNoteTopBar(
//            isEditingMode = false,
//            onBackClick = { },
//            onSaveNoteClick = { },
//            onOpenColorPickerClick = { },
//            onDeleteNoteClick = { }
//        )
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun SaveNoteContentPreview() {
//        SaveNoteContent(note = NoteModel(title = "Title", content = "Content"), onNoteChange = {})
//}

//@Preview(showBackground = true)
//@Composable
//fun SaveNoteTopBarEditingPreview() {
//    JetNotesTheme {
//        SaveNoteTopBar(
//            isEditingMode = true,
//            onBackClick = { },
//            onSaveNoteClick = { },
//            onOpenColorPickerClick = { },
//            onDeleteNoteClick = { }
//        )
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun ColorItemPreview() {
//    ColorItem(color = ColorModel.DEFAULT) {}
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ColorPickerPreview() {
//    ColorPicker(
//        colors = listOf(ColorModel.DEFAULT, ColorModel.DEFAULT, ColorModel.DEFAULT),
//        onColorSelect = {}
//    )
//}

//@Preview(showBackground = true)
//@Composable
//fun ContentTextFieldPreview() {
//    ContentTextField(label = "Title", text = "", onTextChange = {})
//}
//
//@Preview(showBackground = true)
//@Composable
//fun NoteCheckOptionPreview() {
//    NoteCheckOption(isChecked = false, onCheckedChanged = {})
//}

//@Preview(showBackground = true)
//@Composable
//fun PickerColorPreview() {
//    PickedColor(ColorModel.DEFAULT)
//}
