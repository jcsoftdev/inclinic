package com.inclinic.app.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class DisputeReason {
    DOCTOR_NO_SHOW,
    INADEQUATE_SERVICE,
    INCORRECT_CHARGE,
}
