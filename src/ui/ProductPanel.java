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
import java.text.NumberFormat;
import java.util.Locale;

import model.Product;
import util.DBHelper;
import util.IconUtils;

public class ProductPanel extends JPanel {

    private JLabel lblTotalBarang;
    private JLabel lblStokTersedia;
    private JLabel lblStokRendah;
    private JLabel lblTotalKategori;

    // Table
    private JTable tblProducts;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbCategoryFilter;
    private JComboBox<String> cbStockFilter;
    private JLabel lblInfo;
    private JPanel paginationPanel;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int ROWS_PER_PAGE = 8;
    private List<Product> allProducts = new ArrayList<>();

    // Form
    private JTextField txtKodeBarang;
    private JTextField txtNamaBarang;
    private JComboBox<String> cbKategori;
    private JSpinner spinStok;
    private JTextField txtSatuan;
    private JTextField txtHargaBeli;
    private JTextField txtHargaJual;
    private JSpinner spinMinStok;
    private JTextArea txtDeskripsi;

    private int selectedProductId = -1;

    public ProductPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ================= TOP (Stats Cards) =================
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        
        JPanel card1 = createStatCard("TOTAL BARANG", lblTotalBarang = new JLabel("0 Barang"), "Semua barang terdaftar", "box", new Color(239, 246, 255), new Color(59, 130, 246));
        JPanel card2 = createStatCard("STOK TERSEDIA", lblStokTersedia = new JLabel("0 Unit"), "Total stok tersedia", "box-check", new Color(220, 252, 231), new Color(34, 197, 94));
        JPanel card3 = createStatCard("STOK RENDAH", lblStokRendah = new JLabel("0 Barang"), "Perlu restock segera", "trend-up", new Color(255, 237, 213), new Color(249, 115, 22));
        JPanel card4 = createStatCard("TOTAL KATEGORI", lblTotalKategori = new JLabel("0 Kategori"), "Kategori barang", "tag", new Color(243, 232, 255), new Color(168, 85, 247));
        
        statsPanel.add(card1);
        statsPanel.add(card2);
        statsPanel.add(card3);
        statsPanel.add(card4);

        add(statsPanel, BorderLayout.NORTH);

        // ================= CENTER (Split) =================
        JPanel splitPanel = new JPanel(new BorderLayout(20, 0));
        splitPanel.setOpaque(false);

        // LEFT: Table
        splitPanel.add(createTablePanel(), BorderLayout.CENTER);

        // RIGHT: Form
        splitPanel.add(createFormPanel(), BorderLayout.EAST);

        add(splitPanel, BorderLayout.CENTER);

        loadProducts();
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
        iconHolder.setPreferredSize(new Dimension(55, 55));
        JLabel icon = new JLabel(IconUtils.getIcon(iconName, 24, iconColor));
        iconHolder.add(icon);
        pnl.add(iconHolder, BorderLayout.WEST);

        JPanel textHolder = new JPanel();
        textHolder.setLayout(new BoxLayout(textHolder, BoxLayout.Y_AXIS));
        textHolder.setOpaque(false);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(100, 116, 139));
        
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLabel.setForeground(new Color(30, 41, 59));
        
        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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

        // Search & Filters
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(240, 42));
        txtSearch.putClientProperty("JComponent.roundRect", true);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari nama barang atau kode...");
        txtSearch.putClientProperty("JTextField.leadingIcon", IconUtils.getIcon("search", 16, new Color(148, 163, 184)));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 12));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { loadProducts(); }
        });

        cbCategoryFilter = new JComboBox<>(new String[]{"Semua Kategori", "ATK", "Seragam", "Buku", "Makanan", "Minuman", "Lainnya"});
        cbCategoryFilter.setPreferredSize(new Dimension(150, 42));
        cbCategoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbCategoryFilter.setBackground(Color.WHITE);
        cbCategoryFilter.addActionListener(e -> loadProducts());

        cbStockFilter = new JComboBox<>(new String[]{"Status Stok", "Tersedia", "Stok Rendah", "Habis"});
        cbStockFilter.setPreferredSize(new Dimension(130, 42));
        cbStockFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbStockFilter.setBackground(Color.WHITE);
        cbStockFilter.addActionListener(e -> loadProducts());

        filterRow.add(txtSearch);
        filterRow.add(cbCategoryFilter);
        filterRow.add(cbStockFilter);
        
        leftPanel.add(filterRow, BorderLayout.NORTH);

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

        String[] columns = {"Kode Barang", "Nama Barang", "Kategori", "Stok", "Satuan", "Harga Jual", "Status", "Aksi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only action column editable
            }
        };
        tblProducts = new JTable(tableModel);
        tblProducts.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblProducts.setRowHeight(48);
        tblProducts.setShowGrid(false);
        tblProducts.setIntercellSpacing(new Dimension(0, 0));
        tblProducts.setSelectionBackground(new Color(241, 245, 249));
        tblProducts.setSelectionForeground(new Color(30, 41, 59));

        tblProducts.getColumnModel().getColumn(0).setPreferredWidth(100);
        tblProducts.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblProducts.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblProducts.getColumnModel().getColumn(3).setPreferredWidth(60);
        tblProducts.getColumnModel().getColumn(4).setPreferredWidth(60);
        tblProducts.getColumnModel().getColumn(5).setPreferredWidth(100);
        tblProducts.getColumnModel().getColumn(6).setPreferredWidth(100);
        tblProducts.getColumnModel().getColumn(7).setPreferredWidth(90);

        tblProducts.setDefaultRenderer(Object.class, new ProductCellRenderer());
        tblProducts.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());
        tblProducts.getColumnModel().getColumn(7).setCellEditor(new ActionEditor());

        tblProducts.getTableHeader().setBackground(Color.WHITE);
        tblProducts.getTableHeader().setForeground(new Color(100, 116, 139));
        tblProducts.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblProducts.getTableHeader().setPreferredSize(new Dimension(100, 42));
        tblProducts.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        tblProducts.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(tblProducts);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Pagination
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        lblInfo = new JLabel("Menampilkan 0 barang");
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
        JLabel formIcon = new JLabel(IconUtils.getIcon("box", 20, new Color(34, 197, 94)));
        JLabel formTitle = new JLabel("Form Data Barang");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(new Color(30, 41, 59));
        formHeader.add(formIcon);
        formHeader.add(formTitle);
        formCard.add(formHeader);
        formCard.add(Box.createRigidArea(new Dimension(0, 20)));

        // Kode Barang
        formCard.add(createFormLabel("Kode Barang"));
        txtKodeBarang = createTextField("Contoh: BRG001");
        formCard.add(txtKodeBarang);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Nama Barang
        formCard.add(createFormLabel("Nama Barang"));
        txtNamaBarang = createTextField("Masukkan nama barang");
        formCard.add(txtNamaBarang);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Kategori
        formCard.add(createFormLabel("Kategori"));
        cbKategori = new JComboBox<>(new String[]{"ATK", "Seragam", "Buku", "Makanan", "Minuman", "Lainnya"});
        cbKategori.setEditable(true);
        cbKategori.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbKategori.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbKategori.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        cbKategori.setAlignmentX(LEFT_ALIGNMENT);
        cbKategori.setBackground(Color.WHITE);
        formCard.add(cbKategori);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Stok & Satuan
        JPanel rowStok = new JPanel(new GridLayout(1, 2, 10, 0));
        rowStok.setOpaque(false);
        rowStok.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        rowStok.setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));
        rowStok.setAlignmentX(LEFT_ALIGNMENT);
        
        JPanel pnlStok = new JPanel(); pnlStok.setLayout(new BoxLayout(pnlStok, BoxLayout.Y_AXIS)); pnlStok.setOpaque(false);
        pnlStok.add(createFormLabel("Stok"));
        spinStok = new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 1));
        spinStok.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinStok.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        spinStok.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        pnlStok.add(spinStok);

        JPanel pnlSatuan = new JPanel(); pnlSatuan.setLayout(new BoxLayout(pnlSatuan, BoxLayout.Y_AXIS)); pnlSatuan.setOpaque(false);
        pnlSatuan.add(createFormLabel("Satuan"));
        txtSatuan = createTextField("Contoh: Pcs");
        pnlSatuan.add(txtSatuan);

        rowStok.add(pnlStok);
        rowStok.add(pnlSatuan);
        formCard.add(rowStok);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Harga Beli & Jual
        JPanel rowHarga = new JPanel(new GridLayout(1, 2, 10, 0));
        rowHarga.setOpaque(false);
        rowHarga.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        rowHarga.setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));
        rowHarga.setAlignmentX(LEFT_ALIGNMENT);
        
        JPanel pnlHB = new JPanel(); pnlHB.setLayout(new BoxLayout(pnlHB, BoxLayout.Y_AXIS)); pnlHB.setOpaque(false);
        pnlHB.add(createFormLabel("Harga Beli"));
        txtHargaBeli = createTextField("Rp 0");
        pnlHB.add(txtHargaBeli);

        JPanel pnlHJ = new JPanel(); pnlHJ.setLayout(new BoxLayout(pnlHJ, BoxLayout.Y_AXIS)); pnlHJ.setOpaque(false);
        pnlHJ.add(createFormLabel("Harga Jual"));
        txtHargaJual = createTextField("Rp 0");
        pnlHJ.add(txtHargaJual);

        rowHarga.add(pnlHB);
        rowHarga.add(pnlHJ);
        formCard.add(rowHarga);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Minimum Stok
        formCard.add(createFormLabel("Minimum Stok"));
        spinMinStok = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        spinMinStok.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinMinStok.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        spinMinStok.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        spinMinStok.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(spinMinStok);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Deskripsi
        formCard.add(createFormLabel("Deskripsi"));
        txtDeskripsi = new JTextArea(3, 20);
        txtDeskripsi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDeskripsi.setLineWrap(true);
        txtDeskripsi.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDeskripsi);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        descScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 80));
        descScroll.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(descScroll);
        formCard.add(Box.createRigidArea(new Dimension(0, 20)));
        formCard.add(Box.createVerticalGlue());

        // Buttons
        JButton btnAdd = createBtn("+ Tambah Baru", new Color(22, 163, 74), Color.WHITE);
        JButton btnUpdate = createBtn("Simpan Perubahan", new Color(59, 130, 246), Color.WHITE);
        JButton btnDeactivate = createBtn("Nonaktif/Aktifkan", new Color(245, 158, 11), Color.WHITE);
        JButton btnRealDelete = createBtn("Hapus Permanen", new Color(220, 38, 38), Color.WHITE);
        JButton btnClear = createBtn("Reset Form", Color.WHITE, new Color(71, 85, 105));
        
        // Adjust clear button border
        btnClear.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        btnAdd.addActionListener(e -> saveProduct(true));
        btnUpdate.addActionListener(e -> saveProduct(false));
        btnDeactivate.addActionListener(e -> deleteProduct());
        btnRealDelete.addActionListener(e -> realDeleteProduct());
        btnClear.addActionListener(e -> clearForm());

        JButton btnRestock = createBtn("Restock", new Color(139, 92, 246), Color.WHITE);
        btnRestock.addActionListener(e -> showRestockDialog());

        JPanel formGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        formGrid.setOpaque(false);
        formGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        formGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        formGrid.add(btnAdd);
        formGrid.add(btnUpdate);
        formGrid.add(btnRestock);
        formGrid.add(btnDeactivate);
        formGrid.add(btnRealDelete);
        formGrid.add(btnClear);

        formCard.add(formGrid);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.setPreferredSize(new Dimension(420, 0));
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

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        return tf;
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

    public void loadProducts() {
        allProducts.clear();
        String search = txtSearch.getText().trim();
        String category = cbCategoryFilter.getSelectedItem().toString();
        String stockStat = cbStockFilter.getSelectedItem().toString();

        StringBuilder sql = new StringBuilder("SELECT * FROM barang WHERE 1=1 ");
        if (!search.isEmpty()) {
            sql.append("AND (nama_barang LIKE '%").append(search).append("%' OR kode_barang LIKE '%").append(search).append("%') ");
        }
        if (!category.equals("Semua Kategori") && !category.isEmpty()) {
            sql.append("AND kategori = '").append(category).append("' ");
        }
        if (stockStat.equals("Habis")) {
            sql.append("AND stok = 0 ");
        } else if (stockStat.equals("Stok Rendah")) {
            sql.append("AND stok > 0 AND stok <= minimum_stok ");
        } else if (stockStat.equals("Tersedia")) {
            sql.append("AND stok > minimum_stok ");
        }
        sql.append("ORDER BY id DESC");

        int totBarang = 0, totStok = 0, totRendah = 0;
        List<String> categories = new ArrayList<>();

        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {
            
            while (rs.next()) {
                Product p = new Product(
                    rs.getInt("id"),
                    rs.getString("kode_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("kategori"),
                    rs.getInt("stok"),
                    rs.getString("satuan"),
                    rs.getDouble("harga_beli"),
                    rs.getDouble("harga"),
                    rs.getInt("minimum_stok"),
                    rs.getString("deskripsi"),
                    rs.getBoolean("is_active")
                );
                allProducts.add(p);
                
                totBarang++;
                totStok += p.getStock();
                if (p.getStock() <= p.getMinimumStock() && p.getStock() > 0) totRendah++;
                if (p.getStock() == 0) totRendah++; // Or keep separate? The prompt says "Perlu restock", so 0 is also restock.
                if (!categories.contains(p.getCategory())) categories.add(p.getCategory());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        lblTotalBarang.setText(totBarang + " Barang");
        lblStokTersedia.setText(totStok + " Unit");
        lblStokRendah.setText(totRendah + " Barang");
        lblTotalKategori.setText(categories.size() + " Kategori");

        // Update category filter dropdown if needed, but we don't want to lose selection
        if (cbCategoryFilter.getItemCount() == 1 && categories.size() > 0) {
            for (String cat : categories) {
                boolean exists = false;
                for (int i = 0; i < cbCategoryFilter.getItemCount(); i++) {
                    if (cbCategoryFilter.getItemAt(i).equals(cat)) { exists = true; break; }
                }
                if (!exists) cbCategoryFilter.addItem(cat);
            }
        }

        totalPages = Math.max(1, (int) Math.ceil((double) allProducts.size() / ROWS_PER_PAGE));
        if (currentPage > totalPages) currentPage = 1;
        showPage(currentPage);
    }

    private void showPage(int page) {
        currentPage = page;
        tableModel.setRowCount(0);
        
        int start = (page - 1) * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, allProducts.size());
        
        for (int i = start; i < end; i++) {
            Product p = allProducts.get(i);
            String status = p.getStock() == 0 ? "Habis" : (p.getStock() <= p.getMinimumStock() ? "Stok Rendah" : "Tersedia");
            if (!p.isActive()) status = "Nonaktif";
            tableModel.addRow(new Object[]{
                p.getProductCode(),
                p.getName(),
                p.getCategory(),
                p.getStock(),
                p.getUnit(),
                "Rp " + formatRp(p.getPrice()),
                status,
                p // Action column uses Product object
            });
        }

        int totalData = allProducts.size();
        if (totalData == 0) {
            lblInfo.setText("Tidak ada data barang");
        } else {
            lblInfo.setText("Menampilkan " + (start + 1) + " - " + end + " dari " + totalData + " barang");
        }

        updatePagination();
    }

    private void updatePagination() {
        paginationPanel.removeAll();
        if (allProducts.size() <= ROWS_PER_PAGE) {
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

    private void saveProduct(boolean isNew) {
        String code = txtKodeBarang.getText().trim();
        String name = txtNamaBarang.getText().trim();
        String cat = cbKategori.getSelectedItem() == null ? "Lainnya" : cbKategori.getSelectedItem().toString();
        String unit = txtSatuan.getText().trim();
        int stock = (int) spinStok.getValue();
        int minStock = (int) spinMinStok.getValue();
        String desc = txtDeskripsi.getText().trim();

        if (code.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode dan Nama Barang wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double hb = 0, hj = 0;
        try {
            hb = Double.parseDouble(txtHargaBeli.getText().replace("Rp", "").replace(".", "").replace(",", "").trim());
            hj = Double.parseDouble(txtHargaJual.getText().replace("Rp", "").replace(".", "").replace(",", "").trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Harga harus berupa angka!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBHelper.getConnection()) {
            if (isNew) {
                String sql = "INSERT INTO barang (kode_barang, nama_barang, kategori, stok, satuan, harga_beli, harga, minimum_stok, deskripsi) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, code);
                    stmt.setString(2, name);
                    stmt.setString(3, cat);
                    stmt.setInt(4, stock);
                    stmt.setString(5, unit);
                    stmt.setDouble(6, hb);
                    stmt.setDouble(7, hj);
                    stmt.setInt(8, minStock);
                    stmt.setString(9, desc);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Barang berhasil ditambahkan!");
                }
            } else {
                if (selectedProductId == -1) {
                    JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu untuk diupdate!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String sql = "UPDATE barang SET kode_barang=?, nama_barang=?, kategori=?, satuan=?, harga_beli=?, harga=?, minimum_stok=?, deskripsi=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, code);
                    stmt.setString(2, name);
                    stmt.setString(3, cat);
                    stmt.setString(4, unit);
                    stmt.setDouble(5, hb);
                    stmt.setDouble(6, hj);
                    stmt.setInt(7, minStock);
                    stmt.setString(8, desc);
                    stmt.setInt(9, selectedProductId);

                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Barang berhasil diperbarui!");
                }
            }
            clearForm();
            loadProducts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        if (selectedProductId == -1) return;
        Product toArchive = null;
        for (Product p : allProducts) {
            if (p.getId() == selectedProductId) {
                toArchive = p;
                break;
            }
        }
        if (toArchive == null) return;
        
        boolean toActive = !toArchive.isActive();
        String actionStr = toActive ? "mengaktifkan kembali" : "menonaktifkan";

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin " + actionStr + " barang ini?\nBarang yang dinonaktifkan tidak akan muncul di halaman kasir.", 
            "Konfirmasi " + (toActive ? "Aktifkan" : "Nonaktifkan"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE barang SET is_active=? WHERE id=?")) {
                stmt.setBoolean(1, toActive);
                stmt.setInt(2, selectedProductId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Status barang berhasil diperbarui!");
                clearForm();
                loadProducts();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void realDeleteProduct() {
        if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu untuk dihapus!", "Pilih Barang", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus barang ini secara PERMANEN?\nCatatan: Jika barang ini sudah pernah terjual, riwayat penjualannya mungkin akan bermasalah!", 
            "Konfirmasi Hapus Permanen", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM barang WHERE id=?")) {
                stmt.setInt(1, selectedProductId);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Data barang berhasil dihapus permanen!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadProducts();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus barang permanen. Pastikan tidak ada data yang terhubung atau hapus riwayat transaksinya terlebih dahulu.\nError: " + e.getMessage(), "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRestockDialog() {
        if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih barang yang ingin di-restock dari tabel terlebih dahulu!", "Pilih Barang", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel pnl = new JPanel(new GridLayout(2, 2, 10, 10));
        pnl.add(new JLabel("Jumlah Tambahan Stok:"));
        JSpinner spinAdd = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        pnl.add(spinAdd);
        pnl.add(new JLabel("Keterangan:"));
        JTextField txtKet = new JTextField("Dari supplier");
        pnl.add(txtKet);

        int result = JOptionPane.showConfirmDialog(this, pnl, "Restock Barang - " + txtNamaBarang.getText(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            int qty = (int) spinAdd.getValue();
            String ket = txtKet.getText();

            try (Connection conn = DBHelper.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    String sqlUpdate = "UPDATE barang SET stok = stok + ? WHERE id = ?";
                    try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                        psUpdate.setInt(1, qty);
                        psUpdate.setInt(2, selectedProductId);
                        psUpdate.executeUpdate();
                    }

                    String sqlLog = "INSERT INTO riwayat_restock (barang_id, jumlah_tambah, keterangan) VALUES (?, ?, ?)";
                    try (PreparedStatement psLog = conn.prepareStatement(sqlLog)) {
                        psLog.setInt(1, selectedProductId);
                        psLog.setInt(2, qty);
                        psLog.setString(3, ket);
                        psLog.executeUpdate();
                    }

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Berhasil menambahkan " + qty + " stok ke " + txtNamaBarang.getText(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadProducts();
                    
                    for (Product p : allProducts) {
                        if (p.getId() == selectedProductId) {
                            spinStok.setValue(p.getStock());
                            break;
                        }
                    }

                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal melakukan restock: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedProductId = -1;
        txtKodeBarang.setText("");
        txtNamaBarang.setText("");
        cbKategori.setSelectedItem("ATK");
        spinStok.setValue(0);
        spinStok.setEnabled(true);
        txtSatuan.setText("");
        txtHargaBeli.setText("");
        txtHargaJual.setText("");
        spinMinStok.setValue(0);
        txtDeskripsi.setText("");
        tblProducts.clearSelection();
    }

    public void editProduct(Product p) {
        selectedProductId = p.getId();
        txtKodeBarang.setText(p.getProductCode());
        txtNamaBarang.setText(p.getName());
        cbKategori.setSelectedItem(p.getCategory());
        spinStok.setValue(p.getStock());
        spinStok.setEnabled(false); // Disable stock edit on update, use Restock button instead
        txtSatuan.setText(p.getUnit());
        txtHargaBeli.setText(String.format("%.0f", p.getPurchasePrice()));
        txtHargaJual.setText(String.format("%.0f", p.getPrice()));
        spinMinStok.setValue(p.getMinimumStock());
        txtDeskripsi.setText(p.getDescription());
    }

    private String formatRp(double val) {
        return NumberFormat.getInstance(new Locale("id", "ID")).format(val);
    }

    // Custom Cell Renderer for general data
    class ProductCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                label.setForeground(new Color(30, 41, 59));
            }
            if (column == 6) { // Status Badge
                String status = (String) value;
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
                panel.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
                
                JLabel badge = new JLabel(status);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                badge.setOpaque(true);
                badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                
                if (status.equals("Tersedia")) {
                    badge.setBackground(new Color(220, 252, 231));
                    badge.setForeground(new Color(21, 128, 61));
                } else if (status.equals("Stok Rendah")) {
                    badge.setBackground(new Color(255, 237, 213));
                    badge.setForeground(new Color(234, 88, 12));
                } else if (status.equals("Habis")) {
                    badge.setBackground(new Color(254, 226, 226));
                    badge.setForeground(new Color(220, 38, 38));
                } else { // Nonaktif
                    badge.setBackground(new Color(241, 245, 249));
                    badge.setForeground(new Color(71, 85, 105));
                }
                panel.add(badge);
                return panel;
            }
            return label;
        }
    }

    // Action Renderer
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

    // Action Editor
    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton btnEdit;
        private JButton btnDelete;
        private Product currentProduct;

        public ActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
            btnEdit = createIconButton("edit", new Color(59, 130, 246));
            btnDelete = createIconButton("trash", new Color(220, 38, 38));
            
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                editProduct(currentProduct);
            });
            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                selectedProductId = currentProduct.getId();
                deleteProduct();
            });

            panel.add(btnEdit);
            panel.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentProduct = (Product) value;
            panel.setBackground(table.getSelectionBackground());
            
            // Dynamic Delete button rendering based on status
            if (currentProduct.isActive()) {
                btnDelete.setIcon(IconUtils.getIcon("trash", 14, new Color(245, 158, 11)));
                btnDelete.setToolTipText("Nonaktifkan");
            } else {
                btnDelete.setIcon(IconUtils.getIcon("reset", 14, new Color(34, 197, 94)));
                btnDelete.setToolTipText("Aktifkan");
            }

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentProduct;
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
