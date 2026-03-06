package com.snaptool.di

import com.snaptool.data.repository.CameraRepositoryImpl
import com.snaptool.data.repository.MediaRepositoryImpl
import com.snaptool.data.repository.ScreenRecordRepositoryImpl
import com.snaptool.data.repository.SettingsRepositoryImpl
import com.snaptool.domain.repository.CameraRepository
import com.snaptool.domain.repository.MediaRepository
import com.snaptool.domain.repository.ScreenRecordRepository
import com.snaptool.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        mediaRepositoryImpl: MediaRepositoryImpl
    ): MediaRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindCameraRepository(
        cameraRepositoryImpl: CameraRepositoryImpl
    ): CameraRepository

    @Binds
    @Singleton
    abstract fun bindScreenRecordRepository(
        screenRecordRepositoryImpl: ScreenRecordRepositoryImpl
    ): ScreenRecordRepository
}
