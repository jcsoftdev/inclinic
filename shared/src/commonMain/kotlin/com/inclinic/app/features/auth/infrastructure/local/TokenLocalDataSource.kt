package com.inclinic.app.features.auth.infrastructure.local

import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.UserRole
import com.inclinic.app.features.auth.core.port.TokenStorage
import com.russhwolf.settings.Settings

class TokenLocalDataSource(private val settings: Settings) : TokenStorage {

    override suspend fun save(tokens: AuthTokens) {
        settings.putString(KEY_REFRESH, tokens.refreshToken)
        settings.putString(KEY_ACCESS, tokens.accessToken)
    }

    override suspend fun load(): AuthTokens? {
        val access = settings.getStringOrNull(KEY_ACCESS)
        val refresh = settings.getStringOrNull(KEY_REFRESH)
        return when {
            access != null && refresh != null -> AuthTokens(
                accessToken = access,
                refreshToken = refresh,
            )
            else -> {
                settings.remove(KEY_ACCESS)
                settings.remove(KEY_REFRESH)
                null
            }
        }
    }

    override suspend fun clear() {
        settings.remove(KEY_ACCESS)
        settings.remove(KEY_REFRESH)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_EMAIL)
        settings.remove(KEY_FIRST_NAME)
        settings.remove(KEY_LAST_NAME)
        settings.remove(KEY_ROLE)
        settings.remove(KEY_PATIENT_ID)
        settings.remove(KEY_DOCTOR_ID)
    }

    override suspend fun saveUser(user: AuthUser) {
        settings.putString(KEY_USER_ID, user.id)
        settings.putString(KEY_EMAIL, user.email)
        settings.putString(KEY_FIRST_NAME, user.firstName)
        settings.putString(KEY_LAST_NAME, user.lastName)
        settings.putString(KEY_ROLE, user.role.name)
        user.patientId?.let { settings.putString(KEY_PATIENT_ID, it) } ?: settings.remove(KEY_PATIENT_ID)
        user.doctorId?.let { settings.putString(KEY_DOCTOR_ID, it) } ?: settings.remove(KEY_DOCTOR_ID)
    }

    override suspend fun loadUser(): AuthUser? {
        val id = settings.getStringOrNull(KEY_USER_ID) ?: return null
        val email = settings.getStringOrNull(KEY_EMAIL) ?: return null
        val role = settings.getStringOrNull(KEY_ROLE)?.let {
            runCatching { UserRole.valueOf(it) }.getOrNull()
        } ?: return null
        return AuthUser(
            id = id,
            email = email,
            firstName = settings.getStringOrNull(KEY_FIRST_NAME) ?: "",
            lastName = settings.getStringOrNull(KEY_LAST_NAME) ?: "",
            role = role,
            patientId = settings.getStringOrNull(KEY_PATIENT_ID),
            doctorId = settings.getStringOrNull(KEY_DOCTOR_ID),
        )
    }

    private companion object {
        const val KEY_ACCESS = "auth.accessToken"
        const val KEY_REFRESH = "auth.refreshToken"
        const val KEY_USER_ID = "auth.userId"
        const val KEY_EMAIL = "auth.email"
        const val KEY_FIRST_NAME = "auth.firstName"
        const val KEY_LAST_NAME = "auth.lastName"
        const val KEY_ROLE = "auth.role"
        const val KEY_PATIENT_ID = "auth.patientId"
        const val KEY_DOCTOR_ID = "auth.doctorId"
    }
}
