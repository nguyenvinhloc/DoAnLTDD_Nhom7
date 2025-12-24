package com.example.doannhom7.ui.user.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.doannhom7.R
import com.example.doannhom7.ui.user.UserMainActivity

class UserInboundFragment : Fragment(R.layout.fragment_user_inbound) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tvUserInbound).text = "USER - Nhập hàng"
        view.findViewById<Button>(R.id.btnLogoutUser).setOnClickListener {
            (requireActivity() as UserMainActivity).doLogout()
        }
    }
}
