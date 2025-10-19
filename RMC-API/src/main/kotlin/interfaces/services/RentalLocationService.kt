package com.profgroep8.interfaces.services

import com.profgroep8.models.dto.CreateRentalLocationDTO
import com.profgroep8.models.dto.RentalLocationDTO
import com.profgroep8.models.dto.UpdateRentalLocationDTO

interface RentalLocationService {
    fun getSingle(rentalLocationId: Int): RentalLocationDTO
    fun create(item: CreateRentalLocationDTO): RentalLocationDTO
    fun update(rentalLocationId: Int, item: UpdateRentalLocationDTO): RentalLocationDTO
}