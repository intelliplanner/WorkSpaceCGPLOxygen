package com.ipssi.tracker.common.util;

import java.util.HashMap;
import java.util.Properties;

import javax.jms.DeliveryMode;
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
import com.ipssi.processor.utils.VehiclePlusGpsDataList;


public class RPQueueSender {

	private static Logger logger = Logger.getLogger(RPQueueSender.class);

	private static QueueConnection conn = null;
	private static QueueSession session = null;
	private static Queue queue = null;
	private static QueueSender sender = null;
	private static HashMap<Integer,QueueSender> senderMap = new HashMap<Integer,QueueSender>();
	private static int retryCount = 0;
	private static int maxRetryAttempt = 10;

	static {

		try {
			Properties props = PropertyManager.getProperties();
			Context context = new InitialContext(props);

			QueueConnectionFactory tcf = (QueueConnectionFactory) context.lookup(PropertyManager.getString("remote.jndi"));
			conn = tcf.createQueueConnection();
			for (int i = 0; i < 11; i++) {
				queue = (Queue) context.lookup(PropertyManager.getString("ruleEngineQueue") + i);

				session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
				sender = session.createSender(queue);
				sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				senderMap.put(i, sender);
			}
			queue = (Queue) context.lookup(PropertyManager.getString("ruleEngineQueue"));

			session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			sender = session.createSender(queue);

			conn.start();
		} catch (Exception ex) {
			logger.error(" Problem while intitializing the RPQueueSender " + ex);
		}
	}

	public synchronized static void reInit() {
		retryCount++;
		if (retryCount > maxRetryAttempt) {
			System.out.println("RPQueueSender.reInit() Max Retry Count Exceeded..will not Try Re Initalize further");
			return;
		}
		try {
			Properties props = PropertyManager.getProperties();
			Context context = new InitialContext(props);

			QueueConnectionFactory tcf = (QueueConnectionFactory) context.lookup(PropertyManager.getString("remote.jndi"));
			conn = tcf.createQueueConnection();
			for (int i = 0; i < 11; i++) {
				queue = (Queue) context.lookup(PropertyManager.getString("ruleEngineQueue") + i);

				session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
				sender = session.createSender(queue);
				sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				senderMap.put(i, sender);
			}
			queue = (Queue) context.lookup(PropertyManager.getString("ruleEngineQueue"));

			session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			sender = session.createSender(queue);

			conn.start();
			retryCount = 0;
		} catch (Exception ex) {
			logger.error(" Problem while RPQueueSender.reInit() the RPQueueSender " + ex);
		}
	}

	/**
	 * 
	 * @param text
	 * @throws JMSException
	 * @throws NamingException
	 */
	public static void send(String text, String groupId) throws JMSException, NamingException {
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

		try {
			ObjectMessage oMsg = session.createObjectMessage(text);
			oMsg.setStringProperty("JMSXGroupID", groupId);
			//			sender.send(oMsg);
			//			oMsg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
			getSender(groupId).send(oMsg);
		} catch (Exception e) {
			e.printStackTrace();	
			System.out.println("RPQueueSender.send() Error while writing to queue...will try to reInit nexttime");
			sender = null;
			session = null;
		}
	}
	public static void send(VehiclePlusGpsDataList dataToSend) throws JMSException, NamingException {
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
			String vehicleId = Integer.toString(dataToSend.getVehicleId());
			ObjectMessage oMsg = session.createObjectMessage(dataToSend);
			//	oMsg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
			getSender(vehicleId).send(oMsg);
		} catch (Exception e) {
			e.printStackTrace();	
			System.out.println("RPQueueSender.send() Error while writing to queue...will try to reInit nexttime");
			sender = null;
			session = null;
		}
	}
	public static void send(Triple<Integer, Integer,String> dataToSend) throws JMSException, NamingException {
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
			System.out.println("RPQueueSender.send() dataToSend : " + dataToSend);
			String vehicleId = Integer.toString(dataToSend.first);
			ObjectMessage oMsg = session.createObjectMessage(dataToSend);
			//	oMsg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
			getSender(vehicleId).send(oMsg);
		} catch (Exception e) {
			e.printStackTrace();	
			System.out.println("RPQueueSender.send() Error while writing to queue...will try to reInit nexttime");
			sender = null;
			session = null;
		}
	}
	public static QueueSender getSender(String groupId) throws JMSException, NamingException {
		try{
			return senderMap.get(Integer.parseInt(groupId) % 11);
		}catch(Exception e){
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

}
