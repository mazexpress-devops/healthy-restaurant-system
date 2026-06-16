import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdateName {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/healthy_restaurant?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
            Connection con = DriverManager.getConnection(url, "root", "");
            Statement stmt = con.createStatement();
            
            stmt.executeUpdate("UPDATE users SET full_name='إيناس عبد المنعم' WHERE role='CHEF'");
            
            System.out.println("Name updated successfully.");
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
