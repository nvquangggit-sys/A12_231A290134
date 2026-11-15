package vn.edu.vhu.ltdd.a12sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DAO (Data Access Object) — nơi DUY NHẤT trong ứng dụng biết tới SQL.
 *
 * Activity chỉ gọi themMoi(), layTatCa(), dem()… chứ không tự viết câu lệnh SQL.
 * Nhờ vậy, sau này đổi sang Room hay sang API mạng thì chỉ sửa lớp này.
 */
public class SinhVienDao {

    private final DbHelper helper;

    public SinhVienDao(Context context) {
        this.helper = new DbHelper(context.getApplicationContext());
    }

    /**
     * Thêm một sinh viên.
     * @return id vừa được SQLite cấp, hoặc -1 nếu thất bại (ví dụ trùng mã sinh viên).
     */
    public long themMoi(SinhVien sv) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // ContentValues là cặp "tên cột → giá trị". Dùng nó thay cho việc
        // nối chuỗi SQL bằng dấu + — nối chuỗi dễ sai và dễ bị tấn công SQL injection.
        ContentValues v = new ContentValues();
        v.put(DbHelper.COL_MA_SV, sv.getMaSv());
        v.put(DbHelper.COL_HO_TEN, sv.getHoTen());
        v.put(DbHelper.COL_DIEM, sv.getDiem());

        if (sv.getLop() != null && !sv.getLop().trim().isEmpty()) {
            v.put(DbHelper.COL_LOP, sv.getLop().trim());
            v.put(DbHelper.COL_LOP_ID, layHoacTaoLop(db, sv.getLop().trim()));   // NC3
        }
        if (sv.getEmail() != null && !sv.getEmail().trim().isEmpty()) {
            v.put(DbHelper.COL_EMAIL, sv.getEmail().trim());                     // NC4
        }

        // insert trả về -1 khi vi phạm ràng buộc (ở đây là UNIQUE của ma_sv)
        long id = db.insert(DbHelper.TABLE, null, v);
        if (id != -1) sv.setId(id);
        return id;
    }

    /** NC3: tìm lớp theo tên, chưa có thì tạo mới — trả về khóa ngoại lop_id. */
    private long layHoacTaoLop(SQLiteDatabase db, String tenLop) {
        Cursor c = db.query(DbHelper.TABLE_LOP, new String[]{DbHelper.COL_LOP_PK},
                DbHelper.COL_TEN_LOP + " = ?", new String[]{tenLop},
                null, null, null);
        try {
            if (c.moveToFirst()) return c.getLong(0);
        } finally {
            c.close();
        }

        ContentValues v = new ContentValues();
        v.put(DbHelper.COL_TEN_LOP, tenLop);
        long id = db.insert(DbHelper.TABLE_LOP, null, v);
        if (id != -1) return id;

        // Hiếm: một tiến trình khác vừa chèn cùng tên lớp → đọc lại.
        Cursor lai = db.query(DbHelper.TABLE_LOP, new String[]{DbHelper.COL_LOP_PK},
                DbHelper.COL_TEN_LOP + " = ?", new String[]{tenLop}, null, null, null);
        try {
            return lai.moveToFirst() ? lai.getLong(0) : -1;
        } finally {
            lai.close();
        }
    }

    /** Lấy toàn bộ danh sách, sắp xếp theo họ tên. */
    public List<SinhVien> layTatCa() {
        List<SinhVien> ds = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        // query(bảng, cột, điều kiện, tham số, groupBy, having, orderBy)
        Cursor c = db.query(DbHelper.TABLE, null, null, null, null, null,
                DbHelper.COL_HO_TEN + " ASC");

        // try-finally để Cursor LUÔN được đóng, kể cả khi có lỗi giữa chừng
        try {
            // Lấy chỉ số cột MỘT LẦN trước vòng lặp cho nhanh
            int iId = c.getColumnIndexOrThrow(DbHelper.COL_ID);
            int iMa = c.getColumnIndexOrThrow(DbHelper.COL_MA_SV);
            int iTen = c.getColumnIndexOrThrow(DbHelper.COL_HO_TEN);
            int iLop = c.getColumnIndexOrThrow(DbHelper.COL_LOP);
            int iDiem = c.getColumnIndexOrThrow(DbHelper.COL_DIEM);
            int iEmail = c.getColumnIndexOrThrow(DbHelper.COL_EMAIL);

            while (c.moveToNext()) {       // con trỏ bắt đầu TRƯỚC dòng đầu tiên
                ds.add(new SinhVien(
                        c.getLong(iId),
                        c.getString(iMa),
                        c.getString(iTen),
                        c.getString(iLop),
                        c.getDouble(iDiem),
                        c.getString(iEmail)));
            }
        } finally {
            c.close();
        }
        return ds;
    }

    /** Đếm số dòng bằng câu truy vấn thô — minh họa rawQuery. */
    public int dem() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DbHelper.TABLE, null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    /** Điểm trung bình của cả lớp — minh họa hàm tổng hợp của SQL. */
    public double diemTrungBinh() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT AVG(" + DbHelper.COL_DIEM + ") FROM " + DbHelper.TABLE, null);
        try {
            return c.moveToFirst() ? c.getDouble(0) : 0;
        } finally {
            c.close();
        }
    }

    /** NC2: số sinh viên đạt (điểm ≥ 5). */
    public int demDat() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DbHelper.TABLE +
                " WHERE " + DbHelper.COL_DIEM + " >= 5", null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    /** NC2: điểm cao nhất. MAX() trả về NULL khi bảng rỗng nên phải kiểm tra isNull. */
    public double diemCaoNhat() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(" + DbHelper.COL_DIEM + ") FROM " + DbHelper.TABLE, null);
        try {
            return (c.moveToFirst() && !c.isNull(0)) ? c.getDouble(0) : 0;
        } finally {
            c.close();
        }
    }

    /** NC3: thống kê mỗi lớp — sĩ số và điểm trung bình, lấy bằng JOIN hai bảng. */
    public List<ThongKeLop> thongKeTheoLop() {
        List<ThongKeLop> ds = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        String sql = "SELECT lp." + DbHelper.COL_TEN_LOP + ", COUNT(sv." + DbHelper.COL_ID + "), " +
                "AVG(sv." + DbHelper.COL_DIEM + ") " +
                "FROM " + DbHelper.TABLE_LOP + " lp " +
                "LEFT JOIN " + DbHelper.TABLE + " sv ON sv." + DbHelper.COL_LOP_ID + " = lp." +
                DbHelper.COL_LOP_PK + " " +
                "GROUP BY lp." + DbHelper.COL_LOP_PK + " " +
                "ORDER BY lp." + DbHelper.COL_TEN_LOP + " COLLATE NOCASE";

        Cursor c = db.rawQuery(sql, null);
        try {
            while (c.moveToNext()) {
                ds.add(new ThongKeLop(c.getString(0), c.getInt(1), c.getDouble(2)));
            }
        } finally {
            c.close();
        }
        return ds;
    }

    /** Gọi khi không dùng nữa (ví dụ trong onDestroy của Activity). */
    public void dong() {
        helper.close();
    }

    /** Một dòng kết quả của câu JOIN ở trên. */
    public static class ThongKeLop {
        public final String tenLop;
        public final int siSo;
        public final double diemTrungBinh;

        public ThongKeLop(String tenLop, int siSo, double diemTrungBinh) {
            this.tenLop = tenLop;
            this.siSo = siSo;
            this.diemTrungBinh = diemTrungBinh;
        }

        public String moTa() {
            return tenLop + " — " + siSo + " sinh viên · ĐTB " +
                    String.format(Locale.US, "%.2f", diemTrungBinh);
        }
    }
}
