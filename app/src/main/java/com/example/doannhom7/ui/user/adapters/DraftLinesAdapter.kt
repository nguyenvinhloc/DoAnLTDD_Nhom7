package com.example.doannhom7.ui.user.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.repo.DraftLine
import java.text.NumberFormat
import java.util.Locale

class DraftLinesAdapter(
    private var data: List<DraftLine>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<DraftLinesAdapter.VH>() {

    fun setData(newData: List<DraftLine>) {
        data = newData
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvSub: TextView = v.findViewById(R.id.tvSub)
        val btnRemove: Button = v.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_draft_line, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val it = data[position]
        holder.tvName.text = it.name
        holder.tvSub.text = "SL: ${it.qty} ${it.unit} | Giá: ${vnd(it.unitPrice)} | Thành tiền: ${vnd(it.lineTotal)}"
        holder.btnRemove.setOnClickListener { onRemove(position) }
    }

    override fun getItemCount(): Int = data.size

    private fun vnd(x: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return nf.format(x) + " đ"
    }
}
