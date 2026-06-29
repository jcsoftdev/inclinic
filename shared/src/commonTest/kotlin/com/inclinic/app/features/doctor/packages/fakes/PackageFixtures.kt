package com.inclinic.app.features.doctor.packages.fakes

import com.inclinic.app.features.doctor.packages.core.model.PackageStatus
import com.inclinic.app.features.doctor.packages.core.model.TherapyPackage
import com.inclinic.app.features.doctor.packages.core.port.NewPackageDraft

fun samplePackage(
    id: String = "pkg-1",
    packageName: String = "Cardio Premium",
    status: PackageStatus = PackageStatus.ACTIVE,
): TherapyPackage = TherapyPackage(
    id = id,
    patientId = "pat-1",
    patientName = "Roberto Valdez",
    patientEmail = "r.valdez@gmail.com",
    specialtyId = "spe-1",
    specialtyName = "Cardiología",
    packageName = packageName,
    totalSessions = 10,
    regularPricePerSession = 150.0,
    packagePricePerSession = 120.0,
    isPrepaid = true,
    prepaidDiscount = 15.0,
    totalPrepaidAmount = 1020.0,
    sessionsCompleted = 1,
    sessionsScheduled = 1,
    sessionsUsed = 1,
    status = status,
    sessions = emptyList(),
)

fun sampleDraft(
    patientId: String = "pat-1",
    packageName: String = "Plan nutricional",
): NewPackageDraft = NewPackageDraft(
    patientId = patientId,
    specialtyId = "spe-1",
    packageName = packageName,
    totalSessions = 12,
    regularPricePerSession = 70.0,
    packagePricePerSession = 60.0,
    isPrepaid = true,
    prepaidDiscount = 15.0,
    isHomeVisit = false,
)
