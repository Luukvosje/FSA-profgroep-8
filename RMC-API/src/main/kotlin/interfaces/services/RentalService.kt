package com.profgroep8.interfaces.services

import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.RentalWithLocationsDTO
import com.profgroep8.models.dto.UpdateRentalDTO

interface RentalService {
    fun getAll(): List<RentalWithLocationsDTO>
    fun getSingle(rentalID: Int): RentalWithLocationsDTO
    fun create(item: CreateRentalDTO, userID: Int): RentalWithLocationsDTO
    fun update(rentalID: Int, item: UpdateRentalDTO): RentalWithLocationsDTO
    fun delete(rentalID: Int): Boolean
}