package com.example.doannhom7.ui.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.doannhom7.R
import com.example.doannhom7.LoginActivity
import com.example.doannhom7.ui.user.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserMainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        setControl()
        setEvent()

        // Fragment mặc định khi vào màn User
        supportFragmentManager.beginTransaction()
            .replace(R.id.userFragmentContainer, UserImportFragment())
            .commit()
    }

    private fun setControl() {
        bottomNav = findViewById(R.id.userBottomNav)
    }

    private fun setEvent() {
        bottomNav.setOnItemSelectedListener { item ->
            val f = when (item.itemId) {
                R.id.nav_in -> UserImportFragment()
                R.id.nav_out -> UserExportFragment()
                R.id.nav_pay -> UserPaymentFragment()
                R.id.nav_debt -> UserDebtFragment()
                R.id.nav_merge -> UserMergeFragment()
                else -> UserImportFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.userFragmentContainer, f)
                .commit()

            true
        }
    }

    // ✅ Hàm logout để Fragment gọi
    fun doLogout() {
        // Nếu bạn có lưu trạng thái đăng nhập bằng SharedPreferences thì clear ở đây
        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Điều hướng về màn Login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
