package com.inclinic.app.features.doctor.settings.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.features.doctor.settings.presentation.component.DefaultDoctorSettingsComponent
import com.inclinic.app.features.doctor.settings.presentation.component.DoctorSettingsComponent
import org.koin.dsl.module

fun doctorSettingsModule() = module {

    factory<DoctorSettingsComponent> { (ctx: ComponentContext, onOutput: (DoctorSettingsComponent.Output) -> Unit) ->
        DefaultDoctorSettingsComponent(ctx, get(), get(), onOutput)
    }
}
