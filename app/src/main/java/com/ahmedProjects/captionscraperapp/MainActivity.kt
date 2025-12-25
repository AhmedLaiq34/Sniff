package com.ahmedProjects.captionscraperapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.ahmedProjects.captionscraperapp.R
import com.ahmedProjects.captionscraperapp.model.CaptionResponse
import com.ahmedProjects.captionscraperapp.presenter.CaptionPresenter
import com.ahmedProjects.captionscraperapp.presenter.CaptionView
import android.widget.ProgressBar
import android.widget.LinearLayout

class MainActivity : AppCompatActivity(), CaptionView {

    private lateinit var presenter: CaptionPresenter
    private lateinit var searchButton: AppCompatButton
    private lateinit var usernameInput: EditText
    private lateinit var keywordInput: EditText
    private lateinit var progressBar: ProgressBar

    // Buttons for post count selection
    private lateinit var btn10: AppCompatButton
    private lateinit var btn25: AppCompatButton
    private lateinit var btn50: AppCompatButton
    private lateinit var btn100: AppCompatButton

    private var selectedPostCount = 25 // Default selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Initialize UI elements
        usernameInput = findViewById(R.id.etUsername)
        keywordInput = findViewById(R.id.etKeyword)
        searchButton = findViewById(R.id.btnSearch)

        // Initialize post count buttons (you'll need to add IDs to your XML)
        btn10 = findViewById(R.id.btn10)
        btn25 = findViewById(R.id.btn25)
        btn50 = findViewById(R.id.btn50)
        btn100 = findViewById(R.id.btn100)

        presenter = CaptionPresenter(this)

        // Set up post count button listeners
        setupPostCountButtons()

        // Handle search click
        searchButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val keyword = keywordInput.text.toString().trim().lowercase()

            if (username.isEmpty()) {
                Toast.makeText(this, "Enter username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (keyword.isEmpty()) {
                Toast.makeText(this, "Enter a keyword", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            presenter.fetchCaptions(username, selectedPostCount, keyword)
        }
    }

    private fun setupPostCountButtons() {

        btn10.setOnClickListener {
            selectPostCount(10, btn10)

        }

        btn25.setOnClickListener {
            selectPostCount(25, btn25)
        }

        btn50.setOnClickListener {
            selectPostCount(50, btn50)
        }

        btn100.setOnClickListener {
            selectPostCount(100, btn100)
        }
    }

    private fun selectPostCount(count: Int, selectedButton: AppCompatButton) {
        // Deselect all buttons
        btn10.isSelected = false
        btn25.isSelected = false
        btn50.isSelected = false
        btn100.isSelected = false

        // Select the clicked button
        selectedButton.isSelected = true
        selectedPostCount = count
    }

    override fun onLoading(show: Boolean) {
        runOnUiThread {
            // Show/hide loading indicator if you add a ProgressBar to activity_main.xml
            searchButton.isEnabled = !show
            searchButton.text = if (show) "Sniffing..." else "Sniff!"
        }
    }

    override fun onResultSuccess(data: CaptionResponse) {
        runOnUiThread {
            val username = usernameInput.text.toString().trim()

            // Set the username for each post
            data.posts.forEach { post ->
                post.username = username
            }

            // Navigate to ResultsActivity with the data
            val intent = Intent(this, ResultsActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("keyword", keywordInput.text.toString())
            intent.putParcelableArrayListExtra("posts", ArrayList(data.posts))
            startActivity(intent)
        }
    }


    override fun onError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}