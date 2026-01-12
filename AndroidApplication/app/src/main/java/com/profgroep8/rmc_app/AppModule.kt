package com.profgroep8.rmc_app

import com.profgroep8.rmc_app.data.TokenManager
import com.profgroep8.rmc_app.viewmodel.AddCarViewModel
import com.profgroep8.rmc_app.viewmodel.AddRentalViewModel
import com.profgroep8.rmc_app.viewmodel.BonusPointsViewModel
import com.profgroep8.rmc_app.viewmodel.CarInformationViewModel
import com.profgroep8.rmc_app.viewmodel.FilterCarsViewModel
import com.profgroep8.rmc_app.viewmodel.HomeViewModel
import com.profgroep8.rmc_app.viewmodel.LoginViewModel
import com.profgroep8.rmc_app.viewmodel.RentalInformationViewModel
import com.profgroep8.rmc_app.viewmodel.RegisterViewModel
import com.profgroep8.rmc_app.viewmodel.ShowAllCarsViewModel
import com.profgroep8.rmc_app.viewmodel.ShowAllRentalsViewModel
import com.profgroep8.rmc_app.viewmodel.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { TokenManager(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { WelcomeViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
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
    viewModel { (carId: Int) ->
        AddRentalViewModel(
            serviceFactory = get(),
            carId = carId
        )
    }
}

