package cv.toolkit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

enum class BaseConverterMode {
    ENCODE, DECODE
}

enum class NumberBase(val label: String, val shortLabel: String, val radix: Int, val padLength: Int) {
    BASE_2("Binary (Base 2)", "Bin", 2, 8),
    BASE_3("Ternary (Base 3)", "Ter", 3, 6),
    BASE_4("Quaternary (Base 4)", "Quat", 4, 4),
    BASE_5("Quinary (Base 5)", "Quin", 5, 4),
    BASE_6("Senary (Base 6)", "Sen", 6, 4),
    BASE_7("Septenary (Base 7)", "Sept", 7, 3),
    BASE_8("Octal (Base 8)", "Oct", 8, 3),
    BASE_9("Nonary (Base 9)", "Non", 9, 3),
    BASE_10("Decimal (Base 10)", "Dec", 10, 0),
    BASE_11("Undecimal (Base 11)", "Und", 11, 3),
    BASE_12("Duodecimal (Base 12)", "Duo", 12, 3),
    BASE_13("Tridecimal (Base 13)", "Tri", 13, 2),
    BASE_14("Tetradecimal (Base 14)", "Tet", 14, 2),
    BASE_15("Pentadecimal (Base 15)", "Pen", 15, 2),
    BASE_16("Hexadecimal (Base 16)", "Hex", 16, 2),
    BASE_17("Heptadecimal (Base 17)", "Hep", 17, 2),
    BASE_18("Octodecimal (Base 18)", "Octo", 18, 2),
    BASE_19("Enneadecimal (Base 19)", "Enn", 19, 2),
    BASE_20("Vigesimal (Base 20)", "Vig", 20, 2),
    BASE_24("Tetravigesimal (Base 24)", "T24", 24, 2),
    BASE_26("Hexavigesimal (Base 26)", "H26", 26, 2),
    BASE_32("Duotrigesimal (Base 32)", "B32", 32, 2),
    BASE_36("Hexatrigesimal (Base 36)", "B36", 36, 2);

    companion object {
        val commonBases = listOf(BASE_2, BASE_8, BASE_10, BASE_16)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinaryConverterScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(BaseConverterMode.ENCODE) }
    var selectedBase by remember { mutableStateOf(NumberBase.BASE_2) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var useDelimiter by remember { mutableStateOf(true) }
    var showAllBases by remember { mutableStateOf(false) }
    var baseDropdownExpanded by remember { mutableStateOf(false) }
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
                BaseConverterMode.ENCODE -> {
                    // Text to number representation
                    inputText.map { char ->
                        val encoded = char.code.toString(selectedBase.radix).uppercase()
                        if (selectedBase.padLength > 0) {
                            encoded.padStart(selectedBase.padLength, '0')
                        } else {
                            encoded
                        }
                    }.joinToString(if (useDelimiter) " " else "")
                }
                BaseConverterMode.DECODE -> {
                    // Number representation to text
                    val cleaned = inputText.trim()
                    val parts = if (useDelimiter) {
                        cleaned.split(Regex("\\s+"))
                    } else {
                        // Split by expected length when no delimiter
                        if (selectedBase.padLength > 0) {
                            cleaned.chunked(selectedBase.padLength)
                        } else {
                            cleaned.split(Regex("\\s+"))
                        }
                    }

                    parts.filter { it.isNotBlank() }.map { part ->
                        val code = part.toInt(selectedBase.radix)
                        if (code < 0 || code > 0x10FFFF) {
                            throw IllegalArgumentException("Invalid character code: $code")
                        }
                        code.toChar()
                    }.joinToString("")
                }
            }
        } catch (e: NumberFormatException) {
            errorMessage = "Invalid ${selectedBase.label} format"
            outputText = ""
        } catch (e: Exception) {
            errorMessage = e.message ?: "Conversion error"
            outputText = ""
        }
    }

    // Auto-process when input changes
    LaunchedEffect(inputText, mode, selectedBase, useDelimiter) {
        processText()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Base Converter") },
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
                    selected = mode == BaseConverterMode.ENCODE,
                    onClick = {
                        mode = BaseConverterMode.ENCODE
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    icon = { Icon(Icons.Filled.Lock, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("Encode")
                }
                SegmentedButton(
                    selected = mode == BaseConverterMode.DECODE,
                    onClick = {
                        mode = BaseConverterMode.DECODE
                        inputText = ""
                        outputText = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    icon = { Icon(Icons.Filled.LockOpen, null, modifier = Modifier.size(18.dp)) }
                ) {
                    Text("Decode")
                }
            }

            // Common bases quick selector
            Text("Quick Select", style = MaterialTheme.typography.labelMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                NumberBase.commonBases.forEachIndexed { index, base ->
                    SegmentedButton(
                        selected = selectedBase == base,
                        onClick = {
                            selectedBase = base
                            inputText = ""
                            outputText = ""
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, NumberBase.commonBases.size)
                    ) {
                        Text(base.shortLabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // All bases dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("All Bases (2-36)", style = MaterialTheme.typography.labelMedium)

                ExposedDropdownMenuBox(
                    expanded = baseDropdownExpanded,
                    onExpandedChange = { baseDropdownExpanded = it },
                    modifier = Modifier.width(200.dp)
                ) {
                    OutlinedTextField(
                        value = selectedBase.label,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = baseDropdownExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = baseDropdownExpanded,
                        onDismissRequest = { baseDropdownExpanded = false }
                    ) {
                        NumberBase.entries.forEach { base ->
                            DropdownMenuItem(
                                text = { Text(base.label, style = MaterialTheme.typography.bodySmall) },
                                onClick = {
                                    selectedBase = base
                                    baseDropdownExpanded = false
                                    inputText = ""
                                    outputText = ""
                                },
                                leadingIcon = {
                                    if (selectedBase == base) {
                                        Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Options
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = useDelimiter, onCheckedChange = { useDelimiter = it })
                Text("Use space delimiter", style = MaterialTheme.typography.bodySmall)
            }

            // Input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = {
                    Text(
                        if (mode == BaseConverterMode.ENCODE) "Text to Encode"
                        else "${selectedBase.shortLabel} to Decode"
                    )
                },
                placeholder = {
                    Text(
                        if (mode == BaseConverterMode.ENCODE) "Enter text..."
                        else getPlaceholderForBase(selectedBase)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 3,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (mode == BaseConverterMode.DECODE && selectedBase.radix <= 10)
                        KeyboardType.Ascii else KeyboardType.Ascii
                ),
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
                        mode = if (mode == BaseConverterMode.ENCODE) BaseConverterMode.DECODE else BaseConverterMode.ENCODE
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
                            if (mode == BaseConverterMode.ENCODE) "${selectedBase.shortLabel} Output" else "Decoded Text",
                            style = MaterialTheme.typography.labelMedium
                        )
                        IconButton(
                            onClick = {
                                if (outputText.isNotEmpty()) {
                                    scope.launch {
                                        clipboard.setClipEntry(
                                            androidx.compose.ui.platform.ClipEntry(
                                                android.content.ClipData.newPlainText("output", outputText)
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
                    Text("Character 'A' (65) in different bases:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column {
                            Text("Bin: 01000001", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("Oct: 101", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Dec: 65", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("Hex: 41", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("B32: 21", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text("B36: 1T", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}

private fun getPlaceholderForBase(base: NumberBase): String {
    // "Hi" in different bases
    return when (base) {
        NumberBase.BASE_2 -> "01001000 01101001"
        NumberBase.BASE_3 -> "002121 002200"
        NumberBase.BASE_4 -> "001020 001221"
        NumberBase.BASE_5 -> "0232 0314"
        NumberBase.BASE_6 -> "0200 0253"
        NumberBase.BASE_7 -> "0132 0150"
        NumberBase.BASE_8 -> "110 151"
        NumberBase.BASE_9 -> "080 116"
        NumberBase.BASE_10 -> "72 105"
        NumberBase.BASE_11 -> "66 96"
        NumberBase.BASE_12 -> "60 89"
        NumberBase.BASE_13 -> "57 81"
        NumberBase.BASE_14 -> "52 77"
        NumberBase.BASE_15 -> "4C 70"
        NumberBase.BASE_16 -> "48 69"
        NumberBase.BASE_17 -> "45 63"
        NumberBase.BASE_18 -> "42 5F"
        NumberBase.BASE_19 -> "3H 5A"
        NumberBase.BASE_20 -> "3C 55"
        NumberBase.BASE_24 -> "30 49"
        NumberBase.BASE_26 -> "2K 41"
        NumberBase.BASE_32 -> "28 39"
        NumberBase.BASE_36 -> "20 2X"
    }
}
