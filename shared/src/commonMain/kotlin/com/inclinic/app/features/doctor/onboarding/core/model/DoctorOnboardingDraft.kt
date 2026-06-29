package com.inclinic.app.features.doctor.onboarding.core.model

/**
 * Aggregate draft that the doctor fills out during the onboarding flow.
 * Submitted as-is to the backend via [SubmitOnboardingUseCase].
 */
data class DoctorOnboardingDraft(
    val personalData: PersonalData,
    val documents: List<UploadedDoc>,
    val specialties: List<String>,      // specialty ids
    val schedule: WeeklySchedule,
    val prices: PriceConfig,
)

data class PersonalData(
    val firstName: String,
    val lastName: String,
    val cmpLicense: String,
    val phone: String,
)

data class UploadedDoc(
    val id: String,
    val kind: DocKind,
    val url: String,
)

/**
 * Weekly availability grid.
 * Keys are [DayOfWeek.name] strings (e.g. "MONDAY"); values are hour slots (8..20).
 */
data class WeeklySchedule(
    val slots: Map<String, List<Int>>,
    val minNoticeHours: Int = 24,
)

data class PriceConfig(
    val consultationFee: Double,
    val supportsPresential: Boolean,
    val supportsVirtual: Boolean,
)
