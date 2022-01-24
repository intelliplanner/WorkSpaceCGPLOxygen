package com.ipssi.tracker.common.util;

import java.util.HashMap;
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

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;


public class DPQueueSender {
	static Logger logger = Logger.getLogger(DPQueueSender.class);
	private static QueueConnection conn = null;
	private static QueueSession session = null;
	private static Queue queue = null;
	private static QueueSender sender = null;
	private static int retryCount = 0;
	private static int maxRetryAttempt = 10;
	private static HashMap<Integer, QueueSender> senderMap = new HashMap<Integer, QueueSender>();

	static {
		try {
			Properties props = PropertyManager.getProperties();
			Context context = new InitialContext(props);

			QueueConnectionFactory tcf = (QueueConnectionFactory) context.lookup(PropertyManager.getString("remote.jndi"));
			conn = tcf.createQueueConnection();
			for (int i = 0; i < 11; i++) {
				queue = (Queue) context.lookup(PropertyManager.getString("dataProcessorQueue") + i);

				session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
				sender = session.createSender(queue);
				senderMap.put(i, sender);
			}
			queue = (Queue) context.lookup(PropertyManager.getString("dataprocessor.queue"));

			session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			sender = session.createSender(queue);
			conn.start();

		} catch (Exception ex) {
			logger.error(" Problem while intitializing the DataProcessor QueueMsgSender " + ex);
			System.out.println("DPQueueSender.enclosing_method() Problem while intitializing the DPQueueSender " + ex);
			ex.printStackTrace();
		}
	}

	public synchronized static void reInit() {
		retryCount++;
		if (retryCount > maxRetryAttempt) {
			System.out.println("DPQueueSender.reInit() Max Retry Count Exceeded..will not Try Re Initalize further");
			return;
		}
		try {
			Properties props = PropertyManager.getProperties();
			Context context = new InitialContext(props);

			QueueConnectionFactory tcf = (QueueConnectionFactory) context.lookup(PropertyManager.getString("remote.jndi"));
			conn = tcf.createQueueConnection();
			for (int i = 0; i < 11; i++) {
				queue = (Queue) context.lookup(PropertyManager.getString("dataprocessor.queue") + i);

				session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
				sender = session.createSender(queue);
				senderMap.put(i, sender);
			}
			queue = (Queue) context.lookup(PropertyManager.getString("dataprocessor.queue"));

			session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			sender = session.createSender(queue);
			conn.start();

			retryCount = 0;
		} catch (Exception ex) {
			logger.error(" Problem while intitializing the DataProcessor QueueMsgSender " + ex);
			System.out.println("DPQueueSender.enclosing_method() Problem while Re-intitializing the DPQueueSender " + ex);
			ex.printStackTrace();
		}
	}

	/**
	 * 
	 * @param text
	 * @param vehicleId
	 * @throws JMSException
	 * @throws NamingException
	 */
	public static void send(String text, int vehicleId) throws JMSException, NamingException {
		//System.out.println("DPQueueSender.send()text  ######" + text);
		int maxTries = 10;
		int currTries = 0;
		while (currTries <= maxTries) {
			if (session == null) {
				reInit();
				if (session == null ){
					try {
						Thread.sleep(30*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
			}
			else {
				break;
			}
			currTries++;
		}
		try {
			ObjectMessage oMsg = session.createObjectMessage(text);
			getSender(vehicleId).send(oMsg);
		} catch (Exception e) {
			e.printStackTrace();	
			System.out.println("DPQueueSender.send() Error while writing to queue...will try to reInit nexttime");
			sender = null;
			session = null;
		}
	}
	public static void send(Pair<Integer, Integer> dataToSend) throws JMSException, NamingException {
		if (dataToSend == null)
			return;
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
			System.out.println("DPQueueSender.send() dataToSend : " + dataToSend);
			ObjectMessage oMsg = session.createObjectMessage(dataToSend);
			//	oMsg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
			getSender(dataToSend.first).send(oMsg);
		} catch (Exception e) {
			e.printStackTrace();	
			System.out.println("DPQueueSender.send() Error while writing to queue...will try to reInit nexttime");
			sender = null;
			session = null;
		}
	}

	public static void send(Triple<Integer, Integer, String> dataToSend) throws JMSException, NamingException {
		if (dataToSend == null)
			return;
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
			System.out.println("DPQueueSender.send() dataToSend : " + dataToSend);
			ObjectMessage oMsg = session.createObjectMessage(dataToSend);
			//	oMsg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
			getSender(dataToSend.first).send(oMsg);
		} catch (Exception e) {
			e.printStackTrace();	
			System.out.println("DPQueueSender.send() Error while writing to queue...will try to reInit nexttime");
			sender = null;
			session = null;
		}
	}

	public static QueueSender getSender(int vehicleId) throws JMSException, NamingException {
		try {
//			System.out.println("DPQueueSender.getSender() Writing to " + ((groupId) % 11));
			return senderMap.get((vehicleId) % 11);
		} catch (Exception e) {
			e.printStackTrace();
			return sender;
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
	public static void main(String[] args) {
//		mdb.msgHandler(conn, "DATA,17766,021.964571,082.036780,2012-12-06 13:55:59,2012-12-06 13:57:59,1.0,33.0,11967657,1010,00");
//		mdb.msgHandler(conn, "CURRENT,17767,028.652386,077.330968,2012-12-06 13:56:59,2012-12-06 13:58:59,0.0,145.33,null,00000101,0");
//		mdb.msgHandler(conn, "423297,&LOG,423297,231012,100231,2907.5391,N,07546.7077,E,0,347,6359987,0,00,1010,000000835,0");
//		mdb.msgHandler(conn, "CURRENT,17766,028.652386,077.330968,2012-12-06 13:57:59,2012-12-06 13:59:59,0.0,145.33,null,00000101,0");
//		mdb.msgHandler(conn, "CURRENT,17061,028.652386,077.330968,2012-10-22 12:41:40,2012-10-22 12:37:40,0.0,145.33,null,00000101,0");
//		mdb.msgHandler(conn, "CURRENT,17061,028.652386,077.330968,2012-10-22 12:37:40,2012-10-22 12:38:10,0.0,145.33,null,00000000,0");
		
		try {
			DPQueueSender.send("DATA,17766,021.964571,082.036780,2012-12-06 13:55:59,2012-12-06 13:57:59,1.0,33.0,11967657,1010,00", 17766);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
