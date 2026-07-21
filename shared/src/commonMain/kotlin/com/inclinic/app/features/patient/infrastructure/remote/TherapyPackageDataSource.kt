package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.PackageStatement
import com.inclinic.app.core.model.TherapyOffer
import com.inclinic.app.core.model.TherapyPackage

interface TherapyPackageDataSource {
    suspend fun getPatientPackages(patientId: String, status: String?): Result<List<TherapyPackage>>
    suspend fun getPackageDetail(packageId: String): Result<Pair<TherapyPackage, List<PackageSession>>>
    suspend fun getOffers(doctorId: String?): Result<List<TherapyOffer>>
    suspend fun getOfferDetail(offerId: String): Result<TherapyOffer>
    suspend fun getNegotiation(negotiationId: String): Result<PackageNegotiation>
    suspend fun createNegotiation(offerId: String, pricePerSession: Double, sessions: Int, message: String?): Result<PackageNegotiation>
    suspend fun respondNegotiation(negotiationId: String, action: String, pricePerSession: Double?, sessions: Int?, message: String?): Result<String?>
    suspend fun purchasePackage(offerId: String): Result<String>
    suspend fun getPackageStatement(packageId: String): Result<PackageStatement>

    /**
     * Registra un abono parcial. El backend recalcula el precio (la erosión del
     * descuento puede subir el total), así que tras abonar hay que recargar el
     * statement — por eso devuelve Unit y no un estado parcial.
     */
    suspend fun payPackageInstallment(packageId: String, amount: Double): Result<Unit>
}
