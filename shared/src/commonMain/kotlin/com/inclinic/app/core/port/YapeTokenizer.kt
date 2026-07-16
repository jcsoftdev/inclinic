package com.inclinic.app.core.port

/**
 * Tokeniza un pago Yape (MercadoPago Perú) a partir del número de celular y el
 * OTP de 6 dígitos que el usuario copia desde su app Yape. Devuelve el token de
 * un solo uso que luego se envía a `/api/payments/process` con
 * `paymentMethodId = "yape"`.
 *
 * A diferencia de [CardTokenizer] (que necesita el SDK nativo de MercadoPago),
 * la tokenización Yape es un POST HTTP simple, por lo que vive en commonMain y
 * funciona igual en Android e iOS.
 */
interface YapeTokenizer {
    suspend fun tokenize(phoneNumber: String, otp: String): Result<String>
}
