package com.example.doannhom7.ui.base

import androidx.appcompat.app.AppCompatActivity
import com.example.doannhom7.utils.LogoutHelper
import com.google.android.material.appbar.MaterialToolbar

open class BaseToolbarActivity : AppCompatActivity() {

    protected fun setupToolbar(
        toolbar: MaterialToolbar,
        titleText: String,
        showBack: Boolean = true
    ) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = titleText

        if (showBack) {
            toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }

        LogoutHelper.attach(this, toolbar)
    }
}
