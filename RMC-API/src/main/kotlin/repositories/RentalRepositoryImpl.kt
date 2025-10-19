package com.profgroep8.repositories

import com.profgroep8.interfaces.repositories.GenericRepository
import com.profgroep8.interfaces.repositories.RentalRepository
import com.profgroep8.models.domain.Rental
import com.profgroep8.models.entity.RentalEntity
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.and

class RentalRepositoryImpl() : RentalRepository, GenericRepository<Rental> by GenericRepositoryImpl(Rental) {
    override fun getActiveRentalByCar(carId: Int): Rental? {
        return transaction {
            Rental.find { 
                (RentalEntity.carId eq carId) and (RentalEntity.state eq 1) 
            }.firstOrNull()
        }
    }
}

