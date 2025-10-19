package com.profgroep8.interfaces.repositories

import com.profgroep8.models.domain.Rental

interface RentalRepository : GenericRepository<Rental> {
    fun getActiveRentalByCar(carId: Int): Rental?
}
