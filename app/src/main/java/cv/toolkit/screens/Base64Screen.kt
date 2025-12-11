package cv.toolkit.screens

import android.util.Base64
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

enum class Base64Mode {
    ENCODE, DECODE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Base64Screen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(Base64Mode.ENCODE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var urlSafe by remember { mutableStateOf(false) }
    var noPadding by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    fun processText() {
        errorMessage = null
        if (inputText.isBlank()) {
            outputText = ""
            return
        }

        try {
            val flags = (if (urlSafe) Base64.URL_SAFE else Base64.DEFAULT) or
                    (if (noPadding) Base64.NO_PADDING else 0) or
                    Base64.NO_WRAP

            outputText = when (mode) {
                Base64Mode.ENCODE -> {
                    Base64.encodeToString(inputText.toByteArray(Charsets.UTF_8), flags)
                }
                Base64Mode.DECODE -> {
                    String(Base64.decode(inputText.trim(), flags), Charsets.UTF_8)
                }
            }
        } catch (e: Exception) {
            errorMessage = when (mode) {
                Base64Mode.ENCODE -> "Failed to encode: ${e.message}"
                Base64Mode.DECODE -> "Invalid Base64 string: ${e.message}"
            }
            outputText = ""
        }
    }

    // Auto-process when input changes
    LaunchedEffect(inputText, mode, urlSafe, noPadding) {
        processText()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Base64 Encoder/Decoder") },
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
                    selected = mode == Base64Mode.ENCODE,
                    onClick = {
                        mode = Base64Mode.ENCODE
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    icon = { Icon(Icons.Filled.Lock, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("Encode")
                }
                SegmentedButton(
                    selected = mode == Base64Mode.DECODE,
                    onClick = {
                        mode = Base64Mode.DECODE
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    icon = { Icon(Icons.Filled.LockOpen, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("Decode")
                }
            }

            // Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = urlSafe, onCheckedChange = { urlSafe = it })
                    Text("URL Safe", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = noPadding, onCheckedChange = { noPadding = it })
                    Text("No Padding", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text(if (mode == Base64Mode.ENCODE) "Text to Encode" else "Base64 to Decode") },
                placeholder = {
                    Text(
                        if (mode == Base64Mode.ENCODE) "Enter text to encode..."
                        else "Enter Base64 string to decode..."
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 5,
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
                        // Swap input and output
                        val temp = outputText
                        mode = if (mode == Base64Mode.ENCODE) Base64Mode.DECODE else Base64Mode.ENCODE
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
                            if (mode == Base64Mode.ENCODE) "Encoded Output" else "Decoded Output",
                            style = MaterialTheme.typography.labelMedium
                        )
                        IconButton(
                            onClick = {
                                if (outputText.isNotEmpty()) {
                                    scope.launch {
                                        clipboard.setClipEntry(
                                            androidx.compose.ui.platform.ClipEntry(
                                                android.content.ClipData.newPlainText("base64", outputText)
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
                    if (mode == Base64Mode.ENCODE && outputText.isNotEmpty()) {
                        Text("Ratio: ${String.format("%.1f", outputText.length.toFloat() / inputText.length)}x", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}
