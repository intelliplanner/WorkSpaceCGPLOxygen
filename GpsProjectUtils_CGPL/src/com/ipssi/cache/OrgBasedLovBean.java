package com.ipssi.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;

public class OrgBasedLovBean {
	private static ConcurrentHashMap<String, ArrayList<OrgBasedLovBean>> cachedLovs = new ConcurrentHashMap<String, ArrayList<OrgBasedLovBean>>();
	private static String getKeyFor(int portNodeId, int paramId) {
		return portNodeId+"_"+paramId;
	}
	public static ArrayList<OrgBasedLovBean> getLovListFor(Connection conn, int portNodeId, int paramId, boolean atLevel) throws Exception {
		ArrayList<OrgBasedLovBean> retval = null;
		Cache cache = Cache.getCacheInstance(conn);
		MiscInner.PortInfo portInfo = cache.getPortInfo(portNodeId, conn);
		for (;portInfo != null; portInfo = portInfo.m_parent) {
			retval = cachedLovs.get(getKeyFor(portNodeId, paramId));
			if (retval == null) {
				retval = read(conn, portNodeId, paramId);
				cachedLovs.put(getKeyFor(portNodeId, paramId), retval);
			}
			if (atLevel || retval.size() > 0) 
				return retval;
		}
		return null;
	}
	
	private static final int g_maxClassify = 5;
    private int id;
    private String name;
    private int[] classifications = new int[g_maxClassify];
    private String[] classificationsStr = new String[g_maxClassify];
    public OrgBasedLovBean(int id, String name, int cl1, String fl1, String fl2, String fl3, String fl4) {
    	this.id = id;
    	this.name = name;
    	for (int i=0,is = classifications.length;i<is;i++) {
    		classifications[i] = Misc.getUndefInt();
    	}
    	setClassification(0, cl1);
    	setClassificationStr(0, fl1);
    	setClassificationStr(1, fl2);
    	setClassificationStr(2, fl3);
    	setClassificationStr(3, fl4);
    	
    }
    
    public void setClassification(int index, int val) {
    	classifications[index] = val;
    }
    
    public int getClassification(int index) {
    	return classifications[index];
    }
    
    public int getMaxClassifyCount() {
    	return classifications.length;
    }
    
    public void setClassificationStr(int index, String val) {
    	classificationsStr[index] = val;
    }
    
    public String getClassificationStr(int index) {
    	return classificationsStr[index];
    }
    
    public int getMaxClassifyCountStr() {
    	return classificationsStr.length;
    }
    public int getId() {
    	return id;
    }
    public void setId(int id) {
    	this.id = id;
    }
    public String getName() {
    	return name;
    }
    public void setName(String name) {
    	this.name = name;
    }
    
    public static void write(Connection conn, int portNodeId, int paramId, ArrayList<OrgBasedLovBean> lovList, String idtoDel) throws Exception {
    	try {
    		if (idtoDel != null && idtoDel.length() > 0) {
    			StringBuilder sb = new StringBuilder();
    			sb.append("delete from generic_params where port_node_id=? and param_id=? and id in(").append(idtoDel).append(")");
    			PreparedStatement ps = conn.prepareStatement(sb.toString());
    			ps.setInt(1, portNodeId);
    			ps.setInt(2, paramId);
    			ps.execute();
    			ps = Misc.closePS(ps);
    		}
    		if (lovList != null) {
	    		PreparedStatement psUpd = conn.prepareStatement("update generic_params set name=?, int_val1 = ?, flex_field1 = ?, flex_field2 = ?, flex_field3 = ?, flex_field4 = ? where port_node_id=? and param_id=? and id=?");
	    		PreparedStatement psIns = conn.prepareStatement("insert into generic_params(name,  int_val1, flex_field1, flex_field2, flex_field3, flex_field4,port_node_id, param_id, status) values (?,?,?,?,?,?,?,?,1)");
	    		for (OrgBasedLovBean bean : lovList) {
	    			boolean isNew = Misc.isUndef(bean.getId());
	    			PreparedStatement ps =  isNew ? psIns : psUpd;
	    			int colIndex = 1;
	    			ps.setString(colIndex++, bean.getName());
	    			Misc.setParamInt(ps, bean.getClassification(0),colIndex++);
	    			ps.setString(colIndex++, bean.getClassificationStr(0));
	    			ps.setString(colIndex++, bean.getClassificationStr(1));
	    			ps.setString(colIndex++, bean.getClassificationStr(2));
	    			ps.setString(colIndex++, bean.getClassificationStr(3));
	    			Misc.setParamInt(ps, portNodeId,colIndex++);
	    			Misc.setParamInt(ps, paramId,colIndex++);
	    			if (isNew) {
	    				ps.execute();
	    				ResultSet rs = ps.getGeneratedKeys();
	    				if (rs.next())
	    					bean.setId(rs.getInt(1));
	    				rs = Misc.closeRS(rs);
	    			}
	    			else {
	    				ps.setInt(colIndex++, bean.getId());
	    				ps.addBatch();
	    			}
	    		}
	    		psUpd.executeBatch();
	    		psIns = Misc.closePS(psIns);
	    		psUpd = Misc.closePS(psUpd);
    		}
    		OrgBasedLovBean.cachedLovs.put(getKeyFor(portNodeId, paramId), lovList);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    public static ArrayList<OrgBasedLovBean> read(Connection conn, int portNodeId, int paramId) throws Exception {
    	try {
    		ArrayList<OrgBasedLovBean> retval = new ArrayList<OrgBasedLovBean>();
    		PreparedStatement ps = conn.prepareStatement("select id,name,int_val1,flex_field1, flex_field2, flex_field3, flex_field4 from generic_params where port_node_id=? and param_id=? and status in (1)");
    		ps.setInt(1, portNodeId);
    		ps.setInt(2, paramId);
    		ResultSet rs = ps.executeQuery();
    		while (rs.next()) {
    			retval.add(new OrgBasedLovBean(rs.getInt(1), rs.getString(2), Misc.getRsetInt(rs, 3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)));
    		}
    		rs.close();
    		ps.close();
    		cachedLovs.put(getKeyFor(portNodeId, paramId), retval);
    		return retval;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
}
