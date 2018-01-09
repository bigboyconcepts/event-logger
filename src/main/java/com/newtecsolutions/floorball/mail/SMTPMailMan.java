package com.newtecsolutions.floorball.mail;


import com.newtecsolutions.floorball.Consts;
import com.newtecsolutions.floorball.model.Mail;
import com.newtecsolutions.floorball.utils.ConfigManager;
import com.newtecsolutions.floorball.utils.LogUtils;

import org.skynetsoftware.jutils.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by pedja on 9/18/16.
 */

public class SMTPMailMan implements MailMan
{
    private Session session;

    public SMTPMailMan()
    {
        Properties properties = System.getProperties();

        // Setup mail server
        String username = ConfigManager.getInstance().getString(ConfigManager.CONFIG_MAIL_SMTP_USERNAME);
        String password = ConfigManager.getInstance().getString(ConfigManager.CONFIG_MAIL_SMTP_PASSWORD);
        properties.setProperty("mail.smtp.host", ConfigManager.getInstance().getString(ConfigManager.CONFIG_MAIL_SMTP_HOST, "localhost"));

        properties.setProperty("mail.smtp.auth", String.valueOf(ConfigManager.getInstance().getBoolean(ConfigManager.CONFIG_MAIL_SMTP_AUTH, !StringUtils.isEmpty(username) && !StringUtils.isEmpty(password))));
        properties.setProperty("mail.smtp.starttls.enable", String.valueOf(ConfigManager.getInstance().getBoolean(ConfigManager.CONFIG_MAIL_SMTP_STARTTLS_ENABLED, false)));
        properties.setProperty("mail.smtp.ssl.enable", String.valueOf(ConfigManager.getInstance().getBoolean(ConfigManager.CONFIG_MAIL_SMTP_SSL_ENABLED, false)));
        properties.setProperty("mail.smtp.port", String.valueOf(ConfigManager.getInstance().getInt(ConfigManager.CONFIG_MAIL_SMTP_PORT, 465)));

        String sslTrust = ConfigManager.getInstance().getString(ConfigManager.CONFIG_MAIL_SMTP_SSL_TRUST, null);
        if(!StringUtils.isEmpty(sslTrust))
            properties.setProperty("mail.smpt.ssl.trust", sslTrust);

        SMTPAuthenticator authentication = new SMTPAuthenticator(username, password);
        session = Session.getInstance(properties, authentication);
    }

    @Override
    public boolean send(Mail mail)
    {
        LogUtils.info("Sending mail: " + mail.getTo());
        try
        {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(mail.getFrom()));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mail.getTo()));

            // Set Subject: header field
            message.setSubject(mail.getSubject());

            // Send the actual HTML message, as big as you like
            message.setContent(mail.getMessage(), "text/html");

            // Send message
            Transport.send(message);
        }
        catch (MessagingException mex)
        {
            LogUtils.getLogger().log(Level.WARNING, mex.getMessage(), mex);
            mail.setStatus(Mail.Status.failed);
            mail.setStatusMessage(mex.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            mex.printStackTrace(pw);
            mail.setStatusTrace(sw.toString());
            return false;
        }
        return true;
    }

    private static class SMTPAuthenticator extends Authenticator
    {
        private String username, password;

        SMTPAuthenticator(String username, String password)
        {
            super();
            this.username = username;
            this.password = password;
        }

        @Override
        public PasswordAuthentication getPasswordAuthentication()
        {
            if ((username != null) && (username.length() > 0) && (password != null) && (password.length() > 0))
            {
                return new PasswordAuthentication(username, password);
            }

            return null;
        }
    }
}
