package com.inclinic.app.features.auth.config

/**
 * Runtime configuration for the Auth feature.
 * All fields are required — no defaults, no fallbacks.
 * Missing values must crash at Koin initialization (gatekeeper in AuthModule).
 */
interface AuthConfig {
    val apiBaseUrl: String
    val environment: Environment
}

enum class Environment { DEV, STAGING, PROD }

/**
 * Pure validation function — no BuildKonfig dependency, no Koin.
 * Extracted for testability: tests can call this directly without touching generated code.
 *
 * @throws IllegalStateException if [rawUrl] is blank.
 * @throws IllegalStateException if [rawEnv] is blank or not a valid [Environment] name.
 */
fun validateAuthConfig(rawUrl: String, rawEnv: String): AuthConfig {
    if (rawUrl.isBlank()) {
        error("AuthConfig: API_BASE_URL is missing. Build with -Pbuildkonfig.flavor=dev|staging|prod.")
    }
    if (rawEnv.isBlank()) {
        error("AuthConfig: ENVIRONMENT is missing. Build with -Pbuildkonfig.flavor=dev|staging|prod.")
    }
    val parsedEnv = runCatching { Environment.valueOf(rawEnv) }
        .getOrElse {
            error("AuthConfig: ENVIRONMENT '$rawEnv' is not a valid value. Expected DEV|STAGING|PROD.")
        }
    return object : AuthConfig {
        override val apiBaseUrl: String = rawUrl
        override val environment: Environment = parsedEnv
    }
}
