package com.rajkishorbgp.sign_up_sign_in.fragments

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rajkishorbgp.sign_up_sign_in.databinding.FragmentInicioBinding
import Evento
import kotlin.math.abs

class InicioFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val likesRef = FirebaseDatabase.getInstance("https://parchese-eda2d-default-rtdb.firebaseio.com/").getReference("likes")
    private val chatsRef = FirebaseDatabase.getInstance("https://parchese-eda2d-default-rtdb.firebaseio.com/")
        .getReference("chats")

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private val SHAKE_THRESHOLD = 800f

    private val eventos = mutableListOf<Evento>()
    private var currentIndex = 0
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        database = FirebaseDatabase.getInstance("https://parchese-eda2d-default-rtdb.firebaseio.com/")
            .reference.child("eventos")

        binding.btnLike.setOnClickListener {
            val currentUserId = auth.currentUser?.uid ?: return@setOnClickListener
            val evento = eventos.getOrNull(currentIndex) ?: return@setOnClickListener

            if (currentUserId != null) {
                likesRef.child(evento.id).child(currentUserId).setValue(true).addOnSuccessListener {
                    Toast.makeText(context, "Te gustó esta actividad", Toast.LENGTH_SHORT).show()
                    verificarMatch(evento.id, evento.organizadorId, currentUserId)
                }
            }

            showNextEvent()
        }

        binding.btnDislike.setOnClickListener {
            Toast.makeText(context, "No te interesa esta actividad", Toast.LENGTH_SHORT).show()
            showNextEvent()
        }

        loadEventos()
    }

    private fun verificarMatch(eventoId: String, organizadorId: String, actualUserId: String) {
        Toast.makeText(context, "Verificando match...", Toast.LENGTH_SHORT).show()

        if (organizadorId != actualUserId) {
            crearChatSiNoExiste(organizadorId, actualUserId)
        }
    }

    private fun crearChatSiNoExiste(user1Id: String?, user2Id: String?) {
        Toast.makeText(context, "Intentando crear chat...", Toast.LENGTH_SHORT).show()

        if (user1Id.isNullOrEmpty() || user2Id.isNullOrEmpty()) {
            Toast.makeText(context, "IDs vacíos: $user1Id, $user2Id", Toast.LENGTH_SHORT).show()
            return
        }

        val chatId = if (user1Id < user2Id) "$user1Id-$user2Id" else "$user2Id-$user1Id"

        chatsRef.child(chatId).get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Toast.makeText(context, "Chat no existía, creándolo...", Toast.LENGTH_SHORT).show()

                val chat = mapOf(
                    "usuarios" to listOf(user1Id, user2Id),
                    "ultimoMensaje" to "",
                    "timestamp" to System.currentTimeMillis()
                )
                chatsRef.child(chatId).setValue(chat)
            } else {
                Toast.makeText(context, "El chat ya existía", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error accediendo a chatsRef: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadEventos() {
        val currentUserId = auth.currentUser?.uid ?: return

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventos.clear()
                currentIndex = 0

                val totalEventos = snapshot.childrenCount
                var procesados = 0
                val temporales = mutableListOf<Evento>()

                for (eventoSnapshot in snapshot.children) {
                    val evento = eventoSnapshot.getValue(Evento::class.java)
                    if (evento != null) {
                        val eventoId = evento.id

                        likesRef.child(eventoId).child(currentUserId).get()
                            .addOnSuccessListener { likeSnapshot ->
                                if (!likeSnapshot.exists()) {
                                    temporales.add(evento)
                                }

                                procesados++
                                if (procesados == totalEventos.toInt()) {
                                    eventos.addAll(temporales)
                                    if (eventos.isNotEmpty()) {
                                        showEvento(eventos[currentIndex])
                                    } else {
                                        mostrarSinEventos()
                                    }
                                }
                            }
                            .addOnFailureListener {
                                procesados++
                                if (procesados == totalEventos.toInt() && eventos.isEmpty()) {
                                    mostrarSinEventos()
                                }
                            }
                    } else {
                        procesados++
                        if (procesados == totalEventos.toInt() && eventos.isEmpty()) {
                            mostrarSinEventos()
                        }
                    }
                }

                if (totalEventos == 0L) {
                    mostrarSinEventos()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error cargando eventos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarSinEventos() {
        binding.tvTitle.text = "No hay nuevas actividades"
        binding.tvOrganizer.text = ""
        binding.tvDate.text = ""
        binding.ivActivityImage.setImageResource(android.R.color.transparent)
    }

    private fun showEvento(evento: Evento) {
        binding.tvTitle.text = evento.titulo
        binding.tvOrganizer.text = "Organiza: ${evento.descripcion}"
        binding.tvDate.text = "${evento.fecha} - ${evento.hora}"
        Glide.with(this).load(evento.imagenUri).into(binding.ivActivityImage)
    }

    private fun showNextEvent() {
        currentIndex++
        if (currentIndex < eventos.size) {
            showEvento(eventos[currentIndex])
        } else {
            Toast.makeText(context, "No hay más actividades", Toast.LENGTH_SHORT).show()
            mostrarSinEventos()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val curTime = System.currentTimeMillis()
            if ((curTime - lastUpdate) > 100) {
                val diffTime = curTime - lastUpdate
                lastUpdate = curTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

                if (speed > SHAKE_THRESHOLD) {
                    Toast.makeText(context, "¡Match hecho con agitación!", Toast.LENGTH_SHORT).show()
                    showNextEvent()
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager.unregisterListener(this)
        _binding = null
    }
}
