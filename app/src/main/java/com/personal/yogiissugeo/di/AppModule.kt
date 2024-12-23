package com.personal.yogiissugeo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Context 관련 의존성들을 제공하는 모듈입니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /**
     * 애플리케이션의 Context를 Singleton 스코프로 제공합니다.
     */
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context
}