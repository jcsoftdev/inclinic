package com.inclinic.app.features.doctor.onboarding.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.DoctorOnboardingDraft
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.dto.OnboardingStatusDto
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.dto.PersonalDataDto
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.dto.PriceConfigDto
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.dto.SubmitOnboardingRequestDto
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.dto.UploadedDocDto
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.dto.UploadedDocRefDto
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.dto.WeeklyScheduleDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class KtorDoctorOnboardingDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorOnboardingDataSource {

    override suspend fun getStatus(): Result<OnboardingStatusDto> = runCatching {
        client.get {
            url("$baseUrl/api/v1/doctor/onboarding/status")
        }.body<ApiEnvelope<OnboardingStatusDto>>().data ?: error("Onboarding status data missing")
    }

    override suspend fun uploadDocument(
        file: ByteArray,
        fileName: String,
        kind: DocKind,
    ): Result<UploadedDocDto> = runCatching {
        val response = client.post {
            url("$baseUrl/api/v1/doctor/onboarding/documents")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("kind", kind.name)
                        append(
                            "file",
                            file,
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                            },
                        )
                    }
                )
            )
        }
        response.body<ApiEnvelope<UploadedDocDto>>().data ?: error("Upload response missing data")
    }

    override suspend fun submit(draft: DoctorOnboardingDraft): Result<Unit> = runCatching {
        val response = client.post {
            url("$baseUrl/api/v1/doctor/onboarding/submit")
            contentType(ContentType.Application.Json)
            setBody(draft.toDto())
        }
        val status = response.status
        if (status != HttpStatusCode.Accepted && status.value !in 200..299) {
            error("Submit failed with status ${status.value}")
        }
    }

    override suspend fun resubmit(corrections: Map<String, String>): Result<Unit> = runCatching {
        val response = client.post {
            url("$baseUrl/api/v1/doctor/onboarding/resubmit")
            contentType(ContentType.Application.Json)
            setBody(corrections)
        }
        val status = response.status
        if (status != HttpStatusCode.Accepted && status.value !in 200..299) {
            error("Resubmit failed with status ${status.value}")
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private fun DoctorOnboardingDraft.toDto() = SubmitOnboardingRequestDto(
        personalData = PersonalDataDto(
            firstName = personalData.firstName,
            lastName = personalData.lastName,
            cmpLicense = personalData.cmpLicense,
            phone = personalData.phone,
        ),
        documents = documents.map { doc ->
            UploadedDocRefDto(id = doc.id, kind = doc.kind.name, url = doc.url)
        },
        specialties = specialties,
        schedule = WeeklyScheduleDto(slots = schedule.slots, minNoticeHours = schedule.minNoticeHours),
        prices = PriceConfigDto(
            consultationFee = prices.consultationFee,
            supportsPresential = prices.supportsPresential,
            supportsVirtual = prices.supportsVirtual,
        ),
    )
}
