package com.profgroep8.rmc_app

import com.profgroep8.rmc_app.data.TokenManager
import com.profgroep8.rmc_app.viewmodel.car.AddCarViewModel
import com.profgroep8.rmc_app.viewmodel.rental.AddRentalViewModel
import com.profgroep8.rmc_app.viewmodel.user.BonusPointsViewModel
import com.profgroep8.rmc_app.viewmodel.car.CarInformationViewModel
import com.profgroep8.rmc_app.viewmodel.car.FilterCarsViewModel
import com.profgroep8.rmc_app.viewmodel.HomeViewModel
import com.profgroep8.rmc_app.viewmodel.user.LoginViewModel
import com.profgroep8.rmc_app.viewmodel.rental.RentalInformationViewModel
import com.profgroep8.rmc_app.viewmodel.user.RegisterViewModel
import com.profgroep8.rmc_app.viewmodel.car.ShowAllCarsViewModel
import com.profgroep8.rmc_app.viewmodel.rental.ShowAllRentalsViewModel
import com.profgroep8.rmc_app.viewmodel.user.UserInfoViewModel
import com.profgroep8.rmc_app.viewmodel.user.WelcomeViewModel
import com.profgroep8.rmc_app.viewmodel.rental.RentalMapViewModel
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
    viewModel { UserInfoViewModel(get()) }
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
    viewModel { RentalMapViewModel(get()) }
}

