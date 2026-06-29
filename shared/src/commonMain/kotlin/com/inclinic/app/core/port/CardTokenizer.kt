package com.inclinic.app.core.port

import com.inclinic.app.core.model.CardToken
import com.inclinic.app.core.model.RawCard

interface CardTokenizer {
    suspend fun tokenize(card: RawCard): Result<CardToken>
}
