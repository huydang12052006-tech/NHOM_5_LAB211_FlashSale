
# AI AUDIT LOG — INDIVIDUAL ENTRY

## ENTRY 03 — PHÁT HIỆN ẢO GIÁC AI VỀ PAYMENT SYSTEM

---

### Task Context

Tôi phụ trách module Order & Concurrency trong project Flash Sale MVC.
Trong quá trình mở rộng order flow, tôi cần bổ sung bước chọn phương thức thanh toán sau khi đặt hàng thành công.

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

* COD
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

3. Tôi đổi enum từ CASH thành COD để đúng ngữ cảnh ecommerce.

4. Tôi đơn giản hóa PaymentController chỉ còn:

   * create payment
   * choose payment method
   * save payment information

5. Tôi giảm scope xuống simple payment selection để tránh over-engineering.

---

### Evidence

PaymentMethod cuối cùng:

public enum PaymentMethod {
COD,
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
