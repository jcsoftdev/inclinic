package com.inclinic.app.di

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.network.HttpClientEngineProvider
import com.inclinic.app.features.auth.application.GetStoredTokensUseCase
import com.inclinic.app.features.auth.application.LoginUseCase
import com.inclinic.app.features.auth.application.LogoutUseCase
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.config.Environment
import com.inclinic.app.features.auth.core.port.AuthRepository
import com.inclinic.app.features.auth.core.port.TokenStorage
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.auth.di.authModule
import com.inclinic.app.features.auth.infrastructure.DefaultAuthRepository
import com.inclinic.app.features.auth.infrastructure.local.TokenLocalDataSource
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Koin module sanity test for [authModule].
 *
 * Verifies that all [single] and [factory] definitions in the graph can be resolved
 * by providing platform-specific deps (HttpClientEngine, Settings, AppDispatchers)
 * via a test-only override module.
 *
 * ## TDD RED → GREEN
 * RED:  [authModule] only binds [AuthConfig] — all other definitions are missing.
 *       The test fails because [AuthRepository], [LoginUseCase], etc. cannot be resolved.
 * GREEN: After Phase 9 DI wiring is complete, the full graph resolves.
 */
class AuthModuleTest : KoinTest {

    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()

    /**
     * Test platform module providing all platform-specific bindings that authModule
     * depends on, plus overriding AuthConfig to bypass the BuildKonfig gatekeeper.
     */
    private val testSupportModule = module {
        // Override AuthConfig so BuildKonfig gatekeeper doesn't crash
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
        single<HttpClientEngineProvider> {
            object : HttpClientEngineProvider {
                override fun provide() = MockEngine { respond("", HttpStatusCode.OK) }
            }
        }
        single<Settings> { MapSettings() }
    }

    @BeforeTest
    fun setup() {
        stopKoin()
    }

    @AfterTest
    fun teardown() {
        stopKoin()
    }

    /**
     * Starts the Koin graph with authModule + test support module, then manually
     * resolves each binding to verify the graph is complete.
     *
     * We intentionally avoid [checkModules] because it is deprecated in Koin 4.x
     * (migrate to `verify()` API) and its internal implementation starts a new Koin
     * context, which conflicts with the already-running one from [startKoin] in this test.
     * Manual resolution is equivalent and more explicit.
     */
    @Test
    fun authModule_all_bindings_resolve() {
        val koin = startKoin {
            allowOverride(true)
            modules(authModule, testSupportModule)
        }.koin

        // Verify every expected binding resolves without throwing
        val authConfig = koin.get<AuthConfig>()
        assertEquals("https://test.api.inclinic.com", authConfig.apiBaseUrl)
        assertEquals(Environment.DEV, authConfig.environment)

        assertIs<DefaultAuthRepository>(koin.get<AuthRepository>())
        assertIs<TokenLocalDataSource>(koin.get<TokenStorage>())
        assertIs<LoginUseCase>(koin.get<LoginUseCase>())
        assertIs<LogoutUseCase>(koin.get<LogoutUseCase>())
        assertIs<GetStoredTokensUseCase>(koin.get<GetStoredTokensUseCase>())
    }

    @Test
    fun authModule_http_client_resolves() {
        val koin = startKoin {
            allowOverride(true)
            modules(authModule, testSupportModule)
        }.koin

        // HttpClient is bound with APP_HTTP_CLIENT qualifier — verify it can be resolved
        val httpClient = koin.get<io.ktor.client.HttpClient>(qualifier = APP_HTTP_CLIENT)
        assertIs<io.ktor.client.HttpClient>(httpClient)
    }

    @Test
    fun authModule_remote_data_source_resolves() {
        val koin = startKoin {
            allowOverride(true)
            modules(authModule, testSupportModule)
        }.koin

        val remote = koin.get<com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource>()
        assertIs<com.inclinic.app.features.auth.infrastructure.remote.KtorAuthRemoteDataSource>(remote)
    }
}
