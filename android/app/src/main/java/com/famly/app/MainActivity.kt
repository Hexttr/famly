package com.famly.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.famly.app.ui.FamlyViewModel
import com.famly.app.ui.navigation.FamlyNavHost
import com.famly.app.ui.theme.FamlyTheme

class MainActivity : ComponentActivity() {

    private var pendingQuickAddType by mutableStateOf<String?>(null)
    private var pendingJoinCode by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(0xFF1B4332.toInt()),
        )
        handleIntent(intent)

        val app = application as FamlyApplication
        setContent {
            val vm: FamlyViewModel = viewModel(
                factory = FamlyViewModel.Factory(
                    app.repository,
                    app.syncRepository,
                    app.billingManager,
                ),
            )
            val state by vm.uiState.collectAsState()
            FamlyTheme(darkTheme = state.settings.theme == "dark") {
                FamlyNavHost(
                    viewModel = vm,
                    pendingQuickAddType = pendingQuickAddType,
                    pendingJoinCode = pendingJoinCode,
                    onIntentHandled = {
                        pendingQuickAddType = null
                        pendingJoinCode = null
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_QUICK_ADD -> {
                pendingQuickAddType = intent.getStringExtra(EXTRA_QUICK_ADD_TYPE) ?: "expense"
            }
            Intent.ACTION_VIEW -> {
                val data: Uri? = intent.data
                when {
                    data?.scheme == "famly" && data.host == "join" ->
                        pendingJoinCode = data.getQueryParameter("code")
                    data?.host == "famly.app" && data.path?.startsWith("/join/") == true ->
                        pendingJoinCode = data.lastPathSegment
                }
            }
        }
    }

    companion object {
        const val ACTION_QUICK_ADD = "com.famly.app.action.QUICK_ADD"
        const val EXTRA_QUICK_ADD_TYPE = "quick_add_type"
    }
}
