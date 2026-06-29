package com.inclinic.app.features.patient.infrastructure.remote.dto

import kotlinx.serialization.Serializable

/**
 * Respuesta de POST /api/upload — devuelve la URL (pública o firmada) del archivo subido.
 * Forma del backend: { url, path, bucket, size, type }.
 */
@Serializable
data class UploadResultDto(
    val url: String,
    val path: String,
    val bucket: String,
    val size: Long,
    val type: String,
)
