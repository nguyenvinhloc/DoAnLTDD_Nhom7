package com.example.doannhom7.utils

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.example.doannhom7.LoginActivity
import com.example.doannhom7.R
import com.google.android.material.appbar.MaterialToolbar

object LogoutHelper {

    fun attach(activity: FragmentActivity, toolbar: MaterialToolbar) {
        // Nếu toolbar chưa có menu thì inflate
        if (toolbar.menu.findItem(R.id.action_logout) == null) {
            toolbar.inflateMenu(R.menu.menu_logout)
        }

        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) {
                SessionManager(activity).logout()
                val i = Intent(activity, LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(i)
                activity.finish()
                true
            } else false
        }
    }
}
