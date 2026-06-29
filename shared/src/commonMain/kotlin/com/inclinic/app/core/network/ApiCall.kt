package com.inclinic.app.core.network

import com.inclinic.app.core.error.ApiError
import com.inclinic.app.core.error.ApiResult
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

suspend fun <T> runApi(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Ok(block())
} catch (e: ClientRequestException) {
    val envelope = tryParseErrorEnvelope(e.response.bodyAsText())
    when (e.response.status) {
        HttpStatusCode.BadRequest -> ApiResult.Err(ApiError.BadRequest(envelope?.error, envelope?.code))
        HttpStatusCode.Unauthorized -> ApiResult.Err(ApiError.Unauthorized)
        HttpStatusCode.Forbidden -> ApiResult.Err(ApiError.Forbidden)
        HttpStatusCode.NotFound -> ApiResult.Err(ApiError.NotFound)
        HttpStatusCode.Conflict -> ApiResult.Err(ApiError.Conflict(envelope?.error, envelope?.code))
        else -> ApiResult.Err(ApiError.Server(e.response.status.value, envelope?.error))
    }
} catch (e: ServerResponseException) {
    ApiResult.Err(ApiError.Server(e.response.status.value))
} catch (e: HttpRequestTimeoutException) {
    ApiResult.Err(ApiError.Timeout)
} catch (e: IOException) {
    ApiResult.Err(ApiError.Network)
} catch (e: SerializationException) {
    ApiResult.Err(ApiError.Server(0, "Serialization error: ${e.message}"))
} catch (e: Exception) {
    if (e.cause is IOException) ApiResult.Err(ApiError.Network)
    else ApiResult.Err(ApiError.Server(0, e.message))
}

private fun tryParseErrorEnvelope(text: String): ApiErrorEnvelope? = try {
    if (text.isBlank()) null else json.decodeFromString<ApiErrorEnvelope>(text)
} catch (_: Exception) {
    null
}
