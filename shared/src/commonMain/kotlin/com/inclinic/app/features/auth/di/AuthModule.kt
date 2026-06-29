package com.inclinic.app.features.auth.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.core.navigation.DefaultRootComponent
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.core.navigation.RootComponent
import com.inclinic.app.core.network.HttpClientEngineProvider
import com.inclinic.app.core.network.HttpClientFactory
import com.inclinic.app.core.network.RefreshCoordinator
import com.inclinic.app.core.secure.SecureStorage
import com.inclinic.app.core.secure.SettingsSecureStorage
import com.inclinic.app.features.auth.application.ActivateUseCase
import com.inclinic.app.features.auth.application.GetCurrentUserUseCase
import com.inclinic.app.features.auth.application.ForgotPasswordUseCase
import com.inclinic.app.features.auth.application.GetSpecialtiesUseCase
import com.inclinic.app.features.auth.application.GetStoredTokensUseCase
import com.inclinic.app.features.auth.application.LoginUseCase
import com.inclinic.app.features.auth.application.LogoutUseCase
import com.inclinic.app.features.auth.application.RegisterDoctorUseCase
import com.inclinic.app.features.auth.application.RegisterFreelanceDoctorUseCase
import com.inclinic.app.features.auth.application.RegisterPatientUseCase
import com.inclinic.app.features.auth.application.ResendActivationUseCase
import com.inclinic.app.features.auth.application.ResetPasswordUseCase
import com.inclinic.app.features.auth.application.VerifyTwoFactorUseCase
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.config.BuildKonfigAuthConfig
import com.inclinic.app.features.auth.core.port.AuthRepository
import com.inclinic.app.features.auth.core.port.TokenStorage
import com.inclinic.app.features.auth.infrastructure.DefaultAuthRepository
import com.inclinic.app.features.auth.infrastructure.local.SpecialtyCacheDataSource
import com.inclinic.app.features.auth.infrastructure.local.TokenLocalDataSource
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import com.inclinic.app.features.auth.infrastructure.remote.KtorAuthRemoteDataSource
import com.inclinic.app.features.auth.presentation.component.AccountCreatedComponent
import com.inclinic.app.features.auth.presentation.component.ActivateComponent
import com.inclinic.app.features.auth.presentation.component.DefaultAccountCreatedComponent
import com.inclinic.app.features.auth.presentation.component.DefaultActivateComponent
import com.inclinic.app.features.auth.presentation.component.DefaultForgotPasswordComponent
import com.inclinic.app.features.auth.presentation.component.DefaultLoginComponent
import com.inclinic.app.features.auth.presentation.component.DefaultRegisterDoctorComponent
import com.inclinic.app.features.auth.presentation.component.DefaultRegisterPatientComponent
import com.inclinic.app.features.auth.presentation.component.DefaultResetPasswordComponent
import com.inclinic.app.features.auth.presentation.component.DefaultTwoFactorVerifyComponent
import com.inclinic.app.features.auth.presentation.component.ForgotPasswordComponent
import com.inclinic.app.features.auth.presentation.component.LoginComponent
import com.inclinic.app.features.auth.presentation.component.RegisterDoctorComponent
import com.inclinic.app.features.auth.presentation.component.RegisterPatientComponent
import com.inclinic.app.features.auth.presentation.component.ResetPasswordComponent
import com.inclinic.app.features.auth.presentation.component.TwoFactorVerifyComponent
import com.inclinic.app.features.doctor.presentation.component.DoctorFlowComponent
import com.inclinic.app.features.patient.presentation.component.PatientFlowComponent
import com.russhwolf.settings.Settings
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val AUTH_HTTP_CLIENT = named("authHttpClient")
val APP_HTTP_CLIENT = named("appHttpClient")

val authModule = module {

    // ── Config ───────────────────────────────────────────────────────────────
    single<AuthConfig> { BuildKonfigAuthConfig() }

    // ── Session events (singleton) ────────────────────────────────────────────
    single { SessionEvents() }

    // ── SecureStorage (wraps platform-provided Settings) ─────────────────────
    single<SecureStorage> { SettingsSecureStorage(get<Settings>()) }

    // ── Token storage (auth-specific) ─────────────────────────────────────────
    single<TokenStorage> { TokenLocalDataSource(settings = get<Settings>()) }

    // ── HTTP clients ──────────────────────────────────────────────────────────
    single(AUTH_HTTP_CLIENT) {
        HttpClientFactory.create(get<HttpClientEngineProvider>().provide())
    }

    // ── Remote data source ────────────────────────────────────────────────────
    single<AuthRemoteDataSource> {
        KtorAuthRemoteDataSource(
            client = get(AUTH_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    // ── Refresh coordinator ───────────────────────────────────────────────────
    single {
        val remoteDs = get<AuthRemoteDataSource>()
        RefreshCoordinator(
            tokenStorage = get(),
            sessionEvents = get(),
            refreshCall = { refreshToken ->
                remoteDs.refresh(refreshToken).getOrNull()?.let { dto ->
                    val access = dto.accessToken
                    val refresh = dto.refreshToken
                    if (access != null && refresh != null) {
                        com.inclinic.app.features.auth.core.model.AuthTokens(
                            accessToken = access,
                            refreshToken = refresh,
                        )
                    } else {
                        null
                    }
                }
            },
        )
    }

    // ── Authenticated HTTP client (Bearer + auto-refresh) ─────────────────────
    single(APP_HTTP_CLIENT) {
        HttpClientFactory.createAuthenticated(
            engine = get<HttpClientEngineProvider>().provide(),
            baseUrl = get<AuthConfig>().apiBaseUrl,
            tokenStorage = get(),
            refreshCoordinator = get(),
        )
    }

    // ── Specialty cache (24-hour in-memory TTL) ───────────────────────────────
    single { SpecialtyCacheDataSource(remote = get()) }

    // ── Repository ────────────────────────────────────────────────────────────
    single<AuthRepository> {
        DefaultAuthRepository(
            remote = get(),
            local = get(),
            dispatchers = get(),
        )
    }

    // ── Use cases ─────────────────────────────────────────────────────────────
    factory { LoginUseCase(repository = get(), tokenStorage = get(), dispatchers = get()) }
    factory { VerifyTwoFactorUseCase(repository = get(), tokenStorage = get(), dispatchers = get()) }
    factory { LogoutUseCase(tokenStorage = get(), sessionEvents = get(), dispatchers = get()) }
    factory { GetStoredTokensUseCase(tokenStorage = get(), dispatchers = get()) }
    factory { RegisterPatientUseCase(remote = get(), dispatchers = get()) }
    factory { RegisterDoctorUseCase(remote = get(), dispatchers = get()) }
    factory { RegisterFreelanceDoctorUseCase(remote = get(), dispatchers = get()) }
    factory { ActivateUseCase(remote = get(), dispatchers = get()) }
    factory { ResendActivationUseCase(remote = get(), dispatchers = get()) }
    factory { ForgotPasswordUseCase(remote = get(), dispatchers = get()) }
    factory { ResetPasswordUseCase(remote = get(), dispatchers = get()) }
    factory { GetSpecialtiesUseCase(cache = get(), dispatchers = get()) }
    factory { GetCurrentUserUseCase(authenticatedClient = get(APP_HTTP_CLIENT), baseUrl = get<AuthConfig>().apiBaseUrl, dispatchers = get()) }

    // ── Component factories ───────────────────────────────────────────────────
    factory<LoginComponent> { (ctx: ComponentContext, onSuccess: (AuthUser) -> Unit, onTwoFactor: (String) -> Unit, onForgotPassword: () -> Unit, onRegister: () -> Unit) ->
        DefaultLoginComponent(
            componentContext = ctx,
            loginUseCase = get(),
            dispatchers = get(),
            onLoginSucceeded = onSuccess,
            onTwoFactorRequired = onTwoFactor,
            onNavigateForgotPassword = onForgotPassword,
            onNavigateRegister = onRegister,
        )
    }

    factory<TwoFactorVerifyComponent> { (ctx: ComponentContext, partialToken: String, onVerified: (AuthUser) -> Unit, onBack: () -> Unit) ->
        DefaultTwoFactorVerifyComponent(
            componentContext = ctx,
            partialToken = partialToken,
            verifyUseCase = get(),
            dispatchers = get(),
            onVerified = onVerified,
            onBack = onBack,
        )
    }

    factory<RegisterPatientComponent> { (ctx: ComponentContext, onOutput: (RegisterPatientComponent.Output) -> Unit) ->
        DefaultRegisterPatientComponent(
            componentContext = ctx,
            registerUseCase = get(),
            dispatchers = get(),
            onOutput = onOutput,
        )
    }

    factory<RegisterDoctorComponent> { (ctx: ComponentContext, onOutput: (RegisterDoctorComponent.Output) -> Unit) ->
        DefaultRegisterDoctorComponent(
            componentContext = ctx,
            registerFreelanceUseCase = get(),
            getSpecialtiesUseCase = get(),
            dispatchers = get(),
            onOutput = onOutput,
        )
    }

    factory<ActivateComponent> { (ctx: ComponentContext, email: String, onOutput: (ActivateComponent.Output) -> Unit) ->
        DefaultActivateComponent(
            componentContext = ctx,
            email = email,
            activateUseCase = get(),
            resendActivationUseCase = get(),
            dispatchers = get(),
            onOutput = onOutput,
        )
    }

    factory<ForgotPasswordComponent> { (ctx: ComponentContext, onOutput: (ForgotPasswordComponent.Output) -> Unit) ->
        DefaultForgotPasswordComponent(
            componentContext = ctx,
            forgotPasswordUseCase = get(),
            dispatchers = get(),
            onOutput = onOutput,
        )
    }

    factory<ResetPasswordComponent> { (ctx: ComponentContext, token: String, onOutput: (ResetPasswordComponent.Output) -> Unit) ->
        DefaultResetPasswordComponent(
            componentContext = ctx,
            token = token,
            resetPasswordUseCase = get(),
            dispatchers = get(),
            onOutput = onOutput,
        )
    }

    factory<AccountCreatedComponent> { (ctx: ComponentContext, email: String, onOutput: (AccountCreatedComponent.Output) -> Unit) ->
        DefaultAccountCreatedComponent(
            componentContext = ctx,
            email = email,
            resendActivationUseCase = get(),
            dispatchers = get(),
            onOutput = onOutput,
        )
    }

    // ── Root component (new ChildStack-based) ─────────────────────────────────
    factory<RootComponent> { (ctx: ComponentContext) ->
        DefaultRootComponent(
            componentContext = ctx,
            dispatchers = get(),
            sessionEvents = get(),
            getStoredTokens = get(),
            tokenStorage = get(),
            loginComponentFactory = { childCtx, onSuccess: (AuthUser) -> Unit, onTwoFactor: (String) -> Unit, onForgotPassword: () -> Unit, onRegister: () -> Unit ->
                get { parametersOf(childCtx, onSuccess, onTwoFactor, onForgotPassword, onRegister) }
            },
            twoFactorVerifyComponentFactory = { childCtx, partialToken, onVerified: (AuthUser) -> Unit, onBack: () -> Unit ->
                get { parametersOf(childCtx, partialToken, onVerified, onBack) }
            },
            registerPatientComponentFactory = { childCtx, onOutput ->
                get { parametersOf(childCtx, onOutput) }
            },
            registerDoctorComponentFactory = { childCtx, onOutput ->
                get { parametersOf(childCtx, onOutput) }
            },
            activateComponentFactory = { childCtx, email, onOutput ->
                get { parametersOf(childCtx, email, onOutput) }
            },
            accountCreatedComponentFactory = { childCtx, email, onOutput ->
                get { parametersOf(childCtx, email, onOutput) }
            },
            forgotPasswordComponentFactory = { childCtx, onOutput ->
                get { parametersOf(childCtx, onOutput) }
            },
            resetPasswordComponentFactory = { childCtx, token, onOutput ->
                get { parametersOf(childCtx, token, onOutput) }
            },
            patientFlowComponentFactory = { childCtx, patientId ->
                get { parametersOf(childCtx, patientId) }
            },
            doctorFlowComponentFactory = { childCtx, doctorId ->
                get { parametersOf(childCtx, doctorId) }
            },
            adminFlowComponentFactory = { childCtx ->
                get { parametersOf(childCtx) }
            },
        )
    }
}
