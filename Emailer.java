import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Emailer
{
	private final String username = "";
    private final String password = "";
    private String emailRecepientList, emailSubject, emailBody;
    private Session session;
    
    Emailer()
    {
    	initSession();
    	emailRecepientList = "";
    	emailSubject = "";
    	emailBody = "";
    }

	Emailer(String recepientList, String subject, String body)
	{
		initSession();
		emailRecepientList = recepientList;
		emailSubject = subject;
		emailBody = body;
	}
	
	public String getEmailRecepientList()
	{
		return emailRecepientList;
	}

	public String getEmailSubject()
	{
		return emailSubject;
	}

	public String getEmailBody()
	{
		return emailBody;
	}

	public void setEmailRecepientList(String recepientList)
	{
		emailRecepientList = recepientList;
	}

	public void setEmailSubject(String subject)
	{
		emailSubject = subject;
	}

	public void setEmailBody(String body)
	{
		emailBody = body;
	}

	public void sendMessage()
	{
		try
        {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(getEmailRecepientList()));
            message.setSubject(getEmailSubject());
            message.setText(getEmailBody());

            Transport.send(message);

            System.out.println(getEmailSubject() + " Sent");

        }
		catch (MessagingException e)
		{
            throw new RuntimeException(e);
        }
	}

	private void initSession()
	{
		Properties props = new Properties();
		//Currently set up for a gmail address
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

	public static void main(String[] args)
    {
		//Test code
        Emailer newEmailer = new Emailer("", "Watching You", "BWAHAHAHA");
        newEmailer.sendMessage();
    }
}