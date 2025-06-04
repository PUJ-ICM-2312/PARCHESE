package com.rajkishorbgp.sign_up_sign_in

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val descripcion: String = "",
    val usuario: String = "",
    val intereses: List<String> = emptyList()
)