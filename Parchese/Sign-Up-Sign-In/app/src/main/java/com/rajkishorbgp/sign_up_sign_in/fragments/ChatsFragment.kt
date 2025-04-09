package com.rajkishorbgp.sign_up_sign_in.fragments

import Chat
import ChatsAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.rajkishorbgp.sign_up_sign_in.databinding.FragmentChatsBinding

class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private val chats = mutableListOf<Chat>()

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

        setupMockData()
    }

    private fun setupMockData() {
        chats.addAll(listOf(
            Chat(
                name = "Club Runners",
                lastMessage = "¡Hola! ¿Estás interesado en el maratón?",
                time = "10:30"
            ),
            Chat(
                name = "Colectivo Artístico",
                lastMessage = "La exposición comienza a las 17:30",
                time = "Ayer"
            ),
            Chat(
                name = "Grupo Senderismo",
                lastMessage = "Recuerda traer agua y protector solar",
                time = "Lun"
            )
        ))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}