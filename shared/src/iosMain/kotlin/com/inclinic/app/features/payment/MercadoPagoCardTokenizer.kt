package com.inclinic.app.features.payment

import com.inclinic.app.core.model.CardToken
import com.inclinic.app.core.model.RawCard
import com.inclinic.app.core.port.CardTokenizer

class MercadoPagoCardTokenizer : CardTokenizer {
    override suspend fun tokenize(card: RawCard): Result<CardToken> =
        Result.failure(NotImplementedError("MercadoPago iOS SDK not yet bridged"))
}
