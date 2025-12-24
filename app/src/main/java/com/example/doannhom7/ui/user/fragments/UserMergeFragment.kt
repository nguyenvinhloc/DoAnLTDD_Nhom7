package com.example.doannhom7.ui.user.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.CustomerEntity
import com.example.doannhom7.data.repo.InvoiceRepository
import com.example.doannhom7.ui.user.adapters.MergeInvoicesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserMergeFragment : Fragment(R.layout.fragment_user_merge) {

    private lateinit var btnPick: Button
    private lateinit var tvPicked: TextView
    private lateinit var rv: RecyclerView
    private lateinit var btnMerge: Button

    private lateinit var db: AppDatabase
    private lateinit var repo: InvoiceRepository

    private var customers: List<CustomerEntity> = emptyList()
    private var picked: CustomerEntity? = null

    private lateinit var adapter: MergeInvoicesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControl(view)
        setEvent()
        loadCustomers()
    }

    private fun setControl(view: View) {
        btnPick = view.findViewById(R.id.btnPickCustomer)
        tvPicked = view.findViewById(R.id.tvPickedCustomer)
        rv = view.findViewById(R.id.rvInvoices)
        btnMerge = view.findViewById(R.id.btnMerge)

        db = AppDatabase.getInstance(requireContext())
        repo = InvoiceRepository(db)

        adapter = MergeInvoicesAdapter(emptyList())
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
    }

    private fun setEvent() {
        btnPick.setOnClickListener { showPickCustomerDialog() }

        btnMerge.setOnClickListener {
            val c = picked ?: run { toast("Chưa chọn khách"); return@setOnClickListener }
            val ids = adapter.getPickedIds()
            if (ids.size < 2) { toast("Chọn ít nhất 2 đơn để gộp"); return@setOnClickListener }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val mergeId = repo.mergeOutInvoices(c.id, ids)
                    withContext(Dispatchers.Main) {
                        toast("Đã gộp đơn → tạo phiếu MERGE #$mergeId")
                        loadOpenInvoices()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { toast("Lỗi: ${e.message}") }
                }
            }
        }
    }

    private fun loadCustomers() {
        lifecycleScope.launch(Dispatchers.IO) {
            customers = db.customerDao().getAll()
            withContext(Dispatchers.Main) { /* ok */ }
        }
    }

    private fun showPickCustomerDialog() {
        if (customers.isEmpty()) { toast("Chưa có khách"); return }
        val names = customers.map { "${it.shopName} • ${it.phone}" }.toTypedArray()
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Chọn khách hàng")
            .setItems(names) { _, which ->
                picked = customers[which]
                tvPicked.text = "Đã chọn: ${picked!!.shopName} (${picked!!.phone})"
                loadOpenInvoices()
            }.show()
    }

    private fun loadOpenInvoices() {
        val c = picked ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val list = db.invoiceDao().getOpenOutInvoicesByCustomer(c.id)
            withContext(Dispatchers.Main) {
                adapter.submit(list)
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
