package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Product;
import model.Student;
import model.ShopTransaction;
import util.DBHelper;
import util.IconUtils;
import util.ReceiptPrinter;

public class ShopPanel extends JPanel {
    private JTable tblProducts;
    private DefaultTableModel prodModel;
    private List<Product> productList = new ArrayList<>();

    private JTable tblCart;
    private DefaultTableModel cartModel;
    private Map<Product, Integer> cartItems = new HashMap<>();

    private JTable tblShopHistory;
    private DefaultTableModel historyModel;

    private JComboBox<String> cbBuyerType;
    private JComboBox<Object> cbStudents; 
    private JComboBox<String> cbPaymentMethod; 
    private JLabel lblTotalAmount;
    private List<Student> students = new ArrayList<>();

    private JButton btnAddToCart;
    private JButton btnRemoveFromCart;
    private JButton btnCheckout;
    private JButton btnPrintReceipt;

    private double totalAmount = 0.0;
    private ShopTransaction lastTransaction;

    public ShopPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 45));

        JPanel centerPanel = new JPanel(new BorderLayout(20, 0));
        centerPanel.setOpaque(false);

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);
        GridBagConstraints lgbc = new GridBagConstraints();
        lgbc.fill = GridBagConstraints.BOTH;
        lgbc.weightx = 1.0;

        JPanel catalogContainer = createCardPanel();
        catalogContainer.setLayout(new BorderLayout());
        catalogContainer.add(createCardHeader("Katalog Barang Toko", "box", new Color(59, 130, 246)), BorderLayout.NORTH);

        String[] prodColumns = {"ID", "Kode", "Nama Barang", "Harga", "Stok"};
        prodModel = new DefaultTableModel(prodColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblProducts = createCustomTable(prodModel);
        tblProducts.getColumnModel().getColumn(0).setMinWidth(0);
        tblProducts.getColumnModel().getColumn(0).setMaxWidth(0);
        tblProducts.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane prodScroll = new JScrollPane(tblProducts);
        prodScroll.setBorder(BorderFactory.createEmptyBorder());
        catalogContainer.add(prodScroll, BorderLayout.CENTER);

        JPanel catalogBtnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
        catalogBtnBar.setOpaque(false);
        catalogBtnBar.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        btnAddToCart = createButton("Tambah ke Keranjang", "shop", new Color(16, 185, 129));
        btnAddToCart.addActionListener(e -> addToCart());
        catalogBtnBar.add(btnAddToCart);
        catalogContainer.add(catalogBtnBar, BorderLayout.SOUTH);

        lgbc.gridy = 0;
        lgbc.weighty = 0.55;
        leftPanel.add(catalogContainer, lgbc);

        lgbc.gridy = 1;
        lgbc.weighty = 0.0;
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)), lgbc);

        JPanel historyContainer = createCardPanel();
        historyContainer.setLayout(new BorderLayout());
        historyContainer.add(createCardHeader("Riwayat Pembelian Terbaru", "report", new Color(245, 158, 11)), BorderLayout.NORTH);

        String[] histColumns = {"ID Transaksi", "Tanggal", "Pembeli", "Total Belanja", "Metode"};
        historyModel = new DefaultTableModel(histColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblShopHistory = createCustomTable(historyModel);
        JScrollPane histScroll = new JScrollPane(tblShopHistory);
        histScroll.setBorder(BorderFactory.createEmptyBorder());
        historyContainer.add(histScroll, BorderLayout.CENTER);

        JPanel histBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
        histBtnBar.setOpaque(false);
        histBtnBar.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        JButton btnReprint = createButton("Cetak Ulang Struk", "report", new Color(59, 130, 246));
        btnReprint.addActionListener(e -> reprintSelectedHistory());
        histBtnBar.add(btnReprint);
        historyContainer.add(histBtnBar, BorderLayout.SOUTH);

        lgbc.gridy = 2;
        lgbc.weighty = 0.45;
        leftPanel.add(historyContainer, lgbc);

        centerPanel.add(leftPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(380, 0));
        GridBagConstraints rgbc = new GridBagConstraints();
        rgbc.fill = GridBagConstraints.BOTH;
        rgbc.weightx = 1.0;

        JPanel cartContainer = createCardPanel();
        cartContainer.setLayout(new BorderLayout());
        cartContainer.add(createCardHeader("Keranjang Belanja", "shop", new Color(168, 85, 247)), BorderLayout.NORTH);

        String[] cartColumns = {"Barang", "Harga", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cartColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCart = createCustomTable(cartModel);
        JScrollPane cartScroll = new JScrollPane(tblCart);
        cartScroll.setBorder(BorderFactory.createEmptyBorder());
        cartContainer.add(cartScroll, BorderLayout.CENTER);

        JPanel cartBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
        cartBtnBar.setOpaque(false);
        cartBtnBar.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        btnRemoveFromCart = createButton("Hapus Item", "trash", new Color(239, 68, 68));
        btnRemoveFromCart.addActionListener(e -> removeFromCart());
        cartBtnBar.add(btnRemoveFromCart);
        cartContainer.add(cartBtnBar, BorderLayout.SOUTH);

        rgbc.gridy = 0;
        rgbc.weighty = 0.50;
        rightPanel.add(cartContainer, rgbc);

        rgbc.gridy = 1;
        rgbc.weighty = 0.0;
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)), rgbc);

        JPanel checkCard = createCardPanel();
        checkCard.setLayout(new BoxLayout(checkCard, BoxLayout.Y_AXIS));
        checkCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 25, 20));

        JLabel lblCheckTitle = new JLabel("Rincian Pembayaran");
        lblCheckTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCheckTitle.setForeground(new Color(30, 41, 59));
        lblCheckTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkCard.add(lblCheckTitle);
        checkCard.add(Box.createRigidArea(new Dimension(0, 20)));

        cbBuyerType = createComboBox(new String[]{"Guest (Umum)", "Member (Anggota Koperasi)"});
        cbBuyerType.addActionListener(e -> handleBuyerTypeChange());
        checkCard.add(createFormGroup("Tipe Pembeli", cbBuyerType));
        checkCard.add(Box.createRigidArea(new Dimension(0, 15)));

        cbStudents = createComboBox(new Object[]{"--- Pilih Anggota ---"});
        cbStudents.setEnabled(false);
        checkCard.add(createFormGroup("Pilih Anggota", cbStudents));
        checkCard.add(Box.createRigidArea(new Dimension(0, 15)));

        cbPaymentMethod = createComboBox(new String[]{"CASH (Tunai)", "TABUNGAN (Potong Saldo)"});
        cbPaymentMethod.addActionListener(e -> validatePaymentMethod());
        checkCard.add(createFormGroup("Metode Bayar", cbPaymentMethod));
        checkCard.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        totalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel lblTotalLabel = new JLabel("Total Tagihan:");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalLabel.setForeground(new Color(100, 116, 139));

        lblTotalAmount = new JLabel("Rp 0");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTotalAmount.setForeground(new Color(16, 185, 129));

        totalPanel.add(lblTotalLabel, BorderLayout.WEST);
        totalPanel.add(lblTotalAmount, BorderLayout.EAST);
        totalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkCard.add(totalPanel);
        checkCard.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel checkoutBtnGrid = new JPanel(new GridLayout(1, 2, 10, 0));
        checkoutBtnGrid.setOpaque(false);
        checkoutBtnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        checkoutBtnGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCheckout = createButton("Checkout", "wallet", new Color(16, 185, 129));
        btnCheckout.addActionListener(e -> checkoutCart());

        btnPrintReceipt = createButton("Cetak Struk", "report", new Color(59, 130, 246));
        btnPrintReceipt.setEnabled(false);
        btnPrintReceipt.addActionListener(e -> showReceiptDialog());

        checkoutBtnGrid.add(btnCheckout);
        checkoutBtnGrid.add(btnPrintReceipt);
        checkCard.add(checkoutBtnGrid);

        rgbc.gridy = 2;
        rgbc.weighty = 0.50;
        rightPanel.add(checkCard, rgbc);

        centerPanel.add(rightPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        loadProducts();
        loadStudents();
        loadShopHistory();
    }

    private JPanel createCardPanel() {
        JPanel pnl = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pnl.setOpaque(false);
        return pnl;
    }

    private JPanel createCardHeader(String title, String icon, Color iconColor) {
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(30, 41, 59));
        if(icon != null) {
            lbl.setIcon(IconUtils.getIcon(icon, 18, iconColor));
        }
        hdr.add(lbl);
        return hdr;
    }

    private JPanel createFormGroup(String label, JComponent comp) {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        comp.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));

        pnl.add(lbl);
        pnl.add(Box.createRigidArea(new Dimension(0, 6)));
        pnl.add(comp);
        return pnl;
    }

    private <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return cb;
    }

    private JButton createButton(String text, String iconStr, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
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
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if(iconStr != null) {
            btn.setIcon(IconUtils.getIcon(iconStr, 16, Color.WHITE));
        }
        return btn;
    }

    private JTable createCustomTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(241, 245, 249));
        table.setSelectionForeground(new Color(30, 41, 59));

        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(new Color(100, 116, 139));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(100, 40));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                JLabel label = (JLabel) c;
                label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 252, 253));
                    label.setForeground(new Color(30, 41, 59));
                }
                if (value != null && value.toString().startsWith("Rp")) {
                    label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    label.setForeground(new Color(16, 185, 129));
                }
                return label;
            }
        });
        return table;
    }

    public void loadProducts() {
        prodModel.setRowCount(0);
        productList.clear();

        String sql = "SELECT * FROM barang WHERE is_active = TRUE ORDER BY kode_barang ASC";
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product(
                    rs.getInt("id"),
                    rs.getString("kode_barang"),
                    rs.getString("nama_barang"),
                    rs.getDouble("harga"),
                    rs.getInt("stok")
                );
                productList.add(p);

                prodModel.addRow(new Object[]{
                    p.getId(),
                    p.getProductCode(),
                    p.getName(),
                    "Rp " + String.format("%,.0f", p.getPrice()),
                    p.getStock()
                });
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat barang toko: " + e.getMessage());
        }
    }

    public void loadStudents() {
        cbStudents.removeAllItems();
        students.clear();
        cbStudents.addItem("--- Pilih Anggota ---");

        String sql = "SELECT * FROM siswa WHERE is_active = TRUE ORDER BY nis ASC";
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
                    rs.getTimestamp("created_at")
                );
                students.add(s);
                cbStudents.addItem(s);
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat siswa di POS: " + e.getMessage());
        }
    }

    public void loadShopHistory() {
        historyModel.setRowCount(0);

        String sql = "SELECT t.*, s.nama FROM transaksi_toko t LEFT JOIN siswa s ON t.siswa_id = s.id ORDER BY t.tanggal DESC LIMIT 20";
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("nama");
                if (name == null) {
                    name = "Umum (Guest)";
                }
                historyModel.addRow(new Object[]{
                    "TOKO-" + rs.getInt("id"),
                    rs.getTimestamp("tanggal").toString().substring(0, 19),
                    name,
                    "Rp " + String.format("%,.0f", rs.getDouble("total_harga")),
                    rs.getString("metode_pembayaran")
                });
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat riwayat belanja toko: " + e.getMessage());
        }
    }

    private void handleBuyerTypeChange() {
        boolean isMember = cbBuyerType.getSelectedIndex() == 1;
        cbStudents.setEnabled(isMember);

        if (!isMember) {
            cbPaymentMethod.setSelectedIndex(0);
            cbPaymentMethod.setEnabled(false);
            cbStudents.setSelectedIndex(0);
        } else {
            cbPaymentMethod.setEnabled(true);
        }
    }

    private void validatePaymentMethod() {
        boolean isTabungan = cbPaymentMethod.getSelectedIndex() == 1;
        boolean isGuest = cbBuyerType.getSelectedIndex() == 0;

        if (isTabungan && isGuest) {
            JOptionPane.showMessageDialog(this, "Metode pembayaran tabungan memerlukan anggota siswa!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            cbPaymentMethod.setSelectedIndex(0);
        }
    }

    private void addToCart() {
        int selectedRow = tblProducts.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih barang dari katalog terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) prodModel.getValueAt(selectedRow, 0);
        Product product = null;
        for (Product p : productList) {
            if (p.getId() == productId) {
                product = p;
                break;
            }
        }

        if (product == null) return;

        if (product.getStock() <= 0) {
            JOptionPane.showMessageDialog(this, "Stok barang habis!", "Stok Kosong", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyStr = JOptionPane.showInputDialog(this, "Masukkan Jumlah (Qty) untuk " + product.getName() + ":", "Tambah ke Keranjang", JOptionPane.PLAIN_MESSAGE);
        if (qtyStr == null) return;

        int qty = 0;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah barang harus berupa angka positif!", "Format Salah", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int existingQty = cartItems.getOrDefault(product, 0);
        if (existingQty + qty > product.getStock()) {
            JOptionPane.showMessageDialog(this, "Stok tidak cukup!\nTersedia: " + product.getStock() + "\nDi keranjang: " + existingQty, "Stok Kurang", JOptionPane.WARNING_MESSAGE);
            return;
        }

        cartItems.put(product, existingQty + qty);
        updateCartTable();
    }

    private void removeFromCart() {
        int selectedRow = tblCart.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih item di keranjang yang ingin dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = (String) cartModel.getValueAt(selectedRow, 0);
        Product toRemove = null;
        for (Product p : cartItems.keySet()) {
            if (p.getName().equals(name)) {
                toRemove = p;
                break;
            }
        }

        if (toRemove != null) {
            cartItems.remove(toRemove);
            updateCartTable();
        }
    }

    private void updateCartTable() {
        cartModel.setRowCount(0);
        totalAmount = 0.0;

        for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            double subtotal = p.getPrice() * qty;
            totalAmount += subtotal;

            cartModel.addRow(new Object[]{
                p.getName(),
                "Rp " + String.format("%,.0f", p.getPrice()),
                qty,
                "Rp " + String.format("%,.0f", subtotal)
            });
        }

        lblTotalAmount.setText("Rp " + String.format("%,.0f", totalAmount));
    }

    private void checkoutCart() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang belanja masih kosong!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isMember = cbBuyerType.getSelectedIndex() == 1;
        Integer studentId = null;
        Student selectedStudent = null;

        if (isMember) {
            Object selected = cbStudents.getSelectedItem();
            if (selected == null || selected instanceof String) {
                JOptionPane.showMessageDialog(this, "Pilih anggota siswa terlebih dahulu!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedStudent = (Student) selected;
            studentId = selectedStudent.getId();
        }

        String method = cbPaymentMethod.getSelectedIndex() == 0 ? "CASH" : "TABUNGAN";

        ShopTransaction trans = new ShopTransaction(studentId, totalAmount, method, cartItems);

        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false); 
            boolean success = trans.processTransaction(conn);

            if (success) {
                conn.commit();

                JOptionPane.showMessageDialog(
                    this, 
                    "Checkout berhasil! Total Transaksi: Rp" + String.format("%,.0f", totalAmount), 
                    "Sukses", 
                    JOptionPane.INFORMATION_MESSAGE
                );

                lastTransaction = trans;
                btnPrintReceipt.setEnabled(true);

                cartItems.clear();
                updateCartTable();

                loadProducts();
                loadStudents();
                loadShopHistory();

            } else {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Checkout gagal dilakukan. Saldo mungkin tidak cukup.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            JOptionPane.showMessageDialog(this, "Transaksi Gagal: " + e.getMessage(), "Kesalahan Transaksi", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private void reprintSelectedHistory() {
        int row = tblShopHistory.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih riwayat transaksi terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idStr = (String) historyModel.getValueAt(row, 0); 
        int transId = Integer.parseInt(idStr.replace("TOKO-", ""));

        Map<Product, Integer> items = new HashMap<>();
        String sName = null;
        String sNis = null;
        double totAmount = 0;
        String method = (String) historyModel.getValueAt(row, 4);

        try (Connection conn = DBHelper.getConnection()) {
            String sqlTrans = "SELECT t.*, s.nama, s.nis FROM transaksi_toko t LEFT JOIN siswa s ON t.siswa_id = s.id WHERE t.id = ?";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlTrans)) {
                ps.setInt(1, transId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totAmount = rs.getDouble("total_harga");
                        if (rs.getString("nama") != null) {
                            sName = rs.getString("nama");
                            sNis = rs.getString("nis");
                        }
                    }
                }
            }

            String sqlDetails = "SELECT d.jumlah, b.id as bid, b.kode_barang, b.nama_barang, d.harga_satuan FROM detail_transaksi_toko d JOIN barang b ON d.barang_id = b.id WHERE d.transaksi_toko_id = ?";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlDetails)) {
                ps.setInt(1, transId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Product p = new Product(
                            rs.getInt("bid"),
                            rs.getString("kode_barang"),
                            rs.getString("nama_barang"),
                            rs.getDouble("harga_satuan"),
                            0
                        );
                        items.put(p, rs.getInt("jumlah"));
                    }
                }
            }

            ShopTransaction trans = new ShopTransaction(null, totAmount, method, items);
            ReceiptPrinter printer = new ReceiptPrinter(trans, sName, sNis);
            printer.printReceipt();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat detail struk: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReceiptDialog() {
        if (lastTransaction == null) return;

        String receiptText = lastTransaction.generateReceiptText();

        JTextArea area = new JTextArea(receiptText);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(360, 400));
        scroll.setBorder(BorderFactory.createEmptyBorder());

        Object[] options = {"Cetak ke Printer (PDF/Kertas)", "Tutup"};
        int result = JOptionPane.showOptionDialog(
            this,
            scroll,
            "Struk Belanja",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );

        if (result == JOptionPane.YES_OPTION) {
            String sName = null;
            String sNis = null;
            if (cbBuyerType.getSelectedIndex() == 1) { 
                Object sel = cbStudents.getSelectedItem();
                if (sel instanceof Student) {
                    sName = ((Student) sel).getName();
                    sNis = ((Student) sel).getNis();
                }
            }
            ReceiptPrinter printer = new ReceiptPrinter(lastTransaction, sName, sNis);
            printer.printReceipt();
        }
    }
}

