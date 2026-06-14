package util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class IconUtils {
    
    public static Icon getIcon(String type, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.translate(x, y);

                float s = size;
                g2.setStroke(new BasicStroke(Math.max(1.5f, size / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                switch (type.toLowerCase()) {
                    case "dashboard":
                        g2.drawRoundRect((int)(s*0.1), (int)(s*0.1), (int)(s*0.35), (int)(s*0.35), 4, 4);
                        g2.drawRoundRect((int)(s*0.55), (int)(s*0.1), (int)(s*0.35), (int)(s*0.35), 4, 4);
                        g2.drawRoundRect((int)(s*0.1), (int)(s*0.55), (int)(s*0.35), (int)(s*0.35), 4, 4);
                        g2.drawRoundRect((int)(s*0.55), (int)(s*0.55), (int)(s*0.35), (int)(s*0.35), 4, 4);
                        break;
                    case "users":
                        g2.drawOval((int)(s*0.35), (int)(s*0.15), (int)(s*0.3), (int)(s*0.3));
                        g2.drawArc((int)(s*0.15), (int)(s*0.55), (int)(s*0.7), (int)(s*0.6), 0, 180);
                        break;
                    case "wallet":
                        g2.drawRoundRect((int)(s*0.1), (int)(s*0.2), (int)(s*0.8), (int)(s*0.6), 6, 6);
                        g2.drawOval((int)(s*0.65), (int)(s*0.45), (int)(s*0.1), (int)(s*0.1));
                        g2.drawLine((int)(s*0.1), (int)(s*0.35), (int)(s*0.9), (int)(s*0.35));
                        break;
                    case "shop":
                        g2.drawRoundRect((int)(s*0.15), (int)(s*0.3), (int)(s*0.7), (int)(s*0.6), 4, 4);
                        g2.drawArc((int)(s*0.3), (int)(s*0.1), (int)(s*0.4), (int)(s*0.4), 0, 180);
                        break;
                    case "box":
                        g2.drawLine((int)(s*0.1), (int)(s*0.25), (int)(s*0.5), (int)(s*0.1));
                        g2.drawLine((int)(s*0.5), (int)(s*0.1), (int)(s*0.9), (int)(s*0.25));
                        g2.drawLine((int)(s*0.1), (int)(s*0.25), (int)(s*0.1), (int)(s*0.75));
                        g2.drawLine((int)(s*0.9), (int)(s*0.25), (int)(s*0.9), (int)(s*0.75));
                        g2.drawLine((int)(s*0.1), (int)(s*0.75), (int)(s*0.5), (int)(s*0.9));
                        g2.drawLine((int)(s*0.9), (int)(s*0.75), (int)(s*0.5), (int)(s*0.9));
                        g2.drawLine((int)(s*0.5), (int)(s*0.5), (int)(s*0.5), (int)(s*0.9));
                        g2.drawLine((int)(s*0.1), (int)(s*0.25), (int)(s*0.5), (int)(s*0.5));
                        g2.drawLine((int)(s*0.9), (int)(s*0.25), (int)(s*0.5), (int)(s*0.5));
                        break;
                    case "report":
                        g2.drawRoundRect((int)(s*0.2), (int)(s*0.1), (int)(s*0.6), (int)(s*0.8), 4, 4);
                        g2.drawLine((int)(s*0.35), (int)(s*0.3), (int)(s*0.65), (int)(s*0.3));
                        g2.drawLine((int)(s*0.35), (int)(s*0.5), (int)(s*0.65), (int)(s*0.5));
                        g2.drawLine((int)(s*0.35), (int)(s*0.7), (int)(s*0.5), (int)(s*0.7));
                        break;
                    case "settings":
                        g2.drawOval((int)(s*0.3), (int)(s*0.3), (int)(s*0.4), (int)(s*0.4));
                        for (int i = 0; i < 8; i++) {
                            double angle = i * Math.PI / 4;
                            int x1 = (int)(s*0.5 + Math.cos(angle)*s*0.2);
                            int y1 = (int)(s*0.5 + Math.sin(angle)*s*0.2);
                            int x2 = (int)(s*0.5 + Math.cos(angle)*s*0.35);
                            int y2 = (int)(s*0.5 + Math.sin(angle)*s*0.35);
                            g2.drawLine(x1, y1, x2, y2);
                        }
                        break;
                    case "koperasi":
                        // Two hands shaking / cooperation symbol
                        g2.setStroke(new BasicStroke(Math.max(2f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        // Left hand arc
                        g2.drawArc((int)(s*0.05), (int)(s*0.25), (int)(s*0.5), (int)(s*0.5), -30, 180);
                        // Right hand arc
                        g2.drawArc((int)(s*0.45), (int)(s*0.25), (int)(s*0.5), (int)(s*0.5), 30, -180);
                        // Top connecting arc (roof)
                        g2.drawArc((int)(s*0.15), (int)(s*0.05), (int)(s*0.7), (int)(s*0.4), 30, 120);
                        // Bottom line
                        g2.drawLine((int)(s*0.2), (int)(s*0.85), (int)(s*0.8), (int)(s*0.85));
                        break;
                    case "shield":
                        g2.drawArc((int)(s*0.1), (int)(s*0.1), (int)(s*0.8), (int)(s*0.4), 0, 180);
                        g2.drawLine((int)(s*0.1), (int)(s*0.3), (int)(s*0.5), (int)(s*0.9));
                        g2.drawLine((int)(s*0.9), (int)(s*0.3), (int)(s*0.5), (int)(s*0.9));
                        break;
                    case "bell":
                        g2.drawArc((int)(s*0.25), (int)(s*0.2), (int)(s*0.5), (int)(s*0.5), 0, 180);
                        g2.drawLine((int)(s*0.25), (int)(s*0.45), (int)(s*0.25), (int)(s*0.7));
                        g2.drawLine((int)(s*0.75), (int)(s*0.45), (int)(s*0.75), (int)(s*0.7));
                        g2.drawLine((int)(s*0.15), (int)(s*0.7), (int)(s*0.85), (int)(s*0.7));
                        g2.drawArc((int)(s*0.4), (int)(s*0.7), (int)(s*0.2), (int)(s*0.2), 0, -180);
                        break;
                    case "avatar":
                        g2.fillOval((int)(s*0.25), (int)(s*0.15), (int)(s*0.5), (int)(s*0.5));
                        g2.fillArc((int)(s*0.1), (int)(s*0.7), (int)(s*0.8), (int)(s*0.8), 0, 180);
                        break;
                    case "chart":
                        g2.drawLine((int)(s*0.1), (int)(s*0.1), (int)(s*0.1), (int)(s*0.9));
                        g2.drawLine((int)(s*0.1), (int)(s*0.9), (int)(s*0.9), (int)(s*0.9));
                        g2.drawLine((int)(s*0.2), (int)(s*0.8), (int)(s*0.4), (int)(s*0.4));
                        g2.drawLine((int)(s*0.4), (int)(s*0.4), (int)(s*0.6), (int)(s*0.6));
                        g2.drawLine((int)(s*0.6), (int)(s*0.6), (int)(s*0.8), (int)(s*0.2));
                        g2.fillOval((int)(s*0.8)-2, (int)(s*0.2)-2, 4, 4);
                        break;
                    case "clock":
                        g2.drawOval((int)(s*0.1), (int)(s*0.1), (int)(s*0.8), (int)(s*0.8));
                        g2.drawLine((int)(s*0.5), (int)(s*0.5), (int)(s*0.5), (int)(s*0.2));
                        g2.drawLine((int)(s*0.5), (int)(s*0.5), (int)(s*0.7), (int)(s*0.5));
                        break;
                    case "logout":
                        g2.drawRoundRect((int)(s*0.1), (int)(s*0.15), (int)(s*0.5), (int)(s*0.7), 4, 4);
                        g2.drawLine((int)(s*0.45), (int)(s*0.5), (int)(s*0.9), (int)(s*0.5));
                        g2.drawLine((int)(s*0.75), (int)(s*0.35), (int)(s*0.9), (int)(s*0.5));
                        g2.drawLine((int)(s*0.75), (int)(s*0.65), (int)(s*0.9), (int)(s*0.5));
                        break;
                    case "user-plus":
                        g2.drawOval((int)(s*0.25), (int)(s*0.15), (int)(s*0.3), (int)(s*0.3));
                        g2.drawArc((int)(s*0.05), (int)(s*0.55), (int)(s*0.7), (int)(s*0.6), 0, 180);
                        g2.drawLine((int)(s*0.75), (int)(s*0.35), (int)(s*0.95), (int)(s*0.35));
                        g2.drawLine((int)(s*0.85), (int)(s*0.25), (int)(s*0.85), (int)(s*0.45));
                        break;
                    case "search":
                        g2.setStroke(new BasicStroke(Math.max(2f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawOval((int)(s*0.15), (int)(s*0.15), (int)(s*0.45), (int)(s*0.45));
                        g2.drawLine((int)(s*0.47), (int)(s*0.47), (int)(s*0.8), (int)(s*0.8));
                        break;
                    case "trash":
                        g2.drawLine((int)(s*0.2), (int)(s*0.25), (int)(s*0.8), (int)(s*0.25));
                        g2.drawLine((int)(s*0.4), (int)(s*0.25), (int)(s*0.4), (int)(s*0.15));
                        g2.drawLine((int)(s*0.6), (int)(s*0.25), (int)(s*0.6), (int)(s*0.15));
                        g2.drawLine((int)(s*0.4), (int)(s*0.15), (int)(s*0.6), (int)(s*0.15));
                        g2.drawRoundRect((int)(s*0.25), (int)(s*0.25), (int)(s*0.5), (int)(s*0.65), 4, 4);
                        g2.drawLine((int)(s*0.4), (int)(s*0.4), (int)(s*0.4), (int)(s*0.75));
                        g2.drawLine((int)(s*0.6), (int)(s*0.4), (int)(s*0.6), (int)(s*0.75));
                        break;
                    case "reset":
                        g2.drawArc((int)(s*0.15), (int)(s*0.15), (int)(s*0.7), (int)(s*0.7), 45, 270);
                        g2.drawLine((int)(s*0.85), (int)(s*0.5), (int)(s*0.85), (int)(s*0.3));
                        g2.drawLine((int)(s*0.85), (int)(s*0.5), (int)(s*0.65), (int)(s*0.5));
                        break;

                    case "arrow-down-box":
                        g2.drawRoundRect((int)(s*0.15), (int)(s*0.15), (int)(s*0.7), (int)(s*0.7), 6, 6);
                        g2.drawLine((int)(s*0.5), (int)(s*0.3), (int)(s*0.5), (int)(s*0.7));
                        g2.drawLine((int)(s*0.5), (int)(s*0.7), (int)(s*0.3), (int)(s*0.5));
                        g2.drawLine((int)(s*0.5), (int)(s*0.7), (int)(s*0.7), (int)(s*0.5));
                        break;
                    case "arrow-up-box":
                        g2.drawRoundRect((int)(s*0.15), (int)(s*0.15), (int)(s*0.7), (int)(s*0.7), 6, 6);
                        g2.drawLine((int)(s*0.5), (int)(s*0.7), (int)(s*0.5), (int)(s*0.3));
                        g2.drawLine((int)(s*0.5), (int)(s*0.3), (int)(s*0.3), (int)(s*0.5));
                        g2.drawLine((int)(s*0.5), (int)(s*0.3), (int)(s*0.7), (int)(s*0.5));
                        break;
                    case "trend-up":
                        g2.drawRoundRect((int)(s*0.15), (int)(s*0.15), (int)(s*0.7), (int)(s*0.7), 6, 6);
                        g2.drawLine((int)(s*0.25), (int)(s*0.75), (int)(s*0.45), (int)(s*0.55));
                        g2.drawLine((int)(s*0.45), (int)(s*0.55), (int)(s*0.55), (int)(s*0.65));
                        g2.drawLine((int)(s*0.55), (int)(s*0.65), (int)(s*0.75), (int)(s*0.35));
                        g2.drawLine((int)(s*0.75), (int)(s*0.35), (int)(s*0.65), (int)(s*0.35));
                        g2.drawLine((int)(s*0.75), (int)(s*0.35), (int)(s*0.75), (int)(s*0.45));
                        break;
                    case "arrow-down":
                        g2.drawLine((int)(s*0.5), (int)(s*0.2), (int)(s*0.5), (int)(s*0.8));
                        g2.drawLine((int)(s*0.5), (int)(s*0.8), (int)(s*0.3), (int)(s*0.6));
                        g2.drawLine((int)(s*0.5), (int)(s*0.8), (int)(s*0.7), (int)(s*0.6));
                        break;
                    case "arrow-up":
                        g2.drawLine((int)(s*0.5), (int)(s*0.8), (int)(s*0.5), (int)(s*0.2));
                        g2.drawLine((int)(s*0.5), (int)(s*0.2), (int)(s*0.3), (int)(s*0.4));
                        g2.drawLine((int)(s*0.5), (int)(s*0.2), (int)(s*0.7), (int)(s*0.4));
                        break;
                    case "calendar":
                        g2.drawRoundRect((int)(s*0.1), (int)(s*0.2), (int)(s*0.8), (int)(s*0.7), 4, 4);
                        g2.drawLine((int)(s*0.1), (int)(s*0.4), (int)(s*0.9), (int)(s*0.4));
                        g2.drawLine((int)(s*0.3), (int)(s*0.1), (int)(s*0.3), (int)(s*0.3));
                        g2.drawLine((int)(s*0.7), (int)(s*0.1), (int)(s*0.7), (int)(s*0.3));
                        break;
                    case "filter":
                        g2.drawLine((int)(s*0.2), (int)(s*0.3), (int)(s*0.8), (int)(s*0.3));
                        g2.drawLine((int)(s*0.35), (int)(s*0.55), (int)(s*0.65), (int)(s*0.55));
                        g2.drawLine((int)(s*0.45), (int)(s*0.8), (int)(s*0.55), (int)(s*0.8));
                        break;
                    case "box-check":
                        g2.drawLine((int)(s*0.1), (int)(s*0.25), (int)(s*0.5), (int)(s*0.1));
                        g2.drawLine((int)(s*0.5), (int)(s*0.1), (int)(s*0.9), (int)(s*0.25));
                        g2.drawLine((int)(s*0.1), (int)(s*0.25), (int)(s*0.1), (int)(s*0.75));
                        g2.drawLine((int)(s*0.9), (int)(s*0.25), (int)(s*0.9), (int)(s*0.75));
                        g2.drawLine((int)(s*0.1), (int)(s*0.75), (int)(s*0.5), (int)(s*0.9));
                        g2.drawLine((int)(s*0.9), (int)(s*0.75), (int)(s*0.5), (int)(s*0.9));
                        g2.drawLine((int)(s*0.5), (int)(s*0.5), (int)(s*0.5), (int)(s*0.9));
                        g2.drawLine((int)(s*0.1), (int)(s*0.25), (int)(s*0.5), (int)(s*0.5));
                        g2.drawLine((int)(s*0.9), (int)(s*0.25), (int)(s*0.5), (int)(s*0.5));
                        g2.setColor(new Color(34, 197, 94));
                        g2.setStroke(new BasicStroke(Math.max(2f, size / 8f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine((int)(s*0.6), (int)(s*0.5), (int)(s*0.75), (int)(s*0.65));
                        g2.drawLine((int)(s*0.75), (int)(s*0.65), (int)(s*0.95), (int)(s*0.35));
                        break;
                    case "tag":
                        g2.drawPolygon(new int[]{(int)(s*0.1), (int)(s*0.6), (int)(s*0.9), (int)(s*0.4)}, 
                                       new int[]{(int)(s*0.4), (int)(s*0.9), (int)(s*0.6), (int)(s*0.1)}, 4);
                        g2.drawOval((int)(s*0.3), (int)(s*0.3), (int)(s*0.1), (int)(s*0.1));
                        break;
                    case "edit":
                        g2.drawLine((int)(s*0.2), (int)(s*0.8), (int)(s*0.4), (int)(s*0.8));
                        g2.drawPolygon(new int[]{(int)(s*0.2), (int)(s*0.3), (int)(s*0.8), (int)(s*0.7)},
                                       new int[]{(int)(s*0.8), (int)(s*0.9), (int)(s*0.4), (int)(s*0.3)}, 4);
                        g2.drawLine((int)(s*0.6), (int)(s*0.2), (int)(s*0.9), (int)(s*0.5));
                        break;
                    default:
                        g2.drawRect((int)(s*0.2), (int)(s*0.2), (int)(s*0.6), (int)(s*0.6));
                }
                
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }

            @Override
            public int getIconHeight() { return size; }
        };
    }
}
