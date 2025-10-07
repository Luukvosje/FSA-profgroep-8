package com.profgroep8.models.entity

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Cars : Table("Car") {
    val carId = integer("CarID").autoIncrement()
    override val primaryKey = PrimaryKey(carId)

    val licensePlate = varchar("LicencePlate", 10).uniqueIndex()
    val brand = varchar("Brand", 100)
    val model = varchar("Model", 100)
    val year = integer("Year")

    val fuelType = integer("FuelType")
    val price = integer("PriceInCents")

    val userId = integer("UserID").references(
        Users.UserId,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
}