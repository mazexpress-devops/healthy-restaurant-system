import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CustomerFrame extends JFrame {

    int table_number;     // رقم الطاولة الحالية (من تسجيل الدخول)
    String table_name;    // اسم الطاولة

    // ===== مكونات تاب الوجبات الجاهزة =====
    JTable tbl_meals;
    DefaultTableModel model_meals;
    int[] meal_ids;

    // سلة الوجبات الجاهزة
    JTable tbl_cart;
    DefaultTableModel model_cart;
    int[] cart_meal_ids = new int[30];
    int[] cart_meal_qty = new int[30];
    double[] cart_meal_price = new double[30];
    double[] cart_meal_calories = new double[30];
    double[] cart_meal_protein = new double[30];
    double[] cart_meal_carbs = new double[30];
    double[] cart_meal_fat = new double[30];
    int cart_count = 0;
    JLabel lbl_cart_total;

    // ===== مكونات تاب الوجبة المخصصة =====
    JTable tbl_available;
    DefaultTableModel model_available;
    int[] ingredient_ids;

    JTable tbl_selected;
    DefaultTableModel model_selected;

    // مصفوفات موازية للمكونات المختارة
    int[]     selected_ids      = new int[50];
    String[]  selected_names    = new String[50];
    double[]  selected_calories = new double[50];
    double[]  selected_protein  = new double[50];
    double[]  selected_carbs    = new double[50];
    double[]  selected_fat      = new double[50];
    double[]  selected_price    = new double[50];
    boolean[] sel_lactose       = new boolean[50];
    boolean[] sel_gluten        = new boolean[50];
    boolean[] sel_sugar         = new boolean[50];
    boolean[] sel_sodium        = new boolean[50];
    boolean[] sel_highfat       = new boolean[50];
    int selected_count = 0;

    // مجاميع الوجبة المخصصة
    double total_price = 0, total_calories = 0, total_protein = 0, total_carbs = 0, total_fat = 0;

    // مكونات معلومات الزبون
    JTextField txt_age, txt_weight, txt_height;
    JComboBox cmb_goal;
    JCheckBox chk_diabetes, chk_lactose, chk_gluten, chk_pressure, chk_liver;

    JLabel lbl_total, lbl_macros, lbl_daily_needs, lbl_remaining;
    JTextArea txt_feedback;

    public CustomerFrame(int tableNumber, String tableName) {
        this.table_number = tableNumber;
        this.table_name   = tableName;

        setTitle("نظام المطعم الصحي   |   " + tableName.toUpperCase() + "  (رقم الطاولة: " + tableNumber + ")");
        setSize(1150, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // شريط علوي يعرض رقم الطاولة
        JPanel top_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        top_panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        top_panel.setBackground(new Color(240, 248, 255));

        JButton btn_logout = new JButton("تسجيل الخروج");
        btn_logout.addActionListener(e -> btn_logoutActionPerformed());
        top_panel.add(btn_logout);

        JLabel lbl_table = new JLabel("  الطاولة: " + tableName + "   |   رقم: " + tableNumber);
        lbl_table.setFont(new Font("Arial", Font.BOLD, 14));
        lbl_table.setForeground(new Color(0, 100, 0));
        top_panel.add(lbl_table);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("قائمة الوجبات الجاهزة", buildReadyMealsPanel());
        tabs.addTab("إنشاء وجبة مخصصة", buildCustomMealPanel());

        add(top_panel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        // تحميل البيانات عند الفتح
        btn_refreshMealsActionPerformed();
        btn_refreshIngredientsActionPerformed();
    }

    // ===========================================================
    //  تاب الوجبات الجاهزة + سلة التسوق
    // ===========================================================
    JPanel buildReadyMealsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- جدول الوجبات ----
        model_meals = new DefaultTableModel(
                new Object[]{"#", "اسم الوجبة", "الوصف", "سعرات", "بروتين ج", "كربو ج", "دهون ج", "السعر د.ل"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_meals = new JTable(model_meals);
        tbl_meals.setRowHeight(26);
        tbl_meals.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_meals.getTableHeader().setReorderingAllowed(false);

        JPanel menu_panel = new JPanel(new BorderLayout(5, 5));
        menu_panel.setBorder(BorderFactory.createTitledBorder("قائمة الوجبات المتاحة"));

        JPanel menu_btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton btn_refresh = new JButton("تحديث القائمة");
        btn_refresh.addActionListener(e -> btn_refreshMealsActionPerformed());
        menu_btns.add(btn_refresh);

        JButton btn_add_cart = new JButton("أضف للسلة ←");
        btn_add_cart.addActionListener(e -> btn_addToCartActionPerformed());
        menu_btns.add(btn_add_cart);

        menu_panel.add(new JScrollPane(tbl_meals), BorderLayout.CENTER);
        menu_panel.add(menu_btns, BorderLayout.SOUTH);

        // ---- السلة ----
        model_cart = new DefaultTableModel(
                new Object[]{"الوجبة", "الكمية", "السعر د.ل", "سعرات"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_cart = new JTable(model_cart);
        tbl_cart.setRowHeight(26);
        tbl_cart.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel cart_panel = new JPanel(new BorderLayout(5, 5));
        cart_panel.setBorder(BorderFactory.createTitledBorder("سلة الطلبات"));

        lbl_cart_total = new JLabel("المجموع: 0.00 د.ل");
        lbl_cart_total.setFont(new Font("Arial", Font.BOLD, 13));

        JPanel cart_btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton btn_inc = new JButton("+ زيادة الكمية");
        btn_inc.addActionListener(e -> btn_increaseQtyActionPerformed());
        cart_btns.add(btn_inc);

        JButton btn_dec = new JButton("- تقليل الكمية");
        btn_dec.addActionListener(e -> btn_decreaseQtyActionPerformed());
        cart_btns.add(btn_dec);

        JButton btn_remove_cart = new JButton("حذف من السلة");
        btn_remove_cart.addActionListener(e -> btn_removeFromCartActionPerformed());
        cart_btns.add(btn_remove_cart);

        JButton btn_clear_cart = new JButton("تفريغ السلة");
        btn_clear_cart.addActionListener(e -> btn_clearCartActionPerformed());
        cart_btns.add(btn_clear_cart);

        JButton btn_place = new JButton("تأكيد الطلب وإرساله للمطبخ");
        btn_place.setForeground(Color.WHITE);
        btn_place.setBackground(new Color(34, 139, 34));
        btn_place.addActionListener(e -> btn_placeCartOrderActionPerformed());
        cart_btns.add(btn_place);

        JPanel cart_south = new JPanel(new BorderLayout());
        cart_south.add(lbl_cart_total, BorderLayout.NORTH);
        cart_south.add(cart_btns, BorderLayout.SOUTH);

        cart_panel.add(new JScrollPane(tbl_cart), BorderLayout.CENTER);
        cart_panel.add(cart_south, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menu_panel, cart_panel);
        split.setDividerLocation(620);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void btn_refreshMealsActionPerformed() {
        try {
            model_meals.setRowCount(0);
            Connection con = DBConnection.getConnection();
            String sql = "select id, name, description, calories, protein_g, carbs_g, fat_g, price from ready_meals where available=1 order by id";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
                model_meals.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    String.format("%.0f", rs.getDouble("calories")),
                    String.format("%.1f", rs.getDouble("protein_g")),
                    String.format("%.1f", rs.getDouble("carbs_g")),
                    String.format("%.1f", rs.getDouble("fat_g")),
                    rs.getString("price")
                });
            }
            meal_ids = new int[ids.size()];
            for (int i = 0; i < ids.size(); i++) meal_ids[i] = ids.get(i);
            rs.close(); pst.close(); con.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ في تحميل الوجبات:\n" + e.getMessage());
        }
    }

    private void btn_addToCartActionPerformed() {
        int row = tbl_meals.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر وجبة من القائمة أولا"); return; }
        if (cart_count >= 30) { JOptionPane.showMessageDialog(this, "السلة ممتلئة"); return; }

        int meal_id = meal_ids[row];

        // نتحقق إذا الوجبة موجودة في السلة — نزيد الكمية
        for (int i = 0; i < cart_count; i++) {
            if (cart_meal_ids[i] == meal_id) {
                cart_meal_qty[i]++;
                model_cart.setValueAt(cart_meal_qty[i], i, 1);
                model_cart.setValueAt(String.format("%.2f", cart_meal_price[i] * cart_meal_qty[i]), i, 2);
                updateCartTotal();
                return;
            }
        }

        // وجبة جديدة في السلة
        try {
            Connection con = DBConnection.getConnection();
            String sql = "select name, price, calories, protein_g, carbs_g, fat_g from ready_meals where id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, meal_id);
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) { rs.close(); pst.close(); con.close(); return; }

            cart_meal_ids[cart_count]     = meal_id;
            cart_meal_qty[cart_count]     = 1;
            cart_meal_price[cart_count]   = rs.getDouble("price");
            cart_meal_calories[cart_count]= rs.getDouble("calories");
            cart_meal_protein[cart_count] = rs.getDouble("protein_g");
            cart_meal_carbs[cart_count]   = rs.getDouble("carbs_g");
            cart_meal_fat[cart_count]     = rs.getDouble("fat_g");

            model_cart.addRow(new Object[]{
                rs.getString("name"),
                1,
                String.format("%.2f", cart_meal_price[cart_count]),
                String.format("%.0f", cart_meal_calories[cart_count])
            });
            cart_count++;
            rs.close(); pst.close(); con.close();
            updateCartTotal();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ: " + e.getMessage());
        }
    }

    private void btn_increaseQtyActionPerformed() {
        int row = tbl_cart.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر وجبة في السلة"); return; }
        cart_meal_qty[row]++;
        model_cart.setValueAt(cart_meal_qty[row], row, 1);
        model_cart.setValueAt(String.format("%.2f", cart_meal_price[row] * cart_meal_qty[row]), row, 2);
        updateCartTotal();
    }

    private void btn_decreaseQtyActionPerformed() {
        int row = tbl_cart.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر وجبة في السلة"); return; }
        if (cart_meal_qty[row] <= 1) {
            btn_removeFromCartActionPerformed();
            return;
        }
        cart_meal_qty[row]--;
        model_cart.setValueAt(cart_meal_qty[row], row, 1);
        model_cart.setValueAt(String.format("%.2f", cart_meal_price[row] * cart_meal_qty[row]), row, 2);
        updateCartTotal();
    }

    private void btn_removeFromCartActionPerformed() {
        int row = tbl_cart.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر وجبة للحذف"); return; }

        // إزاحة المصفوفات
        for (int i = row; i < cart_count - 1; i++) {
            cart_meal_ids[i]     = cart_meal_ids[i+1];
            cart_meal_qty[i]     = cart_meal_qty[i+1];
            cart_meal_price[i]   = cart_meal_price[i+1];
            cart_meal_calories[i]= cart_meal_calories[i+1];
            cart_meal_protein[i] = cart_meal_protein[i+1];
            cart_meal_carbs[i]   = cart_meal_carbs[i+1];
            cart_meal_fat[i]     = cart_meal_fat[i+1];
        }
        cart_count--;
        model_cart.removeRow(row);
        updateCartTotal();
    }

    private void btn_clearCartActionPerformed() {
        model_cart.setRowCount(0);
        cart_count = 0;
        updateCartTotal();
    }

    private void updateCartTotal() {
        double total = 0;
        for (int i = 0; i < cart_count; i++) total += cart_meal_price[i] * cart_meal_qty[i];
        lbl_cart_total.setText(String.format("المجموع: %.2f د.ل  |  عدد الأصناف: %d", total, cart_count));
    }

    private void btn_placeCartOrderActionPerformed() {
        if (cart_count == 0) { JOptionPane.showMessageDialog(this, "السلة فارغة، أضف وجبات أولا"); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            "تأكيد إرسال " + cart_count + " صنف للمطبخ؟",
            "تأكيد الطلب", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection con = DBConnection.getConnection();

            // نحسب المجاميع الكلية للطلب
            double sum_price = 0, sum_cal = 0, sum_prot = 0, sum_carbs = 0, sum_fat = 0;
            for (int i = 0; i < cart_count; i++) {
                sum_price += cart_meal_price[i] * cart_meal_qty[i];
                sum_cal   += cart_meal_calories[i] * cart_meal_qty[i];
                sum_prot  += cart_meal_protein[i] * cart_meal_qty[i];
                sum_carbs += cart_meal_carbs[i] * cart_meal_qty[i];
                sum_fat   += cart_meal_fat[i] * cart_meal_qty[i];
            }

            // نضيف الطلب الرئيسي
            String sql_order = "insert into orders (table_number, order_type, status, subtotal, calories, protein_g, carbs_g, fat_g) values (?, 'READY_MEAL', 'NEW', ?, ?, ?, ?, ?)";
            PreparedStatement pst_order = con.prepareStatement(sql_order, Statement.RETURN_GENERATED_KEYS);
            pst_order.setInt(1, table_number);
            pst_order.setDouble(2, sum_price);
            pst_order.setDouble(3, sum_cal);
            pst_order.setDouble(4, sum_prot);
            pst_order.setDouble(5, sum_carbs);
            pst_order.setDouble(6, sum_fat);
            pst_order.executeUpdate();

            ResultSet keys = pst_order.getGeneratedKeys();
            int order_id = 0;
            if (keys.next()) order_id = keys.getInt(1);
            keys.close(); pst_order.close();

            // نضيف كل صنف في order_items
            for (int i = 0; i < cart_count; i++) {
                String sql_item = "insert into order_items (order_id, ready_meal_id, name, quantity, unit_price, calories, protein_g, carbs_g, fat_g) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pst_item = con.prepareStatement(sql_item);
                pst_item.setInt(1, order_id);
                pst_item.setInt(2, cart_meal_ids[i]);
                pst_item.setString(3, model_cart.getValueAt(i, 0).toString());
                pst_item.setInt(4, cart_meal_qty[i]);
                pst_item.setDouble(5, cart_meal_price[i]);
                pst_item.setDouble(6, cart_meal_calories[i]);
                pst_item.setDouble(7, cart_meal_protein[i]);
                pst_item.setDouble(8, cart_meal_carbs[i]);
                pst_item.setDouble(9, cart_meal_fat[i]);
                pst_item.executeUpdate();
                pst_item.close();
            }
            con.close();

            JOptionPane.showMessageDialog(this, "✓ تم إرسال الطلب رقم #" + order_id + " للمطبخ\nعدد الأصناف: " + cart_count);
            btn_clearCartActionPerformed();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ في إرسال الطلب:\n" + e.getMessage());
        }
    }

    // ===========================================================
    //  تاب الوجبة المخصصة
    // ===========================================================
    JPanel buildCustomMealPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // ---- معلومات الزبون ----
        JPanel profile_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        profile_panel.setBorder(BorderFactory.createTitledBorder("البيانات الشخصية"));

        profile_panel.add(new JLabel("العمر:"));
        txt_age = new JTextField("25", 4);
        profile_panel.add(txt_age);

        profile_panel.add(new JLabel("الوزن كغ:"));
        txt_weight = new JTextField("75", 4);
        profile_panel.add(txt_weight);

        profile_panel.add(new JLabel("الطول سم:"));
        txt_height = new JTextField("170", 4);
        profile_panel.add(txt_height);

        profile_panel.add(new JLabel("الهدف الرياضي:"));
        cmb_goal = new JComboBox(new String[]{"المحافظة على الوزن", "تضخيم خفيف (Clean Bulk)", "تضخيم (Bulk)", "تنشيف (Cut)"});
        profile_panel.add(cmb_goal);

        JButton btn_calc = new JButton("احسب الاحتياج اليومي");
        btn_calc.addActionListener(e -> btn_calcNeedsActionPerformed());
        profile_panel.add(btn_calc);

        // ---- الحالة الصحية ----
        JPanel health_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        health_panel.setBorder(BorderFactory.createTitledBorder("الحالة الصحية"));

        chk_diabetes = new JCheckBox("مرض السكري");
        chk_lactose  = new JCheckBox("حساسية اللاكتوز");
        chk_gluten   = new JCheckBox("حساسية الغلوتين");
        chk_pressure = new JCheckBox("ارتفاع ضغط الدم");
        chk_liver    = new JCheckBox("دهون الكبد");

        // عند تغيير الحالة الصحية: إعادة تصفية المكونات وحذف المتعارضة
        chk_diabetes.addActionListener(e -> checkAndRemoveConflictingIngredients());
        chk_lactose.addActionListener(e -> checkAndRemoveConflictingIngredients());
        chk_gluten.addActionListener(e -> checkAndRemoveConflictingIngredients());
        chk_pressure.addActionListener(e -> checkAndRemoveConflictingIngredients());
        chk_liver.addActionListener(e -> checkAndRemoveConflictingIngredients());

        health_panel.add(chk_diabetes);
        health_panel.add(chk_lactose);
        health_panel.add(chk_gluten);
        health_panel.add(chk_pressure);
        health_panel.add(chk_liver);

        // ---- عرض الاحتياج اليومي ----
        lbl_daily_needs = new JLabel("احتياجك اليومي: اضغط 'احسب الاحتياج اليومي' أولا");
        lbl_daily_needs.setFont(new Font("Arial", Font.BOLD, 12));
        lbl_daily_needs.setForeground(new Color(0, 80, 160));
        lbl_daily_needs.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        lbl_remaining = new JLabel("المتبقي: -");
        lbl_remaining.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl_remaining.setForeground(new Color(80, 80, 80));
        lbl_remaining.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        JPanel needs_panel = new JPanel(new GridLayout(2, 1));
        needs_panel.setBorder(BorderFactory.createTitledBorder("الاحتياج اليومي والمتبقي"));
        needs_panel.add(lbl_daily_needs);
        needs_panel.add(lbl_remaining);

        JPanel top_section = new JPanel(new BorderLayout(0, 2));
        top_section.add(profile_panel, BorderLayout.NORTH);
        top_section.add(health_panel, BorderLayout.CENTER);
        top_section.add(needs_panel, BorderLayout.SOUTH);

        // ---- المكونات المتاحة (يسار) ----
        model_available = new DefaultTableModel(
                new Object[]{"#", "المكون", "الفئة", "الحصة", "سعرات", "بروتين ج", "كربو ج", "دهون ج", "السعر"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_available = new JTable(model_available);
        tbl_available.setRowHeight(25);
        tbl_available.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_available.getTableHeader().setReorderingAllowed(false);

        JPanel left_panel = new JPanel(new BorderLayout(5, 5));
        left_panel.setBorder(BorderFactory.createTitledBorder("المكونات المتاحة (مصفاة حسب حالتك الصحية)"));

        JPanel left_btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btn_ref_ing = new JButton("تحديث المكونات");
        btn_ref_ing.addActionListener(e -> btn_refreshIngredientsActionPerformed());
        left_btns.add(btn_ref_ing);

        JButton btn_add_ing = new JButton("أضف للوجبة  ←");
        btn_add_ing.addActionListener(e -> btn_addIngredientActionPerformed());
        left_btns.add(btn_add_ing);

        left_panel.add(new JScrollPane(tbl_available), BorderLayout.CENTER);
        left_panel.add(left_btns, BorderLayout.SOUTH);

        // ---- وجبتي الحالية (يمين) ----
        model_selected = new DefaultTableModel(
                new Object[]{"#", "المكون", "الفئة", "سعرات", "بروتين ج", "كربو ج", "دهون ج", "السعر"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_selected = new JTable(model_selected);
        tbl_selected.setRowHeight(25);
        tbl_selected.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_selected.getTableHeader().setReorderingAllowed(false);

        JPanel right_panel = new JPanel(new BorderLayout(5, 5));
        right_panel.setBorder(BorderFactory.createTitledBorder("وجبتي الحالية"));

        lbl_total  = new JLabel("السعر الإجمالي: 0.00 د.ل");
        lbl_total.setFont(new Font("Arial", Font.BOLD, 13));

        lbl_macros = new JLabel("سعرات: 0  |  بروتين: 0ج  |  كربو: 0ج  |  دهون: 0ج");
        lbl_macros.setFont(new Font("Arial", Font.PLAIN, 12));

        txt_feedback = new JTextArea(5, 22);
        txt_feedback.setEditable(false);
        txt_feedback.setLineWrap(true);
        txt_feedback.setWrapStyleWord(true);
        txt_feedback.setText("اختر مكونات لبناء وجبتك...");
        txt_feedback.setBackground(new Color(255, 255, 240));
        txt_feedback.setBorder(BorderFactory.createTitledBorder("Smart Feedback"));

        JPanel right_btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton btn_remove = new JButton("احذف المحدد");
        btn_remove.addActionListener(e -> btn_removeIngredientActionPerformed());
        right_btns.add(btn_remove);

        JButton btn_clear = new JButton("مسح الكل");
        btn_clear.addActionListener(e -> btn_clearMealActionPerformed());
        right_btns.add(btn_clear);

        JButton btn_submit = new JButton("تأكيد وإرسال للمطبخ");
        btn_submit.setForeground(Color.WHITE);
        btn_submit.setBackground(new Color(34, 139, 34));
        btn_submit.addActionListener(e -> btn_submitCustomMealActionPerformed());
        right_btns.add(btn_submit);

        JPanel summary_panel = new JPanel(new BorderLayout(3, 3));
        JPanel labels = new JPanel(new GridLayout(2, 1));
        labels.add(lbl_total);
        labels.add(lbl_macros);
        summary_panel.add(labels, BorderLayout.NORTH);
        summary_panel.add(new JScrollPane(txt_feedback), BorderLayout.CENTER);
        summary_panel.add(right_btns, BorderLayout.SOUTH);

        right_panel.add(new JScrollPane(tbl_selected), BorderLayout.CENTER);
        right_panel.add(summary_panel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left_panel, right_panel);
        split.setDividerLocation(600);

        panel.add(top_section, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // حساب الاحتياج اليومي (معادلة Mifflin-St Jeor)
    private double daily_cal_need = 0, daily_prot_need = 0, daily_carbs_need = 0, daily_fat_need = 0;

    private void btn_calcNeedsActionPerformed() {
        try {
            int age       = Integer.parseInt(txt_age.getText().trim());
            double weight = Double.parseDouble(txt_weight.getText().trim());
            double height = Double.parseDouble(txt_height.getText().trim());

            // BMR (Mifflin-St Jeor - للجنسين معاً بدون تحديد)
            double bmr = 10 * weight + 6.25 * height - 5 * age + 5;

            // معامل النشاط حسب الهدف
            double mult = cmb_goal.getSelectedIndex() == 0 ? 1.45 :
                          cmb_goal.getSelectedIndex() == 1 ? 1.65 :
                          cmb_goal.getSelectedIndex() == 2 ? 1.85 : 1.25;

            daily_cal_need   = bmr * mult;
            daily_prot_need  = weight * (cmb_goal.getSelectedIndex() >= 1 ? 2.0 : 1.6);
            daily_fat_need   = daily_cal_need * 0.25 / 9;
            daily_carbs_need = (daily_cal_need - daily_prot_need * 4 - daily_fat_need * 9) / 4;

            lbl_daily_needs.setText(String.format(
                "احتياجك اليومي ≈  %.0f سعرة  |  بروتين: %.0fج  |  كربو: %.0fج  |  دهون: %.0fج",
                daily_cal_need, daily_prot_need, daily_carbs_need, daily_fat_need));

            updateSummary();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "تأكد من إدخال أرقام صحيحة للعمر والوزن والطول");
        }
    }

    private void btn_refreshIngredientsActionPerformed() {
        try {
            model_available.setRowCount(0);
            Connection con = DBConnection.getConnection();
            String sql = "select id, name, category, serving_label, calories, protein_g, carbs_g, fat_g, price, contains_lactose, contains_gluten, high_sugar, high_sodium, high_fat from ingredients where available=1 order by category, name";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
            while (rs.next()) {
                // تصفية حسب الحالة الصحية
                boolean skip = false;
                if (chk_lactose.isSelected()  && rs.getInt("contains_lactose") == 1) skip = true;
                if (chk_gluten.isSelected()   && rs.getInt("contains_gluten")  == 1) skip = true;
                if (chk_diabetes.isSelected() && rs.getInt("high_sugar")       == 1) skip = true;
                if (chk_pressure.isSelected() && rs.getInt("high_sodium")      == 1) skip = true;
                if (chk_liver.isSelected()    && rs.getInt("high_fat")         == 1) skip = true;
                if (skip) continue;

                ids.add(rs.getInt("id"));
                String cat = rs.getString("category");
                String cat_ar = cat.equals("PROTEIN") ? "بروتين" : cat.equals("CARB") ? "كربو" :
                                cat.equals("FAT") ? "دهون" : cat.equals("ADDON") ? "إضافات" : "صلصة";

                model_available.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("name"), cat_ar, rs.getString("serving_label"),
                    String.format("%.0f", rs.getDouble("calories")),
                    String.format("%.1f", rs.getDouble("protein_g")),
                    String.format("%.1f", rs.getDouble("carbs_g")),
                    String.format("%.1f", rs.getDouble("fat_g")),
                    rs.getString("price")
                });
            }
            ingredient_ids = new int[ids.size()];
            for (int i = 0; i < ids.size(); i++) ingredient_ids[i] = ids.get(i);
            rs.close(); pst.close(); con.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ في تحميل المكونات:\n" + e.getMessage());
        }
    }

    // فحص وإزالة المكونات المتعارضة مع الحالة الصحية
    private void checkAndRemoveConflictingIngredients() {
        for (int i = selected_count - 1; i >= 0; i--) {
            String reason = null;
            if (chk_lactose.isSelected()  && sel_lactose[i])  reason = "يحتوي على لاكتوز (حساسية اللاكتوز)";
            if (chk_gluten.isSelected()   && sel_gluten[i])   reason = "يحتوي على غلوتين (حساسية الغلوتين)";
            if (chk_diabetes.isSelected() && sel_sugar[i])    reason = "نسبة سكر مرتفعة (مرض السكري)";
            if (chk_pressure.isSelected() && sel_sodium[i])   reason = "نسبة صوديوم مرتفعة (ضغط الدم)";
            if (chk_liver.isSelected()    && sel_highfat[i])  reason = "نسبة دهون مرتفعة (دهون الكبد)";

            if (reason != null) {
                // تنبيه صحي
                JOptionPane.showMessageDialog(this,
                    "⚠ تنبيه صحي!\n\nتم إزالة  \"" + selected_names[i] + "\"  من وجبتك\nالسبب: " + reason,
                    "تنبيه صحي", JOptionPane.WARNING_MESSAGE);

                // طرح القيم من المجاميع
                total_price    -= selected_price[i];
                total_calories -= selected_calories[i];
                total_protein  -= selected_protein[i];
                total_carbs    -= selected_carbs[i];
                total_fat      -= selected_fat[i];

                // إزاحة المصفوفات
                for (int j = i; j < selected_count - 1; j++) {
                    selected_ids[j]      = selected_ids[j+1];
                    selected_names[j]    = selected_names[j+1];
                    selected_calories[j] = selected_calories[j+1];
                    selected_protein[j]  = selected_protein[j+1];
                    selected_carbs[j]    = selected_carbs[j+1];
                    selected_fat[j]      = selected_fat[j+1];
                    selected_price[j]    = selected_price[j+1];
                    sel_lactose[j]       = sel_lactose[j+1];
                    sel_gluten[j]        = sel_gluten[j+1];
                    sel_sugar[j]         = sel_sugar[j+1];
                    sel_sodium[j]        = sel_sodium[j+1];
                    sel_highfat[j]       = sel_highfat[j+1];
                }
                selected_count--;
                model_selected.removeRow(i);
            }
        }
        updateSummary();
        btn_refreshIngredientsActionPerformed();
    }

    private void btn_addIngredientActionPerformed() {
        int row = tbl_available.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر مكون من القائمة"); return; }
        if (selected_count >= 50) { JOptionPane.showMessageDialog(this, "وصلت للحد الأقصى"); return; }

        int ing_id = ingredient_ids[row];

        // التحقق من التكرار
        for (int i = 0; i < selected_count; i++) {
            if (selected_ids[i] == ing_id) { JOptionPane.showMessageDialog(this, "هذا المكون مضاف مسبقاً"); return; }
        }

        // جلب بيانات المكون من قاعدة البيانات
        try {
            Connection con = DBConnection.getConnection();
            String sql = "select name, category, price, calories, protein_g, carbs_g, fat_g, contains_lactose, contains_gluten, high_sugar, high_sodium, high_fat from ingredients where id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, ing_id);
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) { rs.close(); pst.close(); con.close(); return; }

            selected_ids[selected_count]      = ing_id;
            selected_names[selected_count]    = rs.getString("name");
            selected_calories[selected_count] = rs.getDouble("calories");
            selected_protein[selected_count]  = rs.getDouble("protein_g");
            selected_carbs[selected_count]    = rs.getDouble("carbs_g");
            selected_fat[selected_count]      = rs.getDouble("fat_g");
            selected_price[selected_count]    = rs.getDouble("price");
            sel_lactose[selected_count]       = rs.getInt("contains_lactose") == 1;
            sel_gluten[selected_count]        = rs.getInt("contains_gluten")  == 1;
            sel_sugar[selected_count]         = rs.getInt("high_sugar")       == 1;
            sel_sodium[selected_count]        = rs.getInt("high_sodium")      == 1;
            sel_highfat[selected_count]       = rs.getInt("high_fat")         == 1;

            String cat = rs.getString("category");
            String cat_ar = cat.equals("PROTEIN") ? "بروتين" : cat.equals("CARB") ? "كربو" :
                            cat.equals("FAT") ? "دهون" : cat.equals("ADDON") ? "إضافات" : "صلصة";

            model_selected.addRow(new Object[]{
                ing_id,
                selected_names[selected_count],
                cat_ar,
                String.format("%.0f", selected_calories[selected_count]),
                String.format("%.1f", selected_protein[selected_count]),
                String.format("%.1f", selected_carbs[selected_count]),
                String.format("%.1f", selected_fat[selected_count]),
                String.format("%.2f", selected_price[selected_count])
            });

            total_price    += selected_price[selected_count];
            total_calories += selected_calories[selected_count];
            total_protein  += selected_protein[selected_count];
            total_carbs    += selected_carbs[selected_count];
            total_fat      += selected_fat[selected_count];
            selected_count++;

            rs.close(); pst.close(); con.close();
            updateSummary();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ: " + e.getMessage());
        }
    }

    private void btn_removeIngredientActionPerformed() {
        int row = tbl_selected.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "اختر مكون للحذف"); return; }

        total_price    -= selected_price[row];
        total_calories -= selected_calories[row];
        total_protein  -= selected_protein[row];
        total_carbs    -= selected_carbs[row];
        total_fat      -= selected_fat[row];

        for (int i = row; i < selected_count - 1; i++) {
            selected_ids[i]      = selected_ids[i+1];
            selected_names[i]    = selected_names[i+1];
            selected_calories[i] = selected_calories[i+1];
            selected_protein[i]  = selected_protein[i+1];
            selected_carbs[i]    = selected_carbs[i+1];
            selected_fat[i]      = selected_fat[i+1];
            selected_price[i]    = selected_price[i+1];
            sel_lactose[i]       = sel_lactose[i+1];
            sel_gluten[i]        = sel_gluten[i+1];
            sel_sugar[i]         = sel_sugar[i+1];
            sel_sodium[i]        = sel_sodium[i+1];
            sel_highfat[i]       = sel_highfat[i+1];
        }
        selected_count--;
        model_selected.removeRow(row);
        updateSummary();
    }

    private void btn_clearMealActionPerformed() {
        model_selected.setRowCount(0);
        selected_count = 0;
        total_price = total_calories = total_protein = total_carbs = total_fat = 0;
        updateSummary();
    }

    private void updateSummary() {
        lbl_total.setText(String.format("السعر الإجمالي: %.2f د.ل", total_price));
        lbl_macros.setText(String.format(
            "سعرات: %.0f  |  بروتين: %.1fج  |  كربو: %.1fج  |  دهون: %.1fج",
            total_calories, total_protein, total_carbs, total_fat));

        // تحديث المتبقي من الاحتياج اليومي
        if (daily_cal_need > 0) {
            lbl_remaining.setText(String.format(
                "المتبقي من يومك ≈  %.0f سعرة  |  بروتين: %.0fج  |  كربو: %.0fج  |  دهون: %.0fج",
                daily_cal_need - total_calories,
                daily_prot_need - total_protein,
                daily_carbs_need - total_carbs,
                daily_fat_need - total_fat));
        }

        // Smart Feedback
        StringBuilder fb = new StringBuilder();
        if (selected_count == 0) {
            fb.append("اختر مكونات لبناء وجبتك...\n");
            fb.append("يُنصح باختيار: بروتين + كربوهيدرات + إضافات");
        } else {
            // فحص التوازن الغذائي
            if (daily_cal_need > 0) {
                double pct = total_calories / daily_cal_need * 100;
                if (pct > 80) fb.append("⚠ الوجبة تستهلك أكثر من 80% من احتياجك اليومي\n");
                else if (pct > 50) fb.append("✓ الوجبة دسمة وتغطي جزءاً كبيراً من احتياجك\n");
                else fb.append("✓ الوجبة في حدود المعقول من احتياجك اليومي\n");
            }

            if (total_protein < 20) fb.append("⚠ البروتين منخفض، فكر في إضافة مصدر بروتين\n");
            else if (total_protein > 60) fb.append("✓ محتوى بروتين ممتاز\n");

            if (total_carbs > 150) fb.append("⚠ كربوهيدرات مرتفعة\n");
            if (total_fat > 40)   fb.append("⚠ دهون مرتفعة في الوجبة\n");

            // فحص الفئات
            boolean has_protein = false, has_carb = false;
            for (int i = 0; i < model_selected.getRowCount(); i++) {
                String cat = model_selected.getValueAt(i, 2).toString();
                if (cat.equals("بروتين"))  has_protein = true;
                if (cat.equals("كربو"))    has_carb = true;
            }
            if (!has_protein) fb.append("💡 لم تختر مصدر بروتين بعد\n");
            if (!has_carb)    fb.append("💡 لم تختر مصدر كربوهيدرات بعد\n");

            int goal = cmb_goal.getSelectedIndex();
            if (goal >= 1 && goal <= 2 && total_protein < 30) fb.append("💪 لبناء العضلات: زد البروتين في وجبتك\n");
            if (goal == 3 && total_carbs > 80) fb.append("🔥 للتنشيف: قلل الكربوهيدرات في وجبتك\n");
        }
        txt_feedback.setText(fb.toString());
    }

    private void btn_submitCustomMealActionPerformed() {
        if (selected_count == 0) { JOptionPane.showMessageDialog(this, "اختر مكونات أولا"); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("إرسال وجبة مخصصة للمطبخ؟\n%d مكون | %.0f سعرة | %.2f د.ل",
                selected_count, total_calories, total_price),
            "تأكيد الطلب", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // بناء ملاحظات صحية
        StringBuilder notes = new StringBuilder("ملاحظات صحية: ");
        if (chk_diabetes.isSelected())  notes.append("[سكري] ");
        if (chk_lactose.isSelected())   notes.append("[حساسية لاكتوز] ");
        if (chk_gluten.isSelected())    notes.append("[حساسية غلوتين] ");
        if (chk_pressure.isSelected())  notes.append("[ضغط الدم] ");
        if (chk_liver.isSelected())     notes.append("[دهون الكبد] ");
        if (daily_cal_need > 0)
            notes.append(String.format(" | هدف: %s | احتياج يومي: %.0f سعرة",
                cmb_goal.getSelectedItem(), daily_cal_need));

        try {
            Connection con = DBConnection.getConnection();

            String sql_order = "insert into orders (table_number, order_type, status, subtotal, calories, protein_g, carbs_g, fat_g, health_notes) values (?, 'CUSTOM_MEAL', 'NEW', ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst_order = con.prepareStatement(sql_order, Statement.RETURN_GENERATED_KEYS);
            pst_order.setInt(1, table_number);
            pst_order.setDouble(2, total_price);
            pst_order.setDouble(3, total_calories);
            pst_order.setDouble(4, total_protein);
            pst_order.setDouble(5, total_carbs);
            pst_order.setDouble(6, total_fat);
            pst_order.setString(7, notes.toString());
            pst_order.executeUpdate();

            ResultSet keys = pst_order.getGeneratedKeys();
            int order_id = 0;
            if (keys.next()) order_id = keys.getInt(1);
            keys.close(); pst_order.close();

            String sql_item = "insert into order_items (order_id, name, quantity, unit_price, calories, protein_g, carbs_g, fat_g) values (?, 'وجبة مخصصة', 1, ?, ?, ?, ?, ?)";
            PreparedStatement pst_item = con.prepareStatement(sql_item, Statement.RETURN_GENERATED_KEYS);
            pst_item.setInt(1, order_id);
            pst_item.setDouble(2, total_price);
            pst_item.setDouble(3, total_calories);
            pst_item.setDouble(4, total_protein);
            pst_item.setDouble(5, total_carbs);
            pst_item.setDouble(6, total_fat);
            pst_item.executeUpdate();

            ResultSet item_keys = pst_item.getGeneratedKeys();
            int item_id = 0;
            if (item_keys.next()) item_id = item_keys.getInt(1);
            item_keys.close(); pst_item.close();

            // إضافة المكونات
            for (int i = 0; i < selected_count; i++) {
                String sql_ing = "select category from ingredients where id=?";
                PreparedStatement pst_cat = con.prepareStatement(sql_ing);
                pst_cat.setInt(1, selected_ids[i]);
                ResultSet rs_cat = pst_cat.executeQuery();
                String category = rs_cat.next() ? rs_cat.getString("category") : "ADDON";
                rs_cat.close(); pst_cat.close();

                String sql_oii = "insert into order_item_ingredients (order_item_id, ingredient_id, ingredient_name, category, unit_price, calories, protein_g, carbs_g, fat_g) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pst_oii = con.prepareStatement(sql_oii);
                pst_oii.setInt(1, item_id);
                pst_oii.setInt(2, selected_ids[i]);
                pst_oii.setString(3, selected_names[i]);
                pst_oii.setString(4, category);
                pst_oii.setDouble(5, selected_price[i]);
                pst_oii.setDouble(6, selected_calories[i]);
                pst_oii.setDouble(7, selected_protein[i]);
                pst_oii.setDouble(8, selected_carbs[i]);
                pst_oii.setDouble(9, selected_fat[i]);
                pst_oii.executeUpdate();
                pst_oii.close();
            }
            con.close();

            JOptionPane.showMessageDialog(this,
                "✓ تم إرسال وجبتك المخصصة للمطبخ!\nرقم الطلب: #" + order_id);
            btn_clearMealActionPerformed();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ:\n" + e.getMessage());
        }
    }

    private void btn_logoutActionPerformed() {
        new TableLoginFrame();
        dispose();
    }
}
