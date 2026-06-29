package com.inclinic.app.features.doctor.packages.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.packages.application.CancelPackageUseCase
import com.inclinic.app.features.doctor.packages.application.CreatePackageUseCase
import com.inclinic.app.features.doctor.packages.application.GetDoctorPackagesUseCase
import com.inclinic.app.features.doctor.packages.core.port.DoctorPackagesRepository
import com.inclinic.app.features.doctor.packages.infrastructure.DefaultDoctorPackagesRepository
import com.inclinic.app.features.doctor.packages.infrastructure.remote.DoctorPackagesDataSource
import com.inclinic.app.features.doctor.packages.infrastructure.remote.KtorDoctorPackagesDataSource
import com.inclinic.app.features.doctor.packages.presentation.component.CreatePackageComponent
import com.inclinic.app.features.doctor.packages.presentation.component.DefaultCreatePackageComponent
import com.inclinic.app.features.doctor.packages.presentation.component.DefaultPackageDetailComponent
import com.inclinic.app.features.doctor.packages.presentation.component.DefaultPackagesListComponent
import com.inclinic.app.features.doctor.packages.presentation.component.PackageDetailComponent
import com.inclinic.app.features.doctor.packages.presentation.component.PackagesListComponent
import com.inclinic.app.features.doctor.packages.presentation.component.SpecialtyOption
import org.koin.dsl.module

val doctorPackagesModule = module {

    // ── Remote data source ────────────────────────────────────────────────────
    single<DoctorPackagesDataSource> {
        KtorDoctorPackagesDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    // ── Repository ────────────────────────────────────────────────────────────
    single<DoctorPackagesRepository> {
        DefaultDoctorPackagesRepository(remote = get(), dispatchers = get())
    }

    // ── Use cases ─────────────────────────────────────────────────────────────
    factory { GetDoctorPackagesUseCase(repository = get(), dispatchers = get()) }
    factory { CreatePackageUseCase(repository = get(), dispatchers = get()) }
    factory { CancelPackageUseCase(repository = get(), dispatchers = get()) }

    // ── Component factories ───────────────────────────────────────────────────
    factory<PackagesListComponent> { (ctx: ComponentContext, onOutput: (PackagesListComponent.Output) -> Unit) ->
        DefaultPackagesListComponent(
            componentContext = ctx,
            getPackages = get(),
            dispatchers = get<AppDispatchers>(),
            onOutput = onOutput,
        )
    }
    factory<PackageDetailComponent> { (ctx: ComponentContext, packageId: String, onOutput: (PackageDetailComponent.Output) -> Unit) ->
        DefaultPackageDetailComponent(
            componentContext = ctx,
            packageId = packageId,
            getPackages = get(),
            cancelPackage = get(),
            dispatchers = get<AppDispatchers>(),
            onOutput = onOutput,
        )
    }
    factory<CreatePackageComponent> { params ->
        @Suppress("UNCHECKED_CAST")
        DefaultCreatePackageComponent(
            componentContext = params[0],
            patientId = params[1],
            patientName = params[2],
            patientEmail = params[3],
            specialties = params.get<List<SpecialtyOption>>(),
            createPackage = get(),
            dispatchers = get<AppDispatchers>(),
            onOutput = params.get<(CreatePackageComponent.Output) -> Unit>(),
        )
    }
}
