package com.example.doannhom7.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.doannhom7.LoginActivity
import com.example.doannhom7.R
import com.example.doannhom7.ui.admin.fragments.AdminCategoryFragment
import com.example.doannhom7.ui.admin.fragments.AdminCustomerFragment
import com.example.doannhom7.ui.admin.fragments.AdminProductFragment
import com.example.doannhom7.ui.admin.fragments.AdminStatsFragment
import com.example.doannhom7.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminMainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        setControl()
        setEvent()

        // mặc định mở tab Loại hàng
        openFragment(AdminCategoryFragment())
    }

    private fun setControl() {
        bottomNav = findViewById(R.id.adminBottomNav)
    }

    private fun setEvent() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_category -> openFragment(AdminCategoryFragment())
                R.id.nav_admin_product -> openFragment(AdminProductFragment())
                R.id.nav_admin_customer -> openFragment(AdminCustomerFragment())
                R.id.nav_admin_stats -> openFragment(AdminStatsFragment())
            }
            true
        }
    }

    private fun openFragment(f: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, f)
            .commit()
    }

    // tiện test: nếu bạn muốn đăng xuất bằng nút trong fragment -> gọi hàm này
    fun doLogout() {
        SessionManager(this).logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
