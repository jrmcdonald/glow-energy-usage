package com.jrmcdonald.energy.usage.external.provider.hildebrand

import com.jrmcdonald.energy.usage.core.model.UsageEntry
import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.ReadingData
import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.ReadingsResponse
import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.Resource
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant.now
import java.time.Instant.parse
import java.time.temporal.ChronoUnit

@ExtendWith(MockKExtension::class)
class GlowProviderServiceTest {

    @MockK
    private lateinit var glowClient: GlowClient

    private lateinit var service: GlowProviderService

    @BeforeEach
    fun beforeEach() {
        service = GlowProviderService(glowClient)
    }

    @AfterEach
    fun afterEach() {
        confirmVerified(glowClient)
    }

    @Test
    fun `Should throw error for invalid date range`() {
        val from = now()
        val to = from.minus(1, ChronoUnit.DAYS)

        val exception = assertThrows<IllegalArgumentException> { service.getEnergyUsage(from, to) }

        with(exception) {
            message shouldBe "Invalid date range supplied, from date should be before to date"
        }
    }

    @Test
    fun `Should get energy readings for date range less than 10 days`() {
        val expectedResourcesResponse = listOf(
            Resource("123", "gas consumption"),
            Resource("456", "electricity consumption")
        )
        val expectedReading = ReadingData(now(), 123.456F)
        val expectedReadingsResponse = ReadingsResponse(listOf(expectedReading))

        every { glowClient.getResources() } returns expectedResourcesResponse
        every {
            glowClient.getReadings(
                "456",
                "PT30M",
                "sum",
                "2022-01-21T00:00:00",
                "2022-01-30T00:00:00"
            )
        } returns expectedReadingsResponse

        val actualUsageEntries = service.getEnergyUsage(parse("2022-01-21T00:00:00Z"), parse("2022-01-30T00:00:00Z"))

        with(actualUsageEntries) {
            size shouldBe 1
            shouldContain(UsageEntry(expectedReading.timestamp, expectedReading.usage))
        }

        verify(exactly = 1) {
            glowClient.getResources()
            glowClient.getReadings("456", "PT30M", "sum", "2022-01-21T00:00:00", "2022-01-30T00:00:00")
        }
    }

    @Test
    fun `Should get energy readings for date range equal to 10 days`() {
        val expectedResourcesResponse = listOf(
            Resource("123", "gas consumption"),
            Resource("456", "electricity consumption")
        )
        val expectedReading = ReadingData(now(), 123.456F)
        val expectedReadings = ReadingsResponse(listOf(expectedReading))

        every { glowClient.getResources() } returns expectedResourcesResponse
        every {
            glowClient.getReadings(
                "456",
                "PT30M",
                "sum",
                "2022-01-20T00:00:00",
                "2022-01-30T00:00:00"
            )
        } returns expectedReadings

        val actualUsageEntries = service.getEnergyUsage(parse("2022-01-20T00:00:00Z"), parse("2022-01-30T00:00:00Z"))

        with(actualUsageEntries) {
            size shouldBe 1
            shouldContain(UsageEntry(expectedReading.timestamp, expectedReading.usage))
        }

        verify(exactly = 1) {
            glowClient.getResources()
            glowClient.getReadings("456", "PT30M", "sum", "2022-01-20T00:00:00", "2022-01-30T00:00:00")
        }
    }

    @Test
    fun `Should get energy readings for date range more than 10 days`() {
        val expectedResourcesResponse = listOf(
            Resource("123", "gas consumption"),
            Resource("456", "electricity consumption")
        )
        val expectedFirstReading = ReadingData(now(), 654.321F)
        val expectedFirstReadingsResponse = ReadingsResponse(listOf(expectedFirstReading))
        val expectedSecondReading = ReadingData(now(), 123.456F)
        val expectedSecondReadingResponse = ReadingsResponse(listOf(expectedSecondReading))

        every { glowClient.getResources() } returns expectedResourcesResponse
        every {
            glowClient.getReadings(
                "456",
                "PT30M",
                "sum",
                "2022-01-15T00:00:00",
                "2022-01-25T00:00:00"
            )
        } returns expectedFirstReadingsResponse
        every {
            glowClient.getReadings(
                "456",
                "PT30M",
                "sum",
                "2022-01-25T00:00:00",
                "2022-01-30T00:00:00"
            )
        } returns expectedSecondReadingResponse

        val actualUsageEntries = service.getEnergyUsage(parse("2022-01-15T00:00:00Z"), parse("2022-01-30T00:00:00Z"))

        with(actualUsageEntries) {
            size shouldBe 2
            shouldContain(UsageEntry(expectedFirstReading.timestamp, expectedFirstReading.usage))
            shouldContain(UsageEntry(expectedSecondReading.timestamp, expectedSecondReading.usage))
        }

        verify(exactly = 1) {
            glowClient.getResources()
            glowClient.getReadings("456", "PT30M", "sum", "2022-01-15T00:00:00", "2022-01-25T00:00:00")
            glowClient.getReadings("456", "PT30M", "sum", "2022-01-25T00:00:00", "2022-01-30T00:00:00")
        }
    }
}
