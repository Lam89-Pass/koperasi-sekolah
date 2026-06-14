package ui;

import model.User;
import util.IconUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    private DashboardPanel dashboardPanel;
    private StudentPanel studentPanel;
    private SavingsPanel savingsPanel;
    private ShopPanel shopPanel;
    private ProductPanel productPanel;
    private SettingsPanel settingsPanel;

    private JPanel sidebar;
    private JLabel lblPageTitle;
    private JLabel lblPageSubtitle;
    
    private JButton btnDashboard;
    private JButton btnStudent;
    private JButton btnSavings;
    private JButton btnShop;
    private JButton btnProduct;
    private JButton btnSettings;
    private JButton activeButton;

    private final Color sidebarBg = new Color(21, 34, 56);
    private final Color contentBg = new Color(248, 250, 252);
    private final Color activeBtnBg = new Color(29, 78, 216);
    private final Color inactiveBtnBg = new Color(21, 34, 56);
    private final Color headerBg = Color.WHITE;
    
    private User loggedInUser;

    public MainFrame(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        setTitle("Sistem Informasi Koperasi Sekolah");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLayout(new BorderLayout());

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.add(createTopHeader(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(contentBg);
        rightWrapper.add(contentPanel, BorderLayout.CENTER);
        
        add(rightWrapper, BorderLayout.CENTER);

        dashboardPanel = new DashboardPanel(this);
        studentPanel = new StudentPanel();
        savingsPanel = new SavingsPanel();
        shopPanel = new ShopPanel();
        productPanel = new ProductPanel();
        settingsPanel = new SettingsPanel();

        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(studentPanel, "STUDENTS");
        contentPanel.add(savingsPanel, "SAVINGS");
        contentPanel.add(shopPanel, "SHOP");
        contentPanel.add(productPanel, "PRODUCTS");
        contentPanel.add(settingsPanel, "SETTINGS");

        showPanel("DASHBOARD", btnDashboard);
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(headerBg);
        header.setPreferredSize(new Dimension(getWidth(), 90));
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel leftHeader = new JPanel(new GridLayout(2, 1, 0, 5));
        leftHeader.setOpaque(false);

        lblPageTitle = new JLabel("Selamat datang, " + loggedInUser.getUsername());
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblPageTitle.setForeground(new Color(30, 41, 59));

        lblPageSubtitle = new JLabel("Berikut ringkasan aktivitas dan performa koperasi sekolah");
        lblPageSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPageSubtitle.setForeground(new Color(100, 116, 139));

        leftHeader.add(lblPageTitle);
        leftHeader.add(lblPageSubtitle);
        header.add(leftHeader, BorderLayout.WEST);

        // Profile section (clickable with logout popup)
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        JPanel profileInfo = new JPanel(new GridLayout(2, 1));
        profileInfo.setOpaque(false);
        JLabel lblProfileName = new JLabel(loggedInUser.getUsername());
        lblProfileName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblProfileName.setForeground(new Color(30, 41, 59));
        lblProfileName.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JLabel lblProfileRole = new JLabel(loggedInUser.getRole());
        lblProfileRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblProfileRole.setForeground(new Color(100, 116, 139));
        lblProfileRole.setHorizontalAlignment(SwingConstants.RIGHT);
        
        profileInfo.add(lblProfileName);
        profileInfo.add(lblProfileRole);

        JLabel lblAvatar = new JLabel(IconUtils.getIcon("avatar", 36, new Color(59, 130, 246)));

        JLabel lblArrow = new JLabel(IconUtils.getIcon("arrow-down", 14, new Color(148, 163, 184)));

        // Create popup menu for logout
        JPopupMenu profileMenu = new JPopupMenu();
        profileMenu.setBackground(Color.WHITE);
        profileMenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JMenuItem menuLogout = new JMenuItem("Keluar dari Sistem");
        menuLogout.setIcon(IconUtils.getIcon("logout", 18, new Color(239, 68, 68)));
        menuLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        menuLogout.setForeground(new Color(239, 68, 68));
        menuLogout.setBackground(Color.WHITE);
        menuLogout.setOpaque(true);
        menuLogout.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 25));
        menuLogout.addChangeListener(e -> {
            if (menuLogout.isArmed()) {
                menuLogout.setBackground(new Color(254, 242, 242));
            } else {
                menuLogout.setBackground(Color.WHITE);
            }
        });
        menuLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Apakah Anda yakin ingin keluar?", 
                "Konfirmasi Logout", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            }
        });
        profileMenu.add(menuLogout);

        // Make profile area clickable
        JPanel profileClickable = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5)) {
            private boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                if (isHovered) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        profileClickable.setOpaque(false);
        profileClickable.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileClickable.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 10));
        profileClickable.add(profileInfo);
        profileClickable.add(lblAvatar);
        profileClickable.add(lblArrow);
        profileClickable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                profileMenu.show(profileClickable, profileClickable.getWidth() - profileMenu.getPreferredSize().width, profileClickable.getHeight() + 5);
            }
        });

        rightHeader.add(profileClickable);
        header.add(rightHeader, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setBackground(sidebarBg);
        panel.setPreferredSize(new Dimension(240, getHeight()));
        panel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 0));
        
        // Koperasi icon using a proper cooperation symbol
        JLabel brandIcon = new JLabel(IconUtils.getIcon("koperasi", 38, new Color(46, 204, 113)));
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        
        JLabel brandTitle = new JLabel("KOPERASI");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandTitle.setForeground(Color.WHITE);
        
        JLabel brandSubtitle = new JLabel("SEKOLAH");
        brandSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brandSubtitle.setForeground(new Color(46, 204, 113));
        
        titlePanel.add(brandTitle);
        titlePanel.add(brandSubtitle);
        
        headerPanel.add(brandIcon);
        headerPanel.add(titlePanel);

        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnDashboard = createNavButton("Dashboard", "dashboard");
        btnStudent = createNavButton("Manajemen Siswa", "users");
        btnSavings = createNavButton("Simpan & Tarik", "wallet");
        btnShop = createNavButton("Toko Koperasi", "shop");
        btnProduct = createNavButton("Kelola Barang", "box");
        btnSettings = createNavButton("Pengaturan", "settings");
        if (loggedInUser.getRole().equals("KASIR")) {
            btnSettings.setVisible(false);
        }

        btnDashboard.addActionListener(e -> showPanel("DASHBOARD", btnDashboard));
        btnStudent.addActionListener(e -> showPanel("STUDENTS", btnStudent));
        btnSavings.addActionListener(e -> showPanel("SAVINGS", btnSavings));
        btnShop.addActionListener(e -> showPanel("SHOP", btnShop));
        btnProduct.addActionListener(e -> showPanel("PRODUCTS", btnProduct));
        btnSettings.addActionListener(e -> showPanel("SETTINGS", btnSettings));

        menuPanel.add(btnDashboard);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        menuPanel.add(btnStudent);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        menuPanel.add(btnSavings);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        menuPanel.add(btnShop);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        menuPanel.add(btnProduct);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        menuPanel.add(btnSettings);

        panel.add(menuPanel, BorderLayout.CENTER);

        // Footer: only version label
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblVersion = new JLabel("Versi 1.0.0");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVersion.setForeground(new Color(148, 163, 184));
        JLabel lblShield = new JLabel(IconUtils.getIcon("shield", 18, new Color(46, 204, 113)));
        footerPanel.add(lblVersion, BorderLayout.WEST);
        footerPanel.add(lblShield, BorderLayout.EAST);

        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createNavButton(String text, String iconType) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getBackground().equals(activeBtnBg)) {
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setIcon(IconUtils.getIcon(iconType, 18, Color.WHITE));
        btn.setIconTextGap(15);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(inactiveBtnBg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        return btn;
    }

    public void showPanel(String panelName, JButton sourceButton) {
        cardLayout.show(contentPanel, panelName);

        if (activeButton != null) {
            activeButton.setBackground(inactiveBtnBg);
            activeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        sourceButton.setBackground(activeBtnBg);
        sourceButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        activeButton = sourceButton;

        switch (panelName) {
            case "DASHBOARD":
                lblPageTitle.setText("Selamat datang, Admin");
                lblPageSubtitle.setText("Berikut ringkasan aktivitas dan performa koperasi sekolah");
                if (dashboardPanel != null) dashboardPanel.refreshData();
                break;
            case "STUDENTS":
                lblPageTitle.setText("Manajemen Siswa");
                lblPageSubtitle.setText("Kelola data anggota siswa koperasi");
                if (studentPanel != null) studentPanel.loadData();
                break;
            case "SAVINGS":
                lblPageTitle.setText("Simpan & Tarik Tabungan");
                lblPageSubtitle.setText("Layanan transaksi tabungan siswa");
                if (savingsPanel != null) {
                    savingsPanel.loadStudents();
                    savingsPanel.loadTransactions();
                }
                break;
            case "SHOP":
                lblPageTitle.setText("Toko Koperasi");
                lblPageSubtitle.setText("Sistem kasir dan transaksi pembelian barang");
                if (shopPanel != null) {
                    shopPanel.loadProducts();
                    shopPanel.loadStudents();
                    shopPanel.loadShopHistory();
                }
                break;
            case "PRODUCTS":
                lblPageTitle.setText("Kelola Barang");
                lblPageSubtitle.setText("Manajemen stok dan harga barang toko");
                if (productPanel != null) productPanel.loadProducts();
                break;
            case "SETTINGS":
                lblPageTitle.setText("Pengaturan Sistem");
                lblPageSubtitle.setText("Konfigurasi akun dan sistem koperasi");
                break;
        }
    }

    public void navigateToShop() {
        showPanel("SHOP", btnShop);
    }

    public void navigateToSavings() {
        showPanel("SAVINGS", btnSavings);
    }

    public void navigateToStudents() {
        showPanel("STUDENTS", btnStudent);
    }

    public void navigateToProducts() {
        showPanel("PRODUCTS", btnProduct);
    }
}
