import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * An object for sending out an email. Synchronization to this object should be handled
 * outside of it in multiple threads.
 * @author Cory Ma
 */
public class Emailer
{
	private final String username;
    private final String password;
    private Session session;
    
    /**
     * Constructor
     * @param emailAddress      Email address to send from, currently has to be a Gmail address
     * @param emailPassword     Password for email
     */
    public Emailer(String emailAddress, String emailPassword)
    {
    	username = emailAddress;
    	password = emailPassword;
    	
    	initSession();
    }
    
    /**
     * Sends an email.
     * @param recepientList     List of email addresses to send message to
     * @param subject           Subject of email
     * @param body              Body of email
     */
    public void sendMessage(String recepientList, String subject, String body)
	{
		try
        {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recepientList));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println(subject + " Sent");
        }
		catch(MessagingException e)
		{
            throw new RuntimeException(e);
        }
	}
    
    /**
     * Initializes the session to send emails with, currently configured for gmail addresses.
     */
	private void initSession()
	{
		Properties props = new Properties();
		//Currently only set up for a gmail address
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        session = Session.getInstance(props,
        		new javax.mail.Authenticator()
        		{
            		protected PasswordAuthentication getPasswordAuthentication()
            		{
            			return new PasswordAuthentication(username, password);
            		}
        		});
	}
}