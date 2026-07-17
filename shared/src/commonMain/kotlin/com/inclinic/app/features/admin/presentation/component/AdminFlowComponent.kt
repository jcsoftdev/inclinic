package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.notifications.presentation.component.AdminNotificationsComponent

interface AdminFlowComponent {
    val currentTab: Value<AdminTab>
    val inicioStack: Value<ChildStack<*, Child>>
    val citasStack: Value<ChildStack<*, Child>>
    val doctoresStack: Value<ChildStack<*, Child>>
    val disputasStack: Value<ChildStack<*, Child>>
    val masStack: Value<ChildStack<*, Child>>

    fun onTabSelected(tab: AdminTab)

    sealed interface Child {
        // Inicio tab
        class Dashboard(val component: AdminDashboardComponent) : Child
        class Notifications(val component: AdminNotificationsComponent) : Child
        class Finance(val component: AdminFinanceComponent) : Child

        // Citas tab
        class Appointments(val component: AdminAppointmentsComponent) : Child
        class AppointmentDetail(val component: AdminAppointmentDetailComponent) : Child

        // Doctores tab
        class DoctoresRoot(val component: AdminDoctorsComponent) : Child
        class DoctorDetail(val component: AdminDoctorDetailComponent) : Child
        class PendingDoctors(val component: AdminPendingDoctorsComponent) : Child
        class PendingDoctorDetail(val component: AdminPendingDoctorDetailComponent) : Child

        // Disputas tab
        class DisputasRoot(val component: AdminDisputasComponent) : Child
        class ResolveDispute(val component: AdminResolveDisputeComponent) : Child
        class ResolveNoShow(val component: AdminResolveNoShowComponent) : Child

        // Más tab
        class MasMenu(val component: AdminMasMenuComponent) : Child
        // Patients lane
        class MasPatients(val component: AdminPatientsComponent) : Child
        class MasPatientDetail(val component: AdminPatientDetailComponent) : Child
        class MasSuspendUser(val component: AdminSuspendUserComponent) : Child
        class MasSpecialties(val component: AdminSpecialtiesComponent) : Child
        class MasSpecialtyRequests(val component: AdminSpecialtyRequestsComponent) : Child
        class MasReports(val component: AdminReportsComponent) : Child
        class MasResolveReport(val component: AdminResolveReportComponent) : Child
        class MasReviews(val component: AdminReviewsComponent) : Child
        class MasBlockedEmails(val component: AdminBlockedEmailsComponent) : Child
        class MasSubscriptions(val component: AdminSubscriptionsComponent) : Child
        class MasProfile(val component: AdminProfileComponent) : Child
        class MasNotifications(val component: AdminNotificationsComponent) : Child
        class MasConfigScreen(val component: AdminConfigComponent) : Child
        class MasSecurity(val component: com.inclinic.app.features.admin.twofactor.presentation.component.AdminSecurityComponent) : Child
        class MasTwoFactorSetup(val component: com.inclinic.app.features.admin.twofactor.presentation.component.AdminTwoFactorSetupComponent) : Child
        class MasPatientAppointments(val component: AdminPatientAppointmentsComponent) : Child
    }

    sealed interface Output {
        data object Logout : Output
    }
}
