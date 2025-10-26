package com.ahmedProjects.captionscraperapp.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmedProjects.captionscraperapp.R
import com.ahmedProjects.captionscraperapp.model.CaptionResponse
import com.ahmedProjects.captionscraperapp.model.PostItem
import com.ahmedProjects.captionscraperapp.presenter.CaptionPresenter
import com.ahmedProjects.captionscraperapp.presenter.CaptionView
import com.ahmedProjects.captionscraperapp.view.adapter.PostAdapter
import com.google.android.material.button.MaterialButton
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity(), CaptionView {

    private lateinit var presenter: CaptionPresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchButton: MaterialButton
    private lateinit var usernameInput: EditText
    private lateinit var numPostsInput: EditText
    private lateinit var keywordInput: EditText
    private lateinit var resultsLayout: RelativeLayout

    private val posts = mutableListOf<PostItem>()
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        // Initialize UI elements
        usernameInput = findViewById(R.id.etUsername)
        numPostsInput = findViewById(R.id.etNumPosts)
        keywordInput = findViewById(R.id.etKeyword) // If you have a separate search button, use that ID
        searchButton = findViewById(R.id.btnSearch) // If you have a separate search button, use that ID
        recyclerView = findViewById(R.id.recyclerView)
        resultsLayout = findViewById(R.id.recyclerContainer)
        resultsLayout.visibility = View.GONE

        presenter = CaptionPresenter(this)

        adapter = PostAdapter(posts) { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Handle search click
        searchButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val keyword = keywordInput.text.toString().trim().lowercase()
            val maxPosts = numPostsInput.text.toString().toIntOrNull() ?: 50

            if (username.isEmpty()) {
                Toast.makeText(this, "Enter username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (keyword.isEmpty()) {
                Toast.makeText(this, "Enter a keyword", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            presenter.fetchCaptions(username, maxPosts, keyword)
        }
    }

    override fun onLoading(show: Boolean) {
        // optional: implement a progress bar
    }

    override fun onResultSuccess(data: CaptionResponse) {
        posts.clear()
        posts.addAll(data.posts)
        adapter.notifyDataSetChanged()
        resultsLayout.visibility = if (posts.isNotEmpty()) View.VISIBLE else View.GONE
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun testBackendConnection() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.1.13:8000/ping")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { println(response.body?.string()) }
            }
        })
    }
}
