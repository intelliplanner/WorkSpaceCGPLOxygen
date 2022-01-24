package com.ipssi.dodetails;

import java.util.ArrayList;

import com.ipssi.gen.utils.SessionManager;

public class DoDefinitionAction {
	public SessionManager m_session = null;

	public  DoDefinitionAction(SessionManager m_session){
		this.m_session = m_session;
	}
	public void insertList(ArrayList<MinesDoBean> beanList) {
		DoDetailsDefinitionDao doDao = new DoDetailsDefinitionDao(m_session);
		try {
			doDao.insertMinesDoDetails(beanList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	}
