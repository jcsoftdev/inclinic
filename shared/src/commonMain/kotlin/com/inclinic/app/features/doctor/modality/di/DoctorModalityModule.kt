package com.inclinic.app.features.doctor.modality.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.modality.application.GetModalityChangeRequestUseCase
import com.inclinic.app.features.doctor.modality.application.RespondModalityChangeUseCase
import com.inclinic.app.features.doctor.modality.core.port.ModalityRequestRepository
import com.inclinic.app.features.doctor.modality.infrastructure.DefaultModalityRequestRepository
import com.inclinic.app.features.doctor.modality.infrastructure.remote.KtorModalityRequestDataSource
import com.inclinic.app.features.doctor.modality.infrastructure.remote.ModalityRequestDataSource
import com.inclinic.app.features.doctor.modality.presentation.component.DefaultRespondModalityComponent
import com.inclinic.app.features.doctor.modality.presentation.component.RespondModalityComponent
import org.koin.dsl.module

fun doctorModalityModule() = module {

    single<ModalityRequestDataSource> {
        KtorModalityRequestDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    single<ModalityRequestRepository> {
        DefaultModalityRequestRepository(remote = get(), dispatchers = get())
    }

    factory { GetModalityChangeRequestUseCase(repository = get(), dispatchers = get()) }
    factory { RespondModalityChangeUseCase(repository = get(), dispatchers = get()) }

    factory<RespondModalityComponent> { (ctx: ComponentContext, requestId: String, onOutput: (RespondModalityComponent.Output) -> Unit) ->
        DefaultRespondModalityComponent(ctx, requestId, get(), get(), get(), onOutput)
    }
}
