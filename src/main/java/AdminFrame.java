import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminFrame extends JFrame {

    // تاب المكونات
    JTable tbl_ingredients;
    DefaultTableModel model_ingredients;

    // تاب الوجبات الجاهزة
    JTable tbl_meals;
    DefaultTableModel model_meals;

    // تاب الطلبات
    JTable tbl_orders;
    DefaultTableModel model_orders;

    // تاب الموظفين
    JTable tbl_staff;
    DefaultTableModel model_staff;

    // تاب الطاولات
    JTable tbl_tables;
    DefaultTableModel model_tables;

    // تاب الإحصائيات
    JLabel lbl_total_orders, lbl_completed, lbl_cancelled, lbl_revenue;
    JLabel lbl_ready_orders, lbl_custom_orders, lbl_avg_order;
    JTable tbl_stats_detail;
    DefaultTableModel model_stats;

    public AdminFrame() {
        setTitle("نظام المطعم الصحي - واجهة المدير");
        setSize(1200, 780);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel top_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top_panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        JButton btn_back = new JButton("رجوع للرئيسية");
        btn_back.addActionListener(e -> btn_backActionPerformed());
        top_panel.add(btn_back);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("إدارة المكونات",      buildIngredientsTab());
        tabs.addTab("إدارة الوجبات الجاهزة", buildReadyMealsTab());
        tabs.addTab("الطلبات",             buildOrdersTab());
        tabs.addTab("إدارة المستخدمين",    buildUserManagementTab());
        tabs.addTab("الإحصائيات",          buildStatisticsTab());

        add(top_panel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        // تحميل كل البيانات عند الفتح
        btn_refreshIngredientsActionPerformed();
        btn_refreshMealsActionPerformed();
        btn_refreshOrdersActionPerformed();
        btn_refreshStaffActionPerformed();
        btn_refreshTablesActionPerformed();
        btn_refreshStatsActionPerformed();
    }

    // ==========================================================
    //  تاب المكونات
    // ==========================================================
    JPanel buildIngredientsTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model_ingredients = new DefaultTableModel(
                new Object[]{"#", "المكون", "الفئة", "الحصة", "سعرات", "بروتين ج", "كربو ج", "دهون ج", "السعر د.ل", "متاح"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_ingredients = new JTable(model_ingredients);
        tbl_ingredients.setRowHeight(25);
        tbl_ingredients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_ingredients.getTableHeader().setReorderingAllowed(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton btn_refresh = new JButton("تحديث");
        btn_refresh.addActionListener(e -> btn_refreshIngredientsActionPerformed());
        actions.add(btn_refresh);
        JButton btn_add = new JButton("إضافة مكون");
        btn_add.addActionListener(e -> btn_addIngredientActionPerformed());
        actions.add(btn_add);
        JButton btn_edit = new JButton("تعديل المحدد");
        btn_edit.addActionListener(e -> btn_editIngredientActionPerformed());
        actions.add(btn_edit);
        JButton btn_delete = new JButton("حذف المحدد");
        btn_delete.addActionListener(e -> btn_deleteIngredientActionPerformed());
        actions.add(btn_delete);
        JButton btn_toggle = new JButton("تفعيل / إيقاف");
        btn_toggle.addActionListener(e -> btn_toggleIngredientActionPerformed());
        actions.add(btn_toggle);

        panel.add(new JScrollPane(tbl_ingredients), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void btn_refreshIngredientsActionPerformed() {
        try {
            model_ingredients.setRowCount(0);
            Connection con = DBConnection.getConnection();
            String sql = "select id, name, category, serving_label, calories, protein_g, carbs_g, fat_g, price, available from ingredients order by category, name";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String cat = rs.getString("category");
                String cat_ar = cat.equals("PROTEIN") ? "بروتين" : cat.equals("CARB") ? "كربو" :
                                cat.equals("FAT") ? "دهون" : cat.equals("ADDON") ? "إضافات" : "صلصة";
                model_ingredients.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("name"), cat_ar, rs.getString("serving_label"),
                    String.format("%.0f", rs.getDouble("calories")),
                    String.format("%.1f", rs.getDouble("protein_g")),
                    String.format("%.1f", rs.getDouble("carbs_g")),
                    String.format("%.1f", rs.getDouble("fat_g")),
                    rs.getString("price"),
                    rs.getInt("available") == 1 ? "✓ نعم" : "✗ لا"
                });
            }
            rs.close(); pst.close(); con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage());
        }
    }

    private void btn_addIngredientActionPerformed() {
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField f_name = new JTextField();
        JComboBox f_cat = new JComboBox(new String[]{"PROTEIN", "CARB", "FAT", "ADDON", "SAUCE"});
        JTextField f_serving = new JTextField("150 جرام");
        JTextField f_price = new JTextField("10.00");
        JTextField f_cal = new JTextField("200");
        JTextField f_prot = new JTextField("0");
        JTextField f_carbs = new JTextField("0");
        JTextField f_fat = new JTextField("0");
        JCheckBox f_lactose = new JCheckBox(); JCheckBox f_gluten = new JCheckBox();
        JCheckBox f_sugar = new JCheckBox(); JCheckBox f_sodium = new JCheckBox();
        JCheckBox f_highfat = new JCheckBox();

        form.add(new JLabel("الاسم:"));          form.add(f_name);
        form.add(new JLabel("الفئة:"));           form.add(f_cat);
        form.add(new JLabel("الحصة:"));           form.add(f_serving);
        form.add(new JLabel("السعر:"));           form.add(f_price);
        form.add(new JLabel("السعرات:"));         form.add(f_cal);
        form.add(new JLabel("بروتين (ج):"));      form.add(f_prot);
        form.add(new JLabel("كربو (ج):"));        form.add(f_carbs);
        form.add(new JLabel("دهون (ج):"));        form.add(f_fat);
        form.add(new JLabel("يحتوي لاكتوز:"));   form.add(f_lactose);
        form.add(new JLabel("يحتوي غلوتين:"));   form.add(f_gluten);
        form.add(new JLabel("سكر عالي:"));        form.add(f_sugar);
        form.add(new JLabel("صوديوم عالي:"));     form.add(f_sodium);
        form.add(new JLabel("دهون عالية:"));      form.add(f_highfat);

        int r = JOptionPane.showConfirmDialog(this, form, "إضافة مكون جديد", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION || f_name.getText().trim().isEmpty()) return;

        try {
            Connection con = DBConnection.getConnection();
            String sql = "insert into ingredients (name, category, serving_label, price, calories, protein_g, carbs_g, fat_g, contains_lactose, contains_gluten, high_sugar, high_sodium, high_fat, available) values (?,?,?,?,?,?,?,?,?,?,?,?,?,1)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, f_name.getText().trim()); pst.setString(2, f_cat.getSelectedItem().toString());
            pst.setString(3, f_serving.getText().trim()); pst.setDouble(4, Double.parseDouble(f_price.getText().trim()));
            pst.setDouble(5, Double.parseDouble(f_cal.getText().trim())); pst.setDouble(6, Double.parseDouble(f_prot.getText().trim()));
            pst.setDouble(7, Double.parseDouble(f_carbs.getText().trim())); pst.setDouble(8, Double.parseDouble(f_fat.getText().trim()));
            pst.setInt(9, f_lactose.isSelected()?1:0); pst.setInt(10, f_gluten.isSelected()?1:0);
            pst.setInt(11, f_sugar.isSelected()?1:0); pst.setInt(12, f_sodium.isSelected()?1:0);
            pst.setInt(13, f_highfat.isSelected()?1:0);
            pst.executeUpdate(); pst.close(); con.close();
            JOptionPane.showMessageDialog(this, "تمت الإضافة بنجاح");
            btn_refreshIngredientsActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    private void btn_editIngredientActionPerformed() {
        int row = tbl_ingredients.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر مكون للتعديل"); return; }
        int id = Integer.parseInt(model_ingredients.getValueAt(row, 0).toString());
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("select name, serving_label, price, calories, protein_g, carbs_g, fat_g from ingredients where id=?");
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) { rs.close(); pst.close(); con.close(); return; }

            JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
            JTextField f_name = new JTextField(rs.getString("name"));
            JTextField f_serving = new JTextField(rs.getString("serving_label"));
            JTextField f_price = new JTextField(rs.getString("price"));
            JTextField f_cal = new JTextField(String.format("%.1f", rs.getDouble("calories")));
            JTextField f_prot = new JTextField(String.format("%.1f", rs.getDouble("protein_g")));
            JTextField f_carbs = new JTextField(String.format("%.1f", rs.getDouble("carbs_g")));
            JTextField f_fat = new JTextField(String.format("%.1f", rs.getDouble("fat_g")));
            rs.close(); pst.close();

            form.add(new JLabel("الاسم:")); form.add(f_name);
            form.add(new JLabel("الحصة:")); form.add(f_serving);
            form.add(new JLabel("السعر:")); form.add(f_price);
            form.add(new JLabel("السعرات:")); form.add(f_cal);
            form.add(new JLabel("بروتين (ج):")); form.add(f_prot);
            form.add(new JLabel("كربو (ج):")); form.add(f_carbs);
            form.add(new JLabel("دهون (ج):")); form.add(f_fat);

            int r = JOptionPane.showConfirmDialog(this, form, "تعديل المكون #" + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) { con.close(); return; }

            PreparedStatement pst2 = con.prepareStatement("update ingredients set name=?, serving_label=?, price=?, calories=?, protein_g=?, carbs_g=?, fat_g=? where id=?");
            pst2.setString(1, f_name.getText().trim()); pst2.setString(2, f_serving.getText().trim());
            pst2.setDouble(3, Double.parseDouble(f_price.getText().trim())); pst2.setDouble(4, Double.parseDouble(f_cal.getText().trim()));
            pst2.setDouble(5, Double.parseDouble(f_prot.getText().trim())); pst2.setDouble(6, Double.parseDouble(f_carbs.getText().trim()));
            pst2.setDouble(7, Double.parseDouble(f_fat.getText().trim())); pst2.setInt(8, id);
            pst2.executeUpdate(); pst2.close(); con.close();
            JOptionPane.showMessageDialog(this, "تم التعديل"); btn_refreshIngredientsActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    private void btn_deleteIngredientActionPerformed() {
        int row = tbl_ingredients.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر مكون للحذف"); return; }
        int id = Integer.parseInt(model_ingredients.getValueAt(row, 0).toString());
        if (JOptionPane.showConfirmDialog(this, "تأكيد الحذف؟", "حذف", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("delete from ingredients where id=?");
            pst.setInt(1, id); pst.executeUpdate(); pst.close(); con.close();
            JOptionPane.showMessageDialog(this, "تم الحذف"); btn_refreshIngredientsActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "لا يمكن الحذف (مرتبط بطلبات):\n" + e.getMessage()); }
    }

    private void btn_toggleIngredientActionPerformed() {
        int row = tbl_ingredients.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر مكوناً"); return; }
        int id = Integer.parseInt(model_ingredients.getValueAt(row, 0).toString());
        int new_val = model_ingredients.getValueAt(row, 9).toString().startsWith("✓") ? 0 : 1;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("update ingredients set available=? where id=?");
            pst.setInt(1, new_val); pst.setInt(2, id);
            pst.executeUpdate(); pst.close(); con.close();
            btn_refreshIngredientsActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    // ==========================================================
    //  تاب الوجبات الجاهزة
    // ==========================================================
    JPanel buildReadyMealsTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model_meals = new DefaultTableModel(
                new Object[]{"#", "الاسم", "الوصف", "سعرات", "بروتين ج", "كربو ج", "دهون ج", "السعر د.ل", "متاح"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_meals = new JTable(model_meals);
        tbl_meals.setRowHeight(25);
        tbl_meals.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_meals.getTableHeader().setReorderingAllowed(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton btn_r = new JButton("تحديث"); btn_r.addActionListener(e -> btn_refreshMealsActionPerformed()); actions.add(btn_r);
        JButton btn_a = new JButton("إضافة وجبة"); btn_a.addActionListener(e -> btn_addMealActionPerformed()); actions.add(btn_a);
        JButton btn_e = new JButton("تعديل المحدد"); btn_e.addActionListener(e -> btn_editMealActionPerformed()); actions.add(btn_e);
        JButton btn_d = new JButton("حذف المحدد"); btn_d.addActionListener(e -> btn_deleteMealActionPerformed()); actions.add(btn_d);
        JButton btn_t = new JButton("تفعيل / إيقاف"); btn_t.addActionListener(e -> btn_toggleMealActionPerformed()); actions.add(btn_t);

        panel.add(new JScrollPane(tbl_meals), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void btn_refreshMealsActionPerformed() {
        try {
            model_meals.setRowCount(0);
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("select id, name, description, calories, protein_g, carbs_g, fat_g, price, available from ready_meals order by id");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model_meals.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("name"), rs.getString("description"),
                    String.format("%.0f", rs.getDouble("calories")), String.format("%.1f", rs.getDouble("protein_g")),
                    String.format("%.1f", rs.getDouble("carbs_g")), String.format("%.1f", rs.getDouble("fat_g")),
                    rs.getString("price"), rs.getInt("available") == 1 ? "✓ نعم" : "✗ لا"
                });
            }
            rs.close(); pst.close(); con.close();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    private void btn_addMealActionPerformed() {
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        JTextField f_name = new JTextField(); JTextField f_desc = new JTextField();
        JTextField f_price = new JTextField("25.00"); JTextField f_cal = new JTextField("500");
        JTextField f_prot = new JTextField("30"); JTextField f_carbs = new JTextField("60"); JTextField f_fat = new JTextField("15");
        form.add(new JLabel("الاسم:")); form.add(f_name);
        form.add(new JLabel("الوصف:")); form.add(f_desc);
        form.add(new JLabel("السعر:")); form.add(f_price);
        form.add(new JLabel("السعرات:")); form.add(f_cal);
        form.add(new JLabel("بروتين (ج):")); form.add(f_prot);
        form.add(new JLabel("كربو (ج):")); form.add(f_carbs);
        form.add(new JLabel("دهون (ج):")); form.add(f_fat);
        int r = JOptionPane.showConfirmDialog(this, form, "إضافة وجبة جاهزة", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION || f_name.getText().trim().isEmpty()) return;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("insert into ready_meals (name, description, price, calories, protein_g, carbs_g, fat_g, available) values (?,?,?,?,?,?,?,1)");
            pst.setString(1, f_name.getText().trim()); pst.setString(2, f_desc.getText().trim());
            pst.setDouble(3, Double.parseDouble(f_price.getText().trim())); pst.setDouble(4, Double.parseDouble(f_cal.getText().trim()));
            pst.setDouble(5, Double.parseDouble(f_prot.getText().trim())); pst.setDouble(6, Double.parseDouble(f_carbs.getText().trim()));
            pst.setDouble(7, Double.parseDouble(f_fat.getText().trim()));
            pst.executeUpdate(); pst.close(); con.close();
            JOptionPane.showMessageDialog(this, "تمت الإضافة"); btn_refreshMealsActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    private void btn_editMealActionPerformed() {
        int row = tbl_meals.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر وجبة"); return; }
        int id = Integer.parseInt(model_meals.getValueAt(row, 0).toString());
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("select name, description, price, calories, protein_g, carbs_g, fat_g from ready_meals where id=?");
            pst.setInt(1, id); ResultSet rs = pst.executeQuery();
            if (!rs.next()) { rs.close(); pst.close(); con.close(); return; }
            JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
            JTextField f_name = new JTextField(rs.getString("name")); JTextField f_desc = new JTextField(rs.getString("description"));
            JTextField f_price = new JTextField(rs.getString("price")); JTextField f_cal = new JTextField(String.format("%.1f", rs.getDouble("calories")));
            JTextField f_prot = new JTextField(String.format("%.1f", rs.getDouble("protein_g"))); JTextField f_carbs = new JTextField(String.format("%.1f", rs.getDouble("carbs_g")));
            JTextField f_fat = new JTextField(String.format("%.1f", rs.getDouble("fat_g")));
            rs.close(); pst.close();
            form.add(new JLabel("الاسم:")); form.add(f_name); form.add(new JLabel("الوصف:")); form.add(f_desc);
            form.add(new JLabel("السعر:")); form.add(f_price); form.add(new JLabel("السعرات:")); form.add(f_cal);
            form.add(new JLabel("بروتين:")); form.add(f_prot); form.add(new JLabel("كربو:")); form.add(f_carbs);
            form.add(new JLabel("دهون:")); form.add(f_fat);
            int r = JOptionPane.showConfirmDialog(this, form, "تعديل وجبة #" + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) { con.close(); return; }
            PreparedStatement pst2 = con.prepareStatement("update ready_meals set name=?, description=?, price=?, calories=?, protein_g=?, carbs_g=?, fat_g=? where id=?");
            pst2.setString(1, f_name.getText().trim()); pst2.setString(2, f_desc.getText().trim());
            pst2.setDouble(3, Double.parseDouble(f_price.getText().trim())); pst2.setDouble(4, Double.parseDouble(f_cal.getText().trim()));
            pst2.setDouble(5, Double.parseDouble(f_prot.getText().trim())); pst2.setDouble(6, Double.parseDouble(f_carbs.getText().trim()));
            pst2.setDouble(7, Double.parseDouble(f_fat.getText().trim())); pst2.setInt(8, id);
            pst2.executeUpdate(); pst2.close(); con.close();
            JOptionPane.showMessageDialog(this, "تم التعديل"); btn_refreshMealsActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    private void btn_deleteMealActionPerformed() {
        int row = tbl_meals.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر وجبة"); return; }
        int id = Integer.parseInt(model_meals.getValueAt(row, 0).toString());
        if (JOptionPane.showConfirmDialog(this, "تأكيد الحذف؟", "حذف", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("delete from ready_meals where id=?");
            pst.setInt(1, id); pst.executeUpdate(); pst.close(); con.close();
            JOptionPane.showMessageDialog(this, "تم الحذف"); btn_refreshMealsActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "لا يمكن الحذف:\n" + e.getMessage()); }
    }

    private void btn_toggleMealActionPerformed() {
        int row = tbl_meals.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر وجبة"); return; }
        int id = Integer.parseInt(model_meals.getValueAt(row, 0).toString());
        int new_val = model_meals.getValueAt(row, 8).toString().startsWith("✓") ? 0 : 1;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("update ready_meals set available=? where id=?");
            pst.setInt(1, new_val); pst.setInt(2, id); pst.executeUpdate(); pst.close(); con.close();
            btn_refreshMealsActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    // ==========================================================
    //  تاب الطلبات
    // ==========================================================
    JPanel buildOrdersTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        model_orders = new DefaultTableModel(
                new Object[]{"#", "الطاولة", "النوع", "الحالة", "المجموع د.ل", "سعرات", "الوقت"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_orders = new JTable(model_orders);
        tbl_orders.setRowHeight(25); tbl_orders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btn_r = new JButton("تحديث"); btn_r.addActionListener(e -> btn_refreshOrdersActionPerformed()); actions.add(btn_r);
        panel.add(new JScrollPane(tbl_orders), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void btn_refreshOrdersActionPerformed() {
        try {
            model_orders.setRowCount(0);
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("select id, table_number, order_type, status, subtotal, calories, created_at from orders order by created_at desc limit 50");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String otype = rs.getString("order_type").equals("READY_MEAL") ? "جاهزة" : "مخصصة";
                String st = rs.getString("status");
                String st_ar = st.equals("NEW")?"جديد":st.equals("PREPARING")?"تحضير":st.equals("READY")?"جاهز":st.equals("COMPLETED")?"مكتمل":"ملغي";
                model_orders.addRow(new Object[]{
                    rs.getInt("id"), rs.getInt("table_number"), otype, st_ar,
                    rs.getString("subtotal"), String.format("%.0f", rs.getDouble("calories")), rs.getString("created_at")
                });
            }
            rs.close(); pst.close(); con.close();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    // ==========================================================
    //  تاب إدارة المستخدمين (موظفون + طاولات)
    // ==========================================================
    JPanel buildUserManagementTab() {
        JTabbedPane sub_tabs = new JTabbedPane();
        sub_tabs.addTab("حسابات الموظفين", buildStaffSubTab());
        sub_tabs.addTab("حسابات الطاولات", buildTablesSubTab());
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(sub_tabs, BorderLayout.CENTER);
        return panel;
    }

    JPanel buildStaffSubTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        model_staff = new DefaultTableModel(
                new Object[]{"#", "اسم المستخدم", "الاسم الكامل", "الدور", "نشط"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_staff = new JTable(model_staff);
        tbl_staff.setRowHeight(25); tbl_staff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton btn_r = new JButton("تحديث"); btn_r.addActionListener(e -> btn_refreshStaffActionPerformed()); actions.add(btn_r);
        JButton btn_a = new JButton("إضافة موظف"); btn_a.addActionListener(e -> btn_addStaffActionPerformed()); actions.add(btn_a);
        JButton btn_e = new JButton("تعديل"); btn_e.addActionListener(e -> btn_editStaffActionPerformed()); actions.add(btn_e);
        JButton btn_t = new JButton("تفعيل / إيقاف"); btn_t.addActionListener(e -> btn_toggleStaffActionPerformed()); actions.add(btn_t);
        panel.add(new JScrollPane(tbl_staff), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void btn_refreshStaffActionPerformed() {
        try {
            model_staff.setRowCount(0);
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("select id, username, full_name, role, active from users order by role, username");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model_staff.addRow(new Object[]{rs.getInt("id"), rs.getString("username"), rs.getString("full_name"), rs.getString("role"), rs.getInt("active")==1?"✓ نشط":"✗ موقوف"});
            }
            rs.close(); pst.close(); con.close();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    private void btn_addStaffActionPerformed() {
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        JTextField f_user = new JTextField(); JTextField f_name = new JTextField();
        JPasswordField f_pass = new JPasswordField(); JComboBox f_role = new JComboBox(new String[]{"ADMIN","CHEF"});
        form.add(new JLabel("اسم المستخدم:")); form.add(f_user);
        form.add(new JLabel("الاسم الكامل:")); form.add(f_name);
        form.add(new JLabel("كلمة المرور:")); form.add(f_pass);
        form.add(new JLabel("الدور:")); form.add(f_role);
        int r = JOptionPane.showConfirmDialog(this, form, "إضافة موظف", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        String user = f_user.getText().trim(), name = f_name.getText().trim(), pass = new String(f_pass.getPassword());
        if (user.isEmpty() || name.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(this, "جميع الحقول مطلوبة"); return; }
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("insert into users (username, password_hash, full_name, role, active) values (?, ?, ?, ?, 1)");
            pst.setString(1, user); pst.setString(2, pass); pst.setString(3, name); pst.setString(4, f_role.getSelectedItem().toString());
            pst.executeUpdate(); pst.close(); con.close();
            JOptionPane.showMessageDialog(this, "تمت الإضافة"); btn_refreshStaffActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    private void btn_editStaffActionPerformed() {
        int row = tbl_staff.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر موظف"); return; }
        int id = Integer.parseInt(model_staff.getValueAt(row, 0).toString());
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        JTextField f_name = new JTextField(model_staff.getValueAt(row, 2).toString());
        JPasswordField f_pass = new JPasswordField();
        JComboBox f_role = new JComboBox(new String[]{"ADMIN","CHEF"});
        f_role.setSelectedItem(model_staff.getValueAt(row, 3).toString());
        form.add(new JLabel("الاسم الكامل:")); form.add(f_name);
        form.add(new JLabel("كلمة مرور جديدة:")); form.add(f_pass);
        form.add(new JLabel("(فارغة = لا تغيير)")); form.add(new JLabel(""));
        form.add(new JLabel("الدور:")); form.add(f_role);
        int r = JOptionPane.showConfirmDialog(this, form, "تعديل موظف", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        try {
            Connection con = DBConnection.getConnection();
            String pass = new String(f_pass.getPassword()).trim();
            PreparedStatement pst;
            if (!pass.isEmpty()) {
                pst = con.prepareStatement("update users set full_name=?, role=?, password_hash=? where id=?");
                pst.setString(1, f_name.getText().trim()); pst.setString(2, f_role.getSelectedItem().toString());
                pst.setString(3, pass); pst.setInt(4, id);
            } else {
                pst = con.prepareStatement("update users set full_name=?, role=? where id=?");
                pst.setString(1, f_name.getText().trim()); pst.setString(2, f_role.getSelectedItem().toString()); pst.setInt(3, id);
            }
            pst.executeUpdate(); pst.close(); con.close();
            JOptionPane.showMessageDialog(this, "تم التعديل"); btn_refreshStaffActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    private void btn_toggleStaffActionPerformed() {
        int row = tbl_staff.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر موظف"); return; }
        int id = Integer.parseInt(model_staff.getValueAt(row, 0).toString());
        int new_val = model_staff.getValueAt(row, 4).toString().startsWith("✓") ? 0 : 1;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("update users set active=? where id=?");
            pst.setInt(1, new_val); pst.setInt(2, id); pst.executeUpdate(); pst.close(); con.close();
            btn_refreshStaffActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    JPanel buildTablesSubTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        model_tables = new DefaultTableModel(
                new Object[]{"#", "رقم الطاولة", "الحساب", "الحالة", "نشط"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_tables = new JTable(model_tables);
        tbl_tables.setRowHeight(25); tbl_tables.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JLabel info = new JLabel("  كلمة المرور الافتراضية لجميع الطاولات: 1234");
        info.setForeground(Color.BLUE);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton btn_r = new JButton("تحديث"); btn_r.addActionListener(e -> btn_refreshTablesActionPerformed()); actions.add(btn_r);
        JButton btn_e = new JButton("تعديل الطاولة"); btn_e.addActionListener(e -> btn_editTableActionPerformed()); actions.add(btn_e);
        JPanel south = new JPanel(new BorderLayout());
        south.add(info, BorderLayout.NORTH); south.add(actions, BorderLayout.SOUTH);
        panel.add(new JScrollPane(tbl_tables), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void btn_refreshTablesActionPerformed() {
        try {
            model_tables.setRowCount(0);
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("select id, table_number, account_name, status, active from dining_tables order by table_number");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model_tables.addRow(new Object[]{
                    rs.getInt("id"), rs.getInt("table_number"), rs.getString("account_name"),
                    rs.getString("status"), rs.getInt("active")==1?"✓ نشط":"✗ موقوف"
                });
            }
            rs.close(); pst.close(); con.close();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    private void btn_editTableActionPerformed() {
        int row = tbl_tables.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر طاولة"); return; }
        int table_num = Integer.parseInt(model_tables.getValueAt(row, 1).toString());
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        JPasswordField f_pass = new JPasswordField();
        JComboBox f_status = new JComboBox(new String[]{"OPEN","CLOSED"});
        f_status.setSelectedItem(model_tables.getValueAt(row, 3).toString());
        JCheckBox f_active = new JCheckBox("نشط", model_tables.getValueAt(row, 4).toString().startsWith("✓"));
        form.add(new JLabel("كلمة مرور جديدة:")); form.add(f_pass);
        form.add(new JLabel("(فارغة = لا تغيير)")); form.add(new JLabel(""));
        form.add(new JLabel("الحالة:")); form.add(f_status);
        form.add(new JLabel("")); form.add(f_active);
        int r = JOptionPane.showConfirmDialog(this, form, "تعديل طاولة " + table_num, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        try {
            Connection con = DBConnection.getConnection();
            String pass = new String(f_pass.getPassword()).trim();
            PreparedStatement pst;
            if (!pass.isEmpty()) {
                pst = con.prepareStatement("update dining_tables set password_hash=?, status=?, active=? where table_number=?");
                pst.setString(1, pass); pst.setString(2, f_status.getSelectedItem().toString());
                pst.setInt(3, f_active.isSelected()?1:0); pst.setInt(4, table_num);
            } else {
                pst = con.prepareStatement("update dining_tables set status=?, active=? where table_number=?");
                pst.setString(1, f_status.getSelectedItem().toString());
                pst.setInt(2, f_active.isSelected()?1:0); pst.setInt(3, table_num);
            }
            pst.executeUpdate(); pst.close(); con.close();
            JOptionPane.showMessageDialog(this, "تم التعديل"); btn_refreshTablesActionPerformed();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    // ==========================================================
    //  تاب الإحصائيات
    // ==========================================================
    JPanel buildStatisticsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // كروت الإحصائيات
        JPanel cards_panel = new JPanel(new GridLayout(2, 4, 10, 10));

        lbl_total_orders  = createStatCard("إجمالي الطلبات", "0",    new Color(70, 130, 180));
        lbl_completed     = createStatCard("مكتملة",          "0",    new Color(34, 139, 34));
        lbl_cancelled     = createStatCard("ملغية",           "0",    new Color(220, 80, 80));
        lbl_revenue       = createStatCard("الإيرادات (د.ل)", "0.00", new Color(184, 134, 11));
        lbl_ready_orders  = createStatCard("وجبات جاهزة",     "0",    new Color(70, 130, 180));
        lbl_custom_orders = createStatCard("وجبات مخصصة",     "0",    new Color(100, 60, 180));
        lbl_avg_order     = createStatCard("متوسط قيمة الطلب","0.00", new Color(60, 150, 150));

        JButton btn_refresh_stats = new JButton("تحديث الإحصائيات");
        btn_refresh_stats.addActionListener(e -> btn_refreshStatsActionPerformed());

        cards_panel.add(lbl_total_orders);
        cards_panel.add(lbl_completed);
        cards_panel.add(lbl_cancelled);
        cards_panel.add(lbl_revenue);
        cards_panel.add(lbl_ready_orders);
        cards_panel.add(lbl_custom_orders);
        cards_panel.add(lbl_avg_order);
        cards_panel.add(btn_refresh_stats);

        // جدول تفصيلي بالطلبات المكتملة
        model_stats = new DefaultTableModel(
                new Object[]{"الحالة", "العدد", "المجموع د.ل", "متوسط السعرات"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_stats_detail = new JTable(model_stats);
        tbl_stats_detail.setRowHeight(28);

        JPanel detail_panel = new JPanel(new BorderLayout());
        detail_panel.setBorder(BorderFactory.createTitledBorder("ملخص الطلبات حسب الحالة"));
        detail_panel.add(new JScrollPane(tbl_stats_detail), BorderLayout.CENTER);

        panel.add(cards_panel, BorderLayout.NORTH);
        panel.add(detail_panel, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createStatCard(String title, String value, Color color) {
        JLabel lbl = new JLabel("<html><center><b style='font-size:13px;color:#444'>" + title +
            "</b><br><span style='font-size:22px;color:#222'>" + value + "</span></center></html>", JLabel.CENTER);
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        lbl.setBackground(color.brighter().brighter().brighter());
        lbl.setOpaque(true);
        return lbl;
    }

    private void updateStatCard(JLabel card, String title, String value, Color color) {
        card.setText("<html><center><b style='font-size:13px;color:#444'>" + title +
            "</b><br><span style='font-size:22px;color:#222'>" + value + "</span></center></html>");
    }

    private void btn_refreshStatsActionPerformed() {
        try {
            Connection con = DBConnection.getConnection();

            // إجمالي
            PreparedStatement pst = con.prepareStatement(
                "select count(*) as total, " +
                "sum(case when status='COMPLETED' then 1 else 0 end) as completed, " +
                "sum(case when status='CANCELLED' then 1 else 0 end) as cancelled, " +
                "sum(case when status='COMPLETED' then subtotal else 0 end) as revenue, " +
                "sum(case when order_type='READY_MEAL' then 1 else 0 end) as ready_cnt, " +
                "sum(case when order_type='CUSTOM_MEAL' then 1 else 0 end) as custom_cnt, " +
                "avg(case when status='COMPLETED' then subtotal else null end) as avg_order " +
                "from orders");
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                updateStatCard(lbl_total_orders,  "إجمالي الطلبات",  String.valueOf(rs.getInt("total")),     new Color(70, 130, 180));
                updateStatCard(lbl_completed,     "مكتملة",           String.valueOf(rs.getInt("completed")), new Color(34, 139, 34));
                updateStatCard(lbl_cancelled,     "ملغية",            String.valueOf(rs.getInt("cancelled")), new Color(220, 80, 80));
                updateStatCard(lbl_revenue,       "الإيرادات (د.ل)", String.format("%.2f", rs.getDouble("revenue")),   new Color(184, 134, 11));
                updateStatCard(lbl_ready_orders,  "وجبات جاهزة",     String.valueOf(rs.getInt("ready_cnt")),  new Color(70, 130, 180));
                updateStatCard(lbl_custom_orders, "وجبات مخصصة",     String.valueOf(rs.getInt("custom_cnt")), new Color(100, 60, 180));
                double avg = rs.getDouble("avg_order");
                updateStatCard(lbl_avg_order, "متوسط قيمة الطلب", rs.wasNull() ? "0.00" : String.format("%.2f", avg), new Color(60, 150, 150));
            }
            rs.close(); pst.close();

            // تفصيل
            model_stats.setRowCount(0);
            PreparedStatement pst2 = con.prepareStatement(
                "select status, count(*) as cnt, sum(subtotal) as total_price, avg(calories) as avg_cal from orders group by status order by field(status,'NEW','PREPARING','READY','COMPLETED','CANCELLED')");
            ResultSet rs2 = pst2.executeQuery();
            while (rs2.next()) {
                String st = rs2.getString("status");
                String st_ar = st.equals("NEW")?"جديد":st.equals("PREPARING")?"قيد التحضير":st.equals("READY")?"جاهز":st.equals("COMPLETED")?"مكتمل":"ملغي";
                model_stats.addRow(new Object[]{
                    st_ar, rs2.getInt("cnt"),
                    String.format("%.2f", rs2.getDouble("total_price")),
                    String.format("%.0f", rs2.getDouble("avg_cal"))
                });
            }
            rs2.close(); pst2.close(); con.close();

        } catch (Exception e) { JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage()); }
    }

    private void btn_backActionPerformed() {
        new MainMenuFrame().setVisible(true);
        dispose();
    }
}
