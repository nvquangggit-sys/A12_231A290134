package vn.edu.vhu.ltdd.a12sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Lớp quản lý tệp cơ sở dữ liệu.
 *
 * SQLiteOpenHelper lo giúp ta ba việc:
 *   - Tạo tệp .db lần đầu ứng dụng cần tới (gọi onCreate).
 *   - Gọi onUpgrade khi DATABASE_VERSION tăng lên.
 *   - Mở sẵn kết nối và giữ lại để dùng nhiều lần.
 *
 * Tệp nằm ở /data/data/vn.edu.vhu.ltdd.a12sqlite/databases/quanlysv.db
 *
 * Lịch sử phiên bản:
 *   v1 - bảng sinh_vien như hướng dẫn.
 *   v2 - NC4: thêm cột email bằng ALTER TABLE.
 *   v3 - NC3: thêm bảng lop và cột lop_id, sau đó JOIN để thống kê theo lớp.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "A12_231A290134";

    public static final String DB_NAME = "quanlysv.db";
    public static final int DB_VERSION = 3;   // TĂNG số này mỗi khi đổi cấu trúc bảng

    public static final String TABLE = "sinh_vien";
    public static final String COL_ID = "id";
    public static final String COL_MA_SV = "ma_sv";
    public static final String COL_HO_TEN = "ho_ten";
    public static final String COL_LOP = "lop";
    public static final String COL_DIEM = "diem";
    public static final String COL_EMAIL = "email";       // NC4
    public static final String COL_LOP_ID = "lop_id";     // NC3

    public static final String TABLE_LOP = "lop";         // NC3
    public static final String COL_LOP_PK = "id";
    public static final String COL_TEN_LOP = "ten_lop";

    /** Câu lệnh tạo bảng sinh_vien ở phiên bản mới nhất. */
    private static final String SQL_CREATE_SINH_VIEN =
            "CREATE TABLE " + TABLE + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_MA_SV + " TEXT NOT NULL UNIQUE, " +   // UNIQUE: không cho trùng MSSV
            COL_HO_TEN + " TEXT NOT NULL, " +
            COL_LOP + " TEXT, " +
            COL_DIEM + " REAL DEFAULT 0, " +
            COL_EMAIL + " TEXT, " +
            COL_LOP_ID + " INTEGER)";

    /** NC3: bảng lớp thứ hai, tách ra để không lặp tên lớp ở mọi dòng. */
    private static final String SQL_CREATE_LOP =
            "CREATE TABLE " + TABLE_LOP + " (" +
            COL_LOP_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TEN_LOP + " TEXT NOT NULL UNIQUE)";

    public DbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /** Chỉ chạy MỘT LẦN, lúc tệp .db chưa tồn tại. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: tạo bảng " + TABLE_LOP + " và " + TABLE);
        db.execSQL(SQL_CREATE_LOP);
        db.execSQL(SQL_CREATE_SINH_VIEN);

        // Dữ liệu mẫu để màn hình không trống trơn lần đầu chạy
        db.execSQL("INSERT INTO " + TABLE_LOP + " (" + COL_TEN_LOP + ") VALUES " +
                "('CNTT K22'), ('CNTT K23')");

        db.execSQL("INSERT INTO " + TABLE + " (" + COL_MA_SV + ", " + COL_HO_TEN + ", " +
                COL_LOP + ", " + COL_DIEM + ", " + COL_EMAIL + ", " + COL_LOP_ID + ") VALUES " +
                "('2312900001', 'Nguyễn Văn An', 'CNTT K22', 8.5, 'an.nguyen@vhu.edu.vn', 1), " +
                "('2312900002', 'Trần Thị Bình', 'CNTT K22', 7.0, 'binh.tran@vhu.edu.vn', 1), " +
                "('2312900003', 'Lê Văn Cường', 'CNTT K23', 9.2, 'cuong.le@vhu.edu.vn', 2)");
    }

    /**
     * Chạy khi DB_VERSION tăng. Bài làm nâng cấp TỪNG BƯỚC để giữ dữ liệu cũ:
     * v1 → v2 thêm cột email, v2 → v3 thêm bảng lop và cột lop_id.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade " + oldVersion + " → " + newVersion);

        if (oldVersion < 2) {
            // NC4: ALTER TABLE giữ nguyên dữ liệu, khác hẳn DROP rồi tạo lại.
            db.execSQL("ALTER TABLE " + TABLE + " ADD COLUMN " + COL_EMAIL + " TEXT");
        }

        if (oldVersion < 3) {
            // NC3: tạo bảng lớp, thêm cột lop_id rồi chuyển dữ liệu cũ sang.
            db.execSQL(SQL_CREATE_LOP);
            db.execSQL("ALTER TABLE " + TABLE + " ADD COLUMN " + COL_LOP_ID + " INTEGER");

            db.execSQL("INSERT OR IGNORE INTO " + TABLE_LOP + " (" + COL_TEN_LOP + ") " +
                    "SELECT DISTINCT " + COL_LOP + " FROM " + TABLE +
                    " WHERE " + COL_LOP + " IS NOT NULL AND " + COL_LOP + " <> ''");

            db.execSQL("UPDATE " + TABLE + " SET " + COL_LOP_ID + " = " +
                    "(SELECT " + COL_LOP_PK + " FROM " + TABLE_LOP +
                    " WHERE " + COL_TEN_LOP + " = " + TABLE + "." + COL_LOP + ")");
        }
    }
}
