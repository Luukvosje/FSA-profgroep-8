package com.profgroep8.models.entity

import com.profgroep8.models.domain.FuelType
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object CarEntity : IntIdTable(name = "Car", columnName = "CarID") {
    val licensePlate = varchar("LicencePlate", 10).uniqueIndex()
    val brand = varchar("Brand", 100)
    val model = varchar("Model", 100)
    val year = integer("Year")
    val fuelType = integer("FuelType")
    val price = integer("Price")
    val userID = integer("UserID").references(
        UserEntity.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
}
