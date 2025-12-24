package com.example.doannhom7.ui.admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.entity.ProductEntity
import java.text.NumberFormat
import java.util.Locale

class ProductsAdapter(
    private var data: List<ProductEntity>,
    private val onClick: (ProductEntity) -> Unit,
    private val onLongClick: (ProductEntity) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.VH>() {

    fun submit(list: List<ProductEntity>) {
        data = list
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvSub: TextView = v.findViewById(R.id.tvSub)
        val tvRight: TextView = v.findViewById(R.id.tvRight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = data[position]
        holder.tvName.text = "${p.name} (${p.code})"
        holder.tvSub.text = "ĐVT: ${p.unit} • Tồn: ${p.stockQty}"
        holder.tvRight.text = vnd(p.price)

        holder.itemView.setOnClickListener { onClick(p) }
        holder.itemView.setOnLongClickListener {
            onLongClick(p); true
        }
    }

    override fun getItemCount(): Int = data.size

    private fun vnd(x: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return nf.format(x) + " đ"
    }
}
