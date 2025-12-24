package com.example.doannhom7.ui.user.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.dao.CustomerDebtRow
import java.text.NumberFormat
import java.util.Locale

class DebtAdapter(
    private var data: List<CustomerDebtRow>,
    private val onClick: (CustomerDebtRow) -> Unit
) : RecyclerView.Adapter<DebtAdapter.VH>() {

    fun submit(newData: List<CustomerDebtRow>) {
        data = newData
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvShop: TextView = v.findViewById(R.id.tvShop)
        val tvSub: TextView = v.findViewById(R.id.tvSub)
        val tvDebt: TextView = v.findViewById(R.id.tvDebt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_debt_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = data[position]
        holder.tvShop.text = r.shopName
        holder.tvSub.text = "SĐT: ${r.phone} • Tổng mua: ${vnd(r.totalOut)} • Đã trả: ${vnd(r.totalPaid)}"
        holder.tvDebt.text = "Công nợ: ${vnd(r.debt)}"
        holder.itemView.setOnClickListener { onClick(r) }
    }

    override fun getItemCount(): Int = data.size

    private fun vnd(x: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return nf.format(x) + " đ"
    }
}
