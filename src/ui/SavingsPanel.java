package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

import model.Student;
import model.SavingsTransaction;
import util.DBHelper;
import util.IconUtils;
import ui.components.DatePicker;

public class SavingsPanel extends JPanel {
    private JComboBox<Student> cbStudents;
    private JLabel lblCurrentBalanceForm;
    private List<Student> students = new ArrayList<>();

    // Form fields
    private JButton btnToggleSimpan;
    private JButton btnToggleTarik;
    private boolean isSimpan = true; // true = Simpan, false = Tarik
    private JTextField txtAmount;
    private JTextArea txtDesc;
    
    // Table
    private JTable tblHistory;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private DatePicker datePicker;
    private JComboBox<String> cbFilterType;
    private JLabel lblInfo;
    private JPanel paginationPanel;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int ROWS_PER_PAGE = 10; // Increased to 10 for better layout usage without stats panel
    private List<Object[]> allRows = new ArrayList<>();

    public SavingsPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ================= CENTER (Split) =================
        JPanel splitPanel = new JPanel(new BorderLayout(20, 0));
        splitPanel.setOpaque(false);

        // LEFT: Table
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);
        JLabel tableTitle = new JLabel("Riwayat Transaksi");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(new Color(30, 41, 59));
        tableTitle.setIcon(IconUtils.getIcon("clock", 20, new Color(59, 130, 246)));
        tableHeaderPanel.add(tableTitle, BorderLayout.NORTH);

        // Search & Filters
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);
        filterRow.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(250, 42));
        txtSearch.putClientProperty("JComponent.roundRect", true);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari NIS atau nama siswa...");
        txtSearch.putClientProperty("JTextField.leadingIcon", IconUtils.getIcon("search", 16, new Color(148, 163, 184)));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 12));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadTransactions();
            }
        });

        cbFilterType = new JComboBox<>(new String[]{"Semua Jenis", "SIMPAN", "TARIK"});
        cbFilterType.setPreferredSize(new Dimension(140, 42));
        cbFilterType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbFilterType.setBackground(Color.WHITE);
        cbFilterType.addActionListener(e -> loadTransactions());

        datePicker = new DatePicker();
        datePicker.setPreferredSize(new Dimension(180, 42));
        datePicker.setOnDateSelected(() -> loadTransactions());

        filterRow.add(txtSearch);
        filterRow.add(cbFilterType);
        filterRow.add(datePicker);
        tableHeaderPanel.add(filterRow, BorderLayout.CENTER);
        
        leftPanel.add(tableHeaderPanel, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tableCard.setOpaque(false);
        tableCard.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        String[] columns = {"Tanggal & Waktu", "NIS", "Nama", "Jenis", "Keterangan", "Nominal", "Saldo Akhir", "Petugas"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHistory = new JTable(tableModel);
        tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblHistory.setRowHeight(48);
        tblHistory.setShowGrid(false);
        tblHistory.setIntercellSpacing(new Dimension(0, 0));
        tblHistory.setSelectionBackground(new Color(241, 245, 249));
        tblHistory.setSelectionForeground(new Color(30, 41, 59));
        tblHistory.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tblHistory.getColumnModel().getColumn(0).setPreferredWidth(120);
        tblHistory.getColumnModel().getColumn(1).setPreferredWidth(80);
        tblHistory.getColumnModel().getColumn(2).setPreferredWidth(130);
        tblHistory.getColumnModel().getColumn(3).setPreferredWidth(70);
        tblHistory.getColumnModel().getColumn(4).setPreferredWidth(150);
        tblHistory.getColumnModel().getColumn(5).setPreferredWidth(100);
        tblHistory.getColumnModel().getColumn(6).setPreferredWidth(100);
        tblHistory.getColumnModel().getColumn(7).setPreferredWidth(60);

        tblHistory.setDefaultRenderer(Object.class, new TransactionCellRenderer());

        tblHistory.getTableHeader().setBackground(Color.WHITE);
        tblHistory.getTableHeader().setForeground(new Color(100, 116, 139));
        tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblHistory.getTableHeader().setPreferredSize(new Dimension(100, 42));
        tblHistory.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        tblHistory.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(tblHistory);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Pagination
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 5));
        lblInfo = new JLabel("Menampilkan 0 data");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(100, 116, 139));
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        paginationPanel.setOpaque(false);
        bottomPanel.add(lblInfo, BorderLayout.WEST);
        bottomPanel.add(paginationPanel, BorderLayout.EAST);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.add(tableCard, BorderLayout.CENTER);
        tableWrapper.add(bottomPanel, BorderLayout.SOUTH);

        leftPanel.add(tableWrapper, BorderLayout.CENTER);
        splitPanel.add(leftPanel, BorderLayout.CENTER);

        // RIGHT: Form
        JPanel formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        formCard.setOpaque(false);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel formHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        formHeader.setOpaque(false);
        formHeader.setAlignmentX(LEFT_ALIGNMENT);
        formHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel formIcon = new JLabel(IconUtils.getIcon("report", 20, new Color(34, 197, 94)));
        JLabel formTitle = new JLabel("Form Transaksi");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(new Color(30, 41, 59));
        formHeader.add(formIcon);
        formHeader.add(formTitle);
        formCard.add(formHeader);
        formCard.add(Box.createRigidArea(new Dimension(0, 25)));

        // Form Fields
        formCard.add(createFormLabel("Pilih Anggota"));
        cbStudents = new JComboBox<>();
        cbStudents.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbStudents.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        cbStudents.setPreferredSize(new Dimension(0, 45));
        cbStudents.setAlignmentX(LEFT_ALIGNMENT);
        cbStudents.setBackground(Color.WHITE);
        cbStudents.setEditable(true); // Allow searching/typing
        
        JTextField cbEditor = (JTextField) cbStudents.getEditor().getEditorComponent();
        cbEditor.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        cbEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    updateBalanceLabel();
                }
            }
        });
        cbStudents.addActionListener(e -> updateBalanceLabel());
        formCard.add(cbStudents);
        formCard.add(Box.createRigidArea(new Dimension(0, 20)));

        formCard.add(createFormLabel("Jenis Transaksi"));
        JPanel togglePanel = new JPanel(new GridLayout(1, 2, 0, 0));
        togglePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        togglePanel.setPreferredSize(new Dimension(0, 45));
        togglePanel.setAlignmentX(LEFT_ALIGNMENT);
        togglePanel.setOpaque(false);

        btnToggleSimpan = new JButton("Simpan");
        btnToggleTarik = new JButton("Tarik");
        styleToggleButton(btnToggleSimpan, true, true);
        styleToggleButton(btnToggleTarik, false, false);
        btnToggleSimpan.addActionListener(e -> setToggle(true));
        btnToggleTarik.addActionListener(e -> setToggle(false));
        togglePanel.add(btnToggleSimpan);
        togglePanel.add(btnToggleTarik);
        formCard.add(togglePanel);
        formCard.add(Box.createRigidArea(new Dimension(0, 20)));

        formCard.add(createFormLabel("Nominal"));
        JPanel amountPanel = new JPanel(new BorderLayout());
        amountPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        amountPanel.setPreferredSize(new Dimension(0, 45));
        amountPanel.setAlignmentX(LEFT_ALIGNMENT);
        amountPanel.setOpaque(false);
        JLabel lblRp = new JLabel("Rp");
        lblRp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRp.setForeground(new Color(100, 116, 139));
        lblRp.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        amountPanel.add(lblRp, BorderLayout.WEST);
        txtAmount = new JTextField();
        txtAmount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtAmount.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        txtAmount.putClientProperty("JTextField.placeholderText", "Masukkan nominal");
        amountPanel.add(txtAmount, BorderLayout.CENTER);
        amountPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder()
        ));
        formCard.add(amountPanel);
        formCard.add(Box.createRigidArea(new Dimension(0, 20)));

        formCard.add(createFormLabel("Keterangan"));
        txtDesc = new JTextArea(3, 20);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDesc);
        descScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        descScroll.setPreferredSize(new Dimension(0, 90));
        descScroll.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(descScroll);
        formCard.add(Box.createRigidArea(new Dimension(0, 25)));

        // Saldo Indicator
        JPanel saldoContainer = new JPanel();
        saldoContainer.setLayout(new BoxLayout(saldoContainer, BoxLayout.Y_AXIS));
        saldoContainer.setOpaque(true);
        saldoContainer.setBackground(new Color(220, 252, 231)); // Green bg
        saldoContainer.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        saldoContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // Increased height
        saldoContainer.setPreferredSize(new Dimension(0, 80));
        saldoContainer.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblSaldoTitle = new JLabel("Saldo Saat Ini");
        lblSaldoTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSaldoTitle.setForeground(new Color(21, 128, 61));
        lblCurrentBalanceForm = new JLabel("Rp 0");
        lblCurrentBalanceForm.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Increased font size
        lblCurrentBalanceForm.setForeground(new Color(21, 128, 61));
        saldoContainer.add(lblSaldoTitle);
        saldoContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        saldoContainer.add(lblCurrentBalanceForm);
        formCard.add(saldoContainer);
        formCard.add(Box.createVerticalGlue()); // Push buttons to bottom

        // Buttons
        JButton btnSubmit = new JButton("Simpan Transaksi") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setBackground(new Color(22, 163, 74));
        btnSubmit.setFocusPainted(false);
        btnSubmit.setBorderPainted(false);
        btnSubmit.setContentAreaFilled(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btnSubmit.setPreferredSize(new Dimension(0, 50));
        btnSubmit.setAlignmentX(LEFT_ALIGNMENT);
        btnSubmit.setIcon(IconUtils.getIcon("report", 16, Color.WHITE));
        btnSubmit.addActionListener(e -> processSavings());

        JButton btnClear = new JButton("Reset Form") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnClear.setForeground(new Color(100, 116, 139));
        btnClear.setBackground(Color.WHITE);
        btnClear.setFocusPainted(false);
        btnClear.setBorderPainted(false);
        btnClear.setContentAreaFilled(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btnClear.setPreferredSize(new Dimension(0, 50));
        btnClear.setAlignmentX(LEFT_ALIGNMENT);
        btnClear.setIcon(IconUtils.getIcon("reset", 16, new Color(100, 116, 139)));
        btnClear.addActionListener(e -> resetForm());

        formCard.add(Box.createRigidArea(new Dimension(0, 25)));
        formCard.add(btnSubmit);
        formCard.add(Box.createRigidArea(new Dimension(0, 12)));
        formCard.add(btnClear);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.setPreferredSize(new Dimension(420, 0)); // Even slightly wider to be safe
        formWrapper.add(formCard, BorderLayout.CENTER);

        splitPanel.add(formWrapper, BorderLayout.EAST);
        add(splitPanel, BorderLayout.CENTER);

        loadStudents();
        loadTransactions();
    }

    private void styleToggleButton(JButton btn, boolean isSimpanBtn, boolean isActive) {
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        if (isActive) {
            if (isSimpanBtn) {
                btn.setBackground(new Color(220, 252, 231)); // Light green
                btn.setForeground(new Color(21, 128, 61));
                btn.setIcon(IconUtils.getIcon("arrow-down", 16, new Color(21, 128, 61)));
                btn.setBorder(BorderFactory.createLineBorder(new Color(220, 252, 231)));
            } else {
                btn.setBackground(new Color(254, 226, 226)); // Light red
                btn.setForeground(new Color(220, 38, 38));
                btn.setIcon(IconUtils.getIcon("arrow-up", 16, new Color(220, 38, 38)));
                btn.setBorder(BorderFactory.createLineBorder(new Color(254, 226, 226)));
            }
        } else {
            btn.setBackground(new Color(248, 250, 252));
            btn.setForeground(new Color(100, 116, 139));
            btn.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
            if (isSimpanBtn) {
                btn.setIcon(IconUtils.getIcon("arrow-down", 16, new Color(100, 116, 139)));
            } else {
                btn.setIcon(IconUtils.getIcon("arrow-up", 16, new Color(100, 116, 139)));
            }
        }
    }

    private void setToggle(boolean toSimpan) {
        isSimpan = toSimpan;
        styleToggleButton(btnToggleSimpan, true, isSimpan);
        styleToggleButton(btnToggleTarik, false, !isSimpan);
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(30, 41, 59));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return lbl;
    }

    public void loadStudents() {
        cbStudents.removeAllItems();
        students.clear();
        
        String sql = "SELECT * FROM siswa WHERE is_active = TRUE ORDER BY nis ASC";
        
        Student dummy = new Student(-1, "", "Pilih Anggota...", "", 0.0, null) {
            @Override
            public String toString() {
                return getName();
            }
        };
        students.add(dummy);
        cbStudents.addItem(dummy);

        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Student s = new Student(
                    rs.getInt("id"),
                    rs.getString("nis"),
                    rs.getString("nama"),
                    rs.getString("kelas"),
                    rs.getDouble("saldo_tabungan"),
                    rs.getTimestamp("created_at"),
                    rs.getBoolean("is_active")
                );
                students.add(s);
                cbStudents.addItem(s);
            }
            cbStudents.setSelectedIndex(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateBalanceLabel();
    }

    private void updateBalanceLabel() {
        Student selected = getSelectedStudent();
        if (selected != null) {
            lblCurrentBalanceForm.setText("Rp " + formatRp(selected.getSavingsBalance()));
        } else {
            lblCurrentBalanceForm.setText("Rp 0");
        }
    }

    private Student getSelectedStudent() {
        Object selectedItem = cbStudents.getSelectedItem();
        if (selectedItem instanceof Student) {
            Student s = (Student) selectedItem;
            return s.getId() == -1 ? null : s;
        } else if (selectedItem instanceof String) {
            String typed = ((String) selectedItem).toLowerCase().trim();
            for (Student s : students) {
                if (s.getNis().toLowerCase().contains(typed) || s.getName().toLowerCase().contains(typed)) {
                    return s;
                }
            }
        }
        return null;
    }

    public void loadStats() {
        // Stats are now removed from UI, keeping method empty so MainFrame doesn't break
    }

    public void loadTransactions() {
        allRows.clear();
        String search = txtSearch.getText().trim();
        String dateFilter = datePicker.getSelectedDateStr();
        String filterType = cbFilterType.getSelectedItem().toString();
        
        StringBuilder sql = new StringBuilder("SELECT t.*, s.nama, s.nis, s.saldo_tabungan FROM transaksi_tabungan t JOIN siswa s ON t.siswa_id = s.id WHERE 1=1 ");
        
        if (!search.isEmpty()) {
            sql.append("AND (s.nama LIKE '%").append(search).append("%' OR s.nis LIKE '%").append(search).append("%') ");
        }
        
        if (!dateFilter.isEmpty()) {
            sql.append("AND DATE(t.tanggal) = '").append(dateFilter).append("' ");
        }
        
        if (filterType.equals("SIMPAN")) {
            sql.append("AND t.jenis_transaksi = 'SIMPAN' ");
        } else if (filterType.equals("TARIK")) {
            sql.append("AND t.jenis_transaksi = 'TARIK' ");
        }
        
        sql.append("ORDER BY t.tanggal DESC");

        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {
            
            while (rs.next()) {
                String type = rs.getString("jenis_transaksi");
                double amt = rs.getDouble("jumlah");
                String strAmt = (type.equals("SIMPAN") ? "+ Rp " : "- Rp ") + formatRp(amt);
                
                allRows.add(new Object[]{
                    rs.getTimestamp("tanggal").toString().substring(0, 19), 
                    rs.getString("nis"),
                    rs.getString("nama"),
                    type,
                    rs.getString("keterangan"),
                    strAmt,
                    "Rp " + formatRp(rs.getDouble("saldo_tabungan")),
                    "Admin"
                });
            }
        } catch (Exception e) {
            // Usually if date format is wrong, SQL query fails. We just gracefully ignore or show empty list.
            System.err.println("Load Transactions error (possibly invalid date format): " + e.getMessage());
        }

        totalPages = Math.max(1, (int) Math.ceil((double) allRows.size() / ROWS_PER_PAGE));
        if (currentPage > totalPages) currentPage = 1;
        showPage(currentPage);
    }

    private void showPage(int page) {
        currentPage = page;
        tableModel.setRowCount(0);
        
        int start = (page - 1) * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, allRows.size());
        
        for (int i = start; i < end; i++) {
            tableModel.addRow(allRows.get(i));
        }

        int totalData = allRows.size();
        if (totalData == 0) {
            lblInfo.setText("Tidak ada data transaksi");
        } else {
            lblInfo.setText("Menampilkan " + (start + 1) + " - " + end + " dari " + totalData + " transaksi");
        }

        updatePagination();
    }

    private void updatePagination() {
        paginationPanel.removeAll();
        if (allRows.size() <= ROWS_PER_PAGE) {
            paginationPanel.revalidate();
            paginationPanel.repaint();
            return;
        }

        JButton btnPrev = createPagBtn("<");
        btnPrev.setEnabled(currentPage > 1);
        btnPrev.addActionListener(e -> showPage(currentPage - 1));
        paginationPanel.add(btnPrev);

        for (int i = 1; i <= totalPages; i++) {
            final int pageNum = i;
            JButton btnPage = createPagBtn(String.valueOf(i));
            if (i == currentPage) {
                btnPage.setBackground(new Color(59, 130, 246));
                btnPage.setForeground(Color.WHITE);
                btnPage.setOpaque(true);
            }
            btnPage.addActionListener(e -> showPage(pageNum));
            paginationPanel.add(btnPage);
        }

        JButton btnNext = createPagBtn(">");
        btnNext.setEnabled(currentPage < totalPages);
        btnNext.addActionListener(e -> showPage(currentPage + 1));
        paginationPanel.add(btnNext);

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private JButton createPagBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(71, 85, 105));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void processSavings() {
        Student student = getSelectedStudent();
        
        if (student == null) {
            JOptionPane.showMessageDialog(this, "Siswa tidak ditemukan! Pastikan NIS/Nama sesuai.", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String amountStr = txtAmount.getText().trim();
        if (amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nominal transaksi tidak boleh kosong!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount = 0;
        try {
            amount = Double.parseDouble(amountStr.replace(".", "").replace(",", ""));
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Nominal harus berupa angka positif!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String type = isSimpan ? "SIMPAN" : "TARIK";
        String desc = txtDesc.getText().trim();

        SavingsTransaction trans = new SavingsTransaction(student.getId(), type, amount, desc);
        trans.setStudentDetails(student.getName(), student.getNis());

        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false); 

            boolean success = trans.processTransaction(conn);
            
            if (success) {
                conn.commit(); 
                JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
                loadStudents(); // Reload balances
                loadTransactions();
            } else {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Gagal memproses transaksi tabungan.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            JOptionPane.showMessageDialog(this, "Gagal memproses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void resetForm() {
        txtAmount.setText("");
        txtDesc.setText("");
        cbStudents.setSelectedIndex(0);
        setToggle(true);
        lblCurrentBalanceForm.setText("Rp 0");
    }

    private String formatRp(double val) {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("id", "ID"));
        return fmt.format(val);
    }

    class TransactionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            
            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                label.setForeground(new Color(30, 41, 59));
            }

            if (column == 3) {
                // Badge for Jenis
                String jenis = (String) value;
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
                if (isSelected) {
                    panel.setBackground(table.getSelectionBackground());
                } else {
                    panel.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                
                JLabel badge = new JLabel(jenis);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setOpaque(true);
                badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                
                if (jenis.equals("SIMPAN")) {
                    badge.setBackground(new Color(220, 252, 231));
                    badge.setForeground(new Color(21, 128, 61));
                } else {
                    badge.setBackground(new Color(254, 226, 226));
                    badge.setForeground(new Color(220, 38, 38));
                }
                panel.add(badge);
                return panel;
            } else if (column == 5) {
                // Nominal coloring
                String nom = (String) value;
                if (nom.startsWith("+")) {
                    label.setForeground(new Color(21, 128, 61));
                } else {
                    label.setForeground(new Color(220, 38, 38));
                }
            }
            
            // Text Alignment
            if (column == 5 || column == 6) {
                label.setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (column == 7 || column == 0 || column == 1) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }

            return label;
        }
    }
}
