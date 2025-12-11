package cv.toolkit.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cv.toolkit.R
import cv.toolkit.ads.AdMobManager
import cv.toolkit.ads.BannerAd
import cv.toolkit.data.IpHistoryEntry
import cv.toolkit.data.IpHistoryManager
import cv.toolkit.data.IpInfo
import cv.toolkit.repository.DetectedIps
import cv.toolkit.repository.IpLookupRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatTimeAgo(timestamp: Long, now: Long): String {
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return when {
        seconds < 60 -> "$seconds sec ago"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hour ago"
        else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpLookupScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val repository = remember { IpLookupRepository() }
    val historyManager = remember { IpHistoryManager(context) }
    val scope = rememberCoroutineScope()

    var ipAddress by remember { mutableStateOf("") }
    var detectedIps by remember { mutableStateOf<DetectedIps?>(null) }
    var ipv4Info by remember { mutableStateOf<IpInfo?>(null) }
    var ipv6Info by remember { mutableStateOf<IpInfo?>(null) }
    var customIpInfo by remember { mutableStateOf<IpInfo?>(null) }
    var showMyIp by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lookupHistory by remember { mutableStateOf(historyManager.getHistory()) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var lastSyncTime by remember { mutableStateOf<Long?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    suspend fun refreshIpAddresses() {
        isLoading = true
        errorMessage = null
        try {
            val ips = repository.detectUserIps()
            detectedIps = ips
            ips.ipv4?.let { ip ->
                repository.getIpInfo(ip).onSuccess {
                    ipv4Info = it
                    historyManager.addEntry(it)
                    lookupHistory = historyManager.getHistory()
                }
            }
            ips.ipv6?.let { ip ->
                repository.getIpInfo(ip).onSuccess {
                    ipv6Info = it
                    historyManager.addEntry(it)
                    lookupHistory = historyManager.getHistory()
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message
        }
        isLoading = false
        lastSyncTime = System.currentTimeMillis()
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    // Auto refresh every 30 seconds
    LaunchedEffect(Unit) {
        while (true) {
            refreshIpAddresses()
            kotlinx.coroutines.delay(30000)
        }
    }

    // Refresh on trigger (manual sync or navigation back)
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            refreshIpAddresses()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ip_lookup_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
            // Search Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text(stringResource(R.string.enter_ip_address)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (ipAddress.isNotBlank()) {
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        customIpInfo = null
                                        showMyIp = false
                                        val result = repository.getIpInfo(ipAddress)
                                        isLoading = false
                                        result.fold(
                                            onSuccess = {
                                                customIpInfo = it
                                                historyManager.addEntry(it)
                                                lookupHistory = historyManager.getHistory()
                                                // Track usage for interstitial ad (every 3 lookups)
                                                activity?.let { act ->
                                                    AdMobManager.trackIpLookupUsage(act)
                                                    // Track for rewarded ad (every 12 lookups)
                                                    AdMobManager.trackIpLookupForReward(act,
                                                        onShowReward = {
                                                            AdMobManager.showRewardedAd(act, onRewarded = {})
                                                        },
                                                        onSkip = {}
                                                    )
                                                }
                                            },
                                            onFailure = { errorMessage = it.message }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = ipAddress.isNotBlank() && !isLoading
                        ) { Text(stringResource(R.string.lookup)) }

                        if (!showMyIp) {
                            OutlinedButton(
                                onClick = {
                                    showMyIp = true
                                    customIpInfo = null
                                    ipAddress = ""
                                    errorMessage = null
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.lookup_my_ip)) }
                        }
                    }
                }
            }

            // Loading
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // Error
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(error, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Recent Lookups Card
            if (lookupHistory.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Lookups", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            if (lookupHistory.size > 3) {
                                TextButton(onClick = { showHistoryDialog = true }) { Text("View All") }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        lookupHistory.take(3).forEach { entry ->
                            HistoryItem(entry, currentTime) {
                                entry.ipInfo.ip?.let {
                                    ipAddress = it
                                    showMyIp = false
                                    customIpInfo = entry.ipInfo
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Your IP Addresses Card
            if (showMyIp) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Your IP Addresses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            TextButton(onClick = { refreshTrigger++ }, enabled = !isLoading) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        detectedIps?.ipv4?.let { CompactInfoRow("IPv4", it) }
                        detectedIps?.ipv6?.let { CompactInfoRow("IPv6", it) }
                        lastSyncTime?.let {
                            val tz = TimeZone.getDefault()
                            val offsetHours = tz.rawOffset / 3600000
                            val sign = if (offsetHours >= 0) "+" else ""
                            val timeStr = SimpleDateFormat("d MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(it))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Last sync: $timeStr (UTC$sign$offsetHours)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // IP Details
            if (showMyIp) {
                ipv4Info?.let { IpInfoCard("IPv4 Details", it) }
                ipv6Info?.let { IpInfoCard("IPv6 Details", it) }
            } else {
                customIpInfo?.let { IpInfoCard("IP Details", it) }
            }

            // Attribution
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IP Address Lookup API and Data is provided by dklyDataHUB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://data.dkly.net"))) }
                )
            }
            }
            BannerAd(modifier = Modifier.fillMaxWidth())
        }

        // Info Dialog
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text("About") },
                text = {
                    Text(
                        text = "IP Address Lookup API and Data is provided by dklyDataHUB",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://data.dkly.net")))
                            showInfoDialog = false
                        }
                    )
                },
                confirmButton = { TextButton(onClick = { showInfoDialog = false }) { Text("Close") } }
            )
        }

        // History Dialog
        if (showHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showHistoryDialog = false },
                title = { Text("Lookup History") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        lookupHistory.forEach { entry ->
                            HistoryItem(entry, currentTime) {
                                entry.ipInfo.ip?.let {
                                    ipAddress = it
                                    showMyIp = false
                                    customIpInfo = entry.ipInfo
                                    showHistoryDialog = false
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showHistoryDialog = false }) { Text("Close") } }
            )
        }
    }
}

@Composable
fun HistoryItem(entry: IpHistoryEntry, currentTime: Long, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.ipInfo.ip ?: "Unknown", fontWeight = FontWeight.Medium)
                Text(
                    "${entry.ipInfo.location?.country?.name ?: ""} ${entry.ipInfo.location?.city ?: ""}".trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(entry.ipInfo.type ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(formatTimeAgo(entry.timestamp, currentTime), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CompactInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun IpInfoCard(title: String, ipInfo: IpInfo) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            // Basic Info
            ipInfo.ip?.let { CompactInfoRow("IP Address", it) }
            ipInfo.type?.let { CompactInfoRow("Type", it) }
            ipInfo.hostname?.let { CompactInfoRow("Hostname", it) }

            // Location
            ipInfo.location?.let { location ->
                Spacer(modifier = Modifier.height(12.dp))
                Text("Location", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                location.country?.let { country ->
                    CompactInfoRow("Country", "${country.flag?.emoji ?: ""} ${country.name ?: ""}")
                }
                location.region?.name?.let { CompactInfoRow("Region", it) }
                location.city?.let { CompactInfoRow("City", it) }
                location.latitude?.let { lat ->
                    location.longitude?.let { lng ->
                        CompactInfoRow("Coordinates", "%.4f, %.4f".format(lat, lng))
                    }
                }
            }

            // Connection
            ipInfo.connection?.let { conn ->
                Spacer(modifier = Modifier.height(12.dp))
                Text("Connection", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                conn.organization?.let { CompactInfoRow("Organization", it) }
                conn.asn?.let { CompactInfoRow("ASN", it.toString()) }
                conn.type?.let { CompactInfoRow("Type", it.uppercase()) }
            }

            // Security
            ipInfo.security?.let { security ->
                Spacer(modifier = Modifier.height(12.dp))
                Text("Security", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                security.isVpn?.let { CompactInfoRow("VPN", if (it) "Yes" else "No") }
                security.isProxy?.let { CompactInfoRow("Proxy", if (it) "Yes" else "No") }
                security.isTor?.let { CompactInfoRow("Tor", if (it) "Yes" else "No") }
                security.isThreat?.let { CompactInfoRow("Threat", if (it) "Yes" else "No") }
            }

            // Timezone
            ipInfo.timeZone?.let { tz ->
                Spacer(modifier = Modifier.height(12.dp))
                Text("Timezone", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                tz.id?.let { CompactInfoRow("Timezone", it) }
                tz.currentTime?.let { CompactInfoRow("Local Time", it) }
            }
        }
    }
}
