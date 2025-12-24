package com.example.doannhom7.ui.admin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.CustomerEntity
import com.example.doannhom7.ui.base.BaseToolbarActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*

class AdminCustomerFormActivity : BaseToolbarActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var edtShop: EditText
    private lateinit var edtOwner: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtNote: EditText
    private lateinit var btnSave: Button

    private lateinit var db: AppDatabase
    private var customerId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_customer_form)

        setControl()
        setEvent()
        loadIfEdit()
    }

    private fun setControl() {
        toolbar = findViewById(R.id.toolbar)
        setupToolbar(toolbar, "Khách hàng", showBack = true)

        edtShop = findViewById(R.id.edtShopName)
        edtOwner = findViewById(R.id.edtOwnerName)
        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)
        edtNote = findViewById(R.id.edtNote)
        btnSave = findViewById(R.id.btnSave)

        db = AppDatabase.getInstance(this)
        customerId = intent.getLongExtra("customerId", -1L)
    }

    private fun setEvent() {
        btnSave.setOnClickListener { save() }
    }

    private fun loadIfEdit() {
        if (customerId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val c = db.customerDao().getById(customerId)
            withContext(Dispatchers.Main) {
                if (c == null) return@withContext
                edtShop.setText(c.shopName)
                edtOwner.setText(c.ownerName)
                edtPhone.setText(c.phone)
                edtAddress.setText(c.address)
                edtNote.setText(c.note ?: "")
            }
        }
    }

    private fun save() {
        val shop = edtShop.text.toString().trim()
        val owner = edtOwner.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val addr = edtAddress.text.toString().trim()
        val note = edtNote.text.toString().trim().ifEmpty { null }

        if (shop.isEmpty() || owner.isEmpty() || phone.isEmpty() || addr.isEmpty()) {
            toast("Nhập đủ thông tin")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (customerId == -1L) {
                db.customerDao().insert(
                    CustomerEntity(
                        shopName = shop,
                        ownerName = owner,
                        phone = phone,
                        address = addr,
                        note = note
                    )
                )
            } else {
                db.customerDao().update(
                    CustomerEntity(
                        id = customerId,
                        shopName = shop,
                        ownerName = owner,
                        phone = phone,
                        address = addr,
                        note = note
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
