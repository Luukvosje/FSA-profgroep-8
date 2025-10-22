package com.profgroep8.unittests

import com.profgroep8.BaseUnitTest
import com.profgroep8.models.dto.CalculateCarRequestDTO
import com.profgroep8.models.dto.CarDTO
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CarUnitTest : BaseUnitTest() {

    @Test
    fun testCalculateGasolineCar() {
        val car = CarDTO(
            carID = 0,
            licensePlate = "18LHB7",
            brand = "OPEL",
            model = "CORSA",
            year = 2010,
            fuelType = 0, // GASOLINE
            price = 0,
            userID = 0
        )

        val request = CalculateCarRequestDTO(standardKmPerYear = 100.0)
        val calculation = serviceFactory.carService.calculateCar(request, car)

        assertNotNull(calculation)
        // GASOLINE:
        // depreciation = 3000
        // maintenance = 700
        // tax = 600
        // energyCost = (6.5 / 100) * 2.10 * 100 = 13.65
        // total = 4313.65
        // per km = 43.1365
        assertEquals(4313.65, calculation.tco, 0.001)
        assertEquals(43.1365, calculation.costPerKm, 0.0001)
    }

    @Test
    fun testCalculateDieselCar() {
        val car = CarDTO(
            carID = 0,
            licensePlate = "22XYZ9",
            brand = "VOLKSWAGEN",
            model = "GOLF",
            year = 2015,
            fuelType = 1, // DIESEL
            price = 0,
            userID = 0
        )

        val request = CalculateCarRequestDTO(standardKmPerYear = 100.0)
        val calculation = serviceFactory.carService.calculateCar(request, car)

        assertNotNull(calculation)
        // DIESEL:
        // depreciation = 3200
        // maintenance = 750
        // tax = 800
        // energyCost = (5.5 / 100) * 1.95 * 100 = 10.725
        // total = 4760.725
        // per km = 47.60725
        assertEquals(4760.725, calculation.tco, 0.001)
        assertEquals(47.60725, calculation.costPerKm, 0.0001)
    }

    @Test
    fun testCalculateElectricCar() {
        val car = CarDTO(
            carID = 0,
            licensePlate = "EV-9999",
            brand = "TESLA",
            model = "MODEL 3",
            year = 2022,
            fuelType = 2, // ELECTRIC
            price = 0,
            userID = 0
        )

        val request = CalculateCarRequestDTO(standardKmPerYear = 100.0)
        val calculation = serviceFactory.carService.calculateCar(request, car)

        assertNotNull(calculation)
        // ELECTRIC:
        // depreciation = 2500
        // maintenance = 400
        // tax = 0
        // energyCost = (18 / 100) * 0.35 * 100 = 6.3
        // total = 2906.3
        // per km = 29.063
        assertEquals(2906.3, calculation.tco, 0.001)
        assertEquals(29.063, calculation.costPerKm, 0.0001)
    }

    @Test
    fun testCalculateHybridCar() {
        val car = CarDTO(
            carID = 0,
            licensePlate = "HY-1234",
            brand = "TOYOTA",
            model = "PRIUS",
            year = 2021,
            fuelType = 3, // HYBRID
            price = 0,
            userID = 0
        )

        val request = CalculateCarRequestDTO(standardKmPerYear = 100.0)
        val calculation = serviceFactory.carService.calculateCar(request, car)

        assertNotNull(calculation)
        // HYBRID:
        // depreciation = 2800
        // maintenance = 600
        // tax = 400
        // energyCost = ((4 / 100) * 2.05 * 100) + (0.05 * 100) = 8.2 + 5 = 13.2
        // total = 3813.2
        // per km = 38.132
        assertEquals(3813.2, calculation.tco, 0.001)
        assertEquals(38.132, calculation.costPerKm, 0.0001)
    }
}
