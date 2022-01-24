package com.ipssi.locTrackerInit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.ipssi.gen.utils.Misc;

public class Init implements ServletContextListener {
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		try{
		if(!"web_lafarge".equalsIgnoreCase(Misc.getServerName()))
			com.ipssi.tprCache.Loader.stop();
		//com.ipssi.mobilenotification.NotificationExecutor.stop();
		if("node_lafarge".equalsIgnoreCase(Misc.getServerName())||"backup_lafarge".equalsIgnoreCase(Misc.getServerName()))
			com.ipssi.orient.jason.reader.OrientJasonIntegration.stop();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		try{
		if(!"web_lafarge".equalsIgnoreCase(Misc.getServerName()))
			com.ipssi.tprCache.Loader.start();
		//com.ipssi.mobilenotification.NotificationExecutor.start();
		if("node_lafarge".equalsIgnoreCase(Misc.getServerName())||"backup_lafarge".equalsIgnoreCase(Misc.getServerName()))
		    com.ipssi.orient.jason.reader.OrientJasonIntegration.start();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
