package com.example.doannhom7.data.repo

import androidx.room.withTransaction
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.PaymentEntity

class PaymentRepository(private val db: AppDatabase) {

    /**
     * Thanh toán cho 1 khách:
     * - Insert payment
     * - Nếu tổng đã trả >= tổng nợ OPEN hiện tại => mark invoice OPEN -> PAID
     */
    suspend fun pay(customerId: Long, amount: Long, note: String? = null) {
        require(amount > 0)

        db.withTransaction {
            // lưu lịch sử trả
            db.paymentDao().insert(
                PaymentEntity(
                    customerId = customerId,
                    amount = amount,
                    note = note
                )
            )

            // kiểm tra trả đủ chưa
            val openDebt = db.invoiceDao().getOpenDebtTotalByCustomer(customerId)
            val paidTotal = db.paymentDao().getPaidTotalByCustomer(customerId)

            // nếu tổng đã trả >= nợ OPEN => đóng các phiếu OPEN
            if (paidTotal >= openDebt && openDebt > 0) {
                db.invoiceDao().markOutPaidByCustomer(customerId)
            }
        }
    }
}
