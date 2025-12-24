package com.example.doannhom7.data.db

import androidx.room.TypeConverter
import com.example.doannhom7.data.entity.InvoiceStatus
import com.example.doannhom7.data.entity.InvoiceType
import com.example.doannhom7.data.entity.UserRole

class Converters {

    // UserRole <-> String
    @TypeConverter
    fun toUserRole(v: String?): UserRole? = v?.let { UserRole.valueOf(it) }

    @TypeConverter
    fun fromUserRole(v: UserRole?): String? = v?.name

    // InvoiceType <-> String  (OUT / MERGE / IN ...)
    @TypeConverter
    fun toInvoiceType(v: String?): InvoiceType? = v?.let { InvoiceType.valueOf(it) }

    @TypeConverter
    fun fromInvoiceType(v: InvoiceType?): String? = v?.name

    // InvoiceStatus <-> String (OPEN / PAID / MERGED ...)
    @TypeConverter
    fun toInvoiceStatus(v: String?): InvoiceStatus? = v?.let { InvoiceStatus.valueOf(it) }

    @TypeConverter
    fun fromInvoiceStatus(v: InvoiceStatus?): String? = v?.name
}
