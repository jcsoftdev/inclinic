package com.inclinic.app.ui.atoms

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.UserRound
import com.composables.icons.lucide.Users
import com.inclinic.app.features.doctor.presentation.component.DoctorTab
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * RED → GREEN tests for [doctorTabIcon].
 *
 * Pure function extracted from DoctorNavBar to enable unit testing in commonTest.
 * Validates that every DoctorTab maps to the correct Lucide ImageVector per Pencil design.
 */
class DoctorNavBarTest {

    @Test
    fun every_tab_maps_to_a_non_null_icon() {
        DoctorTab.entries.forEach { tab ->
            assertNotNull(doctorTabIcon(tab), "Icon for $tab must not be null")
        }
    }

    @Test
    fun inicio_maps_to_house_icon() {
        assertEquals(Lucide.House, doctorTabIcon(DoctorTab.Inicio))
    }

    @Test
    fun agenda_maps_to_calendar_icon() {
        assertEquals(Lucide.Calendar, doctorTabIcon(DoctorTab.Agenda))
    }

    @Test
    fun pacientes_maps_to_users_icon() {
        assertEquals(Lucide.Users, doctorTabIcon(DoctorTab.Pacientes))
    }

    @Test
    fun mensajes_maps_to_message_circle_icon() {
        assertEquals(Lucide.MessageCircle, doctorTabIcon(DoctorTab.Mensajes))
    }

    @Test
    fun perfil_maps_to_user_round_icon() {
        assertEquals(Lucide.UserRound, doctorTabIcon(DoctorTab.Perfil))
    }

    @Test
    fun all_tabs_have_distinct_icons() {
        val icons: List<ImageVector> = DoctorTab.entries.map { doctorTabIcon(it) }
        assertEquals(DoctorTab.entries.size, icons.toSet().size, "Each tab must have a unique icon")
    }
}
