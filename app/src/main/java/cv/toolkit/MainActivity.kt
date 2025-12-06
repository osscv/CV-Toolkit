package cv.toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import cv.toolkit.navigation.NavGraph
import cv.toolkit.ui.theme.CVToolkitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CVToolkitTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}