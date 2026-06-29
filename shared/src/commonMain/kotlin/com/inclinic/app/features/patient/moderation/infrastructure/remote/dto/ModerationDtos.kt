package com.inclinic.app.features.patient.moderation.infrastructure.remote.dto

import kotlinx.serialization.Serializable

/** Body for POST /api/moderation/report */
@Serializable
internal data class ReportUserRequestDto(
    val userId: String,
    val reason: String,
    val category: String? = null,
)

/** Body for POST /api/moderation/block */
@Serializable
internal data class BlockUserRequestDto(
    val userId: String,
    val reason: String? = null,
)
