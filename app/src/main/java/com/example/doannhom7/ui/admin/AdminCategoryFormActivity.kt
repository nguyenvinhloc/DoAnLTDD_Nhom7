package com.example.doannhom7.ui.admin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.CategoryEntity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminCategoryFormActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var edtName: EditText
    private lateinit var edtNote: EditText
    private lateinit var btnSave: Button

    private lateinit var db: AppDatabase
    private var categoryId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_category_form)

        setControl()
        setEvent()
        loadIfEdit()
    }

    private fun setControl() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        edtName = findViewById(R.id.edtCategoryName)
        edtNote = findViewById(R.id.edtCategoryNote)
        btnSave = findViewById(R.id.btnSaveCategory)

        db = AppDatabase.getInstance(this)
        categoryId = intent.getLongExtra("categoryId", -1L)
    }

    private fun setEvent() {
        btnSave.setOnClickListener { save() }
    }

    private fun loadIfEdit() {
        if (categoryId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val c = db.categoryDao().getById(categoryId)
            withContext(Dispatchers.Main) {
                if (c == null) return@withContext
                edtName.setText(c.name)
                edtNote.setText(c.note ?: "")
            }
        }
    }

    private fun save() {
        val name = edtName.text.toString().trim()
        val note = edtNote.text.toString().trim().ifEmpty { null }

        if (name.isEmpty()) {
            toast("Vui lòng nhập tên loại hàng")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (categoryId == -1L) {
                // INSERT
                db.categoryDao().insert(CategoryEntity(name = name, note = note))
            } else {
                // UPDATE
                db.categoryDao().update(CategoryEntity(id = categoryId, name = name, note = note))
            }

            withContext(Dispatchers.Main) {
                toast("Đã lưu")
                finish()
            }
        }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
