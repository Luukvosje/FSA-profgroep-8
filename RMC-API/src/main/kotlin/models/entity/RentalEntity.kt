package com.profgroep8.models.entity

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Rentals : Table("Rental") {
    val rentalId = integer("RentalID").autoIncrement()
    override val primaryKey = PrimaryKey(rentalId)

    val UserId = integer("UserID").references(
        Users.UserId,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val CarId = integer("CarID").references(
        Cars.carId,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val startRentalLocationId = integer("StartRentalLocationID").references(
        RentalLocations.rentalLocationId,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val endRentalLocationId = integer("EndRentalLocationID").references(
        RentalLocations.rentalLocationId,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val state = integer("State")
}

object RentalLocations : Table("RentalLocation") {
    val rentalLocationId = integer("RentalLocationID").autoIncrement()
    override val primaryKey = PrimaryKey(rentalLocationId)

    val date = datetime("Date")
    val longitude = float("Longitude")
    val latitude = float("Latitude")
}