package com.ahmedProjects.captionscraperapp.model

data class CaptionResponse(
    val status: String,
    val posts: List<PostItem>
)