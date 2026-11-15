package vn.edu.vhu.ltdd.a12sqlite;

/**
 * Một dòng trong bảng sinh_vien.
 *
 * Có hai constructor: một nhận id (dùng khi đọc từ CSDL lên),
 * một không nhận id (dùng khi tạo mới, vì SQLite sẽ tự cấp).
 */
public class SinhVien {

    private long id;            // khóa chính, do SQLite tự sinh
    private String maSv;
    private String hoTen;
    private String lop;
    private double diem;
    private String email;       // NC4: thêm ở DB_VERSION 2

    public SinhVien(long id, String maSv, String hoTen, String lop, double diem, String email) {
        this.id = id;
        this.maSv = maSv;
        this.hoTen = hoTen;
        this.lop = lop;
        this.diem = diem;
        this.email = email;
    }

    /** Dùng khi tạo mới: chưa có id vì SQLite sẽ cấp. */
    public SinhVien(String maSv, String hoTen, String lop, double diem, String email) {
        this(-1, maSv, hoTen, lop, diem, email);
    }

    public long getId() { return id; }
    public String getMaSv() { return maSv; }
    public String getHoTen() { return hoTen; }
    public String getLop() { return lop; }
    public double getDiem() { return diem; }
    public String getEmail() { return email; }

    public void setId(long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
}
