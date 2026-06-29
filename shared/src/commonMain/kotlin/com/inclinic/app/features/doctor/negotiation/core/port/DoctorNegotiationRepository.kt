package com.inclinic.app.features.doctor.negotiation.core.port

import com.inclinic.app.features.doctor.negotiation.core.model.NegotiationAction
import com.inclinic.app.features.doctor.negotiation.core.model.PackageNegotiation

interface DoctorNegotiationRepository {
    suspend fun getNegotiation(id: String): Result<PackageNegotiation>
    suspend fun respondNegotiation(id: String, action: NegotiationAction, counterPriceCents: Int?): Result<PackageNegotiation>
}
