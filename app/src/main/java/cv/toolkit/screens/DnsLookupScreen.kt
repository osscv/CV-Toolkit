package cv.toolkit.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cv.toolkit.R
import cv.toolkit.ads.AdMobManager
import cv.toolkit.ads.BannerAd
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.InetAddress
import java.net.URL

data class DnsRecord(val type: String, val value: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsLookupScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    var domain by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var records by remember { mutableStateOf(listOf<DnsRecord>()) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun queryDns(host: String, type: String): List<String> {
        return try {
            val url = URL("https://dns.google/resolve?name=$host&type=$type")
            val response = url.readText()
            val json = JSONObject(response)
            val answers = json.optJSONArray("Answer") ?: return emptyList()
            (0 until answers.length()).mapNotNull { answers.getJSONObject(it).optString("data") }
        } catch (_: Exception) { emptyList() }
    }

    fun lookup() {
        if (domain.isBlank()) return
        isLoading = true
        error = null
        records = emptyList()

        scope.launch(Dispatchers.IO) {
            try {
                val results = mutableListOf<DnsRecord>()
                val host = domain.trim()

                // Query different record types using Google DNS-over-HTTPS
                val types = listOf("A", "AAAA", "MX", "TXT", "NS", "CNAME", "SOA")
                types.forEach { type ->
                    queryDns(host, type).forEach { value ->
                        results.add(DnsRecord(type, value))
                    }
                }

                records = results
                if (results.isEmpty()) error = "No records found"
                // Track usage for interstitial ad (every 5 lookups)
                activity?.let { AdMobManager.trackDnsUsage(it) }
            } catch (e: Exception) {
                error = e.message ?: "Lookup failed"
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dns_lookup_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = domain,
                onValueChange = { domain = it },
                label = { Text("Domain") },
                placeholder = { Text("example.com") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Language, null) }
            )

            Button(
                onClick = { lookup() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && domain.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Search, null)
                }
                Spacer(Modifier.width(8.dp))
                Text("Lookup")
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (records.isNotEmpty()) {
                Text("Records: ${records.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(records) { record ->
                    DnsRecordCard(record)
                }
            }
            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DnsRecordCard(record: DnsRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(record.type, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(record.value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
