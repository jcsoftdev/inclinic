package com.inclinic.app.features.doctor.sharing.di

import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.features.doctor.sharing.application.GetIncomingShareRequestsUseCase
import com.inclinic.app.features.doctor.sharing.application.GetOutgoingShareRequestsUseCase
import com.inclinic.app.features.doctor.sharing.application.RequestShareUseCase
import com.inclinic.app.features.doctor.sharing.application.RespondShareRequestUseCase
import com.inclinic.app.features.doctor.sharing.core.port.DoctorSharingRepository
import com.inclinic.app.features.doctor.sharing.infrastructure.DefaultDoctorSharingRepository
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.DoctorSharingDataSource
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.KtorDoctorSharingDataSource
import com.inclinic.app.features.doctor.sharing.presentation.component.DefaultRequestShareComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.DefaultShareRequestsListComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.RequestShareComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.ShareRequestsListComponent
import org.koin.dsl.module

fun doctorSharingModule() = module {

    single<DoctorSharingDataSource> {
        KtorDoctorSharingDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    single<DoctorSharingRepository> {
        DefaultDoctorSharingRepository(remote = get(), dispatchers = get())
    }

    factory { GetIncomingShareRequestsUseCase(repository = get(), dispatchers = get()) }
    factory { GetOutgoingShareRequestsUseCase(repository = get(), dispatchers = get()) }
    factory { RequestShareUseCase(repository = get(), dispatchers = get()) }
    factory { RespondShareRequestUseCase(repository = get(), dispatchers = get()) }

    factory<ShareRequestsListComponent> { (ctx: ComponentContext, onOutput: (ShareRequestsListComponent.Output) -> Unit) ->
        DefaultShareRequestsListComponent(ctx, get(), get(), get(), get(), onOutput)
    }
    factory<RequestShareComponent> { (ctx: ComponentContext, onOutput: (RequestShareComponent.Output) -> Unit) ->
        DefaultRequestShareComponent(ctx, get(), get(), onOutput)
    }
}
