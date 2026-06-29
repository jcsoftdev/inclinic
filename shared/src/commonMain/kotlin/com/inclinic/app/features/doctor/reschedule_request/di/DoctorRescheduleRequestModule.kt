package com.inclinic.app.features.doctor.reschedule_request.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.reschedule_request.application.RequestRescheduleUseCase
import com.inclinic.app.features.doctor.reschedule_request.core.port.RescheduleRequestRepository
import com.inclinic.app.features.doctor.reschedule_request.infrastructure.DefaultRescheduleRequestRepository
import com.inclinic.app.features.doctor.reschedule_request.infrastructure.remote.KtorRescheduleRequestDataSource
import com.inclinic.app.features.doctor.reschedule_request.infrastructure.remote.RescheduleRequestDataSource
import com.inclinic.app.features.doctor.reschedule_request.presentation.component.DefaultRequestRescheduleComponent
import com.inclinic.app.features.doctor.reschedule_request.presentation.component.RequestRescheduleComponent
import org.koin.dsl.module

fun doctorRescheduleRequestModule() = module {

    single<RescheduleRequestDataSource> {
        KtorRescheduleRequestDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    single<RescheduleRequestRepository> {
        DefaultRescheduleRequestRepository(remote = get(), dispatchers = get())
    }

    factory { RequestRescheduleUseCase(repository = get(), dispatchers = get()) }

    factory<RequestRescheduleComponent> { (ctx: ComponentContext, appointmentId: String, onOutput: (RequestRescheduleComponent.Output) -> Unit) ->
        DefaultRequestRescheduleComponent(
            componentContext = ctx,
            appointmentId = appointmentId,
            getAppointmentDetail = get(),
            requestReschedule = get(),
            dispatchers = get(),
            onOutput = onOutput,
        )
    }
}
