package com.inclinic.app.features.patient.assistant.di

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.config.Environment
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.patient.assistant.application.SendAssistantMessageUseCase
import com.inclinic.app.features.patient.assistant.core.port.AssistantChatDataSource
import com.inclinic.app.features.patient.assistant.infrastructure.KtorAssistantChatDataSource
import com.inclinic.app.features.patient.assistant.presentation.component.AssistantChatComponent
import com.inclinic.app.features.patient.assistant.presentation.component.DefaultAssistantChatComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * Koin DI sanity test for [assistantChatModule].
 *
 * Verifies that all factory/single bindings resolve without exception.
 * Pattern mirrors [com.inclinic.app.di.AuthModuleTest].
 */
class AssistantChatModuleTest : KoinTest {

    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()

    private val testSupportModule = module {
        single<AuthConfig> {
            object : AuthConfig {
                override val apiBaseUrl = "https://test.api.inclinic.com"
                override val environment = Environment.DEV
            }
        }
        single<AppDispatchers> {
            object : AppDispatchers {
                override val main: CoroutineDispatcher = testDispatcher
                override val io: CoroutineDispatcher = testDispatcher
                override val default: CoroutineDispatcher = testDispatcher
            }
        }
        // Provide the authenticated HttpClient via the APP_HTTP_CLIENT qualifier
        // (same qualifier used in AuthModule and consumed by AssistantChatModule)
        single(APP_HTTP_CLIENT) {
            HttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        }
        single { SessionEvents() }
    }

    @BeforeTest
    fun setup() {
        stopKoin()
    }

    @AfterTest
    fun teardown() {
        stopKoin()
    }

    @Test
    fun assistantChatModule_data_source_resolves() {
        val koin = startKoin {
            allowOverride(true)
            modules(assistantChatModule, testSupportModule)
        }.koin

        assertIs<KtorAssistantChatDataSource>(koin.get<AssistantChatDataSource>())
    }

    @Test
    fun assistantChatModule_use_case_resolves() {
        val koin = startKoin {
            allowOverride(true)
            modules(assistantChatModule, testSupportModule)
        }.koin

        assertIs<SendAssistantMessageUseCase>(koin.get<SendAssistantMessageUseCase>())
    }

    @Test
    fun assistantChatModule_component_resolves_with_context_parameter() {
        val koin = startKoin {
            allowOverride(true)
            modules(assistantChatModule, testSupportModule)
        }.koin

        val lifecycle = LifecycleRegistry().also { it.resume() }
        val testCtx: ComponentContext = DefaultComponentContext(lifecycle = lifecycle)

        assertIs<DefaultAssistantChatComponent>(
            koin.get<AssistantChatComponent> {
                parametersOf(testCtx, { _: AssistantChatComponent.Output -> })
            }
        )
    }
}
