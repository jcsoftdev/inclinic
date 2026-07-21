package com.inclinic.app.core.di

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.concurrency.RealAppDispatchers
import com.inclinic.app.core.port.TelemetryService
import com.inclinic.app.core.telemetry.LogTelemetryService
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.config.Environment
import org.koin.dsl.module

/**
 * Core Koin module — binds infrastructure-level dependencies shared across all features.
 *
 * Bindings provided:
 * - [AppDispatchers] → [RealAppDispatchers] (single — reused for the app lifetime)
 * - [TelemetryService] → [LogTelemetryService] (single — swap for a real provider later)
 *
 * Platform modules (android / iOS) extend this graph with platform-specific bindings
 * (HttpClientEngine, Settings) that cannot be declared here.
 */
val coreModule = module {
    single<AppDispatchers> { RealAppDispatchers() }
    single<TelemetryService> {
        LogTelemetryService(enabled = get<AuthConfig>().environment != Environment.PROD)
    }
}
