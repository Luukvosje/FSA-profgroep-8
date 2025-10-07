package com.profgroep8.models.entity

import org.jetbrains.exposed.sql.Table

object Users : Table("User") {
    val UserId = integer("UserID").autoIncrement()
    override val primaryKey = PrimaryKey(UserId)

    val fullName = varchar("FullName", 255)
    val email = varchar("Email", 255)
    val password = varchar("Password", 255)
    val phone = varchar("Phone", 15)
    val address = varchar("Address", 255)
    val zipcode = varchar("Zipcode", 8)
    val city = varchar("City", 255)
    val countryISO = varchar("CountryISO", 2)
    val points = integer("Points")
}