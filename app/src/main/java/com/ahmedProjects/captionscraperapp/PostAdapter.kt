package com.ahmedProjects.captionscraperapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.ahmedProjects.captionscraperapp.R
import com.ahmedProjects.captionscraperapp.model.PostItem
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PostAdapter(
    private val posts: List<PostItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val captionText: TextView = view.findViewById(R.id.tvCaption)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val openButton: AppCompatButton = view.findViewById(R.id.btnOpenInstagram)
        var username: TextView = view.findViewById(R.id.tvUsername)
        fun bind(item: PostItem) {
            captionText.text = item.caption ?: "No caption"

            username.text = item.username
            // Debug: Log the timestamp to see what format we're receiving
            android.util.Log.d("PostAdapter", "Raw timestamp: ${item.timestamp}")

            // Format timestamp to relative time
            dateText.text = formatRelativeTime(item.timestamp)

            // Handle button click to open Instagram
            openButton.setOnClickListener {
                onItemClick(item.url)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    private fun formatRelativeTime(timestamp: String?): String {
        if (timestamp.isNullOrEmpty()) return "Unknown date"

        return try {
            // Parse the timestamp in ISO 8601 format
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            val postDate = format.parse(timestamp) ?: return "Unknown date"

            // Get current time
            val now = Calendar.getInstance()
            val postCalendar = Calendar.getInstance()
            postCalendar.time = postDate

            // Calculate difference in days (ignoring time)
            val diffMillis = now.timeInMillis - postCalendar.timeInMillis
            val days = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()

            when (days) {
                0 -> "today"
                1 -> "1 day ago"
                else -> "$days days ago"
            }
        } catch (e: Exception) {
            "Unknown date"
        }
    }


    private fun parseTimestamp(timestamp: String): Long {
        return try {
            // Try parsing as Unix timestamp (seconds)
            if (timestamp.all { it.isDigit() }) {
                timestamp.toLong() * 1000 // Convert to milliseconds
            } else {
                // Try parsing ISO 8601 format
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                format.timeZone = TimeZone.getTimeZone("UTC")
                format.parse(timestamp)?.time ?: System.currentTimeMillis()
            }
        } catch (e: Exception) {
            // If all else fails, return current time
            System.currentTimeMillis()
        }
    }
}