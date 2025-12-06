package cv.toolkit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cv.toolkit.screens.DeviceInfoScreen
import cv.toolkit.screens.DrmInfoScreen
import cv.toolkit.screens.IpLookupScreen
import cv.toolkit.screens.MainScreen
import cv.toolkit.screens.NetworkStressTestScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object IpLookup : Screen("ip_lookup")
    object NetworkStressTest : Screen("network_stress_test")
    object DrmInfo : Screen("drm_info")
    object DeviceInfo : Screen("device_info")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        
        composable(Screen.IpLookup.route) {
            IpLookupScreen(navController = navController)
        }
        
        composable(Screen.NetworkStressTest.route) {
            NetworkStressTestScreen(navController = navController)
        }
        
        composable(Screen.DrmInfo.route) {
            DrmInfoScreen(navController = navController)
        }
        
        composable(Screen.DeviceInfo.route) {
            DeviceInfoScreen(navController = navController)
        }
    }
}

