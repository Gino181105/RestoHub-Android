package com.example.apprestaurante.ui.documents

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.FragmentDocumentsBinding
import com.example.apprestaurante.domain.model.DocumentType
import com.example.apprestaurante.ui.adapters.SalesDocumentAdapter
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import kotlinx.coroutines.launch

class DocumentsFragment : Fragment() {
    private var _binding: FragmentDocumentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DocumentsViewModel by viewModels {
        RestoHubViewModelFactory(requireContext().app.container)
    }
    private var staffMode = false
    private lateinit var adapter: SalesDocumentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        staffMode = arguments?.getBoolean(ARG_STAFF, false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SalesDocumentAdapter { document ->
            startActivity(
                Intent(requireContext(), DocumentDetailActivity::class.java)
                    .putExtra(DocumentDetailActivity.EXTRA_DOCUMENT_ID, document.id)
            )
        }
        binding.rvDocuments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDocuments.adapter = adapter
        binding.filterContainer.visibility = if (staffMode) View.VISIBLE else View.GONE

        if (staffMode) {
            binding.etSearch.doAfterTextChanged { viewModel.setQuery(it?.toString().orEmpty()) }
            val labels = listOf("Todos") + DocumentType.entries.map { it.label }
            val values = listOf("TODOS") + DocumentType.entries.map { it.name }
            binding.spType.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                labels
            )
            binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.setType(values[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flow = if (staffMode) viewModel.staffDocuments else viewModel.clientDocuments
                flow.collect { documents ->
                    adapter.submitList(documents)
                    binding.tvEmpty.visibility = if (documents.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        if (::adapter.isInitialized) binding.rvDocuments.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_STAFF = "staff_mode"

        fun newClient() = DocumentsFragment().apply {
            arguments = Bundle().apply { putBoolean(ARG_STAFF, false) }
        }

        fun newStaff() = DocumentsFragment().apply {
            arguments = Bundle().apply { putBoolean(ARG_STAFF, true) }
        }
    }
}
