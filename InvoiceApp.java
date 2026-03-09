import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InvoiceApp {
    public InvoiceApp() {
        displayBuyerScreen();
    }

    public void displayBuyerScreen() {
        JFrame frame = new JFrame("Billing System");
        frame.setSize(860, 560);
        frame.setLayout(null);

        JLabel findLabel = new JLabel("Find Customer");
        findLabel.setBounds(10, 12, 110, 30);
        frame.add(findLabel);

        JTextField findField = new JTextField();
        findField.setBounds(125, 12, 160, 30);
        frame.add(findField);

        JButton registerBtn = new JButton("Register New");
        registerBtn.setBounds(720, 12, 120, 30);
        frame.add(registerBtn);

        String headers[] = {
                "ID",
                "Customer Name",
                "Username"
        };

        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // all cells are now non-editable
            }
        };

        JTable buyerTable = new JTable(tableModel);
        buyerTable.setBounds(10, 55, 830, 370);

        JScrollPane scrollPane = new JScrollPane(buyerTable);
        scrollPane.setBounds(10, 55, 830, 370);
        frame.add(scrollPane);

        JButton newBillBtn = new JButton("Create Bill");
        newBillBtn.setBounds(10, 440, 130, 35);
        frame.add(newBillBtn);

        JButton ordersBtn = new JButton("Past Orders");
        ordersBtn.setBounds(150, 440, 140, 35);
        frame.add(ordersBtn);

        JButton exitBtn = new JButton("Exit");
        exitBtn.setBounds(740, 440, 100, 35);
        frame.add(exitBtn);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        populateBuyerTable(tableModel);

        // Action listener for register button
        ActionListener a1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddBuyerForm();
                frame.dispose();
            }
        };

        registerBtn.addActionListener(a1);

        // Key listener for find field
        KeyAdapter k1 = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    String term = findField.getText();

                    if (term.isEmpty()) {
                        populateBuyerTable(tableModel);
                        return;
                    }

                    PreparedStatement ps = null;
                    String query = "select * from customer where username like ? limit 30";

                    try {
                        Connection conn = DBHelper.connect();
                        ps = conn.prepareStatement(query);
                        ps.setString(1, '%' + term + '%');
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

        findField.addKeyListener(k1);

        // Action listener for create bill button
        ActionListener billAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = buyerTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Please select a customer first", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Object id = tableModel.getValueAt(selectedRow, 0);
                Object name = tableModel.getValueAt(selectedRow, 1);
                String stringId = id.toString();
                showBillingScreen(name.toString(), Integer.parseInt(stringId));
                frame.dispose();
            }
        };

        newBillBtn.addActionListener(billAction);

        // Action listener for past orders button
        ActionListener ordersAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = buyerTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Please select a customer first", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Object id = tableModel.getValueAt(selectedRow, 0);
                Object name = tableModel.getValueAt(selectedRow, 1);
                String stringId = id.toString();
                displayOrderHistory(name.toString(), Integer.parseInt(stringId));
                frame.dispose();
            }
        };

        ordersBtn.addActionListener(ordersAction);

        // Action listener for exit button
        ActionListener exitAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        };

        exitBtn.addActionListener(exitAction);
    }

    public void populateBuyerTable(DefaultTableModel tableModel) {
        PreparedStatement ps = null;

        try {
            Connection conn = DBHelper.connect();
            String query = "Select * from customer order by id desc limit 30";
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

    public void showAddBuyerForm() {
        JFrame frame = new JFrame("Register Customer");
        frame.setSize(420, 240);
        frame.setLayout(null);
        frame.setResizable(false);

        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setBounds(20, 20, 120, 30);
        frame.add(fullNameLabel);

        JTextField fullNameField = new JTextField();
        fullNameField.setBounds(150, 20, 220, 30);
        frame.add(fullNameField);

        JLabel handleLabel = new JLabel("Username:");
        handleLabel.setBounds(20, 65, 120, 30);
        frame.add(handleLabel);

        JTextField handleField = new JTextField();
        handleField.setBounds(150, 65, 220, 30);
        frame.add(handleField);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(60, 120, 120, 35);
        frame.add(registerBtn);

        JButton goBackBtn = new JButton("Go Back");
        goBackBtn.setBounds(200, 120, 150, 35);
        frame.add(goBackBtn);

        // Action listener for register button
        ActionListener a1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fullName = fullNameField.getText();
                String handle = handleField.getText();

                if (fullName.isEmpty() || handle.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in both fields", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                PreparedStatement ps = null;
                String query = "Insert into customer (name, username) values (?, ?)";
                try {
                    Connection conn = DBHelper.connect();
                    ps = conn.prepareStatement(query);
                    ps.setString(1, fullName);
                    ps.setString(2, handle);
                    ps.executeUpdate();
                    conn.close();
                    frame.dispose();
                    JOptionPane.showMessageDialog(null, "Customer registered successfully", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    displayBuyerScreen();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getMessage() + ". Please choose a different username", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.out.println(ex.getMessage());
                    return;
                }
            }
        };

        registerBtn.addActionListener(a1);

        // Action listener for go back button
        ActionListener a2 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateHome(frame);
            }
        };

        goBackBtn.addActionListener(a2);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void showBillingScreen(String buyerName, int buyerId) {
        final String[] costVal = { "" };
        final String[] countVal = { "" };
        final Double[] grandTotal = { 0.00 };
        JFrame frame = new JFrame("Create Bill");
        frame.setSize(960, 540);
        frame.setLayout(null);
        frame.setResizable(false);

        JLabel buyerLabel = new JLabel("Billing for:");
        buyerLabel.setBounds(680, 10, 80, 30);
        frame.add(buyerLabel);

        JTextField buyerNameField = new JTextField(buyerName);
        buyerNameField.setBounds(765, 10, 170, 30);
        buyerNameField.setEditable(false);
        frame.add(buyerNameField);

        // Left side - input fields stacked vertically
        JLabel descLabel = new JLabel("Item Description");
        descLabel.setBounds(15, 55, 120, 25);
        frame.add(descLabel);

        JTextField descField = new JTextField();
        descField.setBounds(15, 82, 220, 30);
        SwingUtilities.invokeLater(() -> descField.requestFocusInWindow());
        frame.add(descField);

        JLabel costLabel = new JLabel("Unit Cost");
        costLabel.setBounds(15, 120, 100, 25);
        frame.add(costLabel);

        JTextField costField = new JTextField();
        costField.setBounds(15, 147, 220, 30);
        frame.add(costField);

        JLabel countLabel = new JLabel("Count");
        countLabel.setBounds(15, 185, 100, 25);
        frame.add(countLabel);

        JTextField countField = new JTextField();
        countField.setBounds(15, 212, 220, 30);
        frame.add(countField);

        JButton addItemBtn = new JButton("Add Item");
        addItemBtn.setBounds(15, 258, 105, 30);
        frame.add(addItemBtn);

        JButton removeBtn = new JButton("Remove Item");
        removeBtn.setBounds(130, 258, 105, 30);
        frame.add(removeBtn);

        JButton goBackBtn = new JButton("Go Back");
        goBackBtn.setBounds(15, 300, 220, 30);
        frame.add(goBackBtn);

        // Right side - invoice items table
        String cols[] = { "Description", "Count", "Cost", "Amount" };
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // all cells are now non-editable
            }
        };

        JTable billTable = new JTable(tableModel);
        billTable.setBounds(250, 55, 690, 340);

        JScrollPane scrollPane = new JScrollPane(billTable);
        scrollPane.setBounds(250, 55, 690, 340);
        frame.add(scrollPane);

        JLabel totalLabel = new JLabel("Grand Total:");
        totalLabel.setBounds(700, 405, 100, 30);
        frame.add(totalLabel);

        JTextField totalField = new JTextField();
        totalField.setBounds(805, 405, 130, 30);
        totalField.setEditable(false);
        frame.add(totalField);

        JButton savePrintBtn = new JButton("Save and Print");
        savePrintBtn.setBounds(250, 450, 180, 35);
        frame.add(savePrintBtn);

        // Key listener for cost field
        KeyAdapter k1 = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    String text = costField.getText();
                    if (text.isEmpty() || checkDecimal(text)) {
                        costVal[0] = text;
                        costField.setText(text);
                    } else {
                        costField.setText(costVal[0]);
                    }
                });
            }
        };

        costField.addKeyListener(k1);

        // Key listener for count field
        KeyAdapter k2 = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    String text = countField.getText();
                    if (text.isEmpty() || checkInteger(text)) {
                        countVal[0] = text;
                        countField.setText(text);
                    } else {
                        countField.setText(countVal[0]);
                    }
                });
            }
        };

        countField.addKeyListener(k2);

        // Action listener for add item button
        ActionListener a1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String desc = descField.getText();
                String cost = costField.getText();

                if (desc.isEmpty() || cost.isEmpty() || countField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "All item fields must be filled", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Integer count = Integer.parseInt(countField.getText());
                Double unitCost = Double.parseDouble(cost);
                unitCost = ((long) (unitCost * 100.0)) / 100.0;
                Double amount = count * unitCost;
                grandTotal[0] = grandTotal[0] + amount;

                tableModel.addRow(new Object[] { desc, count, unitCost, amount });
                descField.setText("");
                costField.setText("");
                countField.setText("");
                totalField.setText(grandTotal[0].toString());
                descField.requestFocusInWindow();
            }
        };

        addItemBtn.addActionListener(a1);

        // Action listener for remove button
        ActionListener removeAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = billTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Select a row to remove", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Double removedAmount = Double.parseDouble(tableModel.getValueAt(selectedRow, 3).toString());
                grandTotal[0] = grandTotal[0] - removedAmount;
                tableModel.removeRow(selectedRow);
                totalField.setText(grandTotal[0].toString());
            }
        };

        removeBtn.addActionListener(removeAction);

        // Action listener for go back button
        ActionListener a2 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateHome(frame);
            }
        };

        goBackBtn.addActionListener(a2);

        // Action listener for save and print button
        ActionListener saveAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(null, "Please add at least one item before saving", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Connection conn = null;
                try {
                    conn = DBHelper.connect();
                    conn.setAutoCommit(false);

                    String billQuery = "INSERT INTO invoice (customer_id) VALUES (?)";
                    PreparedStatement billPs = conn.prepareStatement(billQuery, Statement.RETURN_GENERATED_KEYS);
                    billPs.setInt(1, buyerId);
                    billPs.executeUpdate();

                    ResultSet generatedKeys = billPs.getGeneratedKeys();
                    int billId = -1;
                    if (generatedKeys.next()) {
                        billId = generatedKeys.getInt(1);
                    }

                    String lineQuery = "INSERT INTO invoice_item (invoice_id, product_name, unit_price, quantity) VALUES (?, ?, ?, ?)";
                    PreparedStatement linePs = conn.prepareStatement(lineQuery);

                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String itemDesc = tableModel.getValueAt(i, 0).toString();
                        int itemCount = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
                        double itemCost = Double.parseDouble(tableModel.getValueAt(i, 2).toString());

                        linePs.setInt(1, billId);
                        linePs.setString(2, itemDesc);
                        linePs.setDouble(3, itemCost);
                        linePs.setInt(4, itemCount);
                        linePs.addBatch();
                    }

                    linePs.executeBatch();
                    conn.commit();
                    conn.close();

                    JOptionPane.showMessageDialog(null, "Bill #" + billId + " has been saved!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    printReceipt(buyerName, billId, tableModel, grandTotal[0]);

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
                    JOptionPane.showMessageDialog(null, "Failed to save bill: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        savePrintBtn.addActionListener(saveAction);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void displayOrderHistory(String buyerName, int buyerId) {
        JFrame frame = new JFrame("Past Orders - " + buyerName);
        frame.setSize(750, 480);
        frame.setLayout(null);

        JLabel titleLabel = new JLabel("Orders placed by " + buyerName);
        titleLabel.setBounds(15, 10, 450, 30);
        frame.add(titleLabel);

        String headers[] = {
                "Order #",
                "Order Date",
                "Total"
        };

        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // all cells are now non-editable
            }
        };

        JTable ordersTable = new JTable(tableModel);
        ordersTable.setBounds(15, 50, 710, 300);

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBounds(15, 50, 710, 300);
        frame.add(scrollPane);

        JButton detailsBtn = new JButton("View Order Details");
        detailsBtn.setBounds(15, 365, 170, 35);
        frame.add(detailsBtn);

        JButton goBackBtn = new JButton("Go Back");
        goBackBtn.setBounds(195, 365, 120, 35);
        frame.add(goBackBtn);

        populateOrderTable(tableModel, buyerId);

        // Action listener for view details button
        ActionListener a1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = ordersTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Select an order to view its details", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Object orderId = tableModel.getValueAt(selectedRow, 0);
                displayOrderDetails(Integer.parseInt(orderId.toString()));
                frame.dispose();
            }
        };

        detailsBtn.addActionListener(a1);

        // Action listener for go back button
        ActionListener a2 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateHome(frame);
            }
        };

        goBackBtn.addActionListener(a2);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void populateOrderTable(DefaultTableModel tableModel, int buyerId) {
        PreparedStatement ps = null;

        try {
            Connection conn = DBHelper.connect();
            String query = "SELECT id, invoice_date, total_amount FROM invoice WHERE customer_id = ? ORDER BY id DESC";
            ps = conn.prepareStatement(query);
            ps.setInt(1, buyerId);
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

    public void displayOrderDetails(int orderId) {
        JFrame frame = new JFrame("Order #" + orderId);
        frame.setSize(600, 440);
        frame.setLayout(null);

        JLabel titleLabel = new JLabel("Items in Order #" + orderId);
        titleLabel.setBounds(15, 10, 350, 30);
        frame.add(titleLabel);

        String headers[] = {
                "Item",
                "Count",
                "Unit Cost",
                "Amount"
        };

        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // all cells are now non-editable
            }
        };

        JTable itemsTable = new JTable(tableModel);
        itemsTable.setBounds(15, 50, 560, 240);

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBounds(15, 50, 560, 240);
        frame.add(scrollPane);

        populateOrderItems(tableModel, orderId);

        // calculate total from loaded items
        Double orderTotal = 0.00;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            orderTotal = orderTotal + Double.parseDouble(tableModel.getValueAt(i, 3).toString());
        }

        JLabel totalLabel = new JLabel("Order Total: " + String.format("%.2f", orderTotal));
        totalLabel.setBounds(380, 300, 200, 30);
        frame.add(totalLabel);

        JButton goBackBtn = new JButton("Go Back");
        goBackBtn.setBounds(15, 350, 120, 35);
        frame.add(goBackBtn);

        // Action listener for go back button
        ActionListener a1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateHome(frame);
            }
        };

        goBackBtn.addActionListener(a1);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void populateOrderItems(DefaultTableModel tableModel, int orderId) {
        PreparedStatement ps = null;

        try {
            Connection conn = DBHelper.connect();
            String query = "SELECT product_name, quantity, unit_price, total FROM invoice_item WHERE invoice_id = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, orderId);
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

    public void printReceipt(String buyerName, int billId, DefaultTableModel tableModel, double grandTotal) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName("Bill #" + billId + " - " + buyerName);

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
                g2d.drawString("BILL RECEIPT", x, y);
                y += lineHeight + 5;

                // ---- Separator ----
                g2d.drawLine(x, y, pageWidth, y);
                y += 10;

                // ---- Bill info ----
                g2d.setFont(regularFont);
                g2d.drawString("Bill No  : " + billId, x, y);
                y += lineHeight;
                g2d.drawString("Customer : " + buyerName, x, y);
                y += lineHeight;
                String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(new Date());
                g2d.drawString("Date     : " + date, x, y);
                y += lineHeight + 10;

                // ---- Column Headers ----
                g2d.drawLine(x, y, pageWidth, y);
                y += 8;
                g2d.setFont(headerFont);
                g2d.drawString("Description", x, y);
                g2d.drawString("Count", 220, y);
                g2d.drawString("Unit Cost", 290, y);
                g2d.drawString("Amount", 410, y);
                y += 5;
                g2d.drawLine(x, y, pageWidth, y);
                y += lineHeight;

                // ---- Item Rows ----
                g2d.setFont(regularFont);
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String desc = tableModel.getValueAt(i, 0).toString();
                    String count = tableModel.getValueAt(i, 1).toString();
                    String cost = tableModel.getValueAt(i, 2).toString();
                    String amount = tableModel.getValueAt(i, 3).toString();
                    g2d.drawString(desc, x, y);
                    g2d.drawString(count, 220, y);
                    g2d.drawString(cost, 290, y);
                    g2d.drawString(amount, 410, y);
                    y += lineHeight;
                }

                // ---- Grand Total ----
                g2d.drawLine(x, y, pageWidth, y);
                y += lineHeight;
                g2d.setFont(headerFont);
                g2d.drawString(String.format("GRAND TOTAL: %.2f", grandTotal), 280, y);
                y += lineHeight + 10;

                // ---- Footer ----
                g2d.setFont(smallFont);
                g2d.drawLine(x, y, pageWidth, y);
                y += 10;
                g2d.drawString("Thank you for shopping with us!", x, y);

                return PAGE_EXISTS;
            }
        });

        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Print failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void navigateHome(JFrame frame) {
        frame.dispose();
        displayBuyerScreen();
    }

    public static boolean checkDecimal(String text) {
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

    public static boolean checkInteger(String text) {
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
