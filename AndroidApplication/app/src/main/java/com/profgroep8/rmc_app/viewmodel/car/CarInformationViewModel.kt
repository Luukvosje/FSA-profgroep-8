package com.profgroep8.rmc_app.viewmodel.car

import PhotoUtils
import RmcScreen
import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.Car
import com.profgroep8.rmc_app.viewmodel.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CarInformationUiState(
    val car: Car? = null,
    val showImageSourceDialog: Boolean = false,
    val showPermissionDeniedWarning: Boolean = false,
    val isImageLoading: Boolean = false,
    val errorMessage: String? = null
)

class CarInformationViewModel(
    private val sf : ServiceFactory,
    val carId: Int,
): BaseViewModel() {

    private val _uiState = MutableStateFlow(CarInformationUiState())
    val uiState: StateFlow<CarInformationUiState> = _uiState.asStateFlow()

    init {
        getCar(carId)
    }

    private fun getCar(carId: Int) {
        viewModelScope.launch {
            withLoading {

                val carDeferred = async { sf.carService.getSingleCar(carId) }
                val imageDeferred = async { sf.carService.getImage(carId) }

                val carResult = carDeferred.await()
                val imageResult = imageDeferred.await()

                carResult.onSuccess { car ->
                        _uiState.update { it.copy(car = car, isImageLoading = true) }

                    imageResult.onSuccess { bytes ->
                        _uiState.update {
                            it.copy(
                                car = car.copy(imageBytes = bytes),
                                isImageLoading = false
                            )
                        }
                    }

                    imageResult.onError {
                        _uiState.update {
                            it.copy(
                                car = car.copy(imageBytes = null),
                                isImageLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun addPhoto(uri: String, context: Context) {
        viewModelScope.launch {
            withLoading {
                if (uiState.value.car == null) {
                    throw Error();
                }
                val file = PhotoUtils.getFileFromUri(context, uri.toUri())

                if (file == null) {
                   return@withLoading
                }

                val uploadImage = sf.carService.uploadImage(
                    carID = uiState.value.car!!.carID,
                    image = file,
                );
                _uiState.update { it.copy(isImageLoading = true) }

                uploadImage.onSuccess {
                    getCar(carId)
                    _uiState.update { it.copy(isImageLoading = false) }
                }
                uploadImage.onError {
                    _uiState.update { it.copy(isImageLoading = false) }
                }
            }
        }
    }

    fun deleteCarImage() {
        viewModelScope.launch {
            withLoading {
                val car = uiState.value.car ?: return@withLoading
                _uiState.update { it.copy(isImageLoading = true) }
                sf.carService.deleteImage(car.carID)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                car = car.copy(imageBytes = null),
                                isImageLoading = false
                            )
                        }
                    }
                    .onError { _uiState.update { it.copy(isImageLoading = false) } }
            }
        }
    }

    fun showImageSourceDialog() =
        _uiState.update { it.copy(showImageSourceDialog = true) }

    fun dismissImageSourceDialog() =
        _uiState.update { it.copy(showImageSourceDialog = false) }

    fun dismissPermissionWarning() =
        _uiState.update { it.copy(showPermissionDeniedWarning = false) }

    fun onPhotoPermissionResult(granted: Boolean) {
        if (granted) {
            showImageSourceDialog()
        } else {
            _uiState.update { it.copy(showPermissionDeniedWarning = true) }
        }
    }

    fun deleteCar(navigateToScreen: (route: String) -> Unit){
        viewModelScope.launch {
            withLoading {
                val car = uiState.value.car ?: return@withLoading
                val res = sf.carService.deleteCar(car.carID)
                
                res.onSuccess {
                    if (it) {
                        navigateToScreen(RmcScreen.AllCars.name);
                    } else {
                        TODO("Show Toast")
                    }
                }
            }
        }
    }

}