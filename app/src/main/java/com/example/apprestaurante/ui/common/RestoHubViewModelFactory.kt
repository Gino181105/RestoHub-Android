package com.example.apprestaurante.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apprestaurante.AppContainer
import com.example.apprestaurante.ui.admin.AdminDashboardViewModel
import com.example.apprestaurante.ui.admin.AdminUsersViewModel
import com.example.apprestaurante.ui.admin.UserFormViewModel
import com.example.apprestaurante.ui.auth.AuthViewModel
import com.example.apprestaurante.ui.cart.CartViewModel
import com.example.apprestaurante.ui.documents.DocumentDetailViewModel
import com.example.apprestaurante.ui.documents.DocumentsViewModel
import com.example.apprestaurante.ui.home.ClientHomeViewModel
import com.example.apprestaurante.ui.orders.OrderDetailViewModel
import com.example.apprestaurante.ui.orders.OrdersViewModel
import com.example.apprestaurante.ui.products.ProductsViewModel
import com.example.apprestaurante.ui.profile.ProfileViewModel
import com.example.apprestaurante.ui.reception.ProductFormViewModel
import com.example.apprestaurante.ui.reception.ReceptionDashboardViewModel
import com.example.apprestaurante.ui.reception.ReceptionProductsViewModel
import com.example.apprestaurante.ui.sales.StaffSaleViewModel

class RestoHubViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(container.authRepository) as T
        modelClass.isAssignableFrom(ClientHomeViewModel::class.java) ->
            ClientHomeViewModel(
                container.remoteRepository,
                container.orderRepository,
                container.session
            ) as T
        modelClass.isAssignableFrom(ProductsViewModel::class.java) ->
            ProductsViewModel(
                container.productRepository,
                container.cartRepository,
                container.session
            ) as T
        modelClass.isAssignableFrom(CartViewModel::class.java) ->
            CartViewModel(container.cartRepository, container.session) as T
        modelClass.isAssignableFrom(OrdersViewModel::class.java) ->
            OrdersViewModel(container.orderRepository, container.session) as T
        modelClass.isAssignableFrom(OrderDetailViewModel::class.java) ->
            OrderDetailViewModel(container.orderRepository, container.session) as T
        modelClass.isAssignableFrom(DocumentsViewModel::class.java) ->
            DocumentsViewModel(container.salesDocumentRepository, container.session) as T
        modelClass.isAssignableFrom(DocumentDetailViewModel::class.java) ->
            DocumentDetailViewModel(container.salesDocumentRepository, container.session) as T
        modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
            ProfileViewModel(container.database.userDao(), container.session) as T
        modelClass.isAssignableFrom(ReceptionDashboardViewModel::class.java) ->
            ReceptionDashboardViewModel(
                container.productRepository,
                container.orderRepository,
                container.database.userDao()
            ) as T
        modelClass.isAssignableFrom(ReceptionProductsViewModel::class.java) ->
            ReceptionProductsViewModel(container.productRepository) as T
        modelClass.isAssignableFrom(ProductFormViewModel::class.java) ->
            ProductFormViewModel(container.productRepository) as T
        modelClass.isAssignableFrom(AdminDashboardViewModel::class.java) ->
            AdminDashboardViewModel(
                container.productRepository,
                container.orderRepository,
                container.userManagementRepository,
                container.salesDocumentRepository
            ) as T
        modelClass.isAssignableFrom(AdminUsersViewModel::class.java) ->
            AdminUsersViewModel(container.userManagementRepository, container.session) as T
        modelClass.isAssignableFrom(UserFormViewModel::class.java) ->
            UserFormViewModel(container.userManagementRepository, container.session) as T
        modelClass.isAssignableFrom(StaffSaleViewModel::class.java) ->
            StaffSaleViewModel(
                container.staffSaleRepository,
                container.orderRepository,
                container.session
            ) as T
        else -> error("ViewModel no registrado: ${modelClass.name}")
    }
}
