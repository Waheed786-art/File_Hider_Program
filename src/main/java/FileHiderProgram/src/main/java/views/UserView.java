package FileHiderProgram.src.main.java.views;

import FileHiderProgram.src.main.java.dao.DataDAO;
import FileHiderProgram.src.main.java.model.Data;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

import static FileHiderProgram.src.main.java.views.WelcomeView.*;

public class UserView extends JFrame {

    private final String email;
    private DefaultTableModel tableModel;
    private JTable fileTable;
    private JLabel statusBar;

    public UserView(String email) {
        this.email = email;
        setTitle("File Hider — " + email);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 520);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        refreshTable();
    }

    // ── Header ────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(SURFACE);
        h.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COL),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JLabel logo = label("🔐  File Hider", new Font("Segoe UI Emoji", Font.BOLD, 18), TEXT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel userLbl = label("👤  " + email, FONT_SUB, TEXT_DIM);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(FONT_LABEL);
        logoutBtn.setForeground(ERROR_COL);
        logoutBtn.setBackground(SURFACE);
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ERROR_COL, 1, true),
                new EmptyBorder(4, 14, 4, 14)
        ));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new FileHiderProgram.src.main.java.views.WelcomeView().setVisible(true));
        });

        right.add(userLbl);
        right.add(logoutBtn);

        h.add(logo, BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    // ── Center: table + action panel ─────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(20, 24, 12, 24));

        // Section title + refresh
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel sec = label("Hidden Files", new Font("Segoe UI", Font.BOLD, 16), TEXT);
        JButton refresh = ghostButton("⟳  Refresh");
        refresh.addActionListener(e -> refreshTable());
        top.add(sec,     BorderLayout.WEST);
        top.add(refresh, BorderLayout.EAST);

        // Table
        String[] cols = {"#", "File Name", "Full Path"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        fileTable = new JTable(tableModel);
        styleTable(fileTable);

        JScrollPane scroll = new JScrollPane(fileTable);
        scroll.setBackground(CARD);
        scroll.getViewport().setBackground(CARD);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COL, 1));

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        actions.setOpaque(false);

        JButton hideBtn   = accentButton("＋  Hide a File",   ACCENT,   ACCENT_HOV);
        JButton unhideBtn = accentButton("↑  Unhide Selected", new Color(52, 211, 153), new Color(32, 180, 120));
        hideBtn.setMaximumSize(null);
        unhideBtn.setMaximumSize(null);
        hideBtn.setPreferredSize(new Dimension(160, 40));
        unhideBtn.setPreferredSize(new Dimension(170, 40));

        hideBtn.addActionListener(e   -> hideFile());
        unhideBtn.addActionListener(e -> unhideSelected());

        actions.add(hideBtn);
        actions.add(unhideBtn);

        center.add(top,     BorderLayout.NORTH);
        center.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.BEFORE_FIRST_LINE);
        center.add(scroll,  BorderLayout.CENTER);
        center.add(actions, BorderLayout.SOUTH);

        return center;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        bar.setBackground(SURFACE);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COL));
        statusBar = label(" ", FONT_SUB, TEXT_DIM);
        bar.add(statusBar);
        return bar;
    }

    // ── Data ──────────────────────────────────────────────────────────
    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            List<Data> files = DataDAO.getAllFiles(email);
            for (Data f : files) {
                tableModel.addRow(new Object[]{f.getId(), f.getFileName(), f.getPath()});
            }
            setStatus((files.isEmpty() ? "No hidden files." : files.size() + " file(s) hidden."), true);
        } catch (SQLException ex) {
            setStatus("Error loading files: " + ex.getMessage(), false);
        }
    }

    private void hideFile() {
        JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fc.setDialogTitle("Select a file to hide");
        int res = fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        Data d = new Data(0, f.getName(), f.getAbsolutePath(), email);
        try {
            DataDAO.hideFile(d);
            setStatus("File hidden: " + f.getName(), true);
            refreshTable();
        } catch (Exception ex) {
            setStatus("Error hiding file: " + ex.getMessage(), false);
        }
    }

    private void unhideSelected() {
        int row = fileTable.getSelectedRow();
        if (row < 0) {
            setStatus("Please select a file from the table first.", false);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Unhide \"" + name + "\"?", "Confirm Unhide",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            DataDAO.unhide(id);
            setStatus("File restored: " + name, true);
            refreshTable();
        } catch (Exception ex) {
            setStatus("Error unhiding file: " + ex.getMessage(), false);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private void setStatus(String msg, boolean ok) {
        statusBar.setText(msg);
        statusBar.setForeground(ok ? SUCCESS : ERROR_COL);
    }

    private static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_SUB);
        b.setForeground(TEXT_DIM);
        b.setBackground(SURFACE);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COL, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void styleTable(JTable t) {
        t.setBackground(CARD);
        t.setForeground(TEXT);
        t.setFont(FONT_INPUT);
        t.setRowHeight(38);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(99, 102, 241, 60));
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);

        // Header
        JTableHeader th = t.getTableHeader();
        th.setBackground(SURFACE);
        th.setForeground(TEXT_DIM);
        th.setFont(FONT_LABEL);
        th.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COL));
        th.setReorderingAllowed(false);

        // Column widths
        t.getColumnModel().getColumn(0).setPreferredWidth(40);
        t.getColumnModel().getColumn(0).setMaxWidth(60);
        t.getColumnModel().getColumn(1).setPreferredWidth(200);
        t.getColumnModel().getColumn(2).setPreferredWidth(380);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBackground(isSelected ? new Color(99, 102, 241, 60)
                        : (row % 2 == 0 ? CARD : new Color(32, 32, 44)));
                setForeground(TEXT);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        });
    }
}