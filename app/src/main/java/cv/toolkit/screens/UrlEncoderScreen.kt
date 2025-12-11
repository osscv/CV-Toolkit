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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cv.toolkit.ads.BannerAd
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

enum class UrlEncodeMode {
    ENCODE, DECODE
}

enum class UrlEncodeType(val label: String, val description: String) {
    FULL("Full URL", "Encode entire string"),
    COMPONENT("Component", "Encode URL component (path/query)"),
    QUERY_PARAM("Query Param", "Encode query parameter value")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlEncoderScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(UrlEncodeMode.ENCODE) }
    var encodeType by remember { mutableStateOf(UrlEncodeType.FULL) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var encodeSpaceAsPlus by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    fun processText() {
        errorMessage = null
        if (inputText.isBlank()) {
            outputText = ""
            return
        }

        try {
            outputText = when (mode) {
                UrlEncodeMode.ENCODE -> {
                    var encoded = URLEncoder.encode(inputText, "UTF-8")
                    if (!encodeSpaceAsPlus) {
                        encoded = encoded.replace("+", "%20")
                    }
                    // For component encoding, don't encode certain characters
                    if (encodeType == UrlEncodeType.COMPONENT) {
                        encoded = encoded
                            .replace("%2F", "/")
                            .replace("%3A", ":")
                            .replace("%40", "@")
                    }
                    encoded
                }
                UrlEncodeMode.DECODE -> {
                    URLDecoder.decode(inputText.trim(), "UTF-8")
                }
            }
        } catch (e: Exception) {
            errorMessage = when (mode) {
                UrlEncodeMode.ENCODE -> "Failed to encode: ${e.message}"
                UrlEncodeMode.DECODE -> "Invalid URL encoded string: ${e.message}"
            }
            outputText = ""
        }
    }

    // Auto-process when input changes
    LaunchedEffect(inputText, mode, encodeType, encodeSpaceAsPlus) {
        processText()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("URL Encoder/Decoder") },
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
                    selected = mode == UrlEncodeMode.ENCODE,
                    onClick = {
                        mode = UrlEncodeMode.ENCODE
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    icon = { Icon(Icons.Filled.Lock, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("Encode")
                }
                SegmentedButton(
                    selected = mode == UrlEncodeMode.DECODE,
                    onClick = {
                        mode = UrlEncodeMode.DECODE
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    icon = { Icon(Icons.Filled.LockOpen, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("Decode")
                }
            }

            // Encode type selector (only for encode mode)
            if (mode == UrlEncodeMode.ENCODE) {
                Text("Encoding Type", style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    UrlEncodeType.entries.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = encodeType == type,
                            onClick = { encodeType = type },
                            shape = SegmentedButtonDefaults.itemShape(index, UrlEncodeType.entries.size)
                        ) {
                            Text(type.label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Space encoding option
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = encodeSpaceAsPlus, onCheckedChange = { encodeSpaceAsPlus = it })
                    Text("Encode space as + (instead of %20)", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text(if (mode == UrlEncodeMode.ENCODE) "Text to Encode" else "URL to Decode") },
                placeholder = {
                    Text(
                        if (mode == UrlEncodeMode.ENCODE) "Enter text or URL to encode..."
                        else "Enter URL encoded string to decode..."
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 4,
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
                        mode = if (mode == UrlEncodeMode.ENCODE) UrlEncodeMode.DECODE else UrlEncodeMode.ENCODE
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
                            if (mode == UrlEncodeMode.ENCODE) "Encoded Output" else "Decoded Output",
                            style = MaterialTheme.typography.labelMedium
                        )
                        IconButton(
                            onClick = {
                                if (outputText.isNotEmpty()) {
                                    scope.launch {
                                        clipboard.setClipEntry(
                                            androidx.compose.ui.platform.ClipEntry(
                                                android.content.ClipData.newPlainText("url", outputText)
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

            // Common URL encoding reference
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Common Encodings", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column {
                            Text("Space → %20", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("& → %26", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("= → %3D", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("? → %3F", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("/ → %2F", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("# → %23", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("+ → %2B", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("@ → %40", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text(": → %3A", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}
