package cv.toolkit.repository

import cv.toolkit.data.IpInfo
import cv.toolkit.network.CloudflareIpApi
import cv.toolkit.network.IpRegistryApi
import cv.toolkit.network.RetrofitInstance
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DetectedIps(val ipv4: String?, val ipv6: String?)

class IpLookupRepository {

    private val api = RetrofitInstance.api

    suspend fun detectUserIps(): DetectedIps = coroutineScope {
        val ipv4Deferred = async { fetchIpFrom(CloudflareIpApi.IPV4_URL) }
        val ipv6Deferred = async { fetchIpFrom(CloudflareIpApi.IPV6_URL) }

        val ipv4 = ipv4Deferred.await()
        val ipv6 = ipv6Deferred.await()

        // If both return same IP, user likely only has one type
        // Use backup endpoint to determine which
        if (ipv4 == ipv6 && ipv4 != null) {
            val backupIp = fetchIpFrom(CloudflareIpApi.IP_CHECK_URL)
            return@coroutineScope if (backupIp?.contains(":") == true) {
                DetectedIps(null, backupIp)
            } else {
                DetectedIps(backupIp, null)
            }
        }

        DetectedIps(ipv4, ipv6)
    }

    private suspend fun fetchIpFrom(url: String): String? {
        return try {
            val api = CloudflareIpApi.create(url)
            val response = api.getIp()
            if (response.isSuccessful) {
                response.body()?.ipAddress
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMyIpInfo(): Result<IpInfo> {
        return try {
            val response = api.getMyIpInfo(hostname = true, apiKey = IpRegistryApi.API_KEY)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getIpInfo(ip: String): Result<IpInfo> {
        return try {
            val response = api.getIpInfo(ip = ip, hostname = true, apiKey = IpRegistryApi.API_KEY)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

