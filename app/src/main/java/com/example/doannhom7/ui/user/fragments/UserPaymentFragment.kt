package com.example.doannhom7.ui.user.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.doannhom7.R
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.CustomerEntity
import com.example.doannhom7.data.repo.PaymentRepository
import com.example.doannhom7.utils.LogoutHelper
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class UserPaymentFragment : Fragment(R.layout.fragment_user_payment) {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnPickCustomer: Button
    private lateinit var tvPickedCustomer: TextView
    private lateinit var edtAmount: EditText
    private lateinit var edtNote: EditText
    private lateinit var btnPay: Button
    private lateinit var tvHintDebt: TextView

    private lateinit var db: AppDatabase
    private lateinit var repo: PaymentRepository

    private var customers: List<CustomerEntity> = emptyList()
    private var picked: CustomerEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControl(view)
        setEvent()
        loadCustomers()
    }

    private fun setControl(view: View) {
        toolbar = view.findViewById(R.id.toolbarPayment)
        LogoutHelper.attach(requireActivity(), toolbar)

        btnPickCustomer = view.findViewById(R.id.btnPickCustomer)
        tvPickedCustomer = view.findViewById(R.id.tvPickedCustomer)
        edtAmount = view.findViewById(R.id.edtAmount)
        edtNote = view.findViewById(R.id.edtNote)
        btnPay = view.findViewById(R.id.btnPay)
        tvHintDebt = view.findViewById(R.id.tvHintDebt)

        db = AppDatabase.getInstance(requireContext())
        repo = PaymentRepository(db)

        tvHintDebt.text = "Chọn khách để xem công nợ hiện tại"
    }

    private fun setEvent() {
        btnPickCustomer.setOnClickListener { showPickCustomerDialog() }

        btnPay.setOnClickListener {
            val c = picked ?: run { toast("Chưa chọn khách hàng"); return@setOnClickListener }

            val amount = edtAmount.text.toString().trim().replace(",", "").toLongOrNull()
            if (amount == null || amount <= 0) { toast("Số tiền không hợp lệ"); return@setOnClickListener }

            val note = edtNote.text.toString().trim().ifEmpty { null }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    repo.pay(c.id, amount, note)

                    // load lại công nợ sau khi trả
                    val openDebt = db.invoiceDao().getOpenDebtTotalByCustomer(c.id)
                    val paidTotal = db.paymentDao().getPaidTotalByCustomer(c.id)

                    withContext(Dispatchers.Main) {
                        toast("Đã thanh toán: ${vnd(amount)}")
                        edtAmount.setText("")
                        edtNote.setText("")
                        tvHintDebt.text = "Đã trả: ${vnd(paidTotal)} • Nợ OPEN còn: ${vnd(openDebt)}"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { toast("Lỗi: ${e.message}") }
                }
            }
        }
    }

    private fun loadCustomers() {
        lifecycleScope.launch(Dispatchers.IO) {
            customers = db.customerDao().getAll()
        }
    }

    private fun showPickCustomerDialog() {
        if (customers.isEmpty()) { toast("Chưa có khách hàng"); return }

        val names = customers.map { "${it.shopName} • ${it.phone}" }.toTypedArray()
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Chọn khách hàng")
            .setItems(names) { _, which ->
                picked = customers[which]
                tvPickedCustomer.text = "Đã chọn: ${picked!!.shopName} (${picked!!.phone})"

                // show nợ hiện tại
                lifecycleScope.launch(Dispatchers.IO) {
                    val openDebt = db.invoiceDao().getOpenDebtTotalByCustomer(picked!!.id)
                    val paidTotal = db.paymentDao().getPaidTotalByCustomer(picked!!.id)
                    withContext(Dispatchers.Main) {
                        tvHintDebt.text = "Đã trả: ${vnd(paidTotal)} • Nợ OPEN hiện tại: ${vnd(openDebt)}"
                    }
                }
            }
            .show()
    }

    private fun vnd(x: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return nf.format(x) + " đ"
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
