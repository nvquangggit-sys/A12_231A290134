# CREDITS — Lab A12

**Sinh viên:** Nguyễn Việt Quang
**MSSV:** 231A290134
**Lớp:** CNTT — LTDD
**Môn:** INT4211 — Lập trình di động
**Bài:** Lab A12 — SQLite (1): Tạo CSDL, thêm & truy vấn dữ liệu

## Nguồn tham khảo

- Đề bài và hướng dẫn thực hành Lab A12, khoa CNTT.
- Android Developers — *Save data using SQLite*: https://developer.android.com/training/data-storage/sqlite
- Android Developers — *android.database.sqlite.SQLiteDatabase*: https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase
- Android Developers — *Access data using Cursor*: https://developer.android.com/reference/android/database/Cursor
- SQLite — *SQL As Understood By SQLite*: https://www.sqlite.org/lang.html
- Material Design 3 — *Buttons, Text fields*: https://m3.material.io/components

## Công cụ

- Android Studio Quail 3 | 2026.1.3, JDK 17 (JBR đi kèm Android Studio)
- Gradle 9.x, Android Gradle Plugin tương ứng
- Emulator Pixel 6 API 34 (1080 × 2400, 420dpi)
- Ảnh minh chứng trong `docs/screenshots` là ảnh chụp thật từ emulator (`adb screencap`) và từ
  Android Studio; video demo quay bằng `adb shell screenrecord` và ghép bằng ffmpeg.
- Báo cáo xuất từ Word (DOCX → PDF), kiểm tra bằng PyMuPDF.

## Thư viện dùng trong project

- AndroidX AppCompat, RecyclerView 1.3.2
- Material Components (MaterialButton, MaterialAlertDialogBuilder)

Không có thư viện ORM nào; toàn bộ SQL viết tay trong `SinhVienDao`.
