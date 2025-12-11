package cv.toolkit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cv.toolkit.ads.BannerAd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class CertificateInfo(
    val subject: String,
    val issuer: String,
    val serialNumber: String,
    val validFrom: Date,
    val validUntil: Date,
    val daysUntilExpiry: Long,
    val signatureAlgorithm: String,
    val publicKeyAlgorithm: String,
    val publicKeySize: Int,
    val version: Int,
    val sha256Fingerprint: String,
    val sha1Fingerprint: String,
    val subjectAltNames: List<String>,
    val isExpired: Boolean,
    val isNotYetValid: Boolean,
    val isSelfSigned: Boolean
)

data class SSLCheckResult(
    val host: String,
    val port: Int,
    val protocol: String,
    val cipherSuite: String,
    val certificates: List<CertificateInfo>,
    val isChainValid: Boolean,
    val chainValidationError: String?,
    val connectionTime: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SSLCheckerScreen(navController: NavController) {
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("443") }
    var isChecking by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<SSLCheckResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val popularSites = listOf("google.com", "github.com", "amazon.com", "cloudflare.com", "microsoft.com")

    fun checkSSL() {
        if (host.isBlank()) return
        isChecking = true
        errorMessage = null
        result = null

        scope.launch {
            try {
                val checkResult = withContext(Dispatchers.IO) {
                    performSSLCheck(host.trim(), port.toIntOrNull() ?: 443)
                }
                result = checkResult
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to check SSL certificate"
            }
            isChecking = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SSL/TLS Checker") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            // Host input
            OutlinedTextField(
                value = host,
                onValueChange = { host = it.replace("https://", "").replace("http://", "").split("/").first() },
                label = { Text("Domain / Host") },
                placeholder = { Text("example.com") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isChecking,
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Lock, null) }
            )

            // Port input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = port,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) port = it },
                    label = { Text("Port") },
                    modifier = Modifier.width(100.dp),
                    enabled = !isChecking,
                    singleLine = true
                )
                FilterChip(
                    selected = port == "443",
                    onClick = { port = "443" },
                    label = { Text("HTTPS (443)") },
                    enabled = !isChecking
                )
                FilterChip(
                    selected = port == "8443",
                    onClick = { port = "8443" },
                    label = { Text("8443") },
                    enabled = !isChecking
                )
            }

            // Popular sites
            Text("Popular Sites", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(popularSites) { site ->
                    FilterChip(
                        selected = host == site,
                        onClick = { host = site },
                        label = { Text(site) },
                        enabled = !isChecking
                    )
                }
            }

            // Check button
            Button(
                onClick = { checkSSL() },
                modifier = Modifier.fillMaxWidth(),
                enabled = host.isNotBlank() && !isChecking
            ) {
                if (isChecking) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Checking...")
                } else {
                    Icon(Icons.Filled.Security, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Check Certificate")
                }
            }

            // Error message
            errorMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            // Results
            result?.let { r ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Connection Info
                    item {
                        ConnectionInfoCard(r)
                    }

                    // Chain Validation Status
                    item {
                        ChainValidationCard(r)
                    }

                    // Certificate Chain
                    item {
                        Text(
                            "Certificate Chain (${r.certificates.size} certificates)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(r.certificates.withIndex().toList()) { (index, cert) ->
                        CertificateCard(cert, index, r.certificates.size)
                    }
                }
            }

            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ConnectionInfoCard(result: SSLCheckResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Connection Info", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            InfoRow("Host", "${result.host}:${result.port}")
            InfoRow("Protocol", result.protocol)
            InfoRow("Cipher Suite", result.cipherSuite)
            InfoRow("Connection Time", "${result.connectionTime}ms")
        }
    }
}

@Composable
private fun ChainValidationCard(result: SSLCheckResult) {
    val isValid = result.isChainValid
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isValid)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isValid) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                null,
                tint = if (isValid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    if (isValid) "Certificate Chain Valid" else "Certificate Chain Invalid",
                    fontWeight = FontWeight.Bold,
                    color = if (isValid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
                result.chainValidationError?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CertificateCard(cert: CertificateInfo, index: Int, total: Int) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

    val statusColor = when {
        cert.isExpired -> MaterialTheme.colorScheme.error
        cert.isNotYetValid -> MaterialTheme.colorScheme.error
        cert.daysUntilExpiry <= 30 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    val statusText = when {
        cert.isExpired -> "EXPIRED"
        cert.isNotYetValid -> "NOT YET VALID"
        cert.daysUntilExpiry <= 30 -> "EXPIRING SOON"
        else -> "VALID"
    }

    val certType = when (index) {
        0 -> "Server Certificate"
        total - 1 -> "Root CA"
        else -> "Intermediate CA"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${index + 1}. $certType",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (cert.isSelfSigned) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(0xFFFF9800).copy(alpha = 0.2f)
                ) {
                    Text(
                        "Self-Signed",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFFFF9800),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            HorizontalDivider()

            // Subject & Issuer
            Text("Subject", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(cert.subject, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)

            Text("Issuer", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(cert.issuer, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)

            HorizontalDivider()

            // Validity
            Text("Validity", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            InfoRow("Valid From", dateFormat.format(cert.validFrom))
            InfoRow("Valid Until", dateFormat.format(cert.validUntil))
            InfoRow("Days Until Expiry", "${cert.daysUntilExpiry} days", valueColor = statusColor)

            HorizontalDivider()

            // Technical Details
            Text("Technical Details", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            InfoRow("Version", "V${cert.version}")
            InfoRow("Serial Number", cert.serialNumber)
            InfoRow("Signature Algorithm", cert.signatureAlgorithm)
            InfoRow("Public Key", "${cert.publicKeyAlgorithm} ${cert.publicKeySize} bits")

            // Subject Alternative Names
            if (cert.subjectAltNames.isNotEmpty()) {
                HorizontalDivider()
                Text("Subject Alternative Names", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                cert.subjectAltNames.take(10).forEach { san ->
                    Text("  - $san", style = MaterialTheme.typography.bodySmall)
                }
                if (cert.subjectAltNames.size > 10) {
                    Text("  ... and ${cert.subjectAltNames.size - 10} more", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider()

            // Fingerprints
            Text("Fingerprints", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text("SHA-256:", style = MaterialTheme.typography.labelSmall)
            Text(cert.sha256Fingerprint, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(4.dp))
            Text("SHA-1:", style = MaterialTheme.typography.labelSmall)
            Text(cert.sha1Fingerprint, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall, color = valueColor)
    }
}

private fun performSSLCheck(host: String, port: Int): SSLCheckResult {
    val startTime = System.currentTimeMillis()

    // Use SSLSocket to get session info
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, null, null)
    val socketFactory = sslContext.socketFactory
    val socket = socketFactory.createSocket(host, port) as javax.net.ssl.SSLSocket

    try {
        socket.soTimeout = 10000
        socket.startHandshake()

        val connectionTime = System.currentTimeMillis() - startTime
        val session = socket.session
        val certificates = session.peerCertificates
            .filterIsInstance<X509Certificate>()
            .map { parseCertificate(it) }

        // Check chain validity
        var isChainValid = true
        var chainError: String? = null

        try {
            // Verify the certificate chain
            val certs = session.peerCertificates.filterIsInstance<X509Certificate>()
            for (i in 0 until certs.size - 1) {
                certs[i].verify(certs[i + 1].publicKey)
            }
            // Check if any certificate is expired
            certs.forEach { it.checkValidity() }
        } catch (e: Exception) {
            isChainValid = false
            chainError = e.message
        }

        return SSLCheckResult(
            host = host,
            port = port,
            protocol = session.protocol,
            cipherSuite = session.cipherSuite,
            certificates = certificates,
            isChainValid = isChainValid,
            chainValidationError = chainError,
            connectionTime = connectionTime
        )
    } catch (e: SSLPeerUnverifiedException) {
        // Try to get certificate info even if verification fails
        return tryGetCertificateWithoutVerification(host, port, startTime, e.message)
    } catch (e: Exception) {
        // Try to get certificate info even if connection fails
        return tryGetCertificateWithoutVerification(host, port, startTime, e.message)
    } finally {
        socket.close()
    }
}

private fun tryGetCertificateWithoutVerification(host: String, port: Int, startTime: Long, error: String?): SSLCheckResult {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())

    val socketFactory = sslContext.socketFactory
    val socket = socketFactory.createSocket(host, port) as javax.net.ssl.SSLSocket

    try {
        socket.soTimeout = 10000
        socket.startHandshake()

        val connectionTime = System.currentTimeMillis() - startTime
        val session = socket.session
        val certificates = session.peerCertificates
            .filterIsInstance<X509Certificate>()
            .map { parseCertificate(it) }

        return SSLCheckResult(
            host = host,
            port = port,
            protocol = session.protocol,
            cipherSuite = session.cipherSuite,
            certificates = certificates,
            isChainValid = false,
            chainValidationError = error ?: "Certificate verification failed",
            connectionTime = connectionTime
        )
    } finally {
        socket.close()
    }
}

private fun parseCertificate(cert: X509Certificate): CertificateInfo {
    val now = Date()
    val daysUntilExpiry = ((cert.notAfter.time - now.time) / (1000 * 60 * 60 * 24))

    val subjectAltNames = try {
        cert.subjectAlternativeNames?.mapNotNull { san ->
            when (san[0] as Int) {
                2 -> san[1] as? String // DNS Name
                7 -> san[1] as? String // IP Address
                else -> null
            }
        } ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }

    val publicKeySize = try {
        when (val key = cert.publicKey) {
            is java.security.interfaces.RSAPublicKey -> key.modulus.bitLength()
            is java.security.interfaces.ECPublicKey -> key.params.order.bitLength()
            else -> 0
        }
    } catch (_: Exception) { 0 }

    return CertificateInfo(
        subject = cert.subjectX500Principal.name,
        issuer = cert.issuerX500Principal.name,
        serialNumber = cert.serialNumber.toString(16).uppercase(),
        validFrom = cert.notBefore,
        validUntil = cert.notAfter,
        daysUntilExpiry = daysUntilExpiry,
        signatureAlgorithm = cert.sigAlgName,
        publicKeyAlgorithm = cert.publicKey.algorithm,
        publicKeySize = publicKeySize,
        version = cert.version,
        sha256Fingerprint = getFingerprint(cert, "SHA-256"),
        sha1Fingerprint = getFingerprint(cert, "SHA-1"),
        subjectAltNames = subjectAltNames,
        isExpired = now.after(cert.notAfter),
        isNotYetValid = now.before(cert.notBefore),
        isSelfSigned = cert.subjectX500Principal == cert.issuerX500Principal
    )
}

private fun getFingerprint(cert: X509Certificate, algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    val digest = md.digest(cert.encoded)
    return digest.joinToString(":") { "%02X".format(it) }
}
