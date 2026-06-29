package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.PackageSession
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
}
