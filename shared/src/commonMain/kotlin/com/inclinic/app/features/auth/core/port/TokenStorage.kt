package com.inclinic.app.features.auth.core.port

import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser

interface TokenStorage {
    suspend fun save(tokens: AuthTokens)
    suspend fun load(): AuthTokens?
    suspend fun clear()

    suspend fun saveUser(user: AuthUser)
    suspend fun loadUser(): AuthUser?
}
