package com.profgroep8.interfaces.services

import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.RentalWithLocationsDTO
import com.profgroep8.models.dto.UpdateRentalDTO

interface RentalService {
    fun getAll(): List<RentalWithLocationsDTO>
    fun getSingle(rentalId: Int): RentalWithLocationsDTO
    fun create(item: CreateRentalDTO): RentalWithLocationsDTO
    fun update(rentalId: Int, item: UpdateRentalDTO): RentalWithLocationsDTO
    fun delete(rentalId: Int): Boolean
}