package com.example.apprestaurante.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.apprestaurante.R
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.ActivityMainBinding
import com.example.apprestaurante.domain.model.UserRole
import com.example.apprestaurante.ui.admin.AdminDashboardFragment
import com.example.apprestaurante.ui.admin.AdminUsersFragment
import com.example.apprestaurante.ui.auth.LoginActivity
import com.example.apprestaurante.ui.cart.CartFragment
import com.example.apprestaurante.ui.documents.DocumentsFragment
import com.example.apprestaurante.ui.home.ClientHomeFragment
import com.example.apprestaurante.ui.orders.OrdersFragment
import com.example.apprestaurante.ui.products.ProductsFragment
import com.example.apprestaurante.ui.profile.ProfileFragment
import com.example.apprestaurante.ui.reception.ReceptionDashboardFragment
import com.example.apprestaurante.ui.reception.ReceptionProductsFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val session get() = app.container.session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!session.isLoggedIn) {
            openLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureBottomNavigation()
        configureDrawer()
        observeCartBadge()
        configureBackButton()

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = when (session.role) {
                UserRole.CLIENT -> R.id.nav_client_home
                UserRole.RECEPTIONIST -> R.id.nav_reception_home
                UserRole.ADMIN -> R.id.nav_admin_home
            }
        }
    }

    private fun configureBottomNavigation() {
        binding.bottomNavigation.menu.clear()
        binding.bottomNavigation.inflateMenu(
            when (session.role) {
                UserRole.CLIENT -> R.menu.menu_client_bottom
                UserRole.RECEPTIONIST -> R.menu.menu_reception_bottom
                UserRole.ADMIN -> R.menu.menu_admin_bottom
            }
        )

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_client_home -> show(ClientHomeFragment(), "Inicio")
                R.id.nav_client_products -> show(ProductsFragment(), "Carta")
                R.id.nav_client_cart -> show(CartFragment(), "Carrito")
                R.id.nav_client_orders -> show(OrdersFragment.newClient(), "Mis pedidos")

                R.id.nav_reception_home -> show(ReceptionDashboardFragment(), "Recepción")
                R.id.nav_reception_products -> show(ReceptionProductsFragment(), "Productos")
                R.id.nav_reception_orders -> show(OrdersFragment.newReception(), "Pedidos")
                R.id.nav_reception_documents -> show(DocumentsFragment.newStaff(), "Comprobantes")

                R.id.nav_admin_home -> show(AdminDashboardFragment(), "Panel administrador")
                R.id.nav_admin_orders -> show(OrdersFragment.newReception(), "Todos los pedidos")
                R.id.nav_admin_products -> show(ReceptionProductsFragment(), "Productos e inventario")
                R.id.nav_admin_users -> show(AdminUsersFragment(), "Usuarios y roles")
                R.id.nav_admin_documents -> show(DocumentsFragment.newStaff(), "Boletas y facturas")

                R.id.nav_profile -> show(ProfileFragment(), "Mi perfil")
                else -> false
            }
        }
        binding.toolbar.subtitle = "${session.userName} · ${session.role.label}"
    }

    private fun configureDrawer() {
        val role = session.role
        val isClient = role == UserRole.CLIENT
        val isAdmin = role == UserRole.ADMIN

        binding.navigationView.menu.findItem(R.id.drawer_cart).isVisible = isClient
        binding.navigationView.menu.findItem(R.id.drawer_users).isVisible = isAdmin
        binding.navigationView.menu.findItem(R.id.drawer_products).title =
            if (isClient) "Carta de productos" else "Administrar productos"

        val header = binding.navigationView.getHeaderView(0)
        header.findViewById<TextView>(R.id.tvHeaderName).text = session.userName
        header.findViewById<TextView>(R.id.tvHeaderRole).text = role.label

        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_home -> selectHomeForRole()
                R.id.drawer_products -> selectProductsForRole()
                R.id.drawer_cart -> binding.bottomNavigation.selectedItemId = R.id.nav_client_cart
                R.id.drawer_orders -> selectOrdersForRole()
                R.id.drawer_documents -> selectDocumentsForRole()
                R.id.drawer_users -> binding.bottomNavigation.selectedItemId = R.id.nav_admin_users
                R.id.drawer_profile -> {
                    if (role == UserRole.ADMIN) {
                        show(ProfileFragment(), "Mi perfil")
                    } else {
                        binding.bottomNavigation.selectedItemId = R.id.nav_profile
                    }
                }
                R.id.drawer_logout -> confirmLogout()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun selectHomeForRole() {
        binding.bottomNavigation.selectedItemId = when (session.role) {
            UserRole.CLIENT -> R.id.nav_client_home
            UserRole.RECEPTIONIST -> R.id.nav_reception_home
            UserRole.ADMIN -> R.id.nav_admin_home
        }
    }

    private fun selectProductsForRole() {
        binding.bottomNavigation.selectedItemId = when (session.role) {
            UserRole.CLIENT -> R.id.nav_client_products
            UserRole.RECEPTIONIST -> R.id.nav_reception_products
            UserRole.ADMIN -> R.id.nav_admin_products
        }
    }

    private fun selectOrdersForRole() {
        binding.bottomNavigation.selectedItemId = when (session.role) {
            UserRole.CLIENT -> R.id.nav_client_orders
            UserRole.RECEPTIONIST -> R.id.nav_reception_orders
            UserRole.ADMIN -> R.id.nav_admin_orders
        }
    }

    private fun selectDocumentsForRole() {
        when (session.role) {
            UserRole.CLIENT -> show(DocumentsFragment.newClient(), "Mis comprobantes")
            UserRole.RECEPTIONIST -> binding.bottomNavigation.selectedItemId = R.id.nav_reception_documents
            UserRole.ADMIN -> binding.bottomNavigation.selectedItemId = R.id.nav_admin_documents
        }
    }

    private fun observeCartBadge() {
        if (session.role != UserRole.CLIENT) return
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.container.cartRepository.observeCount(session.userId).collect { count ->
                    val badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_client_cart)
                    badge.isVisible = count > 0
                    badge.number = count
                }
            }
        }
    }

    private fun configureBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Deseas salir de RestoHub?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salir") { _, _ ->
                app.container.authRepository.logout()
                openLogin()
            }
            .show()
    }

    private fun openLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    fun selectTab(itemId: Int) {
        binding.bottomNavigation.selectedItemId = itemId
    }

    private fun show(fragment: Fragment, title: String): Boolean {
        binding.toolbar.title = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}
