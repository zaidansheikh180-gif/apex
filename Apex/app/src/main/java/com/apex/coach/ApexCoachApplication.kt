package com.apex.coach

import android.app.Application
import android.content.pm.PackageManager
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.amplitude.api.Amplitude
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ApexCoachApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
        }
        
        // Initialize Amplitude
        try {
            val amplitudeApiKey = packageManager.getApplicationInfo(packageName, 
                PackageManager.GET_META_DATA).metaData.getString("amplitude_api_key")
            if (amplitudeApiKey != null) {
                Amplitude.getInstance().initialize(this, amplitudeApiKey).enableForegroundTracking(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Initialize RevenueCat
        try {
            val revenueCatApiKey = packageManager.getApplicationInfo(packageName,
                PackageManager.GET_META_DATA).metaData.getString("revenuecat_api_key")
            if (revenueCatApiKey != null) {
                Purchases.configure(PurchasesConfiguration.Builder(this, revenueCatApiKey).build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}
