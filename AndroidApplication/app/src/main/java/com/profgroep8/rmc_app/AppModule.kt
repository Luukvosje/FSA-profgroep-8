package com.profgroep8.rmc_app

import com.profgroep8.rmc_app.viewmodel.AddCarViewModel
import com.profgroep8.rmc_app.viewmodel.BonusPointsViewModel
import com.profgroep8.rmc_app.viewmodel.CarInformationViewModel
import com.profgroep8.rmc_app.viewmodel.FilterCarsViewModel
import com.profgroep8.rmc_app.viewmodel.HomeViewModel
import com.profgroep8.rmc_app.viewmodel.LoginViewModel
import com.profgroep8.rmc_app.viewmodel.RentalInformationViewModel
import com.profgroep8.rmc_app.viewmodel.RegisterViewModel
import com.profgroep8.rmc_app.viewmodel.ShowAllCarsViewModel
import com.profgroep8.rmc_app.viewmodel.ShowAllRentalsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { BonusPointsViewModel(get()) }
    viewModel { ShowAllCarsViewModel(get()) }
    viewModel { (carId: Int) ->
        CarInformationViewModel(
            sf = get(),
            carId = carId
        )
    }
    viewModel { AddCarViewModel(get()) }
    viewModel { FilterCarsViewModel(get()) }
    viewModel { ShowAllRentalsViewModel(get()) }
    viewModel { (rentalId: Int) ->
        RentalInformationViewModel(
            serviceFactory = get(),
            rentalId = rentalId
        )
    }
}

