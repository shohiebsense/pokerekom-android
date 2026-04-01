package com.shohiebsense.pokerekom.di

import android.content.Context
import com.shohiebsense.pokerekom.data.local.ImageCache
import com.shohiebsense.pokerekom.data.local.PrefsDataStore
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideImageCache(@ApplicationContext context: Context): ImageCache {
        return ImageCache(context)
    }

    @Provides
    @Singleton
    fun providePrefsDataStore(@ApplicationContext context: Context): PrefsDataStore {
        return PrefsDataStore(context)
    }
}
