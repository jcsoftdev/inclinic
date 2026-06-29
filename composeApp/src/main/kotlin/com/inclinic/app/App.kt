package com.inclinic.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.inclinic.app.core.navigation.RootComponent
import com.inclinic.app.core.navigation.RootContent

@Composable
fun App(rootComponent: RootComponent) {
    RootContent(component = rootComponent, modifier = Modifier.fillMaxSize())
}
