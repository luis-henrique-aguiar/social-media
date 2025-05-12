package br.edu.ifsp.dmo2.redesocial.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo2.redesocial.databinding.PostItemBinding
import br.edu.ifsp.dmo2.redesocial.model.Post

class PostAdapter : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private val posts: MutableList<Post> = mutableListOf()

    fun updatePosts(newPosts: List<Post>) {
        val startPosition = posts.size
        posts.addAll(newPosts)
        notifyItemRangeInserted(startPosition, newPosts.size)
    }

    fun clearAndAddPosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            if (post.description.isNotBlank()) {
                binding.postDescription.text = post.description
                binding.postDescription.visibility = View.VISIBLE
            } else {
                binding.postDescription.visibility = View.GONE
            }
            if (post.photo != null) {
                binding.postImage.setImageBitmap(post.photo)
                binding.postImage.visibility = View.VISIBLE
            } else {
                binding.postImage.visibility = View.GONE
            }
            binding.fullName.text = post.fullName
            binding.profileImage.setImageBitmap(post.userProfilePhoto)
            binding.locale.text = post.location ?: "@${post.username}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}