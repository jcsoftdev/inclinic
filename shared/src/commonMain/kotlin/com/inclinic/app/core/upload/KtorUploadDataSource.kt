package com.inclinic.app.core.upload

import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

/**
 * Ktor implementation of [UploadDataSource].
 *
 * POSTs multipart/form-data to `POST /api/upload` with:
 *   - `bucket` — target storage bucket
 *   - `file`   — raw bytes with Content-Disposition/Content-Type headers
 *
 * Uses the app-level authenticated [HttpClient] (Bearer + auto-refresh) so the
 * server validates the JWT and enforces per-doctor bucket permissions.
 *
 * Server constraints enforced on backend:
 *   - Max 10 MB per file
 *   - Allowed mime types: image/jpeg, image/png, image/webp, image/gif, application/pdf
 */
class KtorUploadDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : UploadDataSource {

    override suspend fun upload(
        bucket: String,
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): Result<UploadResultDto> = runCatching {
        client.post {
            url("$baseUrl/api/upload")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("bucket", bucket)
                        append(
                            "file",
                            bytes,
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                append(HttpHeaders.ContentType, mimeType)
                            },
                        )
                    },
                ),
            )
        }.body<ApiEnvelope<UploadResultDto>>().data ?: error("Upload response missing data")
    }
}
