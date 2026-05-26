# AI AUDIT LOG — FLASH SALE CONCURRENCY PROJECT

## ENTRY 01 — NGHIÊN CỨU SYNCHRONIZED VÀ FILE_LOCK

### Responsibility Area

Order Flow & Concurrency Module

---

### Task Context

Tôi phụ trách module Order & Concurrency trong project Flash Sale MVC.
Trong quá trình mở rộng order flow, tôi cần bổ sung bước chọn phương thức thanh toán sau khi đặt hàng thành công.

---


### Prompt

"Điểm khác nhau giữa synchronized và file lock là gì trong hệ thống flash sale CSV multi-threading?"

---

### AI Output Summary

AI giải thích:

SYNCHRONIZED:

* khóa trong memory
* chỉ hoạt động trong JVM hiện tại
* tốc độ nhanh hơn
* dùng synchronized method/block

FILE_LOCK:

* khóa trực tiếp file CSV bằng Java NIO FileLock
* hoạt động ở OS level
* nhiều process vẫn bị khóa
* I/O overhead cao hơn

---

### Personal Decision

Tôi CHẤP NHẬN việc implement cả synchronized và FileLock.

Tôi quyết định:

* dùng synchronized cho memory locking benchmark
* dùng FileLock cho file-level locking benchmark

Mục tiêu:

* so sánh throughput
* so sánh consistency
* nghiên cứu trade-off giữa performance và safety

---

### Human Delta

1. Tôi bổ sung requirement:
   FILE_LOCK phải release lock bằng try-with-resources.

2. Tôi bổ sung benchmark throughput giữa synchronized và FileLock.

3. Tôi xác định synchronized chỉ phù hợp single JVM.

4. Tôi bổ sung giải thích:
   FileLock hoạt động ở OS level thay vì JVM level.

---

### Evidence

Lock mechanisms cuối cùng được implement:

* NO_LOCK
* SYNCHRONIZED
* FILE_LOCK
* OPTIMISTIC_LOCK
---

### Reflection

Prompt này giúp tôi hiểu rõ sự khác nhau giữa memory-level synchronization và OS-level file synchronization.

Ban đầu tôi nghĩ synchronized có thể khóa trực tiếp file CSV, nhưng sau khi phân tích AI output và tài liệu Java NIO, tôi nhận ra synchronized chỉ hoạt động trong JVM hiện tại.

Tôi không sử dụng nguyên AI output mà bổ sung thêm benchmark analysis và release lock handling để phù hợp research question của project.

====================================================================

## ENTRY 02 — PHÁT HIỆN ẢO GIÁC AI VỀ PAYMENT SYSTEM

---

### Prompt

"Thêm module payment cho hệ thống flash sale Java MVC sử dụng CSV storage."

---

### AI Output Summary

AI đề xuất một payment system khá lớn bao gồm:

* payment gateway simulation
* refund flow
* async payment
* payment retry
* payment status lifecycle
* e-wallet
* banking callback
* transaction logging phức tạp

AI cũng đề xuất thêm nhiều entity và controller không cần thiết cho scope LAB211.

---

### Hallucination Detection

Tôi xác định đây là trường hợp:
CONTEXT MISUNDERSTANDING.

Mặc dù prompt chỉ yêu cầu bổ sung payment module cơ bản, AI lại mở rộng sang domain fintech.

Các đề xuất này không phù hợp vì:

* project chỉ là Java Console MVC
* trọng tâm nghiên cứu là concurrency và synchronization
* payment không phải research focus
* làm tăng complexity không cần thiết
* gây khó khăn khi integration với module order

---

### Decision

Tôi từ chối phần lớn AI suggestion.

Chỉ giữ lại:

* chọn phương thức thanh toán
* lưu payment info vào CSV
* Payment entity đơn giản
* PaymentView
* PaymentController cơ bản

Tôi quyết định hệ thống chỉ hỗ trợ:

* CASH
* BANKING

---

### Human Delta

1. Tôi loại bỏ hoàn toàn:

   * payment gateway
   * refund
   * async payment
   * payment retry
   * e-wallet

2. Tôi bỏ PaymentStatus vì hệ thống không xử lý transaction thật.

3. Tôi đơn giản hóa PaymentController chỉ còn:

   * choose payment method
   * save payment information

. Tôi giảm scope xuống simple payment selection để tránh over-engineering.

---

### Evidence

PaymentMethod cuối cùng:

public enum PaymentMethod {
,
BANKING
}

Payment entity cuối cùng:

* id
* orderId
* customerId
* paymentMethod
* amount
* createdAt

---

### Reflection

Đây là ví dụ rõ nhất về việc không thể copy AI output nguyên xi.
AI tạo ra một hệ thống vượt quá scope môn học và lệch khỏi research question chính.

Thông qua entry này, tôi học được rằng:

* cần giới hạn scope rõ ràng khi dùng AI
* phải đánh giá mức độ phù hợp với kiến trúc hiện tại
* luôn ưu tiên simplicity cho project academic
* AI thường có xu hướng over-engineering nếu prompt không đủ constraint

Entry này giúp tôi cải thiện kỹ năng đánh giá AI suggestion thay vì chỉ sử dụng output trực tiếp.
