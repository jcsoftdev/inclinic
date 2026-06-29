package com.inclinic.app.features.patient.assistant.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.patient.assistant.application.SendAssistantMessageUseCase
import com.inclinic.app.features.patient.assistant.core.port.AssistantChatDataSource
import com.inclinic.app.features.patient.assistant.infrastructure.KtorAssistantChatDataSource
import com.inclinic.app.features.patient.assistant.presentation.component.AssistantChatComponent
import com.inclinic.app.features.patient.assistant.presentation.component.DefaultAssistantChatComponent
import org.koin.dsl.module

/**
 * Koin module for the Patient Assistant Chat vertical slice.
 *
 * ## Dependency graph
 *
 * ```
 * APP_HTTP_CLIENT (HttpClient, from authModule) ─┐
 * AuthConfig.apiBaseUrl ────────────────────────┤
 *                                               ▼
 *                                   KtorAssistantChatDataSource
 *                                               │
 *                                               ▼
 *                                   SendAssistantMessageUseCase
 *                                               │
 * SessionEvents (from authModule) ─────────────┤
 * AppDispatchers ──────────────────────────────┤
 *                                               ▼
 *                                   DefaultAssistantChatComponent
 * ```
 *
 * ## Registration
 * Include this module in [com.inclinic.app.features.patient.di.patientModule]
 * (or the root Koin init) by adding `assistantChatModule` to the modules list.
 */
val assistantChatModule = module {

    factory<AssistantChatDataSource> {
        KtorAssistantChatDataSource(
            client = get(qualifier = APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    factory { SendAssistantMessageUseCase(get()) }

    factory<AssistantChatComponent> { (ctx: ComponentContext) ->
        DefaultAssistantChatComponent(
            componentContext = ctx,
            sendMessage = get(),
            sessionEvents = get(),
            dispatchers = get(),
        )
    }
}
