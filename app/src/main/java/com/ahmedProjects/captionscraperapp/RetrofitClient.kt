package com.ahmedProjects.captionscraperapp.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.ahmedProjects.captionscraperapp.BuildConfig


object RetrofitClient {
    private const val BASE_URL = BuildConfig.BASE_URL //your backend IP

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.MINUTES) // wait 2 minutes to connect
        .readTimeout(5, TimeUnit.MINUTES)    // wait 5 minutes for data
        .writeTimeout(2, TimeUnit.MINUTES)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
