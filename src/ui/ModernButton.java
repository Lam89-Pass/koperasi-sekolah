package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {
    private Color colorStart = new Color(52, 152, 219);
    private Color colorEnd = new Color(41, 128, 185);  
    private Color hoverStart = new Color(41, 128, 185);
    private Color hoverEnd = new Color(31, 97, 141);
    private boolean isHovered = false;
    private int roundness = 15;

    public ModernButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public ModernButton(String text, Color start, Color end) {
        this(text);
        this.colorStart = start;
        this.colorEnd = end;
        this.hoverStart = start.darker();
        this.hoverEnd = end.darker();
    }

    public void setRoundness(int roundness) {
        this.roundness = roundness;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color start = isHovered ? hoverStart : colorStart;
        Color end = isHovered ? hoverEnd : colorEnd;

        GradientPaint gp = new GradientPaint(0, 0, start, 0, getHeight(), end);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), roundness, roundness);

        g2.setColor(Color.WHITE);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(getText())) / 2;
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}

