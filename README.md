# CV Toolkit

<div align="center">
  <img src="app/src/main/res/drawable/logo.png" alt="CV Toolkit Logo" width="200"/>
  
  **A Comprehensive Network Diagnostics & Utility Toolkit for Android**
  
  [![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
  [![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
  [![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-blue.svg)](https://developer.android.com/jetpack/compose)
</div>

---

## 📱 About

**CV Toolkit** is an all-in-one Android application designed for network diagnostics, system information, and a wide variety of utility tools. Whether you're a network administrator, developer, cybersecurity enthusiast, or just curious about your device and network, CV Toolkit provides you with 40+ powerful tools in one convenient app.

All processing is done **locally on your device** - your data never leaves your phone, ensuring complete privacy and security.

---

## ✨ Key Features

### 🌐 Network Tools (13 Tools)

- **Speed Test** - Test your download and upload speeds with detailed metrics
- **Ping Test** - Test connectivity and latency to any host
- **Traceroute** - Trace the network path to any destination
- **Port Scanner** - Scan for open ports on any host
- **Network Scanner (Device Discovery)** - Discover all devices connected to your network
- **IP Lookup** - Get detailed geolocation and ISP information for any IP address
- **DNS Lookup** - Query DNS records (A, AAAA, CNAME, MX, TXT, NS)
- **Subnet Calculator** - Calculate network ranges, hosts, and CIDR notation
- **SSL/TLS Checker** - View SSL certificate details and validation chain
- **Whois Lookup** - Get domain registration information
- **HTTP Headers Viewer** - Inspect HTTP response headers
- **Custom Request Builder** - Build and send custom HTTP/CURL requests with headers
- **CDN & Cloud Latency Test** - Test latency to 311+ regions across 14 major providers:
  - AWS (Amazon Web Services)
  - GCP (Google Cloud Platform)
  - Azure (Microsoft Azure)
  - Oracle Cloud
  - Alibaba Cloud
  - DigitalOcean
  - Linode (Akamai)
  - Vultr
  - Hetzner
  - OVH
  - Cloudflare
  - Fastly
  - BunnyCDN
  - StackPath

### 🛠️ Utility Tools (23 Tools)

**Encoders & Decoders:**
- **Base64 Encoder/Decoder** - Encode and decode Base64 strings
- **URL Encoder/Decoder** - Encode and decode URL strings
- **Binary Converter** - Convert text to/from Binary, Hex, and Octal
- **Hex Encoder/Decoder** - Convert text to/from hexadecimal
- **ASCII Converter** - Convert text to ASCII values and vice versa
- **JWT Decoder** - Decode and inspect JWT tokens

**Security & Cryptography:**
- **Hash Generator** - Generate MD5, SHA-1, SHA-256, SHA-512 hashes
- **Caesar Cipher** - Encrypt/decrypt with Caesar cipher, ROT13, ROT47
- **Morse Code Converter** - Convert text to Morse code and back
- **Password Generator** - Generate strong, secure passwords
- **File Hash Calculator** - Calculate MD5, SHA, CRC32 hashes for files with metadata

**QR Codes & Barcodes:**
- **QR Code Generator** - Generate QR codes from text, URLs, contacts, etc.
- **Barcode Generator** - Generate various barcode formats (EAN, UPC, Code 128, etc.)
- **QR/Barcode Scanner** - Scan and decode QR codes and barcodes with camera

**Text & Data Tools:**
- **Text Counter** - Count words, characters, lines, and paragraphs
- **Text Diff** - Compare two texts and highlight differences
- **IP Calculator** - Convert between different IP formats
- **Color Converter** - Convert between HEX, RGB, HSL, and CMYK color formats

**Time & ID Tools:**
- **World Time** - View current time across global time zones
- **Unix Timestamp Converter** - Convert between Unix timestamps and readable dates
- **UUID Generator** - Generate UUID v1, v4, and v5

**Measurement:**
- **Unit Converter** - Convert length, weight, temperature, and data sizes

**Utility:**
- **Stopwatch** - Timer with lap functionality
- **User Agent Parser** - Parse and analyze browser User-Agent strings

### 📱 Device Tools (3 Tools)

- **Device Information** - Complete system information (CPU, RAM, storage, OS, etc.)
- **DRM & Codec Info** - View DRM systems, video/audio codecs, and supported formats
- **Camera Information** - Detailed camera specifications and capabilities

---

## 🏗️ Technical Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Modern declarative UI)
- **Architecture:** MVVM with Repository pattern
- **Networking:** 
  - Retrofit 2 (REST API calls)
  - OkHttp (HTTP client)
  - Gson (JSON parsing)
- **Camera & ML:**
  - CameraX (Camera preview)
  - ML Kit Barcode Scanning (QR/Barcode scanning)
  - ZXing (QR code generation)
- **Navigation:** Jetpack Navigation Compose
- **Minimum SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 14 (API 36)
- **Image Loading:** Coil
- **Monetization:** Google AdMob

---

## 📸 Screenshots

<!-- Add your screenshots here -->
```
[Screenshot 1: Main Screen]  [Screenshot 2: Network Tools]  [Screenshot 3: Utility Tools]
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK API 24 or higher
- Kotlin 1.9+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/osscv/CV-Toolkit.git
```

2. Open the project in Android Studio

3. Sync Gradle dependencies

4. Build and run on your device or emulator

### Build

```bash
./gradlew assembleDebug
```

---

## 📦 Features Breakdown

### IP Lookup with History
- Lookup your current IP or any custom IP address
- Get detailed information: location, ISP, ASN, timezone
- History tracking with timestamps stored locally
- Uses dkly DATAHUB API

### CDN Latency Testing
- Test connectivity to 311+ regions worldwide
- Supports 14 major cloud and CDN providers
- Real-time latency measurements
- Background service support for continuous testing
- Export results
- Visual indicators for latency quality (Excellent, Good, Fair, Poor)

### Network Scanner
- Discover all devices on your local network
- Identify device manufacturers via MAC lookup
- Parallel scanning for faster results
- Export device list

### Port Scanner
- Scan common or custom port ranges
- Parallel port scanning
- Service name detection
- Export scan results

### QR/Barcode Scanner
- Real-time scanning using ML Kit
- Supports QR codes, EAN, UPC, Code 128, Code 39, and more
- Automatic format detection
- Copy scanned content to clipboard
- History of scanned codes

---

## 🔒 Privacy & Security

**Your Privacy Matters:**
- ✅ All data processing is performed **locally on your device**
- ✅ **No data is uploaded** to our servers or any external servers
- ✅ Network scans, device information, and lookup results stay on your phone only
- ✅ IP lookup history is stored locally only
- ✅ No personal data collection, storage, or transmission
- ✅ Open source and transparent

**Permissions Used:**
- `INTERNET` - Required for network diagnostics, IP lookups, DNS queries
- `ACCESS_NETWORK_STATE` - Detect current network configuration
- `CAMERA` - QR/Barcode scanning (optional feature)
- `FOREGROUND_SERVICE` - Background speed tests
- `POST_NOTIFICATIONS` - Speed test progress notifications

**Advertising:**
- This app displays ads via Google AdMob
- Google may collect data for personalized advertising
- Refer to [Google's Privacy Policy](https://policies.google.com/privacy) for details

---

## ⚠️ Responsible Use

This app is designed for **legitimate network diagnostics, troubleshooting, and educational purposes only**.

**Please Note:**
- Only use network scanning tools on networks and devices you **own** or have **explicit permission** to test
- Unauthorized network scanning or port scanning may be **illegal** in your jurisdiction
- The app is provided "as is" without warranty
- The developer is not responsible for misuse or any legal issues arising from use of this app

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

Copyright © 2024 Khoo Lay Yang. All Rights Reserved.

This project is proprietary software. Unauthorized copying, distribution, modification, or use of this software is prohibited without explicit permission from the author.

---

## 👨‍💻 Author

**Khoo Lay Yang**

- Website: [www.dkly.net](https://www.dkly.net)

---

## 📞 Support

Feel free to open an issue on GitHub for:
- 🐛 Bug reports
- 💡 Feature requests or suggestions
- ❓ Questions about functionality
- 🔧 Technical issues

---

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI toolkit
- [Retrofit](https://square.github.io/retrofit/) - Type-safe HTTP client
- [ZXing](https://github.com/zxing/zxing) - QR code generation
- [ML Kit](https://developers.google.com/ml-kit) - Barcode scanning
- [CameraX](https://developer.android.com/training/camerax) - Camera API
- [dkly DATAHUB](https://data.dkly.net) - IP lookup services
- All cloud providers for their global infrastructure

---

## 📊 Statistics

- **40+ Tools** in one app
- **311+ Test Regions** across 14 cloud/CDN providers
- **100% Local Processing** - Your data stays on your device
- **Modern UI** with Material Design 3
- **Zero Server Dependencies** for core functionality

---

<div align="center">
  
**Made with ❤️ by Khoo Lay Yang**

⭐ Star this repo if you find it useful!

</div>

