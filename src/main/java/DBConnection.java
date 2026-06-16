import java.sql.*;

public class DBConnection {

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/healthy_restaurant?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
        return DriverManager.getConnection(url, "root", "");
    }

    // تهيئة حسابات الطاولات العشر عند أول تشغيل
    public static void initTableAccounts() {
        try {
            Connection con = getConnection();
            for (int i = 1; i <= 10; i++) {
                String sql = "INSERT IGNORE INTO dining_tables (table_number, account_name, password_hash, status, active) VALUES (?, ?, '1234', 'OPEN', 1)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, i);
                pst.setString(2, "table" + i);
                pst.executeUpdate();
                pst.close();
            }
            con.close();
        } catch (Exception e) {
            System.out.println("Table accounts init: " + e.getMessage());
        }
    }
}
