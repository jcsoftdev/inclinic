package com.inclinic.app.features.doctor.no_shows.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem
import com.inclinic.app.features.doctor.no_shows.core.model.PaymentHoldStatus
import kotlinx.coroutines.withContext

/**
 * Fetches all doctor no-show appointments and partitions them into two queues:
 *  - [NoShowQueue.pending]  — [PaymentHoldStatus.HELD] (admin has not yet resolved payment)
 *  - [NoShowQueue.resolved] — [PaymentHoldStatus.RELEASED] or [PaymentHoldStatus.REFUNDED]
 */
data class NoShowQueue(
    val pending: List<NoShowItem>,
    val resolved: List<NoShowItem>,
)

class GetNoShowQueueUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        from: String? = null,
        to: String? = null,
    ): Result<NoShowQueue> =
        withContext(dispatchers.io) {
            dataSource.getNoShowAppointments(from, to).map { items ->
                NoShowQueue(
                    pending = items.filter { it.paymentHoldStatus == PaymentHoldStatus.HELD },
                    resolved = items.filter {
                        it.paymentHoldStatus == PaymentHoldStatus.RELEASED ||
                            it.paymentHoldStatus == PaymentHoldStatus.REFUNDED
                    },
                )
            }
        }
}
