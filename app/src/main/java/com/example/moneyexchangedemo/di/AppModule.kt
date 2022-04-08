package com.example.moneyexchangedemo.di

import android.app.Application
import androidx.room.Room
import com.example.moneyexchangedemo.db.AppDatabase
import com.example.moneyexchangedemo.network.MoneyExchangeAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://api.exchangeratesapi.io/v1/"

    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    @Provides
    @Singleton
    fun getRetroInstance(
        okHttpClient: OkHttpClient
    ) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    //we put all the APIs being used
    @Provides
    @Singleton
    fun provideMoneyExchangeAPI(retrofit: Retrofit): MoneyExchangeAPI =
        retrofit.create(MoneyExchangeAPI::class.java)

    //we put all the databases being used
    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "money_exchange_demo_db"
        ).fallbackToDestructiveMigration()
            .build()
}