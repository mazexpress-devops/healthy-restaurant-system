package com.healthyrestaurant.ui;

import com.healthyrestaurant.model.CustomerProfile;
import com.healthyrestaurant.model.DiningTableAccount;
import com.healthyrestaurant.model.Goal;
import com.healthyrestaurant.model.HealthCondition;
import com.healthyrestaurant.model.Ingredient;
import com.healthyrestaurant.model.IngredientCategory;
import com.healthyrestaurant.model.MealSummary;
import com.healthyrestaurant.model.OrderStatus;
import com.healthyrestaurant.model.OrderTicket;
import com.healthyrestaurant.model.ReadyMeal;
import com.healthyrestaurant.model.StaffAccount;
import com.healthyrestaurant.model.User;
import com.healthyrestaurant.mysql.MySqlRepository;
import com.healthyrestaurant.service.NutritionService;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {
    private static final String SCREEN_HOME = "home";
    private static final String SCREEN_TABLE_LOGIN = "table_login";
    private static final String SCREEN_CUSTOMER = "customer";
    private static final String SCREEN_CHEF = "chef";
    private static final String SCREEN_ADMIN = "admin";
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final MySqlRepository repository = new MySqlRepository();
    private final NutritionService nutritionService = new NutritionService();

    private final Map<HealthCondition, JCheckBox> conditionBoxes =
            new EnumMap<HealthCondition, JCheckBox>(HealthCondition.class);

    private JLabel statusLabel;
    private CardLayout screenLayout;
    private JPanel screenPanel;
    private JButton homeButton;
    private DiningTableAccount fixedTableAccount;

    private JTextField tableUsernameField;
    private JPasswordField tablePasswordField;
    private JLabel tableLoginStatusLabel;

    private ReadOnlyTableModel readyMealModel;
    private JTable readyMealTable;
    private JLabel readyTableAccountLabel;
    private JLabel readyTableStatusLabel;
    private List<DiningTableAccount> tableAccounts = new ArrayList<DiningTableAccount>();
    private List<ReadyMeal> readyMeals = new ArrayList<ReadyMeal>();

    private ReadOnlyTableModel ingredientModel;
    private JTable ingredientTable;
    private ReadOnlyTableModel selectedIngredientModel;
    private JTable selectedIngredientTable;
    private JLabel customTableAccountLabel;
    private JLabel customTableStatusLabel;
    private JSpinner customAgeSpinner;
    private JSpinner customWeightSpinner;
    private JSpinner customHeightSpinner;
    private JComboBox<Goal> customGoalCombo;
    private JLabel targetCaloriesLabel;
    private JLabel summaryPriceLabel;
    private JLabel summaryNutritionLabel;
    private JTextArea feedbackArea;
    private List<Ingredient> availableIngredients = new ArrayList<Ingredient>();
    private List<Ingredient> selectableIngredients = new ArrayList<Ingredient>();
    private List<Ingredient> selectedIngredients = new ArrayList<Ingredient>();

    private JTextField chefUsernameField;
    private JPasswordField chefPasswordField;
    private JLabel chefLoginStatusLabel;
    private JPanel chefLoginPanel;
    private JPanel chefWorkspacePanel;
    private JButton chefRefreshButton;
    private JButton chefPreparingButton;
    private JButton chefReadyButton;
    private JButton chefCompletedButton;
    private JButton chefCancelledButton;
    private ReadOnlyTableModel chefOrderModel;
    private JTable chefOrderTable;
    private JTextArea chefDetailsArea;
    private boolean chefLoggedIn;
    private List<OrderTicket> kitchenOrders = new ArrayList<OrderTicket>();

    private JTextField adminUsernameField;
    private JPasswordField adminPasswordField;
    private JLabel adminLoginStatusLabel;
    private JPanel adminLoginPanel;
    private JPanel adminWorkspacePanel;
    private JButton adminRefreshIngredientsButton;
    private JButton adminAddIngredientButton;
    private JButton adminEditIngredientButton;
    private JButton adminDeleteIngredientButton;
    private JButton adminToggleIngredientButton;
    private JButton adminRefreshOrdersButton;
    private JButton adminEditOrderButton;
    private JButton adminDeleteOrderButton;
    private JButton adminRefreshAccountsButton;
    private JButton adminAddStaffButton;
    private JButton adminEditStaffButton;
    private JButton adminToggleStaffButton;
    private JButton adminEditTableButton;
    private ReadOnlyTableModel adminStaffModel;
    private JTable adminStaffTable;
    private ReadOnlyTableModel adminTableAccountModel;
    private JTable adminTableAccountTable;
    private ReadOnlyTableModel adminIngredientModel;
    private JTable adminIngredientTable;
    private ReadOnlyTableModel recentOrderModel;
    private JTable recentOrderTable;
    private JTextArea adminDetailsArea;
    private boolean adminLoggedIn;
    private List<Ingredient> adminIngredients = new ArrayList<Ingredient>();
    private List<OrderTicket> recentOrders = new ArrayList<OrderTicket>();
    private List<StaffAccount> staffAccounts = new ArrayList<StaffAccount>();
    private List<DiningTableAccount> adminTableAccounts = new ArrayList<DiningTableAccount>();

    public MainFrame() {
        super("Healthy Restaurant Orders - Java Swing + MySQL");
        initUi();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1180, 760);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        loadStartupData();
    }

    private void initUi() {
        setLayout(new BorderLayout(8, 8));
        add(buildHeader(), BorderLayout.NORTH);

        screenLayout = new CardLayout();
        screenPanel = new JPanel(screenLayout);
        screenPanel.add(buildHomePanel(), SCREEN_HOME);
        screenPanel.add(buildTableLoginPanel(), SCREEN_TABLE_LOGIN);
        screenPanel.add(buildCustomerPanel(), SCREEN_CUSTOMER);
        screenPanel.add(buildChefPanel(), SCREEN_CHEF);
        screenPanel.add(buildAdminPanel(), SCREEN_ADMIN);
        add(screenPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 10));
        add(statusLabel, BorderLayout.SOUTH);
        showScreen(SCREEN_HOME);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));

        JLabel title = new JLabel("Healthy Restaurant Orders");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        panel.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 0));

        homeButton = new JButton("Home");
        homeButton.addActionListener(event -> showScreen(SCREEN_HOME));
        actions.add(homeButton);

        panel.add(actions, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildHomePanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Choose Permission"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JButton customerButton = new JButton("1 - Customer");
        customerButton.setPreferredSize(new Dimension(260, 42));
        customerButton.addActionListener(event -> {
            tableLoginStatusLabel.setText("Use table account, for example table2 / 2");
            showScreen(SCREEN_TABLE_LOGIN);
        });
        addHomeButton(panel, gbc, 0, customerButton);

        JButton chefButton = new JButton("2 - Chef");
        chefButton.setPreferredSize(new Dimension(260, 42));
        chefButton.addActionListener(event -> showScreen(SCREEN_CHEF));
        addHomeButton(panel, gbc, 1, chefButton);

        JButton adminButton = new JButton("3 - Admin");
        adminButton.setPreferredSize(new Dimension(260, 42));
        adminButton.addActionListener(event -> showScreen(SCREEN_ADMIN));
        addHomeButton(panel, gbc, 2, adminButton);

        wrapper.add(panel);
        return wrapper;
    }

    private JPanel buildTableLoginPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("1 - Customer"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tableUsernameField = new JTextField("table1", 18);
        tablePasswordField = new JPasswordField("1", 18);
        tableLoginStatusLabel = new JLabel("Use table account, for example table2 / 2");

        JButton loginButton = new JButton("Open Table");
        loginButton.addActionListener(event -> loginCustomerTable());

        addFormRow(panel, gbc, 0, 0, "Account", tableUsernameField);
        addFormRow(panel, gbc, 1, 0, "Password", tablePasswordField);
        addFormRow(panel, gbc, 2, 0, "Status", tableLoginStatusLabel);
        addFormRow(panel, gbc, 3, 0, "", loginButton);

        wrapper.add(panel);
        return wrapper;
    }

    private void addHomeButton(JPanel panel, GridBagConstraints gbc, int row, JButton button) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(button, gbc);
    }

    private void showScreen(String screen) {
        if (screenLayout != null && screenPanel != null) {
            screenLayout.show(screenPanel, screen);
        }
        if (homeButton != null) {
            homeButton.setEnabled(!SCREEN_HOME.equals(screen));
        }
    }

    private JPanel buildCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ready Meals", buildReadyMealPanel());
        tabs.addTab("Custom Meal", buildCustomMealPanel());
        panel.add(tabs, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildReadyMealPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        readyMealModel = new ReadOnlyTableModel(new Object[]{
                "ID", "Meal", "Description", "Calories", "Protein", "Carbs", "Fat", "Price"
        });
        readyMealTable = createTable(readyMealModel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        actions.add(new JLabel("Table account:"));
        readyTableAccountLabel = new JLabel(fixedTableLabelText());
        readyTableAccountLabel.setPreferredSize(new Dimension(170, 26));
        actions.add(readyTableAccountLabel);

        readyTableStatusLabel = new JLabel("No table account loaded");
        actions.add(readyTableStatusLabel);

        JButton refreshButton = new JButton("Refresh Meals");
        refreshButton.addActionListener(event -> refreshReadyMeals());
        actions.add(refreshButton);

        JButton refreshTablesButton = new JButton("Refresh Tables");
        refreshTablesButton.addActionListener(event -> refreshTableAccounts());
        actions.add(refreshTablesButton);

        JButton orderButton = new JButton("Place Ready Meal Order");
        orderButton.addActionListener(event -> placeReadyMealOrder());
        actions.add(orderButton);

        panel.add(new JScrollPane(readyMealTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCustomMealPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(buildProfilePanel(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildAvailableIngredientPanel(),
                buildSelectedIngredientPanel());
        splitPane.setResizeWeight(0.55);
        panel.add(splitPane, BorderLayout.CENTER);

        updateCustomSummary();
        return panel;
    }

    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Customer Profile"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        customTableAccountLabel = new JLabel(fixedTableLabelText());
        customTableAccountLabel.setPreferredSize(new Dimension(170, 26));
        customAgeSpinner = integerSpinner(25, 5, 120, 1);
        customWeightSpinner = decimalSpinner(75.0, 20.0, 350.0, 0.5);
        customHeightSpinner = decimalSpinner(170.0, 90.0, 250.0, 0.5);
        customGoalCombo = new JComboBox<Goal>(Goal.values());
        installGoalRenderer(customGoalCombo);

        addFormRow(panel, gbc, 0, 0, "Table account", customTableAccountLabel);
        addFormRow(panel, gbc, 0, 2, "Age", customAgeSpinner);
        addFormRow(panel, gbc, 0, 4, "Weight kg", customWeightSpinner);
        addFormRow(panel, gbc, 1, 0, "Height cm", customHeightSpinner);
        addFormRow(panel, gbc, 1, 2, "Goal", customGoalCombo);

        customTableStatusLabel = new JLabel("No table account loaded");
        addFormRow(panel, gbc, 1, 4, "Account", customTableStatusLabel);

        JPanel conditionsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        for (HealthCondition condition : HealthCondition.values()) {
            JCheckBox checkBox = new JCheckBox(condition.getDisplayName());
            checkBox.addActionListener(event -> handleHealthConditionChanged());
            conditionBoxes.put(condition, checkBox);
            conditionsPanel.add(checkBox);
        }
        addFormRow(panel, gbc, 2, 0, "Health", conditionsPanel, 5);

        customAgeSpinner.addChangeListener(event -> updateCustomSummary());
        customWeightSpinner.addChangeListener(event -> updateCustomSummary());
        customHeightSpinner.addChangeListener(event -> updateCustomSummary());
        customGoalCombo.addActionListener(event -> updateCustomSummary());

        return panel;
    }

    private JPanel buildAvailableIngredientPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Available Ingredients"));

        ingredientModel = new ReadOnlyTableModel(new Object[]{
                "ID", "Name", "Category", "Serving", "Calories", "Protein", "Carbs", "Fat", "Price"
        });
        ingredientTable = createTable(ingredientModel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        JButton refreshButton = new JButton("Refresh Ingredients");
        refreshButton.addActionListener(event -> refreshAvailableIngredients());
        actions.add(refreshButton);

        JButton addButton = new JButton("Add to Meal");
        addButton.addActionListener(event -> addSelectedIngredientToMeal());
        actions.add(addButton);

        panel.add(new JScrollPane(ingredientTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildSelectedIngredientPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Selected Meal"));

        selectedIngredientModel = new ReadOnlyTableModel(new Object[]{
                "ID", "Name", "Category", "Calories", "Protein", "Carbs", "Fat", "Price"
        });
        selectedIngredientTable = createTable(selectedIngredientModel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(event -> removeSelectedIngredient());
        actions.add(removeButton);

        JButton clearButton = new JButton("Clear Meal");
        clearButton.addActionListener(event -> clearCustomMeal());
        actions.add(clearButton);

        JButton submitButton = new JButton("Submit Custom Meal");
        submitButton.addActionListener(event -> submitCustomMeal());
        actions.add(submitButton);

        JPanel summaryPanel = new JPanel(new BorderLayout(4, 4));
        targetCaloriesLabel = new JLabel("Target meal calories: 0.0");
        summaryPriceLabel = new JLabel("Total: 0.00 LYD");
        summaryNutritionLabel = new JLabel("Calories: 0.0 | Protein: 0.0g | Carbs: 0.0g | Fat: 0.0g");
        feedbackArea = new JTextArea(5, 30);
        feedbackArea.setEditable(false);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);

        JPanel labels = new JPanel(new BorderLayout());
        labels.add(targetCaloriesLabel, BorderLayout.NORTH);
        labels.add(summaryPriceLabel, BorderLayout.CENTER);
        labels.add(summaryNutritionLabel, BorderLayout.SOUTH);

        summaryPanel.add(labels, BorderLayout.NORTH);
        summaryPanel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);
        summaryPanel.add(actions, BorderLayout.SOUTH);

        panel.add(new JScrollPane(selectedIngredientTable), BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildChefPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chefLoginPanel = buildChefLoginPanel();
        panel.add(chefLoginPanel, BorderLayout.NORTH);

        chefOrderModel = new ReadOnlyTableModel(new Object[]{
                "ID", "Table", "Type", "Status", "Calories", "Total", "Created"
        });
        chefOrderTable = createTable(chefOrderModel);
        chefOrderTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    showSelectedKitchenOrder();
                }
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        chefRefreshButton = new JButton("Refresh Orders");
        chefRefreshButton.addActionListener(event -> refreshKitchenOrders());
        actions.add(chefRefreshButton);

        chefPreparingButton = new JButton("Set Preparing");
        chefPreparingButton.addActionListener(event -> updateSelectedKitchenOrder(OrderStatus.PREPARING));
        actions.add(chefPreparingButton);

        chefReadyButton = new JButton("Set Ready");
        chefReadyButton.addActionListener(event -> updateSelectedKitchenOrder(OrderStatus.READY));
        actions.add(chefReadyButton);

        chefCompletedButton = new JButton("Set Completed");
        chefCompletedButton.addActionListener(event -> updateSelectedKitchenOrder(OrderStatus.COMPLETED));
        actions.add(chefCompletedButton);

        chefCancelledButton = new JButton("Cancel");
        chefCancelledButton.addActionListener(event -> updateSelectedKitchenOrder(OrderStatus.CANCELLED));
        actions.add(chefCancelledButton);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(new JScrollPane(chefOrderTable), BorderLayout.CENTER);
        left.add(actions, BorderLayout.SOUTH);

        chefDetailsArea = detailsArea();
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                left,
                new JScrollPane(chefDetailsArea));
        splitPane.setResizeWeight(0.62);

        chefWorkspacePanel = new JPanel(new BorderLayout());
        chefWorkspacePanel.add(splitPane, BorderLayout.CENTER);
        chefWorkspacePanel.setVisible(false);
        panel.add(chefWorkspacePanel, BorderLayout.CENTER);

        setChefControlsEnabled(false);
        return panel;
    }

    private JPanel buildChefLoginPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Chef Login"));

        chefUsernameField = new JTextField("chef", 12);
        chefPasswordField = new JPasswordField("chef123", 12);
        chefLoginStatusLabel = new JLabel("Not logged in");

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(event -> loginChef());

        panel.add(new JLabel("Username"));
        panel.add(chefUsernameField);
        panel.add(new JLabel("Password"));
        panel.add(chefPasswordField);
        panel.add(loginButton);
        panel.add(chefLoginStatusLabel);

        return panel;
    }

    private JPanel buildAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        adminLoginPanel = buildAdminLoginPanel();
        panel.add(adminLoginPanel, BorderLayout.NORTH);

        adminIngredientModel = new ReadOnlyTableModel(new Object[]{
                "ID", "Name", "Category", "Serving", "Calories", "Price", "Available"
        });
        adminIngredientTable = createTable(adminIngredientModel);

        JPanel ingredientActions = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        adminRefreshIngredientsButton = new JButton("Refresh Ingredients");
        adminRefreshIngredientsButton.addActionListener(event -> refreshAdminIngredients());
        ingredientActions.add(adminRefreshIngredientsButton);

        adminAddIngredientButton = new JButton("Add Ingredient");
        adminAddIngredientButton.addActionListener(event -> showAddIngredientDialog());
        ingredientActions.add(adminAddIngredientButton);

        adminEditIngredientButton = new JButton("Edit Ingredient");
        adminEditIngredientButton.addActionListener(event -> showEditIngredientDialog());
        ingredientActions.add(adminEditIngredientButton);

        adminDeleteIngredientButton = new JButton("Delete Ingredient");
        adminDeleteIngredientButton.addActionListener(event -> deleteSelectedIngredient());
        ingredientActions.add(adminDeleteIngredientButton);

        adminToggleIngredientButton = new JButton("Toggle Availability");
        adminToggleIngredientButton.addActionListener(event -> toggleSelectedIngredient());
        ingredientActions.add(adminToggleIngredientButton);

        JPanel ingredientPanel = new JPanel(new BorderLayout(8, 8));
        ingredientPanel.setBorder(BorderFactory.createTitledBorder("Ingredient Table"));
        ingredientPanel.add(new JScrollPane(adminIngredientTable), BorderLayout.CENTER);
        ingredientPanel.add(ingredientActions, BorderLayout.SOUTH);

        recentOrderModel = new ReadOnlyTableModel(new Object[]{
                "ID", "Table", "Type", "Status", "Calories", "Total", "Created"
        });
        recentOrderTable = createTable(recentOrderModel);
        recentOrderTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    showSelectedRecentOrder();
                }
            }
        });

        adminRefreshOrdersButton = new JButton("Refresh Recent Orders");
        adminRefreshOrdersButton.addActionListener(event -> refreshRecentOrders());

        adminEditOrderButton = new JButton("Edit Order");
        adminEditOrderButton.addActionListener(event -> showEditOrderDialog());

        adminDeleteOrderButton = new JButton("Delete Order");
        adminDeleteOrderButton.addActionListener(event -> deleteSelectedOrder());

        JPanel orderActions = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        orderActions.add(adminRefreshOrdersButton);
        orderActions.add(adminEditOrderButton);
        orderActions.add(adminDeleteOrderButton);

        JPanel orderPanel = new JPanel(new BorderLayout(8, 8));
        orderPanel.setBorder(BorderFactory.createTitledBorder("Recent Orders"));
        orderPanel.add(new JScrollPane(recentOrderTable), BorderLayout.CENTER);
        orderPanel.add(orderActions, BorderLayout.SOUTH);

        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ingredientPanel, orderPanel);
        topSplit.setResizeWeight(0.55);

        adminDetailsArea = detailsArea();
        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                topSplit,
                new JScrollPane(adminDetailsArea));
        mainSplit.setResizeWeight(0.68);

        JTabbedPane adminTabs = new JTabbedPane();
        adminTabs.addTab("Menu and Orders", mainSplit);
        adminTabs.addTab("Accounts", buildAdminAccountsPanel());

        adminWorkspacePanel = new JPanel(new BorderLayout());
        adminWorkspacePanel.add(adminTabs, BorderLayout.CENTER);
        adminWorkspacePanel.setVisible(false);
        panel.add(adminWorkspacePanel, BorderLayout.CENTER);

        setAdminControlsEnabled(false);
        return panel;
    }

    private JPanel buildAdminLoginPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Admin Login"));

        adminUsernameField = new JTextField("admin", 12);
        adminPasswordField = new JPasswordField("admin123", 12);
        adminLoginStatusLabel = new JLabel("Not logged in");

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(event -> loginAdmin());

        panel.add(new JLabel("Username"));
        panel.add(adminUsernameField);
        panel.add(new JLabel("Password"));
        panel.add(adminPasswordField);
        panel.add(loginButton);
        panel.add(adminLoginStatusLabel);

        return panel;
    }

    private JPanel buildAdminAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        adminStaffModel = new ReadOnlyTableModel(new Object[]{
                "ID", "Username", "Full Name", "Role", "Active"
        });
        adminStaffTable = createTable(adminStaffModel);

        JPanel staffActions = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        adminRefreshAccountsButton = new JButton("Refresh Accounts");
        adminRefreshAccountsButton.addActionListener(event -> refreshAdminAccounts());
        staffActions.add(adminRefreshAccountsButton);

        adminAddStaffButton = new JButton("Add Staff User");
        adminAddStaffButton.addActionListener(event -> showStaffDialog(false));
        staffActions.add(adminAddStaffButton);

        adminEditStaffButton = new JButton("Edit Staff User");
        adminEditStaffButton.addActionListener(event -> showStaffDialog(true));
        staffActions.add(adminEditStaffButton);

        adminToggleStaffButton = new JButton("Toggle Staff Active");
        adminToggleStaffButton.addActionListener(event -> toggleSelectedStaffUser());
        staffActions.add(adminToggleStaffButton);

        JPanel staffPanel = new JPanel(new BorderLayout(8, 8));
        staffPanel.setBorder(BorderFactory.createTitledBorder("Staff Users and Permissions"));
        staffPanel.add(new JScrollPane(adminStaffTable), BorderLayout.CENTER);
        staffPanel.add(staffActions, BorderLayout.SOUTH);

        adminTableAccountModel = new ReadOnlyTableModel(new Object[]{
                "ID", "Account", "Table", "Status", "Active"
        });
        adminTableAccountTable = createTable(adminTableAccountModel);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        adminEditTableButton = new JButton("Edit Table Account");
        adminEditTableButton.addActionListener(event -> showTableAccountDialog());
        tableActions.add(adminEditTableButton);

        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Customer Table Accounts"));
        tablePanel.add(new JScrollPane(adminTableAccountTable), BorderLayout.CENTER);
        tablePanel.add(tableActions, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, staffPanel, tablePanel);
        splitPane.setResizeWeight(0.5);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadStartupData() {
        runDatabaseTask("Preparing MySQL data...", new DatabaseTask<Void>() {
            @Override
            public Void run() throws Exception {
                repository.prepare();
                return null;
            }
        }, new TaskSuccess<Void>() {
            @Override
            public void onSuccess(Void result) {
                setStatus("Connected to MySQL");
                refreshTableAccounts();
            }
        });
    }

    private void refreshTableAccounts() {
        runDatabaseTask("Loading open table accounts...", new DatabaseTask<List<DiningTableAccount>>() {
            @Override
            public List<DiningTableAccount> run() throws Exception {
                return repository.findOpenTableAccounts();
            }
        }, new TaskSuccess<List<DiningTableAccount>>() {
            @Override
            public void onSuccess(List<DiningTableAccount> result) {
                tableAccounts = result;
                if (fixedTableAccount != null) {
                    fixedTableAccount = findTableAccountByNumber(result, fixedTableAccount.getTableNumber());
                }
                updateTableStatusLabels();
                updateCustomSummary();
                setStatus("Loaded " + tableAccounts.size() + " table accounts");
            }
        });
    }

    private DiningTableAccount findTableAccountByNumber(List<DiningTableAccount> accounts, int tableNumber) {
        for (DiningTableAccount account : accounts) {
            if (account.getTableNumber() == tableNumber) {
                return account;
            }
        }
        return null;
    }

    private void loginCustomerTable() {
        final String username = tableUsernameField.getText().trim();
        final String password = new String(tablePasswordField.getPassword());
        runDatabaseTask("Checking table login...", new DatabaseTask<Optional<DiningTableAccount>>() {
            @Override
            public Optional<DiningTableAccount> run() throws Exception {
                return repository.loginTableAccount(username, password);
            }
        }, new TaskSuccess<Optional<DiningTableAccount>>() {
            @Override
            public void onSuccess(Optional<DiningTableAccount> account) {
                if (!account.isPresent()) {
                    fixedTableAccount = null;
                    tableLoginStatusLabel.setText("Invalid table account or password");
                    updateTableStatusLabels();
                    return;
                }

                fixedTableAccount = account.get();
                tableLoginStatusLabel.setText("Opened " + fixedTableAccount.getAccountName());
                updateTableStatusLabels();
                showScreen(SCREEN_CUSTOMER);
                refreshReadyMeals();
                refreshAvailableIngredients();
            }
        });
    }

    private void updateTableStatusLabels() {
        if (readyTableAccountLabel != null) {
            readyTableAccountLabel.setText(fixedTableLabelText());
        }
        if (customTableAccountLabel != null) {
            customTableAccountLabel.setText(fixedTableLabelText());
        }
        if (readyTableStatusLabel != null) {
            readyTableStatusLabel.setText(tableAccountStatus(fixedTableAccount));
        }
        if (customTableStatusLabel != null) {
            customTableStatusLabel.setText(tableAccountStatus(fixedTableAccount));
        }
    }

    private String tableAccountStatus(DiningTableAccount account) {
        if (account == null) {
            return "No table is signed in";
        }
        return account.getAccountName() + " is open";
    }

    private String fixedTableLabelText() {
        if (fixedTableAccount != null) {
            return fixedTableAccount.getDisplayName();
        }
        return "No table signed in";
    }

    private void refreshReadyMeals() {
        runDatabaseTask("Loading ready meals...", new DatabaseTask<List<ReadyMeal>>() {
            @Override
            public List<ReadyMeal> run() throws Exception {
                return repository.findAllAvailableReadyMeals();
            }
        }, new TaskSuccess<List<ReadyMeal>>() {
            @Override
            public void onSuccess(List<ReadyMeal> result) {
                readyMeals = result;
                readyMealModel.setRowCount(0);
                for (ReadyMeal meal : readyMeals) {
                    readyMealModel.addRow(new Object[]{
                            meal.getId(),
                            meal.getName(),
                            meal.getDescription(),
                            formatDouble(meal.getCalories()),
                            formatDouble(meal.getProteinGrams()),
                            formatDouble(meal.getCarbsGrams()),
                            formatDouble(meal.getFatGrams()),
                            meal.getPrice().toPlainString()
                    });
                }
                setStatus("Loaded " + readyMeals.size() + " ready meals");
            }
        });
    }

    private void placeReadyMealOrder() {
        final int row = selectedModelRow(readyMealTable);
        if (row < 0 || row >= readyMeals.size()) {
            showInfo("Select a ready meal first.");
            return;
        }

        DiningTableAccount tableAccount = selectedCustomerTableAccount();
        if (tableAccount == null) {
            showInfo("Please sign in with a table account first.");
            return;
        }

        final ReadyMeal meal = readyMeals.get(row);
        final int tableNumber = tableAccount.getTableNumber();
        runDatabaseTask("Placing ready meal order...", new DatabaseTask<Integer>() {
            @Override
            public Integer run() throws Exception {
                return repository.createReadyMealOrder(tableNumber, meal);
            }
        }, new TaskSuccess<Integer>() {
            @Override
            public void onSuccess(Integer orderId) {
                setStatus("Ready meal order #" + orderId + " sent to kitchen");
                JOptionPane.showMessageDialog(MainFrame.this,
                        "Order #" + orderId + " was sent to the kitchen.",
                        "Order Created",
                        JOptionPane.INFORMATION_MESSAGE);
                if (chefLoggedIn) {
                    refreshKitchenOrders();
                }
                if (adminLoggedIn) {
                    refreshRecentOrders();
                }
            }
        });
    }

    private void refreshAvailableIngredients() {
        runDatabaseTask("Loading ingredients...", new DatabaseTask<List<Ingredient>>() {
            @Override
            public List<Ingredient> run() throws Exception {
                return repository.findAllAvailableIngredients();
            }
        }, new TaskSuccess<List<Ingredient>>() {
            @Override
            public void onSuccess(List<Ingredient> result) {
                availableIngredients = result;
                refreshSelectableIngredientTable();
            }
        });
    }

    private void addSelectedIngredientToMeal() {
        int row = selectedModelRow(ingredientTable);
        if (row < 0 || row >= selectableIngredients.size()) {
            showInfo("Select an ingredient first.");
            return;
        }

        Ingredient ingredient = selectableIngredients.get(row);
        for (Ingredient selected : selectedIngredients) {
            if (selected.getId() == ingredient.getId()) {
                showInfo("This ingredient is already selected.");
                return;
            }
        }

        CustomerProfile profile = readCustomerProfile();
        List<String> validationErrors = nutritionService.validateIngredient(ingredient, profile);
        if (!validationErrors.isEmpty()) {
            showInfo(joinLines(validationErrors));
            return;
        }

        selectedIngredients.add(ingredient);
        refreshSelectedIngredientTable();
        updateCustomSummary();
    }

    private void removeSelectedIngredient() {
        int row = selectedModelRow(selectedIngredientTable);
        if (row < 0 || row >= selectedIngredients.size()) {
            showInfo("Select an ingredient to remove.");
            return;
        }
        selectedIngredients.remove(row);
        refreshSelectedIngredientTable();
        updateCustomSummary();
    }

    private void clearCustomMeal() {
        selectedIngredients.clear();
        refreshSelectedIngredientTable();
        updateCustomSummary();
    }

    private void handleHealthConditionChanged() {
        CustomerProfile profile = readCustomerProfile();
        List<String> removedNames = removeUnsafeSelectedIngredients(profile);
        refreshSelectedIngredientTable();
        refreshSelectableIngredientTable();
        updateCustomSummary();

        if (!removedNames.isEmpty()) {
            showInfo("تم حذف المنتجات غير المناسبة لحالتك الصحية:\n"
                    + joinLines(removedNames)
                    + "\n\nلن تظهر هذه المنتجات في قائمة الاختيار طالما الحالة الصحية مفعلة.");
        }
    }

    private List<String> removeUnsafeSelectedIngredients(CustomerProfile profile) {
        List<String> removedNames = new ArrayList<String>();
        List<Ingredient> safeIngredients = new ArrayList<Ingredient>();

        for (Ingredient ingredient : selectedIngredients) {
            if (nutritionService.validateIngredient(ingredient, profile).isEmpty()) {
                safeIngredients.add(ingredient);
            } else {
                removedNames.add("- " + ingredient.getName());
            }
        }

        selectedIngredients = safeIngredients;
        return removedNames;
    }

    private void submitCustomMeal() {
        if (selectedIngredients.isEmpty()) {
            showInfo("Choose at least one ingredient.");
            return;
        }
        if (selectedCustomerTableAccount() == null) {
            showInfo("Please sign in with a table account first.");
            return;
        }

        final CustomerProfile profile = readCustomerProfile();
        final MealSummary summary = buildSelectedMealSummary();
        final List<Ingredient> ingredients = new ArrayList<Ingredient>(selectedIngredients);

        runDatabaseTask("Submitting custom meal...", new DatabaseTask<Integer>() {
            @Override
            public Integer run() throws Exception {
                return repository.createCustomMealOrder(profile, ingredients, summary, nutritionService);
            }
        }, new TaskSuccess<Integer>() {
            @Override
            public void onSuccess(Integer orderId) {
                setStatus("Custom meal order #" + orderId + " sent to kitchen");
                JOptionPane.showMessageDialog(MainFrame.this,
                        "Custom order #" + orderId + " was sent to the kitchen.",
                        "Order Created",
                        JOptionPane.INFORMATION_MESSAGE);
                clearCustomMeal();
                if (chefLoggedIn) {
                    refreshKitchenOrders();
                }
                if (adminLoggedIn) {
                    refreshRecentOrders();
                }
            }
        });
    }

    private void refreshSelectedIngredientTable() {
        selectedIngredientModel.setRowCount(0);
        for (Ingredient ingredient : selectedIngredients) {
            selectedIngredientModel.addRow(new Object[]{
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getCategory().getDisplayName(),
                    formatDouble(ingredient.getCalories()),
                    formatDouble(ingredient.getProteinGrams()),
                    formatDouble(ingredient.getCarbsGrams()),
                    formatDouble(ingredient.getFatGrams()),
                    ingredient.getPrice().toPlainString()
            });
        }
    }

    private void refreshSelectableIngredientTable() {
        if (ingredientModel == null) {
            return;
        }

        CustomerProfile profile = readCustomerProfile();
        selectableIngredients = new ArrayList<Ingredient>();
        ingredientModel.setRowCount(0);

        for (Ingredient ingredient : availableIngredients) {
            if (nutritionService.validateIngredient(ingredient, profile).isEmpty()) {
                selectableIngredients.add(ingredient);
                ingredientModel.addRow(availableIngredientRow(ingredient));
            }
        }

        int hiddenCount = availableIngredients.size() - selectableIngredients.size();
        if (hiddenCount > 0) {
            setStatus("Loaded " + selectableIngredients.size()
                    + " safe ingredients; hidden " + hiddenCount + " for selected health conditions");
        } else {
            setStatus("Loaded " + selectableIngredients.size() + " available ingredients");
        }
    }

    private void updateCustomSummary() {
        if (summaryPriceLabel == null || summaryNutritionLabel == null || feedbackArea == null) {
            return;
        }

        CustomerProfile profile = readCustomerProfile();
        MealSummary summary = buildSelectedMealSummary();
        targetCaloriesLabel.setText("Target meal calories: "
                + nutritionService.format(nutritionService.estimateMealTarget(profile)));
        summaryPriceLabel.setText("Total: " + summary.getPrice().toPlainString() + " LYD");
        summaryNutritionLabel.setText(String.format(Locale.US,
                "Calories: %.1f | Protein: %.1fg | Carbs: %.1fg | Fat: %.1fg",
                summary.getCalories(),
                summary.getProteinGrams(),
                summary.getCarbsGrams(),
                summary.getFatGrams()));

        if (selectedIngredients.isEmpty()) {
            feedbackArea.setText("Select ingredients to see nutrition feedback.");
        } else {
            feedbackArea.setText(joinLines(nutritionService.buildSmartFeedback(profile, summary)));
        }
    }

    private CustomerProfile readCustomerProfile() {
        Goal goal = (Goal) customGoalCombo.getSelectedItem();
        if (goal == null) {
            goal = Goal.MAINTAIN;
        }

        Set<HealthCondition> conditions = EnumSet.noneOf(HealthCondition.class);
        for (Map.Entry<HealthCondition, JCheckBox> entry : conditionBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                conditions.add(entry.getKey());
            }
        }

        return new CustomerProfile(
                selectedCustomerTableNumber(),
                intValue(customAgeSpinner),
                doubleValue(customWeightSpinner),
                doubleValue(customHeightSpinner),
                goal,
                conditions);
    }

    private MealSummary buildSelectedMealSummary() {
        MealSummary summary = new MealSummary();
        for (Ingredient ingredient : selectedIngredients) {
            summary.addIngredient(ingredient);
        }
        return summary;
    }

    private void loginChef() {
        final String username = chefUsernameField.getText().trim();
        final String password = new String(chefPasswordField.getPassword());
        runDatabaseTask("Checking chef login...", new DatabaseTask<Optional<User>>() {
            @Override
            public Optional<User> run() throws Exception {
                return repository.login(username, password, User.ROLE_CHEF);
            }
        }, new TaskSuccess<Optional<User>>() {
            @Override
            public void onSuccess(Optional<User> user) {
                if (!user.isPresent()) {
                    chefLoggedIn = false;
                    setChefControlsEnabled(false);
                    chefLoginStatusLabel.setText("Invalid login");
                    return;
                }

                chefLoggedIn = true;
                chefLoginStatusLabel.setText("Logged in: " + user.get().getFullName());
                chefLoginPanel.setVisible(false);
                chefWorkspacePanel.setVisible(true);
                setChefControlsEnabled(true);
                revalidate();
                repaint();
                refreshKitchenOrders();
            }
        });
    }

    private void refreshKitchenOrders() {
        if (!chefLoggedIn) {
            showInfo("Chef login is required.");
            return;
        }

        runDatabaseTask("Loading kitchen orders...", new DatabaseTask<List<OrderTicket>>() {
            @Override
            public List<OrderTicket> run() throws Exception {
                return repository.findKitchenOrders();
            }
        }, new TaskSuccess<List<OrderTicket>>() {
            @Override
            public void onSuccess(List<OrderTicket> result) {
                kitchenOrders = result;
                chefOrderModel.setRowCount(0);
                for (OrderTicket ticket : kitchenOrders) {
                    chefOrderModel.addRow(ticketRow(ticket));
                }
                chefDetailsArea.setText("");
                setStatus("Loaded " + kitchenOrders.size() + " kitchen orders");
            }
        });
    }

    private void showSelectedKitchenOrder() {
        if (!chefLoggedIn) {
            return;
        }

        int orderId = selectedKitchenOrderId();
        if (orderId <= 0) {
            return;
        }
        loadTicketDetails(orderId, chefDetailsArea);
    }

    private void updateSelectedKitchenOrder(final OrderStatus status) {
        if (!chefLoggedIn) {
            showInfo("Chef login is required.");
            return;
        }

        final int orderId = selectedKitchenOrderId();
        if (orderId <= 0) {
            showInfo("Select an order first.");
            return;
        }

        runDatabaseTask("Updating order status...", new DatabaseTask<Void>() {
            @Override
            public Void run() throws Exception {
                repository.updateStatus(orderId, status);
                return null;
            }
        }, new TaskSuccess<Void>() {
            @Override
            public void onSuccess(Void result) {
                setStatus("Order #" + orderId + " updated to " + status.getDisplayName());
                refreshKitchenOrders();
                if (adminLoggedIn) {
                    refreshRecentOrders();
                }
            }
        });
    }

    private int selectedKitchenOrderId() {
        int row = selectedModelRow(chefOrderTable);
        if (row < 0 || row >= kitchenOrders.size()) {
            return -1;
        }
        return kitchenOrders.get(row).getId();
    }

    private void loginAdmin() {
        final String username = adminUsernameField.getText().trim();
        final String password = new String(adminPasswordField.getPassword());
        runDatabaseTask("Checking admin login...", new DatabaseTask<Optional<User>>() {
            @Override
            public Optional<User> run() throws Exception {
                return repository.login(username, password, User.ROLE_ADMIN);
            }
        }, new TaskSuccess<Optional<User>>() {
            @Override
            public void onSuccess(Optional<User> user) {
                if (!user.isPresent()) {
                    adminLoggedIn = false;
                    setAdminControlsEnabled(false);
                    adminLoginStatusLabel.setText("Invalid login");
                    return;
                }

                adminLoggedIn = true;
                adminLoginStatusLabel.setText("Logged in: " + user.get().getFullName());
                adminLoginPanel.setVisible(false);
                adminWorkspacePanel.setVisible(true);
                setAdminControlsEnabled(true);
                revalidate();
                repaint();
                refreshAdminIngredients();
                refreshRecentOrders();
                refreshAdminAccounts();
            }
        });
    }

    private void refreshAdminAccounts() {
        if (!adminLoggedIn) {
            showInfo("Admin login is required.");
            return;
        }

        runDatabaseTask("Loading accounts...", new DatabaseTask<AccountLists>() {
            @Override
            public AccountLists run() throws Exception {
                return new AccountLists(repository.findStaffAccounts(), repository.findAllTableAccounts());
            }
        }, new TaskSuccess<AccountLists>() {
            @Override
            public void onSuccess(AccountLists result) {
                staffAccounts = result.staffAccounts;
                adminTableAccounts = result.tableAccounts;
                refreshStaffTable();
                refreshTableAccountTable();
                setStatus("Loaded staff and table accounts");
            }
        });
    }

    private void refreshStaffTable() {
        adminStaffModel.setRowCount(0);
        for (StaffAccount account : staffAccounts) {
            adminStaffModel.addRow(new Object[]{
                    account.getId(),
                    account.getUsername(),
                    account.getFullName(),
                    account.getRole(),
                    yesNo(account.isActive())
            });
        }
    }

    private void refreshTableAccountTable() {
        adminTableAccountModel.setRowCount(0);
        for (DiningTableAccount account : adminTableAccounts) {
            adminTableAccountModel.addRow(new Object[]{
                    account.getId(),
                    account.getAccountName(),
                    account.getTableNumber(),
                    account.getStatus(),
                    yesNo(account.isActive())
            });
        }
    }

    private void showStaffDialog(boolean editMode) {
        if (!adminLoggedIn) {
            showInfo("Admin login is required.");
            return;
        }

        StaffAccount existing = null;
        if (editMode) {
            int row = selectedModelRow(adminStaffTable);
            if (row < 0 || row >= staffAccounts.size()) {
                showInfo("Select a staff user first.");
                return;
            }
            existing = staffAccounts.get(row);
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField(existing == null ? "" : existing.getUsername(), 18);
        JTextField fullNameField = new JTextField(existing == null ? "" : existing.getFullName(), 18);
        JComboBox<Integer> roleCombo = new JComboBox<Integer>(User.staffRoles());
        if (existing != null) {
            roleCombo.setSelectedItem(existing.getRole());
            usernameField.setEditable(false);
        }
        JPasswordField passwordField = new JPasswordField("", 18);
        JCheckBox activeBox = new JCheckBox("Active", existing == null || existing.isActive());

        addFormRow(panel, gbc, 0, 0, "Username", usernameField);
        addFormRow(panel, gbc, 1, 0, "Full name", fullNameField);
        addFormRow(panel, gbc, 2, 0, "Role", roleCombo);
        addFormRow(panel, gbc, 3, 0, "Password", passwordField);
        addFormRow(panel, gbc, 4, 0, "", activeBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                editMode ? "Edit Staff User" : "Add Staff User",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        final String username = usernameField.getText().trim();
        final String fullName = fullNameField.getText().trim();
        final String password = new String(passwordField.getPassword());
        final Integer selectedRole = (Integer) roleCombo.getSelectedItem();
        final int role = selectedRole == null ? 0 : selectedRole.intValue();
        final boolean active = activeBox.isSelected();
        if (username.isEmpty() || fullName.isEmpty() || role == 0) {
            showInfo("Username, full name, and role are required.");
            return;
        }
        if (!editMode && password.trim().isEmpty()) {
            showInfo("Password is required for a new staff user.");
            return;
        }

        runDatabaseTask("Saving staff user...", new DatabaseTask<Void>() {
            @Override
            public Void run() throws Exception {
                repository.saveStaffAccount(username, password, fullName, role, active);
                return null;
            }
        }, new TaskSuccess<Void>() {
            @Override
            public void onSuccess(Void result) {
                refreshAdminAccounts();
            }
        });
    }

    private void toggleSelectedStaffUser() {
        int row = selectedModelRow(adminStaffTable);
        if (row < 0 || row >= staffAccounts.size()) {
            showInfo("Select a staff user first.");
            return;
        }

        final StaffAccount account = staffAccounts.get(row);
        runDatabaseTask("Updating staff account...", new DatabaseTask<Void>() {
            @Override
            public Void run() throws Exception {
                repository.updateStaffActive(account.getId(), !account.isActive());
                return null;
            }
        }, new TaskSuccess<Void>() {
            @Override
            public void onSuccess(Void result) {
                refreshAdminAccounts();
            }
        });
    }

    private void showTableAccountDialog() {
        if (!adminLoggedIn) {
            showInfo("Admin login is required.");
            return;
        }

        int row = selectedModelRow(adminTableAccountTable);
        if (row < 0 || row >= adminTableAccounts.size()) {
            showInfo("Select a table account first.");
            return;
        }

        DiningTableAccount existing = adminTableAccounts.get(row);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel accountLabel = new JLabel(existing.getAccountName());
        JPasswordField passwordField = new JPasswordField("", 18);
        JComboBox<String> statusCombo = new JComboBox<String>(new String[]{"OPEN", "CLOSED"});
        statusCombo.setSelectedItem(existing.getStatus());
        JCheckBox activeBox = new JCheckBox("Active", existing.isActive());

        addFormRow(panel, gbc, 0, 0, "Account", accountLabel);
        addFormRow(panel, gbc, 1, 0, "New password", passwordField);
        addFormRow(panel, gbc, 2, 0, "Status", statusCombo);
        addFormRow(panel, gbc, 3, 0, "", activeBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Table Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        final int tableNumber = existing.getTableNumber();
        final String password = new String(passwordField.getPassword());
        final String status = (String) statusCombo.getSelectedItem();
        final boolean active = activeBox.isSelected();

        runDatabaseTask("Saving table account...", new DatabaseTask<Void>() {
            @Override
            public Void run() throws Exception {
                repository.saveTableAccount(tableNumber, password, status, active);
                return null;
            }
        }, new TaskSuccess<Void>() {
            @Override
            public void onSuccess(Void result) {
                refreshAdminAccounts();
                refreshTableAccounts();
            }
        });
    }

    private void refreshAdminIngredients() {
        if (!adminLoggedIn) {
            showInfo("Admin login is required.");
            return;
        }

        runDatabaseTask("Loading ingredient table...", new DatabaseTask<List<Ingredient>>() {
            @Override
            public List<Ingredient> run() throws Exception {
                return repository.findAllIngredients();
            }
        }, new TaskSuccess<List<Ingredient>>() {
            @Override
            public void onSuccess(List<Ingredient> result) {
                adminIngredients = result;
                adminIngredientModel.setRowCount(0);
                for (Ingredient ingredient : adminIngredients) {
                    adminIngredientModel.addRow(new Object[]{
                            ingredient.getId(),
                            ingredient.getName(),
                            ingredient.getCategory().getDisplayName(),
                            ingredient.getServingLabel(),
                            formatDouble(ingredient.getCalories()),
                            ingredient.getPrice().toPlainString(),
                            yesNo(ingredient.isAvailable())
                    });
                }
                setStatus("Loaded " + adminIngredients.size() + " ingredients");
            }
        });
    }

    private void showAddIngredientDialog() {
        if (!adminLoggedIn) {
            showInfo("Admin login is required.");
            return;
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JComboBox<IngredientCategory> categoryCombo =
                new JComboBox<IngredientCategory>(IngredientCategory.values());
        installCategoryRenderer(categoryCombo);
        JTextField servingField = new JTextField("1 serving", 20);
        JTextField priceField = new JTextField("0.00", 10);
        JSpinner caloriesSpinner = decimalSpinner(0.0, 0.0, 5000.0, 1.0);
        JSpinner proteinSpinner = decimalSpinner(0.0, 0.0, 500.0, 1.0);
        JSpinner carbsSpinner = decimalSpinner(0.0, 0.0, 500.0, 1.0);
        JSpinner fatSpinner = decimalSpinner(0.0, 0.0, 500.0, 1.0);
        JCheckBox lactoseBox = new JCheckBox("Contains lactose");
        JCheckBox glutenBox = new JCheckBox("Contains gluten");
        JCheckBox sugarBox = new JCheckBox("High sugar");
        JCheckBox sodiumBox = new JCheckBox("High sodium");
        JCheckBox highFatBox = new JCheckBox("High fat");

        addFormRow(panel, gbc, 0, 0, "Name", nameField);
        addFormRow(panel, gbc, 1, 0, "Category", categoryCombo);
        addFormRow(panel, gbc, 2, 0, "Serving", servingField);
        addFormRow(panel, gbc, 3, 0, "Price", priceField);
        addFormRow(panel, gbc, 4, 0, "Calories", caloriesSpinner);
        addFormRow(panel, gbc, 5, 0, "Protein g", proteinSpinner);
        addFormRow(panel, gbc, 6, 0, "Carbs g", carbsSpinner);
        addFormRow(panel, gbc, 7, 0, "Fat g", fatSpinner);

        JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        flags.add(lactoseBox);
        flags.add(glutenBox);
        flags.add(sugarBox);
        flags.add(sodiumBox);
        flags.add(highFatBox);
        addFormRow(panel, gbc, 8, 0, "Flags", flags);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Ingredient",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        final Ingredient ingredient;
        try {
            String name = nameField.getText().trim();
            String serving = servingField.getText().trim();
            if (name.isEmpty() || serving.isEmpty()) {
                showInfo("Name and serving are required.");
                return;
            }

            BigDecimal price = new BigDecimal(priceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                showInfo("Price must not be negative.");
                return;
            }

            ingredient = new Ingredient(
                    0,
                    name,
                    (IngredientCategory) categoryCombo.getSelectedItem(),
                    serving,
                    price,
                    doubleValue(caloriesSpinner),
                    doubleValue(proteinSpinner),
                    doubleValue(carbsSpinner),
                    doubleValue(fatSpinner),
                    lactoseBox.isSelected(),
                    glutenBox.isSelected(),
                    sugarBox.isSelected(),
                    sodiumBox.isSelected(),
                    highFatBox.isSelected(),
                    true);
        } catch (NumberFormatException ex) {
            showInfo("Enter a valid price, for example 12.50.");
            return;
        }

        runDatabaseTask("Adding ingredient...", new DatabaseTask<Integer>() {
            @Override
            public Integer run() throws Exception {
                return repository.insertIngredient(ingredient);
            }
        }, new TaskSuccess<Integer>() {
            @Override
            public void onSuccess(Integer id) {
                setStatus("Ingredient #" + id + " added");
                refreshAdminIngredients();
                refreshAvailableIngredients();
            }
        });
    }

    private void showEditIngredientDialog() {
        if (!adminLoggedIn) {
            showInfo("Admin login is required.");
            return;
        }

        int row = selectedModelRow(adminIngredientTable);
        if (row < 0 || row >= adminIngredients.size()) {
            showInfo("Select an ingredient first.");
            return;
        }

        final Ingredient existing = adminIngredients.get(row);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(existing.getName(), 20);
        JComboBox<IngredientCategory> categoryCombo =
                new JComboBox<IngredientCategory>(IngredientCategory.values());
        installCategoryRenderer(categoryCombo);
        categoryCombo.setSelectedItem(existing.getCategory());
        JTextField servingField = new JTextField(existing.getServingLabel(), 20);
        JTextField priceField = new JTextField(existing.getPrice().toPlainString(), 10);
        JSpinner caloriesSpinner = decimalSpinner(existing.getCalories(), 0.0, 5000.0, 1.0);
        JSpinner proteinSpinner = decimalSpinner(existing.getProteinGrams(), 0.0, 500.0, 1.0);
        JSpinner carbsSpinner = decimalSpinner(existing.getCarbsGrams(), 0.0, 500.0, 1.0);
        JSpinner fatSpinner = decimalSpinner(existing.getFatGrams(), 0.0, 500.0, 1.0);
        JCheckBox lactoseBox = new JCheckBox("Contains lactose", existing.isContainsLactose());
        JCheckBox glutenBox = new JCheckBox("Contains gluten", existing.isContainsGluten());
        JCheckBox sugarBox = new JCheckBox("High sugar", existing.isHighSugar());
        JCheckBox sodiumBox = new JCheckBox("High sodium", existing.isHighSodium());
        JCheckBox highFatBox = new JCheckBox("High fat", existing.isHighFat());
        JCheckBox availableBox = new JCheckBox("Available", existing.isAvailable());

        addFormRow(panel, gbc, 0, 0, "Name", nameField);
        addFormRow(panel, gbc, 1, 0, "Category", categoryCombo);
        addFormRow(panel, gbc, 2, 0, "Serving", servingField);
        addFormRow(panel, gbc, 3, 0, "Price", priceField);
        addFormRow(panel, gbc, 4, 0, "Calories", caloriesSpinner);
        addFormRow(panel, gbc, 5, 0, "Protein g", proteinSpinner);
        addFormRow(panel, gbc, 6, 0, "Carbs g", carbsSpinner);
        addFormRow(panel, gbc, 7, 0, "Fat g", fatSpinner);

        JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        flags.add(lactoseBox);
        flags.add(glutenBox);
        flags.add(sugarBox);
        flags.add(sodiumBox);
        flags.add(highFatBox);
        flags.add(availableBox);
        addFormRow(panel, gbc, 8, 0, "Flags", flags);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Ingredient #" + existing.getId(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        final Ingredient updated;
        try {
            String name = nameField.getText().trim();
            String serving = servingField.getText().trim();
            if (name.isEmpty() || serving.isEmpty()) {
                showInfo("Name and serving are required.");
                return;
            }

            BigDecimal price = new BigDecimal(priceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                showInfo("Price must not be negative.");
                return;
            }

            updated = new Ingredient(
                    existing.getId(),
                    name,
                    (IngredientCategory) categoryCombo.getSelectedItem(),
                    serving,
                    price,
                    doubleValue(caloriesSpinner),
                    doubleValue(proteinSpinner),
                    doubleValue(carbsSpinner),
                    doubleValue(fatSpinner),
                    lactoseBox.isSelected(),
                    glutenBox.isSelected(),
                    sugarBox.isSelected(),
                    sodiumBox.isSelected(),
                    highFatBox.isSelected(),
                    availableBox.isSelected());
        } catch (NumberFormatException ex) {
            showInfo("Enter a valid price, for example 12.50.");
            return;
        }

        runDatabaseTask("Updating ingredient...", new DatabaseTask<Void>() {
            @Override
            public Void run() throws Exception {
                repository.updateIngredient(updated);
                return null;
            }
        }, new TaskSuccess<Void>() {
            @Override
            public void onSuccess(Void result) {
                setStatus("Ingredient #" + updated.getId() + " updated");
                refreshAdminIngredients();
                refreshAvailableIngredients();
            }
        });
    }

    private void deleteSelectedIngredient() {
        if (!adminLoggedIn) {
            showInfo("Admin login is required.");
            return;
        }

        int row = selectedModelRow(adminIngredientTable);
        if (row < 0 || row >= adminIngredients.size()) {
            showInfo("Select an ingredient first.");
            return;
        }

        final Ingredient ingredient = adminIngredients.get(row);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete ingredient #" + ingredient.getId() + " - " + ingredient.getName()
                        + "?\nThis will also remove it from ready-meal ingredient links.",
                "Delete Ingredient",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        runDatabaseTask("Deleting ingredient...", new DatabaseTask<Void>() {
            @Override
            public Void run() throws Exception {
                repository.deleteIngredient(ingredient.getId());
                return null;
            }
        }, new TaskSuccess<Void>() {
            @Override
            public void onSuccess(Void result) {
                setStatus("Ingredient #" + ingredient.getId() + " deleted");
                refreshAdminIngredients();
                refreshAvailableIngredients();
            }
        });
    }

    private void toggleSelectedIngredient() {
        if (!adminLoggedIn) {
            showInfo("Admin login is required.");
            return;
        }

        int row = selectedModelRow(adminIngredientTable);
        if (row < 0 || row >= adminIngredients.size()) {
            showInfo("Select an ingredient first.");
            return;
        }

        final Ingredient ingredient = adminIngredients.get(row);
        final boolean newAvailability = !ingredient.isAvailable();
        runDatabaseTask("Updating ingredient availability...", new DatabaseTask<Void>() {
            @Override
            public Void run() throws Exception {
                repository.updateIngredientAvailability(ingredient.getId(), newAvailability);
                return null;
            }
        }, new TaskSuccess<Void>() {
            @Override
            public void onSuccess(Void result) {
                setStatus("Ingredient #" + ingredient.getId()
                        + " availability set to " + yesNo(newAvailability));
                refreshAdminIngredients();
                refreshAvailableIngredients();
            }
        });
    }

    private void refreshRecentOrders() {
        if (!adminLoggedIn) {
            showInfo("Admin login is required.");
            return;
        }

        runDatabaseTask("Loading recent orders...", new DatabaseTask<List<OrderTicket>>() {
            @Override
            public List<OrderTicket> run() throws Exception {
                return repository.findRecentOrders(20);
            }
        }, new TaskSuccess<List<OrderTicket>>() {
            @Override
            public void onSuccess(List<OrderTicket> result) {
                recentOrders = result;
                recentOrderModel.setRowCount(0);
                for (OrderTicket ticket : recentOrders) {
                    recentOrderModel.addRow(ticketRow(ticket));
                }
                adminDetailsArea.setText("");
                setStatus("Loaded " + recentOrders.size() + " recent orders");
            }
        });
    }

    private void showSelectedRecentOrder() {
        if (!adminLoggedIn) {
            return;
        }

        int row = selectedModelRow(recentOrderTable);
        if (row < 0 || row >= recentOrders.size()) {
            return;
        }
        loadTicketDetails(recentOrders.get(row).getId(), adminDetailsArea);
    }

    private void showEditOrderDialog() {
        if (!adminLoggedIn) {
            showInfo("Admin login is required.");
            return;
        }

        int row = selectedModelRow(recentOrderTable);
        if (row < 0 || row >= recentOrders.size()) {
            showInfo("Select an order first.");
            return;
        }

        final OrderTicket existing = recentOrders.get(row);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JSpinner tableSpinner = new JSpinner(new SpinnerNumberModel(existing.getTableNumber(), 1, 999, 1));
        JComboBox<OrderStatus> statusCombo = new JComboBox<OrderStatus>(OrderStatus.values());
        statusCombo.setSelectedItem(existing.getStatus());
        JTextField subtotalField = new JTextField(existing.getSubtotal().toPlainString(), 10);
        JTextArea notesArea = new JTextArea(existing.getHealthNotes() == null ? "" : existing.getHealthNotes(), 5, 28);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        addFormRow(panel, gbc, 0, 0, "Table", tableSpinner);
        addFormRow(panel, gbc, 1, 0, "Status", statusCombo);
        addFormRow(panel, gbc, 2, 0, "Total", subtotalField);
        addFormRow(panel, gbc, 3, 0, "Notes", new JScrollPane(notesArea));

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Order #" + existing.getId(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        final int tableNumber = ((Number) tableSpinner.getValue()).intValue();
        final OrderStatus status = (OrderStatus) statusCombo.getSelectedItem();
        final BigDecimal subtotal;
        final String notes = notesArea.getText().trim();
        try {
            subtotal = new BigDecimal(subtotalField.getText().trim());
            if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
                showInfo("Total must not be negative.");
                return;
            }
        } catch (NumberFormatException ex) {
            showInfo("Enter a valid total, for example 32.00.");
            return;
        }

        runDatabaseTask("Updating order...", new DatabaseTask<Void>() {
            @Override
            public Void run() throws Exception {
                repository.updateOrder(existing.getId(), tableNumber, status, subtotal, notes);
                return null;
            }
        }, new TaskSuccess<Void>() {
            @Override
            public void onSuccess(Void result) {
                setStatus("Order #" + existing.getId() + " updated");
                refreshRecentOrders();
                if (chefLoggedIn) {
                    refreshKitchenOrders();
                }
            }
        });
    }

    private void deleteSelectedOrder() {
        if (!adminLoggedIn) {
            showInfo("Admin login is required.");
            return;
        }

        int row = selectedModelRow(recentOrderTable);
        if (row < 0 || row >= recentOrders.size()) {
            showInfo("Select an order first.");
            return;
        }

        final OrderTicket ticket = recentOrders.get(row);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete order #" + ticket.getId() + " for table " + ticket.getTableNumber() + "?",
                "Delete Order",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        runDatabaseTask("Deleting order...", new DatabaseTask<Void>() {
            @Override
            public Void run() throws Exception {
                repository.deleteOrder(ticket.getId());
                return null;
            }
        }, new TaskSuccess<Void>() {
            @Override
            public void onSuccess(Void result) {
                adminDetailsArea.setText("");
                setStatus("Order #" + ticket.getId() + " deleted");
                refreshRecentOrders();
                if (chefLoggedIn) {
                    refreshKitchenOrders();
                }
            }
        });
    }

    private void loadTicketDetails(final int orderId, final JTextArea target) {
        runDatabaseTask("Loading order details...", new DatabaseTask<Optional<OrderTicket>>() {
            @Override
            public Optional<OrderTicket> run() throws Exception {
                return repository.findTicket(orderId);
            }
        }, new TaskSuccess<Optional<OrderTicket>>() {
            @Override
            public void onSuccess(Optional<OrderTicket> ticket) {
                if (ticket.isPresent()) {
                    target.setText(formatTicket(ticket.get()));
                    target.setCaretPosition(0);
                    setStatus("Loaded order #" + orderId);
                } else {
                    target.setText("Order not found.");
                }
            }
        });
    }

    private String formatTicket(OrderTicket ticket) {
        StringBuilder text = new StringBuilder();
        text.append("Order #").append(ticket.getId()).append('\n');
        text.append("Table: ").append(ticket.getTableNumber()).append('\n');
        text.append("Type: ").append(ticket.getOrderType().getDisplayName()).append('\n');
        text.append("Status: ").append(ticket.getStatus().getDisplayName()).append('\n');
        text.append("Total: ").append(ticket.getSubtotal().toPlainString()).append(" LYD").append('\n');
        if (ticket.getCreatedAt() != null) {
            text.append("Created: ").append(DATE_TIME_FORMAT.format(ticket.getCreatedAt())).append('\n');
        }
        text.append(String.format(Locale.US,
                "Nutrition: %.1f calories | P %.1fg | C %.1fg | F %.1fg%n",
                ticket.getCalories(),
                ticket.getProteinGrams(),
                ticket.getCarbsGrams(),
                ticket.getFatGrams()));
        text.append('\n').append("Items:").append('\n');
        for (String line : ticket.getLines()) {
            text.append(line).append('\n');
        }
        if (ticket.getHealthNotes() != null && !ticket.getHealthNotes().trim().isEmpty()) {
            text.append('\n').append("Health notes:").append('\n');
            text.append(ticket.getHealthNotes()).append('\n');
        }
        return text.toString();
    }

    private Object[] availableIngredientRow(Ingredient ingredient) {
        return new Object[]{
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getCategory().getDisplayName(),
                ingredient.getServingLabel(),
                formatDouble(ingredient.getCalories()),
                formatDouble(ingredient.getProteinGrams()),
                formatDouble(ingredient.getCarbsGrams()),
                formatDouble(ingredient.getFatGrams()),
                ingredient.getPrice().toPlainString()
        };
    }

    private Object[] ticketRow(OrderTicket ticket) {
        return new Object[]{
                ticket.getId(),
                ticket.getTableNumber(),
                ticket.getOrderType().getDisplayName(),
                ticket.getStatus().getDisplayName(),
                formatDouble(ticket.getCalories()),
                ticket.getSubtotal().toPlainString(),
                ticket.getCreatedAt() == null ? "" : DATE_TIME_FORMAT.format(ticket.getCreatedAt())
        };
    }

    private JTable createTable(ReadOnlyTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    private JTextArea detailsArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(8, 8, 8, 8));
        return area;
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            int column,
            String label,
            Component component) {
        addFormRow(panel, gbc, row, column, label, component, 1);
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            int column,
            String label,
            Component component,
            int componentWidth) {
        gbc.gridy = row;
        gbc.gridx = column;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label, SwingConstants.RIGHT), gbc);

        gbc.gridx = column + 1;
        gbc.weightx = 1;
        gbc.gridwidth = componentWidth;
        panel.add(component, gbc);

        gbc.gridwidth = 1;
    }

    private JSpinner integerSpinner(int value, int min, int max, int step) {
        return new JSpinner(new SpinnerNumberModel(value, min, max, step));
    }

    private JSpinner decimalSpinner(double value, double min, double max, double step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.0"));
        return spinner;
    }

    private int intValue(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    private double doubleValue(JSpinner spinner) {
        return ((Number) spinner.getValue()).doubleValue();
    }

    private int selectedModelRow(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return -1;
        }
        return table.convertRowIndexToModel(row);
    }

    private DiningTableAccount selectedCustomerTableAccount() {
        return fixedTableAccount;
    }

    private int selectedCustomerTableNumber() {
        return fixedTableAccount == null ? 1 : fixedTableAccount.getTableNumber();
    }

    private void installGoalRenderer(JComboBox<Goal> comboBox) {
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Goal) {
                    setText(((Goal) value).getDisplayName());
                }
                return component;
            }
        });
    }

    private void installCategoryRenderer(JComboBox<IngredientCategory> comboBox) {
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof IngredientCategory) {
                    setText(((IngredientCategory) value).getDisplayName());
                }
                return component;
            }
        });
    }

    private void setChefControlsEnabled(boolean enabled) {
        chefRefreshButton.setEnabled(enabled);
        chefPreparingButton.setEnabled(enabled);
        chefReadyButton.setEnabled(enabled);
        chefCompletedButton.setEnabled(enabled);
        chefCancelledButton.setEnabled(enabled);
        chefOrderTable.setEnabled(enabled);
    }

    private void setAdminControlsEnabled(boolean enabled) {
        adminRefreshIngredientsButton.setEnabled(enabled);
        adminAddIngredientButton.setEnabled(enabled);
        adminEditIngredientButton.setEnabled(enabled);
        adminDeleteIngredientButton.setEnabled(enabled);
        adminToggleIngredientButton.setEnabled(enabled);
        adminRefreshOrdersButton.setEnabled(enabled);
        adminEditOrderButton.setEnabled(enabled);
        adminDeleteOrderButton.setEnabled(enabled);
        adminIngredientTable.setEnabled(enabled);
        recentOrderTable.setEnabled(enabled);
        if (adminRefreshAccountsButton != null) {
            adminRefreshAccountsButton.setEnabled(enabled);
            adminAddStaffButton.setEnabled(enabled);
            adminEditStaffButton.setEnabled(enabled);
            adminToggleStaffButton.setEnabled(enabled);
            adminEditTableButton.setEnabled(enabled);
            adminStaffTable.setEnabled(enabled);
            adminTableAccountTable.setEnabled(enabled);
        }
    }

    private <T> void runDatabaseTask(
            final String message,
            final DatabaseTask<T> task,
            final TaskSuccess<T> success) {
        setBusy(message);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.run();
            }

            @Override
            protected void done() {
                clearBusy();
                try {
                    success.onSuccess(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("The operation was interrupted.");
                } catch (ExecutionException ex) {
                    showException(ex.getCause());
                }
            }
        }.execute();
    }

    private void setBusy(String message) {
        setStatus(message);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void clearBusy() {
        setCursor(Cursor.getDefaultCursor());
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        setStatus("Operation failed");
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showException(Throwable throwable) {
        String message = throwable == null ? "Unknown error" : throwable.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = throwable.getClass().getSimpleName();
        }

        showError("MySQL operation failed:\n"
                + message
                + "\n\nCheck that MySQL is running on 127.0.0.1:3306 and database/schema.sql + database/seed.sql were imported.");
    }

    private String joinLines(List<String> lines) {
        StringBuilder text = new StringBuilder();
        for (String line : lines) {
            if (text.length() > 0) {
                text.append('\n');
            }
            text.append(line);
        }
        return text.toString();
    }

    private String formatDouble(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private interface DatabaseTask<T> {
        T run() throws Exception;
    }

    private interface TaskSuccess<T> {
        void onSuccess(T result);
    }

    private static class AccountLists {
        private final List<StaffAccount> staffAccounts;
        private final List<DiningTableAccount> tableAccounts;

        AccountLists(List<StaffAccount> staffAccounts, List<DiningTableAccount> tableAccounts) {
            this.staffAccounts = staffAccounts;
            this.tableAccounts = tableAccounts;
        }
    }

    private static class ReadOnlyTableModel extends DefaultTableModel {
        ReadOnlyTableModel(Object[] columns) {
            super(columns, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
