package a_example.email;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;

public class EmailReceivedFromCollection {

	public static void main(String[] args) {

		String host = "pop.gmail.com";// change accordingly
		String mailStoreType = "pop3";
//		String username = "vicky.gupta1190@gmail.com";
//		String password = "Vicky@Gupta5343";// change accordingly
		String username = "virendravicky65@gmail.com";
		String password = "vicky@gupta1";
		// String username = "amit.kumar03uneecops@gmail.com";
		// String password = "vchglpspnefmvvcu";// change accordingly

		// receiveEmail(host, mailStoreType, username, password);

		host = "imap.gmail.com";
//		String port = "595";
		String port = "993";
//		String port = "995";
		searchEmail(host, port, username, password);
	}

	public static void receiveEmail(String host, String storeType, String user, String password) {
		try {

			// create properties field
			Properties properties = new Properties();

			properties.put("mail.pop3.host", host);
			properties.put("mail.pop3.port", "995");
			properties.put("mail.pop3.starttls.enable", "true");
			Session emailSession = Session.getDefaultInstance(properties);

			// create the POP3 store object and connect with the pop server
			Store store = emailSession.getStore("pop3s");

			store.connect(host, user, password);
			Folder emailFolder = store.getFolder("INBOX");
			emailFolder.open(Folder.READ_ONLY);

			// SimpleDateFormat df1 = new SimpleDateFormat("MM/dd/yyyy");
			// String dt = "11/01/2021";
			// java.util.Date dDate = df1.parse(dt);

			Message[] messages = emailFolder.getMessages();
			System.out.println("messages.length---" + messages.length);

			for (int i = 0, n = messages.length; i < n; i++) {
				Message message = messages[i];
				if( message.getSubject().equalsIgnoreCase("Job") || message.getSubject().equalsIgnoreCase("technolog") ) {
				System.out.println("---------------------------------");
//				System.out.println("Email Number " + (i + 1));
//				System.out.println("Subject: " + message.getSubject());
					System.out.println("From: " + message.getFrom()[0]);
				
//				System.out.println("Text: " + message.getContent().toString());
				}
			}

			// close the store and folder objects
			emailFolder.close(false);
			store.close();

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void searchEmail(String host, String port, String userName, String password) {
		Properties properties = new Properties();

		// server setting
		properties.put("mail.imap.host", host);
		properties.put("mail.imap.port", port);

		// SSL setting
		properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.imap.socketFactory.fallback", "false");
		properties.setProperty("mail.imap.socketFactory.port", String.valueOf(port));

		Session session = Session.getDefaultInstance(properties);

		try {
			// connects to the message store
			Store store = session.getStore("imap");
			store.connect(host,userName, password);

			// opens the inbox folder
			Folder folderInbox = store.getFolder("INBOX");
			folderInbox.open(Folder.READ_ONLY);
			SimpleDateFormat df1 = new SimpleDateFormat("MM/dd/yy");
			String dt = "11/01/21";
			java.util.Date dDate = df1.parse(dt);

			SearchTerm searchCondition = new SearchTerm() {
				@Override
				public boolean match(Message message) {
					try {
						if (message.getSubject().contains("Java")) {
							return true;
						}
					} catch (MessagingException ex) {
						ex.printStackTrace();
					}
					return false;
				}
			};

			// performs search through the folder
			Message[] foundMessages = folderInbox.search(searchCondition);

			for (int i = 0; i < foundMessages.length; i++) {
				Message message = foundMessages[i];
				String subject = message.getSubject();
				System.out.println("Found message #" + i + ": " + subject);
			}

			// disconnect
			folderInbox.close(false);
			store.close();
		} catch (Exception ex) {
			System.out.println("Could not connect to the message store.");
			ex.printStackTrace();
		}
	}
}
