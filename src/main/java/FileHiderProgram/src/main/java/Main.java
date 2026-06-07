package FileHiderProgram.src.main.java;

import FileHiderProgram.src.main.java.views.WelcomeView;
import FileHiderProgram.src.main.java.service.SendOTPService;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SendOTPService.setPassword("elbz fekn attj wsrf");
        SwingUtilities.invokeLater(() -> new WelcomeView().setVisible(true));
    }
}