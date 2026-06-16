import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class LoginFrame extends JFrame {

    JTextField txt_username;
    JPasswordField txt_password;
    JLabel lbl_status;
    String target_role; // "CHEF" or "ADMIN"

    public LoginFrame(String role) {
        this.target_role = role;
        setTitle("تسجيل الدخول - " + (role.equals("ADMIN") ? "المدير" : "الشيف"));
        setSize(380, 280);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("تسجيل دخول " + (role.equals("ADMIN") ? "المدير" : "الشيف"), JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        panel.add(title, g);

        g.gridwidth = 1;
        g.weightx = 0.3;
        g.gridx = 0; g.gridy = 1;
        panel.add(new JLabel("اسم المستخدم:"), g);
        g.weightx = 0.7;
        g.gridx = 1;
        txt_username = new JTextField();
        txt_username.setPreferredSize(new Dimension(150, 30));
        txt_username.setText(role.equals("ADMIN") ? "admin" : "chef");
        panel.add(txt_username, g);

        g.weightx = 0.3;
        g.gridx = 0; g.gridy = 2;
        panel.add(new JLabel("كلمة المرور:"), g);
        g.weightx = 0.7;
        g.gridx = 1;
        txt_password = new JPasswordField();
        txt_password.setPreferredSize(new Dimension(150, 30));
        txt_password.setText(role.equals("ADMIN") ? "admin" : "chef");
        panel.add(txt_password, g);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        lbl_status = new JLabel("ادخل بياناتك ثم اضغط دخول", JLabel.CENTER);
        lbl_status.setForeground(Color.DARK_GRAY);
        panel.add(lbl_status, g);

        JButton btn_login = new JButton("دخول");
        btn_login.setPreferredSize(new Dimension(120, 35));
        g.gridy = 4;
        btn_login.addActionListener(e -> btn_loginActionPerformed());
        panel.add(btn_login, g);

        JButton btn_back = new JButton("رجوع");
        g.gridy = 5;
        btn_back.addActionListener(e -> {
            new MainMenuFrame().setVisible(true);
            dispose();
        });
        panel.add(btn_back, g);

        add(panel);
        setVisible(true);
    }

    private void btn_loginActionPerformed() {
        String username = txt_username.getText().trim();
        String password = new String(txt_password.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lbl_status.setText("الرجاء ادخال اسم المستخدم وكلمة المرور");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            String sql = "select id, full_name, role from users where username=? and password_hash=? and active=1";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                String name = rs.getString("full_name");

                if (target_role.equals("CHEF") && (role.equals("CHEF") || role.equals("ADMIN"))) {
                    JOptionPane.showMessageDialog(this, "مرحبا " + name);
                    new ChefFrame().setVisible(true);
                    dispose();
                } else if (target_role.equals("ADMIN") && role.equals("ADMIN")) {
                    JOptionPane.showMessageDialog(this, "مرحبا " + name);
                    new AdminFrame().setVisible(true);
                    dispose();
                } else {
                    lbl_status.setText("ليس لديك صلاحية للدخول");
                }
            } else {
                lbl_status.setText("اسم المستخدم أو كلمة المرور غير صحيح");
            }

            rs.close();
            pst.close();
            con.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ في الاتصال:\n" + e.getMessage());
        }
    }

}
