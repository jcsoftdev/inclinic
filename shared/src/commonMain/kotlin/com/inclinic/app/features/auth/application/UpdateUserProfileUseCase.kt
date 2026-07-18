package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.error.ApiError
import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.core.network.ApiErrorEnvelope
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.UserRole
import com.inclinic.app.features.auth.infrastructure.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class UpdateUserProfileRequestDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
)

/**
 * PATCH /api/users/me — updates firstName/lastName/phone of the authenticated user.
 *
 * Mirrors [GetCurrentUserUseCase]'s self-contained [HttpClient] call pattern rather than
 * going through [com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource] —
 * that port's own `getMe()` is unreachable via DI because the singleton is registered
 * without an `authenticatedClient` (see [KtorAuthRemoteDataSource.getMe]'s `error(...)` guard);
 * [GetCurrentUserUseCase] already works around this the same way.
 *
 * On a 400 (Zod validation failure), the backend's `{ error, code }` body is surfaced via
 * [ApiError.BadRequest] so [com.inclinic.app.core.error.toUserMessage] renders the real
 * validation message instead of a generic fallback.
 */
class UpdateUserProfileUseCase(
    private val authenticatedClient: HttpClient,
    private val baseUrl: String,
    private val dispatchers: AppDispatchers,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        phone: String?,
    ): Result<AuthUser> = withContext(dispatchers.io) {
        runCatching {
            val response: HttpResponse = authenticatedClient.patch {
                url("$baseUrl/api/users/me")
                contentType(ContentType.Application.Json)
                setBody(UpdateUserProfileRequestDto(firstName = firstName, lastName = lastName, phone = phone))
            }
            when (val status = response.status.value) {
                in 200..299 -> {
                    val dto = response.body<ApiEnvelope<UserDto>>().data
                        ?: throw AuthError.MalformedResponse
                    dto.toAuthUser()
                }
                400 -> {
                    val envelope = tryParseErrorEnvelope(response.bodyAsText())
                    throw ApiError.BadRequest(envelope?.error, envelope?.code)
                }
                else -> throw AuthError.ServerError(status)
            }
        }
    }

    private fun tryParseErrorEnvelope(text: String): ApiErrorEnvelope? = try {
        if (text.isBlank()) null else json.decodeFromString(text)
    } catch (_: Exception) {
        null
    }

    private fun UserDto.toAuthUser() = AuthUser(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.PATIENT),
        doctorId = doctorId,
        patientId = patientId,
        phone = phone,
    )
}
