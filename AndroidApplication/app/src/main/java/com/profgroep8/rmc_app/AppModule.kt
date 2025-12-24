package com.profgroep8.rmc_app

import com.profgroep8.rmc_app.presentation.screens.login.LoginViewModel
import com.profgroep8.rmc_app.viewmodel.CarInformationViewModel
import com.profgroep8.rmc_app.viewmodel.ShowAllCarsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel() }
    viewModel { ShowAllCarsViewModel(get()) }
    viewModel { (carId: Int) ->
        CarInformationViewModel(
            sf = get(),
            carId = carId
        )
    }}