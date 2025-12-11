package cv.toolkit.network

import cv.toolkit.data.IpInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface IpRegistryApi {

    @GET("api/ipdatabase/")
    suspend fun getIpInfo(
        @Query("key") apiKey: String,
        @Query("ip") ip: String
    ): Response<IpInfo>

    @GET("api/ipdatabase/")
    suspend fun getMyIpInfo(
        @Query("key") apiKey: String
    ): Response<IpInfo>

    companion object {
        const val BASE_URL = "https://data.dkly.top/"
        const val API_KEY = "ipd_95623db4ab7c69bd006c6af6256c93e9"
    }
}

