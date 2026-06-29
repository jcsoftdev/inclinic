package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.util.formatDecimal

@Composable
fun DoctorCard(doctor: Doctor, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Doctor ${doctor.fullName}" }
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(doctor.fullName, style = MaterialTheme.typography.titleSmall)
            doctor.specialties.firstOrNull()?.let {
                Text(it.name, style = MaterialTheme.typography.bodySmall)
            }
            doctor.ratingAverage?.let {
                Text("★ ${it.formatDecimal(1)} (${doctor.ratingsCount})", style = MaterialTheme.typography.bodySmall)
            }
            Text("S/. ${doctor.consultationFee}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
