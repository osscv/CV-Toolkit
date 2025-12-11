package cv.toolkit.screens

import androidx.compose.foundation.layout.*
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

enum class CipherMode {
    ENCODE, DECODE
}

enum class CipherType(val label: String, val description: String) {
    ROT13("ROT13", "Shift by 13 positions"),
    CAESAR("Caesar", "Custom shift amount"),
    ROT47("ROT47", "ASCII printable rotation")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaesarCipherScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(CipherMode.ENCODE) }
    var cipherType by remember { mutableStateOf(CipherType.ROT13) }
    var shiftAmount by remember { mutableStateOf("3") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    fun rot13(text: String): String {
        return text.map { char ->
            when {
                char in 'a'..'z' -> 'a' + (char - 'a' + 13) % 26
                char in 'A'..'Z' -> 'A' + (char - 'A' + 13) % 26
                else -> char
            }
        }.joinToString("")
    }

    fun rot47(text: String): String {
        return text.map { char ->
            if (char.code in 33..126) {
                ((char.code - 33 + 47) % 94 + 33).toChar()
            } else {
                char
            }
        }.joinToString("")
    }

    fun caesarCipher(text: String, shift: Int, encode: Boolean): String {
        val actualShift = if (encode) shift else -shift
        return text.map { char ->
            when {
                char in 'a'..'z' -> {
                    val shifted = (char - 'a' + actualShift) % 26
                    'a' + if (shifted < 0) shifted + 26 else shifted
                }
                char in 'A'..'Z' -> {
                    val shifted = (char - 'A' + actualShift) % 26
                    'A' + if (shifted < 0) shifted + 26 else shifted
                }
                else -> char
            }
        }.joinToString("")
    }

    fun processText() {
        errorMessage = null
        if (inputText.isBlank()) {
            outputText = ""
            return
        }

        try {
            outputText = when (cipherType) {
                CipherType.ROT13 -> rot13(inputText)
                CipherType.ROT47 -> rot47(inputText)
                CipherType.CAESAR -> {
                    val shift = shiftAmount.toIntOrNull() ?: 3
                    caesarCipher(inputText, shift, mode == CipherMode.ENCODE)
                }
            }
        } catch (e: Exception) {
            errorMessage = "Cipher error: ${e.message}"
            outputText = ""
        }
    }

    LaunchedEffect(inputText, mode, cipherType, shiftAmount) {
        processText()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caesar/ROT Cipher") },
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
            // Mode selector (only for Caesar)
            if (cipherType == CipherType.CAESAR) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = mode == CipherMode.ENCODE,
                        onClick = {
                            mode = CipherMode.ENCODE
                            inputText = ""
                            outputText = ""
                        },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                        icon = { Icon(Icons.Filled.Lock, null, modifier = Modifier.size(18.dp)) }
                    ) {
                        Text("Encode")
                    }
                    SegmentedButton(
                        selected = mode == CipherMode.DECODE,
                        onClick = {
                            mode = CipherMode.DECODE
                            inputText = ""
                            outputText = ""
                        },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                        icon = { Icon(Icons.Filled.LockOpen, null, modifier = Modifier.size(18.dp)) }
                    ) {
                        Text("Decode")
                    }
                }
            }

            // Cipher type selector
            Text("Cipher Type", style = MaterialTheme.typography.labelMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CipherType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = cipherType == type,
                        onClick = {
                            cipherType = type
                            inputText = ""
                            outputText = ""
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, CipherType.entries.size)
                    ) {
                        Text(type.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Shift amount for Caesar
            if (cipherType == CipherType.CAESAR) {
                OutlinedTextField(
                    value = shiftAmount,
                    onValueChange = {
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            shiftAmount = it
                        }
                    },
                    label = { Text("Shift Amount (1-25)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        cipherType.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Text to ${if (mode == CipherMode.ENCODE || cipherType != CipherType.CAESAR) "Encode" else "Decode"}") },
                placeholder = { Text("Enter text...") },
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
                        if (cipherType == CipherType.CAESAR) {
                            mode = if (mode == CipherMode.ENCODE) CipherMode.DECODE else CipherMode.ENCODE
                        }
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
                            "Output",
                            style = MaterialTheme.typography.labelMedium
                        )
                        IconButton(
                            onClick = {
                                if (outputText.isNotEmpty()) {
                                    scope.launch {
                                        clipboard.setClipEntry(
                                            androidx.compose.ui.platform.ClipEntry(
                                                android.content.ClipData.newPlainText("cipher", outputText)
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

            // Reference card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Examples:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("ROT13: Hello → Uryyb", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    Text("Caesar(3): ABC → DEF", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    Text("ROT47: Hello! → w6==@P", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                }
            }

            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}
