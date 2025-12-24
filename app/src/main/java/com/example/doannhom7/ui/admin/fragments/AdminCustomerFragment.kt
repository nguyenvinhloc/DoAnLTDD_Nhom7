package com.example.doannhom7.ui.admin.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.CustomerEntity
import com.example.doannhom7.ui.admin.AdminCustomerFormActivity
import com.example.doannhom7.ui.admin.adapters.CustomersAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminCustomerFragment : Fragment(R.layout.fragment_admin_customers) {

    private lateinit var edtSearch: EditText
    private lateinit var rv: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    private lateinit var db: AppDatabase
    private lateinit var adapter: CustomersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControl(view)
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        loadAll()
    }

    private fun setControl(view: View) {
        edtSearch = view.findViewById(R.id.edtSearch)
        rv = view.findViewById(R.id.rvCustomers)
        fabAdd = view.findViewById(R.id.fabAddCustomer)

        db = AppDatabase.getInstance(requireContext())

        adapter = CustomersAdapter(
            data = emptyList(),
            onClick = { c ->
                val i = Intent(requireContext(), AdminCustomerFormActivity::class.java)
                i.putExtra("customerId", c.id)
                startActivity(i)
            },
            onLongClick = { c -> askDelete(c) }
        )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
    }

    private fun setEvent() {
        fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AdminCustomerFormActivity::class.java))
        }

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString()?.trim().orEmpty()
                if (q.isEmpty()) loadAll() else search(q)
            }
        })
    }

    private fun loadAll() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = db.customerDao().getAll()
            withContext(Dispatchers.Main) { adapter.submit(list) }
        }
    }

    private fun search(q: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = db.customerDao().search("%$q%")
            withContext(Dispatchers.Main) { adapter.submit(list) }
        }
    }

    private fun askDelete(c: CustomerEntity) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Xoá khách hàng?")
            .setMessage("Xoá: ${c.shopName}?")
            .setPositiveButton("Xoá") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    db.customerDao().delete(c)
                    withContext(Dispatchers.Main) {
                        toast("Đã xoá")
                        loadAll()
                    }
                }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
