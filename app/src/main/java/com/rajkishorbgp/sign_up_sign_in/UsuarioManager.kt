package com.rajkishorbgp.sign_up_sign_in

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

object UsuarioManager {

    var usuarioActual: Usuario? = null
        private set

    fun cargarUsuario(onComplete: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete(false)

        val ref = FirebaseDatabase.getInstance("https://parchese-eda2d-default-rtdb.firebaseio.com/")
            .getReference("usuarios")
            .child(userId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usuarioActual = snapshot.getValue(Usuario::class.java)
                onComplete(true)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false)
            }
        })
    }
}
