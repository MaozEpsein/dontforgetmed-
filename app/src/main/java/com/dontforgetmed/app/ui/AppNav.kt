package com.dontforgetmed.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dontforgetmed.app.data.MedicationRepository
import com.dontforgetmed.app.ui.edit.EditMedicationScreen
import com.dontforgetmed.app.ui.edit.EditMedicationViewModel
import com.dontforgetmed.app.ui.home.HomeScreen
import com.dontforgetmed.app.ui.home.HomeViewModel
import com.dontforgetmed.app.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val EDIT = "edit/{medId}"
    const val SETTINGS = "settings"
    fun edit(medId: Long) = "edit/$medId"
}

@Composable
fun AppNav(repository: MedicationRepository) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
            HomeScreen(
                viewModel = vm,
                onAddMedication = { nav.navigate(Routes.edit(0L)) },
                onEditMedication = { id -> nav.navigate(Routes.edit(id)) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("medId") { type = NavType.LongType }),
        ) { backStack ->
            val medId = backStack.arguments?.getLong("medId") ?: 0L
            val ctx = LocalContext.current.applicationContext
            val vm: EditMedicationViewModel = viewModel(
                factory = EditMedicationViewModel.Factory(repository, ctx, medId)
            )
            EditMedicationScreen(viewModel = vm, onDone = { nav.popBackStack() })
        }
    }
}
