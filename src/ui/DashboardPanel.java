package ui;

import util.IconUtils;
import util.DBHelper;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DashboardPanel extends JPanel {
    private MainFrame mainFrame;
    
    private JLabel lblTotalMembers;
    private JLabel lblTotalSavings;
    private JLabel lblTotalSales;
    private JLabel lblTotalTransactions;

    private JLabel lblSubMembers;
    private JLabel lblSubSavings;
    private JLabel lblSubSales;
    private JLabel lblSubTransactions;

    private JTable tblRecentActivities;
    private DefaultTableModel tableModel;

    // Pagination
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int ROWS_PER_PAGE = 10;
    private JPanel paginationPanel;
    private List<Object[]> allTransactions = new ArrayList<>();

    // Chart data (dynamic)
    private double[] chartTabunganData = new double[12];
    private double[] chartPenarikanData = new double[12];
    private double[] chartTransaksiData = new double[12];
    private String[] chartLabels = new String[12];
    private double chartMaxVal = 1;
    private JPanel chartBody;
    private JComboBox<String> comboChartPeriod;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));

        JPanel topGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        topGrid.setOpaque(false);

        ModernCard cardMembers = createStatCard("<html>TOTAL ANGGOTA<br>SISWA</html>", "0 Siswa", "users", new Color(224, 242, 254), new Color(2, 132, 199));
        lblTotalMembers = (JLabel) cardMembers.getClientProperty("valueLabel");
        lblSubMembers = (JLabel) cardMembers.getClientProperty("subLabel");
        
        ModernCard cardSavings = createStatCard("<html>TOTAL KAS<br>TABUNGAN</html>", "Rp0", "wallet", new Color(220, 252, 231), new Color(22, 163, 74));
        lblTotalSavings = (JLabel) cardSavings.getClientProperty("valueLabel");
        lblSubSavings = (JLabel) cardSavings.getClientProperty("subLabel");

        ModernCard cardSales = createStatCard("<html>OMZET TOKO<br>KOPERASI</html>", "Rp0", "shop", new Color(255, 237, 213), new Color(234, 88, 12));
        lblTotalSales = (JLabel) cardSales.getClientProperty("valueLabel");
        lblSubSales = (JLabel) cardSales.getClientProperty("subLabel");

        ModernCard cardTrans = createStatCard("<html>TOTAL<br>TRANSAKSI</html>", "0", "chart", new Color(243, 232, 255), new Color(147, 51, 234));
        lblTotalTransactions = (JLabel) cardTrans.getClientProperty("valueLabel");
        lblSubTransactions = (JLabel) cardTrans.getClientProperty("subLabel");

        topGrid.add(cardMembers);
        topGrid.add(cardSavings);
        topGrid.add(cardSales);
        topGrid.add(cardTrans);
        
        add(topGrid, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(20, 0));
        centerPanel.setOpaque(false);

        // Activity Table Panel
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBackground(Color.WHITE);
        activityPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel activityHeader = new JPanel(new BorderLayout());
        activityHeader.setOpaque(false);
        JLabel activityTitle = new JLabel("Aktivitas Transaksi Terakhir");
        activityTitle.setIcon(IconUtils.getIcon("clock", 16, new Color(100, 116, 139)));
        activityTitle.setIconTextGap(10);
        activityTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        activityTitle.setForeground(new Color(30, 41, 59));
        
        activityHeader.add(activityTitle, BorderLayout.WEST);
        activityHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        activityPanel.add(activityHeader, BorderLayout.NORTH);

        String[] columns = {"", "Tanggal & Waktu", "Tipe Transaksi", "Detail Aktivitas", "Nominal"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblRecentActivities = new JTable(tableModel);
        tblRecentActivities.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblRecentActivities.setRowHeight(45);
        tblRecentActivities.setShowGrid(false);
        tblRecentActivities.setIntercellSpacing(new Dimension(0, 0));
        tblRecentActivities.setSelectionBackground(new Color(241, 245, 249));
        tblRecentActivities.setSelectionForeground(new Color(30, 41, 59));

        tblRecentActivities.getColumnModel().getColumn(0).setMaxWidth(40);
        tblRecentActivities.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblRecentActivities.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblRecentActivities.getColumnModel().getColumn(3).setPreferredWidth(180);

        tblRecentActivities.setDefaultRenderer(Object.class, new ActivityCellRenderer());

        tblRecentActivities.getTableHeader().setBackground(Color.WHITE);
        tblRecentActivities.getTableHeader().setForeground(new Color(100, 116, 139));
        tblRecentActivities.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblRecentActivities.getTableHeader().setPreferredSize(new Dimension(100, 40));
        tblRecentActivities.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        tblRecentActivities.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(tblRecentActivities);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        activityPanel.add(scrollPane, BorderLayout.CENTER);

        // Pagination panel (dynamic)
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        paginationPanel.setOpaque(false);
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        activityPanel.add(paginationPanel, BorderLayout.SOUTH);

        centerPanel.add(activityPanel, BorderLayout.CENTER);

        // Right panel: Quick Actions + Chart
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(280, getHeight()));

        JPanel quickActionPanel = new JPanel(new BorderLayout());
        quickActionPanel.setOpaque(false);
        
        JLabel actionsTitle = new JLabel("Aksi Cepat");
        actionsTitle.setIcon(IconUtils.getIcon("dashboard", 16, new Color(30, 41, 59)));
        actionsTitle.setIconTextGap(10);
        actionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        actionsTitle.setForeground(new Color(30, 41, 59));
        actionsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        quickActionPanel.add(actionsTitle, BorderLayout.NORTH);

        JPanel btnContainer = new JPanel(new GridLayout(4, 1, 0, 10));
        btnContainer.setOpaque(false);

        btnContainer.add(createActionBtn("Buka Toko Koperasi", "shop", new Color(34, 197, 94), e -> mainFrame.navigateToShop()));
        btnContainer.add(createActionBtn("Simpan / Tarik Tabungan", "wallet", new Color(59, 130, 246), e -> mainFrame.navigateToSavings()));
        btnContainer.add(createActionBtn("Registrasi Anggota", "users", new Color(168, 85, 247), e -> mainFrame.navigateToStudents()));
        btnContainer.add(createActionBtn("Kelola Barang", "box", new Color(249, 115, 22), e -> mainFrame.navigateToProducts()));

        quickActionPanel.add(btnContainer, BorderLayout.CENTER);
        rightPanel.add(quickActionPanel);

        // Chart card
        JPanel chartCard = new JPanel(new BorderLayout());
        chartCard.setBackground(Color.WHITE);
        chartCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel chartHeader = new JPanel(new BorderLayout());
        chartHeader.setOpaque(false);
        JLabel chartTitle = new JLabel("Ringkasan Grafik");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chartTitle.setForeground(new Color(30, 41, 59));
        
        // Build dynamic month selector
        comboChartPeriod = new JComboBox<>();
        comboChartPeriod.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        comboChartPeriod.setBackground(Color.WHITE);
        comboChartPeriod.setPreferredSize(new Dimension(130, 28));
        populateChartPeriods();
        comboChartPeriod.addActionListener(e -> {
            loadChartData();
            if (chartBody != null) chartBody.repaint();
        });
        
        chartHeader.add(chartTitle, BorderLayout.WEST);
        chartHeader.add(comboChartPeriod, BorderLayout.EAST);
        chartCard.add(chartHeader, BorderLayout.NORTH);

        chartBody = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight() - 40;
                if (h <= 0 || w <= 0) return;
                
                // Grid lines
                g2.setColor(new Color(241, 245, 249));
                g2.setStroke(new BasicStroke(1f));
                for (int i = 0; i <= 4; i++) {
                    int y = h - (i * h / 4);
                    g2.drawLine(30, y, w, y);
                    g2.setColor(new Color(148, 163, 184));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    String label = formatChartValue(chartMaxVal * i / 4);
                    g2.drawString(label, 0, y + 4);
                    g2.setColor(new Color(241, 245, 249));
                }

                // X-axis labels
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                int numPoints = chartLabels.length;
                int labelStep = Math.max(1, numPoints / 6);
                for (int i = 0; i < numPoints; i += labelStep) {
                    if (chartLabels[i] != null) {
                        int x = 30 + (i * (w - 30) / Math.max(1, numPoints - 1));
                        g2.drawString(chartLabels[i], x - 8, h + 15);
                    }
                }

                drawLineChart(g2, chartTabunganData, new Color(34, 197, 94), w, h);
                drawLineChart(g2, chartPenarikanData, new Color(59, 130, 246), w, h);
                drawLineChart(g2, chartTransaksiData, new Color(249, 115, 22), w, h);
            }
        };
        chartBody.setOpaque(false);
        chartCard.add(chartBody, BorderLayout.CENTER);

        JPanel chartLegend = new JPanel(new GridLayout(1, 3));
        chartLegend.setOpaque(false);
        chartLegend.add(createLegendDot("Tabungan", new Color(34, 197, 94)));
        chartLegend.add(createLegendDot("Penarikan", new Color(59, 130, 246)));
        chartLegend.add(createLegendDot("Toko", new Color(249, 115, 22)));
        chartCard.add(chartLegend, BorderLayout.SOUTH);

        rightPanel.add(chartCard);

        centerPanel.add(rightPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        refreshData();
    }

    private void populateChartPeriods() {
        comboChartPeriod.removeAllItems();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        for (int i = 0; i < 12; i++) {
            LocalDate d = now.minusMonths(i);
            String label = d.format(formatter);
            // Capitalize first letter
            label = label.substring(0, 1).toUpperCase() + label.substring(1);
            comboChartPeriod.addItem(label);
        }
    }

    private void drawLineChart(Graphics2D g2, double[] data, Color color, int w, int h) {
        if (chartMaxVal <= 0) return;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        int points = data.length;
        for (int i = 0; i < points - 1; i++) {
            int x1 = 30 + (i * (w - 30) / Math.max(1, points - 1));
            int y1 = h - (int)(data[i] * h / chartMaxVal);
            int x2 = 30 + ((i + 1) * (w - 30) / Math.max(1, points - 1));
            int y2 = h - (int)(data[i+1] * h / chartMaxVal);
            g2.drawLine(x1, y1, x2, y2);
            g2.fillOval(x1 - 3, y1 - 3, 6, 6);
            if (i == points - 2) {
                g2.fillOval(x2 - 3, y2 - 3, 6, 6);
            }
        }
    }

    private String formatChartValue(double val) {
        if (val >= 1_000_000) return String.format("%.0fM", val / 1_000_000);
        if (val >= 1_000) return String.format("%.0fK", val / 1_000);
        return String.format("%.0f", val);
    }

    private JPanel createLegendDot(String label, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 2, 8, 8);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(10, 12));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l.setForeground(new Color(100, 116, 139));
        p.add(dot);
        p.add(l);
        return p;
    }

    private JButton createActionBtn(String text, String iconType, Color bg, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString(">", getWidth() - 20, getHeight() / 2 + 5);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setIcon(IconUtils.getIcon(iconType, 16, Color.WHITE));
        btn.setIconTextGap(10);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 30));
        btn.addActionListener(action);
        return btn;
    }

    private ModernCard createStatCard(String title, String val, String iconType, Color iconBg, Color iconFg) {
        ModernCard card = new ModernCard(15);
        card.setLayout(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 10)
        ));

        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(46, 46));
        iconPanel.setLayout(new GridBagLayout());
        JLabel lblIcon = new JLabel(IconUtils.getIcon(iconType, 20, iconFg));
        iconPanel.add(lblIcon);
        
        card.add(iconPanel, BorderLayout.WEST);

        JPanel textContainer = new JPanel(new GridLayout(3, 1, 0, 2));
        textContainer.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLabel.setForeground(new Color(148, 163, 184));

        JLabel valueLabel = new JLabel(val);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(new Color(30, 41, 59));

        JLabel subLabel = new JLabel("-- ");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLabel.setForeground(new Color(34, 197, 94));

        textContainer.add(titleLabel);
        textContainer.add(valueLabel);
        textContainer.add(subLabel);
        
        card.add(textContainer, BorderLayout.CENTER);
        
        card.putClientProperty("valueLabel", valueLabel);
        card.putClientProperty("subLabel", subLabel);

        return card;
    }

    // =================== DATA LOADING ===================

    public void refreshData() {
        loadStatCards();
        loadAllTransactions();
        loadChartData();
        if (chartBody != null) chartBody.repaint();
    }

    private void loadStatCards() {
        try (Connection conn = DBHelper.getConnection()) {
            LocalDate now = LocalDate.now();
            LocalDate startThisMonth = now.withDayOfMonth(1);
            LocalDate startLastMonth = startThisMonth.minusMonths(1);

            // TOTAL ANGGOTA
            int totalMembers = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM siswa")) {
                if (rs.next()) totalMembers = rs.getInt(1);
            }
            lblTotalMembers.setText(totalMembers + " Siswa");

            // Count members registered this month vs last month (approximate with ID)
            int membersThisMonth = 0, membersLastMonth = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM siswa WHERE id > (SELECT IFNULL(MAX(id),0) - ? FROM siswa)")) {
                // Approximate: just show total
                ps.setInt(1, totalMembers);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) membersThisMonth = rs.getInt(1);
                }
            }
            lblSubMembers.setText(totalMembers + " siswa terdaftar");
            lblSubMembers.setForeground(new Color(34, 197, 94));

            // TOTAL KAS TABUNGAN
            double totalSavingsNow = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT IFNULL(SUM(saldo_tabungan),0) FROM siswa")) {
                if (rs.next()) totalSavingsNow = rs.getDouble(1);
            }
            lblTotalSavings.setText("Rp " + String.format("%,.0f", totalSavingsNow).replace(',', '.'));

            // Savings change: sum deposits this month vs last month
            double savingsThisMonth = getSumForPeriod(conn, "SELECT IFNULL(SUM(jumlah),0) FROM transaksi_tabungan WHERE tanggal >= ?", startThisMonth);
            double savingsLastMonth = getSumForPeriod(conn, "SELECT IFNULL(SUM(jumlah),0) FROM transaksi_tabungan WHERE tanggal >= ? AND tanggal < ?", startLastMonth, startThisMonth);
            setPercentageLabel(lblSubSavings, savingsThisMonth, savingsLastMonth);

            // OMZET TOKO
            double totalSalesAll = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT IFNULL(SUM(total_harga),0) FROM transaksi_toko")) {
                if (rs.next()) totalSalesAll = rs.getDouble(1);
            }
            lblTotalSales.setText("Rp " + String.format("%,.0f", totalSalesAll).replace(',', '.'));

            double salesThisMonth = getSumForPeriod(conn, "SELECT IFNULL(SUM(total_harga),0) FROM transaksi_toko WHERE tanggal >= ?", startThisMonth);
            double salesLastMonth = getSumForPeriod(conn, "SELECT IFNULL(SUM(total_harga),0) FROM transaksi_toko WHERE tanggal >= ? AND tanggal < ?", startLastMonth, startThisMonth);
            setPercentageLabel(lblSubSales, salesThisMonth, salesLastMonth);

            // TOTAL TRANSAKSI
            int totalTransAll = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM (SELECT id FROM transaksi_tabungan UNION ALL SELECT id FROM transaksi_toko) as t")) {
                if (rs.next()) totalTransAll = rs.getInt(1);
            }
            lblTotalTransactions.setText(String.valueOf(totalTransAll));

            double transThisMonth = getSumForPeriod(conn, "SELECT COUNT(*) FROM (SELECT id FROM transaksi_tabungan WHERE tanggal >= ? UNION ALL SELECT id FROM transaksi_toko WHERE tanggal >= ?) as t", startThisMonth, startThisMonth);
            double transLastMonth = getSumForPeriod(conn, "SELECT COUNT(*) FROM (SELECT id FROM transaksi_tabungan WHERE tanggal >= ? AND tanggal < ? UNION ALL SELECT id FROM transaksi_toko WHERE tanggal >= ? AND tanggal < ?) as t", startLastMonth, startThisMonth, startLastMonth, startThisMonth);
            setPercentageLabel(lblSubTransactions, transThisMonth, transLastMonth);

        } catch (Exception e) {
            System.err.println("Gagal memuat statistik dashboard: " + e.getMessage());
        }
    }

    private double getSumForPeriod(Connection conn, String sql, LocalDate... dates) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < dates.length; i++) {
                ps.setString(i + 1, dates[i].toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    private void setPercentageLabel(JLabel label, double current, double previous) {
        if (previous <= 0 && current <= 0) {
            label.setText("-- tidak ada data");
            label.setForeground(new Color(148, 163, 184));
        } else if (previous <= 0) {
            label.setText("+ 100% (baru)");
            label.setForeground(new Color(34, 197, 94));
        } else {
            double pct = ((current - previous) / previous) * 100;
            if (pct >= 0) {
                label.setText("+ " + String.format("%.1f", pct) + "% dr bulan lalu");
                label.setForeground(new Color(34, 197, 94));
            } else {
                label.setText("- " + String.format("%.1f", Math.abs(pct)) + "% dr bulan lalu");
                label.setForeground(new Color(239, 68, 68));
            }
        }
    }

    // =================== TRANSACTION TABLE + PAGINATION ===================

    private void loadAllTransactions() {
        allTransactions.clear();
        try (Connection conn = DBHelper.getConnection()) {
            // Load tabungan transactions with SIMPAN/TARIK detail
            String tabunganQuery = 
                "SELECT t.tanggal, t.jenis_transaksi, s.nama, t.jumlah " +
                "FROM transaksi_tabungan t JOIN siswa s ON t.siswa_id = s.id " +
                "ORDER BY t.tanggal DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(tabunganQuery)) {
                while (rs.next()) {
                    String datetime = rs.getTimestamp("tanggal").toString();
                    String date = datetime.substring(0, 10);
                    String time = datetime.substring(11, 19);
                    String nama = rs.getString("nama");
                    String jenis = rs.getString("jenis_transaksi");
                    double amount = rs.getDouble("jumlah");
                    
                    String detail;
                    if (jenis.equals("SIMPAN")) {
                        detail = nama + " menyimpan tabungan";
                    } else {
                        detail = nama + " menarik tabungan";
                    }
                    
                    allTransactions.add(new Object[]{
                        "TABUNGAN_" + jenis,
                        "<html>" + date + "<br><span style='color:gray'>" + time + "</span></html>",
                        "TABUNGAN",
                        detail,
                        amount
                    });
                }
            }

            // Load toko transactions with item names
            String tokoQuery = 
                "SELECT t.tanggal, IFNULL(s.nama, 'Umum') AS nama, t.total_harga, " +
                "GROUP_CONCAT(b.nama_barang SEPARATOR ', ') AS barang_list " +
                "FROM transaksi_toko t " +
                "LEFT JOIN siswa s ON t.siswa_id = s.id " +
                "LEFT JOIN detail_transaksi_toko d ON d.transaksi_toko_id = t.id " +
                "LEFT JOIN barang b ON d.barang_id = b.id " +
                "GROUP BY t.id, t.tanggal, s.nama, t.total_harga " +
                "ORDER BY t.tanggal DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(tokoQuery)) {
                while (rs.next()) {
                    String datetime = rs.getTimestamp("tanggal").toString();
                    String date = datetime.substring(0, 10);
                    String time = datetime.substring(11, 19);
                    String nama = rs.getString("nama");
                    double amount = rs.getDouble("total_harga");
                    String barangList = rs.getString("barang_list");
                    
                    String detail;
                    if (barangList != null && !barangList.isEmpty()) {
                        detail = nama + " beli " + barangList;
                    } else {
                        detail = nama + " belanja di toko";
                    }
                    
                    allTransactions.add(new Object[]{
                        "TOKO",
                        "<html>" + date + "<br><span style='color:gray'>" + time + "</span></html>",
                        "TOKO",
                        detail,
                        amount
                    });
                }
            }

            // Sort all by date descending (newest first)
            allTransactions.sort((a, b) -> {
                String dateA = ((String) a[1]).replaceAll("<[^>]*>", "").trim();
                String dateB = ((String) b[1]).replaceAll("<[^>]*>", "").trim();
                return dateB.compareTo(dateA);
            });

        } catch (Exception e) {
            System.err.println("Gagal memuat transaksi: " + e.getMessage());
        }

        totalPages = Math.max(1, (int) Math.ceil((double) allTransactions.size() / ROWS_PER_PAGE));
        currentPage = 1;
        showPage(currentPage);
    }

    private void showPage(int page) {
        currentPage = page;
        tableModel.setRowCount(0);
        
        int start = (page - 1) * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, allTransactions.size());
        
        for (int i = start; i < end; i++) {
            tableModel.addRow(allTransactions.get(i));
        }

        updatePagination();
    }

    private void updatePagination() {
        paginationPanel.removeAll();

        // Only show pagination if more than ROWS_PER_PAGE
        if (allTransactions.size() <= ROWS_PER_PAGE) {
            paginationPanel.revalidate();
            paginationPanel.repaint();
            return;
        }

        // Previous button
        JButton btnPrev = createPaginationBtn("<");
        btnPrev.setEnabled(currentPage > 1);
        btnPrev.addActionListener(e -> showPage(currentPage - 1));
        paginationPanel.add(btnPrev);

        // Page number buttons
        for (int i = 1; i <= totalPages; i++) {
            final int pageNum = i;
            JButton btnPage = createPaginationBtn(String.valueOf(i));
            if (i == currentPage) {
                btnPage.setBackground(new Color(29, 78, 216));
                btnPage.setForeground(Color.WHITE);
                btnPage.setOpaque(true);
            }
            btnPage.addActionListener(e -> showPage(pageNum));
            paginationPanel.add(btnPage);

            // Show max 5 page buttons around current
            if (totalPages > 7 && i == 3 && currentPage > 4) {
                JLabel dots = new JLabel("...");
                dots.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                dots.setForeground(new Color(100, 116, 139));
                paginationPanel.add(dots);
                i = Math.max(3, currentPage - 1);
            } else if (totalPages > 7 && i == currentPage + 1 && i < totalPages - 1) {
                JLabel dots = new JLabel("...");
                dots.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                dots.setForeground(new Color(100, 116, 139));
                paginationPanel.add(dots);
                i = totalPages - 1;
            }
        }

        // Next button
        JButton btnNext = createPaginationBtn(">");
        btnNext.setEnabled(currentPage < totalPages);
        btnNext.addActionListener(e -> showPage(currentPage + 1));
        paginationPanel.add(btnNext);

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private JButton createPaginationBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(new Color(71, 85, 105));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(36, 30));
        return btn;
    }

    // =================== CHART DATA (DYNAMIC) ===================

    private void loadChartData() {
        int selectedIndex = comboChartPeriod != null ? comboChartPeriod.getSelectedIndex() : 0;
        LocalDate now = LocalDate.now().minusMonths(selectedIndex);
        int year = now.getYear();
        int month = now.getMonthValue();
        int daysInMonth = now.lengthOfMonth();

        // We'll split the month into ~10 segments
        int segments = Math.min(daysInMonth, 10);
        int daysPerSegment = daysInMonth / segments;
        
        chartTabunganData = new double[segments];
        chartPenarikanData = new double[segments];
        chartTransaksiData = new double[segments];
        chartLabels = new String[segments];
        chartMaxVal = 1;

        try (Connection conn = DBHelper.getConnection()) {
            for (int i = 0; i < segments; i++) {
                int startDay = i * daysPerSegment + 1;
                int endDay = (i == segments - 1) ? daysInMonth : (i + 1) * daysPerSegment;
                
                String startDate = String.format("%d-%02d-%02d", year, month, startDay);
                String endDateExcl = String.format("%d-%02d-%02d", year, month, Math.min(endDay + 1, daysInMonth));
                if (endDay == daysInMonth) {
                    // For the last segment, use the first day of next month
                    LocalDate nextMonth = LocalDate.of(year, month, 1).plusMonths(1);
                    endDateExcl = nextMonth.toString();
                }
                
                chartLabels[i] = String.valueOf(startDay);

                // Tabungan deposits
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT IFNULL(SUM(jumlah),0) FROM transaksi_tabungan WHERE jenis_transaksi='SIMPAN' AND tanggal >= ? AND tanggal < ?")) {
                    ps.setString(1, startDate);
                    ps.setString(2, endDateExcl);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) chartTabunganData[i] = rs.getDouble(1);
                    }
                }

                // Penarikan withdrawals
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT IFNULL(SUM(jumlah),0) FROM transaksi_tabungan WHERE jenis_transaksi='TARIK' AND tanggal >= ? AND tanggal < ?")) {
                    ps.setString(1, startDate);
                    ps.setString(2, endDateExcl);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) chartPenarikanData[i] = rs.getDouble(1);
                    }
                }

                // Transaksi toko
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT IFNULL(SUM(total_harga),0) FROM transaksi_toko WHERE tanggal >= ? AND tanggal < ?")) {
                    ps.setString(1, startDate);
                    ps.setString(2, endDateExcl);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) chartTransaksiData[i] = rs.getDouble(1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat data grafik: " + e.getMessage());
        }

        // Find max value for chart scaling
        for (int i = 0; i < segments; i++) {
            chartMaxVal = Math.max(chartMaxVal, chartTabunganData[i]);
            chartMaxVal = Math.max(chartMaxVal, chartPenarikanData[i]);
            chartMaxVal = Math.max(chartMaxVal, chartTransaksiData[i]);
        }
        chartMaxVal *= 1.15; // Add 15% headroom
    }

    // =================== TABLE CELL RENDERER ===================

    class ActivityCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setIcon(null);

            if (column == 0) {
                setHorizontalAlignment(SwingConstants.CENTER);
                setText("");
                String type = value != null ? value.toString() : "";
                if (type.equals("TOKO")) {
                    setIcon(IconUtils.getIcon("shop", 18, new Color(100, 116, 139)));
                } else {
                    setIcon(IconUtils.getIcon("wallet", 18, new Color(100, 116, 139)));
                }
                setForeground(c.getBackground()); // hide raw text
            } else if (column == 1) {
                setForeground(new Color(30, 41, 59));
            } else if (column == 2) {
                setHorizontalAlignment(SwingConstants.CENTER);
                String type = value != null ? value.toString() : "";
                final String badgeType = type;
                JLabel badge = new JLabel(badgeType, SwingConstants.CENTER) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        if (badgeType.equals("TOKO")) {
                            g2.setColor(new Color(224, 242, 254));
                            g2.fillRoundRect(5, 12, getWidth()-10, getHeight()-24, 10, 10);
                            g2.setColor(new Color(59, 130, 246));
                        } else {
                            g2.setColor(new Color(220, 252, 231));
                            g2.fillRoundRect(5, 12, getWidth()-10, getHeight()-24, 10, 10);
                            g2.setColor(new Color(34, 197, 94));
                        }
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        FontMetrics fm = g2.getFontMetrics();
                        int x = (getWidth() - fm.stringWidth(badgeType)) / 2;
                        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                        g2.drawString(badgeType, x, y);
                        g2.dispose();
                    }
                };
                badge.setOpaque(isSelected);
                badge.setBackground(c.getBackground());
                return badge;
            } else if (column == 4) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Double) {
                    double amount = (Double) value;
                    String rawType = table.getValueAt(row, 0) != null ? table.getValueAt(row, 0).toString() : "";
                    // TOKO = minus (pengeluaran), TABUNGAN_TARIK = minus (penarikan)
                    // TABUNGAN_SIMPAN = plus (setoran)
                    if (rawType.equals("TOKO") || rawType.equals("TABUNGAN_TARIK")) {
                        setForeground(new Color(239, 68, 68));
                        setText("- Rp " + String.format("%,.0f", amount).replace(',', '.'));
                    } else {
                        setForeground(new Color(34, 197, 94));
                        setText("+ Rp " + String.format("%,.0f", amount).replace(',', '.'));
                    }
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                }
            }

            return c;
        }
    }
}
