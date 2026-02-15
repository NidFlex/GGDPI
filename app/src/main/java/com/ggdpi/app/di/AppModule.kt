package com.ggdpi.app.di

import android.content.Context
import com.ggdpi.app.core.StrategyManager

object AppModule {
    
    private var strategyManager: StrategyManager? = null
    
    fun provideStrategyManager(context: Context): StrategyManager {
        return strategyManager ?: StrategyManager(context.applicationContext).also {
            strategyManager = it
        }
    }
}