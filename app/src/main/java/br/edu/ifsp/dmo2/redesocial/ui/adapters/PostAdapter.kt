package br.edu.ifsp.dmo2.redesocial.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo2.redesocial.R
import br.edu.ifsp.dmo2.redesocial.model.Post

class PostAdapter(private val posts: Array<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPost: ImageView = view.findViewById(R.id.profile_image)
        val txtDescricao: TextView = view.findViewById(R.id.text_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtDescricao.text = posts[position].getDescricao()
        holder.imgPost.setImageBitmap(posts[position].getFoto())
    }
}