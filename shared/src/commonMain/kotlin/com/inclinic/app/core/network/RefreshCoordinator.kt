package com.inclinic.app.core.network

import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.port.TokenStorage
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RefreshCoordinator(
    private val tokenStorage: TokenStorage,
    private val sessionEvents: SessionEvents,
    private val refreshCall: suspend (refreshToken: String) -> AuthTokens?,
) {
    private val mutex = Mutex()

    suspend fun refresh(oldTokens: BearerTokens?): BearerTokens? = mutex.withLock {
        val current = tokenStorage.load() ?: run {
            sessionEvents.emitExpired()
            return@withLock null
        }
        // Another coroutine already refreshed — reuse new token.
        if (oldTokens != null && current.accessToken != oldTokens.accessToken) {
            return@withLock BearerTokens(current.accessToken, current.refreshToken)
        }
        val newTokens = try { refreshCall(current.refreshToken) } catch (_: Exception) { null }
        if (newTokens != null) {
            tokenStorage.save(newTokens)
            BearerTokens(newTokens.accessToken, newTokens.refreshToken)
        } else {
            tokenStorage.clear()
            sessionEvents.emitExpired()
            null
        }
    }
}
