package com.rajkishorbgp.sign_up_sign_in.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.rajkishorbgp.sign_up_sign_in.R
import com.rajkishorbgp.sign_up_sign_in.databinding.FragmentMapaBinding
import kotlin.math.*
import com.android.volley.Request
import java.text.SimpleDateFormat
import java.util.*

class MapaFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapaBinding? = null
    private val binding get() = _binding!!
    private var currentRoute: Polyline? = null
    private var currentDestinationMarker: Marker? = null
    private var currentLocation: LatLng? = null

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentCircle: Circle? = null
    private var currentMarkers: MutableList<Marker> = mutableListOf()
    private var ubicacionInicialMostrada = false

    private val database = FirebaseDatabase.getInstance("https://computacionmovil-d442b-default-rtdb.firebaseio.com/")

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val bogota = LatLng(4.7109, -74.0721)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bogota, 12f))

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true

            val locationRequest = LocationRequest.create().apply {
                interval = 5000
                fastestInterval = 3000
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {


                    val location = result.lastLocation ?: return
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    currentLocation = currentLatLng
                    if (!ubicacionInicialMostrada) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13f))
                        ubicacionInicialMostrada = true
                    }

                    actualizarCirculo(currentLatLng)
                    cargarEventosCercanos(currentLatLng, 10.0)
                }

            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, requireActivity().mainLooper
            )
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }

        cargarEventosCercanos(bogota, 10.0)
        map.setOnMarkerClickListener { marker ->
            currentDestinationMarker = marker
            marker.showInfoWindow()
            mostrarOpcionesDeRuta(marker)
            true
        }


    }
    private fun mostrarOpcionesDeRuta(marker: Marker) {
        binding.botonIr.visibility = View.VISIBLE
        binding.botonCancelar.visibility = View.VISIBLE

        binding.botonIr.setOnClickListener {
            currentLocation?.let { origin ->
                obtenerRutaConDirectionsAPI(origin, marker.position)
            }
        }

        binding.botonCancelar.setOnClickListener {
            currentRoute?.remove()
            currentRoute = null
            currentDestinationMarker = null
            binding.botonIr.visibility = View.GONE
            binding.botonCancelar.visibility = View.GONE
        }
    }
    private fun obtenerRutaConDirectionsAPI(origen: LatLng, destino: LatLng) {

        val url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248a35e3fea095e4a20b71c85caebfd261a&start=${origen.longitude},${origen.latitude}&end=${destino.longitude},${destino.latitude}"

        val requestQueue = Volley.newRequestQueue(requireContext())
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->

                currentDestinationMarker?.remove()

                val routesArray = response.optJSONArray("features")
                if (routesArray != null && routesArray.length() > 0) {
                    val ruta = routesArray.getJSONObject(0)
                    val geometry = ruta.getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")

                    val puntos = mutableListOf<LatLng>()
                    for (i in 0 until coordinates.length()) {
                        val coord = coordinates.getJSONArray(i)
                        val lon = coord.getDouble(0)
                        val lat = coord.getDouble(1)
                        puntos.add(LatLng(lat, lon))
                    }

                    currentRoute?.remove()
                    val polylineOptions = PolylineOptions()
                        .addAll(puntos)
                        .color(0xFF3B5998.toInt())
                        .width(10f)

                    currentRoute = map.addPolyline(polylineOptions)


                    binding.botonIr.visibility = View.GONE
                    binding.botonCancelar.visibility = View.VISIBLE


                    currentDestinationMarker = map.addMarker(MarkerOptions().position(destino))
                } else {

                    Toast.makeText(requireContext(), "No se encontró una ruta válida.", Toast.LENGTH_SHORT).show()
                    Log.e("API_RESPONSE", "Respuesta sin rutas: $response")
                }
            },
            { error ->
                Log.e("API_ERROR", "Error obteniendo la ruta: ${error.message}")
            })

        requestQueue.add(request)
    }



    private fun actualizarCirculo(center: LatLng) {
        currentCircle?.remove()
        currentCircle = map.addCircle(
            CircleOptions()
                .center(center)
                .radius(10000.0)
                .strokeColor(0x5500AAFF)
                .fillColor(0x2200AAFF)
                .strokeWidth(2f)
        )
    }

    private fun cargarEventosCercanos(center: LatLng, radioKm: Double) {
        val eventosRef = database.getReference("eventos")

        currentMarkers.forEach { it.remove() }
        currentMarkers.clear()

        eventosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventosCercanos = mutableListOf<String>()

                val currentDate = Date()

                for (eventoSnapshot in snapshot.children) {
                    val ubicacionStr = eventoSnapshot.child("ubicacion").getValue(String::class.java)
                    val titulo = eventoSnapshot.child("titulo").getValue(String::class.java)
                    val descripcion = eventoSnapshot.child("descripcion").getValue(String::class.java)
                    val fechaStr = eventoSnapshot.child("fecha").getValue(String::class.java)
                    val horaStr = eventoSnapshot.child("hora").getValue(String::class.java)

                    if (ubicacionStr != null && titulo != null && descripcion != null && fechaStr != null && horaStr != null) {
                        val fechaHoraStr = "$fechaStr $horaStr"
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        try {
                            val eventoDate = dateFormat.parse(fechaHoraStr)

                            if (eventoDate != null && eventoDate.after(currentDate)) {
                                val parts = ubicacionStr.split(",")
                                if (parts.size == 2) {
                                    val lat = parts[0].toDoubleOrNull()
                                    val lon = parts[1].toDoubleOrNull()
                                    if (lat != null && lon != null) {
                                        val location = LatLng(lat, lon)
                                        val distancia = calcularDistancia(center, location)
                                        if (distancia <= radioKm) {
                                            eventosCercanos.add(titulo)

                                            val marker = map.addMarker(
                                                MarkerOptions()
                                                    .position(location)
                                                    .title(titulo)
                                                    .snippet(descripcion)
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                                            )
                                            marker?.let { currentMarkers.add(it) }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FechaError", "Error al parsear la fecha: $fechaHoraStr")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer eventos: ${error.message}")
            }
        })
    }


    private fun calcularDistancia(p1: LatLng, p2: LatLng): Double {
        val R = 6371.0
        val lat1 = Math.toRadians(p1.latitude)
        val lon1 = Math.toRadians(p1.longitude)
        val lat2 = Math.toRadians(p2.latitude)
        val lon2 = Math.toRadians(p2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }


    override fun onDestroyView() {
        super.onDestroyView()
        ubicacionInicialMostrada = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _binding = null
    }
}
