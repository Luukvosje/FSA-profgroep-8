package com.profgroep8.repositories

//import com.profgroep8.Util.RdwImpl
import com.profgroep8.interfaces.repositories.CarRepository
import com.profgroep8.interfaces.repositories.GenericRepository
import com.profgroep8.models.domain.Car
import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.FilterCar
import com.profgroep8.models.entity.CarEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction

class CarRepositoryImpl() : CarRepository, GenericRepository<Car> by GenericRepositoryImpl(Car) {
    override suspend fun findLicense(licensePlate: String): CarDTO? {
        TODO("not implemented")
    }

    override fun getByUserId(userId: Int): List<CarDTO> {
        return transaction {
            Car.find { CarEntity.userId eq userId }.map { it.toCarDTO() }.toList()
        }
    }

    override fun filterCars(filter: FilterCar): List<CarDTO> {
        val normalizedFilter = filter.ToSearchValues()
        return transaction {
            val conditions = mutableListOf<Op<Boolean>>()

            normalizedFilter.licensePlate?.let { plate ->
                val normalizedPlate = plate.lowercase().replace("-", "").trim()
                val replaceFunc = CustomFunction(
                    "REPLACE",
                    TextColumnType(),
                    CarEntity.licensePlate,
                    stringParam("-"),
                    stringParam("")
                )
                val lowered = LowerCase(replaceFunc)
                conditions += lowered like "%$normalizedPlate%"
            }

            normalizedFilter.brand?.let {
                conditions += CarEntity.brand like "%${it.lowercase().trim()}%"
            }
            normalizedFilter.model?.let {
                conditions += CarEntity.model like "%${it.lowercase().trim()}%"
            }
            normalizedFilter.fuelType?.let {
                conditions += CarEntity.fuelType eq it
            }
            normalizedFilter.year?.let {
                conditions += CarEntity.year eq it
            }
            if (normalizedFilter.minPrice != null && normalizedFilter.maxPrice != null) {
                conditions += (CarEntity.price greaterEq normalizedFilter.minPrice.toInt()) and
                        (CarEntity.price lessEq normalizedFilter.maxPrice.toInt())
            } else if (normalizedFilter.minPrice != null) {
                conditions += CarEntity.price greaterEq normalizedFilter.minPrice.toInt()
            } else if (normalizedFilter.maxPrice != null) {
                conditions += CarEntity.price lessEq normalizedFilter.maxPrice.toInt()
            }

            val query = if (conditions.isNotEmpty()) {
                Car.find { conditions.reduce { acc, op -> acc and op } }
            } else {
                Car.all()
            }
            query.map { it.toCarDTO() }        }
    }


}