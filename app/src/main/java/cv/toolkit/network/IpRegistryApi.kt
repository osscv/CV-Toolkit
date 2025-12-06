package cv.toolkit.network

import cv.toolkit.data.IpInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IpRegistryApi {
    
    @GET("{ip}")
    suspend fun getIpInfo(
        @Path("ip") ip: String,
        @Query("hostname") hostname: Boolean = true,
        @Query("key") apiKey: String
    ): Response<IpInfo>
    
    @GET("?")
    suspend fun getMyIpInfo(
        @Query("hostname") hostname: Boolean = true,
        @Query("key") apiKey: String
    ): Response<IpInfo>
    
    companion object {
        const val BASE_URL = "https://data.dkly.net/"
        const val API_KEY = "[Your_API-KEY_HERE]"
        //Register a FREE API key FROM https://data.dkly.net/
    }
}

