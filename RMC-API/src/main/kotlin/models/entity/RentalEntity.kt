package com.profgroep8.models.entity

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object RentalEntity : IntIdTable("Rental") {

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
        RentalLocations.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val endRentalLocationId = integer("EndRentalLocationID").references(
        RentalLocations.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val state = integer("State")
}

object RentalLocations : IntIdTable("RentalLocation") {

    val date = datetime("Date")
    val longitude = float("Longitude")
    val latitude = float("Latitude")
}