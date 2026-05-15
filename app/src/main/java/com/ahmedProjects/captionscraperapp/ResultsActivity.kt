package com.ahmedProjects.captionscraperapp.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmedProjects.captionscraperapp.R
import com.ahmedProjects.captionscraperapp.model.PostItem
import com.ahmedProjects.captionscraperapp.network.PostSummaryItem
import com.ahmedProjects.captionscraperapp.network.RetrofitClient
import com.ahmedProjects.captionscraperapp.network.SummarizeRequest
import com.ahmedProjects.captionscraperapp.network.SummarizeResponse
import com.ahmedProjects.captionscraperapp.view.adapter.PostAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageView
    private val posts = mutableListOf<PostItem>()
    private lateinit var adapter: PostAdapter
    private var username: String? = null

    // AI Insight views
    private lateinit var insightCard: LinearLayout
    private lateinit var tvInsightSummary: TextView
    private lateinit var insightLoading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Initialize UI elements
        recyclerView = findViewById(R.id.recyclerView)
        backButton = findViewById(R.id.btnBack)
        insightCard = findViewById(R.id.insightCard)
        tvInsightSummary = findViewById(R.id.tvInsightSummary)
        insightLoading = findViewById(R.id.insightLoading)
        username = intent.getStringExtra("username")

        // Get data from intent
        val keyword = intent.getStringExtra("keyword") ?: ""
        val receivedPosts = intent.getParcelableArrayListExtra<PostItem>("posts")

        if (receivedPosts != null) {
            posts.addAll(receivedPosts)
        }

        // Set up RecyclerView
        adapter = PostAdapter(posts) { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Handle back button
        backButton.setOnClickListener {
            finish()
        }

        // Fetch AI insight (non-blocking, fires after UI is ready)
        if (posts.isNotEmpty()) {
            fetchAiInsight(username ?: "", keyword, posts)
        }
    }

    private fun fetchAiInsight(username: String, keyword: String, posts: List<PostItem>) {
        // Show card with loading spinner
        insightCard.visibility = View.VISIBLE
        insightLoading.visibility = View.VISIBLE
        tvInsightSummary.visibility = View.GONE

        val summaryPosts = posts.map { post ->
            PostSummaryItem(
                caption = post.caption,
                url = post.url,
                timestamp = post.timestamp
            )
        }

        val request = SummarizeRequest(
            username = username,
            keyword = keyword,
            posts = summaryPosts
        )

        RetrofitClient.instance.summarizeResults(request)
            .enqueue(object : Callback<SummarizeResponse> {
                override fun onResponse(
                    call: Call<SummarizeResponse>,
                    response: Response<SummarizeResponse>
                ) {
                    runOnUiThread {
                        insightLoading.visibility = View.GONE
                        if (response.isSuccessful && response.body() != null) {
                            tvInsightSummary.text = response.body()!!.summary
                            tvInsightSummary.visibility = View.VISIBLE
                        } else {
                            insightCard.visibility = View.GONE
                        }
                    }
                }

                override fun onFailure(call: Call<SummarizeResponse>, t: Throwable) {
                    runOnUiThread {
                        // Silently hide — don't disrupt the results view
                        insightCard.visibility = View.GONE
                    }
                }
            })
    }
}