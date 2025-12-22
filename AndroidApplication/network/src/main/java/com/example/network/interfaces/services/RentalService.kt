package com.example.network.interfaces.services

import com.example.network.models.domain.Car
import com.example.network.models.domain.CarTCOResult
import com.example.network.models.domain.Rental
import com.example.network.models.domain.RentalLocation
import com.example.network.models.domain.RentalLocations
import com.example.network.models.domain.RentalWithLocations
import com.example.network.models.remote.CalculateCarRequestDTO
import com.example.network.models.remote.CreateCarDTO
import com.example.network.models.remote.CreateRentalDTO
import com.example.network.models.remote.RemoteCar
import com.example.network.models.remote.UpdateCarDTO
import com.example.network.models.remote.UpdateRentalDTO
import com.example.network.models.remote.UpdateRentalLocationDTO
import com.example.network.services.ApiResult
import java.util.Date

interface RentalService {
    suspend fun getAllRentals(): ApiResult<List<RentalWithLocations>>
    suspend fun getSingleRental(rentalID: Int): ApiResult<RentalWithLocations>
    suspend fun getRentalLocations(rentalID: Int): ApiResult<RentalLocations>
    suspend fun createRental(request: CreateRentalDTO): ApiResult<RentalWithLocations>
    suspend fun updateRental(rentalID: Int, request: UpdateRentalDTO): ApiResult<RentalWithLocations>
    suspend fun endRental(rentalID: Int): ApiResult<RentalWithLocations>
    suspend fun updateRentalLocation(rentalID: Int, locationID: Int, request: UpdateRentalLocationDTO): ApiResult<RentalLocation>
    suspend fun deleteRental(rentalID: Int): ApiResult<Boolean>
}