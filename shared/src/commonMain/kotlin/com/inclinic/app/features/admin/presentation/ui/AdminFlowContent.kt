package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.admin.notifications.presentation.ui.AdminNotificationsScreen
import com.inclinic.app.features.admin.presentation.component.AdminFlowComponent
import com.inclinic.app.features.admin.twofactor.presentation.ui.AdminSecurityScreen
import com.inclinic.app.features.admin.twofactor.presentation.ui.AdminTwoFactorSetupScreen
import com.inclinic.app.features.admin.presentation.component.AdminTab
import com.inclinic.app.ui.atoms.AdminNavBar

@Composable
fun AdminFlowContent(component: AdminFlowComponent, modifier: Modifier = Modifier) {
    val currentTab by component.currentTab.subscribeAsState()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            AdminNavBar(
                currentTab = currentTab,
                onTabSelected = component::onTabSelected,
            )
        },
    ) { padding ->
        val activeStack = when (currentTab) {
            AdminTab.Inicio -> component.inicioStack
            AdminTab.Citas -> component.citasStack
            AdminTab.Doctores -> component.doctoresStack
            AdminTab.Disputas -> component.disputasStack
            AdminTab.Mas -> component.masStack
        }

        Children(
            stack = activeStack,
            modifier = Modifier.padding(padding),
            animation = stackAnimation(slide()),
        ) { child ->
            when (val c = child.instance) {
                is AdminFlowComponent.Child.Dashboard -> AdminDashboardScreen(c.component)
                is AdminFlowComponent.Child.Finance -> AdminFinanceScreen(c.component)
                is AdminFlowComponent.Child.Notifications -> AdminNotificationsScreen(c.component)
                is AdminFlowComponent.Child.DoctorApprovals -> AdminPlaceholderScreen(c.component)
                is AdminFlowComponent.Child.Appointments -> AdminAppointmentsScreen(c.component)
                is AdminFlowComponent.Child.AppointmentDetail -> AdminAppointmentDetailScreen(c.component)
                is AdminFlowComponent.Child.DoctoresRoot -> AdminDoctorsScreen(c.component)
                is AdminFlowComponent.Child.DoctorDetail -> AdminDoctorDetailScreen(c.component)
                is AdminFlowComponent.Child.PendingDoctors -> AdminPendingDoctorsScreen(c.component)
                is AdminFlowComponent.Child.PendingDoctorDetail -> AdminPendingDoctorDetailScreen(c.component)
                is AdminFlowComponent.Child.DisputasRoot -> AdminDisputasScreen(c.component)
                is AdminFlowComponent.Child.ResolveDispute -> AdminResolveDisputeScreen(c.component)
                is AdminFlowComponent.Child.ResolveNoShow -> AdminResolveNoShowScreen(c.component)
                is AdminFlowComponent.Child.MasMenu -> AdminMasMenuScreen(c.component)
                is AdminFlowComponent.Child.MasPatients -> AdminPatientsScreen(c.component)
                is AdminFlowComponent.Child.MasPatientDetail -> AdminPatientDetailScreen(c.component)
                is AdminFlowComponent.Child.MasSuspendUser -> AdminSuspendUserScreen(c.component)
                is AdminFlowComponent.Child.MasSpecialties -> AdminSpecialtiesScreen(c.component)
                is AdminFlowComponent.Child.MasSpecialtyRequests -> AdminSpecialtyRequestsScreen(c.component)
                is AdminFlowComponent.Child.MasReports -> AdminReportsScreen(c.component)
                is AdminFlowComponent.Child.MasResolveReport -> AdminResolveReportScreen(c.component)
                is AdminFlowComponent.Child.MasReviews -> AdminReviewsScreen(c.component)
                is AdminFlowComponent.Child.MasBlockedEmails -> AdminBlockedEmailsScreen(c.component)
                is AdminFlowComponent.Child.MasSubscriptions -> AdminSubscriptionsScreen(c.component)
                is AdminFlowComponent.Child.MasProfile -> AdminProfileScreen(c.component)
                is AdminFlowComponent.Child.MasNotifications -> AdminNotificationsScreen(c.component)
                is AdminFlowComponent.Child.MasConfigScreen -> AdminConfigScreen(c.component)
                is AdminFlowComponent.Child.MasSecurity -> AdminSecurityScreen(c.component)
                is AdminFlowComponent.Child.MasTwoFactorSetup -> AdminTwoFactorSetupScreen(c.component)
            }
        }
    }
}
