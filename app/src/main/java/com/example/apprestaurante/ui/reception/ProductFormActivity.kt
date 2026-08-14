package com.example.apprestaurante.ui.reception

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.apprestaurante.core.ImageLoader
import com.example.apprestaurante.core.app
import com.example.apprestaurante.data.local.entity.ProductEntity
import com.example.apprestaurante.databinding.ActivityProductFormBinding
import com.example.apprestaurante.domain.model.UserRole
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File

class ProductFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductFormBinding
    private val viewModel: ProductFormViewModel by viewModels {
        RestoHubViewModelFactory(app.container)
    }
    private var productId: Long = 0L
    private var loadedProduct: ProductEntity? = null
    private var selectedImageUri: String = ""
    private var pendingCameraUri: Uri? = null
    private var formPopulated = false

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        selectedImageUri = uri.toString()
        binding.etImageUrl.setText("")
        ImageLoader.load(binding.imgPreview, selectedImageUri)
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = pendingCameraUri?.toString().orEmpty()
            binding.etImageUrl.setText("")
            ImageLoader.load(binding.imgPreview, selectedImageUri)
        }
    }

    private val requestCamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Snackbar.make(binding.root, "Permiso de cámara denegado", Snackbar.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!app.container.session.role.isStaff) {
            finish()
            return
        }
        binding = ActivityProductFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        productId = intent.getLongExtra(EXTRA_PRODUCT_ID, 0L)
        binding.toolbar.title = if (productId == 0L) "Nuevo producto" else "Editar producto"

        binding.btnGallery.setOnClickListener { pickImage.launch(arrayOf("image/*")) }
        binding.btnCamera.setOnClickListener { requestOrOpenCamera() }
        binding.btnPreviewUrl.setOnClickListener {
            val url = binding.etImageUrl.text?.toString().orEmpty().trim()
            if (url.isBlank()) {
                binding.tilImageUrl.error = "Ingresa una URL"
            } else {
                binding.tilImageUrl.error = null
                selectedImageUri = url
                ImageLoader.load(binding.imgPreview, url)
            }
        }
        binding.btnSave.setOnClickListener { submit() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
                    binding.btnSave.isEnabled = !state.loading
                    state.product?.let { product ->
                        loadedProduct = product
                        if (!formPopulated) {
                            formPopulated = true
                            binding.etName.setText(product.name)
                            binding.etDescription.setText(product.description)
                            binding.etCategory.setText(product.category)
                            binding.etPrice.setText(product.price.toString())
                            binding.etStock.setText(product.stock.toString())
                            binding.cbActive.isChecked = product.isActive
                            selectedImageUri = product.imageUri
                            if (product.imageUri.startsWith("http")) {
                                binding.etImageUrl.setText(product.imageUri)
                            }
                            ImageLoader.load(binding.imgPreview, product.imageUri)
                        }
                    }
                    state.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        viewModel.consumeMessage()
                    }
                    if (state.saved) {
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
        }
        viewModel.load(productId)
    }

    private fun submit() {
        val name = binding.etName.text?.toString().orEmpty().trim()
        val description = binding.etDescription.text?.toString().orEmpty().trim()
        val category = binding.etCategory.text?.toString().orEmpty().trim()
        val price = binding.etPrice.text?.toString()?.toDoubleOrNull()
        val stock = binding.etStock.text?.toString()?.toIntOrNull()
        val url = binding.etImageUrl.text?.toString().orEmpty().trim()

        binding.tilName.error = if (name.length < 3) "Ingresa un nombre válido" else null
        binding.tilDescription.error = if (description.length < 5) "Describe el producto" else null
        binding.tilCategory.error = if (category.isBlank()) "La categoría es obligatoria" else null
        binding.tilPrice.error = if (price == null || price <= 0.0) "Precio no válido" else null
        binding.tilStock.error = if (stock == null || stock < 0) "Stock no válido" else null

        if (
            binding.tilName.error != null || binding.tilDescription.error != null ||
            binding.tilCategory.error != null || binding.tilPrice.error != null ||
            binding.tilStock.error != null
        ) return

        val image = url.ifBlank { selectedImageUri }
        val original = loadedProduct
        viewModel.save(
            ProductEntity(
                id = original?.id ?: 0L,
                name = name,
                description = description,
                category = category,
                price = price!!,
                stock = stock!!,
                imageUri = image,
                isActive = binding.cbActive.isChecked,
                createdAt = original?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun requestOrOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            requestCamera.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val directory = File(filesDir, "product_images").apply { mkdirs() }
        val file = File(directory, "product_${System.currentTimeMillis()}.jpg")
        pendingCameraUri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            file
        )
        pendingCameraUri?.let { takePicture.launch(it) }
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
    }
}
