package com.ipssi.tracker.jrm;

import java.util.ArrayList;

import com.ipssi.gen.utils.SessionManager;



public class JRMDefinitionAction {

	public static final int DEFAULT_PROIRITY_INDEX = 0;
	private SessionManager m_session = null;

	public JRMDefinitionAction(SessionManager m_session) {
		super();
		this.m_session = m_session;
	}

	
	public void processRegion(String mapName,boolean developGeom,ArrayList<JRMBean> beanList) {
		 JRMDefinitionDao jrmDefinitionDao = new JRMDefinitionDao(m_session);
		for (int i = 0; i < beanList.size(); i++){
			try {
				JRMBean bean = beanList.get(i);
				int id = jrmDefinitionDao.saveRegion(bean);
				if(bean.getRegionType() == com.ipssi.tracker.common.util.ApplicationConstants.JRM){
						bean.setLandmark_region_seg_id(id);
						jrmDefinitionDao.insertList(bean);
				}		
			}  catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public static void main(String a[]) {
		// LandmarkDefinitionAction abc = new LandmarkDefinitionAction(null);
		// abc.updateThisLandmark("1", "New Landmark", "newn ", "5, 0,0, 0,1, 1,1, 1,0, 0,0", "122", "", "");
		// abc.showThisLandmarkOnMap("1,6", "a9697bb0-ffff-ffff-8000-0024d2117742_en_7F0000010AFC0AFB0AFA", "Sheboygan");
		// abc.getLandmarkList();
	}

}
