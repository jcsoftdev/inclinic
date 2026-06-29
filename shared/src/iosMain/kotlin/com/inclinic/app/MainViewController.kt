package com.inclinic.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.navigation.RootComponent
import com.inclinic.app.core.navigation.RootContent
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

fun MainViewController(rootComponent: RootComponent): UIViewController =
    ComposeUIViewController {
        RootContent(component = rootComponent, modifier = Modifier.fillMaxSize())
    }

fun createRootComponent(): RootComponent {
    val lifecycle = LifecycleRegistry()
    lifecycle.resume()
    val componentContext = DefaultComponentContext(lifecycle)
    return KoinPlatform.getKoin().get { parametersOf(componentContext) }
}
