package com.example.network.interfaces.services

import com.example.network.models.domain.AvailableCar
import com.example.network.models.domain.Car
import com.example.network.models.domain.CarTCOResult
import com.example.network.models.domain.FilterCar
import com.example.network.models.remote.CalculateCarRequestDTO
import com.example.network.models.remote.CreateCarDTO
import com.example.network.models.remote.UpdateCarDTO
import com.example.network.services.ApiResult
import kotlinx.datetime.LocalDate
import java.io.File

interface CarService {
    suspend fun getAllCars(): ApiResult<List<Car>>
    suspend fun getAllAvailableCars(date: LocalDate): ApiResult<List<AvailableCar>>
    suspend fun getUserCars(userID: Int): ApiResult<List<Car>>
    suspend fun searchCars(keyword: String): ApiResult<List<Car>>
    suspend fun filterCars(request: FilterCar): ApiResult<List<Car>>
    suspend fun getSingleCar(carID: Int): ApiResult<Car>
    suspend fun getSingleCar(licensePlate: String): ApiResult<Car>
    suspend fun createCar(request: CreateCarDTO): ApiResult<Car>
    suspend fun createCarByLicense(request: String): ApiResult<Car>
    suspend fun calculateCarTCO(carID: Int, request: CalculateCarRequestDTO): ApiResult<CarTCOResult>
    suspend fun updateCar(carID: Int, request: UpdateCarDTO): ApiResult<Car>
    suspend fun deleteCar(carID: Int): ApiResult<Boolean>
    suspend fun uploadImage(carID: Int, image: File): ApiResult<Boolean>
    suspend fun getImage(carID: Int): ApiResult<ByteArray>
    suspend fun deleteImage(carID: Int): ApiResult<Boolean>
}

