import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.profgroep8.rmc_app.R
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.digitalarchitects.rmc_app.presentation.screens.welcome.WelcomeScreen
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
}

@Preview(showBackground = true)
@Composable
fun RmcApp(
    navController: NavHostController = rememberNavController()
) {
    val welcomeViewModel: WelcomeViewModel = viewModel()
    val registerViewModel: RegisterViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()

    val startDestination = RmcScreen.Home
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

    }
}

@Preview
@Composable
fun Preview() {
    RMCappTheme() {
        RmcApp()
    }
}