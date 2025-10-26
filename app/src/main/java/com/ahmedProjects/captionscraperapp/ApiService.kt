package com.ahmedProjects.captionscraperapp.network

import com.ahmedProjects.captionscraperapp.model.CaptionResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class SearchRequest(
    val username: String,
    val max_posts: Int,
    val phrase: String
)


interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/api/scrape_instagram")
    fun searchCaptions(@Body request: SearchRequest): Call<CaptionResponse>
}
