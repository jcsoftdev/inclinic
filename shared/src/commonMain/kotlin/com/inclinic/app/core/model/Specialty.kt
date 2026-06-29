package com.inclinic.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Specialty(
    val id: String,
    val name: String,
    val slug: String = "",
)
