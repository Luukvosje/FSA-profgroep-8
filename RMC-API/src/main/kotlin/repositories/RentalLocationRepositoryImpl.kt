package com.profgroep8.repositories

import com.profgroep8.interfaces.repositories.GenericRepository
import com.profgroep8.interfaces.repositories.RentalLocationRepository
import com.profgroep8.models.domain.RentalLocation

class RentalLocationRepositoryImpl() : RentalLocationRepository, GenericRepository<RentalLocation> by GenericRepositoryImpl(RentalLocation)