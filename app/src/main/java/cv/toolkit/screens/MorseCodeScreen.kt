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

enum class MorseMode {
    ENCODE, DECODE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorseCodeScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(MorseMode.ENCODE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var useDotDash by remember { mutableStateOf(true) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val morseMap = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
        'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
        'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
        'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--",
        'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
        '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '\'' to ".----.",
        '!' to "-.-.--", '/' to "-..-.", '(' to "-.--.", ')' to "-.--.-",
        '&' to ".-...", ':' to "---...", ';' to "-.-.-.", '=' to "-...-",
        '+' to ".-.-.", '-' to "-....-", '_' to "..--.-", '"' to ".-..-.",
        '$' to "...-..-", '@' to ".--.-.", ' ' to "/"
    )

    val reverseMorseMap = morseMap.entries.associate { (k, v) -> v to k }

    fun textToMorse(text: String): String {
        return text.uppercase().map { char ->
            morseMap[char] ?: if (char.isWhitespace()) "/" else "?"
        }.joinToString(" ")
    }

    fun morseToText(morse: String): String {
        val normalized = morse
            .replace("·", ".")
            .replace("•", ".")
            .replace("−", "-")
            .replace("–", "-")
            .replace("—", "-")

        return normalized.split(" ").map { code ->
            when {
                code == "/" || code == "|" -> ' '
                code.isBlank() -> ' '
                else -> reverseMorseMap[code] ?: '?'
            }
        }.joinToString("").replace(Regex("\\s+"), " ").trim()
    }

    fun convertOutput(morse: String): String {
        return if (useDotDash) {
            morse
        } else {
            morse.replace(".", "·").replace("-", "−")
        }
    }

    fun processText() {
        errorMessage = null
        if (inputText.isBlank()) {
            outputText = ""
            return
        }

        try {
            outputText = when (mode) {
                MorseMode.ENCODE -> convertOutput(textToMorse(inputText))
                MorseMode.DECODE -> morseToText(inputText)
            }
        } catch (e: Exception) {
            errorMessage = "Conversion error: ${e.message}"
            outputText = ""
        }
    }

    LaunchedEffect(inputText, mode, useDotDash) {
        processText()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Morse Code") },
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
                    selected = mode == MorseMode.ENCODE,
                    onClick = {
                        mode = MorseMode.ENCODE
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    icon = { Icon(Icons.Filled.Lock, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("Text → Morse")
                }
                SegmentedButton(
                    selected = mode == MorseMode.DECODE,
                    onClick = {
                        mode = MorseMode.DECODE
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    icon = { Icon(Icons.Filled.LockOpen, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("Morse → Text")
                }
            }

            // Options
            if (mode == MorseMode.ENCODE) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useDotDash, onCheckedChange = { useDotDash = it })
                    Text(
                        if (useDotDash) "Using . and -" else "Using · and −",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = {
                    Text(if (mode == MorseMode.ENCODE) "Text to Encode" else "Morse Code to Decode")
                },
                placeholder = {
                    Text(
                        if (mode == MorseMode.ENCODE) "Enter text..."
                        else "Enter morse code (e.g., .... . .-.. .-.. ---)"
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
                        mode = if (mode == MorseMode.ENCODE) MorseMode.DECODE else MorseMode.ENCODE
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
                            if (mode == MorseMode.ENCODE) "Morse Code Output" else "Decoded Text",
                            style = MaterialTheme.typography.labelMedium
                        )
                        IconButton(
                            onClick = {
                                if (outputText.isNotEmpty()) {
                                    scope.launch {
                                        clipboard.setClipEntry(
                                            androidx.compose.ui.platform.ClipEntry(
                                                android.content.ClipData.newPlainText("morse", outputText)
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
                    Text("Morse Code Reference:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("A: .-", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("E: .", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("S: ...", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("O: ---", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("T: -", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("H: ....", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("SOS: ... --- ...", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("Space: /", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}
