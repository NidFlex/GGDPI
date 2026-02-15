package com.ggdpi.app

import android.app.Application
import timber.log.Timber

class GGDPIApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        System.loadLibrary("ggdpi")
    }
}