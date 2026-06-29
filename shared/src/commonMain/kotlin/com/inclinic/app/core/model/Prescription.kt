package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Prescription(
    val id: String,
    val code: String,
    val doctorId: String,
    val doctorName: String? = null,
    val doctorLicense: String? = null,
    val specialtyName: String? = null,
    val issuedAt: Instant,
    val validUntil: Instant? = null,
    val medications: List<Medication> = emptyList(),
    val generalInstructions: String? = null,
    val status: PrescriptionStatus = PrescriptionStatus.ACTIVE,
)

@Serializable
data class Medication(
    val name: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val instructions: String? = null,
)

@Serializable
enum class PrescriptionStatus { ACTIVE, EXPIRED }
