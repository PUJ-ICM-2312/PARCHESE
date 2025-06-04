package com.rajkishorbgp.sign_up_sign_in

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.rajkishorbgp.sign_up_sign_in.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginTab.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        binding.signupButton.setOnClickListener {
            val name = binding.signupName.text.toString()
            val email = binding.signupEmail.text.toString()
            val password = binding.signupPassword.text.toString()
            val confirmPassword = binding.signupConfirm.text.toString()
            val usuario = binding.signupUsuario.text.toString()
            val descripcion = binding.signupDescripcion.text.toString()

            val intereses = getSelectedIntereses()

            if (intereses.size > 5) {
                Toast.makeText(this, "Máximo 5 intereses", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() &&
                usuario.isNotEmpty() && descripcion.isNotEmpty() && password == confirmPassword) {

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            user?.let {
                                val userId = it.uid
                                val usuarioObj = Usuario(
                                    id = userId,
                                    nombre = name,
                                    email = email,
                                    usuario = usuario,
                                    descripcion = descripcion,
                                    intereses = intereses
                                )

                                FirebaseDatabase.getInstance("https://parchese-eda2d-default-rtdb.firebaseio.com/")
                                    .getReference("usuarios")
                                    .child(userId)
                                    .setValue(usuarioObj)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, SignInActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error al guardar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

            } else {
                val errorMessage = when {
                    password != confirmPassword -> "Las contraseñas no coinciden"
                    else -> "Completa todos los campos"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginRedirectText.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun getSelectedIntereses(): List<String> {
        val intereses = mutableListOf<String>()
        val checkBoxIds = listOf(
            binding.interes1,
            binding.interes2,
            binding.interes3,
            binding.interes4,
            binding.interes5,
            binding.interes6
        )
        for (checkBox in checkBoxIds) {
            if (checkBox.isChecked) intereses.add(checkBox.text.toString())
        }
        return intereses
    }
}
