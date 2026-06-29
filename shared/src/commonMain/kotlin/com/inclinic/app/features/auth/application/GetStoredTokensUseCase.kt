package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.port.TokenStorage
import kotlinx.coroutines.withContext

/**
 * Reads stored tokens from secure storage.
 * Pure read — does NOT mutate storage or trigger any network call.
 * Returns null when no session exists (fresh install or after logout).
 */
class GetStoredTokensUseCase(
    private val tokenStorage: TokenStorage,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): AuthTokens? = withContext(dispatchers.io) {
        tokenStorage.load()
    }
}
