package coty.band.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coty.band.app.domain.Measurement
import coty.band.app.presentation.auth.LoginScreen
import coty.band.app.presentation.auth.RegisterScreen
import coty.band.app.presentation.camera.CameraFlowScreen
import coty.band.app.presentation.history.MeasurementHistoryScreen
import coty.band.app.presentation.result.MeasurementResultScreen

object Routes {
    const val LOGIN    = "login"
    const val REGISTER = "register"
    const val CAMERA   = "camera"
    const val RESULT   = "result"
    const val HISTORY  = "history"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val startDestination = Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess      = { navController.navigate(Routes.HISTORY) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }},
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Routes.HISTORY) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }},
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.CAMERA) { entry ->
            CameraFlowScreen(
                onAnalysisComplete = { measurement ->
                    entry.savedStateHandle["measurement"] = measurement
                    navController.navigate(Routes.RESULT)
                }
            )
        }

        composable(Routes.RESULT) {
            val measurement = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Measurement>("measurement")

            if (measurement != null) {
                MeasurementResultScreen(
                    measurement          = measurement,
                    onSaved              = {
                        navController.navigate(Routes.HISTORY) {
                            popUpTo(Routes.CAMERA) { inclusive = true }
                        }
                    },
                    onNavigateToHistory  = {
                        navController.navigate(Routes.HISTORY) {
                            popUpTo(Routes.CAMERA) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.HISTORY) {
            MeasurementHistoryScreen(
                onNewMeasurement = { navController.navigate(Routes.CAMERA) },
                onLogout         = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HISTORY) { inclusive = true }
                    }
                }
            )
        }
    }
}
