package com.inclinic.app.features.payment

import com.inclinic.app.core.port.YapeTokenizer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.serialization.Serializable

/**
 * Tokeniza pagos Yape contra el endpoint PCI de MercadoPago:
 *   POST https://api.mercadopago.com/platforms/pci/yape/v1/payment?public_key=<key>
 *   body: { phoneNumber, otp, requestId }
 *
 * El token resultante es de un solo uso y se envía a nuestro backend
 * (`/api/payments/process` con `paymentMethodId = "yape"`).
 *
 * Con una public key de PRODUCCIÓN (`APP_USR-...`) hace la tokenización real.
 * Con cualquier otra key (dev/test) devuelve un token stub `mp_stub_yape_<xxxx>`
 * que el backend acepta en no-producción — mismo patrón que el card tokenizer.
 */
class KtorYapeTokenizer(
    private val client: HttpClient,
    private val publicKey: String,
) : YapeTokenizer {

    override suspend fun tokenize(phoneNumber: String, otp: String): Result<String> {
        if (!publicKey.startsWith("APP_USR-")) {
            // Dev/test: token stub aceptado por el backend en no-producción.
            return Result.success("mp_stub_yape_${phoneNumber.takeLast(4)}")
        }
        return runCatching {
            val requestId = "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(0, Int.MAX_VALUE)}"
            val response = client.post {
                url(YAPE_TOKEN_URL)
                parameter("public_key", publicKey)
                contentType(ContentType.Application.Json)
                setBody(YapeTokenRequest(phoneNumber = phoneNumber, otp = otp, requestId = requestId))
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException("No se pudo generar el token de Yape. Verifica el celular y el código.")
            }
            val dto = response.body<YapeTokenResponse>()
            dto.id ?: dto.token
            ?: throw IllegalStateException("Respuesta de Yape sin token.")
        }
    }

    private companion object {
        const val YAPE_TOKEN_URL = "https://api.mercadopago.com/platforms/pci/yape/v1/payment"
    }
}

@Serializable
private data class YapeTokenRequest(
    val phoneNumber: String,
    val otp: String,
    val requestId: String,
)

@Serializable
private data class YapeTokenResponse(
    val id: String? = null,
    val token: String? = null,
)
