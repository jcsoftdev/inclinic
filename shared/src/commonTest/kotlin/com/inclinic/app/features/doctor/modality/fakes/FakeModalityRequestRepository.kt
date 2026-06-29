package com.inclinic.app.features.doctor.modality.fakes

import com.inclinic.app.features.doctor.modality.core.model.ModalityChangeRequest
import com.inclinic.app.features.doctor.modality.core.model.ModalityRequestStatus
import com.inclinic.app.features.doctor.modality.core.model.ModalityResponseAction
import com.inclinic.app.features.doctor.modality.core.port.ModalityRequestRepository

class FakeModalityRequestRepository : ModalityRequestRepository {

    var getRequestResult: Result<ModalityChangeRequest> = Result.success(stubModalityRequest("req-1"))
    var respondResult: Result<ModalityChangeRequest> =
        Result.success(stubModalityRequest("req-1", ModalityRequestStatus.APPROVED))

    var getRequestCallCount = 0
    var respondCallCount = 0

    var lastGetId: String? = null
    var lastRespondId: String? = null
    var lastRespondAction: ModalityResponseAction? = null
    var lastRespondPrice: Int? = null

    override suspend fun getRequest(id: String): Result<ModalityChangeRequest> {
        getRequestCallCount++
        lastGetId = id
        return getRequestResult
    }

    override suspend fun respond(
        id: String,
        action: ModalityResponseAction,
        adjustedPrice: Int?,
    ): Result<ModalityChangeRequest> {
        respondCallCount++
        lastRespondId = id
        lastRespondAction = action
        lastRespondPrice = adjustedPrice
        return respondResult
    }
}

fun stubModalityRequest(
    id: String,
    status: ModalityRequestStatus = ModalityRequestStatus.PENDING,
) = ModalityChangeRequest(
    id = id,
    patientName = "Luis Mendoza",
    patientSubtitle = "68 años · paciente recurrente",
    appointmentSlot = "Mar 18 · 11:30",
    currentModality = "Oficina",
    requestedModality = "Domicilio",
    reason = "Movilidad reducida tras la cirugía",
    address = "Av. La Marina 1450 · San Miguel",
    suggestedPrice = 180,
    status = status,
)
