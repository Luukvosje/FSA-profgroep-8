package com.profgroep8.repositories

//import com.profgroep8.Util.RdwImpl
import com.profgroep8.interfaces.repositories.CarRepository
import com.profgroep8.interfaces.repositories.GenericRepository
import com.profgroep8.models.domain.Car
import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.FilterCar
import com.profgroep8.models.entity.CarEntity
import com.profgroep8.models.entity.RentalEntity
import com.profgroep8.models.entity.RentalLocationsEntity
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction

class CarRepositoryImpl() : CarRepository, GenericRepository<Car> by GenericRepositoryImpl(Car) {
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
                conditions += CarEntity.brand.lowerCase().trim() like "%${it.lowercase().trim()}%"
            }
            normalizedFilter.model?.let {
                conditions += CarEntity.model.lowerCase().trim() like "%${it.lowercase().trim()}%"
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

            if (normalizedFilter.startDate != null && normalizedFilter.endDate != null) {
                conditions += CheckAvailability(normalizedFilter.startDate, normalizedFilter.endDate)
            }

            val query = if (conditions.isNotEmpty()) {
                Car.find { conditions.reduce { acc, op -> acc and op } }
            } else {
                Car.all()
            }
            query.map { it.toCarDTO() }
        }
    }

    private fun CheckAvailability(startDate: LocalDateTime, endDate: LocalDateTime): Op<Boolean> {
        val startLoc = RentalLocationsEntity.alias("startLoc")
        val endLoc = RentalLocationsEntity.alias("endLoc")
        val rentalAlias = RentalEntity.alias("rentalAlias")

        return exists(
            rentalAlias.join(
                startLoc,
                JoinType.INNER,
                RentalEntity.startRentalLocationID,
                startLoc[RentalLocationsEntity.id]
            )
                .join(endLoc, JoinType.INNER, RentalEntity.endRentalLocationID, endLoc[RentalLocationsEntity.id])
                .select(rentalAlias[RentalEntity.id]).where(
                    (startLoc[RentalLocationsEntity.date] lessEq endDate) and
                            (endLoc[RentalLocationsEntity.date] greaterEq startDate)
                )
        )
    }

    override fun searchCars(keyword: String?): List<CarDTO> {
        val normalizedKeyword = "%${keyword?.trim()?.lowercase()}%"

        return transaction {
            Car.find {
                (CarEntity.brand.lowerCase() like normalizedKeyword) or
                        (CarEntity.licensePlate.lowerCase() like normalizedKeyword) or
                        (CarEntity.model.lowerCase() like normalizedKeyword)
            }.map { it.toCarDTO() }
        }
    }
}