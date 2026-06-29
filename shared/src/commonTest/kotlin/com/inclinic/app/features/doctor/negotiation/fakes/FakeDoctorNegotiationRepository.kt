package com.inclinic.app.features.doctor.negotiation.fakes

import com.inclinic.app.features.doctor.negotiation.core.model.NegotiationAction
import com.inclinic.app.features.doctor.negotiation.core.model.PackageNegotiation
import com.inclinic.app.features.doctor.negotiation.core.model.PackageNegotiationStatus
import com.inclinic.app.features.doctor.negotiation.core.port.DoctorNegotiationRepository

class FakeDoctorNegotiationRepository : DoctorNegotiationRepository {

    var getResult: Result<PackageNegotiation> = Result.success(stubNegotiation("neg-1"))
    var respondResult: Result<PackageNegotiation> =
        Result.success(stubNegotiation("neg-1", PackageNegotiationStatus.ACCEPTED))

    var getCallCount = 0
    var respondCallCount = 0

    var lastGetId: String? = null
    var lastRespondId: String? = null
    var lastRespondAction: NegotiationAction? = null
    var lastRespondCounterPriceCents: Int? = null

    override suspend fun getNegotiation(id: String): Result<PackageNegotiation> {
        getCallCount++
        lastGetId = id
        return getResult
    }

    override suspend fun respondNegotiation(
        id: String,
        action: NegotiationAction,
        counterPriceCents: Int?,
    ): Result<PackageNegotiation> {
        respondCallCount++
        lastRespondId = id
        lastRespondAction = action
        lastRespondCounterPriceCents = counterPriceCents
        return respondResult
    }
}

fun stubNegotiation(
    id: String,
    status: PackageNegotiationStatus = PackageNegotiationStatus.PENDING,
    originalPriceCents: Int = 12000,
    proposedPriceCents: Int = 10000,
    message: String? = "Doctor, mi presupuesto está ajustado este mes.",
) = PackageNegotiation(
    id = id,
    patientName = "Patricia Vega",
    packageName = "Control Diabetes Tipo 2",
    originalPriceCents = originalPriceCents,
    proposedPriceCents = proposedPriceCents,
    message = message,
    status = status,
)
