package ui;

import util.IconUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Student;
import util.DBHelper;

public class StudentPanel extends JPanel {
    private JTable tblStudents;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    
    private JTextField txtNis;
    private JTextField txtName;
    private JTextField txtClass;
    private JTextField txtInitialSavings;
    
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnRealDelete;
    private JButton btnClear;
    
    private int selectedStudentId = -1;
    private List<Student> studentList = new ArrayList<>();

    // Pagination
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int ROWS_PER_PAGE = 10;
    private JPanel paginationPanel;
    private JLabel lblInfo;
    private List<Object[]> allRows = new ArrayList<>();

    public StudentPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));

        // =================== MAIN SPLIT ===================
        JPanel splitPanel = new JPanel(new BorderLayout(20, 0));
        splitPanel.setOpaque(false);

        // =================== LEFT: TABLE SECTION ===================
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);

        // Search bar row
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.putClientProperty("JComponent.roundRect", true);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari nama atau NIS...");
        txtSearch.putClientProperty("JTextField.leadingIcon", IconUtils.getIcon("search", 16, new Color(148, 163, 184)));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 12));
        txtSearch.setBackground(Color.WHITE);
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadData();
            }
        });

        searchRow.add(txtSearch, BorderLayout.WEST);
        leftPanel.add(searchRow, BorderLayout.NORTH);

        // Table card
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

        String[] columns = {"ID", "NIS", "Nama", "Kelas", "Saldo Tabungan", "Status", "Terdaftar Sejak", "Aksi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only action column
            }
        };
        tblStudents = new JTable(tableModel);
        tblStudents.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblStudents.setRowHeight(48);
        tblStudents.setShowGrid(false);
        tblStudents.setIntercellSpacing(new Dimension(0, 0));
        tblStudents.setSelectionBackground(new Color(241, 245, 249));
        tblStudents.setSelectionForeground(new Color(30, 41, 59));
        tblStudents.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hide ID column
        tblStudents.getColumnModel().getColumn(0).setMinWidth(0);
        tblStudents.getColumnModel().getColumn(0).setMaxWidth(0);
        tblStudents.getColumnModel().getColumn(0).setWidth(0);

        tblStudents.getColumnModel().getColumn(1).setPreferredWidth(80);
        tblStudents.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblStudents.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblStudents.getColumnModel().getColumn(4).setPreferredWidth(110);
        tblStudents.getColumnModel().getColumn(5).setPreferredWidth(70);
        tblStudents.getColumnModel().getColumn(6).setPreferredWidth(100);
        tblStudents.getColumnModel().getColumn(7).setPreferredWidth(70);

        tblStudents.setDefaultRenderer(Object.class, new StudentCellRenderer());

        // Table header styling
        tblStudents.getTableHeader().setBackground(Color.WHITE);
        tblStudents.getTableHeader().setForeground(new Color(100, 116, 139));
        tblStudents.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblStudents.getTableHeader().setPreferredSize(new Dimension(100, 42));
        tblStudents.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        tblStudents.getTableHeader().setReorderingAllowed(false);

        // Action column: click handler
        tblStudents.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblStudents.rowAtPoint(e.getPoint());
                int col = tblStudents.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 7) {
                    int cellWidth = tblStudents.getColumnModel().getColumn(7).getWidth();
                    int clickX = e.getX();
                    for (int i = 0; i < 7; i++) {
                        clickX -= tblStudents.getColumnModel().getColumn(i).getWidth();
                    }
                    if (clickX >= cellWidth / 2 || clickX < cellWidth) {
                        // Arsip button clicked
                        int studentId = (int) tableModel.getValueAt(row, 0);
                        boolean isActive = tableModel.getValueAt(row, 5).equals("Aktif");
                        toggleArchiveStudentById(studentId, !isActive);
                    }
                } else if (row >= 0) {
                    populateFormFromRow(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblStudents);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom: info + pagination
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

        // =================== RIGHT: FORM ===================
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
        JLabel formIcon = new JLabel(IconUtils.getIcon("user-plus", 18, new Color(30, 41, 59)));
        JLabel formTitle = new JLabel("Form Data Siswa");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(new Color(30, 41, 59));
        formHeader.add(formIcon);
        formHeader.add(formTitle);
        formCard.add(formHeader);
        formCard.add(Box.createRigidArea(new Dimension(0, 20)));

        // NIS
        formCard.add(createFormLabel("NIS"));
        txtNis = createFormField("Masukkan NIS");
        formCard.add(txtNis);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Nama
        formCard.add(createFormLabel("Nama Lengkap"));
        txtName = createFormField("Masukkan nama lengkap");
        formCard.add(txtName);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Kelas
        formCard.add(createFormLabel("Kelas"));
        txtClass = createFormField("Masukkan kelas");
        formCard.add(txtClass);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Saldo
        formCard.add(createFormLabel("Saldo Tabungan Awal"));
        txtInitialSavings = createFormField("0");
        txtInitialSavings.setText("0");
        formCard.add(txtInitialSavings);
        formCard.add(Box.createRigidArea(new Dimension(0, 25)));

        // Buttons
        btnAdd = createFormButton("+ Tambah Baru", new Color(34, 197, 94));
        btnUpdate = createFormButton("Simpan Perubahan", new Color(59, 130, 246));
        btnUpdate.setIcon(IconUtils.getIcon("report", 14, Color.WHITE));
        btnDelete = createFormButton("Nonaktifkan", new Color(245, 158, 11)); // Amber color
        btnDelete.setIcon(IconUtils.getIcon("trash", 14, Color.WHITE));
        btnRealDelete = createFormButton("Hapus Permanen", new Color(220, 38, 38)); // Red color
        btnRealDelete.setIcon(IconUtils.getIcon("trash", 14, Color.WHITE));
        btnClear = new JButton("Reset");
        btnClear.setIcon(IconUtils.getIcon("reset", 14, new Color(100, 116, 139)));
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClear.setForeground(new Color(100, 116, 139));
        btnClear.setBackground(Color.WHITE);
        btnClear.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btnClear.setFocusPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btnClear.setPreferredSize(new Dimension(0, 50));
        btnClear.setAlignmentX(LEFT_ALIGNMENT);

        formCard.add(btnAdd);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));
        formCard.add(btnUpdate);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));
        formCard.add(btnDelete);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));
        formCard.add(btnRealDelete);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));
        formCard.add(btnClear);

        // Spacer at bottom
        formCard.add(Box.createVerticalGlue());

        btnAdd.addActionListener(e -> addStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnRealDelete.addActionListener(e -> realDeleteStudent());
        btnClear.addActionListener(e -> clearForm());

        clearForm();

        // Wrapper to fix form width
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.setPreferredSize(new Dimension(320, 0));
        formWrapper.add(formCard, BorderLayout.CENTER);

        splitPanel.add(formWrapper, BorderLayout.EAST);

        add(splitPanel, BorderLayout.CENTER);
        loadData();
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(30, 41, 59));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        return lbl;
    }

    private JTextField createFormField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.putClientProperty("JComponent.roundRect", true);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private JButton createFormButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isEnabled()) {
                    g2.setColor(getBackground());
                } else {
                    g2.setColor(new Color(200, 200, 200));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setPreferredSize(new Dimension(0, 50));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        return btn;
    }

    // =================== DATA ===================

    public void loadData() {
        studentList.clear();
        allRows.clear();
        String searchQuery = txtSearch != null ? txtSearch.getText().trim() : "";
        
        String sql = "SELECT * FROM siswa";
        if (!searchQuery.isEmpty()) {
            sql += " WHERE nis LIKE ? OR nama LIKE ?";
        }
        sql += " ORDER BY nis ASC";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (!searchQuery.isEmpty()) {
                ps.setString(1, "%" + searchQuery + "%");
                ps.setString(2, "%" + searchQuery + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
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
                    studentList.add(s);
                    
                    allRows.add(new Object[]{
                        s.getId(),
                        s.getNis(),
                        s.getName(),
                        s.getStudentClass(),
                        s.getSavingsBalance(),
                        s.isActive() ? "Aktif" : "Nonaktif",
                        s.getCreatedAt().toString().substring(0, 10),
                        s.isActive() ? "Nonaktifkan" : "Aktifkan"
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat data siswa: " + e.getMessage());
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
            lblInfo.setText("Tidak ada data siswa");
        } else {
            lblInfo.setText("Menampilkan " + (start + 1) + " - " + end + " dari " + totalData + " data");
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
                btnPage.setBackground(new Color(29, 78, 216));
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

    // =================== FORM ACTIONS ===================

    private void populateFormFromRow(int row) {
        int studentId = (int) tableModel.getValueAt(row, 0);
        for (Student s : studentList) {
            if (s.getId() == studentId) {
                selectedStudentId = s.getId();
                txtNis.setText(s.getNis());
                txtNis.setEnabled(false);
                txtName.setText(s.getName());
                txtClass.setText(s.getStudentClass());
                txtInitialSavings.setText(String.valueOf(s.getSavingsBalance()));
                txtInitialSavings.setEnabled(false);
                btnAdd.setEnabled(false);
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
                btnRealDelete.setEnabled(true);
                btnDelete.setText(s.isActive() ? "Nonaktifkan" : "Aktifkan");
                break;
            }
        }
    }

    private void addStudent() {
        String nis = txtNis.getText().trim();
        String name = txtName.getText().trim();
        String cls = txtClass.getText().trim();
        String initialStr = txtInitialSavings.getText().trim();

        if (nis.isEmpty() || name.isEmpty() || cls.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seluruh field data siswa harus diisi!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double initialSavings = 0;
        try {
            initialSavings = Double.parseDouble(initialStr);
            if (initialSavings < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Saldo awal tabungan harus berupa angka positif!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO siswa (nis, nama, kelas, saldo_tabungan) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, nis);
            ps.setString(2, name);
            ps.setString(3, cls);
            ps.setDouble(4, initialSavings);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                int newStudentId = -1;
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newStudentId = generatedKeys.getInt(1);
                    }
                }
                
                if (initialSavings > 0 && newStudentId != -1) {
                    String logTransSql = "INSERT INTO transaksi_tabungan (siswa_id, jenis_transaksi, jumlah, keterangan) VALUES (?, 'SIMPAN', ?, 'Setoran Awal Registrasi')";
                    try (PreparedStatement logPs = conn.prepareStatement(logTransSql)) {
                        logPs.setInt(1, newStudentId);
                        logPs.setDouble(2, initialSavings);
                        logPs.executeUpdate();
                    }
                }

                JOptionPane.showMessageDialog(this, "Berhasil menambahkan siswa baru!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadData();
            }
        } catch (Exception e) {
            System.err.println("Gagal menambahkan siswa: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Gagal menambahkan siswa: NIS sudah digunakan atau database error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStudent() {
        if (selectedStudentId == -1) return;

        String name = txtName.getText().trim();
        String cls = txtClass.getText().trim();

        if (name.isEmpty() || cls.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dan Kelas tidak boleh kosong!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE siswa SET nama = ?, kelas = ? WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, name);
            ps.setString(2, cls);
            ps.setInt(3, selectedStudentId);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Data siswa berhasil diubah!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadData();
            }
        } catch (Exception e) {
            System.err.println("Gagal mengubah data siswa: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Gagal mengubah data siswa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent() {
        if (selectedStudentId == -1) return;
        boolean currentActive = true;
        for (Student s : studentList) {
            if (s.getId() == selectedStudentId) {
                currentActive = s.isActive();
                break;
            }
        }
        toggleArchiveStudentById(selectedStudentId, !currentActive);
    }

    private void toggleArchiveStudentById(int studentId, boolean toActive) {
        String actionStr = toActive ? "mengaktifkan kembali" : "menonaktifkan";
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Apakah Anda yakin ingin " + actionStr + " siswa ini?\nSiswa yang dinonaktifkan tidak akan muncul saat transaksi, namun riwayat tabungannya tetap aman.", 
            "Konfirmasi " + (toActive ? "Aktifkan" : "Nonaktifkan"), 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "UPDATE siswa SET is_active = ? WHERE id = ?";
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setBoolean(1, toActive);
                ps.setInt(2, studentId);
                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Status siswa berhasil diperbarui.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadData();
                }
            } catch (Exception e) {
                System.err.println("Gagal menghapus siswa: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Gagal menghapus siswa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void realDeleteStudent() {
        if (selectedStudentId == -1) return;
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Apakah Anda yakin ingin menghapus siswa ini secara PERMANEN?\nCatatan: Jika siswa ini memiliki riwayat tabungan, data transaksinya juga akan ikut terhapus!", 
            "Konfirmasi Hapus Permanen", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.ERROR_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM siswa WHERE id = ?";
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, selectedStudentId);
                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Data siswa berhasil dihapus permanen.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadData();
                }
            } catch (Exception e) {
                System.err.println("Gagal menghapus permanen siswa: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Gagal menghapus siswa. Pastikan tidak ada data yang terhubung atau hapus transaksinya terlebih dahulu.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedStudentId = -1;
        txtNis.setText("");
        txtNis.setEnabled(true);
        txtName.setText("");
        txtClass.setText("");
        txtInitialSavings.setText("0");
        txtInitialSavings.setEnabled(true);
        
        tblStudents.clearSelection();
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        btnRealDelete.setEnabled(false);
    }

    // =================== CELL RENDERER ===================

    class StudentCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                label.setForeground(new Color(30, 41, 59));
            }

            if (column == 5) {
                String status = (String) value;
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
                if (isSelected) {
                    panel.setBackground(table.getSelectionBackground());
                } else {
                    panel.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                JLabel badge = new JLabel(status);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setOpaque(true);
                badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                if (status.equals("Aktif")) {
                    badge.setBackground(new Color(220, 252, 231));
                    badge.setForeground(new Color(21, 128, 61));
                } else {
                    badge.setBackground(new Color(254, 226, 226));
                    badge.setForeground(new Color(220, 38, 38));
                }
                panel.add(badge);
                return panel;
            } else if (column == 4) {
                double amount = (Double) value;
                setText("Rp " + String.format("%,.0f", amount).replace(',', '.'));
                label.setForeground(new Color(34, 197, 94));
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else if (column == 7) {
                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
                if (isSelected) {
                    actionPanel.setBackground(table.getSelectionBackground());
                } else {
                    actionPanel.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }

                String actionText = (String) value;
                JLabel actionBtn = new JLabel(actionText);
                actionBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
                actionBtn.setForeground(actionText.equals("Aktifkan") ? new Color(21, 128, 61) : new Color(245, 158, 11));
                actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                actionPanel.add(actionBtn);

                return actionPanel;
            }

            return c;
        }
    }
}
