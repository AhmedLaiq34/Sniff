package com.ahmedProjects.captionscraperapp.presenter

import com.ahmedProjects.captionscraperapp.model.CaptionResponse
import com.ahmedProjects.captionscraperapp.network.RetrofitClient
import com.ahmedProjects.captionscraperapp.network.SearchRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface CaptionView {
    fun onLoading(show: Boolean)
    fun onResultSuccess(data: CaptionResponse)
    fun onError(message: String)
}

class CaptionPresenter(private val view: CaptionView) {

    fun fetchCaptions(username: String, maxPosts: Int, keyword: String) {
        view.onLoading(true)

        val request = SearchRequest(username = username, max_posts = maxPosts, phrase = keyword)

        RetrofitClient.instance.searchCaptions(request).enqueue(object : retrofit2.Callback<com.ahmedProjects.captionscraperapp.model.CaptionResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.ahmedProjects.captionscraperapp.model.CaptionResponse>,
                response: retrofit2.Response<com.ahmedProjects.captionscraperapp.model.CaptionResponse>
            ) {
                view.onLoading(false)
                if (response.isSuccessful && response.body() != null) {
                    // Filter posts containing the keyword
                    val filteredPosts = response.body()!!.posts.filter {
                        it.caption?.lowercase()?.contains(keyword) == true
                    }
                    view.onResultSuccess(com.ahmedProjects.captionscraperapp.model.CaptionResponse(
                        status = response.body()!!.status,
                        posts = filteredPosts
                    ))
                } else {
                    view.onError("Unexpected response")
                }
            }

            override fun onFailure(
                call: retrofit2.Call<com.ahmedProjects.captionscraperapp.model.CaptionResponse>,
                t: Throwable
            ) {
                view.onLoading(false)
                view.onError("Network error: ${t.localizedMessage}")
            }
        })
    }
}
