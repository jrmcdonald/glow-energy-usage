package com.jrmcdonald.energy.usage.core.service

import com.jrmcdonald.energy.usage.core.model.UsageEntry
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CsvOutputService(outputDirectory: String, outputFileName: String) {

    private val outputPath = Paths.get(outputDirectory).toAbsolutePath().resolve(outputFileName)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun output(entries: List<UsageEntry>) =
        getWriter().use { writer ->
            getPrinter(writer).use { printer ->
                entries.forEach { entry -> printUsageEntry(printer, entry) }
            }
        }

    private fun getWriter() = Files.newBufferedWriter(outputPath)

    private fun getPrinter(writer: BufferedWriter) =
        CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Date", "Time", "Usage"))

    private fun printUsageEntry(printer: CSVPrinter, usageEntry: UsageEntry) =
        with(usageEntry) { printer.printRecord(getDate(timestamp), getTime(timestamp), usage) }

    private fun getDate(timestamp: Instant) =
        dateFormatter.format(LocalDate.ofInstant(timestamp, ZoneId.of("Europe/London")))

    private fun getTime(timestamp: Instant) =
        timeFormatter.format(LocalTime.ofInstant(timestamp, ZoneId.of("Europe/London")))
}
