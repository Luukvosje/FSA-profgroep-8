package com.profgroep8.repositories

//import com.profgroep8.Util.RdwImpl
import com.profgroep8.interfaces.repositories.CarRepository
import com.profgroep8.interfaces.repositories.GenericRepository
import com.profgroep8.models.domain.Car
import com.profgroep8.models.dto.CarAvailability
import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.FilterCar
import com.profgroep8.models.dto.FilterSortOrder
import com.profgroep8.models.entity.CarEntity
import com.profgroep8.models.entity.RentalEntity
import com.profgroep8.models.entity.RentalLocationsEntity
import io.ktor.server.plugins.BadRequestException
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class CarRepositoryImpl() : CarRepository, GenericRepository<Car> by GenericRepositoryImpl(Car) {
    override fun getByUserId(userId: Int): List<CarDTO> {
        return transaction {
            Car.find { CarEntity.userID eq userId }.map { it.toCarDTO() }.toList()
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

            val query = if (conditions.isNotEmpty()) {
                Car.find { conditions.reduce { acc, op -> acc and op } }
            } else {
                Car.all()
            }

            val sortOrder = filter.sortOrder?.let { FilterSortOrder.fromString(it) } ?: FilterSortOrder.nothing

            val sortedQuery = when (sortOrder) {
                FilterSortOrder.price -> query.orderBy(CarEntity.price to SortOrder.ASC)
                FilterSortOrder.Brand -> query.orderBy(CarEntity.brand to SortOrder.ASC)
                FilterSortOrder.Model -> query.orderBy(CarEntity.model to SortOrder.ASC)
                FilterSortOrder.FuelType -> query.orderBy(CarEntity.fuelType to SortOrder.ASC)
                FilterSortOrder.Year -> query.orderBy(CarEntity.year to SortOrder.DESC)
                else -> query
            }

            sortedQuery.map { it.toCarDTO() }

            query.map { it.toCarDTO() }
        }
    }

    private fun isAvailableBetween(startDate: LocalDateTime?, endDate: LocalDateTime?): Op<Boolean> {
        val rentalAlias = RentalEntity.alias("rentalAlias")
        val startLoc = RentalLocationsEntity.alias("start_loc")
        val endLoc = RentalLocationsEntity.alias("end_loc")

        val overlapCondition = Op.build {
            (rentalAlias[RentalEntity.carID] eq CarEntity.id) and
                    (startDate?.let { endLoc[RentalLocationsEntity.date] greaterEq it } ?: Op.TRUE) and
                    (endDate?.let { startLoc[RentalLocationsEntity.date] lessEq it } ?: Op.TRUE)
        }

        return NotExists(
            rentalAlias
                .join(
                    startLoc,
                    JoinType.INNER,
                    rentalAlias[RentalEntity.startRentalLocationID],
                    startLoc[RentalLocationsEntity.id]
                )
                .join(
                    endLoc,
                    JoinType.INNER,
                    rentalAlias[RentalEntity.endRentalLocationID],
                    endLoc[RentalLocationsEntity.id]
                )
                .select(rentalAlias[RentalEntity.id])
                .where { overlapCondition }
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

    override fun getAvailableCars(
        startDate: LocalDateTime?,
    ): List<CarAvailability> {
        return transaction {
            val today = java.time.LocalDateTime.now().toKotlinLocalDateTime();

            val lastRentalAlias = RentalEntity
                .join(RentalLocationsEntity, JoinType.LEFT, RentalEntity.endRentalLocationID, RentalLocationsEntity.id)
                .select(RentalEntity.carID, RentalLocationsEntity.date.max())
                .where { RentalLocationsEntity.date greaterEq (startDate ?: today) }
                .groupBy(RentalEntity.carID)
                .alias("last_rental")

            val nextRentalAlias = RentalEntity
                .join(
                    RentalLocationsEntity,
                    JoinType.LEFT,
                    RentalEntity.startRentalLocationID,
                    RentalLocationsEntity.id
                )
                .select(RentalEntity.carID, RentalLocationsEntity.date.min())
                .where { RentalLocationsEntity.date greater (startDate ?: today) }
                .groupBy(RentalEntity.carID)
                .alias("next_rental")

            CarEntity.leftJoin(
                lastRentalAlias,
                { CarEntity.id },
                { lastRentalAlias[RentalEntity.carID] }
            ).leftJoin(
                nextRentalAlias,
                { CarEntity.id },
                { nextRentalAlias[RentalEntity.carID] }
            )
            .selectAll()
            .map { row ->
                val carDTO = CarDTO(
                    carID = row[CarEntity.id].value,
                    licensePlate = row[CarEntity.licensePlate],
                    brand = row[CarEntity.brand],
                    model = row[CarEntity.model],
                    year = row[CarEntity.year],
                    fuelType = row[CarEntity.fuelType],
                    price = row[CarEntity.price],
                    userID = row[CarEntity.userID]
                )
                val availableFrom: LocalDateTime =
                    row[lastRentalAlias[RentalLocationsEntity.date.max()]] ?: (startDate ?: today)
                val availableTill: LocalDateTime =
                    row[nextRentalAlias[RentalLocationsEntity.date.min()]] ?: (startDate ?: today)
                CarAvailability(car = carDTO, availableFrom = availableFrom, availableTill = availableTill)
            }
        }
    }


}