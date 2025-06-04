package com.rajkishorbgp.sign_up_sign_in

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.rajkishorbgp.sign_up_sign_in.databinding.ActivitySignInBinding
import java.util.concurrent.Executor

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth

    // SharedPreferences para almacenar si el usuario activó huella
    private lateinit var prefs: SharedPreferences
    private val PREFS_NAME = "prefs_login"
    private val KEY_HUELLA_ACTIVADA = "huella_activada"
    private val KEY_USUARIO_GUARDADO = "usuario_guardado"

    // Componentes de BiometricPrompt
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricExecutor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // ──── 1) Configurar BiometricPrompt ────
        biometricExecutor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this@SignInActivity,
            biometricExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        this@SignInActivity,
                        "Error autenticación: $errString",
                        Toast.LENGTH_SHORT
                    ).show()
                    mostrarFormulario()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    irAPantallaPrincipal()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        this@SignInActivity,
                        "Huella no reconocida. Intenta de nuevo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        // ──── 2) Ajustar PromptInfo SIN setNegativeButtonText ────
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Iniciar sesión con huella")
            .setSubtitle("Coloca tu dedo en el sensor para autenticarte")
            // NO usar setNegativeButtonText cuando DEVICE_CREDENTIAL está permitido
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        // ──── 3) Verificar disponibilidad biométrica y preferencia de huella ────
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                    or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        val huellaActivada = prefs.getBoolean(KEY_HUELLA_ACTIVADA, false)

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS && huellaActivada) {
            ocultarFormulario()
            binding.imgFingerprint.visibility = View.VISIBLE
            biometricPrompt.authenticate(promptInfo)
        } else {
            mostrarFormulario()
        }

        // ──── 4) Resto de lógica de login tradicional y listeners ────
        binding.imgFingerprint.setOnClickListener {
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS && huellaActivada) {
                ocultarFormulario()
                biometricPrompt.authenticate(promptInfo)
            } else {
                Toast.makeText(
                    this@SignInActivity,
                    "Huella no disponible. Usa email y contraseña.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.signupTab.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString().trim()
            val password = binding.loginPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@SignInActivity) { task ->
                        if (task.isSuccessful) {
                            UsuarioManager.cargarUsuario { cargado ->
                                if (cargado) {
                                    prefs.edit()
                                        .putBoolean(KEY_HUELLA_ACTIVADA, true)
                                        .putString(KEY_USUARIO_GUARDADO, email)
                                        .apply()
                                    startActivity(
                                        Intent(
                                            this@SignInActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@SignInActivity,
                                        "Error al cargar los datos del usuario",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                this@SignInActivity,
                                "Error: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Ingresa email y contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // -------------------------------------
    // MÉTODOS AUXILIARES
    // -------------------------------------

    /**
     * Muestra el formulario tradicional (email + contraseña) y oculta el ícono de huella.
     */
    private fun mostrarFormulario() {
        binding.formContainer.visibility = View.VISIBLE
        binding.imgFingerprint.visibility = View.GONE
    }

    /**
     * Oculta el formulario tradicional y (por convención) deja visible el ícono de huella.
     */
    private fun ocultarFormulario() {
        binding.formContainer.visibility = View.GONE
        // binding.imgFingerprint.visibility ya se deja en visible si se llamó desde onCreate
    }

    /**
     * Abre MainActivity (pantalla principal) tras autenticación exitosa.
     */
    private fun irAPantallaPrincipal() {
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish() // Para que no regrese al pulsar “Atrás”
    }
}
