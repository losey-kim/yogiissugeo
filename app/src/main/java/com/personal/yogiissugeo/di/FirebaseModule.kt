package com.personal.yogiissugeo.di

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Firebase 관련 의존성들을 제공하는 모듈입니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * Firebase Remote Config 인스턴스를 제공.
     *
     * @return FirebaseRemoteConfig의 인스턴스.
     */
    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return Firebase.remoteConfig
    }
}