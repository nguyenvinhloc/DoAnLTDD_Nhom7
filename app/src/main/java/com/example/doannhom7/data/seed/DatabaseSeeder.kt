package com.example.doannhom7.data.seed

import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.CategoryEntity
import com.example.doannhom7.data.entity.CustomerEntity
import com.example.doannhom7.data.entity.ProductEntity
import com.example.doannhom7.data.entity.UserRole
import com.example.doannhom7.data.entity.UserEntity

object DatabaseSeeder {

    suspend fun seedIfEmpty(db: AppDatabase) {

        if (db.userDao().count() == 0) {
            db.userDao().insert(UserEntity(username = "owner", password = "123456", role = UserRole.ADMIN))
            db.userDao().insert(UserEntity(username = "staff", password = "123456", role = UserRole.USER))
        }

        val catDao = db.categoryDao()
        val proDao = db.productDao()
        val cusDao = db.customerDao()

        if (catDao.count() == 0) {
            val ids = mutableMapOf<String, Long>()
            val catNames = listOf(
                "Dây - Cáp điện",
                "Thiết bị nước",
                "Thiết bị điện",
                "Vật tư - Dụng cụ",
                "Ống nước - Phụ kiện",
                "Đèn - Chiếu sáng"
            )
            catNames.forEach { name ->
                val id = catDao.insert(CategoryEntity(name = name))
                ids[name] = id
            }

            // Products
            val p = listOf(
                ProductEntity(categoryId = ids["Dây - Cáp điện"]!!, name="Dây điện Cadivi 2.5mm", code="DAY25", unit="mét", price=12000, stockQty=300),
                ProductEntity(categoryId = ids["Dây - Cáp điện"]!!, name="Dây điện 1.5mm", code="DAY15", unit="mét", price=9000, stockQty=500),
                ProductEntity(categoryId = ids["Ống nước - Phụ kiện"]!!, name="Ống PVC Bình Minh 21", code="OPVC21", unit="cây", price=65000, stockQty=80),
                ProductEntity(categoryId = ids["Ống nước - Phụ kiện"]!!, name="Co PVC 21", code="COPVC21", unit="cái", price=3000, stockQty=400),
                ProductEntity(categoryId = ids["Thiết bị nước"]!!, name="Vòi lavabo inox", code="VOILV01", unit="cái", price=180000, stockQty=35),
                ProductEntity(categoryId = ids["Thiết bị điện"]!!, name="Công tắc 1 chiều", code="CT1C", unit="cái", price=25000, stockQty=120),
                ProductEntity(categoryId = ids["Thiết bị điện"]!!, name="Ổ cắm đôi", code="OC2", unit="cái", price=35000, stockQty=90),
                ProductEntity(categoryId = ids["Đèn - Chiếu sáng"]!!, name="Đèn LED bulb 9W", code="LED9W", unit="cái", price=28000, stockQty=200)
            )
            p.forEach { proDao.insert(it) }

            // Customers
            val c = listOf(
                CustomerEntity(shopName="Cửa hàng Điện Nước Minh Tâm", ownerName="Nguyễn Minh", phone="0901000001", address="Q. Thủ Đức", note="Lấy hàng đều"),
                CustomerEntity(shopName="Điện Nước Hồng Phúc", ownerName="Trần Phúc", phone="0901000002", address="Q.9", note=null),
                CustomerEntity(shopName="Vật tư Xây dựng An Khang", ownerName="Lê An", phone="0901000003", address="Dĩ An", note="Hay lấy ống + phụ kiện"),
                CustomerEntity(shopName="Đại lý Thành Công", ownerName="Phạm Thành", phone="0901000004", address="Bình Thạnh", note=null),
                CustomerEntity(shopName="Shop Điện Nước Phú Mỹ", ownerName="Đặng Mỹ", phone="0901000005", address="Gò Vấp", note="Thanh toán cuối tháng")
            )
            c.forEach { cusDao.insert(it) }
        }
    }
}
