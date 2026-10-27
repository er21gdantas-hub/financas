package br.com.minhasfinancas.di

import android.content.Context
import androidx.room.Room
import br.com.minhasfinancas.data.local.AppDatabase
import br.com.minhasfinancas.data.local.MIGRACAO_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {
    @Provides
    @Singleton
    fun banco(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "minhas_financas.db").addMigrations(MIGRACAO_1_2).build()
}
