package com.rajkishorbgp.sign_up_sign_in.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.rajkishorbgp.sign_up_sign_in.R
import com.rajkishorbgp.sign_up_sign_in.databinding.FragmentMapaBinding

class MapaFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapaBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Configurar mapa quemado (Bogotá como ejemplo)
        val bogota = LatLng(4.7109, -74.0721)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bogota, 12f))

        // Añadir marcadores ficticios
        addMarker(LatLng(4.711, -74.072), "Adventure Hike", "Sierra Nevada")
        addMarker(LatLng(4.698, -74.083), "Art Gallery Tour", "Centro Cultural")
        addMarker(LatLng(4.705, -74.051), "Beach Yoga", "Parque Simón Bolívar")
    }

    private fun addMarker(position: LatLng, title: String, snippet: String) {
        map.addMarker(
            MarkerOptions()
                .position(position)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}