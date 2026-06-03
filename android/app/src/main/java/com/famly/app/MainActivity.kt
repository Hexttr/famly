package com.famly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.famly.app.ui.FamlyViewModel
import com.famly.app.ui.navigation.FamlyNavHost
import com.famly.app.ui.theme.FamlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                FamlyNavHost(vm)
            }
        }
    }
}
