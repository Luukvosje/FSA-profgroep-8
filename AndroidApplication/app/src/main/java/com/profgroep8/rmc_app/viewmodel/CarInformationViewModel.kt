package com.profgroep8.rmc_app.viewmodel

import PhotoUtils
import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.Car
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CarInformationUiState(
    val car: Car? = null,
    val showImageSourceDialog: Boolean = false,
    val showPermissionDeniedWarning: Boolean = false ,
)

class CarInformationViewModel(
    private val sf : ServiceFactory,
    val carId: Int
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
                        _uiState.update { it.copy(car = car) }
                    imageResult.onSuccess { bytes ->
                        val updatedCar = car.copy(imageBytes = bytes)
                        _uiState.update {
                            it.copy(car = updatedCar)
                        }
                    }
                    imageResult.onError {
                        _uiState.update {
                            it.copy(car = car.copy(imageBytes = null))
                        }
                    }

                    println("AAP, $imageResult")
                }
            }
        }
    }

    fun addPhoto(uri: String, context: Context) {
        viewModelScope.launch {
            withLoading {
                if(uiState.value.car == null){
                    throw Error();
                }
                val file = PhotoUtils.getFileFromUri(context, uri.toUri())

                if(file == null){
                    throw Error("File missing");
                }

                val uploadImage = sf.carService.uploadImage(
                    carID = uiState.value.car!!.carID,
                    image = file,
                );
                uploadImage.onSuccess { it ->
                    getCar(carId)
                }
            }
        }
    }

    fun deleteCarImage() {
        viewModelScope.launch {
            withLoading {
                val car = uiState.value.car ?: return@withLoading
                sf.carService.deleteImage(car.carID)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                car = car.copy(imageBytes = null)
                            )
                        }
                    }
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

}