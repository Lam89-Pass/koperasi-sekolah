package ui;

import model.User;
import util.DBHelper;
import util.IconUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("Login - Koperasi Sekolah");
        setSize(850, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true); // Modern borderless window
        setShape(new RoundRectangle2D.Double(0, 0, 850, 500, 20, 20)); // Rounded corners

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(Color.WHITE);

        // ================= LEFT PANEL (GRADIENT BRANDING) =================
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Premium Green to Deep Teal Gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(16, 185, 129),
                        0, getHeight(), new Color(15, 118, 110)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        leftPanel.setPreferredSize(new Dimension(400, 500));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(80, 40, 40, 40));

        JLabel lblIcon = new JLabel(IconUtils.getIcon("shop", 64, Color.WHITE));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblBrandTitle = new JLabel("KOPERASI SEKOLAH");
        lblBrandTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblBrandTitle.setForeground(Color.WHITE);
        lblBrandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblBrandSubtitle = new JLabel("<html><center>Sistem Manajemen Keuangan<br>dan Point of Sale Terpadu</center></html>");
        lblBrandSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblBrandSubtitle.setForeground(new Color(209, 250, 229));
        lblBrandSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(lblIcon);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        leftPanel.add(lblBrandTitle);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(lblBrandSubtitle);
        leftPanel.add(Box.createVerticalGlue());

        // ================= RIGHT PANEL (LOGIN FORM) =================
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(50, 60, 50, 60));

        // Close button at top right
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lblClose = new JLabel(IconUtils.getIcon("trash", 20, new Color(148, 163, 184))); // using trash as close placeholder if "close" doesn't exist, better text "X"
        lblClose.setText("X");
        lblClose.setIcon(null);
        lblClose.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblClose.setForeground(new Color(148, 163, 184));
        lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClose.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { System.exit(0); }
            public void mouseEntered(MouseEvent e) { lblClose.setForeground(new Color(239, 68, 68)); }
            public void mouseExited(MouseEvent e) { lblClose.setForeground(new Color(148, 163, 184)); }
        });
        topBar.add(lblClose);

        JLabel lblLoginTitle = new JLabel("Selamat Datang");
        lblLoginTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblLoginTitle.setForeground(new Color(30, 41, 59));
        lblLoginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLoginSub = new JLabel("Silakan masuk ke akun Anda");
        lblLoginSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblLoginSub.setForeground(new Color(100, 116, 139));
        lblLoginSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Form Fields
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsername.setForeground(new Color(71, 85, 105));
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        txtUsername.setPreferredSize(new Dimension(Integer.MAX_VALUE, 45));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassword.setForeground(new Color(71, 85, 105));
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        txtPassword.setPreferredSize(new Dimension(Integer.MAX_VALUE, 45));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login Button
        btnLogin = new JButton("Masuk Sekarang") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(15, 118, 110));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(52, 211, 153));
                } else {
                    g2.setColor(new Color(16, 185, 129));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnLogin.setPreferredSize(new Dimension(Integer.MAX_VALUE, 45));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogin.addActionListener(e -> attemptLogin());
        
        // Add Enter Key binding
        getRootPane().setDefaultButton(btnLogin);

        // Assembly Right Panel
        rightPanel.add(topBar);
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(lblLoginTitle);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(lblLoginSub);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 35)));
        rightPanel.add(lblUsername);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        rightPanel.add(txtUsername);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        rightPanel.add(lblPassword);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        rightPanel.add(txtPassword);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        rightPanel.add(btnLogin);
        rightPanel.add(Box.createVerticalGlue());

        mainContent.add(leftPanel, BorderLayout.WEST);
        mainContent.add(rightPanel, BorderLayout.CENTER);

        add(mainContent);
    }

    private void attemptLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBHelper.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User loggedInUser = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getTimestamp("created_at")
                        );
                        MainFrame mainFrame = new MainFrame(loggedInUser);
                        mainFrame.setVisible(true);
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Username atau Password salah!", "Error Login", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan koneksi database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
