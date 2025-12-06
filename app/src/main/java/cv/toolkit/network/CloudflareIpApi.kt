package cv.toolkit.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

data class CloudflareIpResponse(
    @SerializedName("ip_address") val ipAddress: String,
    @SerializedName("ip_version") val ipVersion: String
)

interface CloudflareIpApi {

    @GET("/")
    suspend fun getIp(): Response<CloudflareIpResponse>

    companion object {
        const val IPV4_URL = "https://ipv4-check-perf.radar.cloudflare.com/"
        const val IPV6_URL = "https://ipv6-check-perf.radar.cloudflare.com/"
        const val IP_CHECK_URL = "https://ip-check-perf.radar.cloudflare.com/"

        private val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        fun create(baseUrl: String): CloudflareIpApi {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CloudflareIpApi::class.java)
        }
    }
}
