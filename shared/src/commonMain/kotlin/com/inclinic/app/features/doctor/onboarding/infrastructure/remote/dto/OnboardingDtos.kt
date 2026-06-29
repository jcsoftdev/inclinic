package com.inclinic.app.features.doctor.onboarding.infrastructure.remote.dto

import kotlinx.serialization.Serializable

// ── Response DTOs ─────────────────────────────────────────────────────────────

@Serializable
data class OnboardingStatusDto(
    val status: String,
)

@Serializable
data class UploadedDocDto(
    val id: String,
    val kind: String,
    val url: String,
)

// ── Request DTOs ──────────────────────────────────────────────────────────────

@Serializable
data class PersonalDataDto(
    val firstName: String,
    val lastName: String,
    val cmpLicense: String,
    val phone: String,
)

@Serializable
data class SubmitOnboardingRequestDto(
    val personalData: PersonalDataDto,
    val documents: List<UploadedDocRefDto>,
    val specialties: List<String>,
    val schedule: WeeklyScheduleDto,
    val prices: PriceConfigDto,
)

@Serializable
data class UploadedDocRefDto(
    val id: String,
    val kind: String,
    val url: String,
)

@Serializable
data class WeeklyScheduleDto(
    val slots: Map<String, List<Int>>,
    val minNoticeHours: Int = 24,
)

@Serializable
data class PriceConfigDto(
    val consultationFee: Double,
    val supportsPresential: Boolean,
    val supportsVirtual: Boolean,
)
