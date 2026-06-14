package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import util.IconUtils;

public class DatePicker extends JPanel {
    private JTextField txtDate;
    private JButton btnIcon;
    private JPopupMenu popup;
    private Calendar currentCalendar;
    private JPanel daysPanel;
    private JLabel lblMonthYear;
    private Date selectedDate;
    private Runnable onDateSelected;

    public DatePicker() {
        setLayout(new BorderLayout());
        setOpaque(false);

        txtDate = new JTextField();
        txtDate.setEditable(false);
        txtDate.setBackground(Color.WHITE);
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDate.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 10));
        txtDate.putClientProperty("JComponent.roundRect", true);
        txtDate.putClientProperty("JTextField.placeholderText", "Semua Tanggal");

        btnIcon = new JButton(IconUtils.getIcon("calendar", 16, new Color(148, 163, 184)));
        btnIcon.setBackground(Color.WHITE);
        btnIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        btnIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIcon.setFocusPainted(false);
        btnIcon.setContentAreaFilled(false);

        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setBackground(Color.WHITE);
        fieldPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder()
        ));
        fieldPanel.add(txtDate, BorderLayout.CENTER);
        fieldPanel.add(btnIcon, BorderLayout.EAST);

        add(fieldPanel, BorderLayout.CENTER);

        currentCalendar = Calendar.getInstance();
        popup = new JPopupMenu();
        popup.add(createCalendarPanel());

        ActionListener showPopup = e -> {
            updateCalendarPanel();
            popup.show(fieldPanel, 0, fieldPanel.getHeight());
        };
        btnIcon.addActionListener(showPopup);
        txtDate.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showPopup.actionPerformed(null); }
        });
    }

    public void setOnDateSelected(Runnable runnable) {
        this.onDateSelected = runnable;
    }

    public String getSelectedDateStr() {
        if (selectedDate == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
    }

    public void clear() {
        selectedDate = null;
        txtDate.setText("");
        if(onDateSelected != null) onDateSelected.run();
    }

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JButton btnPrev = new JButton("<");
        btnPrev.setFocusPainted(false);
        btnPrev.addActionListener(e -> { currentCalendar.add(Calendar.MONTH, -1); updateCalendarPanel(); });

        JButton btnNext = new JButton(">");
        btnNext.setFocusPainted(false);
        btnNext.addActionListener(e -> { currentCalendar.add(Calendar.MONTH, 1); updateCalendarPanel(); });

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 14));

        header.add(btnPrev, BorderLayout.WEST);
        header.add(lblMonthYear, BorderLayout.CENTER);
        header.add(btnNext, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
        daysPanel.setBackground(Color.WHITE);
        panel.add(daysPanel, BorderLayout.CENTER);

        JButton btnClear = new JButton("Reset Tanggal");
        btnClear.setBackground(new Color(241, 245, 249));
        btnClear.setFocusPainted(false);
        btnClear.addActionListener(e -> {
            clear();
            popup.setVisible(false);
        });
        panel.add(btnClear, BorderLayout.SOUTH);

        return panel;
    }

    private void updateCalendarPanel() {
        daysPanel.removeAll();
        lblMonthYear.setText(new SimpleDateFormat("MMMM yyyy").format(currentCalendar.getTime()));

        String[] days = {"Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            daysPanel.add(lbl);
        }

        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; 
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOfWeek; i++) {
            daysPanel.add(new JLabel(""));
        }

        for (int i = 1; i <= daysInMonth; i++) {
            final int day = i;
            JButton btnDay = new JButton(String.valueOf(i));
            btnDay.setFocusPainted(false);
            btnDay.setBackground(Color.WHITE);
            btnDay.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249)));
            btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (selectedDate != null) {
                Calendar selCal = Calendar.getInstance();
                selCal.setTime(selectedDate);
                if (selCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    selCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                    selCal.get(Calendar.DAY_OF_MONTH) == day) {
                    btnDay.setBackground(new Color(59, 130, 246));
                    btnDay.setForeground(Color.WHITE);
                }
            }

            btnDay.addActionListener(e -> {
                Calendar sel = (Calendar) currentCalendar.clone();
                sel.set(Calendar.DAY_OF_MONTH, day);
                selectedDate = sel.getTime();
                txtDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(selectedDate));
                popup.setVisible(false);
                if (onDateSelected != null) {
                    onDateSelected.run();
                }
            });
            daysPanel.add(btnDay);
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }
}

