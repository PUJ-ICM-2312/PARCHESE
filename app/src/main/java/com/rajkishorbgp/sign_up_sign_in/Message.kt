package com.rajkishorbgp.sign_up_sign_in

data class Message(
    val text: String = "",
    val timestamp: String = "",
    val isSent: Boolean = false,
    val remitenteId: String = ""
)
