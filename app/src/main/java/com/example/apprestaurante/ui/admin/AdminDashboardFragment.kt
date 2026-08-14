package com.example.apprestaurante.ui.admin

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
import com.example.apprestaurante.databinding.FragmentAdminDashboardBinding
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.example.apprestaurante.ui.main.MainActivity
import com.example.apprestaurante.ui.sales.StaffSaleActivity
import kotlinx.coroutines.launch

class AdminDashboardFragment : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminDashboardViewModel by viewModels {
        RestoHubViewModelFactory(requireContext().app.container)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvWelcome.text = "Hola, ${requireContext().app.container.session.userName}"
        binding.btnOrders.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_admin_orders)
        }
        binding.btnProducts.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_admin_products)
        }
        binding.btnUsers.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_admin_users)
        }
        binding.btnDocuments.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_admin_documents)
        }
        binding.btnNewSale.setOnClickListener {
            startActivity(Intent(requireContext(), StaffSaleActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dashboard.collect { data ->
                    binding.tvProducts.text = data.activeProducts.toString()
                    binding.tvLowStock.text = data.lowStockProducts.toString()
                    binding.tvOrders.text = data.openOrders.toString()
                    binding.tvClients.text = data.clients.toString()
                    binding.tvStaff.text = (data.receptionists + data.administrators).toString()
                    binding.tvDocuments.text = data.issuedDocuments.toString()
                    binding.tvSales.text = PriceFormatter.format(data.totalSales)
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
