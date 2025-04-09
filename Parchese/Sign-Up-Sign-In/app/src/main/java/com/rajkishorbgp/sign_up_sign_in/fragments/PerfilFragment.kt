package com.rajkishorbgp.sign_up_sign_in.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.rajkishorbgp.sign_up_sign_in.R
import com.rajkishorbgp.sign_up_sign_in.databinding.FragmentPerfilBinding

class PerfilFragment : Fragment() {
    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun cargarDatosUsuario() {
        // Los TextView deben coincidir con los IDs del XML
        binding.tvNombre.text = "Carlos Rodríguez"
        binding.tvUsuario.text = "@carlos_r"
        binding.tvDescripcion.text = "Entusiasta de deportes al aire libre y actividades culturales. Me encanta conocer gente nueva y participar en eventos comunitarios."
        binding.tvUbicacion.text = "Bogotá, Colombia"
        binding.tvFechaUnion.text = "Se unió en Marzo 2025"

        // Eliminar la configuración dinámica de intereses
        // (ya están definidos en el XML)
    }

    private fun configurarCerrarSesion() {
        binding.btnCerrarSesion.setOnClickListener {
            // Verificar que esta acción exista en nav_graph.xml
            findNavController().navigate(R.id.action_perfilFragment_to_signInActivity)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}