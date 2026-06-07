package FileHiderProgram.src.main.java.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendOTPService {
    // Gmail email address
    private static final String SENDER_EMAIL = "waheedmaitlounofficial@gmail.com";
    // Gmail app password - set using setPassword() method
    private static String SENDER_PASSWORD = "your-app-password";

    /**
     * Set the Gmail app password for sending OTPs
     * @param password Your Gmail app password
     */
    public static void setPassword(String password) {
        SENDER_PASSWORD = password;
    }

    public static void sendOTP(String email, String genOTP) {
        if (SENDER_PASSWORD.equals("your-app-password")) {
            System.out.println("ERROR: Gmail app password is not configured!");
            System.out.println("Please set the password in Main.java using: SendOTPService.setPassword(\"your-app-password\");");
            System.out.println("To get an app password: https://support.google.com/accounts/answer/185833");
            return;
        }


        String from = SENDER_EMAIL;

        String host = "smtp.gmail.com";

        Properties properties = new Properties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(from, SENDER_PASSWORD);

            }

        });

        session.setDebug(true);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("OTP for File Enc app");

            String body = "Your OTP for File Enc app is: " + genOTP
                    + "\n\nThis OTP is valid for a limited time."
                    + "\nIf you did not request this, please ignore this email.";

            message.setText("Your OTP for File Enc app is: " + genOTP
                    + "\n\nThis OTP is valid for a limited time."
                    + "\nIf you did not request this, please ignore this email.");
            message.saveChanges();

            System.out.println("Sending...");
            Transport.send(message);
            System.out.println("Email sent successfully.");
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            System.out.println("ERROR: Failed to send OTP email");
            mex.printStackTrace();
        }
    }
}
