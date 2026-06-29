package com.inclinic.app.features.payment

import com.inclinic.app.core.model.CardToken
import com.inclinic.app.core.model.RawCard
import com.inclinic.app.core.port.CardTokenizer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MercadoPagoCardTokenizer(private val publicKey: String) : CardTokenizer {
    override suspend fun tokenize(card: RawCard): Result<CardToken> = runCatching {
        suspendCancellableCoroutine { cont ->
            // MercadoPago Android SDK createToken callback — bridged here when SDK is integrated.
            // For now, produce a deterministic stub so the UI can be exercised end-to-end.
            try {
                cont.resume(
                    CardToken(
                        token = "mp_stub_${card.pan.takeLast(4)}",
                        last4 = card.pan.takeLast(4),
                        brand = if (card.pan.startsWith("4")) "VISA" else "MASTERCARD",
                    )
                )
            } catch (t: Throwable) {
                cont.resumeWithException(t)
            }
        }
    }
}
