package com.inclinic.app.features.auth.fakes

import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.port.TokenStorage

class FakeTokenStorage : TokenStorage {

    private var stored: AuthTokens? = null
    private var storedUser: AuthUser? = null

    var saveCallCount = 0
    var loadCallCount = 0
    var clearCallCount = 0

    override suspend fun save(tokens: AuthTokens) {
        saveCallCount++
        stored = tokens
    }

    override suspend fun load(): AuthTokens? {
        loadCallCount++
        return stored
    }

    override suspend fun clear() {
        clearCallCount++
        stored = null
        storedUser = null
    }

    override suspend fun saveUser(user: AuthUser) {
        storedUser = user
    }

    override suspend fun loadUser(): AuthUser? = storedUser

    val current: AuthTokens? get() = stored
}
