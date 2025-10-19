package com.profgroep8.models.domain

import com.profgroep8.models.dto.RentalLocationDTO
import com.profgroep8.models.entity.RentalLocationsEntity
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class RentalLocation(rentalLocationId: EntityID<Int>) : IntEntity(rentalLocationId) {
    companion object : IntEntityClass<RentalLocation>(RentalLocationsEntity)

    var date by RentalLocationsEntity.date
    var longitude by RentalLocationsEntity.longitude
    var latitude by RentalLocationsEntity.latitude

    fun toRentalLocationDTO(): RentalLocationDTO {
        return RentalLocationDTO(
            this.id.value,
            this.date,
            this.longitude,
            this.latitude
        )
    }
}

