package cv.toolkit.data

import com.google.gson.annotations.SerializedName

data class IpInfo(
    @SerializedName("ip")
    val ip: String?,
    
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("hostname")
    val hostname: String?,
    
    @SerializedName("carrier")
    val carrier: Carrier?,
    
    @SerializedName("company")
    val company: Company?,
    
    @SerializedName("location")
    val location: Location?,
    
    @SerializedName("time_zone")
    val timeZone: TimeZone?,
    
    @SerializedName("currency")
    val currency: Currency?,
    
    @SerializedName("connection")
    val connection: Connection?,
    
    @SerializedName("security")
    val security: Security?,
    
    @SerializedName("user_agent")
    val userAgent: UserAgent?
)

data class Carrier(
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("mcc")
    val mcc: String?,
    
    @SerializedName("mnc")
    val mnc: String?
)

data class Company(
    @SerializedName("domain")
    val domain: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("type")
    val type: String?
)

data class Location(
    @SerializedName("continent")
    val continent: Continent?,
    
    @SerializedName("country")
    val country: Country?,
    
    @SerializedName("region")
    val region: Region?,
    
    @SerializedName("city")
    val city: String?,
    
    @SerializedName("postal")
    val postal: String?,
    
    @SerializedName("latitude")
    val latitude: Double?,
    
    @SerializedName("longitude")
    val longitude: Double?,
    
    @SerializedName("language")
    val language: Language?,
    
    @SerializedName("in_eu")
    val inEu: Boolean?
)

data class Continent(
    @SerializedName("code")
    val code: String?,
    
    @SerializedName("name")
    val name: String?
)

data class Country(
    @SerializedName("code")
    val code: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("area")
    val area: Int?,
    
    @SerializedName("borders")
    val borders: List<String>?,
    
    @SerializedName("calling_code")
    val callingCode: String?,
    
    @SerializedName("capital")
    val capital: String?,
    
    @SerializedName("population")
    val population: Long?,
    
    @SerializedName("population_density")
    val populationDensity: Double?,
    
    @SerializedName("flag")
    val flag: Flag?,
    
    @SerializedName("languages")
    val languages: List<Language>?,
    
    @SerializedName("tld")
    val tld: String?
)

data class Flag(
    @SerializedName("emoji")
    val emoji: String?,
    
    @SerializedName("emoji_unicode")
    val emojiUnicode: String?
)

data class Region(
    @SerializedName("code")
    val code: String?,
    
    @SerializedName("name")
    val name: String?
)

data class Language(
    @SerializedName("code")
    val code: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("native")
    val native: String?
)

data class TimeZone(
    @SerializedName("id")
    val id: String?,
    
    @SerializedName("abbreviation")
    val abbreviation: String?,
    
    @SerializedName("current_time")
    val currentTime: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("offset")
    val offset: Int?,
    
    @SerializedName("in_daylight_saving")
    val inDaylightSaving: Boolean?
)

data class Currency(
    @SerializedName("code")
    val code: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("name_native")
    val nameNative: String?,
    
    @SerializedName("plural")
    val plural: String?,
    
    @SerializedName("plural_native")
    val pluralNative: String?,
    
    @SerializedName("symbol")
    val symbol: String?,
    
    @SerializedName("symbol_native")
    val symbolNative: String?,
    
    @SerializedName("format")
    val format: CurrencyFormat?
)

data class CurrencyFormat(
    @SerializedName("decimal_separator")
    val decimalSeparator: String?,
    
    @SerializedName("group_separator")
    val groupSeparator: String?,
    
    @SerializedName("positive")
    val positive: CurrencyFormatPattern?,
    
    @SerializedName("negative")
    val negative: CurrencyFormatPattern?
)

data class CurrencyFormatPattern(
    @SerializedName("prefix")
    val prefix: String?,
    
    @SerializedName("suffix")
    val suffix: String?
)

data class Connection(
    @SerializedName("asn")
    val asn: Int?,
    
    @SerializedName("organization")
    val organization: String?,
    
    @SerializedName("domain")
    val domain: String?,
    
    @SerializedName("route")
    val route: String?,
    
    @SerializedName("type")
    val type: String?
)

data class Security(
    @SerializedName("is_abuser")
    val isAbuser: Boolean?,
    
    @SerializedName("is_attacker")
    val isAttacker: Boolean?,
    
    @SerializedName("is_bogon")
    val isBogon: Boolean?,
    
    @SerializedName("is_cloud_provider")
    val isCloudProvider: Boolean?,
    
    @SerializedName("is_proxy")
    val isProxy: Boolean?,
    
    @SerializedName("is_relay")
    val isRelay: Boolean?,
    
    @SerializedName("is_tor")
    val isTor: Boolean?,
    
    @SerializedName("is_tor_exit")
    val isTorExit: Boolean?,
    
    @SerializedName("is_vpn")
    val isVpn: Boolean?,
    
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean?,
    
    @SerializedName("is_threat")
    val isThreat: Boolean?
)

data class UserAgent(
    @SerializedName("header")
    val header: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("version")
    val version: String?,
    
    @SerializedName("version_major")
    val versionMajor: String?,
    
    @SerializedName("device")
    val device: Device?,
    
    @SerializedName("engine")
    val engine: Engine?,
    
    @SerializedName("os")
    val os: OperatingSystem?
)

data class Device(
    @SerializedName("brand")
    val brand: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("type")
    val type: String?
)

data class Engine(
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("version")
    val version: String?,
    
    @SerializedName("version_major")
    val versionMajor: String?
)

data class OperatingSystem(
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("version")
    val version: String?
)

