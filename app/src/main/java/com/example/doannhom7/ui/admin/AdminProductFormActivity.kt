package com.example.doannhom7.ui.admin

import android.os.Bundle
import android.widget.*
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.CategoryEntity
import com.example.doannhom7.data.entity.ProductEntity
import com.example.doannhom7.ui.base.BaseToolbarActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*

class AdminProductFormActivity : BaseToolbarActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var spCategory: Spinner
    private lateinit var edtCode: EditText
    private lateinit var edtName: EditText
    private lateinit var edtUnit: EditText
    private lateinit var edtPrice: EditText
    private lateinit var btnSave: Button

    private lateinit var db: AppDatabase
    private var productId: Long = -1L

    private var categories: List<CategoryEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_product_form)

        setControl()
        setEvent()
        loadCategories()
    }

    private fun setControl() {
        toolbar = findViewById(R.id.toolbar)
        setupToolbar(toolbar, "Sản phẩm", showBack = true)

        spCategory = findViewById(R.id.spCategory)
        edtCode = findViewById(R.id.edtCode)
        edtName = findViewById(R.id.edtName)
        edtUnit = findViewById(R.id.edtUnit)
        edtPrice = findViewById(R.id.edtPrice)
        btnSave = findViewById(R.id.btnSave)

        db = AppDatabase.getInstance(this)
        productId = intent.getLongExtra("productId", -1L)
    }

    private fun setEvent() {
        btnSave.setOnClickListener { save() }
    }

    private fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            categories = db.categoryDao().getAll()
            withContext(Dispatchers.Main) {
                if (categories.isEmpty()) {
                    toast("Chưa có loại hàng. Hãy tạo loại hàng trước!")
                    finish()
                    return@withContext
                }

                val names = categories.map { it.name }
                spCategory.adapter = ArrayAdapter(this@AdminProductFormActivity, android.R.layout.simple_spinner_dropdown_item, names)

                if (productId != -1L) loadIfEdit()
            }
        }
    }

    private fun loadIfEdit() {
        CoroutineScope(Dispatchers.IO).launch {
            val p = db.productDao().getById(productId)
            withContext(Dispatchers.Main) {
                if (p == null) return@withContext
                edtCode.setText(p.code)
                edtName.setText(p.name)
                edtUnit.setText(p.unit)
                edtPrice.setText(p.price.toString())

                val idx = categories.indexOfFirst { it.id == p.categoryId }.coerceAtLeast(0)
                spCategory.setSelection(idx)
            }
        }
    }

    private fun save() {
        val code = edtCode.text.toString().trim()
        val name = edtName.text.toString().trim()
        val unit = edtUnit.text.toString().trim()
        val price = edtPrice.text.toString().trim().toLongOrNull()
        val cat = categories.getOrNull(spCategory.selectedItemPosition)

        if (cat == null || code.isEmpty() || name.isEmpty() || unit.isEmpty() || price == null || price < 0) {
            toast("Nhập đủ thông tin (giá là số)")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (productId == -1L) {
                db.productDao().insert(
                    ProductEntity(
                        categoryId = cat.id,
                        code = code,
                        name = name,
                        unit = unit,
                        price = price,
                        stockQty = 0
                    )
                )
            } else {
                val old = db.productDao().getById(productId)
                val keepStock = old?.stockQty ?: 0
                db.productDao().update(
                    ProductEntity(
                        id = productId,
                        categoryId = cat.id,
                        code = code,
                        name = name,
                        unit = unit,
                        price = price,
                        stockQty = keepStock
                    )
                )
            }

            withContext(Dispatchers.Main) {
                toast("Đã lưu")
                finish()
            }
        }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
