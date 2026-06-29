package com.inclinic.app.features.doctor.notifications.di

import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.features.doctor.notifications.application.GetDoctorNotificationsUseCase
import com.inclinic.app.features.doctor.notifications.application.MarkAllNotificationsReadUseCase
import com.inclinic.app.features.doctor.notifications.application.MarkNotificationReadUseCase
import com.inclinic.app.features.doctor.notifications.core.port.DoctorNotificationsRepository
import com.inclinic.app.features.doctor.notifications.infrastructure.DefaultDoctorNotificationsRepository
import com.inclinic.app.features.doctor.notifications.infrastructure.remote.DoctorNotificationsDataSource
import com.inclinic.app.features.doctor.notifications.infrastructure.remote.KtorDoctorNotificationsDataSource
import com.inclinic.app.features.doctor.notifications.presentation.component.DefaultDoctorNotificationsComponent
import com.inclinic.app.features.doctor.notifications.presentation.component.DoctorNotificationsComponent
import org.koin.dsl.module

fun doctorNotificationsModule() = module {

    single<DoctorNotificationsDataSource> {
        KtorDoctorNotificationsDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    single<DoctorNotificationsRepository> {
        DefaultDoctorNotificationsRepository(remote = get(), dispatchers = get())
    }

    factory { GetDoctorNotificationsUseCase(repository = get(), dispatchers = get()) }
    factory { MarkNotificationReadUseCase(repository = get(), dispatchers = get()) }
    factory { MarkAllNotificationsReadUseCase(repository = get(), dispatchers = get()) }

    factory<DoctorNotificationsComponent> { (ctx: ComponentContext, onOutput: (DoctorNotificationsComponent.Output) -> Unit) ->
        DefaultDoctorNotificationsComponent(ctx, get(), get(), get(), get(), onOutput)
    }
}
