package com.dontforgetmed.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.dontforgetmed.app.ui.home.HomeScreen
import com.dontforgetmed.app.ui.home.HomeViewModel
import com.dontforgetmed.app.ui.theme.DontForgetMedTheme

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory((application as DontForgetMedApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DontForgetMedTheme {
                HomeScreen(
                    viewModel = homeViewModel,
                    onAddMedication = { /* TODO in Phase 4 */ },
                )
            }
        }
    }
}
