package com.rsinukov.shutterstockclient.dagger

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ClientModule {

    private companion object {
        const val CONNECT_TIMEOUT_SEC = 100L
        const val READ_TIMEOUT_SEC = 100L

        const val BASE_URL = "https://api.shutterstock.com/v2/"
        const val CLIENT_ID = "79ffd-41b8f-9e1fd-b463a-83811-d48d9"
        const val CLIENT_SECRET = "0e9d3-d4d02-d6b3a-2287e-cb291-b6817"
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authenticator: Authenticator
    ): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
            .authenticator(authenticator)
            .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)

        okHttpClient.addNetworkInterceptor(loggingInterceptor)

        return okHttpClient.build()
    }

    @Provides
    fun provideHttpLoggingInterceptor(logger: HttpLoggingInterceptor.Logger): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor(logger)
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC

        return loggingInterceptor
    }

    @Provides
    fun provideAuthentificator(): Authenticator {
        return Authenticator { _, response ->
            if (response.request().header("Authorization") != null) {
                return@Authenticator null // Give up, we've already attempted to authenticate.
            }

            val credential = Credentials.basic(CLIENT_ID, CLIENT_SECRET)
            response.request().newBuilder()
                .header("Authorization", credential)
                .build()
        }
    }

    @Provides
    fun provideLogger() = HttpLoggingInterceptor.Logger { message -> Timber.d(message) }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }
}
