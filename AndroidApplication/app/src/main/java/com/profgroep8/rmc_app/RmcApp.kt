package com.profgroep8.rmc_app

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

enum class RmcScreen(@StringRes val title: Int) {
    Welcome(title = R.string.screen_title_welcome),
    Register(title = R.string.screen_title_register),
    TermsAndConditions(title = R.string.screen_title_terms),
    Login(title = R.string.screen_title_login),
    RentACar(title = R.string.screen_title_rent_a_car),
    Search(title = R.string.screen_title_search),
    MyRentals(title = R.string.screen_title_my_rentals),
    RentOutMyCar(title = R.string.screen_title_rent_my_car),
    MyVehicles(title = R.string.screen_title_my_vehicles),
    RegisterVehicle(title = R.string.screen_title_register_vehicle),
    MyAccount(title = R.string.screen_title_my_account),
    EditMyAccount(title = R.string.screen_title_edit_account),
    EditMyVehicle(title = R.string.screen_title_edit_vehicle),
    RmcTestScreen(title = R.string.rmcTestScreenTitle),
    RmcLocationTestScreen(title = R.string.rmcTestScreenTitle)
}

@Preview(showBackground = true)
@Composable
fun RmcApp(
    navController: NavHostController = rememberNavController()
) {

    val repositoryTestViewModel: RepositoryTestViewModel = hiltViewModel()
    val welcomeViewModel: WelcomeViewModel = hiltViewModel()
    val registerViewModel: RegisterViewModel = hiltViewModel()
    val termsAndConditionsViewModel: TermsAndConditionsViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val rentACarViewModel: RentACarViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()
    val rentOutMyCarViewModel: RentOutMyCarViewModel = hiltViewModel()
    val myVehiclesViewModel: MyVehiclesViewModel = hiltViewModel()
    val editMyAccountViewModel: EditMyAccountViewModel = hiltViewModel()
    val registerVehicleViewModel: RegisterVehicleViewModel = hiltViewModel()
    val myRentalsViewModel: MyRentalsViewModel = hiltViewModel()
    val editMyVehicleViewModel: EditMyVehicleViewModel = hiltViewModel()
    val myAccountViewModel: MyAccountViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = RmcScreen.Welcome.name,
    ) {
        composable(route = RmcScreen.Welcome.name) {
            WelcomeScreen(
                viewModel = welcomeViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(route = RmcScreen.Register.name) {
            RegisterScreen(
                viewModel = registerViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(route = RmcScreen.TermsAndConditions.name) {
            TermsAndConditionsScreen(
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(route = RmcScreen.Login.name) {
            LoginScreen(
                // onForgotPasswordTextClicked = { navController.navigate(RmcScreen.ForgotPassword.name) },
                viewModel = loginViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(route = RmcScreen.RentACar.name) {
            RentACarScreen(
                viewModel = rentACarViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(route = RmcScreen.Search.name) {
            SearchScreen(
                viewModel = searchViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(route = RmcScreen.MyRentals.name) {
            MyRentalsScreen(
                viewModel = myRentalsViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(route = RmcScreen.RentOutMyCar.name) {

            RentOutMyCarScreen(
                viewModel = rentOutMyCarViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(route = RmcScreen.MyVehicles.name) {
            MyVehiclesScreen(
                viewModel = myVehiclesViewModel,
                navigateToScreen = { route -> navController.navigate(route) },
                navigateToEditVehicle = { route, vehicleId ->
                    navController.navigate("$route/$vehicleId")
                }
            )
        }
        composable(route = RmcScreen.RegisterVehicle.name) {
            RegisterVehicleScreen(
                viewModel = registerVehicleViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(
            route = "${RmcScreen.EditMyVehicle.name}/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId")
            EditMyVehicleScreen(
                viewModel = editMyVehicleViewModel,
                navigateToScreen = { route -> navController.navigate(route) },
                vehicleId = vehicleId
            )
        }
        composable(route = RmcScreen.MyAccount.name) {
            MyAccountScreen(
                viewModel = myAccountViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(route = RmcScreen.EditMyAccount.name) {
            EditMyAccountScreen(
                viewModel = editMyAccountViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(route = RmcScreen.RmcTestScreen.name) {
            RepositoryTestScreen(
                viewModel = repositoryTestViewModel
            )
        }
    }
}

@Preview
@Composable
fun Preview() {
    RmcAppTheme {
        RmcApp()
    }
}