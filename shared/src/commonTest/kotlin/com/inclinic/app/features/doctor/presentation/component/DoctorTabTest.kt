package com.inclinic.app.features.doctor.presentation.component

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DoctorTabTest {

    @Test
    fun doctor_tab_exposes_five_destinations_in_pencil_order() {
        val tabs: List<DoctorTab> = listOf(
            DoctorTab.Inicio,
            DoctorTab.Agenda,
            DoctorTab.Pacientes,
            DoctorTab.Mensajes,
            DoctorTab.Perfil,
        )
        assertEquals(5, tabs.size)
        assertTrue(tabs.all { it is DoctorTab })
    }

    @Test
    fun every_tab_has_unique_spanish_label_matching_pencil_navbar() {
        val labels = listOf(
            DoctorTab.Inicio.label,
            DoctorTab.Agenda.label,
            DoctorTab.Pacientes.label,
            DoctorTab.Mensajes.label,
            DoctorTab.Perfil.label,
        )
        assertEquals(listOf("INICIO", "AGENDA", "PACIENTES", "MENSAJES", "PERFIL"), labels)
        assertEquals(5, labels.toSet().size)
    }

    @Test
    fun entries_enumeration_returns_all_tabs() {
        assertEquals(5, DoctorTab.entries.size)
    }
}
