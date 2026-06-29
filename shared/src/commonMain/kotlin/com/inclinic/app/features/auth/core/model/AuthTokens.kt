package com.inclinic.app.features.auth.core.model

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)
