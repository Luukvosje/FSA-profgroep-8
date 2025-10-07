package com.profgroep8.models.entity

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object RentalEntity : IntIdTable("Rental", "RentalID") {

    val UserId = integer("UserID").references(
        UserEntity.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val CarId = integer("CarID").references(
        RentalEntity.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val startRentalLocationId = integer("StartRentalLocationID").references(
        RentalLocationsEntity.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val endRentalLocationId = integer("EndRentalLocationID").references(
        RentalLocationsEntity.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val state = integer("State")
}

object RentalLocationsEntity : IntIdTable("RentalLocation", "RentalLocationID") {
    val date = datetime("Date")
    val longitude = float("Longitude")
    val latitude = float("Latitude")
}