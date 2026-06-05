

Flash Sale Simulation (LAB211)
1. Giới thiệu
Dự án này mô phỏng hệ thống Flash Sale trên sàn thương mại điện tử bằng ngôn ngữ Java theo kiến trúc MVC.
Mục tiêu chính của dự án:

Xử lý đồng thời nhiều đơn hàng trong thời gian ngắn.

Ngăn chặn tình trạng âm kho.

So sánh hiệu quả của các cơ chế đồng bộ hóa khác nhau.

Đo lường hiệu năng hệ thống thông qua chỉ số TPS (Transactions Per Second).

Dữ liệu được lưu trữ bằng CSV thay vì cơ sở dữ liệu truyền thống.

2. Công nghệ sử dụng
Java 17

Kiến trúc MVC

Lưu trữ dữ liệu bằng CSV

Multi-threading

ExecutorService

CountDownLatch

FileLock (Java NIO)

Optimistic Locking

Generic Repository

Console UI

3. Các cơ chế đồng bộ hóa
Hệ thống được triển khai và kiểm thử với bốn cơ chế đồng bộ hóa:

NO_LOCK

Không sử dụng bất kỳ cơ chế đồng bộ nào.

Cho TPS cao nhất nhưng có thể dẫn đến âm kho.

SYNCHRONIZED

Sử dụng synchronized method/block trong Java.

Đảm bảo không âm kho, TPS giảm so với NO_LOCK.

FILE_LOCK

Dùng FileLock của Java NIO để khóa file CSV.

Ngăn âm kho nhưng có overhead lớn do I/O.

OPTIMISTIC_LOCK

Sử dụng trường version để kiểm tra xung đột.

Retry tối đa 3 lần khi có conflict.

TPS tốt hơn synchronized, nhưng có tỷ lệ retry.

4. Cấu trúc thư mục
Code
src/
 ├── model/
 ├── repository/
 ├── controller/
 ├── view/
 ├── exception/
 ├── util/
 ├── simulator/
 └── data/
5. Cách chạy chương trình
Biên dịch
bash
javac -d out $(find src -name "*.java")
Chạy chương trình chính
bash
java -cp out Main
Chạy simulator
bash
java -cp out simulator.SimulatorMain
6. Đo lường hiệu năng
Simulator cho phép kiểm thử hệ thống với nhiều thread đồng thời. Các thông số đo lường bao gồm:

TPS (Transactions Per Second)

Tỷ lệ âm kho

Tỷ lệ retry

Số đơn hàng thất bại

Độ trễ xử lý

Các kịch bản benchmark:

100 threads

500 threads

1000 threads

7. Kỳ vọng kết quả
NO_LOCK: TPS cao nhất nhưng có âm kho.

SYNCHRONIZED: Không âm kho, TPS giảm.

FILE_LOCK: Không âm kho, TPS thấp hơn do overhead I/O.

OPTIMISTIC_LOCK: Không âm kho, TPS tốt hơn synchronized, có retry.

8. Phân công công việc
Thành viên Đinh Thiện Nhân: Product & Flash Sale Module

Thành viên Võ Phương Thảo: Order & Concurrency Module

Thành viên Nguyễn Huỳnh Đăng huy  : Simulator & Research Module

Thành viên Đỗ Tấn Vũ: Authentication & Payment Module
javac -d bin src/**/*.java

