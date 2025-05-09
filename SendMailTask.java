//package com.example.shreyash;
//
//import android.os.AsyncTask;
//import android.util.Log;
//import java.io.UnsupportedEncodingException;
//import java.util.List;
//import java.util.Properties;
//import javax.mail.*;
//import javax.mail.internet.*;
//
//public class SendMailTask extends AsyncTask<Void, Void, Void> {
//    private String userEmail = "shreyash4585@gmail.com";  // Your Gmail
//    private String userPassword = "lbfb zjyh sthh twbg";  // Use App Password
//    private List<String> emailList;  // List of selected student emails
//
//    public SendMailTask(List<String> selectedEmails) {
//        this.emailList = selectedEmails;
//    }
//
//    @Override
//    protected Void doInBackground(Void... voids) {
//        if (!emailList.isEmpty()) {
//            sendEmailsInParallel();
//        } else {
//            Log.e("SendMailTask", "No student emails selected.");
//        }
//        return null;
//    }
//
//    private void sendEmailsInParallel() {
//        Log.d("SendMailTask", "Preparing to send emails...");
//
//        try {
//            Properties props = new Properties();
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.starttls.enable", "true");
//            props.put("mail.smtp.host", "smtp.gmail.com");
//            props.put("mail.smtp.port", "587");
//
//            Session session = Session.getInstance(props, new Authenticator() {
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication(userEmail, userPassword);
//                }
//            });
//
//            // Send emails using multi-threading for better performance
//            Thread emailThread = new Thread(() -> {
//                try {
//                    Message message = new MimeMessage(session);
//
//                    // ✅ Fix: Handle UnsupportedEncodingException
//                    try {
//                        message.setFrom(new InternetAddress(userEmail, "Shreyash Nahate")); // Custom sender name
//                    } catch (UnsupportedEncodingException e) {
//                        Log.e("SendMailTask", "Encoding error in sender name", e);
//                        message.setFrom(new InternetAddress(userEmail)); // Fallback
//                    }
//
//                    message.setReplyTo(InternetAddress.parse("support@yourcollege.com")); // Helps in avoiding spam
//
//                    // Add all recipients using BCC
//                    for (String email : emailList) {
//                        message.addRecipient(Message.RecipientType.BCC, new InternetAddress(email));
//                    }
//
//                    // Set Headers to avoid spam
//                    message.setHeader("Content-Type", "text/html; charset=UTF-8"); // HTML email format
//                    message.setHeader("X-Priority", "1"); // High priority
//                    message.setHeader("X-Mailer", "AndroidMailer"); // Identifies sender
//                    message.setHeader("Precedence", "bulk"); // Helps in bulk mailing legitimacy
//
//                    // Email Content (Formatted in HTML for better readability)
//                    String emailBody = "<h2>Hello Student,</h2>"
//                            + "<p>This is an <strong>official announcement</strong> from your college.</p>"
//                            + "<p>Please check your portal for further details.</p>"
//                            + "<br><p>Regards,<br><strong>Your College</strong></p>";
//
//                    message.setSubject("📢 Important College Notice");
//                    message.setContent(emailBody, "text/html; charset=utf-8");
//
//                    // Send Email
//                    Transport.send(message);
//                    Log.d("SendMailTask", "✅ Email sent successfully to all recipients!");
//
//                } catch (MessagingException e) {
//                    Log.e("SendMailTask", "❌ Error sending email", e);
//                }
//            });
//
//            emailThread.start(); // Start email thread
//
//        } catch (Exception e) {
//            Log.e("SendMailTask", "❌ Error preparing email", e);
//        }
//    }
//}
