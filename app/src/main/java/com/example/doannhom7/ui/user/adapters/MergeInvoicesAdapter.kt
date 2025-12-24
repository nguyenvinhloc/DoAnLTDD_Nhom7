package com.example.doannhom7.ui.user.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.entity.InvoiceEntity
import java.text.NumberFormat
import java.util.Locale

class MergeInvoicesAdapter(
    private var data: List<InvoiceEntity>
) : RecyclerView.Adapter<MergeInvoicesAdapter.VH>() {

    private val picked = linkedSetOf<Long>()

    fun submit(newData: List<InvoiceEntity>) {
        data = newData
        picked.clear()
        notifyDataSetChanged()
    }

    fun getPickedIds(): List<Long> = picked.toList()

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cb: CheckBox = v.findViewById(R.id.cbPick)
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvSub: TextView = v.findViewById(R.id.tvSub)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_merge_invoice, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val it = data[position]
        holder.tvTitle.text = "Phiếu #${it.id} • ${it.status.name}"
        holder.tvSub.text = "Tổng: ${vnd(it.totalAmount)}"
        holder.cb.setOnCheckedChangeListener(null)
        holder.cb.isChecked = picked.contains(it.id)

        holder.cb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) picked.add(it.id) else picked.remove(it.id)
        }
        holder.itemView.setOnClickListener {
            holder.cb.isChecked = !holder.cb.isChecked
        }
    }

    override fun getItemCount(): Int = data.size

    private fun vnd(x: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return nf.format(x) + " đ"
    }
}
