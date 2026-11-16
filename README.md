# Lab A12 — SQLite (1): Tạo CSDL, thêm & truy vấn dữ liệu

**Sinh viên:** Nguyễn Việt Quang — **MSSV:** 231A290134 — **Lớp:** CNTT (LTDD)
**Môn:** INT4211 — Lập trình di động
**Package:** `vn.edu.vhu.ltdd.a12sqlite`

Ứng dụng quản lý sinh viên tối giản, lưu dữ liệu bằng SQLite có sẵn trong Android:
thêm sinh viên, đọc lại danh sách từ đĩa sau khi tắt hẳn app, thống kê bằng hàm tổng hợp của SQL,
và bốn bài nâng cao NC1–NC4.

## Kiến trúc

```
MainActivity  →  SinhVienDao  →  DbHelper  →  /data/data/vn.edu.vhu.ltdd.a12sqlite/databases/quanlysv.db
```

- `MainActivity` chỉ kiểm tra dữ liệu nhập và gọi DAO — **không có câu SQL nào**.
- `SinhVienDao` là nơi duy nhất chứa `SELECT` / `INSERT` / `UPDATE`, dùng `ContentValues`,
  `query()`, `rawQuery()` và đóng `Cursor` trong `finally`.
- `DbHelper` giữ `DB_VERSION`, `onCreate`, `onUpgrade` và dữ liệu mẫu.

## Cấu trúc thư mục

```
app/src/main/java/vn/edu/vhu/ltdd/a12sqlite/
├── SinhVien.java          // lớp dữ liệu thuần, 2 constructor
├── DbHelper.java          // DB_VERSION = 3, CREATE TABLE, seed, onUpgrade v1→v2→v3
├── SinhVienDao.java       // toàn bộ SQL + thống kê theo lớp (JOIN)
├── SinhVienAdapter.java   // RecyclerView.Adapter
└── MainActivity.java      // form, kiểm tra, thống kê, SharedPreferences

app/src/main/res/
├── layout/activity_main.xml, item_sinh_vien.xml
├── values/strings.xml, colors.xml, themes.xml, dimens.xml
└── drawable/bg_input.xml, bg_stats.xml, divider.xml

docs/
├── screenshots/           // 13 ảnh minh chứng thật (xem Phụ lục A của báo cáo)
├── logs/logcat_a12.txt    // log của TAG A12_231A290134
├── A12_231A290134_demo.mp4        // video demo 2 phút 41
└── BaoCao_LabA12_231A290134.docx  // báo cáo (kèm bản .pdf)
```

## Cấu trúc CSDL (phiên bản 3)

```sql
CREATE TABLE sinh_vien (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    ma_sv   TEXT NOT NULL UNIQUE,
    ho_ten  TEXT NOT NULL,
    lop     TEXT,
    diem    REAL DEFAULT 0,
    email   TEXT,
    lop_id  INTEGER
);

CREATE TABLE lop (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    ten_lop TEXT NOT NULL UNIQUE
);
```

- v1: bảng `sinh_vien` cơ bản.
- v2 (NC4): `ALTER TABLE sinh_vien ADD COLUMN email TEXT` — không mất dữ liệu.
- v3 (NC3): thêm bảng `lop`, thêm cột `lop_id`, backfill từ cột `lop` rồi mới dùng JOIN.

## Bài nâng cao

| Bài | Nội dung | Chỗ xem |
|-----|----------|---------|
| NC1 | Lưu tên lớp mặc định bằng `SharedPreferences` (`cai_dat.xml`) | `MainActivity.docLopMacDinh/luuLopMacDinh` |
| NC2 | `COUNT`, `AVG`, `MAX` tính trong SQLite | `SinhVienDao.demDat/diemCaoNhat` |
| NC3 | Bảng `lop` chuẩn hóa + `LEFT JOIN` thống kê theo lớp | `SinhVienDao.thongKeTheoLop` |
| NC4 | Nâng `DB_VERSION` 2 → 3, thêm cột `email` bằng `ALTER TABLE` | `DbHelper.onUpgrade` |

## Build & chạy

```bash
# Windows
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
gradlew.bat assembleDebug

# hoặc
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

Cài và mở:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n vn.edu.vhu.ltdd.a12sqlite/.MainActivity
```

Xem log:

```bash
adb logcat -s A12_231A290134
```

Xem CSDL: **View → Tool Windows → App Inspection → Database Inspector**, chọn
`quanlysv.db` → `sinh_vien`. Bản Android Studio này không có tab Data / Schema / SQL riêng,
nên xem cấu trúc bằng **Open New Query Tab** rồi chạy:

```sql
PRAGMA table_info(sinh_vien);
SELECT * FROM sinh_vien WHERE diem >= 8 ORDER BY diem DESC;
```

## Video demo

`docs/A12_231A290134_demo.mp4` — 2 phút 41, quay màn hình Pixel 6 (720 × 1600) bằng
`adb shell screenrecord`, đi đúng kịch bản 1–6, 8 và NC3, NC4.

## Báo cáo

`docs/BaoCao_LabA12_231A290134.docx` và bản PDF cùng tên: 24 trang, ảnh minh chứng nhúng
trực tiếp, mô tả 8 kịch bản, Database Inspector, 3 câu phân tích, NC1–NC4 và phần ôn tập.

## Kịch bản kiểm thử

1. Mở app lần đầu → 3 sinh viên mẫu, chip thống kê.
2. Thêm sinh viên hợp lệ → dòng mới + Toast `Đã thêm, id = 4`.
3. Tắt hẳn app rồi mở lại → dữ liệu còn, ô Lớp được điền sẵn (NC1).
4. Thêm trùng MSSV → lỗi tại ô MSSV, không thêm dòng.
5. Nhập điểm 15 → lỗi "Điểm phải từ 0 đến 10".
6. Nhập chữ vào ô điểm → bàn phím số chặn ký tự; để trống rồi bấm thêm → lỗi "Điểm phải là số".
7. Database Inspector → thấy đúng số dòng và cột `email`.
8. Thêm đủ 5 sinh viên → 8 dòng, chip `8 sinh viên · ĐTB 6.63 · Đạt 6 · Cao nhất 9.2`.
9. Nút **Theo lớp** → hộp thoại K22 (4 SV, ĐTB 6.25) và K23 (4 SV, ĐTB 7.00).

## Ghi chú về ảnh minh chứng

13 ảnh trong `docs/screenshots` là **ảnh chụp thật**: 9 ảnh từ emulator Pixel 6 (API 34,
1080 × 2400) bằng `adb shell screencap`, 4 ảnh chụp cửa sổ Android Studio (Database Inspector,
Device Explorer). Ảnh được nhúng trực tiếp vào báo cáo, không phải liên kết; Phụ lục A của
báo cáo liệt kê từng tệp và cách chụp lại.
