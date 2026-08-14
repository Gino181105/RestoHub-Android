package com.example.apprestaurante.ui.orders

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.FragmentOrdersBinding
import com.example.apprestaurante.domain.model.OrderStatus
import com.example.apprestaurante.ui.adapters.OrderAdapter
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import kotlinx.coroutines.launch

class OrdersFragment : Fragment() {
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrdersViewModel by viewModels {
        RestoHubViewModelFactory(requireContext().app.container)
    }
    private var receptionMode = false
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        receptionMode = arguments?.getBoolean(ARG_RECEPTION, false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        orderAdapter = OrderAdapter(showClient = receptionMode) { order ->
            startActivity(
                Intent(requireContext(), OrderDetailActivity::class.java)
                    .putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.id)
            )
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = orderAdapter
        binding.filterContainer.visibility = if (receptionMode) View.VISIBLE else View.GONE

        if (receptionMode) {
            val labels = listOf("Todos") + OrderStatus.entries.map { it.label }
            val values = listOf("TODOS") + OrderStatus.entries.map { it.name }
            binding.spStatus.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, labels
            )
            binding.spStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    viewModel.setStatusFilter(values[position])
                }
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flow = if (receptionMode) {
                    viewModel.receptionOrders
                } else {
                    viewModel.clientOrders
                }
                flow.collect { orders ->
                    orderAdapter.submitList(orders)
                    binding.tvEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
                    binding.tvEmpty.text = if (receptionMode) {
                        "No hay pedidos con este estado"
                    } else {
                        "Aún no tienes pedidos"
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Room actualiza automáticamente la lista al volver del detalle.
    }

    override fun onDestroyView() {
        if (::orderAdapter.isInitialized) binding.rvOrders.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_RECEPTION = "reception_mode"
        fun newClient() = OrdersFragment().apply {
            arguments = Bundle().apply { putBoolean(ARG_RECEPTION, false) }
        }
        fun newReception() = OrdersFragment().apply {
            arguments = Bundle().apply { putBoolean(ARG_RECEPTION, true) }
        }
    }
}
