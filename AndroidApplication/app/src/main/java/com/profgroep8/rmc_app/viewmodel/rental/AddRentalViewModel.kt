package com.profgroep8.rmc_app.viewmodel.rental

import RmcScreen
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.Car
import com.example.network.models.remote.CreateRentalDTO
import com.example.network.models.remote.CreateRentalLocationDTO
import com.example.network.services.ApiResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.utils.NominatimGeocoder
import com.profgroep8.rmc_app.utils.UiText
import com.profgroep8.rmc_app.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class AddRentalUiState(
    val car: Car? = null,
    val startDate: Long? = null,
    val startTime: String = "",
    val startTimeError: UiText? = null,
    val endDate: Long? = null,
    val endTime: String = "",
    val endTimeError: UiText? = null,
    val startLatitude: Float? = null,
    val startLongitude: Float? = null,
    val endAddress: String = "",
    val endAddressError: UiText? = null,
    val endLatitude: Float? = null,
    val endLongitude: Float? = null,
    val isValid: Boolean = false,
    val errorMessage: String? = null,
    val isGettingLocation: Boolean = false,
    val needsLocationPermission: Boolean = false,
    val showCarAlreadyRentedDialog: Boolean = false
)

class AddRentalViewModel(
    private val serviceFactory: ServiceFactory,
    val carId: Int
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AddRentalUiState())
    val uiState: StateFlow<AddRentalUiState> = _uiState.asStateFlow()

    init {
        loadCar()
        setDefaultStartDateTime()
    }

    private fun setDefaultStartDateTime() {
        val now = LocalDateTime.now()
        val dateMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        
        _uiState.update {
            it.copy(
                startDate = dateMillis,
                startTime = now.format(timeFormatter),
                startTimeError = null
            )
        }
    }

    private fun loadCar() {
        viewModelScope.launch {
            withLoading {
                val carResult = serviceFactory.carService.getSingleCar(carId)
                when (carResult) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(car = carResult.data, errorMessage = null) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(errorMessage = carResult.exception.message ?: "Failed to load car")
                        }
                    }
                }
            }
        }
    }

    fun getCurrentLocation(context: Context, hasPermission: Boolean) {
        if (!hasPermission) {
            _uiState.update { it.copy(needsLocationPermission = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGettingLocation = true, needsLocationPermission = false) }
            
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val cancellationTokenSource = CancellationTokenSource()
                
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await()

                if (location != null) {
                    _uiState.update {
                        it.copy(
                            startLatitude = location.latitude.toFloat(),
                            startLongitude = location.longitude.toFloat(),
                            isGettingLocation = false,
                            isValid = isFormValid(
                                startDate = it.startDate,
                                startTime = it.startTime,
                                endDate = it.endDate,
                                endTime = it.endTime,
                                startLatitude = location.latitude.toFloat(),
                                startLongitude = location.longitude.toFloat(),
                                endLatitude = it.endLatitude,
                                endLongitude = it.endLongitude
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isGettingLocation = false,
                            errorMessage = "Could not get location"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGettingLocation = false,
                        errorMessage = "Failed to get location: ${e.message}"
                    )
                }
            }
        }
    }

    fun onStartDateChange(dateMillis: Long?) {
        _uiState.update {
            it.copy(
                startDate = dateMillis,
                isValid = isFormValid(
                    startDate = dateMillis,
                    startTime = it.startTime,
                    endDate = it.endDate,
                    endTime = it.endTime,
                    startLatitude = it.startLatitude,
                    startLongitude = it.startLongitude,
                    endLatitude = it.endLatitude,
                    endLongitude = it.endLongitude
                )
            )
        }
    }

    fun onStartTimeChange(value: String) {
        _uiState.update {
            it.copy(
                startTime = value,
                startTimeError = validateTime(value),
                isValid = isFormValid(
                    startDate = it.startDate,
                    startTime = value,
                    endDate = it.endDate,
                    endTime = it.endTime,
                    startLatitude = it.startLatitude,
                    startLongitude = it.startLongitude,
                    endLatitude = it.endLatitude,
                    endLongitude = it.endLongitude
                )
            )
        }
    }

    fun onEndDateChange(dateMillis: Long?) {
        _uiState.update {
            it.copy(
                endDate = dateMillis,
                isValid = isFormValid(
                    startDate = it.startDate,
                    startTime = it.startTime,
                    endDate = dateMillis,
                    endTime = it.endTime,
                    startLatitude = it.startLatitude,
                    startLongitude = it.startLongitude,
                    endLatitude = it.endLatitude,
                    endLongitude = it.endLongitude
                )
            )
        }
    }

    fun onEndTimeChange(value: String) {
        _uiState.update {
            it.copy(
                endTime = value,
                endTimeError = validateTime(value),
                isValid = isFormValid(
                    startDate = it.startDate,
                    startTime = it.startTime,
                    endDate = it.endDate,
                    endTime = value,
                    startLatitude = it.startLatitude,
                    startLongitude = it.startLongitude,
                    endLatitude = it.endLatitude,
                    endLongitude = it.endLongitude
                )
            )
        }
    }

    fun onEndAddressChange(value: String) {
        _uiState.update {
            it.copy(
                endAddress = value,
                endAddressError = null,
                endLatitude = null,
                endLongitude = null,
                isValid = false
            )
        }
    }

    fun searchAddress() {
        val address = _uiState.value.endAddress
        if (address.isBlank()) {
            _uiState.update { it.copy(endAddressError = UiText.StringResource(R.string.field_required)) }
            return
        }

        viewModelScope.launch {
            withLoading {
                val result = NominatimGeocoder.geocodeAddress(address)
                if (result != null) {
                    val (lat, lon) = result
                    _uiState.update {
                        it.copy(
                            endLatitude = lat,
                            endLongitude = lon,
                            endAddressError = null,
                            isValid = isFormValid(
                                startDate = it.startDate,
                                startTime = it.startTime,
                                endDate = it.endDate,
                                endTime = it.endTime,
                                startLatitude = it.startLatitude,
                                startLongitude = it.startLongitude,
                                endLatitude = lat,
                                endLongitude = lon
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            endAddressError = UiText.StringResource(R.string.invalid_address),
                            endLatitude = null,
                            endLongitude = null,
                            isValid = false
                        )
                    }
                }
            }
        }
    }

    fun createRental(navigateToScreen: (String) -> Unit) {
        val state = _uiState.value
        
        if (!state.isValid) return

        val startLat = state.startLatitude ?: return
        val startLon = state.startLongitude ?: return
        val endLat = state.endLatitude ?: return
        val endLon = state.endLongitude ?: return

        val startDateTime = combineDateAndTime(state.startDate, state.startTime) ?: return
        val endDateTime = combineDateAndTime(state.endDate, state.endTime) ?: return

        viewModelScope.launch {
            withLoading {
                val createRentalDTO = CreateRentalDTO(
                    carID = carId,
                    startLocation = CreateRentalLocationDTO(
                        date = startDateTime,
                        latitude = startLat,
                        longitude = startLon
                    ),
                    endLocation = CreateRentalLocationDTO(
                        date = endDateTime,
                        latitude = endLat,
                        longitude = endLon
                    )
                )

                val result = serviceFactory.rentalService.createRental(createRentalDTO)
                
                when (result) {
                    is ApiResult.Success -> {
                        navigateToScreen("${RmcScreen.RentalInformation.name}/${result.data.rentalID}")
                    }
                    is ApiResult.Error -> {
                        val errorMsg = result.exception.message ?: "Failed to create rental"
                        
                        if (errorMsg.contains("Car is currently rented", ignoreCase = true)) {
                            _uiState.update {
                                it.copy(showCarAlreadyRentedDialog = true, errorMessage = null)
                            }
                        } else {
                            _uiState.update {
                                it.copy(errorMessage = errorMsg, showCarAlreadyRentedDialog = false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun combineDateAndTime(dateMillis: Long?, timeString: String): String? {
        if (dateMillis == null || timeString.isBlank()) return null
        
        return try {
            val date = Instant.ofEpochMilli(dateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
            val dateTime = LocalDateTime.of(date, time)
            dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            null
        }
    }

    private fun validateTime(value: String): UiText? {
        if (value.isBlank()) {
            return UiText.StringResource(R.string.field_required)
        }
        
        return try {
            LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"))
            null
        } catch (e: DateTimeParseException) {
            UiText.StringResource(R.string.invalid_time_format)
        }
    }

    private fun isFormValid(
        startDate: Long?,
        startTime: String,
        endDate: Long?,
        endTime: String,
        startLatitude: Float?,
        startLongitude: Float?,
        endLatitude: Float?,
        endLongitude: Float?
    ): Boolean {
        return startDate != null &&
                validateTime(startTime) == null &&
                endDate != null &&
                validateTime(endTime) == null &&
                startLatitude != null &&
                startLongitude != null &&
                endLatitude != null &&
                endLongitude != null
    }

    fun retry() {
        _uiState.update { it.copy(errorMessage = null) }
        loadCar()
    }

    fun dismissLocationPermissionDialog() {
        _uiState.update { it.copy(needsLocationPermission = false) }
    }

    fun dismissCarAlreadyRentedDialog() {
        _uiState.update { it.copy(showCarAlreadyRentedDialog = false) }
    }
}
