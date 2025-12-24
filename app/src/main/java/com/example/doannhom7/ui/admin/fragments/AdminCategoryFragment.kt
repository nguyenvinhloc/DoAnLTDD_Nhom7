package com.example.doannhom7.ui.admin.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.CategoryEntity
import com.example.doannhom7.ui.admin.AdminCategoryFormActivity
import com.example.doannhom7.ui.admin.adapters.CategoriesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminCategoryFragment : Fragment(R.layout.fragment_admin_category) {

    private lateinit var edtSearch: EditText
    private lateinit var btnAdd: View
    private lateinit var rv: RecyclerView

    private lateinit var adapter: CategoriesAdapter
    private lateinit var db: AppDatabase

    private var searchJob: Job? = null
    private var lastKeyword: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControl(view)
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        loadData(lastKeyword)
    }

    private fun setControl(view: View) {
        edtSearch = view.findViewById(R.id.edtSearchCategory)
        btnAdd = view.findViewById(R.id.btnAddCategory)
        rv = view.findViewById(R.id.rvCategories)

        db = AppDatabase.getInstance(requireContext())

        adapter = CategoriesAdapter(
            data = emptyList(),
            onClick = { c ->
                val i = Intent(requireContext(), AdminCategoryFormActivity::class.java)
                i.putExtra("categoryId", c.id)
                startActivity(i)
            },
            onLongClick = { c -> confirmDelete(c) }
        )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
    }

    private fun setEvent() {
        btnAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AdminCategoryFormActivity::class.java))
        }

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val k = s?.toString()?.trim()
                lastKeyword = k

                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(250)
                    loadData(k)
                }
            }
        })
    }

    private fun loadData(keyword: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = if (keyword.isNullOrEmpty()) {
                db.categoryDao().getAll()
            } else {
                db.categoryDao().search(keyword)
            }
            withContext(Dispatchers.Main) {
                adapter.submit(list)
            }
        }
    }

    private fun confirmDelete(c: CategoryEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa loại hàng?")
            .setMessage("Xóa: ${c.name}\n\nLưu ý: nếu đang có sản phẩm thuộc loại này, Room sẽ không cho xóa.")
            .setPositiveButton("Xóa") { _, _ -> doDelete(c) }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun doDelete(c: CategoryEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.categoryDao().delete(c)
                val list = db.categoryDao().getAll()
                withContext(Dispatchers.Main) {
                    adapter.submit(list)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Không thể xóa")
                        .setMessage("Loại hàng đang được dùng trong sản phẩm. Hãy chuyển sản phẩm sang loại khác rồi xóa.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
}
