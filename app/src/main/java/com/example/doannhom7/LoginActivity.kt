package com.example.doannhom7

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.UserRole
import com.example.doannhom7.data.seed.DatabaseSeeder
import com.example.doannhom7.ui.admin.AdminMainActivity
import com.example.doannhom7.ui.user.UserMainActivity
import com.example.doannhom7.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView

    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setControl()
        setEvent()

        // seed dữ liệu ban đầu
        lifecycleScope.launch(Dispatchers.IO) {
            DatabaseSeeder.seedIfEmpty(db)
        }

        // nếu đã login thì vào thẳng
        if (session.isLoggedIn()) {
            goByRole(session.getRole())
        }
    }

    private fun setControl() {
        edtUsername = findViewById(R.id.edtUsername)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)

        db = AppDatabase.getInstance(this)
        session = SessionManager(this)
    }

    private fun setEvent() {
        btnLogin.setOnClickListener {
            tvError.visibility = View.GONE

            val u = edtUsername.text.toString().trim()
            val p = edtPassword.text.toString().trim()

            if (u.isEmpty() || p.isEmpty()) {
                showError("Vui lòng nhập tài khoản và mật khẩu")
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val user = db.userDao().login(u, p)

                withContext(Dispatchers.Main) {
                    if (user == null) {
                        showError("Sai tài khoản hoặc mật khẩu")
                    } else {
                        session.saveLogin(user.id, user.role)
                        goByRole(user.role)
                    }
                }
            }
        }
    }

    private fun showError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }

    private fun goByRole(role: UserRole?) {
        when (role) {
            UserRole.ADMIN -> {
                startActivity(Intent(this, AdminMainActivity::class.java))
                finish()
            }
            UserRole.USER -> {
                startActivity(Intent(this, UserMainActivity::class.java))
                finish()
            }
            else -> {}
        }
    }
}
