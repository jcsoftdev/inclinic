package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import kotlinx.coroutines.withContext

class ActivateUseCase(
    private val remote: AuthRemoteDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(token: String): Result<Unit> = withContext(dispatchers.io) {
        remote.activate(token)
    }
}
