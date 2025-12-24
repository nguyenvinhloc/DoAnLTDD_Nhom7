package com.example.doannhom7.ui.user.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.LoginActivity
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.CustomerEntity
import com.example.doannhom7.data.entity.ProductEntity
import com.example.doannhom7.data.repo.DraftLine
import com.example.doannhom7.data.repo.InvoiceRepository
import com.example.doannhom7.ui.user.UserOutInvoicesActivity
import com.example.doannhom7.ui.user.adapters.DraftLinesAdapter
import com.example.doannhom7.utils.SessionManager
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class UserExportFragment : Fragment(R.layout.fragment_user_export) {

    private lateinit var toolbarExport: MaterialToolbar
    private lateinit var btnOpenOutList: Button
    private lateinit var btnPickCustomer: Button
    private lateinit var tvPickedCustomer: TextView
    private lateinit var btnPickProduct: Button
    private lateinit var tvPickedProduct: TextView
    private lateinit var edtQty: EditText
    private lateinit var btnAdd: Button
    private lateinit var rv: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnSave: Button

    private lateinit var db: AppDatabase
    private lateinit var repo: InvoiceRepository

    private var customers: List<CustomerEntity> = emptyList()
    private var products: List<ProductEntity> = emptyList()

    private var pickedCustomer: CustomerEntity? = null
    private var pickedProduct: ProductEntity? = null

    private val lines: MutableList<DraftLine> = mutableListOf()
    private lateinit var adapter: DraftLinesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ chống crash: nếu layout/id mismatch thì không văng app, hiện toast + log
        try {
            setControl(view)
            setEvent()
            loadData()
        } catch (e: Exception) {
            Log.e("UserExportFragment", "Crash prevented: ${e.message}", e)
            Toast.makeText(requireContext(), "Lỗi giao diện Xuất hàng: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setControl(view: View) {
        toolbarExport = view.findViewById(R.id.toolbarExport)
        btnOpenOutList = view.findViewById(R.id.btnOpenOutList)

        btnPickCustomer = view.findViewById(R.id.btnPickCustomer)
        tvPickedCustomer = view.findViewById(R.id.tvPickedCustomer)

        btnPickProduct = view.findViewById(R.id.btnPickProduct)
        tvPickedProduct = view.findViewById(R.id.tvPickedProduct)

        edtQty = view.findViewById(R.id.edtQty)
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
        toolbarExport.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    SessionManager(requireContext()).logout()
                    val i = Intent(requireContext(), LoginActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                    requireActivity().finish()
                    true
                }
                else -> false
            }
        }

        btnOpenOutList.setOnClickListener {
            startActivity(Intent(requireContext(), UserOutInvoicesActivity::class.java))
        }

        btnPickCustomer.setOnClickListener { showPickCustomerDialog() }
        btnPickProduct.setOnClickListener { showPickProductDialog() }

        btnAdd.setOnClickListener {
            pickedCustomer ?: run { toast("Chưa chọn khách hàng"); return@setOnClickListener }
            val p = pickedProduct ?: run { toast("Chưa chọn sản phẩm"); return@setOnClickListener }

            val qty = edtQty.text.toString().trim().toIntOrNull()
            if (qty == null || qty <= 0) { toast("Số lượng không hợp lệ"); return@setOnClickListener }

            // ✅ Không nhập giá: lấy từ product.price
            lines.add(DraftLine(p.id, p.name, p.unit, qty, p.price))
            adapter.setData(lines)
            edtQty.setText("")
            updateTotal()
        }

        btnSave.setOnClickListener {
            val c = pickedCustomer ?: run { toast("Chưa chọn khách hàng"); return@setOnClickListener }
            if (lines.isEmpty()) { toast("Chưa có dòng hàng"); return@setOnClickListener }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    repo.createOutInvoice(c.id, lines, note = null)
                    withContext(Dispatchers.Main) {
                        toast("Đã lưu phiếu xuất")
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

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            customers = db.customerDao().getAll()
            products = db.productDao().getAll()
        }
    }

    private fun showPickCustomerDialog() {
        if (customers.isEmpty()) { toast("Chưa có khách hàng"); return }
        val names = customers.map { "${it.shopName} • ${it.phone}" }.toTypedArray()
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Chọn khách hàng")
            .setItems(names) { _, which ->
                pickedCustomer = customers[which]
                tvPickedCustomer.text = "Đã chọn: ${pickedCustomer!!.shopName} (${pickedCustomer!!.phone})"
            }.show()
    }

    private fun showPickProductDialog() {
        if (products.isEmpty()) { toast("Chưa có sản phẩm"); return }
        val names = products.map { "${it.name} (${it.code}) • tồn ${it.stockQty} ${it.unit}" }.toTypedArray()
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Chọn sản phẩm")
            .setItems(names) { _, which ->
                pickedProduct = products[which]
                tvPickedProduct.text = "Đã chọn: ${pickedProduct!!.name} • Giá: ${vnd(pickedProduct!!.price)}"
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

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
