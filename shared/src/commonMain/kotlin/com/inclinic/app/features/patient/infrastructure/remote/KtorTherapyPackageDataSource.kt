package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.NegotiationProposal
import com.inclinic.app.core.model.NegotiationStatus
import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.PackageStatement
import com.inclinic.app.core.model.PackageStatus
import com.inclinic.app.core.model.StatementPayment
import com.inclinic.app.core.model.StatementProjection
import com.inclinic.app.core.model.SessionStatus
import com.inclinic.app.core.model.TherapyOffer
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class KtorTherapyPackageDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : TherapyPackageDataSource {

    override suspend fun getPatientPackages(patientId: String, status: String?): Result<List<TherapyPackage>> = runCatching {
        client.get {
            url("$baseUrl/api/therapy-packages")
            parameter("patientId", patientId)
            status?.let { parameter("status", it) }
        }.body<ApiEnvelope<List<PackageDto>>>().data?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getPackageDetail(packageId: String): Result<Pair<TherapyPackage, List<PackageSession>>> = runCatching {
        val response = client.get {
            url("$baseUrl/api/therapy-packages/$packageId")
        }.body<ApiEnvelope<PackageDetailDto>>().data ?: error("Package not found")
        response.toDomain()
    }

    override suspend fun getOffers(doctorId: String?): Result<List<TherapyOffer>> = runCatching {
        client.get {
            url("$baseUrl/api/therapy-offers")
            doctorId?.let { parameter("doctorId", it) }
        }.body<ApiEnvelope<List<OfferDto>>>().data?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getOfferDetail(offerId: String): Result<TherapyOffer> = runCatching {
        client.get {
            url("$baseUrl/api/therapy-offers/$offerId")
        }.body<ApiEnvelope<OfferDto>>().data?.toDomain() ?: error("Offer not found")
    }

    override suspend fun getNegotiation(negotiationId: String): Result<PackageNegotiation> = runCatching {
        client.get {
            url("$baseUrl/api/package-negotiations/$negotiationId")
        }.body<ApiEnvelope<NegotiationDto>>().data?.toDomain() ?: error("Negotiation not found")
    }

    override suspend fun createNegotiation(
        offerId: String,
        pricePerSession: Double,
        sessions: Int,
        message: String?,
    ): Result<PackageNegotiation> = runCatching {
        // POST response carries only proposals (no offer/doctor relations), so we use
        // it just to obtain the created id, then re-fetch for a fully populated negotiation.
        val createdId = client.post {
            url("$baseUrl/api/package-negotiations")
            contentType(ContentType.Application.Json)
            setBody(CreateNegotiationBody(offerId = offerId, pricePerSession = pricePerSession, sessions = sessions, message = message))
        }.body<ApiEnvelope<NegotiationDto>>().data?.id ?: error("Failed to create negotiation")

        getNegotiation(createdId).getOrThrow()
    }

    override suspend fun respondNegotiation(
        negotiationId: String,
        action: String,
        pricePerSession: Double?,
        sessions: Int?,
        message: String?,
    ): Result<String?> = runCatching {
        client.post {
            url("$baseUrl/api/package-negotiations/$negotiationId/respond")
            contentType(ContentType.Application.Json)
            setBody(RespondNegotiationBody(action = action, pricePerSession = pricePerSession, sessions = sessions, message = message))
        }.body<ApiEnvelope<RespondResultDto>>().data?.therapyPackageId
    }

    override suspend fun purchasePackage(offerId: String): Result<String> = runCatching {
        client.post {
            url("$baseUrl/api/therapy-offers/$offerId/purchase")
        }.body<ApiEnvelope<PurchaseResultDto>>().data?.therapyPackageId ?: error("Purchase failed")
    }

    override suspend fun getPackageStatement(packageId: String): Result<PackageStatement> = runCatching {
        client.get {
            url("$baseUrl/api/therapy-packages/$packageId/statement")
        }.body<ApiEnvelope<PackageStatementDto>>().data?.toDomain() ?: error("Statement missing")
    }

    override suspend fun payPackageInstallment(packageId: String, amount: Double): Result<Unit> = runCatching {
        val response = client.post {
            url("$baseUrl/api/therapy-packages/$packageId/payments")
            contentType(ContentType.Application.Json)
            setBody(PackageInstallmentBodyDto(amount = amount))
        }
        val status = response.status.value
        if (status !in 200..299) {
            // El backend responde 400 con un mensaje accionable (entrada/abono mínimo);
            // lo propagamos tal cual para que la UI lo muestre.
            val err = runCatching { response.body<PackageInstallmentErrorDto>() }.getOrNull()
            error(err?.error ?: "No se pudo registrar el abono")
        }
        Unit
    }
}

// ── DTOs ──────────────────────────────────────────────────────────────────────

@Serializable
private data class CreateNegotiationBody(
    val offerId: String,
    val pricePerSession: Double,
    val sessions: Int,
    val message: String? = null,
)

@Serializable
private data class RespondNegotiationBody(
    val action: String,
    val pricePerSession: Double? = null,
    val sessions: Int? = null,
    val message: String? = null,
)

@Serializable
private data class RespondResultDto(
    val status: String = "",
    val therapyPackageId: String? = null,
)

@Serializable
private data class PurchaseResultDto(val therapyPackageId: String = "")

@Serializable
private data class PackageDoctorUserDto(
    val firstName: String? = null,
    val lastName: String? = null,
)

@Serializable
private data class PackageDoctorDto(
    val user: PackageDoctorUserDto? = null,
)

@Serializable
private data class PackageSpecialtyDto(
    val name: String? = null,
)

@Serializable
private data class PackageDto(
    val id: String,
    val offerId: String = "",
    val doctorId: String = "",
    val name: String = "",
    val totalSessions: Int = 0,
    val completedSessions: Int = 0,
    val pricePerSession: Double = 0.0,
    val totalPrice: Double = 0.0,
    val discount: Int = 0,
    val status: String = "ACTIVE",
    val paymentDeadline: String? = null,
    val createdAt: String? = null,
    val doctor: PackageDoctorDto? = null,
    val specialty: PackageSpecialtyDto? = null,
) {
    fun toDomain(): TherapyPackage {
        val now = Clock.System.now()
        val fn = doctor?.user?.firstName
        val ln = doctor?.user?.lastName
        val fullName = listOfNotNull(fn, ln).joinToString(" ").ifBlank { null }
        return TherapyPackage(
            id = id,
            offerId = offerId,
            doctorId = doctorId,
            doctorName = fullName,
            specialtyName = specialty?.name,
            name = name,
            totalSessions = totalSessions,
            completedSessions = completedSessions,
            pricePerSession = pricePerSession,
            totalPrice = totalPrice,
            discount = discount,
            status = runCatching { PackageStatus.valueOf(status) }.getOrDefault(PackageStatus.ACTIVE),
            paymentDeadline = paymentDeadline?.let { runCatching { Instant.parse(it) }.getOrNull() },
            createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrElse { now } } ?: now,
        )
    }
}

@Serializable
private data class SessionDto(
    val id: String? = null,
    val sessionNumber: Int = 0,
    val appointmentId: String? = null,
    val scheduledAt: String? = null,
    val visitType: String? = null,
    val status: String = "UNSCHEDULED",
) {
    fun toDomain(): PackageSession = PackageSession(
        id = id,
        sessionNumber = sessionNumber,
        appointmentId = appointmentId,
        scheduledAt = scheduledAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
        visitType = visitType?.let { runCatching { VisitType.valueOf(it) }.getOrNull() },
        status = runCatching { SessionStatus.valueOf(status) }.getOrDefault(SessionStatus.UNSCHEDULED),
    )
}

@Serializable
private data class PackageDetailDto(
    val id: String,
    val offerId: String = "",
    val doctorId: String = "",
    val name: String = "",
    val totalSessions: Int = 0,
    val completedSessions: Int = 0,
    val pricePerSession: Double = 0.0,
    val totalPrice: Double = 0.0,
    val discount: Int = 0,
    val status: String = "ACTIVE",
    val paymentDeadline: String? = null,
    val createdAt: String? = null,
    val doctor: PackageDoctorDto? = null,
    val specialty: PackageSpecialtyDto? = null,
    val sessions: List<SessionDto> = emptyList(),
) {
    fun toDomain(): Pair<TherapyPackage, List<PackageSession>> {
        val now = Clock.System.now()
        val fn = doctor?.user?.firstName
        val ln = doctor?.user?.lastName
        val fullName = listOfNotNull(fn, ln).joinToString(" ").ifBlank { null }
        val pkg = TherapyPackage(
            id = id,
            offerId = offerId,
            doctorId = doctorId,
            doctorName = fullName,
            specialtyName = specialty?.name,
            name = name,
            totalSessions = totalSessions,
            completedSessions = completedSessions,
            pricePerSession = pricePerSession,
            totalPrice = totalPrice,
            discount = discount,
            status = runCatching { PackageStatus.valueOf(status) }.getOrDefault(PackageStatus.ACTIVE),
            paymentDeadline = paymentDeadline?.let { runCatching { Instant.parse(it) }.getOrNull() },
            createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrElse { now } } ?: now,
        )
        return pkg to sessions.map { it.toDomain() }
    }
}

@Serializable
private data class OfferDto(
    val id: String,
    val doctorId: String = "",
    @SerialName("title") val name: String = "",
    @SerialName("totalSessions") val sessions: Int = 0,
    val visitTypes: List<String> = emptyList(),
    val description: String? = null,
    val pricePerSession: Double = 0.0,
    val originalPrice: Double? = null,
    val doctor: PackageDoctorDto? = null,
    val specialty: PackageSpecialtyDto? = null,
) {
    fun toDomain(): TherapyOffer {
        val fn = doctor?.user?.firstName
        val ln = doctor?.user?.lastName
        val fullName = listOfNotNull(fn, ln).joinToString(" ").ifBlank { null }
        return TherapyOffer(
            id = id,
            doctorId = doctorId,
            doctorName = fullName,
            specialtyName = specialty?.name,
            name = name,
            sessions = sessions,
            visitTypes = visitTypes.mapNotNull { runCatching { VisitType.valueOf(it) }.getOrNull() },
            description = description,
            pricePerSession = pricePerSession,
            originalPrice = originalPrice,
            // Backend has no per-offer "negotiable" flag; any active offer accepts
            // a negotiation (minPricePerSession is just the floor), so always true.
            isNegotiable = true,
        )
    }
}

@Serializable
private data class ProposalDto(
    val id: String,
    val proposedBy: String = "PATIENT",
    val pricePerSession: Double = 0.0,
    val sessions: Int = 0,
    val message: String? = null,
    val createdAt: String? = null,
) {
    fun toDomain(): NegotiationProposal {
        val now = Clock.System.now()
        return NegotiationProposal(
            id = id,
            proposedBy = proposedBy,
            pricePerSession = pricePerSession,
            sessions = sessions,
            message = message,
            createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrElse { now } } ?: now,
        )
    }
}

@Serializable
private data class NegotiationOfferDoctorDto(
    val user: PackageDoctorUserDto? = null,
)

@Serializable
private data class NegotiationOfferDto(
    val title: String? = null,
    val doctor: NegotiationOfferDoctorDto? = null,
)

@Serializable
private data class NegotiationDto(
    val id: String,
    val offerId: String = "",
    val status: String = "PENDING_DOCTOR",
    val finalPricePerSession: Double? = null,
    val finalSessions: Int? = null,
    val acceptedTherapyPackageId: String? = null,
    val offer: NegotiationOfferDto? = null,
    val proposals: List<ProposalDto> = emptyList(),
) {
    fun toDomain(): PackageNegotiation {
        val fn = offer?.doctor?.user?.firstName
        val ln = offer?.doctor?.user?.lastName
        val fullName = listOfNotNull(fn, ln).joinToString(" ").ifBlank { null }
        return PackageNegotiation(
            id = id,
            offerId = offerId,
            offerName = offer?.title,
            doctorName = fullName,
            status = runCatching { NegotiationStatus.valueOf(status) }.getOrDefault(NegotiationStatus.PENDING_DOCTOR),
            proposals = proposals.map { it.toDomain() },
            finalPricePerSession = finalPricePerSession,
            finalSessions = finalSessions,
            acceptedTherapyPackageId = acceptedTherapyPackageId,
        )
    }
}

// ── Progressive payment DTOs ──────────────────────────────────────────────────

@Serializable
private data class PackageInstallmentBodyDto(val amount: Double)

@Serializable
private data class PackageInstallmentErrorDto(val error: String? = null, val code: String? = null)

@Serializable
private data class StatementProjectionDto(
    val paymentsCount: Int = 0,
    val unitPrice: Double = 0.0,
    val total: Double = 0.0,
    val totalIncrease: Double = 0.0,
    val balanceAfterMinimum: Double = 0.0,
)

@Serializable
private data class StatementPaymentDto(
    val id: String = "",
    val amount: Double = 0.0,
    val paymentNumber: Int = 0,
    val unitPriceAtPayment: Double = 0.0,
    val totalAtPayment: Double = 0.0,
    val isEntry: Boolean = false,
    val createdAt: String? = null,
)

@Serializable
private data class PackageStatementDto(
    val packageId: String = "",
    val packageName: String = "",
    val status: String = "",
    val totalSessions: Int = 0,
    val paymentsCount: Int = 0,
    val unitPrice: Double = 0.0,
    val total: Double = 0.0,
    val amountPaid: Double = 0.0,
    val balance: Double = 0.0,
    val discount: Double = 0.0,
    val maxDiscount: Double = 0.0,
    val discountLost: Double = 0.0,
    val sessionsUnlocked: Int = 0,
    val sessionsUsed: Int = 0,
    val nextSession: Int = 0,
    val minimumNextPayment: Double = 0.0,
    val canScheduleNext: Boolean = false,
    val entryPercent: Double = 0.0,
    val entryAmount: Double = 0.0,
    val payoffAmount: Double = 0.0,
    val nextPaymentProjection: StatementProjectionDto? = null,
    val payments: List<StatementPaymentDto> = emptyList(),
) {
    fun toDomain(): PackageStatement {
        val now = Clock.System.now()
        return PackageStatement(
            packageId = packageId,
            packageName = packageName,
            status = status,
            totalSessions = totalSessions,
            paymentsCount = paymentsCount,
            unitPrice = unitPrice,
            total = total,
            amountPaid = amountPaid,
            balance = balance,
            discount = discount,
            maxDiscount = maxDiscount,
            discountLost = discountLost,
            sessionsUnlocked = sessionsUnlocked,
            sessionsUsed = sessionsUsed,
            nextSession = nextSession,
            minimumNextPayment = minimumNextPayment,
            canScheduleNext = canScheduleNext,
            entryPercent = entryPercent,
            entryAmount = entryAmount,
            payoffAmount = payoffAmount,
            nextPaymentProjection = nextPaymentProjection?.let {
                StatementProjection(
                    paymentsCount = it.paymentsCount,
                    unitPrice = it.unitPrice,
                    total = it.total,
                    totalIncrease = it.totalIncrease,
                    balanceAfterMinimum = it.balanceAfterMinimum,
                )
            },
            payments = payments.map {
                StatementPayment(
                    id = it.id,
                    amount = it.amount,
                    paymentNumber = it.paymentNumber,
                    unitPriceAtPayment = it.unitPriceAtPayment,
                    totalAtPayment = it.totalAtPayment,
                    isEntry = it.isEntry,
                    createdAt = it.createdAt?.let { d -> runCatching { Instant.parse(d) }.getOrElse { now } } ?: now,
                )
            },
        )
    }
}
