package com.tce.hdcheck;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Date;
import java.util.Properties;
import java.text.SimpleDateFormat;

public class SendEmailTLS {
    static final String username = "hdspacesmonitor@gmail.com";
    static final String password = "Psapi123!";
	 
	//emailTo="fan8118@gmail.com, carol@gmail.com"
    public static String send(String gmailAccount, String gamilPassword,String emailTo, String subject, String body) {
    	//username=gmailAccount;
    	//password=gamilPassword;
        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS
        
        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

        	String emails=emailTo;
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("hdspacesmonitor@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(emailTo)
            );
            
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);

            System.out.println("Done at " + getCurrentTime());
            return "OK";
        } catch (MessagingException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static String getCurrentTime(){
    	try{
    		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		Date date = new Date(System.currentTimeMillis());
    		return formatter.format(date);
    	}catch (Exception e){
    		return "";
    	}
    }
}