package cv.toolkit.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cv.toolkit.R
import cv.toolkit.ads.BannerAd
import cv.toolkit.navigation.Screen

data class ToolItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val networkTools = listOf(
        // Basic connectivity tests first
        ToolItem(stringResource(R.string.ping_test_title), stringResource(R.string.ping_test_desc), Icons.Filled.NetworkPing, Screen.PingTest.route),
        ToolItem(stringResource(R.string.traceroute_title), stringResource(R.string.traceroute_desc), Icons.AutoMirrored.Filled.AltRoute, Screen.Traceroute.route),
        // Scanning tools
        ToolItem(stringResource(R.string.port_scan_title), stringResource(R.string.port_scan_desc), Icons.Filled.Router, Screen.PortScan.route),
        ToolItem(stringResource(R.string.network_scan_title), stringResource(R.string.network_scan_desc), Icons.Filled.Radar, Screen.NetworkScan.route),
        // Lookup tools
        ToolItem(stringResource(R.string.ip_lookup_title), stringResource(R.string.ip_lookup_desc), Icons.Filled.LocationOn, Screen.IpLookup.route),
        ToolItem(stringResource(R.string.dns_lookup_title), stringResource(R.string.dns_lookup_desc), Icons.Filled.Dns, Screen.DnsLookup.route),
        // Calculator tools
        ToolItem(stringResource(R.string.subnet_calc_title), stringResource(R.string.subnet_calc_desc), Icons.Filled.Calculate, Screen.SubnetCalculator.route),
        // Security tools
        ToolItem(stringResource(R.string.ssl_checker_title), stringResource(R.string.ssl_checker_desc), Icons.Filled.Security, Screen.SSLChecker.route),
        // Performance test
        ToolItem(stringResource(R.string.speed_test_title), stringResource(R.string.speed_test_desc), Icons.Filled.Speed, Screen.SpeedTest.route)
    )

    val utilityTools = listOf(
        ToolItem(stringResource(R.string.ip_calc_title), stringResource(R.string.ip_calc_desc), Icons.Filled.Transform, Screen.IPCalculator.route),
        ToolItem(stringResource(R.string.base64_title), stringResource(R.string.base64_desc), Icons.Filled.Code, Screen.Base64Tool.route),
        ToolItem(stringResource(R.string.url_encoder_title), stringResource(R.string.url_encoder_desc), Icons.Filled.Link, Screen.UrlEncoder.route),
        ToolItem(stringResource(R.string.binary_converter_title), stringResource(R.string.binary_converter_desc), Icons.Filled.DataObject, Screen.BinaryConverter.route),
        ToolItem(stringResource(R.string.hash_generator_title), stringResource(R.string.hash_generator_desc), Icons.Filled.Tag, Screen.HashGenerator.route),
        ToolItem(stringResource(R.string.caesar_cipher_title), stringResource(R.string.caesar_cipher_desc), Icons.Filled.Key, Screen.CaesarCipher.route),
        ToolItem(stringResource(R.string.morse_code_title), stringResource(R.string.morse_code_desc), Icons.Filled.GraphicEq, Screen.MorseCode.route),
        ToolItem(stringResource(R.string.hex_encoder_title), stringResource(R.string.hex_encoder_desc), Icons.Filled.Memory, Screen.HexEncoder.route),
        ToolItem(stringResource(R.string.ascii_converter_title), stringResource(R.string.ascii_converter_desc), Icons.Filled.TextFields, Screen.AsciiConverter.route)
    )

    val deviceTools = listOf(
        ToolItem(stringResource(R.string.drm_info_title), stringResource(R.string.drm_info_desc), Icons.Filled.Lock, Screen.DrmInfo.route),
        ToolItem(stringResource(R.string.device_info_title), stringResource(R.string.device_info_desc), Icons.Filled.Phone, Screen.DeviceInfo.route)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "CV Toolkit Logo",
                        modifier = Modifier.height(40.dp),
                        contentScale = ContentScale.Fit
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Privacy & Terms") },
                                onClick = {
                                    showMenu = false
                                    showPrivacyDialog = true
                                },
                                leadingIcon = { Icon(Icons.Filled.Policy, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("About Author") },
                                onClick = {
                                    showMenu = false
                                    showAboutDialog = true
                                },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                item(span = { GridItemSpan(2) }) {
                    Text("Network Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
                items(networkTools) { tool ->
                    ToolCard(tool = tool, onClick = { navController.navigate(tool.route) })
                }
                item(span = { GridItemSpan(2) }) {
                    Text("Utility Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
                items(utilityTools) { tool ->
                    ToolCard(tool = tool, onClick = { navController.navigate(tool.route) })
                }
                item(span = { GridItemSpan(2) }) {
                    Text("Device Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
                items(deviceTools) { tool ->
                    ToolCard(tool = tool, onClick = { navController.navigate(tool.route) })
                }
            }
            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }

    // Privacy & Terms Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy & Terms", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Privacy Policy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        """CV Toolkit respects your privacy and is committed to protecting your personal data.

Data Collection:
- We DO NOT upload your network information to our servers or anywhere else.
- All network scans, device information, and lookup results stay on your phone only.
- All data processing is performed locally on your device.
- We do not collect, store, transmit, or share your personal data.

Permissions:
- Internet: Required for IP lookups, DNS queries, and network connectivity tests.
- Network State: Used to detect your current network configuration.
- Foreground Service: Used for background speed tests.

Advertising:
- This app displays advertisements provided by Google AdMob.
- Google may collect and use data for personalized advertising. Please refer to Google's Privacy Policy for more information.

Local Storage:
- All data including IP lookup history is stored locally on your device only.
- No data is uploaded to our servers or any external servers.
- You can clear this data by clearing the app's data in your device settings.""",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Terms of Use", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        """By using CV Toolkit, you agree to the following terms:

1. Intended Use: This app is designed for legitimate network diagnostics, troubleshooting, and educational purposes only.

2. Responsible Use: You agree to use this app only on networks and devices you own or have explicit permission to test. Unauthorized network scanning may be illegal in your jurisdiction.

3. No Warranty: This app is provided "as is" without warranty of any kind. The developer is not responsible for any damages or legal issues arising from the use of this app.

4. Accuracy: While we strive for accuracy, network information and test results may vary and should not be relied upon for critical decisions.

5. Third-Party Services: Some features rely on third-party APIs (IP lookup, DNS resolution). Their availability and accuracy are not guaranteed.

6. Updates: We may update these terms at any time. Continued use of the app constitutes acceptance of any changes.

Last updated: December 2024""",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // About Author Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Author", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Khoo Lay Yang",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "www.dkly.net",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dkly.net")))
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Copyright All Rights Reserved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ToolCard(
    tool: ToolItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon container with background
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.title,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = tool.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = MaterialTheme.typography.bodySmall.fontSize.value.times(1.3).sp
            )
        }
    }
}

