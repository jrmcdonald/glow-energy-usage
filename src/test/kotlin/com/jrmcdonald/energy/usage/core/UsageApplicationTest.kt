package com.jrmcdonald.energy.usage.core

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.MissingOption
import com.jrmcdonald.energy.usage.core.model.UsageEntry
import com.jrmcdonald.energy.usage.core.service.CsvOutputService
import com.jrmcdonald.energy.usage.core.service.EnergyProviderService
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant.now
import java.time.Instant.parse

@ExtendWith(MockKExtension::class)
class UsageApplicationTest {

    @MockK
    private lateinit var energyProviderService: EnergyProviderService

    @MockK
    private lateinit var csvOutputService: CsvOutputService

    private lateinit var usageApplication: UsageApplication

    @BeforeEach
    fun beforeEach() {
        usageApplication = UsageApplication(energyProviderService, csvOutputService)
    }

    @AfterEach
    fun afterEach() {
        confirmVerified(energyProviderService, csvOutputService)
    }

    @ParameterizedTest
    @MethodSource("validArguments")
    fun `Should handle valid arguments`(args: Array<String>) {
        val from = parse("2022-01-01T00:00:00Z")
        val to = parse("2022-01-31T00:00:00Z")

        val expectedUsageEntries = listOf(
            UsageEntry(now(), 123.456F),
            UsageEntry(now(), 654.321F)
        )

        every { energyProviderService.getEnergyUsage(from, to) } returns expectedUsageEntries
        every { csvOutputService.output(expectedUsageEntries) } returns Unit

        usageApplication.main(args)

        verifySequence {
            energyProviderService.getEnergyUsage(from, to)
            csvOutputService.output(expectedUsageEntries)
        }
    }

    @ParameterizedTest
    @MethodSource("requiredArgsWithExpectedExceptions")
    fun `Should fail if required arguments are not supplied`(args: Array<String>, expectedMessage: String) {
        with(assertThrows<MissingOption> { usageApplication.parse(args) }) {
            message shouldBe expectedMessage
        }

        verify(exactly = 0) {
            energyProviderService.getEnergyUsage(any(), any())
            csvOutputService.output(any())
        }
    }

    @ParameterizedTest
    @MethodSource("invalidArgsWithExpectedExceptions")
    fun `Should fail if supplied arguments are invalid`(args: Array<String>, expectedMessage: String) {
        with(assertThrows<BadParameterValue> { usageApplication.parse(args) }) {
            message shouldBe expectedMessage
        }

        verify(exactly = 0) {
            energyProviderService.getEnergyUsage(any(), any())
            csvOutputService.output(any())
        }
    }

    companion object {
        @JvmStatic
        fun validArguments() = listOf(
            Arguments.of(arrayOf("-f", "2022-01-01", "-t", "2022-01-31")),
            Arguments.of(arrayOf("--from", "2022-01-01", "--to", "2022-01-31")),
            Arguments.of(arrayOf("--from", "2022-01-01", "-t", "2022-01-31")),
            Arguments.of(arrayOf("-f", "2022-01-01", "--to", "2022-01-31"))
        )

        @JvmStatic
        fun requiredArgsWithExpectedExceptions() = listOf(
            Arguments.of(arrayOf<String>(), """Missing option "--from""""),
            Arguments.of(arrayOf("--from", "2022-01-01"), """Missing option "--to""""),
            Arguments.of(arrayOf("--to", "2022-01-31"), """Missing option "--from"""")
        )

        @JvmStatic
        fun invalidArgsWithExpectedExceptions() = listOf(
            Arguments.of(
                arrayOf("--from", "01-01-2022"),
                """Invalid value for "--from": '01-01-2022' could not be parsed, expected format is 'yyyy-MM-dd'"""
            ),
            Arguments.of(
                arrayOf("--to", "31-01-2022"),
                """Invalid value for "--to": '31-01-2022' could not be parsed, expected format is 'yyyy-MM-dd'"""
            )
        )
    }
}
