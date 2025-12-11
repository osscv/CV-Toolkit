package cv.toolkit.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cv.toolkit.ads.BannerAd
import kotlinx.coroutines.launch

enum class IPInputFormat(val label: String) {
    DECIMAL("Decimal (192.168.1.1)"),
    BINARY("Binary"),
    HEX("Hexadecimal"),
    INTEGER("Integer (32-bit)")
}

data class IPConversionResult(
    val decimal: String,
    val binary: String,
    val binaryDotted: String,
    val hex: String,
    val hexDotted: String,
    val integer: Long,
    val octalDotted: String,
    val ipClass: String,
    val ipType: String,
    val isValid: Boolean,
    val octets: List<Int>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IPCalculatorScreen(navController: NavController) {
    var inputValue by remember { mutableStateOf("192.168.1.1") }
    var selectedFormat by remember { mutableStateOf(IPInputFormat.DECIMAL) }
    var result by remember { mutableStateOf<IPConversionResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    fun convertIP() {
        errorMessage = null
        result = null

        try {
            val octets = when (selectedFormat) {
                IPInputFormat.DECIMAL -> parseDecimalIP(inputValue.trim())
                IPInputFormat.BINARY -> parseBinaryIP(inputValue.trim())
                IPInputFormat.HEX -> parseHexIP(inputValue.trim())
                IPInputFormat.INTEGER -> parseIntegerIP(inputValue.trim())
            }

            if (octets.any { it < 0 || it > 255 }) {
                errorMessage = "Each octet must be between 0 and 255"
                return
            }

            result = calculateConversions(octets)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Invalid input format"
        }
    }

    // Auto-convert when input changes
    LaunchedEffect(inputValue, selectedFormat) {
        if (inputValue.isNotBlank()) {
            convertIP()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IP Calculator") },
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
            // Input format selector
            Text("Input Format", style = MaterialTheme.typography.labelMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                IPInputFormat.entries.forEachIndexed { index, format ->
                    SegmentedButton(
                        selected = selectedFormat == format,
                        onClick = {
                            selectedFormat = format
                            // Clear input when changing format
                            inputValue = when (format) {
                                IPInputFormat.DECIMAL -> "192.168.1.1"
                                IPInputFormat.BINARY -> "11000000.10101000.00000001.00000001"
                                IPInputFormat.HEX -> "C0.A8.01.01"
                                IPInputFormat.INTEGER -> "3232235777"
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, IPInputFormat.entries.size)
                    ) {
                        Text(
                            when (format) {
                                IPInputFormat.DECIMAL -> "Decimal"
                                IPInputFormat.BINARY -> "Binary"
                                IPInputFormat.HEX -> "Hex"
                                IPInputFormat.INTEGER -> "Integer"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Input field
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text(selectedFormat.label) },
                placeholder = {
                    Text(
                        when (selectedFormat) {
                            IPInputFormat.DECIMAL -> "192.168.1.1"
                            IPInputFormat.BINARY -> "11000000.10101000.00000001.00000001"
                            IPInputFormat.HEX -> "C0.A8.01.01 or C0A80101"
                            IPInputFormat.INTEGER -> "3232235777"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Calculate, null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (selectedFormat == IPInputFormat.INTEGER)
                        KeyboardType.Number else KeyboardType.Ascii
                ),
                isError = errorMessage != null
            )

            // Error message
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Results
            result?.let { r ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Text("Conversion Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    // All Formats Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("IP Address Formats", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)

                                ConversionRow("Decimal (Dotted)", r.decimal, clipboard)
                                ConversionRow("Binary (Dotted)", r.binaryDotted, clipboard)
                                ConversionRow("Binary (Full)", r.binary, clipboard)
                                ConversionRow("Hexadecimal (Dotted)", r.hexDotted, clipboard)
                                ConversionRow("Hexadecimal", r.hex, clipboard)
                                ConversionRow("Integer (32-bit)", r.integer.toString(), clipboard)
                                ConversionRow("Octal (Dotted)", r.octalDotted, clipboard)
                            }
                        }
                    }

                    // Octet Breakdown Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Octet Breakdown", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    r.octets.forEachIndexed { index, octet ->
                                        OctetCard(index + 1, octet)
                                    }
                                }
                            }
                        }
                    }

                    // IP Classification Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("IP Classification", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                                InfoRow("IP Class", r.ipClass)
                                InfoRow("IP Type", r.ipType)
                            }
                        }
                    }

                    // Bit Position Reference
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Bit Position Reference", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Position:  128  64  32  16   8   4   2   1",
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Bit:         7   6   5   4   3   2   1   0",
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Common Values:", style = MaterialTheme.typography.labelMedium)
                                Text("255 = 11111111, 254 = 11111110, 252 = 11111100", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                                Text("248 = 11111000, 240 = 11110000, 224 = 11100000", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                                Text("192 = 11000000, 128 = 10000000, 0 = 00000000", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ConversionRow(
    label: String,
    value: String,
    clipboard: androidx.compose.ui.platform.Clipboard
) {
    var showCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(showCopied) {
        if (showCopied) {
            kotlinx.coroutines.delay(1500)
            showCopied = false
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(
            onClick = {
                scope.launch {
                    clipboard.setClipEntry(
                        androidx.compose.ui.platform.ClipEntry(
                            android.content.ClipData.newPlainText("ip", value)
                        )
                    )
                }
                showCopied = true
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                if (showCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                contentDescription = "Copy",
                modifier = Modifier.size(18.dp),
                tint = if (showCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OctetCard(octetNumber: Int, value: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Octet $octetNumber", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(value.toString(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                value.toString(2).padStart(8, '0'),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "0x${value.toString(16).uppercase().padStart(2, '0')}",
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

// Parsing functions
private fun parseDecimalIP(input: String): List<Int> {
    val parts = input.split(".")
    if (parts.size != 4) throw IllegalArgumentException("Invalid decimal IP format. Use: 192.168.1.1")
    return parts.map {
        it.toIntOrNull() ?: throw IllegalArgumentException("Invalid octet: $it")
    }
}

private fun parseBinaryIP(input: String): List<Int> {
    // Remove spaces and dots for flexibility
    val cleaned = input.replace(" ", "").replace(".", "")

    if (cleaned.length != 32 || !cleaned.all { it == '0' || it == '1' }) {
        // Try dotted binary format
        val parts = input.split(".")
        if (parts.size == 4) {
            return parts.map { part ->
                val cleanPart = part.replace(" ", "")
                if (cleanPart.length != 8 || !cleanPart.all { it == '0' || it == '1' }) {
                    throw IllegalArgumentException("Invalid binary octet: $part")
                }
                cleanPart.toInt(2)
            }
        }
        throw IllegalArgumentException("Invalid binary format. Use: 11000000.10101000.00000001.00000001")
    }

    return listOf(
        cleaned.substring(0, 8).toInt(2),
        cleaned.substring(8, 16).toInt(2),
        cleaned.substring(16, 24).toInt(2),
        cleaned.substring(24, 32).toInt(2)
    )
}

private fun parseHexIP(input: String): List<Int> {
    val cleaned = input.replace(" ", "").replace("0x", "").replace("0X", "")

    // Try dotted hex format (C0.A8.01.01)
    if (cleaned.contains(".")) {
        val parts = cleaned.split(".")
        if (parts.size != 4) throw IllegalArgumentException("Invalid hex format")
        return parts.map {
            it.toIntOrNull(16) ?: throw IllegalArgumentException("Invalid hex octet: $it")
        }
    }

    // Try continuous hex format (C0A80101)
    if (cleaned.length == 8 && cleaned.all { it.isDigit() || it.uppercaseChar() in 'A'..'F' }) {
        return listOf(
            cleaned.substring(0, 2).toInt(16),
            cleaned.substring(2, 4).toInt(16),
            cleaned.substring(4, 6).toInt(16),
            cleaned.substring(6, 8).toInt(16)
        )
    }

    throw IllegalArgumentException("Invalid hex format. Use: C0.A8.01.01 or C0A80101")
}

private fun parseIntegerIP(input: String): List<Int> {
    val value = input.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid integer value")

    if (value < 0 || value > 4294967295L) {
        throw IllegalArgumentException("Integer must be between 0 and 4294967295")
    }

    return listOf(
        ((value shr 24) and 0xFF).toInt(),
        ((value shr 16) and 0xFF).toInt(),
        ((value shr 8) and 0xFF).toInt(),
        (value and 0xFF).toInt()
    )
}

private fun calculateConversions(octets: List<Int>): IPConversionResult {
    val decimal = octets.joinToString(".")

    val binaryOctets = octets.map { it.toString(2).padStart(8, '0') }
    val binaryDotted = binaryOctets.joinToString(".")
    val binary = binaryOctets.joinToString("")

    val hexOctets = octets.map { it.toString(16).uppercase().padStart(2, '0') }
    val hexDotted = hexOctets.joinToString(".")
    val hex = "0x${hexOctets.joinToString("")}"

    val integer = (octets[0].toLong() shl 24) or
            (octets[1].toLong() shl 16) or
            (octets[2].toLong() shl 8) or
            octets[3].toLong()

    val octalDotted = octets.joinToString(".") { it.toString(8).padStart(3, '0') }

    // Determine IP class
    val ipClass = when {
        octets[0] in 1..126 -> "Class A (1.0.0.0 - 126.255.255.255)"
        octets[0] in 128..191 -> "Class B (128.0.0.0 - 191.255.255.255)"
        octets[0] in 192..223 -> "Class C (192.0.0.0 - 223.255.255.255)"
        octets[0] in 224..239 -> "Class D - Multicast (224.0.0.0 - 239.255.255.255)"
        octets[0] in 240..255 -> "Class E - Reserved (240.0.0.0 - 255.255.255.255)"
        octets[0] == 0 -> "Reserved (0.0.0.0/8)"
        octets[0] == 127 -> "Loopback (127.0.0.0/8)"
        else -> "Unknown"
    }

    // Determine IP type
    val ipType = when {
        octets[0] == 10 -> "Private (RFC 1918) - 10.0.0.0/8"
        octets[0] == 172 && octets[1] in 16..31 -> "Private (RFC 1918) - 172.16.0.0/12"
        octets[0] == 192 && octets[1] == 168 -> "Private (RFC 1918) - 192.168.0.0/16"
        octets[0] == 127 -> "Loopback - 127.0.0.0/8"
        octets[0] == 169 && octets[1] == 254 -> "Link-Local (APIPA) - 169.254.0.0/16"
        octets[0] == 100 && octets[1] in 64..127 -> "Carrier-Grade NAT (RFC 6598) - 100.64.0.0/10"
        octets[0] == 192 && octets[1] == 0 && octets[2] == 0 -> "IETF Protocol Assignments - 192.0.0.0/24"
        octets[0] == 192 && octets[1] == 0 && octets[2] == 2 -> "Documentation (TEST-NET-1) - 192.0.2.0/24"
        octets[0] == 198 && octets[1] == 51 && octets[2] == 100 -> "Documentation (TEST-NET-2) - 198.51.100.0/24"
        octets[0] == 203 && octets[1] == 0 && octets[2] == 113 -> "Documentation (TEST-NET-3) - 203.0.113.0/24"
        octets[0] in 224..239 -> "Multicast - 224.0.0.0/4"
        octets[0] >= 240 -> "Reserved for Future Use - 240.0.0.0/4"
        octets.all { it == 0 } -> "This Network - 0.0.0.0"
        octets.all { it == 255 } -> "Limited Broadcast - 255.255.255.255"
        else -> "Public (Globally Routable)"
    }

    return IPConversionResult(
        decimal = decimal,
        binary = binary,
        binaryDotted = binaryDotted,
        hex = hex,
        hexDotted = hexDotted,
        integer = integer,
        octalDotted = octalDotted,
        ipClass = ipClass,
        ipType = ipType,
        isValid = true,
        octets = octets
    )
}
