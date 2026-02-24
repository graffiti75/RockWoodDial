package com.cericatto.rockwooddial.data.di

import android.app.Application
import android.content.Context
import com.cericatto.rockwooddial.data.SongParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app.applicationContext

	@Provides
	@Singleton
	fun provideSongParser(
		@ApplicationContext context: Context
	): SongParser = SongParser(context)

	@Provides
	@Singleton
	fun provideCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
