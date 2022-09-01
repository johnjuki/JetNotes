package com.raywenderlich.android.jetnotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raywenderlich.android.jetnotes.theme.rwGreen

@Composable
fun Note() {
    Box(modifier = Modifier
        .size(40.dp)
        .background(rwGreen)) {
        Text(text = "Title", maxLines = 1)
        Text(text = "Content", maxLines = 1)
        Checkbox(checked = false, onCheckedChange = {}, modifier = Modifier.padding(start = 8.dp))
    }
}

