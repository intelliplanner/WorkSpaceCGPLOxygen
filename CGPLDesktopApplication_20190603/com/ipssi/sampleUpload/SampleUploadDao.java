package com.ipssi.sampleUpload;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;

public class SampleUploadDao {

	SimpleDateFormat dateFormat = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
	public SessionManager m_session = null;

	public SampleUploadDao(SessionManager m_session) {
		this.m_session = m_session;
	}

	public void saveSampleData(String xmlData, int postLotId,int labId) throws Exception {
		boolean saveStatus=false;
		Connection conn = null;
		PreparedStatement ps = null;
		SampleUploadBean bean = null;
		List<SampleUploadBean> dataBean = parseSampleData(xmlData);
		int size = Misc.getUndefInt();
		if (dataBean != null) {
			size = dataBean.size();
		}
		
		if (size > 0) {
			try {
				conn = m_session.getConnection();
				String query = "insert into sample_upload_details (wet_qty_mt,tm_arb,im_adb,ash_adb,vm_adb,fc_adb,gcv_adb,gcv_arb,im_eq,ash_eq,vm_eq," +
						"fc_eq,gcv_kcal_kg,date_of_sample_preparation,analyzed_grade,status,mpl_lot_sample_id,created_on, updated_on,lab_details_id,port_node_id ) " +
						"values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				ps = conn.prepareStatement(query);
				for (int i = 0; i < size; i++) {
					int insertRow=1;
					bean = dataBean.get(i);
//					ps.setString(insertRow++, bean.getNameOfSiding());
//					ps.setString(insertRow++, bean.getRrNo());
//					ps.setString(insertRow++, bean.getTransporter());
//					Misc.setParamInt(ps, bean.getNoOfTrucksLoaded(), insertRow++);
//					Misc.setParamInt(ps, bean.getNoOfTrucksUnloaded(), insertRow++);
					Misc.setParamDouble(ps, bean.getWetQtyMt(), insertRow++);
					Misc.setParamDouble(ps, bean.getTmArb(), insertRow++);
					Misc.setParamDouble(ps, bean.getImAdb(), insertRow++);
					Misc.setParamDouble(ps, bean.getAshAdb(), insertRow++);
					Misc.setParamDouble(ps, bean.getVmAdb(), insertRow++);
					Misc.setParamDouble(ps, bean.getFcAdb(), insertRow++);
					Misc.setParamDouble(ps, bean.getGcvAdb(), insertRow++);
					Misc.setParamDouble(ps, bean.getGcvArb(), insertRow++);
					
					Misc.setParamDouble(ps, bean.getImEq(), insertRow++);
					Misc.setParamDouble(ps, bean.getAshEq(), insertRow++);
					Misc.setParamDouble(ps, bean.getVmEq(), insertRow++);
					Misc.setParamDouble(ps, bean.getFcEq(), insertRow++);
					
					Misc.setParamDouble(ps, bean.getGcvKcalKg(), insertRow++);
//					ps.setString(insertRow++, bean.getGcvOrGrade());
//					ps.setString(insertRow++, bean.getDateOfUnloading());
					java.sql.Date dateOfSamplePreparation = bean.getDateOfSamplePreparation() == null ? null : new java.sql.Date(bean.getDateOfSamplePreparation().getTime());
					ps.setDate(insertRow++, dateOfSamplePreparation);
					ps.setString(insertRow++, bean.getAnalyzedGrade());
					Misc.setParamInt(ps, 1, insertRow++);// status
					Misc.setParamInt(ps,postLotId, insertRow++);
					ps.setTimestamp(insertRow++, new Timestamp(System.currentTimeMillis()));
					ps.setTimestamp(insertRow++, new Timestamp(System.currentTimeMillis()));
					Misc.setParamInt(ps,labId, insertRow++);
					Misc.setParamInt(ps,463, insertRow++);
					System.out.println("[Sample Data]: "+ps.toString());
					ps.addBatch(); 
				}
				ps.executeBatch();
				saveStatus = true;
			} catch (Exception e) {
				e.printStackTrace();
				
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if(saveStatus){
			updateLotSampleAfterUpload(conn, postLotId,labId);
			freeLotNumberAfterUpload(conn, postLotId);
		}
		System.out.println("The Method Is Executed Well");

	}

	private List<SampleUploadBean> parseSampleData(String xmlData) {
		List<SampleUploadBean> dataList = new ArrayList<SampleUploadBean>();

		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xmlData);
		org.w3c.dom.NodeList nList = xmlDoc.getElementsByTagName("MainTag");
		int size = nList.getLength();
		java.util.Date now = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		for (int i = 0; i < size; i++) {
			SampleUploadBean bean = new SampleUploadBean();
			org.w3c.dom.Node n = nList.item(i);
			org.w3c.dom.Element e = (org.w3c.dom.Element) n;

			 String name_of_siding = Misc.getParamAsString(e.getAttribute("name_of_siding"));
			 bean.setNameOfSiding(name_of_siding);
			 String rr_no = Misc.getParamAsString(e.getAttribute("rr_no"));
			 bean.setRrNo(rr_no);
			 String transporter = Misc.getParamAsString(e.getAttribute("transporter"));
			 bean.setTransporter(transporter);
			 int no_of_trucks_loaded = Misc.getParamAsInt(e.getAttribute("no_of_trucks_loaded"));
			 bean.setNoOfTrucksLoaded(no_of_trucks_loaded);
			 int no_of_trucks_unloaded = Misc.getParamAsInt(e.getAttribute("no_of_trucks_unloaded"));
			 bean.setNoOfTrucksUnloaded(no_of_trucks_unloaded);
			 double wet_qty_mt = Misc.getParamAsDouble(e.getAttribute("wet_qty_mt"));
			 bean.setWetQtyMt(wet_qty_mt);
			 double tm_arb = Misc.getParamAsDouble(e.getAttribute("tm_arb"));
			 bean.setTmArb(tm_arb);
			 double im_adb = Misc.getParamAsDouble(e.getAttribute("im_adb"));
			 bean.setImAdb(im_adb);
			 double ash_adb = Misc.getParamAsDouble(e.getAttribute("ash_adb"));
			 bean.setAshAdb(ash_adb);
			 double vm_adb = Misc.getParamAsDouble(e.getAttribute("vm_adb"));
			 bean.setVmAdb(vm_adb);
			 double fc_adb = Misc.getParamAsDouble(e.getAttribute("fc_adb"));
			 bean.setFcAdb(fc_adb);
			 int gcv_adb = Misc.getParamAsInt(e.getAttribute("gcv_adb"));
			 bean.setGcvAdb(gcv_adb);
			 int gcv_arb = Misc.getParamAsInt(e.getAttribute("gcv_arb"));
			 bean.setGcvArb(gcv_arb);
			 double im_eq = Misc.getParamAsDouble(e.getAttribute("im_eq"));
			 bean.setImEq(im_eq);
			 double ash_eq = Misc.getParamAsDouble(e.getAttribute("ash_eq"));
			 bean.setAshEq(ash_eq);
			 double vm_eq = Misc.getParamAsDouble(e.getAttribute("vm_eq"));
			 bean.setVmEq(vm_eq);
			 double fc_eq = Misc.getParamAsDouble(e.getAttribute("fc_eq"));
			 bean.setFcEq(fc_eq);
			 double gcv_kcal_kg = Misc.getParamAsDouble(e.getAttribute("gcv_kcal_kg"));
			 bean.setGcvKcalKg(gcv_kcal_kg);
			 String gcv_or_grade = Misc.getParamAsString(e.getAttribute("gcv_or_grade"));
			 bean.setGcvOrGrade(gcv_or_grade);
//			 String date_of_unloading = Misc.getParamAsString(e.getAttribute("date_of_unloading"));
//			 bean.setDateOfUnloading(date_of_unloading);

//			 Date date_of_sample_preparation = Misc.getParamAsDateFull(e.getAttribute("date_of_sample_preparation"));
			 Date date_of_sample_preparation = Misc.getParamAsDate(e.getAttribute("date_of_sample_preparation"), now, df);
//			 date_of_sample_preparation  = (date_of_sample_preparation==null || date_of_sample_preparation.length()==0) ? null : date_of_sample_preparation;
			 bean.setDateOfSamplePreparation(date_of_sample_preparation);
			 String analyzed_grade = Misc.getParamAsString(e.getAttribute("analyzed_grade"));
			 bean.setAnalyzedGrade(analyzed_grade);
			
			 dataList.add(bean);

		}
		return dataList;

	}

	public Triple<Integer,String,Integer> searchPostLotData(int lotId, String lotName,int labId) {
//		int postLotId = Misc.getUndefInt();
		Connection conn = null;
		boolean destroyIt = true;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Triple<Integer,String,Integer> tripleVal = null;
		try {
			int i = 1;
			int portNodeId = Misc.getParamAsInt(
					m_session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
			conn = m_session.getConnection();
		//	StringBuilder query = new StringBuilder("Select id from mpl_post_lot_details where epc_code is not null and mpl_lots_lab_details.sample_upload_tag_read=1 and mpl_post_lot_details.port_node_id=? ");
//			StringBuilder query = new StringBuilder("Select mpl_post_lot_details.id,mpl_post_lot_details.name,mpl_lots_lab_details.lab_details_id from mpl_post_lot_details join  mpl_lots_lab_details on (mpl_post_lot_details.id=mpl_lots_lab_details.post_sample_lot_id) where mpl_lots_lab_details.rfid_epc is not null and mpl_lots_lab_details.sample_upload_tag_read=1 and mpl_post_lot_details.port_node_id=? ");
			StringBuilder query = new StringBuilder("Select mpl_post_lot_details.id post_lot_id,mpl_post_lot_details.name lot_name,mpl_lots_lab_details.lab_details_id from mpl_post_lot_details join  mpl_lots_lab_details on (mpl_post_lot_details.id=mpl_lots_lab_details.post_sample_lot_id) where mpl_lots_lab_details.rfid_epc is not null and mpl_lots_lab_details.sample_upload_tag_read=1 and mpl_lots_lab_details.status=1 and mpl_post_lot_details.port_node_id=?");
			if (!Misc.isUndef(lotId)) {
				query.append(" and mpl_post_lot_details.id=?");
			}
//			if (lotName != null && lotName.length() > 0) {
//				query.append(" and mpl_post_lot_details.name=? ");
//			}
			if (!Misc.isUndef(labId) && labId !=0) {
				query.append(" and mpl_lots_lab_details.lab_details_id=? ");
			}

			m_session.setAttribute("pv123", portNodeId + "", false);
			ps = conn.prepareStatement(query.toString());
			ps.setInt(i++, portNodeId);
			if (!Misc.isUndef(lotId)) {
				ps.setInt(i++, lotId);
			}
//			if (lotName != null && lotName.length() > 0) {
//				lotName = lotName.contains("lot_") ? lotName : "lot_" + lotName;
//				ps.setString(i++, lotName);
//			}
			if (!Misc.isUndef(labId) && labId !=0) {
				ps.setInt(i++, labId);
			}
			System.out.println("[SampleUploadDao] [searchPostLotData()] : " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
					tripleVal = new Triple<Integer,String,Integer>(rs.getInt(1),rs.getString(2),rs.getInt(3));
			}

		} catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return tripleVal;
	}

	public static ArrayList<Pair<Integer,String>> getLabList(Connection conn){
		boolean isExist = false;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Pair<Integer,String>> templateList = new ArrayList<Pair<Integer,String>>();
		
		String query = "SELECT id,name from mpl_lab_details where status=1";
		Pair<Integer,String> userTemplate = null;
		try {
			ps = conn.prepareStatement(query);
			System.out.print("SampleUploadDao getLabList Query :"+ ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				userTemplate = new Pair<Integer,String>(rs.getInt(1), rs.getString(2));
				templateList.add(userTemplate);
			}
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		} finally {

			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
		return templateList;
	
	}
	
	 public static void updateLotSampleAfterUpload(Connection conn,int id,int labId ) {
			boolean destroyIt = false;
			PreparedStatement ps = null;
			ResultSet rs = null;
			Pair<Integer, String> pairVal = null;

			String query = "update mpl_lots_lab_details set status = 2 where  post_sample_lot_id=? and lab_details_id=?";
			try {
				ps = conn.prepareStatement(query);
				ps.setInt(1, id);
				ps.setInt(2, labId);
				System.out.println(ps.toString());
				ps.executeUpdate();
			} catch (Exception ex) {
				destroyIt = true;
				ex.printStackTrace();
			}finally{
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	 
	    public static void freeLotNumberAfterUpload(Connection conn,
				int latestAssignedId) {
			boolean destroyIt = false;
			PreparedStatement ps = null;
			String query = "update mpl_post_lot_number set is_free=0 where latest_assigned_id = ?";
			try {
				ps = conn.prepareStatement(query);
				ps.setInt(1, latestAssignedId);
				System.out.println(ps.toString());
				ps.executeUpdate();
			} catch (Exception ex) {
				destroyIt = true;
				ex.printStackTrace();
			} finally {
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		public int isExistSampleUploadForSelectedLab(int lotId, int labId) {
			Connection conn = null;
			int isExist = Misc.getUndefInt();
			boolean destroyIt = false;
			PreparedStatement ps = null;
			ResultSet rs = null;
			
			String query = "SELECT id from mpl_lots_lab_details where status=2 and post_sample_lot_id=? and lab_details_id=?";
			try {
				conn = m_session.getConnection();
				ps = conn.prepareStatement(query);
				ps.setInt(1, lotId);
				ps.setInt(2, labId);
				System.out.print("[SampleUploadDao] [isExistSampleUploadForSelectedLab] Query :"+ ps.toString());
				rs = ps.executeQuery();
				
				while (rs.next()) {
					isExist = rs.getInt(1);
				}
			} catch (Exception ex) {
				destroyIt = true;
				ex.printStackTrace();
			} finally {

				try {
					if (ps != null) {
						ps.close();
					}
					if (rs != null) {
						rs.close();
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
			}
			return isExist;

		}
		
	public static void main(String s[]){
		
	}


}
