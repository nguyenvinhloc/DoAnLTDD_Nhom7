package com.example.doannhom7.ui.admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.entity.CategoryEntity

class CategoriesAdapter(
    private var data: List<CategoryEntity>,
    private val onClick: (CategoryEntity) -> Unit,
    private val onLongClick: (CategoryEntity) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.VH>() {

    fun submit(newData: List<CategoryEntity>) {
        data = newData
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvNote: TextView = v.findViewById(R.id.tvNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = data[position]
        holder.tvName.text = c.name
        holder.tvNote.text = c.note ?: "(Không có ghi chú)"

        holder.itemView.setOnClickListener { onClick(c) }
        holder.itemView.setOnLongClickListener {
            onLongClick(c)
            true
        }
    }

    override fun getItemCount(): Int = data.size
}
