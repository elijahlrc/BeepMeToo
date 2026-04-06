package com.beepmetoo.ui.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Sample") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { /* TODO: camera */ }) {
                Text("Attach photo")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onSaved,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }
        }
    }
}
