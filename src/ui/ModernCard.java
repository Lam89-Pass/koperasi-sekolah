package ui;

import javax.swing.*;
import java.awt.*;

public class ModernCard extends JPanel {
    private int roundness = 28;
    private Color shadowColor = new Color(0, 0, 0, 30);
    private int shadowSize = 5;

    public ModernCard() {
        setOpaque(false);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }

    public ModernCard(int roundness) {
        this();
        this.roundness = roundness;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2.setColor(shadowColor);
        for (int i = 0; i < shadowSize; i++) {
            g2.fillRoundRect(
                shadowSize - i, 
                shadowSize - i, 
                width - (shadowSize - i) * 2, 
                height - (shadowSize - i) * 2, 
                roundness, 
                roundness
            );
        }

        g2.setColor(getBackground());
        g2.fillRoundRect(
            shadowSize, 
            shadowSize, 
            width - shadowSize * 2, 
            height - shadowSize * 2, 
            roundness, 
            roundness
        );

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Insets getInsets() {
        return new Insets(15 + shadowSize, 15 + shadowSize, 15 + shadowSize, 15 + shadowSize);
    }
}
