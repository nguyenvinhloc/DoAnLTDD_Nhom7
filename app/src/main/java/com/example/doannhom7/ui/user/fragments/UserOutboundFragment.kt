package com.example.doannhom7.ui.user.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.doannhom7.R

class UserOutboundFragment : Fragment(R.layout.fragment_user_outbound) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tvUserOutbound).text = "USER - Xuất hàng"
    }
}
