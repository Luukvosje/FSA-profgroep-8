
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
import com.profgroep8.rmc_app.presentation.screens.AddCar.AddCarScreen
import com.profgroep8.rmc_app.presentation.screens.CarInformation.CarInformationScreen
import com.profgroep8.rmc_app.presentation.screens.home.HomeScreen
import com.profgroep8.rmc_app.presentation.screens.login.LoginScreen
import com.profgroep8.rmc_app.presentation.screens.register.RegisterScreen
import com.profgroep8.rmc_app.presentation.screens.showCars.AllCarsScreen
import com.profgroep8.rmc_app.ui.theme.RMCappTheme

enum class RmcScreen(@StringRes val title: Int){
    Welcome(R.string.app_name),
    Register(R.string.register),
    Login(R.string.login),
    Home(R.string.home),
    AddCar(R.string.home_add_car),
    CarInformation(R.string.car_information),
    AllCars(R.string.home_manage_cars)
}

@Composable
fun RmcApp(
    navController: NavHostController = rememberNavController()
) {
    val startDestination = RmcScreen.AllCars

    NavHost(
        navController,
        startDestination.name
    ) {
        composable(RmcScreen.Welcome.name) {
            WelcomeScreen(
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
        composable(RmcScreen.Home.name){
            HomeScreen(
                navigateToScreen = { navController.navigate(it)},
                userName = "LoekTEST"
            )
        }
        composable(RmcScreen.AddCar.name) {
            AddCarScreen(
                navigateToScreen = {navController.navigate((it))}
            )
        }
        composable(
            route = "${RmcScreen.CarInformation.name}/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.IntType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId");
            CarInformationScreen(
                carId = carId,
                navigateToScreen = {navController.navigate(it)}
            )
        }
        composable(RmcScreen.AllCars.name) {
            AllCarsScreen(
                navigateToScreen = {navController.navigate(it)}
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