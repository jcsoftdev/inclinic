package com.inclinic.app.features.admin.twofactor.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.TwoFactorStatus
import kotlinx.coroutines.withContext

class GetTwoFactorStatusUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<TwoFactorStatus> =
        withContext(dispatchers.io) { dataSource.getTwoFactorStatus() }
}
