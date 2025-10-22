package com.profgroep8.mocks

import com.profgroep8.interfaces.repositories.CarRepository
import com.profgroep8.interfaces.repositories.DatabaseFactory
import com.profgroep8.interfaces.repositories.RentalLocationRepository
import com.profgroep8.interfaces.repositories.RentalRepository
import com.profgroep8.interfaces.repositories.UserRepository
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.entity.CarEntity
import com.profgroep8.models.entity.RentalEntity
import com.profgroep8.models.entity.RentalLocationsEntity
import com.profgroep8.models.entity.UserEntity
import com.profgroep8.repositories.CarRepositoryImpl
import com.profgroep8.repositories.RentalLocationRepositoryImpl
import com.profgroep8.repositories.RentalRepositoryImpl
import com.profgroep8.repositories.UserRepositoryImpl
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

internal object MockDatabaseFactoryImpl : DatabaseFactory {
    fun init() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            // Create Car table
            SchemaUtils.create(CarEntity)
            SchemaUtils.create(UserEntity)
            SchemaUtils.create(RentalEntity)
            SchemaUtils.create(RentalLocationsEntity)
        }
    }

    override val carRepository: CarRepository by lazy { CarRepositoryImpl() }
    override val userRepository: UserRepository by lazy { UserRepositoryImpl() }
    override val rentalRepository: RentalRepository by lazy { RentalRepositoryImpl() }
    override val rentalLocationRepository: RentalLocationRepository by lazy { RentalLocationRepositoryImpl() }
}