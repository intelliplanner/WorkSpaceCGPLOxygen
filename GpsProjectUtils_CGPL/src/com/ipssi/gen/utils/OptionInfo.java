package com.ipssi.gen.utils;

import java.io.Serializable;

public class OptionInfo  implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
         public int m_id = Misc.getUndefInt();
         public String m_code = null;
         public String m_targetTag = null;
         public String m_label = null;
         public String m_directPage = null;
         public String m_paramTemplate = null;
         public boolean m_doPopup = false;
         public boolean m_suspendTimer = false;
         public String m_javascriptOptional = null;
         public OptionInfo(int id, String code, String label, String directPage, String paramTemplate, boolean doPopup, boolean suspendTimer, String targetTag, String preHandler) {
            m_id = id;
            m_code = code;
            m_label = label;
            m_directPage = directPage;
            m_paramTemplate = paramTemplate;
            m_doPopup = doPopup;
            m_suspendTimer = suspendTimer;
            m_targetTag = targetTag;
            if (m_targetTag == null)
            	m_targetTag = m_code;
            m_javascriptOptional = preHandler;

         }
}