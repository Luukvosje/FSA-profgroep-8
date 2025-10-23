package com.profgroep8.unittests

import com.profgroep8.BaseUnitTest
import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.CreateRentalLocationDTO
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.RentalWithLocationsDTO
import com.profgroep8.models.dto.UpdateRentalDTO
import com.profgroep8.models.dto.UpdateRentalLocationDTO
import kotlinx.datetime.LocalDateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RentalUnitTest : BaseUnitTest() {

    @Test
    fun testCreateRentalWithValidData() {
        // Create test user and car first
        val userDTO = CreateUserDTO(
            fullName = "Test User",
            email = "test@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = serviceFactory.userService.register(userDTO)

        val carDTO = CreateCarDTO(
            licensePlate = "TEST-123",
            brand = "Tesla",
            model = "Model S",
            year = 2025,
            fuelType = 1,
            price = 75000
        )
        val createdCar = serviceFactory.carService.create(carDTO, createdUser.userID)

        // Create rental
        val rentalDTO = CreateRentalDTO(
            carID = createdCar?.carID ?: 0,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createdRental = serviceFactory.rentalService.create(rentalDTO, createdUser.userID)

        assertNotNull(createdRental)
        assertEquals(createdUser.userID, createdRental.userID)
        assertEquals(createdCar?.carID, createdRental.carID)
        assertEquals(1, createdRental.state) // Active state
        assertNotNull(createdRental.startRentalLocation)
        assertNotNull(createdRental.endRentalLocation)
    }

    @Test
    fun testCreateRentalWithInvalidCarId() {
        val userDTO = CreateUserDTO(
            fullName = "Test User",
            email = "test2@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = serviceFactory.userService.register(userDTO)

        val rentalDTO = CreateRentalDTO(
            carID = -1, // Invalid car ID
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        try {
            serviceFactory.rentalService.create(rentalDTO, createdUser.userID)
            assertTrue(false, "Should have thrown an exception for invalid car ID")
        } catch (e: Exception) {
            // Expected to fail
            assertTrue(e is io.ktor.server.plugins.BadRequestException || e.message?.contains("Car") == true)
        }
    }

    @Test
    fun testCreateRentalLocationWithValidData() {
        val locationDTO = CreateRentalLocationDTO(
            date = LocalDateTime.parse("2025-01-01T10:00:00"),
            longitude = 4.9041f,
            latitude = 52.3676f
        )

        val createdLocation = serviceFactory.rentalLocationService.create(locationDTO)

        assertNotNull(createdLocation)
        assertEquals(4.9041f, createdLocation.longitude)
        assertEquals(52.3676f, createdLocation.latitude)
        assertEquals(LocalDateTime.parse("2025-01-01T10:00:00"), createdLocation.date)
    }

    @Test
    fun testUpdateRentalState() {
        // Create test data
        val userDTO = CreateUserDTO(
            fullName = "Test User",
            email = "test3@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = serviceFactory.userService.register(userDTO)

        val carDTO = CreateCarDTO(
            licensePlate = "TEST-456",
            brand = "BMW",
            model = "X5",
            year = 2025,
            fuelType = 1,
            price = 85000
        )
        val createdCar = serviceFactory.carService.create(carDTO, createdUser.userID)

        val rentalDTO = CreateRentalDTO(
            carID = createdCar?.carID ?: 0,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createdRental = serviceFactory.rentalService.create(rentalDTO, createdUser.userID)

        // Update rental state
        val updateDTO = UpdateRentalDTO(state = 0) // Completed state
        val updatedRental = serviceFactory.rentalService.update(createdRental.rentalID, updateDTO)

        assertNotNull(updatedRental)
        assertEquals(0, updatedRental.state)
        assertEquals(createdRental.rentalID, updatedRental.rentalID)
    }

    @Test
    fun testUpdateRentalLocation() {
        // Create a rental location first
        val locationDTO = CreateRentalLocationDTO(
            date = LocalDateTime.parse("2025-01-01T10:00:00"),
            longitude = 4.9041f,
            latitude = 52.3676f
        )

        val createdLocation = serviceFactory.rentalLocationService.create(locationDTO)

        // Update the location
        val updateDTO = UpdateRentalLocationDTO(
            date = LocalDateTime.parse("2025-01-01T12:00:00"),
            longitude = 4.9050f,
            latitude = 52.3680f
        )

        val updatedLocation = serviceFactory.rentalLocationService.update(createdLocation.rentalLocationID, updateDTO)

        assertNotNull(updatedLocation)
        assertEquals(4.9050f, updatedLocation.longitude)
        assertEquals(52.3680f, updatedLocation.latitude)
        assertEquals(LocalDateTime.parse("2025-01-01T12:00:00"), updatedLocation.date)
    }

    @Test
    fun testGetRentalWithLocations() {
        // Create test data
        val userDTO = CreateUserDTO(
            fullName = "Test User",
            email = "test4@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = serviceFactory.userService.register(userDTO)

        val carDTO = CreateCarDTO(
            licensePlate = "TEST-789",
            brand = "Audi",
            model = "A4",
            year = 2023,
            fuelType = 0,
            price = 45000
        )
        val createdCar = serviceFactory.carService.create(carDTO, createdUser.userID)

        val rentalDTO = CreateRentalDTO(
            carID = createdCar?.carID ?: 0,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createdRental = serviceFactory.rentalService.create(rentalDTO, createdUser.userID)

        // Get the rental with locations
        val retrievedRental = serviceFactory.rentalService.getSingle(createdRental.rentalID)

        assertNotNull(retrievedRental)
        assertEquals(createdRental.rentalID, retrievedRental.rentalID)
        assertNotNull(retrievedRental.startRentalLocation)
        assertNotNull(retrievedRental.endRentalLocation)
        assertEquals(4.9041f, retrievedRental.startRentalLocation.longitude)
        assertEquals(52.3676f, retrievedRental.startRentalLocation.latitude)
    }


    @Test
    fun testGetRentalLocationNotFound() {
        try {
            serviceFactory.rentalLocationService.getSingle(99999)
            assertTrue(false, "Should have thrown NotFoundException")
        } catch (e: Exception) {
            assertTrue(e is io.ktor.server.plugins.NotFoundException || e.message?.contains("not found") == true)
        }
    }

    @Test
    fun testDeleteRental() {
        // Create test data
        val userDTO = CreateUserDTO(
            fullName = "Test User",
            email = "test5@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = serviceFactory.userService.register(userDTO)

        val carDTO = CreateCarDTO(
            licensePlate = "TEST-DELETE",
            brand = "Volkswagen",
            model = "Golf",
            year = 2022,
            fuelType = 0,
            price = 25000
        )
        val createdCar = serviceFactory.carService.create(carDTO, createdUser.userID)

        val rentalDTO = CreateRentalDTO(
            carID = createdCar?.carID ?: 0,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createdRental = serviceFactory.rentalService.create(rentalDTO, createdUser.userID)

        // Delete the rental
        val deleteResult = serviceFactory.rentalService.delete(createdRental.rentalID)

        assertTrue(deleteResult)

        // Verify rental is deleted
        try {
            serviceFactory.rentalService.getSingle(createdRental.rentalID)
            assertTrue(false, "Rental should have been deleted")
        } catch (e: Exception) {
            // Expected - rental should be deleted
            assertTrue(e is io.ktor.server.plugins.NotFoundException || e.message?.contains("not found") == true)
        }
    }

    @Test
    fun testDeleteNonExistentRental() {
        val deleteResult = serviceFactory.rentalService.delete(99999)
        assertTrue(!deleteResult)
    }



    @Test
    fun testCreateRentalWithValidDataAndZeroUserId() {
        // Create test user first
        val userDTO = CreateUserDTO(
            fullName = "Test User",
            email = "test${java.util.UUID.randomUUID()}@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = serviceFactory.userService.register(userDTO)

        // Create test car
        val carDTO = CreateCarDTO(
            licensePlate = "TEST-${(1000..9999).random()}",
            brand = "Tesla",
            model = "Model S",
            year = 2025,
            fuelType = 1,
            price = 75000
        )
        val createdCar = serviceFactory.carService.create(carDTO, 0)

        // Create rental
        val createRentalDTO = CreateRentalDTO(
            carID = createdCar?.carID ?: 0,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createdRental = serviceFactory.rentalService.create(createRentalDTO, 0)

        assertNotNull(createdRental)
        assertEquals(createdUser.userID, createdRental.userID)
        assertEquals(createdCar?.carID ?: 0, createdRental.carID)
        assertEquals(4.9041f, createdRental.startRentalLocation.longitude)
        assertEquals(52.3676f, createdRental.startRentalLocation.latitude)
    }

    @Test
    fun testUpdateRentalWithZeroUserId() {
        // Create test user first
        val userDTO = CreateUserDTO(
            fullName = "Test User",
            email = "test${java.util.UUID.randomUUID()}@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = serviceFactory.userService.register(userDTO)

        // Create test car
        val carDTO = CreateCarDTO(
            licensePlate = "TEST-${(1000..9999).random()}",
            brand = "Tesla",
            model = "Model S",
            year = 2025,
            fuelType = 1,
            price = 75000
        )
        val createdCar = serviceFactory.carService.create(carDTO, 0)

        // Create rental
        val createRentalDTO = CreateRentalDTO(
            carID = createdCar?.carID ?: 0,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )
        val createdRental = serviceFactory.rentalService.create(createRentalDTO, 0)

        // Update rental
        val updateRentalDTO = UpdateRentalDTO(state = 0)
        val updatedRental = serviceFactory.rentalService.update(createdRental.rentalID, updateRentalDTO)

        assertNotNull(updatedRental)
        assertEquals(createdRental.rentalID, updatedRental.rentalID)
        assertEquals(0, updatedRental.state)
    }

    @Test
    fun testDeleteRentalWithZeroUserId() {
        // Create test user first
        val userDTO = CreateUserDTO(
            fullName = "Test User",
            email = "test${java.util.UUID.randomUUID()}@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = serviceFactory.userService.register(userDTO)

        // Create test car
        val carDTO = CreateCarDTO(
            licensePlate = "TEST-${(1000..9999).random()}",
            brand = "Tesla",
            model = "Model S",
            year = 2025,
            fuelType = 1,
            price = 75000
        )
        val createdCar = serviceFactory.carService.create(carDTO, 0)

        // Create rental
        val createRentalDTO = CreateRentalDTO(
            carID = createdCar?.carID ?: 0,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )
        val createdRental = serviceFactory.rentalService.create(createRentalDTO, 0)

        // Delete rental
        val deleteResult = serviceFactory.rentalService.delete(createdRental.rentalID)

        assertNotNull(deleteResult)
        assertEquals(true, deleteResult)
    }

}
