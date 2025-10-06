package com.profgroep8.Data.Tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object RentalTable : Table("Rental") {
    val rentalId = integer("RentalID").autoIncrement()
    override val primaryKey = PrimaryKey(rentalId)

    val UserId = integer("UserID").references(
        UserTable.UserId,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val CarId = integer("CarID").references(
        CarTable.carId,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val startRentalLocationId = integer("StartRentalLocationID").references(
        RentalLocationTable.rentalLocationId,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val endRentalLocationId = integer("EndRentalLocationID").references(
        RentalLocationTable.rentalLocationId,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val state = integer("State")
}

object RentalLocationTable : Table("RentalLocation") {
    val rentalLocationId = integer("RentalLocationID").autoIncrement()
    override val primaryKey = PrimaryKey(rentalLocationId)

    val date = datetime("Date")
    val longitude = float("Longitude")
    val latitude = float("Latitude")
}