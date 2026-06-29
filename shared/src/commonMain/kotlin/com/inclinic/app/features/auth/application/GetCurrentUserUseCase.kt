package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.UserRole
import com.inclinic.app.features.auth.infrastructure.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.withContext

class GetCurrentUserUseCase(
    private val authenticatedClient: HttpClient,
    private val baseUrl: String,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<AuthUser> = withContext(dispatchers.io) {
        runCatching {
            val response = authenticatedClient.get {
                url("$baseUrl/api/users/me")
            }
            val status = response.status.value
            if (status in 200..299) {
                val dto = response.body<ApiEnvelope<UserDto>>().data
                    ?: throw AuthError.MalformedResponse
                dto.toAuthUser()
            } else {
                throw AuthError.Unauthorized
            }
        }
    }

    private fun UserDto.toAuthUser() = AuthUser(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.PATIENT),
        doctorId = doctorId,
        patientId = patientId,
    )
}
