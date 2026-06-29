package com.inclinic.app.features.auth.infrastructure.remote

import com.inclinic.app.core.model.Specialty
import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.infrastructure.remote.dto.AuthErrorDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.DoctorFreelanceRequestDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.LoginRequestDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.LoginResponseDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.PatientRegisterRequestDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.RegisterRequestDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.TwoFactorVerifyRequestDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.TwoFactorVerifyResponseDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Ktor-based implementation of [AuthRemoteDataSource].
 *
 * Error mapping strategy:
 * - 2xx: deserialise body → [LoginResponseDto]. SerializationException → [AuthError.MalformedResponse].
 * - 4xx/5xx: attempt to deserialise [AuthErrorDto] body (code field drives fine-grained mapping).
 * - IOException / connect failure: [AuthError.NetworkError].
 * - Anything unexpected: [AuthError.Unknown].
 *
 * @param client Pre-configured [HttpClient] (ContentNegotiation must be installed).
 * @param baseUrl Base URL for the API (e.g. "https://dev.api.inclinic.local").
 */
class KtorAuthRemoteDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
    private val authenticatedClient: HttpClient? = null,
) : AuthRemoteDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun login(request: LoginRequestDto): Result<LoginResponseDto> = runCatching {
        val response: HttpResponse = client.post {
            url("$baseUrl/api/auth/login")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val status = response.status.value
        if (status in 200..299) {
            try {
                response.body<ApiEnvelope<LoginResponseDto>>().data ?: throw AuthError.MalformedResponse
            } catch (e: SerializationException) {
                throw AuthError.MalformedResponse
            } catch (e: Exception) {
                throw AuthError.MalformedResponse
            }
        } else {
            val errorBody = tryParseErrorBody(response)
            throw mapHttpToAuthError(status, errorBody)
        }
    }.recoverCatching { cause ->
        when (cause) {
            is AuthError -> throw cause
            is SerializationException -> throw AuthError.MalformedResponse
            is IOException -> throw AuthError.NetworkError
            else -> if (cause.cause is IOException) throw AuthError.NetworkError
                    else throw AuthError.Unknown(cause)
        }
    }

    override suspend fun verifyTwoFactor(partialToken: String, code: String): Result<TwoFactorVerifyResponseDto> =
        runCatching {
            val response: HttpResponse = client.post {
                url("$baseUrl/api/auth/2fa/verify")
                contentType(ContentType.Application.Json)
                setBody(TwoFactorVerifyRequestDto(partialToken = partialToken, code = code))
            }
            val status = response.status.value
            if (status in 200..299) {
                try {
                    response.body<ApiEnvelope<TwoFactorVerifyResponseDto>>().data
                        ?: throw AuthError.MalformedResponse
                } catch (e: SerializationException) {
                    throw AuthError.MalformedResponse
                } catch (e: Exception) {
                    throw AuthError.MalformedResponse
                }
            } else {
                val errorBody = tryParseErrorBody(response)
                throw mapHttpToAuthError(status, errorBody)
            }
        }.recoverCatching(::wrapException)

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        specialtyId: String?,
    ): Result<Unit> = runCatching {
        val response: HttpResponse = client.post {
            url("$baseUrl/api/auth/register")
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(name = name, email = email, password = password, role = role, specialtyId = specialtyId))
        }
        val status = response.status.value
        if (status !in 200..299) {
            val errorBody = tryParseErrorBody(response)
            throw mapHttpToAuthError(status, errorBody)
        }
    }.recoverCatching(::wrapException)

    override suspend fun registerPatient(
        firstName: String,
        lastName: String,
        email: String,
        phone: String?,
        password: String,
    ): Result<Unit> = runCatching {
        val response: HttpResponse = client.post {
            url("$baseUrl/api/patients/register")
            contentType(ContentType.Application.Json)
            setBody(
                PatientRegisterRequestDto(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password,
                    phone = phone?.takeIf { it.isNotBlank() },
                )
            )
        }
        val status = response.status.value
        if (status !in 200..299) {
            val errorBody = tryParseErrorBody(response)
            throw mapHttpToAuthError(status, errorBody)
        }
    }.recoverCatching(::wrapException)

    override suspend fun registerFreelanceDoctor(
        request: DoctorFreelanceRequestDto,
    ): Result<Unit> = runCatching {
        val response: HttpResponse = client.post {
            url("$baseUrl/api/doctors/freelance")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val status = response.status.value
        if (status !in 200..299) {
            val errorBody = tryParseErrorBody(response)
            throw mapHttpToAuthError(status, errorBody)
        }
    }.recoverCatching(::wrapException)

    override suspend fun activate(token: String): Result<Unit> = runCatching {
        val response: HttpResponse = client.post {
            url("$baseUrl/api/auth/activate")
            contentType(ContentType.Application.Json)
            setBody(mapOf("token" to token))
        }
        val status = response.status.value
        if (status !in 200..299) {
            val errorBody = tryParseErrorBody(response)
            throw mapHttpToAuthError(status, errorBody)
        }
    }.recoverCatching(::wrapException)

    override suspend fun resendActivation(email: String): Result<Unit> = runCatching {
        val response: HttpResponse = client.post {
            url("$baseUrl/api/auth/resend-activation")
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email))
        }
        val status = response.status.value
        if (status !in 200..299) {
            val errorBody = tryParseErrorBody(response)
            throw mapHttpToAuthError(status, errorBody)
        }
    }.recoverCatching(::wrapException)

    override suspend fun forgotPassword(email: String): Result<Unit> = runCatching {
        val response: HttpResponse = client.post {
            url("$baseUrl/api/auth/forgot-password")
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email))
        }
        val status = response.status.value
        if (status !in 200..299) {
            val errorBody = tryParseErrorBody(response)
            throw mapHttpToAuthError(status, errorBody)
        }
    }.recoverCatching(::wrapException)

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> = runCatching {
        val response: HttpResponse = client.post {
            url("$baseUrl/api/auth/reset-password")
            contentType(ContentType.Application.Json)
            setBody(mapOf("token" to token, "newPassword" to newPassword))
        }
        val status = response.status.value
        if (status !in 200..299) {
            val errorBody = tryParseErrorBody(response)
            throw mapHttpToAuthError(status, errorBody)
        }
    }.recoverCatching(::wrapException)

    override suspend fun refresh(refreshToken: String): Result<LoginResponseDto> = runCatching {
        val response: HttpResponse = client.post {
            url("$baseUrl/api/auth/refresh")
            contentType(ContentType.Application.Json)
            setBody(mapOf("refreshToken" to refreshToken))
        }
        val status = response.status.value
        if (status in 200..299) {
            try {
                response.body<ApiEnvelope<LoginResponseDto>>().data ?: throw AuthError.MalformedResponse
            } catch (_: Exception) {
                throw AuthError.MalformedResponse
            }
        } else {
            val errorBody = tryParseErrorBody(response)
            throw mapHttpToAuthError(status, errorBody)
        }
    }.recoverCatching(::wrapException)

    override suspend fun getMe(): Result<UserDto> = runCatching {
        val appClient = authenticatedClient ?: error("Authenticated client not provided")
        val response: HttpResponse = appClient.get {
            url("$baseUrl/api/users/me")
        }
        val status = response.status.value
        if (status in 200..299) {
            try {
                response.body<ApiEnvelope<UserDto>>().data ?: throw AuthError.MalformedResponse
            } catch (_: Exception) {
                throw AuthError.MalformedResponse
            }
        } else {
            val errorBody = tryParseErrorBody(response)
            throw mapHttpToAuthError(status, errorBody)
        }
    }.recoverCatching(::wrapException)

    override suspend fun getSpecialties(): Result<List<Specialty>> = runCatching {
        val response: HttpResponse = client.get {
            url("$baseUrl/api/specialties")
        }
        val status = response.status.value
        if (status in 200..299) {
            try {
                response.body<ApiEnvelope<List<Specialty>>>().data ?: emptyList()
            } catch (_: Exception) {
                throw AuthError.MalformedResponse
            }
        } else {
            val errorBody = tryParseErrorBody(response)
            throw mapHttpToAuthError(status, errorBody)
        }
    }.recoverCatching(::wrapException)

    private suspend fun tryParseErrorBody(response: HttpResponse): AuthErrorDto? {
        return try {
            val text = response.bodyAsText()
            if (text.isBlank()) null else json.decodeFromString<AuthErrorDto>(text)
        } catch (_: Exception) {
            null
        }
    }

    private fun wrapException(cause: Throwable): Nothing = when (cause) {
        is AuthError -> throw cause
        is SerializationException -> throw AuthError.MalformedResponse
        is IOException -> throw AuthError.NetworkError
        else -> if (cause.cause is IOException) throw AuthError.NetworkError
                else throw AuthError.Unknown(cause)
    }
}
