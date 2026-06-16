import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ChefFrame extends JFrame {

    JTable tbl_orders;
    DefaultTableModel model_orders;
    int[] order_ids;

    JTextArea txt_details;

    public ChefFrame() {
        setTitle("نظام المطعم الصحي - واجهة الشيف");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // شريط الأدوات
        JPanel top_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top_panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JButton btn_back = new JButton("رجوع للرئيسية");
        btn_back.addActionListener(e -> btn_backActionPerformed());
        top_panel.add(btn_back);

        JButton btn_refresh = new JButton("تحديث الطلبات");
        btn_refresh.addActionListener(e -> btn_refreshOrdersActionPerformed());
        top_panel.add(btn_refresh);

        JButton btn_invoice = new JButton("عرض الفاتورة");
        btn_invoice.setBackground(new Color(70, 130, 180));
        btn_invoice.setForeground(Color.WHITE);
        btn_invoice.addActionListener(e -> btn_showInvoiceActionPerformed());
        top_panel.add(btn_invoice);

        // أزرار تحديث الحالة
        JPanel status_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        status_panel.setBorder(BorderFactory.createTitledBorder("تحديث حالة الطلب المحدد"));

        JButton btn_preparing = new JButton("قيد التحضير");
        btn_preparing.setBackground(new Color(255, 200, 80));
        btn_preparing.addActionListener(e -> btn_setStatusActionPerformed("PREPARING"));
        status_panel.add(btn_preparing);

        JButton btn_ready = new JButton("جاهز للاستلام");
        btn_ready.setBackground(new Color(100, 200, 100));
        btn_ready.addActionListener(e -> btn_setStatusActionPerformed("READY"));
        status_panel.add(btn_ready);

        JButton btn_completed = new JButton("مكتمل  (طباعة الفاتورة)");
        btn_completed.setBackground(new Color(100, 149, 237));
        btn_completed.setForeground(Color.WHITE);
        btn_completed.addActionListener(e -> btn_completeAndPrintActionPerformed());
        status_panel.add(btn_completed);

        JButton btn_cancelled = new JButton("إلغاء الطلب");
        btn_cancelled.setBackground(new Color(220, 80, 80));
        btn_cancelled.setForeground(Color.WHITE);
        btn_cancelled.addActionListener(e -> btn_setStatusActionPerformed("CANCELLED"));
        status_panel.add(btn_cancelled);

        JPanel north = new JPanel(new BorderLayout());
        north.add(top_panel, BorderLayout.NORTH);
        north.add(status_panel, BorderLayout.SOUTH);

        // جدول الطلبات
        model_orders = new DefaultTableModel(
                new Object[]{"#", "الطاولة", "نوع الطلب", "الحالة", "المجموع د.ل", "الوقت"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_orders = new JTable(model_orders);
        tbl_orders.setRowHeight(27);
        tbl_orders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_orders.getTableHeader().setReorderingAllowed(false);
        tbl_orders.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) tbl_ordersValueChanged();
        });

        // منطقة التفاصيل
        txt_details = new JTextArea();
        txt_details.setEditable(false);
        txt_details.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txt_details.setMargin(new Insets(10, 10, 10, 10));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tbl_orders), new JScrollPane(txt_details));
        split.setDividerLocation(600);

        add(north, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        btn_refreshOrdersActionPerformed();
    }

    private void btn_refreshOrdersActionPerformed() {
        try {
            model_orders.setRowCount(0);
            txt_details.setText("");
            Connection con = DBConnection.getConnection();
            String sql = "select id, table_number, order_type, status, subtotal, created_at from orders where status in ('NEW','PREPARING','READY') order by created_at";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
                String otype = rs.getString("order_type").equals("READY_MEAL") ? "وجبة جاهزة" : "وجبة مخصصة";
                String st = rs.getString("status");
                String st_ar = st.equals("NEW") ? "🆕 جديد" : st.equals("PREPARING") ? "🔥 تحضير" :
                               st.equals("READY") ? "✅ جاهز" : st;
                model_orders.addRow(new Object[]{
                    rs.getInt("id"), rs.getInt("table_number"), otype, st_ar,
                    rs.getString("subtotal"), rs.getString("created_at")
                });
            }
            order_ids = new int[ids.size()];
            for (int i = 0; i < ids.size(); i++) order_ids[i] = ids.get(i);
            rs.close(); pst.close(); con.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage());
        }
    }

    private void tbl_ordersValueChanged() {
        int row = tbl_orders.getSelectedRow();
        if (row < 0 || order_ids == null || row >= order_ids.length) return;
        showOrderDetails(order_ids[row]);
    }

    private void showOrderDetails(int order_id) {
        try {
            Connection con = DBConnection.getConnection();
            StringBuilder d = new StringBuilder();
            d.append("== تفاصيل الطلب #").append(order_id).append(" ==\n\n");

            String sql_o = "select table_number, order_type, status, subtotal, calories, protein_g, carbs_g, fat_g, health_notes, created_at from orders where id=?";
            PreparedStatement pst_o = con.prepareStatement(sql_o);
            pst_o.setInt(1, order_id);
            ResultSet rs_o = pst_o.executeQuery();
            boolean is_custom = false;
            if (rs_o.next()) {
                d.append("الطاولة  : ").append(rs_o.getInt("table_number")).append("\n");
                is_custom = rs_o.getString("order_type").equals("CUSTOM_MEAL");
                d.append("النوع    : ").append(is_custom ? "وجبة مخصصة" : "وجبة جاهزة").append("\n");
                d.append("الحالة   : ").append(rs_o.getString("status")).append("\n");
                d.append("المجموع  : ").append(rs_o.getString("subtotal")).append(" د.ل\n");
                d.append("السعرات  : ").append(String.format("%.0f", rs_o.getDouble("calories"))).append("\n");
                d.append("بروتين   : ").append(String.format("%.1f", rs_o.getDouble("protein_g"))).append(" ج\n");
                d.append("كربو     : ").append(String.format("%.1f", rs_o.getDouble("carbs_g"))).append(" ج\n");
                d.append("دهون     : ").append(String.format("%.1f", rs_o.getDouble("fat_g"))).append(" ج\n");
                d.append("الوقت    : ").append(rs_o.getString("created_at")).append("\n");
                String notes = rs_o.getString("health_notes");
                if (notes != null && !notes.isEmpty()) d.append("\n").append(notes).append("\n");
            }
            rs_o.close(); pst_o.close();

            d.append("\n--- العناصر ---\n");
            String sql_i = "select name, quantity, unit_price, calories, protein_g, carbs_g, fat_g from order_items where order_id=?";
            PreparedStatement pst_i = con.prepareStatement(sql_i);
            pst_i.setInt(1, order_id);
            ResultSet rs_i = pst_i.executeQuery();
            while (rs_i.next()) {
                d.append("• ").append(rs_i.getString("name"))
                 .append("  x").append(rs_i.getInt("quantity"))
                 .append("  | ").append(rs_i.getString("unit_price")).append(" د.ل")
                 .append("  | ").append(String.format("%.0f", rs_i.getDouble("calories"))).append(" سعرة\n");
            }
            rs_i.close(); pst_i.close();

            if (is_custom) {
                d.append("\n--- المكونات المختارة ---\n");
                String sql_ing = "select oii.ingredient_name, oii.category, oii.unit_price, oii.calories, oii.protein_g, oii.carbs_g, oii.fat_g from order_item_ingredients oii join order_items oi on oii.order_item_id=oi.id where oi.order_id=?";
                PreparedStatement pst_ing = con.prepareStatement(sql_ing);
                pst_ing.setInt(1, order_id);
                ResultSet rs_ing = pst_ing.executeQuery();
                while (rs_ing.next()) {
                    d.append("  - ").append(rs_ing.getString("ingredient_name"))
                     .append("  | ").append(rs_ing.getString("unit_price")).append(" د.ل")
                     .append("  | ").append(String.format("%.0f", rs_ing.getDouble("calories"))).append(" سعرة\n");
                }
                rs_ing.close(); pst_ing.close();
            }

            con.close();
            txt_details.setText(d.toString());
            txt_details.setCaretPosition(0);

        } catch (Exception e) {
            txt_details.setText("خطأ: " + e.getMessage());
        }
    }

    private void btn_setStatusActionPerformed(String new_status) {
        int row = tbl_orders.getSelectedRow();
        if (row < 0 || order_ids == null || row >= order_ids.length) {
            JOptionPane.showMessageDialog(this, "اختر طلب أولا");
            return;
        }
        int order_id = order_ids[row];
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("update orders set status=? where id=?");
            pst.setString(1, new_status);
            pst.setInt(2, order_id);
            pst.executeUpdate();
            pst.close();
            con.close();
            btn_refreshOrdersActionPerformed();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage());
        }
    }

    private void btn_completeAndPrintActionPerformed() {
        int row = tbl_orders.getSelectedRow();
        if (row < 0 || order_ids == null || row >= order_ids.length) {
            JOptionPane.showMessageDialog(this, "اختر طلب أولا");
            return;
        }

        int order_id = order_ids[row];

        // تحديث الحالة أولا
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("update orders set status='COMPLETED' where id=?");
            pst.setInt(1, order_id);
            pst.executeUpdate();
            pst.close();
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ في تحديث الحالة:\n" + e.getMessage());
            return;
        }

        // طباعة الفاتورة
        printInvoice(order_id);
        btn_refreshOrdersActionPerformed();
    }

    private void btn_showInvoiceActionPerformed() {
        int row = tbl_orders.getSelectedRow();
        if (row < 0 || order_ids == null || row >= order_ids.length) {
            JOptionPane.showMessageDialog(this, "اختر طلب لعرض الفاتورة");
            return;
        }
        printInvoice(order_ids[row]);
    }

    private void printInvoice(int order_id) {
        try {
            Connection con = DBConnection.getConnection();
            StringBuilder inv = new StringBuilder();

            inv.append("========================================\n");
            inv.append("        المطعم الصحي\n");
            inv.append("========================================\n");

            String sql_o = "select table_number, order_type, subtotal, calories, created_at from orders where id=?";
            PreparedStatement pst_o = con.prepareStatement(sql_o);
            pst_o.setInt(1, order_id);
            ResultSet rs_o = pst_o.executeQuery();

            double subtotal = 0;
            if (rs_o.next()) {
                inv.append("رقم الطلب : #").append(order_id).append("\n");
                inv.append("الطاولة   : ").append(rs_o.getInt("table_number")).append("\n");
                inv.append("النوع     : ").append(rs_o.getString("order_type").equals("READY_MEAL") ? "وجبة جاهزة" : "وجبة مخصصة").append("\n");
                inv.append("التاريخ   : ").append(rs_o.getString("created_at")).append("\n");
                subtotal = rs_o.getDouble("subtotal");
                inv.append("السعرات   : ").append(String.format("%.0f", rs_o.getDouble("calories"))).append("\n");
            }
            rs_o.close(); pst_o.close();

            inv.append("----------------------------------------\n");
            inv.append(String.format("%-22s %6s  %8s\n", "الصنف", "الكمية", "السعر"));
            inv.append("----------------------------------------\n");

            String sql_i = "select name, quantity, unit_price from order_items where order_id=?";
            PreparedStatement pst_i = con.prepareStatement(sql_i);
            pst_i.setInt(1, order_id);
            ResultSet rs_i = pst_i.executeQuery();
            int item_count = 0;
            while (rs_i.next()) {
                item_count++;
                String name = rs_i.getString("name");
                if (name.length() > 22) name = name.substring(0, 20) + "..";
                inv.append(String.format("%-22s %6d  %8.2f\n",
                    name, rs_i.getInt("quantity"), rs_i.getDouble("unit_price") * rs_i.getInt("quantity")));
            }
            rs_i.close(); pst_i.close();
            con.close();

            inv.append("========================================\n");
            inv.append(String.format("  عدد الأصناف: %-5d\n", item_count));
            inv.append(String.format("  الإجمالي   :         %.2f د.ل\n", subtotal));
            inv.append("========================================\n");
            inv.append("        شكراً لزيارتكم!\n");
            inv.append("========================================");

            // عرض الفاتورة في نافذة
            JTextArea invoice_area = new JTextArea(inv.toString());
            invoice_area.setFont(new Font("Monospaced", Font.PLAIN, 14));
            invoice_area.setEditable(false);
            invoice_area.setMargin(new Insets(10, 15, 10, 15));
            invoice_area.setBackground(new Color(255, 255, 250));

            JScrollPane scroll = new JScrollPane(invoice_area);
            scroll.setPreferredSize(new Dimension(420, 450));

            JOptionPane.showMessageDialog(this, scroll, "الفاتورة - طلب #" + order_id,
                JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ في طباعة الفاتورة:\n" + e.getMessage());
        }
    }

    private void btn_backActionPerformed() {
        new MainMenuFrame().setVisible(true);
        dispose();
    }
}
