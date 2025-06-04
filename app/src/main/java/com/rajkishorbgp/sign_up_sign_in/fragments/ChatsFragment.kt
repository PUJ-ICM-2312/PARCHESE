package com.rajkishorbgp.sign_up_sign_in.fragments

import Chat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rajkishorbgp.sign_up_sign_in.ChatsAdapter
import com.rajkishorbgp.sign_up_sign_in.databinding.FragmentChatsBinding

class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private val chats = mutableListOf<Pair<String, Chat>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatsRef = FirebaseDatabase.getInstance().getReference("chats")

        chats.clear()
        val adapter = ChatsAdapter(chats) { chatId ->
            val action = ChatsFragmentDirections.actionChatsFragmentToChatDetailFragment(chatId)
            findNavController().navigate(action)
        }
        binding.recyclerView.adapter = adapter

        chatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chats.clear()
                for (chatSnap in snapshot.children) {
                    val chat = chatSnap.getValue(Chat::class.java) ?: continue
                    if (chat.usuarios.contains(userId)) {
                        chats.add(chatSnap.key!! to chat)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error cargando chats: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
