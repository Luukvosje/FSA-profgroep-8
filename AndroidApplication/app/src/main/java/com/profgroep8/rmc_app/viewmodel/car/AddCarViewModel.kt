package com.profgroep8.rmc_app.viewmodel.car

import RmcScreen
import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.utils.UiText
import com.profgroep8.rmc_app.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddCarUiState(
    val licensePlate: String = "",
    val licensePlateError: UiText? = null,
    val isValid: Boolean = false
)

class AddCarViewModel(
    private val sf: ServiceFactory
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AddCarUiState())
    val uiState: StateFlow<AddCarUiState> = _uiState.asStateFlow()

    fun onLicensePlateChange(value: String) {
        _uiState.update {
            it.copy(
                licensePlate = value,
                licensePlateError = validateLicensePlate(value),
                isValid = validateLicensePlate(value) == null
            )
        }
    }

    fun addCar(navigateToScreen: (route: String) -> Unit) {
        viewModelScope.launch {
            withLoading {
                val res = sf.carService.createCarByLicense(_uiState.value.licensePlate);

                println("AAP $res ${_uiState.value.licensePlate}")
                res.onSuccess {
                    navigateToScreen("${RmcScreen.CarInformation.name}/${it.carID}")
                }
                res.onError {
                    _uiState.update {
                        it.copy(
                            licensePlateError = UiText.StringResource(R.string.invalid_license),
                            isValid = false
                        )
                    }
                }
            }
        }
    }

    private fun validateLicensePlate(value: String): UiText? {
        if (value.isBlank()) return UiText.StringResource(R.string.invalid_license )
        if (value.length < 6) return UiText.StringResource(R.string.invalid_license_to_short)
        return null
    }

}