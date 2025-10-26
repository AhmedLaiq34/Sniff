package com.ahmedProjects.captionscraperapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahmedProjects.captionscraperapp.R
import com.ahmedProjects.captionscraperapp.model.PostItem

class PostAdapter(
    private val posts: List<PostItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val captionText: TextView = view.findViewById(R.id.captionText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val linkText: TextView = view.findViewById(R.id.linkText)

        fun bind(item: PostItem) {
            captionText.text = item.caption ?: "No caption"
            dateText.text = item.timestamp ?: "Unknown date"
            linkText.text = item.url
            itemView.setOnClickListener { onItemClick(item.url) }
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
}
