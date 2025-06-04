package com.rajkishorbgp.sign_up_sign_in.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rajkishorbgp.sign_up_sign_in.MessagesAdapter
import com.rajkishorbgp.sign_up_sign_in.databinding.FragmentChatDetailBinding

data class Mensaje(
    val text: String = "",
    val timestamp: String = "",
    val remitenteId: String = "",
    var isSent: Boolean = false
)

class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MessagesAdapter
    private val messages = mutableListOf<Mensaje>()

    private lateinit var database: DatabaseReference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var chatId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MessagesAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = adapter

        chatId = arguments?.getString("chatId")

        database = FirebaseDatabase.getInstance("https://parchese-eda2d-default-rtdb.firebaseio.com/")
            .reference.child("chats").child(chatId ?: "")

        escucharMensajes()

        binding.btnSend.setOnClickListener {
            val texto = binding.etMessage.text.toString().trim()
            if (texto.isNotEmpty() && currentUserId != null) {
                enviarMensaje(texto)
                binding.etMessage.setText("")
            }
        }
    }

    private fun escucharMensajes() {
        database.child("mensajes").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val mensaje = snapshot.getValue(Mensaje::class.java)
                mensaje?.let {
                    it.isSent = it.remitenteId == currentUserId
                    messages.add(it)
                    adapter.notifyItemInserted(messages.size - 1)
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        })
    }

    private fun enviarMensaje(texto: String) {
        val mensajeRef = database.child("mensajes").push()
        val mensaje = Mensaje(
            text = texto,
            timestamp = System.currentTimeMillis().toString(),
            remitenteId = currentUserId ?: "",
            isSent = true
        )
        mensajeRef.setValue(mensaje)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
