import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ResetPasswords {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/healthy_restaurant?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
            Connection con = DriverManager.getConnection(url, "root", "");
            Statement stmt = con.createStatement();
            
            // Reset users: set password to plaintext 'admin' for admin, 'chef' for chef, '1234' otherwise
            stmt.executeUpdate("UPDATE users SET password_hash='admin' WHERE role='ADMIN'");
            stmt.executeUpdate("UPDATE users SET password_hash='chef' WHERE role='CHEF'");
            
            // Reset tables: set password to plaintext '1234'
            stmt.executeUpdate("UPDATE dining_tables SET password_hash='1234'");
            
            System.out.println("Passwords reset to plain text successfully.");
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
