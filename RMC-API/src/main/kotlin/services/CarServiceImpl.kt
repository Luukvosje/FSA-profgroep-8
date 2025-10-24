package com.profgroep8.services

import com.profgroep8.interfaces.services.CarService
import com.profgroep8.models.domain.FuelType
import com.profgroep8.models.dto.*
import io.ktor.server.plugins.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction

class CarServiceImpl(val serviceFactoryImpl: ServiceFactoryImpl) : CarService {
    override suspend fun findByLicense(licensePlate: String): CreateCarDTO? =
        serviceFactoryImpl.rdwClient.getCar(licensePlate)

    override fun getAllCars() =
        serviceFactoryImpl.databaseFactory.carRepository.getAll().map { it.toCarDTO() }

    override fun getCarsByUserId(userID: Int): List<CarDTO> =
        serviceFactoryImpl.databaseFactory.carRepository.getByUserId(userID)

    override fun filterCars(filter: FilterCar): List<CarDTO> =
        serviceFactoryImpl.databaseFactory.carRepository.filterCars(filter)

    override fun searchCars(keyword: String?): List<CarDTO> =
        serviceFactoryImpl.databaseFactory.carRepository.searchCars(keyword)

    override fun getSingle(carID: Int): CarDTO? =
        serviceFactoryImpl.databaseFactory.carRepository.getSingle(carID)?.toCarDTO()

    override fun create(item: CreateCarDTO, userID: Int): CarDTO? {
        val plate = item.licensePlate.lowercase().replace("-", "").trim()

        val createdCar = serviceFactoryImpl.databaseFactory.carRepository.create {
            this.licensePlate = plate
            this.brand = item.brand
            this.model = item.model
            this.brand = item.brand
            this.price = item.price
            this.year = item.year
            this.fuelType = item.fuelType
            this.userID = userID
        }

        return createdCar?.toCarDTO()
    }

    override fun update(carID: Int, item: UpdateCarDTO): CarDTO? {
        val updatedCar = serviceFactoryImpl.databaseFactory.carRepository.update(carID, {
            this.licensePlate = item.licensePlate
            this.brand = item.brand
            this.model = item.model
            this.brand = item.brand
            this.price = item.price
            this.year = item.year
            this.fuelType = item.fuelType
        })

        return updatedCar?.toCarDTO()
    }

    override fun delete(carID: Int): Boolean =
        serviceFactoryImpl.databaseFactory.carRepository.delete(carID)

    override fun calculateCar(request: CalculateCarRequestDTO, car: CarDTO): CalculateCarResponseDTO {
        val depreciation: Double
        val maintenance: Double
        val tax: Double
        val energyCost: Double

        when (FuelType.fromCode(car.fuelType)) {
            FuelType.GASOLINE -> {
                depreciation = 3000.0
                maintenance = 700.0
                tax = 600.0
                val fuelConsumptionPer100Km = 6.5
                val fuelPrice = 2.10
                energyCost = (fuelConsumptionPer100Km / 100.0) * fuelPrice * request.standardKmPerYear
            }

            FuelType.DIESEL -> {
                depreciation = 3200.0
                maintenance = 750.0
                tax = 800.0
                val fuelConsumptionPer100Km = 5.5
                val fuelPrice = 1.95
                energyCost = (fuelConsumptionPer100Km / 100.0) * fuelPrice * request.standardKmPerYear
            }

            FuelType.ELECTRIC -> {
                depreciation = 2500.0
                maintenance = 400.0
                tax = 0.0
                val consumptionPer100Km = 18.0
                val electricityPrice = 0.35
                energyCost = (consumptionPer100Km / 100.0) * electricityPrice * request.standardKmPerYear
            }

            FuelType.HYBRID -> {
                depreciation = 2800.0
                maintenance = 600.0
                tax = 400.0
                val fuelConsumptionPer100Km = 4.0
                val fuelPrice = 2.05
                val electricityCost = 0.05 * request.standardKmPerYear
                energyCost = (fuelConsumptionPer100Km / 100.0) * fuelPrice * request.standardKmPerYear + electricityCost
            }
        }

        val totalCost = depreciation + maintenance + tax + energyCost
        val costPerKm = totalCost / request.standardKmPerYear

        return CalculateCarResponseDTO(
            car = car,
            tco = totalCost,
            costPerKm = costPerKm
        )
    }

    override fun getAvailableCars(availability: Availability): List<CarAvailability> {
        var startDate = availability.startDate;

        if(availability.startDate != null && availability.startDate < java.time.LocalDateTime.now().toKotlinLocalDateTime()) {
            startDate = java.time.LocalDateTime.now().toKotlinLocalDateTime()
        }

        return serviceFactoryImpl.databaseFactory.carRepository.getAvailableCars(startDate)
    }
}