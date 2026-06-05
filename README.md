

Thành viên:
Phương Thảo
Đăng Huy
Tấn Vũ 
Chí Nhân

# NHOM_5_LAB211_FlashSale

## 📌 Giới thiệu
Dự án mô phỏng **Flash Sale Concurrency** bằng Java 17.  
Mục tiêu là xây dựng hệ thống theo kiến trúc **MVC**, sử dụng **ExecutorService** + **CountDownLatch** để mô phỏng hàng nghìn người dùng đồng thời, lưu trữ dữ liệu bằng **CSV**, và benchmark các cơ chế lock khác nhau.

---

## 🎯 Mục tiêu
- Mô phỏng 1000+ người dùng cùng đặt hàng trong sự kiện flash sale.
- Đảm bảo tính **concurrency** và kiểm soát stock không bị âm.
- So sánh hiệu năng giữa các cơ chế lock:  
  - NO_LOCK  
  - SYNCHRONIZED  
  - FILE_LOCK  
  - OPTIMISTIC_LOCK
- Đo **TPS (Transactions per Second)**, **latency**, **retry rate**.
- Xuất kết quả ra **CSV** và hiển thị trên console bằng bảng ASCII.

---

## 📂 Cấu trúc Project
- **model/**: Entity (BaseEntity, Product, FlashSaleEvent, FlashSaleItem, Customer, Order, OrderDetail, OrderTransaction, Enum)
- **repository/**: CsvRepository, FlashSaleItemRepository
- **controller/**: OrderController, SimulatorController
- **view/**: SimulatorView, Logger
- **README.md**: Tài liệu hướng dẫn

---

## ⚙️ Yêu cầu
- Java 17
- IDE: NetBeans / IntelliJ / Eclipse

---

## 🚀 Compile & Run
### Compile
```bash
javac -d bin src/**/*.java

