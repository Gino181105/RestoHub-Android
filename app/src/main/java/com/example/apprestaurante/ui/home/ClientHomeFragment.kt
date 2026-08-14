package com.example.apprestaurante.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprestaurante.R
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.FragmentClientHomeBinding
import com.example.apprestaurante.ui.adapters.RecipeAdapter
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.example.apprestaurante.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ClientHomeFragment : Fragment() {
    private var _binding: FragmentClientHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ClientHomeViewModel by viewModels {
        RestoHubViewModelFactory(requireContext().app.container)
    }
    private val recipeAdapter = RecipeAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        _binding = FragmentClientHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.tvWelcome.text = "Hola, ${requireContext().app.container.session.userName}"
        binding.rvRecipes.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvRecipes.adapter = recipeAdapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        binding.btnMenu.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_client_products)
        }
        binding.btnCart.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_client_cart)
        }
        binding.btnLocation.setOnClickListener {
            val uri = Uri.parse("geo:-12.0464,-77.0428?q=RestoHub Lima")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            runCatching { startActivity(intent) }.onFailure {
                Snackbar.make(binding.root, "No hay una aplicación de mapas", Snackbar.LENGTH_LONG).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { ui ->
                        binding.swipeRefresh.isRefreshing = ui.loading
                        recipeAdapter.submitList(ui.recipes)
                        binding.tvRemoteEmpty.visibility =
                            if (!ui.loading && ui.recipes.isEmpty()) View.VISIBLE else View.GONE
                        ui.message?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                            viewModel.consumeMessage()
                        }
                    }
                }
                launch {
                    viewModel.orderCount.collect { count ->
                        binding.tvOrderCount.text = count.toString()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.rvRecipes.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
