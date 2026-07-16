package com.inclinic.app.features.patient.payment.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RawCard
import com.inclinic.app.core.port.CardTokenizer
import com.inclinic.app.core.port.YapeTokenizer
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class ProcessPaymentUseCase(
    private val cardTokenizer: CardTokenizer,
    private val yapeTokenizer: YapeTokenizer,
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    private companion object {
        const val YAPE_METHOD = "yape"
    }
    suspend operator fun invoke(rawCard: RawCard, appointmentId: String): Result<PaymentResult> =
        withContext(dispatchers.io) {
            val tokenResult = cardTokenizer.tokenize(rawCard)
            if (tokenResult.isFailure) return@withContext Result.failure(tokenResult.exceptionOrNull()!!)
            val token = tokenResult.getOrThrow()
            val paymentMethodId = token.brand.lowercase()
            dataSource.processPayment(token.token, paymentMethodId, appointmentId)
        }

    suspend fun payPackage(rawCard: RawCard, therapyPackageId: String): Result<PaymentResult> =
        withContext(dispatchers.io) {
            val tokenResult = cardTokenizer.tokenize(rawCard)
            if (tokenResult.isFailure) return@withContext Result.failure(tokenResult.exceptionOrNull()!!)
            val token = tokenResult.getOrThrow()
            val paymentMethodId = token.brand.lowercase()
            dataSource.processPackagePayment(token.token, paymentMethodId, therapyPackageId)
        }

    suspend fun payWithYape(phoneNumber: String, otp: String, appointmentId: String): Result<PaymentResult> =
        withContext(dispatchers.io) {
            val tokenResult = yapeTokenizer.tokenize(phoneNumber, otp)
            if (tokenResult.isFailure) return@withContext Result.failure(tokenResult.exceptionOrNull()!!)
            dataSource.processPayment(tokenResult.getOrThrow(), YAPE_METHOD, appointmentId)
        }

    suspend fun payPackageWithYape(phoneNumber: String, otp: String, therapyPackageId: String): Result<PaymentResult> =
        withContext(dispatchers.io) {
            val tokenResult = yapeTokenizer.tokenize(phoneNumber, otp)
            if (tokenResult.isFailure) return@withContext Result.failure(tokenResult.exceptionOrNull()!!)
            dataSource.processPackagePayment(tokenResult.getOrThrow(), YAPE_METHOD, therapyPackageId)
        }
}
