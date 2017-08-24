import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class Email {


    public static void sendEmail(String recipient, String subjectLine, ArrayList<Student> listOfStudents) { //Same method that just works with an ArrayList.
        String body = "";
        for (Student thisStudent : listOfStudents) {
            body = body + thisStudent.toStringWithID() + "\n";
        }
        sendEmail(recipient, subjectLine, body);
    }

    private static void sendEmail(String recipient, String subject, String body) {  //This  will send an email using SSL Authentication, via Gmail's SMTP server.

        //Username/password to use
        final String username = "attendance@nist.ac.th";
        final String password = "xc$%hmTP";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true"); //This is to enable SSL Authentication
        properties.put("mail.smtp.host", "smtp.gmail.com"); //This is Gmail's SMTP Host
        properties.put("mail.smtp.socketFactory.port", "465"); //The Gmail SSL Port
        properties.put("mail.smtp.port", "465"); //SMTP Port
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        Session session = Session.getDefaultInstance(properties, auth);

        body =  "\nClass Names: " + MainPageController.classNames +
                "\n" +
                "\nThese students have not yet arrived :" +
                "\n\n" + body;

        //Now to actually send the email, after the session is created and everything is established.

        try {
            MimeMessage msg = new MimeMessage(session);
            //This sets the message headers to be used.
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.setFrom(new InternetAddress("attendance@nist.ac.th", "NIST Attendance"));
            msg.setReplyTo(InternetAddress.parse("attendance@nist.ac.th", false));
            msg.setSubject(subject, "UTF-8");
            msg.setText(body, "UTF-8");
            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
            Transport.send(msg);
            System.out.println("The email has been sent to " + "\"" + recipient + "\"" + ".");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}