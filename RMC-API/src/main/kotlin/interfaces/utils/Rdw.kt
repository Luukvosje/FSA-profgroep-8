package com.profgroep8.interfaces.utils

import kotlinx.serialization.Serializable

@Serializable
data class RdwVehicle(
    val kenteken: String,
    val merk: String,
    val handelsbenaming: String,
    val datum_eerste_toelating_dt: String,
)

@Serializable
data class RdwFuel(
    val kenteken: String,
    val brandstof_omschrijving: String,
)

enum class RdwFuelTypes(val code: Int) {
    Unknown(-1),
    Elektriciteit(2),
    Diesel(1),
    Benzine(0),
    Lpg(3),
    Waterstof(3);

    companion object {
        fun fromString(value: String): Int? {
            return values().find { it.name.equals(value, ignoreCase = true) }?.code
        }
    }
}