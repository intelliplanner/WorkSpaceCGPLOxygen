package a_example.email;

import java.util.Date;

import javax.mail.Message;
import javax.mail.search.SearchTerm;

public class SentDateSearchTerm extends SearchTerm {

	/**
	 * Virendra Gupta
	 */
	private static final long serialVersionUID = 1L;
	Date receivedDate;

	public SentDateSearchTerm(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	@Override
	public boolean match(Message message) {
		try {
			if ( message.getSentDate().after(receivedDate)) {
//				if (message.getSubject().contains("Job") /*&& message.getSentDate().after(receivedDate)*/) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}