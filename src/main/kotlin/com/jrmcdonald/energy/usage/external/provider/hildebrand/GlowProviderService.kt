package com.jrmcdonald.energy.usage.external.provider.hildebrand

import com.jrmcdonald.energy.usage.core.model.UsageEntry
import com.jrmcdonald.energy.usage.core.service.EnergyProviderService
import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.ReadingsResponse
import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.Resource
import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.toUsageEntries
import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAmount

class GlowProviderService(private val client: GlowClient) : EnergyProviderService {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        .withZone(ZoneId.of("Europe/London"))

    override fun getEnergyUsage(from: Instant, to: Instant): List<UsageEntry> {
        if (to.isBefore(from))
            throw IllegalArgumentException("Invalid date range supplied, from date should be before to date")

        val resource = getElectricConsumptionResource()

        return splitDateRangeToIntervalsRec(from, to, Period.ofDays(MAX_DAYS))
            .map { interval -> getReadings(resource.resourceId, interval) }
            .map(ReadingsResponse::toUsageEntries)
            .flatten()
            .toList()
    }

    private fun splitDateRangeToIntervalsRec(start: Instant, end: Instant, size: TemporalAmount): List<GlowInterval> {
        tailrec fun inner(
            start: Instant,
            end: Instant,
            size: TemporalAmount,
            intervals: List<GlowInterval>
        ): List<GlowInterval> =
            if (start.plus(size) < end) {
                val endOfInterval = start.plus(size)
                inner(endOfInterval, end, size, intervals + GlowInterval(start, endOfInterval))
            } else {
                intervals + GlowInterval(start, end)
            }

        return inner(start, end, size, mutableListOf())
    }

    private fun getElectricConsumptionResource(): Resource =
        client.getResources().first { resource -> resource.name == "electricity consumption" }

    private fun getReadings(resourceId: String, interval: GlowInterval): ReadingsResponse =
        client.getReadings(
            resourceId,
            "PT30M",
            "sum",
            dateTimeFormatter.format(interval.start),
            dateTimeFormatter.format(interval.end)
        )

    companion object {
        const val MAX_DAYS = 10
    }
}
