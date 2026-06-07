package FileHiderProgram.src.main.java.views;
import FileHiderProgram.src.main.java.service.SendOTPService;
import FileHiderProgram.src.main.java.service.GenerateOTP;
import FileHiderProgram.src.main.java.service.UserService;

import FileHiderProgram.src.main.java.dao.UserDAO;
import FileHiderProgram.src.main.java.model.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class WelcomeView extends JFrame {

    static final Color BG          = new Color(18,  18,  24);
    static final Color SURFACE     = new Color(28,  28,  38);
    static final Color CARD        = new Color(36,  36,  50);
    static final Color ACCENT      = new Color(99,  102, 241); // indigo
    static final Color ACCENT_HOV  = new Color(79,  82,  221);
    static final Color TEXT        = new Color(236, 236, 246);
    static final Color TEXT_DIM    = new Color(140, 140, 165);
    static final Color BORDER_COL  = new Color(55,  55,  75);
    static final Color SUCCESS     = new Color(52,  211, 153);
    static final Color ERROR_COL   = new Color(248, 113, 113);
    static final Font  FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  26);
    static final Font  FONT_SUB    = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font  FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  13);
    static final Font  FONT_INPUT  = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font  FONT_BTN    = new Font("Segoe UI", Font.BOLD,  14);

    public WelcomeView() {
        setTitle("File Hider");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(440, 540);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        add(buildCard(), BorderLayout.CENTER);
    }

    private JPanel buildCard() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);

        JPanel card = new JPanel();
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COL, 1, true),
                new EmptyBorder(40, 44, 40, 44)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(360, 440));

        // Lock icon
        JLabel icon = new JLabel("🔐", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = label("File Hider", FONT_TITLE, TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = label("Secure your files with OTP authentication", FONT_SUB, TEXT_DIM);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COL);
        sep.setBackground(BORDER_COL);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JButton loginBtn  = accentButton("Login",    ACCENT,      ACCENT_HOV);
        JButton signupBtn = accentButton("Sign Up",  SURFACE,     CARD);
        signupBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COL, 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        signupBtn.setForeground(TEXT);

        JButton exitBtn = new JButton("Exit");
        exitBtn.setAlignmentX(CENTER_ALIGNMENT);
        exitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        exitBtn.setBackground(SURFACE);
        exitBtn.setForeground(ERROR_COL);
        exitBtn.setFont(FONT_BTN);
        exitBtn.setFocusPainted(false);
        exitBtn.setBorderPainted(false);
        exitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> System.exit(0));

        loginBtn.addActionListener(e  -> openLogin());
        signupBtn.addActionListener(e -> openSignup());

        card.add(icon);
        card.add(vgap(8));
        card.add(title);
        card.add(vgap(6));
        card.add(sub);
        card.add(vgap(28));
        card.add(sep);
        card.add(vgap(28));
        card.add(loginBtn);
        card.add(vgap(12));
        card.add(signupBtn);
        card.add(vgap(16));
        card.add(exitBtn);

        outer.add(card);
        return outer;
    }

    // ── Login dialog ────────────────────────────────────────────────
    private void openLogin() {
        JDialog dlg = dialog("Login", 400, 360);
        JPanel p = dialogPanel();

        p.add(label("Email Address", FONT_LABEL, TEXT_DIM));
        p.add(vgap(6));
        JTextField emailF = inputField("your@email.com");
        p.add(emailF);
        p.add(vgap(20));

        JButton sendBtn = accentButton("Send OTP", ACCENT, ACCENT_HOV);
        p.add(sendBtn);
        p.add(vgap(20));

        JLabel otpLbl = label("Enter OTP", FONT_LABEL, TEXT_DIM);
        otpLbl.setVisible(false);
        JTextField otpF = inputField("4-digit code");
        otpF.setVisible(false);
        JButton verifyBtn = accentButton("Verify & Login", SUCCESS, SUCCESS.darker());
        verifyBtn.setVisible(false);

        p.add(otpLbl);
        p.add(vgap(6));
        p.add(otpF);
        p.add(vgap(16));
        p.add(verifyBtn);

        JLabel status = statusLabel();
        p.add(vgap(10));
        p.add(status);

        final String[] genOTP = {null};

        sendBtn.addActionListener(e -> {
            String email = emailF.getText().trim();
            if (email.isEmpty()) { setStatus(status, "Please enter your email.", false); return; }
            try {
                if (!UserDAO.isExists(email)) {
                    setStatus(status, "No account found for this email.", false); return;
                }
                genOTP[0] = GenerateOTP.getOTP();
                SendOTPService.sendOTP(email, genOTP[0]);
                setStatus(status, "OTP sent! Check your inbox.", true);
                otpLbl.setVisible(true);
                otpF.setVisible(true);
                verifyBtn.setVisible(true);
                dlg.revalidate(); dlg.repaint();
            } catch (SQLException ex) {
                setStatus(status, "DB error: " + ex.getMessage(), false);
            }
        });

        verifyBtn.addActionListener(e -> {
            if (otpF.getText().trim().equals(genOTP[0])) {
                dlg.dispose();
                SwingUtilities.invokeLater(() ->
                        new UserView(emailF.getText().trim()).setVisible(true));
            } else {
                setStatus(status, "Incorrect OTP. Please try again.", false);
            }
        });

        dlg.add(p);
        dlg.setVisible(true);
    }


    private void openSignup() {
        JDialog dlg = dialog("Create Account", 400, 420);
        JPanel p = dialogPanel();

        p.add(label("Full Name", FONT_LABEL, TEXT_DIM));
        p.add(vgap(6));
        JTextField nameF = inputField("John Doe");
        p.add(nameF);
        p.add(vgap(16));

        p.add(label("Email Address", FONT_LABEL, TEXT_DIM));
        p.add(vgap(6));
        JTextField emailF = inputField("your@email.com");
        p.add(emailF);
        p.add(vgap(20));

        JButton sendBtn = accentButton("Send OTP", ACCENT, ACCENT_HOV);
        p.add(sendBtn);
        p.add(vgap(20));

        JLabel otpLbl = label("Enter OTP", FONT_LABEL, TEXT_DIM);
        otpLbl.setVisible(false);
        JTextField otpF = inputField("4-digit code");
        otpF.setVisible(false);
        JButton regBtn = accentButton("Create Account", SUCCESS, SUCCESS.darker());
        regBtn.setVisible(false);

        p.add(otpLbl);
        p.add(vgap(6));
        p.add(otpF);
        p.add(vgap(16));
        p.add(regBtn);

        JLabel status = statusLabel();
        p.add(vgap(10));
        p.add(status);

        final String[] genOTP = {null};

        sendBtn.addActionListener(e -> {
            String name  = nameF.getText().trim();
            String email = emailF.getText().trim();
            if (name.isEmpty() || email.isEmpty()) {
                setStatus(status, "Please fill in all fields.", false); return;
            }
            genOTP[0] = GenerateOTP.getOTP();
            SendOTPService.sendOTP(email, genOTP[0]);
            setStatus(status, "OTP sent! Check your inbox.", true);
            otpLbl.setVisible(true);
            otpF.setVisible(true);
            regBtn.setVisible(true);
            dlg.revalidate(); dlg.repaint();
        });

        regBtn.addActionListener(e -> {
            if (!otpF.getText().trim().equals(genOTP[0])) {
                setStatus(status, "Incorrect OTP. Please try again.", false); return;
            }
            User user = new User(nameF.getText().trim(), emailF.getText().trim());
            int res = UserService.saveUser(user);
            if (res == 1) {
                setStatus(status, "Account already exists. Please login.", false);
            } else {
                setStatus(status, "Account created! You can now login.", true);
                regBtn.setEnabled(false);
                sendBtn.setEnabled(false);
            }
        });

        dlg.add(p);
        dlg.setVisible(true);
    }

    // ── Helpers ──────────────────────────────────────────────────────
    static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    static JTextField inputField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(FONT_INPUT);
        f.setBackground(new Color(22, 22, 32));
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COL, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        // placeholder effect
        f.setText(placeholder);
        f.setForeground(TEXT_DIM);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT); }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(TEXT_DIM); }
            }
        });
        return f;
    }

    static String fieldValue(JTextField f, String placeholder) {
        String v = f.getText().trim();
        return v.equals(placeholder) ? "" : v;
    }

    static JButton accentButton(String text, Color bg, Color hover) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_BTN);
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return b;
    }

    static JLabel statusLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(FONT_SUB);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    static void setStatus(JLabel l, String msg, boolean ok) {
        l.setText(msg);
        l.setForeground(ok ? SUCCESS : ERROR_COL);
    }

    static Component vgap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    static JDialog dialog(String title, int w, int h) {
        JDialog d = new JDialog();
        d.setTitle(title);
        d.setSize(w, h);
        d.setLocationRelativeTo(null);
        d.setModal(true);
        d.setResizable(false);
        d.getContentPane().setBackground(CARD);
        d.setLayout(new BorderLayout());
        return d;
    }

    static JPanel dialogPanel() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(30, 36, 30, 36));
        return p;
    }
}