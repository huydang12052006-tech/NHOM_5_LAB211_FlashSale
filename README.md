# ⚡ Flash Sale Simulator

### Concurrent Inventory Protection in High-Traffic E-Commerce Systems

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Architecture](https://img.shields.io/badge/Architecture-MVC-blue)
![Storage](https://img.shields.io/badge/Storage-CSV-green)
![Concurrency](https://img.shields.io/badge/Concurrency-MultiThread-red)
![Research](https://img.shields.io/badge/Research-Concurrency%20Analysis-purple)

**LAB211 – Object-Oriented Programming**

**FPT University**

---

### 🚀 E-Commerce Flash Sale Simulation

### Preventing Overselling under Massive Concurrent Orders

</div>

---

# 📖 Project Overview

Modern e-commerce platforms such as Shopee, Lazada, and Amazon process thousands of purchase requests within seconds during Flash Sale events.

When multiple customers attempt to purchase the same limited-stock item simultaneously, race conditions can occur, causing:

* ❌ Overselling
* ❌ Negative inventory
* ❌ Data inconsistency
* ❌ Order cancellation after successful payment
* ❌ Customer dissatisfaction

This project recreates that scenario in a Java-based simulation environment and investigates how different synchronization mechanisms affect both:

* Data correctness
* System throughput

---

# 🔬 Research Question

> **When multiple threads process orders concurrently on the same inventory file, what race conditions occur, and which synchronization mechanism can prevent overselling while maintaining acceptable throughput?**

The project evaluates several concurrency control approaches and measures their effectiveness through practical experiments.

---

# 🎯 Project Objectives

### Functional Goals

✔ Product Management

✔ Flash Sale Event Management

✔ Customer Management

✔ Order Processing

✔ Inventory Management

✔ Transaction Logging

✔ Performance Simulation

---

### Research Goals

✔ Reproduce race conditions

✔ Demonstrate overselling scenarios

✔ Compare synchronization techniques

✔ Measure throughput (TPS)

✔ Analyze trade-offs between consistency and performance

---

# 🏗 System Architecture

The application follows the **MVC (Model – View – Controller)** architecture.

```text
+----------------+
|      VIEW      |
+----------------+
         |
         v
+----------------+
|   CONTROLLER   |
+----------------+
         |
         v
+----------------+
| MODEL / REPO   |
+----------------+
         |
         v
+----------------+
|    CSV FILES   |
+----------------+
```

### Model Layer

Business entities:

* Product
* FlashSaleEvent
* FlashSaleItem
* Customer
* Payment
* User
* Order
* OrderDetail
* OrderTransaction

---

### Repository Layer

Responsible for:

* CSV persistence
* CRUD operations
* Inventory updates
* Synchronization mechanisms

Core repository:

```text
CsvRepository<T>
```

---

### Controller Layer

Coordinates application flow:

* ProductController
* FlashSaleController
* OrderController
* SimulatorController
* AuthController
* PaymentController

---

### View Layer

Console-based user interface:

* MainView
* ProductView
* FlashSaleView
* OrderView
* SimulatorView

---

# ⚡ Core Business Rules

## Inventory Invariant

```text
soldQty ≤ limitedQty
```

Must never be violated.

---

## Purchase Limit

Each customer may purchase:

```text
Maximum 2 units
per flash sale item
```

---

## Version Control

Every inventory update increases:

```text
version++
```

Used for Optimistic Locking.

---

# 🔒 Synchronization Mechanisms

The project implements and compares four different approaches.

---

## 1. NO_LOCK

### Purpose

Baseline implementation.

### Characteristics

* No synchronization
* Maximum throughput
* High risk of race condition

### Expected Result

```text
Overselling possible
Negative stock possible
```

---

## 2. SYNCHRONIZED

### Technique

Java synchronized methods.

### Characteristics

* Simple implementation
* Strong consistency
* Reduced parallelism

### Expected Result

```text
0% overselling
Moderate TPS reduction
```

---

## 3. FILE_LOCK

### Technique

Java NIO FileLock

### Characteristics

* Operating-system level lock
* Protects shared CSV file
* Suitable for external process access

### Expected Result

```text
0% overselling
Higher I/O overhead
```

---

## 4. OPTIMISTIC_LOCK

### Technique

Version-based concurrency control

### Workflow

1. Read version
2. Validate stock
3. Attempt update
4. Compare version
5. Retry if conflict detected

### Expected Result

```text
0% overselling
Highest scalability
Possible retries
```

---

# 🚀 Concurrency Simulator

The simulator reproduces real Flash Sale traffic.

Technologies:

* ExecutorService
* CountDownLatch
* Multi-threading

Simulation scenarios:

```text
100 Threads
200 Threads
500 Threads
1000 Threads
```

Metrics collected:

* Success Orders
* Failed Orders
* TPS
* Retry Count
* Oversell Rate
* Execution Time

---

# 📊 Performance Analysis

The simulator generates transaction logs and enables comparison between mechanisms.

Example metrics:

| Mechanism    | TPS     | Oversell Rate | Retry    |
| ------------ | ------- | ------------- | -------- |
| NO_LOCK      | Highest | High          | 0        |
| SYNCHRONIZED | Medium  | 0%            | 0        |
| FILE_LOCK    | Lower   | 0%            | 0        |
| OPTIMISTIC   | High    | 0%            | Variable |

---

# 📁 Project Structure

```text
src
│
├── model
├── repository
├── controller
├── view
│
data
├── products.csv
├── customers.csv
├── flash_events.csv
├── flash_items.csv
├── orders.csv
├── order_details.csv
└── transactions.csv
│
docs
├── report.docx
├── slide.pptx
├── class_diagram.png
└── flowcharts
│
ai_logs
│
README.md
```

---

# 🧪 Dataset

The system operates entirely on CSV files.

Dataset size:

```text
10,000+ records
```

Generated automatically using:

```java
DataGenerator.java
```

---

# 📈 Expected Contributions

This project demonstrates:

### Object-Oriented Programming

* Inheritance
* Abstraction
* Polymorphism
* Encapsulation

### Software Architecture

* MVC Pattern
* Generic Repository Pattern

### Concurrent Programming

* Race Condition Analysis
* Thread Synchronization
* File Locking
* Optimistic Concurrency Control

### Software Engineering

* Testing
* Benchmarking
* Experimental Evaluation

---

# 👨‍💻 Team Mission

> Build a Flash Sale system that remains correct under extreme concurrency pressure.

Not merely a CRUD application.

A research-oriented simulation that explores the fundamental trade-off between:

### Consistency

and

### Performance

in modern e-commerce systems.

---

# 📌 Final Goal

```text
0% Overselling
0 Negative Inventory

while

Throughput Reduction < 30%
```

---

<div align="center">

### ⚡ Concurrency Bugs Don't Wait For You

### They Wait For Production.

</div>


## 👨‍💻 THÀNH VIÊN NHÓM (GROUP 5)
| STT | Họ và Tên | Vai trò |
|-----|-----------|---------|
| 1   | Đinh Thien Nhan | Product & Flash Sale Module |
| 2   | Pham Phuong Thao | Order & Concurrency Module |
| 3   | Nguyen Dang Huy | Simulator & Research Module |
| 4   | Do Tan Vu | Authentication & Payment Module |



## 1) Compile

Nếu máy bạn có Maven:
```powershell
cd d:\github\NHOM_5_LAB211_FlashSale
mvn -DskipTests compile
```

Nếu môi trường không có Maven, dùng cách sau (đã kiểm tra được trên máy hiện tại):
```powershell
cd d:\github\NHOM_5_LAB211_FlashSale
javac -encoding UTF-8 -d out $(Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName })
```

> Nếu dùng PowerShell trên Windows, lệnh trên sẽ hoạt động tốt. Nếu bạn dùng CMD thì dùng:
```cmd
cd /d d:\github\NHOM_5_LAB211_FlashSale
javac -encoding UTF-8 -d out @<(
  dir /s /b src\*.java
)
```

---

## 2) Chạy DataGenerator

File chính để sinh dữ liệu là DataGenerator.java.

Chạy:
```powershell
cd d:\github\NHOM_5_LAB211_FlashSale
java -cp out DataGenerator
```

Sau khi chạy, dữ liệu sẽ được sinh vào thư mục:
- data

Các file thường dùng gồm:
- products.csv
- flash_events.csv
- flash_items.csv
- customers.csv
- orders.csv
- order_details.csv
- paytransactions.csv
- users.csv

---

## 3) Chạy Simulator

Điểm vào chương trình chính là Main.java.

Chạy:
```powershell
cd d:\github\NHOM_5_LAB211_FlashSale
java -cp out Main
```

Khi chạy, bạn sẽ thấy menu console. Để vào phần Simulator:

1. Chọn “4. Administration”
2. Đăng nhập bằng tài khoản ADMIN
   - Username/Password được tạo từ dữ liệu users.csv
   - Mật khẩu theo quy ước trong code là:
     ```text
     pass + id
     ```
     Ví dụ: nếu user id là U00001 thì mật khẩu là:
     ```text
     passU00001
     ```

3. Trong menu Admin:
   - “5. Configure Thread Count for Simulation”
   - “6. Configure System Lock Mechanism”
   - “7. Run 4-Mechanism Simulation”

---

## 4) Đọc kết quả

Kết quả chạy Simulator sẽ được in trực tiếp trên console và có thể xuất ra file:
- docs/simulation_result.txt

Nếu bạn muốn xem kết quả sau khi chạy, mở file:
```powershell
notepad docs\simulation_result.txt
```

Ngoài ra, kết quả cũng có thể xem qua các báo cáo in ra trong console:
- Dashboard hệ thống
- Inventory report
- Throughput report
- Negative stock report

---

## 5) Lưu ý quan trọng

- Trước khi chạy Simulator, nên chạy DataGenerator trước để có dữ liệu đầu vào.
- Nếu bạn gặp lỗi encoding khi compile, hãy dùng:
```powershell
javac -encoding UTF-8 -d out $(Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName })
``