package cv.toolkit

import android.app.Application
import com.google.android.gms.ads.MobileAds
import cv.toolkit.ads.AdMobManager

class CVToolkitApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(this) {
            // Preload ads after SDK initialization
            AdMobManager.loadInterstitialAd(this)
            AdMobManager.loadRewardedAd(this)
        }
    }
}
