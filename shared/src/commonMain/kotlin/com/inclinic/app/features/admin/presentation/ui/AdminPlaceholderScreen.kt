package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composables.icons.lucide.Hourglass
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.admin.presentation.component.AdminPlaceholderComponent
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun AdminPlaceholderScreen(component: AdminPlaceholderComponent, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.sand),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(
            title = component.title,
            subtitle = "Próximamente",
            icon = Lucide.Hourglass,
        )
    }
}
