import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BillingDashboard {
    private JFrame window;

    public void launch() {
        window = new JFrame("Invoice Manager");
        window.setSize(960, 530);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        displayCustomerPanel();
        window.setVisible(true);
    }

    private void showView(JPanel view) {
        window.setContentPane(view);
        window.revalidate();
        window.repaint();
    }

    private void displayCustomerPanel() {
        JPanel panel = new JPanel(null);

        JLabel heading = new JLabel("Customers");
        heading.setFont(new Font("Arial", Font.BOLD, 16));
        heading.setBounds(10, 8, 200, 28);
        panel.add(heading);

        JButton registerBtn = new JButton("Register Customer");
        registerBtn.setBounds(800, 8, 135, 28);
        panel.add(registerBtn);

        JLabel filterLabel = new JLabel("Search:");
        filterLabel.setBounds(10, 42, 50, 25);
        panel.add(filterLabel);

        JTextField filterField = new JTextField();
        filterField.setBounds(65, 42, 150, 25);
        panel.add(filterField);

        String[] headers = {"ID", "Full Name", "Handle"};
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(10, 75, 925, 310);
        panel.add(sp);

        JButton createInvBtn = new JButton("New Invoice");
        createInvBtn.setBounds(10, 400, 120, 32);
        panel.add(createInvBtn);

        JButton historyBtn = new JButton("Invoice History");
        historyBtn.setBounds(140, 400, 140, 32);
        panel.add(historyBtn);

        fetchCustomers(model, "");

        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegisterDialog(model);
            }
        });

        filterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                model.setRowCount(0);
                fetchCustomers(model, filterField.getText().trim());
            }
        });

        createInvBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(window, "Pick a customer first.", "Notice", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int cid = (int) model.getValueAt(row, 0);
                String cname = model.getValueAt(row, 1).toString();
                displayInvoicePanel(cid, cname);
            }
        });

        historyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(window, "Pick a customer first.", "Notice", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int cid = (int) model.getValueAt(row, 0);
                String cname = model.getValueAt(row, 1).toString();
                displayHistoryPanel(cid, cname);
            }
        });

        showView(panel);
    }

    private void openRegisterDialog(DefaultTableModel customerModel) {
        JDialog dialog = new JDialog(window, "Register Customer", true);
        dialog.setSize(330, 180);
        dialog.setLayout(null);
        dialog.setResizable(false);

        JLabel nl = new JLabel("Name:");
        nl.setBounds(15, 15, 70, 25);
        dialog.add(nl);

        JTextField nameField = new JTextField();
        nameField.setBounds(95, 15, 200, 25);
        dialog.add(nameField);

        JLabel hl = new JLabel("Handle:");
        hl.setBounds(15, 50, 70, 25);
        dialog.add(hl);

        JTextField handleField = new JTextField();
        handleField.setBounds(95, 50, 200, 25);
        dialog.add(handleField);

        JButton saveBtn = new JButton("Save");
        saveBtn.setBounds(120, 90, 90, 30);
        dialog.add(saveBtn);

        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String n = nameField.getText().trim();
                String h = handleField.getText().trim();
                if (n.isEmpty() || h.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Fill in all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    Connection c = DatabaseHelper.open();
                    PreparedStatement ps = c.prepareStatement("INSERT INTO customer (name, username) VALUES (?, ?)");
                    ps.setString(1, n);
                    ps.setString(2, h);
                    ps.executeUpdate();
                    c.close();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(window, "Customer registered.", "Done", JOptionPane.INFORMATION_MESSAGE);
                    customerModel.setRowCount(0);
                    fetchCustomers(customerModel, "");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, ex.getMessage() + ". Try a different handle.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.setLocationRelativeTo(window);
        dialog.setVisible(true);
    }

    private void displayInvoicePanel(int customerId, String customerName) {
        JPanel panel = new JPanel(null);

        JLabel title = new JLabel("Invoice \u2014 " + customerName);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setBounds(10, 8, 500, 28);
        panel.add(title);

        JLabel pl = new JLabel("Product:");
        pl.setBounds(10, 45, 60, 25);
        panel.add(pl);

        JTextField prodField = new JTextField();
        prodField.setBounds(75, 45, 170, 25);
        panel.add(prodField);

        JLabel rl = new JLabel("Rate:");
        rl.setBounds(255, 45, 35, 25);
        panel.add(rl);

        JTextField rateField = new JTextField();
        rateField.setBounds(295, 45, 80, 25);
        panel.add(rateField);

        JLabel ql = new JLabel("Qty:");
        ql.setBounds(385, 45, 30, 25);
        panel.add(ql);

        JTextField qtyField = new JTextField();
        qtyField.setBounds(420, 45, 60, 25);
        panel.add(qtyField);

        JButton appendBtn = new JButton("Add");
        appendBtn.setBounds(495, 45, 70, 25);
        panel.add(appendBtn);

        JButton dropBtn = new JButton("Remove Selected");
        dropBtn.setBounds(575, 45, 140, 25);
        panel.add(dropBtn);

        String[] cols = {"Product", "Qty", "Rate", "Subtotal"};
        DefaultTableModel itemModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable itemTable = new JTable(itemModel);
        JScrollPane sp = new JScrollPane(itemTable);
        sp.setBounds(10, 80, 925, 280);
        panel.add(sp);

        JLabel gl = new JLabel("Grand Total:");
        gl.setFont(new Font("Arial", Font.BOLD, 13));
        gl.setBounds(720, 370, 90, 25);
        panel.add(gl);

        JTextField grandField = new JTextField("0.00");
        grandField.setBounds(815, 370, 120, 25);
        grandField.setEditable(false);
        panel.add(grandField);

        JButton backBtn = new JButton("Back");
        backBtn.setBounds(10, 425, 90, 32);
        panel.add(backBtn);

        JButton saveBtn = new JButton("Save & Print");
        saveBtn.setBounds(110, 425, 130, 32);
        panel.add(saveBtn);

        SwingUtilities.invokeLater(() -> prodField.requestFocusInWindow());

        appendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String product = prodField.getText().trim();
                String rateStr = rateField.getText().trim();
                String qtyStr = qtyField.getText().trim();

                if (product.isEmpty() || rateStr.isEmpty() || qtyStr.isEmpty()) {
                    JOptionPane.showMessageDialog(window, "All item fields are required.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!validateDecimal(rateStr) || !validateInteger(qtyStr)) {
                    JOptionPane.showMessageDialog(window, "Invalid rate or quantity.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double rate = Math.round(Double.parseDouble(rateStr) * 100.0) / 100.0;
                int qty = Integer.parseInt(qtyStr);
                double subtotal = Math.round(rate * qty * 100.0) / 100.0;

                itemModel.addRow(new Object[]{product, qty, rate, subtotal});
                grandField.setText(String.format("%.2f", computeGrandTotal(itemModel)));
                prodField.setText("");
                rateField.setText("");
                qtyField.setText("");
                prodField.requestFocusInWindow();
            }
        });

        dropBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sel = itemTable.getSelectedRow();
                if (sel >= 0) {
                    itemModel.removeRow(sel);
                    grandField.setText(String.format("%.2f", computeGrandTotal(itemModel)));
                }
            }
        });

        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayCustomerPanel();
            }
        });

        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (itemModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(window, "Add at least one item.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int invId = persistInvoice(customerId, itemModel);
                if (invId > 0) {
                    JOptionPane.showMessageDialog(window, "Invoice #" + invId + " saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    generatePrintout(customerName, invId, itemModel, computeGrandTotal(itemModel));
                    displayCustomerPanel();
                }
            }
        });

        showView(panel);
    }

    private void displayHistoryPanel(int customerId, String customerName) {
        JPanel panel = new JPanel(null);

        JLabel heading = new JLabel("Invoices \u2014 " + customerName);
        heading.setFont(new Font("Arial", Font.BOLD, 14));
        heading.setBounds(10, 8, 400, 28);
        panel.add(heading);

        String[] cols = {"Invoice #", "Date", "Amount"};
        DefaultTableModel hModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable hTable = new JTable(hModel);
        JScrollPane sp = new JScrollPane(hTable);
        sp.setBounds(10, 45, 925, 330);
        panel.add(sp);

        JButton detailBtn = new JButton("View Details");
        detailBtn.setBounds(10, 390, 120, 32);
        panel.add(detailBtn);

        JButton backBtn = new JButton("Back");
        backBtn.setBounds(140, 390, 90, 32);
        panel.add(backBtn);

        fetchInvoices(hModel, customerId);

        detailBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = hTable.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(window, "Select an invoice.", "Notice", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int invId = (int) hModel.getValueAt(row, 0);
                openDetailDialog(invId);
            }
        });

        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayCustomerPanel();
            }
        });

        showView(panel);
    }

    private void openDetailDialog(int invoiceId) {
        JDialog dialog = new JDialog(window, "Invoice #" + invoiceId + " Details", true);
        dialog.setSize(520, 380);
        dialog.setLayout(null);

        String[] cols = {"Product", "Qty", "Unit Price", "Line Total"};
        DefaultTableModel dModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable dTable = new JTable(dModel);
        JScrollPane sp = new JScrollPane(dTable);
        sp.setBounds(10, 10, 485, 240);
        dialog.add(sp);

        fetchInvoiceItems(dModel, invoiceId);

        JLabel totalLabel = new JLabel("Total: " + String.format("%.2f", computeGrandTotal(dModel)));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 13));
        totalLabel.setBounds(320, 260, 180, 25);
        dialog.add(totalLabel);

        JButton closeBtn = new JButton("Close");
        closeBtn.setBounds(10, 295, 80, 30);
        dialog.add(closeBtn);

        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setLocationRelativeTo(window);
        dialog.setVisible(true);
    }

    private void fetchCustomers(DefaultTableModel model, String keyword) {
        try {
            Connection c = DatabaseHelper.open();
            PreparedStatement ps;
            if (keyword.isEmpty()) {
                ps = c.prepareStatement("SELECT id, name, username FROM customer ORDER BY id DESC LIMIT 50");
            } else {
                ps = c.prepareStatement("SELECT id, name, username FROM customer WHERE username LIKE ? ORDER BY id DESC LIMIT 50");
                ps.setString(1, "%" + keyword + "%");
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("username")});
            }
            c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fetchInvoices(DefaultTableModel model, int customerId) {
        try {
            Connection c = DatabaseHelper.open();
            PreparedStatement ps = c.prepareStatement(
                "SELECT id, invoice_date, total_amount FROM invoice WHERE customer_id = ? ORDER BY id DESC"
            );
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("invoice_date"), rs.getDouble("total_amount")});
            }
            c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fetchInvoiceItems(DefaultTableModel model, int invoiceId) {
        try {
            Connection c = DatabaseHelper.open();
            PreparedStatement ps = c.prepareStatement(
                "SELECT product_name, quantity, unit_price, total FROM invoice_item WHERE invoice_id = ?"
            );
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("product_name"), rs.getInt("quantity"),
                    rs.getDouble("unit_price"), rs.getDouble("total")
                });
            }
            c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int persistInvoice(int customerId, DefaultTableModel items) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.open();
            conn.setAutoCommit(false);

            PreparedStatement invStmt = conn.prepareStatement(
                "INSERT INTO invoice (customer_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS
            );
            invStmt.setInt(1, customerId);
            invStmt.executeUpdate();

            ResultSet keys = invStmt.getGeneratedKeys();
            int invId = -1;
            if (keys.next()) invId = keys.getInt(1);

            PreparedStatement lineStmt = conn.prepareStatement(
                "INSERT INTO invoice_item (invoice_id, product_name, unit_price, quantity) VALUES (?, ?, ?, ?)"
            );

            for (int i = 0; i < items.getRowCount(); i++) {
                lineStmt.setInt(1, invId);
                lineStmt.setString(2, items.getValueAt(i, 0).toString());
                lineStmt.setDouble(3, Double.parseDouble(items.getValueAt(i, 2).toString()));
                lineStmt.setInt(4, Integer.parseInt(items.getValueAt(i, 1).toString()));
                lineStmt.addBatch();
            }

            lineStmt.executeBatch();
            conn.commit();
            conn.close();
            return invId;
        } catch (Exception ex) {
            ex.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); conn.close(); } catch (SQLException se) { se.printStackTrace(); }
            }
            JOptionPane.showMessageDialog(window, "Failed to save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    private void generatePrintout(String customerName, int invoiceId, DefaultTableModel items, double grandTotal) {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setJobName("Inv-" + invoiceId);

        pj.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
                if (page > 0) return NO_SUCH_PAGE;

                Graphics2D g2 = (Graphics2D) g;
                g2.translate(pf.getImageableX(), pf.getImageableY());

                int left = 15, top = 25, gap = 18;
                int width = 480;

                Font headingFont = new Font("SansSerif", Font.BOLD, 16);
                Font labelFont = new Font("SansSerif", Font.BOLD, 11);
                Font bodyFont = new Font("SansSerif", Font.PLAIN, 11);
                Font footerFont = new Font("SansSerif", Font.ITALIC, 9);

                g2.setFont(headingFont);
                g2.drawString("BILLING INVOICE", left, top);
                top += gap + 8;

                g2.drawLine(left, top, width, top);
                top += 12;

                g2.setFont(bodyFont);
                g2.drawString("Ref: INV-" + invoiceId, left, top);
                top += gap;
                g2.drawString("Customer: " + customerName, left, top);
                top += gap;
                String stamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                g2.drawString("Issued: " + stamp, left, top);
                top += gap + 8;

                g2.drawLine(left, top, width, top);
                top += 6;
                g2.setFont(labelFont);
                g2.drawString("Description", left, top);
                g2.drawString("Qty", 250, top);
                g2.drawString("Price", 310, top);
                g2.drawString("Amount", 400, top);
                top += 4;
                g2.drawLine(left, top, width, top);
                top += gap;

                g2.setFont(bodyFont);
                for (int i = 0; i < items.getRowCount(); i++) {
                    g2.drawString(items.getValueAt(i, 0).toString(), left, top);
                    g2.drawString(items.getValueAt(i, 1).toString(), 250, top);
                    g2.drawString(items.getValueAt(i, 2).toString(), 310, top);
                    g2.drawString(items.getValueAt(i, 3).toString(), 400, top);
                    top += gap;
                }

                g2.drawLine(left, top, width, top);
                top += gap;
                g2.setFont(labelFont);
                g2.drawString(String.format("GRAND TOTAL: %.2f", grandTotal), 290, top);
                top += gap + 8;

                g2.setFont(footerFont);
                g2.drawLine(left, top, width, top);
                top += 12;
                g2.drawString("We appreciate your business.", left, top);

                return PAGE_EXISTS;
            }
        });

        if (pj.printDialog()) {
            try { pj.print(); } catch (PrinterException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(window, "Print error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private double computeGrandTotal(DefaultTableModel model) {
        double sum = 0;
        int lastCol = model.getColumnCount() - 1;
        for (int i = 0; i < model.getRowCount(); i++) {
            sum += Double.parseDouble(model.getValueAt(i, lastCol).toString());
        }
        return Math.round(sum * 100.0) / 100.0;
    }

    public static boolean validateDecimal(String val) {
        try {
            double d = Double.parseDouble(val);
            return d >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean validateInteger(String val) {
        try {
            int i = Integer.parseInt(val);
            return i > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
