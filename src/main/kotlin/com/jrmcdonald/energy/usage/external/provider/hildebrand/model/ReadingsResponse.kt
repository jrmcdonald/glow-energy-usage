package com.jrmcdonald.energy.usage.external.provider.hildebrand.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.jrmcdonald.energy.usage.core.model.UsageEntry
import java.time.Instant

data class ReadingsResponse(
    val data: List<ReadingData>
)

fun ReadingsResponse.toUsageEntries() =
    data.map { reading ->
        with(reading) { UsageEntry(timestamp, usage) }
    }

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder("timestamp", "usage")
data class ReadingData(
    @JsonDeserialize(using = LongInstantDeserializer::class)
    val timestamp: Instant,
    val usage: Float
)

object LongInstantDeserializer : JsonDeserializer<Instant>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant {
        return Instant.ofEpochSecond(p.readValueAsTree<JsonNode>().asLong())
    }
}
