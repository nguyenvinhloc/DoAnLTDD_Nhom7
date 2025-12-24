package com.example.doannhom7.ui.user

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.CustomerEntity
import com.example.doannhom7.data.entity.ProductEntity
import com.example.doannhom7.data.repo.DraftLine
import com.example.doannhom7.data.repo.InvoiceRepository
import com.example.doannhom7.ui.user.adapters.DraftLinesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class UserOutInvoiceEditActivity : AppCompatActivity() {

    private lateinit var tvInfo: TextView
    private lateinit var btnPickCustomer: Button
    private lateinit var tvCustomer: TextView

    private lateinit var btnPickProduct: Button
    private lateinit var tvProduct: TextView
    private lateinit var edtQty: EditText
    private lateinit var btnAddLine: Button

    private lateinit var tvTotal: TextView
    private lateinit var rv: RecyclerView
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    private lateinit var db: AppDatabase
    private lateinit var repo: InvoiceRepository

    private var invoiceId: Long = -1L
    private var customers: List<CustomerEntity> = emptyList()
    private var products: List<ProductEntity> = emptyList()

    private var pickedCustomer: CustomerEntity? = null
    private var pickedProduct: ProductEntity? = null

    private val lines: MutableList<DraftLine> = mutableListOf()
    private lateinit var adapter: DraftLinesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_out_invoice_edit)

        setControl()
        setEvent()
        loadBaseData()
        loadInvoice()
    }

    private fun setControl() {
        tvInfo = findViewById(R.id.tvInfo)
        btnPickCustomer = findViewById(R.id.btnPickCustomer)
        tvCustomer = findViewById(R.id.tvCustomer)

        btnPickProduct = findViewById(R.id.btnPickProduct)
        tvProduct = findViewById(R.id.tvProduct)
        edtQty = findViewById(R.id.edtQty)
        btnAddLine = findViewById(R.id.btnAddLine)

        tvTotal = findViewById(R.id.tvTotal)
        rv = findViewById(R.id.rvLines)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)

        db = AppDatabase.getInstance(this)
        repo = InvoiceRepository(db)

        invoiceId = intent.getLongExtra("invoiceId", -1L)

        adapter = DraftLinesAdapter(lines) { pos ->
            lines.removeAt(pos)
            adapter.setData(lines)
            updateTotal()
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        updateTotal()
    }

    private fun setEvent() {
        btnPickCustomer.setOnClickListener { pickCustomerDialog() }
        btnPickProduct.setOnClickListener { pickProductDialog() }

        btnAddLine.setOnClickListener {
            val p = pickedProduct ?: run { toast("Chưa chọn sản phẩm"); return@setOnClickListener }
            val qty = edtQty.text.toString().trim().toIntOrNull()
            if (qty == null || qty <= 0) { toast("Số lượng không hợp lệ"); return@setOnClickListener }

            // KHÔNG nhập giá -> lấy từ product.price
            lines.add(DraftLine(p.id, p.name, p.unit, qty, p.price))
            adapter.setData(lines)
            edtQty.setText("")
            updateTotal()
        }

        btnSave.setOnClickListener {
            val c = pickedCustomer ?: run { toast("Chưa có khách"); return@setOnClickListener }
            if (lines.isEmpty()) { toast("Chưa có dòng hàng"); return@setOnClickListener }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    repo.updateOutInvoice(invoiceId, c.id, lines, note = null)
                    withContext(Dispatchers.Main) {
                        toast("Đã lưu chỉnh sửa")
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { toast("Lỗi: ${e.message}") }
                }
            }
        }

        btnDelete.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Xóa phiếu #$invoiceId?")
                .setMessage("Xóa sẽ hoàn kho lại.")
                .setPositiveButton("Xóa") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            repo.deleteOutInvoice(invoiceId)
                            withContext(Dispatchers.Main) {
                                toast("Đã xóa phiếu")
                                finish()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) { toast("Lỗi: ${e.message}") }
                        }
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun loadBaseData() {
        lifecycleScope.launch(Dispatchers.IO) {
            customers = db.customerDao().getAll()
            products = db.productDao().getAll()
        }
    }

    private fun loadInvoice() {
        if (invoiceId == -1L) { toast("invoiceId không hợp lệ"); finish(); return }

        lifecycleScope.launch(Dispatchers.IO) {
            val detail = db.invoiceDao().getInvoiceWithItems(invoiceId)
            if (detail == null) {
                withContext(Dispatchers.Main) { toast("Không tìm thấy phiếu"); finish() }
                return@launch
            }

            val c = detail.invoice.customerId?.let { db.customerDao().getById(it) }
            val draft = detail.items.map {
                DraftLine(it.productId, it.productName, it.unit, it.qty, it.unitPrice)
            }

            withContext(Dispatchers.Main) {
                pickedCustomer = c
                tvInfo.text = "Sửa phiếu xuất #$invoiceId • ${detail.invoice.status.name}"
                tvCustomer.text = "Khách: ${c?.shopName ?: "?"} • ${c?.phone ?: ""}"

                lines.clear()
                lines.addAll(draft)
                adapter.setData(lines)
                updateTotal()
            }
        }
    }

    private fun pickCustomerDialog() {
        if (customers.isEmpty()) { toast("Chưa có khách"); return }
        val names = customers.map { "${it.shopName} • ${it.phone}" }.toTypedArray()
        android.app.AlertDialog.Builder(this)
            .setTitle("Chọn khách hàng")
            .setItems(names) { _, which ->
                pickedCustomer = customers[which]
                tvCustomer.text = "Khách: ${pickedCustomer!!.shopName} • ${pickedCustomer!!.phone}"
            }.show()
    }

    private fun pickProductDialog() {
        if (products.isEmpty()) { toast("Chưa có sản phẩm"); return }
        val names = products.map { "${it.name} (${it.code}) • tồn ${it.stockQty} ${it.unit}" }.toTypedArray()
        android.app.AlertDialog.Builder(this)
            .setTitle("Chọn sản phẩm")
            .setItems(names) { _, which ->
                pickedProduct = products[which]
                tvProduct.text = "Sản phẩm: ${pickedProduct!!.name} • Giá: ${vnd(pickedProduct!!.price)}"
            }.show()
    }

    private fun updateTotal() {
        val total = lines.sumOf { it.lineTotal }
        tvTotal.text = "Tổng: ${vnd(total)}"
    }

    private fun vnd(x: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return nf.format(x) + " đ"
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
