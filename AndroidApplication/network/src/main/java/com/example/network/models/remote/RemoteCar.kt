package com.example.network.models.remote

import com.example.network.models.domain.Car
import com.example.network.models.domain.CarFuelType
import com.example.network.models.domain.CarTCOResult
import kotlinx.serialization.Serializable

@Serializable
data class RemoteCar(
    val carID: Int,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: Int,
    val price: Int,
    val userID: Int,
)

fun RemoteCar.toDomainCar(): Car {
    val carFuelType = when (fuelType) {
        0 -> CarFuelType.Gasoline
        1 -> CarFuelType.Diesel
        2 -> CarFuelType.Electric
        3 -> CarFuelType.Hybrid
        else -> CarFuelType.Unknown
    }

    return Car(
        carID = carID,
        licensePlate = licensePlate,
        brand = brand,
        model = model,
        year = year,
        fuelType = carFuelType,
        price = price,
        userID = userID
    )
}

@Serializable
data class CreateCarDTO(
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: Int,
    val price: Int,
)

@Serializable
data class UpdateCarDTO(
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: Int,
    val price: Int,
)

@Serializable
data class CalculateCarRequestDTO(
    val standardKmPerYear: Double,
)

@Serializable
data class RemoteCalculateCar(
    val car: RemoteCar,
    val tco: Double,
    val costPerKm: Double,
)

fun RemoteCalculateCar.toDomainCarTCOResult(): CarTCOResult {
    return CarTCOResult(
        car = car.toDomainCar(),
        tco = tco,
        costPerKm = costPerKm,
    )
}

@Serializable
data class RemoteImage(
    val imagePath: String,
)

fun RemoteImage.toBoolean(): Boolean {
    if(imagePath == "null") {
        return false
    }
    return true
}