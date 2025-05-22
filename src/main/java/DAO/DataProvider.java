package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DataProvider {

    private static String driver = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Connection getConnection() {
        String url = "jdbc:mysql://localhost:3306/quanlynhahang";
        String user = "root";
        String password = "";
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {

            throw new RuntimeException(ex);
        }
    }
//    /**
//     * Xây dựng PreparedStatement
//     *
//     * @param sql là câu lệnh SQL chứa có thể chứa tham số. Nó có thể là một lời
//     * gọi thủ tục lưu
//     * @param args là danh sách các giá trị được cung cấp cho các tham số trong
//     * câu lệnh sql
//     * @return PreparedStatement tạo được
//     * @throws java.sql.SQLException lỗi sai cú pháp
//     */

    public static PreparedStatement getStmt(String sql, Object... args) throws SQLException {

        Connection connection = getConnection();
        PreparedStatement pstmt = null;
        if (sql.trim().startsWith("{")) {
            pstmt = connection.prepareCall(sql);
        } else {
            pstmt = connection.prepareStatement(sql);
        }
        for (int i = 0; i < args.length; i++) {

            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }
//    /**
//     * Thực hiện câu lệnh SQL thao tác (INSERT, UPDATE, DELETE) hoặc thủ tục lưu
//     * thao tác dữ liệu
//     *
//     * @param sql là câu lệnh SQL chứa có thể chứa tham số. Nó có thể là một lời
//     * gọi thủ tục lưu
//     * @param args là danh sách các giá trị được cung cấp cho các tham số trong
//     * câu lệnh sql *
//     */

    public static void update(String sql, Object... args) {
        try {
            PreparedStatement stmt = DataProvider.getStmt(sql, args);
            try {
                stmt.executeUpdate();
            } finally {
                stmt.getConnection().close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
// /**
//     * Thực hiện câu lệnh SQL truy vấn (SELECT) hoặc thủ tục lưu truy vấn dữ
//     * liệu
//     *
//     * @param sql là câu lệnh SQL chứa có thể chứa tham số. Nó có thể là một lời
//     * gọi thủ tục lưu
//     * @param args là danh sách các giá trị được cung cấp cho các tham số trong
//     * câu lệnh sql
//     */

    public static ResultSet query(String sql, Object... args) {
        try {
            PreparedStatement stmt = DataProvider.getStmt(sql, args);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Object value(String sql, Object... args) {
        try {
            ResultSet rs = DataProvider.query(sql, args);
            if (rs.next()) {
                return rs.getObject(1);
            }
            rs.getStatement().getConnection().close();
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = null;
        conn = getConnection();
        if (conn != null) {
            System.out.println("Kết nối đến cơ sở dữ liệu thành công!");
        }
    }
}
