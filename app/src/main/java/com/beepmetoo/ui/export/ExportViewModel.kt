package com.beepmetoo.ui.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beepmetoo.data.export.DataExporter
import com.beepmetoo.data.repository.SampleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val repository: SampleRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    fun exportCsv(onShareIntent: (Intent) -> Unit) {
        viewModelScope.launch {
            val samples = repository.getAllSamplesWithTags().first()
            val csv = DataExporter.toCsv(samples)
            val file = DataExporter.exportCsvToFile(context, csv)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            onShareIntent(Intent.createChooser(intent, "Export CSV"))
        }
    }

    fun exportDatabase(onShareIntent: (Intent) -> Unit) {
        viewModelScope.launch {
            val file = DataExporter.copyDatabaseToCache(context)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/x-sqlite3"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            onShareIntent(Intent.createChooser(intent, "Export Database"))
        }
    }
}
