package com.inclinic.app.features.doctor.patients.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.patients.application.GetDoctorPatientsUseCase
import com.inclinic.app.features.doctor.patients.application.SearchPatientByEmailUseCase
import com.inclinic.app.features.doctor.patients.core.port.DoctorPatientsRepository
import com.inclinic.app.features.doctor.patients.infrastructure.DefaultDoctorPatientsRepository
import com.inclinic.app.features.doctor.patients.infrastructure.remote.DoctorPatientsDataSource
import com.inclinic.app.features.doctor.patients.infrastructure.remote.KtorDoctorPatientsDataSource
import com.inclinic.app.features.doctor.patients.presentation.component.DefaultPatientsListComponent
import com.inclinic.app.features.doctor.patients.presentation.component.DefaultSearchPatientComponent
import com.inclinic.app.features.doctor.patients.presentation.component.PatientsListComponent
import com.inclinic.app.features.doctor.patients.presentation.component.SearchPatientComponent
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val doctorPatientsModule = module {

    // ── Remote data source ────────────────────────────────────────────────────
    single<DoctorPatientsDataSource> {
        KtorDoctorPatientsDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    // ── Repository ────────────────────────────────────────────────────────────
    single<DoctorPatientsRepository> {
        DefaultDoctorPatientsRepository(remote = get(), dispatchers = get())
    }

    // ── Use cases ─────────────────────────────────────────────────────────────
    factory { GetDoctorPatientsUseCase(repository = get(), dispatchers = get()) }
    factory { SearchPatientByEmailUseCase(repository = get(), dispatchers = get()) }

    // ── Component factories ───────────────────────────────────────────────────
    factory<PatientsListComponent> { (ctx: ComponentContext, onOutput: (PatientsListComponent.Output) -> Unit) ->
        DefaultPatientsListComponent(
            componentContext = ctx,
            getPatients = get(),
            dispatchers = get<AppDispatchers>(),
            onOutput = onOutput,
        )
    }
    factory<SearchPatientComponent> { (ctx: ComponentContext, onOutput: (SearchPatientComponent.Output) -> Unit) ->
        DefaultSearchPatientComponent(
            componentContext = ctx,
            searchPatient = get(),
            dispatchers = get<AppDispatchers>(),
            onOutput = onOutput,
        )
    }
}
