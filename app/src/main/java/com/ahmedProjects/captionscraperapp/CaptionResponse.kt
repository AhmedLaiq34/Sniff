package com.ahmedProjects.captionscraperapp.model

data class CaptionResponse(
    val status: String,
    val posts: List<PostItem>
)

data class PostItem(
    val url: String,
    val caption: String?,
    val timestamp: String?
)