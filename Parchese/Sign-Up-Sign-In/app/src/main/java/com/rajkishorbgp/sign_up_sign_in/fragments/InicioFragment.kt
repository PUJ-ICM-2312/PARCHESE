package com.rajkishorbgp.sign_up_sign_in.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rajkishorbgp.sign_up_sign_in.databinding.FragmentInicioBinding

class InicioFragment : Fragment() {
    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActivity()
        setupButtons()
    }

    private fun setupActivity() {
        // Ejemplo de datos estáticos (puedes cambiar por datos reales)
        binding.tvTitle.text = "Senderismo en Montañas"
        binding.tvOrganizer.text = "Organiza: Isabella Martínez"
        binding.tvDate.text = "15 Abr - 10:00 AM"
    }

    private fun setupButtons() {
        binding.btnLike.setOnClickListener {
            showNextActivity("Te gustó esta actividad")
        }

        binding.btnDislike.setOnClickListener {
            showNextActivity("No te interesa esta actividad")
        }
    }

    private fun showNextActivity(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        // Aquí iría la lógica para cargar nueva actividad
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}