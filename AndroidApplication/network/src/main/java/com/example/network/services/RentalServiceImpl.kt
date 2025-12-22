package com.example.network.services

import com.example.network.interfaces.services.RentalService
import com.example.network.models.domain.Rental
import com.example.network.models.domain.RentalLocation
import com.example.network.models.domain.RentalLocations
import com.example.network.models.domain.RentalWithLocations
import com.example.network.models.remote.CreateRentalDTO
import com.example.network.models.remote.RemoteRental
import com.example.network.models.remote.RemoteRentalLocation
import com.example.network.models.remote.RemoteRentalLocations
import com.example.network.models.remote.RemoteRentalWithLocations
import com.example.network.models.remote.UpdateRentalDTO
import com.example.network.models.remote.UpdateRentalLocationDTO
import com.example.network.models.remote.toDomainRental
import com.example.network.models.remote.toDomainRentalLocation
import com.example.network.models.remote.toDomainRentalLocations

internal class RentalServiceImpl : BaseServiceImpl(), RentalService {
    val baseUrl = "rentals"

    override suspend fun getAllRentals(): ApiResult<List<RentalWithLocations>> {
        return safeExecute {
            get<List<RemoteRentalWithLocations>>(baseUrl).map{ it.toDomainRental() }
        }
    }

    override suspend fun getSingleRental(rentalID: Int): ApiResult<RentalWithLocations> {
        return safeExecute {
            get<RemoteRentalWithLocations>("$baseUrl/$rentalID").toDomainRental()
        }
    }

    override suspend fun getRentalLocations(rentalID: Int): ApiResult<RentalLocations> {
        return safeExecute {
            get<RemoteRentalLocations>("$baseUrl/$rentalID/locations").toDomainRentalLocations()
        }
    }

    override suspend fun createRental(request: CreateRentalDTO): ApiResult<RentalWithLocations> {
        return safeExecute {
            post<RemoteRentalWithLocations, CreateRentalDTO>(baseUrl, request).toDomainRental()
        }
    }

    override suspend fun updateRental(rentalID: Int, request: UpdateRentalDTO): ApiResult<RentalWithLocations> {
        return safeExecute {
            put<RemoteRentalWithLocations, UpdateRentalDTO>("$baseUrl/$rentalID", request).toDomainRental()
        }
    }

    override suspend fun endRental(rentalID: Int): ApiResult<RentalWithLocations> {
        return safeExecute {
            put<RemoteRentalWithLocations, Boolean>("$baseUrl/$rentalID/end", true).toDomainRental()
        }
    }

    override suspend fun updateRentalLocation(rentalID: Int, locationID: Int, request: UpdateRentalLocationDTO): ApiResult<RentalLocation> {
        return safeExecute {
            put<RemoteRentalLocation, UpdateRentalLocationDTO>("$baseUrl/$rentalID/locations/$locationID", request).toDomainRentalLocation()
        }
    }

    override suspend fun deleteRental(rentalID: Int): ApiResult<Boolean> {
        return safeExecute {
            delete<Boolean>("$baseUrl/$rentalID")
        }
    }
}
