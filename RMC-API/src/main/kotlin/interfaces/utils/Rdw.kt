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
    Unknown(0),
    Elektriciteit(1),
    Diesel(2),
    Benzine(3),
    Lpg(4),
    Waterstof(5);

    companion object {
        fun fromString(value: String): Int? {
            return values().find { it.name.equals(value, ignoreCase = true) }?.code
        }
    }
}