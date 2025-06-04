package com.rajkishorbgp.sign_up_sign_in

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
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

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && password == confirmPassword) {
                // Crear cuenta en Firebase
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Mostrar mensaje de éxito y redirigir a la pantalla de inicio de sesión
                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, SignInActivity::class.java))
                            finish()
                        } else {

                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {

                val errorMessage = when {
                    password != confirmPassword -> "Las contraseñas no coinciden"
                    email.isEmpty() || password.isEmpty() || name.isEmpty() -> "Completa todos los campos"
                    else -> "Ocurrió un error al registrar la cuenta"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginRedirectText.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
}
