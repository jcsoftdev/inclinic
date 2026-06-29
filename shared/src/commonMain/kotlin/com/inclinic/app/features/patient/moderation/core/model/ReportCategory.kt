package com.inclinic.app.features.patient.moderation.core.model

/** Maps to the `category` field the backend expects: lowercase enum string. */
enum class ReportCategory(val apiValue: String) {
    Spam("spam"),
    Abuse("abuse"),
    Fraud("fraud"),
    Other("other"),
}
