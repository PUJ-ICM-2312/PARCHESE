package com.rajkishorbgp.sign_up_sign_in.fragments

import Evento
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.rajkishorbgp.sign_up_sign_in.R
import com.rajkishorbgp.sign_up_sign_in.databinding.FragmentCrearBinding
import java.util.Calendar
import java.util.UUID

class CrearFragment : Fragment() {
    private var _binding: FragmentCrearBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private val database = FirebaseDatabase.getInstance("https://computacionmovil-d442b-default-rtdb.firebaseio.com/")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearBinding.inflate(inflater, container, false)
        return binding.root
    }
    private var selectedLocation: String = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()

        parentFragmentManager.setFragmentResultListener("ubicacionRequestKey", viewLifecycleOwner) { _, bundle ->
            val ubicacion = bundle.getString("ubicacion")
            Log.d("CrearFragment", "Resultado recibido: $ubicacion")
            if (ubicacion != null) {
                binding.btnSeleccionarUbicacion.text = ubicacion
                selectedLocation = ubicacion
                Toast.makeText(requireContext(), "Ubicación seleccionada: $ubicacion", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("CrearFragment", "Ubicación no encontrada en el resultado del fragmento")
            }
        }

    }



    private fun setupListeners() {
        binding.btnSeleccionarImagen.setOnClickListener {
            openImagePicker()
        }

        binding.btnSeleccionarFecha.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnSeleccionarHora.setOnClickListener {
            showTimePickerDialog()
        }

        binding.btnSeleccionarUbicacion.setOnClickListener {
            openLocationPicker()
        }

        binding.btnCrearEvento.setOnClickListener {
            if (validateForm()) {
                createEvent()
            }
        }
    }

    private fun openImagePicker() {
        // Implementar selección de imagen
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate = "$day/${month + 1}/$year"
                binding.btnSeleccionarFecha.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                binding.btnSeleccionarHora.text = selectedTime
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun openLocationPicker() {
        findNavController().navigate(R.id.action_crearFragment_to_mapFragment)
    }



    private fun validateForm(): Boolean {
        return when {
            binding.etTitulo.text.isNullOrEmpty() -> {
                binding.etTitulo.error = "Ingresa un título"
                false
            }
            binding.etDescripcion.text.isNullOrEmpty() -> {
                binding.etDescripcion.error = "Ingresa una descripción"
                false
            }
            selectedDate.isEmpty() -> {
                Toast.makeText(context, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                false
            }
            selectedTime.isEmpty() -> {
                Toast.makeText(context, "Selecciona una hora", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun createEvent() {

        val nuevoEvento = Evento(
            titulo = binding.etTitulo.text.toString(),
            descripcion = binding.etDescripcion.text.toString(),
            fecha = selectedDate,
            hora = selectedTime,
            imagenUri = selectedImageUri.toString(),
            ubicacion = selectedLocation
        )


        val eventosRef = database.reference.child("eventos")

        // Usar un ID único para el nuevo evento
        val eventoId = UUID.randomUUID().toString()

        // Guardar el evento en Firebase usando el ID único
        eventosRef.child(eventoId).setValue(nuevoEvento)
            .addOnSuccessListener {
                Toast.makeText(context, "Evento creado exitosamente!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al crear evento: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("CrearFragment", "Error al crear evento en Firebase", e)
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}