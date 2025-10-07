package com.profgroep8.repositories

import com.profgroep8.models.entity.Cars
import com.profgroep8.models.entity.RentalLocations
import com.profgroep8.models.entity.Rentals
import com.profgroep8.models.entity.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/RentMyCar",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "8520"
        )

        transaction {
            SchemaUtils.create(
                Cars,
                Users,
                Rentals,
                RentalLocations
            )
        }
    }
}
