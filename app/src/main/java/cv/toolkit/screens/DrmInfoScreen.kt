package cv.toolkit.screens

import android.media.MediaCodecList
import android.media.MediaDrm
import android.media.MediaFormat
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import java.util.UUID

data class DrmInfo(val name: String, val vendor: String, val version: String, val securityLevel: String, val properties: Map<String, String>)
data class CodecInfo(val name: String, val type: String, val isHardware: Boolean, val maxResolution: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrmInfoScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val drmInfoList = remember { getDrmInfoList() }
    val codecList = remember { getCodecList() }

    // Track usage for interstitial ad (every 2 views)
    LaunchedEffect(Unit) {
        activity?.let { AdMobManager.trackDrmCodesUsage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.drm_info_title)) },
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("DRM Systems", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(drmInfoList) { drm ->
                    DrmCard(drm)
                }
                item { Spacer(Modifier.height(8.dp)) }
                item { Text("Video Decoders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(codecList.filter { it.type.startsWith("video") }) { codec ->
                    CodecCard(codec)
                }
                item { Spacer(Modifier.height(8.dp)) }
                item { Text("Audio Decoders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(codecList.filter { it.type.startsWith("audio") }) { codec ->
                    CodecCard(codec)
                }
                item { Spacer(Modifier.height(8.dp)) }
                item { Text("Audio Capabilities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                item { AudioCapabilitiesCard() }
                item { Spacer(Modifier.height(8.dp)) }
                item { Text("Subtitle Formats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                item { SubtitleFormatsCard() }
            }
            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DrmCard(drm: DrmInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(drm.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            DrmProperty("Vendor", drm.vendor)
            DrmProperty("Version", drm.version)
            DrmProperty("Security Level", drm.securityLevel)
            if (drm.properties.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                drm.properties.forEach { (key, value) ->
                    DrmProperty(key, value)
                }
            }
        }
    }
}

@Composable
private fun DrmProperty(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.6f))
    }
}

private fun getDrmInfoList(): List<DrmInfo> {
    val drmList = mutableListOf<DrmInfo>()

    // Widevine
    try {
        val widevineUUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
        if (MediaDrm.isCryptoSchemeSupported(widevineUUID)) {
            val drm = MediaDrm(widevineUUID)
            val props = mutableMapOf<String, String>()
            listOf("systemId", "algorithms", "hdcpLevel", "maxHdcpLevel", "maxNumberOfSessions", "numberOfOpenSessions").forEach {
                try { props[it] = drm.getPropertyString(it) } catch (_: Exception) {}
            }
            drmList.add(DrmInfo(
                name = "Widevine",
                vendor = drm.getPropertyString(MediaDrm.PROPERTY_VENDOR),
                version = drm.getPropertyString(MediaDrm.PROPERTY_VERSION),
                securityLevel = getWidevineSecurityLevel(drm),
                properties = props
            ))
            drm.close()
        }
    } catch (_: Exception) {}

    // PlayReady
    try {
        val playReadyUUID = UUID(-0x65fb0f8667bfbd7aL, -0x546d19a41f77a06bL)
        if (MediaDrm.isCryptoSchemeSupported(playReadyUUID)) {
            val drm = MediaDrm(playReadyUUID)
            drmList.add(DrmInfo(
                name = "PlayReady",
                vendor = drm.getPropertyString(MediaDrm.PROPERTY_VENDOR),
                version = drm.getPropertyString(MediaDrm.PROPERTY_VERSION),
                securityLevel = try { drm.getPropertyString("securityLevel") } catch (_: Exception) { "Unknown" },
                properties = emptyMap()
            ))
            drm.close()
        }
    } catch (_: Exception) {}

    // ClearKey
    try {
        val clearKeyUUID = UUID(-0x1d8e62a7567a4c37L, 0x781AB030AF78D30EL)
        if (MediaDrm.isCryptoSchemeSupported(clearKeyUUID)) {
            val drm = MediaDrm(clearKeyUUID)
            drmList.add(DrmInfo(
                name = "ClearKey",
                vendor = drm.getPropertyString(MediaDrm.PROPERTY_VENDOR),
                version = drm.getPropertyString(MediaDrm.PROPERTY_VERSION),
                securityLevel = "Software",
                properties = emptyMap()
            ))
            drm.close()
        }
    } catch (_: Exception) {}

    return drmList
}

private fun getWidevineSecurityLevel(drm: MediaDrm): String {
    return try {
        val level = drm.getPropertyString("securityLevel")
        when (level) {
            "L1" -> "L1 (Hardware)"
            "L2" -> "L2 (Hardware Decrypt)"
            "L3" -> "L3 (Software)"
            else -> level
        }
    } catch (_: Exception) { "Unknown" }
}

@Composable
private fun CodecCard(codec: CodecInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(codec.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(codec.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (codec.maxResolution.isNotEmpty()) {
                    Text("Max: ${codec.maxResolution}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (codec.isHardware) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    if (codec.isHardware) "HW" else "SW",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getCodecList(): List<CodecInfo> {
    val codecList = mutableListOf<CodecInfo>()
    val mcl = MediaCodecList(MediaCodecList.ALL_CODECS)

    mcl.codecInfos.filter { !it.isEncoder }.forEach { info ->
        info.supportedTypes.forEach { type ->
            val isHardware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.isHardwareAccelerated else !info.name.startsWith("OMX.google")
            val maxRes = try {
                val caps = info.getCapabilitiesForType(type)
                val vc = caps.videoCapabilities
                if (vc != null) "${vc.supportedWidths.upper}x${vc.supportedHeights.upper}" else ""
            } catch (_: Exception) { "" }

            codecList.add(CodecInfo(
                name = info.name,
                type = type,
                isHardware = isHardware,
                maxResolution = maxRes
            ))
        }
    }
    return codecList.sortedWith(compareBy({ !it.isHardware }, { it.type }, { it.name }))
}

@Composable
private fun SubtitleFormatsCard() {
    val subtitleFormats = listOf(
        "WebVTT" to "text/vtt",
        "SRT" to "application/x-subrip",
        "SSA/ASS" to "text/x-ssa",
        "TTML" to "application/ttml+xml",
        "CEA-608" to "text/cea-608",
        "CEA-708" to "text/cea-708",
        "DVB" to "application/dvbsubs",
        "PGS" to "application/pgs",
        "TX3G" to "application/x-quicktime-tx3g"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            subtitleFormats.forEach { (name, mime) ->
                val supported = isSubtitleSupported(mime)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(mime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        if (supported) "✓" else "✗",
                        color = if (supported) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioCapabilitiesCard() {
    val audioFormats = listOf(
        "AAC" to "audio/mp4a-latm",
        "HE-AAC v1" to "audio/mp4a-latm",
        "HE-AAC v2" to "audio/mp4a-latm",
        "xHE-AAC" to "audio/mp4a-latm",
        "MP3" to "audio/mpeg",
        "Opus" to "audio/opus",
        "Vorbis" to "audio/vorbis",
        "FLAC" to "audio/flac",
        "ALAC" to "audio/alac",
        "AC3 (Dolby Digital)" to "audio/ac3",
        "E-AC3 (Dolby Digital+)" to "audio/eac3",
        "Dolby AC4" to "audio/ac4",
        "Dolby Atmos (E-AC3 JOC)" to "audio/eac3-joc",
        "Dolby TrueHD" to "audio/true-hd",
        "DTS" to "audio/vnd.dts",
        "DTS-HD" to "audio/vnd.dts.hd",
        "DTS:X" to "audio/vnd.dts.uhd",
        "Audio Vivid" to "audio/av3a",
        "MPEG-H 3D Audio" to "audio/mhm1",
        "AMR-NB" to "audio/3gpp",
        "AMR-WB" to "audio/amr-wb",
        "PCM" to "audio/raw",
        "GSM" to "audio/gsm",
        "MIDI" to "audio/midi"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            audioFormats.forEach { (name, mime) ->
                val supported = isAudioSupported(mime)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(mime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        if (supported) "✓" else "✗",
                        color = if (supported) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun isAudioSupported(mimeType: String): Boolean {
    return try {
        val mcl = MediaCodecList(MediaCodecList.ALL_CODECS)
        mcl.codecInfos.any { info ->
            !info.isEncoder && info.supportedTypes.any { it.equals(mimeType, ignoreCase = true) }
        }
    } catch (_: Exception) { false }
}

private fun isSubtitleSupported(mimeType: String): Boolean {
    // Text-based subtitles are handled by media players (ExoPlayer/MediaPlayer), not MediaCodec
    val supportedFormats = setOf(
        "text/vtt",
        "application/x-subrip",
        "text/x-ssa",
        "application/ttml+xml",
        "text/cea-608",
        "text/cea-708",
        "application/dvbsubs",
        "application/pgs",
        "application/x-quicktime-tx3g"
    )
    return supportedFormats.contains(mimeType)
}
