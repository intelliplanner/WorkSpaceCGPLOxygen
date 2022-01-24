package com.ipssi.mining;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.dashboard.TripInfoBean;
import com.ipssi.dyn.DynUpload;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.PageHeader;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.common.util.TripProcessorGateway;
import com.ipssi.tracker.drivers.DVUtils;
import com.ipssi.tracker.drivers.DriverCoreBean;
import com.ipssi.tracker.drivers.DriverDetailsDao;
import com.ipssi.tracker.drivers.DriverSkillsBean;
import com.ipssi.tracker.web.ActionI;

public class DOSInvPileAction implements ActionI{
	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, Exception {
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		try{
			if("create".equals(action))
				return "/dos_inventory_pile_inp.jsp";
			if("save".equals(action))
				saveInvPile(request, response);
			ArrayList<DOSInvPileBean> infoList = getInvPile(request);
			request.setAttribute("infoList", infoList);
//			actionForward = "/dos_inventory_pile_view.jsp";
		}
		catch (GenericException e) {
			System.out.println("actionList "+e.getMessage());
			e.printStackTrace();
		}
		actionForward = "/dos_inventory_pile.jsp";
		return actionForward;
	}

	

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		return "/dos_inventory_pile.jsp";
	}
	
	public void saveInvPile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String xml = Common.getParamAsString(request.getParameter("XML_DATA"));
		SessionManager session = InitHelper.helpGetSession(request);
		int v123 = Misc.getParamAsInt(session.getParameter("applicableTo"));
		if (Misc.isUndef(v123))
			v123 = Misc.getParamAsInt(session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
		int pileType = Misc.getParamAsInt(session.getParameter("pile_type"), 1);
		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
		ArrayList<DOSInvPileBean> beanList = new ArrayList<DOSInvPileBean>();
	    for (Node n = xmlDoc == null || xmlDoc.getDocumentElement() == null ? null : xmlDoc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
	    	if (n.getNodeType() != 1)
	    		continue;
	    	Element e = (Element) n;
			DOSInvPileBean bean = new DOSInvPileBean(Misc.getParamAsInt(e.getAttribute("id")), Misc.getParamAsInt(e.getAttribute("bench_id")),Misc.getParamAsInt(e.getAttribute("direction_id")),
					Misc.getParamAsInt(e.getAttribute("port_node_id"), v123), Misc.getParamAsString(e.getAttribute("short_code"),null)
					, Misc.getParamAsString(e.getAttribute("name"), null), Misc.getParamAsString(e.getAttribute("other_notes"), null), Misc.getParamAsDouble(e.getAttribute("longitude")), Misc.getParamAsDouble(e.getAttribute("latitude"))
					, Misc.getParamAsDouble(e.getAttribute("width")), Misc.getParamAsDouble(e.getAttribute("len")),Misc.getParamAsInt(e.getAttribute("status"))
					, Misc.getParamAsDate(e.getAttribute("createDate"), null, new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT))
					, Misc.getParamAsDate(e.getAttribute("closeDate"), null, new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT)), null
					, Misc.getParamAsInt(e.getAttribute("pile_type_line"), Misc.getParamAsInt(session.getParameter("pile_type"), 1))
					, Misc.getParamAsInt(e.getAttribute("region_id")), Misc.getParamAsInt(e.getAttribute("landmark_id")), Misc.getParamAsInt(e.getAttribute("priority")), Misc.getParamAsString(e.getAttribute("landmark_name")),
					Misc.getParamAsString(e.getAttribute("region_name")), Misc.getParamAsInt(e.getAttribute("pit_id")), Misc.getParamAsDouble(e.getAttribute("difficulty_factor"), 1)
					, Misc.getParamAsDouble(e.getAttribute("avg_cycle_time")), Misc.getParamAsDouble(e.getAttribute("positioning_time"))
					, Misc.getParamAsDouble(e.getAttribute("pos_trip")), Misc.getParamAsDouble(e.getAttribute("clearing_time"))
					, Misc.getParamAsDouble(e.getAttribute("tonnage")), Misc.getParamAsDouble(e.getAttribute("extra_double_2")), Misc.getParamAsDouble(e.getAttribute("extra_double_3"))
			);
			
			bean.setDirty("1".equals(e.getAttribute("is_dirty")));
			if (bean.getPortNodeId() != v123) {
				bean.setPortNodeId(v123);
				bean.setDirty(true);
			}
			for (Node cn = e.getFirstChild(); cn != null; cn = cn.getNextSibling()) {
				if (cn.getNodeType() != 1)
					continue;
				Element ce = (Element) cn;
				if ("m".equals(ce.getTagName())) {
					int materialId = Misc.getParamAsInt(ce.getAttribute("material_id"));
					if (Misc.isUndef(materialId))
						continue;
					bean.addMaterial(materialId,Misc.getParamAsDouble(ce.getAttribute("mix")));
				}
				else if ("d".equals(ce.getTagName())) {
					int vid = Misc.getParamAsInt(ce.getAttribute("dumper_id"));
					if (Misc.isUndef(vid))
						continue;
					bean.addNotAllowedVehicleType(vid, 0);
				}
				else if ("s".equals(ce.getTagName())) {
					int vid = Misc.getParamAsInt(ce.getAttribute("shovel_id"));
					if (Misc.isUndef(vid))
						continue;
					bean.addNotAllowedVehicleType(vid, 1);
				}
				
			}
			beanList.add(bean);
	    }
	    DOSInvPileBean.saveInvPiles(session.getConnection(), beanList);
	    //for Updating LoadSite and Destination DYNA DIMS LOV
	    updateRelatedDims();
//		TripProcessorGateway.refreshLoadPile(v123);

	}
	
	private void updateRelatedDims() {
		int refDynDimId[]={82421,82420};//Load and Unload Site
		for (int i = 0; i < refDynDimId.length; i++) {
				DimInfo refDim = DimInfo.getDimInfo((Integer) refDynDimId[i]);
				if (refDim != null)
					refDim.makeDirty();
		}
		
	}



	public ArrayList<DOSInvPileBean> getInvPile(HttpServletRequest request) throws Exception {
		ArrayList<DOSInvPileBean> retval = new ArrayList<DOSInvPileBean>();
		SessionManager session = InitHelper.helpGetSession(request);
		Cache _cache = session.getCache();
		Connection conn = session.getConnection();
		String pgContext = Misc.getParamAsString(session.getParameter("page_context"), "dos_load_locations");
		MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request, pgContext);
		int pileType = Misc.getParamAsInt(session.getParameter("pile_type"));
		int v123 = Misc.getParamAsInt(session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
		ArrayList<Integer> status = new ArrayList<Integer>();
		
		String statusString = session.getParameter(searchBoxHelper.m_topPageContext+"22045");
		String startDt = session.getParameter(searchBoxHelper.m_topPageContext+"20035");
		String endDt = session.getParameter(searchBoxHelper.m_topPageContext+"20036");
		if (statusString == null || statusString.contains(Integer.toString(Misc.G_HACKANYVAL)))
			statusString = "0,1";
		if (statusString != null)
			Misc.convertValToVector(statusString, status);
		retval = DOSInvPileBean.getInvPiles(conn,v123, status,pileType,startDt,endDt);
		
		return retval;
	}
	
	public static ArrayList<MiscInner.PairIntStr> getRegionList(Connection conn, int portNodeId) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select regions.id,regions.short_code from regions join port_nodes leaf on (leaf.id = regions.port_node_id) join port_nodes anc on (anc.id = ? and (? = 2 or leaf.id<> 2) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number))) where regions.status in (1) and (regions.is_artificial is null or regions.is_artificial=0) order by regions.short_code");
		ps.setInt(1, portNodeId);
		ps.setInt(2, portNodeId);
		ArrayList<MiscInner.PairIntStr> retval = Misc.getLovList(ps);
		ps.close();
		return retval;
	}
	
	public static ArrayList<MiscInner.PairIntStr> getLandmarkList(Connection conn, int portNodeId) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select landmarks.id, landmarks.name from landmarks join port_nodes leaf on (leaf.id = landmarks.port_node_id) join port_nodes anc on (anc.id = ? and (? = 2 or leaf.id<> 2) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number)))  order by landmarks.name");
		ps.setInt(1, portNodeId);
		ps.setInt(2, portNodeId);
		ArrayList<MiscInner.PairIntStr> retval = Misc.getLovList(ps);
		ps.close();
		return retval;
	}

	public static ArrayList<MiscInner.PairIntStr> getMaterialList(Connection conn, int portNodeId) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select generic_params.id, generic_params.name from generic_params join port_nodes leaf on (leaf.id = generic_params.port_node_id) join port_nodes anc on (anc.id=? and (? = 2 or leaf.id<> 2) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number))) where generic_params.status in (1) and generic_params.param_id=20451 order by generic_params.name ");
		ps.setInt(1, portNodeId);
		ps.setInt(2, portNodeId);
		ArrayList<MiscInner.PairIntStr> retval = Misc.getLovList(ps);
		ps.close();
		return retval;
	}
	
	public static ArrayList<MiscInner.PairIntStr> getVehicleTypeList(Connection conn, int portNodeId, int vehicleCat) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select vehicle_types.id, vehicle_types.name from vehicle_types join port_nodes leaf on (leaf.id = vehicle_types.port_node_id) join port_nodes anc on (anc.id=? and (? = 2 or leaf.id<> 2) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number))) where vehicle_types.status in (1) and vehicle_types.vehicle_cat=? order by vehicle_types.name ");
		ps.setInt(1, portNodeId);
		ps.setInt(2, portNodeId);
		ps.setInt(3, vehicleCat);
		ArrayList<MiscInner.PairIntStr> retval = Misc.getLovList(ps);
		ps.close();
		return retval;
	}

}
