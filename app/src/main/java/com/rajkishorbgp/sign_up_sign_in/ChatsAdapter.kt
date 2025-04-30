import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rajkishorbgp.sign_up_sign_in.R

class ChatsAdapter(
    private val chats: List<Chat>,
    private val onChatClicked: (Chat) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvChatName: TextView = view.findViewById(R.id.tvChatName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val ivProfile: ImageView = view.findViewById(R.id.ivProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.tvChatName.text = chat.name
        holder.tvLastMessage.text = chat.lastMessage
        holder.tvTime.text = chat.time

        // Cargar imagen con Glide/Picasso
        Glide.with(holder.itemView.context)
            .load(chat.profileImage)
            .circleCrop()
            .into(holder.ivProfile)

        holder.itemView.setOnClickListener { onChatClicked(chat) }
    }

    override fun getItemCount() = chats.size
}