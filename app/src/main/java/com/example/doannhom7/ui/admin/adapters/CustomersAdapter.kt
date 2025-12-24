package com.example.doannhom7.ui.admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doannhom7.R
import com.example.doannhom7.data.entity.CustomerEntity

class CustomersAdapter(
    private var data: List<CustomerEntity>,
    private val onClick: (CustomerEntity) -> Unit,
    private val onLongClick: (CustomerEntity) -> Unit
) : RecyclerView.Adapter<CustomersAdapter.VH>() {

    fun submit(list: List<CustomerEntity>) {
        data = list
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvShop: TextView = v.findViewById(R.id.tvShop)
        val tvSub: TextView = v.findViewById(R.id.tvSub)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_customer, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = data[position]
        holder.tvShop.text = c.shopName
        holder.tvSub.text = "Chủ: ${c.ownerName} • SĐT: ${c.phone}\nĐC: ${c.address}"

        holder.itemView.setOnClickListener { onClick(c) }
        holder.itemView.setOnLongClickListener {
            onLongClick(c); true
        }
    }

    override fun getItemCount(): Int = data.size
}
