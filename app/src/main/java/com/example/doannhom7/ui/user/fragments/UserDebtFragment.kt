package com.example.doannhom7.ui.user.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.dao.CustomerDebtRow
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.ui.user.adapters.DebtAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class UserDebtFragment : Fragment(R.layout.fragment_user_debt) {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: DebtAdapter
    private lateinit var db: AppDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControl(view)
        setEvent()
        load()
    }

    private fun setControl(view: View) {
        rv = view.findViewById(R.id.rvDebt)
        db = AppDatabase.getInstance(requireContext())

        adapter = DebtAdapter(emptyList()) { row ->
            showDetail(row)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
    }

    private fun setEvent() {}

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = db.debtDao().getCustomerDebts()
            withContext(Dispatchers.Main) { adapter.submit(list) }
        }
    }

    private fun showDetail(r: CustomerDebtRow) {
        val msg = "Tổng mua: ${vnd(r.totalOut)}\nĐã trả: ${vnd(r.totalPaid)}\nCông nợ: ${vnd(r.debt)}"
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("${r.shopName} (${r.phone})")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun vnd(x: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return nf.format(x) + " đ"
    }
}
