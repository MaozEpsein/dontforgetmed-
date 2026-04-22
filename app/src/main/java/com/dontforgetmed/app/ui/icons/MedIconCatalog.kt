package com.dontforgetmed.app.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.ui.graphics.vector.ImageVector

object MedIconCatalog {

    data class Item(val key: String, val label: String, val icon: ImageVector)

    val items: List<Item> = listOf(
        Item("pill", "כדור", Icons.Default.Medication),
        Item("capsule", "קפסולה", Icons.Default.LocalPharmacy),
        Item("syringe", "זריקה", Icons.Default.Vaccines),
        Item("drops", "טיפות", Icons.Default.Opacity),
        Item("cream", "משחה", Icons.Default.Spa),
        Item("inhaler", "משאף", Icons.Default.MedicalServices),
        Item("syrup", "סירופ", Icons.Default.Science),
        Item("heart", "לב", Icons.Default.Favorite),
        Item("hospital", "רפואי", Icons.Default.LocalHospital),
    )

    private val byKey = items.associateBy { it.key }

    fun iconFor(key: String): ImageVector = byKey[key]?.icon ?: Icons.Default.Medication
    fun labelFor(key: String): String = byKey[key]?.label ?: items.first().label
    const val DEFAULT_KEY = "pill"
}
