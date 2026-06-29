package com.inclinic.app.features.auth.config

import com.inclinic.app.config.BuildKonfig

/**
 * AuthConfig implementation that reads from BuildKonfig-generated fields.
 *
 * BuildKonfig generates `com.inclinic.app.config.BuildKonfig` with `val` (NOT `const val`)
 * per K2 requirements. The values are set at compile time via `-Pbuildkonfig.flavor=<flavor>`.
 *
 * Validation is delegated to [validateAuthConfig] so it can be unit-tested in commonTest
 * without depending on the generated BuildKonfig object.
 */
class BuildKonfigAuthConfig : AuthConfig by validateAuthConfig(
    rawUrl = resolveApiBaseUrl(
        rawUrl = BuildKonfig.API_BASE_URL,
        rawEnv = BuildKonfig.ENVIRONMENT,
        loopbackHost = platformLoopbackHost,
    ),
    rawEnv = BuildKonfig.ENVIRONMENT,
)
