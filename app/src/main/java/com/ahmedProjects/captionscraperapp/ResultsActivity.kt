package com.ahmedProjects.captionscraperapp.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmedProjects.captionscraperapp.R
import com.ahmedProjects.captionscraperapp.model.PostItem
import com.ahmedProjects.captionscraperapp.view.adapter.PostAdapter

class ResultsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageView
    private val posts = mutableListOf<PostItem>()
    private lateinit var adapter: PostAdapter
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Initialize UI elements
        recyclerView = findViewById(R.id.recyclerView)
        backButton = findViewById(R.id.btnBack)
        username = intent.getStringExtra("username")

        // Get data from intent
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
    }
}