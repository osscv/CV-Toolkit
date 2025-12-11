package cv.toolkit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
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

enum class AsciiMode {
    TEXT_TO_ASCII, ASCII_TO_TEXT
}

enum class AsciiFormat(val label: String) {
    DECIMAL("Decimal"),
    BINARY("Binary"),
    OCTAL("Octal"),
    HEX("Hex")
}

data class CharacterInfo(
    val char: Char,
    val decimal: Int,
    val binary: String,
    val octal: String,
    val hex: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsciiConverterScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(AsciiMode.TEXT_TO_ASCII) }
    var asciiFormat by remember { mutableStateOf(AsciiFormat.DECIMAL) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showTable by remember { mutableStateOf(false) }
    var characterInfoList by remember { mutableStateOf<List<CharacterInfo>>(emptyList()) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    fun textToAscii(text: String): String {
        characterInfoList = text.map { char ->
            CharacterInfo(
                char = char,
                decimal = char.code,
                binary = char.code.toString(2).padStart(8, '0'),
                octal = char.code.toString(8),
                hex = char.code.toString(16).uppercase()
            )
        }

        return when (asciiFormat) {
            AsciiFormat.DECIMAL -> text.map { it.code.toString() }.joinToString(" ")
            AsciiFormat.BINARY -> text.map { it.code.toString(2).padStart(8, '0') }.joinToString(" ")
            AsciiFormat.OCTAL -> text.map { it.code.toString(8).padStart(3, '0') }.joinToString(" ")
            AsciiFormat.HEX -> text.map { it.code.toString(16).uppercase().padStart(2, '0') }.joinToString(" ")
        }
    }

    fun asciiToText(ascii: String): String {
        val parts = ascii.trim().split(Regex("\\s+"))
        return parts.map { part ->
            val code = when (asciiFormat) {
                AsciiFormat.DECIMAL -> part.toInt()
                AsciiFormat.BINARY -> part.toInt(2)
                AsciiFormat.OCTAL -> part.toInt(8)
                AsciiFormat.HEX -> part.toInt(16)
            }
            code.toChar()
        }.joinToString("")
    }

    fun processText() {
        errorMessage = null
        if (inputText.isBlank()) {
            outputText = ""
            characterInfoList = emptyList()
            return
        }

        try {
            outputText = when (mode) {
                AsciiMode.TEXT_TO_ASCII -> textToAscii(inputText)
                AsciiMode.ASCII_TO_TEXT -> asciiToText(inputText)
            }
        } catch (e: NumberFormatException) {
            errorMessage = "Invalid ${asciiFormat.label.lowercase()} format"
            outputText = ""
        } catch (e: Exception) {
            errorMessage = "Conversion error: ${e.message}"
            outputText = ""
        }
    }

    LaunchedEffect(inputText, mode, asciiFormat) {
        processText()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ASCII Converter") },
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
                    selected = mode == AsciiMode.TEXT_TO_ASCII,
                    onClick = {
                        mode = AsciiMode.TEXT_TO_ASCII
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    icon = { Icon(Icons.Filled.Lock, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("Text → ASCII")
                }
                SegmentedButton(
                    selected = mode == AsciiMode.ASCII_TO_TEXT,
                    onClick = {
                        mode = AsciiMode.ASCII_TO_TEXT
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    icon = { Icon(Icons.Filled.LockOpen, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("ASCII → Text")
                }
            }

            // Format selector
            Text("Number Format", style = MaterialTheme.typography.labelMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AsciiFormat.entries.forEachIndexed { index, format ->
                    SegmentedButton(
                        selected = asciiFormat == format,
                        onClick = {
                            asciiFormat = format
                            if (inputText.isNotEmpty()) processText()
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, AsciiFormat.entries.size)
                    ) {
                        Text(format.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = {
                    Text(
                        if (mode == AsciiMode.TEXT_TO_ASCII) "Text to Convert"
                        else "${asciiFormat.label} Values (space-separated)"
                    )
                },
                placeholder = {
                    Text(
                        if (mode == AsciiMode.TEXT_TO_ASCII) "Enter text..."
                        else when (asciiFormat) {
                            AsciiFormat.DECIMAL -> "72 101 108 108 111"
                            AsciiFormat.BINARY -> "01001000 01100101 01101100"
                            AsciiFormat.OCTAL -> "110 145 154 154 157"
                            AsciiFormat.HEX -> "48 65 6C 6C 6F"
                        }
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
                        mode = if (mode == AsciiMode.TEXT_TO_ASCII) AsciiMode.ASCII_TO_TEXT else AsciiMode.TEXT_TO_ASCII
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
                            if (mode == AsciiMode.TEXT_TO_ASCII) "${asciiFormat.label} Output" else "Decoded Text",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row {
                            if (mode == AsciiMode.TEXT_TO_ASCII && characterInfoList.isNotEmpty()) {
                                IconButton(
                                    onClick = { showTable = !showTable },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        if (showTable) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.TableChart,
                                        "Toggle table",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    if (outputText.isNotEmpty()) {
                                        scope.launch {
                                            clipboard.setClipEntry(
                                                androidx.compose.ui.platform.ClipEntry(
                                                    android.content.ClipData.newPlainText("ascii", outputText)
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
                    }
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    if (showTable && characterInfoList.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Char", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text("Dec", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text("Bin", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                                    Text("Oct", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text("Hex", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                }
                                HorizontalDivider()
                            }
                            items(characterInfoList) { info ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        if (info.char == ' ') "␣" else info.char.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(info.decimal.toString(), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                                    Text(info.binary, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(2f))
                                    Text(info.octal, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                                    Text(info.hex, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        Text(
                            outputText.ifEmpty { "Output will appear here..." },
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (outputText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Reference card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Common ASCII Values:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("A-Z: 65-90", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("a-z: 97-122", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("0-9: 48-57", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("Space: 32", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Newline: 10", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("Tab: 9", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}
