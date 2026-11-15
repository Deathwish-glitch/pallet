package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton client that creates and manages the Retrofit service for Gemini API.
 */
object GeminiRetrofitClient {

  private const val BASE_URL = "https://generativelanguage.googleapis.com/"

  val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  val okHttpClient: OkHttpClient by lazy {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BASIC
    }
    OkHttpClient.Builder()
      .connectTimeout(20, TimeUnit.SECONDS)
      .readTimeout(20, TimeUnit.SECONDS)
      .writeTimeout(20, TimeUnit.SECONDS)
      .addInterceptor(loggingInterceptor)
      .build()
  }

  val retrofit: Retrofit by lazy {
    createRetrofit(BASE_URL, okHttpClient, moshi)
  }

  val service: GeminiApiService by lazy {
    retrofit.create(GeminiApiService::class.java)
  }

  fun createRetrofit(
    baseUrl: String = BASE_URL,
    client: OkHttpClient = okHttpClient,
    moshiInstance: Moshi = moshi
  ): Retrofit {
    return Retrofit.Builder()
      .baseUrl(baseUrl)
      .client(client)
      .addConverterFactory(MoshiConverterFactory.create(moshiInstance))
      .build()
  }

  fun createService(
    baseUrl: String = BASE_URL,
    client: OkHttpClient = okHttpClient
  ): GeminiApiService {
    return createRetrofit(baseUrl, client, moshi).create(GeminiApiService::class.java)
  }
}
