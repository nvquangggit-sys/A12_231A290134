package vn.edu.vhu.ltdd.a12sqlite;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lab A12 — Quản lý sinh viên bằng SQLite.
 *
 * Bài làm theo mẫu 3 lớp: MainActivity → SinhVienDao → DbHelper → quanlysv.db.
 * Activity không chứa câu lệnh SQL nào; mọi truy vấn nằm trong DAO.
 *
 * Bài nâng cao đã làm:
 *   NC1 — SharedPreferences nhớ tên lớp mặc định.
 *   NC2 — thống kê số sinh viên đạt và điểm cao nhất.
 *   NC3 — bảng lop thứ hai, thống kê theo lớp bằng JOIN.
 *   NC4 — DB_VERSION 2: thêm cột email bằng ALTER TABLE.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "A12_231A290134";

    /** NC1: tên tệp SharedPreferences và khóa lưu lớp mặc định. */
    private static final String PREF_NAME = "cai_dat";
    private static final String KEY_LOP_MAC_DINH = "lop_mac_dinh";

    private SinhVienDao dao;
    private final List<SinhVien> danhSach = new ArrayList<>();
    private SinhVienAdapter adapter;

    private EditText edtMaSv, edtHoTen, edtLop, edtDiem, edtEmail;
    private TextView tvThongKe, tvTrong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            final int pad = getResources().getDimensionPixelSize(R.dimen.screen_padding);
            findViewById(R.id.header).setPadding(pad, pad + bars.top, pad, pad);
            findViewById(R.id.noiDung).setPadding(
                    pad + bars.left, pad, pad + bars.right, pad + bars.bottom);
            return insets;
        });

        edtMaSv = findViewById(R.id.edtMaSv);
        edtHoTen = findViewById(R.id.edtHoTen);
        edtLop = findViewById(R.id.edtLop);
        edtDiem = findViewById(R.id.edtDiem);
        edtEmail = findViewById(R.id.edtEmail);
        tvThongKe = findViewById(R.id.tvThongKe);
        tvTrong = findViewById(R.id.tvTrong);
        Button btnThem = findViewById(R.id.btnThem);
        Button btnTheoLop = findViewById(R.id.btnTheoLop);

        RecyclerView rv = findViewById(R.id.rvSinhVien);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SinhVienAdapter(danhSach);
        rv.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        rv.addItemDecoration(divider);

        // Tạo DAO — chính lúc này tệp .db được tạo nếu chưa có
        dao = new SinhVienDao(this);
        napDanhSach();

        // NC1: điền lại lớp đã dùng lần trước
        edtLop.setText(docLopMacDinh());

        btnThem.setOnClickListener(v -> themSinhVien());
        btnTheoLop.setOnClickListener(v -> hienThongKeTheoLop());
    }

    /** Đọc lại toàn bộ dữ liệu từ CSDL và vẽ lại danh sách. */
    private void napDanhSach() {
        danhSach.clear();
        danhSach.addAll(dao.layTatCa());
        adapter.notifyDataSetChanged();

        // NC2: thống kê bằng hàm tổng hợp của SQL, không đọc cả bảng ra Java.
        String diemTb = String.format(Locale.US, "%.2f", dao.diemTrungBinh());
        String diemMax = String.format(Locale.US, "%.1f", dao.diemCaoNhat());
        tvThongKe.setText(getString(R.string.thong_ke,
                dao.dem(), diemTb, dao.demDat(), diemMax));

        boolean trong = danhSach.isEmpty();
        tvTrong.setVisibility(trong ? android.view.View.VISIBLE : android.view.View.GONE);

        Log.d(TAG, "Nạp " + danhSach.size() + " sinh viên từ CSDL");
    }

    private void themSinhVien() {
        String maSv = edtMaSv.getText().toString().trim();
        String hoTen = edtHoTen.getText().toString().trim();
        String lop = edtLop.getText().toString().trim();
        String diemStr = edtDiem.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        if (!maSv.matches("\\d{10}")) {
            edtMaSv.setError(getString(R.string.err_ma_sv));
            edtMaSv.requestFocus();
            return;
        }
        if (hoTen.isEmpty()) {
            edtHoTen.setError(getString(R.string.err_ho_ten));
            edtHoTen.requestFocus();
            return;
        }
        if (lop.isEmpty()) {
            edtLop.setError(getString(R.string.err_lop));
            edtLop.requestFocus();
            return;
        }
        double diem;
        try {
            diem = Double.parseDouble(diemStr);
        } catch (NumberFormatException e) {
            edtDiem.setError(getString(R.string.err_diem));
            edtDiem.requestFocus();
            return;
        }
        if (diem < 0 || diem > 10) {
            edtDiem.setError(getString(R.string.err_diem_range));
            edtDiem.requestFocus();
            return;
        }
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError(getString(R.string.err_email));
            edtEmail.requestFocus();
            return;
        }

        long id = dao.themMoi(new SinhVien(maSv, hoTen, lop, diem, email));
        if (id == -1) {
            // Vi phạm ràng buộc UNIQUE của cột ma_sv
            edtMaSv.setError(getString(R.string.err_trung_ma));
            edtMaSv.requestFocus();
            Toast.makeText(this, R.string.err_trung_ma, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "insert trả về -1: trùng ma_sv = " + maSv);
            return;
        }

        luuLopMacDinh(lop);   // NC1

        Log.d(TAG, "Đã thêm sinh viên, id = " + id);
        Toast.makeText(this, getString(R.string.da_them, id), Toast.LENGTH_SHORT).show();

        edtMaSv.setText("");
        edtHoTen.setText("");
        edtDiem.setText("");
        edtEmail.setText("");
        edtMaSv.requestFocus();

        napDanhSach();   // đọc lại từ CSDL để chắc chắn màn hình khớp dữ liệu thật
    }

    /** NC3: hộp thoại thống kê mỗi lớp — dữ liệu lấy từ câu JOIN trong DAO. */
    private void hienThongKeTheoLop() {
        List<SinhVienDao.ThongKeLop> ds = dao.thongKeTheoLop();
        if (ds.isEmpty()) {
            Toast.makeText(this, R.string.chua_co_du_lieu, Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder noiDung = new StringBuilder();
        for (SinhVienDao.ThongKeLop tk : ds) {
            noiDung.append("•  ").append(tk.moTa()).append('\n');
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.thong_ke_lop)
                .setMessage(noiDung.toString().trim())
                .setPositiveButton(R.string.dong, null)
                .show();
        Log.d(TAG, "Thống kê theo lớp: " + ds.size() + " lớp");
    }

    // ===================================================================
    // NC1 — SharedPreferences: nhớ lớp mặc định, không lưu cả danh sách
    // ===================================================================

    private String docLopMacDinh() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return pref.getString(KEY_LOP_MAC_DINH, "");
    }

    private void luuLopMacDinh(String lop) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_LOP_MAC_DINH, lop)
                .apply();
    }

    @Override
    protected void onDestroy() {
        dao.dong();      // trả lại kết nối CSDL
        super.onDestroy();
    }
}
