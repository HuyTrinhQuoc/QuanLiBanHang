package apiclient;

import POJO.NhanVien;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class NhanVienClient {
    private static final String BASE_URL = "http://localhost:8080/api/nhanvien";
    private final ObjectMapper mapper = new ObjectMapper();
    
     public NhanVien login(String maNV, String matKhau) {
        try {
            String urlStr = BASE_URL + "/login?MaNV=" + maNV + "&password=" + matKhau;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                return mapper.readValue(br, NhanVien.class);
            } else if (responseCode == 401) {
                System.out.println("Sai tài khoản hoặc mật khẩu");
                return null;
            } else {
                System.out.println("Lỗi kết nối: Mã lỗi HTTP " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<NhanVien> getAllNhanVien() throws IOException {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            return mapper.readValue(is, new TypeReference<List<NhanVien>>() {});
        }
        return null;
    }

    public boolean addNhanVien(NhanVien nv) throws IOException {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        mapper.writeValue(os, nv);
        os.flush();

        return conn.getResponseCode() == 200;
    }
}

