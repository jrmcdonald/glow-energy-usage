package com.jrmcdonald.energy.usage.core

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.OptionCallTransformContext
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.jrmcdonald.energy.usage.core.service.CsvOutputService
import com.jrmcdonald.energy.usage.core.service.EnergyProviderService
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField

class UsageApplication(
    private val energyProviderService: EnergyProviderService,
    private val csvOutputService: CsvOutputService
) : CliktCommand() {
    private val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd")
        .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
        .toFormatter()
        .withZone(ZoneId.of("Europe/London"))

    private val from: Instant by option("-f", "--from", help = "From date (yyyy-MM-dd)")
        .convert(conversion = convertToInstant())
        .required()

    private val to: Instant by option("-t", "--to", help = "To date (yyyy-MM-dd)")
        .convert(conversion = convertToInstant())
        .required()

    override fun run() = energyProviderService.getEnergyUsage(from, to).let(csvOutputService::output)

    private fun convertToInstant(): OptionCallTransformContext.(String) -> Instant {
        return {
            try {
                formatter.parse(it, Instant::from)
            } catch (e: DateTimeParseException) {
                throw IllegalArgumentException("'$it' could not be parsed, expected format is 'yyyy-MM-dd'")
            }
        }
    }
}
