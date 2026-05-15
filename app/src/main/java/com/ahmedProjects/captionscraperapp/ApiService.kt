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

// ----- LLM Summarization Models -----
data class PostSummaryItem(
    val caption: String?,
    val url: String,
    val timestamp: String?
)

data class SummarizeRequest(
    val username: String,
    val keyword: String,
    val posts: List<PostSummaryItem>
)

data class SummarizeResponse(
    val status: String,
    val summary: String
)

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/api/scrape_instagram")
    fun searchCaptions(@Body request: SearchRequest): Call<CaptionResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/summarize")
    fun summarizeResults(@Body request: SummarizeRequest): Call<SummarizeResponse>
}
