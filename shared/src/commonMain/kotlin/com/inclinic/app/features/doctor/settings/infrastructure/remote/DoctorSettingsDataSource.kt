package com.inclinic.app.features.doctor.settings.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

/**
 * Data source for doctor settings operations that require authenticated DOCTOR role.
 *
 * Endpoints:
 *   GET  /api/doctors/me/mercadopago/connect  → OAuth URL (503 when not configured)
 *   DELETE /api/doctors/me/mercadopago         → disconnect integration
 */
interface DoctorSettingsDataSource {
    /**
     * Returns the MercadoPago OAuth authorization URL.
     * Fails with message "MP_NOT_CONFIGURED" when backend returns 503.
     */
    suspend fun getMercadoPagoConnectUrl(): Result<String>

    /**
     * Disconnects the doctor's MercadoPago integration.
     */
    suspend fun disconnectMercadoPago(): Result<Unit>
}

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
private data class MercadoPagoConnectDto(val url: String = "")

// ── Ktor implementation ───────────────────────────────────────────────────────

class KtorDoctorSettingsDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorSettingsDataSource {

    override suspend fun getMercadoPagoConnectUrl(): Result<String> = runCatching {
        val response = client.get {
            url("$baseUrl/api/doctors/me/mercadopago/connect")
        }
        when {
            response.status == HttpStatusCode.ServiceUnavailable -> {
                error("MP_NOT_CONFIGURED: La integración de MercadoPago no está configurada.")
            }
            response.status.value !in 200..299 -> {
                error("Error al obtener URL de MercadoPago: ${response.status}")
            }
            else -> {
                response.body<ApiEnvelope<MercadoPagoConnectDto>>().data?.url
                    ?: error("MercadoPago connect URL missing in response")
            }
        }
    }

    override suspend fun disconnectMercadoPago(): Result<Unit> = runCatching {
        val response = client.delete {
            url("$baseUrl/api/doctors/me/mercadopago")
        }
        if (response.status.value !in 200..299) {
            error("Error al desconectar MercadoPago: ${response.status}")
        }
    }
}
