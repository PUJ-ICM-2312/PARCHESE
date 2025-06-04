package com.rajkishorbgp.sign_up_sign_in

import Chat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.rajkishorbgp.sign_up_sign_in.databinding.ItemChatBinding

class ChatsAdapter(
    private val chats: List<Pair<String, Chat>>,
    private val onChatClick: (String) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatId: String, chat: Chat) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val otherUserId = chat.usuarios.firstOrNull { it != currentUserId } ?: "Desconocido"

            binding.tvChatName.text = "Usuario: $otherUserId"
            binding.tvLastMessage.text = chat.ultimoMensaje

            binding.root.setOnClickListener {
                onChatClick(chatId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun getItemCount(): Int = chats.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val (chatId, chat) = chats[position]
        holder.bind(chatId, chat)
    }
}
