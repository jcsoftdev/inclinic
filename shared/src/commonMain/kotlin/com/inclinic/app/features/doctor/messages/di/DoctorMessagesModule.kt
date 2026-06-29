package com.inclinic.app.features.doctor.messages.di

import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.features.doctor.messages.application.GetDoctorChatThreadsUseCase
import com.inclinic.app.features.doctor.messages.application.MarkThreadReadUseCase
import com.inclinic.app.features.doctor.messages.core.port.DoctorMessagesRepository
import com.inclinic.app.features.doctor.messages.infrastructure.DefaultDoctorMessagesRepository
import com.inclinic.app.features.doctor.messages.infrastructure.remote.DoctorMessagesDataSource
import com.inclinic.app.features.doctor.messages.infrastructure.remote.KtorDoctorMessagesDataSource
import com.inclinic.app.features.doctor.messages.presentation.component.DefaultDoctorChatListComponent
import com.inclinic.app.features.doctor.messages.presentation.component.DoctorChatListComponent
import org.koin.dsl.module

fun doctorMessagesModule() = module {

    single<DoctorMessagesDataSource> {
        KtorDoctorMessagesDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    single<DoctorMessagesRepository> {
        DefaultDoctorMessagesRepository(remote = get(), dispatchers = get())
    }

    factory { GetDoctorChatThreadsUseCase(repository = get(), dispatchers = get()) }
    // MarkThreadReadUseCase is a no-op stub (read state is updated server-side)
    factory { MarkThreadReadUseCase() }

    factory<DoctorChatListComponent> { (ctx: ComponentContext, onOutput: (DoctorChatListComponent.Output) -> Unit) ->
        DefaultDoctorChatListComponent(ctx, get(), get(), onOutput)
    }
}
