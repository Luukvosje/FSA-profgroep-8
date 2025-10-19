package com.profgroep8.models.domain

import com.profgroep8.models.dto.RentalDTO
import com.profgroep8.models.dto.RentalWithLocationsDTO
import com.profgroep8.models.entity.RentalEntity
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Rental(rentalId: EntityID<Int>) : IntEntity(rentalId) {
    companion object : IntEntityClass<Rental>(RentalEntity)

    var userId by RentalEntity.userId
    var carId by RentalEntity.carId
    var startRentalLocationId by RentalEntity.startRentalLocationId
    var endRentalLocationId by RentalEntity.endRentalLocationId
    var state by RentalEntity.state

    fun toRentalDTO(): RentalDTO {
        return RentalDTO(
            this.id.value,
            this.userId,
            this.carId,
            this.startRentalLocationId,
            this.endRentalLocationId,
            this.state
        )
    }

    fun toRentalWithLocationsDTO(startLocation: RentalLocation, endLocation: RentalLocation): RentalWithLocationsDTO {
        return RentalWithLocationsDTO(
            this.id.value,
            this.userId,
            this.carId,
            startLocation.toRentalLocationDTO(),
            endLocation.toRentalLocationDTO(),
            this.state
        )
    }
}

