
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.screens.HomeScreen
import com.profgroep8.rmc_app.ui.screens.car.AddCarScreen
import com.profgroep8.rmc_app.ui.screens.car.AllCarsScreen
import com.profgroep8.rmc_app.ui.screens.car.CarInformationScreen
import com.profgroep8.rmc_app.ui.screens.car.FilterCarsScreen
import com.profgroep8.rmc_app.ui.screens.rental.AddRentalScreen
import com.profgroep8.rmc_app.ui.screens.rental.AllRentalsScreen
import com.profgroep8.rmc_app.ui.screens.rental.RentalInformationScreen
import com.profgroep8.rmc_app.ui.screens.rental.RentalMapScreen
import com.profgroep8.rmc_app.ui.screens.user.BonusPointsScreen
import com.profgroep8.rmc_app.ui.screens.user.LoginScreen
import com.profgroep8.rmc_app.ui.screens.user.RegisterScreen
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
    Rentals(R.string.home_rentals),
    RentalInformation(R.string.rental_information),
    AddRental(R.string.create_rental),
    RentalMap(R.string.rental_map),
    UserInformation(R.string.user_information)
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
            _root_ide_package_.com.profgroep8.rmc_app.presentation.screens.welcome.WelcomeScreen(
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(RmcScreen.Register.name) {
            RegisterScreen(
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(RmcScreen.Login.name) {
            LoginScreen(
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(RmcScreen.Home.name) {
            HomeScreen(
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.AddCar.name) {
            AddCarScreen(
                navigateToScreen = { navController.navigate((it)) }
            )
        }

        composable(
            route = "${RmcScreen.CarInformation.name}/{carId}/{available}",
            arguments = listOf(
                navArgument("carId") { type = NavType.IntType },
                navArgument("available") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: 0
            val available = backStackEntry.arguments?.getBoolean("available") ?: false

            CarInformationScreen(
                carId = carId,
                available = available,
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.AllCars.name) {
            AllCarsScreen(
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.FilterCars.name) {
            FilterCarsScreen(
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.ViewPoints.name) {
            BonusPointsScreen(
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.Rentals.name) {
            AllRentalsScreen(
                navigateToScreen = { navController.navigate(it) }
            )
        }
        
        composable(
            route = "${RmcScreen.RentalInformation.name}/{rentalId}",
            arguments = listOf(navArgument("rentalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val rentalId = backStackEntry.arguments?.getInt("rentalId")
            RentalInformationScreen(
                rentalId = rentalId,
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(
            route = "${RmcScreen.AddRental.name}/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.IntType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId")
            AddRentalScreen(
                carId = carId,
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.RentalMap.name) {
            RentalMapScreen(
                navigateToScreen = { navController.navigate(it) }
            )
        }

        composable(RmcScreen.UserInformation.name) {
            com.profgroep8.rmc_app.ui.screens.userinfo.UserInfoScreen(
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