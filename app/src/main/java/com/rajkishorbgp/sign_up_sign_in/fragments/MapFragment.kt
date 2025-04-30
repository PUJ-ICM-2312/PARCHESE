package com.rajkishorbgp.sign_up_sign_in.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.rajkishorbgp.sign_up_sign_in.R

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var gMap: GoogleMap
    private var selectedLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return view
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map
        val defaultLatLng = LatLng(4.6482837, -74.2478938) // Bogotá

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 12f))

        gMap.setOnMapClickListener { latLng ->
            gMap.clear()
            gMap.addMarker(MarkerOptions().position(latLng))
            selectedLocation = latLng

            setFragmentResult("ubicacionRequestKey", bundleOf("ubicacion" to "${latLng.latitude},${latLng.longitude}"))

            parentFragmentManager.popBackStack()
        }
    }
}
