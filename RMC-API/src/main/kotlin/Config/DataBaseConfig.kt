package com.profgroep8.Config

import com.profgroep8.Data.Tables.CarTable
import com.profgroep8.Data.Tables.RentalLocationTable
import com.profgroep8.Data.Tables.RentalTable
import com.profgroep8.Data.Tables.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {

    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/RentMyCar",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "8520"
        )

        transaction {
            SchemaUtils.create(
                CarTable,
                UserTable,
                RentalTable,
                RentalLocationTable
            )
        }
    }
}
