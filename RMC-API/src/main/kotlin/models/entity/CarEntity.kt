package com.profgroep8.models.entity

import com.profgroep8.models.domain.FuelType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object CarEntity : IntIdTable("Car") {
    val licensePlate = varchar("LicencePlate", 10).uniqueIndex()
    val brand = varchar("Brand", 100)
    val model = varchar("Model", 100)
    val year = integer("Year")

    val fuelType = enumerationByName<FuelType>("FuelType", 100)
    val price = integer("PriceInCents")

    val userId = integer("UserID").references(
        UserEntity.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
}