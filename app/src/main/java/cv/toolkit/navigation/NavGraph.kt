package cv.toolkit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cv.toolkit.screens.*

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object IpLookup : Screen("ip_lookup")
    object NetworkStressTest : Screen("network_stress_test")
    object DrmInfo : Screen("drm_info")
    object DeviceInfo : Screen("device_info")
    object SpeedTest : Screen("speed_test")
    object NetworkScan : Screen("network_scan")
    object DnsLookup : Screen("dns_lookup")
    object PingTest : Screen("ping_test")
    object PortScan : Screen("port_scan")
    object Traceroute : Screen("traceroute")
    object SubnetCalculator : Screen("subnet_calculator")
    object SSLChecker : Screen("ssl_checker")
    object IPCalculator : Screen("ip_calculator")
    object Base64Tool : Screen("base64_tool")
    object UrlEncoder : Screen("url_encoder")
    object BinaryConverter : Screen("binary_converter")
    object HashGenerator : Screen("hash_generator")
    object CaesarCipher : Screen("caesar_cipher")
    object MorseCode : Screen("morse_code")
    object HexEncoder : Screen("hex_encoder")
    object AsciiConverter : Screen("ascii_converter")
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

        composable(Screen.SpeedTest.route) {
            SpeedTestScreen(navController = navController)
        }

        composable(Screen.NetworkScan.route) {
            NetworkScanScreen(navController = navController)
        }

        composable(Screen.DnsLookup.route) {
            DnsLookupScreen(navController = navController)
        }

        composable(Screen.PingTest.route) {
            PingTestScreen(navController = navController)
        }

        composable(Screen.PortScan.route) {
            PortScanScreen(navController = navController)
        }

        composable(Screen.Traceroute.route) {
            TracerouteScreen(navController = navController)
        }

        composable(Screen.SubnetCalculator.route) {
            SubnetCalculatorScreen(navController = navController)
        }

        composable(Screen.SSLChecker.route) {
            SSLCheckerScreen(navController = navController)
        }

        composable(Screen.IPCalculator.route) {
            IPCalculatorScreen(navController = navController)
        }

        composable(Screen.Base64Tool.route) {
            Base64Screen(navController = navController)
        }

        composable(Screen.UrlEncoder.route) {
            UrlEncoderScreen(navController = navController)
        }

        composable(Screen.BinaryConverter.route) {
            BinaryConverterScreen(navController = navController)
        }

        composable(Screen.HashGenerator.route) {
            HashGeneratorScreen(navController = navController)
        }

        composable(Screen.CaesarCipher.route) {
            CaesarCipherScreen(navController = navController)
        }

        composable(Screen.MorseCode.route) {
            MorseCodeScreen(navController = navController)
        }

        composable(Screen.HexEncoder.route) {
            HexEncoderScreen(navController = navController)
        }

        composable(Screen.AsciiConverter.route) {
            AsciiConverterScreen(navController = navController)
        }
    }
}
