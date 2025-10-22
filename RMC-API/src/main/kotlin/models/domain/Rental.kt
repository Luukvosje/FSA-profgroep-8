package com.profgroep8.models.domain

import com.profgroep8.models.dto.RentalDTO
import com.profgroep8.models.dto.RentalWithLocationsDTO
import com.profgroep8.models.entity.RentalEntity
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Rental(rentalId: EntityID<Int>) : IntEntity(rentalId) {
    companion object : IntEntityClass<Rental>(RentalEntity)

    var userID by RentalEntity.userID
    var carID by RentalEntity.carID
    var startRentalLocationID by RentalEntity.startRentalLocationID
    var endRentalLocationID by RentalEntity.endRentalLocationID
    var state by RentalEntity.state

    fun toRentalDTO(): RentalDTO {
        return RentalDTO(
            this.id.value,
            this.userID,
            this.carID,
            this.startRentalLocationID,
            this.endRentalLocationID,
            this.state
        )
    }

    fun toRentalWithLocationsDTO(startLocation: RentalLocation, endLocation: RentalLocation): RentalWithLocationsDTO {
        return RentalWithLocationsDTO(
            this.id.value,
            this.userID,
            this.carID,
            startLocation.toRentalLocationDTO(),
            endLocation.toRentalLocationDTO(),
            this.state
        )
    }
}

