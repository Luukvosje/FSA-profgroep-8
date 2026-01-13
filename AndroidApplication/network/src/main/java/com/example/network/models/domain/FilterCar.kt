package com.example.network.models.domain

import kotlinx.serialization.Serializable

@Serializable
data class FilterCar(
    val sortOrder: String? = null,
    val licensePlate: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val fuelType: Int? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
) {
    fun toSearchValues(): FilterCar = this.copy(
        licensePlate = this.licensePlate?.lowercase()?.replace("-", "")?.trim(),
        brand = this.brand?.lowercase()?.trim(),
        model = this.model?.lowercase()?.trim(),
    )
}
enum class FilterSortOrder(val sortString: String) {
    Year("year"),
    FuelType("fuelType"),
    Model("model"),
    Brand("brand"),
    price("price"),
    nothing("");

    companion object {
        fun fromString(sortString: String): FilterSortOrder {
            return values().find { it.sortString == sortString }
                ?: nothing
        }
    }
}