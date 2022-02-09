package com.jrmcdonald.energy.usage.external.provider.hildebrand

import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.AuthRequest
import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.AuthResponse
import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.ReadingsResponse
import com.jrmcdonald.energy.usage.external.provider.hildebrand.model.Resource
import org.http4k.cloudnative.RemoteRequestFailed
import org.http4k.core.Body
import org.http4k.core.Credentials
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.UriTemplate.Companion.from
import org.http4k.core.then
import org.http4k.format.Jackson.auto

class GlowClient(handler: HttpHandler, config: GlowConfig) {

    private val http = GlowClientFilters.applicationId(config.applicationId).then(handler)
    private val authenticatedHttp =
        GlowClientFilters.tokenFlow(Credentials(config.username, config.password), http)
            .then(http)

    private val readingsResponse = Body.auto<ReadingsResponse>().toLens()
    private val resourcesResponse = Body.auto<List<Resource>>().toLens()

    private companion object {
        const val RESOURCE_ENDPOINT = "/api/v0-1/resource"
        const val READINGS_ENDPOINT = "/api/v0-1/resource/{id}/readings"
    }

    fun getResources(): List<Resource> =
        with(authenticatedHttp(Request(GET, Uri.of(RESOURCE_ENDPOINT)))) {
            when (status) {
                Status.OK -> resourcesResponse(this)
                else -> throw RemoteRequestFailed(status, "Glow API request failed", Uri.of(RESOURCE_ENDPOINT))
            }
        }

    fun getReadings(id: String, period: String, function: String, from: String, to: String): ReadingsResponse =
        with(
            authenticatedHttp(
                Request(GET, from(READINGS_ENDPOINT).generate(mapOf("id" to id)))
                    .query("period", period)
                    .query("function", function)
                    .query("from", from)
                    .query("to", to)
            )
        ) {
            when (status) {
                Status.OK -> readingsResponse(this)
                else -> throw RemoteRequestFailed(status, "Glow API request failed", Uri.of(READINGS_ENDPOINT))
            }
        }
}

object GlowClientFilters {
    private val authRequest = Body.auto<AuthRequest>().toLens()
    private val authResponse = Body.auto<AuthResponse>().toLens()

    fun applicationId(applicationId: String): Filter = Filter { next ->
        { req ->
            next(req.header("applicationId", applicationId))
        }
    }

    fun tokenFlow(credentials: Credentials, backend: HttpHandler): Filter {
        val tokenProvider: () -> String? = {
            val request = authRequest(
                AuthRequest(credentials.user, credentials.password),
                Request(Method.POST, "/api/v0-1/auth")
            )

            backend.invoke(request)
                .takeIf { it.status.successful }
                ?.let { authResponse(it).token }
        }

        return CustomTokenAuth(tokenProvider)
    }

    object CustomTokenAuth {
        operator fun invoke(provider: () -> String?): Filter = Filter { next ->
            { req ->
                provider()
                    ?.let {
                        next(req.header("token", it))
                    }
                    ?: Response(Status.UNAUTHORIZED)
            }
        }
    }
}
