package com.jrmcdonald.energy.usage.external.provider.hildebrand

import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.ReadingData
import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.Resource
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.http4k.client.OkHttp
import org.http4k.cloudnative.RemoteRequestFailed
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant

class GlowClientTest {

    private lateinit var glowMock: MockWebServer
    private lateinit var glowClient: GlowClient

    @BeforeEach
    fun beforeEach() {
        glowMock = MockWebServer()
        glowMock.start()

        val okhttp = ClientFilters.SetHostFrom(Uri.of("http://${glowMock.hostName}:${glowMock.port}")).then(OkHttp())
        glowClient = GlowClient(
            okhttp,
            GlowConfig("username-value", "password-value", "b0f1b774-a586-4f72-9edd-27ead8aa7a8d")
        )
    }

    @AfterEach
    fun afterEach() {
        glowMock.shutdown()
    }

    @Test
    fun `Should get resources`() {
        val tokenResponse = """
            {
                "token": "eyJ...."
            }
        """.trimIndent()

        val jsonResponse = """
            [
                {"name": "gas consumption", "resourceId": "abcd"},
                {"name": "electric consumption", "resourceId": "efgh"}
            ]
        """.trimIndent()

        glowMock.enqueue(MockResponse().setBody(tokenResponse))
        glowMock.enqueue(MockResponse().setBody(jsonResponse))

        val actualResources = glowClient.getResources()

        actualResources shouldHaveSize 2
        actualResources shouldContain Resource("abcd", "gas consumption")
        actualResources shouldContain Resource("efgh", "electric consumption")

        glowMock.requestCount shouldBe 2

        verifyTokenRequest()

        with(glowMock.takeRequest()) {
            method shouldBe "GET"
            path shouldBe "/api/v0-1/resource"
            headers["applicationId"] shouldBe "b0f1b774-a586-4f72-9edd-27ead8aa7a8d"
        }
    }

    @Test
    fun `Should throw RemoteRequestFailed exception when get readings call fails`() {
        val tokenResponse = """
            {
                "token": "eyJ...."
            }
        """.trimIndent()

        glowMock.enqueue(MockResponse().setBody(tokenResponse))
        glowMock.enqueue(MockResponse().setResponseCode(500))

        val exception = shouldThrowExactly<RemoteRequestFailed> {
            glowClient.getResources()
        }

        with(exception) {
            status shouldBe Status.INTERNAL_SERVER_ERROR
            message shouldContain "/api/v0-1/resource"
            message shouldContain "Glow API request failed"
        }

        glowMock.requestCount shouldBe 2

        verifyTokenRequest()

        with(glowMock.takeRequest()) {
            method shouldBe "GET"
            path shouldBe "/api/v0-1/resource"
            headers["applicationId"] shouldBe "b0f1b774-a586-4f72-9edd-27ead8aa7a8d"
        }
    }

    @Test
    fun `Should get readings`() {
        val tokenResponse = """
            {
                "token": "eyJ...."
            }
        """.trimIndent()

        val jsonResponse = """
            {
                "data": [
                    [
                        1642464000,
                        0.091
                    ],
                    [
                        1642465800,
                        0.102
                    ]
                ]
            }
        """.trimIndent()

        glowMock.enqueue(MockResponse().setBody(tokenResponse))
        glowMock.enqueue(MockResponse().setBody(jsonResponse))

        val expectedResourceId = "123"
        val expectedPeriod = "PT30M"
        val expectedFunction = "sum"
        val expectedFrom = "2022-01-24T00:00:00"
        val expectedTo = "2022-01-28T00:00:00"

        with(glowClient.getReadings(expectedResourceId, expectedPeriod, expectedFunction, expectedFrom, expectedTo)) {
            data shouldHaveSize 2
            data shouldContain ReadingData(Instant.ofEpochSecond(1642464000), 0.091.toFloat())
            data shouldContain ReadingData(Instant.ofEpochSecond(1642465800), 0.102.toFloat())
        }

        glowMock.requestCount shouldBe 2

        verifyTokenRequest()

        with(glowMock.takeRequest()) {
            method shouldBe "GET"
            path shouldBe EXPECTED_REQUEST_FMT.format(
                expectedResourceId,
                expectedPeriod,
                expectedFunction,
                URLEncoder.encode(expectedFrom, StandardCharsets.UTF_8),
                URLEncoder.encode(expectedTo, StandardCharsets.UTF_8)
            )
            headers["applicationId"] shouldBe "b0f1b774-a586-4f72-9edd-27ead8aa7a8d"
        }
    }

    @Test
    fun `Should  throw RemoteRequestFailed exception when get readings call fails`() {
        val tokenResponse = """
            {
                "token": "eyJ...."
            }
        """.trimIndent()

        glowMock.enqueue(MockResponse().setBody(tokenResponse))
        glowMock.enqueue(MockResponse().setResponseCode(500))

        val expectedResourceId = "123"
        val expectedPeriod = "PT30M"
        val expectedFunction = "sum"
        val expectedFrom = "2022-01-24T00:00:00"
        val expectedTo = "2022-01-28T00:00:00"

        val exception = shouldThrowExactly<RemoteRequestFailed> {
            glowClient.getReadings(expectedResourceId, expectedPeriod, expectedFunction, expectedFrom, expectedTo)
        }

        with(exception) {
            status shouldBe Status.INTERNAL_SERVER_ERROR
            message shouldContain "/api/v0-1/resource"
            message shouldContain "Glow API request failed"
        }
        glowMock.requestCount shouldBe 2

        verifyTokenRequest()

        with(glowMock.takeRequest()) {
            method shouldBe "GET"
            path shouldBe EXPECTED_REQUEST_FMT.format(
                expectedResourceId,
                expectedPeriod,
                expectedFunction,
                URLEncoder.encode(expectedFrom, StandardCharsets.UTF_8),
                URLEncoder.encode(expectedTo, StandardCharsets.UTF_8)
            )
            headers["applicationId"] shouldBe "b0f1b774-a586-4f72-9edd-27ead8aa7a8d"
        }
    }

    private fun verifyTokenRequest() =
        with(glowMock.takeRequest()) {
            method shouldBe "POST"
            path shouldBe "/api/v0-1/auth"
            headers["applicationId"] shouldBe "b0f1b774-a586-4f72-9edd-27ead8aa7a8d"
            body.readUtf8() shouldEqualJson """
                {
                    "username": "username-value",
                    "password": "password-value"
                }
            """.trimIndent()
        }

    companion object {
        const val EXPECTED_REQUEST_FMT = "/api/v0-1/resource/%s/readings?period=%s&function=%s&from=%s&to=%s"
    }
}
