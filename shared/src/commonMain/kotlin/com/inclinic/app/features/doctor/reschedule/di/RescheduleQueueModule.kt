package com.inclinic.app.features.doctor.reschedule.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.reschedule.application.GetRescheduleRequestsUseCase
import com.inclinic.app.features.doctor.reschedule.application.RespondRescheduleRequestUseCase
import com.inclinic.app.features.doctor.reschedule.core.port.RescheduleQueueRepository
import com.inclinic.app.features.doctor.reschedule.infrastructure.DefaultRescheduleQueueRepository
import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.KtorRescheduleQueueDataSource
import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.RescheduleQueueDataSource
import com.inclinic.app.features.doctor.reschedule.presentation.component.DefaultRescheduleQueueComponent
import com.inclinic.app.features.doctor.reschedule.presentation.component.RescheduleQueueComponent
import org.koin.dsl.module

fun rescheduleQueueModule() = module {

    single<RescheduleQueueDataSource> {
        KtorRescheduleQueueDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    single<RescheduleQueueRepository> {
        DefaultRescheduleQueueRepository(remote = get(), dispatchers = get())
    }

    factory { GetRescheduleRequestsUseCase(repository = get(), dispatchers = get()) }
    factory { RespondRescheduleRequestUseCase(repository = get(), dispatchers = get()) }

    factory<RescheduleQueueComponent> { (ctx: ComponentContext, onOutput: (RescheduleQueueComponent.Output) -> Unit) ->
        DefaultRescheduleQueueComponent(ctx, get(), get(), get(), onOutput)
    }
}
