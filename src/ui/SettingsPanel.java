package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

import model.User;
import util.DBHelper;
import util.IconUtils;

public class SettingsPanel extends JPanel {

    private JLabel lblTotalAdmin;
    private JLabel lblTotalBaru;

    private JTable tblUsers;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblInfo;
    private JPanel paginationPanel;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int ROWS_PER_PAGE = 8;
    private List<User> allUsers = new ArrayList<>();

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cbRole;

    private int selectedUserId = -1;

    public SettingsPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        statsPanel.setOpaque(false);

        JPanel card1 = createStatCard("TOTAL AKUN", lblTotalAdmin = new JLabel("0 Akun"), "Total seluruh pengguna", "users", new Color(239, 246, 255), new Color(59, 130, 246));
        JPanel card2 = createStatCard("AKUN BARU", lblTotalBaru = new JLabel("0 Akun"), "Terdaftar bulan ini", "user-plus", new Color(220, 252, 231), new Color(34, 197, 94));

        statsPanel.add(card1);
        statsPanel.add(card2);

        add(statsPanel, BorderLayout.NORTH);

        JPanel splitPanel = new JPanel(new BorderLayout(20, 0));
        splitPanel.setOpaque(false);

        splitPanel.add(createTablePanel(), BorderLayout.CENTER);

        splitPanel.add(createFormPanel(), BorderLayout.EAST);

        add(splitPanel, BorderLayout.CENTER);

        loadUsers();
    }

    private JPanel createStatCard(String title, JLabel valLabel, String subtitle, String iconName, Color bg, Color iconColor) {
        JPanel pnl = new JPanel(new BorderLayout(15, 0)) {
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
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel iconHolder = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconHolder.setOpaque(false);
        iconHolder.setPreferredSize(new Dimension(65, 65));
        JLabel icon = new JLabel(IconUtils.getIcon(iconName, 28, iconColor));
        iconHolder.add(icon);
        pnl.add(iconHolder, BorderLayout.WEST);

        JPanel textHolder = new JPanel();
        textHolder.setLayout(new BoxLayout(textHolder, BoxLayout.Y_AXIS));
        textHolder.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(100, 116, 139));

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLabel.setForeground(new Color(30, 41, 59));

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        textHolder.add(lblTitle);
        textHolder.add(Box.createRigidArea(new Dimension(0, 4)));
        textHolder.add(valLabel);
        textHolder.add(Box.createRigidArea(new Dimension(0, 4)));
        textHolder.add(lblSub);

        pnl.add(textHolder, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createTablePanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filterRow.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(300, 42));
        txtSearch.putClientProperty("JComponent.roundRect", true);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari username...");
        txtSearch.putClientProperty("JTextField.leadingIcon", IconUtils.getIcon("search", 16, new Color(148, 163, 184)));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 12));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { loadUsers(); }
        });

        filterRow.add(txtSearch);
        leftPanel.add(filterRow, BorderLayout.NORTH);

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

        String[] columns = {"ID", "Username", "Role", "Tgl Dibuat", "Aksi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; 
            }
        };
        tblUsers = new JTable(tableModel);
        tblUsers.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblUsers.setRowHeight(48);
        tblUsers.setShowGrid(false);
        tblUsers.setIntercellSpacing(new Dimension(0, 0));
        tblUsers.setSelectionBackground(new Color(241, 245, 249));
        tblUsers.setSelectionForeground(new Color(30, 41, 59));

        tblUsers.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblUsers.getColumnModel().getColumn(1).setPreferredWidth(200);
        tblUsers.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblUsers.getColumnModel().getColumn(3).setPreferredWidth(150);
        tblUsers.getColumnModel().getColumn(4).setPreferredWidth(80);

        tblUsers.setDefaultRenderer(Object.class, new UserCellRenderer());
        tblUsers.getColumnModel().getColumn(4).setCellRenderer(new ActionRenderer());
        tblUsers.getColumnModel().getColumn(4).setCellEditor(new ActionEditor());

        tblUsers.getTableHeader().setBackground(Color.WHITE);
        tblUsers.getTableHeader().setForeground(new Color(100, 116, 139));
        tblUsers.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblUsers.getTableHeader().setPreferredSize(new Dimension(100, 42));
        tblUsers.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        tblUsers.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(tblUsers);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        lblInfo = new JLabel("Menampilkan 0 akun");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(100, 116, 139));
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        paginationPanel.setOpaque(false);
        bottomPanel.add(lblInfo, BorderLayout.WEST);
        bottomPanel.add(paginationPanel, BorderLayout.EAST);

        tableCard.add(bottomPanel, BorderLayout.SOUTH);
        leftPanel.add(tableCard, BorderLayout.CENTER);

        return leftPanel;
    }

    private JPanel createFormPanel() {
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
        JLabel formIcon = new JLabel(IconUtils.getIcon("settings", 20, new Color(59, 130, 246)));
        JLabel formTitle = new JLabel("Form Data Akun");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(new Color(30, 41, 59));
        formHeader.add(formIcon);
        formHeader.add(formTitle);
        formCard.add(formHeader);
        formCard.add(Box.createRigidArea(new Dimension(0, 25)));

        formCard.add(createFormLabel("Username"));
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtUsername.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        txtUsername.setAlignmentX(LEFT_ALIGNMENT);
        txtUsername.putClientProperty("JTextField.placeholderText", "Masukkan username");
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        formCard.add(txtUsername);
        formCard.add(Box.createRigidArea(new Dimension(0, 20)));

        formCard.add(createFormLabel("Password"));
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtPassword.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        txtPassword.setAlignmentX(LEFT_ALIGNMENT);
        txtPassword.putClientProperty("JTextField.placeholderText", "Isi untuk mengganti/membuat");
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        formCard.add(txtPassword);
        formCard.add(Box.createRigidArea(new Dimension(0, 20)));

        formCard.add(createFormLabel("Role"));
        cbRole = new JComboBox<>(new String[]{"ADMIN", "KASIR"});
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cbRole.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        cbRole.setAlignmentX(LEFT_ALIGNMENT);
        cbRole.setBackground(Color.WHITE);
        formCard.add(cbRole);
        formCard.add(Box.createRigidArea(new Dimension(0, 30)));
        formCard.add(Box.createVerticalGlue());

        JButton btnAdd = createBtn("+ Tambah Baru", new Color(22, 163, 74), Color.WHITE);
        JButton btnUpdate = createBtn("Simpan Perubahan", new Color(59, 130, 246), Color.WHITE);
        JButton btnDelete = createBtn("Hapus Data", new Color(220, 38, 38), Color.WHITE);
        JButton btnClear = createBtn("Reset Form", Color.WHITE, new Color(71, 85, 105));

        btnClear.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        btnAdd.addActionListener(e -> saveUser(true));
        btnUpdate.addActionListener(e -> saveUser(false));
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearForm());

        formCard.add(btnAdd);
        formCard.add(Box.createRigidArea(new Dimension(0, 10)));
        formCard.add(btnUpdate);
        formCard.add(Box.createRigidArea(new Dimension(0, 10)));
        formCard.add(btnDelete);
        formCard.add(Box.createRigidArea(new Dimension(0, 10)));
        formCard.add(btnClear);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.setPreferredSize(new Dimension(380, 0));
        formWrapper.add(formCard, BorderLayout.CENTER);

        return formWrapper;
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(30, 41, 59));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private JButton createBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        return btn;
    }

    public void loadUsers() {
        allUsers.clear();
        String search = txtSearch.getText().trim();

        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1 ");
        if (!search.isEmpty()) {
            sql.append("AND username LIKE '%").append(search).append("%' ");
        }
        sql.append("ORDER BY id ASC");

        int totAdmin = 0;

        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                User u = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getTimestamp("created_at")
                );
                allUsers.add(u);
                totAdmin++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        lblTotalAdmin.setText(totAdmin + " Akun");
        lblTotalBaru.setText(totAdmin + " Akun"); 

        totalPages = Math.max(1, (int) Math.ceil((double) allUsers.size() / ROWS_PER_PAGE));
        if (currentPage > totalPages) currentPage = 1;
        showPage(currentPage);
    }

    private void showPage(int page) {
        currentPage = page;
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");

        int start = (page - 1) * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, allUsers.size());

        for (int i = start; i < end; i++) {
            User u = allUsers.get(i);
            String dateStr = u.getCreatedAt() != null ? sdf.format(u.getCreatedAt()) : "-";
            tableModel.addRow(new Object[]{
                u.getId(),
                u.getUsername(),
                u.getRole(),
                dateStr,
                u 
            });
        }

        int totalData = allUsers.size();
        if (totalData == 0) {
            lblInfo.setText("Tidak ada data akun");
        } else {
            lblInfo.setText("Menampilkan " + (start + 1) + " - " + end + " dari " + totalData + " akun");
        }

        updatePagination();
    }

    private void updatePagination() {
        paginationPanel.removeAll();
        if (allUsers.size() <= ROWS_PER_PAGE) {
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

    private void saveUser(boolean isNew) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String role = cbRole.getSelectedItem().toString();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBHelper.getConnection()) {
            if (isNew) {
                if (password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Password wajib diisi untuk akun baru!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, role);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Akun berhasil ditambahkan!");
                }
            } else {
                if (selectedUserId == -1) {
                    JOptionPane.showMessageDialog(this, "Pilih akun terlebih dahulu untuk diupdate!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (password.isEmpty()) {

                    String sql = "UPDATE users SET username=?, role=? WHERE id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, username);
                        stmt.setString(2, role);
                        stmt.setInt(3, selectedUserId);
                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Akun berhasil diperbarui!");
                    }
                } else {

                    String sql = "UPDATE users SET username=?, password=?, role=? WHERE id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, username);
                        stmt.setString(2, password);
                        stmt.setString(3, role);
                        stmt.setInt(4, selectedUserId);
                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Akun & Password berhasil diperbarui!");
                    }
                }
            }
            clearForm();
            loadUsers();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUser() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih akun terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus akun ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
                stmt.setInt(1, selectedUserId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Akun dihapus!");
                clearForm();
                loadUsers();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedUserId = -1;
        txtUsername.setText("");
        txtPassword.setText("");
        cbRole.setSelectedItem("ADMIN");
        tblUsers.clearSelection();
    }

    public void editUser(User u) {
        selectedUserId = u.getId();
        txtUsername.setText(u.getUsername());
        txtPassword.setText(""); 
        cbRole.setSelectedItem(u.getRole());
    }

    class UserCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                label.setForeground(new Color(30, 41, 59));
            }
            if (column == 2) { 
                String r = (String) value;
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
                panel.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

                JLabel badge = new JLabel(r);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                badge.setOpaque(true);
                badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

                badge.setBackground(new Color(243, 232, 255));
                badge.setForeground(new Color(168, 85, 247));

                panel.add(badge);
                return panel;
            }
            return label;
        }
    }

    class ActionRenderer extends JPanel implements TableCellRenderer {
        private JButton btnEdit;
        private JButton btnDelete;

        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 8));
            setOpaque(true);
            btnEdit = createIconButton("edit", new Color(59, 130, 246));
            btnDelete = createIconButton("trash", new Color(220, 38, 38));
            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            return this;
        }
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton btnEdit;
        private JButton btnDelete;
        private User currentUser;

        public ActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
            btnEdit = createIconButton("edit", new Color(59, 130, 246));
            btnDelete = createIconButton("trash", new Color(220, 38, 38));

            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                editUser(currentUser);
            });
            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                selectedUserId = currentUser.getId();
                deleteUser();
            });

            panel.add(btnEdit);
            panel.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentUser = (User) value;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentUser;
        }
    }

    private JButton createIconButton(String iconName, Color color) {
        JButton btn = new JButton(IconUtils.getIcon(iconName, 14, color));
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

