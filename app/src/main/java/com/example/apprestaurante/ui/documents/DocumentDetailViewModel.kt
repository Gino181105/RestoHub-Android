package com.example.apprestaurante.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.data.local.entity.OrderItemEntity
import com.example.apprestaurante.data.local.entity.SalesDocumentEntity
import com.example.apprestaurante.data.repository.SalesDocumentRepository
import com.example.apprestaurante.data.session.SessionManager
import com.example.apprestaurante.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class DocumentDetailUiState(
    val loading: Boolean = true,
    val document: SalesDocumentEntity? = null,
    val items: List<OrderItemEntity> = emptyList(),
    val error: String? = null
)

class DocumentDetailViewModel(
    private val repository: SalesDocumentRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _state = MutableStateFlow(DocumentDetailUiState())
    val state: StateFlow<DocumentDetailUiState> = _state.asStateFlow()

    fun load(documentId: Long) = viewModelScope.launch {
        val document = repository.getById(documentId)
        if (document == null) {
            _state.value = DocumentDetailUiState(loading = false, error = "Comprobante no encontrado")
            return@launch
        }
        val order = repository.getOrder(document.orderId)
        if (session.role == UserRole.CLIENT && order?.userId != session.userId) {
            _state.value = DocumentDetailUiState(loading = false, error = "No tienes permiso")
            return@launch
        }
        _state.value = DocumentDetailUiState(
            loading = false,
            document = document,
            items = repository.getItems(document.orderId)
        )
    }
}
