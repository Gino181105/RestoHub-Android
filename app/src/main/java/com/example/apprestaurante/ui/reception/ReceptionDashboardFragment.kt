package com.example.apprestaurante.ui.reception

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.apprestaurante.R
import com.example.apprestaurante.core.PriceFormatter
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.FragmentReceptionDashboardBinding
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.example.apprestaurante.ui.main.MainActivity
import com.example.apprestaurante.ui.sales.StaffSaleActivity
import kotlinx.coroutines.launch

class ReceptionDashboardFragment : Fragment() {
    private var _binding: FragmentReceptionDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReceptionDashboardViewModel by viewModels {
        RestoHubViewModelFactory(requireContext().app.container)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        _binding = FragmentReceptionDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.tvWelcome.text = "Bienvenida, ${requireContext().app.container.session.userName}"
        binding.btnProducts.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_reception_products)
        }
        binding.btnOrders.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_reception_orders)
        }
        binding.btnNewSale.setOnClickListener {
            startActivity(Intent(requireContext(), StaffSaleActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dashboard.collect { data ->
                    binding.tvProducts.text = data.activeProducts.toString()
                    binding.tvLowStock.text = data.lowStockProducts.toString()
                    binding.tvOrders.text = data.pendingOrders.toString()
                    binding.tvClients.text = data.clientCount.toString()
                    binding.tvSales.text = PriceFormatter.format(data.deliveredSales)
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
