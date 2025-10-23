package com.profgroep8.unittests

import com.profgroep8.BaseUnitTest
import com.profgroep8.mocks.MockDatabaseFactoryImpl.rentalLocationRepository
import com.profgroep8.mocks.MockDatabaseFactoryImpl.rentalRepository
import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.CreateRentalLocationDTO
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.CreateCarDTO
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RentalUnitTest : BaseUnitTest() {

    @Test
    fun testCanAccessRentalHappyPath() {
        // Create test user and car
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

        // Test the logic directly: user should be able to access their own rental
        val rental = serviceFactory.rentalService.getSingle(createdRental.rentalID)
        val car = serviceFactory.carService.getSingle(rental.carID)
        
        // User can access if they are either the renter or the car owner
        val canAccess = createdUser.userID == rental.userID || createdUser.userID == car?.userID
        assertTrue(canAccess, "User should be able to access their own rental")
    }

    @Test
    fun testCanAccessRentalNonHappyPath() {
        // Create test user and car
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

        // Test the logic directly: different user should not be able to access the rental
        val rental = serviceFactory.rentalService.getSingle(createdRental.rentalID)
        val car = serviceFactory.carService.getSingle(rental.carID)
        
        // Different user (ID 999) should not be able to access
        val differentUserId = 999
        val canAccess = differentUserId == rental.userID || differentUserId == car?.userID
        assertFalse(canAccess, "Different user should not be able to access the rental")
    }

    @Test
    fun testToRentalWithLocationsDtoHappyPath() {
        // Create test rental locations
        val startLocationDTO = CreateRentalLocationDTO(
            date = LocalDateTime.parse("2025-01-01T10:00:00"),
            longitude = 4.9041f,
            latitude = 52.3676f
        )
        val endLocationDTO = CreateRentalLocationDTO(
            date = LocalDateTime.parse("2025-01-01T18:00:00"),
            longitude = 4.9050f,
            latitude = 52.3680f
        )
        val startLocation = serviceFactory.rentalLocationService.create(startLocationDTO)
        val endLocation = serviceFactory.rentalLocationService.create(endLocationDTO)

        // Create a rental through the service to get the domain object
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

        val rentalDTO = CreateRentalDTO(
            carID = createdCar?.carID ?: 0,
            startLocation = startLocationDTO,
            endLocation = endLocationDTO
        )
        val createdRental = serviceFactory.rentalService.create(rentalDTO, createdUser.userID)

        // Get the actual domain objects from the database
        val rentalDomain = rentalRepository.getSingle(createdRental.rentalID)
        val startLocationDomain = rentalLocationRepository.getSingle(startLocation.rentalLocationID)
        val endLocationDomain = rentalLocationRepository.getSingle(endLocation.rentalLocationID)

        // Test the conversion
        val result = rentalDomain?.toRentalWithLocationsDTO(startLocationDomain!!, endLocationDomain!!)

        assertNotNull(result)
        assertEquals(createdRental.rentalID, result?.rentalID)
        assertEquals(createdRental.userID, result?.userID)
        assertEquals(createdRental.carID, result?.carID)
        assertEquals(createdRental.state, result?.state)
        assertEquals(4.9041f, result?.startRentalLocation?.longitude)
        assertEquals(52.3676f, result?.startRentalLocation?.latitude)
        assertEquals(4.9050f, result?.endRentalLocation?.longitude)
        assertEquals(52.3680f, result?.endRentalLocation?.latitude)
    }

    @Test
    fun testToRentalWithLocationsDtoNonHappyPath() {
        // Create rental locations with zero values
        val startLocationDTO = CreateRentalLocationDTO(
            date = LocalDateTime.parse("2025-01-01T10:00:00"),
            longitude = 0.0f,
            latitude = 0.0f
        )
        val endLocationDTO = CreateRentalLocationDTO(
            date = LocalDateTime.parse("2025-01-01T18:00:00"),
            longitude = 0.0f,
            latitude = 0.0f
        )
        val startLocation = serviceFactory.rentalLocationService.create(startLocationDTO)
        val endLocation = serviceFactory.rentalLocationService.create(endLocationDTO)

        // Create a rental through the service
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
            startLocation = startLocationDTO,
            endLocation = endLocationDTO
        )
        val createdRental = serviceFactory.rentalService.create(rentalDTO, createdUser.userID)

        // Get the actual domain objects from the database
        val rentalDomain = rentalRepository.getSingle(createdRental.rentalID)
        val startLocationDomain = rentalLocationRepository.getSingle(startLocation.rentalLocationID)
        val endLocationDomain = rentalLocationRepository.getSingle(endLocation.rentalLocationID)

        // Test the conversion with zero values
        val result = rentalDomain?.toRentalWithLocationsDTO(startLocationDomain!!, endLocationDomain!!)

        assertNotNull(result)
        assertEquals(createdRental.rentalID, result?.rentalID)
        assertEquals(createdRental.userID, result?.userID)
        assertEquals(createdRental.carID, result?.carID)
        assertEquals(createdRental.state, result?.state)
        assertEquals(0.0f, result?.startRentalLocation?.longitude)
        assertEquals(0.0f, result?.startRentalLocation?.latitude)
        assertEquals(0.0f, result?.endRentalLocation?.longitude)
        assertEquals(0.0f, result?.endRentalLocation?.latitude)
    }
}
