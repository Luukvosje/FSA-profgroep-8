package com.profgroep8.repositories

import com.profgroep8.interfaces.repositories.DatabaseFactory
import com.profgroep8.interfaces.repositories.GenericRepository
import com.profgroep8.models.domain.Car
import com.profgroep8.models.entity.CarEntity
import com.profgroep8.models.entity.RentalEntity
import com.profgroep8.models.entity.RentalLocationsEntity
import com.profgroep8.models.entity.UserEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactoryImpl : DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:postgresql://foxtrek.nl:5432/RMC_API",
            driver = "org.postgresql.Driver",
            user = "RMCApi",
            password = "bBvt65nT1L70ii7r8aM9CM"
        )

        transaction {
            SchemaUtils.create(
                CarEntity,
                UserEntity,
                RentalEntity,
                RentalLocationsEntity
            )
        }
    }

    override val carRepository : GenericRepository<Car> = GenericRepositoryImpl(Car)
}
