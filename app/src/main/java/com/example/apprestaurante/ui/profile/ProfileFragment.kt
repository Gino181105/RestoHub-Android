package com.example.apprestaurante.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.apprestaurante.core.ImageLoader
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.FragmentProfileBinding
import com.example.apprestaurante.domain.model.UserRole
import com.example.apprestaurante.ui.auth.LoginActivity
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels {
        RestoHubViewModelFactory(requireContext().app.container)
    }

    private val pickPhoto = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        runCatching {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        viewModel.savePhoto(uri.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        val container = requireContext().app.container
        binding.btnPhoto.setOnClickListener { pickPhoto.launch(arrayOf("image/*")) }
        binding.btnMap.setOnClickListener {
            val uri = Uri.parse("geo:-12.0464,-77.0428?q=RestoHub Lima")
            runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                .onFailure {
                    Snackbar.make(binding.root, "No hay una aplicación de mapas", Snackbar.LENGTH_LONG).show()
                }
        }
        binding.btnLogout.setOnClickListener {
            container.authRepository.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { user ->
                        user ?: return@collect
                        binding.tvName.text = user.fullName
                        binding.tvEmail.text = user.email
                        binding.tvPhone.text = if (user.phone.isBlank()) "Sin teléfono" else user.phone
                        val role = runCatching { UserRole.valueOf(user.role) }
                            .getOrDefault(UserRole.CLIENT)
                        binding.tvRole.text = role.label
                        ImageLoader.load(binding.imgProfile, user.photoUri)
                    }
                }
                launch {
                    viewModel.message.collect { message ->
                        message?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                            viewModel.consumeMessage()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
