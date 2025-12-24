package com.example.doannhom7.ui.user.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.dao.OutInvoiceWithCustomer
import java.text.NumberFormat
import java.util.Locale

class OutInvoicesAdapter(
    private var data: List<OutInvoiceWithCustomer>,
    private val onClick: (Long) -> Unit,
    private val onLongDelete: (Long) -> Unit
) : RecyclerView.Adapter<OutInvoicesAdapter.VH>() {

    fun submit(newData: List<OutInvoiceWithCustomer>) {
        data = newData
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTop: TextView = v.findViewById(R.id.tvTop)
        val tvMid: TextView = v.findViewById(R.id.tvMid)
        val tvBottom: TextView = v.findViewById(R.id.tvBottom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_out_invoice, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val row = data[position]
        val inv = row.invoice
        val cus = row.customer

        holder.tvTop.text = "Phiếu #${inv.id} • ${inv.status.name}"
        holder.tvMid.text = "Khách: ${cus?.shopName ?: "?"} • ${cus?.phone ?: ""}"
        holder.tvBottom.text = "Tổng: ${vnd(inv.totalAmount)}"

        holder.itemView.setOnClickListener { onClick(inv.id) }
        holder.itemView.setOnLongClickListener { onLongDelete(inv.id); true }
    }

    override fun getItemCount(): Int = data.size

    private fun vnd(x: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return nf.format(x) + " đ"
    }
}
