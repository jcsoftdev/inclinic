package com.inclinic.app.features.doctor.appointments.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.X
import com.inclinic.app.features.doctor.appointments.presentation.component.MarkNoShowComponent
import com.inclinic.app.features.doctor.appointments.presentation.component.MarkNoShowState
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun MarkNoShowScreen(
    component: MarkNoShowComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Box(modifier.fillMaxSize().background(colors.sand)) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
            return@Box
        }

        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().height(56.dp).background(colors.surface).padding(horizontal = dimens.spacingMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Marcar como no-show", style = AppTheme.typography.displayNano, fontSize = 18.sp, color = colors.text)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(24.dp).clip(CircleShape).clickable(onClick = component::onBack),
                ) {
                    Icon(Lucide.X, contentDescription = "Cerrar", tint = colors.text, modifier = Modifier.size(24.dp))
                }
            }

            Column(
                Modifier.fillMaxSize().padding(dimens.spacing20),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(colors.amberBg),
                ) {
                    Icon(Lucide.TriangleAlert, contentDescription = null, tint = colors.amber, modifier = Modifier.size(32.dp))
                }

                Text(
                    "El paciente no se presentó a la consulta",
                    style = AppTheme.typography.displayNano,
                    fontSize = 18.sp,
                    color = colors.text,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.radiusLarge))
                        .background(colors.redBg)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Lucide.ShieldAlert, contentDescription = null, tint = colors.red, modifier = Modifier.size(20.dp))
                    Text(
                        "El pago quedará en custodia esperando resolución admin",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.red,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Razón del no-show (mín 10 chars)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
                    Text("${state.reason.length}/${MarkNoShowState.MAX_REASON_LENGTH}", fontSize = 12.sp, color = colors.muted)
                }

                ReasonTextArea(
                    value = state.reason,
                    onValueChange = component::onReasonChanged,
                    placeholder = "Describe qué pasó...",
                    enabled = !state.isSubmitting,
                )

                state.error?.let {
                    Text(it, style = AppTheme.typography.subtitle, color = colors.red, modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.weight(1f))

                ConfirmCta(
                    enabled = state.canConfirm && !state.isSubmitting,
                    loading = state.isSubmitting,
                    onClick = component::onConfirm,
                )

                Text(
                    "Cancelar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.muted,
                    modifier = Modifier.clickable(enabled = !state.isSubmitting, onClick = component::onBack),
                )
            }
        }
    }
}

@Composable
private fun ReasonTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val shape = RoundedCornerShape(dimens.radius)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = AppTheme.typography.body.copy(color = colors.text),
        cursorBrush = SolidColor(colors.navy),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .border(1.dp, colors.border, shape)
            .background(colors.surface, shape),
        decorationBox = { inner ->
            Box(Modifier.fillMaxSize().padding(14.dp)) {
                if (value.isEmpty()) {
                    Text(placeholder, style = AppTheme.typography.body, color = colors.light)
                }
                inner()
            }
        },
    )
}

@Composable
private fun ConfirmCta(enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(dimens.radius))
            .background(if (enabled || loading) colors.red else colors.red.copy(alpha = 0.5f))
            .clickable(enabled = enabled && !loading, onClick = onClick),
    ) {
        if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
        else Text("Confirmar no-show", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
