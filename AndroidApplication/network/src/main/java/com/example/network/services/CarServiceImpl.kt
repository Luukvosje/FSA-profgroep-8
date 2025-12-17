package com.example.network.services

import com.example.network.interfaces.services.CarService
import com.example.network.models.domain.Car
import com.example.network.models.domain.CarTCOResult
import com.example.network.models.remote.CalculateCarRequestDTO
import com.example.network.models.remote.CreateCarDTO
import com.example.network.models.remote.RemoteCalculateCar
import com.example.network.models.remote.UpdateCarDTO
import com.example.network.models.remote.RemoteCar
import com.example.network.models.remote.toDomainCar
import com.example.network.models.remote.toDomainCarTCOResult
import java.util.Date

internal class CarServiceImpl : BaseServiceImpl(), CarService {
    override suspend fun getAllCars(): ApiResult<List<Car>> {
        return safeExecute {
            get<List<RemoteCar>>("cars").map{ it.toDomainCar() }
        }
    }

    override suspend fun getAllAvailableCars(date: Date): ApiResult<List<Car>> {
        return safeExecute {
            get<List<RemoteCar>>("cars/available/$date").map{ it.toDomainCar() }
        }
    }

    override suspend fun getUserCars(userID: Int): ApiResult<List<Car>> {
        return safeExecute {
            get<List<RemoteCar>>("cars/users/$userID").map{ it.toDomainCar() }
        }
    }

    override suspend fun searchCars(keyword: String): ApiResult<List<Car>> {
        return safeExecute {
            get<List<RemoteCar>>("cars/search?keyword=$keyword").map{ it.toDomainCar() }
        }
    }

    // TODO(Filter Cars)

    override suspend fun getSingleCar(carID: Int): ApiResult<Car> {
        return safeExecute {
            get<RemoteCar>("cars/$carID").toDomainCar()
        }
    }

    override suspend fun getSingleCar(licensePlate: String): ApiResult<Car> {
        return safeExecute {
            get<RemoteCar>("cars/license/$licensePlate").toDomainCar()
        }
    }

    override suspend fun createCar(request: CreateCarDTO): ApiResult<Car> {
        return safeExecute {
            post<RemoteCar, CreateCarDTO>("cars", request).toDomainCar()
        }
    }

    override suspend fun calculateCarTCO(carID: Int, request: CalculateCarRequestDTO): ApiResult<CarTCOResult> {
        return safeExecute {
            put<RemoteCalculateCar, CalculateCarRequestDTO>("cars/$carID/calculate", request).toDomainCarTCOResult()
        }
    }

    override suspend fun updateCar(carID: Int, request: UpdateCarDTO): ApiResult<Car> {
        return safeExecute {
            put<RemoteCar, UpdateCarDTO>("cars/$carID", request).toDomainCar()
        }
    }

    // TODO(Upload Image)

    override suspend fun deleteCar(carID: Int): ApiResult<Boolean> {
        return safeExecute {
            delete<Boolean>("cars/$carID")
        }
    }
}
