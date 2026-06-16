import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainMenuFrame extends JFrame {

    public MainMenuFrame() {
        setTitle("نظام المطعم الصحي");
        setSize(420, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(12, 25, 12, 25);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("نظام المطعم الصحي", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        g.gridx = 0; g.gridy = 0;
        panel.add(title, g);

        JLabel sub = new JLabel("اختر نوع الدخول", JLabel.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 13));
        sub.setForeground(Color.GRAY);
        g.gridy = 1;
        panel.add(sub, g);

        JButton btn_customer = new JButton("دخول الزبون  (تسجيل دخول الطاولة)");
        btn_customer.setPreferredSize(new Dimension(280, 48));
        btn_customer.setFont(new Font("Arial", Font.PLAIN, 14));
        btn_customer.addActionListener(e -> btn_customerActionPerformed());
        g.gridy = 2;
        panel.add(btn_customer, g);

        JButton btn_chef = new JButton("دخول الشيف");
        btn_chef.setPreferredSize(new Dimension(280, 48));
        btn_chef.setFont(new Font("Arial", Font.PLAIN, 14));
        btn_chef.addActionListener(e -> btn_chefActionPerformed());
        g.gridy = 3;
        panel.add(btn_chef, g);

        JButton btn_admin = new JButton("دخول المدير");
        btn_admin.setPreferredSize(new Dimension(280, 48));
        btn_admin.setFont(new Font("Arial", Font.PLAIN, 14));
        btn_admin.addActionListener(e -> btn_adminActionPerformed());
        g.gridy = 4;
        panel.add(btn_admin, g);

        add(panel);
        setVisible(true);
    }

    private void btn_customerActionPerformed() {
        new TableLoginFrame();
        dispose();
    }

    private void btn_chefActionPerformed() {
        new LoginFrame("CHEF");
        dispose();
    }

    private void btn_adminActionPerformed() {
        new LoginFrame("ADMIN");
        dispose();
    }

    public static void main(String[] args) {
        // تهيئة حسابات الطاولات عند أول تشغيل
        DBConnection.initTableAccounts();
        SwingUtilities.invokeLater(() -> new MainMenuFrame());
    }
}
