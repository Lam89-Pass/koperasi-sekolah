package util;

import model.Product;
import model.ShopTransaction;

import java.awt.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;
import java.util.Map;

public class ReceiptPrinter implements Printable {

    private ShopTransaction transaction;
    private String studentName;
    private String studentNis;

    public ReceiptPrinter(ShopTransaction transaction, String studentName, String studentNis) {
        this.transaction = transaction;
        this.studentName = studentName;
        this.studentNis = studentNis;
    }

    public void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        
        // Custom paper size for thermal printer (e.g. 58mm width)
        PageFormat pf = job.defaultPage();
        Paper paper = new Paper();
        double width = 58 * 2.83465; // 58mm in points
        double height = 150 * 2.83465; // 150mm height (dynamic ideally)
        paper.setSize(width, height);
        paper.setImageableArea(10, 10, width - 20, height - 20);
        pf.setPaper(paper);
        
        job.setPrintable(this, pf);

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                System.err.println("Gagal mencetak struk: " + e.getMessage());
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        int y = 20;
        int x = 10;
        int width = (int) pf.getImageableWidth();

        // Fonts
        Font fontTitle = new Font("Monospaced", Font.BOLD, 12);
        Font fontNormal = new Font("Monospaced", Font.PLAIN, 9);
        Font fontBold = new Font("Monospaced", Font.BOLD, 9);

        // Header
        g2d.setFont(fontTitle);
        FontMetrics fmTitle = g2d.getFontMetrics();
        String title = "KOPERASI SEKOLAH";
        g2d.drawString(title, (width - fmTitle.stringWidth(title)) / 2, y);
        y += 15;

        g2d.setFont(fontNormal);
        FontMetrics fmNormal = g2d.getFontMetrics();
        String addr = "Jl. Pendidikan No.1";
        g2d.drawString(addr, (width - fmNormal.stringWidth(addr)) / 2, y);
        y += 15;

        g2d.drawLine(x, y, width - x, y);
        y += 15;

        // Transaction Info
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        g2d.drawString("Tgl  : " + sdf.format(new java.util.Date()), x, y);
        y += 12;
        if (studentName != null) {
            g2d.drawString("Nama : " + studentName, x, y);
            y += 12;
            g2d.drawString("NIS  : " + studentNis, x, y);
            y += 12;
        } else {
            g2d.drawString("Pelanggan Umum (CASH)", x, y);
            y += 12;
        }

        g2d.drawLine(x, y, width - x, y);
        y += 15;

        // Items
        for (Map.Entry<Product, Integer> entry : transaction.getItems().entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            double subtotal = p.getPrice() * qty;

            g2d.drawString(p.getName(), x, y);
            y += 12;
            
            String qtyStr = qty + "x @" + String.format("%,.0f", p.getPrice());
            String subStr = String.format("%,.0f", subtotal);
            
            g2d.drawString(qtyStr, x + 10, y);
            g2d.drawString(subStr, width - x - fmNormal.stringWidth(subStr), y);
            y += 15;
        }

        g2d.drawLine(x, y, width - x, y);
        y += 15;

        // Total
        g2d.setFont(fontBold);
        g2d.drawString("TOTAL:", x, y);
        String totStr = "Rp " + String.format("%,.0f", transaction.getAmount());
        g2d.drawString(totStr, width - x - g2d.getFontMetrics().stringWidth(totStr), y);
        y += 15;
        
        g2d.setFont(fontNormal);
        g2d.drawString("Bayar: " + transaction.getPaymentMethod(), x, y);
        y += 20;

        // Footer
        String footer = "Terima Kasih";
        g2d.drawString(footer, (width - fmNormal.stringWidth(footer)) / 2, y);

        return PAGE_EXISTS;
    }
}
