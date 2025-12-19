package com.profgroep8.rmc_app.presentation.screens.addCar

import androidx.lifecycle.ViewModel
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.Car
import com.example.network.services.ApiResult
import com.example.network.services.ServiceFactoryImpl

class AddCarViewModel(
    ServiceFactoryImpl : ServiceFactory
): ViewModel() {

    suspend fun GetAllCars(): ApiResult<List<Car>> {
        return ServiceFactoryImpl().carService.getAllCars()
    }
}