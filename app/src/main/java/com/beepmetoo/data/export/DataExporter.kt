package com.beepmetoo.data.export

import android.content.Context
import com.beepmetoo.data.db.entity.SampleWithTags
import java.io.File

object DataExporter {

    private const val HEADER = "id,title,description,photoPath,timestamp,createdAt,tags"

    fun toCsv(samples: List<SampleWithTags>): String {
        val sb = StringBuilder()
        sb.appendLine(HEADER)
        for (item in samples) {
            val s = item.sample
            val tags = item.tags.joinToString(",") { it.name }
            sb.appendLine(
                listOf(
                    s.id.toString(),
                    csvField(s.title),
                    csvField(s.description ?: ""),
                    csvField(s.photoPath ?: ""),
                    s.timestamp.toString(),
                    s.createdAt.toString(),
                    csvField(tags),
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    fun exportCsvToFile(context: Context, csv: String): File {
        val file = File(context.cacheDir, "beepmetoo_export.csv")
        file.writeText(csv)
        return file
    }

    fun copyDatabaseToCache(context: Context): File {
        val dbFile = context.getDatabasePath("beepmetoo.db")
        val cacheFile = File(context.cacheDir, "beepmetoo_export.db")
        dbFile.copyTo(cacheFile, overwrite = true)
        return cacheFile
    }

    private fun csvField(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
