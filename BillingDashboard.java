import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BillingDashboard {
    public BillingDashboard() {
        showCustomerList();
    }

    public void showCustomerList() {
        JFrame frame = new JFrame("Invoice Manager");
        frame.setSize(950, 520);
        frame.setLayout(null);

        JButton newCustomerBtn = new JButton("New Customer");
        newCustomerBtn.setBounds(10, 10, 140, 30);
        frame.add(newCustomerBtn);

        JButton createInvoiceBtn = new JButton("Create Invoice");
        createInvoiceBtn.setBounds(160, 10, 250, 30);

        JButton viewHistoryBtn = new JButton("View Invoice History");
        viewHistoryBtn.setBounds(420, 10, 200, 30);

        JLabel searchLabel = new JLabel("Search by Username");
        searchLabel.setBounds(690, 10, 140, 30);
        frame.add(searchLabel);

        JTextField searchField = new JTextField();
        searchField.setBounds(830, 10, 100, 30);
        frame.add(searchField);

        String columnName[] = {
                "id",
                "name",
                "username"
        };

        DefaultTableModel tableModel = new DefaultTableModel(columnName, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // all cells are now non-editable
            }
        };

        JTable customerTable = new JTable(tableModel);
        customerTable.setBounds(20, 50, 900, 350);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBounds(20, 50, 900, 350);
        frame.add(scrollPane);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        loadCustomerData(tableModel);

        // Action listener for new customer button
        ActionListener a1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNewCustomerForm();
                frame.dispose();
            }
        };

        newCustomerBtn.addActionListener(a1);

        // Key listener for search field
        KeyAdapter k1 = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    String keyword = searchField.getText();

                    if (keyword.isEmpty()) {
                        loadCustomerData(tableModel);
                        return;
                    }

                    PreparedStatement ps = null;
                    String query = "select * from customer where username like ? limit 20";

                    try {
                        Connection conn = DatabaseHelper.open();
                        ps = conn.prepareStatement(query);
                        ps.setString(1, '%' + keyword + '%');
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            tableModel.addRow(
                                    new Object[] { rs.getInt("id"), rs.getString("name"), rs.getString("username") });
                        }
                        conn.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        };

        searchField.addKeyListener(k1);

        // event to detect row selection in the customer table
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = customerTable.getSelectedRow();

                if (selectedRow != -1) {
                    Object name = tableModel.getValueAt(selectedRow, 1);
                    createInvoiceBtn.setText("Create Invoice for " + name.toString());
                    frame.add(createInvoiceBtn);
                    frame.add(viewHistoryBtn);
                    frame.revalidate();
                    frame.repaint();
                }
            }
        });

        // Action listener for create invoice button
        ActionListener createInvoiceAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object id = tableModel.getValueAt(customerTable.getSelectedRow(), 0);
                Object name = tableModel.getValueAt(customerTable.getSelectedRow(), 1);
                String stringId = id.toString();
                openInvoiceForm(name.toString(), Integer.parseInt(stringId));
                frame.dispose();
            }
        };

        createInvoiceBtn.addActionListener(createInvoiceAction);

        // Action listener for view history button
        ActionListener viewHistoryAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object id = tableModel.getValueAt(customerTable.getSelectedRow(), 0);
                Object name = tableModel.getValueAt(customerTable.getSelectedRow(), 1);
                String stringId = id.toString();
                showInvoiceHistory(name.toString(), Integer.parseInt(stringId));
                frame.dispose();
            }
        };

        viewHistoryBtn.addActionListener(viewHistoryAction);
    }

    public void loadCustomerData(DefaultTableModel tableModel) {
        PreparedStatement ps = null;

        try {
            Connection conn = DatabaseHelper.open();
            String query = "Select * from customer limit 20";
            ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("name"), rs.getString("username")
                });
            }
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    public void openNewCustomerForm() {
        JFrame frame = new JFrame("New Customer");
        frame.setSize(380, 200);
        frame.setLayout(null);
        frame.setResizable(false);

        JLabel nameLabel = new JLabel("Customer Name:");
        nameLabel.setBounds(10, 10, 150, 30);
        frame.add(nameLabel);

        JTextField nameField = new JTextField();
        nameField.setBounds(170, 10, 170, 30);
        frame.add(nameField);

        JLabel usernameLabel = new JLabel("Customer Username:");
        usernameLabel.setBounds(10, 50, 150, 30);
        frame.add(usernameLabel);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(170, 50, 170, 30);
        frame.add(usernameField);

        JButton saveBtn = new JButton("Save");
        saveBtn.setBounds(50, 100, 100, 30);
        frame.add(saveBtn);

        JButton backBtn = new JButton("Back to Customers");
        backBtn.setBounds(160, 100, 180, 30);
        frame.add(backBtn);

        // Action listener for save button
        ActionListener a1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String customerName = nameField.getText();
                String customerUsername = usernameField.getText();

                if (customerName.isEmpty() || customerUsername.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "All fields are required", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                PreparedStatement ps = null;
                String query = "Insert into customer (name, username) values (?, ?)";
                try {
                    Connection conn = DatabaseHelper.open();
                    ps = conn.prepareStatement(query);
                    ps.setString(1, customerName);
                    ps.setString(2, customerUsername);
                    ps.executeUpdate();
                    conn.close();
                    frame.dispose();
                    JOptionPane.showMessageDialog(null, "Customer added successfully", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    showCustomerList();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getMessage() + ". Try a different username", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        };

        saveBtn.addActionListener(a1);

        // Action listener for back button
        ActionListener a2 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackToCustomers(frame);
            }
        };

        backBtn.addActionListener(a2);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void openInvoiceForm(String customerName, int customerId) {
        final String[] priceValue = { "" };
        final String[] qtyValue = { "" };
        final Double[] runningTotal = { 0.00 };
        JFrame frame = new JFrame("New Invoice");
        frame.setSize(1000, 500);
        frame.setLayout(null);
        frame.setResizable(false);

        JLabel customerLabel = new JLabel("Customer");
        customerLabel.setBounds(10, 10, 80, 30);
        frame.add(customerLabel);

        JTextField customerNameField = new JTextField(customerName);
        customerNameField.setBounds(100, 10, 150, 30);
        customerNameField.setEditable(false);
        frame.add(customerNameField);

        JLabel productLabel = new JLabel("Product Name");
        productLabel.setBounds(10, 50, 100, 30);
        frame.add(productLabel);

        JTextField productField = new JTextField();
        productField.setBounds(10, 85, 120, 30);
        SwingUtilities.invokeLater(() -> productField.requestFocusInWindow());
        frame.add(productField);

        JLabel priceLabel = new JLabel("Price");
        priceLabel.setBounds(140, 50, 80, 30);
        frame.add(priceLabel);

        JTextField priceField = new JTextField();
        priceField.setBounds(140, 85, 100, 30);
        frame.add(priceField);

        JLabel qtyLabel = new JLabel("Quantity");
        qtyLabel.setBounds(250, 50, 80, 30);
        frame.add(qtyLabel);

        JTextField qtyField = new JTextField();
        qtyField.setBounds(250, 85, 80, 30);
        frame.add(qtyField);

        JButton addItemBtn = new JButton("Add to Invoice");
        addItemBtn.setBounds(10, 125, 130, 30);
        frame.add(addItemBtn);

        JButton backBtn = new JButton("Back to Customers");
        backBtn.setBounds(150, 125, 180, 30);
        frame.add(backBtn);

        String cells[] = { "Product", "Qty", "Price", "Total" };
        DefaultTableModel tableModel = new DefaultTableModel(cells, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // all cells are now non-editable
            }
        };

        JTable invoiceTable = new JTable(tableModel);
        invoiceTable.setBounds(20, 165, 500, 220);

        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        scrollPane.setBounds(20, 165, 500, 220);
        frame.add(scrollPane);

        JLabel totalLabel = new JLabel("Total Amount");
        totalLabel.setBounds(20, 395, 100, 30);
        frame.add(totalLabel);

        JTextField totalField = new JTextField();
        totalField.setBounds(380, 395, 120, 30);
        totalField.setEditable(false);
        frame.add(totalField);

        JButton submitBtn = new JButton("Save and Print Invoice");
        submitBtn.setBounds(280, 435, 200, 30);
        frame.add(submitBtn);

        // Key listener for price field
        KeyAdapter k1 = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    String text = priceField.getText();
                    if (text.isEmpty() || isValidDecimal(text)) {
                        priceValue[0] = text;
                        priceField.setText(text);
                    } else {
                        priceField.setText(priceValue[0]);
                    }
                });
            }
        };

        priceField.addKeyListener(k1);

        // Key listener for quantity field
        KeyAdapter k2 = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    String text = qtyField.getText();
                    if (text.isEmpty() || isValidInteger(text)) {
                        qtyValue[0] = text;
                        qtyField.setText(text);
                    } else {
                        qtyField.setText(qtyValue[0]);
                    }
                });
            }
        };

        qtyField.addKeyListener(k2);

        // Action listener for add item button
        ActionListener a1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String product = productField.getText();
                String price = priceField.getText();
                Integer quantity = Integer.parseInt(qtyField.getText());
                Double unitPrice = Double.parseDouble(price);
                unitPrice = ((long) (unitPrice * 100.0)) / 100.0;
                Double lineTotal = quantity * unitPrice;
                runningTotal[0] = runningTotal[0] + lineTotal;

                if (product.isEmpty() || price.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Product name and price are required", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                tableModel.addRow(new Object[] { product, quantity, unitPrice, lineTotal });
                productField.setText("");
                priceField.setText("");
                qtyField.setText("");
                totalField.setText(runningTotal[0].toString());
            }
        };

        addItemBtn.addActionListener(a1);

        // Action listener for back button
        ActionListener a2 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackToCustomers(frame);
            }
        };

        backBtn.addActionListener(a2);

        // Action listener for submit button
        ActionListener submitAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(null, "Add at least one item to the invoice", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Connection conn = null;
                try {
                    conn = DatabaseHelper.open();
                    conn.setAutoCommit(false);

                    String invoiceQuery = "INSERT INTO invoice (customer_id) VALUES (?)";
                    PreparedStatement invoicePs = conn.prepareStatement(invoiceQuery, Statement.RETURN_GENERATED_KEYS);
                    invoicePs.setInt(1, customerId);
                    invoicePs.executeUpdate();

                    ResultSet generatedKeys = invoicePs.getGeneratedKeys();
                    int invoiceId = -1;
                    if (generatedKeys.next()) {
                        invoiceId = generatedKeys.getInt(1);
                    }

                    String itemQuery = "INSERT INTO invoice_item (invoice_id, product_name, unit_price, quantity) VALUES (?, ?, ?, ?)";
                    PreparedStatement itemPs = conn.prepareStatement(itemQuery);

                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String productName = tableModel.getValueAt(i, 0).toString();
                        int qty = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
                        double itemPrice = Double.parseDouble(tableModel.getValueAt(i, 2).toString());

                        itemPs.setInt(1, invoiceId);
                        itemPs.setString(2, productName);
                        itemPs.setDouble(3, itemPrice);
                        itemPs.setInt(4, qty);
                        itemPs.addBatch();
                    }

                    itemPs.executeBatch();
                    conn.commit();
                    conn.close();

                    JOptionPane.showMessageDialog(null, "Invoice #" + invoiceId + " saved successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    printInvoiceReceipt(customerName, invoiceId, tableModel, runningTotal[0]);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (conn != null) {
                        try {
                            conn.rollback();
                            conn.close();
                        } catch (SQLException se) {
                            se.printStackTrace();
                        }
                    }
                    JOptionPane.showMessageDialog(null, "Error saving invoice: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        submitBtn.addActionListener(submitAction);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void showInvoiceHistory(String customerName, int customerId) {
        JFrame frame = new JFrame("Invoice History - " + customerName);
        frame.setSize(700, 450);
        frame.setLayout(null);

        JLabel heading = new JLabel("Invoices for " + customerName);
        heading.setBounds(10, 10, 400, 30);
        frame.add(heading);

        String columnName[] = {
                "Invoice #",
                "Date",
                "Total Amount"
        };

        DefaultTableModel tableModel = new DefaultTableModel(columnName, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // all cells are now non-editable
            }
        };

        JTable historyTable = new JTable(tableModel);
        historyTable.setBounds(20, 50, 645, 280);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBounds(20, 50, 645, 280);
        frame.add(scrollPane);

        JButton viewDetailsBtn = new JButton("View Details");
        viewDetailsBtn.setBounds(20, 345, 130, 30);
        frame.add(viewDetailsBtn);

        JButton backBtn = new JButton("Back to Customers");
        backBtn.setBounds(160, 345, 180, 30);
        frame.add(backBtn);

        loadInvoiceHistory(tableModel, customerId);

        // Action listener for view details button
        ActionListener a1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = historyTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Please select an invoice first", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Object invoiceId = tableModel.getValueAt(selectedRow, 0);
                showInvoiceDetails(Integer.parseInt(invoiceId.toString()));
                frame.dispose();
            }
        };

        viewDetailsBtn.addActionListener(a1);

        // Action listener for back button
        ActionListener a2 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackToCustomers(frame);
            }
        };

        backBtn.addActionListener(a2);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void loadInvoiceHistory(DefaultTableModel tableModel, int customerId) {
        PreparedStatement ps = null;

        try {
            Connection conn = DatabaseHelper.open();
            String query = "SELECT id, invoice_date, total_amount FROM invoice WHERE customer_id = ? ORDER BY id DESC";
            ps = conn.prepareStatement(query);
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getInt("id"), rs.getString("invoice_date"), rs.getDouble("total_amount")
                });
            }
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    public void showInvoiceDetails(int invoiceId) {
        JFrame frame = new JFrame("Invoice #" + invoiceId + " Details");
        frame.setSize(550, 400);
        frame.setLayout(null);

        JLabel heading = new JLabel("Invoice #" + invoiceId);
        heading.setBounds(10, 10, 300, 30);
        frame.add(heading);

        String columnName[] = {
                "Product",
                "Qty",
                "Unit Price",
                "Total"
        };

        DefaultTableModel tableModel = new DefaultTableModel(columnName, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // all cells are now non-editable
            }
        };

        JTable detailTable = new JTable(tableModel);
        detailTable.setBounds(20, 50, 500, 220);

        JScrollPane scrollPane = new JScrollPane(detailTable);
        scrollPane.setBounds(20, 50, 500, 220);
        frame.add(scrollPane);

        loadInvoiceItems(tableModel, invoiceId);

        // calculate total from loaded items
        Double totalAmount = 0.00;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            totalAmount = totalAmount + Double.parseDouble(tableModel.getValueAt(i, 3).toString());
        }

        JLabel totalLabel = new JLabel("Total: " + String.format("%.2f", totalAmount));
        totalLabel.setBounds(350, 280, 180, 30);
        frame.add(totalLabel);

        JButton backBtn = new JButton("Back to Customers");
        backBtn.setBounds(20, 320, 180, 30);
        frame.add(backBtn);

        // Action listener for back button
        ActionListener a1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackToCustomers(frame);
            }
        };

        backBtn.addActionListener(a1);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void loadInvoiceItems(DefaultTableModel tableModel, int invoiceId) {
        PreparedStatement ps = null;

        try {
            Connection conn = DatabaseHelper.open();
            String query = "SELECT product_name, quantity, unit_price, total FROM invoice_item WHERE invoice_id = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getString("product_name"), rs.getInt("quantity"),
                        rs.getDouble("unit_price"), rs.getDouble("total")
                });
            }
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    public void printInvoiceReceipt(String customerName, int invoiceId, DefaultTableModel tableModel, double totalAmount) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName("Invoice #" + invoiceId + " - " + customerName);

        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0)
                    return NO_SUCH_PAGE;

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                int x = 10;
                int y = 30;
                int lineHeight = 20;

                Font titleFont = new Font("Arial", Font.BOLD, 18);
                Font headerFont = new Font("Arial", Font.BOLD, 12);
                Font regularFont = new Font("Arial", Font.PLAIN, 12);
                Font smallFont = new Font("Arial", Font.ITALIC, 10);

                int pageWidth = 500;

                // ---- Title ----
                g2d.setFont(titleFont);
                g2d.drawString("BILLING INVOICE", x, y);
                y += lineHeight + 5;

                // ---- Separator ----
                g2d.drawLine(x, y, pageWidth, y);
                y += 10;

                // ---- Invoice info ----
                g2d.setFont(regularFont);
                g2d.drawString("Invoice #: " + invoiceId, x, y);
                y += lineHeight;
                g2d.drawString("Customer : " + customerName, x, y);
                y += lineHeight;
                String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(new Date());
                g2d.drawString("Date     : " + date, x, y);
                y += lineHeight + 10;

                // ---- Table Header ----
                g2d.drawLine(x, y, pageWidth, y);
                y += 8;
                g2d.setFont(headerFont);
                g2d.drawString("Product", x, y);
                g2d.drawString("Qty", 230, y);
                g2d.drawString("Unit Price", 290, y);
                g2d.drawString("Total", 410, y);
                y += 5;
                g2d.drawLine(x, y, pageWidth, y);
                y += lineHeight;

                // ---- Table Rows ----
                g2d.setFont(regularFont);
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String product = tableModel.getValueAt(i, 0).toString();
                    String qty = tableModel.getValueAt(i, 1).toString();
                    String price = tableModel.getValueAt(i, 2).toString();
                    String total = tableModel.getValueAt(i, 3).toString();
                    g2d.drawString(product, x, y);
                    g2d.drawString(qty, 230, y);
                    g2d.drawString(price, 290, y);
                    g2d.drawString(total, 410, y);
                    y += lineHeight;
                }

                // ---- Total ----
                g2d.drawLine(x, y, pageWidth, y);
                y += lineHeight;
                g2d.setFont(headerFont);
                g2d.drawString(String.format("TOTAL AMOUNT: %.2f", totalAmount), 270, y);
                y += lineHeight + 10;

                // ---- Footer ----
                g2d.setFont(smallFont);
                g2d.drawLine(x, y, pageWidth, y);
                y += 10;
                g2d.drawString("Thank you for your purchase!", x, y);

                return PAGE_EXISTS;
            }
        });

        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Printing failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void goBackToCustomers(JFrame frame) {
        frame.dispose();
        showCustomerList();
    }

    public static boolean isValidDecimal(String text) {
        if (text == null || text.isEmpty())
            return false;

        // Only one dot is allowed
        int dotCount = text.length() - text.replace(".", "").length();
        if (dotCount > 1)
            return false;

        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidInteger(String text) {
        if (text == null || text.isEmpty())
            return false;
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
