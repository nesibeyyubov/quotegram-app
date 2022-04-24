package com.nesib.quotegram.di

import com.ihsanbal.logging.LoggingInterceptor
import com.nesib.quotegram.data.network.AuthApi
import com.nesib.quotegram.data.network.MainApi
import com.nesib.quotegram.data.network.MyOkHttpClientInterceptor
import com.nesib.quotegram.utils.Constants.API_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }


    @Provides
    fun provideOkHttpClient(
        myOkHttpClientInterceptor: MyOkHttpClientInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(myOkHttpClientInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideMainRetrofit(
        gsonConverterFactory: GsonConverterFactory,
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(gsonConverterFactory)
            .baseUrl(API_URL)
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideMainApi(retrofit: Retrofit): MainApi {
        return retrofit.create(MainApi::class.java)
    }


    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

}