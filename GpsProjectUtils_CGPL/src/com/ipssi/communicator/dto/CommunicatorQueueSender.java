package com.ipssi.communicator.dto;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class CommunicatorQueueSender {
	
	
	private static QueueConnection conn = null;
	private static QueueSession session = null;
	private static Queue queue = null;
	private static QueueSender sender = null;
	private static int retryCount = 0;
	private static int maxRetryAttempt = 10;
	
	static {

			try {
				Properties props = PropertyManager.getProperties();
				Context context = new InitialContext(props);

				QueueConnectionFactory tcf = (QueueConnectionFactory) context.lookup(PropertyManager.getString("remote.jndi"));
				conn = tcf.createQueueConnection();
				queue = (Queue) context.lookup(PropertyManager.getString("communicatorQueue"));

				session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
				sender = session.createSender(queue);

				conn.start();
			} 
			catch (Exception ex) {
			  ex.printStackTrace();
			}
	}
	public synchronized static void reInit() {
		retryCount++;
		if (retryCount > maxRetryAttempt) {
			System.out.println("CommunicatorQueueSender.reInit() Max Retry Count Exceeded..will not Try Re Initalize further");
			return;
		}

			try {
				Properties props = PropertyManager.getProperties();
				Context context = new InitialContext(props);

				QueueConnectionFactory tcf = (QueueConnectionFactory) context.lookup(PropertyManager.getString("remote.jndi"));
				conn = tcf.createQueueConnection();
				queue = (Queue) context.lookup(PropertyManager.getString("communicatorQueue"));

				session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
				sender = session.createSender(queue);

				conn.start();
				retryCount = 0;
			} 
			catch (Exception ex) {
				ex.printStackTrace();
			}
	}
	/**
	 * 
	 * @param text
	 * @throws JMSException
	 * @throws NamingException
	 */

	public static void send(CommunicatorDTO text)  {
		if (session == null) {
			reInit();
			if (session == null ){
				try {
					Thread.sleep(30*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try{
			ObjectMessage oMsg = session.createObjectMessage(text);
			sender.send(oMsg);
		} catch (Exception e) {
			e.printStackTrace();	
			System.out.println("CommunicatorEmailQueueSender.send() Error while writing to queue...will try to reInit nexttime");
			sender = null;
			session = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		if (sender != null) {
			sender.close();
		}

		if (conn != null) {
			conn.stop();
		}

		if (session != null) {
			session.close();
		}

		if (conn != null) {
			conn.close();
		}

	}
	
	public static void main(String a[]){
		try {
			CommunicatorQueueSender.send(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
