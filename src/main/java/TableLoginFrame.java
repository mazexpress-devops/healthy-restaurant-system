import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class TableLoginFrame extends JFrame {

    JTextField txt_username;
    JPasswordField txt_password;
    JLabel lbl_status;

    public TableLoginFrame() {
        setTitle("نظام المطعم الصحي - دخول الزبون");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 15, 10, 15);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("دخول الزبون", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        panel.add(title, g);

        JLabel hint = new JLabel("مثال:  table1  /  1234", JLabel.CENTER);
        hint.setFont(new Font("Arial", Font.PLAIN, 12));
        hint.setForeground(Color.GRAY);
        g.gridy = 1;
        panel.add(hint, g);

        g.gridwidth = 1;
        g.weightx = 0.3;
        g.gridx = 0; g.gridy = 2;
        panel.add(new JLabel("رقم الطاولة (table1 ... table10):"), g);
        g.weightx = 0.7;
        g.gridx = 1;
        txt_username = new JTextField();
        txt_username.setPreferredSize(new Dimension(150, 30));
        txt_username.setText("table1");
        panel.add(txt_username, g);

        g.weightx = 0.3;
        g.gridx = 0; g.gridy = 3;
        panel.add(new JLabel("كلمة المرور:"), g);
        g.weightx = 0.7;
        g.gridx = 1;
        txt_password = new JPasswordField();
        txt_password.setPreferredSize(new Dimension(150, 30));
        txt_password.setText("1234");
        panel.add(txt_password, g);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        lbl_status = new JLabel(" ", JLabel.CENTER);
        lbl_status.setForeground(Color.RED);
        panel.add(lbl_status, g);

        JButton btn_login = new JButton("دخول");
        btn_login.setPreferredSize(new Dimension(130, 38));
        btn_login.addActionListener(e -> btn_loginActionPerformed());
        g.gridy = 5;
        panel.add(btn_login, g);

        JButton btn_back = new JButton("رجوع");
        g.gridy = 6;
        btn_back.addActionListener(e -> {
            new MainMenuFrame().setVisible(true);
            dispose();
        });
        panel.add(btn_back, g);

        // Enter key triggers login
        txt_password.addActionListener(e -> btn_loginActionPerformed());

        add(panel);
        setVisible(true);
    }

    private void btn_loginActionPerformed() {
        String username = txt_username.getText().trim().toLowerCase();
        String password = new String(txt_password.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lbl_status.setText("ادخل اسم الطاولة وكلمة المرور");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            String sql = "select id, table_number, account_name from dining_tables where account_name=? and password_hash=? and active=1 and status='OPEN'";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int table_num = rs.getInt("table_number");
                String table_name = rs.getString("account_name");
                rs.close();
                pst.close();
                con.close();

                new CustomerFrame(table_num, table_name).setVisible(true);
                dispose();
            } else {
                rs.close();
                pst.close();
                con.close();
                lbl_status.setText("اسم الطاولة أو كلمة المرور غير صحيحة");
            }

        } catch (Exception e) {
            lbl_status.setText("خطأ: " + e.getMessage());
        }
    }
}
