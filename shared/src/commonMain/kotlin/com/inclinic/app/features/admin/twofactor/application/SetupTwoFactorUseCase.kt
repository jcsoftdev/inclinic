package com.inclinic.app.features.admin.twofactor.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.TwoFactorSetup
import kotlinx.coroutines.withContext

class SetupTwoFactorUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<TwoFactorSetup> =
        withContext(dispatchers.io) { dataSource.setupTwoFactor() }
}
