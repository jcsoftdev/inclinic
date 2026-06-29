package com.inclinic.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.inclinic.app.core.navigation.DeepLink
import com.inclinic.app.core.navigation.RootComponent
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform

class MainActivity : ComponentActivity() {

    private lateinit var root: RootComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val componentContext = defaultComponentContext()
        root = KoinPlatform.getKoin().get<RootComponent> { parametersOf(componentContext) }

        // Handle deep link that opened this Activity cold.
        intent?.data?.let { uri -> handleUri(uri) }

        setContent {
            App(rootComponent = root)
        }
    }

    /**
     * Handle deep links received while the Activity is already running
     * (android:launchMode="singleTask" or singleTop).
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri -> handleUri(uri) }
    }

    private fun handleUri(uri: Uri) {
        if (uri.scheme != "inclinic") return
        val deepLink: DeepLink = when (uri.host) {
            "reset-password" -> {
                val token = uri.getQueryParameter("token") ?: return
                DeepLink.ResetPassword(token)
            }
            "appointments" -> {
                // Expected path: /appointments/{id}  or  host=appointments path=/{id}
                val appointmentId = uri.pathSegments.firstOrNull()
                    ?: uri.getQueryParameter("id")
                    ?: return
                DeepLink.AppointmentDetail(appointmentId)
            }
            else -> return
        }
        root.handleDeepLink(deepLink)
    }
}
