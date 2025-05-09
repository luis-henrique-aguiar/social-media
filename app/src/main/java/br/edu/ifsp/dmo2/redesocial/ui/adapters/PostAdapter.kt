package br.edu.ifsp.dmo2.redesocial.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo2.redesocial.databinding.PostItemBinding
import br.edu.ifsp.dmo2.redesocial.model.Post

class PostAdapter : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private val posts: MutableList<Post> = mutableListOf()

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.postDescription.text = post.description
            binding.postImage.setImageBitmap(post.photo)
            binding.fullName.text = post.fullName
            binding.profileImage.setImageBitmap(post.userProfilePhoto)
            binding.locale.text = post.location ?: ""
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