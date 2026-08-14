package com.example.apprestaurante.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.apprestaurante.AppContainer
import com.example.apprestaurante.BuildConfig
import com.example.apprestaurante.core.Validation
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.ActivityLoginBinding
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.example.apprestaurante.ui.main.MainActivity
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var credentialManager: CredentialManager

    private val viewModel: AuthViewModel by viewModels {
        RestoHubViewModelFactory(app.container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (app.container.session.isLoggedIn) {
            openMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        credentialManager = CredentialManager.create(this)

        binding.btnLogin.setOnClickListener { submit() }
        binding.btnGoogle.setOnClickListener { signInWithGoogle() }
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        binding.btnClientDemo.setOnClickListener {
            binding.etEmail.setText(AppContainer.CLIENT_EMAIL)
            binding.etPassword.setText(AppContainer.CLIENT_PASSWORD)
        }
        binding.btnReceptionDemo.setOnClickListener {
            binding.etEmail.setText(AppContainer.RECEPTION_EMAIL)
            binding.etPassword.setText(AppContainer.RECEPTION_PASSWORD)
        }
        binding.btnAdminDemo.setOnClickListener {
            binding.etEmail.setText(AppContainer.ADMIN_EMAIL)
            binding.etPassword.setText(AppContainer.ADMIN_PASSWORD)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderLoading(state.loading)
                    if (state.success) openMain()
                    state.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        viewModel.consumeMessage()
                    }
                }
            }
        }
    }

    private fun submit() {
        val email = binding.etEmail.text?.toString().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        binding.tilEmail.error = Validation.email(email)
        binding.tilPassword.error = Validation.password(password)
        if (binding.tilEmail.error == null && binding.tilPassword.error == null) {
            viewModel.login(email, password)
        }
    }

    private fun signInWithGoogle() {
        if (!BuildConfig.FIREBASE_CONFIGURED) {
            Snackbar.make(
                binding.root,
                "Falta app/google-services.json. Revisa FIREBASE_GOOGLE_SETUP.md.",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        val webClientId = getDefaultWebClientId()
        if (webClientId.isNullOrBlank()) {
            Snackbar.make(
                binding.root,
                "Firebase no generó default_web_client_id. Activa Google en Authentication y vuelve a descargar google-services.json.",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        val firebaseApp = runCatching {
            FirebaseApp.initializeApp(this) ?: FirebaseApp.getInstance()
        }.getOrNull()
        if (firebaseApp == null) {
            Snackbar.make(
                binding.root,
                "No se pudo inicializar Firebase. Verifica google-services.json.",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        val googleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        lifecycleScope.launch {
            renderGoogleLoading(true)
            try {
                val result = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )
                handleGoogleCredential(result.credential)
            } catch (error: GetCredentialException) {
                Snackbar.make(
                    binding.root,
                    error.localizedMessage ?: "No se pudo abrir el acceso con Google",
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                renderGoogleLoading(false)
            }
        }
    }

    private fun handleGoogleCredential(credential: Credential) {
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            Snackbar.make(
                binding.root,
                "Google devolvió una credencial no compatible",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        val googleCredential = runCatching {
            GoogleIdTokenCredential.createFrom(credential.data)
        }.getOrElse { error ->
            Snackbar.make(
                binding.root,
                error.localizedMessage ?: "No se pudo leer la cuenta de Google",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        val auth = runCatching { FirebaseAuth.getInstance() }.getOrElse { error ->
            Snackbar.make(
                binding.root,
                error.localizedMessage ?: "Firebase Authentication no está disponible",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        renderGoogleLoading(true)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                renderGoogleLoading(false)
                if (!task.isSuccessful) {
                    Snackbar.make(
                        binding.root,
                        task.exception?.localizedMessage ?: "No se pudo iniciar sesión con Google",
                        Snackbar.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }

                val firebaseUser = auth.currentUser
                val email = firebaseUser?.email.orEmpty()
                if (email.isBlank()) {
                    Snackbar.make(
                        binding.root,
                        "La cuenta de Google no proporcionó un correo",
                        Snackbar.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }

                val name = firebaseUser?.displayName
                    ?.trim()
                    .orEmpty()
                    .ifBlank { email.substringBefore('@') }

                viewModel.loginWithGoogle(
                    fullName = name,
                    email = email,
                    photoUri = firebaseUser?.photoUrl?.toString()
                )
            }
    }

    private fun getDefaultWebClientId(): String? {
        val resourceId = resources.getIdentifier(
            "default_web_client_id",
            "string",
            packageName
        )
        return if (resourceId == 0) null else getString(resourceId)
    }

    private fun renderLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.btnGoogle.isEnabled = !loading
        binding.btnRegister.isEnabled = !loading
    }

    private fun renderGoogleLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnGoogle.isEnabled = !loading
        binding.btnLogin.isEnabled = !loading
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
