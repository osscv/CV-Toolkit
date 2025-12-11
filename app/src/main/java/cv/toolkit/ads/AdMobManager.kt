package cv.toolkit.ads

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobManager {

    // Ad Unit IDs
    object AdUnitIds {
        const val BANNER = "ca-app-pub-6119901489929327/2561491835"
        const val INTERSTITIAL = "ca-app-pub-6119901489929327/1356329172"
        const val APP_OPEN = "ca-app-pub-6119901489929327/1460270950"
        const val NATIVE_ADVANCED = "ca-app-pub-6119901489929327/7949657680"
        const val REWARDED_INTERSTITIAL = "ca-app-pub-6119901489929327/6521025941"
        const val REWARDED = "ca-app-pub-6119901489929327/5207944279"
    }

    // Usage thresholds for interstitial ads
    object InterstitialThresholds {
        const val IP_LOOKUP = 3
        const val PING = 5
        const val TRACEROUTE = 5
        const val DNS = 5
        const val PORT_SCAN = 4
        const val DEVICE_DISCOVERY = 2
        const val DRM_CODES = 2
        const val DEVICE_INFO = 2
    }

    // Rewarded ad threshold
    const val REWARDED_IP_LOOKUP_THRESHOLD = 12

    private const val PREFS_NAME = "ad_usage_prefs"
    private const val KEY_IP_LOOKUP_COUNT = "ip_lookup_count"
    private const val KEY_PING_COUNT = "ping_count"
    private const val KEY_TRACEROUTE_COUNT = "traceroute_count"
    private const val KEY_DNS_COUNT = "dns_count"
    private const val KEY_PORT_SCAN_COUNT = "port_scan_count"
    private const val KEY_DEVICE_DISCOVERY_COUNT = "device_discovery_count"
    private const val KEY_DRM_CODES_COUNT = "drm_codes_count"
    private const val KEY_DEVICE_INFO_COUNT = "device_info_count"
    private const val KEY_IP_LOOKUP_REWARDED_COUNT = "ip_lookup_rewarded_count"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isLoadingInterstitial = false
    private var isLoadingRewarded = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Load interstitial ad
    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isLoadingInterstitial) return

        isLoadingInterstitial = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            AdUnitIds.INTERSTITIAL,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingInterstitial = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoadingInterstitial = false
                }
            }
        )
    }

    // Load rewarded ad
    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || isLoadingRewarded) return

        isLoadingRewarded = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            AdUnitIds.REWARDED,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingRewarded = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoadingRewarded = false
                }
            }
        )
    }

    // Show interstitial ad
    private fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitialAd(activity)
            onDismissed()
        }
    }

    // Show rewarded ad
    fun showRewardedAd(activity: Activity, onRewarded: () -> Unit, onDismissed: () -> Unit = {}) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onDismissed()
                }
            }
            ad.show(activity) { onRewarded() }
        } else {
            loadRewardedAd(activity)
            onDismissed()
        }
    }

    // Track usage and show interstitial if threshold reached
    fun trackIpLookupUsage(activity: Activity, onComplete: () -> Unit = {}) {
        val prefs = getPrefs(activity)
        val count = prefs.getInt(KEY_IP_LOOKUP_COUNT, 0) + 1
        prefs.edit().putInt(KEY_IP_LOOKUP_COUNT, count).apply()

        if (count >= InterstitialThresholds.IP_LOOKUP) {
            prefs.edit().putInt(KEY_IP_LOOKUP_COUNT, 0).apply()
            showInterstitialAd(activity, onComplete)
        } else {
            onComplete()
        }
    }

    fun trackPingUsage(activity: Activity, onComplete: () -> Unit = {}) {
        val prefs = getPrefs(activity)
        val count = prefs.getInt(KEY_PING_COUNT, 0) + 1
        prefs.edit().putInt(KEY_PING_COUNT, count).apply()

        if (count >= InterstitialThresholds.PING) {
            prefs.edit().putInt(KEY_PING_COUNT, 0).apply()
            showInterstitialAd(activity, onComplete)
        } else {
            onComplete()
        }
    }

    fun trackTracerouteUsage(activity: Activity, onComplete: () -> Unit = {}) {
        val prefs = getPrefs(activity)
        val count = prefs.getInt(KEY_TRACEROUTE_COUNT, 0) + 1
        prefs.edit().putInt(KEY_TRACEROUTE_COUNT, count).apply()

        if (count >= InterstitialThresholds.TRACEROUTE) {
            prefs.edit().putInt(KEY_TRACEROUTE_COUNT, 0).apply()
            showInterstitialAd(activity, onComplete)
        } else {
            onComplete()
        }
    }

    fun trackDnsUsage(activity: Activity, onComplete: () -> Unit = {}) {
        val prefs = getPrefs(activity)
        val count = prefs.getInt(KEY_DNS_COUNT, 0) + 1
        prefs.edit().putInt(KEY_DNS_COUNT, count).apply()

        if (count >= InterstitialThresholds.DNS) {
            prefs.edit().putInt(KEY_DNS_COUNT, 0).apply()
            showInterstitialAd(activity, onComplete)
        } else {
            onComplete()
        }
    }

    fun trackPortScanUsage(activity: Activity, onComplete: () -> Unit = {}) {
        val prefs = getPrefs(activity)
        val count = prefs.getInt(KEY_PORT_SCAN_COUNT, 0) + 1
        prefs.edit().putInt(KEY_PORT_SCAN_COUNT, count).apply()

        if (count >= InterstitialThresholds.PORT_SCAN) {
            prefs.edit().putInt(KEY_PORT_SCAN_COUNT, 0).apply()
            showInterstitialAd(activity, onComplete)
        } else {
            onComplete()
        }
    }

    fun trackDeviceDiscoveryUsage(activity: Activity, onComplete: () -> Unit = {}) {
        val prefs = getPrefs(activity)
        val count = prefs.getInt(KEY_DEVICE_DISCOVERY_COUNT, 0) + 1
        prefs.edit().putInt(KEY_DEVICE_DISCOVERY_COUNT, count).apply()

        if (count >= InterstitialThresholds.DEVICE_DISCOVERY) {
            prefs.edit().putInt(KEY_DEVICE_DISCOVERY_COUNT, 0).apply()
            showInterstitialAd(activity, onComplete)
        } else {
            onComplete()
        }
    }

    fun trackDrmCodesUsage(activity: Activity, onComplete: () -> Unit = {}) {
        val prefs = getPrefs(activity)
        val count = prefs.getInt(KEY_DRM_CODES_COUNT, 0) + 1
        prefs.edit().putInt(KEY_DRM_CODES_COUNT, count).apply()

        if (count >= InterstitialThresholds.DRM_CODES) {
            prefs.edit().putInt(KEY_DRM_CODES_COUNT, 0).apply()
            showInterstitialAd(activity, onComplete)
        } else {
            onComplete()
        }
    }

    fun trackDeviceInfoUsage(activity: Activity, onComplete: () -> Unit = {}) {
        val prefs = getPrefs(activity)
        val count = prefs.getInt(KEY_DEVICE_INFO_COUNT, 0) + 1
        prefs.edit().putInt(KEY_DEVICE_INFO_COUNT, count).apply()

        if (count >= InterstitialThresholds.DEVICE_INFO) {
            prefs.edit().putInt(KEY_DEVICE_INFO_COUNT, 0).apply()
            showInterstitialAd(activity, onComplete)
        } else {
            onComplete()
        }
    }

    // Track IP lookup for rewarded ad
    fun trackIpLookupForReward(activity: Activity, onShowReward: () -> Unit, onSkip: () -> Unit) {
        val prefs = getPrefs(activity)
        val count = prefs.getInt(KEY_IP_LOOKUP_REWARDED_COUNT, 0) + 1
        prefs.edit().putInt(KEY_IP_LOOKUP_REWARDED_COUNT, count).apply()

        if (count >= REWARDED_IP_LOOKUP_THRESHOLD) {
            prefs.edit().putInt(KEY_IP_LOOKUP_REWARDED_COUNT, 0).apply()
            onShowReward()
        } else {
            onSkip()
        }
    }

    fun isRewardedAdReady(): Boolean = rewardedAd != null
}
