package com.inclinic.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmRatingRequest(
    val punctuality: Int,
    val professionalism: Int,
    val empathy: Int,
    val comment: String? = null,
)
