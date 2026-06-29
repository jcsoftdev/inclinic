package com.inclinic.app.features.doctor.settings.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.settings.infrastructure.remote.DoctorSettingsDataSource
import com.inclinic.app.features.doctor.settings.infrastructure.remote.KtorDoctorSettingsDataSource
import com.inclinic.app.features.doctor.settings.presentation.component.DefaultDoctorSettingsComponent
import com.inclinic.app.features.doctor.settings.presentation.component.DoctorSettingsComponent
import org.koin.dsl.module

fun doctorSettingsModule() = module {

    single<DoctorSettingsDataSource> {
        KtorDoctorSettingsDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    factory<DoctorSettingsComponent> { (ctx: ComponentContext, onOutput: (DoctorSettingsComponent.Output) -> Unit) ->
        DefaultDoctorSettingsComponent(ctx, get(), get(), get(), onOutput)
    }
}
