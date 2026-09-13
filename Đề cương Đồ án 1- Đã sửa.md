## CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM Độc Lập - Tự Do - Hạnh Phúc

## TRƯỜNG ĐẠI HỌC CÔNG NGHỆ THÔNG TIN

## ĐỀ CƯƠNG CHI TIẾT

TÊN ĐỀ TÀI TIẾNG VIỆT: TRỢ LÝ TƯ VẤN MUA SẮM THÔNG MINH

TÊN ĐỀ TÀI TIẾNG ANH: AI SHOPPING AGENT

Cán bộ hướng dẫn: ThS. Trần Thị Hồng Yến

Thời gian thực hiện: Từ ngày 14/09/2026 đến ngày 26/12/2026

Sinh viên thực hiện:

Huỳnh Gia Thịnh - 24521680

Nguyễn Hữu Tính - 24521792

## Nội dung đề tài:

## 1. Tổng quan đề tài:

Bối cảnh thị trường: Sự bùng nổ của các nền tảng thương mại điện tử mang lại lượng hàng hóa khổng lồ, nhưng đồng thời gây ra tình trạng "quá tải thông tin" cho người tiêu dùng. Người mua thường tốn nhiều thời gian tra cứu, đọc bình luận để lọc đánh giá thật/giả, và gặp khó khăn khi đối chiếu cấu hình, giá cả để tìm sản phẩm sát nhu cầu nhất.

## Thực trạng các công cụ hiện hành:

- Các hệ thống tìm kiếm trên sàn TMĐT hiện nay chủ yếu hoạt động dựa trên từ khóa (keyword-based) hoặc bộ lọc cứng nhắc, thiếu khả năng phân tích ngữ nghĩa và hiểu nhu cầu tự nhiên của người dùng.

- Chưa có giải pháp tự động tổng hợp và tóm tắt hàng trăm đánh giá của người dùng cũ bằng AI để đưa ra góc nhìn khách quan, khiến người mua dễ bị ảnh hưởng bởi các đánh giá ảo (seeding).

## 2. Mục tiêu của đề tài:

- Xây dựng hệ thống Trợ lý AI tiếp nhận truy vấn bằng ngôn ngữ tự nhiên (ví dụ: "Tìm laptop lập trình dưới 20 triệu, pin trâu"), tự động trích xuất các thực thể ý định (danh mục, ngân sách, tính năng) để đề xuất danh sách Top-K sản phẩm phù hợp nhất.


- Cải tiến trải nghiệm người dùng bằng cách sinh tóm tắt ưu/nhược điểm sản phẩm từ các bình luận có sẵn, giảm thời gian người dùng phải tự đọc và đối chiếu hàng trăm review.

## 3. Phương pháp thực hiện:

- Sử dụng LLM API có sẵn (Gemini/OpenAI API) làm công cụ trung gian để chuyển câu truy vấn ngôn ngữ tự nhiên thành dữ liệu có cấu trúc dạng JSON (danh mục, ngân sách, tính năng mong muốn)

- Xây dựng công thức xếp hạng sản phẩm đa tiêu chí (Weighted Scoring) bằng logic nghiệp vụ: chuẩn hóa (Min-Max) và tính điểm tổng hợp giữa độ khớp thông số, mức giá và điểm đánh giá trung bình.

- Gọi LLM API để sinh đoạn tóm tắt ngắn gọn từ tập review của từng sản phẩm, lưu kết quả (cache) để tái sử dụng, tránh gọi API lặp lại.

- Dùng Structured Output (responseSchema của Gemini / json_schema strict của OpenAI) kết hợp validate nghiệp vụ, retry, và ghi log.

## 4. Các nội dung chính của đề tài:

- Thiết kế cơ sở dữ liệu PostgreSQL (sản phẩm, thông số kỹ thuật, review, lịch sử tìm kiếm, kết quả tóm tắt) và chuẩn bị một tập dữ liệu mẫu (thu thập/tổng hợp thủ công) cho 1 ngành hàng cụ thể, nạp vào hệ thống qua import CSV.

- Xây dựng REST API bằng Spring Boot: endpoint nhận truy vấn, gọi LLM API để trích xuất ràng buộc (validate), truy vấn Postgres, tính điểm và trả kết quả xếp hạng.

- Xây dựng module gọi LLM API để tóm tắt review (Trước khi đưa review vào LLM để tóm tắt, sẽ lọc bỏ các review rác như: spam icon, nội dung vô nghĩa, …), có cơ chế cache kết quả trong Postgres để tối ưu chi phí và tốc độ.

- Khai báo schema ràng buộc, viết lớp kiểm tra nghiệp vụ, cơ chế retry tối đa 1–2 lần, và lưu log request/response.

## 5. Giới hạn đề tài:

- Phạm vi dữ liệu: sử dụng tập dữ liệu tĩnh (thu thập/tổng hợp thủ công) cho 1 ngành hàng kỹ thuật cao (ví dụ: Laptop hoặc Điện thoại) để dễ đối soát thông số; không xây dựng crawler thu thập dữ liệu thời gian thực.

- Phạm vi kỹ thuật AI: hệ thống sử dụng LLM API có sẵn như một dịch vụ bên ngoài (qua prompt engineering), không tự huấn luyện hay fine-tune mô hình học máy.


## 6. Hướng phát triển đề tài:

- Hỗ trợ cá nhân hóa gợi ý theo lịch sử người dùng: Lưu lại lịch sử tìm kiếm và sản phẩm đã xem/quan tâm của từng người dùng; tích hợp thêm hệ số ưu tiên vào công thức xếp hạng hiện có để tự động đề xuất các sản phẩm phù hợp với xu hướng quan tâm cá nhân, thay vì chỉ dựa trên nội dung của một truy vấn đơn lẻ.

- Mở rộng thu thập dữ liệu theo thời gian thực và đa ngành hàng: Tích hợp API chính thức từ các sàn thương mại điện tử để tự động cập nhật giá, tồn kho và review mới theo chu kỳ; đồng thời thiết kế cơ chế khai báo tiêu chí lọc/xếp hạng linh hoạt theo từng ngành hàng, giúp hệ thống mở rộng vượt ra ngoài phạm vi dữ liệu tĩnh và 1 ngành hàng cố định như hiện tại.

## Kế hoạch thực hiện:

| Giai đoạn | Nội dung công việc | Sinh viên thực | Sản phẩm dự kiến |
| --- | --- | --- | --- |
|   |   | hiện |   |
| Khảo sát & Chuẩn | • Khảo sát, chọn |   | Lược đồ CSDL, tài |
| bị nền tảng |   | Huỳnh Gia Thịnh |   |
|   | ngành hàng, thu |   | liệu đặc tả hệ thống, |
| Từ ngày 14/9/2026 | thập/tổng hợp tập | Nguyễn Hữu Tính | tập dữ liệu mẫu đã |
| đến ngày 27/9/2026 | dữ liệu mẫu (sản |   | import, bộ truy vấn |
|   | phẩm + review). |   | kiểm thử kèm tiêu |
|   | • Thiết kế lược đồ |   | chí gán nhãn. |
|   | CSDL PostgreSQL |   |   |
|   | và viết tài liệu đặc |   |   |
|   | tả yêu cầu |   |   |
|   | (FR/NFR). |   |   |
|   | • Xây dựng bộ truy |   |   |
|   | vấn kiểm thử mẫu |   |   |
|   | (ví dụ 30-50 truy |   |   |
|   | vấn) và xác định |   |   |
|   | tiêu chí gán nhãn |   |   |
|   | Ground Truth cho |   |   |
|   | từng truy vấn. |   |   |


| Xây dựng | • Xây dựng Spring | API CRUD sản |
| --- | --- | --- |
| Backend lõi: Trích | Thịnh: Backend |   |
|   | Boot project, | phẩm hoàn chỉnh, |
| xuất truy vấn, Xếp | Core & Extraction |   |
|   | entity/repository và | module gọi LLM |
| hạng & Tóm tắt | API CRUD cơ bản Tính: Frontend UI | trích xuất truy vấn |
| Review | cho sản | hoạt động được. & Review Cleaning |
| Từ 28/9/2026 đến | phẩm/review. | Module xếp hạng |
| ngày 25/10/2026 | • Thiết kế prompt | sản phẩm và |
|   | và tích hợp gọi | module tóm tắt |
|   | LLM API để trích | review (có cache) |
|   | xuất ràng buộc từ | hoạt động ổn định. |
|   | truy vấn tự nhiên |   |
|   | (JSON output). |   |
|   | • Cài đặt thuật toán |   |
|   | xếp hạng sản phẩm |   |
|   | đa tiêu chí |   |
|   | (Weighted Scoring: |   |
|   | chuẩn hóa |   |
|   | Min-Max + tính |   |
|   | điểm tổng hợp). |   |
|   | • Thiết kế prompt |   |
|   | và tích hợp gọi |   |
|   | LLM API để tóm |   |
|   | tắt review (kèm lọc |   |
|   | review rác), lưu |   |
|   | cache kết quả vào |   |
|   | Postgres. |   |
|   | • Xây dựng lớp |   |
|   | validate ràng buộc |   |
|   | nghiệp vụ + cơ chế |   |
|   | retry/log cho kết |   |
|   | quả trích xuất (dùng |   |


|   | Structured Output |   |
| --- | --- | --- |
|   | của LLM API) |   |
| Xây dựng Giao | • Bắt đầu xây dựng | Giao diện phiên bản |
| diện song song & | Thịnh: Ranking |   |
|   | giao diện (song | đầu (wireframe -> |
| Tài liệu API | Algorithm |   |
|   | song với việc hoàn | UI thật) đã kết nối |
| Từ ngày 26/10/2026 | thiện backend): Tính: Review | được một phần API. |
| đến ngày 8/11/2026 | màn hình tìm kiếm, Summarizer & |   |
|   | hiển thị kết quả xếp Cache |   |
|   | hạng, hiển thị tóm |   |
|   | tắt review. |   |
|   | • Viết Swagger/API |   |
|   | docs cho các |   |
|   | endpoint đã có. |   |
| Tích hợp Hệ thống | • Tích hợp toàn bộ | Hệ thống hoàn |
| End-to-End | Thịnh: Backend |   |
|   | luồng: truy vấn -> | chỉnh, luồng |
| Từ ngày 9/11/2026 | Pipeline |   |
|   | trích xuất ràng buộc | end-to-end từ giao |
| đến ngày | -> truy vấn Postgres Tính: Frontend | diện đến backend |
| 22/11/2026 | -> tính điểm xếp Integration | hoạt động ổn định. |
|   | hạng -> tóm tắt |   |
|   | review -> trả kết |   |
|   | quả. |   |
|   | • Hoàn thiện giao |   |
|   | diện, xử lý các |   |
|   | trường hợp |   |
|   | lỗi/loading/API |   |
|   | timeout. |   |
| Thực nghiệm & | • Gán nhãn Ground | Bảng số liệu thực |
| Đánh giá Hệ thống | Huỳnh Gia Thịnh |   |
|   | Truth cho bộ truy | nghiệm, biểu đồ |
| Từ ngày 23/11/2026 | vấn kiểm thử đã Nguyễn Hữu Tính | đánh giá |
| đến ngày 7/12/2026 | chuẩn bị từ Tuần | Precision/Recall và |
|   | 1-2. | độ trễ. |


|   | • Chạy thử nghiệm |
| --- | --- |
|   | với dữ liệu và truy |
|   | vấn thực tế; đo |
|   | Precision/Recall, độ |
|   | trễ API, chi phí gọi |
|   | LLM (số lần cache |
|   | hit/miss). |
| Buffer: Tối ưu & | • Buffer: sửa lỗi Hệ thống ổn định, |
| Sửa lỗi | Huỳnh Gia Thịnh |
|   | phát sinh từ tích cải thiện dựa trên số |
| Từ ngày 7/12/2026 | hợp và thực liệu thực nghiệm. Nguyễn Hữu Tính |
| đến ngày | nghiệm, tối ưu |
| 13/12/2026 | prompt/công thức |
|   | xếp hạng dựa trên |
|   | kết quả đo được. |
| Viết Báo cáo Đồ án | • Viết báo cáo đồ Bản thảo báo cáo |
| Từ ngày 14/12/2026 | Huỳnh Gia Thịnh |
|   | án, chuẩn bị số liệu, hoàn chỉnh. |
| đến ngày | biểu đồ, phân tích Nguyễn Hữu Tính |
| 20/12/2026 | kết quả. |
| Hoàn thiện & | • Hoàn thiện báo Cuốn báo cáo đồ án |
| Chuẩn bị Báo cáo | Huỳnh Gia Thịnh |
|   | cáo, slide thuyết và hệ thống demo |
| Từ ngày 21/12/2026 | trình, dọn dẹp sẵn sàng báo cáo. Nguyễn Hữu Tính |
| đến ngày | source code, chuẩn |
| 26/12/2026 | bị kịch bản demo. |

## Xác nhận của CBHD

(Ký tên và ghi rõ họ tên)

## TP. HCM, ngày 11 tháng 09 năm 2026

Sinh viên

(Ký tên và ghi rõ họ tên)

ThS. Trần Thị Hồng Yến

Huỳnh Gia Thịnh Nguyễn Hữu Tính
