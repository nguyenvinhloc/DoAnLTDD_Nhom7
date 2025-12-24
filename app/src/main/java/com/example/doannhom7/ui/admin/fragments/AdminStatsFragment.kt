package com.example.doannhom7.ui.admin.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class AdminStatsFragment : Fragment(R.layout.fragment_admin_stats) {

    private lateinit var tv1: TextView
    private lateinit var tv2: TextView
    private lateinit var tv3: TextView
    private lateinit var tv4: TextView
    private lateinit var tv5: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnLogout: Button
    private lateinit var db: AppDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControl(view)
        setEvent()
        loadStats()
    }

    private fun setControl(view: View) {
        tv1 = view.findViewById(R.id.tvStat1)
        tv2 = view.findViewById(R.id.tvStat2)
        tv3 = view.findViewById(R.id.tvStat3)
        tv4 = view.findViewById(R.id.tvStat4)
        tv5 = view.findViewById(R.id.tvStat5)
        btnRefresh = view.findViewById(R.id.btnRefresh)
        btnLogout = view.findViewById(R.id.btnLogout)
        db = AppDatabase.getInstance(requireContext())
    }

    private fun setEvent() {
        btnRefresh.setOnClickListener { loadStats() }
        btnLogout.setOnClickListener {
            val session = com.example.doannhom7.utils.SessionManager(requireContext())
            session.logout()

            // về login và clear backstack
            val i = android.content.Intent(requireContext(), com.example.doannhom7.LoginActivity::class.java)
            i.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }

    }

    private fun loadStats() {
        lifecycleScope.launch(Dispatchers.IO) {
            val c1 = db.categoryDao().count()
            val c2 = db.productDao().count()
            val c3 = db.customerDao().count()
            val stock = db.productDao().sumStock()
            val stockValue = db.productDao().sumStockValue()

            withContext(Dispatchers.Main) {
                tv1.text = "Loại hàng: $c1"
                tv2.text = "Sản phẩm: $c2"
                tv3.text = "Khách hàng: $c3"
                tv4.text = "Tổng tồn kho: $stock"
                tv5.text = "Giá trị tồn kho: ${vnd(stockValue)}"
            }
        }
    }


    private fun vnd(x: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return nf.format(x) + " đ"
    }
}
