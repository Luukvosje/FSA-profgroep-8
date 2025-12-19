package com.profgroep8.rmc_app

import com.profgroep8.rmc_app.presentation.screens.addCar.AddCarViewModel
import com.profgroep8.rmc_app.presentation.screens.login.LoginViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val viewModelModule = module {
    viewModel { LoginViewModel() }
    viewModel { AddCarViewModel(get()) }

}