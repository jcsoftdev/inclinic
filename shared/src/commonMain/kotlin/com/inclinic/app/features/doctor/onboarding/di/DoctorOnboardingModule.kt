package com.inclinic.app.features.doctor.onboarding.di

import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.onboarding.application.GetOnboardingStatusUseCase
import com.inclinic.app.features.doctor.onboarding.application.ResubmitOnboardingUseCase
import com.inclinic.app.features.doctor.onboarding.application.SubmitOnboardingUseCase
import com.inclinic.app.features.doctor.onboarding.application.UploadDocumentUseCase
import com.inclinic.app.features.doctor.onboarding.core.port.DoctorOnboardingRepository
import com.inclinic.app.features.doctor.onboarding.infrastructure.DefaultDoctorOnboardingRepository
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.DoctorOnboardingDataSource
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.KtorDoctorOnboardingDataSource
import org.koin.dsl.module

fun doctorOnboardingModule() = module {

    // ── Remote data source ────────────────────────────────────────────────────
    single<DoctorOnboardingDataSource> {
        KtorDoctorOnboardingDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    // ── Repository ────────────────────────────────────────────────────────────
    single<DoctorOnboardingRepository> {
        DefaultDoctorOnboardingRepository(remote = get(), dispatchers = get())
    }

    // ── Use cases ─────────────────────────────────────────────────────────────
    factory { GetOnboardingStatusUseCase(repository = get(), dispatchers = get()) }
    factory { SubmitOnboardingUseCase(repository = get(), dispatchers = get()) }
    factory { ResubmitOnboardingUseCase(repository = get(), dispatchers = get()) }
    factory { UploadDocumentUseCase(repository = get(), dispatchers = get()) }
}
