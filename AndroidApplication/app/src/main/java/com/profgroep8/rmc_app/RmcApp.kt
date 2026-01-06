
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.digitalarchitects.rmc_app.presentation.screens.welcome.WelcomeScreen
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.screens.AddCar.AddCarScreen
import com.profgroep8.rmc_app.ui.screens.CarInformation.CarInformationScreen
import com.profgroep8.rmc_app.ui.screens.home.HomeScreen
import com.profgroep8.rmc_app.ui.screens.login.LoginScreen
import com.profgroep8.rmc_app.ui.screens.register.RegisterScreen
import com.profgroep8.rmc_app.ui.screens.showCars.AllCarsScreen
import com.profgroep8.rmc_app.ui.theme.RMCappTheme

enum class RmcScreen(@StringRes val title: Int){
    Welcome(R.string.app_name),
    Register(R.string.register),
    Login(R.string.login),
    Home(R.string.home),
    AddCar(R.string.home_add_car),
    CarInformation(R.string.car_information),
    FilterCars(R.string.home_search_car),
    AllCars(R.string.home_manage_cars),
    ViewPoints(R.string.home_view_points),
    Rentals(R.string.home_reservations),
    RentalInformation(R.string.rental_information)
}

@Composable
fun RmcApp(
    navController: NavHostController = rememberNavController()
) {
    val startDestination = RmcScreen.Welcome

    NavHost(
        navController,
        startDestination.name
    ) {
        composable(RmcScreen.Welcome.name) {
            _root_ide_package_.com.digitalarchitects.rmc_app.presentation.screens.welcome.WelcomeScreen(
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(RmcScreen.Register.name) {
            _root_ide_package_.com.profgroep8.rmc_app.ui.screens.register.RegisterScreen(
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(RmcScreen.Login.name) {
            _root_ide_package_.com.profgroep8.rmc_app.ui.screens.login.LoginScreen(
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(RmcScreen.Home.name) {
            com.profgroep8.rmc_app.ui.screens.home.HomeScreen(
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.AddCar.name) {
            _root_ide_package_.com.profgroep8.rmc_app.ui.screens.AddCar.AddCarScreen(
                navigateToScreen = { navController.navigate((it)) }
            )
        }

        composable(
            route = "${RmcScreen.CarInformation.name}/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.IntType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId");
            _root_ide_package_.com.profgroep8.rmc_app.ui.screens.CarInformation.CarInformationScreen(
                carId = carId,
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.AllCars.name) {
            _root_ide_package_.com.profgroep8.rmc_app.ui.screens.showCars.AllCarsScreen(
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.FilterCars.name) {
            _root_ide_package_.com.profgroep8.rmc_app.ui.screens.filterCars.FilterCarsScreen(
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.ViewPoints.name) {
            _root_ide_package_.com.profgroep8.rmc_app.ui.screens.bonuspoints.BonusPointsScreen(
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.Rentals.name) {
            _root_ide_package_.com.profgroep8.rmc_app.ui.screens.showRentals.AllRentalsScreen(
                navigateToScreen = { navController.navigate(it)}
            )
        }
        
        composable(
            route = "${RmcScreen.RentalInformation.name}/{rentalId}",
            arguments = listOf(navArgument("rentalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val rentalId = backStackEntry.arguments?.getInt("rentalId")
            com.profgroep8.rmc_app.ui.screens.RentalInformation.RentalInformationScreen(
                rentalId = rentalId,
                navigateToScreen = { navController.navigate(it) }
            )
        }
    }
}

@Preview
@Composable
fun Preview() {
    RMCappTheme() {
        RmcApp()
    }
}