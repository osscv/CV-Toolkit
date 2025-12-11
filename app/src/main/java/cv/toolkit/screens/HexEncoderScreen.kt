package cv.toolkit.screens

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cv.toolkit.ads.BannerAd
import kotlinx.coroutines.launch

enum class HexMode {
    ENCODE, DECODE
}

enum class HexFormat(val label: String, val description: String) {
    PLAIN("Plain", "48656C6C6F"),
    SPACED("Spaced", "48 65 6C 6C 6F"),
    PREFIXED("0x Prefix", "0x48 0x65 0x6C"),
    COLON("Colon", "48:65:6C:6C:6F"),
    BACKSLASH("\\x Escape", "\\x48\\x65\\x6C")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HexEncoderScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(HexMode.ENCODE) }
    var hexFormat by remember { mutableStateOf(HexFormat.SPACED) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var useUppercase by remember { mutableStateOf(true) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    fun textToHex(text: String): String {
        val hexBytes = text.toByteArray(Charsets.UTF_8).map { byte ->
            val hex = "%02x".format(byte)
            if (useUppercase) hex.uppercase() else hex
        }

        return when (hexFormat) {
            HexFormat.PLAIN -> hexBytes.joinToString("")
            HexFormat.SPACED -> hexBytes.joinToString(" ")
            HexFormat.PREFIXED -> hexBytes.joinToString(" ") { "0x$it" }
            HexFormat.COLON -> hexBytes.joinToString(":")
            HexFormat.BACKSLASH -> hexBytes.joinToString("") { "\\x$it" }
        }
    }

    fun hexToText(hex: String): String {
        val cleaned = hex
            .replace("0x", "")
            .replace("0X", "")
            .replace("\\x", "")
            .replace("\\X", "")
            .replace(":", "")
            .replace(" ", "")
            .replace("\n", "")
            .replace("\t", "")

        if (cleaned.length % 2 != 0) {
            throw IllegalArgumentException("Invalid hex string length")
        }

        val bytes = cleaned.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return String(bytes, Charsets.UTF_8)
    }

    fun processText() {
        errorMessage = null
        if (inputText.isBlank()) {
            outputText = ""
            return
        }

        try {
            outputText = when (mode) {
                HexMode.ENCODE -> textToHex(inputText)
                HexMode.DECODE -> hexToText(inputText)
            }
        } catch (e: NumberFormatException) {
            errorMessage = "Invalid hexadecimal format"
            outputText = ""
        } catch (e: Exception) {
            errorMessage = "Conversion error: ${e.message}"
            outputText = ""
        }
    }

    LaunchedEffect(inputText, mode, hexFormat, useUppercase) {
        processText()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hex Encoder/Decoder") },
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
            // Mode selector
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = mode == HexMode.ENCODE,
                    onClick = {
                        mode = HexMode.ENCODE
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    icon = { Icon(Icons.Filled.Lock, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("Encode")
                }
                SegmentedButton(
                    selected = mode == HexMode.DECODE,
                    onClick = {
                        mode = HexMode.DECODE
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    icon = { Icon(Icons.Filled.LockOpen, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("Decode")
                }
            }

            // Format selector (only for encode)
            if (mode == HexMode.ENCODE) {
                Text("Output Format", style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    HexFormat.entries.take(3).forEachIndexed { index, format ->
                        SegmentedButton(
                            selected = hexFormat == format,
                            onClick = { hexFormat = format },
                            shape = SegmentedButtonDefaults.itemShape(index, 3)
                        ) {
                            Text(format.label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    HexFormat.entries.drop(3).forEachIndexed { index, format ->
                        SegmentedButton(
                            selected = hexFormat == format,
                            onClick = { hexFormat = format },
                            shape = SegmentedButtonDefaults.itemShape(index, 2)
                        ) {
                            Text(format.label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Options
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useUppercase, onCheckedChange = { useUppercase = it })
                    Text("Uppercase output", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = {
                    Text(if (mode == HexMode.ENCODE) "Text to Encode" else "Hex to Decode")
                },
                placeholder = {
                    Text(
                        if (mode == HexMode.ENCODE) "Enter text..."
                        else "Enter hex (e.g., 48656C6C6F or 48 65 6C 6C 6F)"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 3,
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { inputText = "" }) {
                            Icon(Icons.Filled.Clear, "Clear")
                        }
                    }
                }
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            clipboard.getClipEntry()?.clipData?.getItemAt(0)?.text?.let {
                                inputText = it.toString()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ContentPaste, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Paste")
                }
                Button(
                    onClick = {
                        val temp = outputText
                        mode = if (mode == HexMode.ENCODE) HexMode.DECODE else HexMode.ENCODE
                        inputText = temp
                    },
                    modifier = Modifier.weight(1f),
                    enabled = outputText.isNotEmpty()
                ) {
                    Icon(Icons.Filled.SwapVert, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Swap")
                }
            }

            // Error message
            errorMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Output field
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (mode == HexMode.ENCODE) "Hex Output" else "Decoded Text",
                            style = MaterialTheme.typography.labelMedium
                        )
                        IconButton(
                            onClick = {
                                if (outputText.isNotEmpty()) {
                                    scope.launch {
                                        clipboard.setClipEntry(
                                            androidx.compose.ui.platform.ClipEntry(
                                                android.content.ClipData.newPlainText("hex", outputText)
                                            )
                                        )
                                    }
                                }
                            },
                            enabled = outputText.isNotEmpty()
                        ) {
                            Icon(Icons.Filled.ContentCopy, "Copy", modifier = Modifier.size(20.dp))
                        }
                    }
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        outputText.ifEmpty { "Output will appear here..." },
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (outputText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Stats
            if (inputText.isNotEmpty() || outputText.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("Input: ${inputText.length} chars", style = MaterialTheme.typography.labelSmall)
                    Text("Output: ${outputText.length} chars", style = MaterialTheme.typography.labelSmall)
                    if (mode == HexMode.ENCODE) {
                        Text("Bytes: ${inputText.toByteArray(Charsets.UTF_8).size}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Reference card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("ASCII to Hex Reference:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("A = 41", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("a = 61", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("0 = 30", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("9 = 39", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Space = 20", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("Hello = 48656C6C6F", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}
