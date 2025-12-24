package com.example.doannhom7.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.repo.InvoiceRepository
import com.example.doannhom7.ui.user.adapters.OutInvoicesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserOutInvoicesActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var db: AppDatabase
    private lateinit var repo: InvoiceRepository
    private lateinit var adapter: OutInvoicesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_out_invoices)

        rv = findViewById(R.id.rvOutInvoices)
        db = AppDatabase.getInstance(this)
        repo = InvoiceRepository(db)

        adapter = OutInvoicesAdapter(
            data = emptyList(),
            onClick = { id ->
                val i = Intent(this, UserOutInvoiceEditActivity::class.java)
                i.putExtra("invoiceId", id)
                startActivity(i)
            },
            onLongDelete = { id -> confirmDelete(id) }
        )

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = db.invoiceDao().getOutInvoicesWithCustomer()
            withContext(Dispatchers.Main) { adapter.submit(list) }
        }
    }

    private fun confirmDelete(invoiceId: Long) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Xóa phiếu #$invoiceId?")
            .setMessage("Xóa sẽ hoàn kho lại.")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        repo.deleteOutInvoice(invoiceId)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@UserOutInvoicesActivity, "Đã xóa phiếu", Toast.LENGTH_SHORT).show()
                            load()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@UserOutInvoicesActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
