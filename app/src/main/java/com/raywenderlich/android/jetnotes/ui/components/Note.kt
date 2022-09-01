package com.raywenderlich.android.jetnotes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raywenderlich.android.jetnotes.theme.rwGreen

@Composable
fun Note(paddingValues: PaddingValues) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
    ) {
        NoteColor(color = rwGreen, padding = 4.dp, size = 40.dp, border = 1.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Title", maxLines = 1)
            Text(text = "Content", maxLines = 1)
        }
        Checkbox(checked = false, onCheckedChange = {}, modifier = Modifier.padding(start = 8.dp))
    }

}

@Preview(showBackground = true)
@Composable
private fun NotePreview() {
    Note(PaddingValues())
}
