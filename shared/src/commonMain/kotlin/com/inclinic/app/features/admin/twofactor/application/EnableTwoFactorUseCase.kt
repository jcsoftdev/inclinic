package com.inclinic.app.features.admin.twofactor.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class EnableTwoFactorUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(code: String): Result<Unit> =
        withContext(dispatchers.io) { dataSource.enableTwoFactor(code) }
}
