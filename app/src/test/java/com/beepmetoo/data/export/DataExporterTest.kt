package com.beepmetoo.data.export

import com.beepmetoo.data.db.entity.Sample
import com.beepmetoo.data.db.entity.SampleWithTags
import com.beepmetoo.data.db.entity.Tag
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DataExporterTest {

    @Test
    fun `toCsv with empty list returns header only`() {
        val csv = DataExporter.toCsv(emptyList())
        assertThat(csv).isEqualTo("id,title,description,photoPath,timestamp,createdAt,tags\n")
    }

    @Test
    fun `toCsv with samples produces correct rows`() {
        val data = listOf(
            SampleWithTags(
                sample = Sample(id = 1, title = "Test", description = "Desc", timestamp = 1000, createdAt = 1001),
                tags = listOf(Tag(id = 1, name = "mood"), Tag(id = 2, name = "work")),
            ),
            SampleWithTags(
                sample = Sample(id = 2, title = "No tags", timestamp = 2000, createdAt = 2001),
                tags = emptyList(),
            ),
        )

        val csv = DataExporter.toCsv(data)
        val lines = csv.lines()

        assertThat(lines[0]).isEqualTo("id,title,description,photoPath,timestamp,createdAt,tags")
        assertThat(lines[1]).isEqualTo("1,Test,Desc,,1000,1001,\"mood,work\"")
        assertThat(lines[2]).isEqualTo("2,No tags,,,2000,2001,\"\"")
    }

    @Test
    fun `toCsv escapes commas in title`() {
        val data = listOf(
            SampleWithTags(
                sample = Sample(id = 1, title = "Hello, world", timestamp = 1000, createdAt = 1001),
                tags = emptyList(),
            ),
        )

        val csv = DataExporter.toCsv(data)
        val lines = csv.lines()

        assertThat(lines[1]).isEqualTo("1,\"Hello, world\",,,1000,1001,\"\"")
    }

    @Test
    fun `toCsv escapes quotes in description`() {
        val data = listOf(
            SampleWithTags(
                sample = Sample(id = 1, title = "T", description = "She said \"hi\"", timestamp = 1000, createdAt = 1001),
                tags = emptyList(),
            ),
        )

        val csv = DataExporter.toCsv(data)
        val lines = csv.lines()

        assertThat(lines[1]).contains("\"She said \"\"hi\"\"\"")
    }
}
