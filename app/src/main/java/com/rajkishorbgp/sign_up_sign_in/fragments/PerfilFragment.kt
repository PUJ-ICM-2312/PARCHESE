package com.rajkishorbgp.sign_up_sign_in.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rajkishorbgp.sign_up_sign_in.R
import com.rajkishorbgp.sign_up_sign_in.Usuario
import com.rajkishorbgp.sign_up_sign_in.databinding.FragmentPerfilBinding

class PerfilFragment : Fragment() {
    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance("https://parchese-eda2d-default-rtdb.firebaseio.com/")
        .getReference("usuarios")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarDatosUsuario()
        configurarCerrarSesion()
        binding.btnEditarPerfil.setOnClickListener { mostrarDialogoEditarPerfil() }
    }

    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return
        dbRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuario = snapshot.getValue(Usuario::class.java)
                usuario?.let {
                    binding.tvNombre.text = it.nombre
                    binding.tvUsuario.text = "@${it.usuario}"
                    binding.tvDescripcion.text = it.descripcion ?: ""
                    binding.tvUbicacion.text = "Bogotá, Colombia" // Puedes hacerlo dinámico luego
                    binding.tvFechaUnion.text = "Se unió en Marzo 2025"

                    // Mostrar intereses dinámicos
                    binding.flexboxIntereses.removeAllViews()
                    it.intereses?.forEach { interes ->
                        val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                            text = interes
                            isClickable = false
                        }
                        binding.flexboxIntereses.addView(chip)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarDialogoEditarPerfil() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputNombre = EditText(context).apply {
            hint = "Nombre"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        val inputUsuario = EditText(context).apply {
            hint = "@usuario"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        val inputDescripcion = EditText(context).apply {
            hint = "Descripción"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        val inputIntereses = EditText(context).apply {
            hint = "Intereses separados por coma (máx. 5)"
        }

        layout.addView(inputNombre)
        layout.addView(inputUsuario)
        layout.addView(inputDescripcion)
        layout.addView(inputIntereses)

        AlertDialog.Builder(context)
            .setTitle("Editar Perfil")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val usuario = inputUsuario.text.toString().trim().removePrefix("@")
                val descripcion = inputDescripcion.text.toString().trim()
                val intereses = inputIntereses.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(5)

                val uid = auth.currentUser?.uid ?: return@setPositiveButton
                val updates = mapOf(
                    "nombre" to nombre,
                    "usuario" to usuario,
                    "descripcion" to descripcion,
                    "intereses" to intereses
                )

                dbRef.child(uid).updateChildren(updates).addOnSuccessListener {
                    Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    cargarDatosUsuario()
                }.addOnFailureListener {
                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun configurarCerrarSesion() {
        binding.btnCerrarSesion.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_signInActivity)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
