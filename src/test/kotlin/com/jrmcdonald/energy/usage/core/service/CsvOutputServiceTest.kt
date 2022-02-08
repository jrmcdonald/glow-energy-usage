package com.jrmcdonald.energy.usage.core.service

import com.jrmcdonald.energy.usage.core.model.UsageEntry
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Instant.parse
import kotlin.io.path.exists

internal class CsvOutputServiceTest {

    @Test
    fun `Should write usage entries to csv`(@TempDir tempDir: Path) {
        val service = CsvOutputService(tempDir.toString(), "output.csv")

        val entries = listOf(
            UsageEntry(parse("2022-01-01T00:30:00Z"), 123.456F),
            UsageEntry(parse("2022-01-01T01:00:00Z"), 654.321F)
        )

        service.output(entries)

        val expectedLines = listOf(
            "Date,Time,Usage",
            "2022-01-01,00:30:00,123.456",
            "2022-01-01,01:00:00,654.321"
        )

        with(tempDir.resolve("output.csv")) {
            exists() shouldBe true
            toFile().readLines() shouldContainExactly expectedLines
        }
    }
}
