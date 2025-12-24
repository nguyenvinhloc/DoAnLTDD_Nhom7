package com.example.doannhom7.ui.user.fragments

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.ProductEntity
import com.example.doannhom7.data.repo.DraftLine
import com.example.doannhom7.data.repo.InvoiceRepository
import com.example.doannhom7.ui.user.adapters.DraftLinesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class UserImportFragment : Fragment(R.layout.fragment_user_import) {

    private lateinit var btnPick: Button
    private lateinit var tvPicked: TextView
    private lateinit var edtQty: EditText
    private lateinit var edtPrice: EditText
    private lateinit var btnAdd: Button
    private lateinit var rv: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnSave: Button

    private lateinit var db: AppDatabase
    private lateinit var repo: InvoiceRepository

    private var products: List<ProductEntity> = emptyList()
    private var picked: ProductEntity? = null
    private var lines: MutableList<DraftLine> = mutableListOf()
    private lateinit var adapter: DraftLinesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControl(view)
        setEvent()
        loadProducts()
    }

    private fun setControl(view: View) {
        btnPick = view.findViewById(R.id.btnPickProduct)
        tvPicked = view.findViewById(R.id.tvPickedProduct)
        edtQty = view.findViewById(R.id.edtQty)
        edtPrice = view.findViewById(R.id.edtPrice)
        btnAdd = view.findViewById(R.id.btnAddLine)
        rv = view.findViewById(R.id.rvLines)
        tvTotal = view.findViewById(R.id.tvTotal)
        btnSave = view.findViewById(R.id.btnSave)

        db = AppDatabase.getInstance(requireContext())
        repo = InvoiceRepository(db)

        adapter = DraftLinesAdapter(lines) { pos ->
            lines.removeAt(pos)
            adapter.setData(lines)
            updateTotal()
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        updateTotal()
    }

    private fun setEvent() {
        btnPick.setOnClickListener { showPickProductDialog() }

        btnAdd.setOnClickListener {
            val p = picked ?: run { toast("Chưa chọn sản phẩm"); return@setOnClickListener }
            val qty = edtQty.text.toString().trim().toIntOrNull()
            val price = edtPrice.text.toString().trim().toLongOrNull()

            if (qty == null || qty <= 0) { toast("Số lượng không hợp lệ"); return@setOnClickListener }
            if (price == null || price <= 0) { toast("Giá nhập không hợp lệ"); return@setOnClickListener }

            lines.add(DraftLine(p.id, p.name, p.unit, qty, price))
            adapter.setData(lines)

            edtQty.setText("")
            edtPrice.setText("")
            updateTotal()
        }

        btnSave.setOnClickListener {
            if (lines.isEmpty()) { toast("Chưa có dòng hàng"); return@setOnClickListener }
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    repo.createInInvoice(lines)
                    withContext(Dispatchers.Main) {
                        toast("Đã lưu phiếu nhập & cập nhật tồn kho")
                        lines.clear()
                        adapter.setData(lines)
                        updateTotal()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { toast("Lỗi: ${e.message}") }
                }
            }
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch(Dispatchers.IO) {
            products = db.productDao().getAll()
            withContext(Dispatchers.Main) {
                toast("Đã tải ${products.size} sản phẩm")
            }
        }
    }

    private fun showPickProductDialog() {
        if (products.isEmpty()) { toast("Chưa có sản phẩm"); return }
        val names = products.map { "${it.name} (${it.code}) • tồn ${it.stockQty} ${it.unit}" }.toTypedArray()
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Chọn sản phẩm")
            .setItems(names) { _, which ->
                picked = products[which]
                tvPicked.text = "Đã chọn: ${picked!!.name} (${picked!!.unit})"
                edtPrice.setText(picked!!.price.toString())
            }
            .show()
    }

    private fun updateTotal() {
        val total = lines.sumOf { it.lineTotal }
        tvTotal.text = "Tổng: ${vnd(total)}"
    }

    private fun vnd(x: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return nf.format(x) + " đ"
    }

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
