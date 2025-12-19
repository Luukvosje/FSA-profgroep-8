
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.digitalarchitects.rmc_app.presentation.screens.welcome.WelcomeScreen
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.presentation.screens.CarInformation.CarInformationScreen
import com.profgroep8.rmc_app.presentation.screens.addCar.AddCarScreen
import com.profgroep8.rmc_app.presentation.screens.home.HomeScreen
import com.profgroep8.rmc_app.presentation.screens.login.LoginScreen
import com.profgroep8.rmc_app.presentation.screens.login.LoginViewModel
import com.profgroep8.rmc_app.presentation.screens.register.RegisterScreen
import com.profgroep8.rmc_app.presentation.screens.register.RegisterViewModel
import com.profgroep8.rmc_app.presentation.screens.welcome.WelcomeViewModel
import com.profgroep8.rmc_app.ui.theme.RMCappTheme

enum class RmcScreen(@StringRes val title: Int){
    Welcome(R.string.app_name),
    Register(R.string.register),
    Login(R.string.login),
    Home(R.string.home),
    AddCar(R.string.home_add_car),
    CarInformation(R.string.car_information)
}

@Composable
fun RmcApp(
    navController: NavHostController = rememberNavController()
) {
    val welcomeViewModel: WelcomeViewModel = viewModel()
    val registerViewModel: RegisterViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()

    val startDestination = RmcScreen.Welcome

    NavHost(
        navController,
        startDestination.name
    ) {
        composable(RmcScreen.Welcome.name) {
            WelcomeScreen(
                viewModel = welcomeViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(RmcScreen.Register.name) {
            RegisterScreen(
                viewModel = registerViewModel,
                navigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable(RmcScreen.Login.name) {
            LoginScreen(
                viewModel = loginViewModel,
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
        composable(RmcScreen.CarInformation.name) {
            CarInformationScreen(
                car = null,
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