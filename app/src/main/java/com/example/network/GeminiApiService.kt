package com.example.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface defining Gemini API endpoints for generating
 * natural-language design system configurations.
 */
interface GeminiApiService {

  @POST("v1beta/models/{model}:generateContent")
  suspend fun generateContent(
    @Path("model") model: String,
    @Query("key") apiKey: String,
    @Body request: GeminiRequest
  ): Response<GeminiResponse>

  @POST("v1beta/models/gemini-2.5-flash:generateContent")
  suspend fun generateDesignConfig(
    @Query("key") apiKey: String,
    @Body request: GeminiRequest
  ): Response<GeminiResponse>
}
