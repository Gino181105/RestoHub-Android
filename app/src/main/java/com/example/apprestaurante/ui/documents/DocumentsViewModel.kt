package com.example.apprestaurante.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.data.repository.SalesDocumentRepository
import com.example.apprestaurante.data.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentsViewModel(
    private val repository: SalesDocumentRepository,
    private val session: SessionManager
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val type = MutableStateFlow("TODOS")

    val staffDocuments = combine(query, type) { text, selectedType -> text to selectedType }
        .flatMapLatest { (text, selectedType) ->
            repository.observeDocuments(text, selectedType)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val clientDocuments = repository.observeForClient(session.userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        query.value = value
    }

    fun setType(value: String) {
        type.value = value
    }
}
