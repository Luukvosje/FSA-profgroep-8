package com.example.network

import com.example.network.models.domain.CarFuelType
import com.example.network.models.remote.RemoteCalculateCar
import com.example.network.models.remote.RemoteCar
import com.example.network.models.remote.RemoteImage
import com.example.network.models.remote.toDomainCar
import com.example.network.models.remote.toDomainCarTCOResult
import com.example.network.models.remote.toBoolean
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarUnitTest {
    @Test
    fun `unknown fuelType maps to Unknown`() {
        val remoteCar = RemoteCar(
            carID = 2,
            licensePlate = "XYZ-999",
            brand = "Test",
            model = "Test",
            year = 2022,
            fuelType = 99,
            price = 10000,
            userID = 1
        )

        val domainCar = remoteCar.toDomainCar()

        assertEquals(CarFuelType.Unknown, domainCar.fuelType)
    }

    @Test
    fun `maps RemoteCalculateCar to CarTCOResult correctly`() {
        val remoteCar = RemoteCar(
            carID = 1,
            licensePlate = "TCO-001",
            brand = "Tesla",
            model = "Model 3",
            year = 2023,
            fuelType = 2,
            price = 45000,
            userID = 5
        )

        val remoteCalculateCar = RemoteCalculateCar(
            car = remoteCar,
            tco = 15000.0,
            costPerKm = 0.25
        )

        val result = remoteCalculateCar.toDomainCarTCOResult()

        assertEquals(15000.0, result.tco, 0.0)
        assertEquals(0.25, result.costPerKm, 0.0)
        assertEquals("Tesla", result.car.brand)
        assertEquals(CarFuelType.Electric, result.car.fuelType)
    }

    @Test
    fun `RemoteImage toBoolean returns false when imagePath is null`() {
        val remoteImage = RemoteImage(imagePath = "null")
        assertFalse(remoteImage.toBoolean())
    }
}
