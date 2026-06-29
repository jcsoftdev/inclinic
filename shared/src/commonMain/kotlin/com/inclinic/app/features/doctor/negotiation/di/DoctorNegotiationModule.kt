package com.inclinic.app.features.doctor.negotiation.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.negotiation.application.GetPackageNegotiationUseCase
import com.inclinic.app.features.doctor.negotiation.application.RespondPackageNegotiationUseCase
import com.inclinic.app.features.doctor.negotiation.core.port.DoctorNegotiationRepository
import com.inclinic.app.features.doctor.negotiation.infrastructure.DefaultDoctorNegotiationRepository
import com.inclinic.app.features.doctor.negotiation.infrastructure.remote.DoctorNegotiationDataSource
import com.inclinic.app.features.doctor.negotiation.infrastructure.remote.KtorDoctorNegotiationDataSource
import com.inclinic.app.features.doctor.negotiation.presentation.component.DefaultRespondPackageNegotiationComponent
import com.inclinic.app.features.doctor.negotiation.presentation.component.RespondPackageNegotiationComponent
import org.koin.dsl.module

fun doctorNegotiationModule() = module {

    single<DoctorNegotiationDataSource> {
        KtorDoctorNegotiationDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    single<DoctorNegotiationRepository> {
        DefaultDoctorNegotiationRepository(remote = get(), dispatchers = get())
    }

    factory { GetPackageNegotiationUseCase(repository = get(), dispatchers = get()) }
    factory { RespondPackageNegotiationUseCase(repository = get(), dispatchers = get()) }

    factory<RespondPackageNegotiationComponent> { (ctx: ComponentContext, negotiationId: String, onOutput: (RespondPackageNegotiationComponent.Output) -> Unit) ->
        DefaultRespondPackageNegotiationComponent(ctx, negotiationId, get(), get(), get(), onOutput)
    }
}
