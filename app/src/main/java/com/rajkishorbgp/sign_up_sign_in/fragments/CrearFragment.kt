package com.rajkishorbgp.sign_up_sign_in.fragments

import Evento
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.FirebaseDatabase
import com.rajkishorbgp.sign_up_sign_in.R
import com.rajkishorbgp.sign_up_sign_in.databinding.FragmentCrearBinding
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.UUID

class CrearFragment : Fragment() {
    private var _binding: FragmentCrearBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var selectedLocation: String = ""

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_GALLERY = 2

    private val database = FirebaseDatabase.getInstance("https://parchese-eda2d-default-rtdb.firebaseio.com/")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()

        parentFragmentManager.setFragmentResultListener("capturedImage", viewLifecycleOwner) { _, bundle ->
            val uriStr = bundle.getString("capturedImageUri")
            uriStr?.let {
                val uri = Uri.parse(it)
                selectedImageUri = uri
                binding.ivImagenSeleccionada.setImageURI(uri)
                binding.btnSeleccionarImagen.text = getString(R.string.imagen_seleccionada)
            }
        }

        parentFragmentManager.setFragmentResultListener("ubicacionRequestKey", viewLifecycleOwner) { _, bundle ->
            val ubicacion = bundle.getString("ubicacion")
            if (ubicacion != null) {
                selectedLocation = ubicacion
                binding.btnSeleccionarUbicacion.text = getString(R.string.ubicacion_seleccionada, ubicacion)
            }
        }

        selectedImageUri?.let {
            binding.ivImagenSeleccionada.setImageURI(it)
            binding.btnSeleccionarImagen.text = getString(R.string.imagen_seleccionada)
        }

        if (selectedLocation.isNotEmpty()) {
            binding.btnSeleccionarUbicacion.text = getString(R.string.ubicacion_seleccionada, selectedLocation)
        }

        if (selectedDate.isNotEmpty()) {
            binding.btnSeleccionarFecha.text = selectedDate
        }

        if (selectedTime.isNotEmpty()) {
            binding.btnSeleccionarHora.text = selectedTime
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
        val options = arrayOf("Tomar foto", "Seleccionar de galería")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Seleccionar imagen")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> findNavController().navigate(R.id.action_crearFragment_to_cameraFragment)
                1 -> dispatchPickPictureIntent()
            }
        }
        builder.show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(requireContext(), "No se encontró una app de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dispatchPickPictureIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                val imageBitmap = data.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    val uri = saveImageToCache(imageBitmap)
                    selectedImageUri = uri
                    binding.ivImagenSeleccionada.setImageBitmap(imageBitmap)
                    binding.btnSeleccionarImagen.text = getString(R.string.imagen_seleccionada)
                }
            }
            REQUEST_IMAGE_GALLERY -> {
                val uri = data.data
                selectedImageUri = uri
                binding.ivImagenSeleccionada.setImageURI(uri)
                binding.btnSeleccionarImagen.text = getString(R.string.imagen_seleccionada)
            }
        }
    }

    private fun saveImageToCache(bitmap: Bitmap): Uri {
        val imagesFolder = File(requireContext().cacheDir, "images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "selected_image.jpg")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
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
        val eventoId = UUID.randomUUID().toString()

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