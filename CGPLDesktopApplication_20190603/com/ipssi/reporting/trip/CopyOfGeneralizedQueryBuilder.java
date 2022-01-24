package com.ipssi.reporting.trip;

import static com.ipssi.gen.utils.Cache.DATE_TYPE;
import static com.ipssi.gen.utils.Cache.INTEGER_TYPE;
import static com.ipssi.gen.utils.Cache.LOV_NO_VAL_TYPE;
import static com.ipssi.gen.utils.Cache.LOV_TYPE;
import static com.ipssi.gen.utils.Cache.NUMBER_TYPE;
import static com.ipssi.gen.utils.Cache.STRING_TYPE;
import static com.ipssi.workflow.WorkflowHelper.G_OBJ_TPRECORD;
import static com.ipssi.workflow.WorkflowHelper.G_OBJ_VEHICLES;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletOutputStream;
import javax.servlet.jsp.JspWriter;

import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.trip.NewProfileCache;
import com.ipssi.common.ds.trip.VehicleControlling;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.ColumnMappingHelper;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimCalc;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.DriverExtendedInfo;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.FmtI;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.PageHeader;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.TimePeriodHelper;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.User;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.VehicleExtendedInfo;
import com.ipssi.gen.utils.CacheTrack.VehicleSetup;
import com.ipssi.gen.utils.DimConfigInfo.ExprHelper.CalcFunctionEnum;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.gen.utils.MiscInner.ContextInfo;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.input.InputTemplate;
import com.ipssi.reporting.cache.CacheManager;
import com.ipssi.reporting.common.db.DBQueries;
import com.ipssi.reporting.trip.ResultInfo.VirtualEventData;
import com.ipssi.reporting.trip.ResultInfo.VirtualGpsData;
import com.ipssi.reporting.trip.ResultInfo.VirtualHelper;
import com.ipssi.reporting.trip.ResultInfo.VirtualVal;
import com.ipssi.tracker.colorcode.ColorCodeDao;
import com.ipssi.workflow.WorkflowDef;
import com.ipssi.workflow.WorkflowHelper;
/*
 * customField3 in InputTemplate and FrontPageInfo are used for mandatory is_cached at XML level
 * customField1 in DimConfigInfo used to pass parameters to nested XML
 * customField2 in InputTemplate (and FrontPageInfo) used to indicate to print label then in next row (like in dashboard)
 */
public class CopyOfGeneralizedQueryBuilder {

	int maxAllowedCells = "web_lafarge".equals(Misc.getServerName()) ? 1200000 : 300000;//3*10^5*300 = 90MB
	int currCells = 0;
	boolean hadTooManyCells = false;
	boolean needToCheckCells = true;
	public static final String sel = " select ";
	public static final String from = " from singleton  ";
	public static final String grp = " group by ";
	public static final String whr = " where ";
	public static final String rollup = " with rollup ";
	public static final String having = " having ";
	//public static final boolean g_doRollupAtJava = false; //TODO - not implemented for true case yet
	public static boolean isEngineEventTrackRemoved = false;
	public ArrayList<Pair<String, Integer>> updateIndex = new ArrayList<Pair<String,Integer>>();
	public java.util.Date startDt = null;
	public java.util.Date endDt = null;
	boolean m_mat_check = true;
	boolean m_opStation_check = true;
	boolean m_workstation_check = true;
	boolean m_trip_info_ext_check = false;
	boolean m_shift_check = false;
	public String m_shiftStr = "";
	public String orgId = null;
	public String vehicleName = null;
	public static final String VEHICLE_FILTER = "vehicle_filter_str";
	public HashMap<Integer, String> dataToShow = new HashMap<Integer, String>();
	public HashMap<Integer, String> dataListToShow = new HashMap<Integer, String>();
	public String m_firstTable = null;
	public static boolean doJoinAreaCode = Misc.g_doAREACODE;
	static public class ParamRequired {
		public Object m_Value=null;
		public int m_type;
	}
	/*
	static public class FirstRowInfo {
		public StringBuilder m_selClause = new StringBuilder();
		public StringBuilder m_fromClause = new StringBuilder();
		public StringBuilder m_whereClause = new StringBuilder();
		public String query;
		public String nestedTable;
		public int driverObject;
		public String firstByOrder = null;
		public ArrayList<Integer> queryColToDimConfigIndx = new ArrayList<Integer>();
		public String getQuery() {
			if (query == null) {
				StringBuilder tmp = new StringBuilder();
				query = "select "+m_selClause+" from "+nestedTable+" join "+baseTable+" on where "+""+" = ? "+
			}
		}
		//public static  
	}
	*/
	static public class QueryParts {
		//public ArrayList<FirstRowInfo> m_firstRowGetterOfNested = new ArrayList<FirstRowInfo>();//indexed by DimConfigInfo
		public ArrayList<VirtualHelper> m_virtualCol = null; 
		public StringBuilder m_virtualQuery[] = null;//0 == logged, 1 = current, 2 = engine events, 3 = latest engine events
		public StringBuilder m_virtualQueryJustBefore[] = null;
		public ArrayList<HashMap<Integer, ArrayList<Pair<Integer, Integer>>>> m_virtualAttribIdToDimConfigColAnd1Checker = null; //see comments in buildVirtualQuery
		//index by 0,1,2,3,4 as above. Hashmap key = attrib Id, Value = List of dimconfig index and null indicator's col pos in query's sel

		public StringBuilder m_selClause = new StringBuilder();
		public StringBuilder m_fromClause = new StringBuilder();
		public StringBuilder m_whereClause = new StringBuilder();
		public StringBuilder m_groupByClause = new StringBuilder();
		public StringBuilder m_havingClause = new StringBuilder();
		public StringBuilder m_rollupClause = new StringBuilder();
		public StringBuilder m_aliasedOrderByClause = new StringBuilder();
		public boolean m_hasColWithAgg = false;
		public boolean m_needsRollup = false;
		public boolean m_doRollupAtJava = Misc.g_doRollupAtJava;// false;
		public ArrayList<Integer> m_isInclInGroupBy = new ArrayList<Integer>();
		
		public StringBuilder m_orderByClause = new StringBuilder();
		public StringBuilder m_orderByClauseAsec = new StringBuilder();
		public boolean m_hasGranularity  = false;
		public int m_desiredGranularity  = Misc.getUndefInt();
		public int m_limit = Misc.getUndefInt();
		public StringBuilder m_orderByFromLimit = null;
		public int m_hackSkipTimeGap = Misc.getUndefInt();
		public double m_hackSkipDistGap = Misc.getUndefInt();
		public int m_timeColIndex = Misc.getUndefInt();
		public int m_distColIndex = Misc.getUndefInt();
		public int m_vehicleIdColIndex = Misc.getUndefInt();		
		public String usesPeriodTable = null;
		public ArrayList<Integer> scopesNeededForYTDSpecAtCol = null;
		public ArrayList<Integer> groupByRollupAggIndicator = new ArrayList<Integer>(); //0 -> agg, 1 -> group by 2 -> roll up && group by
		public String frozenBaseTable = null;
		public ArrayList<Integer> getRollupIndices() {
			ArrayList<Integer> retval = new ArrayList<Integer>();
			for (int i=0,is=groupByRollupAggIndicator == null ? 0 : groupByRollupAggIndicator.size(); i<is;i++)
				if (groupByRollupAggIndicator.get(i) == 2)
					retval.add(i);
			return retval;
		}
		public void addYtdScope(int scope) {//ignore @period stuff
			if (scope >= 100)
				return;
			boolean found = false;
			for (int i=0,is=scopesNeededForYTDSpecAtCol == null ? 0 : scopesNeededForYTDSpecAtCol.size(); i<is; i++) {
				if (scopesNeededForYTDSpecAtCol.get(i) == scope) {
					found = true;
					break;
				}
			}
			if (!found) {
				if (scopesNeededForYTDSpecAtCol == null) {
					scopesNeededForYTDSpecAtCol = new ArrayList<Integer>();
				}
				scopesNeededForYTDSpecAtCol.add(scope);
			}
			
		}
		public boolean doJoinAreaCode = Misc.g_doAREACODE;
		public boolean isDrivenByPeriod = false;
		public boolean hasAtVehicleListQ = false;
		public boolean hasAtPeriodCol = false;
		public void mergeHasFields(QueryParts other) {
			isDrivenByPeriod  = isDrivenByPeriod || other.isDrivenByPeriod;
			hasAtVehicleListQ  = hasAtVehicleListQ || other.hasAtVehicleListQ;
			hasAtPeriodCol  = hasAtPeriodCol || other.hasAtPeriodCol;
		}
	}

	private static HashMap<String, OrderedTimeTableInfo> gTableList = new HashMap<String, OrderedTimeTableInfo>(); //the entries here must match the one below
	private static class OrderedTimeTableInfo {
		public String tabName = null;
		public String colName = null;
		public String startColName = null;
		public String endColName = null;
		public int indexInObjectType = Misc.getUndefInt();
		boolean hasVehicleIdFK = false;
		public int flexEventSearch = -1;//current trip_details, TPTR, DO can be searched by different time events
		public String flexEventDefaultCol = null;
		public int flexEventSearchAlt = -1;//for subq we may have diff for diff queries
		public String orgBasedTimeMatchEnd = null;
		public OrderedTimeTableInfo(String tabName, String colName, int indexInObjectType, String startColName, String endColName, boolean hasVehicleIdFK, int flexEventSearch, String flexEventDefaultCol, int flexEventSearchAlt, String orgBasedTimeMatchEnd) {
			this.tabName = tabName;
			this.colName = colName;
			this.startColName = startColName;
			this.endColName = endColName;
			this.indexInObjectType = indexInObjectType;
			this.hasVehicleIdFK = hasVehicleIdFK;
			this.flexEventSearch = flexEventSearch;
			this.flexEventDefaultCol = flexEventDefaultCol;
			this.flexEventSearchAlt = flexEventSearchAlt;
			this.orgBasedTimeMatchEnd = orgBasedTimeMatchEnd;
		}
		public StringBuilder appendFlexEventDateCol(StringBuilder sb) {
			if (flexEventSearch >= 0)
				sb.append("@");
			sb.append(tabName).append(".").append(colName);
			return sb;
		}
		public String getFlexEventDateCol() {
			return flexEventSearch >= 0 ? "@"+tabName+"."+colName : tabName+"."+colName; //change behaviour re trip_info - was earlier hard coded to @trip_info.combo_start 
			//... now effectively @trip_info.shift_date ... however doesnt matter
		}
	}
	private ArrayList<OrderedTimeTableInfo> mTableWithDateOrdered = new ArrayList<OrderedTimeTableInfo>();
	private static ArrayList<OrderedTimeTableInfo> gTableWithDateOrdered = new ArrayList<OrderedTimeTableInfo>();
	static {
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("@period","start_time",-1, "start_time", "end_time", false, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("mpl_coal_sample_details", "sampling_done_time", -1, "sampling_done_time","sampling_done_time", false, -1, null, -1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("tp_record","latest_load_gate_in_out",-1, "combo_start", "combo_end", true, 20064, "combo_start",20067, null));		
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("challan_details","challan_date",-1, "challan_date", "challan_date", true, -1, null,-1,"close_date")); //will be selecte 
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("trip_info_otherLU", "gate_in",-1, "gate_in", "gate_out", true,-1,null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("trip_info","shift_date",-1, "combo_start", "combo_end", true, 20061, "shift_date",-1, "unload_gate_out"));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("other_shipments","combo_start",WorkflowHelper.G_OTHERSHIPMENT, "combo_start", "combo_end", false, 40500, "combo_start",-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("demo_scan_trip","combo_start",-1, "combo_start", "combo_end", true, -1, null,-1, null));
		
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("engine_events","event_start_time",-1,"event_start_time", "event_stop_time", true,-1,null,-1, null));
		
		
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("logged_data","gps_record_time",-1, "gps_record_time", "gps_record_time", true, -1, null,-1, null));
		
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("ais_logged_data","gps_record_time",-1, "gps_record_time", "gps_record_time", true, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("ais_login_data","gps_record_time",-1, "gps_record_time", "gps_record_time", true, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("ais_health_data","gps_record_time",-1, "gps_record_time", "gps_record_time", true, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("ais_emergency_data","gps_record_time",-1, "gps_record_time", "gps_record_time", true, -1, null,-1, null));
		
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("rfid_handheld_log","record_time",WorkflowHelper.G_OBJ_DEVICE_LOG, "record_time", "record_time", false, 20066, "record_time",-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("vehicle_messages","in_date",-1, "in_date", "in_date", true, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("safety_violation_details","observed_time",-1, "observed_time", "observed_time", true, -1, null,-1, null));
		
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("vehicle_interaction_notes","updated_on",-1, "updated_on", "updated_on", true, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("vehicle_recvd_messages","record_time",-1, "record_time", "record_time", true, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("eta_info","src_exit",-1,"src_exit", "dest_entry_act", true, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("vehicle_maint","actual_start",-1,"actual_start", "actual_end", true, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("summary_trip_period_trip_detention","combo_start",-1, "combo_start", "combo_end", true, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("summary_trip_period_MPL","combo_start",-1, "combo_start", "combo_end", true,-1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("summary_safety_voilation","event_start_time",-1, "event_start_time", "event_stop_time", true,-1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("ttms_daily_report","start_date_time",-1, "start_date", "start_date", true,-1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("plans","plan_period_start",-1, "plan_period_start","plan_period_end", false,-1,null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("do_rr_details","start_date",-1,"start_date", "lapse_date", false,20065,"start_date",-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("mines_do_details","do_date",-1,"do_date", "validity_date", false,20090,"do_date",-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("qc_report_details","date_of_test",-1,"date_of_test", "date_of_test", false,-1,null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("genral_logging_details","on_date",-1,"on_date", "on_date", false,-1,null,-1, null));
		
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("rfid_trip","combo_start",-1,"combo_start", "combo_end", true,-1,null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("wb_log","wb_date",-1,"wb_date", "wb_date", true,-1,null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("logged_scans","gps_record_time",-1, "logged_scans", "logged_scans", true,-1,null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("po_line_item","po_release_date",WorkflowHelper.G_PO_LINE_ITEMS, "po_release_date", "po_release_date", false,-1,null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("grns","posting_date",WorkflowHelper.G_GRNS, "posting_date", "posting_date", false,-1,null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("workflows","created_on",WorkflowHelper.G_WORKFLOWS, "created_on", "updated_on", false,-1,null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("driver_blocking_hist", "block_from",-1, "block_from", "block_to", false,-1,null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("@indep_period","start_time",-1, "start_time", "end_time", false, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("swm_bin_status_info","start_time",WorkflowHelper.G_BINS, "start_time", "end_time", false, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("weightment","date_time",-1,"date_time", "date_time", true,-1,null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("bsnl_complaints","complaint_date",WorkflowHelper.G_OBJ_BSNL_COMPLAINTS, "complaint_date", "resolution_date", false, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("can_data","can_record_time",-1, "can_record_time", "can_record_time", true, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("user_login_track","ts",WorkflowHelper.G_USERS, "ts", "ts", false, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("user_mgmt_track","ts",WorkflowHelper.G_USERS, "ts", "ts", false, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("secl_tpr_invoice_hist","invoice_date",WorkflowHelper.G_TPRINVOICE_HIST_ID, "invoice_date", "invoice_date", false, -1, null,-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("mines_do_details_hist","do_date",WorkflowHelper.G_DORR_HIST_ID,"do_date", "validity_date", false,20090,"do_date",-1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("mining_log_message","recv_time",-1, "recv_time", "recv_time", true, -1, null,-1, null));					
//		gTableWithDateOrdered.add(new OrderedTimeTableInfo("rfid_log","load_wb_gross_date"));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("secl_operator_attendence", "date_of", -1, "date_of","date_of", false, -1, null, -1, null));		
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("ble_read_event", "gps_record_time", -1, "gps_record_time","gps_record_time", true, -1, null, -1, null));		
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("exc_load_event", "gps_record_time", -1, "gps_record_time","gps_record_time", true, -1, null, -1, null));
		gTableWithDateOrdered.add(new OrderedTimeTableInfo("sample_upload_details", "created_on", -1, "created_on","created_on", false, -1, null, -1, null));
		
		// To handle engine_event_track
		//gTableWithDateOrdered.add(new OrderedTimeTableInfo("engine_events_track","event_start_time"));
		

		for (OrderedTimeTableInfo entry : gTableWithDateOrdered) {
			int iv = WorkflowHelper.getObjectTypeFromTable(entry.tabName);
			entry.indexInObjectType = iv;
			gTableList.put(entry.tabName, entry);
		}
	}
	private static HashMap<String, String> gMatTableList = new HashMap<String, String>();
	private static HashMap<String,String> gOpstationVehicles = new HashMap<String, String>();
	public static String g_fdhs_unvisited_from_part = 
			" from op_station allop join opstation_mapping allopmap on (allop.id = allopmap.op_station_id and allop.status=1) "+
					" join port_nodes leaf on (allopmap.port_node_id = leaf.id) join port_nodes anc on (anc.id in (213) and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
					" left outer join  "+
					" (select max(trip_info.load_gate_in) lgin, load_gate_op opid from trip_info group by load_gate_op) mxloadin "+ 
					" on (mxloadin.opid = allop.id) "+
					" left outer join trip_info on (mxloadin.lgin = trip_info.load_gate_in ) "+ //could be buggy if multiple trips with same gate in at same op
					" left outer join vehicle on (vehicle.id = trip_info.vehicle_id) "
					;
	public static String g_summary_safety_voilation = "(" +
			" select voilation_count.id, voilation_count.vehicle_id, voilation_count.event_begin_longitude, voilation_count.event_begin_latitude,voilation_count.event_end_longitude," +
			" voilation_count.event_end_latitude,voilation_count.event_start_time,voilation_count.event_stop_time,voilation_count.attribute_id,voilation_count.attribute_value,voilation_count.refrence_rule_id," +
			" voilation_count.temp,voilation_count.acknowledgement,voilation_count.addnl_value1,voilation_count.acknowledgement_time,voilation_count.assessed_factor, voilation_count.rule_id,voilation_count.count,voilation_count.event_begin_name ,voilation_count.event_end_name " +
			" from "+
			" ((select engine_events.id, engine_events.vehicle_id, engine_events.event_begin_longitude, engine_events.event_begin_latitude,engine_events.event_end_longitude,engine_events.event_end_latitude,engine_events.event_start_time,engine_events.event_stop_time,engine_events.attribute_id,engine_events.attribute_value,engine_events.refrence_rule_id,engine_events.temp,engine_events.acknowledgement,engine_events.addnl_value1,engine_events.acknowledgement_time,engine_events.assessed_factor, engine_events.rule_id, (case when engine_events.id is not null then 1 else 0 end) count,engine_events.event_begin_name ,engine_events.event_end_name  from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id  left outer join engine_events on (engine_events.vehicle_id = vehicle.id and engine_events.rule_id in (319))  where engine_events.event_start_time >= '@user_start' and engine_events.event_start_time <= '@user_end') "+
			" union all "+ 
			" (select engine_events.id, engine_events.vehicle_id, engine_events.event_begin_longitude, engine_events.event_begin_latitude,engine_events.event_end_longitude,engine_events.event_end_latitude,engine_events.event_start_time,engine_events.event_stop_time,engine_events.attribute_id,engine_events.attribute_value,engine_events.refrence_rule_id,engine_events.temp,engine_events.acknowledgement,engine_events.addnl_value1,engine_events.acknowledgement_time,engine_events.assessed_factor, engine_events.rule_id,(case when engine_events.id is not null then 1 else 0 end) count,engine_events.event_begin_name ,engine_events.event_end_name from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id  left outer join engine_events on (engine_events.vehicle_id = vehicle.id and engine_events.rule_id in (320))  where engine_events.event_start_time >= '@user_start' and engine_events.event_start_time <= '@user_end' ) "+
			" union all "+
			" (select engine_events.id, engine_events.vehicle_id, engine_events.event_begin_longitude, engine_events.event_begin_latitude,engine_events.event_end_longitude,engine_events.event_end_latitude,engine_events.event_start_time,engine_events.event_stop_time,engine_events.attribute_id,engine_events.attribute_value,engine_events.refrence_rule_id,engine_events.temp,engine_events.acknowledgement,engine_events.addnl_value1,engine_events.acknowledgement_time,engine_events.assessed_factor, engine_events.rule_id,(case when engine_events.id is not null then 1 else 0 end) count,engine_events.event_begin_name ,engine_events.event_end_name from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id  left outer join engine_events on (engine_events.vehicle_id = vehicle.id and engine_events.rule_id in (482))  where engine_events.event_start_time >= '@user_start' and engine_events.event_start_time <= '@user_end' ) "+
			" union all "+
			" (select engine_events.id, engine_events.vehicle_id, engine_events.event_begin_longitude, engine_events.event_begin_latitude,engine_events.event_end_longitude,engine_events.event_end_latitude,engine_events.event_start_time,engine_events.event_stop_time,engine_events.attribute_id,engine_events.attribute_value,engine_events.refrence_rule_id,engine_events.temp,engine_events.acknowledgement,engine_events.addnl_value1,engine_events.acknowledgement_time,engine_events.assessed_factor, engine_events.rule_id,(case when engine_events.id is not null then 1 else 0 end) count,engine_events.event_begin_name ,engine_events.event_end_name from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id  left outer join engine_events on (engine_events.vehicle_id = vehicle.id and engine_events.rule_id in (483))  where engine_events.event_start_time >= '@user_start' and engine_events.event_start_time <= '@user_end' ) "+
			" union all "+
			" (select engine_events.id, engine_events.vehicle_id, engine_events.event_begin_longitude, engine_events.event_begin_latitude,engine_events.event_end_longitude,engine_events.event_end_latitude,engine_events.event_start_time,engine_events.event_stop_time,engine_events.attribute_id,engine_events.attribute_value,engine_events.refrence_rule_id,engine_events.temp,engine_events.acknowledgement,engine_events.addnl_value1,engine_events.acknowledgement_time,engine_events.assessed_factor, engine_events.rule_id,(case when engine_events.id is not null then 1 else 0 end) count,engine_events.event_begin_name ,engine_events.event_end_name from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id  left outer join engine_events on (engine_events.vehicle_id = vehicle.id and engine_events.rule_id in (484))  where engine_events.event_start_time >= '@user_start' and engine_events.event_start_time <= '@user_end' ) "+
			" union all "+
			" (select engine_events.id, engine_events.vehicle_id, engine_events.event_begin_longitude, engine_events.event_begin_latitude,engine_events.event_end_longitude,engine_events.event_end_latitude,engine_events.event_start_time,engine_events.event_stop_time,engine_events.attribute_id,engine_events.attribute_value,engine_events.refrence_rule_id,engine_events.temp,engine_events.acknowledgement,engine_events.addnl_value1,engine_events.acknowledgement_time,engine_events.assessed_factor, engine_events.rule_id,(case when engine_events.id is not null then 1 else 0 end) count,engine_events.event_begin_name ,engine_events.event_end_name from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id  left outer join engine_events on (engine_events.vehicle_id = vehicle.id and engine_events.rule_id in (485))  where engine_events.event_start_time >= '@user_start' and engine_events.event_start_time <= '@user_end' ) "+
			" union all "+
			" (select null, vehicle.id, null,null,null,null,'@user_start','@user_end',null,null,null,null,null,null,null,null,319 rule_id,0 count,null,null from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id and vehicle.plant is not null ) "+
			" union all "+
			" (select null, vehicle.id, null,null,null,null,'@user_start','@user_end',null,null,null,null,null,null,null,null,320 rule_id,0 count,null,null from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id and vehicle.plant is not null) "+
			" union all "+
			" (select null, vehicle.id, null,null,null,null,'@user_start','@user_end',null,null,null,null,null,null,null,null,482 rule_id,0 count,null,null from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id and vehicle.plant is not null) "+
			" union all "+
			" (select null, vehicle.id, null,null,null,null,'@user_start','@user_end',null,null,null,null,null,null,null,null,483 rule_id,0 count,null,null from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id and vehicle.plant is not null) "+
			" union all "+
			" (select null, vehicle.id, null,null,null,null,'@user_start','@user_end',null,null,null,null,null,null,null,null,484 rule_id,0 count,null,null from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id and vehicle.plant is not null) "+
			" union all "+
			" (select null, vehicle.id, null,null,null,null,'@user_start','@user_end',null,null,null,null,null,null,null,null,485 rule_id,0 count,null,null from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id and vehicle.plant is not null)) voilation_count) "+
			" summary_safety_voilation ";
	public static String g_summary_trip_period_trip_detention = " ( " +
			" select trip_event_count.vehicle_id, trip_event_count.id, " +
			" trip_event_count.combo_start, trip_event_count.load_gate_op, trip_event_count.load_gate_in, trip_event_count.load_area_wait_in, " +
			" trip_event_count.load_area_in, trip_event_count.load_area_out,trip_event_count.load_area_wait_out, trip_event_count.load_gate_out, " +
			" trip_event_count.unload_gate_op, trip_event_count.unload_gate_in, trip_event_count.unload_area_wait_in,trip_event_count.unload_area_in, " +
			" trip_event_count.unload_area_out,trip_event_count.unload_area_wait_out, trip_event_count.unload_gate_out, trip_event_count.combo_end, " +
			" trip_event_count.confirm_time, trip_event_count.info_complete, trip_event_count.shift_date, trip_event_count.shift, trip_event_count.driver, " +
			" trip_event_count.movement_type,trip_event_count.wb1in, trip_event_count.wb2in, trip_event_count.wb1out, " +
			" trip_event_count.wb2out, trip_event_count.u_wb1in,trip_event_count.u_wb2in, trip_event_count.u_wb1out, trip_event_count.u_wb2out" +
			",trip_event_count.count ,trip_event_count.rule_id from " +
			" (" +
			" (select trip_info.vehicle_id, trip_info.load_gate_in,trip_info.load_gate_out, trip_info.load_area_in, trip_info.load_area_out, trip_info.unload_gate_in, " +
			" trip_info.unload_gate_out,trip_info.unload_area_in, trip_info.unload_area_out,trip_info.confirm_time, trip_info.load_area_wait_in, trip_info.unload_area_wait_in,trip_info.info_complete, " +
			" trip_info.id, trip_info.shift_date, trip_info.shift, trip_info.driver, trip_info.load_gate_op, trip_info.unload_gate_op,trip_info.combo_start, trip_info.combo_end," +
			" trip_info.load_area_wait_out,trip_info.unload_area_wait_out, trip_info.movement_type, trip_info.wb1in, trip_info.wb2in, trip_info.wb1out, trip_info.wb2out, " +
			" trip_info.u_wb1in,trip_info.u_wb2in, trip_info.u_wb1out, trip_info.u_wb2out, " +
			" (case when (TIMESTAMPDIFF(MINUTE, load_area_wait_in, load_gate_in) > 30) then 1 else 0 end) count, 1 rule_id from vehicle join " +
			" (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
			" left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on " +
			" (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
			" or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
			" left outer join trip_info on (trip_info.vehicle_id = vehicle.id)  where trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end') " +
			" union all " +
			" (select trip_info.vehicle_id, trip_info.load_gate_in,trip_info.load_gate_out, trip_info.load_area_in, trip_info.load_area_out, trip_info.unload_gate_in, " +
			" trip_info.unload_gate_out,trip_info.unload_area_in, trip_info.unload_area_out,trip_info.confirm_time, trip_info.load_area_wait_in, trip_info.unload_area_wait_in,trip_info.info_complete, " +
			" trip_info.id, trip_info.shift_date, trip_info.shift, trip_info.driver, trip_info.load_gate_op, trip_info.unload_gate_op,trip_info.combo_start, trip_info.combo_end," +
			" trip_info.load_area_wait_out,trip_info.unload_area_wait_out, trip_info.movement_type, trip_info.wb1in, trip_info.wb2in, trip_info.wb1out, trip_info.wb2out, " +
			" trip_info.u_wb1in,trip_info.u_wb2in, trip_info.u_wb1out, trip_info.u_wb2out, " +
			" (case when (TIMESTAMPDIFF(MINUTE, load_gate_in, load_area_in) > 10) then 1 else 0 end) count, 2 rule_id " +
			" from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
			" left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join " +
			" port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or " +
			" (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
			" left outer join trip_info on (trip_info.vehicle_id = vehicle.id)  where trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end') " +
			" union all " +
			" (select trip_info.vehicle_id, trip_info.load_gate_in,trip_info.load_gate_out, trip_info.load_area_in, trip_info.load_area_out, trip_info.unload_gate_in, " +
			" trip_info.unload_gate_out,trip_info.unload_area_in, trip_info.unload_area_out,trip_info.confirm_time, trip_info.load_area_wait_in, trip_info.unload_area_wait_in,trip_info.info_complete, " +
			" trip_info.id, trip_info.shift_date, trip_info.shift, trip_info.driver, trip_info.load_gate_op, trip_info.unload_gate_op,trip_info.combo_start, trip_info.combo_end," +
			" trip_info.load_area_wait_out,trip_info.unload_area_wait_out, trip_info.movement_type, trip_info.wb1in, trip_info.wb2in, trip_info.wb1out, trip_info.wb2out, " +
			" trip_info.u_wb1in,trip_info.u_wb2in, trip_info.u_wb1out, trip_info.u_wb2out, " +
			" (case when (TIMESTAMPDIFF(MINUTE, load_area_out, wb2in) > 30) then 1 else 0 end) count, 3 rule_id " +
			" from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
			" left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) " +
			" join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
			" or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
			" left outer join trip_info on (trip_info.vehicle_id = vehicle.id)  where trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end') " +
			" union all " +
			" (select trip_info.vehicle_id, trip_info.load_gate_in,trip_info.load_gate_out, trip_info.load_area_in, trip_info.load_area_out, trip_info.unload_gate_in, " +
			" trip_info.unload_gate_out,trip_info.unload_area_in, trip_info.unload_area_out,trip_info.confirm_time, trip_info.load_area_wait_in, trip_info.unload_area_wait_in,trip_info.info_complete, " +
			" trip_info.id, trip_info.shift_date, trip_info.shift, trip_info.driver, trip_info.load_gate_op, trip_info.unload_gate_op,trip_info.combo_start, trip_info.combo_end," +
			" trip_info.load_area_wait_out,trip_info.unload_area_wait_out, trip_info.movement_type, trip_info.wb1in, trip_info.wb2in, trip_info.wb1out, trip_info.wb2out, " +
			" trip_info.u_wb1in,trip_info.u_wb2in, trip_info.u_wb1out, trip_info.u_wb2out, " +
			" (case when (TIMESTAMPDIFF(MINUTE, wb2in, load_gate_out) > 10) then 1 else 0 end) count, 4 rule_id " +
			" from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
			" left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) " +
			" join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
			" or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
			" left outer join trip_info on (trip_info.vehicle_id = vehicle.id)  where trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end') " +
			" ) " +
			" trip_event_count " +
			" ) " +
			" summary_trip_period_trip_detention"; 
    public static String g_summary_trip_voilation = " (select trip_info.id , sum(case when stoppage.id is null then 1 else 0 end) stop_count,sum(case when route.id is not null then 1 else 0 end) route_count " +
    		" from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on " +
    		" (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (481) and " +
    		" ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on " +
    		" vi.vehicle_id = vehicle.id left outer join trip_info on (trip_info.vehicle_id=vehicle.id) left outer join (select id,vehicle_id,event_start_time,event_stop_time from engine_events " +
    		" where engine_events.event_start_time >= '@user_start' and engine_events.event_start_time <= '@user_end' and engine_events.rule_id in (1)) stoppage on (stoppage.event_start_time >= trip_info.load_gate_in " +
    		" and (stoppage.event_stop_time <= trip_info.unload_gate_in or trip_info.unload_gate_in is null) and stoppage.vehicle_id=trip_info.vehicle_id ) left outer join  (select id,vehicle_id,event_start_time,event_stop_time " +
    		" from engine_events where engine_events.event_start_time >= '@user_start' and engine_events.event_start_time <= '@user_end' and engine_events.rule_id in (489)) route on (route.event_start_time >= trip_info.load_gate_in " +
    		" and (route.event_stop_time <= trip_info.unload_gate_in or trip_info.unload_gate_in is null) and route.vehicle_id=trip_info.vehicle_id) where trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end' " +
    		" group by trip_info.id) summary_trip_voilation";
    public static String g_summary_trip_critical_events = " (select trip_info.id trip_info_id, sum(case when engine_events.id is not null then 1 else 0 end) event_count  from vehicle " +
    		" join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
    		" left outer join vehicle_access_groups on  (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on " +
    		" (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and  ((anc.lhs_number <= leaf.lhs_number " +
    		" and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on  vi.vehicle_id = vehicle.id " +
    		" left outer join trip_info on (trip_info.vehicle_id=vehicle.id) left outer join engine_events on (trip_info.vehicle_id=engine_events.vehicle_id and engine_events.event_start_time >= coalesce(trip_info.load_gate_in,trip_info.combo_start) " +
    		" and (engine_events.event_start_time <= coalesce(trip_info.unload_gate_in,trip_info.unload_gate_out,trip_info.confirm_time) or coalesce(trip_info.unload_gate_in,trip_info.unload_gate_out,trip_info.confirm_time) is null)) " +
    		" where engine_events.criticality in (4,5) and trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end'  group by trip_info.id) " +
    		" summary_trip_critical_events ";
    //BELOW IS NOT USED
    public static String g_trip_opstation = " (select trip_info.id trip_info_id,op1.name unload_op,op1.sub_type unload_op_sub_type,op2.name nearest_cfa_op,op2.sub_type nearest_cfa_sub_type,op_lm.name nearest_cfa_lm,op_lm.sub_type nearest_cfa_lm_sub_type" +
    		" ,(case when (challan_details.consignee like '%CFA%' or challan_details.consignee like '%CDP%' or challan_details.consignee like '%Lafarge%') then challan_details.consignee else concat(challan_details.consignee,',',challan_details.dest_city,',',challan_details.dest_state) end) jde_op" +
    		" ,(case when (challan_details.consignee like '%CFA%' or challan_details.consignee like '%CDP%'  or challan_details.consignee like '%Lafarge%') then 102 else 103 end) jde_op_sub_type " +
    		" from vehicle join  (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on  (custleaf.id = vehicle.customer_id) " +
    		" left outer join vehicle_access_groups on   (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on  (leaf.id = vehicle_access_groups.port_node_id) " +
    		" join port_nodes anc  on (anc.id in (@pv123) and   ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number  " +
    		" and anc.rhs_number >= custleaf.rhs_number))) ) vi on  vi.vehicle_id = vehicle.id left outer join trip_info on  (trip_info.vehicle_id=vehicle.id) left outer join op_station op1 " +
    		" on (op1.id=trip_info.unload_gate_op) left outer join op_station op2 on (op2.id=trip_info.unload_nearest_opid) left outer join landmarks op_lm on (op_lm.id=trip_info.unload_nearest_lmid) " +
    		" left outer join challan_details on (challan_details.trip_info_id = trip_info.id) where trip_info.combo_start >= '@user_start' and  trip_info.combo_start <= '@user_end') " +
    		" trip_opstation_detail ";
	public static String g_summary_trip_period_MPL = " ( " +
			" select trip_event_count.vehicle_id, trip_event_count.id, " +
			" trip_event_count.combo_start, trip_event_count.load_gate_op, trip_event_count.load_gate_in, trip_event_count.load_area_wait_in, " +
			" trip_event_count.load_area_in, trip_event_count.load_area_out,trip_event_count.load_area_wait_out, trip_event_count.load_gate_out, " +
			" trip_event_count.unload_gate_op, trip_event_count.unload_gate_in, trip_event_count.unload_area_wait_in,trip_event_count.unload_area_in, " +
			" trip_event_count.unload_area_out,trip_event_count.unload_area_wait_out, trip_event_count.unload_gate_out, trip_event_count.combo_end, " +
			" trip_event_count.confirm_time, trip_event_count.info_complete, trip_event_count.shift_date, trip_event_count.shift, trip_event_count.driver, " +
			" trip_event_count.movement_type,trip_event_count.wb1in, trip_event_count.wb2in, trip_event_count.wb1out, " +
			" trip_event_count.wb2out, trip_event_count.u_wb1in,trip_event_count.u_wb2in, trip_event_count.u_wb1out, trip_event_count.u_wb2out" +
			",trip_event_count.count ,trip_event_count.rule_id from " +
			" (" +
			" (select trip_info.vehicle_id, trip_info.load_gate_in,trip_info.load_gate_out, trip_info.load_area_in, trip_info.load_area_out, trip_info.unload_gate_in, " +
			" trip_info.unload_gate_out,trip_info.unload_area_in, trip_info.unload_area_out,trip_info.confirm_time, trip_info.load_area_wait_in, trip_info.unload_area_wait_in,trip_info.info_complete, " +
			" trip_info.id, trip_info.shift_date, trip_info.shift, trip_info.driver, trip_info.load_gate_op, trip_info.unload_gate_op,trip_info.combo_start, trip_info.combo_end," +
			" trip_info.load_area_wait_out,trip_info.unload_area_wait_out, trip_info.movement_type, trip_info.wb1in, trip_info.wb2in, trip_info.wb1out, trip_info.wb2out, " +
			" trip_info.u_wb1in,trip_info.u_wb2in, trip_info.u_wb1out, trip_info.u_wb2out, " +
			" (case when trip_info.unload_gate_op=3211237 and trip_info.confirm_time is not null then 1 else 0 end) count, 1 rule_id from vehicle join " +
			" (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
			" left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on " +
			" (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
			" or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
			" left outer join trip_info on (trip_info.vehicle_id = vehicle.id)  where trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end') " +
			" union all " +
			" (select trip_info.vehicle_id, trip_info.load_gate_in,trip_info.load_gate_out, trip_info.load_area_in, trip_info.load_area_out, trip_info.unload_gate_in, " +
			" trip_info.unload_gate_out,trip_info.unload_area_in, trip_info.unload_area_out,trip_info.confirm_time, trip_info.load_area_wait_in, trip_info.unload_area_wait_in,trip_info.info_complete, " +
			" trip_info.id, trip_info.shift_date, trip_info.shift, trip_info.driver, trip_info.load_gate_op, trip_info.unload_gate_op,trip_info.combo_start, trip_info.combo_end," +
			" trip_info.load_area_wait_out,trip_info.unload_area_wait_out, trip_info.movement_type, trip_info.wb1in, trip_info.wb2in, trip_info.wb1out, trip_info.wb2out, " +
			" trip_info.u_wb1in,trip_info.u_wb2in, trip_info.u_wb1out, trip_info.u_wb2out, " +
			" (case when (trip_info.unload_gate_op != 3211237 or trip_info.unload_gate_op is null) and trip_info.confirm_time is not null then 1 else 0 end) count, 2 rule_id " +
			" from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
			" left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join " +
			" port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or " +
			" (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
			" left outer join trip_info on (trip_info.vehicle_id = vehicle.id)  where trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end') " +
			" union all " +
			" (select trip_info.vehicle_id, trip_info.load_gate_in,trip_info.load_gate_out, trip_info.load_area_in, trip_info.load_area_out, trip_info.unload_gate_in, " +
			" trip_info.unload_gate_out,trip_info.unload_area_in, trip_info.unload_area_out,trip_info.confirm_time, trip_info.load_area_wait_in, trip_info.unload_area_wait_in,trip_info.info_complete, " +
			" trip_info.id, trip_info.shift_date, trip_info.shift, trip_info.driver, trip_info.load_gate_op, trip_info.unload_gate_op,trip_info.combo_start, trip_info.combo_end," +
			" trip_info.load_area_wait_out,trip_info.unload_area_wait_out, trip_info.movement_type, trip_info.wb1in, trip_info.wb2in, trip_info.wb1out, trip_info.wb2out, " +
			" trip_info.u_wb1in,trip_info.u_wb2in, trip_info.u_wb1out, trip_info.u_wb2out, " +
			" (case when vehicle_extended.plant =111 and trip_info.load_gate_in is not null and trip_info.unload_gate_in is null then 1 else 0 end) count, 3 rule_id " +
			" from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
			" left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) " +
			" join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
			" or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
			" left outer join trip_info on (trip_info.vehicle_id = vehicle.id) left outer join vehicle_extended on (vehicle_extended.vehicle_id=vehicle.id)  where trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end') " +
			" union all " +
			" (select trip_info.vehicle_id, trip_info.load_gate_in,trip_info.load_gate_out, trip_info.load_area_in, trip_info.load_area_out, trip_info.unload_gate_in, " +
			" trip_info.unload_gate_out,trip_info.unload_area_in, trip_info.unload_area_out,trip_info.confirm_time, trip_info.load_area_wait_in, trip_info.unload_area_wait_in,trip_info.info_complete, " +
			" trip_info.id, trip_info.shift_date, trip_info.shift, trip_info.driver, trip_info.load_gate_op, trip_info.unload_gate_op,trip_info.combo_start, trip_info.combo_end," +
			" trip_info.load_area_wait_out,trip_info.unload_area_wait_out, trip_info.movement_type, trip_info.wb1in, trip_info.wb2in, trip_info.wb1out, trip_info.wb2out, " +
			" trip_info.u_wb1in,trip_info.u_wb2in, trip_info.u_wb1out, trip_info.u_wb2out, " +
			" (case when (vehicle_extended.plant = 112 or vehicle_extended.plant is null) and trip_info.load_gate_in is not null and trip_info.unload_gate_in is null then 1 else 0 end) count, 4 rule_id " +
			" from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
			" left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) " +
			" join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
			" or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
			" left outer join trip_info on (trip_info.vehicle_id = vehicle.id) left outer join vehicle_extended on (vehicle_extended.vehicle_id=vehicle.id) where trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end') " +
			" union all"+
			" (select trip_info.vehicle_id, trip_info.load_gate_in,trip_info.load_gate_out, trip_info.load_area_in, trip_info.load_area_out, trip_info.unload_gate_in, trip_info.unload_gate_out,trip_info.unload_area_in, trip_info.unload_area_out,trip_info.confirm_time, trip_info.load_area_wait_in, trip_info.unload_area_wait_in,trip_info.info_complete, trip_info.id, trip_info.shift_date, trip_info.shift, trip_info.driver, trip_info.load_gate_op, trip_info.unload_gate_op,trip_info.combo_start, trip_info.combo_end,trip_info.load_area_wait_out,trip_info.unload_area_wait_out, trip_info.movement_type, trip_info.wb1in, trip_info.wb2in, trip_info.wb1out, trip_info.wb2out, trip_info.u_wb1in,trip_info.u_wb2in, trip_info.u_wb1out, trip_info.u_wb2out, (case when ((trip_info.unload_gate_op=3211237 and trip_info.confirm_time) or (vehicle_extended.plant =111 and trip_info.load_gate_in is not null and trip_info.unload_gate_in is null)) and rule_id in (482,483,484) then 1 else (case when trip_info_otherLU.gate_in is not null and trip_info_otherLU.gate_in >= load_gate_out and ((trip_info.unload_gate_op=3211237 and trip_info.confirm_time) or (vehicle_extended.plant =111 and trip_info.load_gate_in is not null and trip_info.unload_gate_in is null)) and (case when coalesce(unload_gate_in, confirm_time) is not null then trip_info_otherLU.gate_in <= coalesce(unload_gate_in, confirm_time) else true end) then 1 else 0 end) end) count, 6 rule_id from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id left outer join trip_info on (trip_info.vehicle_id = vehicle.id) left outer join vehicle_extended on (vehicle_extended.vehicle_id=vehicle.id)  left outer join engine_events on (trip_info.load_gate_out is not null and event_start_time >= load_gate_out  and (case when coalesce(unload_gate_in, confirm_time) is not null then event_start_time <= coalesce(unload_gate_in, confirm_time) end) and engine_events.vehicle_id = trip_info.vehicle_id) left outer join trip_info_otherLU on (trip_info.id = trip_info_otherLU.trip_id) join op_station on (trip_info_otherLU.opstation_id != op_station.id and op_station.name != 'MPL Coal Stock Yard')  where trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end') " +
			" union all "+
			" (select trip_info.vehicle_id, trip_info.load_gate_in,trip_info.load_gate_out, trip_info.load_area_in, trip_info.load_area_out, trip_info.unload_gate_in, trip_info.unload_gate_out,trip_info.unload_area_in, trip_info.unload_area_out,trip_info.confirm_time, trip_info.load_area_wait_in, trip_info.unload_area_wait_in,trip_info.info_complete, trip_info.id, trip_info.shift_date, trip_info.shift, trip_info.driver, trip_info.load_gate_op, trip_info.unload_gate_op,trip_info.combo_start, trip_info.combo_end,trip_info.load_area_wait_out,trip_info.unload_area_wait_out, trip_info.movement_type, trip_info.wb1in, trip_info.wb2in, trip_info.wb1out, trip_info.wb2out, trip_info.u_wb1in,trip_info.u_wb2in, trip_info.u_wb1out, trip_info.u_wb2out, (case when ((trip_info.unload_gate_op=3211237 and trip_info.confirm_time) or (vehicle_extended.plant =111 and trip_info.load_gate_in is not null and trip_info.unload_gate_in is null)) and (trip_info.u_wb1in is null or trip_info.u_wb2in is null or trip_info.unload_area_in is null ) then 1 else (case when ((trip_info.unload_gate_op=3211237 and trip_info.confirm_time) or (vehicle_extended.plant =111 and trip_info.load_gate_in is not null and trip_info.unload_gate_in is null)) and (area_info_otherLU.gate_in is not null and trip_info.u_wb2in is null and area_info_otherLU.gate_in < trip_info.u_wb2in) then 1 else 0 end) end) count, 7 rule_id from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id left outer join trip_info on (trip_info.vehicle_id = vehicle.id) left outer join vehicle_extended on (vehicle_extended.vehicle_id=vehicle.id)  left outer join area_info_otherLU on (trip_info.load_gate_out is not null and trip_info.id = area_info_otherLU.trip_id) where trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end') "+
			" ) " +
			" trip_event_count " +
			" ) " +
			" summary_trip_period_MPL"; 
	
	public static String g_trip_info_ext = " ( "+
			" select trip_info.vehicle_id, load_gate_in, load_gate_out, load_area_in, " +
			"	load_area_out, load_area_guess, load_area_manual, load_material_guess, load_amt_guess, load_amt_manual, " +
			"	load_material_manual, unload_gate_in, unload_gate_out, unload_area_in, unload_area_out, unload_area_guess, " +
			"	unload_area_manual, confirm_time, load_area_wait_in, unload_area_wait_in, info_complete, trip_info.id, " +
			"	shift_date, shift, trip_info.driver, load_gate_op, unload_gate_op, is_curr_channel, load_region_loc, " +
			"	unload_material_guess, create_recv_time, unload_region_loc, combo_start, combo_end, target_unload_area_guess, " +
			"	target_unload_area_manual, wb1id, wb1name, wb1time, wb2id, wb2name, wb2time, wb3id, wb3name, wb3time, wb4id," +
			" 	wb4name, wb4time, wb1in, wb2in, wb3in, u_wb1in, u_wb2in, u_wb3in, u_wb1id, u_wb2id, u_wb3id, load_area_wait_out," +
			" 	unload_area_wait_out, movement_type, load_first_area_guess, load_first_area_in_time, load_first_area_out_time, " +
			"	unload_first_area_guess, unload_first_area_in_time, unload_first_area_out_time, "+
			" wb1out, wb2out, wb3out, u_wb1out, u_wb2out, u_wb3out, unload_amt_guess, "+
			" challan_details.id challan_id, challan_details.port_node_id challan_port_node_id, challan_details.vehicle_id challan_vehicle_id, " +
			"	challan_details.branch challan_branch, challan_details.challan_no challan_challan_no, " +
			"	challan_details.truck_no challan_truck_no, challan_details.consignor challan_consignor, " +
			"	challan_details.cig_agent challan_cig_agent, challan_details.bill_party challan_bill_party, " +
			"	challan_details.shipping_line challan_shipping_line, challan_details.container_1_no challan_container_1_no, " +
			"	challan_details.container_1_size challan_container_1_size, challan_details.from_location challan_from_location, " +
			"	challan_details.to_location challan_to_location, challan_details.description challan_description, " +
			"	challan_details.transporter challan_transporter, challan_details.notes challan_notes, " +
			"	challan_details.challan_date challan_challan_date, challan_details.challan_rec_date " +
			"	challan_challan_rec_date, challan_details.consignee challan_consignee, " +
			"	challan_details.job_order_no challan_job_order_no, challan_details.container_2_no challan_container_2_no, " +
			"	challan_details.container_2_size challan_container_2_size,challan_details.driver challan_driver, " +
			"	challan_details.wt_by_qty challan_wt_by_qty, challan_details.challan_type challan_challan_type, " +
			"	challan_details.trip_info_id challan_trip_info_id, challan_details.updated_on challan_updated_on, " +
			"	challan_details.challan_seal_1 challan_challan_seal_1, challan_details.challan_seal_2 challan_challan_seal_2, " +
			"	challan_details.material_id challan_material_id, challan_details.teu_desc challan_teu_desc from " +
			"	(select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on " +
			"	(custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on " +
			"	(vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on " +
			"	(leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and " +
			"	((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  " +
			"	(anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
			"	join trip_info  on (vi.vehicle_id = trip_info.vehicle_id " +
			"	and ((trip_info.combo_start >= '@user_start' and trip_info.combo_start <= '@user_end') or " +
			"        (trip_info.combo_end >= '@user_start' and trip_info.combo_end <= '@user_end'))) "+
			"	left outer join " +
			"	challan_details on (trip_info.id=challan_details.trip_info_id) " +
			"	 " +
			"	union all " +
			"	select vi.vehicle_id vehicle_id,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null" +
			"	,null,null,challan_details.challan_date shift_date,null,null,null,null,null,null,null,null,null," +
			"	challan_details.challan_date combo_start,challan_details.challan_date combo_end,null,null,null,null,null,null,null," +
			"	null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null," +
			"	null,null,null,null,null,null, "+
			" null, null, null, null, null, null, null, "+
			" challan_details.id challan_id, challan_details.port_node_id challan_port_node_id, " +
			"	challan_details.vehicle_id challan_vehicle_id, challan_details.branch challan_branch, " +
			"	challan_details.challan_no challan_challan_no, challan_details.truck_no challan_truck_no, " +
			"	challan_details.consignor challan_consignor, challan_details.cig_agent challan_cig_agent, " +
			"	challan_details.bill_party challan_bill_party, challan_details.shipping_line challan_shipping_line, " +
			"	challan_details.container_1_no challan_container_1_no, challan_details.container_1_size challan_container_1_size, " +
			"	challan_details.from_location challan_from_location, challan_details.to_location challan_to_location, " +
			"	challan_details.description challan_description, challan_details.transporter challan_transporter, " +
			"	challan_details.notes challan_notes, challan_details.challan_date challan_challan_date, " +
			"	challan_details.challan_rec_date challan_challan_rec_date, challan_details.consignee challan_consignee, " +
			"	challan_details.job_order_no challan_job_order_no, challan_details.container_2_no challan_container_2_no, " +
			"	challan_details.container_2_size challan_container_2_size,challan_details.driver challan_driver, " +
			"	challan_details.wt_by_qty challan_wt_by_qty, challan_details.challan_type challan_challan_type, " +
			"	challan_details.trip_info_id challan_trip_info_id, challan_details.updated_on challan_updated_on, " +
			"	challan_details.challan_seal_1 challan_challan_seal_1, challan_details.challan_seal_2 " +
			"	challan_challan_seal_2, challan_details.material_id challan_material_id, challan_details.teu_desc " +
			"	challan_teu_desc from " +
			"	(select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on " +
			"	(custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on " +
			"	(vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on " +
			"	(leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and " +
			"	((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  " +
			"	(anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
			"	join challan_details on (vi.vehicle_id = challan_details.vehicle_id " +
			"	and ((challan_details.challan_date >= '@user_start' and challan_details.challan_date <= '@user_end'))) "+
			"	where challan_details.trip_info_id is null "+
			")  trip_info_ext " ;

	public static String g_material_list = " ( select generic_params.id , generic_params.name material_name from generic_params join" +
			" 	port_nodes leaf on (leaf.id = generic_params.port_node_id ) join port_nodes" +
			" 	anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id in (@pv123)))" +
			" 	material_list";

	public static String g_ytdPlanTillToday = " (select material_id, cast(sum(qty) as decimal(10,1)) tot_till_today "+
			"	, cast(sum(qty*(case when end_time < now() then 1  when start_time >= now() then 0 "+
			"	else datediff(date(now()),start_time)/datediff(end_time, start_time) "+
			"	end)) as decimal(10,1)) ytd_till_today "+
			"	from production_plan join port_nodes leaf on (leaf.id = production_plan.port_node_id) join port_nodes anc on "+ 
			"	(anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id in (@pv123)) "+
			"	where start_time >= (case when month(now()) > 3 then concat(year(now()),'/04/01') else concat(year(now())-1,'/04/01') end) "+
			"	and start_time < (case when month(now()) > 3 then concat(year(now())+1,'/04/01') else concat(year(now()),'/04/01') end) "+
			"	group by material_id) mat_ytd_plan_till_today ";

	public static String g_planToday = " (select material_id, cast(sum(qty*1.0/datediff(end_time,start_time)) as decimal(10,1)) tot_today "+
			"	from production_plan join port_nodes leaf on (leaf.id = production_plan.port_node_id) join port_nodes anc on "+ 
			"	(anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id in (@pv123)) "+
			"	where start_time <= now() and end_time > now() "+
			"	group by material_id) mat_plan_today ";

	public static String g_ytdProdByChallanTillToday = " (select material_id, cast(sum(wt_by_qty)as decimal(10,1)) tot_actual_till_today, count(*) challan_count_till_today from challan_details join port_nodes leaf on (leaf.id = challan_details.port_node_id) "+
			"	join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id in (@pv123)) "+
			"	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and date(start_time) = date(now())) end_marker "+
			"	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and date(start_time) = (case when month(now()) > 3 then concat(year(now()),'/04/01') else concat(year(now())-1,'/04/01') end)) beg_marker "+
			"	where material_id is not null and challan_date >= beg_marker.ss and challan_date <= end_marker.ss "+
			"	group by material_id) mat_ytd_prod_by_challan_till_today ";

	public static String g_ytdProdByTripsTillToday = " (select unload_material_guess material_id, cast(sum(case when load_amt_manual is not null " +
			"	then load_amt_manual else load_amt_guess end) as decimal(10,1)) tot_amt, count(*) trip_count_till_today "+
			"	from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) "+
			"	join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id in (@pv123)) "+
			"	join trip_info on (trip_info.vehicle_id = vehicle.id) "+
			"	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and date(start_time) = date(now())) end_marker "+
			"	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and date(start_time) = (case when month(now()) > 3 then concat(year(now()),'/04/01') else concat(year(now())-1,'/04/01') end)) beg_marker "+
			"	where unload_material_guess is not null and unload_gate_in >= beg_marker.ss "+
			"	group by unload_material_guess) mat_ytd_prod_by_trip_till_today ";
	
	public static String g_mtdProdByChallanTillToday = " (select material_id, cast(sum(wt_by_qty)as decimal(10,1)) tot_mon_actual_till_today, count(*) mon_challan_count_till_today from challan_details join port_nodes leaf on (leaf.id = challan_details.port_node_id) "+
	"	join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id in (@pv123)) "+
	"	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and date(start_time) = date(now())) end_marker "+
	"	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and date(start_time) = (concat(year(now()),'/',month(now()),'/01'))) beg_marker "+
	"	where material_id is not null and challan_date >= beg_marker.ss and challan_date <= end_marker.ss "+
	"	group by material_id) mat_mtd_prod_by_challan_till_today ";

public static String g_mtdProdByTripsTillToday = " (select unload_material_guess material_id, cast(sum(case when load_amt_manual is not null " +
	"	then load_amt_manual else load_amt_guess end) as decimal(10,1)) tot_mon_amt, count(*) trip_count_mon_till_today "+
	"	from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) "+
	"	join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id in (@pv123)) "+
	"	join trip_info on (trip_info.vehicle_id = vehicle.id) "+
	"	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and date(start_time) = date(now())) end_marker "+
	"	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and date(start_time) = (concat(year(now()),'/',month(now()),'/01'))) beg_marker "+
	"	where unload_material_guess is not null and unload_gate_in >= beg_marker.ss "+
	"	group by unload_material_guess) mat_mtd_prod_by_trip_till_today ";

	public static String g_prodByChallanToday = " (select material_id, cast(sum(wt_by_qty) as decimal(10,1)) today_challan_qty, cast(sum(wt_by_qty * (case when challan_details.challan_date >= shift_marker.ss then 1 else 0 end)) as decimal(10,1)) curr_shift_challan_qty, " +
			"	count(*) today_challan_count ,sum(1 * (case when challan_details.challan_date >= shift_marker.ss then 1 else 0 end)) shift_challan_count "+ 
			"	from challan_details join port_nodes leaf on (leaf.id = challan_details.port_node_id) "+
			"	join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id in (@pv123)) "+
			"	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and date(start_time) = date(now())) today_marker "+
			"	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and now() between start_time and end_time) shift_marker "+
			"	where material_id is not null and challan_date >= today_marker.ss "+
			"	group by material_id) mat_prod_by_challan_today ";

	public static String g_prodByTripToday = " (select unload_material_guess material_id, cast(sum((case when load_amt_manual is not null then load_amt_manual else load_amt_guess end)) as decimal(10,1)) " +
			"	today_trip_qty, cast(sum((case when load_amt_manual is not null then load_amt_manual else load_amt_guess end) * (case when trip_info.unload_gate_in >= shift_marker.ss then 1 else 0 end)) as decimal(10,1)) curr_shift_trip_qty "+
			" 	, count(*) today_trip_count, sum(1 * (case when trip_info.unload_gate_in >= shift_marker.ss then 1 else 0 end)) curr_shift_trip_count "+
			" 	from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) "+
			" 	join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id  in (@pv123)) "+
			" 	join trip_info on (trip_info.vehicle_id = vehicle.id) "+
			" 	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and date(start_time) = date(now())) today_marker "+
			" 	cross join (select min(start_time) ss from shift_table where port_node_id in (@pv123) and now() between start_time and end_time) shift_marker "+
			" 	where unload_material_guess is not null and trip_info.unload_gate_in >= today_marker.ss "+
			" 	group by unload_material_guess) mat_prod_by_trip_today ";

	static {
		gMatTableList.put("material_list",g_material_list);
		gMatTableList.put("mat_ytd_plan_till_today",g_ytdPlanTillToday);
		gMatTableList.put("mat_plan_today",g_planToday);
		gMatTableList.put("mat_ytd_prod_by_challan_till_today",g_ytdProdByChallanTillToday);
		gMatTableList.put("mat_ytd_prod_by_trip_till_today",g_ytdProdByTripsTillToday);
		gMatTableList.put("mat_prod_by_challan_today",g_prodByChallanToday);
		gMatTableList.put("mat_prod_by_trip_today",g_prodByTripToday);
		gMatTableList.put("mat_mtd_prod_by_challan_till_today",g_mtdProdByChallanTillToday);
		gMatTableList.put("mat_mtd_prod_by_trip_till_today",g_mtdProdByTripsTillToday);
	}
	// public static String g_tripPlusChallan = 
	;
	public static String g_workStationSummary = " (select temp_info.work_station_name, temp_info.work_station_id, temp_info.dest_type work_station_type, sum(case when (trip_status in (4,5,6) and dest_in_act_time > date(now())) then 1 else 0 end) wt_today_count "+
			" , sum(case when (trip_status in (4,5,6) and dest_out_act_time > adddate(date(now()),-1) and dest_out_act_time <= adddate(date(now()),0)) then 1 else 0 end) wt_yesterday_count "+
			" , sum(case when (trip_status in (4,5,6) and dest_out_act_time > adddate(date(now()),-2) and dest_out_act_time <= adddate(date(now()),-1)) then 1 else 0 end) wt_day_before_yesterday_count "+
			" , sum(case when (trip_status in (3) and dest_in_act_time is not null and dest_out_act_time is null) then 1 else 0 end) av_today_count "+
			" , sum(case when (trip_status in (2,3) and dest_in_eta_time <= adddate(date(now()),1) and dest_in_eta_time > adddate(date(now()),0)) then 1 else 0 end) av_tomorrow_count "+
			" , sum(case when (trip_status in (2,3) and dest_in_eta_time <= adddate(date(now()),2) and dest_in_eta_time > adddate(date(now()),1)) then 1 else 0 end) av_day_after_tomorrow_count "+
			" from ( select current_vehicle_trip_dest_info.*,(case when dest_type = 0 then op_station.name else (case when dest_type = 1 then regions.short_code else (case when dest_type = 2 then landmarks.name else ' ' end) end) end) work_station_name, (case when dest_type = 0 then op_station.id else (case when dest_type = 1 then regions.id else (case when dest_type = 2 then landmarks.id else ' ' end) end) end) work_station_id from current_vehicle_trip_dest_info "+
			" join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc on (anc.id in (27) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = current_vehicle_trip_dest_info.vehicle_id "+ 
			" left outer join op_station on (op_station.id = current_vehicle_trip_dest_info.dest_id) "+
			" left outer join regions on (regions.id = current_vehicle_trip_dest_info.dest_region_id) "+
			" left outer join landmarks on (landmarks.id = current_vehicle_trip_dest_info.dest_landmark_id)) temp_info "+
			" group by temp_info.work_station_name) workstationSummary ";
	
	 public static String opOrgList = " (select op_station.id, op_station.name from op_station join opstation_mapping " +
	 			" on (op_station.id = opstation_mapping.op_station_id and type in (2,11)) join port_nodes leaf on " +
	 			" (leaf.id = opstation_mapping.port_node_id) join port_nodes anc on (anc.id = @pv123 and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)) " +
	 			" org_opstation ";
	 
    public static String g_summary4totalvehicleavailable = " ( select  count(*) count,trip_info.unload_gate_op op_id, op_station.name op_name from trip_info   join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = trip_info.vehicle_id   join op_station on (op_station.id = trip_info.unload_gate_op)  where unload_gate_in is not null and unload_gate_out is null and DATE_ADD(unload_gate_in, INTERVAL (op_station.int_val6/1000) second) > adddate(now(),-3) and !(movement_type like '%Trip Complete%')   group by trip_info.unload_gate_op )opStat_summary4_total_vehicle_available ";
    
//  public static String g_summary5vehicleavailable = "( " +
//              " select date(trip_info.unload_gate_in), count(*) count,trip_info.unload_gate_op op_id,op_station.name from trip_info  " +
//              " join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+"  ) vi on vi.vehicle_id = trip_info.vehicle_id  " +
//              " join op_station on (op_station.id = trip_info.unload_gate_op) " +
//              " where unload_gate_in is not null and unload_gate_out is null and DATE_ADD(unload_gate_in, INTERVAL op_station.int_val6 second) > adddate(now(),-3) and !(movement_type like '%Trip Complete%')  " +
////            " -- and trip_info.unload_gate_op = 123 and date(trip_info.unload_gate_in) = '2013-07-14' " +
//              " group by trip_info.unload_gate_op,date(trip_info.unload_gate_in)" +
//              " ) summary5_vehicle_available";
    
//  public static String g_listtotalavailablevehicle = "( " +
//              " select vehicle.name,trip_info.unload_gate_op op_id,op_station.name from trip_info  " +
//              " join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+"  ) vi on vi.vehicle_id = trip_info.vehicle_id  " +
//              " join vehicle on vehicle.id = vi.vehicle_id " +
//              " join op_station on (op_station.id = trip_info.unload_gate_op) " +
//              " where unload_gate_in is not null and unload_gate_out is null and DATE_ADD(unload_gate_in, INTERVAL op_station.int_val6 second) > adddate(now(),-3) and !(movement_type like '%Trip Complete%') " + 
//              "  and trip_info.unload_gate_op = @op_station_id " +
////            " -- group by trip_info.unload_gate_op " +
//              " ) list_total_available_vehicle";
    
    public static String g_listavailablevehicle = "( " +
                " select vehicle.id vehicle_id,vehicle.name vehicle_name,vehicle.type vehicle_type,date(trip_info.unload_gate_in) process_day,port_nodes.name customer_name,trip_info.unload_gate_op op_id,op_station.name from trip_info  " +
                " join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+"  ) vi on vi.vehicle_id = trip_info.vehicle_id  " +
                " join vehicle on vehicle.id = vi.vehicle_id " +
                " join op_station on (op_station.id = trip_info.unload_gate_op) " +
                " left outer join port_nodes on (vehicle.customer_id = port_nodes.id) "+
                " where unload_gate_in is not null and unload_gate_out is null and DATE_ADD(unload_gate_in, INTERVAL (op_station.int_val6/1000) second) > adddate(now(),-3) and !(movement_type like '%Trip Complete%') " + 
               // "  and trip_info.unload_gate_op = @op_station_id  and date(trip_info.unload_gate_in) = @unload_date" +
//  " -- group by trip_info.unload_gate_op " +
                " ) opStat_list_available_vehicle";
    
    public static String g_summary6vehicleavailablenow = " ( select  count(*) count,trip_info.unload_gate_op op_id, op_station.name op_name from trip_info   join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = trip_info.vehicle_id   join op_station on (op_station.id = trip_info.unload_gate_op)  where unload_gate_in is not null and unload_gate_out is null and DATE_ADD(unload_gate_in, INTERVAL (op_station.int_val6/1000) second) > adddate(now(),0) and  !(movement_type like '%Trip Complete%')   group by trip_info.unload_gate_op )opStat_summary6_vehicle_available_now ";

    public static String g_summary7vehicleavailablesinceyesterday = " (select  count(*) count,trip_info.unload_gate_op op_id, op_station.name op_name from trip_info   join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = trip_info.vehicle_id   join op_station on (op_station.id = trip_info.unload_gate_op)  where unload_gate_in is not null and unload_gate_out is null and DATE_ADD(unload_gate_in, INTERVAL (op_station.int_val6/1000) second) > adddate(now(),-1) and DATE_ADD(unload_gate_in, INTERVAL (op_station.int_val6/1000) second) < adddate(now(),0) and !(movement_type like '%Trip Complete%')   group by trip_info.unload_gate_op) opStat_summary7_vehicle_available_since_yesterday ";
    
    public static String g_summary8vehicleavailablesincedaybeforeyesterday = " (select  count(*) count,trip_info.unload_gate_op op_id, op_station.name op_name from trip_info   join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = trip_info.vehicle_id   join op_station on (op_station.id = trip_info.unload_gate_op)  where unload_gate_in is not null and unload_gate_out is null and DATE_ADD(unload_gate_in, INTERVAL (op_station.int_val6/1000) second) > adddate(now(),-2) and DATE_ADD(unload_gate_in, INTERVAL (op_station.int_val6/1000) second) < adddate(now(),-1) and !(movement_type like '%Trip Complete%')   group by trip_info.unload_gate_op ) opStat_summary8_vehicle_available_since_day_before_yesterday  ";
    
    public static String g_summary9vehicleavailablesince2daybeforeyesterday = " ( select  count(*) count,trip_info.unload_gate_op op_id, op_station.name op_name from trip_info   join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = trip_info.vehicle_id   join op_station on (op_station.id = trip_info.unload_gate_op)  where unload_gate_in is not null and unload_gate_out is null and DATE_ADD(unload_gate_in, INTERVAL (op_station.int_val6/1000) second) > adddate(now(),-3) and DATE_ADD(unload_gate_in, INTERVAL (op_station.int_val6/1000) second) < adddate(now(),-2) and !(movement_type like '%Trip Complete%')   group by trip_info.unload_gate_op ) opStat_summary9_vehicle_available_since_2_day_before_yesterday ";
    
    public static String g_etabasedavailablevehicletomorrow = " ( " +
                " select count(*) count, src_dest_items.dest_op_station_id op_id, op_station.name " +
                " from eta_info join src_dest_items on (eta_info.src_dest_item_id = src_dest_items.id) " +
                " join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+"  ) vi on vi.vehicle_id = eta_info.vehicle_id  " +
                " join op_station on (op_station.id = src_dest_items.dest_op_station_id) " + 
                " where eta_info.dest_entry_act is null  " +
                " and eta_info.dest_entry_eta > adddate(date(now()),0) and eta_info.dest_entry_eta <= adddate(date(now()),1)" +
                ") opStat_eta_based_available_vehicle_tomorrow ";
    
    public static String g_etabasedavailablevehicledayaftertomorrow = " ( " +
                " select count(*) count, src_dest_items.dest_op_station_id op_id, op_station.name " +
                " from eta_info join src_dest_items on (eta_info.src_dest_item_id = src_dest_items.id) " +
                " join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+"  ) vi on vi.vehicle_id = eta_info.vehicle_id  " +
                " join op_station on (op_station.id = src_dest_items.dest_op_station_id) " + 
                " where eta_info.dest_entry_act is null  " +
                " and eta_info.dest_entry_eta > adddate(date(now()),1) and eta_info.dest_entry_eta <= adddate(date(now()),2)" +
                ") opStat_eta_based_available_vehicle_day_after_tomorrow ";
     
    public static String g_etabasedavailablevehicle2dayaftertomorrow = " ( " +
                " select count(*) count, src_dest_items.dest_op_station_id op_id, op_station.name " +
                " from eta_info join src_dest_items on (eta_info.src_dest_item_id = src_dest_items.id) " +
                " join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+"  ) vi on vi.vehicle_id = eta_info.vehicle_id  " +
                " join op_station on (op_station.id = src_dest_items.dest_op_station_id) " + 
                " where eta_info.dest_entry_act is null  " +
                " and eta_info.dest_entry_eta > adddate(date(now()),2) and eta_info.dest_entry_eta <= adddate(date(now()),3)" +
                ") opStat_eta_based_available_vehicle_2_day_after_tomorrow ";

    static {
    	gOpstationVehicles.put("org_opstation",opOrgList);
    	gOpstationVehicles.put("opStat_summary4_total_vehicle_available",g_summary4totalvehicleavailable);
    	gOpstationVehicles.put("opStat_list_available_vehicle",g_listavailablevehicle);
    	gOpstationVehicles.put("opStat_summary6_vehicle_available_now",g_summary6vehicleavailablenow);
    	gOpstationVehicles.put("opStat_summary7_vehicle_available_since_yesterday",g_summary7vehicleavailablesinceyesterday);
    	gOpstationVehicles.put("opStat_summary8_vehicle_available_since_day_before_yesterday",g_summary8vehicleavailablesincedaybeforeyesterday);
    	gOpstationVehicles.put("opStat_summary9_vehicle_available_since_2_day_before_yesterday",g_summary9vehicleavailablesince2daybeforeyesterday);
    	gOpstationVehicles.put("opStat_eta_based_available_vehicle_tomorrow",g_etabasedavailablevehicletomorrow);
    	gOpstationVehicles.put("opStat_eta_based_available_vehicle_day_after_tomorrow",g_etabasedavailablevehicledayaftertomorrow);
    	gOpstationVehicles.put("opStat_eta_based_available_vehicle_2_day_after_tomorrow",g_etabasedavailablevehicle2dayaftertomorrow);
    	
    }
	;
	
	
	public static String g_currEtaInfo =
			" ( select rec.id, rec.vehicle_id, rec.src_dest_item_id, rec.src_exit, rec.status, rec.cumm_dist_at_src_exit, rec.dest_entry_eta, rec.dest_entry_act, rec.dest_entry_baseline, cds.attribute_value curr_dist "+
					" from eta_info rec join current_data cds on (rec.vehicle_id = cds.vehicle_id and cds.attribute_id=0) "+
					" join (select vehicle_id, max(src_exit) src_exit from eta_info where status in (1,3,4) group by vehicle_id) mx on (mx.vehicle_id = rec.vehicle_id and mx.src_exit = rec.src_exit and rec.status in (1,3,4)) "+
					" ) curr_eta_info ";

	public static String g_summaryPeriodEvent =
		" ( "+ 
				"		  SELECT  temp_period_event.item_id, temp_period_event.vehicle_id   "+
				"       ,temp_period_event.engine_dur, temp_period_event.engine_off_dur" +
//				",temp_period_event.max_stop_location_name" +
				", temp_period_event.stop_dur, temp_period_event.garage_dur, temp_period_event.rest_area_dur, temp_period_event.max_stop_dur, temp_period_event.move_dur "+
				"      ,temp_period_event.rpm_idle_or_off, temp_period_event.rpm_idle_and_on, temp_period_event.rpm_on_dur ,temp_period_event.count_stoppage" +
				"      ,temp_period_event.count_overspeed, temp_period_event.count_disconnect, temp_period_event.count_majstop, temp_period_event.count_haldi_overspeed " +

				" , min(mxstoploc.event_start_time) event_start_time, min(mxstoploc.event_begin_name) event_begin_name "+
//				"		,temp_period_event.minmax_mi, temp_period_event.minmax_mx " +
//				"		,temp_period_event.minstopstart   ,temp_period_event.maxstopstart  " +     
				
				//					"      ,data_ee1.event_begin_name"+
				//" , temp_period_event.percentage_util "+

	"       , (case when sop > pen or sop < pst then null "+
	"                    when sop > '@user_end' then null "+
	"                     else  sop end) start_op_time "+
	"       , (case when eop <= sop then least(pen, '@user_end') "+
	"                    when eop < pst then null "+
	"                    when sop > '@user_end' then null "+		
	"                     else  eop end) end_op_time "+
	", move_dur/(move_dur+stop_dur)*100 percentage_util "+
	" ,move_dur/(move_dur+stop_dur_excl_lu)*100 moveadj_percentage_util "+
	", work_area_stop_count "+
	//For apmed
	", assigned_work_area_dur, outside_assigned_work_area_dur "+
	", center_stop_count, center_sop "+
	", center_stop_count_1, center_stop_count_2, center_stop_count_3, center_stop_count_4, center_stop_count_5 "+

	", (case when (move_dur+rest_area_dur) > (move_dur+stop_dur) then 1.0 else (move_dur+rest_area_dur) end)/(move_dur+stop_dur)*100 adj_percentage_util "+
	" from "+
	"      (select call_ee.id as item_id, vi.vehicle_id vehicle_id "+
	"       ,call_ee.start_time pst, call_ee.end_time pen " +
	
	"		,minmax_rec_time.mi minmax_mi, minmax_rec_time.mx minmax_mx " +
	"		,min((case when data_ee.rule_id=1 then data_ee.event_start_time else null end)) minstopstart    " +     
	"		,max((case when data_ee.rule_id=1 then data_ee.event_start_time else null end)) maxstopstart "+
	
	"       ,sum((case when data_ee.rule_id=5 then 1 else 0 end)) work_area_stop_count "+
	"       ,min((case when data_ee.rule_id=4 then data_ee.event_stop_time else null end)) sop "+
	"       ,max((case when data_ee.rule_id=4 then data_ee.event_start_time else null end)) eop  "+

	//for apmed
	"		 ,sum((case when data_ee.rule_id=327 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as assigned_work_area_dur "+
	"		 ,(case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(second, GREATEST(call_ee.start_time,minmax_rec_time.mi,'@user_start'), LEAST(call_ee.end_time,minmax_rec_time.mx,'@user_end')" +
	"		)/60.0 - sum((case when data_ee.rule_id=327 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) )) end) as outside_assigned_work_area_dur " +
	"       ,sum((case when data_ee.rule_id=328 then 1 else 0 end)) center_stop_count "+
	"       ,min((case when data_ee.rule_id=328 then data_ee.event_start_time else null end)) center_sop "+
	"       ,sum((case when data_ee.rule_id=328 and regions.region_type=21 then 1 else 0 end)) center_stop_count_1 "+
	"       ,sum((case when data_ee.rule_id=328 and regions.region_type=22  then 1 else 0 end)) center_stop_count_2 "+
	"       ,sum((case when data_ee.rule_id=328 and regions.region_type=23  then 1 else 0 end)) center_stop_count_3 "+
	"       ,sum((case when data_ee.rule_id=328 and regions.region_type=24  then 1 else 0 end)) center_stop_count_4 "+
	"       ,sum((case when data_ee.rule_id=328 and regions.region_type=25  then 1 else 0 end)) center_stop_count_5 "+

	"		 ,sum((case when data_ee.rule_id=227 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as rpm_idle_or_off "+
	"		 ,sum((case when data_ee.rule_id=243 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as rpm_idle_and_on "+
	"		 ,(case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(second, GREATEST(call_ee.start_time,minmax_rec_time.mi,'@user_start'), LEAST(call_ee.end_time,minmax_rec_time.mx,'@user_end') " +
	"		)/60.0 - sum((case when data_ee.rule_id=227 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) )) end) as rpm_on_dur " +
	// engine_off_dur based on (dur-engine_dur)
	"		,(case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(second, GREATEST(call_ee.start_time,minmax_rec_time.mi,'@user_start'), LEAST(call_ee.end_time,minmax_rec_time.mx,'@user_end')              " +
	"		)/60.0 - sum((case when data_ee.rule_id=56 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(call_ee.start_time,minmax_rec_time.mi,'@user_start'), LEAST(call_ee.end_time,minmax_rec_time.mx,'@user_end')) else 0 end) )) end) as engine_off_dur"+
	"		 ,sum((case when data_ee.rule_id=56 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(call_ee.start_time,minmax_rec_time.mi,'@user_start'), LEAST(call_ee.end_time,minmax_rec_time.mx,'@user_end')) else null end) ) as engine_dur "+
//	"		 ,sum((case when data_ee.rule_id=56 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as engine_dur "+
	"		 ,sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as stop_dur "+
	"		 ,sum(case when data_ee.rule_id=1 and  (data_ee.event_start_time between trip_info.load_area_wait_in and trip_info.load_area_wait_out or data_ee.event_start_time between trip_info.unload_area_wait_in and trip_info.unload_area_wait_out "+
	" or (data_ee.event_start_time >= trip_info.load_area_wait_in and trip_info.load_area_wait_out is null) or(data_ee.event_start_time >= trip_info.unload_area_wait_in and trip_info.unload_area_wait_out is null)  "+
	" ) then 0 when data_ee.rule_id=1 then "+
	" GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) as stop_dur_excl_lu  "+
	"		 ,sum((case when data_ee.rule_id=3 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as garage_dur "+
	"		 ,sum((case when data_ee.rule_id=4 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as rest_area_dur "+
	"		 ,max((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as max_stop_dur " +
//	"	 	 , (case when (data_ee.rule_id=1 and (max((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'2012-05-09 06:00:00'), LEAST(minmax_rec_time.mx,'2012-05-10 09:59:00')) else null end) )= (GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'2012-05-09 06:00:00'), LEAST(minmax_rec_time.mx,'2012-05-10 09:59:00')))))then data_ee.event_begin_name else null end ) as max_stop_location_name "+		
	"		 ,(case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(second, GREATEST(call_ee.start_time,minmax_rec_time.mi,'@user_start'), LEAST(call_ee.end_time,minmax_rec_time.mx,'@user_end') " +
	//		"		(case when call_ee.start_time < minmax_rec_time.mi then minmax_rec_time.mi else call_ee.start_time end), " +
	//		"		(case when call_ee.end_time > minmax_rec_time.mx then minmax_rec_time.mx else call_ee.end_time end)" +
	"		)/60.0 - sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) )) end) as move_dur" +
	//rajeev percentage_util moved_to_top		" 		, (((case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(second, GREATEST(call_ee.start_time,minmax_rec_time.mi,'@user_start'), LEAST(call_ee.end_time,minmax_rec_time.mx,'@user_end') )/60.0 - sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) )) end) )/ Timestampdiff(second,Greatest(call_ee.start_time,'@user_start'),Least(call_ee.end_time, '@user_end') )/60.0)*100 percentage_util	"+
	"       ,sum((case when data_ee.rule_id=1 then 1 else null end)) count_stoppage "+
	"       ,sum((case when data_ee.rule_id=319 then 1 else null end)) count_overspeed "+
	"       ,sum((case when data_ee.rule_id=130 then 1 else null end)) count_disconnect "+
	"       ,sum((case when data_ee.rule_id=538 then 1 else null end)) count_majstop "+
	"       ,sum((case when data_ee.rule_id=422 then 1 else null end)) count_haldi_overspeed "+
	"		 from  "+
	"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
	"		    join minmax_rec_time on (minmax_rec_time.vehicle_id = vi.vehicle_id) "+
	"		    join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
	"		    left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (@data_ee_list) " +
	"			and " +
	"			((data_ee.event_start_time <= '@user_end') and " +
	"			(data_ee.event_stop_time is null or (data_ee.event_stop_time >= '@user_start'  ) ))"+
	"		        and (data_ee.event_start_time <= call_ee.end_time) "+
	"				and (data_ee.event_stop_time is null or data_ee.event_stop_time >= call_ee.start_time) "+ 
//TODO - rajeev below is buggy - above is correct - but needs to be applied to other queries	
//	"			((data_ee.event_start_time >= '@user_start' and data_ee.event_start_time <= '@user_end') or " +
//	"			(data_ee.event_stop_time is null or (data_ee.event_stop_time >= '@user_start' and data_ee.event_stop_time <= '@user_end'  ) ))"+
//	"		        and (data_ee.event_start_time <= call_ee.end_time) "+
//	"				and (data_ee.event_stop_time is null or data_ee.event_stop_time >= call_ee.start_time) "+ 
	"				) "+		
	"           left outer join regions on (regions.id = data_ee.ref_region_id) "+
	"           left outer join trip_info on (data_ee.vehicle_id = trip_info.vehicle_id and data_ee.event_start_time between trip_info.combo_start and trip_info.combo_end) "+
	" where " +
	" vi.vehicle_id in (@VEHICLE_ID_LIST) "+
	" and data_ee.rule_id in (@data_ee_list) "+
	"		  group by call_ee.id, vi.vehicle_id, call_ee.start_time, call_ee.end_time  , minmax_rec_time.mi, minmax_rec_time.mx  "+
	" ) temp_period_event " +
	"	left outer join engine_events mxstoploc on (mxstoploc.vehicle_id = temp_period_event.vehicle_id and mxstoploc.rule_id=1 and mxstoploc.event_start_time >= temp_period_event.minstopstart and mxstoploc.event_start_time <= temp_period_event.maxstopstart and "+ 
	"	 temp_period_event.max_stop_dur = GET_DURATION(mxstoploc.event_start_time,mxstoploc.event_stop_time,temp_period_event.pst, temp_period_event.pen, GREATEST(minmax_mi,'@user_start'), LEAST(minmax_mx,'@user_end') "+ 
	"	 )) "+ 
	"	 group by temp_period_event.item_id, temp_period_event.vehicle_id, temp_period_event.max_stop_dur "+ 
	"		) summary_period_event "
	;
	public static String g_summary24hrdist = 
		" (select lgd.vehicle_id, lgd.attribute_value from "+
		"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
		" join (select vehicle_id, min(gps_record_time) grt from logged_data where gps_record_time >= DATE_ADD(now(), INTERVAL -60*24 MINUTE) and attribute_id=0 group by vehicle_id) mx "+
		" on (mx.vehicle_id = vi.vehicle_id) "+
		" join logged_data lgd " +
		" on (mx.vehicle_id = lgd.vehicle_id and lgd.attribute_id=0 and mx.grt = lgd.gps_record_time)  "+
		" ) summary_24hr_logged"
		;

	public static String g_summary24hrEvent =
		" ( "+ 
				"		  SELECT  temp_period_event.item_id, temp_period_event.vehicle_id   "+
				"       ,temp_period_event.engine_dur, temp_period_event.engine_off_dur" +
//				",temp_period_event.max_stop_location_name" +
				", temp_period_event.stop_dur, temp_period_event.garage_dur, temp_period_event.rest_area_dur, temp_period_event.max_stop_dur, temp_period_event.move_dur "+
				"      ,temp_period_event.rpm_idle_or_off, temp_period_event.rpm_idle_and_on, temp_period_event.rpm_on_dur ,temp_period_event.count_stoppage" +
				"      ,temp_period_event.count_overspeed, temp_period_event.count_disconnect, temp_period_event.count_majstop " +

				" , min(mxstoploc.event_start_time) event_start_time, min(mxstoploc.event_begin_name) event_begin_name "+
//				"		,temp_period_event.minmax_mi, temp_period_event.minmax_mx " +
//				"		,temp_period_event.minstopstart   ,temp_period_event.maxstopstart  " +     
				
				//					"      ,data_ee1.event_begin_name"+
				//" , temp_period_event.percentage_util "+

	" , move_dur/(move_dur+stop_dur)*100 percentage_util "+
	" , (case when (move_dur+rest_area_dur) > (move_dur+stop_dur) then 1.0 else (move_dur+rest_area_dur) end)/(move_dur+stop_dur)*100 adj_percentage_util "+
	" from "+
	"      (select call_ee.id as item_id, vi.vehicle_id vehicle_id "+
	"       ,call_ee.start_time pst, call_ee.end_time pen " +
	
	"		,minmax_rec_time.mi minmax_mi, minmax_rec_time.mx minmax_mx " +
	"		,min((case when data_ee.rule_id=1 then data_ee.event_start_time else null end)) minstopstart    " +     
	"		,max((case when data_ee.rule_id=1 then data_ee.event_start_time else null end)) maxstopstart "+
	
	
	"		 ,sum((case when data_ee.rule_id=227 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as rpm_idle_or_off "+
	"		 ,sum((case when data_ee.rule_id=243 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as rpm_idle_and_on "+
	"		 ,(case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(second, GREATEST(call_ee.start_time,minmax_rec_time.mi,call_ee.start_time), LEAST(call_ee.end_time,minmax_rec_time.mx,call_ee.end_time) " +
	"		)/60.0 - sum((case when data_ee.rule_id=227 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) )) end) as rpm_on_dur " +
	// engine_off_dur based on (dur-engine_dur)
	"		,(case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(second, GREATEST(call_ee.start_time,minmax_rec_time.mi,call_ee.start_time), LEAST(call_ee.end_time,minmax_rec_time.mx,call_ee.end_time)              " +
	"		)/60.0 - sum((case when data_ee.rule_id=56 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(call_ee.start_time,minmax_rec_time.mi,call_ee.start_time), LEAST(call_ee.end_time,minmax_rec_time.mx,call_ee.end_time)) else 0 end) )) end) as engine_off_dur"+
	"		 ,sum((case when data_ee.rule_id=56 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(call_ee.start_time,minmax_rec_time.mi,call_ee.start_time), LEAST(call_ee.end_time,minmax_rec_time.mx,call_ee.end_time)) else null end) ) as engine_dur "+
//	"		 ,sum((case when data_ee.rule_id=56 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as engine_dur "+
	"		 ,sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as stop_dur "+
	"		 ,sum((case when data_ee.rule_id=3 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as garage_dur "+
	"		 ,sum((case when data_ee.rule_id=4 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as rest_area_dur "+
	"		 ,max((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as max_stop_dur " +
//	"	 	 , (case when (data_ee.rule_id=1 and (max((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'2012-05-09 06:00:00'), LEAST(minmax_rec_time.mx,'2012-05-10 09:59:00')) else null end) )= (GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'2012-05-09 06:00:00'), LEAST(minmax_rec_time.mx,'2012-05-10 09:59:00')))))then data_ee.event_begin_name else null end ) as max_stop_location_name "+		
	"		 ,(case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(second, GREATEST(call_ee.start_time,minmax_rec_time.mi,call_ee.start_time), LEAST(call_ee.end_time,minmax_rec_time.mx,call_ee.end_time) " +
	//		"		(case when call_ee.start_time < minmax_rec_time.mi then minmax_rec_time.mi else call_ee.start_time end), " +
	//		"		(case when call_ee.end_time > minmax_rec_time.mx then minmax_rec_time.mx else call_ee.end_time end)" +
	"		)/60.0 - sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) )) end) as move_dur" +
	//rajeev percentage_util moved_to_top		" 		, (((case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(second, GREATEST(call_ee.start_time,minmax_rec_time.mi,call_ee.start_time), LEAST(call_ee.end_time,minmax_rec_time.mx,call_ee.end_time) )/60.0 - sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) )) end) )/ Timestampdiff(second,Greatest(call_ee.start_time,call_ee.start_time),Least(call_ee.end_time, call_ee.end_time) )/60.0)*100 percentage_util	"+
	"       ,sum((case when data_ee.rule_id=1 then 1 else 0 end)) count_stoppage "+
	"       ,sum((case when data_ee.rule_id=319 then 1 else 0 end)) count_overspeed "+
	"       ,sum((case when data_ee.rule_id=130 then 1 else 0 end)) count_disconnect "+
	"       ,sum((case when data_ee.rule_id=538 then 1 else 0 end)) count_majstop "+
	"		 from  "+
	"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
	"		    join minmax_rec_time on (minmax_rec_time.vehicle_id = vi.vehicle_id) "+
	"		   cross  join (select 1 id, DATE_ADD(now(), INTERVAL -60*24 MINUTE) start_time, now() end_time) call_ee  "+
	"		    left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (@data_ee_list) " +
	"			and " +
	"			((data_ee.event_start_time <= call_ee.end_time) and " +
	"			(data_ee.event_stop_time is null or (data_ee.event_stop_time >= call_ee.start_time  ) ))"+
	"		        and (data_ee.event_start_time <= call_ee.end_time) "+
	"				and (data_ee.event_stop_time is null or data_ee.event_stop_time >= call_ee.start_time) "+ 
	"				) "+		
	"           left outer join regions on (regions.id = data_ee.ref_region_id) "+
	"		  group by call_ee.id, vi.vehicle_id, call_ee.start_time, call_ee.end_time  , minmax_rec_time.mi, minmax_rec_time.mx  "+
	" ) temp_period_event " +

	"	left outer join engine_events mxstoploc on (mxstoploc.vehicle_id = temp_period_event.vehicle_id and mxstoploc.rule_id=1 and mxstoploc.event_start_time >= temp_period_event.minstopstart and mxstoploc.event_start_time <= temp_period_event.maxstopstart and "+ 
	"	 temp_period_event.max_stop_dur = GET_DURATION(mxstoploc.event_start_time,mxstoploc.event_stop_time,temp_period_event.pst, temp_period_event.pen, GREATEST(minmax_mi,temp_period_event.pst), LEAST(minmax_mx,temp_period_event.pen) "+ 
	"	 )) "+ 
	"	 group by temp_period_event.item_id, temp_period_event.vehicle_id, temp_period_event.max_stop_dur "+ 
	"		) summary_24hr_event "
	;
	//Lafarge events 319,320,485,486,487,488,489,490
	public static String g_summaryPeriodSafetyEvent =
		" ( "+
		"		  SELECT  call_ee.id as item_id, vi.vehicle_id as vehicle_id "+
		"		 ,sum(case when data_ee.rule_id=319 then 1 else 0 end) as c319 "+
		"		 ,sum(case when data_ee.rule_id=320 then 1 else 0 end) as c320 "+
		"		 ,sum(case when data_ee.rule_id=485 then 1 else 0 end) as c485 "+
		"		 ,sum(case when data_ee.rule_id=486 then 1 else 0 end) as c486 "+
		"		 ,sum(case when data_ee.rule_id=487 then 1 else 0 end) as c487 "+
		"		 ,sum(case when data_ee.rule_id=488 then 1 else 0 end) as c488 "+
		"		 ,sum(case when data_ee.rule_id=489 then 1 else 0 end) as c489 "+
		"		 ,sum(case when data_ee.rule_id=490 then 1 else 0 end) as c490 "+
		"       ,sum(case when data_ee.rule_id in (319,320,485,486,487,488,489,490) then 1 else 0 end) as c_safe_tot "+
 
	"		 from  "+
	"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
	"		    join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
	"		    left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (319,320,485,486,487,488,489,490) " +
	"			and " +
	"			((data_ee.event_start_time >= '@user_start' and data_ee.event_start_time <= '@user_end') or " +
	"			(data_ee.event_stop_time is null or (data_ee.event_stop_time >= '@user_start' and data_ee.event_stop_time <= '@user_end'  ) ))"+
	"		        and (data_ee.event_start_time <= call_ee.end_time) "+
	"				and (data_ee.event_stop_time is null or data_ee.event_stop_time >= call_ee.start_time) "+ 
	"				) "+		
	"		  group by call_ee.id, vi.vehicle_id  "+
	" ) summary_period_safety_event " 
	;

	public static String g_summaryPeriodSafetyEventMTDRollup =
		" ( "+
		"		  SELECT  call_ee.id as item_id, vi.vehicle_id as vehicle_id "+
		"		 ,sum(case when data_ee.rule_id=319 then 1 else 0 end) as c319 "+
		"		 ,sum(case when data_ee.rule_id=320 then 1 else 0 end) as c320 "+
		"		 ,sum(case when data_ee.rule_id=485 then 1 else 0 end) as c485 "+
		"		 ,sum(case when data_ee.rule_id=486 then 1 else 0 end) as c486 "+
		"		 ,sum(case when data_ee.rule_id=487 then 1 else 0 end) as c487 "+
		"		 ,sum(case when data_ee.rule_id=488 then 1 else 0 end) as c488 "+
		"		 ,sum(case when data_ee.rule_id=489 then 1 else 0 end) as c489 "+
		"		 ,sum(case when data_ee.rule_id=490 then 1 else 0 end) as c490 "+
		"       ,sum(case when data_ee.rule_id in (319,320,485,486,487,488,489,490) then 1 else 0 end) as c_safe_tot "+
		"       from "+
		"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
		"		    join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
		"         join month_table on (month_table.start_time <= call_ee.start_time and month_table.end_time >= call_ee.start_time) "+
		"		    left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (319,320,485,486,487,488,489,490) " +
		"			and " +
		"			((data_ee.event_start_time >= month_table.start_time and data_ee.event_start_time <= '@user_end') or " +
		"			(data_ee.event_stop_time is null or (data_ee.event_stop_time >=month_table.start_time and data_ee.event_stop_time <= '@user_end'  ) ))"+
		"		        and (data_ee.event_start_time <= call_ee.end_time) "+
		"				and (data_ee.event_stop_time is null or data_ee.event_stop_time >= month_table.start_time) "+ 
		"				) "+		
		"		  group by call_ee.id, vi.vehicle_id  "+
		" ) summary_period_safety_event_mtd " 
		;

	public static String g_summaryPeriodSafetyEventYTDRollup =
		" ( "+
		"		  SELECT  call_ee.id as item_id, vi.vehicle_id as vehicle_id "+
		"		 ,sum(case when data_ee.rule_id=319 then 1 else 0 end) as c319 "+
		"		 ,sum(case when data_ee.rule_id=320 then 1 else 0 end) as c320 "+
		"		 ,sum(case when data_ee.rule_id=485 then 1 else 0 end) as c485 "+
		"		 ,sum(case when data_ee.rule_id=486 then 1 else 0 end) as c486 "+
		"		 ,sum(case when data_ee.rule_id=487 then 1 else 0 end) as c487 "+
		"		 ,sum(case when data_ee.rule_id=488 then 1 else 0 end) as c488 "+
		"		 ,sum(case when data_ee.rule_id=489 then 1 else 0 end) as c489 "+
		"		 ,sum(case when data_ee.rule_id=490 then 1 else 0 end) as c490 "+
		"       ,sum(case when data_ee.rule_id in (319,320,485,486,487,488,489,490) then 1 else 0 end) as c_safe_tot "+
 
		"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
		"		    join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
		"         join year_table on (year_table.start_time <= call_ee.start_time and year_table.end_time >= call_ee.start_time) "+
		"		    left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (319,320,485,486,487,488,489,490) " +
		"			and " +
		"			((data_ee.event_start_time >= year_table.start_time and data_ee.event_start_time <= '@user_end') or " +
		"			(data_ee.event_stop_time is null or (data_ee.event_stop_time >=year_table.start_time and data_ee.event_stop_time <= '@user_end'  ) ))"+
		"		        and (data_ee.event_start_time <= call_ee.end_time) "+
		"				and (data_ee.event_stop_time is null or data_ee.event_stop_time >= year_table.start_time) "+ 
		"				) "+		
		"		  group by call_ee.id, vi.vehicle_id  "+
		" ) summary_period_safety_event_ytd " 
		;
	public static String g_summaryPeriodTripCount =
		" ( "+
		"		  SELECT  call_ee.id as item_id, vi.vehicle_id as vehicle_id "+
		"		 ,sum(case when load_gate_out between call_ee.start_time and call_ee.end_time then 1 else 0 end) as t_load "+
		"		 ,sum(case when unload_gate_in between call_ee.start_time and call_ee.end_time then 1 else 0 end) as t_unload "+
		"		 ,sum(case when load_gate_out <= call_ee.start_time and (coalesce(unload_gate_in, confirm_time)  is null or coalesce(unload_gate_in, confirm_time) > call_ee.end_time) then 1 else 0 end) as t_onway "+
	"		 from  "+
	"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
	"		    join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
	"		    left outer join trip_info data_ee on (vi.vehicle_id = data_ee.vehicle_id  " +
	"			and " +
	"			((data_ee.combo_start >= '@user_start' and data_ee.combo_start <= '@user_end') or " +
	"			(coalesce(unload_gate_in, confirm_time) is null or (coalesce(unload_gate_in, confirm_time) between '@user_start' and  '@user_end'  ) ))"+
	"		        and (data_ee.combo_start <= call_ee.end_time) "+
	"				and (coalesce(unload_gate_in, confirm_time) is null or coalesce(unload_gate_in, confirm_time) >= call_ee.start_time) "+ 
	"				) "+		
	"		  group by call_ee.id, vi.vehicle_id  "+
	" ) summary_period_trip_count " 
	;

	public static String g_summaryHaldiDashboard =
		" ( "+
		"		  SELECT  milgd.vehicle_id, hdb_event.count_haldi_overspeed, hdb_trip.ucount, hdb_trip.ltl_unload "+
		"        ,(lgden.attribute_value - lgdst.attribute_value) * 1.05 dist "+
		"        , hdb_event.stop_dur "+
		"       ,hdb_event.tot_dur "+
		"       , hdb_event.tot_dur-12*60*ceiling(hdb_event.tot_dur/(24*60))-45*(hdb_trip.ucount+hdb_trip.ltl_unload) adj_tot_dur "+
		" , (case when hdb_event.tot_dur-12*60*ceiling(hdb_event.tot_dur/(24*60))-45*(hdb_trip.ucount+hdb_trip.ltl_unload) < 1 then null else (hdb_event.tot_dur - hdb_event.stop_dur)/(hdb_event.tot_dur-12*60*ceiling(hdb_event.tot_dur/(24*60))-45*(hdb_trip.ucount+hdb_trip.ltl_unload)) end)*100 adj_util "+
	"		 from  "+
	" (select logged_data.vehicle_id, min(logged_data.gps_record_time) grt from "+
	"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
	"          join logged_data on (logged_data.vehicle_id = vi.vehicle_id and logged_data.attribute_id = 0 and logged_data.gps_record_time >= '@user_start') group by logged_data.vehicle_id  "+
	") milgd "+
	" join logged_data lgdst on (lgdst.vehicle_id = milgd.vehicle_id and lgdst.attribute_id=0 and lgdst.gps_record_time = milgd.grt) "+
	" join "+
	" (select logged_data.vehicle_id, max(logged_data.gps_record_time) grt from "+
	"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
	"          join logged_data on (logged_data.vehicle_id = vi.vehicle_id and logged_data.attribute_id = 0 and logged_data.gps_record_time <= '@user_end') group by logged_data.vehicle_id)  mxlgd "+
	" on (mxlgd.vehicle_id = milgd.vehicle_id) "+	
	" join logged_data lgden on (lgden.vehicle_id = mxlgd.vehicle_id and lgden.attribute_id=0 and lgden.gps_record_time = mxlgd.grt) "+
   " left outer join "+
	" ( " + //trip related stuff
	"         select vi.vehicle_id, count(distinct trip_info.unload_gate_in) ucount, count(*) ltl_unload "+
	"         from "+
	"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
	"		    join trip_info on (trip_info.vehicle_id = vi.vehicle_id and trip_info.unload_gate_in between '@user_start' and '@user_end') "+
	"          join trip_info_otherLU on (trip_info_otherLU.trip_id = trip_info.id and trip_info_otherLU.is_load=0) "+
	"         group by vi.vehicle_id "+
	" ) hdb_trip on (hdb_trip.vehicle_id = milgd.vehicle_id) "+
	" left outer join "+
	" (select engine_events.vehicle_id, sum(case when engine_events.rule_id = 422 then 1 else 0 end) count_haldi_overspeed "+
	"		 ,sum((case when engine_events.rule_id=1 then GET_DURATION(engine_events.event_start_time,engine_events.event_stop_time,'@user_start', '@user_end', GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as stop_dur "+
	"      ,timestampdiff(minute, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) tot_dur "+
	" from "+
	"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
	"		    join minmax_rec_time on (minmax_rec_time.vehicle_id = vi.vehicle_id) "+
	"        join engine_events on (engine_events.vehicle_id = vi.vehicle_id and engine_events.rule_id in (1,422)) "+
	"       where engine_events.event_start_time < '@user_end' and (engine_events.event_stop_time is null or engine_events.event_stop_time >= '@user_start') "+
	"       group by engine_events.vehicle_id,timestampdiff(minute, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) "+
	") hdb_event on (hdb_event.vehicle_id = milgd.vehicle_id) "+
	") summary_haldi_db "
	
	 
	;
	
	
	public static String g_summaryPeriodTripCountMTDRollup =
		" ( "+
		"		  SELECT  call_ee.id as item_id, vi.vehicle_id as vehicle_id "+
		"		 ,sum(case when load_gate_out between month_table.start_time and call_ee.end_time then 1 else 0 end) as t_load "+
		"		 ,sum(case when unload_gate_in between month_table.start_time and call_ee.end_time then 1 else 0 end) as t_unload "+
		"		 ,sum(case when load_gate_out <= month_table.start_time and (coalesce(unload_gate_in, confirm_time)  is null or coalesce(unload_gate_in, confirm_time) > call_ee.end_time) then 1 else 0 end) as t_onway "+
	"		 from  "+
	"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
	"		    join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
	"         join month_table on (month_table.start_time <= call_ee.start_time and month_table.end_time >= call_ee.start_time) "+
	"		    left outer join trip_info data_ee on (vi.vehicle_id = data_ee.vehicle_id  " +
	"			and " +
	"			((data_ee.combo_start >= '@user_start' and data_ee.combo_start <= '@user_end') or " +
	"			(coalesce(unload_gate_in, confirm_time) is null or (coalesce(unload_gate_in, confirm_time) between '@user_start' and  '@user_end'  ) ))"+
	"		        and (data_ee.combo_start <= call_ee.end_time) "+
	"				and (coalesce(unload_gate_in, confirm_time) is null or coalesce(unload_gate_in, confirm_time) >= month_table.start_time) "+ 
	"				) "+		
	"		  group by call_ee.id, vi.vehicle_id  "+
	" ) summary_period_trip_count_mtd " 
	;

	public static String g_summaryPeriodMTDEvent =
			" ( "+ 
					"		  SELECT  temp_period_event.item_id, temp_period_event.vehicle_id   "+
					"       ,temp_period_event.engine_dur, temp_period_event.stop_dur, temp_period_event.garage_dur, temp_period_event.rest_area_dur, temp_period_event.max_stop_dur, temp_period_event.move_dur "+
					//" , temp_period_event.percentage_util "+
					"       , (case when sop > pen then null "+
					"                    when sop > '@user_end' then null "+
					"                     else  sop end) start_op_time "+
					"       , (case when eop <= sop then least(pen, '@user_end') "+
					"                    when sop > pen then null "+
					"                    when sop > '@user_end' then null "+		
					"                     else  eop end) end_op_time "+
					", move_dur/(move_dur+stop_dur)*100 percentage_util "+
					", (case when (move_dur+rest_area_dur) > (move_dur+stop_dur) then 1.0 else( move_dur+rest_area_dur) end)/(move_dur+stop_dur)*100 adj_percentage_util "+
					" from "+
					"      (select call_ee.id as item_id, vi.vehicle_id vehicle_id "+
					"       ,call_ee.start_time pst, call_ee.end_time pen "+
					"       ,min((case when data_ee.rule_id=4 then data_ee.event_stop_time else null end)) sop "+
					"       ,max((case when data_ee.rule_id=4 then data_ee.event_start_time else null end)) eop  "+
					"		 ,sum((case when data_ee.rule_id=56 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as engine_dur "+
					"		 ,sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as stop_dur "+
					"		 ,sum((case when data_ee.rule_id=3 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as garage_dur "+
					"		 ,sum((case when data_ee.rule_id=4 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as rest_area_dur "+
					"		 ,max((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as max_stop_dur "+		
					"		 ,(case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(minute, GREATEST(call_ee.start_time,minmax_rec_time.mi,call_ee.start_time), LEAST(call_ee.end_time,minmax_rec_time.mx,'@user_end') " +
					//		"		(case when call_ee.start_time < minmax_rec_time.mi then minmax_rec_time.mi else call_ee.start_time end), " +
					//		"		(case when call_ee.end_time > minmax_rec_time.mx then minmax_rec_time.mx else call_ee.end_time end)" +
					"		) - sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,'@user_end')) else null end) )) end) as move_dur" +
					//rajeev percentage_util moved_to_top		" 		, (((case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(minute, GREATEST(call_ee.start_time,minmax_rec_time.mi,call_ee.start_time), LEAST(call_ee.end_time,minmax_rec_time.mx,'@user_end') ) - sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,'@user_end')) else null end) )) end) )/ Timestampdiff(minute,Greatest(call_ee.start_time,call_ee.start_time),Least(call_ee.end_time, '@user_end') ))*100 percentage_util	"+
					"		 from  "+
					"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
					"		    join minmax_rec_time on (minmax_rec_time.vehicle_id = vi.vehicle_id) "+
					"		    join (select month_table.start_time start_time, low_table.id id, low_table.end_time end_time, low_table.label label, low_table.start_time lts from month_table join @period low_table on (month_table.start_time <= low_table.start_time and month_table.end_time > low_table.start_time)) call_ee  on (lts >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
					"		    left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (@data_ee_list) " +
					"			and " +
					"			((data_ee.event_start_time >= call_ee.start_time and data_ee.event_start_time <= '@user_end') or " +
					"			(data_ee.event_stop_time is null or (data_ee.event_stop_time >= call_ee.start_time and data_ee.event_stop_time <= '@user_end'  ) ))"+
					"		        and (data_ee.event_start_time <= call_ee.end_time) "+
					"				and (data_ee.event_stop_time is null or data_ee.event_stop_time >= call_ee.start_time) "+ 
					"				) "+		
					"		  group by call_ee.id, vi.vehicle_id, call_ee.start_time, call_ee.end_time    "+
					" ) temp_period_event "+
					"		) summary_period_mtd_event "
					;

	public static String g_summary3PeriodTripEvent =
		" ( "+
		" select call_ee.id as item_id, vi.vehicle_id vehicle_id  , "+
		" sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.combo_start, "+ 
		" 		call_ee.combo_end, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end'))  "+
		" 		else null end) ) as stop_dur 		 , "+
		" max((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.combo_start, "+
		" 		call_ee.combo_end, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) "+
		" 		else null end) ) as max_stop_dur 		 , "+
		" (case when minmax_rec_time.mx < call_ee.combo_start then null else (Timestampdiff(minute, GREATEST(call_ee.combo_start, "+
		" 		minmax_rec_time.mi,'@user_start'), LEAST(call_ee.combo_end,minmax_rec_time.mx,'@user_end') )  "+
		" 		- sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.combo_start, "+
		" 		call_ee.combo_end, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) "+
		" 		else null end) )) end) as move_dur       , "+
		" count((case when data_ee.rule_id=1 then 1 else null end)) count_stoppage "+
		" ,max(case when data_ee.rule_id = 322 then lgd.speed else null end)  mxspeed "+      
		" ,count( distinct (case when data_ee.rule_id = 322 then data_ee.id else null end)) max_speed_count "+ 
		
		" from  (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi "+ 		   
		" join minmax_rec_time on (minmax_rec_time.vehicle_id = vi.vehicle_id) 		     "+
		" join trip_info call_ee on (call_ee.combo_start >= '@user_start' and call_ee.combo_start <= '@user_end' and vi.vehicle_id = 		call_ee.vehicle_id) "+
		"		    left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (@data_ee_list) " +
		"			and " +
		"			((data_ee.event_start_time >= '@user_start' and data_ee.event_start_time <= '@user_end') or " +
		"			(data_ee.event_stop_time is null or (data_ee.event_stop_time >= '@user_start' and data_ee.event_stop_time <= '@user_end'  ) ))"+
		"		        and (data_ee.event_start_time <= call_ee.combo_end) "+
		"				and (data_ee.event_stop_time is null or data_ee.event_stop_time >= call_ee.combo_start) "+ 
		"				) "+
//		" left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (1,322) "+
//		" 		and ((data_ee.event_start_time >= '2013-05-18 00:00:00' and data_ee.event_start_time <= '2013-05-19 13:47:00') or "+ 
//		" 		(data_ee.event_stop_time is null or (data_ee.event_stop_time >= '2013-05-18 00:00:00'  "+
//		"		and data_ee.event_stop_time <= '2013-05-19 13:47:00'  ) ))  and (data_ee.event_start_time <= call_ee.combo_end) "+
//		" 		and (data_ee.event_stop_time is null or data_ee.event_stop_time >= call_ee.combo_start) 	) "+
		" left outer join logged_data lgd on (vi .vehicle_id = lgd.vehicle_id and data_ee.event_start_time = lgd.gps_record_time) "+		  
		
		" group by call_ee.id, vi.vehicle_id, call_ee.combo_start, call_ee.combo_end  , minmax_rec_time.mi, minmax_rec_time.mx  "+
		" ) summary_3_period_trip_event";
	
	public static String g_summaryPeriodTrip =
			" ( "+
					"		  SELECT  call_ee.id as item_id, vi.vehicle_id vehicle_id "+
					"       ,sum((case when loadop.id is not null then 1 else 0 end)) center_stop_count "+
					"       ,sum((case when loadop.sub_type=21 then 1 else 0 end)) center_stop_count_1 "+
					"       ,sum((case when loadop.sub_type=22 then 1 else 0 end)) center_stop_count_2 "+
					"       ,sum((case when loadop.sub_type=23 then 1 else 0 end)) center_stop_count_3 "+
					"       ,sum((case when loadop.sub_type=24 then 1 else 0 end)) center_stop_count_4 "+
					"       ,sum((case when loadop.sub_type=25 then 1 else 0 end)) center_stop_count_5 "+
					"       ,min((case when data_ee.load_gate_in < call_ee.start_time  then call_ee.start_time else data_ee.load_gate_in end)) center_sop "+
					"		 ,sum(GET_DURATION(data_ee.load_area_wait_in,data_ee.load_gate_out,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end'))) as load_time "+
					"		 ,sum(GET_DURATION(data_ee.unload_area_wait_in,data_ee.unload_gate_out,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end'))) as unload_time "+
					"		 ,sum(GET_DURATION((case when data_ee.load_gate_out is not null and data_ee.unload_area_wait_in is not null then data_ee.load_gate_out else null end), (case when data_ee.load_gate_out is not null and data_ee.unload_area_wait_in is not null then data_ee.unload_area_wait_in else null end),call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end'))) as load_lead_time "+
					"		 ,sum(GET_DURATION((case when data_ee.unload_gate_out is not null and data_ee.confirm_time is not null then data_ee.unload_gate_out else null end),(case when data_ee.unload_gate_out is not null and data_ee.confirm_time is not null then data_ee.confirm_time else null end),call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end'))) as unload_lead_time "+
					"       ,min((case when data_ee.load_gate_out >= call_ee.start_time and data_ee.load_gate_out < call_ee.end_time then data_ee.load_gate_out "+
					"                            when data_ee.unload_gate_out >= call_ee.start_time and data_ee.unload_gate_out < call_ee.end_time then data_ee.unload_gate_out "+
					"                            when data_ee.load_gate_out is null or data_ee.load_gate_out >= call_ee.end_time then null "+
					"                            when data_ee.unload_gate_out is null or data_ee.unload_gate_out >= call_ee.end_time then null "+
					"                            else call_ee.start_time end)) minst "+
					"       ,max(case when data_ee.load_gate_in >= call_ee.start_time and (data_ee.load_gate_out is null or data_ee.load_gate_out > call_ee.end_time) then data_ee.load_gate_in "+
					"                             when data_ee.unload_gate_in >= call_ee.start_time and (data_ee.unload_gate_out is null or data_ee.unload_gate_out > call_ee.end_time) then data_ee.unload_gate_in "+
					"                              else null end) maxen "+
					"       ,sum(case when data_ee.unload_gate_in between call_ee.start_time and call_ee.end_time then 1 else 0 end) ucount "+
					"		 from  "+
					"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
					"		    join minmax_rec_time on (minmax_rec_time.vehicle_id = vi.vehicle_id) "+
					"		    join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
					"		    left outer join trip_info data_ee on (vi.vehicle_id = data_ee.vehicle_id " +
					"			and " +
					"			((data_ee.combo_start >= '@user_start' and data_ee.combo_start <= '@user_end') or " +
					"			(data_ee.confirm_time is null or (data_ee.confirm_time >= '@user_start' and data_ee.confirm_time <= '@user_end'  ) ))"+
					"		        and (data_ee.combo_start <= call_ee.end_time) "+
					"				and (data_ee.confirm_time is null or data_ee.confirm_time >= call_ee.start_time) "+ 
					"				) "+
					"        left outer join op_station loadop on (loadop.id = data_ee.load_gate_op) "+
					"		  group by call_ee.id, vi.vehicle_id    "+
					"		) summary_period_trip " 
					;

	
	public static String g_summaryPeriodMaxTrip =
		" ( "+
		     "select temp_trip.item_id, temp_trip.vehicle_id, ltp.load_gate_in, ltp.load_gate_out, ltp.unload_gate_in, ltp.unload_gate_out, ltp.confirm_time, ltp.load_gate_op, ltp.unload_gate_op, chd.from_location, chd.challan_date, chd.to_location, chd.load_status, lop.name load_name, uop.name unload_name, chd.driver, chd.mrs_contact_details "+
		     " from ("+
				"		  SELECT  call_ee.id as item_id, vi.vehicle_id vehicle_id "+
				" ,max(combo_start) cst "+
				"		 from  "+
				"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
				"		    join minmax_rec_time on (minmax_rec_time.vehicle_id = vi.vehicle_id) "+
				"		    join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
				"		    left outer join trip_info data_ee on (vi.vehicle_id = data_ee.vehicle_id " +
				"			and " +
				"			((data_ee.combo_start >= '@user_start' and data_ee.combo_start <= '@user_end') or " +
				"			(data_ee.confirm_time is null or (data_ee.confirm_time >= '@user_start' and data_ee.confirm_time <= '@user_end'  ) ))"+
				"		        and (data_ee.combo_start <= call_ee.end_time) "+
				"				and (data_ee.confirm_time is null or data_ee.confirm_time >= call_ee.start_time) "+ 
				"				) "+
				"        left outer join op_station loadop on (loadop.id = data_ee.load_gate_op) "+
				"		  group by call_ee.id, vi.vehicle_id    "+
				") temp_trip "+
				" join trip_info ltp on (ltp.vehicle_id = temp_trip.vehicle_id and ltp.combo_start = temp_trip.cst) "+
				" left outer join challan_details chd on (chd.vehicle_id = ltp.vehicle_id and chd.trip_info_id = ltp.id) "+
				" left outer join op_station lop on (lop.id = ltp.load_gate_op) left outer join op_station uop on (uop.id = ltp.unload_gate_op) "+
				"		) summary_period_max_trip " 
				;

	public static String g_summary2PeriodTrip =
			" ( "+
					"		  SELECT  call_ee.id as item_id, vi.vehicle_id vehicle_id, (case when unloadop.id is not null then (case when unloadop.nick_name is not null then unloadop.nick_name  else unloadop.name end)"+
					"                       else (case when loadop.nick_name is not null then loadop.nick_name else loadop.name end) end) op_name "+
					"        ,count(distinct vi.vehicle_id) veh_count "+
					"        ,sum(case when data_ee.id is null then 0 else 1 end) trip_count "+
					"        ,sum(case when data_ee.id is null then 0 else 1 end)/(timestampdiff(minute, min(combo_start), max(combo_end))/timestampdiff(minute, call_ee.start_time, call_ee.end_time)) trip_per_vehicle "+
					"       ,sum(case when data_ee.id is null then 0 else 1 end)/(timestampdiff(minute, min(combo_start), max(combo_end))/60.0) trip_vehicle_rate "+
					"       ,sum(case when data_ee.id is null then 0 when data_ee.unload_gate_in is not null and timestampdiff(minute, data_ee.unload_gate_in, now()) < 60 then 1 "+
					"                          when data_ee.unload_gate_in is null and timestampdiff(minute, data_ee.load_gate_out, now()) < 60 then 1 else 0 end) "+
					" /(timestampdiff(minute, min(case when (data_ee.unload_gate_in is not null and timestampdiff(minute, data_ee.unload_gate_in, now()) < 60) or (data_ee.unload_gate_in is null and timestampdiff(minute, data_ee.load_gate_out, now()) < 60) then combo_start else null end), "+
					"        max(case when (data_ee.unload_gate_in is not null and timestampdiff(minute, data_ee.unload_gate_in, now()) < 60) or (data_ee.unload_gate_in is null and timestampdiff(minute, data_ee.load_gate_out, now()) < 60) then combo_end else null end))/60.0) last_trip_vehicle_rate "+
					"		 from  "+
					"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+ VEHICLE_FILTER +" ) vi "+
					"		    join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
					"		    join trip_info data_ee on (vi.vehicle_id = data_ee.vehicle_id " +
					"			and " +
					"         ( (data_ee.unload_gate_in is not null and data_ee.unload_gate_in >= call_ee.start_time and data_ee.unload_gate_in < call_ee.end_time) or "+
					"          (data_ee.unload_gate_in is null and data_ee.load_gate_out >= call_ee.start_time and data_ee.load_gate_out < call_ee.end_time) ) "+
					"				) "+
					"        left outer join op_station loadop on (loadop.id = data_ee.load_gate_op) left outer join op_station unloadop on (unloadop.id = data_ee.unload_gate_op) "+
					"		  group by call_ee.id, vi.vehicle_id    "+
					" , (case when unloadop.id is not null then (case when unloadop.nick_name is not null then unloadop.nick_name  else unloadop.name end)"+
					"                       else (case when loadop.nick_name is not null then loadop.nick_name else loadop.name end) end) "+
					"		) summary_2_period_trip " 
					;
	public static String g_summaryPeriodTripLU =
			" ( "+
					"		  SELECT  call_ee.id as item_id, vi.vehicle_id vehicle_id "+
					"        ,sum(case when opstation_mapping.type=1 then 1 else 0 end) load_picked "+
					"        ,sum(case when opstation_mapping.type=2 then 1 else 0 end) unload_done "+
					"		 from  "+
					"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
					"		    join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
					"		    left outer join trip_info_otherLU data_ee on (vi.vehicle_id = data_ee.vehicle_id " +
					"			and " +
					"			((data_ee.gate_in >= '@user_start' and data_ee.gate_in <= '@user_end') or " +
					"			(data_ee.gate_out is null or (data_ee.gate_out >= '@user_start' and data_ee.gate_out <= '@user_end'  ) ))"+
					"		        and (data_ee.gate_in <= call_ee.end_time) "+
					"				and (data_ee.gate_out is null or data_ee.gate_out >= call_ee.start_time) "+ 
					"				) "+		
					"          left outer join op_station on (op_station.id = data_ee.opstation_id) left outer join opstation_mapping on (opstation_mapping.op_station_id = op_station.id) "+
					"		  group by call_ee.id, vi.vehicle_id    "+
					"		) summary_period_lu_trip " 
					;

	public static String g_SummaryBreakdownEvent =
			"(" +
					"	SELECT garage.vehicle_id as vehicle_id, garage.item_id as item_id, (garage.garage_dur+ticket.ticket_dur-overlap.overlap_dur) as breakdown_dur " +
					"	from" +
					"		(  select vi.vehicle_id as vehicle_id, call_ee.id as item_id "+
					"		,sum((case when data_ee.rule_id=3 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end')) else null end) ) as garage_dur "+
					"		 from  "+
					"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
					"		    join minmax_rec_time on (minmax_rec_time.vehicle_id = vi.vehicle_id) "+
					"		    join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
					"		    left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (@data_ee_list) " +
					"			and " +
					"			((data_ee.event_start_time >= '@user_start' and data_ee.event_start_time <= '@user_end') or " +
					"			(data_ee.event_stop_time is null or (data_ee.event_stop_time >= '@user_start' and data_ee.event_stop_time <= '@user_end'  ) ))"+
					"		        and (data_ee.event_start_time <= call_ee.end_time) "+
					"				and (data_ee.event_stop_time is null or data_ee.event_stop_time >= call_ee.start_time) "+ 
					"				) "+		
					"		  group by call_ee.id, vi.vehicle_id    "+
					"		)garage " +
					"	left outer join " +
					"		(" +
					"		select vi.vehicle_id as vehicle_id, call_ee.id as item_id "+
					"		,sum(GET_TICKET_DURATION(vm.actual_start,vm.actual_end,vm.planned_start,vm.planned_end,call_ee.start_time,call_ee.end_time)) as ticket_dur"+
					"		from "+
					"		(select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi"+
					"		join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port )"+	
					"		left outer join vehicle_maint vm on ( "+
					"		(vi.vehicle_id = vm.vehicle_id and  vm.actual_start is not null)"+			
					"			and (vm.actual_start  >= '@start_period' and vm.actual_end <= '@end_period'))"+
					"		group by call_ee.id, vi.vehicle_id "+
					"		)ticket on (ticket.vehicle_id = garage.vehicle_id and ticket.item_id = garage.item_id) " +
					"	left outer join "+
					"		(" +
					"		select vi.vehicle_id as vehicle_id, call_ee.id as item_id, sum(GET_TIME_OVERLAP(data_ee.event_start_time,data_ee.event_stop_time,vm.actual_start,vm.actual_end,call_ee.start_time,call_ee.end_time)) as overlap_dur"+
					"		from"+
					"		(select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi"+ 
					"		join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port)"+		
					"		left outer join engine_events data_ee on ("+
					"			(vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id =3)"+			
					"				and	 ("+
					"					(data_ee.event_start_time >= '@start_period' and data_ee.event_start_time <= '@end_period')"+
					"					or (data_ee.event_stop_time is null or (data_ee.event_stop_time >= '@start_period' and data_ee.event_stop_time <= '@end_period'  ) )"+
					"					)"+
					"				and (data_ee.event_start_time <= call_ee.end_time) "+				
					"				and (data_ee.event_stop_time is null or data_ee.event_stop_time >= call_ee.start_time)"+ 
					"			      ) "+
					"		left outer join vehicle_maint  vm on ( (vm.vehicle_id = vi.vehicle_id)"+
					"					and ("+
					"						(data_ee.event_stop_time is null and vm.actual_end is null and vm.planned_end is null)"+
					"					or	(data_ee.event_stop_time is null and ((case when vm.actual_end is null then vm.planned_end else vm.actual_end end) >= data_ee.event_start_time))"+
					"					or	(vm.actual_end is null and vm.planned_end is null and ((case when vm.actual_start is null then vm.planned_start else vm.actual_start end) < data_ee.event_stop_time))"+
					"					or 	("+
					"							((case when vm.actual_start is null then vm.planned_start else vm.actual_start end) <= data_ee.event_stop_time) and ((case when vm.actual_end is null then vm.planned_end else vm.actual_end end)  >= data_ee.event_start_time)"+
					"						)"+
					"					)"+					
					"				)"+
					"		group by call_ee.id, vehicle_id"+
					"		) overlap on (overlap.vehicle_id = garage.vehicle_id and overlap.vehicle_id = ticket.vehicle_id and overlap.item_id = garage.item_id)"+ 
					"	group by garage.item_id,vehicle_id"+
					")summary_breakdown_event "
					;

	public static String g_summaryPeriodLog =
		" (" +
				" SELECT minrec.vehicle_id, '@PERIOD_ITEM_ID' item_id, (case when (m2.attribute_value - m1.attribute_value - 0) < 0 then 0 else (m2.attribute_value-m1.attribute_value ) end) dist" +
				" , m1.gps_record_time start_time,m1.name start_location,m2.gps_record_time end_time,m2.name end_location " +
				" from " +
				" (select vehicle_id, attribute_id, min(gps_record_time)mi, max(gps_record_time)mx" +
				" from" +
				" 		logged_data " +
				" where attribute_id=0 and logged_data.gps_record_time between '@PERIOD_START_TIME' and '@PERIOD_END_TIME' and logged_data.gps_record_time >= '@user_start' and logged_data.gps_record_time <= '@user_end'"+
				" and vehicle_id in (@VEHICLE_ID_LIST) "+
				" group by vehicle_id, attribute_id " +
				" ) minrec join logged_data m1 on (minrec.vehicle_id = m1.vehicle_id and m1.attribute_id=0 and m1.gps_record_time = minrec.mi)" +
				" join logged_data m2 on (minrec.vehicle_id = m2.vehicle_id and m2.attribute_id=0 and m2.gps_record_time = minrec.mx)" +
				" ) summary_period_lgd "
				;

	public static String g_summaryPeriodLogOrig = //above is curr and is supposed to be used multiple times per 
			" (" +
					" SELECT minrec.vehicle_id, minrec.item_id, (case when (m2.attribute_value - m1.attribute_value - 0) < 0 then 0 else (m2.attribute_value-m1.attribute_value ) end) dist" +
					" , m1.gps_record_time start_time,m1.name start_location,m2.gps_record_time end_time,m2.name end_location " +
					" from " +
					" (select vi.vehicle_id vehicle_id, call_ee.id item_id, min(gps_record_time)mi, max(gps_record_time)mx" +
					" from" +
					" 		(select distinct(vehicle.id) vehicle_id from vehicle  left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi" +
					" 		join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period'  @shift_port)" +
					" 		join logged_data on (logged_data.vehicle_id = vi.vehicle_id and logged_data.attribute_id = 0 and" +
					" logged_data.gps_record_time >= call_ee.start_time and logged_data.gps_record_time <= call_ee.end_time" +
					" and logged_data.gps_record_time >= '@user_start' and logged_data.gps_record_time <= '@user_end')" +
					" group by vehicle_id, call_ee.id" +
					" ) minrec join logged_data m1 on (minrec.vehicle_id = m1.vehicle_id and m1.attribute_id=0 and m1.gps_record_time = minrec.mi)" +
					" join logged_data m2 on (minrec.vehicle_id = m2.vehicle_id and m2.attribute_id=0 and m2.gps_record_time = minrec.mx)" +
					" ) summary_period_lgd "
					;
	public static String g_summaryPeriodLogMTD = 
		" (" +
				" SELECT minrec.vehicle_id, '@PERIOD_ITEM_ID' item_id, (case when (m2.attribute_value - m1.attribute_value - 0) < 0 then 0 else (m2.attribute_value-m1.attribute_value ) end) dist" +
				" , m1.gps_record_time start_time,m1.name start_location,m2.gps_record_time end_time,m2.name end_location " +
				" from " +
				" (select vehicle_id, attribute_id, min(gps_record_time)mi, max(gps_record_time)mx" +
				" from" +
				" 		logged_data where logged_data.attribute_id = 0 and" +
				" logged_data.gps_record_time between '@PERIOD_START_TIME_MONTH and '@PERIOD_END_TIME' " +
				" and logged_data.gps_record_time <= '@user_end' " +
				" and vehicle_id in (@VEHICLE_ID_LIST) "+
				" group by vehicle_id, attribute_id " +
				" ) minrec join logged_data m1 on (minrec.vehicle_id = m1.vehicle_id and m1.attribute_id=0 and m1.gps_record_time = minrec.mi)" +
				" join logged_data m2 on (minrec.vehicle_id = m2.vehicle_id and m2.attribute_id=0 and m2.gps_record_time = minrec.mx)" +
				" ) summary_period_lgd_mtd "
				;

	public static String g_eventBasedMovement = "( "+
		" (select e1.id id, call_ee.id item_id,e1.vehicle_id vehicle_id ,e1.rule_id rule_id,e1.event_begin_longitude event_begin_longitude" +
		",e1.event_begin_latitude event_begin_latitude,e1.event_end_longitude event_end_longitude " +
		",e1.event_end_latitude event_end_latitude ,e1.event_start_time event_start_time ,e1.event_stop_time event_stop_time " +
		",e1.attribute_id attribute_id,e1.attribute_value attribute_value " +
		",e1.event_end_name event_end_name,e1.event_begin_name event_begin_name,TIMESTAMPDIFF(MINUTE, e1.event_start_time, e1.event_stop_time) duration" +
		", 'Stop' state,0 dist from " +
		" 		(select distinct(vehicle.id) vehicle_id from vehicle  left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi" +
		" 		join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period'  @shift_port)" +
		"	    left outer join engine_events e1 on (vi.vehicle_id = e1.vehicle_id and e1.rule_id = 1 " +
		"			and ( "+
		"            (e1.event_start_time >= '@user_start' and e1.event_start_time <= '@user_end') "+
		"             or " +
		"			  (e1.event_stop_time is null or (e1.event_stop_time >= '@user_start' and e1.event_stop_time <= '@user_end'  ) ) "+
		"          ) "+
		"		    and (e1.event_start_time <= call_ee.end_time) "+
		"		    and (e1.event_stop_time is null or e1.event_stop_time >= call_ee.start_time) "+ 
		"		) )"+
//		"engine_events e1 where vehicle_id = 17266 and rule_id = 1 "+
//	" 	and e1.event_start_time > '2013-04-13 00:00:01' and e1.event_stop_time < '2013-04-13 23:59:59') "+
	" 	union "+
	" (select e1.id id, call_ee.id item_id,e1.vehicle_id vehicle_id,e1.rule_id rule_id,e1.event_end_longitude event_begin_longitude,e1.event_end_latitude event_begin_latitude" +
	" ,e2.event_begin_longitude event_end_longitude,e2.event_begin_latitude event_end_latitude "+
	" ,	e1.event_stop_time event_start_time,e2.event_start_time event_stop_time,e2.attribute_id attribute_id,e2.attribute_value attribute_value" +
	" ,e2.event_begin_name event_end_name,e1.event_end_name event_begin_name,TIMESTAMPDIFF(MINUTE, e1.event_stop_time, e2.event_start_time) duration " +
	" , 'Transit' state, (l2.attribute_value-l1.attribute_value) dist from " +
	" 		(select distinct(vehicle.id) vehicle_id from vehicle  left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi" +
	" 		join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period'  @shift_port)" +
	"	    left outer join engine_events e1 on (vi.vehicle_id = e1.vehicle_id and e1.rule_id = 1 " +
	"			and ( "+
	"            (e1.event_start_time >= '@user_start' and e1.event_start_time <= '@user_end') "+
	"             or " +
	"			  (e1.event_stop_time is null or (e1.event_stop_time >= '@user_start' and e1.event_stop_time <= '@user_end'  ) ) "+
	"          ) "+
	"		    and (e1.event_start_time <= call_ee.end_time) "+
	"		    and (e1.event_stop_time is null or e1.event_stop_time >= call_ee.start_time) "+ 
	"		) "+
	"	 join engine_events e2 on e1.vehicle_id = e2.vehicle_id and e1.rule_id = e2.rule_id " +
	"	 and e1.event_stop_time <= e2.event_start_time left outer join logged_data l1 on (l1.vehicle_id=e1.vehicle_id and l1.gps_record_time=e1.event_stop_time and l1.attribute_id=0) " +
	"    left outer join logged_data l2 on (l2.vehicle_id=e1.vehicle_id and l2.gps_record_time=e2.event_start_time and l2.attribute_id=0)" +
//	" engine_events e1 join engine_events e2 on e1.vehicle_id = e2.vehicle_id and e1.rule_id = e2.rule_id "+
//	" 	 and e1.event_stop_time <= e2.event_start_time  "+
//	" 	 and e1.vehicle_id = 17266 and e1.rule_id = 1 "+
//	" 	and e1.event_start_time > '2013-04-13 00:00:01' and e1.event_stop_time < '2013-04-13 23:59:59' "+
	" 	 group by e1.event_start_time) "+
	" 	order by event_start_time "+
	
	") event_based_movement";
	
	
	
	
//	public static String g_summaryPeriodStartEnd = " ( select minmax.item_id as item_id, minmax.vehicle_id as vehicle_id," +
//	"lgd1.gps_record_time start_time,lgd1.name start_location,lgd2.gps_record_time end_time,lgd2.name end_location, " +
//	" (case when (lgd2.attribute_value -lgd1.attribute_value - 0) < 0 then 0 else (lgd2.attribute_value- lgd1.attribute_value ) end) dist "+ 
//"  from (select call_ee.id as item_id, vi.vehicle_id as vehicle_id ,min(logged_data.gps_record_time) as mi,max(logged_data.gps_record_time) as mx "+ 
//"  from " +
//"     (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi "+
//"	 join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port) "+
//"     join logged_data on (logged_data.vehicle_id = vi.vehicle_id and logged_data.attribute_id = 0 and" +
//"   logged_data.gps_record_time >= call_ee.start_time and (logged_data.gps_record_time <= call_ee.end_time)" +
//"   and logged_data.gps_record_time >= '@user_start' and logged_data.gps_record_time <= '@user_end') "+
//"  group by call_ee.id, vi.vehicle_id ) minmax  "+
//"  join logged_data lgd1 on (lgd1.vehicle_id = minmax.vehicle_id and minmax.mi = lgd1.gps_record_time and lgd1.attribute_id = 0) "+
//"  join logged_data lgd2 on (lgd2.vehicle_id = minmax.vehicle_id and minmax.mx = lgd2.gps_record_time and lgd2.attribute_id = 0)" +
//" ) summary_period_start_end ";
	public static String g_summaryPeriodAttrib2 =
		" (" +
		" SELECT minrec.vehicle_id, '@PERIOD_ITEM_ID' item_id, (case when (m2.speed - m1.speed - 0) < 0 then 0 else (m2.speed-m1.speed ) end)*60 hrs, " +
				" TIMESTAMPDIFF(MINUTE,greatest('@user_start','@PERIOD_START_TIME'),least('@user_end','@PERIOD_END_TIME'))-(case when (m2.speed - m1.speed - 0) < 0 then 0 else (m2.speed-m1.speed ) end)*60 off_hrs " +
				" from " +
				" (select vehicle_id, attribute_id,  min(gps_record_time)mi, max(gps_record_time)mx" +
				" from" +
//				" 		(select distinct(vehicle.id) vehicle_id from vehicle  left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi" +
//				" 		join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period'  @shift_port)" +
				" 		logged_data where logged_data.attribute_id = 2 and" +
				" logged_data.gps_record_time between '@PERIOD_START_TIME'  and '@PERIOD_END_TIME' " +
				" and logged_data.gps_record_time between '@user_start' and  '@user_end'" +
				" and vehicle_id in (@VEHICLE_ID_LIST) "+
				" group by vehicle_id, attribute_id " +
				" ) minrec join logged_data m1 on (minrec.vehicle_id = m1.vehicle_id and m1.attribute_id=2 and m1.gps_record_time = minrec.mi)" +
				" join logged_data m2 on (minrec.vehicle_id = m2.vehicle_id and m2.attribute_id=2 and m2.gps_record_time = minrec.mx)" +
				" ) summary_period_attrib2 "
				;
	public static String g_summaryPeriodAttrib21 = //dupl of 2
		" (" +
		" SELECT minrec.vehicle_id, '@PERIOD_ITEM_ID' item_id, (case when (m2.speed - m1.speed - 0) < 0 then 0 else (m2.speed-m1.speed ) end)*60 hrs " +
				" from " +
				" (select vehicle_id, attribute_id, min(gps_record_time)mi, max(gps_record_time)mx" +
				" from" +
//				" 		(select distinct(vehicle.id) vehicle_id from vehicle  left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi" +
//				" 		join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period'  @shift_port)" +
				" 		logged_data  where logged_data.attribute_id = 21 and" +
				" logged_data.gps_record_time between '@PERIOD_START_TIME' and '@PERIOD_END_TIME' " +
				" and logged_data.gps_record_time between '@user_start' and  '@user_end' " +
				" and vehicle_id in (@VEHICLE_ID_LIST) "+
				" group by vehicle_id, attribute_id " +
				" ) minrec join logged_data m1 on (minrec.vehicle_id = m1.vehicle_id and m1.attribute_id=21 and m1.gps_record_time = minrec.mi)" +
				" join logged_data m2 on (minrec.vehicle_id = m2.vehicle_id and m2.attribute_id=21 and m2.gps_record_time = minrec.mx)" +
				" ) summary_period_attrib21 "
				;
	
	public static String g_summaryPeriodEventLog = 
			" (" +
					" SELECT minrec.vehicle_id, '@PERIOD_ITEM_ID' item_id, (case when (m2.attribute_value - m1.attribute_value - 0) < 0 then 0 else (m2.attribute_value-m1.attribute_value ) end) dist" +
					" from " +
					
					" (select logged_data.vehicle_id, logged_data.attribute_id, min(gps_record_time)mi, max(gps_record_time)mx" +
					" from" +
					" engine_events data_ee "+
//					" 		(select distinct(vehicle.id) vehicle_id from vehicle  left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi" +
//					" 		join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period'  @shift_port)" +
//					"	   engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (327) " +
//					"			and ( "+
//					"            (data_ee.event_start_time >= '@user_start' and data_ee.event_start_time <= '@user_end') "+
//					"             or " +
//					"			  (data_ee.event_stop_time is null or (data_ee.event_stop_time >= '@user_start' and data_ee.event_stop_time <= '@user_end'  ) ) "+
//					"          ) "+
//					"		    and (data_ee.event_start_time <= call_ee.end_time) "+
//					"		    and (data_ee.event_stop_time is null or data_ee.event_stop_time >= call_ee.start_time) "+ 
//					"		) "+		
					" 		join logged_data on (data_ee.rule_id in (327) and logged_data.vehicle_id = data_ee.vehicle_id and logged_data.attribute_id = 0  " +
					" and logged_data.gps_record_time >= '@PERIOD_START_TIME' and logged_data.gps_record_time <= '@PERIOD_END_TIME' " +
					" and logged_data.gps_record_time >= '@user_start' and logged_data.gps_record_time <= '@user_end' " +
					" and logged_data.gps_record_time >= data_ee.event_start_time "+
					" and (data_ee.event_stop_time is null or logged_data.gps_record_time <= data_ee.event_stop_time)" +
					"		    and (data_ee.event_start_time <= '@PERIOD_END_TIME') "+
					"		    and (data_ee.event_stop_time is null or data_ee.event_stop_time >= '@PERIOD_START_TIME' ) "+ 
					
					" ) "+
					" where logged_data.vehicle_id in (@VEHICLE_ID_LIST) "+
					" and logged_data.attribute_id=0 "+
					" and logged_data.gps_record_time >= '@PERIOD_START_TIME' and logged_data.gps_record_time <= '@PERIOD_END_TIME' " +
					"		    and (data_ee.event_start_time <= '@PERIOD_END_TIME') "+
					"		    and (data_ee.event_stop_time is null or data_ee.event_stop_time >= '@PERIOD_START_TIME' ) "+ 
					" and data_ee.vehicle_id in (@VEHICLE_ID_LIST) "+
					" group by logged_data.vehicle_id, logged_data.attribute_id" +
					" ) minrec join logged_data m1 on (minrec.vehicle_id = m1.vehicle_id and m1.attribute_id=0 and m1.gps_record_time = minrec.mi)" +
					" join logged_data m2 on (minrec.vehicle_id = m2.vehicle_id and m2.attribute_id=0 and m2.gps_record_time = minrec.mx)" +
					" ) summary_period_event_lgd "
					;
	
	
	public static String g_summaryPeriodSpeed = 
			" (" +
			
			"  SELECT @PERIOD_ITEM_ID  as item_id, sps.vehicle_id vehicle_id "+
			"  ,sps.mxspeed mxspeed "+
			"  ,min(lgd1.gps_record_time) max_speed_time "+
			"  ,min(lgd1.name) max_speed_loc "+
			"  ,sps.avgspeed avg_speed"+
			"  from  "+
			
					" ( SELECT logged_data.vehicle_id, attribute_id "+
					
					"  , max(logged_data.gps_record_time) mxgrd, min(logged_data.gps_record_time) migrd "+
					"  ,max(logged_data.speed) mxspeed " +
					"  ,avg(logged_data.speed) avgspeed "+
					"  from  "+
					"     logged_data where logged_data.attribute_id = 0 and" +
					" logged_data.gps_record_time between '@PERIOD_START_TIME' and '@PERIOD_END_TIME' " +
					" and logged_data.gps_record_time >= '@user_start' and logged_data.gps_record_time <= '@user_end' "+
					" and vehicle_id in (@VEHICLE_ID_LIST) "+
					"   group by vehicle_id, attribute_id "+
					" ) sps " +
					" left outer " +
//					"	 join @period call_ee1 on (call_ee1.start_time >= '@start_period' and call_ee1.start_time <= '@end_period' @shift_port) "+
					"     join logged_data lgd1 on (lgd1.vehicle_id = sps.vehicle_id and lgd1.attribute_id = 0 and " +
					"   lgd1.gps_record_time >= sps.migrd and lgd1.gps_record_time <= sps.mxgrd" +
//					" lgd1.gps_record_time >= call_ee1.start_time and (lgd1.gps_record_time <= call_ee1.end_time)" +
//					" and lgd1.gps_record_time >= '@user_start' and lgd1.gps_record_time <= '@user_end' " +
					" and sps.mxspeed = lgd1.speed ) " +
					"  group by sps.vehicle_id, sps.mxspeed "+
//					"join logged_data lgd1 on (lgd1.vehicle_id = sps.vehicle_id and sps.mxspeed = lgd1.speed and lgd1.attribute_id = 0) group by sps.vehicle_id" +
					") summary_period_speed " 
					;
public static String g_summaryPeriodFuel = "( "+
  " select inttbl.item_id, inttbl.vehicle_id, inttbl.start_level, inttbl.end_level, inttbl.tot_add, inttbl.tot_sub "+
  ", (inttbl.start_level - inttbl.end_level + inttbl.tot_add) consumption "+
  ", (inttbl.end_dist - inttbl.start_dist) dist "+
  
  " ,(case when (inttbl.start_level - inttbl.end_level + inttbl.tot_add) < 0.5 or inttbl.end_dist < inttbl.start_dist then null else ((inttbl.end_dist-inttbl.start_dist)/(inttbl.start_level - inttbl.end_level + inttbl.tot_add)) end) kmpl "+
  " from "+
	"( "+
		" SELECT call_ee.item_id, call_ee.vehicle_id "+ 
		"  ,sum(case when engine_events.is_refuel=1 then engine_events.addnl_value1 else 0 end) tot_add "+
		" ,sum(case when engine_events.is_refuel=0 or is_refuel is null then engine_events.addnl_value1 else 0 end) tot_sub "+
		" ,lgd1.attribute_value start_level, lgd2.attribute_value end_level "+
		" ,lgd1d.attribute_value start_dist, lgd2d.attribute_value end_dist "+
		//" , (lgd1.attribute_value - lgd2.attribute_value "+
		//"    + sum(case when engine_events.is_refuel=1 then engine_events.addnl_value1 else 0 end)) consumption "+
		
		"  from   "+
		"   (select logged_data.vehicle_id, attribute_id, min(gps_record_time) migrt, max(gps_record_time) mxgrt from "+
		"  logged_data where attribute_id = 3 and logged_data.gps_record_time between '@PERIOD_START_TIME' and '@PERIOD_END_TIME' " +
		" and logged_data.gps_record_time >= '@user_start' and logged_data.gps_record_time <= '@user_end' "+
		" and vehicle_id in (@VEHICLE_ID_LIST) "+
		"  group by vehicle_id, attribute_id "+
		"  ) call_ee join logged_data lgd1 on (lgd1.vehicle_id = call_ee.vehicle_id and lgd1.attribute_id = 3 and lgd1.gps_record_time = call_ee.migrt) "+
		"   join logged_data lgd2 on (lgd2.vehicle_id = call_ee.vehicle_id and lgd2.attribute_id = 3 and lgd2.gps_record_time = call_ee.mxgrt) "+
		"   join "+
		"   (select vehicle_id, attribute_id, min(gps_record_time) migrt, max(gps_record_time) mxgrt from "+
		"  logged_data where attribute_id = 0 and logged_data.gps_record_time between '@PERIOD_START_TIME' and '@PERIOD_END_TIME'  "+
		" and logged_data.gps_record_time >= '@user_start' and logged_data.gps_record_time <= '@user_end' "+
		" and logged_data.vehicle_id in (@VEHICLE_ID_LIST) "+
		"  group by vehicle_id, attribute_id "+
		"  ) call_eed on (call_eed.vehicle_id = call_ee.vehicle_id and call_eed.item_id = call_ee.item_id) "+
		" join logged_data lgd1d on (lgd1d.vehicle_id = call_eed.vehicle_id and lgd1d.attribute_id = 0 and lgd1d.gps_record_time = call_eed.migrt) "+
		"   join logged_data lgd2d on (lgd2d.vehicle_id = call_eed.vehicle_id and lgd2d.attribute_id = 0 and lgd2d.gps_record_time = call_eed.mxgrt) "+
		"   left outer join "+
		"   engine_events on (engine_events.vehicle_id = call_ee.vehicle_id and engine_events.rule_id = 151 and engine_events.event_stop_time >= call_ee.start_time and engine_events.event_stop_time <= call_ee.end_time "+
		" and engine_events.event_stop_time >= '@user_start' and engine_events.event_stop_time <= '@user_end' "+
		") "+
		"   group by call_ee.item_id, call_ee.vehicle_id, lgd1.attribute_value, lgd2.attribute_value, lgd1d.attribute_value, lgd2d.attribute_value "+
		" ) inttbl "+
		") summary_period_fuel "
		;
	public static String g_summaryDailyTrip =
			" ( "+
					"		  SELECT  vi.vehicle_id vehicle_id "+
					"       ,sum((case when loadop.id is not null and load_gate_in >= call_ee.start_time then 1 else 0 end)) center_stop_count "+
					"       ,sum((case when loadop.sub_type=21  and load_gate_in >= call_ee.start_time then 1 else 0 end)) center_stop_count_1 "+
					"       ,sum((case when loadop.sub_type=22  and load_gate_in >= call_ee.start_time then 1 else 0 end)) center_stop_count_2 "+
					"       ,sum((case when loadop.sub_type=23  and load_gate_in >= call_ee.start_time then 1 else 0 end)) center_stop_count_3 "+
					"       ,sum((case when loadop.sub_type=24  and load_gate_in >= call_ee.start_time then 1 else 0 end)) center_stop_count_4 "+
					"       ,sum((case when loadop.sub_type=25  and load_gate_in >= call_ee.start_time then 1 else 0 end)) center_stop_count_5 "+
					"       ,sum((case when unloadop.id is not null and unload_gate_in >= call_ee.start_time then 1 else 0 end)) unload_count "+					  
					"       ,min((case when data_ee.load_gate_in < call_ee.start_time  then call_ee.start_time else data_ee.load_gate_in end)) center_sop "+
					//"		 ,sum(GET_DURATION(data_ee.load_area_wait_in,data_ee.load_gate_out,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end'))) as load_time "+
					//"		 ,sum(GET_DURATION(data_ee.unload_area_wait_in,data_ee.unload_gate_out,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end'))) as unload_time "+
					//"		 ,sum(GET_DURATION((case when data_ee.load_gate_out is not null and data_ee.unload_area_wait_in is not null then data_ee.load_gate_out else null end), (case when data_ee.load_gate_out is not null and data_ee.unload_area_wait_in is not null then data_ee.unload_area_wait_in else null end),call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end'))) as load_lead_time "+
					//"		 ,sum(GET_DURATION((case when data_ee.unload_gate_out is not null and data_ee.confirm_time is not null then data_ee.unload_gate_out else null end),(case when data_ee.unload_gate_out is not null and data_ee.confirm_time is not null then data_ee.confirm_time else null end),call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,'@user_start'), LEAST(minmax_rec_time.mx,'@user_end'))) as unload_lead_time "+
					"		 from  "+
					"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
					//"		    join minmax_rec_time on (minmax_rec_time.vehicle_id = vi.vehicle_id) "+
					"		    cross join (select curdate() start_time, now() end_time from dual) call_ee "+
					"		    left outer join trip_info data_ee on (vi.vehicle_id = data_ee.vehicle_id " +
//RAJEEV_20150201					"			and data_ee.confirm_time is null  "+
					"		        and (data_ee.combo_start <= call_ee.end_time) "+
					"				and (data_ee.confirm_time is null or data_ee.confirm_time >= call_ee.start_time) "+ 
					"				) "+
					"        left outer join op_station loadop on (loadop.id = data_ee.load_gate_op) "+
					"        left outer join op_station unloadop on (unloadop.id = data_ee.unload_gate_op) "+
					"		  group by vi.vehicle_id    "+
					"		) summary_daily_trip " 
					;
	public static String g_summaryDailyTripLU = //todo generalize for shift based view
			" ( "+
			" select summ.vehicle_id, summ.load_picked, summ.unload_done, summ.gin last_load_at, summ.unload_gin last_unload_at, op_station.name last_load_station, est_groups.name est_group_name, est_groups.recommended_vehicle veh_need "+
			" from "+
			"		  (SELECT vi.vehicle_id vehicle_id "+
			"        ,sum(case when opstation_mapping.type=1 then 1 else 0 end) load_picked "+
			"        ,sum(case when opstation_mapping.type=2 then 1 else 0 end) unload_done "+
			"        ,max(case when opstation_mapping.type=1 then gate_in else null end) gin "+
			"        ,max(case when opstation_mapping.type=2 then gate_in else null end) unload_gin "+
			"		 from  "+
			"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
			"		    join trip_info_otherLU data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.gate_in >= curdate()) " +
			"          join op_station on (op_station.id = data_ee.opstation_id) left outer join opstation_mapping on (opstation_mapping.op_station_id = op_station.id) "+
			"		 group by  vi.vehicle_id    "+
			"		) summ join trip_info_otherLU on (summ.vehicle_id = trip_info_otherLU.vehicle_id and summ.gin = trip_info_otherLU.gate_in) "+
			"      join op_station on (op_station.id = trip_info_otherLU.opstation_id) "+
			"      join group_opstation_items est_items on (est_items.opstation_id = trip_info_otherLU.opstation_id) join group_opstations est_groups on (est_groups.id = est_items.group_opstation_id) "+
			//"      left outer join "+ //TODO for assignment
			" ) summary_daily_tripLU " 
			;
	public static String g_summaryDailyEvent =
			" ( "+ 
					"		  SELECT  temp_period_event.vehicle_id   "+
					"       ,temp_period_event.engine_dur, temp_period_event.stop_dur, temp_period_event.garage_dur, temp_period_event.rest_area_dur, temp_period_event.max_stop_dur, temp_period_event.move_dur, temp_period_event.percentage_util, temp_period_event.work_area_stop_count "+
					"       , (case when sop > pen or sop < pst then null "+
					"                     else  sop end) start_op_time "+
					"       , (case when eop <= sop then pen "+
					"                    when eop < pst then null "+
					"                     else  eop end) end_op_time "+
					//For APMED
					" , assigned_work_area_dur, outside_assigned_work_area_dur "+
					", center_stop_count, center_sop "+
					", center_stop_count_1, center_stop_count_2, center_stop_count_3, center_stop_count_4, center_stop_count_5 "+

		" from "+
		"      (select  vi.vehicle_id vehicle_id "+
		//For APMed
		"       ,min((case when data_ee.rule_id=328 then data_ee.event_start_time else null end)) center_sop "+
		"		 ,sum((case when data_ee.rule_id=327 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as assigned_work_area_dur "+
		"		 ,(case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(minute, GREATEST(call_ee.start_time,minmax_rec_time.mi,call_ee.start_time), LEAST(call_ee.end_time,minmax_rec_time.mx,call_ee.end_time) " +
		"		) - sum((case when data_ee.rule_id=327 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) )) end) as outside_assigned_work_area_dur " +
		"       ,sum((case when data_ee.rule_id=328 then 1 else 0 end)) center_stop_count "+
		"       ,sum((case when data_ee.rule_id=328 and regions.region_type=21 then 1 else 0 end)) center_stop_count_1 "+
		"       ,sum((case when data_ee.rule_id=328 and regions.region_type=22  then 1 else 0 end)) center_stop_count_2 "+
		"       ,sum((case when data_ee.rule_id=328 and regions.region_type=23  then 1 else 0 end)) center_stop_count_3 "+
		"       ,sum((case when data_ee.rule_id=328 and regions.region_type=24  then 1 else 0 end)) center_stop_count_4 "+
		"       ,sum((case when data_ee.rule_id=328 and regions.region_type=25  then 1 else 0 end)) center_stop_count_5 "+


		"       ,call_ee.start_time pst, call_ee.end_time pen "+
		"      ,sum((case when data_ee.rule_id=5 then 1 else 0 end)) work_area_stop_count "+
		"       ,min((case when data_ee.rule_id=4  then data_ee.event_stop_time else null end)) sop "+
		"       ,max((case when data_ee.rule_id=4  then data_ee.event_start_time else null end)) eop  "+
		"		 ,sum((case when data_ee.rule_id=56 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as engine_dur "+
		"		 ,sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as stop_dur "+
		"		 ,sum((case when data_ee.rule_id=3 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as garage_dur "+
		"		 ,sum((case when data_ee.rule_id=4 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as rest_area_dur "+
		"		 ,max((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) ) as max_stop_dur "+		
		"		 ,(case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(minute, GREATEST(call_ee.start_time,minmax_rec_time.mi,call_ee.start_time), LEAST(call_ee.end_time,minmax_rec_time.mx,call_ee.end_time) " +
		//		"		(case when call_ee.start_time < minmax_rec_time.mi then minmax_rec_time.mi else call_ee.start_time end), " +
		//		"		(case when call_ee.end_time > minmax_rec_time.mx then minmax_rec_time.mx else call_ee.end_time end)" +
		"		) - sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) )) end) as move_dur" +
		" 		, (((case when minmax_rec_time.mx < call_ee.start_time then null else (Timestampdiff(minute, GREATEST(call_ee.start_time,minmax_rec_time.mi,call_ee.start_time), LEAST(call_ee.end_time,minmax_rec_time.mx,call_ee.end_time) ) - sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.start_time, call_ee.end_time, GREATEST(minmax_rec_time.mi,call_ee.start_time), LEAST(minmax_rec_time.mx,call_ee.end_time)) else null end) )) end) )/ Timestampdiff(minute,Greatest(call_ee.start_time,call_ee.start_time),Least(call_ee.end_time, call_ee.end_time) ))*100 percentage_util	"+
		"		 from  "+
		"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
		"		    join minmax_rec_time on (minmax_rec_time.vehicle_id = vi.vehicle_id) "+
		"		    cross join (select curdate() start_time, now() end_time from dual) call_ee "+
		"		    left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (@data_ee_list) " +
		"			and " +
		"			((data_ee.event_start_time >= call_ee.start_time) or " +
		"			(data_ee.event_stop_time is null) or (data_ee.event_stop_time >= call_ee.start_time) "+
		"               )"+
		"			) "+		
		"         left outer join regions on (regions.id = data_ee.ref_region_id) "+
		"		  group by  vi.vehicle_id, call_ee.start_time, call_ee.end_time    "+
		" ) temp_period_event "+
		"		) summary_daily_event "
		;

	
	public static String g_summaryDailyEventLog =
			" (" +
					" SELECT minrec.vehicle_id, (case when (m2.attribute_value - m1.attribute_value - 0) < 0 then 0 else (m2.attribute_value-m1.attribute_value ) end) dist" +
					" from " +
					" (select logged_data.vehicle_id, logged_data.attribute_id, min(gps_record_time)mi, max(gps_record_time)mx" +
					" from" +
					" engine_events data_ee "+
					" 		join logged_data on (logged_data.vehicle_id = data_ee.vehicle_id and logged_data.attribute_id = 0 and " +
					" logged_data.gps_record_time >= curdate() and logged_data.gps_record_time <= now() " +
					" and logged_data.gps_record_time >= data_ee.event_start_time and (data_ee.event_stop_time is null or logged_data.gps_record_time <= data_ee.event_stop_time)" +
					" and data_ee.rule_id in (327) " +
					
					"			and " +
					"			((data_ee.event_start_time >= curdate()) or " +
					"			(data_ee.event_stop_time is null) or (data_ee.event_stop_time >= curdate()) "+
					"               )"+
					")" +
					" where data_ee.rule_id = 327 and data_ee.vehicle_id in (@VEHICLE_ID_LIST) "+
					" and "+
					" ((data_ee.event_start_time >= curdate()) or " +
							"			(data_ee.event_stop_time is null) or (data_ee.event_stop_time >= curdate()) "+
							"               )"+
					" and logged_data.attribute_id=0 "+
					" and data_ee.vehicle_id in (@VEHICLE_ID_LIST) "+
					" group by logged_data.vehicle_id, logged_data.attribute_id " +
					" ) minrec join logged_data m1 on (minrec.vehicle_id = m1.vehicle_id and m1.attribute_id=0 and m1.gps_record_time = minrec.mi)" +
					" join logged_data m2 on (minrec.vehicle_id = m2.vehicle_id and m2.attribute_id=0 and m2.gps_record_time = minrec.mx)" +
					" ) summary_daily_event_lgd "
					;

public static String g_summaryDailyFuel = "(" +
		" SELECT call_ee.vehicle_id "+ 
		"  ,sum(case when engine_events.is_refuel= 1 then engine_events.addnl_value1 else 0 end) tot_add "+
		"  ,sum(case when engine_events.is_refuel=0 or engine_events.is_refuel is null then engine_events.addnl_value1 else 0 end) tot_sub "+
		" ,lgd1.attribute_value start_level, lgd2.attribute_value end_level "+
		" , (lgd1.attribute_value - lgd2.attribute_value "+
		"    + sum(case when engine_events.is_refuel=1 then engine_events.addnl_value1 else 0 end)) consumption "+
		"  from   "+
		" (select vehicle_id, attribute_id, min(gps_record_time) migrt, max(gps_record_time) mxgrt "+
        " from logged_data where attribute_id = 3 and logged_data.gps_record_time >= curdate() "+
        " and logged_data.vehicle_id in (@VEHICLE_ID_LIST) "+
		
		"  group by vehicle_id, attribute_id "+
		"  ) call_ee join logged_data lgd1 on (lgd1.vehicle_id = call_ee.vehicle_id and lgd1.attribute_id = 3 and lgd1.gps_record_time = migrt) "+
		"   join logged_data lgd2 on (lgd2.vehicle_id = call_ee.vehicle_id and lgd2.attribute_id = 3 and lgd2.gps_record_time = mxgrt) "+
		"   left outer join "+
		"   engine_events on (engine_events.vehicle_id = call_ee.vehicle_id and engine_events.rule_id = 151 and engine_events.event_stop_time >= curdate() "+
		") "+
		"   group by call_ee.vehicle_id, lgd1.attribute_value, lgd2.attribute_value "+
		") summary_daily_fuel "
		;


	public static String g_summaryEventEvent =
			" ( "+
					"		  SELECT  call_ee.id as item_id "+
					"		 ,sum((case when data_ee.rule_id=56 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.event_start_time, call_ee.event_stop_time, call_ee.event_start_time, minmax_rec_time.mx) else null end) ) as engine_dur "+
					"		 ,sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.event_start_time, call_ee.event_stop_time, call_ee.event_start_time, minmax_rec_time.mx) else null end) ) as stop_dur "+
					"		 ,(case when minmax_rec_time.mx < call_ee.event_start_time then null else (Timestampdiff(minute, call_ee.event_start_time, (case when call_ee.event_stop_time is null then minmax_rec_time.mx else call_ee.event_stop_time end)) - sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.event_start_time, call_ee.event_stop_time, call_ee.event_start_time, minmax_rec_time.mx) else null end) )) end) as move_dur "+
					"		 from  "+
					"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
					"		    join minmax_rec_time on (minmax_rec_time.vehicle_id = vi.vehicle_id) "+
					"		    join engine_events call_ee on (call_ee.vehicle_id = minmax_rec_time.vehicle_id) "+
					"		    join rules on (rules.id = call_ee.rule_id and rules.use_internal_shift = 1) "+
					"		    join engine_events data_ee on (call_ee.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (@data_ee_list) "+
					"		        and ( "+
					"				(call_ee.event_stop_time is not null "+
					"				and data_ee.event_start_time < call_ee.event_stop_time "+
					"				and (data_ee.event_stop_time is null or data_ee.event_stop_time > call_ee.event_start_time) "+ 
					"				) "+
					"		                or "+
					"		                (call_ee.event_stop_time is null "+
					"				and (data_ee.event_stop_time is null or data_ee.event_stop_time > call_ee.event_start_time) "+
					"		                ) "+
					"			     ) "+
					"			) "+
					"		   where 1=1 "+
					"		   and call_ee.event_start_time >= '@start_period' "+
					"		   and (call_ee.event_stop_time is null or call_ee.event_stop_time <= '@end_period' ) "+
					"		  group by call_ee.id    "+
					"		) summary_event_event " 
					;
//Lafarge events 319,320,485,486,487,488,489,490

	public static String g_summaryTripSafetyEvent =
		" ( "+
				"		  SELECT  call_ee.id as item_id "+
				"		 ,sum(case when data_ee.rule_id=319 then 1 else 0 end) as c319 "+
				"		 ,sum(case when data_ee.rule_id=320 then 1 else 0 end) as c320 "+
				"		 ,sum(case when data_ee.rule_id=485 then 1 else 0 end) as c485 "+
				"		 ,sum(case when data_ee.rule_id=486 then 1 else 0 end) as c486 "+
				"		 ,sum(case when data_ee.rule_id=487 then 1 else 0 end) as c487 "+
				"		 ,sum(case when data_ee.rule_id=488 then 1 else 0 end) as c488 "+
				"		 ,sum(case when data_ee.rule_id=489 then 1 else 0 end) as c489 "+
				"		 ,sum(case when data_ee.rule_id=490 then 1 else 0 end) as c490 "+
				"		 ,sum(case when data_ee.rule_id=564 and data_ee.event_begin_name not like 'MPL%' and data_ee.event_start_time between call_ee.load_gate_out and  call_ee.unload_gate_in then 1 else 0 end) as c564 "+
				"		 ,sum(case when data_ee.rule_id=562 and (data_ee.end_cumm_dist-data_ee.start_cumm_dist) <= 7 and timestampdiff(minute, data_ee.event_start_time, data_ee.event_stop_time) <= 15  then 1 else 0 end) as c562 "+
				"		 ,sum(case when data_ee.rule_id=1 and data_ee.event_begin_name like '[UZ]%' and data_ee.event_start_time between call_ee.load_gate_out and  call_ee.unload_gate_in then 1 else 0 end) as cUZ "+
				"		 ,sum(case when data_ee.rule_id=575  and data_ee.event_start_time between call_ee.load_gate_out and  call_ee.unload_gate_in then 1 else 0 end) as cAdDeviation "+
				"		 ,sum(case when data_ee.rule_id in (571,572)  and (timestampdiff(minute, data_ee.event_start_time, data_ee.event_stop_time) >= 5 or data_ee.event_stop_time is null)  and data_ee.event_start_time between call_ee.load_gate_out and  call_ee.unload_gate_in then 1 else 0 end) as cPower "+
				"		 ,sum(case when data_ee.rule_id in (573,573)  and data_ee.event_start_time between call_ee.load_gate_out and  call_ee.unload_gate_in then 1 else 0 end) as cNetwork "+
				"       ,sum(case when data_ee.rule_id in (319,320,485,486,487,488,489,490) then 1 else 0 end) as c_safe_tot "+
				"		 from  "+
				"		   (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
				"		    join trip_info call_ee on (call_ee.vehicle_id = vi.vehicle_id) "+
				"		    join engine_events data_ee on (call_ee.vehicle_id = data_ee.vehicle_id and call_ee.combo_start <= data_ee.event_start_time "+
				"		        and ( "+
				"				call_ee.confirm_time is  null "+
				"				or call_ee.combo_end >= data_ee.event_start_time "+
				"				) "+
				"			) "+
				"		   where 1=1 "+
				"		   and call_ee.combo_start >= '@start_period' "+
				"         and data_ee.rule_id in (319,320,485,486,487,488,489,490,562,564, 571, 572, 573, 574, 575) "+
				"		   and (call_ee.confirm_time is null or call_ee.confirm_time <= '@end_period' ) "+
				"		  group by call_ee.id    "+
				"		) summary_trip_safety_event " 
				;

	
	public static String g_summaryEventLog =
			" ( "+
					"   SELECT call_ee.id as item_id ,max(speed) max_speed" +
					",(case when logged_data.attribute_id = 0 then max(logged_data.attribute_value) - min(logged_data.attribute_value) else null end)dist "+
					//"  ,sum(case when logged_data.attribute_id = 0 and logged_data.speed >= 2.4 then logged_data.attribute_value else null end) dist "+
					"  from  "+
					"     (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
					"     join engine_events call_ee on (vi.vehicle_id = call_ee.vehicle_id) "+
					"     join rules on (rules.id = call_ee.rule_id and rules.use_internal_shift = 1) "+
					"     join logged_data on (logged_data.vehicle_id = call_ee.vehicle_id and logged_data.attribute_id = 0 and logged_data.gps_record_time >= call_ee.event_start_time and (call_ee.event_stop_time is null or logged_data.gps_record_time <= call_ee.event_stop_time)) "+
					"    where 1=1 "+
					"    and call_ee.event_start_time >= '@start_period' "+
					"    and (call_ee.event_stop_time is null or call_ee.event_stop_time <= '@end_period' ) "+
					"   group by call_ee.id "+
					" ) summary_event_lgd " 
					;


	public static String g_internalShiftSnippet = 
			" join  rules on (rules.id = engine_events.rule_id and rules.use_internal_shift=1) "+
					" join conditions_clauses on (conditions_clauses.rule_id = rules.id and conditions_clauses.rule_type_id=1) "+
					" join regions regions9075 on (regions9075.id = conditions_clauses.param_0) "
					;
	//currently g_lastMoveDistance is done using max(attribute_value) - min (attribute_value) ... if found to be slow then rewrite using same approach as g_summaryPeriodLog
	//assumption being that there only few points between stops ... so complicated joins are no good
	//this query only works for vehicle having had at least one stop and that is ended
	public static String g_lastMoveDistance = "( select logged_data.vehicle_id, " +
			" max(logged_data.attribute_value) - min(logged_data.attribute_value) last_move_dist "+
			" from " +
			"(select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi " +
			"join" +
			"(select engine_events.vehicle_id, max(event_stop_time) en from engine_events where (rule_id = 1) and event_stop_time is not null group by engine_events.vehicle_id) mimx"+ 
			" on vi.vehicle_id = mimx.vehicle_id " +
			" join logged_data on (logged_data.vehicle_id = mimx.vehicle_id and logged_data.attribute_id = 0 and logged_data.gps_record_time >= mimx.en and logged_data.speed >= 2.4) "+
			" group by logged_data.vehicle_id) last_distance_movement "
			;
	/*
	public static String g_distanceUsingOdometerOld = "(select current_data.vehicle_id," +
			"min(logged_data.attribute_value) miles_data from logged_data where logged_data.attribute_id = 0 and logged_data.gps_record_time >= CURDATE() " +
			"group by logged_data.vehicle_id ) " +
			"odometer_distance_movement";

	public static String g_distanceMovedToday = "( select vehicle_id," +
			"sum(case when logged_data.attribute_id = 0 and logged_data.speed >= 2.4 then logged_data.attribute_value else 0 end) dist from logged_data " +
			"where logged_data.gps_record_time > CURDATE() " +
			"group by logged_data.vehicle_id) distance_movement";

	public static String g_distanceMovedUsingOdometerOld = "(select logged_data.vehicle_id," +
			"(case when attribute_id = 0 then (max(logged_data.attribute_value) - min(logged_data.attribute_value)) else 0 end) moved_miles_data from logged_data where logged_data.attribute_id = 0 and logged_data.gps_record_time >= CURDATE() " +
			"group by logged_data.vehicle_id ) " +
			"odometer_moved_distance_movement";
	 */

	public static String g_sinceStopEngineOn = 
			"	(select vehicle.id vehicle_id "+
					"	,(case when timestampdiff(minute, current_data.gps_record_time, currtime.ts) > 240 then timestampdiff(minute, current_data.gps_record_time, currtime.ts) else "+
					"      (case when stopped.since is null then 0 else greatest(0,timestampdiff(minute, stopped.since, currtime.ts)) end)"+
					"   end ) sdur "+
					"	,(case when timestampdiff(minute, current_data.gps_record_time, currtime.ts) > 240 then timestampdiff(minute, current_data.gps_record_time, currtime.ts) else "+
					"	    (case when off.since is null then null else greatest(0,timestampdiff(minute, off.since, currtime.ts)) end) "+
					"   end) odur "+
					", stopped.since stopped_time "+
					", off.since off_time "+
					"	from "+
					"	(select now() ts) currtime "+
					"	cross join vehicle "+
					"  join current_data on (current_data.vehicle_id = vehicle.id and current_data.attribute_id=0) "+
					"	left outer join "+
					"	(select ee.vehicle_id vehicle_id, ee.event_start_time since "+
					"	from engine_events ee join  "+
					"	(select vehicle_id, max(engine_events.event_start_time) eestart from engine_events "+ 
					"	 where rule_id in (1) "+
					"	 group by vehicle_id "+
					"	) mx on (ee.rule_id=1 and mx.vehicle_id = ee.vehicle_id and mx.eestart = ee.event_start_time) "+
					"	where ee.event_stop_time is null "+
					"	) stopped on (stopped.vehicle_id = vehicle.id) "+
					"	left outer join ( "+
					"	select ee.vehicle_id vehicle_id, ee.event_stop_time since "+
					"	from engine_events ee join  "+
					"	(select vehicle_id, max(engine_events.event_start_time) eestart from engine_events "+ 
					"	 where rule_id in (56) "+
					"	 group by vehicle_id "+
					"	) mx on (ee.rule_id=56 and mx.vehicle_id = ee.vehicle_id and mx.eestart = ee.event_start_time) "+
					"	where ee.event_stop_time is not null "+
					"	) off on (off.vehicle_id = vehicle.id) "+
					") stop_engine_off_since "
					;

	public static String g_summary_latest_out_message = "( select msg.vehicle_id, msg.message, (case when msg.acknowledge_date is not null then msg.acknowledge_date when msg.latest_try_date is not null then latest_try_date else msg.in_date end) status_change_date, msg.status  " +
			" from (select vehicle_id, max(in_date) dt from vehicle_messages  group by vehicle_id) mx"+ 
			" join vehicle_messages msg on (msg.vehicle_id = mx.vehicle_id and msg.in_date = mx.dt)  group by mx.vehicle_id  "+
			" ) summary_latest_out_message "
			;
	public static String g_summary_latest_in_message = "( select msg.vehicle_id,  msg.message, msg.record_time  " +
			" from (select vehicle_id, max(record_time) dt from vehicle_recvd_messages  group by vehicle_id) mx"+ 
			" join vehicle_recvd_messages msg on (msg.vehicle_id = mx.vehicle_id and msg.record_time = mx.dt) "+
			" ) summary_latest_in_message "
			;

	public static String g_summary_latest_trip_info = "(select ltp.vehicle_id, ltp.load_area_wait_in, ltp.load_gate_out, ltp.unload_area_wait_in, ltp.unload_gate_out, ltp.load_gate_op, ltp.unload_gate_op, ltp.load_gate_in, ltp.unload_gate_in, ltp.confirm_time "+
			" ,ch_de.challan_date, ch_de.challan_no, ch_de.gr_no_, ch_de.tripsheet_no_, ch_de.bill_party, ch_de.container_1_no, ch_de.container_2_no, ch_de.driver "+
			", ltp.id, ch_de.consignee,  ch_de.dest_city, ch_de.dest_state, ch_de.id challan_id, ch_de.from_location, ch_de.to_location, ch_de.invoice_distkm, ch_de.mrs_contact_details "+
			",lop.name load_name, uop.name unload_name "+
			",ch_de.trip_status, ch_de.load_status "+
			",ltp.load_material_guess, ltp.load_material_manual, ltp.unload_material_guess "+
			",ltp.unload_stop_based, ltp.movement_type "+
			" , ch_de.load_gross, ch_de.unload_gross, ch_de.load_tare, ch_de.unload_tare "+
			" from (select vehicle_id, max(combo_start) dt from trip_info group by vehicle_id) mx"+
			" join trip_info ltp on (ltp.vehicle_id = mx.vehicle_id and ltp.combo_start = mx.dt) "+
			" left outer join challan_details ch_de on (ltp.vehicle_id = ch_de.vehicle_id and ltp.id =  ch_de.trip_info_id) "+
			" left outer join op_station lop on (lop.id = ltp.load_gate_op) "+
			" left outer join op_station uop on (uop.id = ltp.unload_gate_op) "+
			" group by ch_de.challan_no, ltp.vehicle_id "+
			" ) summary_latest_trip_info "
			;
	public static String g_currentTPR = "(select ctp.vehicle_id, tp_record.tpr_id, tp_record.tpr_status, tp_record.mines_id, mines_details.name mines_name, tp_record.combo_start, tp_record.challan_date, tp_record.combo_end, tp_record.material_cat, tp_record.earliest_load_gate_in, tp_record.earliest_load_gate_out, tp_record.earliest_unload_gate_in, tp_record.earliest_unload_gate_out, tp_record.challan_date, tp_record.rf_challan_date, tp_record.plant_id "+
	 " from current_vehicle_tpr ctp left outer join tp_record on (ctp.tpr_id = tp_record.tpr_id) left outer join mines_details on (mines_details.id = tp_record.mines_id)) g_currentTPR "
	;

	public static String g_summary_latest_token = "( select token1.vehicle_id, token1.load_gate_in, token1.load_gate_out " +
			", token1.unload_gate_in, token1.unload_gate_out, token1.load_gross_in, token1.load_tare_value, token1.load_gross_value, token1.unload_gross_in" +
			", token1.unload_tare_value, token1.unload_gross_value, token1.combo_start, token1.combo_end, rscode.name area_code " +
			",rscode.name system_code ,rscode.name code_name,load_rscode.name load_system_code,load_rscode.name load_code_name ,unload_rscode.name unload_system_code, unload_rscode.name unload_code_name" +
	" from (select vehicle_id, max(combo_start) dt from rfid_trip  group by vehicle_id) mx"+ 
	" join rfid_trip token1 on (token1.vehicle_id = mx.vehicle_id and token1.combo_start = mx.dt) " +
	" left outer join rfid_opstation rscode on (rscode.id = (case when token1.load_op_gate is not null then token1.load_op_gate else token1.unload_op_gate end)) " +
	" left outer join rfid_info load_rscode on (load_rscode.id = token1.load_gross_rfid) " +
	" left outer join rfid_info unload_rscode on (unload_rscode.id = token1.unload_gross_rfid) "+
	" ) summary_latest_token "
	;
	public static String g_summary_latest_ch_info = "(select  ch_de.vehicle_id, "+
	" ch_de.challan_date, ch_de.consignor, ch_de.delivery_date, ch_de.challan_no, ch_de.gr_no_, ch_de.tripsheet_no_, ch_de.bill_party, ch_de.container_1_no, ch_de.container_2_no, ch_de.driver "+
	", ch_de.id, ch_de.consignee,  ch_de.dest_city, ch_de.dest_state, ch_de.trip_status, ch_de.id challan_id "+
	" from (select vehicle_id, max(challan_date) dt from challan_details where trip_status in (1) group by vehicle_id) mx"+
	" join challan_details ch_de on (ch_de.vehicle_id = mx.vehicle_id and ch_de.challan_date = mx.dt) "+
	" ) summary_latest_ch_info "
	;
	public static String g_summary_latest_critical_event = "(select engine_events.vehicle_id "+
	", max((case when engine_events.rule_id=502 then '9-Narrow Deviation' "+
	"           when engine_events.rule_id=1 and engine_events.event_begin_name like '[UZ%' then '8-UZ' "+
	"           when engine_events.rule_id=504 then '7-Full turn' "+
	"           when engine_events.rule_id = 564 then '6-Deviation' "+
	"           when engine_events.rule_id = 503 then '5-Wide Turn' "+
	"           else concat('4-',engine_events.rule_id) end)) rule_name "+
	" , engine_events.event_start_time, engine_events.event_stop_time, engine_events.event_begin_name "+
	" from " +
	" (select vehicle_id, max(combo_start) cst from trip_info group by vehicle_id) mxtrip left outer join "+
	" (select vehicle_id, max(event_start_time) est from engine_events where rule_id in (502,504,564,503) or (rule_id in (1) and event_begin_name like '[UZ%]') group by vehicle_id) mx on (mxtrip.vehicle_id = mx.vehicle_id) "+
	" left outer join engine_events on (engine_events.vehicle_id = mx.vehicle_id and engine_events.event_start_time = mx.est) "+
	" where mxtrip.load_gate_out < mx.est and (mxtrip.unload_gate_in is null or mxtrip.unload_gate_out > mx.est) "+
	" group by engine_events.vehicle_id,  engine_events.event_start_time, engine_events.event_stop_time, engine_events.begin_name "+
" ) summary_latest_critical_event "
;

	public static String g_summary_latest_critical_event_orig = "(select engine_events.vehicle_id, max((case when rules.id=502 then '9-Narrow Deviation' when rules.id=1 and engine_events.event_begin_name like '[UZ%' then '8-UZ' when rules.id=504 then '7-Full turn' when rules.id = 1 then '6-Stoppage' when rules.id = 503 then '5-Wide Turn' else concat(4,rules.name) end)) rule_name, engine_events.event_start_time, engine_events.event_stop_time, engine_events.event_begin_name from " +
			" (select vehicle_id, max(combo_start) cst from trip_info group by vehicle_id) mxtrip left outer join "+
			" (select vehicle_id, max(event_start_time) est from engine_events where criticality >= 4 group by vehicle_id) mx on (mxtrip.vehicle_id = mx.vehicle_id) "+
			" left outer join engine_events on (engine_events.vehicle_id = mx.vehicle_id and engine_events.event_start_time = mx.est) "+
			" left outer join rules on (rules.id = engine_events.rule_id) where mxtrip.cst < mx.est and rules.id in (1,502,503,504,564) group by engine_events.vehicle_id,  engine_events.event_start_time, engine_events.event_stop_time "+
	" ) summary_latest_critical_event "
	;

	public static String g_summary_latest_trip_info_for_join = "(select vehicle_id, max(combo_start) cst from trip_info group by vehicle_id) trip_info_ltp "
		;
	public static String g_summary_latest_challan_details_for_join = "(select vehicle_id, max(challan_date) cst from challan_details group by vehicle_id) challan_details_ltp "
		;
	public static String g_summary_latest_tp_record_for_join = "(select vehicle_id, max(latest_load_gate_in_out) cst from tp_record group by vehicle_id) tp_record_ltp "
		;
	public static String g_summary_latest_engine_event_for_join = "(select vehicle_id, rule_id, max(event_start_time) cst from engine_events group by vehicle_id, rule_id) engine_events_ltp "
		;

	public static String g_summary_last_trip_info_not_used_use_summary_latest_trip_info = " (select ltp.* from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
			" left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) " +
			" join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number)))) " +
			" vi on (vi.vehicle_id=vehicle.id) join (select vehicle_id, max(combo_start) dt from trip_info group by vehicle_id) mx on (mx.vehicle_id=vehicle.id) left outer join trip_info ltp on (ltp.vehicle_id = mx.vehicle_id " +
			" and ltp.combo_start = mx.dt) group by mx.vehicle_id  ) summary_last_trip_info "
			;
	
	public static String g_summary_unclosed_latest_trip_info = "(select ltp.vehicle_id, ltp.load_gate_out, ltp.unload_gate_out" +
	", ltp.load_gate_op, ltp.unload_gate_op, ltp.load_gate_in, ltp.unload_gate_in, ltp.confirm_time, " +
	" rec.id, rec.vehicle_id, rec.src_dest_item_id, rec.src_exit, rec.status, rec.cumm_dist_at_src_exit, rec.dest_entry_eta, rec.dest_entry_act," +
	" cds.gps_record_time ,src_dest_items.transit_time, src_dest_items.transit_dist "+
	" from (select vehicle_id, max(combo_start) dt from trip_info group by vehicle_id) mxtrip"+
	" join trip_info ltp on (ltp.vehicle_id = mxtrip.vehicle_id and ltp.combo_start = mxtrip.dt) "+
	" join eta_info rec on (ltp.vehicle_id = rec.vehicle_id)" +
	" join current_data cds on (rec.vehicle_id = cds.vehicle_id and cds.attribute_id=0) "+
	" join (select vehicle_id, max(src_exit) src_exit from eta_info where status in (1,3,4) group by vehicle_id) mx " +
	"  on (mx.vehicle_id = rec.vehicle_id and mx.src_exit = rec.src_exit and rec.status in (1,3,4)) " +
	" join src_dest_items on (src_dest_items.id = rec.src_dest_item_id)"+
	"  group by ltp.vehicle_id "+
	" ) summary_unclosed_latest_trip_info ";
	
	public static String g_summary_latest_trip_event_voilations_before_plant = "( " +
			" select vehicle_id , (case when rule_id in (482,483,484) then 1 else  " +
			" (case when trip_info_otherLU.gate_in is not null and trip_info_otherLU.gate_in >= load_gate_out and  " +
			" (case when coalesce(unload_gate_in, confirm_time) is not null then trip_info_otherLU.gate_in <= coalesce(unload_gate_in, confirm_time) else true end) then 1 else 0 end) end) " +
			" from  (select vehicle_id, max(combo_start) dt from trip_info group by vehicle_id) mxtrip"+
			" join trip_info on (trip_info.vehicle_id = mxtrip.vehicle_id and trip_info.combo_start = mxtrip.dt) " +
			" left outer join engine_events on (trip_info.load_gate_out is not null and event_start_time >= load_gate_out  " +
			" and (case when coalesce(unload_gate_in, confirm_time) is not null then event_start_time <= coalesce(unload_gate_in, confirm_time) end) " +
			" and engine_events.vehicle_id = trip_info.vehicle_id) left outer join trip_info_otherLU on (trip_info.id = trip_info_otherLU.trip_id) join op_station on (trip_info_otherLU.opstation_id <> op_station.id and op_station.name != 'MPL Coal Stock Yard')" +
			") summary_latest_trip_event_voilations_before_plant ";
	
	public static String g_summary_latest_trip_event_voilations_within_plant = "( " +
	" select vehicle_id , (case when (u_wb1in is null or u_wb2in is null or unload_area_in is null ) then 1 else " +
	" (case when (area_info_otherLU.gate_in is not null and u_wb2in is null and area_info_otherLU.gate_in < u_wb2in)" +
	" then 1 else 0 end) end)" +
	" from  (select vehicle_id, max(combo_start) dt from trip_info group by vehicle_id) mxtrip"+
	" join trip_info on (trip_info.vehicle_id = mxtrip.vehicle_id and trip_info.combo_start = mxtrip.dt) " +
	" left outer join area_info_otherLU on (trip_info.load_gate_out is not null and trip_info.id = area_info_otherLU.trip_id)" +
	" ) summary_latest_trip_event_voilations_within_plant ";

	public static String g_trip_load_lead_in_to_in = "left join logged_data linldx on ( linldx.vehicle_id = trip_info.vehicle_id  and linldx.attribute_id " +
	"= 0 and linldx.gps_record_time = coalesce(trip_info.load_area_in, trip_info.load_area_out, trip_info.load_gate_in,trip_info.load_gate_out,trip_info.combo_start)) left join logged_data linldy on ( linldy.vehicle_id = trip_info.vehicle_id  and linldy.attribute_id = 0 " +
	"and linldy.gps_record_time = coalesce(trip_info.unload_area_in,trip_info.unload_area_out, trip_info.unload_gate_in,trip_info.unload_gate_out,trip_info.confirm_time)) "+
	
	" left join logged_data uinldx on ( uinldx.vehicle_id = trip_info.vehicle_id  and uinldx.attribute_id " +
	"= 0 and uinldx.gps_record_time = coalesce(trip_info.unload_area_in, trip_info.unload_area_out, trip_info.unload_gate_in,trip_info.unload_gate_out,trip_info.confirm_time)) left join logged_data uinldy on ( uinldy.vehicle_id = trip_info.vehicle_id  and uinldy.attribute_id = 0 " +
	"and uinldy.gps_record_time = coalesce(trip_info.confirm_time, trip_info.combo_end)) ";

	
	public static String g_trip_load_lead = "left join logged_data ldx on ( ldx.vehicle_id = trip_info.vehicle_id  and ldx.attribute_id " +
			"= 0 and ldx.gps_record_time = coalesce(trip_info.load_area_in, trip_info.load_area_out, trip_info.load_gate_in,trip_info.load_gate_out,trip_info.combo_start)) left join logged_data ldy on ( ldy.vehicle_id = trip_info.vehicle_id  and ldy.attribute_id = 0 " +
			"and ldy.gps_record_time = coalesce(trip_info.unload_area_out, trip_info.unload_area_in, trip_info.unload_gate_out,trip_info.unload_gate_in)) ";
	
	public static String g_trip_round_lead = "left join logged_data rdx on ( rdx.vehicle_id = trip_info.vehicle_id  and rdx.attribute_id " +
	"= 0 and rdx.gps_record_time = coalesce(trip_info.load_area_in, trip_info.load_area_out, trip_info.load_gate_in,trip_info.load_gate_out, trip_info.combo_start)) left join logged_data rdy on ( rdy.vehicle_id = trip_info.vehicle_id  and rdy.attribute_id = 0 " +
	"and rdy.gps_record_time = trip_info.confirm_time) ";

	public static String g_trip_unload_lead = "left join logged_data udx on ( udx.vehicle_id = trip_info.vehicle_id  and udx.attribute_id " +
			"= 0 and udx.gps_record_time = trip_info.unload_gate_out) left join logged_data udy on ( udy.vehicle_id = trip_info.vehicle_id  and udy.attribute_id = 0 " +
			"and udy.gps_record_time = trip_info.confirm_time) ";

	
	public static String g_multitrip_unload_lead = " left join logged_data mudx on (mudx.vehicle_id = trip_info.vehicle_id and mudx.attribute_id=0 and mudx.gps_record_time = trip_info.load_gate_out) "+
			" left join logged_data mudy on ( mudy.vehicle_id = trip_info.vehicle_id  and mudy.attribute_id = 0 and mudy.gps_record_time = trip_info_otherLU.gate_in) "
			;
	public static String g_multitrip_load_unlead = " left join logged_data mudx on (mudx.vehicle_id = trip_info.vehicle_id and mudx.attribute_id=0 and mudx.gps_record_time = trip_info_otherLU.gate_in) "+
	" left join logged_data mudy on ( mudy.vehicle_id = trip_info.vehicle_id  and mudy.attribute_id = 0 and mudy.gps_record_time = (case when trip_info.unload_gate_in is null then trip_info.combo_end else trip_info.unload_gate_in end)) "
	;
    public static String g_trip_load_opstation = " left outer join op_station trip_load_opstation on (trip_load_opstation.id = trip_info.load_gate_op) ";
    public static String g_trip_unload_opstation = " left outer join op_station trip_unload_opstation on (trip_unload_opstation.id = trip_info.unload_gate_op) ";
	public static String g_trip_multilu_summary =  " (select trip_info.id trip_id, sum(case when opstation_id <> load_gate_op and  is_load=1 then 1 else 0 end) other_load_count, "+
			" sum(case when opstation_id <> unload_gate_op and is_load=0 then 1 else 0 end) other_unload_count, "+
			" sum(case when opstation_id <> load_gate_op and is_load=1 and dir_change <> 0 then 1 else 0 end) other_possible_load_count, "+
			" sum(case when opstation_id <> unload_gate_op and is_load=0 and dir_change <> 0 then 1 else 0 end) other_possible_unload_count, "+
			" sum(case when opstation_id <> load_gate_op and is_load=1 then Timestampdiff(MINUTE, gate_in, gate_out) else 0 end) other_load_time, "+
			" sum(case when opstation_id <> unload_gate_op and is_load=0 then Timestampdiff(MINUTE, gate_in, gate_out) else 0 end) other_unload_time, "+
			" sum(case when opstation_id <> load_gate_op and is_load=1 and dir_change <> 0 then Timestampdiff(MINUTE, gate_in, gate_out) else 0 end) other_possible_load_time, "+
			" sum(case when opstation_id <> unload_gate_op and is_load=0 and dir_change <> 0 then Timestampdiff(MINUTE, gate_in, gate_out) else 0 end) other_possible_unload_time "+
			" from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id)  "+
			" join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id  in (@pv123)) "+ 
			" join trip_info on (trip_info.vehicle_id = vehicle.id)  "+
			" join trip_info_otherLU on (trip_info.id = trip_info_otherLU.trip_id) "+
			" group by trip_info.id) trip_multilu_summary ";
			
public static String g_inner_shift_based_trip_start_end = 
		" select shift_table.id item_id, shift_table.start_time sst, shift_table.label label, trip_info.vehicle_id vehicle_id "+
		" , min(case when load_gate_out is null then unload_gate_in else load_gate_out end) start_time, max(case when unload_gate_in is null then load_gate_out else unload_gate_in end) end_time "+
		" , timestampdiff(MINUTE, shift_table.start_time, min(load_gate_out)) delayed_start_dur "+
		" , timestampdiff(MINUTE, max(unload_gate_in), shift_table.end_time) early_end_dur "+
		" from "+
		" (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+ 
		" join	  "+
		" shift_table on (shift_table.port_node_id = @pv123 and shift_table.start_time >= '@user_start' and shift_table.start_time <= '@user_end') "+
		" join trip_info on (trip_info.vehicle_id = vi.vehicle_id and(( trip_info.load_gate_out is not null and trip_info.load_gate_out >= shift_table.start_time and trip_info.load_gate_out <= shift_table.end_time) "+
		" or ( trip_info.load_gate_out is  null and trip_info.unload_gate_in >= shift_table.start_time and trip_info.unload_gate_in <= shift_table.end_time))) "+
		" group by shift_table.id, shift_table.start_time, shift_table.label, trip_info.vehicle_id "
		;

	public static String g_trip_start_end_summ = "(select call_ee.id item_id, shiftdetail.vehicle_id, min(shiftdetail.start_time) start_time, max(shiftdetail.end_time) end_time, sum(shiftdetail.delayed_start_dur) delayed_start_dur, sum(shiftdetail.early_end_dur) early_end_dur "+
			" from "+
			" (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+VEHICLE_FILTER+" ) vi "+
			" join @period call_ee on (call_ee.start_time >= '@start_period' and call_ee.start_time <= '@end_period' @shift_port ) "+
			" join ("+g_inner_shift_based_trip_start_end +") shiftdetail on (shiftdetail.sst >= call_ee.start_time and shiftdetail.sst < call_ee.end_time and shiftdetail.vehicle_id = vi.vehicle_id) "+
			" group by call_ee.id, shiftdetail.vehicle_id) g_trip_start_end_summ "
			;
    public static String g_vehicle_maint_summary = " ((select 1 as src,vehicle_maint.id ticket_id, vehicle_maint.vehicle_id vehicle_id, vehicle_maint.planned_start planned_start, vehicle_maint.planned_end planned_end, " +
    		" vehicle_maint.actual_start actual_start, vehicle_maint.actual_end actual_end, vehicle_maint.veh_maint_cause veh_maint_cause, null as total_cost,vehicle_maint.status ticket_status, " +
    		" vehicle_maint.created_on create_on,vehicle_maint.updated_on updated_on, null as labour_cost, null as mat_cost,vehicle_maint_services.service_cost service_cost,service_item.name service_name, " +
    		" (case when vehicle_maint.veh_maint_total_cost is not null then ((TimeStampDiff(MINUTE,(case when vehicle_maint.actual_start is null then vehicle_maint.planned_start else vehicle_maint.actual_start end)," +
    		" (case when vehicle_maint.actual_end is null then vehicle_maint.planned_end else vehicle_maint.actual_end end))*vehicle_maint_services.service_cost )/vehicle_maint.veh_maint_total_cost) else 0 end) service_time, " +
    		" null as part_cost,null as part_cat,null as part_name,null as part_replacement_time,vehicle_maint_services.id as vehicle_maint_services_id,null as vehicle_maint_part_replacement_id from vehicle_maint join vehicle_maint_services on (vehicle_maint.id=vehicle_maint_services.ticket_id) join service_item on (vehicle_maint_services.service_item_id=service_item.id)" +
    		" where vehicle_maint.actual_start >= '@start_period' and vehicle_maint.actual_start <= '@end_period' ) " +
    		" union " +
    		" (select 2 as src,vehicle_maint.id ticket_id, vehicle_maint.vehicle_id vehicle_id, vehicle_maint.planned_start planned_start, vehicle_maint.planned_end planned_end, vehicle_maint.actual_start actual_start, " +
    		" vehicle_maint.actual_end actual_end, vehicle_maint.veh_maint_cause veh_maint_cause, null as total_cost,vehicle_maint.status ticket_status, vehicle_maint.created_on create_on, " +
    		" vehicle_maint.updated_on updated_on, null as labour_cost, null mat_cost,null,null,null,vehicle_maint_part_replacement.cost part_cost,vehicle_maint_part_replacement.part_cat part_cat, " +
    		" inventory_product.item_name part_name,(case when vehicle_maint.veh_maint_total_cost is not null then ((TimeStampDiff(MINUTE, (case when vehicle_maint.actual_start is null then " +
    		" vehicle_maint.planned_start else vehicle_maint.actual_start end),(case when vehicle_maint.actual_end is null then vehicle_maint.planned_end else vehicle_maint.actual_end end))*vehicle_maint_part_replacement.cost )/vehicle_maint.veh_maint_total_cost) else 0 end)  part_replacement_time,null as vehicle_maint_services_id,vehicle_maint_part_replacement.id as vehicle_maint_part_replacement_id " +
    		" from vehicle_maint join vehicle_maint_part_replacement on (vehicle_maint.id=vehicle_maint_part_replacement.ticket_id) left outer join inventory_product on (vehicle_maint_part_replacement.part_code=inventory_product.item_code)" +
    		" where vehicle_maint.actual_start >= '@start_period' and vehicle_maint.actual_start <= '@end_period' )" +
    		" union  " +
    		" (select 3 as src,vehicle_maint.id ticket_id, vehicle_maint.vehicle_id vehicle_id, vehicle_maint.planned_start planned_start, vehicle_maint.planned_end planned_end, vehicle_maint.actual_start actual_start,  vehicle_maint.actual_end actual_end, vehicle_maint.veh_maint_cause veh_maint_cause," +
    		"  vehicle_maint.veh_maint_total_cost total_cost,vehicle_maint.status ticket_status, vehicle_maint.created_on create_on,  vehicle_maint.updated_on updated_on, vehicle_maint.other_cost labour_cost, vehicle_maint.other_mat_cost mat_cost,null,null,null,null,null,null,null,null,null  from vehicle_maint" +
    		" where vehicle_maint.actual_start >= '@start_period' and vehicle_maint.actual_start <= '@end_period'  )" +
    		") vehicle_maint_summary ";
    
    public static String g_maint_part_cycle_summ ="(select maint_part.part_replacement_id,maint_part.vehicle_id,maint_part.ticket_id,maint_part.work_type,maint_part.part_code,maint_part.part_sr_no, maint_part.part_cat,sum(maint_part.odometer) odometer,maint_part.old_part_status,inventory_product.life std_life " +
    		" from (select vehicle_maint.vehicle_id,vehicle_maint_part_replacement.id part_replacement_id,vehicle_maint_part_replacement.ticket_id,vehicle_maint_part_replacement.work_type, " +
    		" vehicle_maint_part_replacement.part_code,vehicle_maint_part_replacement.part_sr_no, vehicle_maint_part_replacement.part_cat,(case when old_part.odometer is not null then (old_part.odometer-vehicle_maint.odometer) else GET_DISTANCE_IN_INTERVAL(vehicle_maint.actual_start,'@end_period', vehicle_maint.vehicle_id) end) odometer ,(case when old_part.part_status is null then 3 else old_part.part_status end) old_part_status  " +
    		" from vehicle_maint join vehicle_maint_part_replacement on  (vehicle_maint_part_replacement.ticket_id=vehicle_maint.id) left outer join (select old_part_log.part_replacement_id,vehicle_maint.vehicle_id,old_part_log.ticket_id,old_part_log.part_sr_no,vehicle_maint.odometer,vehicle_maint.engine_hr,old_part_log.part_status from old_part_log join vehicle_maint on (vehicle_maint.id=old_part_log.ticket_id)  " +
    		" where vehicle_maint.actual_start >= '@start_period' and vehicle_maint.actual_start <='@end_period' order by vehicle_maint.actual_start desc) old_part on (old_part.part_sr_no = vehicle_maint_part_replacement.part_sr_no and old_part.vehicle_id = vehicle_maint.vehicle_id) where vehicle_maint.actual_start >= '@start_period' and vehicle_maint.actual_start <='@end_period' order by vehicle_maint.actual_start desc)  maint_part left " +
    		" outer join inventory_product on (inventory_product.item_code=maint_part.part_code) group by maint_part.part_replacement_id,maint_part.part_sr_no,maint_part.part_code,maint_part.part_cat)  " +
    		" maint_part_cycle_summ";
    public static String g_maint_service_item_summ ="(select maint_part.part_replacement_id,maint_part.vehicle_id,maint_part.ticket_id,maint_part.work_type,maint_part.part_code,maint_part.part_sr_no, maint_part.part_cat,sum(maint_part.odometer) odometer,maint_part.old_part_status,inventory_product.life std_life " +
    		" from (select vehicle_maint.vehicle_id,vehicle_maint_part_replacement.id part_replacement_id,vehicle_maint_part_replacement.ticket_id,vehicle_maint_part_replacement.work_type, " +
    		" vehicle_maint_part_replacement.part_code,vehicle_maint_part_replacement.part_sr_no, vehicle_maint_part_replacement.part_cat,(case when old_part.odometer is not null then (old_part.odometer-vehicle_maint.odometer) else GET_DISTANCE_IN_INTERVAL(vehicle_maint.actual_start,'@end_period', vehicle_maint.vehicle_id) end) odometer ,(case when old_part.part_status is null then 3 else old_part.part_status end) old_part_status  " +
    		" from vehicle_maint join vehicle_maint_part_replacement on  (vehicle_maint_part_replacement.ticket_id=vehicle_maint.id) left outer join (select old_part_log.part_replacement_id,vehicle_maint.vehicle_id,old_part_log.ticket_id,old_part_log.part_sr_no,vehicle_maint.odometer,vehicle_maint.engine_hr,old_part_log.part_status from old_part_log join vehicle_maint on (vehicle_maint.id=old_part_log.ticket_id)  " +
    		" where vehicle_maint.actual_start >= '@start_period' and vehicle_maint.actual_start <='@end_period' order by vehicle_maint.actual_start desc) old_part on (old_part.part_sr_no = vehicle_maint_part_replacement.part_sr_no and old_part.vehicle_id = vehicle_maint.vehicle_id) where vehicle_maint.actual_start >= '@start_period' and vehicle_maint.actual_start <='@end_period' order by vehicle_maint.actual_start desc)  maint_part left " +
    		" outer join inventory_product on (inventory_product.item_code=maint_part.part_code) group by maint_part.part_replacement_id,maint_part.part_sr_no,maint_part.part_code,maint_part.part_cat)  " +
    		" maint_part_cycle_summ";
    public static String g_lipl_distance_summary = "(select mimx.vehicle_id, sum(case when (tin.ol_sub_type in (101) or tin.ou_sub_type in (101)) then (lgd2.attribute_value-lgd1.attribute_value)*1.05 else 0 end) dist," +
    		" sum(case when (tin.ol_sub_type in (101) or tin.ou_sub_type in (101)) then 1 else 0 end) lipl_trips," +
    		" count(*) total_trips " +
    		" from ( "+
    		" select logged_data.vehicle_id, logged_data.attribute_id, min(gps_record_time) mi,max(gps_record_time) mx from "+
    		" logged_data lgd1  where 1=1 and lgd1.attribute_id=0 " +
    		" and lgd1.gps_record_time between '@start_period' and '@end_period' "+
    		" and lgd1.vehicle_id in (@VEHICLE_ID_LIST) "+
    		" group by lgd1.vehicle_id, attribute_id) mimx "+
    		" join (select vi.vehicle_id, t1.load_gate_in, t1.load_gate_out, t1.unload_gate_in, t1.unload_gate_out, t1.confirm_time,oL.id ol_id,oL.sub_type ol_sub_type,ou.id ou_id,ou.sub_type ou_sub_type from vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) " +
    		" left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number)))) vi on vi.vehicle_id = vehicle.id join trip_info t1 on (t1.vehicle_id=vehicle.id) left outer join op_station oL " +
    		" on (oL.id = t1.load_gate_op) left outer join op_station ou on (ou.id = t1.unload_gate_op) where 1=1  and (t1.combo_end >= '@start_period' and (t1.combo_start <= '@end_period' or t1.combo_start >= '@start_period')) ) tin on " +
    		" (mimx.vehicle_id = tin.vehicle_id and coalesce(tin.unload_gate_out,tin.unload_gate_in, tin.confirm_time) >= mimx.mi and coalesce(tin.load_gate_in,tin.unload_gate_out,tin.unload_gate_in,tin.unload_gate_out, tin.confirm_time) <= mimx.mx) " +
    		" join logged_data lgd1 on (lgd1.vehicle_id=mimx.vehicle_id and lgd1.attribute_id=0 and lgd1.gps_record_time = greatest(mimx.mi, coalesce(tin.load_gate_in,tin.load_gate_out,tin.unload_gate_in,tin.unload_gate_out, tin.confirm_time))) " +
    		" join logged_data lgd2 on (lgd2.vehicle_id=mimx.vehicle_id and lgd2.attribute_id=0 and lgd2.gps_record_time = least(mimx.mx, coalesce(tin.unload_gate_out,tin.unload_gate_in, tin.confirm_time))) group by mimx.vehicle_id) " +
    		" lipl_distance_summary";
    
    public static String g_non_lipl_distance_summary = "(select mimx.vehicle_id, sum(lgd2.attribute_value-lgd1.attribute_value)*1.05 dist from "+
    " (select vehicle_id, attribute_id, min(gps_record_time) mi,max(gps_record_time) mx from "+
    " logged_data lgd1  where 1=1 and lgd1.attribute_id=0 and lgd1.gps_record_time between '@start_period' and '@end_period' "+
    " and lgd1.vehicle_id in (@VEHICLE_ID_LIST) "+
    " group by lgd1.vehicle_id, lgd1.attribute_id) mimx join logged_data lgd1 on (lgd1.vehicle_id=mimx.vehicle_id and lgd1.attribute_id=0 " +
    		" and lgd1.gps_record_time = mimx.mi) join logged_data lgd2 on (lgd2.vehicle_id=mimx.vehicle_id and lgd2.attribute_id=0 and lgd2.gps_record_time = mimx.mx) group by mimx.vehicle_id) " +
    		" non_lipl_distance_summary ";
    
    /*public static String g_summaryIdlePeriodTripEvent = "(  select call_ee.id as item_id, vi.vehicle_id vehicle_id  , coalesce(confirm_ee.load_area_wait_in,confirm_ee.load_gate_in) trip_confirm_time , " +
    		" sum((case when data_ee.rule_id=1 and call_ee.load_gate_out is not null then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.load_gate_out, coalesce(call_ee.unload_gate_in,'@user_end') , '@user_start', '@user_end') else null end) ) as stop_dur_load_lead, " +
    		" sum((case when data_ee.rule_id=1 and call_ee.unload_gate_out is not null then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.unload_gate_in, coalesce(confirm_ee.load_area_wait_in,confirm_ee.load_gate_in,'@user_end'), '@user_start', '@user_end') else null end) ) as stop_dur_return_lead , sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.combo_start, coalesce(confirm_ee.load_area_wait_in,confirm_ee.load_gate_in), '@user_start', '@user_end') else null end) ) as stop_dur_total , (case when call_ee.load_gate_out is null then null else (Timestampdiff(minute, GREATEST(call_ee.load_gate_out,'@user_start'), LEAST(coalesce(call_ee.unload_gate_in,'@user_end'),'@user_end') ) - sum((case when data_ee.rule_id=1 and call_ee.load_gate_out is not null then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.load_gate_out, coalesce(call_ee.unload_gate_in,'@user_end') , '@user_start', '@user_end') else 0 end) ) ) end) as move_dur_load_lead ,  " +
    		" (case when call_ee.load_gate_out is null then null else (Timestampdiff(minute, GREATEST(call_ee.load_gate_out,'@user_start'), LEAST(coalesce(call_ee.unload_gate_in,'@user_end'),'@user_end') ) -  sum((case when data_ee.rule_id=1 and call_ee.unload_gate_out is not null then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.unload_gate_in, coalesce(confirm_ee.load_area_wait_in,confirm_ee.load_gate_in,'@user_end'), '@user_start', '@user_end') else 0 end) ) ) end) as move_dur_return_lead       , " +
    		" (case when call_ee.combo_start is null then null else (Timestampdiff(minute, GREATEST(call_ee.combo_start,'@user_start'), LEAST(coalesce(coalesce(confirm_ee.load_area_wait_in,confirm_ee.load_gate_in),'@user_end'),'@user_end') ) - sum((case when data_ee.rule_id=1 then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.combo_start,  coalesce(confirm_ee.load_area_wait_in,confirm_ee.load_gate_in), '@user_start', '@user_end') else 0 end) ) ) end) as move_dur_total " +
    		" from  (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi  " +
    		" join trip_info call_ee on (call_ee.combo_start >= '@user_start' and call_ee.combo_start <= '@user_end' and vi.vehicle_id = 		call_ee.vehicle_id) left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (1)and ((data_ee.event_start_time >= '@user_start' and data_ee.event_start_time <= '@user_end') or (data_ee.event_stop_time is null or (data_ee.event_stop_time >= '@user_start' and data_ee.event_stop_time <= '@user_end'  ) ) and ((data_ee.event_start_time >= call_ee.combo_start and data_ee.event_start_time <= call_ee.combo_end) or (data_ee.event_stop_time >= call_ee.combo_start and data_ee.event_stop_time <= call_ee.combo_end) ) ) ) left outer join trip_info confirm_ee on (confirm_ee.vehicle_id = call_ee.vehicle_id and call_ee.next_trip_id = confirm_ee.id)  group by call_ee.id, vi.vehicle_id, call_ee.combo_start) " +
    		" summary_idle_period_trip_event ";*/
    public static String g_summaryIdlePeriodTripEvent = "( select dur_matrix.item_id,dur_matrix.vehicle_id,dur_matrix.trip_confirm_time,dur_matrix.stop_dur_load_lead,dur_matrix.stop_dur_return_lead,(dur_matrix.stop_dur_load_lead+dur_matrix.stop_dur_return_lead ) stop_dur_total,(dur_matrix.load_lead-dur_matrix.stop_dur_load_lead) move_dur_load_lead ,(dur_matrix.return_lead-dur_matrix.stop_dur_return_lead) move_dur_return_lead,((dur_matrix.load_lead-dur_matrix.stop_dur_load_lead)+(dur_matrix.return_lead-dur_matrix.stop_dur_return_lead)) move_dur_total  from (  select call_ee.id as item_id, vi.vehicle_id vehicle_id  , coalesce(confirm_ee.load_area_wait_in,confirm_ee.load_gate_in) trip_confirm_time , " +
    		" sum((case when data_ee.rule_id=1 and call_ee.load_gate_out is not null then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.load_gate_out, coalesce(call_ee.unload_gate_in,'@user_end') , '@user_start', '@user_end') else 0 end) ) as stop_dur_load_lead, " +
    		" sum((case when data_ee.rule_id=1 and call_ee.unload_gate_out is not null then GET_DURATION(data_ee.event_start_time,data_ee.event_stop_time,call_ee.unload_gate_out, coalesce(confirm_ee.load_area_wait_in,confirm_ee.load_gate_in,'@user_end'), '@user_start', '@user_end') else 0 end) ) as stop_dur_return_lead ,Timestampdiff(minute, GREATEST(call_ee.load_gate_out,'@user_start'), LEAST(coalesce(call_ee.unload_gate_in,'@user_end'),'@user_end'))  load_lead,Timestampdiff(minute, GREATEST(call_ee.unload_gate_out,'@user_start'), LEAST(coalesce(confirm_ee.load_area_wait_in,confirm_ee.load_gate_in,'@user_end'),'@user_end') )return_lead " +
    		" from  (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi  " +
    		" join trip_info call_ee on (call_ee.combo_start >= '@user_start' and call_ee.combo_start <= '@user_end' and vi.vehicle_id = 		call_ee.vehicle_id) left outer join engine_events data_ee on (vi.vehicle_id = data_ee.vehicle_id and data_ee.rule_id in (1)and ((data_ee.event_start_time >= '@user_start' and data_ee.event_start_time <= '@user_end') or (data_ee.event_stop_time is null or (data_ee.event_stop_time >= '@user_start' and data_ee.event_stop_time <= '@user_end'  ) ) and ((data_ee.event_start_time >= call_ee.combo_start and data_ee.event_start_time <= call_ee.combo_end) or (data_ee.event_stop_time >= call_ee.combo_start and data_ee.event_stop_time <= call_ee.combo_end) ) ) ) left outer join trip_info confirm_ee on (confirm_ee.vehicle_id = call_ee.vehicle_id and call_ee.next_trip_id = confirm_ee.id)  group by call_ee.id, vi.vehicle_id, call_ee.combo_start) dur_matrix) " +
    		" summary_idle_period_trip_event ";
    
    public static void putIfHasAtPeriodStuff(QueryParts qp) {
    	putIfHasAtPeriodStuff(qp, qp.m_selClause);
    	putIfHasAtPeriodStuff(qp, qp.m_fromClause);
    	putIfHasAtPeriodStuff(qp, qp.m_whereClause);
    	putIfHasAtPeriodStuff(qp,qp.m_groupByClause);
    	putIfHasAtPeriodStuff(qp,qp.m_orderByClause);
    }
    public static void putIfHasAtPeriodStuff(QueryParts qp, StringBuilder sb) {
    	if (sb.indexOf("@indep_period") > 0)
    		qp.isDrivenByPeriod = true;    	
    	if (sb.indexOf("@PERIOD_") > 0)
    		qp.hasAtPeriodCol = true;    	
    	if (sb.indexOf("@VEHICLE_ID_LIST") > 0)
    		qp.hasAtVehicleListQ = true;
    }
    public static class HelperPeriodItemList {
    	int itemId;
    	String label;
    	long startTime;
    	long endTime;
    	public HelperPeriodItemList(int itemId, String label, long startTime, long endTime) {
    		this.itemId = itemId;
    		this.label = label;
    		this.startTime = startTime;
    		this.endTime = endTime;
    	}
    	public Date getMonthStDate() {
    		java.util.Date dt = new java.util.Date(startTime);
    		dt.setDate(1);
    		return dt;
    	}
    }
    public ArrayList<HelperPeriodItemList> getPeriodsToDrive(Connection conn, SessionManager session, SearchBoxHelper searchBoxHelper) {
    	ArrayList<HelperPeriodItemList> retval = new ArrayList<HelperPeriodItemList>();
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		int pv123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
    		Date now = new Date(System.currentTimeMillis());
    		String granParamName = searchBoxHelper == null ? "pv20051": searchBoxHelper.m_topPageContext+"20051";
    		int granDesired = Misc.getParamAsInt(session.getParameter(granParamName));
    		
    		String period = m_shift_check ? "shift_table" : granDesired == Misc.SCOPE_WEEK ? "week_table" : granDesired == Misc.SCOPE_MONTH ? "month_table" : granDesired == Misc.SCOPE_ANNUAL ? "year_table" : granDesired == Misc.SCOPE_SHIFT ? 
    				"shift_table" : granDesired == Misc.SCOPE_HOUR ? "hour_table" : granDesired == Misc.SCOPE_HOUR_RELATIVE ? "(select date_add(now(), INTERVAL -60 MINUTE) start_time, now() end_time, 1 id, concat(date(now()),' ', hour(now()), 'hr') label, null port_node_id) "
    						: granDesired == Misc.SCOPE_USER_PERIOD ?  "(select @user_start start_time, @user_end end_time, 1 id, @user_start label, null port_node_id) "
    						: "day_table";
    		StringBuilder sb = new StringBuilder();
    		sb.append("select start_time, end_time, id, label from ").append(period);
    		sb.append(" where start_time between ? and  ? ");
    		sb.append(" or start_time  between date(?) and  date(?) ");
    		boolean needsPv123 = false;
    		if ("shift_table".equals(period)) {
    			//getShift relevant portNode
    			ps = conn.prepareStatement("select anc.id from  port_nodes anc join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join shift_table on (anc.id = shift_table.port_node_id) order by anc.lhs_number desc limit 1");
    			ps.setInt(1, pv123);
    			rs = ps.executeQuery();
    			if (rs.next()) {
    				pv123 = rs.getInt(1);
    			}
    			else {
    				pv123 = Misc.getUndefInt();
    			}
    			rs = Misc.closeRS(rs);
    			ps = Misc.closePS(ps);
    			if (!Misc.isUndef(pv123)) {
    				sb.append(" and shift_table.port_node_id = ?");
    				needsPv123  = true;
    			}
    		}
    		sb.append(" order by start_time ");
    		ps = conn.prepareStatement(sb.toString());
    		ps.setTimestamp(1, Misc.utilToSqlDate(startDt == null ? now : startDt));
    		ps.setTimestamp(2, Misc.utilToSqlDate(endDt == null ? now : endDt));
    		ps.setTimestamp(3, Misc.utilToSqlDate(startDt == null ? now : startDt));
    		ps.setTimestamp(4, Misc.utilToSqlDate(endDt == null ? now : endDt));
    		if (needsPv123)
    			ps.setInt(5, pv123);
    		rs = ps.executeQuery();
    		while (rs.next()) {
    			//start_time, end_time, id, label
    	    	HelperPeriodItemList entry = new HelperPeriodItemList(rs.getInt(3), rs.getString(4), Misc.sqlToLong(rs.getTimestamp(1)), Misc.sqlToLong(rs.getTimestamp(2)));
    	    	retval.add(entry);
    	    }
    		rs = Misc.closeRS(rs);
    		ps = Misc.closePS(ps);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	finally {
    		rs = Misc.closeRS(rs);
    		ps = Misc.closePS(ps);
    	}
    	return retval;
    }
    public static String getVehicleListDesiredCSV(Connection conn, String vehicleFilter, String pv123)  {//
		String q = "select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in ("+pv123+") and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) " +
		((vehicleFilter != null && vehicleFilter.indexOf("vehicle") > 0) ? vehicleFilter : "")
				;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder retval = new StringBuilder();
		try {
			ps = conn.prepareStatement(q);
			 rs = ps.executeQuery();
			
			while (rs.next()) {
				if (retval.length() != 0)
					retval.append(",");
				retval.append(rs.getInt(1));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		if (retval.length() == 0)
			retval.append(Misc.getUndefInt());
		return retval.toString();
	}
    
    public StringBuilder helperFixSummaryWithCurrParam(StringBuilder strbuf, SessionManager session, SearchBoxHelper searchBoxHelper, HashMap<String, String> tList, ArrayList<ArrayList<DimConfigInfo>> searchCriteria, int subQIndex, int driverObject, boolean dontChangeStartEndPeriod, String vehicleListCSV) {
		//lazy approach
		String str = strbuf.toString();
		String pv123 = Integer.toString(Misc.getParamAsInt(session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT));
		if (str.indexOf("@VEHICLE_ID_LIST") > 0) {
			if (vehicleListCSV == null)
				vehicleListCSV = Integer.toString(Misc.getUndefInt());
			str = str.replaceAll("@VEHICLE_ID_LIST", vehicleListCSV);			
		}
		if (str.indexOf("@indep_period") >= 0) {
			str = str.replaceAll("@indep_period", "@period");
		}
		if (str.indexOf("@hier_level") >= 0) {
			String hl = session.getAttribute(searchBoxHelper.m_topPageContext+81041);
			if (hl != null)
				hl = hl.trim();
			int hiv = Misc.getParamAsInt(hl);
			if (Misc.isUndef(hiv) || hiv == Misc.G_HACKANYVAL) {
				hl = "1";
			}
			else {
				
			}
			str = str.replaceAll("@hier_level", hl);
		}
		
		String dataEEList = "1,56,3,4,5,227,243,327,328,319,130,538";
		Date now = new Date(System.currentTimeMillis());
		String granParamName = searchBoxHelper == null ? "pv20051": searchBoxHelper.m_topPageContext+"20051";
		int granDesired = Misc.getParamAsInt(session.getParameter(granParamName));
		str = fixForFlexDate(str, searchBoxHelper, session, tList, searchCriteria, subQIndex);
		String period = m_shift_check ? "shift_table" : granDesired == Misc.SCOPE_WEEK ? "week_table" : granDesired == Misc.SCOPE_MONTH ? "month_table" : granDesired == Misc.SCOPE_ANNUAL ? "year_table" : granDesired == Misc.SCOPE_SHIFT ? 
				"shift_table" : granDesired == Misc.SCOPE_HOUR ? "hour_table" : granDesired == Misc.SCOPE_HOUR_RELATIVE ? "(select date_add(now(), INTERVAL -60 MINUTE) start_time, now() end_time, 1 id, concat(date(now()),' ', hour(now()), 'hr') label, null port_node_id) "
						: granDesired == Misc.SCOPE_USER_PERIOD ?  "(select @user_start start_time, @user_end end_time, 1 id, @user_start label, null port_node_id) "
						: "day_table";
        if(m_shift_check){
        	str = str.replaceAll("@shift_port", " and call_ee.port_node_id = @pv123 and call_ee.label "+m_shiftStr);
        }
        else if ( granDesired == Misc.SCOPE_SHIFT ){
			str = str.replaceAll("@shift_port", " and call_ee.port_node_id = @pv123 ");
		} else {
			str = str.replaceAll("@shift_port", "");
		}
		//str = str.replaceAll("@allop", "op_station");
        
		str = str.replaceAll("@pv123", pv123);

		str = str.replaceAll("@data_ee_list", dataEEList);
		SimpleDateFormat indepDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Date startDate = startDt == null ? null : new Date(startDt.getTime());
		if (startDate != null && granDesired != Misc.SCOPE_SHIFT) //HACK ... todo properly for SCOPE_SHIFT
			TimePeriodHelper.setBegOfDate(startDate, granDesired);
		Date endDate = endDt == null ? null : new Date(endDt.getTime());
		/*if (endDate != null && granDesired != Misc.SCOPE_HOUR&& !"tr_ana_genevent".equalsIgnoreCase(session.getParameter("page_context")) && granDesired != Misc.SCOPE_SHIFT) //HACK ... todo properly for SCOPE_SHIFT
			TimePeriodHelper.setBegOfDate(endDate, granDesired);*/
		if (endDate == null || endDate.after(now))
			endDate = now;
		String st = startDt == null ? null : indepDateFormat.format(startDt);
		String en = endDt == null ? null : indepDateFormat.format(endDt);
		if (str.indexOf("@day_start") >= 0 && endDt != null) {
			Date temp =TimePeriodHelper.getBegOfDate(endDt, Misc.SCOPE_DAY);
			str = str.replaceAll("@day_start", indepDateFormat.format(temp));
		}
		if (str.indexOf("@mon_start") >= 0 && endDt != null) {
			Date temp =TimePeriodHelper.getBegOfDate(endDt, Misc.SCOPE_MONTH);
			str = str.replaceAll("@mon_start", indepDateFormat.format(temp));
		}
		if (str.indexOf("@day_end") >= 0 && endDt != null) {
			Date temp =TimePeriodHelper.getBegOfDate(endDt, Misc.SCOPE_DAY);
			TimePeriodHelper.addScopedDur(temp, Misc.SCOPE_DAY, 1);
			str = str.replaceAll("@day_end", indepDateFormat.format(temp));
		}
		if (str.indexOf("@mon_end") >= 0 && endDt != null) {
			Date temp =TimePeriodHelper.getBegOfDate(endDt, Misc.SCOPE_MONTH);
			TimePeriodHelper.addScopedDur(temp, Misc.SCOPE_MONTH, 1);
			str = str.replaceAll("@mon_end", indepDateFormat.format(temp));
		}
		if (startDate != null && !dontChangeStartEndPeriod)
			str = str.replaceAll("@start_period", indepDateFormat.format(startDate) ); // begining of period
		if (endDate != null && !dontChangeStartEndPeriod)
			str = str.replaceAll("@end_period",  indepDateFormat.format(endDate));
		boolean hasPeriod = str.indexOf("@period") >= 0;
		String replForENDPERIOD = hasPeriod ? "@period.end_time" :"'@user_end'";
		str = str.replaceAll("@END_PERIOD", replForENDPERIOD);
		if (hasPeriod) {
			str = str.replaceAll("@period", period);
		}
		if ( granDesired == Misc.SCOPE_SHIFT){
			str = str.replaceAll("shift.name", "shift_table.label");
		}
		if (st != null) {
			int pvVal = Misc.getParamAsInt(session.getParameter("duration"));
			if (!Misc.isUndef(pvVal) && pvVal<0) {//if duration <0 then subtract from start time
				String rpStr = " date_add('" + st + "', INTERVAL " + pvVal + " "
						+ Misc.getParamAsString(session.getParameter("unit"))+") ";
				//str.replace(indx, indx+18,rpStr);
				str = str.replaceAll("'@full_user_start'", rpStr);
			} else {
				str = str.replaceAll("@full_user_start", st);
			}

		}
		if (en != null) {
			int pvVal = Misc.getParamAsInt(session.getParameter("duration"));
			if (!Misc.isUndef(pvVal) && pvVal>0) {
				String rpStr = " date_add('" + en + "', INTERVAL " + pvVal + " "
						+ Misc.getParamAsString(session.getParameter("unit"))+") ";
				str = str.replaceAll("'@full_user_end'", rpStr);
			} else {
				str = str.replaceAll("@full_user_end", en);
			}

		}
		
		if (st != null)
			str = str.replaceAll("@user_start", st);
		if (en != null)
			str = str.replaceAll("@user_end", en);
		
		str = str.replaceAll("@user", Long.toString(session.getUserId()));
		if (str.indexOf("@workflow_type") >= 0) {
			int ofObject = driverObject;
			if (ofObject == WorkflowHelper.G_WORKFLOWS) {
				ofObject = Misc.getParamAsInt(session.getParameter("object_type"));
				if (Misc.isUndef(ofObject))
					ofObject = WorkflowHelper.G_OBJ_TPRECORD;
			}
			ArrayList<Integer> allWorkflowTypes = WorkflowDef.getWorkflowIdsForObjectType(ofObject,0);
			String wkfType = InputTemplate.getCleanedString(session.getParameter("pv416"));
			if (wkfType == null && searchBoxHelper != null)
				wkfType = InputTemplate.getCleanedString(session.getParameter(searchBoxHelper.m_topPageContext+416));
			if (wkfType == null && searchBoxHelper != null)
				wkfType = InputTemplate.getCleanedString(session.getParameter(searchBoxHelper.m_topPageContext+402));
			if (allWorkflowTypes != null && allWorkflowTypes.size() > 0) {
				if (wkfType == null) {
					StringBuilder tsb = new StringBuilder();
					Misc.convertInListToStr(allWorkflowTypes, tsb);
					wkfType = tsb.toString();
					if (wkfType.length() == 0)
						wkfType = null;
				}
				else {
					ArrayList<Integer> askedWkf = new ArrayList<Integer>();
					Misc.convertValToVector(wkfType, askedWkf);
					for (int i1=askedWkf.size()-1;i1>=0;i1--) {
						boolean found = false;
						for (int j1=0,j1s=allWorkflowTypes.size();j1<j1s;j1++) {
							if (allWorkflowTypes.get(j1).intValue() == askedWkf.get(i1).intValue()) {
								found = true;
								break;
							}
						}
						if (!found)
							askedWkf.remove(i1);
					}
					if (askedWkf.size() != 0) {
						StringBuilder tsb = new StringBuilder();
						Misc.convertInListToStr(askedWkf, tsb);
						wkfType = tsb.toString();
						if (wkfType.length() == 0)
							wkfType = null;
					}
				}
			}
			if (wkfType != null)
				str = str.replaceAll("@workflow_type", wkfType);
		}
		return new StringBuilder(str);


	}
    public static String fixForFlexDate(String query, SearchBoxHelper searchBoxHelper, SessionManager session, HashMap<String, String> tList, ArrayList<ArrayList<DimConfigInfo>> searchCriteria, int subQIndex) {
    	for (OrderedTimeTableInfo tableWithDateCol : gTableWithDateOrdered) {
    		String tab = tableWithDateCol.tabName;
    		String col = tableWithDateCol.colName;
    		//if (!"tp_record".equals(tab) && (tList == null || !tList.containsKey(tab)))//"tp_record - hack ...daily_target_plan ... we really should be searching for @tab. and if found go forw 
    		//	continue;
    		if (query.indexOf("@"+tab+".") < 0)
    			continue;
    		if (tableWithDateCol.flexEventSearch >= 0) {
    			
    			int dimOfFlex = tableWithDateCol.flexEventSearch;
    			if (subQIndex >= 0 && searchCriteria != null) {
    				if (!FrontPageInfo.isSearchDimForSubQ(searchCriteria, tableWithDateCol.flexEventSearch, subQIndex))
    					dimOfFlex = tableWithDateCol.flexEventSearchAlt; 
    			}
    			int d20061 = Misc.getParamAsInt(session.getParameter(searchBoxHelper.m_topPageContext+Integer.toString(dimOfFlex)));
    			if (d20061 < 0) {
    				
    			}
    			String flexCol = tableWithDateCol.getFlexEventDateCol();
    			
    			DimInfo dim = DimInfo.getDimInfo(tableWithDateCol.flexEventSearch);
    			String eventTabName = tableWithDateCol.tabName;
    			String eventColName = null;
    			String orEventColName = null;
    			if (dim != null) {
    				DimInfo.ValInfo v = dim.getValInfo(d20061);
    				if (v != null) {
    					eventColName = v.getOtherProperty("col");
    					orEventColName = v.getOtherProperty("or_col");
    					String tempt = v.getOtherProperty("table");
    					if (tempt != null && tempt.length() > 0)
    						eventTabName = tempt;
    				}
    			}
    			if (eventColName == null)
    				eventColName = tableWithDateCol.flexEventDefaultCol;
    			query = helperFixForFlexTripInfo(query, flexCol, d20061, eventColName, orEventColName, eventTabName);
    		}
    	}
    	return query;
    }
    
    public static String helperFixForFlexTripInfo(String query, String pattern,  int d20061, String eventColName, String orEventColName, String eventTabName) {
    	int pos = 0;
    	int patternLen = pattern.length();
    	StringBuilder repl = new StringBuilder();
    	StringBuilder relOp = new StringBuilder();
    	StringBuilder rhs = new StringBuilder();
    	String tab = pattern.substring(1, pattern.indexOf("."));
    	tab = eventTabName;
    	while (true) {
    		pos = query.indexOf(pattern, pos);
    		if (pos < 0)
    			break;
    		repl.setLength(0);
    		relOp.setLength(0);
    		rhs.setLength(0);
    		int i=pos+patternLen;
    		int sz = query.length();
    		boolean seenSpace = false;
    		for (;i<sz;i++) {
    			char ch=query.charAt(i);
    			if (Character.isSpace(ch)) {
    				seenSpace = true;
    				continue;
    			}
    			else if (ch == '<' || ch == '=' || ch == '>' || ch == '!') {
    				relOp.append(ch); 
    			}
    			else if (seenSpace && (ch == 'b' || ch == 'B')) {
    				String partial = query.substring(i, i+"between".length());
    				if (partial.equalsIgnoreCase("BETWEEN")) {
    					relOp.append(partial);
    					i += "between".length()-1; //++ happens later
    				}
    			}
    			else
    				break;
    		}
    		boolean seenQuoteStart = false;
    		for (;i<sz;i++) {
    			char ch=query.charAt(i);
    			if (ch == '\'') {
    				if (seenQuoteStart) {
    					rhs.append(ch);
    					i++;
    					break;
    				}
    				else {
    					seenQuoteStart = true;
    				}
    			}
    			if (Character.isSpace(ch) && !seenQuoteStart)
    				break;
    			rhs.append(ch);
    		}
    		if (relOp.length() == 0) {
    			repl.append(tab).append(".").append(eventColName);
    			String actPattern = pattern;
    			String replStr = repl.toString();
    			query = query.replace(actPattern, replStr);
    			pos += replStr.length();
    		}
    		else if (relOp.length() > 0 && rhs.length() > 0) {
    			if (orEventColName != null) {
	    			repl.append(" (").append(tab).append(".").append(eventColName).append(" ").append(relOp).append(" ").append(rhs)
					.append(" or ").append(tab).append(".").append(orEventColName).append(" ").append(relOp).append(" ").append(rhs)
					.append(") ");
    			}
    			else {
    				if(!eventColName.contains("coalesce"))
    					repl.append(tab).append(".");
    				repl.append(eventColName).append(" ").append(relOp).append(" ").append(rhs);
    			}
    			String actPattern = query.substring(pos, i);
    			String replStr = repl.toString();
    			query = query.replace(actPattern, replStr);
    			pos += replStr.length();
    		}
    	}
    	return query;
    }
    public static String helperFixForFlexTripInfoOrig(String query, String pattern,  int d20061) {
    	int pos = 0;
    	int patternLen = pattern.length();
    	StringBuilder repl = new StringBuilder();
    	StringBuilder relOp = new StringBuilder();
    	StringBuilder rhs = new StringBuilder();
    	String tab = pattern.substring(1, pattern.indexOf("."));
    	while (true) {
    		pos = query.indexOf(pattern, pos);
    		if (pos < 0)
    			break;
    		repl.setLength(0);
    		relOp.setLength(0);
    		rhs.setLength(0);
    		int i=pos+patternLen;
    		int sz = query.length();
    		boolean seenSpace = false;
    		for (;i<sz;i++) {
    			char ch=query.charAt(i);
    			if (Character.isSpace(ch)) {
    				seenSpace = true;
    				continue;
    			}
    			else if (ch == '<' || ch == '=' || ch == '>' || ch == '!') {
    				relOp.append(ch); 
    			}
    			else if (seenSpace && (ch == 'b' || ch == 'B')) {
    				String partial = query.substring(i, i+"between".length());
    				if (partial.equalsIgnoreCase("BETWEEN")) {
    					relOp.append(partial);
    					i += "between".length()-1; //++ happens later
    				}
    			}
    			else
    				break;
    		}
    		boolean seenQuoteStart = false;
    		for (;i<sz;i++) {
    			char ch=query.charAt(i);
    			if (ch == '\'') {
    				if (seenQuoteStart) {
    					rhs.append(ch);
    					i++;
    					break;
    				}
    				else {
    					seenQuoteStart = true;
    				}
    			}
    			if (Character.isSpace(ch) && !seenQuoteStart)
    				break;
    			rhs.append(ch);
    		}
    		if (relOp.length() == 0) {
    			if (Misc.isUndef(d20061)) {
    				repl.append(tab).append(".shift_date");
    			}
    			else if (d20061 == 0) {
    				repl.append(tab).append(".combo_start");
    			}
    			else if (d20061 == 2) {
    				repl.append(tab).append(".shift_date");
    			}
    			else if (d20061 == 2 || d20061 == 4) {
    				repl.append(tab).append(".load_gate_in");
    			}
    			else if (d20061 == 3) {
    				repl.append(tab).append(".load_gate_out");
    			}
    			else if (d20061 == 5 || d20061 == 7) {
    				repl.append(tab).append(".unload_gate_in");
    			}
    			else if (d20061 == 6) {
    				repl.append(tab).append(".unload_gate_out");
    			}
    			String actPattern = pattern;
    			String replStr = repl.toString();
    			query = query.replace(actPattern, replStr);
    			pos += replStr.length();
    		}
    		else if (relOp.length() > 0 && rhs.length() > 0) {
    			if (Misc.isUndef(d20061)) {
    				repl.append(tab).append(".shift_date").append(" ").append(relOp).append(" ").append(rhs);
    			}
    			else if (d20061 == 0) {
    				repl.append(tab).append(".combo_start").append(" ").append(relOp).append(" ").append(rhs);
    			}
    			else if (d20061 == 2) {
    				repl.append(tab).append(".shift_date").append(" ").append(relOp).append(" ").append(rhs);
    			}
    			else if (d20061 == 2) {
    				repl.append(tab).append(".load_gate_in").append(" ").append(relOp).append(" ").append(rhs);
    			}
    			else if (d20061 == 3) {
    				repl.append(tab).append(".load_gate_out").append(" ").append(relOp).append(" ").append(rhs);
    			}
    			else if (d20061 == 4) {
    				repl.append(" (").append(tab).append(".load_gate_in").append(" ").append(relOp).append(" ").append(rhs)
    				.append(" or ").append(tab).append(".load_gate_out").append(" ").append(relOp).append(" ").append(rhs)
    				.append(") ");
    			}
    			else if (d20061 == 5) {
    				repl.append(tab).append(".unload_gate_in").append(" ").append(relOp).append(" ").append(rhs);
    			}
    			else if (d20061 == 6) {
    				repl.append(tab).append(".unload_gate_out").append(" ").append(relOp).append(" ").append(rhs);
    			}
    			else if (d20061 == 7) {
    				repl.append(" (").append(tab).append(".unload_gate_in").append(" ").append(relOp).append(" ").append(rhs)
    				.append(" or ").append(tab).append(".unload_gate_out").append(" ").append(relOp).append(" ").append(rhs)
    				.append(") ");
    			}
    			String actPattern = query.substring(pos, i);
    			String replStr = repl.toString();
    			query = query.replace(actPattern, replStr);
    			pos += replStr.length();
    		}
    	}
    	return query;
    }
	public void hackGetSkipParameters(ArrayList<DimConfigInfo> dimConfigList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SessionManager session, SearchBoxHelper searchBoxHelper, QueryParts qp, HashMap<String,Integer> nameIndexLookup) {
		String sname = searchBoxHelper == null ? "pv20394": searchBoxHelper.m_topPageContext+"20394";
		qp.m_hackSkipTimeGap = Misc.getParamAsInt(session.getParameter(sname));
		sname = searchBoxHelper == null ? "pv20395": searchBoxHelper.m_topPageContext+"20395";
		qp.m_hackSkipDistGap = Misc.getParamAsDouble(session.getParameter(sname));
		Integer tempInt = nameIndexLookup.get("d20134");  // gps_record_time
		if (tempInt != null)
			qp.m_timeColIndex = tempInt;
		tempInt = nameIndexLookup.get("d20161"); // Attribute Value
		if (tempInt != null)
			qp.m_distColIndex = tempInt;
		tempInt = nameIndexLookup.get("d20274");   //vehicle id
		if (tempInt != null)
			qp.m_vehicleIdColIndex = tempInt;
	}
	public QueryParts buildQueryParts (ArrayList<DimConfigInfo> dimConfigList, ArrayList<ArrayList<DimConfigInfo>> searchBox,
			boolean fpi_m_hackTrackDriveTimeTableJoinLoggedData, SessionManager session, SearchBoxHelper searchBoxHelper,
			ResultInfo.FormatHelper formatHelper, HashMap<String,Integer> nameIndexLookup, ArrayList<Integer> orderIds, ProcessShowResult processShowResult, int externalAliasedTableIndex, int rollupAtJava, boolean doDimsLoadFromDBOnly, int pageLevelDoOrgBasedTiming) {
		return buildQueryParts (dimConfigList, searchBox,
				fpi_m_hackTrackDriveTimeTableJoinLoggedData, session, searchBoxHelper,
				formatHelper, nameIndexLookup, getDriverObjectFromName("vehicle"), orderIds, processShowResult, false, externalAliasedTableIndex, rollupAtJava, doDimsLoadFromDBOnly, pageLevelDoOrgBasedTiming);
	}
	
	public QueryParts buildQueryParts (ArrayList<DimConfigInfo> dimConfigList, ArrayList<ArrayList<DimConfigInfo>> searchBox,
			boolean fpi_m_hackTrackDriveTimeTableJoinLoggedData, SessionManager session, SearchBoxHelper searchBoxHelper,
			ResultInfo.FormatHelper formatHelper, HashMap<String,Integer> nameIndexLookup, int driverObject, ArrayList<Integer> orderIds, ProcessShowResult processShowResult, boolean excludeNonDriver
			, int externalAliasedTableIndex, int rollupAtJava, boolean doDimsLoadFromDBOnly, int pageLevelDoOrgBasedTiming) {
		//rollupAtJava = -1 //dont do rollup even if asked .. to be used for inner queries
		//rollupAtJava = 0 // do rollup in query
		//rollupAtJava = 1 //do rollup in java
		//A few hacks mostly dealing with handling of 'datetime' of item being asked and joining of trip_info, [trip_info_otherLU] engine_events, logged_data
		//Here is the gist -
		//if there are more than one of these tables being used then the natural order of join is trip->engine event->logged data collared by
		//engine_event's start/end being inbetween trip's combo_start/combo_end and logged_data's gps_record_time bein between
		//engine_event's start/end
		//if However fpi.m_hackTrackDriveTimeTableJoinLoggedData is true then the join is driven by logged_data -> engine_event->trip_info
		// and time is in between granularity adjusted date match
		//
		//Further more 20023/20034 resp stand for generic start/end and depending upon the first table driving the join, will refer to datetime field of
		//that table
		//20023 and 20113 and 20257 refer the datetime field of the appropriate first table driving the join
		//
		//TODO - driver, shift name and granularity by shift will only work if the driving table is trip_info
		//
		////0 dont do regardless of global parameter, 1 do regardless of global parameter 2 only if global parameter says yes
		boolean doOrgTimingBased = pageLevelDoOrgBasedTiming == 0 ? false : pageLevelDoOrgBasedTiming == 1 ? true : Misc.g_doOrgAssignmentTimeBased;  
		if (!Misc.g_hasvehicle_org_time_assignments)
			doOrgTimingBased = false;
			String externalAliasedTable = externalAliasedTableIndex < 0 ? null : "_t"+externalAliasedTableIndex;
		boolean doUnvisitedHack=false;
		QueryParts qp = new QueryParts();
		qp.m_doRollupAtJava = 1 == rollupAtJava; 
		qp.m_fromClause.append(from);
		qp.m_selClause.append(sel);
		WorkflowHelper.TableObjectInfo tableInfo = WorkflowHelper.getTableInfo(driverObject);
		qp.m_groupByClause.append(grp);
		qp.m_rollupClause.append(rollup);
		qp.m_havingClause.append(having);
		StringBuilder vehicleFilterStr = new StringBuilder(whr);

		//moved to later ... where we are smarter about date field to use
		//qp.m_whereClause.append(getWhrQuery(qp, session, searchBox, searchBoxHelper));
		StringBuilder whereClauseFromSel = new StringBuilder();
		StringBuilder fromClauseTemp = new StringBuilder();
		StringBuilder joinClauseFromDim = new StringBuilder();
		String lastGranTable = null;
		HashMap<String, String> tList = new HashMap<String, String>();
		HashMap<String, String> cList = new HashMap<String, String>();
		if (tableInfo != null)
			tList.put(tableInfo.getName(), tableInfo.getName());
		//	tList.put("vehicle_access_groups", "vehicle_access_groups");

		boolean doingMySQL = DBQueries.isMySql;
		int granDesired = 10;//raw
		boolean hackNeedsDriver = false; //TODO - currently driven off trip info table
		boolean onlyCommcommTab = false;
		String tempName = searchBoxHelper == null ? "pv21416": searchBoxHelper.m_topPageContext+"21416";
		String tempVal = session.getParameter(tempName);
		if (tempVal == null)
			tempVal = session.getParameter("v21416");
		int doIncludeLatestTrip = Misc.getParamAsInt(tempVal);
		//first get the time tables needed in the query
		boolean hasOtherLU = false;
		ArrayList<String> orderyByForLimit = null;
		hackGetSkipParameters(dimConfigList, searchBox, session, searchBoxHelper, qp, nameIndexLookup);
		String tripChallanDriverParamName = searchBoxHelper == null ? "pv21196" : searchBoxHelper.m_topPageContext+"21196";
		int tripChallanDriver = Misc.getParamAsInt(session.getParameter(tripChallanDriverParamName), 0);
		String granParamName = searchBoxHelper == null ? "pv20051": searchBoxHelper.m_topPageContext+"20051";
		granDesired = Misc.getParamAsInt(session.getParameter(granParamName));
		if(!Misc.isUndef(granDesired)){
			qp.m_hasGranularity = true;
			qp.m_desiredGranularity = granDesired;
		}
		
		for (int i=0,is=dimConfigList.size();i<is;i++){
			DimConfigInfo dimConfig = dimConfigList.get(i);
			if (dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null) {
				continue;
			}
			if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.table == null || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.length() == 0 || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.equals("Dummy")) {
				/*if(dimConfig.m_dimCalc.m_dimInfo.m_id == 60980 && gTableWithDateOrdered.size() > 5){
					OrderedTimeTableInfo tpr = gTableWithDateOrdered.remove(1);
					OrderedTimeTableInfo trip = gTableWithDateOrdered.remove(3);
					gTableWithDateOrdered.add(1, trip);
					gTableWithDateOrdered.add(3, tpr);
				}*/
				continue;
			}
			if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.equals("allop")) {
				doUnvisitedHack = true;
				qp.m_fromClause.setLength(0);
				qp.m_fromClause.append(g_fdhs_unvisited_from_part);
				tList.put("allop", "allop");
				tList.put("allopmap", "allopmap");

				tList.put("op_station", "op_station");
				tList.put("trip_info", "trip_info");
				tList.put("vehicle", "vehicle");
			}
			if (dimConfig.m_granularity || dimConfig.m_dimCalc.m_dimInfo.m_id == 20113 || dimConfig.m_dimCalc.m_dimInfo.m_id == 20257)
				continue;
			if(dimConfig.m_dimCalc.m_dimInfo.m_id == 20207)
				onlyCommcommTab = true;
			ColumnMappingHelper colMap = dimConfig.m_dimCalc.m_dimInfo.m_colMap;
			String tab = colMap.table;
			if (tab != null) {
				if (tab.equals("workstationSummary")&& m_workstation_check) {
					int from_size = qp.m_fromClause.length();
					qp.m_fromClause.delete(5,from_size);
					m_workstation_check = false;
				}
				if (tab.startsWith("opStat")&& m_opStation_check) {//TODO_NEW_APPROACH
					int from_size = qp.m_fromClause.length();
					qp.m_fromClause.delete(5,from_size);
					qp.m_fromClause.append(opOrgList);
					//gTableList.put("org_opstation", "org_opstation");
					m_opStation_check = false;
				}
				/*if (tab.startsWith("mat")&& m_mat_check) {//TODO_NEW_APPROACH
					int from_size = qp.m_fromClause.length();
					qp.m_fromClause.delete(5,from_size);
					qp.m_fromClause.append(g_material_list);
					//gTableList.put("material_list", "material_list");
					m_mat_check = false;
				}*/
				if ("Singleton".equals(tab)) {
					tab = colMap.base_table;
					if (tab == null || tab.length() == 0 || tab.equals("Dummy"))
						tab = "trip_info";
					if ("$Counter".equals(tab)) {
						tab = "Singleton";
					}
				}
				else if (tab.startsWith("summary_period") || tab.equals("g_trip_start_end_summ") || tab.equals("summary_2_period_trip") || tab.equals("event_based_movement"))
					tab = "@period";
				else if (tab.startsWith("summary_event"))
					tab = "engine_events";
				else if (tab.startsWith("summary_breakdown"))
					tab = "vehicle_maint";
				else if (tab.equals("g_vehicle_maint_summary") || tab.equals("g_maint_part_cycle_summ") || tab.equalsIgnoreCase("g_maint_service_item_summ")){
					tab = "vehicle_maint";
				}
				
				/*else if (tab.startsWith("summary_trip"))
					tab = "trip_info";*/
				else if ("trip_info_ext".equals(tab))
				{
					m_trip_info_ext_check = true;
				}
			}
			
			if (gTableList.containsKey(tab)) {
				tList.put(tab, tab);
			}
		}
		//now get the 1st driving table
		String firstTableUsedInTimeTableJoin = null;
		String colForFirstTableUsedInTimeTableJoin = null;
		
		if (!fpi_m_hackTrackDriveTimeTableJoinLoggedData) {//see comments at the beginning of the function
			for (int i=0,is=gTableWithDateOrdered.size();i<is;i++) {
				if (tList.containsKey(gTableWithDateOrdered.get(i).tabName)) {
					firstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(i).tabName;
					colForFirstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(i).colName;
					break;
				}
			}
		}
		else {
			for (int i=gTableWithDateOrdered.size()-1; i>=0; i--) {
				if (tList.containsKey(gTableWithDateOrdered.get(i).tabName)) {
					firstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(i).tabName;
					colForFirstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(i).colName;
					break;
				}
			}
		}
//		if(tripChallanDriver == 2 && gTableWithDateOrdered != null && gTableWithDateOrdered.get(1) != null){ // 2 => TPR
//			firstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(1).tabName;
//			colForFirstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(1).colName;
//		}else 
		boolean hasTPR = tList.containsKey("tp_record");
		if(tripChallanDriver == 0 && hasTPR){  //0 => Trip
			firstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(4).tabName;
			colForFirstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(4).colName;
		}
		
		boolean hasTrip = tList.containsKey("trip_info");
		boolean hasChallan = tList.containsKey("challan_details") || tList.containsKey("tp_record") || tList.containsKey("rfid_trip");
		if(!hasTrip && hasTPR){  //0 => Trip
			firstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(1).tabName;
			colForFirstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(1).colName;
		}
		if (driverObject == G_OBJ_TPRECORD || driverObject == WorkflowHelper.G_OBJ_CHALLAN || driverObject == WorkflowHelper.G_OBJ_RFIDTRIP)
			tripChallanDriver = 1;
		if (!hasChallan)
			tripChallanDriver = 0;
		else if (hasChallan && !hasTrip)
			tripChallanDriver = 1;
		if (tripChallanDriver != 0)
			doIncludeLatestTrip = 0; //DEBUG13 TODO dont know for challan
		
		//REPLACE_NEW if (tripChallanDriver == 0 && "challan_details".equals(firstTableUsedInTimeTableJoin) ) {
		//REPLACE_NEW 	firstTableUsedInTimeTableJoin = "trip_info";
		//REPLACE_NEW	colForFirstTableUsedInTimeTableJoin = "shift_date";
		//REPLACE_NEW }
		//end of getting time tables
		
		if (tripChallanDriver == 0 && "challan_details".equals(firstTableUsedInTimeTableJoin)) {
			OrderedTimeTableInfo firstDriverInfo = gTableList.get("trip_info");
			firstTableUsedInTimeTableJoin = firstDriverInfo.tabName;
			colForFirstTableUsedInTimeTableJoin = firstDriverInfo.colName;
		}
		if (tableInfo != null && gTableList.containsKey(tableInfo.getName())) {
			OrderedTimeTableInfo firstDriverInfo = gTableList.get(tableInfo.getName());
			firstTableUsedInTimeTableJoin = firstDriverInfo.tabName;
			colForFirstTableUsedInTimeTableJoin = firstDriverInfo.colName;
			if (firstDriverInfo.hasVehicleIdFK)
				tList.put("vehicle", "vehicle");
		}
		int adjDriverObject = driverObject;
		if ("1".equals(session.getParameter("_report_on_freeze")))  {
			if ("engine_events".equals(firstTableUsedInTimeTableJoin)) {
				qp.frozenBaseTable = "engine_events";
				//adjDriverObject = WorkflowHelper.G_FREEZE_EE;
			}
			else if ("trip_info".equals(firstTableUsedInTimeTableJoin)) {
				qp.frozenBaseTable = "trip_info";
				//adjDriverObject = WorkflowHelper.G_FREEZE_TI;
			}
		}
		
		String orgStr = CopyOfGeneralizedQueryBuilder.doDriverObjectJoin(adjDriverObject, tList, doOrgTimingBased);
		if (orgStr != null) {
			qp.m_fromClause.append(qp.m_fromClause.equals(from) || qp.m_fromClause.equals("from") || qp.m_fromClause.equals("from") ? " " : ",").append(orgStr);
		}
		//now get the sel cols
		boolean hackNeedsShift = false;
		boolean hackNeedsRatio = false;
		boolean hackNeedTripLead = true;
		//TODO - make date format
		boolean hasVirtualTable = false;
		WorkflowHelper.TableObjectInfo driverTableObjectInfo = WorkflowHelper.getTableInfo(driverObject);
		for (int i=0,is=dimConfigList.size();i<is;i++){
			DimConfigInfo dimConfig = dimConfigList.get(i);
			boolean dimNeedsRollup = dimConfig.m_doRollupTotal && rollupAtJava != -1;
			if (dimNeedsRollup)
				qp.m_needsRollup = true;
			if(dimConfig != null && dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null && dataListToShow.containsKey(dimConfig.m_dimCalc.m_dimInfo.m_id)){
				qp.groupByRollupAggIndicator.add(dimNeedsRollup ? 2 : 1);
				continue;
			}
			if (doDimsLoadFromDBOnly && (dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null || dimConfig.m_dimCalc.m_dimInfo.m_loadFromDB != 1)) {
				if (false) {//dont unnecessarily add null stuff IF MADE TRUE move below if part
					qp.m_selClause.append("null");
					qp.groupByRollupAggIndicator.add(dimNeedsRollup ? 2 : 1);
					if (externalAliasedTable != null)
						qp.m_selClause.append(" as _c").append(i);
				}
				continue;
			}
			if (!qp.m_selClause.toString().equals(sel))
				qp.m_selClause.append(", ");
	
			if (dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null) {
				qp.m_selClause.append("null");
				qp.groupByRollupAggIndicator.add(dimNeedsRollup ? 2 : 1);
				if (externalAliasedTable != null)
					qp.m_selClause.append(" as _c").append(i);
				continue;
			}
			if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.table == null || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.length() == 0 || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.equals("Dummy")) {
				qp.m_selClause.append("null");
				qp.groupByRollupAggIndicator.add(dimNeedsRollup ? 2 : 1);
				if (externalAliasedTable != null)
					qp.m_selClause.append(" as _c").append(i);
				continue;
			}
			if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.startsWith("f$")) {
				hasVirtualTable = true;
				qp.m_selClause.append("null");
				qp.groupByRollupAggIndicator.add(dimNeedsRollup ? 2 : 1);
				if (externalAliasedTable != null)
					qp.m_selClause.append(" as _c").append(i);
				continue;
			}
			
			ColumnMappingHelper colMap = dimConfig.m_dimCalc.m_dimInfo.m_colMap;
			if (excludeNonDriver && colMap.table != null && driverTableObjectInfo != null) {
				boolean toCont = true;
				String tab = colMap.table;
				if (tab.equals("_in_template_workflows") || tab.equals("_in_template_workflows_inner")) {
					toCont = false;
				}
				else if (tab.equals(driverTableObjectInfo.getName())) {
					toCont = false;
				}
				else {
					ArrayList<String> tablist = driverTableObjectInfo.getExtendedTables();
					for (int t3=0,t3s=tablist.size(); t3<t3s; t3++) {
						if (tab.equals(tablist.get(t3))) {
							toCont= false;
							break;
						}
					}
				}
				if (toCont) {
					qp.m_selClause.append("null");
					qp.groupByRollupAggIndicator.add(dimNeedsRollup ? 2 : 1);
					if (externalAliasedTable != null)
						qp.m_selClause.append(" as _c").append(i);
					continue;					
				}
				
			}
			
			if (dimConfig.m_orderBy) {
				if (qp.m_orderByClause.length() > 0 ){
					qp.m_orderByClause.append(",");
				}
				
				String tabPrefix = colMap == null || colMap.useColumnOnlyForName ? "" : colMap.table+"."; 
				qp.m_orderByClause.append(" ").append(tabPrefix).append(colMap.column);
				if ( !dimConfig.m_orderByTag && qp.m_orderByClause.length() != 0  ){
					qp.m_orderByClause.append(" desc ");
				}
				//if ( !dimConfig.m_orderByTag && qp.m_orderByClauseAsec.length() != 0  ){
				//	qp.m_orderByClauseAsec.append(" desc ");
				//}
			}

			String aggregateOp = null;
			String adjColName = null;
			boolean isTimeDim = false;
			
			if (dimConfig.m_granularity || dimConfig.m_dimCalc.m_dimInfo.m_id == 20113 || dimConfig.m_dimCalc.m_dimInfo.m_id == 20257) {
				granParamName = searchBoxHelper == null ? "pv20051": searchBoxHelper.m_topPageContext+"20051";
				granDesired = Misc.getParamAsInt(session.getParameter(granParamName));
				//				if("tr_ana_trip_detail".equalsIgnoreCase(session.getParameter("page_context")) && ShiftPlanInfo.getShiftInfo(session.getParameter("pv123") , new Date(), session.getConnection()) != null)
				if("tr_ana_trip_detail".equalsIgnoreCase(session.getParameter("page_context")) && session.getParameter("pv123") != null && "22".equalsIgnoreCase(session.getParameter("pv123")))
					granDesired = 6;
				//some thing to get the granularity adjusted colName that will adjColName
				//needShiftTableJoin = granDesired == 6;
				FmtI.Date dateFormatter = (FmtI.Date) formatHelper.getFormatter(i);
				String sqlDatePattern = Misc.convertJavaDataFormatToMySQL(dateFormatter == null ? Misc.G_DEFAULT_DATE_FORMAT : dateFormatter.getPattern());
				adjColName = getGranBasedString(firstTableUsedInTimeTableJoin, colForFirstTableUsedInTimeTableJoin, granDesired, sqlDatePattern);
				isTimeDim = true;
//				if(dimConfig.m_granularity){
//					qp.m_hasGranularity = true;
//					qp.m_desiredGranularity = granDesired;
//				}
			}
			int granOfFirstPeriod = "@period".equals(firstTableUsedInTimeTableJoin) ? granDesired : -1; 
			if (dimConfig.m_aggregate) {//copied .. ProcessForShowResult
				aggregateOp = helperGetAggregateOp(dimConfig, searchBoxHelper, session);
			}
			
			//     public boolean appendTableColName(StringBuilder selClause, StringBuilder qJoinClause, StringBuilder groupClause, StringBuilder andClause, String aggregateOp, String useThisForColumnName, StringBuilder orderByClause, boolean doingMySQL, String orderingInfo,boolean skipGroupBy,boolean addJoinCluase, int ytdScopeAgg, String dateColForYtd, String endPeriodForYtd) { //retrns true if added to groupClause
			int dimconfigYtdScope = dimConfig.getYtdScope();
			int endScopeForYTD = dimConfig.getEndScopeForYtd();
			String timeColForYtd = null;
			if (granOfFirstPeriod >= 0 && (Misc.isLHSHigherScope(granOfFirstPeriod, dimconfigYtdScope) || granOfFirstPeriod == dimconfigYtdScope)) {
				dimconfigYtdScope = -1;
				endScopeForYTD = -1;
			}
			if (dimconfigYtdScope >= 0) {
				String tabToCheck = colMap.table;
				if ("Singleton".equals(colMap.table)) {
					tabToCheck = colMap.base_table;
					if (tabToCheck == null || "Dummy".equals(tabToCheck))
						tabToCheck = "trip_info";
				}
				if (gTableList.containsKey(tabToCheck)) {
					OrderedTimeTableInfo timeInfo =gTableList.get(tabToCheck);
					timeColForYtd = timeInfo.getFlexEventDateCol();
				}
				
				if (driverTableObjectInfo != null && timeColForYtd == null && gTableList.containsKey(driverTableObjectInfo.getName())) {
					OrderedTimeTableInfo timeInfo =gTableList.get(driverTableObjectInfo.getName());
					timeColForYtd = timeInfo.getFlexEventDateCol();	
				}
				if (timeColForYtd == null) {//check base table if this is an extended table
					WorkflowHelper.TableObjectInfo baseTabInfo = WorkflowHelper.g_tableObjectInfoForExtended.get(tabToCheck);
					if (baseTabInfo != null && baseTabInfo.getId() > 1) {//0 =veh, 1 = driver, handled elsewhere
						 String baseTab = baseTabInfo.getName();
						 if (gTableList.containsKey(baseTab)) {
							OrderedTimeTableInfo timeInfo =gTableList.get(baseTab);
							timeColForYtd = timeInfo.getFlexEventDateCol();
						 }
					}
				}
				qp.addYtdScope(dimconfigYtdScope);
				if (endScopeForYTD >= 0)
					qp.addYtdScope(endScopeForYTD);
				
			}
			//if aliased, add to aliased order by if it is not inner or if inner then not hidden
			boolean addedInGroupBy = colMap.appendTableColName(qp.m_selClause,joinClauseFromDim, qp.m_groupByClause, whereClauseFromSel, aggregateOp, adjColName, qp.m_orderByClause, doingMySQL, dimConfig.m_orderByInFrontPage,dimConfig.m_skip_groupby,(!cList.containsKey(dimConfig.m_internalName)), dimconfigYtdScope, endScopeForYTD, timeColForYtd, externalAliasedTable != null && (!dimConfig.innerMandatory || !dimConfig.m_hidden)? qp.m_aliasedOrderByClause : null, externalAliasedTable != null ? externalAliasedTable+"._c"+i : null);
			
			if (externalAliasedTable != null)
				qp.m_selClause.append(" as _c").append(i);
			cList.put(dimConfig.m_internalName, dimConfig.m_internalName);
			if (addedInGroupBy) {
				if (dimConfig.m_doRollupTotal && rollupAtJava != -1) {
					qp.m_needsRollup = true;
					qp.groupByRollupAggIndicator.add(2);
				}
				else {
					qp.groupByRollupAggIndicator.add(1);
				}
				//below commented will cause difference to be only to be found at rollup ... but prob better to find diff at all and only print result of tot at change indicated ...
				qp.m_isInclInGroupBy.add(i);
				//if (rollupAtJava == 0) {
				//	if (dimConfig.m_doRollupTotal)
				//		qp.m_isInclInGroupBy.add(i);
				//}
				//else {
				//	qp.m_isInclInGroupBy.add(i);
				//}
			}
			else {
				qp.m_hasColWithAgg = true;
				qp.groupByRollupAggIndicator.add(0);
			}
			if (dimConfig.m_doRollupTotal && rollupAtJava != -1)
				qp.m_needsRollup = true;
			if (dimConfig.m_dimCalc.m_dimInfo.m_id == 20024)
				hackNeedsShift = true;
			if (dimConfig.m_dimCalc.m_dimInfo.m_id == 20116)
				hackNeedsRatio = true;

			if (!tList.containsKey(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)) {
				if("Singleton".equalsIgnoreCase(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table))
				{
					//	fromClauseTemp.append(" join ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" ");
				}else{
					String tabName = dimConfig.m_dimCalc.m_dimInfo.m_colMap.table;
					//old if(lastGranTable == null || !gTableList.containsKey(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)) {
					
					if (!gTableList.containsKey(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)) {
						//this should take care of join with current_data table
						//						fromClauseTemp.append(" left outer join ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" on ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".vehicle_id = vehicle.id ");
						//@#@#@#@#
						String tab = dimConfig.m_dimCalc.m_dimInfo.m_colMap.table;
						WorkflowHelper.TableObjectInfo baseTabInfo = WorkflowHelper.g_tableObjectInfoForExtended.get(tab);
						if (baseTabInfo != null && baseTabInfo.getId() > 1) {//0 =veh, 1 = driver, handled elsewhere
							 String baseTab = baseTabInfo.getName();
							 String linkCol = baseTabInfo.getParamName();
							 if (!tList.containsKey(baseTab) && !gTableList.containsKey(baseTab)) {
								 fromClauseTemp.append(" left outer join ").append(baseTab).append(" on (").append(baseTab).append(".vehicle_id = vehicle.id) ");
							 }
							 tList.put(baseTab, baseTab);
							 fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(baseTab).append(".").append(baseTabInfo.getPrimaryIdCol()).append(" = ").append(tab).append(".").append(baseTabInfo.getParamName()).append(") ");
							 tList.put(tab, tab);
						}
						else if ("g_bin_stat_by_hier".equals(tab)) {
							fromClauseTemp.append(",").append(swm_bin_by_hier_hourly);
						}
					
						//else if ("swm_bin_status_info".equals(tab)) {
						//	fromClauseTemp.append(" left outer join ").append(swm_bin_status_info_internal).append(" on (swm_bin_status_info.swm_bin_id = swm_bins.id) ");
						//}
						else if ("demo_hierarchy".equals(tab)) {
							if (tList.containsKey("g_bin_stat_by_hier")) {
								fromClauseTemp.append(" left outer join ").append("demo_hierarchy").append(" on (");
								fromClauseTemp.append("g_bin_stat_by_hier.id");
								fromClauseTemp.append(" = ").append("demo_hierarchy").append(".").append("id) ");
							}
							//rest handled later
						}
						/*
						else if ("demo_hierarchy".equals(tab)) { //add at end
							
							if (tList.containsKey("g_bin_stat_by_hier")) {
								fromClauseTemp.append(" left outer join ").append(tab).append(" on (");
								fromClauseTemp.append("g_bin_stat_by_hier.id");
								fromClauseTemp.append(" = ").append(tab).append(".").append("id) ");
							}
							else if (tList.containsKey("oplu")) {
								if (!tList.containsKey("swm_bins")) {
									fromClauseTemp.append(" left outer join swm_bins on (swm_bins.id = oplu.ref_mines_id) ");
									tList.put("swm_bins", "swm_bins");
								}
								if (!tList.containsKey("swm_bin_hierarchy")) {
									fromClauseTemp.append(" left outer join swm_bin_hierarchy on (swm_bins.id = swm_bin_hierarchy.swm_bin_id) ");
									tList.put("swm_bin_hierarchy", "swm_bin_hierarchy");
								}
							}
							if (tList.containsKey("swm_bin_hierarchy")) {
								fromClauseTemp.append(" left outer join ").append(tab).append(" on (");
								fromClauseTemp.append("swm_bin_hierarchy.val ");
								fromClauseTemp.append(" = ").append(tab).append(".").append("id) ");
							}
							
						}
						*/
						else if ("tp_step".equals(tab)) {
							if (!tList.containsKey("tp_record"))
								tList.put("tp_record", "tp_record");
							fromClauseTemp.append(" left outer join tp_step on (tp_step.tpr_id = tp_record.tpr_id ");
							if (tList.containsKey("tpr_block_status")) {
								fromClauseTemp.append(" and tpr_block_status.workstation_type_id = tp_step.work_station_type  and (tp_step.for_block=1 or tp_step.for_block is null)");
							}
							fromClauseTemp.append(")");
						}
						else if ("tps_question_detail".equals(tab)) {
							if (!tList.containsKey("tp_record"))
								tList.put("tp_record", "tp_record");
							fromClauseTemp.append(" left outer join tps_question_detail on (tps_question_detail.tpr_id = tp_record.tpr_id) ");
						}
						else if ("tat_process_details".equals(tab) && driverObject != WorkflowHelper.G_OBJ_TAT_PROCESS_DETAILS) {
							//wrong ... do it after doTimeBasedJoin
							if (tList.containsKey("tp_record") || driverObject == WorkflowHelper.G_OBJ_TPRECORD) {
								fromClauseTemp.append(" left outer join tat_process_details on (tp_step.work_station_type = tat_process_details.from_step)");
							}
							else {
								fromClauseTemp.append(" cross join  tat_process_details ");//WRONG TODO incorp portNode
							}
						}
						else if ("tat_station_details".equals(tab) && driverObject != WorkflowHelper.G_OBJ_TAT_STATION_DETAILS) {
							//wrong ... do it after doTimeBasedJoin
							if (tList.containsKey("tp_record") || driverObject == WorkflowHelper.G_OBJ_TPRECORD) {
								fromClauseTemp.append(" left outer join tat_station_details on (tp_record.mines_id = tat_station_details.from_opstation)");
							}
							else {
								fromClauseTemp.append(" cross join tat_station_details ");//WRONG TODO - inc port
							}
						}
						else if ("tpr_block_status".equals(tab)) {
							if (!tList.containsKey("tp_record"))
								tList.put("tp_record", "tp_record");
							fromClauseTemp.append(" left outer join tpr_block_status on (tpr_block_status.tpr_id = tp_record.tpr_id ");
							if (tList.containsKey("tp_step")) {
								fromClauseTemp.append(" and tpr_block_status.workstation_type_id = tp_step.work_station_type and (tp_step.for_block=1 or tp_step.for_block is null)");
							}
							fromClauseTemp.append(")");
						}
						else if ("driving_blocking_hist".equals(tab)) {
							if (!tList.containsKey("driver_details"))
								tList.put("driver_details", "driver_details");
							fromClauseTemp.append(" join ").append(tab).append(" on (driver_details.id = ").append(tab).append(".driver_id) ");
						}
						else if ("rfid_handheld_log_unique".equals(tab)) {
							fromClauseTemp.append(" left outer join (").append(rfid_handheld_log_unique).append(") rfid_handheld_log_unique on (tp_record.rf_device_id = rfid_handheld_log_unique.device_id and tp_record.rf_record_id = rfid_handheld_log_unique.record_id and tp_record.rf_challan_date = rfid_handheld_log_unique.record_time) ");
						}
						else if ("rfid_handheld_log_unique_narrow".equals(tab)) {
							fromClauseTemp.append(" join (").append(rfid_handheld_log_unique_narrow).append(") rfid_handheld_log_unique_narrow on (rfid_handheld_log.id = rfid_handheld_log_unique_narrow.rfid)  ");
						}
						else if ("rail_movement_desp".equals(tab)) {
							if (!tList.containsKey("do_rr_details")) 
								tList.put("do_rr_details", "do_rr_details");
							fromClauseTemp.append(" left outer join rail_movement_desp on (do_rr_details.id = rail_movement_desp.do_id) ");
						}
						else if ("secl_remote_comm_result".equals(tab)) {
							if (!tList.containsKey("mines_do_details")) 
								tList.put("mines_do_details", "mines_do_details");
							fromClauseTemp.append(" left outer join secl_remote_comm_result on (mines_do_details.do_number = secl_remote_comm_result.do_number) ");
						}
						else if ("secl_wb_alloc_change_history".equals(tab)) {
							if (!tList.containsKey("mines_do_details")) 
								tList.put("mines_do_details", "mines_do_details");
							fromClauseTemp.append(" left outer join secl_wb_alloc_change_history on (mines_do_details.do_number = secl_wb_alloc_change_history.do_number) ");
						}
						else if ("swm_bin_details".equals(tab)) {
							if (!tList.containsKey("oplu")) {
								if (!tList.containsKey("swm_bins")) 
									tList.put("swm_bin_details", "swm_bin_details");
								fromClauseTemp.append(" left outer join swm_bin_details on (swm_bins.id = swm_bin_details.swm_bin_id) ");
							} //else handled later
							
						}
						else if ("swm_bin_hierarchy".equals(tab)) {
							if (!tList.containsKey("oplu")) { 
								if (!tList.containsKey("swm_bins")) 
									tList.put("swm_bin_hierarchy", "swm_bin_hierarchy");
								fromClauseTemp.append(" left outer join swm_bin_hierarchy on (swm_bins.id = swm_bin_hierarchy.swm_bin_id) ");
							}//else handled later
						}
						else if ("_pending_approval_count".equals(tab)) {
							
							ArrayList<Integer> ety = WorkflowDef.getWorkflowIdsForObjectType(driverObject);
							if (ety == null || ety.size() == 0 || driverTableObjectInfo == null) {
								fromClauseTemp.append(" cross join (select 0 as cnt) _pending_approval_count");
							}
							else {
								String refColName = driverTableObjectInfo.getParamName();
								fromClauseTemp.append(" left outer join (select object_id as ").append(refColName).append(", count(*) cnt from workflows where workflow_type_id in (");
								Misc.convertInListToStr(ety, fromClauseTemp);
								fromClauseTemp.append(") and workflows.status in (1) group by object_id) _pending_approval_count on (_pending_approval_count.")
								.append(refColName)
								.append(" = ").append(driverTableObjectInfo.getName()).append(".").append(driverTableObjectInfo.getPrimaryIdCol()).append(") ")
								;
							}
						}
						else if ("_in_template_workflows".equals(tab)) {
							
							fromClauseTemp.append(" left outer join workflows on (workflows.object_id = ").append(driverTableObjectInfo.getName()).append(".").append(driverTableObjectInfo.getPrimaryIdCol()).append(" and workflows.status in (1) and workflows.workflow_type_id in (@workflow_type) and workflows.pending_approval_of in (@user)) ");
							;
						}
						else if ("_in_template_workflows_inner".equals(tab)) {
							
							fromClauseTemp.append(" join workflows on (workflows.object_id = ").append(driverTableObjectInfo.getName()).append(".").append(driverTableObjectInfo.getPrimaryIdCol()).append(" and workflows.status in (1) and workflows.workflow_type_id in (@workflow_type) and workflows.pending_approval_of in (@user)) ");
							;
						}
						else if ("workflow_hist".equals(tab)) {
							fromClauseTemp.append(" join workflow_hist on (workflow_hist.workflow_id = workflows.id) ");
						}
						else if ("trip_info_for_ee".equals(tab)) {
							fromClauseTemp.append(" left outer join trip_info trip_info_for_ee on (trip_info_for_ee.vehicle_id = engine_events.vehicle_id and engine_events.event_start_time >= trip_info_for_ee.combo_start and (engine_events.event_start_time <= trip_info_for_ee.combo_end or trip_info_for_ee.confirm_time is null)) left outer join op_station lop_for_ee on (lop_for_ee.id = trip_info_for_ee.load_gate_op) left outer join op_station uop_for_ee on (uop_for_ee.id=trip_info_for_ee.unload_gate_op) ");
						}
						else if ("challan_info_ee".equals(tab)) {
							if (!tList.containsKey("trip_info_for_ee"))
								fromClauseTemp.append(" left outer join trip_info trip_info_for_ee on (trip_info_for_ee.vehicle_id = engine_events.vehicle_id and engine_events.event_start_time >= trip_info_for_ee.combo_start and (engine_events.event_start_time <= trip_info_for_ee.combo_end or trip_info_for_ee.confirm_time is null))  left outer join op_station lop_for_ee on (lop_for_ee.id = trip_info_for_ee.load_gate_op) left outer join op_station uop_for_ee on (uop_for_ee.id=trip_info_for_ee.unload_gate_op) ");
							fromClauseTemp.append(" left outer join challan_details challan_info_ee on (challan_info_ee.trip_info_id = trip_info_for_ee.id) ");
						}
						else if ("transporter_org".equals(tab)) {
							fromClauseTemp.append(" left outer join ")
							.append(" (select vehicle.id vehicle_id, max(case when anc.id is not null then anc.lhs_number else leaf.lhs_number end) lid ") 
							.append(" from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) left outer join ") 
							.append(" port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.org_type=2) ")
							.append(" group by vehicle.id) _tptsel on (_tptsel.vehicle_id = vehicle.id) left outer join port_nodes transporter_org on (_tptsel.lid = transporter_org.lhs_number and transporter_org.status = 1) " );
						}
						else if("engine_events_track".equalsIgnoreCase(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)){
							fromClauseTemp.append(" left outer join ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" on ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".engine_events_id = engine_events.id ");
						}
						else if ("trip_info_ext".equalsIgnoreCase(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)) {
							//do nothing ... join is taken care of elsewhere
						}
						//REPLACE_NEW else if("challan_details".equalsIgnoreCase(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)){
						//REPLACE_NEW 	if (!m_trip_info_ext_check && tripChallanDriver != 1)
						//REPLACE_NEW 		fromClauseTemp.append(" left outer join ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" on (challan_details.trip_status in (1,2) and ((").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".trip_info_id = trip_info.id))) ");
						//REPLACE_NEW }
						else if(!onlyCommcommTab && "communicator_command".equalsIgnoreCase(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)){
							
							fromClauseTemp.append(" inner join ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" on ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".engine_event_id = engine_events.id ");
						}else if("actioninfo".equalsIgnoreCase(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)){
							fromClauseTemp.append(" left outer join ").append("(select f.ticket_id,f.anotes,f.adate , f.aissue from (select a.ticket_id,a.anotes,a.adate,a.aissue from actioninfo a  order by a.adate DESC) f  group by f.ticket_id) actioninfo").append(" on ").append("actioninfo.ticket_id = ticket_summary.id ");
						}
						else if("vendorinfo".equalsIgnoreCase(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)){
							fromClauseTemp.append(" left outer join ").append("(select f.ticket_id,f.vnotes,f.vdate , f.vissue from (select a.ticket_id,a.vnotes,a.vdate,a.vissue from vendorinfo a  order by a.vdate DESC) f  group by f.ticket_id) vendorinfo").append(" on ").append("vendorinfo.ticket_id = ticket_summary.id ");
						}
						else if("fieldinfo".equalsIgnoreCase(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)){
							fromClauseTemp.append(" left outer join ").append("(select f.ticket_id,f.fnotes,f.fdate , f.fissue from (select a.ticket_id,a.fnotes,a.fdate,a.fissue from fieldinfo a  order by a.fdate DESC) f  group by f.ticket_id) fieldinfo").append(" on ").append("fieldinfo.ticket_id = ticket_summary.id ");
						}
						else if (tab != null && tab.startsWith("vehicle_extended")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						//REPLACE_NEW else if (tab != null && tab.equals("challan_dispatch_item")) {
						//REPLACE_NEW 	fromClauseTemp.append(" left outer join ").append(tab).append(" on (challan_details.id = ").append(tab).append(".challan_id) ");
						//REPLACE_NEW }
						else if (tab != null && tab.equals("flat_multi_lu")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (trip_info.id = ").append(tab).append(".trip_id) ");
						}
						else if (tab != null && tab.equals("driver_details")) {
							doDriverJoin(driverObject, tList, fromClauseTemp, firstTableUsedInTimeTableJoin, colForFirstTableUsedInTimeTableJoin);
							fromClauseTemp.append(" left outer join driver_details on (driver_details.id = driver_log.driver_id) ");
						}
						else if (tab != null && tab.startsWith("driver_details_extended")) {
							doDriverJoin(driverObject, tList, fromClauseTemp, firstTableUsedInTimeTableJoin, colForFirstTableUsedInTimeTableJoin);
							if (G_OBJ_VEHICLES == driverObject && !tList.containsKey("driver_details")) {
								fromClauseTemp.append(" left outer join driver_details on (driver_details.id = driver_log.driver_id) ");
								tList.put("driver_details", "driver_details");
							}
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (driver_details.id = ").append(tab).append(".driver_id) ");
						}
						else if (tab != null && tab.equals("summary_trip_safety_event")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryTripSafetyEvent).append(" on (trip_info.id = ").append(tab).append(".item_id) ");
						}
						else if (tab != null && tab.equals("summary_trip_voilation")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_trip_voilation).append(" on (trip_info.id = ").append(tab).append(".id) ");
						}
						else if (tab != null && tab.equals("current_scans")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (vehicle.id = ").append(tab).append(".first_scan_vehicle_id and ").append(tab).append(".second_scan_vehicle_id is null) ");							
						}
						else if (tab != null && tab.equals("logged_scans")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("card_info")) {
							//do it after knowing all the scans
						}
						else if (tab != null && tab.equals("summary_trip_critical_events")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_trip_critical_events).append(" on (trip_info.id = ").append(tab).append(".trip_info_id) ");
						}
//						else if (tab != null && tab.equals("ttms_daily_report")) {
//							fromClauseTemp.append(" left outer join ttms_daily_report on (ttms_daily_report.vehicle_id = trip_info.vehicle_id and ttms_daily_report.start_date_time between trip_info.combo_start and trip_info.combo_end) ");
//						}
						/*else if (tab != null && tab.equals("ttms_daily_report") || tab != null && tab.equals("jdedv_daily_report")) {
							fromClauseTemp.append(" left outer join ").append("ttms_daily_report").append(" on (trip_info.vehicle_id = ").append("ttms_daily_report").append(".vehicle_id ")
							.append(" and ").append(" ((trip_info.combo_start <= ttms_daily_report.inDateMainGate and (trip_info.combo_end >= ttms_daily_report.inDateMainGate or trip_info.combo_end is null) )")
							.append(" or ").append(" (trip_info.combo_start <= ttms_daily_report.inDateWeighBridge and (trip_info.combo_end >= ttms_daily_report.inDateWeighBridge or trip_info.combo_end is null)) ")
							.append(" or ").append(" (trip_info.combo_start <= ttms_daily_report.start_date_time and (trip_info.combo_end >= ttms_daily_report.start_date_time or trip_info.combo_end is null)) ")
							.append(" or ").append(" (trip_info.combo_start <= ttms_daily_report.packingPlantOutDate and (trip_info.combo_end >= ttms_daily_report.packingPlantOutDate or trip_info.combo_end is null)))) ");
							if (tab != null && tab.equals("jdedv_daily_report"))
								fromClauseTemp.append(" left outer join ").append("jdedv_daily_report").append(" on (ttms_daily_report.pick_slip_no = ").append(tab).append(".pick_slip_no) ");
						}
						else if (tab != null && tab.equals("ttms_daily_report") || tab != null && tab.equals("jdedv_daily_report")) {
							fromClauseTemp.append(" left outer join ").append("ttms_daily_report").append(" on (trip_info.id = ").append("ttms_daily_report").append(".trip_info_id) ");
							if (tab != null && tab.equals("jdedv_daily_report"))
								fromClauseTemp.append(" left outer join ").append("jdedv_daily_report").append(" on (ttms_daily_report.pick_slip_no = ").append(tab).append(".pick_slip_no) ");
						}*/
						else if (tab != null && tab.equals("trip_opstation_detail")) {
							String tempTab = "unload_nearest_landmarks";
							if (!tList.containsKey(tempTab)) {
								fromClauseTemp.append(" left outer join landmarks unload_nearest_landmarks ").append(" on (trip_info.unload_nearest_lmid = ").append(tempTab).append(".id) ");
								tList.put(tempTab, tempTab);
							}
							tempTab = "unload_nearest_opstation";
							if (!tList.containsKey(tempTab)) {
								fromClauseTemp.append(" left outer join op_station unload_nearest_opstation ").append(" on (trip_info.unload_nearest_opid = ").append(tempTab).append(".id) ");
								tList.put(tempTab, tempTab);
							}
							tempTab = "unload_opstation";
							if (!tList.containsKey(tempTab)) {
								fromClauseTemp.append(" left outer join op_station unload_opstation ").append(" on (trip_info.unload_gate_op = ").append(tempTab).append(".id) ");
								tList.put(tempTab, tempTab);
							}
							//fromClauseTemp.append(" left outer join ").append(g_trip_opstation).append(" on (trip_info.id = ").append(tab).append(".trip_info_id) ");
						}
						else if (tab != null && tab.equals("g_currentTPR")){
							fromClauseTemp.append(" left outer join ").append(g_currentTPR).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
							
						else if (tab != null && tab.equals("lipl_distance_summary")){
							fromClauseTemp.append(" left outer join ").append(g_lipl_distance_summary).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("non_lipl_distance_summary")){
							fromClauseTemp.append(" left outer join ").append(g_non_lipl_distance_summary).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("summary_haldi_db")){
							fromClauseTemp.append(" left outer join ").append(g_summaryHaldiDashboard).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_event_event")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryEventEvent).append(" on (engine_events.id = ").append(tab).append(".item_id) ");
						}
						
						else if (tab != null && tab.startsWith("summary_event_lgd")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryEventLog).append(" on (engine_events.id = ").append(tab).append(".item_id) ");
						}
						
						else if (tab != null && tab.startsWith("summary_24hr_event")) {
							fromClauseTemp.append(" left outer join ").append(g_summary24hrEvent).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_24hr_logged")) {
							fromClauseTemp.append(" left outer join ").append(g_summary24hrdist).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_period_event_lgd")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodEventLog).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("summary_period_max_trip")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodMaxTrip).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("summary_period_trip_count")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodTripCount).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("summary_period_trip_count_mtd")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodTripCountMTDRollup).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}

						else if (tab != null && tab.equals("summary_period_safety_event")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodSafetyEvent).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("summary_period_safety_event_mtd")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodSafetyEventMTDRollup).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("summary_period_safety_event_ytd")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodSafetyEventYTDRollup).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_period_event")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodEvent).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("g_trip_start_end_summ")) {
							fromClauseTemp.append(" left outer join ").append(g_trip_start_end_summ).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("g_vehicle_maint_summary")) {
							fromClauseTemp.append(" left outer join ").append(g_vehicle_maint_summary).append(" on (vehicle_maint_summary.ticket_id = ").append("vehicle_maint").append(".id )");
						}
						else if (tab != null && tab.startsWith("g_maint_part_cycle_summ")) {
							fromClauseTemp.append(" left outer join ").append(g_maint_part_cycle_summ).append(" on (maint_part_cycle_summ.ticket_id = ").append("vehicle_maint").append(".id and vehicle_maint_summary.vehicle_maint_part_replacement_id = maint_part_cycle_summ.part_replacement_id)");
						}
						else if (tab != null && tab.startsWith("g_maint_service_item_summ")) {
							fromClauseTemp.append(" left outer join ").append(g_maint_service_item_summ).append(" on (maint_service_item_summ.ticket_id = ").append("vehicle_maint").append(".id and vehicle_maint_summary.vehicle_maint_services_id = maint_service_item_summ.vehicle_maint_services_id)");
						}
						else if (tab != null && tab.startsWith("summary_period_mtd_event")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodMTDEvent).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_2_period_trip")) {
							fromClauseTemp.append("  join ").append(g_summary2PeriodTrip).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_period_trip")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodTrip).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_3_period_trip_event")) {
							fromClauseTemp.append(" left outer join ").append(g_summary3PeriodTripEvent).append(" on (trip_info.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_idle_period_trip_event")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryIdlePeriodTripEvent).append(" on (trip_info.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_breakdown_event")) {
							fromClauseTemp.append(" left outer join ").append(g_SummaryBreakdownEvent).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_period_lgd_mtd") ) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodLogMTD).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_period_lgd") ) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodLog).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("event_based_movement") ) {
							fromClauseTemp.append(" left outer join ").append(g_eventBasedMovement).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_period_attrib21") ) { //21 must be before 2 becasue due to some stupid reason we do startwith
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodAttrib21).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_period_attrib2") ) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodAttrib2).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}

						else if (tab != null && tab.startsWith("summary_period_speed") ) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodSpeed).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
//						else if (tab != null && tab.startsWith("summary_period_start_end") ) {
//							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodStartEnd).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
//						}
						else if (tab != null && tab.startsWith("summary_event_event")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryEventEvent).append(" on (engine_events.id = ").append(tab).append(".item_id) ");
						}
						else if (tab != null && tab.startsWith("summary_period_fuel")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodFuel).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}else if (tab != null && tab.equals("summary_trip_period_trip_detention")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_trip_period_trip_detention).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("summary_trip_period_MPL")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_trip_period_MPL).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("summary_safety_voilation")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_safety_voilation).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_period_lu_trip")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryPeriodTripLU).append(" on (@period.id = ").append(tab).append(".item_id and vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_latest_out_message")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_latest_out_message).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_latest_in_message")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_latest_in_message).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_latest_token")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_latest_token).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("ext_latest_ch_trip")) {
							String ttab = "summary_latest_trip_info";
							if (!tList.containsKey(ttab)) {
								tList.put(ttab, ttab);
								fromClauseTemp.append(" left outer join ").append(g_summary_latest_trip_info).append(" on (vehicle.id = ").append(ttab).append(".vehicle_id) ");
							}
							ttab = "summary_latest_ch_info";
							if (!tList.containsKey(ttab)) {
								tList.put(ttab, ttab);
								fromClauseTemp.append(" left outer join ").append(g_summary_latest_ch_info).append(" on (vehicle.id = ").append(ttab).append(".vehicle_id) ");
							}
						}
						else if (tab != null && tab.equals("summary_latest_trip_info")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_latest_trip_info).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("summary_latest_trip_info_with_lgd")) {
							String ttab = "summary_latest_trip_info";
							if (!tList.containsKey(ttab)) {
								tList.put(ttab, ttab);
								fromClauseTemp.append(" left outer join ").append(g_summary_latest_trip_info).append(" on (vehicle.id = ").append(ttab).append(".vehicle_id) ");
							}
							fromClauseTemp.append(" left outer join logged_data lgd21446 on (lgd21446.vehicle_id = vehicle.id and lgd21446.attribute_id=0 and lgd21446.gps_record_time = (case when summary_latest_trip_info.unload_gate_in is null then summary_latest_trip_info.load_gate_out when summary_latest_trip_info.confirm_time is null then summary_latest_trip_info.unload_gate_out else null end) ) ");
						}
						else if (tab != null && tab.equals("summary_latest_ch_info")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_latest_ch_info).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("summary_latest_critical_event")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_latest_critical_event).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						//else if (tab != null && tab.startsWith("summary_last_trip_info")) {
						//	fromClauseTemp.append(" left outer join ").append(g_summary_last_trip_info).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						//}
						else if (tab != null && tab.startsWith("summary_unclosed_latest_trip_info")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_unclosed_latest_trip_info).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_latest_trip_event_voilations_before_plant")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_latest_trip_event_voilations_before_plant).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_latest_trip_event_voilations_within_plant")) {
							fromClauseTemp.append(" left outer join ").append(g_summary_latest_trip_event_voilations_within_plant).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.equals("curr_eta_info")) {
							fromClauseTemp.append(" left outer join ").append(g_currEtaInfo).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");//added commonly later .. left outer join src_dest_items on (src_dest_items.id = ").append(tab).append(".src_dest_item_id) ") ;
						}
						//else if (tab != null && tab.equals("eta_info")) {
						//	fromClauseTemp.append(" left outer join ").append(g_currEtaInfo).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) left outer join src_dest_items on (src_dest_items.id = ").append(tab).append(".src_dest_item_id) ") ;
						//}
						else if ("stop_engine_off_since".equals(tab)) {
							fromClauseTemp.append(" left outer join ").append(g_sinceStopEngineOn).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						/*else if ("distance_movement".equals(tab)) {//last_distance_movement
							fromClauseTemp.append(" left outer join ").append(g_distanceMovedToday).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}

						else if ("odometer_distance_movement".equals(tab)) {//DEBUG GPS_DISTANCE
							fromClauseTemp.append(" left outer join ").append(g_distanceUsingOdometer).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if ("odometer_moved_distance_movement".equals(tab)) {//DEBUG GPS_DISTANCE
							fromClauseTemp.append(" left outer join ").append(g_distanceMovedUsingOdometer).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}*/
						else if ("last_distance_movement".equals(tab)) {//last_distance_movement
							fromClauseTemp.append(" left outer join ").append(g_lastMoveDistance).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if ("summary_daily_fuel".equals(tab)) {//last_distance_movement
							fromClauseTemp.append(" left outer join ").append(g_summaryDailyFuel).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_daily_tripLU")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryDailyTripLU).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_daily_trip")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryDailyTrip).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}

						else if (tab != null && tab.startsWith("summary_daily_event_lgd")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryDailyEventLog).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						else if (tab != null && tab.startsWith("summary_daily_event")) {
							fromClauseTemp.append(" left outer join ").append(g_summaryDailyEvent).append(" on (vehicle.id = ").append(tab).append(".vehicle_id) ");
						}
						/*else if (tab != null && tab.startsWith("summary_trip")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (trip_info.id = ").append(tab).append(".item_id) ");
						}*/
						else if (tab != null && tab.equals("vehicle_history")) {
							fromClauseTemp.append(" left outer join ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" on ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".vehicle_id = vehicle.id ");
						}
						else if (tab != null && tab.equals("eta_trip_info")) {
							fromClauseTemp.append(" left outer join eta_setup_op_to_op eta_trip_info   on (eta_trip_info.lopid = trip_info.load_gate_op and eta_trip_info.uopid = trip_info.unload_gate_op)  ");
						}
						else if (tab != null && (tab.equals("g_trip_lead") || tab.equals("g_trip_load_lead") || tab.equals("g_trip_round_lead") || tab.equals("g_trip_unload_lead")
								|| tab.equals("g_trip_load_lead_in_to_in") )) {
							if(hackNeedTripLead){
								//fromClauseTemp.append(" left outer join current_data curr on (curr.vehicle_id=vehicle.id and curr.attribute_id=0) ");
								//fromClauseTemp.append(" left outer join logged_data curr on (curr.vehicle_id=trip_info.vehicle_id and curr.gps_record_time=trip_info.confirm_time and curr.attribute_id=0) ");
								fromClauseTemp.append("left outer join (select vehicle_id, attribute_id, max(gps_record_time) grt from logged_data where attribute_id=0 group by vehicle_id, attribute_id) mx7630  on (trip_info.vehicle_id =mx7630.vehicle_id ) left outer join logged_data curr on (curr.vehicle_id=mx7630.vehicle_id and curr.attribute_id=mx7630.attribute_id and curr.gps_record_time=mx7630.grt and curr.attribute_id=0)");
								hackNeedTripLead = false;
							}
							if (tab != null && tab.equals("g_trip_load_lead")) {
							fromClauseTemp.append(g_trip_load_lead);
							}
							else if (tab != null && tab.equals("g_trip_round_lead")) {
							fromClauseTemp.append(g_trip_round_lead);
							}
							else if (tab != null && tab.equals("g_trip_unload_lead")) {
							fromClauseTemp.append(g_trip_unload_lead);
							}
							else if (tab != null && tab.equals("g_trip_load_lead_in_to_in")) {
								fromClauseTemp.append(g_trip_load_lead_in_to_in);
							}
							
						}
						else if (tab != null && tab.equals("trip_load_opstation")) {
							fromClauseTemp.append(g_trip_load_opstation);
							}
						else if (tab != null && tab.equals("trip_unload_opstation")) {
							fromClauseTemp.append(g_trip_unload_opstation);
							}
						else if (tab != null && tab.equals("trip_multilu_summary")) {
							fromClauseTemp.append("left outer join ").append(g_trip_multilu_summary).append(" on (trip_multilu_summary.trip_id = trip_info.id) ");
						}
						else if (tab != null && tab.equals("g_multitrip_unload_lead")) {
							fromClauseTemp.append(g_multitrip_unload_lead);
						}
						/*else if (tab != null && tab.startsWith("mat")) {
							fromClauseTemp.append(" left outer join ").append(gMatTableList.get(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)).append(" on ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".material_id = material_list.id ");
						}*/
						else if (tab != null && tab.startsWith("opStat")) {
							fromClauseTemp.append(" left outer join ").append(gOpstationVehicles.get(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)).append(" on ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".op_id = org_opstation.id ");
						}
						else if (tab != null && tab.equalsIgnoreCase("workstationSummary")) {
							fromClauseTemp.append(g_workStationSummary);
						}
						else if (tab != null && tab.equals("driver_log")) {
							doDriverJoin(driverObject, tList, fromClauseTemp, firstTableUsedInTimeTableJoin, colForFirstTableUsedInTimeTableJoin);
						}else if (tab != null && tab.equals("rfid_log")) {
//							fromClauseTemp.append(" left outer join rfid_log on (rfid_log.vehicle_id = vehicle.id ) ");//.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" on ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".vehicle_id = vehicle.id ");
							fromClauseTemp.append(" left outer join ").append("rfid_log").append(" on (vehicle.id = ").append("rfid_log").append(".vehicle_id and rfid_log.load_wb_gross_date between trip_info.combo_start and trip_info.combo_end) ");
//							fromClauseTemp.append(" left outer join ").append("engine_events").append(" on (vehicle.id = engine_events.vehicle_id and engine_events.event_start_time between trip_info.combo_start and trip_info.combo_end) ");
						}
						else if(("tr_event_report.xml".equalsIgnoreCase(session.getParameter("front_page")) || "tr_transit_report.xml".equalsIgnoreCase(session.getParameter("front_page"))) && tab != null && tab.equals("shift_table")){
							/*fromClauseTemp.append(" left outer join ").append("shift_table").append(" on ((shift_table.start_time >= '@start_period' " +
									"and shift_table.start_time <= '@end_period'   and shift_table.port_node_id = @pv123  " +
									"and engine_events.event_start_time >= shift_table.start_time " +
									"and engine_events.event_start_time <= shift_table.end_time) ) ");*/
							continue;
						}
						
						//RFID stuff goes here
						else if (tab != null && tab.equals("other_shipment_material")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".other_shipment_id=other_shipments.id) ");
						}
						else if (tab != null && tab.equals("other_shipment_dates")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".other_shipment_id=other_shipments.id) ");
						}
						else if (tab != null && tab.equals("other_shipment_transshipments")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".other_shipment_id=other_shipments.id) ");
						}
						else if (tab != null && tab.equals("do_grade_transporter")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".do_id=do_rr_details.id) ");
						}
						else if (tab != null && tab.equals("current_do_status")) {
							if (driverObject == 60)
								fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".wb_id=secl_workstation_details.id) ");
							else if(doJoinAreaCode)
									fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".do_id=mines_do_details.id and ").append(tab).append(".area_code=mines_do_details.area_code) ");
							else 
								fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".do_id=mines_do_details.id) ");
						}
						else if (tab != null && tab.equals("secl_workstation_screens")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".workstation_profile_id =secl_workstation_profile.id) ");
						}else if (tab != null && tab.equals("haulage_diff_details")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".dumper_type_id =dumper_type_details.id) ");
						}else if (tab != null && tab.equals("load_diff_details")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".load_site_id =loading_site_details.id) ");
						}else if (tab != null && tab.equals("unload_diff_details")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".unload_site_id =unloading_site_details.id) ");
						}
						else if (tab != null && tab.equals("driver_attendance_bsnl")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".driver_id =driver_details.id) ");
						}
						else if (tab != null && tab.equals("profile")) {
							fromClauseTemp.append(" left outer join ").append(tab).append(" on (").append(tab).append(".id =bsnl_complaints.profile_id) ");
						}
						else if (tab != null && tab.equals("grf_monthly_daily_target_plan")) {
							fromClauseTemp.append(" cross join ").append(grf_monthly_daily_target_plan);
						}
						
						// below should be at end
						else if (tab != null && tab.startsWith("swm_bin")) {
							//handled later
						}
						else if (tab != null && tab.startsWith("sccl_process_step_workstation")) {
                            fromClauseTemp.append(" left outer join ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" on ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".process_step_id = sccl_process_step_details.id ");
						}
						else if (tab != null && !tab.startsWith("oplu")) {//oplu is handled elsewhere
							
							if(tab.startsWith("mpl_mines_ranking"))
								fromClauseTemp.append(","+tab);
							else if(tab.startsWith("rake_details"))
								fromClauseTemp.append(" left outer join ").append(tab).append(" on ").append(" trip_info.vehicle_id=rake_details.vehicle_id and trip_info.combo_start between rake_details.combo_start and rake_details.combo_end  ");
							else if(tab.startsWith("dos_pits"))
								fromClauseTemp.append(" left outer join ").append(tab).append(" on ").append(" dos_pits.id=dos_pit_stats.pit_id");
							else
								fromClauseTemp.append(" left outer join ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" on ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".vehicle_id = vehicle.id ");
							
							//if(tab.startsWith("exc_load_event"))
							//	tList.remove(("trip_info"));
						}
						

					}
					else{
						//old .. instead we will create join later ... see comments at the beginning of the function

						//fromClauseTemp.append(" left outer join ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)
						//.append(" on (").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".vehicle_id = ")
						//.append(lastGranTable).append(".vehicle_id and (")
						//.append(getGranBasedString(lastGranTable, gTableList.get(lastGranTable), granDesired)).append(" = ")
						//.append(getGranBasedString(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table, gTableList.get(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table), granDesired)).append(" ))");
					}
					//old .. instead we will create join later

					//if(gTableList.containsKey(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table))
					//	lastGranTable = dimConfig.m_dimCalc.m_dimInfo.m_colMap.table;

				}

				if (!isTimeDim)
					tList.put(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table, dimConfig.m_dimCalc.m_dimInfo.m_colMap.table);
					
			}
		}
		if (tList.containsKey("card_info")) {
			if (tList.containsKey("current_scans")) {
				fromClauseTemp.append(" left outer join card_info on (current_scans.scan = card_info.scan and current_scans.first_scan_ts between card_info.valid_from and card_info.valid_to) ");
			}
			else {
				fromClauseTemp.append(" left outer join card_info on (logged_scans.scan = card_info.scan and logged_scans.attribute_id=0 and logged_scans.gps_record_time between card_info.valid_from and card_info.valid_to) ");
			}
		}
		/*if (tList.containsKey("rfid_log")) {
//			fromClauseTemp.append(" left outer join rfid_log on (rfid_log.vehicle_id = vehicle.id ) ");//.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" on ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".vehicle_id = vehicle.id ");
			fromClauseTemp.append(" left outer join ").append("trip_info").append(" on (vehicle.id = ").append("trip_info").append(".vehicle_id and rfid_log.load_wb_gross_date between trip_info.combo_start and trip_info.combo_end) ");
//			fromClauseTemp.append(" left outer join ").append("engine_events").append(" on (vehicle.id = engine_events.vehicle_id and engine_events.event_start_time between trip_info.combo_start and trip_info.combo_end) ");
		}*/
		CopyOfGeneralizedQueryBuilder.prepForOtherIntermediates(tList);
		
		//REPLACE_NEW if (tripChallanDriver == 0 && tList.containsKey("challan_details")) {
		//REPLACE_NEW 	fromClauseTemp.append(" left outer join challan_details on (challan_details.trip_info_id = trip_info.id) ");
		//REPLACE_NEW }
		
		
		if (tList.containsKey("eta_info")) {
			fromClauseTemp.append(" left outer join src_dest_items on (eta_info.src_dest_item_id = src_dest_items.id) ");
		}
		if (tList.containsKey("curr_eta_info")) {
			fromClauseTemp.append(" left outer join src_dest_items on (curr_eta_info.src_dest_item_id = src_dest_items.id) ");
		}
		/* included elsewhere
		if (doIncludeLatestTrip > 0) {
			if ("trip_info".equals(firstTableUsedInTimeTableJoin))
				fromClauseTemp.append(" left outer join ").append(g_summary_latest_trip_info_for_join).append(" on (vehicle.id = trip_info_ltp.vehicle_id) ");
			else if ("engine_events".equals(firstTableUsedInTimeTableJoin))
				fromClauseTemp.append(" left outer join ").append(g_summary_latest_engine_event_for_join).append(" on (engine_events.vehicle_id = engine_events_ltp.vehicle_id and engine_events.rule_id = engine_events_ltp.rule_id) ");
			else {
				doIncludeLatestTrip = 0; //DEBUG13 TODO dont know what to do ... need to think for challan
			}
		}
		*/
		if (processShowResult != null && processShowResult.maxYTDScopeAsked == Misc.SCOPE_TILL_DATE) {
			boolean hasIndep = tList.containsKey("@indep_period");
			firstTableUsedInTimeTableJoin = hasIndep ? "@period" : null;
			colForFirstTableUsedInTimeTableJoin = hasIndep ? "start_time" : null;
		}
		String whrClause = getWhrQuery(qp, session, searchBox, searchBoxHelper, tList, firstTableUsedInTimeTableJoin, colForFirstTableUsedInTimeTableJoin,vehicleFilterStr, driverObject, doIncludeLatestTrip, externalAliasedTableIndex, doDimsLoadFromDBOnly);
		
		if (processShowResult != null && processShowResult.maxYTDScopeAsked >= 0) {
			if (Misc.isLHSHigherScope(processShowResult.maxYTDScopeAsked, granDesired)) {
				Date en = this.endDt == null ? this.endDt : new Date();
				Date ytdBegDate = new Date(en.getTime());
				TimePeriodHelper.setBegOfDate(ytdBegDate, processShowResult.maxYTDScopeAsked);
				if (this.startDt == null || ytdBegDate.before(this.startDt)) {
					this.startDt = ytdBegDate;
				}
			}
		}
		if (doUnvisitedHack) { //not correct will show things filtered on op_station
			qp.m_fromClause.setLength(0);
			qp.m_fromClause.append(g_fdhs_unvisited_from_part);
			qp.m_whereClause.append(" where (trip_info.id is null) or (").append(whrClause.substring(6)).append(")");
		}
		else {
			qp.m_whereClause.append(whrClause);
		}
		if (tList.containsKey("current_data") && firstTableUsedInTimeTableJoin != null && !"current_data".equals(firstTableUsedInTimeTableJoin) && qp.m_whereClause.indexOf("current_data.attribute_id") < 0)
			qp.m_whereClause.append(" and (current_data.attribute_id=0 or current_data.attribute_id is null) ");
		if (whereClauseFromSel.length() != 0)
			qp.m_whereClause.append(" and ").append(whereClauseFromSel);
		String askedTripIds[] = session.request.getParameterValues("trip_id");
		int askedTripId = Misc.getUndefInt();
		if (askedTripIds == null || askedTripIds.length == 0) {
			askedTripId = Misc.getParamAsInt(session.getParameter("trip_id"));
			if (!Misc.isUndef(askedTripId)) {
				askedTripIds = new String[1];
				askedTripIds[0] = Integer.toString(askedTripId);
			}
		}
		if (askedTripIds != null && askedTripIds.length > 0) {
			qp.m_whereClause.append(" and trip_info.id in (");
			Misc.convertInListToStr(askedTripIds, qp.m_whereClause);
			qp.m_whereClause.append(") ");
		}
		
		int askedTripForEvent = Misc.getParamAsInt(session.getParameter("trip_ee_id"));
		if (!Misc.isUndef(askedTripForEvent)) {
			if (!tList.containsKey("trip_info_for_ee")) {
				qp.m_fromClause.append(" left outer join trip_info trip_info_for_ee on (trip_info_for_ee.vehicle_id = engine_events.vehicle_id and engine_events.event_start_time >= trip_info_for_ee.combo_start and (engine_events.event_start_time <= trip_info_for_ee.combo_end or trip_info_for_ee.confirm_time is null)) ");
			}
			qp.m_whereClause.append(" and trip_info_for_ee.id = ").append(askedTripForEvent).append(" ");
		}
		int askedEventId = Misc.getParamAsInt(session.getParameter("event_id"));
		if (!Misc.isUndef(askedTripId) && (tList.containsKey("engine_events"))) {
			qp.m_whereClause.append(" and engine_events.id = ").append(askedEventId).append(" ");
		}
		//now get the tables for date related stuff
		//BEGIN ... special processing for joing trip/engine event/logged data as well as 'attaching' shift information etc. (TODO fill out comments)
		hackNeedsShift = hackNeedsShift || granDesired == 6;

		if (!doUnvisitedHack && !fpi_m_hackTrackDriveTimeTableJoinLoggedData) {//see comments at the beginning of the function
			CopyOfGeneralizedQueryBuilder.doTimeBasedJoin(qp.m_fromClause, tripChallanDriver, granDesired, doIncludeLatestTrip, adjDriverObject, tList, firstTableUsedInTimeTableJoin, qp.scopesNeededForYTDSpecAtCol, doOrgTimingBased);
			/* REPLACE_NEW
			String prevExistTab = null;
			String prevExistCol = null;
			for (OrderedTimeTableInfo tableWithDateCol : gTableWithDateOrdered) {
				String tab = tableWithDateCol.tabName;
				String col = tableWithDateCol.colName;
				if (tripChallanDriver == 0 && "challan_details".equals(tab))
					continue;
				if (tripChallanDriver == 0 && "tp_record".equals(tab))
					continue;
				
				if (tList.containsKey(tab)) {
					boolean isPeriod =tab.equals("@period"); 
					if (isPeriod) {
						if (granDesired == 6) {
							qp.m_fromClause.append(" join @period on (@period.port_node_id=@pv123) ");
						}
						else {
							qp.m_fromClause .append(" cross join ").append(tab);
						}

					}
					else {
						if (tab != null && tab.equals("summary_trip_period_trip_detention")) {
							qp.m_fromClause.append(" left outer join ").append(g_summary_trip_period_trip_detention).append(" on (vehicle.id = ").append(tab).append(".vehicle_id");
						}
						else if (tab != null && tab.equals("summary_trip_period_MPL")) {
							qp.m_fromClause.append(" left outer join ").append(g_summary_trip_period_MPL).append(" on (vehicle.id = ").append(tab).append(".vehicle_id");
						}
						else if (tab != null && tab.equals("summary_safety_voilation")) {
							qp.m_fromClause.append(" left outer join ").append(g_summary_safety_voilation).append(" on (vehicle.id = ").append(tab).append(".vehicle_id");
						}
						else if (doIncludeLatestTrip > 0 && tab.equals(firstTableUsedInTimeTableJoin)) {
							if ("tp_record".equals(firstTableUsedInTimeTableJoin)) {
								qp.m_fromClause.append(" left outer join ").append(g_summary_latest_tp_record_for_join).append(" on (vehicle.id = tp_record_ltp.vehicle_id) ");
							}
							if ("challan_details".equals(firstTableUsedInTimeTableJoin)) {
								qp.m_fromClause.append(" left outer join ").append(g_summary_latest_challan_details_for_join).append(" on (vehicle.id = challan_details_ltp.vehicle_id) ");
							}
							else if ("trip_info".equals(firstTableUsedInTimeTableJoin))
								qp.m_fromClause.append(" left outer join ").append(g_summary_latest_trip_info_for_join).append(" on (vehicle.id = trip_info_ltp.vehicle_id) ");
							else if ("engine_events".equals(firstTableUsedInTimeTableJoin))
								qp.m_fromClause.append(" left outer join ").append(g_summary_latest_engine_event_for_join).append(" on (vehicle.id = engine_events_ltp.vehicle_id) ");
							qp.m_fromClause.append(" left outer join ").append(tab);
							qp.m_fromClause.append(" on (").append(tab).append(".vehicle_id = vehicle.id ");
							if ("engine_events".equals(firstTableUsedInTimeTableJoin))
								qp.m_fromClause.append(" and engine_events_ltp.rule_id = engine_events.rule_id ");
								
						}
						else{
							qp.m_fromClause.append(" left outer join ").append(tab);
							qp.m_fromClause.append(" on (").append(tab).append(".vehicle_id = vehicle.id ");
						}
					}
					if (prevExistTab != null) {
						qp.m_fromClause.append(" and ");
						if (tab.equals("trip_info_otherLU")) {//prev can only be @period
							qp.m_fromClause.append(" ((case when trip_info_otherLU.gate_out is null then trip_info_otherLU.gate_in else trip_info_otherLU.gate_out end) between @period.start_time and @period.end_time) ");
						}
						else if (tab.equals("challan_details")) {//prev can obly be @period
							qp.m_fromClause.append(" (challan_details.challan_date between @period.start_time and @period.end_time) ");
						}
						else if (tab.equals("tp_record")) {//prev can only be @period
							if (prevExistTab.equals("@period")) {
								qp.m_fromClause.append(" (tp_record.latest_load_gate_in_out between @period.start_time and @period.end_time) ");
							}
							else if (prevExistTab.equals("trip_info")) {
								qp.m_fromClause.append(" tp_record.trip_info_id = trip_info.id ");
							}
							
						}
						else if (tab.equals("trip_info")) {//prev can only be @period or trip_info_otherLU or challan_details
							if (prevExistTab.equals("@period"))
								qp.m_fromClause.append(" ((case when trip_info.unload_gate_in is null then trip_info.combo_start else trip_info.unload_gate_in end) between @period.start_time and @period.end_time) ");
							else if (prevExistTab.equals("tp_record")) {
								qp.m_fromClause.append(" tp_record.trip_info_id = trip_info.id ");								
							}
							else if (prevExistTab.equals("challan_details")) {
								qp.m_fromClause.append(" challan_details.trip_info_id = trip_info.id ");								
							}
							else 
								qp.m_fromClause.append(" (trip_info.id = trip_info_otherLU.trip_id) ");
						}
						else if (tab.equals("demo_scan_trip")) {//prev can only be @period or trip_info_otherLU or challan_details
							if (prevExistTab.equals("@period"))
								qp.m_fromClause.append(" (demo_scan_trip.combo_start between @period.start_time and @period.end_time) ");
							
						}
						else if (tab.equals("rfid_trip")) {//prev can only be @period or trip_info_otherLU
							if (prevExistTab.equals("@period"))
								qp.m_fromClause.append(" (rfid_trip.combo_start between @period.start_time and @period.end_time) ");
							else if (prevExistTab.equals("trip_info"))
								qp.m_fromClause.append(" (rfid_trip.combo_end between trip_info.combo_start and trip_info.combo_end) ");
							else 
								qp.m_fromClause.append(" (1=1) ");
						}
						else if (tab.equals("wb_log")) {//prev can only be @period or trip_info_otherLU
							if (prevExistTab.equals("@period"))
								qp.m_fromClause.append(" (wb_log.wb_date between @period.start_time and @period.end_time) ");
							else if (prevExistTab.equals("trip_info"))
								qp.m_fromClause.append(" (wb_log.wb_date between trip_info.combo_start and trip_info.combo_end) ");
							else 
								qp.m_fromClause.append(" (1=1) ");
						}
						else if (tab.equals("engine_events")) { //prev can only be trip_info or @period or trip_info_otherLU
							if (prevExistTab.equals("trip_info")) {
								qp.m_fromClause.append(" (trip_info.combo_start <= engine_events.event_start_time and trip_info.combo_end >= engine_events.event_start_time) ");
							}
							else if (prevExistTab.equals("@period")) 
								//HUH - WAS qp.m_fromClause.append(" ((case when trip_info.unload_gate_in is null then trip_info.combo_start else trip_info.unload_gate_in end) between @period.start_time and @period.end_time) ");
								qp.m_fromClause.append(" ((case when engine_events.event_stop_time is null then engine_events.event_start_time else engine_events.stop_time end) between @period.start_time and @period.end_time) ");							
							else if (prevExistTab.equals("trip_info_otherLU")) {
								qp.m_fromClause.append(" (trip_info_otherLU.gate_in <= engine_events.event_start_time and (trip_info_otherLU.gate_out is null or trip_info_otherLU.gate_out >= engine_events.event_start_time)) ");
							}
						}
						else if (tab.equals("safety_violations_log")) { //prev can only be trip_info or @period or trip_info_otherLU
							if (prevExistTab.equals("tp_record")) {
								qp.m_fromClause.append(" (trip_info.combo_start <= safety_violations_log.event_start_time and trip_info.combo_end >= engine_events.event_start_time) ");
							}
							if (prevExistTab.equals("trip_info")) {
								qp.m_fromClause.append(" (trip_info.combo_start <= engine_events.event_start_time and trip_info.combo_end >= engine_events.event_start_time) ");
							}
							else if (prevExistTab.equals("@period")) 
								//HUH - WAS qp.m_fromClause.append(" ((case when trip_info.unload_gate_in is null then trip_info.combo_start else trip_info.unload_gate_in end) between @period.start_time and @period.end_time) ");
								qp.m_fromClause.append(" ((case when engine_events.event_stop_time is null then engine_events.event_start_time else engine_events.stop_time end) between @period.start_time and @period.end_time) ");							
							else if (prevExistTab.equals("trip_info_otherLU")) {
								qp.m_fromClause.append(" (trip_info_otherLU.gate_in <= engine_events.event_start_time and (trip_info_otherLU.gate_out is null or trip_info_otherLU.gate_out >= engine_events.event_start_time)) ");
							}
						}
						else if (tab.equals("logged_data")) {//prev can trip_info or engine event - but both have a notion of start/end
							String starter = null;
							String ender = null;
							if (prevExistTab.equals("trip_info")) {
								starter = "trip_info.combo_start";
								ender = "trip_info.combo_end";
							}
							else if (prevExistTab.equals("engine_events")){
								starter = "engine_events.event_start_time";
								ender = "engine_events.event_stop_time";
							}
							else if (prevExistTab.equals("@period")){
								starter = "@period.start_time";
								ender = "@period.end_time";
							}
							else if (prevExistTab.equals("trip_info_otherLU")) {
								starter = "trip_info_otherLU.gate_in";
								ender = "trip_info_otherLU.gate_out";
							}

							qp.m_fromClause.append(" (").append(starter).append("<= logged_data.gps_record_time and (").append(ender).append(" is null or (").append(ender).append(">= logged_data.gps_record_time))) ");
						}
						else if (tab.equals("vehicle_messages")) {//prev can trip_info or engine event - but both have a notion of start/end
							String starter = null;
							String ender = null;
							if (prevExistTab.equals("trip_info")) {
								starter = "trip_info.combo_start";
								ender = "trip_info.combo_end";
							}
							else if (prevExistTab.equals("engine_events")){
								starter = "engine_events.event_start_time";
								ender = "engine_events.event_stop_time";
							}
							else if (prevExistTab.equals("@period")){
								starter = "@period.start_time";
								ender = "@period.end_time";
							}
							else if (prevExistTab.equals("trip_info_otherLU")) {
								starter = "trip_info_otherLU.gate_in";
								ender = "trip_info_otherLU.gate_out";
							}

							qp.m_fromClause.append(" (").append(starter).append("<= vehicle_messages.in_date and (").append(ender).append(" is null or (").append(ender).append(">= vehicle_messages.in_date))) ");
						}
						else if (tab.equals("logged_scans")) {//prev can trip_info or engine event - but both have a notion of start/end
							String starter = null;
							String ender = null;
							if (prevExistTab.equals("demo_scan_trip")) {
								starter = "demo_scan_trip.combo_start";
								ender = "demo_scan_trip.combo_end";
							}
							else if (prevExistTab.equals("trip_info")) {
								starter = "trip_info.combo_start";
								ender = "trip_info.combo_end";
							}
							else if (prevExistTab.equals("engine_events")){
								starter = "engine_events.event_start_time";
								ender = "engine_events.event_stop_time";
							}
							else if (prevExistTab.equals("@period")){
								starter = "@period.start_time";
								ender = "@period.end_time";
							}
							else if (prevExistTab.equals("trip_info_otherLU")) {
								starter = "trip_info_otherLU.gate_in";
								ender = "trip_info_otherLU.gate_out";
							}

							qp.m_fromClause.append(" (").append(starter).append("<= logged_scans.gps_record_time and (").append(ender).append(" is null or (").append(ender).append(">= logged_scans.gps_record_time))) ");
						}
						else if (tab.equals("vehicle_interaction_notes")) {//prev can trip_info or engine event - but both have a notion of start/end
							String starter = null;
							String ender = null;
							if (prevExistTab.equals("trip_info")) {
								starter = "trip_info.combo_start";
								ender = "trip_info.combo_end";
							}
							else if (prevExistTab.equals("engine_events")){
								starter = "engine_events.event_start_time";
								ender = "engine_events.event_stop_time";
							}
							else if (prevExistTab.equals("@period")){
								starter = "@period.start_time";
								ender = "@period.end_time";
							}
							else if (prevExistTab.equals("trip_info_otherLU")) {
								starter = "trip_info_otherLU.gate_in";
								ender = "trip_info_otherLU.gate_out";
							}

							qp.m_fromClause.append(" (").append(starter).append("<= vehicle_interaction_notes.updated_on and (").append(ender).append(" is null or (").append(ender).append(">= vehicle_interaction_notes.updated_on))) ");
						}
						else if (tab.equals("vehicle_maint")) {//prev can trip_info or engine event - but both have a notion of start/end
							String starter = null;
							String ender = null;
							if (prevExistTab.equals("trip_info")) {
								starter = "trip_info.combo_start";
								ender = "trip_info.combo_end";
							}
							else if (prevExistTab.equals("engine_events")){
								starter = "engine_events.event_start_time";
								ender = "engine_events.event_stop_time";
							}
							else if (prevExistTab.equals("@period")){
								starter = "@period.start_time";
								ender = "@period.end_time";
							}
							else if (prevExistTab.equals("trip_info_otherLU")) {
								starter = "trip_info_otherLU.gate_in";
								ender = "trip_info_otherLU.gate_out";
							}
							qp.m_fromClause.append(" (").append(starter).append("<= vehicle_maint.actual_start and (").append(ender).append(" is null or (").append(ender).append(">= vehicle_maint.actual_start))) ");
						}
						else if (tab.equals("vehicle_recvd_messages")) {//prev can trip_info or engine event - but both have a notion of start/end
							String starter = null;
							String ender = null;
							if (prevExistTab.equals("trip_info")) {
								starter = "trip_info.combo_start";
								ender = "trip_info.combo_end";
							}
							else if (prevExistTab.equals("engine_events")){
								starter = "engine_events.event_start_time";
								ender = "engine_events.event_stop_time";
							}
							else if (prevExistTab.equals("@period")){
								starter = "@period.start_time";
								ender = "@period.end_time";
							}
							else if (prevExistTab.equals("trip_info_otherLU")) {
								starter = "trip_info_otherLU.gate_in";
								ender = "trip_info_otherLU.gate_out";
							}

							qp.m_fromClause.append(" (").append(starter).append("<= vehicle_recvd_messages.in_date and (").append(ender).append(" is null or (").append(ender).append(">= vehicle_recvd_messages.in_date))) ");
						}
						else if (tab.equals("eta_info")) {//prev can trip_info or engine event - but both have a notion of start/end
							String starter = null;
							String ender = null;
							if (prevExistTab.equals("trip_info")) {
								starter = "trip_info.combo_start";
								ender = "trip_info.combo_end";
							}
							else if (prevExistTab.equals("engine_events")){
								starter = "engine_events.event_start_time";
								ender = "engine_events.event_stop_time";
							}
							else if (prevExistTab.equals("@period")){
								starter = "@period.start_time";
								ender = "@period.end_time";
							}
							else if (prevExistTab.equals("trip_info_otherLU")) {
								starter = "trip_info_otherLU.gate_in";
								ender = "trip_info_otherLU.gate_out";
							}

							qp.m_fromClause.append(" (").append(starter).append("<= eta_info.src_exit and (").append(ender).append(" is null or (").append(ender).append(">= eta_info.src_exit))) ");
						}
					}
					else {
						//do nothing
					}
					if (!isPeriod)
						qp.m_fromClause.append(")");

					//.append(lastGranTable).append(".vehicle_id and (")
					//.append(getGranBasedString(lastGranTable, gTableList.get(lastGranTable), granDesired)).append(" = ")
					//.append(getGranBasedString(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table, gTableList.get(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table), granDesired)).append(" ))");
					prevExistTab = tab;
					prevExistCol = col;
				}//if timeTable exists in search
			}//for each timeTable
			REPLACE_NEW */
		}//regular join startegry : trip->engine event->logged_data
		else if (!doUnvisitedHack) {//drving join from logged_data etc. .... here we will have
			String prevExistTab = null;
			String prevExistCol = null;
			for (int i=gTableWithDateOrdered.size()-1;i>=0;i--) {
				OrderedTimeTableInfo tableWithDateCol = gTableWithDateOrdered.get(i);
				String tab = tableWithDateCol.tabName;
				String col = tableWithDateCol.colName;
				if ("trip_info".equals(tab)) {
					col = "combo_start";
				}
				else if ("trip_info_otherLU".equals(tab)) {
					col = "gate_in";
				}
				if (tList.containsKey(tab)) {
					boolean isPeriod =tab.equals("@period"); 
					if (isPeriod) {
						qp.m_fromClause .append(" cross join ").append(tab);
					}
					else {
						qp.m_fromClause.append(" left outer join ").append(tab);
						qp.m_fromClause.append(" on (").append(tab).append(".vehicle_id = vehicle.id ");					
					}
					if (prevExistTab != null) {
						//qp.m_fromClause.append(" and ").append(getGranBasedString(prevExistTab, prevExistCol, granDesired)).append(" = ") ... shit this leads to double counting ... instead we need to do time match
						//                              .append(getGranBasedString(tab, col, granDesired));

						qp.m_fromClause.append(" and ").append(firstTableUsedInTimeTableJoin).append(".").append(colForFirstTableUsedInTimeTableJoin).append("=").append(tab).append(".").append(col);
					}
					else {
						//do nothing
					}
					if (!isPeriod)
						qp.m_fromClause.append(")");

					//.append(lastGranTable).append(".vehicle_id and (")
					//.append(getGranBasedString(lastGranTable, gTableList.get(lastGranTable), granDesired)).append(" = ")
					//.append(getGranBasedString(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table, gTableList.get(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table), granDesired)).append(" ))");
					prevExistTab = tab;
					prevExistCol = col;
				}
			}
			// TODO use granDesired and #_table to add time based record slicing

		}
		if (tList.containsKey("trip_info_otherLU")) {
			//TBD  -just put in op_station & op_station groups linkage to make life easier
			fromClauseTemp.append(" left outer join op_station oplu on (oplu.id = trip_info_otherLU.opstation_id) left outer join opstation_mapping oplu_map on (oplu_map.op_station_id = oplu.id) left outer join group_opstation_items oplu_gr_items on (oplu_gr_items.opstation_id = oplu.id) left outer join group_opstations oplu_gr on (oplu_gr.id = oplu_gr_items.group_opstation_id) ");
			if (tList.containsKey("demo_hierarchy") || tList.containsKey("swm_bins") || tList.containsKey("swm_bin_details") || tList.containsKey("swm_bin_hierarchy")) { //add at end
				
				if (tList.containsKey("g_bin_stat_by_hier") && tList.containsKey("demo_hierarchy")) {
					fromClauseTemp.append(" left outer join ").append("demo_hierarchy").append(" on (");
					fromClauseTemp.append("g_bin_stat_by_hier.id");
					fromClauseTemp.append(" = ").append("demo_hierarchy").append(".").append("id) ");
				}
				else {
					fromClauseTemp.append(" left outer join swm_bins on (swm_bins.id = oplu.ref_mines_id)  left outer join swm_bin_hierarchy on (swm_bin_hierarchy.swm_bin_id = swm_bins.id and swm_bin_hierarchy.level = @hier_level) left outer join swm_bin_details on (swm_bin_details.swm_bin_id = swm_bins.id) ");
					if (tList.containsKey("demo_hierarchy")) {
						fromClauseTemp.append(" left outer join demo_hierarchy on (demo_hierarchy.id = swm_bin_hierarchy.val) ");
					}
				}
			}
		}
		if (hackNeedsShift) {
			if ("trip_info".equals(firstTableUsedInTimeTableJoin)) {
				qp.m_fromClause.append(" left outer join shift on (shift.id = trip_info.shift)  ");
			}
			else if ("trip_info_otherLU".equals(firstTableUsedInTimeTableJoin)){
				qp.m_fromClause.append(" left join trip_info on (trip_info.id = trip_info_otherLU.trip_id) left outer join shift on (shift.id = trip_info.id) ");
				tList.put("trip_info", "trip_info");
			}
			else {
				//TODO
			}
		}
		// 
		if( hackNeedsRatio && (granDesired == 4 || granDesired == 3 || granDesired == 2 || granDesired == 6 || granDesired == 7 || granDesired == 8) && tList.containsKey("trip_info")){
			qp.m_fromClause.append("left join @period call_ee  on  ( trip_info.shift_date >= call_ee.start_time and trip_info.shift_date < call_ee.end_time @shift_port )");
			//will work for trip_info_otherLU
		}
		if(tList.containsKey("vehicle") && !tList.containsKey("vehicle_extended")){
			qp.m_fromClause.append(" left join vehicle_extended  on  ( vehicle_extended.vehicle_id = vehicle.id )");
		}
		
		// use granDesired and #_table to add time based record slicing
		if((granDesired == 4 || granDesired == 3 || granDesired == 2 || granDesired == 6) && tList.containsKey("engine_events") && !tList.containsKey("shift_table")){
			if(m_shift_check){
				fromClauseTemp.append(" left outer join ").append("shift_table").append(" on ((shift_table.start_time >= '@start_period' " +
						"and shift_table.start_time <= '@end_period'   and shift_table.port_node_id = @pv123  " +
						"and engine_events.event_start_time >= shift_table.start_time " +
						"and engine_events.event_start_time <= shift_table.end_time) ) ");
			}
				//qp.m_fromClause.append(" LEFT OUTER JOIN #_table ON (engine_events.event_start_time < #_table.end_time AND engine_events.event_stop_time >= #_table.start_time and #_table.port_node_id = @pv123) ");
			else{
				qp.m_fromClause.append(" left outer join ").append("#_table").append(" on ((#_table.start_time >= '@start_period' " +
						"and #_table.start_time <= '@end_period'   " +
						"and engine_events.event_start_time >= #_table.start_time " +
						"and engine_events.event_start_time <= #_table.end_time) ) ");
				String selClause = qp.m_selClause.toString();
				String groupClause = qp.m_groupByClause.toString();
				//qp.m_fromClause.append(" LEFT OUTER JOIN #_table ON (engine_events.event_start_time < #_table.end_time AND engine_events.event_stop_time >= #_table.start_time) ");
				selClause = selClause.replaceAll(", shift_table"+"\\."+"label",", null");
				qp.m_selClause.setLength(0);
				qp.m_selClause.append(selClause);
				groupClause = groupClause.replaceAll(",shift_table"+"\\."+"label","");
				qp.m_groupByClause.setLength(0);
				qp.m_groupByClause.append(groupClause);
			
			}
			
			String pv123 = session.getParameter("pv123");
			if(pv123 != null && pv123.length() > 0)
				replaceSpecialCharString(qp, pv123);
			replaceSpecialCharacters(qp, granDesired);
		}
		//END special processing
		
		if (orderIds != null && orderIds.size() > 0) {
			qp.m_orderByClause.setLength(0);
			qp.m_orderByClauseAsec.setLength(0);
			for (int t1=0,t1s = orderIds.size();t1<t1s;t1++) {
				int idx = orderIds.get(t1);
				boolean desc = idx < 0;
				if (desc)
					idx *= -1;
				idx--;
				DimConfigInfo dc = dimConfigList.get(idx);
				if (qp.m_orderByClause.length() > 0)
					qp.m_orderByClause.append(",");
				DimInfo di = dc.m_dimCalc.m_dimInfo;
				if (di.m_colMap.useColumnOnlyForName)
					qp.m_orderByClause.append(" ").append(di.m_colMap.column);
				else
					qp.m_orderByClause.append(" ").append(dc.m_dimCalc.m_dimInfo.m_colMap.table).append(".").append(dc.m_dimCalc.m_dimInfo.m_colMap.column);
				if (desc)
					qp.m_orderByClause.append(" desc ");

			}
		}
		
		if (m_trip_info_ext_check)
		{
			String selClause = qp.m_selClause.toString();
			String fromClause = qp.m_fromClause.toString();
			String whereClause = qp.m_whereClause.toString();
			qp.m_selClause.delete(0,qp.m_selClause.length());
			qp.m_fromClause.delete(0,qp.m_fromClause.length());
			qp.m_whereClause.delete(0,qp.m_whereClause.length());
			selClause = selClause.replaceAll("trip_info"+"\\.","trip_info_ext.");
			selClause = selClause.replaceAll("challan_details"+"\\.","challan_");
			fromClause = fromClause.replaceAll("trip_info"+"\\.","trip_info_ext.");
			fromClause = fromClause.replaceAll("challan_details"+"\\.","challan_");
			fromClause = fromClause.replaceAll("trip_info ",g_trip_info_ext);
			fromClause = fromClause.replaceAll("trip_info,",g_trip_info_ext);
			whereClause = whereClause.replaceAll("trip_info"+"\\.","trip_info_ext.");
			whereClause = whereClause.replaceAll("challan_details"+"\\.","challan_");
			if (joinClauseFromDim.length() != 0) {
				String joinClauseFromDimStr = joinClauseFromDim.toString();
				joinClauseFromDimStr = joinClauseFromDimStr.replaceAll("trip_info"+"\\.","trip_info_ext.");
				joinClauseFromDimStr = joinClauseFromDimStr.replaceAll("challan_details"+"\\.","challan_");
				joinClauseFromDim.setLength(0);
				joinClauseFromDim.append(joinClauseFromDimStr);
			}
			if (fromClauseTemp.length() != 0) {
				String fromClauseTempStr = fromClauseTemp.toString();
				fromClauseTempStr = fromClauseTempStr.replaceAll("trip_info"+"\\.","trip_info_ext.");
				fromClauseTempStr = fromClauseTempStr.replaceAll("challan_details"+"\\.","challan_");
				fromClauseTemp.setLength(0);
				fromClauseTemp.append(fromClauseTempStr);
			}
			String groupClause = qp.m_groupByClause.toString();
			groupClause = groupClause.replaceAll("trip_info"+"\\.","trip_info_ext.");
			groupClause = groupClause.replaceAll("challan_details"+"\\.","challan_");
			qp.m_groupByClause.setLength(0);
			qp.m_groupByClause.append(groupClause);

			String havingClause = qp.m_havingClause.toString();
			havingClause = havingClause.replaceAll("trip_info"+"\\.","trip_info_ext.");
			havingClause = havingClause.replaceAll("challan_details"+"\\.","challan_");
			qp.m_havingClause.setLength(0);
			qp.m_havingClause.append(havingClause);

			String rollupClause = qp.m_rollupClause.toString();
			rollupClause = rollupClause.replaceAll("trip_info"+"\\.","trip_info_ext.");
			rollupClause = rollupClause.replaceAll("challan_details"+"\\.","challan_");
			qp.m_rollupClause.setLength(0);
			qp.m_rollupClause.append(rollupClause);
			
			String orderByClause = qp.m_orderByClause.toString();
			orderByClause = orderByClause.replaceAll("trip_info"+"\\.","trip_info_ext.");
			orderByClause = orderByClause.replaceAll("challan_details"+"\\.","challan_");
			qp.m_orderByClause.setLength(0);
			qp.m_orderByClause.append(orderByClause);

			String orderByClauseAsec = qp.m_orderByClauseAsec.toString();
			orderByClauseAsec = orderByClauseAsec.replaceAll("trip_info"+"\\.","trip_info_ext.");
			orderByClauseAsec = orderByClauseAsec.replaceAll("challan_details"+"\\.","challan_");
			qp.m_orderByClauseAsec.setLength(0);
			qp.m_orderByClauseAsec.append(orderByClauseAsec);



			qp.m_selClause.append(selClause);
			qp.m_fromClause.append(fromClause);
			qp.m_whereClause.append(whereClause);
		}
		qp.m_fromClause.append(fromClauseTemp);
		if (joinClauseFromDim.length() != 0)
			qp.m_fromClause.append(joinClauseFromDim);
		putIfHasAtPeriodStuff(qp);
		boolean dontChangeStartEndPeriod = qp.hasAtPeriodCol;
		String vehicleListCSV = null;
		Connection conn = session.getConnection();
		if (qp.hasAtVehicleListQ) {
			String pv123Str = Integer.toString(Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT));
			vehicleListCSV = CopyOfGeneralizedQueryBuilder.getVehicleListDesiredCSV(conn, vehicleFilterStr.toString(), pv123Str);
		}
		
		if (qp.m_fromClause.indexOf("@") != -1)
			qp.m_fromClause = helperFixSummaryWithCurrParam(qp.m_fromClause, session, searchBoxHelper, tList,searchBox, externalAliasedTableIndex, driverObject,dontChangeStartEndPeriod, vehicleListCSV);
		if (qp.m_whereClause.indexOf("@") != -1){
			qp.m_whereClause = helperFixSummaryWithCurrParam(qp.m_whereClause, session, searchBoxHelper, tList,searchBox, externalAliasedTableIndex, driverObject, dontChangeStartEndPeriod, vehicleListCSV);
		}
		if (qp.m_selClause.indexOf("@") != -1)
			qp.m_selClause = helperFixSummaryWithCurrParam(qp.m_selClause, session, searchBoxHelper, tList,searchBox, externalAliasedTableIndex, driverObject, dontChangeStartEndPeriod, vehicleListCSV);
		if (qp.m_groupByClause.indexOf("@") != -1)
			qp.m_groupByClause = helperFixSummaryWithCurrParam(qp.m_groupByClause, session, searchBoxHelper, tList,searchBox, externalAliasedTableIndex, driverObject,dontChangeStartEndPeriod, vehicleListCSV);
		if (qp.m_orderByClause.indexOf("@") != -1)
			qp.m_orderByClause = helperFixSummaryWithCurrParam(qp.m_orderByClause, session, searchBoxHelper, tList,searchBox, externalAliasedTableIndex, driverObject, dontChangeStartEndPeriod, vehicleListCSV);
		if(vehicleFilterStr.toString().contains("vehicle"))
			replaceStr(qp.m_fromClause,VEHICLE_FILTER,vehicleFilterStr.toString());
		else
			replaceStr(qp.m_fromClause,VEHICLE_FILTER,"");
		if (hasVirtualTable) {
			buildQueryForVirtualTables (dimConfigList, searchBox, session, searchBoxHelper, qp, nameIndexLookup);
		}
		return qp;
	}

	public QueryParts buildRollupInfoForCached (ArrayList<DimConfigInfo> dimConfigList,  int rollupAtJava) {
		//rollupAtJava = -1 //dont do rollup even if asked .. to be used for inner queries
		//rollupAtJava = 0 // do rollup in query
		//rollupAtJava = 1 //do rollup in java
		QueryParts qp = new QueryParts();
		qp.m_doRollupAtJava = 1 == rollupAtJava; 
		
		for (int i=0,is=dimConfigList.size();i<is;i++){
			DimConfigInfo dimConfig = dimConfigList.get(i);
			boolean hasAgg = dimConfig.m_aggregate;
			boolean dimNeedsRollup = dimConfig.m_doRollupTotal && rollupAtJava != -1;
			if (dimNeedsRollup)
				qp.m_needsRollup = true;		
			if (!hasAgg) {
				qp.groupByRollupAggIndicator.add(dimNeedsRollup ? 2 : 1);
				qp.m_isInclInGroupBy.add(i);
			}
			else {
				qp.m_hasColWithAgg = true;
				qp.groupByRollupAggIndicator.add(0);			
			}
		}
		//END special processing
		return qp;
	}
	
	private static void doDriverJoin(int driverObject, HashMap<String, String> tList, StringBuilder fromClauseTemp, String firstTableUsedInTimeTableJoin, String colForFirstTableUsedInTimeTableJoin) {
		if (G_OBJ_VEHICLES == driverObject && !tList.containsKey("driver_log")) {
			fromClauseTemp.append(" left outer join driver_log on (driver_log.vehicle_id = vehicle.id ");
			if (firstTableUsedInTimeTableJoin != null && colForFirstTableUsedInTimeTableJoin != null) {
				fromClauseTemp.append(" and ").append(firstTableUsedInTimeTableJoin).append(".").append(colForFirstTableUsedInTimeTableJoin)
				.append(" between driver_log.start_time and (case when driver_log.end_time is not null then driver_log.end_time else now() end) )")
				;
			}
			else {
				fromClauseTemp.append(" and now() between driver_log.start_time and (case when driver_log.end_time is not null then driver_log.end_time else now() end) )")
				;
			}
			tList.put("driver_log", "driver_log");
		}
	}

	private static void doDriverJoinOld(int driverObject, HashMap<String, String> tList, StringBuilder fromClauseTemp, String firstTableUsedInTimeTableJoin, String colForFirstTableUsedInTimeTableJoin) {
		if (G_OBJ_VEHICLES == driverObject && !tList.containsKey("driver_log")) {
			fromClauseTemp.append(" left outer join driver_log on (driver_log.vehicle_id = vehicle.id ");
			if (firstTableUsedInTimeTableJoin != null && colForFirstTableUsedInTimeTableJoin != null) {
				fromClauseTemp.append(" and ").append(firstTableUsedInTimeTableJoin).append(".").append(colForFirstTableUsedInTimeTableJoin)
				.append(" between driver_log.start_time and (case when driver_log.end_time is not null then driver_log.end_time else now() end) )")
				;
			}
			else {
				fromClauseTemp.append(" and now() between driver_log.start_time and (case when driver_log.end_time is not null then driver_log.end_time else now() end) )")
				;
			}
			tList.put("driver_log", "driver_log");
		}
	}
	

	private static void replaceSpecialCharacters(QueryParts qp, int granularity){
		if(qp.m_selClause != null && granularity == 4){
			qp.m_selClause = new StringBuilder(qp.m_selClause.toString().replaceAll("#", "day"));
		}else if(qp.m_selClause != null && granularity == 3){
			qp.m_selClause = new StringBuilder(qp.m_selClause.toString().replaceAll("#", "week"));
		}else if(qp.m_selClause != null && granularity == 2){
			qp.m_selClause = new StringBuilder(qp.m_selClause.toString().replaceAll("#", "month"));
		}else if(qp.m_selClause != null && granularity == 6){
			qp.m_selClause = new StringBuilder(qp.m_selClause.toString().replaceAll("#", "shift"));
		}
		if(qp.m_fromClause != null && granularity == 4){
			qp.m_fromClause = new StringBuilder(qp.m_fromClause.toString().replaceAll("#", "day"));
		}else if(qp.m_fromClause != null && granularity == 3){
			qp.m_fromClause = new StringBuilder(qp.m_fromClause.toString().replaceAll("#", "week"));
		}else if(qp.m_fromClause != null && granularity == 2){
			qp.m_fromClause = new StringBuilder(qp.m_fromClause.toString().replaceAll("#", "month"));
		}else if(qp.m_fromClause != null && granularity == 6){
			qp.m_fromClause = new StringBuilder(qp.m_fromClause.toString().replaceAll("#", "shift"));
		}
	}
	private static void replaceSpecialCharString(QueryParts qp, String pv123){
		if(qp.m_selClause != null && "113".equals(pv123) && qp.m_selClause.toString().contains("#RULE_ID_319")){
			qp.m_selClause = new StringBuilder(qp.m_selClause.toString().replaceAll("#RULE_ID_319", "319"));
		}
		else if(qp.m_selClause != null && "113".equals(pv123) && qp.m_selClause.toString().contains("#RULE_ID_320")){
			qp.m_selClause = new StringBuilder(qp.m_selClause.toString().replaceAll("#RULE_ID_320", "320"));
		}
		else if(qp.m_selClause != null){
			qp.m_selClause = new StringBuilder(qp.m_selClause.toString().replaceAll("#RULE_ID_319", "319"));
			qp.m_selClause = new StringBuilder(qp.m_selClause.toString().replaceAll("#RULE_ID_320", "320"));
		}
		
//		else if(qp.m_selClause != null && "117".equals(pv123)){
//			qp.m_selClause = new StringBuilder(qp.m_selClause.toString().replaceAll("#RULE_ID", "342"));
//		}
	}
	private static String g_hackTripInfoDateFormat = "%Y-%m-%d";
	private static String getGranBasedString(String table, String column, int granDesired, String sqlDateFormat) {
		sqlDateFormat = g_hackTripInfoDateFormat;//TODO until we figure out how to sort properly
		String col = table+"."+column;
		if (gTableList.containsKey(table)) {
			OrderedTimeTableInfo timeInfo =gTableList.get(table);
			col = timeInfo.getFlexEventDateCol();
		}
		
		String adjColName = "";
		if (granDesired == 4)
			adjColName = "DATE_FORMAT(cast("+col+" as date), '"+sqlDateFormat+"')";
		//	cast(cast("+col+" as date) as datetime)
		else if (granDesired == 6)
			adjColName = "concat("+"DATE_FORMAT(cast("+col+" as date), '"+sqlDateFormat+"')"+", ' ' ,shift.name)";
		else if (granDesired == Misc.SCOPE_HOUR)
			adjColName = "DATE_FORMAT(cast("+col+" as datetime), '"+sqlDateFormat+":%H hr')";
			//adjColName = "concat(hour(col),'-',hour(col)+2,' Hr')";//DATE_FORMAT(cast("+col+" as datetime), '"+sqlDateFormat+" %H hrs')";
			//adjColName = "concat(date_format(col,'%H-'),(date_format(datehour(col),'-',hour(col)+2,' Hr')";//DATE_FORMAT(cast("+col+" as datetime), '"+sqlDateFormat+" %H hrs')";
		else if (granDesired == 3)
			adjColName = "DATE_FORMAT(adddate(cast("+col+" as date),1-dayofweek(cast("+col+" as date))), '"+sqlDateFormat+"')";
		else if (granDesired == 2)
			adjColName = "DATE_FORMAT(adddate(cast("+col+" as date),1-dayofmonth(cast("+col+" as date))), '"+sqlDateFormat+"')";
		else if (granDesired != 10)
			adjColName = "DATE_FORMAT(cast("+col+" as date), '"+sqlDateFormat+"')";
		//		else if (granDesired != 10)
		//			adjColName = "DATE_FORMAT(adddate(cast("+col+" as date),1-dayofmonth(cast("+col+" as date))), '"+sqlDateFormat+"')";
		else
			adjColName = "DATE_FORMAT("+col+", '"+sqlDateFormat+"')";;
			return adjColName;
	}

	public String buildQuery(SessionManager session, QueryParts qp, ArrayList<Pair<Integer,Long>> vehicleList, boolean doDimsLoadFromDBOnly){
		String shiftQuery = null;
		String shiftSelQuery = null;
		String shiftOrderQuery = null;
		if("tr_ana_internal_shift_detail".equalsIgnoreCase(session.getParameter("page_context"))){
			String pv123str = Integer.toString(Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT));
			shiftQuery =
					g_internalShiftSnippet;			
			shiftSelQuery = "  regions9075.short_code ";
			shiftOrderQuery = " order by vehicle.id, engine_events.event_start_time ";
		}
		StringBuilder querySB = new StringBuilder();
		querySB.append(qp.m_selClause.toString().equals(sel) ? "" : shiftSelQuery != null ? qp.m_selClause.toString().replaceFirst("null", shiftSelQuery) : qp.m_selClause.toString())
		;
		//System.out.println(querySB);
		if (doDimsLoadFromDBOnly) {
			//vi.vehicle_id in selClause at end
			querySB.append(", vi.vehicle_id vvid ");
		}
		querySB.append(qp.m_fromClause.toString())
				.append(shiftQuery != null ? shiftQuery : "")
				;
		//System.out.println(querySB);
		if (false && doDimsLoadFromDBOnly) {
			querySB.append(" where vehicle.id in (");
			if (vehicleList == null || vehicleList.size() == 0) {
				querySB.append(Misc.getUndefInt());
			}
			else {
				for (int t1=0,t1s=vehicleList.size(); t1<t1s;t1++) {
					if (t1 != 0)
						querySB.append(",");
					querySB.append(vehicleList.get(t1).first);
				}
			}
			querySB.append(") ");
		}
		else {
				querySB.append(qp.m_whereClause.toString().equals("null") || qp.m_whereClause.toString().equalsIgnoreCase(" where ") ? "" : qp.m_whereClause.toString());
		}
		//System.out.println(querySB);
		querySB.append(!qp.m_hasColWithAgg || qp.m_groupByClause.toString().equals(grp) ? "" : qp.m_groupByClause.toString())
				.append(qp.m_needsRollup && !qp.m_doRollupAtJava ? qp.m_rollupClause.toString() : "")
				.append(qp.m_havingClause.toString().equals(having) ? "" : qp.m_havingClause.toString())
				;
		//System.out.println(querySB);
		if(shiftOrderQuery != null){
			querySB.append(shiftOrderQuery);
			//System.out.println(querySB);
		}
		else if (qp.m_orderByClause.length() != 0) {
			if (true || !DBQueries.isMySql || !qp.m_needsRollup)
				querySB.append(" order by "+ (qp.m_orderByClause) + (qp.m_orderByClauseAsec));
			//System.out.println(querySB);
		}
		if (qp.m_limit > 0)
			querySB.append(" limit ").append(qp.m_limit);
		//System.out.println(querySB);
		String query = querySB.toString();
		if (query.contains(rollup) && !query.contains(grp))
			query = query.replaceAll(rollup, "");
		if (qp.frozenBaseTable != null) {
			query = query.replaceAll("\\btrip_info\\b", "trip_info_freeze");
			query = query.replaceAll("\\bengine_events\\b", "engine_events_freeze");
		}
		if (query.contains("_1rake_details"))
			query = query.replaceAll("_1rake_details", "rake_details");
		
		return query;
	}
	public static int getDriverObjectFromName(String str) {
		int id = Misc.getParamAsInt(str);
		if (Misc.isUndef(id)) {
			id = WorkflowHelper.getObjectTypeFromTable(str);
		}
		return id;
	}
	public void printPageHeader(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper, JspWriter out) throws Exception {
		printPageHeader(conn, fpi, session, searchBoxHelper, out, null);
	}
	public void printPageHeader(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper, JspWriter out, ProcessShowResult processShowResult) throws Exception {
		StringBuilder sb = new StringBuilder();
		if (fpi.m_headerInfo == null || fpi.m_headerInfo.size() == 0) 
			return;
		ArrayList<DimConfigInfo> flatList = new ArrayList<DimConfigInfo>();
		int maxCol = -1;
		for (int i=0,is = fpi.m_headerInfo.size();i<is;i++) {
			ArrayList<DimConfigInfo> row =(ArrayList<DimConfigInfo>)  fpi.m_headerInfo.get(i);
			int colCount = 0;
			for (int j=0,js = row.size();j<js;j++) {
				DimConfigInfo col = row.get(j);
				flatList.add(col);
				if (col.m_hidden)
					continue;
				colCount += (col.m_dataSpan < 0 ? 1 : col.m_dataSpan);
			}
			if (colCount > maxCol)
				maxCol = colCount;
		}
		ResultInfo.FormatHelper formatHelper = ResultInfo.getFormatHelper(flatList, session, searchBoxHelper); //TODO - get rid of dependency on searchBoxHelper so that formatHelper is called before processSearchBox
		sb.append("<table ID='HEADER_TABLE' border='0' cellspacing='0' cellpadding='3' bordercolor='#003366'>");
		QueryParts qp = buildQueryParts(flatList, fpi.m_headerFilterList, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session, searchBoxHelper,
				formatHelper, fpi.m_colIndexLookup, null, processShowResult, Misc.getUndefInt(),-1, false, fpi.m_orgTimingBased);
		String query = buildQuery(session, qp, null, false);
		System.out.println("#############"+query);

		try{
			ResultSet rs = null;
			ResultInfo resultInfo = null;

			{

				Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				stmt.setFetchSize(Integer.MIN_VALUE);
				//				System.out.println("GeneralizedQueryBuilder.printPage()   : 111  stmt.getFetchSize()   :   : "+stmt.getFetchSize());
				rs = stmt.executeQuery(query);
				int rsetIndex = -1;
				resultInfo = new ResultInfo(flatList, null, rs, session, searchBoxHelper,null, null, formatHelper, null, null,null, null, null, false);

				if (resultInfo.next()) {
					for (int i=0, is=fpi.m_headerInfo.size();i<is;i++) {
						ArrayList rowInfo = (ArrayList) fpi.m_headerInfo.get(i);
						boolean hasNonHiddenCol = false;
						for (int j=0,js=rowInfo == null ? 0 : rowInfo.size();j<js;j++) {
							com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
							if (dimConfig != null && !dimConfig.m_hidden) {
								hasNonHiddenCol = true;
								break;
							}
						}
						if (!hasNonHiddenCol)
							continue;
						sb.append("<tr>")
						.append("<td width='6'  class='sh'>&nbsp;</td>");

						int colSize = rowInfo.size();
						int totColSpan = maxCol*2+1;
						int colSpanPrinted = 1;
						for (int j=0;j<colSize;j++) {
							rsetIndex++;
							com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
							if (dimConfig.m_hidden)
								continue;
							String disp = resultInfo.getValStr(rsetIndex);
							if (disp == null || disp.length() == 0)
								disp = "&nbsp;";
							String tempLabel = dimConfig.m_name;
							if (tempLabel == null || tempLabel.length() == 0)
								tempLabel = "&nbsp;";
							else
								tempLabel += ":";           

							int width = dimConfig.m_width;           
							int labelWidth = dimConfig.m_labelWidth;
							String widthStr = labelWidth > 0 ? " width='"+labelWidth+"' " : "";

							int dataSpan = (dimConfig.m_dataSpan-1)*2+1;     

							sb.append("<td class='sh'  ").append(widthStr).append(" valign='top'>");
							sb.append(tempLabel);
							sb.append("</td>");

							sb.append("<td class='tn' valign='top' colspan='").append(j < (colSize-1)? dataSpan :(totColSpan-colSpanPrinted-1)).append("'>");
							sb.append(disp);
							sb.append("</td>");
						} //looped thru all cols
						sb.append("</tr>");
					}//thru all rows
				}//if resultInfo has next
				sb.append("</table>");
				out.println(sb);
				sb.setLength(0);
				rs.close();
				stmt.close();
			}
		} 
		catch (Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper, JspWriter out, InputTemplate perRowTemplate, Writer genOut, StringBuilder addnlHeader) throws Exception {
		return printPage(conn, fpi, session, searchBoxHelper, out, null, Misc.HTML,"",Misc.getUndefInt(),null, null, perRowTemplate, genOut, addnlHeader);
		
		
	}
	
	private static MiscInner.Pair subqGetSubQueryPosOfDimConfig(DimConfigInfo dci, ArrayList<SubQueryPreprocess> subQueries) {
		int subQIndex = Integer.MAX_VALUE;
		
		for (int j=0,js=dci.subQueryGroup == null ? 0 : dci.subQueryGroup.size(); j<js;j++) {
			if (dci.subQueryGroup.get(j) < subQIndex)
				subQIndex = dci.subQueryGroup.get(j);
		}
		if (subQIndex == Integer.MAX_VALUE)
			subQIndex = 0;
		int posInSubQ=-1;
		SubQueryPreprocess prentry = subQueries.get(subQIndex);
		for (int j=0,js=prentry.dimconfigList.size();j<js;j++) {
			if (prentry.dimconfigList.get(j) == dci) {//may be dicey!! ...we are comparing objectAddress
				posInSubQ = j;
				break;
			}
		}
		return new MiscInner.Pair(subQIndex, posInSubQ);
	}
	
	private static String subqGetOuterCalcExpr(DimConfigInfo dci, ArrayList<DimConfigInfo> dciList, HashMap<String, Integer> colIndexMap, ArrayList<SubQueryPreprocess> subQueries) {
		String outerExpr = dci.outerCalcExpr;
		if (outerExpr == null)
			return null;
		
		StringBuilder retval = new StringBuilder();
		
		int prev = 0;
		int sz = outerExpr.length();
		
		while (prev < sz) {
			int next = outerExpr.indexOf('$', prev);
			if (next >= 0) {
				retval.append(outerExpr.substring(prev, next));
				int endDollar = outerExpr.indexOf('$',next+1);
				if (endDollar < 0)
					endDollar = outerExpr.length();
				String dim = outerExpr.substring(next+1, endDollar);
				Integer idxInteger = colIndexMap.get(dim);
				
				if (idxInteger == null)
					return "null";
				DimConfigInfo referredDci = dciList.get(idxInteger);
				MiscInner.Pair subQposInfo = subqGetSubQueryPosOfDimConfig(referredDci, subQueries);
				if (subQposInfo == null || subQposInfo.first < 0 || subQposInfo.second < 0)
					return "null";
				retval.append("_t").append(subQposInfo.first).append(".").append("_c").append(subQposInfo.second);
				prev = endDollar+1;
			}
			else {
				retval.append(outerExpr.substring(prev, outerExpr.length()));
				break;
			}
		}
		
		return retval.toString();
	}
	public static boolean subqAppendTabColName(DimConfigInfo dci, ArrayList<DimConfigInfo> dciList, HashMap<String, Integer> colIndexMap, ArrayList<SubQueryPreprocess> subQueries, StringBuilder selClause, StringBuilder groupByClause) {
		return subqAppendTabColName(dci, dciList, colIndexMap, subQueries, selClause, groupByClause, false) ;
	}
	public static boolean subqAppendTabColName(DimConfigInfo dci, ArrayList<DimConfigInfo> dciList, HashMap<String, Integer> colIndexMap, ArrayList<SubQueryPreprocess> subQueries, StringBuilder selClause, StringBuilder groupByClause, boolean addEvenIfHidden) {
		String colPart = null;
		if (dci.innerMandatory && dci.m_hidden && !addEvenIfHidden)
			colPart = "null";
		else
			colPart = subqGetOuterCalcExpr(dci, dciList, colIndexMap, subQueries);
		if (colPart == null) {
			MiscInner.Pair subQposInfo = subqGetSubQueryPosOfDimConfig(dci, subQueries);
			if (subQposInfo == null || subQposInfo.first < 0 || subQposInfo.second < 0)
				colPart = "null";
			else
				colPart = "_t"+subQposInfo.first+"."+"_c"+subQposInfo.second;
		}
		if (selClause.length() != 0)
			selClause.append(",");
		boolean hasAgg = false;
		if ("null".equals(colPart)) {
			selClause.append(colPart);
		}
		else if (dci.outerQueryAggOp != null) {
			hasAgg = true;
			selClause.append(dci.outerQueryAggOp).append("(").append(colPart).append(")");
		}
		else {
			selClause.append(colPart);
		}
		if (!hasAgg) {
			if (groupByClause.length() != 0) {
				groupByClause.append(",");
			}
			groupByClause.append(colPart);
		}
		return hasAgg;
	}
	
	public Table corePrintPage(Connection conn, Table parentTable, int reportType, JspWriter out, String frontPageName, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper, InputTemplate perRowTemplate, Writer genOut, StringBuilder addnlHeader) throws Exception {
		StringBuilder sb = new StringBuilder();
		boolean doingTransitTable = "tr_transit_report.xml".equalsIgnoreCase(frontPageName);
		boolean doPlainTable = perRowTemplate != null;
		boolean isCached =  "1".equalsIgnoreCase(session.getParameter("is_cached"));
		if (isCached && "0".equals(fpi.m_customField3))
			isCached = false;
		else if (!isCached && "1".equals(fpi.m_customField3))
			isCached = true;
		boolean doDimsLoadFromDBOnly = false;
		if (isCached) {
			doDimsLoadFromDBOnly = CopyOfGeneralizedQueryBuilder.checkIfLoadFromDBIfCached(fpi.m_frontInfoList);
		}
		ResultSet rs = null;
		ResultInfo resultInfo = null;
		QueryParts qp = null;
		String query = null;
		long startTime = System.currentTimeMillis();
		DimInfo d20765 = DimInfo.getDimInfo(20765);
		String pgContext = session.getParameter("page_context");
		
		System.out.println("[CCACHE]"+isCached+" CF3:"+fpi.m_customField3+Thread.currentThread().getId()+"FP:"+frontPageName);
		preProcessForDataToShow( fpi,  fpi.m_frontInfoList,  fpi.m_frontSearchCriteria,  session,  searchBoxHelper);
		ProcessShowResult processDataToShowResult = ProcessShowResult.processForDataToShow(fpi, fpi.m_frontInfoList, fpi.m_frontSearchCriteria, session, searchBoxHelper);
		//Pair<String, Integer> rowLinkHelper = processDataToShowResult.rowLinkHelperOld;
		ColorCodeDao.getColorCodeInfo(session.getConnection(),Misc.getParamAsInt(session.getParameter("pv123")),session,fpi.m_frontInfoList,
				searchBoxHelper);
		ResultInfo.FormatHelper formatHelper = ResultInfo.getFormatHelper(fpi.m_frontInfoList, session, searchBoxHelper);
		boolean doingCongestionHack = fpi.m_driverObjectLocTracker.startsWith("congestion");
		boolean tableNeedsMainpulationBeforePrinting = processDataToShowResult != null && processDataToShowResult.needsManipulation();
		//if tableNeedsManipulationBeforePrinting ... then we dont stream the results 
		//once we get the table then we manipulate the table body and table header
		
		Table table = doingTransitTable  ? Table.createTable():
			reportType == Misc.HTML || reportType == Misc.DOTMATRIX ? Table.createTable(parentTable, reportType, session, tableNeedsMainpulationBeforePrinting  ? null : out, doPlainTable, fpi, tableNeedsMainpulationBeforePrinting  ? null : genOut,  addnlHeader) //curr page taking time
					:
						Table.createTable();
		if (table.isNullStreamingGenerator() && (parentTable == null || parentTable.isNullStreamingGenerator())) {
			this.maxAllowedCells = Integer.MAX_VALUE;
			this.needToCheckCells = false;
		}
		if (doingCongestionHack) {
			setColorCodeHackCongestion(fpi.m_frontInfoList);
		}
		if (perRowTemplate == null) {
			if (addnlHeader != null && addnlHeader.length() != 0 && reportType == Misc.HTML) {
				TR tr = new TR();
				table.setHeader(tr);
				TD td = new TD();
				
				td.setClassId(CssClassDefinition.getClassIdByClassName("tn"));
				td.setContent(addnlHeader.toString());
				td.setColSpan(fpi.m_frontInfoList.size());
				tr.setRowData(td);
			}
			printTableHeader(fpi, session, formatHelper, table);
			
		}
		
		int portNodeId = getPortNodeId(session, fpi.m_frontSearchCriteria, searchBoxHelper);
		ArrayList<Pair<Integer,Long>> vehicleList = null;
		long ts1 = System.currentTimeMillis();
		long ts2 = ts1;
		if (isCached) {// no longer getting vehicleList and for doDimsLoadFromDBOnly || doDimsLoadFromDBOnly) {
		
			System.out.println("[CCACHE] Getting vehicleList:"+Thread.currentThread().getId()+ "FP:"+frontPageName);
			vehicleList = VehicleSetup.getVehicleList(conn, session.getCache(), portNodeId);
			ts2 = System.currentTimeMillis();
			System.out.println("[CCACHE] Got vehicleList:"+Thread.currentThread().getId()+ "FP:"+frontPageName + "Time(ms):"+(ts2-ts1));
			ts2 = ts1;
		}
		boolean hasStartEndPERIOD = false;
		if (!isCached || doDimsLoadFromDBOnly){
			ArrayList<CopyOfGeneralizedQueryBuilder.SubQueryPreprocess> subQueries = this.preprocessForSubQuery(fpi);
			
			if (subQueries != null && subQueries.size() > 1) {
				
				StringBuilder subqBuilder = new StringBuilder();
				StringBuilder compositeOrderBy = new StringBuilder();
				MiscInner.ContextInfo contextInfo = Misc.getContextInfo(session, session.getConnection(), session.getCache(), session.getLogger(), session.getUser());
				for (int i=0,is=subQueries.size();i <is;i++) {
					SubQueryPreprocess subq = subQueries.get(i);
					int subQSpecificGran = subq.subqSpecificGran;
					CopyOfGeneralizedQueryBuilder tempgqb = i == 0 ? this : new CopyOfGeneralizedQueryBuilder();
					if (subQSpecificGran > 0) {
						session.rememberSessionVars();
						PageHeader.hackPostProcessSearchBox(searchBoxHelper, session, contextInfo, false, subQSpecificGran, true, subq.deltaForRelative);
					}
					QueryParts tempQ = tempgqb.buildQueryParts(subq.dimconfigList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session,
							searchBoxHelper, formatHelper, subq.colMapIndex, subq.objectDriver, fpi.m_orderIds, processDataToShowResult, false, i,-1, doDimsLoadFromDBOnly, fpi.m_orgTimingBased);//"_t"+i is the alias
					if (i == 0)
						qp = tempQ;
					else {
						qp.mergeHasFields(tempQ);
					}
					if (tempQ.m_aliasedOrderByClause.length() != 0) {
						if (compositeOrderBy.length() > 0)
							compositeOrderBy.append(",");
						compositeOrderBy.append(tempQ.m_aliasedOrderByClause);
					}
					String subQuery = doingCongestionHack ? null : tempgqb.buildQuery(session, tempQ, vehicleList, doDimsLoadFromDBOnly);
					boolean hasJoin = false;
					if (subqBuilder.length() != 0) {
						if (subq.joinSpec == null || subq.joinSpec.size() == 0)
							subqBuilder.append(" cross join ");
						else {
							subqBuilder.append(" left outer join ");
							hasJoin = true;
						}
					}
					subqBuilder.append("(").append(subQuery).append(") _t").append(i);
					if (hasJoin) {
						//create the join clause
						subqBuilder.append(" on (");
						boolean added = false;
						for (int j=0,js=subq.joinSpec.size(); j<js;j++) {
							Pair<Integer, ArrayList<Pair<Integer, Integer>>> joinSpec = subq.joinSpec.get(j);
							int referSubQIndex = joinSpec.first;
							for (int k=0,ks=joinSpec.second.size(); k<ks; k++) {
								if (added) {
									subqBuilder.append(" and ");
								}
								added = true;
								subqBuilder.append("_t").append(i).append("._c").append(joinSpec.second.get(k).first).append("=").append("_t").append(referSubQIndex).append("._c").append(joinSpec.second.get(k).second);	
							}
						}//for each otherSubQ ref
						subqBuilder.append(") ");
					}
					if (subQSpecificGran > 0) {
						session.setToRememberedVars();
					}
				}//for eachSubQ
				//now put the select clause
				hasStartEndPERIOD = qp.hasAtPeriodCol;
				StringBuilder tempSelClause = new StringBuilder();
				StringBuilder tempGroupByClause = new StringBuilder();
				boolean hasAgg = false;
				
				qp.groupByRollupAggIndicator = new ArrayList<Integer>();
				boolean hasRollup = false;
				int rollupAtJava = fpi.m_doRollupAtJava ? 1 : 0;
				 if (processDataToShowResult != null && processDataToShowResult.needsManipulation())
					 rollupAtJava = 1; //only implemented for ma
				 else if (!Misc.g_doRollupAtJava) //testing rollup calc at java end for regular tables also ... so if not specified at   
					 rollupAtJava = 0;
				 qp.m_doRollupAtJava = rollupAtJava == 1;
				 qp.groupByRollupAggIndicator.clear();
				 if (qp.m_isInclInGroupBy != null)
					 qp.m_isInclInGroupBy.clear();
				 for (int i=0,is=fpi.m_frontInfoList == null ? 0 : fpi.m_frontInfoList.size();i<is;i++) {
					DimConfigInfo dci = (DimConfigInfo) fpi.m_frontInfoList.get(i);
					boolean addEvenIfHidden = i == fpi.projectNameLookupFieldIndex;
					boolean thisColAgg = subqAppendTabColName(dci, fpi.m_frontInfoList, fpi.m_colIndexLookup, subQueries, tempSelClause, tempGroupByClause, addEvenIfHidden);
					hasAgg = thisColAgg   || hasAgg;
					qp.groupByRollupAggIndicator.add(thisColAgg ? 0 : dci.m_doRollupTotal ? 2 :1);
					qp.m_hasColWithAgg = qp.m_hasColWithAgg || hasAgg;
					if (dci.m_doRollupTotal)
						qp.m_needsRollup = true;
					if (!thisColAgg)
						qp.m_isInclInGroupBy.add(i);
				}
				StringBuilder tempFullQ = new StringBuilder();
				tempFullQ.append("select ").append(tempSelClause).append(" from ").append(subqBuilder);
				if (hasAgg && tempGroupByClause.length() > 0)
					tempFullQ.append(" group by ").append(tempGroupByClause);
				tempFullQ.append(qp.m_needsRollup && !qp.m_doRollupAtJava ? qp.m_rollupClause.toString() : "");
				if (!(qp.m_needsRollup && !qp.m_doRollupAtJava) && compositeOrderBy.length() > 0)
					tempFullQ.append(" order by ").append(compositeOrderBy);
				
				tempFullQ = helperFixSummaryWithCurrParam(tempFullQ, session, searchBoxHelper, null, fpi.m_frontSearchCriteria, -1, CopyOfGeneralizedQueryBuilder.getDriverObjectFromName(fpi.m_driverObjectLocTracker), !hasStartEndPERIOD, null);
				
				query = tempFullQ.toString();
				//GeneralizedQueryBuilder.hel
			}
			else {
				
				int rollupAtJava = fpi.m_doRollupAtJava ? 1 : 0;
				if (processDataToShowResult != null && processDataToShowResult.needsManipulation())
					 rollupAtJava = 1; //only implemented for ma
				 else if (!Misc.g_doRollupAtJava) //20160420 ... now rollup at java preferred
					 rollupAtJava = 0;
				qp = doingCongestionHack ? null : buildQueryParts(fpi.m_frontInfoList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session,
					searchBoxHelper, formatHelper, fpi.m_colIndexLookup, getDriverObjectFromName(fpi.m_driverObjectLocTracker), fpi.m_orderIds, processDataToShowResult, false, Misc.getUndefInt(), rollupAtJava, doDimsLoadFromDBOnly, fpi.m_orgTimingBased);
				hasStartEndPERIOD = qp.hasAtPeriodCol;
				query = doingCongestionHack ? null : buildQuery(session, qp, vehicleList, doDimsLoadFromDBOnly);
				if (fpi.m_customField1 != null && fpi.m_customField1.length() > 0)
					query += " limit "+fpi.m_customField1;
			}
			System.out.println("#############"+query);
		}
		
		try{
			if (!doingCongestionHack) {
				if (isCached){
					int rollupAtJava = fpi.m_doRollupAtJava ? 1 : 0;
					 if (processDataToShowResult != null && processDataToShowResult.needsManipulation())
						 rollupAtJava = 1; //only implemented for ma
					 else if (!Misc.g_doRollupAtJava) //testing rollup calc at java end for regular tables also ... so if not specified at   
						 rollupAtJava = 0;
					
					qp = this.buildRollupInfoForCached(fpi.m_frontInfoList, rollupAtJava);
					qp.m_doRollupAtJava = rollupAtJava == 1;
						
					boolean doCombinedApproach  = true;
					HashMap<String, Value> loadFromDBVals = null;//key = vehicleId+_+dimId
					if (doDimsLoadFromDBOnly) {
						ts1 = System.currentTimeMillis();
						System.out.println("[CCACHE] Loading Values from DB:"+Thread.currentThread().getId()+ "FP:"+frontPageName+" Query:"+query);
						loadFromDBVals = CopyOfGeneralizedQueryBuilder.getLoadFromDBIfDoingCached(conn, session, portNodeId, fpi.m_frontInfoList, query);
						ts2 = System.currentTimeMillis();
						System.out.println("[CCACHE] Loaded Values from DB:"+Thread.currentThread().getId()+ "FP:"+frontPageName+" time(ms):"+(ts2-ts1));
						ts1 = ts2;
					}
					else {
						ts1 = System.currentTimeMillis();
					}
					System.out.println("[CCACHE] Getting Filter:"+Thread.currentThread().getId()+ "FP:"+frontPageName);
					if (!doCombinedApproach)
						filterVehicleList(session, fpi.m_frontSearchCriteria, searchBoxHelper, vehicleList, loadFromDBVals);
					ts2 = System.currentTimeMillis();
					System.out.println("[CCACHE] After Filter:"+Thread.currentThread().getId()+ "FP:"+frontPageName+" time(ms):"+(ts2-ts1));
					ts1=ts2;
					if (vehicleList != null && vehicleList.size() > 0){
						CurrCacheGrouper groupHelper = new CurrCacheGrouper(fpi.m_frontInfoList, doCombinedApproach ? fpi.m_frontSearchCriteria : null, null, loadFromDBVals);
						if (doCombinedApproach || groupHelper.hasGrouping()) {
							groupHelper.doGetDataAndGrouping(conn, vehicleList,  session, searchBoxHelper);
							if (groupHelper.hasGrouping()&&!fpi.m_preventGrouping && (processDataToShowResult == null || !processDataToShowResult.hasPivoting())) {
								Triple<Integer, ArrayList<Value>, ArrayList<Value>> totRow = groupHelper.doTotal(0, -1, null); //-1 will force to end of
								if (totRow != null)
									groupHelper.addTotalRowAt(-1, totRow);
							}	
							if (groupHelper.getSize() == 0 && fpi.m_doZeroRow)
								groupHelper.addZeroRow();
							resultInfo = new ResultInfo(fpi.m_frontInfoList, fpi.m_colIndexLookup, rs, session, searchBoxHelper
									,qp.m_needsRollup && qp.m_hasColWithAgg ? qp.m_isInclInGroupBy : null, fpi.m_colIndexUsingExpr, formatHelper, null, null,groupHelper,processDataToShowResult != null && processDataToShowResult.colsInRow != null && processDataToShowResult.colsInRow.size() > 0 ? processDataToShowResult.colsInRow : qp.m_hasColWithAgg ? qp.m_isInclInGroupBy : null, processDataToShowResult, qp.groupByRollupAggIndicator, qp.m_doRollupAtJava);
							//DEBUG13 above figure out how to use grouping of ResultInfo
						}
						else {
							resultInfo = new ResultInfo(fpi.m_frontInfoList, fpi.m_colIndexLookup, rs, session, searchBoxHelper
									,qp.m_needsRollup && qp.m_hasColWithAgg ? qp.m_isInclInGroupBy : null, fpi.m_colIndexUsingExpr, formatHelper, null, null,vehicleList,processDataToShowResult != null && processDataToShowResult.colsInRow != null && processDataToShowResult.colsInRow.size() > 0 ? processDataToShowResult.colsInRow : qp.m_hasColWithAgg ? qp.m_isInclInGroupBy : null, processDataToShowResult, qp.groupByRollupAggIndicator, qp.m_doRollupAtJava);
						}
						ts2 = System.currentTimeMillis();
						System.out.println("[CCACHE] After Grouping:"+Thread.currentThread().getId()+ "FP:"+frontPageName+" time(ms):"+(ts2-ts1));
						ts1=ts2;
						printTable(resultInfo, fpi, searchBoxHelper, session,table, qp, processDataToShowResult, perRowTemplate, genOut, addnlHeader);
						ts2 = System.currentTimeMillis();
						System.out.println("[CCACHE] After printTable:"+Thread.currentThread().getId()+ "FP:"+frontPageName+" time(ms):"+(ts2-ts1));
						ts1=ts2;
					}
				}
				else {
					ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>> virtualResult = buildResultForVirtualTables(conn, qp, session, searchBoxHelper);
					ArrayList<HelperPeriodItemList> driverPeriods = hasStartEndPERIOD ? this.getPeriodsToDrive(conn, session, searchBoxHelper)
							 : null;
					boolean hasValidPeriods = driverPeriods != null && driverPeriods.size() > 0; 
					SimpleDateFormat indepFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					for (int i=0,is=hasValidPeriods ? driverPeriods.size() : 1; i<is; i++) {
						String expandedQuery = query; 
						if (hasValidPeriods) {
							HelperPeriodItemList periodInfo = driverPeriods.get(i);
							java.util.Date monStart = new java.util.Date(periodInfo.startTime);
							monStart.setDate(1);
							
							expandedQuery = expandedQuery.replaceAll("@PERIOD_START_TIME_MONTH", indepFormat.format(monStart));
							String stPeriod = indepFormat.format(new java.util.Date(periodInfo.startTime));
							String enPeriod = indepFormat.format(new java.util.Date(periodInfo.endTime));
							expandedQuery = expandedQuery.replaceAll("@PERIOD_START_TIME", stPeriod);
							expandedQuery = expandedQuery.replaceAll("@start_period", stPeriod);
							expandedQuery = expandedQuery.replaceAll("@PERIOD_END_TIME", enPeriod);
							expandedQuery = expandedQuery.replaceAll("@end_period", enPeriod);
							expandedQuery = expandedQuery.replaceAll("@PERIOD_ITEM_ID", Integer.toString(periodInfo.itemId));
							expandedQuery = expandedQuery.replaceAll("@PERIOD_LABEL", periodInfo.label);
							System.out.println("Thread:"+Thread.currentThread().getId()+", ###PERIOD_QUERY:"+expandedQuery);
						}
						Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
						stmt.setFetchSize(Integer.MIN_VALUE); //.. leads to issues
						if(!"WEB_LAFARGE".equalsIgnoreCase(Misc.getServerName())){
							if (fpi.m_queryTimeOut > 0 ){
								System.out.println ("Set Query TimeOut" + fpi.m_queryTimeOut);
								stmt.setQueryTimeout(fpi.m_queryTimeOut);
							}else {
								int globalQueryTimeOut = Misc.getParamAsInt(Misc.newConnProp.getProperty(Misc.getServerName()+".global_query_timeout", ""+20*60));
								System.out.println ("Set Default Query TimeOut : " + globalQueryTimeOut);
								stmt.setQueryTimeout(globalQueryTimeOut);
							}
						}
						/*
						int globalQueryTimeOut = 10*60;//Misc.getParamAsInt(Misc.newConnProp.getProperty(Misc.getServerName()+".global_query_timeout", ""+20*60));
						System.out.println ("Set Default Query TimeOut : " + globalQueryTimeOut);
						stmt.setQueryTimeout(globalQueryTimeOut);
						*/
						rs = stmt.executeQuery(expandedQuery);
						resultInfo = new ResultInfo(fpi.m_frontInfoList, fpi.m_colIndexLookup, rs, session, searchBoxHelper,
								qp.m_needsRollup && qp.m_hasColWithAgg ? qp.m_isInclInGroupBy : null, fpi.m_colIndexUsingExpr, formatHelper, virtualResult, qp.m_virtualCol,vehicleList, processDataToShowResult != null && processDataToShowResult.colsInRow != null && processDataToShowResult.colsInRow.size() > 0 ? processDataToShowResult.colsInRow : qp.m_hasColWithAgg ? qp.m_isInclInGroupBy : null, processDataToShowResult, qp.groupByRollupAggIndicator, qp.m_doRollupAtJava);
						printTable(resultInfo, fpi, searchBoxHelper, session,table, qp, processDataToShowResult, perRowTemplate, genOut, addnlHeader);
						rs.close();
						stmt.close();
					}
				}
			}
			else {
				printTableCongestion(conn, formatHelper, fpi, searchBoxHelper, session, table);
			}
			table.closeTable();
			System.out.println("[REPORT GENRATION TIME:"+(System.currentTimeMillis() - startTime)+"]");
		}catch (SQLException  s){
			System.out.println("Exception Code :: " + s.getErrorCode());
			if (1317 == s.getErrorCode()){
				System.out.println("Exception Message :: " + s.getMessage());
				out.println ("<br><br><br><br> </t> </t> Please contact customer care to get this report on Email <br>");
				out.println ("<br> </t></t> Email : support@ipssi.com, operation@ipssi.com  <br>");
				out.println ("<br> </t></t> Contact : 7835004444  <br>");
			} else {
				throw s;
			}
		} catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		if (tableNeedsMainpulationBeforePrinting) {
			manipulateTablePost(table, conn, fpi.m_frontInfoList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session,
					searchBoxHelper, formatHelper, fpi.m_colIndexLookup, getDriverObjectFromName(fpi.m_driverObjectLocTracker), fpi.m_orderIds, processDataToShowResult, resultInfo, fpi.m_doPivotMeasureFirstRow,qp);
			
		}
		
		return table;
	}
	
	
	public static boolean checkIfLoadFromDBIfCached(ArrayList<DimConfigInfo> dciList) {
		for (int i=0,is=dciList == null ? 0 : dciList.size(); i<is; i++) {
			DimConfigInfo dci = dciList.get(i);
			if (dci == null || dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null)
				continue;
			if (dci.m_dimCalc.m_dimInfo.m_loadFromDB == 1)
				return true;
		}
		return false;
	}
	public static HashMap<String, Value> getLoadFromDBIfDoingCached(Connection conn, SessionManager session, int portNodeId, ArrayList<DimConfigInfo> dciList, String query) {
		//return null if there is nothing needed
		HashMap<String, Value> retval = new HashMap<String, Value>(3000, 0.75f);
		Statement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.createStatement();
			rs = ps.executeQuery(query);
			while (rs.next()) {
				int vid = Misc.getRsetInt(rs, "vvid");
				if (Misc.isUndef(vid))
					continue;
				int currIdx = 0;
				for (int i=0,is=dciList.size(); i<is;i++) {
					DimConfigInfo dci = dciList.get(i);
					
					DimInfo dimInfo = dci != null && dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
					if (dimInfo == null || dimInfo.m_loadFromDB != 1)
						continue;
					
					Value v = null;
					if (dimInfo.m_type == Cache.STRING_TYPE) {
						String s = rs.getString(currIdx+1);
						if (s != null)
							v = new Value(rs.getString(currIdx+1));
					}
					else if (dimInfo.m_type == Cache.NUMBER_TYPE) {
						double d = Misc.getRsetDouble(rs, currIdx+1);
						if (!Misc.isUndef(d))
							v = new Value(d);
					}
					else if (dimInfo.m_type == Cache.DATE_TYPE) {
						long dt = Misc.sqlToLong(rs.getTimestamp(currIdx+1));
						if (dt > 0)
							v = new Value(dt);
					}
					else {
						int iv = Misc.getRsetInt(rs, currIdx+1);
						if (!Misc.isUndef(iv))
							v = new Value(iv);
					}
					if (v != null)
						retval.put(vid+"_"+dimInfo.m_id, v);
					currIdx++;
				}
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closeStmt(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closeStmt(ps);
		}
		return retval;
	}
	public static boolean hasNested(ArrayList<DimConfigInfo> fpList) {
		for (DimConfigInfo dci: fpList) {
			if (dci.m_refMasterBlockInPFM != null && dci.m_refMasterBlockInPFM.length() != 0)
				return true;
		}
		return false;
	}
	public static class SubQueryPreprocess {
		public ArrayList<DimConfigInfo> dimconfigList = new ArrayList<DimConfigInfo>();
		public HashMap<String, Integer> colMapIndex = new HashMap<String, Integer>();
		public ArrayList<Pair<Integer, ArrayList<Pair<Integer, Integer>>>> joinSpec = new ArrayList<Pair<Integer, ArrayList<Pair<Integer, Integer>>>>();
		//1st: Which SubQueryReferred to
		//2nd: ArrayList of Pair<me DimConfigIndex, DimConfigIndex in subQueryReferredTo> 
		public int objectDriver = WorkflowHelper.G_OBJ_NODRIVER;
		public int subqSpecificGran = -1;
		public int deltaForRelative = 0;
	}
	private ArrayList<SubQueryPreprocess> preprocessForSubQuery(FrontPageInfo fpi) {
		ArrayList<SubQueryPreprocess> retval = null;
		ArrayList<DimConfigInfo> dimConfigList = fpi.m_frontInfoList;
		for (int i=0,is=dimConfigList == null ? 0 : dimConfigList.size(); i<is; i++) {
			DimConfigInfo dci = dimConfigList.get(i);
			for (int j=0,js = dci.subQueryGroup == null ? 0 : dci.subQueryGroup.size(); j<js; j++) {
				int qgr = dci.subQueryGroup.get(j);
				if (retval == null)
					retval = new ArrayList<SubQueryPreprocess>();
				for (int k=retval.size();k<= qgr;k++)
					retval.add(new SubQueryPreprocess());
				SubQueryPreprocess addTo = retval.get(qgr);
				addTo.dimconfigList.add(dci);
			}
		}
		for (int i=0,is=fpi.subQueryDrivers == null ? 0 : fpi.subQueryDrivers.size(); i<is; i++) {
			int idx = fpi.subQueryDrivers.get(i);
			SubQueryPreprocess prresult = retval.get(i);
			prresult.objectDriver = idx;
		}
		
		for (int i=0,is=fpi.subQueryTimeGran == null ? 0 : fpi.subQueryTimeGran.size(); i<is; i++) {
			int idx = fpi.subQueryTimeGran.get(i);
			SubQueryPreprocess prresult = retval.get(i);
			prresult.subqSpecificGran = idx;
		}
		for (int i=0,is=fpi.subQueryRelativeTimeDelta == null ? 0 : fpi.subQueryRelativeTimeDelta.size(); i<is; i++) {
			int idx = fpi.subQueryRelativeTimeDelta.get(i);
			SubQueryPreprocess prresult = retval.get(i);
			prresult.deltaForRelative = idx;
		}
		for (int i=0,is=retval == null ? 0 : retval.size(); i<is; i++) {
			SubQueryPreprocess prresult = retval.get(i);
			for (int j=0,js=prresult.dimconfigList.size(); j<js; j++) {
				DimConfigInfo dci = prresult.dimconfigList.get(j);
				if (dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null) {
					int dimId = dci.m_dimCalc.m_dimInfo.m_id;
					prresult.colMapIndex.put("d"+dimId, j);
					prresult.colMapIndex.put(Integer.toString(dimId), j);
				}
				if (dci.m_columnName != null && dci.m_columnName.length() != 0) {
					prresult.colMapIndex.put(dci.m_columnName, j);
				}
				if (dci.m_name != null && dci.m_name.length() != 0) {
					prresult.colMapIndex.put(dci.m_name, j);
				}
			}
		}
		//now interpret the join clause
		for (int i=0,is=retval == null ? 0 : retval.size(); i<is; i++) {
			SubQueryPreprocess prresult = retval.get(i);
			for (int j=0,js=prresult.dimconfigList.size(); j<js; j++) {
				DimConfigInfo dci = prresult.dimconfigList.get(j);
				for (int k=0,ks=dci.subQueryJoinColName == null ? 0 : dci.subQueryJoinColName.size(); k<ks;k++) {
					Pair<Integer, String> entry = dci.subQueryJoinColName.get(k);
					int refersToSubQIndex = entry.first;
					SubQueryPreprocess referToSubQ = retval.get(refersToSubQIndex);
					Integer refersToDimIdx = referToSubQ.colMapIndex.get(entry.second);
					if (refersToDimIdx != null) {
						int joinSpecIdx = -1;
						for (int l=0,ls = prresult.joinSpec.size(); l<ls; l++) {
							if (prresult.joinSpec.get(l).first == refersToSubQIndex) {
								joinSpecIdx = l;
								break;
							}
						}
						if (joinSpecIdx == -1) {
							prresult.joinSpec.add(new Pair<Integer, ArrayList<Pair<Integer, Integer>>>(refersToSubQIndex, new ArrayList<Pair<Integer, Integer>>()));
							joinSpecIdx = prresult.joinSpec.size()-1;
						}
						ArrayList<Pair<Integer, Integer>> joinList = prresult.joinSpec.get(joinSpecIdx).second;
						joinList.add(new Pair<Integer, Integer>(j, refersToDimIdx));
					}//if valid refersToDimIdx
				}//for each joinSubQ
			}//for each dimConfiInfo
		}//for each subProcessResult
		return retval;
	}
	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper, JspWriter out,ByteArrayOutputStream stream,int reportType,String reportName,int reportId, ServletOutputStream servletStream, Table parentTable, InputTemplate perRowTemplate, Writer genOut, StringBuilder addnlHeader) throws Exception {
		return printPageGen(conn, fpi, session, searchBoxHelper, out, stream, reportType, reportName, reportId, servletStream, parentTable, perRowTemplate, genOut, addnlHeader).first;
	}
	static int g_dbgCurrGCCount = 0;
	static boolean g_doGCStuff = true;
	public Pair<String,Table> printPageGen(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper, JspWriter out,ByteArrayOutputStream stream,int reportType,String reportName,int reportId, ServletOutputStream servletStream, Table parentTable, InputTemplate perRowTemplate, Writer genOut, StringBuilder addnlHeader) throws Exception {
		
		StringBuilder sb = new StringBuilder();
		String frontPageName = session.getParameter("front_page");
		Runtime instance = null;
		int totMem;
		int avMem;
		int maxMem;
		boolean toGC = false;
		if (g_doGCStuff) {
			instance = Runtime.getRuntime();
			totMem = (int)(instance.totalMemory()/1048576);
			avMem = (int)(instance.freeMemory()/1048576);
			maxMem = (int)(instance.maxMemory()/1048576);
			toGC = g_dbgCurrGCCount%100 == 0;
			g_dbgCurrGCCount++;
			if (toGC)
				g_dbgCurrGCCount = 0;
			System.out.println("[GQB start:]"+frontPageName+" [Thread:]"+Thread.currentThread().getId()+" TotMem:"+totMem + " FreeMem:"+avMem +" MaxMem:"+maxMem);
			if (false && toGC) {// || true) {
				instance.gc();
				totMem = (int)(instance.totalMemory()/1048576);
				avMem = (int)(instance.freeMemory()/1048576);
				maxMem = (int)(instance.maxMemory()/1048576);
				System.out.println("[GQB start aft GC:]"+frontPageName+" [Thread:]"+Thread.currentThread().getId()+" TotMem:"+totMem + " FreeMem:"+avMem+ " MaxMem:"+maxMem);
			}
		}
		
		/*
		String perRowTemplateName = session.getParameter("input_template");
		InputTemplate perRowTemplate = null;
		if (perRowTemplateName != null && perRowTemplateName.length() != 0)
			perRowTemplate = InputTemplate.getTemplate(session.getCache(), conn, session.getParameter("page_context"), Misc.getUserTrackControlOrg(session), perRowTemplateName, session);
			*/
		
		int tempReportType = Misc.getParamAsInt(session.getParameter("report_type"),Misc.UNDEF_VALUE);
		if(tempReportType!=Misc.UNDEF_VALUE){
			reportType = tempReportType;
		}
		boolean hasNested = hasNested(fpi.m_frontInfoList);
		Table table = corePrintPage(conn, parentTable, reportType, hasNested ? null : out, frontPageName, fpi, session, searchBoxHelper, perRowTemplate, genOut,  addnlHeader);
		if("tr_transit_report.xml".equalsIgnoreCase(frontPageName))
			HtmlGenerator.postProcessTransit(table, session);
		boolean doPlainTable = perRowTemplate != null;
		if (out != null && tempReportType == Misc.CHART) {
			try {
				ChartUtil.loadVirtualShovels(session);
				int portNodeId = getPortNodeId(session, fpi.m_frontSearchCriteria, searchBoxHelper);
				String dataSource = ChartJasonGenerator.getJason(fpi, table, session,searchBoxHelper,out,portNodeId);
				
				if (Misc.getParamAsInt(session.getParameter("real_time"),Misc.UNDEF_VALUE) == 1) {
					 out.print(dataSource);
				} else if(dataSource!=null && !"null".equalsIgnoreCase(dataSource)) {
					fpi.chartInfo.setDataSource(dataSource);
					FusionCharts fChart = new FusionCharts(fpi.chartInfo);
					out.print(fChart.render());
				}else if(dataSource!=null && "null".equalsIgnoreCase(dataSource)) {
					out.print("Data not available for Some selected Parameters/Vehicle.");
				}
			} catch (ChartException e) {
				out.print(e.toString());
			}
		}/*else if(out != null && tempReportType == Misc.JSON){
			JSONArray nestedJson = JasonGenerator.printJason(table, sb, session);
			sb.append(nestedJson.toString());
			out.print(sb);
		}*/else if(out != null && tempReportType == Misc.JSON){
			JsonStreamer.printJason(table, sb, session, out);
		}else if (table.isNullStreamingGenerator()) {
			if	(out != null && reportType == Misc.HTML){
				
				HtmlGenerator.printHtmlTable(table, sb, session, doPlainTable);
				out.println(sb);
				sb.setLength(0);
				StringBuilder sb2 = printColIndexes(fpi);
				out.println(sb2);
			}
			else if (genOut != null && reportType == Misc.DOTMATRIX) {
				TextGenerator textPrinter = new TextGenerator(addnlHeader);
				textPrinter.prepare(fpi, session);
				textPrinter.printHtmlTable(table, sb, session);
				genOut.write(sb.toString());
				sb.setLength(0);
			}
			else if	(stream != null && reportType == Misc.PDF)
			{
				PdfGenerator pdfGen = new PdfGenerator();
				pdfGen.printPdf(stream, reportName, table, session, reportId);	
			}
			else if	(stream != null && reportType == Misc.EXCEL)
			{
				if(CssClassDefinition.getTemplateFile(reportId, session) != null && CssClassDefinition.getTemplateFile(reportId, session).length() > 0){
					ExcelGenerator_poi excelGen = new ExcelGenerator_poi();
					excelGen.printExcel(stream, reportName, table,session,reportId);
				}
				else{
					ExcelGenerator excelGen = new ExcelGenerator();
					excelGen.printExcel(stream, reportName, table,session,reportId);
				}
			}
			else if	(reportType == Misc.XML && servletStream != null)
			{
				XmlGenerator.printXML(table, sb, session, servletStream);
			}
			/*else if	(reportType == 5 && out != null){
				JasonGenerator.printJason(table, sb, session, servletStream);
				out.print(sb);
			}*/
			
			/*else if(reportType == Misc.JASON && servletStream !=null)
			{
			JasonGenerator.printJason(table, sb, session, servletStream)	;
			}*/
		}
		else if	(out != null && reportType == Misc.HTML){
			StringBuilder sb2 = printColIndexes(fpi);
			out.println(sb2);
		}
		if(updateIndex != null && updateIndex.size() > 0){
			session.setAttributeObj("updateIndex", updateIndex);
		}
		if (g_doGCStuff) {
			totMem = (int)(instance.totalMemory()/1048576);
			avMem = (int)(instance.freeMemory()/1048576);
			maxMem = (int)(instance.maxMemory()/1048576);
			System.out.println("[GQB end:]"+frontPageName+" Thread:"+Thread.currentThread().getId()+" TotMem:"+totMem + " FreeMem:"+avMem + " MaxMem:"+maxMem+" sbLen:"+sb.length()+ " tableRows:"+(table == null || table.getBody() == null ? 0 : table.getBody().size()));
			if (false && toGC) {// || true) {
				instance.gc();
				totMem = (int)(instance.totalMemory()/1048576);
				avMem = (int)(instance.freeMemory()/1048576);
				maxMem = (int)(instance.maxMemory()/1048576);
				System.out.println("[GQB end aft GC:]"+frontPageName+" Thread:"+Thread.currentThread().getId()+" TotMem:"+totMem + " FreeMem:"+avMem+" MaxMem:"+maxMem);
			}
		}
		return new  Pair<String, Table>(sb.toString(),table);
	}
	public StringBuilder printColIndexes(FrontPageInfo fpi) {
		StringBuilder sb = new StringBuilder();
		ArrayList<DimConfigInfo> fpiList = fpi.m_frontInfoList;
		sb.append("<script>var _jg_colIndex = new Array();");
		for (int i=0,is=fpiList.size();i<is;i++) {
			DimConfigInfo dc= fpiList.get(i);
			DimInfo dimInfo = dc == null || dc.m_dimCalc == null ? null : dc.m_dimCalc.m_dimInfo;
			sb.append(" _jg_colIndex[").append(i).append("]=").append(dimInfo == null ? -1 : dimInfo.m_id).append(";");
		}
		sb.append("</script>");
		return sb;
	}
	public String printDashBoard(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper, JspWriter out, ProcessShowResult processShowResult) throws Exception {
		StringBuilder sb = new StringBuilder();
		Table table = Table.createTable();
		ColorCodeDao.getColorCodeInfo(session.getConnection(),Misc.getParamAsInt(session.getParameter("pv123")),session,fpi.m_frontInfoList,
				searchBoxHelper);
		ResultInfo.FormatHelper formatHelper = ResultInfo.getFormatHelper(fpi.m_frontInfoList, session, searchBoxHelper); //TODO - get rid of dependency on searchBoxHelper so that formatHelper is called before processSearchBox
		boolean doTranspose = "1".equals(session.getAttribute("do_transpose"));
		printTableHeader(fpi, session, formatHelper, table);
		int rollupAtJava = fpi.m_doRollupAtJava ? 1 : 0; 
		QueryParts qp = buildQueryParts(fpi.m_frontInfoList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session, 
				searchBoxHelper, formatHelper, fpi.m_colIndexLookup, fpi.m_orderIds, processShowResult, Misc.getUndefInt(), rollupAtJava, false, fpi.m_orgTimingBased);
		String query = buildQuery(session, qp, null, false);
		System.out.println("#############"+query);
		try{
			Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(Integer.MIN_VALUE);
			ResultSet rs = stmt.executeQuery(query);
			ResultInfo resultInfo = new ResultInfo(fpi.m_frontInfoList, fpi.m_colIndexLookup, rs, session, searchBoxHelper,
					qp.m_needsRollup ? qp.m_isInclInGroupBy : null, fpi.m_colIndexUsingExpr, formatHelper, null, null, qp.m_isInclInGroupBy, null, qp.groupByRollupAggIndicator, qp.m_doRollupAtJava);
			printTable(resultInfo, fpi, searchBoxHelper, session, table, qp, null, null);
			rs.close();
			stmt.close();
			if(doTranspose)
				HtmlGenerator.printTransposedHTMLTable(sb, table);
			else
				HtmlGenerator.printHtmlTable(table, sb, session);
			} catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		return sb.toString();
	}

	public static boolean hasNestedColHeader(ArrayList<DimConfigInfo> fpList) {
		for (int i=0,is=fpList == null ? 0 : fpList.size();i<is;i++) {
			DimConfigInfo dci = fpList.get(i);
			if (dci != null && dci.m_dataSpan > 1)
				return true;
		}
		return false;
	}

	public static String generateLink(DimConfigInfo dimConfig, ArrayList<DimConfigInfo> fpList, ResultInfo rs, String topPageContext,
			SessionManager session) throws Exception { //will return null if there is no link to be printed
		return generateLink(dimConfig, fpList, rs, topPageContext, session, null, Misc.getUndefInt());
	}
	
	public static String generateLink(DimConfigInfo dimConfig, ArrayList<DimConfigInfo> fpList, ResultInfo rs, String topPageContext,
			SessionManager session, String objectIdParamLabel, int objectIdParamCol) throws Exception { //will return null if there is no link to be printed
		if (dimConfig == null || dimConfig.m_linkHelper == null)
			return null;
		DimConfigInfo.LinkHelper linkHelper = dimConfig.m_linkHelper;
		return generateLink(linkHelper, fpList, rs, topPageContext, false, session, objectIdParamLabel, objectIdParamCol, null);
	}

	
	public static String generateLink(DimConfigInfo.LinkHelper linkHelper, ArrayList<DimConfigInfo> fpList, ResultInfo rs, String topPageContext,
			boolean onlyParamPart, SessionManager session, String objectIdParamLabel, int objectIdParamCol, ArrayList<Value> altRowVals) throws Exception { //will return null if there is no link to be printed
		if (linkHelper == null)
			return null;
		StringBuilder retval = new StringBuilder();
		boolean isJavaScript = linkHelper.m_pagePart != null && linkHelper.m_pagePart.startsWith("javascript:");
		ArrayList<String> paramList = isJavaScript ? new ArrayList<String>() : null;
		if (!onlyParamPart) {
			retval.append(linkHelper.m_pagePart);
			if (linkHelper.m_fixedParamPart != null || linkHelper.m_paramName.size() != 0)
				retval.append("?");
		}
		boolean firstParamAdded = false;
		
		if (linkHelper.m_fixedParamPart != null) {
			
			retval.append(linkHelper.m_fixedParamPart);
			firstParamAdded = true;
		}
		boolean addedObjectIdParam = false;
		for (int i=0,is=linkHelper.m_paramName.size();i<is;i++) {

			MiscInner.PairStrBool paramName = linkHelper.m_paramName.get(i);
			MiscInner.PairIntStr paramVal = linkHelper.m_paramValue.get(i);
			String val = null;
			if (paramName.second) {//get from search parameter
				String pn = topPageContext+paramVal.second;
				val = session.getParameter(pn);
				if (val == null)
					val = session.getParameter("pv"+paramVal.second);
			}
			else {
				if (paramVal.first >= 0) {
					int index = paramVal.first;
					DimConfigInfo refDimConfig = fpList.get(paramVal.first);
					Value rval = rs == null ? (altRowVals == null || altRowVals.size() <= index) ? null  : altRowVals.get(index) : rs.getVal(index);
					if (rval != null && rval.isNotNull())
						val = rval.toString();
				}
				else {
					val = paramVal.second;
				}
			}
			if (val == null) {
				if (paramList != null)
					paramList.add(null);
				continue;
			}
			if (firstParamAdded)
				retval.append("&");
			//if (paramName.second)
			//	retval.append(topPageContext);
			if (objectIdParamLabel != null && objectIdParamLabel.equals(paramName.first))
				addedObjectIdParam  = true;
			retval.append(paramName.first);
			retval.append("=");
			retval.append(val);
			firstParamAdded = true;
			if (paramList != null)
				paramList.add(val);
		}
		if (!addedObjectIdParam && objectIdParamLabel != null && !Misc.isUndef(objectIdParamCol) && objectIdParamCol != -1) {
			if (firstParamAdded)
				retval.append("&");
			else
				retval.append("?");
			retval.append(objectIdParamLabel).append("=");
			int index = objectIdParamCol;
			Value rval = rs == null ? (altRowVals == null || altRowVals.size() <= index) ? null  : altRowVals.get(index) : rs.getVal(index);
			String val = null;
			if (rval != null && rval.isNotNull())
				val = rval.toString();
			retval.append(val);
			firstParamAdded = true;
			if (paramList != null) {
				if (paramList.size() == 0)
					paramList.add(val);
				else
					paramList.add(0, val);
					
			}
		}
		if(firstParamAdded){
			retval.append("&SearchButton=1");
		}else {
			retval.append("?SearchButton=1");
			firstParamAdded = true;
		}
	   
		retval.append("&_from_link=1").append("&_prev_top_page_context=").append(topPageContext);
		if (isJavaScript) {
			retval.setLength(0);
			retval.append(linkHelper.m_pagePart).append("(");
			for (int i=0,is=paramList == null ? 0 : paramList.size(); i<is; i++) {
				if (i != 0)
					retval.append(",");
				if (paramList.get(i) == null)
					retval.append("null");
				else
					retval.append("'").append(paramList.get(i)).append("'");
			}
			retval.append(")");
		}
		return retval.toString();
	}

	public void printTableHeader(FrontPageInfo fpi, SessionManager session, ResultInfo.FormatHelper formatHelper, Table table) throws Exception{
		   printTableHeader(fpi.m_frontInfoList, fpi.m_objectIdParamLabel, this.updateIndex, this.dataListToShow, session, table);
	}
	public static void printTableHeader(ArrayList<DimConfigInfo> fpList, String selectCheckBoxVarName, ArrayList<Pair<String, Integer>> updateIndex, HashMap<Integer, String> dataListToShow, SessionManager session, Table table) throws Exception{
		/*
		 *id-class 
		 *0 -tshb
		 *1 -tshc
		 *2 -cn
		 *3 -nn
		 *4 -nnGreen
		 *5 -nnYellow
		 *6 -nnRed
		 */
		boolean doingInSelectionMode = "1".equals(session.getParameter("_selectionMode"));//will print checkbox as radio
		TR tr = null; 
		TD td = null;
		String displayLink = null;
		//ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		int cols = 0;
		int index = 0;
		if(fpList != null)
			cols = fpList.size();
		tr = new TR();
		Cache cache = session.getCache();
		boolean hasMultiple = hasNestedColHeader(fpList);
		int headerClassId = hasMultiple ? 0 : 1;
		tr.setClassId(headerClassId);
		//String selectCheckBoxVarName=fpi.m_objectIdParamLabel;
		//doing top level
		tr.setId("scrollmenu");
		StringBuilder tempSB = new StringBuilder();
		for(int i=0, numsToSkip; i<cols; i += numsToSkip){
			numsToSkip = 1;
			td = new TD();
			DimConfigInfo dci = fpList.get(i);
			if (dci == null )
				continue;
			if(dci.m_hidden)
				td.setHidden(true);
			DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
			int attribType = dimInfo != null ? dimInfo.getAttribType() : cache.STRING_TYPE;
            boolean doDate = attribType == cache.DATE_TYPE;
            boolean doInterval = dimInfo != null && "20510".equals(dimInfo.m_subtype);
            String colSpanLabel = dci.m_frontPageColSpanLabel;
            if(dimInfo != null && dataListToShow.containsKey(dimInfo.m_id)){
            	continue;
            }
			
			for (int l1=i+1, l1s = colSpanLabel == null || colSpanLabel.length() == 0 ? 0 : cols; l1<l1s;l1++, numsToSkip++) {
				if (!colSpanLabel.equals(fpList.get(l1).m_frontPageColSpanLabel))
					break;
			}
			td.setContentType(doDate ? attribType : doInterval ? Misc.getParamAsInt(dimInfo.m_subtype,2) : attribType); 
			td.setColSpan(hasMultiple && numsToSkip > 1 ? numsToSkip:1);
			td.setRowSpan(hasMultiple && numsToSkip <= 1 ? 2:1);
			//get the number of contigous cols sharing the same colSpan label
			
			//numsToSkip = dci.m_dataSpan > 1 ? dci.m_dataSpan : 1;
			if (numsToSkip > 1)
				td.setColSpan(numsToSkip);
			if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0) {
				tempSB.setLength(0);
				FrontPageInfo.getAllListMenuDiv(session, dci, tempSB, true, null);
				if (tempSB.length() == 0) {
					//no priv
					td.setHidden(true);
				}
			}
			boolean ignore = dci.m_isSelect || (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0);
			td.setDoIgnore(ignore);
			boolean doSortLink = dci.m_dataSpan <= 1;
			String name = numsToSkip > 1 ? dci.m_frontPageColSpanLabel : dci.m_name;
			if (doSortLink){
				displayLink = "<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>";
			//	displayLink	+= name != null && name.length() != 0 ? name : "&nbsp;";
			}
			else {
				displayLink = null;
			}

			td.setContent(name);
			td.setLinkAPart(displayLink);
			if(dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null)
			{
				int paramid = dci.m_dimCalc.m_dimInfo.m_id;
				td.setId(paramid);
				if(paramid == 20206 || paramid == 20205 || paramid == 20204 || paramid == 20203 || paramid == 20202)
					updateIndex.add(new Pair<String, Integer>(name, index));
			}
			index++;
			if (dci.m_isSelect){
				
				if (!doingInSelectionMode) {
					displayLink = "<br/><input type='checkbox' name='select_"+selectCheckBoxVarName+"' class='tn' onclick='setSelectAll(this)'/>";
				}
				else {
					displayLink = "<br/><input type='radio' name='"+selectCheckBoxVarName+"' class='tn' onclick='autoSelect(this)'/>";
				}
				td.setDisplay(displayLink);
			}
			tr.setRowData(td);  	
		}
		table.setHeader(tr);
		if (hasMultiple) {
			tr = new TR();
			tr.setClassId(headerClassId);
			for(int i=0, numsToConsider=0; i<cols; i++,numsToConsider--){
				DimConfigInfo dci = fpList.get(i);
				if (dci == null) 
					continue;
				if (numsToConsider <= 0) {
					int numsToSkip = 1;
					String colSpanLabel = dci.m_frontPageColSpanLabel;
					for (int l1=i+1, l1s = colSpanLabel == null || colSpanLabel.length() == 0 ? 0 : cols; l1<l1s;l1++, numsToSkip++) {
						if (!colSpanLabel.equals(fpList.get(l1).m_frontPageColSpanLabel))
							break;
					}
					
					numsToConsider = numsToSkip;
					if (numsToConsider <= 1)
						continue;
				}
				
				td = new TD();
				if(dci.m_hidden)
					td.setHidden(true);
				
				DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
				int attribType = dimInfo != null ? dimInfo.getAttribType() : cache.STRING_TYPE;
	            boolean doDate = attribType == cache.DATE_TYPE;
	            boolean doInterval = dimInfo != null && "20510".equals(dimInfo.m_subtype);
				td.setContentType(doDate ? attribType : doInterval ? Misc.getParamAsInt(dimInfo.m_subtype,2) : attribType); 
				if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0) {
					tempSB.setLength(0);
					FrontPageInfo.getAllListMenuDiv(session, dci, tempSB, true, null);
					if (tempSB.length() == 0) {
						//no priv
						td.setHidden(true);
					}
				}
				if(dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null)
				{
					int paramid = dci.m_dimCalc.m_dimInfo.m_id;
					td.setId(paramid);
				}
				boolean doSortLink = true;
				if (doSortLink){
					displayLink = "<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>";
					//displayLink += dci.m_name != null && dci.m_name.length() != 0 ? dci.m_name : "&nbsp;";
					//displayLink += "</a>";
				}

				td.setContent(dci.m_name);
				td.setLinkAPart(displayLink);
				if (dci.m_isSelect) {
					
					if (!doingInSelectionMode) {
						displayLink = "<br/><input type='checkbox' name='select_'"+selectCheckBoxVarName;
						displayLink	+= "' class='tn' onclick='setSelectAll(this)'/>";
					}
					else {
						displayLink = "<br/><input type='radio' name='"+selectCheckBoxVarName;
						displayLink	+= "' class='tn' onclick='autoSelect(this)'/>";
					}
					td.setDisplay(displayLink);
				}
				tr.setRowData(td);	
			}
			table.setHeader(tr);
		}

	}

	public void printTable(ResultInfo resultInfo, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, SessionManager session,
			Table table, QueryParts qp, Writer genOut, StringBuilder addnlHeader) throws Exception{
		printTable(resultInfo, fpi, searchBoxHelper, session, table, qp, null, genOut, addnlHeader);
	}

	public void printTable(ResultInfo resultInfo, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, SessionManager session,
			Table table, QueryParts qp,ProcessShowResult processDataToShowResult, Writer genOut, StringBuilder addnlHeader) throws Exception {
		
		printTable(resultInfo, fpi, searchBoxHelper, session, table, qp, processDataToShowResult, null, genOut, addnlHeader);
	}
	private static Connection checkIfNestedFPAndParamsToPass(ArrayList<DimConfigInfo> fpList, SessionManager session, String pgContext, ArrayList<Pair<Integer, Integer>> dcIndexDimIdToPassAsParam, ArrayList<FrontPageInfo> nestedFrontPageInfo) throws Exception {
		//return non-null Connection to use if nested - the returned connection needs to be closed by the caller
		//Other "results" are returned , ArrayList<Pair<Integer, Integer>> dcIndexDimIdToPassAsParam, ArrayList<FrontPageInfo> nestedFrontPageInfo  
		boolean foundNested = false;
		
		Connection newConn = null;
		for (int i=0,is=fpList == null ? 0 : fpList.size(); i<is;i++) {
			DimConfigInfo dci = fpList.get(i);
			if (dci != null) {
				if (dci.m_customField1 != null && dci.m_customField1.length() > 0) {
					int id = Misc.getParamAsInt(dci.m_customField1);
					if (Misc.isUndef(id))
							continue;
					dcIndexDimIdToPassAsParam.add(new Pair<Integer, Integer>(i,id));
				}
				if (dci.m_refMasterBlockInPFM != null && dci.m_refMasterBlockInPFM.length() > 0) {
					for (int j=nestedFrontPageInfo.size();j<i;j++)
						nestedFrontPageInfo.add(null);
				
					
					if (newConn == null)
						newConn = DBConnectionPool.getConnectionFromPoolNonWeb();
					FrontPageInfo nestedFP = null;
					try {
					nestedFP = CacheManager.getFrontPageConfig(newConn , session.getUser().getUserId(), Misc.getUserTrackControlOrg(session), pgContext, dci.m_refMasterBlockInPFM, 0, 0);
					}
					catch (Exception e) {
						nestedFP = null;
						e.printStackTrace();
						//eat it;
					}
					nestedFrontPageInfo.add(nestedFP);
					if (nestedFP != null)
						foundNested = true;	
				}
			}
		}
		if (!foundNested) {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(newConn);
			}
			catch (Exception e) {
				
			}
		}
		if (newConn == null)
			dcIndexDimIdToPassAsParam.clear();
		return newConn;
	}
	private static ArrayList<ArrayList<Value>> subTotalInJavaInitRollupHolder(int numRollup, int numCols) {
		ArrayList<ArrayList<Value>> rollupTot = new ArrayList<ArrayList<Value>>();
		for (int i=0;i<numRollup;i++) {
			ArrayList<Value> valList = new ArrayList<Value>();
			for (int j=0;j<numCols;j++) {
				valList.add(null);
			}
			rollupTot.add(valList);
		}
		return rollupTot;
	}
	private static void subTotalInJavaResetRollupHolder(ArrayList<ArrayList<Value>> rollupTot, int initFromIncl) {
		for (int i=initFromIncl, is = rollupTot.size();i<is;i++) {
			ArrayList<Value> valList = rollupTot.get(i);
			for (int j=0, js = valList.size();j<js;j++) {
				valList.set(j, null);
			}
		}
	}
	
	private static void subTotalInJavaDoCumm(ArrayList<ArrayList<Value>> rollupTot, ArrayList<Integer> groupIndicator, ArrayList<Value> row) {
		for (int i=0, is = rollupTot.size();i<is;i++) {
			ArrayList<Value> valList = rollupTot.get(i);
			for (int j=0, js = valList.size();j<js;j++) {
				Value rhs = row.get(j);
				
				
				if (rhs == null || rhs.isNull())
					continue;
				if (groupIndicator.get(j) != 0) {
					ResultInfo.setValInRow(valList, j, new Value(rhs));
					continue;
				}
				Value lhs = valList.get(j);
				if (lhs == null || lhs.isNull())
					ResultInfo.setValInRow(valList, j, new Value(rhs));
				else {
					lhs.applyOp(DimConfigInfo.ExprHelper.CalcFunctionEnum.CUMM, rhs);
				}
			}
		}
	}
	
	private static void subTotalInJavaPrepForPrintOfIndex(ArrayList<ArrayList<Value>> rollupTot, ArrayList<Integer> groupIndicator, int ofGroupIndex, int colIndexIncl) {
		ArrayList<Value> valList = rollupTot.get(ofGroupIndex);
		for (int i=colIndexIncl,is=valList.size();i<is;i++) {
			if (groupIndicator.get(i) != 0)
				valList.set(i, null);
		}
	}
	
	public boolean printTable(ResultInfo resultInfo, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, SessionManager session,
			Table table, QueryParts qp, ProcessShowResult processDataToShowResult, InputTemplate perRowTemplate, Writer genOut, StringBuilder addnlHeader) throws Exception{
		//return true if succ else false (for eg. out of memory ...
		
		int rowsCountReport = 0;
		int numRowsPerPage = Misc.getParamAsInt(session.getParameter("num_rows_page"));
		Connection newConnForNested = null;
		boolean destroyIt = false;
		try {
			HelperCellPrint helperCellPrint = new HelperCellPrint();
			
			TR tr = null;
			TD td = null;
			String displayLink;
			ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
			ArrayList<Pair<Integer, Integer>> dcIndexDimIdToPassAsParam = new ArrayList<Pair<Integer, Integer>>();
			ArrayList<FrontPageInfo> nestedFrontPageInfo = new ArrayList<FrontPageInfo>();
			String pgContext = session.getParameter("page_context");
			if (pgContext == null)
				pgContext = searchBoxHelper.m_topPageContext;
			
			newConnForNested = checkIfNestedFPAndParamsToPass(fpList, session, pgContext, dcIndexDimIdToPassAsParam, nestedFrontPageInfo);
			
			int cols = 0;
			int type = 0;
			String disp = null;
			String col = null;
			String subType = null;
			if(fpList != null)
				cols = fpList.size();
			Cache cache = session.getCache();
			int objectIndex = fpi.getColIndexByName(fpi.m_objectIdColName);
			boolean doingMultiSelect = true;//fpi.m_multiSelect;
			String selectCheckBoxVarName=fpi.m_objectIdParamLabel;
			ContextInfo contextInfo = Misc.getContextInfo(session, session.getConnection(), cache, session.getLogger(), session.getUser());
			int privIdForOrg = session.getUser().getPrivToCheckForOrg(session, null); 
			StringBuilder tempSB = new StringBuilder();
			helperCellPrint.cache = cache;
			helperCellPrint.contextInfo = contextInfo;
			helperCellPrint.doingMultiSelect = doingMultiSelect;
			helperCellPrint.fpi = fpi;
			helperCellPrint.fpiList = fpList;
			helperCellPrint.objectIndex = objectIndex;
			helperCellPrint.privIdForOrg = privIdForOrg;
			helperCellPrint.rowLinkHelper = processDataToShowResult == null ? null : processDataToShowResult.rowLinkHelperOld;
			helperCellPrint.searchBoxHelper = searchBoxHelper;
			helperCellPrint.selectCheckBoxVarName = selectCheckBoxVarName;
			helperCellPrint.session = session;
			helperCellPrint.table = table;
			helperCellPrint.tempSB = tempSB;
			boolean doingInSelectionMode = "1".equals(session.getParameter("_selectionMode"));//will print checkbox as radio
			helperCellPrint.doingInSelectionMode = doingInSelectionMode;
			helperCellPrint.addnlValueDim = Misc.getParamAsInt(session.getParameter("_addnl_selection_dimid"));
			if (helperCellPrint.addnlValueDim >= 0) {
				int colIndex = fpi.getColIndexByName("d"+helperCellPrint.addnlValueDim); 
				if (colIndex >= 0) {
					helperCellPrint.addnlValueVarIndex = colIndex;
				}
			}
			helperCellPrint.nameValueDim = Misc.getParamAsInt(session.getParameter("_name_dimid"));
			if (helperCellPrint.nameValueDim >= 0) {
				int colIndex = fpi.getColIndexByName("d"+helperCellPrint.nameValueDim); 
				if (colIndex >= 0) {
					helperCellPrint.nameVarIndex = colIndex;
				}
			}

			boolean doDashBoardStyle = perRowTemplate != null && "1".equals(perRowTemplate.m_customField2);
			int rowNumber = 0;
			long prevSkipTime = Misc.getUndefInt();
			double prevSkipDist = Misc.getUndefInt();
			int prevVehicleId = Misc.getUndefInt();
			boolean todoSkip = false;
			if (qp != null && qp.m_vehicleIdColIndex >= 0) {
				todoSkip = true;
				if (todoSkip && qp.m_hackSkipDistGap >= 0 && qp.m_distColIndex < 0)
					todoSkip = false;
				if (todoSkip && qp.m_hackSkipTimeGap >= 0 && qp.m_timeColIndex < 0)
					todoSkip = false;
				if (todoSkip && qp.m_hackSkipDistGap < 0 && qp.m_hackSkipTimeGap < 0)
					todoSkip = false;
			}
			boolean seenFirstRow = false;
			ArrayList<Integer> toSkipRowIndex = perRowTemplate != null ? new ArrayList<Integer> () : null;
			int maxColCountInTemplate = perRowTemplate != null ? perRowTemplate.getMaxColCountAndSkipRow(perRowTemplate.getRows(), toSkipRowIndex, false) : -1;
			
			
			boolean doRollupAtJava = qp != null && qp.m_doRollupAtJava && qp.m_needsRollup ;
			boolean tableNeedsMainpulationBeforePrinting = processDataToShowResult != null && processDataToShowResult.needsManipulation();
			//if needed manipulation we will do the calc in manipulation ..
			Runtime instance = g_doGCStuff ? Runtime.getRuntime() : null;
			boolean hasProjectName = session.getAttribute("_project_name") != null;
			while(resultInfo.next()){
				if (!hasProjectName) {
					if (fpi.projectNameLookupFieldIndex >= 0) {
						Value v = resultInfo.getVal(fpi.projectNameLookupFieldIndex);
						if (v != null && !v.isNull()) {
							String wbName = v.toString();
							Connection tempConnToLookupProjectName = null;
							PreparedStatement psProjNameLookup = null;
							ResultSet rsProjectNameLookup = null;
							String projectName = null;
							try {
								tempConnToLookupProjectName = DBConnectionPool.getConnectionFromPoolNonWeb();
								psProjNameLookup = tempConnToLookupProjectName.prepareStatement("select coalesce(l1.project_name, l2.project_name, l3.project_name, l4.project_name) from secl_workstation_mines_group join mines_details l1 on (l1.sn = mines_code) left outer join mines_details l2 on (l2.sn = l1.parent_mines_code) left outer join mines_details l3 on (l3.sn = l1.parent_sub_area_code) left outer join mines_details l4 on (l4.sn = l1.parent_area_code) where secl_workstation_mines_group.workstation_code = ? order by coalesce(l1.project_name, l2.project_name, l3.project_name, l4.project_name) desc");
								psProjNameLookup.setString(1, wbName);
								rsProjectNameLookup = psProjNameLookup.executeQuery();
								if (rsProjectNameLookup.next()) {
									projectName = rsProjectNameLookup.getString(1);
								}
								rsProjectNameLookup = Misc.closeRS(rsProjectNameLookup);
								psProjNameLookup = Misc.closePS(psProjNameLookup);
							}
							catch (Exception e) {
								
							}
							finally {
								Misc.closeRS(rsProjectNameLookup);
								Misc.closePS(psProjNameLookup);
								if (tempConnToLookupProjectName != null)
									DBConnectionPool.returnConnectionToPoolNonWeb(tempConnToLookupProjectName);
							}
							session.setAttribute("_project_name", projectName, false);
						}
							
					}
					hasProjectName = true;
				}
				if (this.hadTooManyCells) {
					continue; /// need to skip
				}
				if (this.needToCheckCells && this.currCells > this.maxAllowedCells) {
					
					this.hadTooManyCells = true;
					if (instance == null)
						instance = Runtime.getRuntime();
					double avMem = (int)(instance.freeMemory()/1048576);
					int maxMem = (int)(instance.maxMemory()/1048576);
					int totMem = (int)(instance.totalMemory()/1048576);
					System.out.println("[GQB]Too many cells Thread:"+Thread.currentThread().getId()+" AvMem:"+avMem+ " Row:"+rowsCountReport+ " TotMem:"+totMem+ " MaxMem:"+maxMem);
					continue;
				}
				rowsCountReport++;
				if (instance != null) {
					double avMem = (int)(instance.freeMemory()/1048576);
					int maxMem = (int)(instance.maxMemory()/1048576);
					int totMem = (int)(instance.totalMemory()/1048576);
					if (avMem < 1 && Math.abs(totMem-maxMem) < 5) {
						this.hadTooManyCells = true;
						System.out.println("[GQB] OutOfMemory [Thread:]"+Thread.currentThread().getId()+" AvMem:"+avMem+ " Row:"+rowsCountReport+ " TotMem:"+totMem+ " MaxMem:"+maxMem);
						continue;
					}
				}
				
				if (perRowTemplate != null && seenFirstRow) { //print page br indicator
					boolean doPageBreak = numRowsPerPage > 0 && (rowsCountReport-1) % numRowsPerPage == 0; 
					tr = new TR();
					table.setBody(tr);
					td = new TD();
					if (this.needToCheckCells)
						this.currCells++;
					td.setColSpan(maxColCountInTemplate);
					td.setDisplay( "<hr noshade size=\"1\">");
					tr.setRowData(td);
					if (doPageBreak) {
						tr.setClassId(16);//extPageBreak
					}
				}
				seenFirstRow = true;
				//if (!g_doRollupAtJava  && resultInfo.isCurrEqualToRelativeRow(1)) {//HUH DEBUG13 why is this needed?
				//	continue;
				//}
				
				boolean checkAll = true;
				boolean doGroup = resultInfo.isRollupRow();
				int groupedItemIndex = Misc.g_doRollupAtJava ? resultInfo.getCurrColBeingRolledUp() : (doRollupAtJava ? - 1 : doGroup ? resultInfo.getCurrColBeingRolledUp() : -1);
				DimConfigInfo groupedDCI = groupedItemIndex < 0 ? null : fpList.get(groupedItemIndex);
				if (groupedDCI != null && !groupedDCI.m_doRollupTotal) {
					continue;
				}
				if (doGroup && processDataToShowResult != null && processDataToShowResult.hasPivoting())
					continue; //grouping done in manip
				rowNumber++;
				if(groupedDCI != null)
				{
					if(groupedDCI.m_doRollupTotal)
					{
						checkAll = false;
					}
				}
				else if (doGroup && doRollupAtJava)
					checkAll = false;
				if (!doGroup && todoSkip) {
					int currVehicleId = resultInfo.getVal(qp.m_vehicleIdColIndex).m_iVal;
					Date ct = resultInfo.getVal(qp.m_timeColIndex).getDateVal();
					long currTime = ct == null ? Misc.getUndefInt() : ct.getTime();
					double cd = resultInfo.getVal(qp.m_distColIndex).m_dVal;
					boolean toPrint = true;
					if (currVehicleId == prevVehicleId) {
						int tg = (int)((double)(currTime - prevSkipTime)/60000.0);
						double dg = cd - prevSkipDist;
						if (qp.m_hackSkipDistGap >= 0 && dg < qp.m_hackSkipDistGap)
							toPrint = false;
						if (qp.m_hackSkipTimeGap >= 0 && tg < qp.m_hackSkipTimeGap)
							toPrint = false;
					}
					if (toPrint) {
						prevVehicleId = currVehicleId;
						prevSkipTime = currTime;
						prevSkipDist = cd;
					}
					else {
						continue;
					}
				}
				
				int linkMenuIndex = 0;
				helperCellPrint.linkMenuIndex = linkMenuIndex;
				helperCellPrint.groupedItemIndex = groupedItemIndex;
				helperCellPrint.doGroup = doGroup;
				helperCellPrint.checkAll = checkAll;
				helperCellPrint.rowNumber = rowNumber;
				boolean isPivotGrDiff = processDataToShowResult == null || resultInfo.isCurrDifferentForSelDimIndex(processDataToShowResult.dimIndexToPivot);
				if (perRowTemplate == null) {
					tr = new TR();
					tr.setPivotChange(isPivotGrDiff);
					for(int i=0; i<cols; i++) {
						td = this.printCell(helperCellPrint, resultInfo, i, doRollupAtJava || tableNeedsMainpulationBeforePrinting);
						tr.setRowData(td);
					}//end of for each col
					table.setBody(tr);
				}
				else {//for perRowTemplate
					int colIndex = 0;
					ArrayList<MiscInner.Pair> colsForRowSpan = null;
					for (int i=0,is=perRowTemplate.getRows().size();i<is;i++) {
						ArrayList<DimConfigInfo> row = perRowTemplate.getRows().get(i);
						boolean toSkip = false;
						for (int j=0,js = toSkipRowIndex.size();j<js;j++) {
							if (toSkipRowIndex.get(j) == i) {
								colIndex += row.size();
								toSkip= true;
								break;
							}
						}
						if (toSkip) {
							helperReduceColsForRowSpan(colsForRowSpan);
							continue;
						}
						int perRowIterBeg = doDashBoardStyle ? 0 : 1;
						//if doing perRowIterSz ... we will first print one row for labels and then second row forValue
						for (int art = perRowIterBeg; art < 2; art++) {
							tr = new TR();
							table.setBody(tr);
							MiscInner.Pair labelDatacolsUsed = helperGetColsForRowSpans(colsForRowSpan);
							int colsUsed = labelDatacolsUsed.second;
							if (doDashBoardStyle)
								colsUsed += labelDatacolsUsed.first;
							for (int j=0,js = row.size();j<js;j++) {
								DimConfigInfo dimConfig = row.get(j);
								
								if (dimConfig.m_hidden) {
									if (art == 1)
										colIndex++;
									continue;
								}
								
								int hideControl = dimConfig.m_hiddenSpecialControl;
								boolean toHide = hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_POSTCREATE;
								if (toHide) {
									continue;
								}
								boolean noColLabel = dimConfig.m_name == null || dimConfig.m_name.length() == 0;
								if (!noColLabel && (art == 0 || !doDashBoardStyle)) {
									
									int labelClassId = dimConfig.m_labelStyleClass == null || dimConfig.m_labelStyleClass.length() == 0 ? 7 : CssClassDefinition.getClassIdByClassName(dimConfig.m_labelStyleClass);
									td = new TD();
									if (this.needToCheckCells)
										this.currCells++;
									td.setClassId(labelClassId);
								
									if (dimConfig.m_labelNowrap)
										td.setNoWrap(true);
									else if (dimConfig.m_labelWidth > 0) {
										td.setWidth(dimConfig.m_labelWidth);
									}
									String labelDisp = dimConfig.m_name == null || dimConfig.m_name.length() == 0 ? "&nbsp;" : dimConfig.m_name; 
									if (7 == labelClassId && dimConfig.m_name != null && !dimConfig.m_name.endsWith(":"))
										labelDisp += ":";
									if (!doDashBoardStyle && dimConfig.m_rowSpan > 1) {
										td.setRowSpan(dimConfig.m_rowSpan);
									}
									
									if (dimConfig.m_refMasterBlockInPFM != null) {
										String disLink = "   <a  class='tn' style='margin-left:0px' href=\"javascript:popCustomizeFPLink('"+pgContext;
										disLink	+= "', '"+ dimConfig.m_refMasterBlockInPFM+ "',"+ Misc.getUndefInt()+ ")\">";
										disLink	+= "Customize</a>";
										labelDisp+=disLink;
										disLink=null;
									} 
									
									td.setContent(labelDisp);
									int dataColspan = 1;
									if (doDashBoardStyle) {
										dataColspan = dimConfig.m_dataSpan > 0 && dimConfig.m_dataSpan < 1000 ? 1 : dimConfig.m_dataSpan;
										if (j == js-1)
											dataColspan = maxColCountInTemplate-colsUsed;
										else
											dataColspan++;
									}
								    if (dataColspan <= 1)
								    	dataColspan = 1;
								   colsUsed += dataColspan;
								   if (dataColspan > 1)
									   td.setColSpan(dataColspan);
								   tr.setRowData(td);
								}
								
								if (art == 0)
									continue; //dont do rest of the stuff
								//now printing for data row
								if (art == 1 && doDashBoardStyle) {
									td = new TD();
									if (this.needToCheckCells)
										this.currCells++;
									td.setContent("&nbsp;&nbsp;");
									colsUsed++;
									if (dimConfig.m_rowSpan > 1) {
										td.setRowSpan(dimConfig.m_rowSpan);
									}
									tr.setRowData(td);
								}
								colIndex++;
								if (dimConfig.m_refMasterBlockInPFM == null || dimConfig.m_refMasterBlockInPFM.length() == 0) {
									//regular cell
									td = this.printCell(helperCellPrint, resultInfo, colIndex-1, doRollupAtJava || tableNeedsMainpulationBeforePrinting);
									
								}
								else {
									td = new TD(); //TODO for streaming
									if (this.needToCheckCells)
										this.currCells++;
								}
															
								int dataColspan = dimConfig.m_dataSpan > 0 && dimConfig.m_dataSpan < 1000 ? 1 : dimConfig.m_dataSpan;
								if (noColLabel)
									dataColspan++;
								if (j == js-1)
									dataColspan = maxColCountInTemplate-colsUsed;
								
								if (dataColspan > 1)
									td.setColSpan(dataColspan);
								if (dimConfig.m_rowSpan > 1)
									td.setRowSpan(doDashBoardStyle ? dimConfig.m_rowSpan*2-1 : dimConfig.m_rowSpan);
								colsUsed += dataColspan;
								if (dimConfig.m_nowrap)
									td.setNoWrap(true);
								td.setClassId(8);//tn
								tr.setRowData(td); //TODO streaming
								FrontPageInfo fpiNested = nestedFrontPageInfo.size() > (colIndex-1) ? nestedFrontPageInfo.get(colIndex-1) : null;
								if (dimConfig.m_refMasterBlockInPFM != null && dimConfig.m_refMasterBlockInPFM.length() != 0 && fpiNested != null) {
									//set up adornments of TD and add and set for partial print
									//then create 
									session.rememberSessionVars();
									//			ArrayList<Pair<Integer, Integer>> dcIndexDimIdToPassAsParam = new ArrayList<Pair<Integer, Integer>>();
									//ArrayList<FrontPageInfo> nestedFrontPageInfo = new ArrayList<FrontPageInfo>();
									String dbgpv3 = session.getParameter("pv35219");
									String dbgpv4 = session.getAttribute("pv35219");

									//session.setAttribute("_from_link","1", false);//@#@#@#@
									//session.setAttribute("_prev_top_page_context",searchBoxHelper.m_topPageContext, false);
									DimConfigInfo dci0 = fpList.get(0);
									String objectVal = objectIndex < 0 ? null : resultInfo.getValStr(objectIndex, false, false, "");
									String objectName = selectCheckBoxVarName;
									//session.setAttribute("is_cached", "0", false);
									if (objectName != null && objectVal != null)
										session.setAttribute(objectName, objectVal, false);
									for (int k=0,ks=dcIndexDimIdToPassAsParam.size();k<ks;k++) {
										int dciIdx = dcIndexDimIdToPassAsParam.get(k).first;
										int dimIdToPass = dcIndexDimIdToPassAsParam.get(k).second;
										Value passVal = resultInfo.getVal(dciIdx);
										String passValStr = passVal == null ? null : passVal.m_type == Cache.DATE_TYPE ? resultInfo.getValStr(dciIdx) : passVal.toString();
										String passParam = searchBoxHelper.m_topPageContext+dimIdToPass;
										session.setAttribute(passParam, passValStr, false);
									}
									
									String frontPageNameNested = dimConfig.m_refMasterBlockInPFM; 
									//HACK ... processSearchBox uses session.getConnection for guessShiftId which causes streaming exception
									Connection tempConn = null;
									try {
										tempConn = session.getConnection();
										session.request.setAttribute("_dbConnection", newConnForNested);
										String dbgpv5 = session.getParameter("pv35219");
										String dbgpv6 = session.getAttribute("pv35219");

										MiscInner.SearchBoxHelper searchBoxHelperNested = PageHeader.processSearchBox(session, searchBoxHelper.m_privIdForOrg, pgContext, fpiNested.m_frontSearchCriteria, null);
										String dbgpv7 = session.getParameter("pv35219");
										String dbgpv8 = session.getAttribute("pv35219");

										Table nestedTable = corePrintPage(newConnForNested, table, table.getStreamingReportType(), table.getStreamingOut(), frontPageNameNested, fpiNested, session, searchBoxHelperNested, null, genOut, addnlHeader);
										td.setNestedTable(nestedTable);//TODO for streaming
									}
									catch (Exception e3) {
										destroyIt = true;
										
										e3.printStackTrace();
										throw e3;
									}
									finally {
										if (tempConn != null) {
											session.request.setAttribute("_dbConnection", tempConn);
										}
									}
							//		session.setAttribute("_from_link","0", false);
									session.setToRememberedVars();
									String dbgpv10 = session.getParameter("pv35219");
									String dbgpv11 = session.getAttribute("pv35219");
									String dbgpv12 = null;
								}
								if (dimConfig.m_rowSpan > 1) {
									colsForRowSpan = helperAddColsForRowSpan(colsForRowSpan, dimConfig.m_rowSpan, dimConfig.m_dataSpan);
								}
							}//end of col
							helperReduceColsForRowSpan(colsForRowSpan);
						}//art row for iteration of dashboardStyle versus regular style
					}//end of row
				}//for perRowTemplate				
			}//for each row of data
		} 
			catch (Exception e) {
			// TODO Auto-generated catch block
			destroyIt = true;
			e.printStackTrace();
			throw e;
		}
		finally {
			if (newConnForNested != null) {
				DBConnectionPool.returnConnectionToPoolNonWeb(newConnForNested, destroyIt);
				newConnForNested = null;
			}
		}
		if (this.hadTooManyCells) {
			if (table != null) {
				TR dbgTR = new TR();
				table.getBody().add(0, dbgTR);
				TD dbgTD = new TD();
				dbgTD.setContent("System running low on memory - Please contact IntelliPlanner for resolution and/or run report with lesser vehicles/date range");
				dbgTD.setClassId(1);
				dbgTD.setColSpan(1000);
				dbgTR.setRowData(dbgTD);
			}
		}
		return !this.hadTooManyCells;
	}
    public static MiscInner.Pair helperGetColsForRowSpans(ArrayList<MiscInner.Pair> rowSpansSpec) {
    	int labelCnt = 0;
    	int cnt = 0;
    	for (int i=0,is = rowSpansSpec == null ? 0 : rowSpansSpec.size(); i<is; i++) {
			if (rowSpansSpec.get(i).first > 1) {
				cnt+= rowSpansSpec.get(i).second;
				labelCnt++;
			}
		}
    	return new MiscInner.Pair(labelCnt, cnt);
    }
    private static int helperRemovePivotColsAndGetFirstPivot(ArrayList<TD> row, ArrayList<Integer> dimIndexPivot, ArrayList<Integer> dimMeasurePivot, ArrayList<Integer> qpGroupAddIndicator) {
		int retval = dimIndexPivot.get(0);
		ArrayList<Integer> tempMeasureIndexAdj = new ArrayList<Integer>();
		for (int i=0,is=dimMeasurePivot.size(); i<is; i++) {
			tempMeasureIndexAdj.add(0);
		}
		//first remove dimPivot ... while also noting adj for measure
		for (int i=dimIndexPivot.size()-1;i>=0;i--) {
			int idx = dimIndexPivot.get(i);
			row.remove(idx);
			if (qpGroupAddIndicator != null)
				qpGroupAddIndicator.remove(idx);
			for (int j=dimMeasurePivot.size()-1; j>=0; j--) {
				if (dimMeasurePivot.get(j) > idx)
					tempMeasureIndexAdj.set(j, tempMeasureIndexAdj.get(j)+1);
				else
					break;
			}
		}
    	
		for (int j=dimMeasurePivot.size()-1; j>=0; j--) {
			int idx = dimMeasurePivot.get(j)-tempMeasureIndexAdj.get(j);
			row.remove(idx);
			if (qpGroupAddIndicator != null)
				qpGroupAddIndicator.remove(idx);
			if (idx < retval)
				retval--;
		}
		return retval;
    }
    public void manipulateTablePost(Table table, Connection conn, ArrayList<DimConfigInfo> dimConfigList, ArrayList<ArrayList<DimConfigInfo>> searchBox,
			boolean fpi_m_hackTrackDriveTimeTableJoinLoggedData, SessionManager session, SearchBoxHelper searchBoxHelper,
			ResultInfo.FormatHelper formatHelper, HashMap<String,Integer> nameIndexLookup, int driverObject, ArrayList<Integer> orderIds, ProcessShowResult processShowResult, ResultInfo resultInfo, int doMeasureInFirstRowSpec, CopyOfGeneralizedQueryBuilder.QueryParts qp) throws Exception {
    		//1st get list of values for each
    	//2nd create total if needed
    	//3rd update the row number col if any
    	boolean doMeasureInFirstRow = doMeasureInFirstRowSpec == 1;
    	ArrayList<TR> rows = table.getBody();
    	ArrayList<Integer> dimIndexPivot = processShowResult.dimIndexToPivot;
    	ArrayList<Integer> dimMeasurePivot = processShowResult.dimIndexOfPivotMeasure;
    	if (dimIndexPivot != null && dimIndexPivot.size() > 0 && dimMeasurePivot != null && dimMeasurePivot.size() > 0) {
    		ArrayList<ArrayList<Value>> expandedValsOfPivotCol = getExpandedValsOfPivotCols(table, conn, dimConfigList, session, searchBoxHelper, processShowResult);

    		//put in header ..
    		ArrayList<TR> headerRows = table.getHeader();
    		TR firstHeaderRow = headerRows.get((headerRows!=null && headerRows.size()>1?1:0));//Initially was 0 but at 0 there is search criteria now getting NullPOinter
    		
    		ArrayList<TD> tdHeaderOfDim = new ArrayList<TD>();
    		for (int i=0,is=dimIndexPivot.size(); i<is; i++) {
    			tdHeaderOfDim.add(firstHeaderRow.get(dimIndexPivot.get(i)));
    		}
    		
    		ArrayList<TD> tdHeaderOfMeasure = new ArrayList<TD>();
    		int hiddenCnt = 0;
    		ArrayList<Integer> groupIndicatorForPivotMeasure = new ArrayList<Integer>();
    		for (int i=0,is=dimMeasurePivot.size(); i<is; i++) {
    			tdHeaderOfMeasure.add(firstHeaderRow.get(dimMeasurePivot.get(i)));
    			if (tdHeaderOfMeasure.get(tdHeaderOfMeasure.size()-1).getHidden())
    				hiddenCnt++;
    			groupIndicatorForPivotMeasure.add(qp.groupByRollupAggIndicator.get(dimMeasurePivot.get(i)));
    		}

    		boolean putMeasureInHeader = dimMeasurePivot.size() > 1;
    		int firstPivotIndex = helperRemovePivotColsAndGetFirstPivot(firstHeaderRow.getRowData(), dimIndexPivot, dimMeasurePivot, qp.groupByRollupAggIndicator);
    		
    		int numExpandedCols = expandedValsOfPivotCol == null ? 0 : expandedValsOfPivotCol.size();
    		int sz = numExpandedCols;
    		if (processShowResult.doTotalAcrossColInPivot)
    			numExpandedCols++;
    		
    		int rowSpanForNewCell = doMeasureInFirstRow ? numExpandedCols : dimMeasurePivot.size()-hiddenCnt;//some 1st row span over hidden causes rendering problem
    		
    		boolean hasMultiple = true;//rowSpanForNewCell > 1; want to print col header always
    		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
    		Cache cache = session.getCache();
    		if (hasMultiple) {//make sure we have rowSpan=2 where rowSpan=1 for first headerRow
    			ArrayList<TD> cols = firstHeaderRow.getRowData();
    			for (int i=0,is = cols.size(); i<is; i++) {
    				TD col = cols.get(i);
    				if (col.getRowSpan() == 1)
    					col.setRowSpan(2);
    			}
    		}
    		int headerClassId = hasMultiple ? 0 : 1;
    		firstHeaderRow.setClassId(headerClassId);
    		
    		TR secondHeaderRow = null;
    		if (hasMultiple) {
    			secondHeaderRow = new TR();
    			secondHeaderRow.setClassId(firstHeaderRow.getClassId());
    			table.setHeader(secondHeaderRow);
    		}
			
    		if (doMeasureInFirstRow && hasMultiple) {
    			for (int k=0,ks=dimMeasurePivot.size();k<ks;k++) {
					int midx = dimMeasurePivot.get(k);
					DimConfigInfo dci = dimConfigList.get(midx);
					String name = dci.m_name;
        			
        			TD td = tdHeaderOfMeasure.get(k).copy();
        			td.setColSpan(dci.doTotOnlyInPivotForMeasure ? 1 : rowSpanForNewCell);
        			td.setRowSpan(1);
        			//get the number of contigous cols sharing the same colSpan label
        			td.setLinkAPart(null);
        			td.setDoIgnore(false);
        			td.setContent(name);
        			firstHeaderRow.setRowData(k+firstPivotIndex, td);
				}
    		}
    		for (int art=0,arts = doMeasureInFirstRow ? dimMeasurePivot.size() : 1; art<arts;art++) { 
	    		for (int i=0; i<numExpandedCols; i++) {
	    			StringBuilder sb = new StringBuilder();
	    			int midx = doMeasureInFirstRow ? dimMeasurePivot.get(art) : -1;
					DimConfigInfo measuredci = midx < 0 ? null : dimConfigList.get(midx);
					boolean showNonTot = measuredci == null || ! measuredci.doTotOnlyInPivotForMeasure;
	    			
	    			if (i <sz) {
	    				if (!showNonTot)
	    					continue;
	    				ArrayList<Value> pivotVals = expandedValsOfPivotCol.get(i);
		    			for (int j=0,js=pivotVals.size(); j<js; j++) {
		    				Value pivotVal = pivotVals.get(j);
		    				int idx = dimIndexPivot.get(j);
		    				DimConfigInfo dci = dimConfigList.get(idx);
		    				DimInfo dimInfo = dci == null || dci.m_dimCalc == null ? null : dci.m_dimCalc.m_dimInfo; 
		    				String str = pivotVal.toString(dimInfo, formatHelper.multScaleFactors.get(idx), formatHelper.formatters.get(idx), session, session.getCache(), conn, sdf);
		    				if (sb.length() != 0)
		    					sb.append(", ");
		    				sb.append(str);
		    			}
	    			}
	    			else {
	    				sb.append("Total");
	    			}
	    			
	    			TD td = tdHeaderOfMeasure.get(0).copy();
	    			td.setDoGroup(i >= sz);
	    			td.setContent(sb.toString());
	    			td.setColSpan(doMeasureInFirstRow ? 1 : rowSpanForNewCell);
	    			td.setRowSpan(1);
	    			if (hasMultiple && !doMeasureInFirstRow)
	    				td.setLinkAPart(null);
	    			td.setDoIgnore(false);
	    			if (doMeasureInFirstRow) {
	    				if (tdHeaderOfMeasure.get(art).getHidden()) {
	    					td.setHidden(true);
	    				}
	    			}
	    			if (hasMultiple && doMeasureInFirstRow) {
	    				secondHeaderRow.setRowData(td);
	    			}
	    			else {
	    				firstHeaderRow.setRowData(i+firstPivotIndex, td);
	    			}
	    			if (doMeasureInFirstRow || !hasMultiple) {
	    				qp.groupByRollupAggIndicator.add(i+firstPivotIndex, groupIndicatorForPivotMeasure.get(art));
	    			}
	    		}//for each expandedPivotCol
    		}
    		if (hasMultiple && !doMeasureInFirstRow) { //now print measureCol
    			int compositeAddAtIndex = firstPivotIndex;
    			
    			for (int i=0; i<numExpandedCols; i++) {
    				for (int k=0,ks=dimMeasurePivot.size();k<ks;k++) {
    					int midx = dimMeasurePivot.get(k);
    					DimConfigInfo dci = dimConfigList.get(midx);
    					String name = dci.m_name;
	        			
	        			TD td = tdHeaderOfMeasure.get(k).copy();
	        			td.setColSpan(1);
	        			td.setRowSpan(1);
	        			//get the number of contigous cols sharing the same colSpan label
	        			td.setDoIgnore(false);
	        			td.setContent(name);
	        			secondHeaderRow.setRowData(td);
		    			qp.groupByRollupAggIndicator.add(compositeAddAtIndex, groupIndicatorForPivotMeasure.get(k));
		    			compositeAddAtIndex++;
    				}
        		}//for each expandedPivotCol
    		}//for each measure
    				
    		//now print rows
    		ArrayList<Integer> rollupIndices = qp.getRollupIndices();
    		boolean doRollup = qp.m_doRollupAtJava && qp.m_needsRollup;
    		ArrayList<ArrayList<Value>> rollupGroupTot = doRollup ? 
    				CopyOfGeneralizedQueryBuilder.subTotalInJavaInitRollupHolder(rollupIndices.size(), qp.groupByRollupAggIndicator.size()) 
    				: null;
    		ArrayList<Value> prevDataRow = new ArrayList<Value>();
    		ArrayList<Value> currDataRow = new ArrayList<Value>();
    		for (int i=0,is=qp.groupByRollupAggIndicator.size(); i<is; i++) {
    			prevDataRow.add(null);
    			currDataRow.add(null);
    		}
    		
    		ArrayList<String> aggregateOpOfMeasure = new ArrayList<String>();
			for (int l=0,ls = dimMeasurePivot.size(); l<ls;l++) {
				int measureIndex = dimMeasurePivot.get(l);
				aggregateOpOfMeasure.add(helperGetAggregateOp(dimConfigList.get(measureIndex), searchBoxHelper, session));
			}
			int prevRowIndex = -1;
    		for (int r=0;r< (rows == null ? 0 : rows.size());r++) {//rows.size() changes within the loop
    			TR firstRow = rows.get(r);
    			int compositeAddAtIndex = firstPivotIndex;
    			
    			int endExcl = r+1;
    			for (int rsz=rows.size(); endExcl < rsz; endExcl++)
    				if (rows.get(endExcl).isPivotChange()) 
    					break;
    			
    			int prevPivotColPos = -1;
    			int indexToAddValAt = firstPivotIndex;
    			ArrayList<TD> tdOfMeasureFirstRow = null;
				ArrayList<Value> summOfMeasure = new ArrayList<Value>();
		 		ArrayList<Value> prevVals = new ArrayList<Value>();
		
				for (int l=0,ls = dimMeasurePivot.size(); l<ls;l++) {
					DimConfigInfo dci = dimConfigList.get(dimMeasurePivot.get(l));
					int attribType = dci == null || dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null ? Cache.INTEGER_TYPE : dci.m_dimCalc.m_dimInfo.m_type;
					Value nv = null;
					if (attribType ==Cache.NUMBER_TYPE)
						nv = new Value(Misc.getUndefDouble());
					else if (attribType == Cache.STRING_TYPE)
						nv = new Value ((String) null);
					else if (attribType == Cache.DATE_TYPE)
						nv = new Value(0L);
					else
						nv = new Value(Misc.getUndefInt());
					prevVals.add(null);
					summOfMeasure.add(nv);
				}
				///@#@#@#
    			for (int j=r;j<endExcl;j++) {
    				TR currRow = rows.get(j);
    				int pivotValColPos = getRowIndexInExpandedVals(currRow, expandedValsOfPivotCol, dimIndexPivot, prevPivotColPos);
    				
    				ArrayList<TD> tdOfMeasure = new ArrayList<TD>();
    				if (tdOfMeasureFirstRow == null)
    					tdOfMeasureFirstRow = tdOfMeasure;
    				ArrayList<Value> valueOfMeasure = new ArrayList<Value>();
    				
    				for (int l=0,ls = dimMeasurePivot.size(); l<ls;l++) {
						int measureIndex = dimMeasurePivot.get(l);
						tdOfMeasure.add(currRow.get(measureIndex));
						valueOfMeasure.add(currRow.get(measureIndex).getOptionalValue());
    				}
    				helperRemovePivotColsAndGetFirstPivot(currRow.getRowData(), dimIndexPivot, dimMeasurePivot, null);
    				if (doMeasureInFirstRow && j == r) {//create place holders
    					int cnt = firstPivotIndex;
    					for (int l=0,ls = dimMeasurePivot.size(); l<ls;l++) {
    						int midx = dimMeasurePivot.get(l);
    						DimConfigInfo measuredci = midx < 0 ? null : dimConfigList.get(midx);
    						boolean showNonTot = measuredci == null || ! measuredci.doTotOnlyInPivotForMeasure;
    		    			
    						for (int m=0, ms = !showNonTot ? 1 : numExpandedCols;m<ms;m++) {
        						TD valueTD = tdOfMeasure.get(l);//hack no copy ... but will be replaced below with proper stuff
        						firstRow.setRowData(cnt++, valueTD);
    						}
    					}
    				}
    				//now go thru each row for the pivot and put values in respective cells ..
    				boolean seenPivotAllVal = pivotValColPos == expandedValsOfPivotCol.size();
    				for (int k=prevPivotColPos+1, ks = (j == endExcl-1 ? sz-1 : pivotValColPos >= sz ? sz-1 : pivotValColPos);k<=ks;k++) {
    					boolean doEmpty = k != pivotValColPos;
    					int doMeasureInFirstRowCellIndex = firstPivotIndex;
    					for (int l=0,ls = dimMeasurePivot.size(); l<ls;l++) {
    						int measureIndex = dimMeasurePivot.get(l);
    						DimConfigInfo measuredci = measureIndex < 0 ? null : dimConfigList.get(measureIndex);
    						boolean showNonTot = measuredci == null || ! measuredci.doTotOnlyInPivotForMeasure;
    						
    						TD valueTD = tdOfMeasure.get(l).copy();
    						Value v = valueOfMeasure.get(l);
    						Value prevVal = prevVals.get(l);
    						if (doEmpty) {
    							if (dimConfigList.get(measureIndex).isDoJavaCumm()) {
    								v = prevVal == null ? new Value() : new Value(prevVal);
    							}
    							else {
	    							v = new Value();
    							}
    							valueTD.setContent(resultInfo.getValString(v, measureIndex, Misc.nbspString));
	    						valueTD.setOptionalAndRefDciIndexValue(v, tdOfMeasure.get(l).getOptionalDimCofigIndex());
    						}
    						ResultInfo.setValInRow(prevVals, l, v);
    						if (v != null && v.isNotNull()) {
    							summOfMeasure.get(l).applyOp(CalcFunctionEnum.getFuncCode(aggregateOpOfMeasure.get(l)), v);
    						}
    						if (showNonTot) {
	    						if (doMeasureInFirstRow) {
	    							firstRow.setButNotAddRowData(doMeasureInFirstRowCellIndex+k, valueTD);
	    						}
	    						else {
	    							firstRow.setRowData(indexToAddValAt++, valueTD);
	    						}
	    						doMeasureInFirstRowCellIndex += numExpandedCols;
    						}
    						else {
    							doMeasureInFirstRowCellIndex += 1; //assumed total to show
    						}
    					}
    				}
    				prevPivotColPos = pivotValColPos;
    			}//for each row having common non pivot
    			//add cols for total
				if (processShowResult.doTotalAcrossColInPivot) {
					int doMeasureInFirstRowCellIndex = firstPivotIndex;

					for (int l=0,ls = dimMeasurePivot.size(); l<ls;l++) {
						int measureIndex = dimMeasurePivot.get(l);
						DimConfigInfo measuredci = measureIndex < 0 ? null : dimConfigList.get(measureIndex);
						boolean showNonTot = measuredci == null || ! measuredci.doTotOnlyInPivotForMeasure;
						int deltaIfShowNonTotMeasure1stRow = showNonTot ? numExpandedCols : 1;
						TD valueTD = tdOfMeasureFirstRow.get(l).copy();
						Value v = summOfMeasure.get(l);
						String valStr = v == null || v.isNull() ? Misc.nbspString : v.toString(dimConfigList.get(measureIndex).m_dimCalc.m_dimInfo, formatHelper.multScaleFactors.get(measureIndex), formatHelper.formatters.get(measureIndex), session, session.getCache(), conn, sdf);
						valueTD.setContent(valStr);
						valueTD.setOptionalAndRefDciIndexValue(v, tdOfMeasureFirstRow.get(l).getOptionalDimCofigIndex());
						valueTD.setDoGroup(true);
						if (doMeasureInFirstRow) {
							firstRow.setButNotAddRowData(doMeasureInFirstRowCellIndex+deltaIfShowNonTotMeasure1stRow-1, valueTD);
						}
						else {
							firstRow.setRowData(indexToAddValAt++, valueTD);
						}
						doMeasureInFirstRowCellIndex += deltaIfShowNonTotMeasure1stRow;
					}
				}//if to add rows for total
				//now remove non first Row
				for (int j=endExcl-1;j>r;j--) {
					rows.remove(j);
				}
				
				//now process for sub group 
				if (doRollup) {
					//1. extract data from currRow
					ArrayList<TD> firstRowTD = firstRow.getRowData();
					for (int i=0,is=firstRowTD.size(); i<is; i++) {
						int dciIndex = firstRow.get(i).getOptionalDimCofigIndex();
						DimConfigInfo dci = dimConfigList.get(dciIndex);
						Value v = dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_dimCalc.m_dimInfo.m_id == 20356 ? 
								   null : firstRow.get(i).getOptionalValue();
						currDataRow.set(i, firstRow.get(i).getOptionalValue());
					}
					if (prevRowIndex != -1) {
						int latestNonAggColDiff = CopyOfGeneralizedQueryBuilder.subTotalInJavaGetDiff(prevDataRow, currDataRow, qp.groupByRollupAggIndicator, rollupIndices.get(rollupIndices.size()-1));
						//make above efficient - currently requires values of all cells to be remembered 
						int lastSubTotableDimConfigIndex = rollupIndices.get(rollupIndices.size()-1);
						int lastSubGroupIndexPrinted = rollupIndices.size();
						int addAfter = prevRowIndex;
						for (int i=rollupIndices.size()-1; i>=0; i--) {
							int currRollupDimConfigIndex = rollupIndices.get(i);
							if (currRollupDimConfigIndex >= latestNonAggColDiff) {//need to print Row ..
								lastSubGroupIndexPrinted = i;
								CopyOfGeneralizedQueryBuilder.subTotalInJavaPrepForPrintOfIndex(rollupGroupTot, qp.groupByRollupAggIndicator, i, rollupIndices.get(i));
								//add row before currRow & print with value
								CopyOfGeneralizedQueryBuilder.subTotalInJavaPrintRow(table, currRollupDimConfigIndex, prevRowIndex, addAfter, rollupGroupTot.get(i), qp.groupByRollupAggIndicator, dimConfigList, formatHelper, session, sdf, conn);
								addAfter++;
								r++;
							}
						}
						if (lastSubGroupIndexPrinted < rollupIndices.size()) {
							CopyOfGeneralizedQueryBuilder.subTotalInJavaResetRollupHolder(rollupGroupTot, lastSubGroupIndexPrinted);
							
						}
						
					}
					prevRowIndex = r;
					CopyOfGeneralizedQueryBuilder.subTotalInJavaDoCumm(rollupGroupTot, qp.groupByRollupAggIndicator, currDataRow);
					ArrayList<Value> tvl = prevDataRow;
					prevDataRow = currDataRow;
					currDataRow = tvl;
				}//if doing rollupAtJava
    		}//for each row .
    		if (doRollup) {
				//1. extract data from currRow
				
				if (prevRowIndex != -1) {
					int latestNonAggColDiff = 0;
					//make above efficient - currently requires values of all cells to be remembered 
					int lastSubTotableDimConfigIndex = rollupIndices.get(rollupIndices.size()-1);
					int lastSubGroupIndexPrinted = rollupIndices.size();
					int addAfter = prevRowIndex;
					for (int i=rollupIndices.size()-1; i>=0; i--) {
						int currRollupDimConfigIndex = rollupIndices.get(i);
						if (currRollupDimConfigIndex >= latestNonAggColDiff) {//need to print Row ..
							lastSubGroupIndexPrinted = i;
							CopyOfGeneralizedQueryBuilder.subTotalInJavaPrepForPrintOfIndex(rollupGroupTot, qp.groupByRollupAggIndicator, i, rollupIndices.get(i));
							//add row before currRow & print with value
							CopyOfGeneralizedQueryBuilder.subTotalInJavaPrintRow(table, currRollupDimConfigIndex, prevRowIndex, addAfter, rollupGroupTot.get(i), qp.groupByRollupAggIndicator, dimConfigList, formatHelper, session, sdf, conn);
							addAfter++;
						}
					}
				}
			}//if doing rollupAtJava
    	}//if to pivot
    	//now put row number
    	if (processShowResult != null && processShowResult.hasColWithRowId) {
    		ArrayList<TR> tbody = table.getBody();
    		if (tbody != null && tbody.size() > 0) {
	    		TR firstHeaderRow = tbody.get(0);
	    		ArrayList<Integer> rowIndices = new ArrayList<Integer>();
	    		ArrayList<TD> rowData = firstHeaderRow.getRowData();
	    		for (int i=0, is=rowData.size(); i<is; i++) {
	    			TD td = rowData.get(i);
	    			if (td.getOptionalDimCofigIndex() >= 0) {
	    				DimConfigInfo dci = dimConfigList.get(td.getOptionalDimCofigIndex());
	    				if (dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_dimCalc.m_dimInfo.m_id == 20356) {
	    					rowIndices.add(i);
	    				}
	    			}
	    		}
	    		if (rowIndices.size() > 0) {
	    			for (int i=0,is=tbody.size(); i<is; i++) {	    				
	    				TR tr = tbody.get(i);
	    				ArrayList<TD> rd = tr.getRowData();
	    				for (int j=0,js=rowIndices.size(); j<js;j++) {
	    					int idx = rowIndices.get(j);
	    					TD td = rd.get(idx);
	    					if (i == is-1 && td.getDoGroup())
	    						continue;//no need to change text
	    					td.setOptionalAndRefDciIndexValue(new Value(i+1), td.getOptionalDimCofigIndex());
	    					td.setContent(Integer.toString(i+1));
	    				}
	    			}
	    		}
    		}
    	}
	}

    private static int subTotalInJavaGetDiff(ArrayList<Value> prevDataRow, ArrayList<Value> currDataRow, ArrayList<Integer> groupByRollupAggIndicator, int lookFrom) {
		 for (int i=0;i<=lookFrom;i++) {
			 if (groupByRollupAggIndicator.get(i) == 2) {
				 Value prev = prevDataRow.get(i);
				 Value curr = currDataRow.get(i);
				 boolean prevNull = prev == null || prev.isNull();
				 boolean currNull = curr == null || curr.isNull();
				 if (prevNull && currNull)
					 continue;
				 else if (((prevNull && !currNull) || (!prevNull && currNull) || !prev.equals(curr))) {
					 return i;
				 }
			 }
		 }
		 return groupByRollupAggIndicator.size();
	}
    private static void subTotalInJavaPrintRow(Table table, int totaledColIndex, int similarToRow, int addAfter, ArrayList<Value> valList, ArrayList<Integer> groupIndicator, ArrayList<DimConfigInfo> dimConfigList, ResultInfo.FormatHelper formatHelper, SessionManager session, SimpleDateFormat sdf, Connection conn) throws Exception {
    	TR newRow = new TR();
    	table.setBody(addAfter, newRow);
    	TR similarAs = table.getBody().get(similarToRow);
    	ArrayList<TD> rowData = similarAs.getRowData();
    	for (int i=0,is=rowData.size();i <is;i++) {
    		TD copyOf = rowData.get(i);
    		TD newTD = copyOf.copy();
    		newTD.setDoGroup(true);
    		int dciIndex = copyOf.getOptionalDimCofigIndex();
    		Value v = valList.get(i);
    		newTD.setOptionalAndRefDciIndexValue(v, dciIndex);
    		
    		//if (groupIndicator.get(i) == 0 || i == totaledColIndex) {
        		//DEBUG13 ... change the linkPart to ignore values of rollupindices on right 
    			String valStr = i == totaledColIndex ? "Total" : v == null || v.isNull() ? Misc.nbspString : v.toString(dimConfigList.get(dciIndex).m_dimCalc.m_dimInfo, formatHelper.multScaleFactors.get(dciIndex), formatHelper.formatters.get(dciIndex), session, session.getCache(), conn, sdf);
    			newTD.setContent(valStr);
    		//}
    		newRow.setRowData(newTD);
    	}
    }
		
    private int getRowIndexInExpandedVals(TR dataRow, ArrayList<ArrayList<Value>> valList, ArrayList<Integer> dimIndexPivot, int prevMatch) {
    	int retval = -1;
    	ArrayList<Value> pivotValsAsk = new ArrayList<Value>();
    	for (int i=0,is=dimIndexPivot.size(); i<is; i++) {
    		int idx = dimIndexPivot.get(i);
    		TD cell = dataRow.get(idx);
    		Value v = cell.getOptionalValue();
    		pivotValsAsk.add(v);
    	}
    	Value.ArrayOfValComparator comp = new Value.ArrayOfValComparator();
    	for (int i=prevMatch+1, is=valList.size(); i<is; i++) {
    		if (comp.compare(valList.get(i),pivotValsAsk) == 0)
    			return i;
    	}
    	return valList.size();
    }
    
    private ArrayList<ArrayList<Value>> getExpandedValsOfPivotCols(Table table, Connection conn, ArrayList<DimConfigInfo> dimConfigList
			, SessionManager session, SearchBoxHelper searchBoxHelper
			, ProcessShowResult processShowResult) throws Exception {
    	ArrayList<ArrayList<Value>> expandedVals = null;
    	ArrayList<TR> rows = table.getBody();
    	ArrayList<Integer> dimIndexPivot = processShowResult.dimIndexToPivot;
    	ArrayList<Integer> dimMeasurePivot = processShowResult.dimIndexOfPivotMeasure;
    	if (dimIndexPivot != null && dimIndexPivot.size() > 0 && dimMeasurePivot != null && dimMeasurePivot.size() > 0) {
    		boolean needToSeeRows = false;
    		boolean onlyFromRows = true;
    		boolean onlyPeriod = true;
    		boolean hasPeriod = false;
    		ArrayList<ArrayList<Value>> valLists = new ArrayList<ArrayList<Value>>();
    		for (int i=0,is=dimIndexPivot.size(); i<is; i++) {
    			int idx = dimIndexPivot.get(i);
    			DimConfigInfo dci = dimConfigList.get(idx);
    			int doPivot = dci.getDoPivot();
    			ArrayList<Value> vals = null;
    			
    			if (doPivot == 2) {
    				vals = getAllValsForDim(dci.m_dimCalc.m_dimInfo, conn, session, searchBoxHelper);
    			}
    			if (vals == null || vals.size() == 0) {
    				needToSeeRows = true;
    				vals = null;
    			}
    			else {
    				onlyFromRows = false;
    			}
    			valLists.add(vals);
    		}
    		expandedVals = new ArrayList<ArrayList<Value>>();
    		
    		//below if values are only valid combo from rows, then expandedVals will be populated else for those indexes whoze value is not obtained from dim
    		//will be populated by going thru row.
    		//For the 1st approach as well for each needed index we keep hashMap respective of ArrayList<Value> and Value seen. 
    		//After going thru all rows we get ArrayList and then sort
    		if (needToSeeRows) {
    			ArrayList<HashMap<Value, Value>> valsSeen = onlyFromRows ? null : new ArrayList<HashMap<Value, Value>>();//if only From Rows ... then valid combo so need to get indiv
    			if (!onlyFromRows) {
	    			for (int j=0,js=valLists.size(); j<js; j++) {
	    				if (valLists.get(j) == null)
	    					valsSeen.add(new HashMap<Value, Value>());
	    			}
    			}
    			HashMap<String, ArrayList<Value>> valsSeenFromRows = onlyFromRows ? new HashMap<String, ArrayList<Value>>() : null;//else no point getting valsSeenFromRow
    			for (int i=0,is = rows == null ? 0 : rows.size();i<is;i++) {
    				TR row = rows.get(i);
    				StringBuilder key = onlyFromRows ? new StringBuilder() : null;
    				ArrayList<Value> valsSeenInRow = onlyFromRows ? new ArrayList<Value>() : null;
    				boolean hasNonNull = false;
    				for (int j=0,js=valLists.size(); j<js; j++) {
    					int idx = dimIndexPivot.get(j);
    					Value data = row.get(idx).getOptionalValue();
    					if (data == null)
    						data = new Value();
    					if (onlyFromRows) {
    						if (key.length() != 0)
    							key.append("_");
    						key.append(data == null || data.isNull() ? "null" : data.getStringVal());
    						valsSeenInRow.add(data);
    						hasNonNull = hasNonNull || data.isNotNull();
    	    						
    					}
    					else {
    						ArrayList<Value> vals = valLists.get(j);
    						if (vals == null) {
    							valsSeen.get(j).put(data, data);
    							hasNonNull = hasNonNull || data.isNotNull();
    						}
    						else {
    							hasNonNull = true;
    						}
    					}//else onlyFromRows
    				}//for pivot dim
    				if (onlyFromRows) {
    					if (hasNonNull)
    						valsSeenFromRows.put(key.toString(), valsSeenInRow);
    				}
    			}//for each row
    			
    			if (onlyFromRows) { //populatedExpandedVals from Hashmap
    				Collection<ArrayList<Value>> vals = valsSeenFromRows.values();
    				for (ArrayList<Value> v: vals) {
    					expandedVals.add(v);
    				}
    				Collections.sort(expandedVals, new Value.ArrayOfValComparator());
    			}
    			else {
    				for (int j=0,js=valsSeen.size(); j<js; j++) {
    					HashMap<Value, Value> valueMap = valsSeen.get(j);
    					if (valueMap != null) {
    						Collection<Value> vals = valueMap.values();
    						valLists.add(new ArrayList<Value>());
    						for (Value v: vals) {
    							valLists.get(j).add(v);
    						}
    						Collections.sort(valLists.get(j));
    					}
    				}
    			}
    		}//if need to get from row
    		if (!onlyFromRows) {
    			//populated expanded vals ... TODO only 1 dimension implementd
    			ArrayList<Value> zeroValList = valLists.get(0);
    			for (int i=0,is=zeroValList == null ? 0 : zeroValList.size();i<is;i++) {
    				ArrayList<Value> val = new ArrayList<Value>();
    				val.add(zeroValList.get(i));
    				expandedVals.add(val);
    			}
    		}
    	}//if to pivot
    	return expandedVals;
    }
	
    public ArrayList<Value> getAllValsForDim(DimInfo dimInfo, Connection conn, SessionManager session, SearchBoxHelper searchBoxHelper) throws Exception {
    	ArrayList<Value> retval = new ArrayList<Value>();
    	if (dimInfo.m_type == Cache.LOV_TYPE) {
    		ArrayList<ValInfo> valList = dimInfo.getValList(conn, session);
    		for (int i=0,is=valList == null ? 0 : valList.size(); i<is; i++) {
    			retval.add(new Value(valList.get(i).m_id));
    		}
    		Collections.sort(retval);
    	}
    	else {
    		ColumnMappingHelper colMap = dimInfo.m_colMap;
    		String table = colMap.table;
    		if ("@period".equals(table) || "@indep_period".equals(table)) {
    			String granParamName = searchBoxHelper == null ? "pv20051": searchBoxHelper.m_topPageContext+"20051";
    			int granDesired = Misc.getParamAsInt(session.getParameter(granParamName));
    			if (granDesired == Misc.SCOPE_HOUR)
    				table = "hour_table";
    			else if (granDesired == Misc.SCOPE_DAY)
    				table = "day_table";
    			else if (granDesired == Misc.SCOPE_MONTH)
    				table = "month_table";
    			else if (granDesired == Misc.SCOPE_QTR)
    				table = "quarter_table";
    			else if (granDesired == Misc.SCOPE_ANNUAL)
    				table = "year_table";
    		}
    		if ("day_table".equals(table) || "hour_table".equals(table) || "month_table".equals(table) || "quarter_table".equals(table) || "year_table".equals(table)) {
    			PreparedStatement ps = conn.prepareStatement("select label from "+table+" tab where start_time >= ? and start_time < ? order by start_time ");
    			Date st = this.startDt;
    			Date en = this.endDt;
    			if (en == null)
    				en = st;
    			if (en == null)
    				en = new Date();
    			if (st == null) { 
    				st = new Date(en.getTime()-1000);
    			}
    			ps.setTimestamp(1, Misc.utilToSqlDate(st));
    			ps.setTimestamp(2, Misc.utilToSqlDate(en));
    			ResultSet rs = ps.executeQuery();
    			while (rs.next()) {
    				retval.add(new Value(rs.getString(1)));
    			}
    			rs = Misc.closeRS(rs);
    			ps = Misc.closePS(ps);
    		}
    	}
    	return retval;
    }
	public static ArrayList<MiscInner.Pair> helperAddColsForRowSpan(ArrayList<MiscInner.Pair> rowSpansSpec, int rowSpan, int colSpan) {
		if (rowSpan <= 1)
			return rowSpansSpec;
		if (colSpan < 1)
			colSpan = 1;
		if (rowSpansSpec == null)
			rowSpansSpec = new ArrayList<MiscInner.Pair>();
		rowSpansSpec.add(new MiscInner.Pair(rowSpan, colSpan));
		return rowSpansSpec;
	}
	public static void helperReduceColsForRowSpan(ArrayList<MiscInner.Pair> rowSpansSpec) {
		for (int i=0,is = rowSpansSpec == null ? 0 : rowSpansSpec.size(); i<is; i++) {
			MiscInner.Pair entry = rowSpansSpec.get(i);
			int v = entry.first;
			v--;
			if (v <= 1)
				rowSpansSpec.remove(i);
			else
				entry.first = v;
		}
	}
	public static class HelperCellPrint {
		public FrontPageInfo fpi;
		public ArrayList<DimConfigInfo> fpiList;
		public boolean checkAll;
		public Pair<String,Integer> rowLinkHelper;
		public SessionManager session;
		public SearchBoxHelper searchBoxHelper;
		public Table table;
		public Cache cache;
		public StringBuilder tempSB;
		public String selectCheckBoxVarName;
		public int privIdForOrg;
		public MiscInner.ContextInfo contextInfo;
		boolean doingMultiSelect;
		int objectIndex;
		//row specifc
		public int rowNumber;
		public boolean doGroup;
		int groupedItemIndex;
		int linkMenuIndex;
		public boolean doingInSelectionMode = false;
		public int addnlValueDim = Misc.getUndefInt();
		public int nameValueDim = Misc.getUndefInt();
		public int addnlValueVarIndex = -1;
		public int nameVarIndex = -1;
	}
	
	private TD printCell(HelperCellPrint ctl, ResultInfo resultInfo, int i/* the colIndex*/, boolean doRollupAtJava) throws Exception {
		StringBuilder tempSB = ctl.tempSB;
		
		TD td = new TD();

		DimConfigInfo dci = ctl.fpiList.get(i);
		boolean isPivotRelated = dci.isDoPivotMeasure() || dci.getDoPivot() > 0 || doRollupAtJava;
		if (isPivotRelated) {
			Value v = resultInfo.getVal(i);
			td.setOptionalAndRefDciIndexValue(v == null ? null : new Value(v), i);
		}
		if (dci.m_hidden){
			td.setHidden(true);
			/*if(dci.m_isMandatory)
				td.setHidden(true);
			else
				continue;*/
		}
		String disp = null;
		String displayLink = null;
		if (dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_dimCalc.m_dimInfo.m_id == 32161) {
			String width = dci.m_width > 0 ?" width=\""+dci.m_width+"px\" " : "";
			disp = "<img  src=\"getFile.do?play_back=1&trip_id="+resultInfo.getValStr(i)+"\""+width+"/></a>";
			td.setDisplay(disp);
		}
		
		else if(dci.m_use_image){
			disp = "<img src=\""+Misc.G_IMAGES_BASE+dci.m_image_file+"\">";
			td.setDisplay(disp);
		}
		else{
			disp = resultInfo.getValStr(i);
			td.setContent(disp);
		}
		
		if(dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null){
			int paramid = dci.m_dimCalc.m_dimInfo.m_id;
			td.setId(paramid);
			ctl.table.setIndexById(paramid,i);
		}
		//checkAll = true;
		if (dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_dimCalc.m_dimInfo != null
				&& dci.m_dimCalc.m_dimInfo.m_id == 20356){
			disp = Integer.toString(ctl.rowNumber);
			td.setContent(disp);
		}
		if (ctl.doGroup && i == ctl.groupedItemIndex){
			disp = "Total";
			td.setContent(disp);
		}
		boolean ignore = dci.m_isSelect || (dci.m_innerMenuList != null && dci.m_innerMenuList.size() != 0);
		td.setDoIgnore(ignore);
		boolean isNull = disp == null || disp.equals(Misc.emptyString) || disp.equals(Misc.nbspString);
		int type = dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null ? dci.m_dimCalc.m_dimInfo.m_type : Cache.INTEGER_TYPE;
		td.setContentType(type);
		boolean doNumber = type == Cache.INTEGER_TYPE || type == Cache.LOV_NO_VAL_TYPE || type == Cache.NUMBER_TYPE;
		if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() != 0){
			td.setAlignment(0);
			doNumber = true;
		}
		String objectIdParamLabel = ctl.fpi.m_objectIdParamLabel;
		String objectIdParamCol = ctl.fpi.m_objectIdColName;
		int objectIdParamColIndex = ctl.fpi.getColIndexByName(objectIdParamCol);
		
		//String link = !isNull ? null : generateLink(dci, fpList, resultInfo,searchBoxHelper == null ? "p" : searchBoxHelper.m_topPageContext,session, objectIdParamLabel, objectIdParamColIndex);
		String link = isNull ? null : generateLink(dci, ctl.fpiList, resultInfo, ctl.searchBoxHelper  == null ? "p" : ctl. searchBoxHelper.m_topPageContext,
				ctl.session, objectIdParamLabel, objectIdParamColIndex);
		if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() != 0){
			td.setAlignment(0);
		}
		String cellColor = null;
		int cellClass = doNumber ? 3 : 2;
		if(dci.m_valStyleClass != null && dci.m_valStyleClass.length() != 0)
		{
			cellClass=CssClassDefinition.getClassIdByClassName(dci.m_valStyleClass);
		}
		boolean colorForAll = ctl.checkAll;
		if(dci.m_color_code && disp != null && disp!=Misc.nbspString)
		{
			Value val = dci.m_color_code_by_index >= 0 ? resultInfo.getVal(dci.m_color_code_by_index) : resultInfo.getVal(i);
			double dispDouble = doNumber ? ( val.m_type == Cache.NUMBER_TYPE ? val.m_dVal : val.m_iVal) : (val.m_type == Cache.INTEGER_TYPE ? val.m_iVal : 100);//text is goig to be green

			if(!colorForAll)
			{
				colorForAll = dci.m_param1_color==1?true:colorForAll;
			}
			if(dci.m_param1_color==2)
			{
				colorForAll = colorForAll?false:true;
			}
			if(colorForAll) {
				boolean tc = true;

				if(dci.m_param2_color == 0) {
					cellClass = dispDouble <=dci.m_param1? 4 : dispDouble > dci.m_param1 && 
							dispDouble <= dci.m_param2 ? 5 : 6;
				}
				else {
					cellClass = dispDouble >= dci.m_param2 ? 4 : dispDouble >dci.m_param1 && 
							dispDouble <= dci.m_param2 ? 5 : 6;
				}
				//hack to align left if not number
				if (!doNumber) {
					td.setAlignment(-1); //note there is ' coming after cellClass
				}
			}
		}
		String hidden="";
		String optionalMenuParams = "";
		if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0) {
			optionalMenuParams = getLinkForMenu(dci, ctl.fpi, resultInfo);
			tempSB.setLength(0);
			FrontPageInfo.getAllListMenuDiv(ctl.session, dci, tempSB, true, null);
			if (tempSB.length() == 0) {
				//no priv
				td.setHidden(true);
			}
		}
		td.setDoGroup(ctl.doGroup);
		td.setClassId(cellClass);
		
		if(dci.m_show_modal_dialog){
			if (link != null) {	
				if (link.indexOf('?') >= 0)
					link += "&_do_popup=1";
				else
					link += "?_do_popup=1";
				if (ctl.rowLinkHelper != null)
					link = getRowLink(link, ctl.rowLinkHelper, resultInfo);
				displayLink = "<a href=\"#\" onClick=\"showModalDialog('"+link;
				displayLink	+= "', event.srcElement,'', '"+dci.m_dialog_width+"', '"+dci.m_dialog_height+"')\">";
				//displayLink += disp == null ? "" : disp;
				//displayLink += "</a>";
				td.setLinkAPart(displayLink);
			}
		}
		else if (dci.m_isSelect) {
			int valInt = ctl == null || ctl.objectIndex < 0 || resultInfo.getVal(ctl.objectIndex) == null ? Misc.getUndefInt() : resultInfo.getVal(ctl.objectIndex).m_iVal;
			displayLink = "<input class='tmNormalText' type='";
			displayLink	+= ctl.doingMultiSelect && !ctl.doingInSelectionMode? "checkbox" : "radio" ;
			displayLink	+= "' name='"+ctl.selectCheckBoxVarName+"' value='"+valInt+"' " ;
			displayLink += !ctl.doingMultiSelect || ctl.doingInSelectionMode ?" onclick='autoSelect(this)' ": "" ;
			displayLink += "/>";
			if (ctl.addnlValueVarIndex >= 0) {
				Value v1 = resultInfo.getVal(ctl.addnlValueVarIndex);
				if (v1 != null && v1.isNotNull()) {
					displayLink += "<input type='hidden' name='_v"+ctl.addnlValueDim+"' value='"+v1.toString()+"'/>";
				}
			}
			if (ctl.nameVarIndex >= 0) {
				Value v1 = resultInfo.getVal(ctl.nameValueDim);
				if (v1 != null && v1.isNotNull()) {
					displayLink += "<input type='hidden' name='_v"+ctl.nameValueDim+"' value='"+v1.toString()+"'/>";
				}
			}
			td.setDisplay(displayLink);
		}
		else if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() != 0) {
			int valInt = ctl == null || ctl.objectIndex < 0 || resultInfo.getVal(ctl.objectIndex) == null ? Misc.getUndefInt() : resultInfo.getVal(ctl.objectIndex).m_iVal;
			
			String link2 = generateLink(ctl.fpi.m_commonLinkHelper, ctl.fpiList, resultInfo,
					ctl.searchBoxHelper == null ? "p" : ctl.searchBoxHelper.m_topPageContext, true, ctl.session, objectIdParamLabel, objectIdParamColIndex, null);
			String img = dci.m_image_file == null || dci.m_image_file.length() == 0 ? "prj_action.gif" : dci.m_image_file;
			tempSB.setLength(0);
			String linkMenuName = "page_link_menu"+(ctl.linkMenuIndex==0?"":Integer.toString(ctl.linkMenuIndex));
			ctl.linkMenuIndex++;
			FrontPageInfo.getAllListMenuDiv(ctl.session, dci, tempSB, true, linkMenuName);
			if (tempSB.length() != 0) {
				displayLink = "<img src='"+Misc.G_IMAGES_BASE+img;
				displayLink += "'  onMouseOut=\"closeDropDown('page_link_menu')\"  onMouseOver=\"showPageLinkMenu(";
				// to be varified
				displayLink	+= valInt + (link2 == null ? ",null" : (",'"+link2+(optionalMenuParams != null && !optionalMenuParams.equalsIgnoreCase("") ? "&" : "")+optionalMenuParams+"'"))+",'"+ctl.selectCheckBoxVarName;
				displayLink	+= "','"+linkMenuName+"')\"";
				displayLink	+= " onclick=\"gotoOnClickEnh('"+tempSB+"',";
				displayLink += ((DimConfigInfo.InnerMenuInfo)dci.m_innerMenuList.get(0)).m_doPopup+",'"+link2;        
				displayLink += "','"+ctl.selectCheckBoxVarName+"',false,"+valInt+","+((DimConfigInfo.InnerMenuInfo)dci.m_innerMenuList.get(0)).m_suspendTimer+")\">";
				td.setDisplay(displayLink);	
			}
		}
		
		else if (dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && (dci.m_doEdit || !dci.m_readOnly)) {
			if(dci.m_dimCalc.m_dimInfo.m_type == ctl.cache.LOV_TYPE){
				int valInt = dci.m_initZero || resultInfo.getVal(i) == null  ? Misc.getUndefInt() : resultInfo.getVal(i).m_iVal;
				tempSB.setLength(0);
				ctl.cache.printDimVals(ctl.session, ctl.session.getConnection(), ctl.session.getUser(), dci.m_dimCalc.m_dimInfo, valInt, null, tempSB, dci.m_dimCalc.m_dimInfo.m_colMap.column, false,  null, 
					false, ctl.privIdForOrg, 1, 20, false, null, false, false, false, "",
					null, Misc.getUndefInt(), Misc.getUndefInt(), ctl.contextInfo);
				displayLink	= tempSB.toString();
				if (dci.m_readOnly) //not sure why below
				//displayLink += "<input type='hidden' class='"+dci.m_name+"' name='"+dci.m_dimCalc.m_dimInfo.m_colMap.table+"' id='"+dci.m_dimCalc.m_dimInfo.m_colMap.column+"' value='"+valInt+"'/>";
					displayLink += "<input type='hidden' columnName='"+dci.m_name+"' tabName='"+dci.m_dimCalc.m_dimInfo.m_colMap.table+"' internalName='"+dci.m_dimCalc.m_dimInfo.m_id+"' name='"+dci.m_dimCalc.m_dimInfo.m_colMap.column+"' id='"+dci.m_dimCalc.m_dimInfo.m_colMap.column+"' value='"+valInt+"'/>";
			}
			else{
				displayLink	= "<input  class='tn' type='text'  id='"+dci.m_dimCalc.m_dimInfo.m_colMap.column+"'";
				displayLink	+= "' value='"+(disp == null || dci.m_initZero ? "" : disp)+"' ";
				displayLink += " name='"+dci.m_dimCalc.m_dimInfo.m_colMap.column+"'";
				displayLink += "size='8' />";
				//displayLink += "<input type='hidden' class='"+dci.m_name+"' name='"+dci.m_dimCalc.m_dimInfo.m_colMap.table+"' id='"+dci.m_dimCalc.m_dimInfo.m_colMap.column+"' value='"+(disp == null ? "" : disp)+"'/>";
				if (dci.m_readOnly) //not sure why below
					displayLink += "<input type='hidden' columnName='"+dci.m_name+"' tabName='"+dci.m_dimCalc.m_dimInfo.m_colMap.table+"' internalName='"+dci.m_dimCalc.m_dimInfo.m_id+"' name='"+dci.m_dimCalc.m_dimInfo.m_colMap.column+"' id='"+dci.m_dimCalc.m_dimInfo.m_colMap.column+"' value='"+(disp == null ? "" : disp)+"'/>";
			}
			td.setDisplay(displayLink);
		}
		else if (dci.m_buttonAction != null && dci.m_buttonAction.length() != 0 ) {
			Value v = resultInfo.getVal(ctl.objectIndex);
			//v is null or v !=0 we print else not
			Value cellVal = resultInfo.getVal(i);
			if(cellVal != null && !cellVal.isNull() ){//&& (v == null || v.isNull() || !"0".equals(v.toString()))){
				displayLink = "<input type='button' class='Input_buttons' name='button1' value='"+dci.m_buttonAction+"' onclick=\""+link+"\"/>";
				td.setLinkAPart(displayLink);
				td.setContent("");
			}
			else {
				td.setContent("&nbsp;");
			}
		}
		else {
			if (link != null) {
				displayLink = "<a target='_newTab' href=\""+link+"\">";
				//displayLink +=disp == null ? "" : disp;
				//displayLink	+= "</a>";
				td.setLinkAPart(displayLink);	
			}
		}
		return td;
	}
	
	public static void printTableCongestion(Connection conn, ResultInfo.FormatHelper formatHelper, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, SessionManager session, Table table) throws Exception {
		MiningCurrentViewTemp miningView = new MiningCurrentViewTemp();
		miningView.loadOpInfo(conn, Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT));
		miningView.calcPlus5SimpleHack(5);
		ArrayList<MiningCurrentViewTemp.OpInfo> opInfoList = miningView.getOpInfos();
		TR tr = null;
		TD td = null;
		ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		int cols = 0;
		int type = 0;
		String disp = null;
		String col = null;
		String subType = null;
		if(fpList != null)
			cols = fpList.size();
		Cache cache = session.getCache();
		boolean doingMultiSelect = true;//fpi.m_multiSelect;
		ContextInfo contextInfo = Misc.getContextInfo(session, session.getConnection(), cache, session.getLogger(), session.getUser());
		StringBuilder tempBuf = new StringBuilder();
		int privIdForOrg = session.getUser().getPrivToCheckForOrg(session, null); 
		StringBuilder tempSB = new StringBuilder();
		int rowNumber = 0;
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		
		for (int oi=0,ois=opInfoList.size();oi<ois;oi++) {
			rowNumber++;
			MiningCurrentViewTemp.OpInfo opInfo = opInfoList.get(oi);
			tr = new TR();
			for(int i=0; i<cols; i++){
				td = new TD();
				DimConfigInfo dci = fpList.get(i);
				DimInfo dimInfo = dci == null || dci.m_dimCalc == null ? null : dci.m_dimCalc.m_dimInfo;
				Pair<Value, String> valCombo = dimInfo == null ? null : opInfo.getValue(conn, dimInfo.m_id);
				Value val = valCombo == null ? null : valCombo.first;
				disp = valCombo == null || valCombo.first == null ? null : valCombo.first.toString(dimInfo, formatHelper.multScaleFactors.get(i), formatHelper.formatters.get(i), session, session.getCache(), conn, sdf);
				String popLink = valCombo == null ? null : valCombo.second;
				if (disp != null && !disp.equals(Misc.emptyString) && popLink != null) {
					disp = "<span title='"+popLink+"'>"+disp+"</span>";
				}
				if (dci.m_hidden){
					td.setHidden(true);
				}
				td.setContent(disp);
				
				if(dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null){
					int paramid = dci.m_dimCalc.m_dimInfo.m_id;
					td.setId(paramid);
					table.setIndexById(paramid,i);
				}
				//checkAll = true;
				if (dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_dimCalc.m_dimInfo != null
						&& dci.m_dimCalc.m_dimInfo.m_id == 20356){
					disp = Integer.toString(rowNumber);
					td.setContent(disp);
				}
				boolean ignore = false;
				td.setDoIgnore(ignore);
				boolean isNull = disp == null || disp.equals(Misc.emptyString) || disp.equals(Misc.nbspString);
				type = dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null ? dci.m_dimCalc.m_dimInfo.m_type : Cache.INTEGER_TYPE;
				td.setContentType(type);
				boolean doNumber = type == Cache.INTEGER_TYPE || type == Cache.LOV_NO_VAL_TYPE || type == Cache.NUMBER_TYPE;
				if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() != 0){
					td.setAlignment(0);
					doNumber = true;
				}
				String cellColor = null;
				int cellClass = doNumber ? 3 : 2;
				if(dci.m_valStyleClass != null && dci.m_valStyleClass.length() != 0)
				{
					cellClass=CssClassDefinition.getClassIdByClassName(dci.m_valStyleClass);
				}
				boolean colorForAll = false;
				if(dci.m_color_code && disp != null && disp!=Misc.nbspString)
				{
					
					double dispDouble = doNumber ? ( val.m_type == Cache.NUMBER_TYPE ? val.m_dVal : val.m_iVal) : (val.m_type == Cache.INTEGER_TYPE ? val.m_iVal : 100);//text is goig to be green

					if(!colorForAll)
					{
						colorForAll = dci.m_param1_color==1?true:colorForAll;
					}
					if(dci.m_param1_color==2)
					{
						colorForAll = colorForAll?false:true;
					}
					if(colorForAll) {
						boolean tc = true;

						if(dci.m_param2_color == 0) {
							cellClass = dispDouble <=dci.m_param1? 4 : dispDouble > dci.m_param1 && 
									dispDouble <= dci.m_param2 ? 5 : 6;
						}
						else {
							cellClass = dispDouble >= dci.m_param2 ? 4 : dispDouble >dci.m_param1 && 
									dispDouble <= dci.m_param2 ? 5 : 6;
						}
						//hack to align left if not number
						if (!doNumber) {
							td.setAlignment(-1); //note there is ' coming after cellClass
						}
					}
				}
				
				String hidden="";
				String optionalMenuParams = "";
				td.setClassId(cellClass);
				td.setDoGroup(false);
				tr.setRowData(td);
			}//end of cols
			table.setBody(tr);
		}//end of row
	}

	public static void setColorCodeHackCongestion(ArrayList<DimConfigInfo> fpList) {
		for(int i=0, is=fpList.size(); i<is ; i++) {
			DimConfigInfo dci = fpList.get(i);
			DimInfo dimInfo = dci == null || dci.m_dimCalc == null ? null : dci.m_dimCalc.m_dimInfo;
			if (dimInfo == null)
				continue;
			int dimId = dimInfo.m_id;
			int param1 = Misc.getUndefInt();
			int param2 = Misc.getUndefInt();
			if (dimId == MiningCurrentViewTemp.OpInfo.g_latestProcTimeSec || dimId == MiningCurrentViewTemp.OpInfo.g_5minMaxWaitingTimeToClear) {
				param1 = 5;
				param2 = 8;
			}
			else if (dimId == MiningCurrentViewTemp.OpInfo.g_waitingVehCount || dimId == MiningCurrentViewTemp.OpInfo.g_5minVehWaitCount) {
				param1 = 2;
				param2 = 3;
			}
			if (!Misc.isUndef(param1)) {
				dci.m_color_code = true;
				dci.m_param1_color = 0;
				dci.m_param2_color = 0;
				dci.m_param1 = param1;
				dci.m_param2 = param2;
				//classid 4 = green, 5 = yellow, 6 = red
			}
		}
	}

	public static String getLinkForMenu(DimConfigInfo dimConfig, FrontPageInfo fpi, ResultInfo rs) {
		String retval = ""; 
		if (dimConfig == null || dimConfig.m_innerMenuList == null || dimConfig.m_innerMenuList.size() == 0)
             return retval;
		HashMap<String, String> paramMap = new HashMap<String, String>();
		 
		for (int i=0,is = dimConfig.m_innerMenuList.size();i<is;i++) {
             DimConfigInfo.InnerMenuInfo innerMenu = (DimConfigInfo.InnerMenuInfo) (dimConfig.m_innerMenuList.get(i));
             String page = innerMenu.m_page.toString();
             String fixedStr = "";
             String paramStr = "";
             String url = "";
             if (innerMenu.m_page == null || innerMenu.m_page.length() == 0)
            	 return retval;
             int qmarkPart = page.indexOf('?');
             if (qmarkPart > -1) {
            	 url = page.substring(0,qmarkPart);
            	 String paramPart = page.substring(qmarkPart+1);    		  
            	 StringTokenizer strtok = new StringTokenizer(paramPart,"&=",false);

            	 while(strtok.hasMoreTokens()) {
            		 try {
            			 boolean dynamicVal = false;
            			 String firstToken = strtok.nextToken();
            			 if (strtok.hasMoreTokens()) {
            				 String val = strtok.nextToken();
		                     if (val == null || val.length() == 0 || firstToken == null || firstToken.length() == 0) {
		                    	 continue;
		                     }
		                     else {
		                    	 String paramName = firstToken;
		                    	 boolean doReplaceWithTop = false;
		                    	 if (firstToken.startsWith("@")) {
		                    		 paramName = firstToken.substring(1);
		                    		 doReplaceWithTop = true;
		                    	 }
		                    	 int indexInFrontInfoList = -1;	   	                    	 
		                    	 String colName = val;
		                    	 if (val.startsWith("$") || val.startsWith("#")) {
		                    		 colName = val.substring(1, val.length()-1);
		                    		 Integer indexInteger = fpi.m_colIndexLookup.get(colName);
		                    		 if (indexInteger != null) {
		                    			 indexInFrontInfoList = indexInteger.intValue();
//		                    			 continue;
		                    		 }
		                    		 dynamicVal = true;
		                    	 }
		                    	 if (indexInFrontInfoList < 0 && !doReplaceWithTop ) { //goes to fixedPart
		                    		 if(dynamicVal) {
			                    		 continue;
		                    		 }
		                    		 if (fixedStr == null || fixedStr == "") {
		                    			 fixedStr = "";
		                    		 }
		                    		 else {
		                    			 fixedStr += "&";
		                    		 }
		                    		 fixedStr += paramName+"="+colName;
		                    	 }
		                    	 else {
		                    		 paramMap.put(paramName, (indexInFrontInfoList >= 0 ?rs.getVal(indexInFrontInfoList) : Misc.getUndefInt())+"");
		                    	 }
		                     }//if val proper
		                  }//val part of tokenizer
	                }
	                catch (Exception e) {
	             	  e.printStackTrace();
	                }
	            }//for each token
	 	   }//if qmark
        }//for each menu
		if(paramMap != null){
			 Iterator it = paramMap.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pairs = (Map.Entry)it.next();
           		     if (retval == null || retval == "") {
           		    	retval = "";
           		     }
           		     else {
        			 retval += "&";
           		     }
           		     retval += pairs.getKey() + "=" + pairs.getValue();
           		     it.remove();
			    }
		}
		return retval; 
	}
	
	int getPortNodeId(SessionManager _session, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper){
		int retval = Misc.getUndefInt();
		if (searchBoxHelper == null )
			return Misc.getUndefInt();
		boolean isFound = false;
		String topPageContext = searchBoxHelper.m_topPageContext;
		try{
			for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
				ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
				int colSize = rowInfo.size();
				for (int j=0;j<colSize;j++) {
					DimConfigInfo dimConfig = rowInfo.get(j);
					if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
						continue;
					boolean is123 = dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
					boolean isTime = "20506".equals(dimConfig.m_dimCalc.m_dimInfo.m_subtype);
					int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
					String tempVarName = is123 ? "pv123" : topPageContext+paramId;
					String tempVal = _session.getAttribute(tempVarName);
					if(is123 && tempVal != null){
						retval = Misc.getParamAsInt(tempVal);
						isFound = true;
						break;
					}
				}
				if(isFound)
					break;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	
	//NOT USED
	private void filterVehicleList(SessionManager _session, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper,ArrayList<Pair<Integer,Long>> vehicleList, HashMap<String, Value> loadFromDBVals){
		//NOT USED EFFECTIVELY
		boolean doVehicle = false;
		Connection conn = null;
		Value val = null;
		try{
			if (searchBoxHelper == null || vehicleList == null || vehicleList.size() == 0)
				return;
			if(_session != null)
				conn = _session.getConnection();
			ArrayList<Integer> vehicleIdListParamPassed = new ArrayList<Integer>();
			addObjectIdPassedInWhr(_session, WorkflowHelper.getTableInfo(G_OBJ_VEHICLES), null, vehicleIdListParamPassed, false);
			
			ArrayList<ArrayList<CachedWhereItem>> clauses = getWhrQueryCached(_session, searchBox, searchBoxHelper);
			
			for(int i=0,is = vehicleList.size(); i<is;i++) {//changed vindex/vindexs inside
				boolean matches = true;
				Pair<Integer,Long> vehicle  = vehicleList.get(i);
				int vId = vehicle.first;
				if (vehicleIdListParamPassed.size() > 0) {
					boolean found = false;
					for (int t1=0,t1s=vehicleIdListParamPassed.size(); t1<t1s;t1++) {
						if (vehicleIdListParamPassed.get(t1) == vId) {
							found = true;
							break;
						}
					}
					if (!found) {
						matches = false;
					}
				}
				matches = matches && evaluateCachedWhere(conn,_session, vId, clauses, loadFromDBVals);
				if (!matches) {
					vehicleList.remove(i);
					i--;
					is--;
				}
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static class CachedWhereItem {
		public int dimId;
		public int op;
		public ArrayList<Value> vals;
		boolean doLOVLike = false;
		boolean doNumeric = false;
		boolean doString = false;
		boolean doDate = false;
		public String toString() {
			return dimId+" op "+op + " ty:"+(doLOVLike ? " lov " : doNumeric ? " num " : doString ? " str " : " date ") + "["+vals ==  null ? "No Val" : vals.toString()+"]";
		}
		public static CachedWhereItem getString(int dimId, String vals[]) {
			if (vals == null || vals.length == 0)
				return null;
			
			CachedWhereItem retval = new CachedWhereItem();
			retval.doString = true;
			retval.dimId = dimId;
			retval.vals = new ArrayList<Value>();
			for (int i=0,is=vals.length;i<is;i++) {
				String v = vals[i];
				if (v == null)
					continue;
				v = v.trim();
				if (v.length() == 0)
					continue;
				retval.vals.add(new Value(v));
			}
			return retval;
		}
		public static CachedWhereItem getNumeric(int dimId, String opStr, String op1, String op2) {
			double v1 = Misc.getParamAsDouble(op1);
			double v2 = Misc.getParamAsDouble(op2);
			if (Misc.isUndef(v1) && Misc.isUndef(v2))
				return null;
			CachedWhereItem retval = new CachedWhereItem();
			retval.doNumeric = true;
			retval.dimId = dimId;
			retval.op = Misc.getParamAsInt(opStr);
			retval.vals = new ArrayList<Value>();
			retval.vals.add(new Value(v1));
			retval.vals.add(new Value(v2));
			return retval;
		}
		public static CachedWhereItem getLov(int dimId, String vals[]) {
			if (vals == null || vals.length == 0)
				return null;
			CachedWhereItem retval = new CachedWhereItem();
			retval.dimId = dimId;
			retval.doLOVLike = true;
			retval.vals = new ArrayList<Value>();
			for (int i=0,is=vals.length;i<is;i++) {
				String v = vals[i];
				if (v == null)
					continue;
				v = v.trim();
				if (v.length() == 0)
					continue;
				int vi = Misc.getParamAsInt(v);
				retval.vals.add(new Value(v));
			}
			return retval;
		}
		public static CachedWhereItem getDate(int dimId, boolean greater, Date dt) {
			if (dt == null)
				return null;
			CachedWhereItem retval = new CachedWhereItem();
			retval.doDate = true;
			retval.dimId = dimId;
			retval.op = greater ? 4 : 5;
			retval.vals = new ArrayList<Value>();
			retval.vals.add(new Value(dt));
			return retval;
		}
	}
	public static boolean evaluateCachedWhere(Connection conn,SessionManager session, int vehicleId, ArrayList<ArrayList<CachedWhereItem>> clauses, HashMap<String, Value> loadFromDBVals) throws Exception {
		boolean retval = true;
		if (clauses == null || clauses.size() == 0)
			return true;
		CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
		VehicleExtendedInfo vehicleExt = VehicleExtendedInfo.getVehicleExtended(conn, vehicleId);
		DriverExtendedInfo driverExt = null;//DEBUG13 maybe OOM on MPLDriverExtendedInfo.getDriverExtendedByVehicleId(conn, vehicleId);
		VehicleControlling vehicleControlling = NewProfileCache.getOrCreateControlling( vehicleId);
		StopDirControl stopDirControl = vehicleControlling.getStopDirControl(conn, vehSetup);
		
		VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
		synchronized (vdf) {
			for (int i=0,is=clauses == null ? 0 : clauses.size(); i<is;i++) {
				ArrayList<CachedWhereItem> orClauses = clauses.get(i);
				boolean foundTrue = true; // at the first valid it will be set to false .. thereafter first true will cause true and break;
				for (int j=0,js=orClauses == null ? 0 : orClauses.size(); j<js;j++) {
					CachedWhereItem item = orClauses.get(j);
					if (item.dimId < 0 || item.vals == null || item.vals.size() == 0)
						continue;
					if (item.dimId == 82545 || item.dimId == 82548)
						return true;
					foundTrue = false;
					Value val = ResultInfo.getCachedValueWithHack(conn,session, vehicleId, item.dimId, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					if (val == null)
						continue;
					if (item.doDate) {
						long lv = val == null ? 0 : val.getDateValLong();
						long rv = item.vals.get(0) == null ? 0 : item.vals.get(0).getDateValLong();
						if (val.getDateValLong() <= 0)
							continue;
						if (item.op == 4)
							foundTrue =  rv <= 0 || (lv >= rv && lv >= 0);
						else
							foundTrue = rv <= 0 || (lv <= rv && lv >= 0) ;
					}
					else if (item.doString) {
						String vs = val.getStringVal();
						for (int k=0,ks = item.vals.size(); k<ks;k++) {
							String rv = item.vals.get(k).getStringVal();
							if (rv == null || rv.length() == 0) {
								foundTrue = true;
								break;
							}
							if (rv.equals("_") && vs != null && vs.length() > 0) {
								foundTrue = true;
								break;
							}
							if (rv.equals("null") && (vs == null || vs.length() == 0)) {
								foundTrue = true;
								break;
							}
							else {
								foundTrue = vs != null && vs.indexOf(rv) >= 0 ;
								if (foundTrue)
									break;
							}
						}//end of each vals
					}//end of string
					else if (item.doLOVLike) {
						int vs = val.getIntVal();
						for (int k=0,ks = item.vals.size(); k<ks;k++) {
							int rv = item.vals.get(k).getIntVal();
							foundTrue = rv == Misc.G_HACKANYVAL || rv == vs;
							if (foundTrue)
								break;
						}//end of each vals
					}//end of lov
					else {
						double v1 = item.vals != null && item.vals.size() > 0 ? item.vals.get(0).getDoubleVal() : Misc.getUndefDouble();
						double v2 = item.vals != null && item.vals.size() > 1 ? item.vals.get(1).getDoubleVal() : Misc.getUndefDouble();
						double v = val.getDoubleVal();
						switch (item.op){
						case 1:
							foundTrue = Misc.isUndef(v1) || (v > v1 && !Misc.isEqual(v,v1));
							break;
						case 2:
							foundTrue = Misc.isUndef(v1) || (v < v1 && !Misc.isEqual(v,v1));
							break;
						case 3:
							foundTrue = Misc.isUndef(v1) || (Misc.isEqual(v,v1));
							break;
						case 4:
							foundTrue = Misc.isUndef(v1) || (v >= v1 || Misc.isEqual(v,v1));
							break;
						case 5:
							foundTrue = Misc.isUndef(v1) || (v <= v1 || Misc.isEqual(v,v1));
							break;
						case 6:
							foundTrue = Misc.isUndef(v1) || (!Misc.isEqual(v,v1));
							break;
						case 7:
							foundTrue = Misc.isUndef(v1) || Misc.isUndef(v2) || (v >= v1 && v <= v2) || Misc.isEqual(v, v1) || Misc.isEqual(v, v2);
							break;
						}
					}//end of numeric
					if (foundTrue)
						break; //orClause found to be true
				}//end of orClause
				if (!foundTrue)
					return false;
			}//all and clause
			return true;
		}//end of sync
	}
	public static ArrayList<ArrayList<CachedWhereItem>> getWhrQueryCached(SessionManager _session, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper) throws Exception {
		//return array of and clauses that can be ored together
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		SimpleDateFormat sdfTime = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		SimpleDateFormat indepDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		ArrayList<ArrayList<CachedWhereItem>> retval = null;
		
		try {
			if (searchBoxHelper == null)
				return retval;
			//            int maxCol = searchBoxHelper.m_maxCol;
			String topPageContext = searchBoxHelper.m_topPageContext;
			boolean seenVehicleStatus = false;
			boolean inOrBlock = false;
			ArrayList<CachedWhereItem> currentAndClause = null;
			boolean prevIsOrBlock = false;
			for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
				ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
				int colSize = rowInfo.size();
				for (int j=0;j<colSize;j++) {
					DimConfigInfo dimConfig = rowInfo.get(j);
					if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
						continue;
					boolean amOrBlock = dimConfig.m_orBlock;
					if (!amOrBlock || !prevIsOrBlock)
						currentAndClause = null;
					prevIsOrBlock = amOrBlock;
					boolean is123 = dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
					if (is123)
						continue; //already accounted
					boolean isTime = "20506".equals(dimConfig.m_dimCalc.m_dimInfo.m_subtype);
					//					boolean isDummy = false;
					int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
					String tempVarName = is123 ? "pv123" : topPageContext+paramId;
					String tempVal = _session.getAttribute(tempVarName);
					String altVarName = "pv"+paramId;
					if (tempVal == null) {
						tempVal = _session.getAttribute(altVarName);
					}
					if (paramId == 20661) {
						continue;
					}
					if(paramId == 20495){
						continue; //cant do shift
					}
					if (paramId == 20159)
						continue;
					if (paramId == 9000)
						continue;
					seenVehicleStatus = seenVehicleStatus || (dimConfig.m_dimCalc.m_dimInfo.m_id == 9008 && tempVal != null && !"-1000".equals(tempVal) 
							&& tempVal.length() > 0) || (dimConfig.m_dimCalc.m_dimInfo.m_subsetOf == 9000 && tempVal != null && !"-1000".equals(tempVal));
					String end = "";
					// to handle io logged data
					if(paramId == 20450) {
						continue;
					}
					if (tempVal == null || tempVal.equals("") || tempVal.equals("-1000"))
						continue;
			//rajeev20130403		if((!is123 && tempVal != null && !"".equals(tempVal) && !"-1000".equals(tempVal)) || dimConfig.m_numeric_filter){
					
					
					String encl ="";
					int type = dimConfig.m_dimCalc.m_dimInfo.m_type;
					boolean doingTripDate = false;
					boolean doingEventDate = false;
					String tName = dimConfig.m_dimCalc.m_dimInfo.m_colMap.table;
					String cName = dimConfig.m_dimCalc.m_dimInfo.m_colMap.column;
					if (tName.equals("Dummy") )
						continue;
					if ("trick".equals(tName) || "Singleton".equals(tName))
						continue;
					boolean isStartDateId = false;
					boolean isEndDateId = false;
					if (currentAndClause == null) {
						currentAndClause = new ArrayList<CachedWhereItem>();
						if (retval == null)
							retval = new ArrayList<ArrayList<CachedWhereItem>>();
						retval.add(currentAndClause);
					}
						
					if (dimConfig.m_numeric_filter) {
						String tempVarNameOperator = tempVarName + "_operator";
						String tempVarNameOperandFirst = tempVarName + "_operand_first";
						String tempVarNameOperandSecond = tempVarName + "_operand_second";

						String paramValOperator = _session.getAttribute(tempVarNameOperator);
						String paramValOperandFirst = _session.getAttribute(tempVarNameOperandFirst);
						String paramValOperandSecond = _session.getAttribute(tempVarNameOperandSecond);
						currentAndClause.add(CachedWhereItem.getNumeric(paramId, paramValOperator, paramValOperandFirst, paramValOperandSecond));
						continue;
					}
							// to handle comma separated multi text search
							//if (dimConfig.m_dimCalc.m_dimInfo.m_id == 9002)

					if (dimConfig.m_dimCalc.m_dimInfo.m_type == Cache.STRING_TYPE) {
						String [] tempValArray = tempVal.split(",");
						for (int l=0,ls = dimConfig.m_addnlDimInfoNew == null  ? 1 : dimConfig.m_addnlDimInfoNew.size()+1; l<ls; l++) {
							DimInfo useme = l == 0 ? dimConfig.m_dimCalc.m_dimInfo : ((DimCalc) dimConfig.m_addnlDimInfoNew.get(l-1)).m_dimInfo;
							currentAndClause.add(CachedWhereItem.getString(useme.m_id, tempValArray));
						}
						continue;
					}
					if (type != DATE_TYPE) {
						currentAndClause.add(CachedWhereItem.getLov(paramId, tempVal.split(",")));
						continue;
					}
					Date dt = null;
					dt = Misc.getParamAsDate(tempVal, null, sdf);
					if (dt == null)
						dt = Misc.getParamAsDate(tempVal, null, sdfTime);
					if (dt == null)
						dt = Misc.getParamAsDate(tempVal,  null, indepDateFormat);
					currentAndClause.add(CachedWhereItem.getDate(paramId,dimConfig.m_forDateApplyGreater,dt));
				}//end of col
			}//end of row
			//clean up for invalid stuff
			for (int i=0,is=retval == null ? 0 : retval.size(); i<is;i++) {
				ArrayList<CachedWhereItem> orClauses = retval.get(i);
				boolean foundTrue = true; 
				for (int j=0,js=orClauses == null ? 0 : orClauses.size(); j<js;j++) {
					CachedWhereItem item = orClauses.get(j);
					
					if (item.dimId >= 0 && item.vals != null && item.vals.size() > 0) {
						for (int k=0,ks = item.vals.size(); k < ks; k++) {
							if (item.vals.get(k) == null) {
								item.vals.remove(k);
								k--;
								ks--;
							}
							else if (item.doLOVLike && Misc.isUndef(item.vals.get(k).getIntVal())) {
								item.vals.remove(k);
								k--;
								ks--;
							}
							else if (item.doDate && item.vals.get(k).getDateVal() == null) {
								item.vals.remove(k);
								k--;
								ks--;
							}
							else if (item.doString && (item.vals.get(k).getStringVal() == null || "".equals(item.vals.get(k).getStringVal()))) {
								item.vals.remove(k);
								k--;
								ks--;
							}
							else if (item.doNumeric && Misc.isUndef(item.vals.get(k).getDoubleVal())) {
								item.vals.remove(k);
								k--;
								ks--;
							}	
						}//for each individual val
						if (item.vals.size() == 0) {
							orClauses.remove(j);
							j--;
							js--;
						}
					}//if possibly vals provided
					else {
						orClauses.remove(j);
						j--;
						js--;
					}
				}//for each item
				if (orClauses.size() == 0) {
					retval.remove(i);
					i--;
					is--;
				}
			}
			if (retval != null && retval.size() == 0)
				retval = null;
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
			
	}


private static String doDriverObjectJoin(int driverObject, HashMap<String, String> tList, boolean doOrgTimeBased) {//returns true if was handled here itself
	if (driverObject < 0)
		return null;
	if (driverObject == WorkflowHelper.G_BIN_PREQUERY) {
		return null;
	}
	WorkflowHelper.TableObjectInfo tableInfo = WorkflowHelper.getTableInfo(driverObject);
	if (tableInfo == null)
		tableInfo = WorkflowHelper.getTableInfo(G_OBJ_VEHICLES);
	StringBuilder sb = new StringBuilder();
	boolean doVehicleFirst = false;
//	if (driverObject == G_OBJ_VEHICLES || tableInfo.isHasVehicleIDFK()) {
	if (driverObject == G_OBJ_VEHICLES){
		doVehicleFirst = true;
		tList.put("vehicle", "vehicle");
	}
	
	if (doVehicleFirst) {
		if (!doOrgTimeBased) {
		sb.append("(select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (#) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ")
			.append(VEHICLE_FILTER)
			.append(") vi join vehicle on (vi.vehicle_id = vehicle.id) ");
		}
		else {
			sb.append("(select  vehicle_org_time_assignments.vehicle_id, vehicle_org_time_assignments.start_time, vehicle_org_time_assignments.end_time from vehicle join vehicle_org_time_assignments on (vehicle.id=vehicle_org_time_assignments.vehicle_id)  join port_nodes leaf on (leaf.id = vehicle_org_time_assignments.port_node_id)  join port_nodes anc  on (anc.id in (#) and (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)) ")
			.append(VEHICLE_FILTER)
			.append(" group by vehicle_org_time_assignments.vehicle_id, vehicle_org_time_assignments.start_time, vehicle_org_time_assignments.end_time ) vi join vehicle on (vi.vehicle_id = vehicle.id) ");
		}
		
	}
	
	if (driverObject != G_OBJ_VEHICLES) {
		
		if (doVehicleFirst) {
			sb.append(" join ").append(tableInfo.getName()).append(" on (").append(tableInfo.getName()).append(".vehicle_id = vehicle.id) ");
			
		}
		else if (driverObject == WorkflowHelper.G_OBJ_MATERIAL) {
			//TODO_NEW .. get list of materials join to (tp_record or challan or rfid_trip or trip_info, vehicles)
		}
		else if (driverObject == WorkflowHelper.G_OBJ_OP_STATION) {
			//TODO_NEW ... get list of op_station join to to (tp_record or challan or rfid_trip or trip_info, vehicles) ... need to figure out how to get load/unload
			sb.append(tableInfo.getName()).append(" join opstation_mapping _opm on (_opm.op_station_id=op_station.id and _opm.type in (@_opm_type)) ")
			.append(" join port_nodes leaf_driver on (leaf_driver.id = ").append(tableInfo.getName()).append(".").append(tableInfo.getPortNodeCol())
			.append(") join port_nodes anc_driver on (anc_driver.id in (@pv123) and anc_driver.lhs_number <= leaf_driver.lhs_number and anc_driver.rhs_number >= leaf_driver.rhs_number) "); 
		}
		else if (WorkflowHelper.G_WORKFLOWS == driverObject) {
			sb.append(tableInfo.getName());
		}
		else if (WorkflowHelper.G_USERS == driverObject) {
			sb.append(tableInfo.getName());
		}
		else if (WorkflowHelper.G_TPRINVOICE_HIST_ID == driverObject) {
			sb.append(tableInfo.getName());
		}
		else {
			sb.append(tableInfo.getName()).append(" join port_nodes leaf_driver on (leaf_driver.id = ").append(tableInfo.getName()).append(".").append(tableInfo.getPortNodeCol())
			.append(") join port_nodes anc_driver on (anc_driver.id in (@pv123) and anc_driver.lhs_number <= leaf_driver.lhs_number and anc_driver.rhs_number >= leaf_driver.rhs_number) ");
			
			if (tableInfo.isHasVehicleIDFK()) {
				tList.put("vehicle", "vehicle");
				sb.append(" left outer join vehicle on (vehicle.id = ").append(tableInfo.getName()).append(".vehicle_id)");
			}
			
		}
	}
	return sb.toString();
}

public static void prepForOtherIntermediates(HashMap<String, String> tList) {
	if ((tList.containsKey("do_rr_details") || tList.containsKey("mines_do_details")) && (tList.containsKey("vehicle") || tList.containsKey("driver_details"))) {
		tList.put("tp_record", "tp_record");
	}
}
private static int helperAddToScopeListDesc(ArrayList<Integer> currList, int toAdd, boolean justGetPos) {
	int addAt = 0;
	for (int i=0,is=currList.size(); i<is; i++) {
		int scp = currList.get(i);
		if (scp == toAdd || Misc.isLHSHigherScope(toAdd, scp)) {
			break;
		}
		addAt = i;
	}
	if (justGetPos)
		return addAt;
	else {
		
		if (addAt == currList.size())
			currList.add(toAdd);
		else if (currList.get(addAt) == toAdd) {
			
		}
		else {
			currList.add(addAt, toAdd);
		}
		return addAt;
	}
}

public static void doTimeBasedJoin(StringBuilder sb, int tripChallanDriver, int granDesired, int doIncludeLatestTrip, int driverObject, HashMap<String, String> tList, String firstTableUsedInTimeTableJoin, ArrayList<Integer> scopesForYTD, boolean doOrgTimeBased) {
	//tripChallanDriver => if 0 means trip_info join (challan or rfid_trip or tp_record) and vice versa
	//granDesired = granuliarty of time period - special processing for shift based

	//driverObject => main driver
	
	//SPECIAL processing FOR YTD - comments to be added
	//Assumes scopesForYTD is sorted in descending and if there is a granDesired and firstColAsked is @period then the scope is higher than granDesired
	boolean dorrDriver = driverObject == WorkflowHelper.G_OBJ_DORR || driverObject == WorkflowHelper.G_OBJ_MINES_DO_DETAIL;//make it parameter passed from top and have it parameterizable
	
	StringBuilder scpTable = new StringBuilder();
	String ytdFirstTable = null;
	for (int i=0,is=scopesForYTD == null ? 0 : scopesForYTD.size(); i<is; i++) {
		int scp = scopesForYTD.get(i);
		if (scp == Misc.SCOPE_TILL_DATE) {
			if (ytdFirstTable == null || scp == granDesired)
				ytdFirstTable = "singleton";
			continue;
		}
		scpTable.append(" cross join ");
		
		if (scp == Misc.SCOPE_TILL_DATE) {
			scpTable.append("(select '1990-01-01' start_time, '@user_end' end_time ");
		}
		else if (scp == Misc.SCOPE_USER_PERIOD) {
			scpTable.append("(select '@user_start' start_time, '@user_end' end_time ");
		}
		else {
			scpTable.append("(select start_time, end_time from ");
			scpTable.append(scp == Misc.SCOPE_WEEK ? "week_table" 
					: scp == Misc.SCOPE_MONTH ? "month_table" 
					: scp == Misc.SCOPE_ANNUAL ? "year_table"
					: scp == Misc.SCOPE_QTR ? "quarter_table"
					: scp == Misc.SCOPE_HOUR ? "hour_table"
					: scp == Misc.SCOPE_SHIFT ? "shift_table"
							: "day_table"
								);
		}
		scpTable.append(" where '@end_period' between start_time and end_time ");
		if (scp == Misc.SCOPE_SHIFT)
			scpTable.append(" and shift_table.port_node_id = @pv123 ");
		String tabName = "_ytdlookup_"+scp;
		scpTable.append(") as ").append(tabName).append(" ");
		if (ytdFirstTable == null || scp == granDesired)
			ytdFirstTable = tabName;
	}
	sb.append(scpTable);
	
	OrderedTimeTableInfo prevTimeInfo = null;
	String prevExistTab = null;
	String prevExistCol = null;
	WorkflowHelper.TableObjectInfo driverObjectTableInfo = WorkflowHelper.getTableInfo(driverObject);
	String driverObjectTable = driverObjectTableInfo == null ? null : driverObjectTableInfo.getName();
	
	if (driverObjectTable != null && gTableList.containsKey(driverObjectTable)) {
		//will be first thing in from clause and handled elsewhere ... but prevExistTab need to be set up so that others can join
		prevTimeInfo = gTableList.get(driverObjectTable);
		prevExistTab = driverObjectTable;
		prevExistCol = prevTimeInfo.colName;
	}
	boolean tripInfoIsSecondaryToTPR = false;//if tpr/dorr driver then true (ie tp_record left outer join trip_info) else trip_info left outer join tp_record
	
	if (dorrDriver || driverObject == WorkflowHelper.G_OBJ_TPRECORD || "tp_record".equals(firstTableUsedInTimeTableJoin)) {
		tripInfoIsSecondaryToTPR = true;
	}
	boolean toSkipTPRBecauseConsideredWIthTripInfo = false;
	boolean toSkillTripInfoBecauseConsideredWithTPR = false;
	//for challan_details, rf_trip, tp_record, trip_info_otherLU ... joining with trip_info is not based on time
	//So if tripChallanDriver != 0, we will join trip_info (if needed) as soon as we see one of the above tables and ignore
	// ... trip_info while going thru ordered list
	//And if (tripChallanDriver == 0) we will ignore these tables if trip_info is asked for and join when trip_info is seen
	
	for (OrderedTimeTableInfo tableWithDateCol : gTableWithDateOrdered) {
		String tab = tableWithDateCol.tabName;
		String col = tableWithDateCol.colName;
		if (!tList.containsKey(tab))
			continue;
		boolean isChallanLike = "challan_details".equals(tab) || "tp_record".equals(tab) || "trip_info_otherLU".equals(tab) || "rfid_trip".equals(tab);
		boolean isTripInfoLike = "trip_info".equals(tab);
		boolean isTPR = "tp_record".equals(tab);
		boolean isDO = "do_rr_details".equals(tab) || "mines_do_details".equals(tab);
		int objectIdType = tableWithDateCol.indexInObjectType;
		if (tab.equals("user_login_track") || tab.equals("user_mgmt_track")) {
			sb.append(" left outer join ").append(tab).append(" on (").append(tab).append(".user_id=users.id)");
			continue;
		}
		if (objectIdType == driverObject) {//joined in doDriverObjectJoin
			
			if (isTPR && tList.containsKey("do_rr_details")) {
				sb.append(" left outer join do_rr_details on (tp_record.do_id = do_rr_details.id) ");
			}
			if (doJoinAreaCode && isTPR && tList.containsKey("mines_do_details")) {
				sb.append(" left outer join mines_do_details on (tp_record.do_number = mines_do_details.do_number) ");
			} else if (isTPR && tList.containsKey("mines_do_details")) {
				sb.append(" left outer join mines_do_details on (tp_record.do_id = mines_do_details.id) ");
			}
			continue;
		}
		if (tripChallanDriver == 0 && isChallanLike && tList.containsKey("trip_info")) {
			continue; //we will join challanLike table when we see trip_info
		}
		if (tripChallanDriver != 0 && isTripInfoLike) {//tripChallanDriver != 0 only if there were challan_details etc so no need to check tList.containsKey
			continue;
		}
		if (dorrDriver && isTPR) {
			
			sb.append(" left outer join tp_record on (tp_record.do_id = ").append(driverObjectTable).append(".id) ");
			if (tripInfoIsSecondaryToTPR && tList.containsKey("trip_info")) {
				sb.append(" left outer join trip_info on (tp_record.vehicle_id = trip_info.vehicle_id and (tp_record.material_cat=))");
			}
			continue;
		}
		else if (!dorrDriver && isDO && tList.containsKey("tp_record")) //handled is isTPR
			continue;
		String timeCol = tableWithDateCol.getFlexEventDateCol();
		StringBuilder orgTimeBasedClause =  null;
		if (doOrgTimeBased && driverObject == 0) {
			String sttime = tableWithDateCol.startColName;
			String entime = tableWithDateCol.orgBasedTimeMatchEnd;
			if (entime == null)
				entime = tableWithDateCol.endColName;
			if (sttime.equals(entime))
				entime = null;
			sttime = tab+"."+sttime;
			if (entime != null)
				entime = tab+"."+entime;
			orgTimeBasedClause = new StringBuilder();
			
			orgTimeBasedClause.append("and (") 
					.append("(")
					.append(" (").append(sttime).append(" >= vi.start_time) and (").append(sttime).append("  < vi.end_time or vi.end_time is null) ")
					.append(")")
					.append(" or ")
					.append(" ( ")
					.append(" (vi.start_time >= ").append(sttime).append(" ) and (vi.start_time < ").append(entime).append("  or ").append(sttime).append("  is null) ")
					.append(" ) ")
					.append(" ) ")
					;
		}
			
		
		if (driverObject == WorkflowHelper.G_GRNS && isTPR) {
			sb.append(" left outer join tp_record on (tp_record.grn_id = grns.id) ");
			continue;
		}
		
		if (tab.equals("grns")) {
			if (objectIdType != driverObject) {
				if (tList.containsKey("tp_record")) 
					sb.append(" left outer join grns on (grns.id = tp_record.grn_id) ");
				else if (tList.containsKey("do_rr_details")) {
					sb.append(" left outer join tp_record on (tp_record.do_id = do_rr_details.id) left outer join grns on (grns.id = tp_record.grn_id) ");
				}else if (doJoinAreaCode && tList.containsKey("mines_do_details")) {
					sb.append(" left outer join tp_record on (tp_record.do_number = mines_do_details.do_number) left outer join grns on (grns.id = tp_record.grn_id) ");
				}
				else if (tList.containsKey("mines_do_details")) {
					sb.append(" left outer join tp_record on (tp_record.do_id = mines_do_details.id) left outer join grns on (grns.id = tp_record.grn_id) ");
				}
			}
			else {
				//handled elsewhere in doDriverJoin
			}
			
			continue;
		}
		if ("workflows".equals(tab)) {
			sb.append(" left outer join workflows on (workflows.object_id=").append(driverObjectTableInfo).append(".").append(driverObjectTableInfo.getPrimaryIdCol()).append(" and workflows.workflow_type_id=@workflow_type) ");
			continue;
		}
		if ("@indep_period".equals(tab)) {
			//sb.append(" left outer join @period on (@period.start_time between '@user_start' and '@user_end') ");
			if (orgTimeBasedClause == null) {
				sb.append(" cross join @period  ");
			}
			else {
				sb.append(" left outer join @period on (@period.start_time between '@user_start' and '@user_end' ").append(orgTimeBasedClause).append(") ");
			}
			continue;
		}
		if ("driver_blocking_hist".equals(tab)) {
			sb.append(" join driver_blocking_hist on (driver_blocking_hist.driver_id=driver_details.id) ");
			continue;
		}
		if (doIncludeLatestTrip > 0 && tab.equals(firstTableUsedInTimeTableJoin)) {
			if ("tp_record".equals(firstTableUsedInTimeTableJoin)) {
				sb.append(" left outer join ").append(g_summary_latest_tp_record_for_join).append(" on (vehicle.id = tp_record_ltp.vehicle_id) ");
			}
			if ("challan_details".equals(firstTableUsedInTimeTableJoin)) {
				sb.append(" left outer join ").append(g_summary_latest_challan_details_for_join).append(" on (vehicle.id = challan_details_ltp.vehicle_id) ");
			}
			else if ("trip_info".equals(firstTableUsedInTimeTableJoin))
				sb.append(" left outer join ").append(g_summary_latest_trip_info_for_join).append(" on (vehicle.id = trip_info_ltp.vehicle_id) ");
			else if ("engine_events".equals(firstTableUsedInTimeTableJoin))
				sb.append(" left outer join ").append(g_summary_latest_engine_event_for_join).append(" on (vehicle.id = engine_events_ltp.vehicle_id) ");
			else if ("ais_logged_data".equals(firstTableUsedInTimeTableJoin))
				sb.append(" left outer join ").append("(select vehicle_id, max(gps_record_time) grt from ais_logged_data group by vehicle_id) aislgdltp").append(" on (vehicle.id = aislgdltp.vehicle_id) ");
			else if ("ais_login_data".equals(firstTableUsedInTimeTableJoin))
				sb.append(" left outer join ").append("(select vehicle_id, max(gps_record_time) grt from ais_login_data group by vehicle_id) aisloginltp").append(" on (vehicle.id = aisloginltp.vehicle_id) ");
			else if ("ais_health_data".equals(firstTableUsedInTimeTableJoin))
				sb.append(" left outer join ").append("(select vehicle_id, max(gps_record_time) grt from ais_health_data group by vehicle_id) aishltltp").append(" on (vehicle.id = aishltltp.vehicle_id) ");
			else if ("ais_emergency_data".equals(firstTableUsedInTimeTableJoin))
				sb.append(" left outer join ").append("(select vehicle_id, max(gps_record_time) grt from ais_emergency_data group by vehicle_id) aisemeltp").append(" on (vehicle.id = aisemeltp.vehicle_id) ");
			
		}
		boolean needsAnd = false;
		boolean hasJoinEnd = true;
		if (tableWithDateCol.hasVehicleIdFK || prevExistTab != null || (tab.equals("@period") && granDesired == 6)) {
			sb.append(" left outer join  ");
		}
		else {
			sb.append(" cross join ");
			hasJoinEnd = false;
		}
		if ("swm_bin_status_info".equals(tab)) {
			sb.append(" ").append(swm_bin_status_info_internal).append(" ");
		}
		else {
		sb.append(tab);
		}
		if (hasJoinEnd)
			sb.append(" on (");
		if (granDesired == 6) {
			sb.append("@period.port_node_id=@pv123 ");
			needsAnd = true;
		}
		
		
		if (tableWithDateCol.hasVehicleIdFK) {
			if (needsAnd)
				sb.append(" and ");
			if(tripChallanDriver == 2){
				sb.append(tab).append(".").append("vehicle_name").append("= vehicle.std_name ");
			}else{
				sb.append(tab).append(".").append("vehicle_id").append("= vehicle.id ");
				if (orgTimeBasedClause != null)
					sb.append(orgTimeBasedClause);
			}
			
			needsAnd = true;
		}
		if ("swm_bin_status_info".equals(tab)) {
			if (needsAnd)
				sb.append(" and ");
			sb.append(tab).append(".").append("swm_bin_id").append("= swm_bins.id ");
			needsAnd = true;
		}
		boolean doNormalPrevExistTab = true;
		
		
		String prevExistTabToUseInJoin = ytdFirstTable != null && "@period".equals(prevExistTab) ? ytdFirstTable : prevExistTab;
		if (prevExistTab != null && !prevExistTabToUseInJoin.equals("singleton")) {
			if (needsAnd)
				sb.append(" and ");
			sb.append("( ").append(timeCol).append(" between ").append(prevExistTabToUseInJoin).append(".").append(prevTimeInfo.startColName).append(" and ").append(prevExistTabToUseInJoin).append(".").append(prevTimeInfo.endColName).append(")");
			needsAnd = true;
		}

		//do special stuff
		if (doIncludeLatestTrip > 0 && "engine_events".equals(firstTableUsedInTimeTableJoin)) {
			if (needsAnd)
				sb.append(" and ");
			sb.append(" engine_events_ltp.rule_id = engine_events.rule_id ");
		}

		if (hasJoinEnd)
			sb.append(") ");
		if (isTPR) {
			if (tList.containsKey("trip_info")) {
                sb.append(" left outer join trip_tpr_mapping ttm on (tp_record.tpr_id = ttm.tpr_id) left outer join trip_info on (trip_info.id = ttm.trip_info_id) ");
            }
			if (tList.containsKey("do_rr_details")) {
				sb.append(" left outer join do_rr_details on (tp_record.do_id = do_rr_details.id) ");
			}
			if(doJoinAreaCode && tList.containsKey("mines_do_details")) {
				sb.append(" left outer join mines_do_details on (tp_record.do_number = mines_do_details.do_number) ");
			}else if (tList.containsKey("mines_do_details")) {
				sb.append(" left outer join mines_do_details on (tp_record.do_id = mines_do_details.id) ");
			}
		}
		if (isDO) {
			
			if (tList.containsKey("tp_record")) {
				if ("do_rr_details".equals(tab))
					sb.append(" left outer join tp_record on (tp_record.do_id = do_rr_details.id) ");
				else if(doJoinAreaCode)
					sb.append(" left outer join tp_record on (tp_record.do_number =mines_do_details.do_number) ");
				else
					sb.append(" left outer join tp_record on (tp_record.do_id =mines_do_details.id) ");
			}
		}
		if (isChallanLike) {
			//if we get to here ... then we need to join trip here itself if necessary
			if (tList.containsKey("trip_info")) {
				if(tripChallanDriver == 2){
					sb.append(" left outer join trip_info on (trip_info.vehicle_id = vehicle.id and tp_record.latest_load_combo_out between trip_info.combo_start and trip_info.combo_end) ");
				}else{
					sb.append(" left outer join trip_info on (trip_info.id = ").append(tab).append(".");
					if ("trip_info_otherLU".equals(tab))
						sb.append("trip_id");
					else 
						sb.append("trip_info_id");
					sb.append(") ");
				}
			}
			if (tList.containsKey("challan_dispatch_item")) {
				sb.append(" left outer join challan_dispatch_item on (challan_dispatch_item.challan_id = challan_details.challan_id) ");
			}
		}
		if (isTripInfoLike) {//if we get to here ... then we need to join challan here
			if (tList.containsKey("challan_details")) {
				sb.append(" left outer join challan_details on (challan_details.trip_info_id = trip_info.id) ");
			}
			
			if (tList.containsKey("tp_record")) {
                sb.append(" left outer join trip_tpr_mapping ttm on (trip_info.id = ttm.trip_info_id) left outer join tp_record on (tp_record.tpr_id = ttm.tpr_id) ");
            }

//			else if (tList.containsKey("tp_record")) {
//				sb.append(" left outer join tp_record on (tp_record.vehicle_name = vehicle.std_name and tp_record.latest_load_combo_out between trip_info.combo_start and trip_info.combo_end) ");
//			}
			if (tList.containsKey("rfid_trip")) {
				sb.append(" left outer join rfid_trip on (rfid_trip.trip_info_id = trip_info.id) ");
			}
			if (tList.containsKey("trip_info_otherLU")) {
				sb.append(" left outer join trip_info_otherLU on (trip_info_otherLU.trip_id = trip_info.id) ");
			}
		}
		prevTimeInfo = tableWithDateCol;
		prevExistTab = tab;
		prevExistCol = col;
	}//for each timeTable
}
public static void putPassedIdsInForm(SessionManager _session, JspWriter out) throws IOException {
	String objIdNamePassed = _session.getParameter("_passed_obj_name");
	String objIdPassed = _session.getParameter("_passed_obj_val");
	int objectType = Misc.getParamAsInt(_session.getParameter("object_type"));
	    
    if (!Misc.isUndef(objectType)) {
    	out.println("<input type='hidden' name='object_type' value='"+Integer.toString(objectType)+"'/>");
    }
    if (objIdNamePassed != null && objIdPassed != null) {
    	out.println("<input type='hidden' name='"+objIdNamePassed+"' value='"+objIdPassed+"'/>");
    }
}

public static void setWorkflowTypeInSession(SessionManager _session, SearchBoxHelper searchBoxHelper) {
	if ((_session.getParameter("pv402") == null && _session.getParameter(searchBoxHelper.m_topPageContext+402) == null) || !"Go".equals(_session.getParameter("SearchButton"))) {
 		ArrayList<Integer> allWorkflowTypes = com.ipssi.workflow.WorkflowDef.getWorkflowIdsForObjectType(Misc.getParamAsInt(_session.getParameter("object_type")),0);
 		StringBuilder sb1 = new StringBuilder();
 		if (allWorkflowTypes != null && allWorkflowTypes.size() > 0) {
 			Misc.convertInListToStr(allWorkflowTypes, sb1);
 			String s1 = sb1.toString();
 			_session.setAttribute("pv402", s1,false);
 			_session.setAttribute(searchBoxHelper.m_topPageContext+402, s1,false);
 		}
 	 }	
}

public static void setPassedObjectInSession(SessionManager _session) {
	boolean retval = false;
	if ("1".equals(_session.getParameter("_computed_passed")))
		return;
	_session.setAttribute("_computed_passed", "1", false);
	if (!"1".equals(_session.getParameter("ign_vehicle_id"))) {//hack to get around showing details for vehicle_id
		int objectType = Misc.getParamAsInt(_session.getParameter("object_type"));
		WorkflowHelper.TableObjectInfo driverTableObjectInfo = WorkflowHelper.getTableInfo(objectType);
		if (driverTableObjectInfo != null) {
			String objectName = driverTableObjectInfo.getParamName();
			String vehicleIds[] = _session.request.getParameterValues(objectName);
			StringBuilder csv = new StringBuilder();
			if (vehicleIds != null && vehicleIds.length > 0) {
				Misc.convertInListToStr(vehicleIds, csv);
			}
			if (csv.length() > 0) {
				_session.setAttribute("object_type", Integer.toString(objectType), false);
				_session.setAttribute("_passed_obj_name", objectName, false);
				_session.setAttribute("_passed_obj_val", csv.toString(), false);
			}//if csv
		}//if driverFound
	}//if ign_vehicle_id
}


public static boolean addObjectIdPassedInWhr(SessionManager _session, WorkflowHelper.TableObjectInfo driverTableObjectInfo, StringBuilder sb, ArrayList<Integer> idList, boolean addAfterAnd) {
	boolean retval = false;
	setPassedObjectInSession(_session);
	String objVal = _session.getParameter("_passed_obj_val");
	if (objVal != null && objVal.length() > 0) {
		String idCol = "vehicle.id";
		if (driverTableObjectInfo != null) {
			if (driverTableObjectInfo.getId() == WorkflowHelper.G_WORKFLOWS) { //changed within
				idCol = "workflows.object_id";
			}
			else {
				idCol = driverTableObjectInfo.getName()+"."+driverTableObjectInfo.getPrimaryIdCol();
			}
		}
		retval = true;
		if (sb != null) {
			if (addAfterAnd)
				sb.append(" and ");
			sb.append(" ").append(idCol).append(" in (").append(objVal).append(")");
		}
		 if (idList != null) {
			 Misc.convertValToVector(objVal, idList);
		}
	}
	return retval;
}

private String getWhrQuery(QueryParts qp, SessionManager _session, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper,
			HashMap<String, String> tableSeen, String firstTableUsedInTimeTableJoin, String colForFirstTableUsedInTimeTableJoin,StringBuilder vehicleFilter, int driverObject, int doIncludeLatestTrip, int externalAliasedTableIndex, boolean doDimsLoadFromDBOnly){
		boolean fromLink = "1".equals(_session.getParameter("_from_link"));
		String whrCl = null;
		boolean doVehicle = false;
		StringBuilder whrStr = new StringBuilder(whr);
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		SimpleDateFormat sdfTime = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		SimpleDateFormat indepDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		WorkflowHelper.TableObjectInfo driverTableObjectInfo = WorkflowHelper.getTableInfo(driverObject);
		//if (driverTableObjectInfo == null)
		//	driverTableObjectInfo = WorkflowHelper.getTableInfo(G_OBJ_VEHICLES);
		
//TODO
		//MOVED_TO_BUILD_Q String orgStr = GeneralizedQueryBuilder.doDriverObjectJoin(driverObject, tableSeen);
		//REPLACE_NEW String orgStr = "(select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (#) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "
		//REPLACE_NEW		+ VEHICLE_FILTER +") vi on vi.vehicle_id = vehicle.id ";
		//REPLACE_NEW if (driverObject != G_OBJ_VEHICLES) {
		//REPLACE_NEW	orgStr = "(select distinct(driver_details.id) driver_id from driver_details join port_nodes leaf on (leaf.id = driver_details.org_id) join port_nodes anc  on (anc.id in (#) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number))) "
		//REPLACE_NEW		+ VEHICLE_FILTER +") vi on vi.driver_id = driver_details.id ";
		//REPLACE_NEW }
		boolean addWhr = false;
		try {
			/*int projectId = (int) _session.getProjectId();
            Cache _cache = _session.getCache();*/
			
			if (searchBoxHelper == null) {
				StringBuilder sb = new StringBuilder();
				addWhr = addObjectIdPassedInWhr(_session, driverTableObjectInfo, whrStr, null, addWhr);
				return addWhr ? null : whrStr.toString();
			}
			//            int maxCol = searchBoxHelper.m_maxCol;
			String topPageContext = searchBoxHelper.m_topPageContext;
			
			boolean seenVehicleStatus = false;
			boolean inOrBlock = false;
			
			OrderedTimeTableInfo inforOfFirst = firstTableUsedInTimeTableJoin == null ? null : gTableList.get(firstTableUsedInTimeTableJoin);
			String timeCol = inforOfFirst == null ? firstTableUsedInTimeTableJoin+"."+colForFirstTableUsedInTimeTableJoin :  inforOfFirst.getFlexEventDateCol();
			for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
				ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
				int colSize = rowInfo.size();
				for (int j=0;j<colSize;j++) {
					DimConfigInfo dimConfig = rowInfo.get(j);
					if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
						continue;
					int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
					if(paramId==60027){//Hack for getting SECL Previous balance 
						int indx=whrStr.indexOf("@user_start");
						if (indx<0)
							continue;
						indx--;
						whrStr.replace(indx, indx+13, "date_add('@user_start',INTERVAL "+dimConfig.m_param1+" DAY)");
						if(dimConfig.m_param2==1){
							whrStr.replace(whrStr.indexOf("@"), whrStr.indexOf("@")+1, "(( @");
							String col_str=" ) or (coalesce(tp_record.latest_load_gate_out_out,tp_record.latest_load_wb_out_out,tp_record.latest_load_gate_in_out) >= '@user_start'"+
							" and coalesce(tp_record.latest_load_gate_in_out,tp_record.latest_load_wb_out_out,tp_record.latest_load_gate_out_out) <= '@user_end')) ";
							whrStr.append(col_str);
						}
						continue;
					}
						
					boolean exactSearch = dimConfig.m_searchExact;
					int attribType = dimConfig.m_dimCalc.m_dimInfo.m_type;
					boolean isIntLike = attribType != Cache.STRING_TYPE && attribType != Cache.NUMBER_TYPE && attribType != Cache.DATE_TYPE;
					DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
					boolean is123 = dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
					boolean isTime = "20506".equals(dimConfig.m_dimCalc.m_dimInfo.m_subtype);
					
					String tempVarName = is123 ? "pv123" : topPageContext+paramId;
					String tempVal = _session.getAttribute(tempVarName);
					boolean isStartDateId = PageHeader.isStartDateId(dimConfig.m_dimCalc.m_dimInfo.m_id);
					boolean isEndDateId = PageHeader.isEndDateId(dimConfig.m_dimCalc.m_dimInfo.m_id);
					//if dates in filter but nothing to filter for .. but still remember ..//TODO rewrite dupli of code
					if (isStartDateId || isEndDateId) {
						Date dt = null;
						if (isTime) {
							try {
								dt = sdfTime.parse(tempVal);
							}
							catch (ParseException e2) {
								dt = sdf.parse(tempVal);
							}
						}
						else {
							try {
								dt = sdf.parse(tempVal);
							}
							catch (ParseException e2) {
								dt = sdfTime.parse(tempVal);
							}
						}
						if(isStartDateId)
							startDt = new java.util.Date( dt.getTime());
						if(isEndDateId)
							endDt = new java.util.Date( dt.getTime());
					}
					if (!Misc.isUndef(externalAliasedTableIndex)) {
						boolean toExclude = false;
						if (dimConfig.includeFilterFromSubQ != null && dimConfig.includeFilterFromSubQ.size() > 0) {
							toExclude = true;
							for (int t1=0,t1s=dimConfig.includeFilterFromSubQ.size(); t1<t1s;t1++) {
								if (dimConfig.includeFilterFromSubQ.get(t1) == externalAliasedTableIndex) {
									toExclude = false;
									break;
								}
							}
						}
						if (toExclude)
							continue;
						if (dimConfig.excludeFilterFromSubQ != null && dimConfig.excludeFilterFromSubQ.size() > 0) {
							for (int t1=0,t1s=dimConfig.excludeFilterFromSubQ.size(); t1<t1s;t1++) {
								if (dimConfig.excludeFilterFromSubQ.get(t1) == externalAliasedTableIndex) {
									toExclude = true;
									break;
								}
							}
						}
						if (toExclude)
							continue;
					}
					//					boolean isDummy = false;
					
					doVehicle = paramId == 9008 || paramId == 9002 || paramId == 9003;
					if (paramId == 20661) {
						qp.m_limit = Misc.getParamAsInt(tempVal);
						continue;
					}
					if(paramId == 20495){
						if(tempVal == null || "null".equalsIgnoreCase(tempVal) || "-1".equals(tempVal))
							continue;
						else
							m_shift_check = true;	
					}
					seenVehicleStatus = seenVehicleStatus || (dimConfig.m_dimCalc.m_dimInfo.m_id == 9008 && tempVal != null && !"-1000".equals(tempVal) 
							&& tempVal.length() > 0) || (dimConfig.m_dimCalc.m_dimInfo.m_subsetOf == 9000 && tempVal != null && !"-1000".equals(tempVal));
					String end = "";
					// to handle io logged data
					if(paramId == 20450 && "-1000".equals(tempVal)){
						ArrayList dimList = DimInfo.getDimInfo(20450).getValList();
						StringBuilder sb = new StringBuilder();
						ArrayList inList = new ArrayList();
						inList.add(0);
						for (int k = 0; k < dimList.size(); k++) {
							inList.add(((ValInfo)dimList.get(k)).m_id);
						}
						Misc.convertInListToStr(inList, sb);
						tempVal = sb.toString();
						_session.setAttribute(tempVarName, tempVal, false);
					}
					else if(paramId == 20450 && tempVal.length() > 0){
						tempVal = "0,"+tempVal;
						_session.setAttribute(tempVarName, tempVal, false);
					}
					if(is123 && tempVal != null){
						// Hack to handle SMPL -- shud be removed or if it is a bug then resolve.
						/*if("tr_dashboard_link10".equalsIgnoreCase(_session.getParameter("page_context")) && !"22".equalsIgnoreCase(tempVal)){
							tempVal = "22";
						}*/
						//MOVED_TOP if(m_mat_check && m_opStation_check && m_workstation_check)
						//MOVED_TOP 	qp.m_fromClause.append("  join ").append(orgStr);
						if(qp.m_fromClause.indexOf("#") > -1)
							qp.m_fromClause.replace(qp.m_fromClause.indexOf("#"), qp.m_fromClause.indexOf("#") + 1, tempVal);
						orgId = tempVal;
					}
					if(paramId == 9002){
						vehicleName = tempVal;
					}
			//rajeev20130403		if((!is123 && tempVal != null && !"".equals(tempVal) && !"-1000".equals(tempVal)) || dimConfig.m_numeric_filter){
					if((!is123 && tempVal != null && !"".equals(tempVal) && !"-1000".equals(tempVal))){
						boolean amOrBlock = dimConfig.m_orBlock;
						if (!amOrBlock && inOrBlock) {
							whrStr.append(")");
							if(doVehicle && vehicleFilter.toString().contains("vehicle")) {
								vehicleFilter.append(")");
							}
							inOrBlock = false;
						}

						String encl ="";
						int type = dimConfig.m_dimCalc.m_dimInfo.m_type;
						boolean doingTripDate = false;
						boolean doingEventDate = false;
						boolean doingAISDate = false;
						String aisltpTable = null;
						String tName = dimConfig.m_dimCalc.m_dimInfo.m_colMap.table;
						if (tName.equalsIgnoreCase("Dummy"))
							continue;
						String cName = dimConfig.m_dimCalc.m_dimInfo.m_colMap.column;
						if (dimConfig.m_dimCalc.m_dimInfo.m_id == 9008) {
							if (driverTableObjectInfo == null)
								continue;
							tName = driverTableObjectInfo.getName();
							cName = "status";
						}
							
						if (dimConfig.m_numeric_filter && (tempVal == null || tempVal.length() == 0))
							continue;
						
						if (firstTableUsedInTimeTableJoin == null) {
							//if dates in filter but nothing to filter for .. but still remember ..//TODO rewrite dupli of code
							if (isStartDateId || isEndDateId) {								
								continue;
							}
						}
						
						if(!("trick".equals(tName) || "Singleton".equals(tName))){
							
						
							String tempVarNameOperator = tempVarName + "_operator";
							String paramValOperator = _session.getAttribute(tempVarNameOperator);
							int operator = Misc.getParamAsInt(paramValOperator);
							if (operator != 8 && operator != 9 && !isStartDateId && !isEndDateId) {
								boolean usable = dimInfo != null && dimInfo.m_colMap != null && dimInfo.m_colMap.table != null && (tableSeen.containsKey(dimInfo.m_colMap.table) || (dimInfo.m_colMap.base_table != null && tableSeen.containsKey(dimInfo.m_colMap.base_table)));
								if (!usable) {
//other checkes									
								}
								if (!usable)
									continue;
							}
							if (!addWhr) {
								if (amOrBlock) {
									whrStr.append(" (");
									inOrBlock = true;
									if(doVehicle && vehicleFilter.toString().contains("vehicle")) {
										vehicleFilter.append(" (");
									}
								}
							}
							else  {
								if (amOrBlock) {
									if (!inOrBlock) {
										whrStr.append(" and (");
										inOrBlock = true;
									}
									else {
										whrStr.append(" or ");
									}
								}
								else {
									whrStr.append(" and ");
								}
								if(doVehicle && vehicleFilter.toString().contains("vehicle")) {
									if (amOrBlock) {
										if (!inOrBlock) {
											vehicleFilter.append(" and (");
										}
										else {
											vehicleFilter.append(" or ");
										}
									}
									else {
										vehicleFilter.append(" and ");
									}
								}
							}

							
							StringBuilder sbSpecial = null;
							ColumnMappingHelper colMapHelper = dimConfig.m_dimCalc.m_dimInfo.m_colMap;
							if ((operator == 8 || operator == 9)) {//in not in check of multi vals ... special case
								//e.g. consider do_rr_line items and do_rr, 
								//if one of exists (select 1 from line items where fk_id = primary table.id and colName in ());
								//if tempVal contains "null" then exists(select 1 from line items where ... where colName is null)
								//if tempVal contains not null then exists(select where colName is not null)
								if (tempVal != null && tempVal.length() != 0) {
									sbSpecial = new StringBuilder();
									boolean hasNull = "null".equals(tempVal);
									boolean has_ = "_".equals(tempVal);
									sbSpecial.append(operator == 9 ? " not " : "");
									String lineTable = colMapHelper.table;
									
									sbSpecial.append(" exists (select 1 from ").append(lineTable).append(" where ")
									   .append(lineTable).append(".").append(driverTableObjectInfo.getParamName())
									   .append(" = ").append(driverTableObjectInfo.getName()).append(".").append(driverTableObjectInfo.getPrimaryIdCol())
									   ;
									if (colMapHelper.and_clause != null && colMapHelper.and_clause.length() > 0)
										sbSpecial.append(" and ").append(colMapHelper.and_clause);
									sbSpecial.append(" and ");
									
									if (hasNull) {
										colMapHelper.appendTableColName(sbSpecial, null);
										sbSpecial.append(" is null ");
									}
									else if (has_) {
										colMapHelper.appendTableColName(sbSpecial, null);
										sbSpecial.append(" is not null");
									}
									else {
										if (dimInfo.m_type == Cache.STRING_TYPE) {
											String csv[] = null;
											if (fromLink || dimConfig.m_doExactSearch) {
												csv = new String[1];
												csv[0] = tempVal;
											}
											else {
												csv = tempVal.split(",");
											}
											sbSpecial.append("(");
											for (int t1=0,t1s=csv == null ? 0 : csv.length; t1<t1s;t1++) {
												if (t1 != 0)
													sbSpecial.append(" or ");
												colMapHelper.appendTableColName(sbSpecial, null);
												if (exactSearch)
													sbSpecial.append(" like '").append(csv[t1]).append("' ");
												else
													sbSpecial.append(" like '%").append(csv[t1]).append("%' ");
											}
											sbSpecial.append(") ");
										}
										else {
											colMapHelper.appendTableColName(sbSpecial, null);
											//hack for tpr_status
											
											sbSpecial.append(" in (").append(tempVal).append(") ");
										}
									}
									sbSpecial.append(")"); //for exists (select ..
									whrStr.append("(").append(sbSpecial).append(")");
									addWhr = true;
								}
								continue;
							}
							else if ("_".equals(tempVal) || "null".equals(tempVal) || (isIntLike && (Misc.G_HACK_ISNULL_LOVSTR.equals(tempVal) || Misc.G_HACK_ISNOTNULL_LOVSTR.equals(tempVal)))) {
								StringBuilder sb = new StringBuilder();
								sb.append(" ( ");
								if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.useColumnOnlyForName)
									sb.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
								else
									sb.append(tName + "." + cName);
								sb.append(" is ").append("_".equals(tempVal) || Misc.G_HACK_ISNOTNULL_LOVSTR.equals(tempVal) ? " not " :"").append("null ");
								sb.append(" ) ");
								
								whrStr.append(sb);
								if (doVehicle)
									vehicleFilter.append(sb);
								addWhr = true;
								continue;
							}
							// to handle numeric filter
							else if(dimConfig.m_numeric_filter) {
								String aggregateOp = null;
								if (dimConfig.m_aggregate) {
									String aggParamName = searchBoxHelper == null ? "p20053": searchBoxHelper.m_topPageContext+"20053";
									int aggDesired =Misc.getParamAsInt(_session.getParameter(aggParamName));
									DimInfo aggDim = DimInfo.getDimInfo(20053);
									if (aggDim != null) {
										DimInfo.ValInfo valInfo = aggDim.getValInfo(aggDesired);
										if (valInfo != null) {
											aggregateOp = valInfo.getOtherProperty("op_text");
										}
									}
									if (dimConfig.m_default != null && !"".equals(dimConfig.m_default))
										aggregateOp = dimConfig.m_default;
									if (aggregateOp == null || aggregateOp.length() == 0)
										aggregateOp = "sum";
								}
								
								String tempVarNameOperandFirst = tempVarName + "_operand_first";
								String tempVarNameOperandSecond = tempVarName + "_operand_second";

								String paramValOperandFirst = _session.getAttribute(tempVarNameOperandFirst);
								String paramValOperandSecond = _session.getAttribute(tempVarNameOperandSecond);
								
								
								StringBuilder sb = new StringBuilder();
								boolean doAppend = false;
								if(aggregateOp != null && aggregateOp.length() > 0){
									sb.append(aggregateOp).append("(");
								}
								if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.useColumnOnlyForName)
									sb.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
								else
									sb.append(tName + "." + cName);
								if(aggregateOp != null && aggregateOp.length() > 0){
									sb.append(") ");
								}
								if(paramValOperator != null && paramValOperator.length() > 0 && tempVarNameOperandFirst != null
										&& tempVarNameOperandFirst.length() > 0){
									//int operandFirst = Misc.getParamAsInt(paramValOperandFirst);
									switch(operator){
									case 1:
										sb.append(" > ").append(paramValOperandFirst);
										doAppend = true;
										break;
									case 2:
										sb.append(" < ").append(paramValOperandFirst);
										doAppend = true;
										break;
									case 3:
										sb.append(" = ").append(paramValOperandFirst);
										doAppend = true;
										break;
									case 4:
										sb.append(" >= ").append(paramValOperandFirst);
										doAppend = true;
										break;
									case 5:
										sb.append(" <= ").append(paramValOperandFirst);
										doAppend = true;
										break;
									case 6:
										sb.append(" <> ").append(paramValOperandFirst);
										doAppend = true;
										break;
									case 7:
										if(paramValOperandSecond != null && paramValOperandSecond.length() > 0){
											sb.append(" BETWEEN ").append(paramValOperandFirst).append(" and ").append(paramValOperandSecond);
											doAppend = true;
											break;
										}
									default:
										if(whrStr.length() > 5 && " and ".equalsIgnoreCase(whrStr.substring(whrStr.length() - 5, whrStr.length())))
											whrStr.setLength(whrStr.length()-5);

									}
									if(doAppend && aggregateOp != null && aggregateOp.length() > 0){
										qp.m_havingClause.append(sb);
									}
									else if(doAppend){
										whrStr.append(" ( ").append(sb).append(" ) ");
										addWhr = true;
									}
								}
								continue;
							}

							// to handle comma separated multi text search
							//if (dimConfig.m_dimCalc.m_dimInfo.m_id == 9002)

							if (dimConfig.m_dimCalc.m_dimInfo.m_type == Cache.STRING_TYPE) {
								whrStr.append(" ( ");
								if(doVehicle)
									vehicleFilter.append(" ( ");
								for (int l=0,ls = dimConfig.m_addnlDimInfoNew == null  ? 1 : dimConfig.m_addnlDimInfoNew.size()+1; l<ls; l++) {
									DimInfo useme = l == 0 ? dimConfig.m_dimCalc.m_dimInfo : ((DimCalc) dimConfig.m_addnlDimInfoNew.get(l-1)).m_dimInfo;
									if (l != 0){
										whrStr.append(" or ");
										if(doVehicle)
											vehicleFilter.append(" or ");
										}
									String [] tempValArray = tempVal.split(",");
									boolean first = true;
									for (int k = 0; k < tempValArray.length; k++) {
										if (tempValArray[k] == null)
											continue;
										tempValArray[k] = tempValArray[k].trim();
										if (tempValArray[k].length() == 0)
											continue;
										if(!first){
											whrStr.append(" or ");
											if(doVehicle)
												vehicleFilter.append(" or ");
										}
										first = false;
										whrStr.append(" ( ");
										if(doVehicle)
											vehicleFilter.append(" ( ");
										if (useme.m_colMap.useColumnOnlyForName){
											whrStr.append(useme.m_colMap.column);
											if(doVehicle)
												vehicleFilter.append(useme.m_colMap.column);
										}
										else{
											whrStr.append(useme.m_colMap.table + "." + useme.m_colMap.column);
											if(doVehicle)
												vehicleFilter.append(useme.m_colMap.table + "." + useme.m_colMap.column);
										}
										if ("null".equals(tempValArray[k])){
											whrStr.append(" is null ");
											if(doVehicle)
												vehicleFilter.append(" is null ");
										}
										else {
											if (exactSearch)
												whrStr.append(" like '");
											else
												whrStr.append(" like '%");
											
											encl = exactSearch ? "'" : "%'";
											end = " ";
											if(doVehicle){
												if (exactSearch)
													vehicleFilter.append(" like '");
												else
													vehicleFilter.append(" like '%");
												vehicleFilter.append(tempValArray[k] + encl);
											}
											whrStr.append(tempValArray[k] + encl);
										}
										if(doVehicle)
											vehicleFilter.append(" ) ");
										whrStr.append(" ) ");
									}//for each multi valued
								}//for each orable dim to search
								addWhr = true;
								if(doVehicle)
									vehicleFilter.append(" ) ");
								whrStr.append(" ) ");
								continue;
							}
							addWhr = true;
							if(type != DATE_TYPE) {
								if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.useColumnOnlyForName)
									whrStr.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
								else{
									whrStr.append(tName + "." + cName);
									if(doVehicle)
										vehicleFilter.append(tName + "." + cName);
								}
							}
							switch(type){
							case LOV_TYPE :
							case LOV_NO_VAL_TYPE:
							case INTEGER_TYPE:
								if (paramId == 20495) {
									if (exactSearch)
										whrStr.append(" like '");
									else
										whrStr.append(" like '%");
									encl = exactSearch ? "'" : "%'";
									end = " ";
									if(doVehicle) {
										if (exactSearch)
											vehicleFilter.append(" like '");
										else
											vehicleFilter.append(" like '%");
									}
								}else{
									whrStr.append(" in (");
									if(doVehicle)
										vehicleFilter.append(" in (");
									encl = "";
									end = ") ";
								}
								break;
							case STRING_TYPE :
								if("null".equalsIgnoreCase(tempVal)){
									whrStr.append(" is ");
									encl = "";
									end = " ";
									if(doVehicle)
										vehicleFilter.append(" is ");
								}else{
									if (exactSearch)
										whrStr.append(" like '");
									else
										whrStr.append(" like '%");
									encl = exactSearch ? "'" : "%'";
									end = " ";
									if(doVehicle) {
										if (exactSearch)
											vehicleFilter.append(" like '");
										else
											vehicleFilter.append(" like '%");
									}
								}
								break;
							case NUMBER_TYPE : whrStr.append(" = ");
							if(doVehicle)
								vehicleFilter.append(" = ");
							encl = "";
							end = "";
							break;
							case DATE_TYPE :
								boolean toAdd = true;
								
								//TODO combo_start thingy HACK
								
								if ("challan_details".equals(firstTableUsedInTimeTableJoin)) {
									doingTripDate = true;
									if (doIncludeLatestTrip <= 1) {
										whrStr.append(" (( " );//why??
										whrStr.append(firstTableUsedInTimeTableJoin).append(".").append("challan_date ");
									}
									else {
										whrStr.append(" (challan_details.challan_date = challan_details_ltp.cst or challan_details.id is null) " );//why??
									}
									toAdd = false;
								}
								else if ("trip_info".equals(firstTableUsedInTimeTableJoin) && (dimConfig.m_dimCalc.m_dimInfo.m_id == 20035 
										|| dimConfig.m_dimCalc.m_dimInfo.m_id == 20036)) {
									doingTripDate = true;
									if (doIncludeLatestTrip <= 1) {
										whrStr.append(" ((trip_info.shift_date like '%_%' and " );//why??
										whrStr.append(timeCol);
									}
									else {
										whrStr.append(" (trip_info.combo_start = trip_info_ltp.cst or trip_info.id is null) " );//why??
									}
									toAdd = false;
								}
								else if ("engine_events".equals(firstTableUsedInTimeTableJoin)) {
									doingEventDate = true;
									if (doIncludeLatestTrip <= 1) {
										whrStr.append(" ((" );//why??
										whrStr.append("engine_events.event_start_time ");
									}
									else {
										whrStr.append(" (engine_events.event_start_time  = engine_events_ltp.cst or engine_events.id is null) " );//why??
									}
									toAdd = false;
								}
								else if ("ais_logged_data".equals(firstTableUsedInTimeTableJoin)) {
									doingAISDate = true;
									aisltpTable = "aislgdltp";
									if (doIncludeLatestTrip <= 1) {
										whrStr.append(" ((" );//why??
										whrStr.append("ais_logged_data.gps_record_time ");
									}
									else {
										whrStr.append(" (ais_logged_data.gps_record_time  = aislgdltp.grt) " );//why??
									}
									toAdd = false;
								}
								else if ("ais_health_data".equals(firstTableUsedInTimeTableJoin)) {
									doingAISDate = true;
									aisltpTable = "aishltltp";
									if (doIncludeLatestTrip <= 1) {
										whrStr.append(" ((" );//why??
										whrStr.append("ais_health_data.gps_record_time ");
									}
									else {
										whrStr.append(" (ais_health_data.gps_record_time  = aishltltp.grt) " );//why??
									}
									toAdd = false;
								}
								else if ("ais_emergency_data".equals(firstTableUsedInTimeTableJoin)) {
									doingAISDate = true;
									aisltpTable = "aisemeltp";
									if (doIncludeLatestTrip <= 1) {
										whrStr.append(" ((" );//why??
										whrStr.append("ais_emergency_data.gps_record_time ");
									}
									else {
										whrStr.append(" (ais_emergency_data.gps_record_time  = aisemeltp.grt) " );//why??
									}
									toAdd = false;
								}
								else if ("ais_login_data".equals(firstTableUsedInTimeTableJoin)) {
									doingAISDate = true;
									aisltpTable = "aisloginltp";
									if (doIncludeLatestTrip <= 1) {
										whrStr.append(" ((" );//why??
										whrStr.append("ais_login_data.gps_record_time ");
									}
									else {
										whrStr.append(" (ais_login_data.gps_record_time  = aisloginltp.grt) " );//why??
									}
									toAdd = false;
								}
								else if (isStartDateId || isEndDateId) {
									//start/end date is being used as a proxy ..
									//guess the table against which to filter
									whrStr.append(timeCol);
									toAdd = false;
								}
								else if (dimConfig.m_dimCalc.m_dimInfo.m_id == 94964) {//special hack for secl dorr hist rate list date
									String replStr = dimConfig.m_dimCalc.m_dimInfo.m_colMap.column;
									Date dt = null;
									try {
										
										if (tempVal != null && tempVal.length() > 0) {
											try {
												dt = sdfTime.parse(tempVal);
												
											}
											catch (ParseException e2) {
												dt = sdf.parse(tempVal);
											}
										}
									}
									catch (Exception e3) {
										dt = new Date();
									}
										
									replStr = replStr.replaceAll("@", indepDateFormat.format(dt));
									toAdd = false;
									whrStr.append(replStr);
									continue;
								}
								if (toAdd) {
									if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.useColumnOnlyForName)
										whrStr.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
									else
										whrStr.append(tName + "." + cName);
								}
								if ((!doingTripDate && !doingEventDate && !doingAISDate) || doIncludeLatestTrip <= 1) {
									if(dimConfig.m_forDateApplyGreater)
										whrStr.append(" >= '");
									else
										whrStr.append(" <= '");
								}
								encl = "'";
								end = "";

								break;
							default :
								if (exactSearch)
									whrStr.append(" like '");
								else
									whrStr.append(" like '%");
							if(doVehicle) {
								if (exactSearch)
									vehicleFilter.append(" like '");
								else
									vehicleFilter.append(" like '%");
							}
							encl = exactSearch ? "'" : "%'";
							end = ") ";
							}

							if(type != DATE_TYPE){
								if (paramId == 20495 ) {
									m_shiftStr = " like '"+(exactSearch ? "" : "%") + ("1".equals(tempVal) ? "ShiftA" : "2".equals(tempVal) ? "ShiftB" : "3".equals(tempVal) ? "ShiftC" : "_") + encl;
									whrStr.append(("1".equals(tempVal) ? "ShiftA" : "2".equals(tempVal) ? "ShiftB" : "3".equals(tempVal) ? "ShiftC" : "_") + encl);
									if(doVehicle)
										vehicleFilter.append(tempVal + encl);
								}else{
								whrStr.append(tempVal + encl);
								if(doVehicle)
									vehicleFilter.append(tempVal + encl);
								}
							}
							else{
								try {
									Date dt = null;
									if (isTime) {
										try {
											dt = sdfTime.parse(tempVal);
										}
										catch (ParseException e2) {
											dt = sdf.parse(tempVal);
										}
									}
									else {
										try {
											dt = sdf.parse(tempVal);
										}
										catch (ParseException e2) {
											dt = sdfTime.parse(tempVal);
										}
									}
									if(isStartDateId)
										startDt = new java.util.Date( dt.getTime());
									if(isEndDateId)
										endDt = new java.util.Date( dt.getTime());
									//TODO HACK
									if ((!doingTripDate && !doingEventDate && !doingAISDate)|| doIncludeLatestTrip <= 1) {
										if (firstTableUsedInTimeTableJoin != null &&  colForFirstTableUsedInTimeTableJoin != null 
												&& firstTableUsedInTimeTableJoin.equalsIgnoreCase("@period") 
												&& colForFirstTableUsedInTimeTableJoin.equalsIgnoreCase("start_time")){
											if ( isStartDateId){
												whrStr.append("@start_period"+encl);
											}
											else if ( isEndDateId){
												whrStr.append("@end_period"+encl);
											}
										} else {
											if ( isStartDateId){
												whrStr.append("@full_user_start"+encl);
											}
											else if ( isEndDateId){
												whrStr.append("@full_user_end"+encl);
											}
											else
												whrStr.append(indepDateFormat.format(dt) + encl);
										}
									}
									if (doingTripDate || doingEventDate || doingAISDate) {
										if (doIncludeLatestTrip <= 0)
											whrStr.append(")");
										else if (doIncludeLatestTrip == 1) {
											if (doingTripDate)
												if ("challan_details".equals(firstTableUsedInTimeTableJoin)) {
													whrStr.append(" )or challan_details.id is null or challan_details.challan_date = challan_details_ltp.cst");
												}
												else {
													whrStr.append(" )or trip_info.id is null or trip_info.combo_start = trip_info_ltp.cst");
												}
											else if (doingEventDate)
												whrStr.append(" )or engine_events.id is null or engine_events.event_start_time = engine_events_ltp.cst");
											else if (doingAISDate)
												whrStr.append(") or ").append(firstTableUsedInTimeTableJoin).append(".gps_record_time = ").append(aisltpTable).append(".grt");
										}
										if (doIncludeLatestTrip <= 1)
											whrStr.append(")");
									}

								} catch (ParseException e) {
									e.printStackTrace();
									throw e;
								}
							}
						}
					}
					//rajeev 8th jul 2011
					//if(addWhr)
					//	whrCl = whrStr.append(end).toString();
					if (addWhr){
						whrStr.append(end);
						if(doVehicle)
							vehicleFilter.append(end);
					}
				}
			}
			if (inOrBlock) {
				whrStr.append(")");
				if(doVehicle && vehicleFilter.toString().contains("vehicle")) {
					vehicleFilter.append(")");
				}
				inOrBlock = false;
			}

			if (!seenVehicleStatus && driverTableObjectInfo != null && !"secl_tpr_invoice_hist".equals(driverTableObjectInfo.getName()) && !"users".equals(driverTableObjectInfo.getName()) &&  !"Dummy".equals(driverTableObjectInfo.getName()) && !"tr_opstation_vehicle.xml".equalsIgnoreCase(_session.getParameter("front_page"))) {
				if (addWhr)
					whrStr.append(" and ");
				whrStr.append(driverTableObjectInfo.getName()).append(".status in (1,2)");
				if (vehicleFilter != null && driverTableObjectInfo.getId() == WorkflowHelper.G_OBJ_VEHICLES) {
					if (vehicleFilter.indexOf("vehicle") > 0)
						vehicleFilter.append(" and ");
					vehicleFilter.append(" vehicle.status in (1,2)");
				}
				addWhr = true;
			}
			addWhr = addObjectIdPassedInWhr(_session, driverTableObjectInfo, whrStr, null, addWhr);
		}catch (Exception e){
			e.printStackTrace();
		}
		
		whrCl = whrStr.toString();
		return whrCl;
	}

	public void GetDimInfoJSObject(DimInfo dimInfo, JspWriter out) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("<script>");
		sb.append("var l_dimInfo").append(dimInfo.m_name).append(" = new Array();");
		ArrayList valList = dimInfo.getValList();
		for (int i=0,count=valList == null ? 0 : valList.size();i<count;i++) {

			DimInfo.ValInfo valInfo = (DimInfo.ValInfo) valList.get(i);
			int id = valInfo.m_id;
			String nameStr = valInfo.m_name;
			sb.append("l_dimInfo").append(dimInfo.m_name).append(" [").append(id).append("] = ").append(" \"").append(nameStr).append("\";");
		}
		sb.append("</script>");
		out.println(sb);
	} 


	/*
   In order to show attribute value for no dist in same row ... we are doing this
   we will do for logged_data, current_data, engine_events, latest ... but others can also be done
   Imagine we have following tables
   for logged_data/current_data
   vehicle_id, gps_record_time, attribute_<ID>, attribute_<ID>, attribute_<ID> ....

     for engine_evnts
     vehicle_id, time stamp, on_off_<RULE_ID>,start_<RULE_ID>. end_<RULE_ID>, id_<RULE_ID>, begin_name_<RULE_ID>, end_name_<RULE_ID> .... 

     for latest_engine_event, similar to engine_events but having latest only

     How will it be configured:
     In DimInfo's def (internal.xml), the table will start with f$<table_name>
     In DimConfigInfo def (the col cell) we will have an attrib m_lookHelp1 (attrib name look1) of string which is the col name for vehicle,
      m_lookHelp2 of Date (look2) which is the time at which we want to find vals

       How will it processed:
       Based upon the vehicle and time constraint of start/end we will read the values of from the respective table and store it in HashMap of FastList
       When looking for logged_data/current_data, we look at the value that is nearest but before while when doing engine_events, 
       we look at the value that is in between time tasked

       Now more about data structures used
       consider first build query
       we look for 4 queries - log/curr/engine event/latest engine event
       we want to make 1 query for each - so will collect all attribId that are being asked for and get results
       But device events (24) present special challenge. For e.g with itrac 24=91 means low battery etc. So we add a hack ... 
       we will also put the and_clause in dimInfo
       in sel as (case when XX then 1 else 0 end). If this value is 0 then this value did not exist at all.

       In build query part we will do preprocess we'd keep for each attrib the dimconfig's index in which it is used and the index 
       (after fixed sel) the 'trueness' part


       Next look at query execution
       Conceptually for each of table we will have a hashmap of pair<dimConfigIndex, vehicleId> to FastList<Data>
       To represent data we have VirtualVal extended by GpsData and EventData
       In both we have getVal method, that takes col given in dim info to get the val. In case of eventData the getVal takes as parameter the time ...
        inorder to calc on/off






	 */
	



	public ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>> buildResultForVirtualTables (Connection conn,  QueryParts qp, SessionManager session, SearchBoxHelper searchBoxHelper) throws Exception {
		//ArrayList - index corresponds to whether logged, current, engine_events or latest engine events
		//HashMap key's first = vehicleId, colIndex in second
		//a given attrib Id may be added against multiple colIndex ... see comments in buildQuery qp.XXX
		//

		try {
			ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>> retval = new ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>>();
			for (int i=0,is = qp.m_virtualQuery == null ? 0 : qp.m_virtualQuery.length; i<is;i++)
				retval.add(null);
			for (int i=0,is = qp.m_virtualQuery == null ? 0 : qp.m_virtualQuery.length; i<is;i++) {
				StringBuilder query = qp.m_virtualQuery[i];
				if (query == null)
					continue;
				for (int a=0;a<2;a++) {
					if (a == 1)
						query = qp.m_virtualQueryJustBefore[i];
					if (query == null)
						continue;
					query = helperFixSummaryWithCurrParam(query, session, searchBoxHelper, null,null, Misc.getUndefInt(), WorkflowHelper.G_OBJ_VEHICLES, false, null);
					Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
					stmt.setFetchSize(Integer.MIN_VALUE);
					//				System.out.println("GeneralizedQueryBuilder.printPage()   : 111  stmt.getFetchSize()   :   : "+stmt.getFetchSize());
					ResultSet rs = stmt.executeQuery(query.toString());
					HashMap<MiscInner.Pair, FastList<VirtualVal>> addToThis = retval.get(i);
					if (addToThis == null) {
						addToThis = new HashMap<MiscInner.Pair, FastList<VirtualVal>>();
						retval.set(i, addToThis);
					}
					while (rs.next()) {
						int vehicleId = rs.getInt(1);
						Date time = Misc.sqlToUtilDate(rs.getTimestamp((2)));
						int attrib = rs.getInt(3);
						VirtualVal addVal = null;
						double odoDay = Misc.getUndefDouble();
						double odoWeek = Misc.getUndefDouble();
						double odoMonth = Misc.getUndefDouble();
						if (i == 1) {
							odoDay = Misc.getRsetDouble(rs, 6);
							odoWeek = Misc.getRsetDouble(rs, 7);
							odoMonth = Misc.getRsetDouble(rs, 8);
						}
						if (i == 0 || i == 1) {
							addVal = new VirtualGpsData(time, Misc.getRsetDouble(rs, 4), Misc.getRsetDouble(rs, 5), odoDay, odoWeek, odoMonth);	
						}
						else {
							addVal = new VirtualEventData(time, Misc.sqlToUtilDate(rs.getTimestamp(4)), rs.getInt(5), rs.getString(6), rs.getString(7),
									rs.getDouble(8));
						}
						ArrayList<Pair<Integer, Integer>> colList = qp.m_virtualAttribIdToDimConfigColAnd1Checker.get(i).get(attrib);
						for (int j=0,js=colList == null ? 0 : colList.size();j<js;j++) {
							Pair<Integer, Integer> whichAddTo = colList.get(j);
							int colIndexInDimConfig = whichAddTo.first;
							int checkFor1 = whichAddTo.second == null ? -1 : whichAddTo.second;
							if (checkFor1 < 0 || rs.getInt(whichAddTo.second+1) == 1) {
								MiscInner.Pair key = new MiscInner.Pair(vehicleId, colIndexInDimConfig);
								FastList<VirtualVal> list = addToThis.get(key);
								if (list == null) {
									list = new FastList<VirtualVal>();
									addToThis.put(key, list);
								}
								list.add(addVal);
							}
						}
					}
					rs.close();
					stmt.close();
				}
			}	
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void buildQueryForVirtualTables (ArrayList<DimConfigInfo> dimConfigList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SessionManager session,
			SearchBoxHelper searchBoxHelper, QueryParts qp, HashMap<String, Integer>nameIndexLookup) {
		//things related to tpr blocks NOT DONE YET ... but should not impact .. so long as we dont have dims with f$tpr_block_status and f$latest_tpr_block_status
		// 0 = logged_data, 1 = current_data, 2 = engine_events, 3 = latest_engine_events, 4 = tpr blocks, 5 = latest blocks existing
		int numTabs = 4;
		StringBuilder attrib[] = new StringBuilder[numTabs];
		ArrayList<VirtualHelper> col = new ArrayList<VirtualHelper>();

		StringBuilder query[] = new StringBuilder[numTabs];
		StringBuilder justBeforeQuery[] = new StringBuilder[numTabs];
		StringBuilder and[] = new StringBuilder[numTabs];
		ArrayList<HashMap<Integer, ArrayList<Pair<Integer, Integer>>>> virtualAttribIdToDimConfigColAnd1Checker = new ArrayList<HashMap<Integer,
				ArrayList<Pair<Integer, Integer>>>>();
		int casePart[] = new int[numTabs];
		for (int i=0;i<numTabs;i++) {
			virtualAttribIdToDimConfigColAnd1Checker.add(null);
			casePart[i] = 0;
		}
		qp.m_virtualCol = col;
		qp.m_virtualQuery = query;
		qp.m_virtualAttribIdToDimConfigColAnd1Checker = virtualAttribIdToDimConfigColAnd1Checker;
		qp.m_virtualQueryJustBefore = justBeforeQuery;


		for (int i=0,is=dimConfigList.size();i<is;i++){
			DimConfigInfo dimConfig = dimConfigList.get(i);

			if (dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null || dimConfig.m_dimCalc.m_dimInfo.m_colMap == null ||
					dimConfig.m_dimCalc.m_dimInfo.m_colMap.table == null || !dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.startsWith("f$")) {
				col.add(null);
				continue;
			}
			String table = dimConfig.m_dimCalc.m_dimInfo.m_colMap.table;
			String tcol = dimConfig.m_dimCalc.m_dimInfo.m_colMap.column;

			if (tcol == null || tcol.equals("Dummy")) {
				col.add(null);
				continue;
			}
			table = table.substring(2);
			int last_ = tcol.lastIndexOf('_'); 
			String attribId = tcol.substring(last_+1);
			int attribInt = Misc.getParamAsInt(attribId);
			tcol = tcol.substring(0, last_);
			
			String caseClause = dimConfig.m_dimCalc.m_dimInfo.m_colMap.and_clause; 
			if (caseClause != null && caseClause.length() == 0)
				caseClause = null;
			int index = -1;
			
			if (table.equals("logged_data")) {
				index = 0;
			}
			else if (table.equals("current_data")) {
				index = 1;
				//look1="d20274" look2="d20173"
				if (true) {//HACK
					int dimId = dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null ? dimConfig.m_dimCalc.m_dimInfo.m_id : Misc.getUndefInt();

					if (dimId >= 20312 && dimId <= 20316) {
						if (dimConfig.m_lookHelp1 == null || dimConfig.m_lookHelp1.length() == 0)
							dimConfig.m_lookHelp1 = "d20274";
						if (dimConfig.m_lookHelp2 == null || dimConfig.m_lookHelp2.length() == 0)
							dimConfig.m_lookHelp2 = "d20173";
					}
				}
			}
			else if (table.equals("engine_events")) {
				index = 2;
			}
			else if (table.equals("latest_engine_events")) {
				index = 3;
			}
			else if (table.equals("tpr_block_status")) {
				index = 4;
			}
			else if (table.equals("latest_tpr_block_status")) {
				index = 5;
			}
			if (index < 0) {
				col.add(null);
				continue;
			}
			//tquery.append("select vi.vehicle_id, gps_record_time, attribute_id, attribute_value").append(and[j] == null ? "" : and[j]).append(" from ")
			//tquery.append("select vi.vehicle_id, event_start_time, rule_id attrid, event_stop_time, engine_events.id, event_begin_name, event_end_name").append(and[j] == null ? "" : and[j]).append(" from ")
			//tquery.append("select tpr_id, workstation_type_id,instruction_id,override_status,override_workstation_type_id,create_type from tpr_block_status
			int fixedColCount = index == 0 || index == 1 ? 3 : index == 4 || index == 5 ? 6 : 7;

			HashMap<Integer, ArrayList<Pair<Integer, Integer>>> mapper = virtualAttribIdToDimConfigColAnd1Checker.get(index);
			if (mapper == null) {
				mapper = new HashMap<Integer, ArrayList<Pair<Integer, Integer>>>();
				virtualAttribIdToDimConfigColAnd1Checker.set(index, mapper);
			}
			ArrayList<Pair<Integer, Integer>> mappingList = mapper.get(attribInt);
			if (mappingList == null) {
				mappingList = new ArrayList<Pair<Integer, Integer>>();
				mapper.put(attribInt, mappingList);
			}
			mappingList.add(new Pair<Integer, Integer>(i, caseClause == null ? -1 : casePart[index]+fixedColCount));
			if (caseClause != null)
				casePart[index] += 1;

			VirtualHelper virtualHelper = new VirtualHelper();
			virtualHelper.isVirtual = true;
			virtualHelper.tabIndex = index;
			virtualHelper.col = tcol;
			Integer t1 = dimConfig.m_lookHelp1 == null || dimConfig.m_lookHelp1.length() == 0 ? null : nameIndexLookup.get(dimConfig.m_lookHelp1);
			Integer t2  = dimConfig.m_lookHelp2 == null || dimConfig.m_lookHelp2.length() == 0 ? null : nameIndexLookup.get(dimConfig.m_lookHelp2);
			virtualHelper.look1 = t1 == null ? -1 : t1.intValue();
			virtualHelper.look2 = t2 == null ? -1 : t2.intValue();
			col.add(virtualHelper);
			StringBuilder tattrib = attrib[index];
			StringBuilder tadd = and[index];
			if (tattrib == null) {
				tattrib = attrib[index] = new StringBuilder();
			}
			else {
				tattrib.append(",");
			}
			tattrib.append(attribId);

			if (caseClause != null) {				
				if (tadd == null) {
					tadd = and[index] = new StringBuilder();
				}				
				tadd.append(", (case when (").append(caseClause).append(") then 1 else 0 end) ");			
			}
		}
		String orgStr = "(select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "
				+ ") vi on vi.vehicle_id = vehicle.id ";
		String orderClause = null;

		for (int j=0;j<numTabs;j++) {
			if (attrib[j] != null) {
				StringBuilder tquery = query[j] = new StringBuilder();
				if (j == 0) {
					tquery.append("select vi.vehicle_id v1, gps_record_time, attribute_id, attribute_value, speed ").append(and[j] == null ? "" : and[j])
					.append(" from ")
					.append("vehicle join ").append(orgStr).append(" join logged_data on (vi.vehicle_id = logged_data.vehicle_id) ")
					;
					orderClause = " order by v1, attribute_id, gps_record_time ";
					String whrJustBeforeClause = getWhrClauseVirtual(j == 0 ? "logged_data" : j == 1 ? "current_data" : j == 2 ? "engine_events" 
							: "latest_engine_events", session, searchBox, searchBoxHelper, true);
					if (whrJustBeforeClause != null) {
						justBeforeQuery[j] = new StringBuilder();
						justBeforeQuery[j].append("select vehicle_id v1, gps_record_time, attribute_id, attribute_value, speed ")
						.append(and[j] == null ? "" : and[j]).append(" from ")					
						.append("(select vi.vehicle_id vid, attribute_id aid, max(gps_record_time) grt from vehicle join ")
						.append(orgStr).append(" join logged_data on (vi.vehicle_id = logged_data.vehicle_id and attribute_id in (")
						.append(attrib[j]).append(")) where ").append(whrJustBeforeClause)
						.append(" group by vi.vehicle_id, attribute_id) mx join logged_data on (logged_data.vehicle_id = mx.vid ")
						.append("and logged_data.attribute_id = mx.aid and logged_data.gps_record_time = mx.grt) ")
						.append(orderClause);
					}

					;
				}
				else if (j == 1) {
					tquery.append("select vi.vehicle_id v1, gps_record_time, attribute_id attrid, attribute_value, speed, odometer_day, odometer_week, odometer_month ").append(and[j] == null ? "" 
							: and[j]).append(" from ")
							.append("vehicle join ").append(orgStr).append(" join current_data on (vi.vehicle_id = current_data.vehicle_id) ")
							;
					orderClause = " order by v1, attribute_id, gps_record_time ";
				}
				else if (j == 2) {
					tquery.append("select vi.vehicle_id v1, event_start_time, rule_id attrid, event_stop_time, engine_events.id, event_begin_name," +
							" event_end_name, addnl_value1 ").append(and[j] == null ? "" : and[j]).append(" from ")
							.append("vehicle join ").append(orgStr).append(" join engine_events on (vi.vehicle_id = engine_events.vehicle_id) ")
							;
					orderClause = " order by v1, attribute_id, engine_events.event_start_time ";
					String whrJustBeforeClause = getWhrClauseVirtual(j == 0 ? "logged_data" : j == 1 ? "current_data" : j == 2 ? "engine_events" 
							: "latest_engine_events", session, searchBox, searchBoxHelper, true);
					if (whrJustBeforeClause != null) {
						justBeforeQuery[j] = new StringBuilder();
						justBeforeQuery[j].append("select vehicle_id v1, event_start_time, rule_id attrid, event_stop_time, engine_events.id, " +
								"event_begin_name, event_end_name, addnl_value1").append(and[j] == null ? "" : and[j]).append(" from ")					
								.append("(select vi.vehicle_id vid, rule_id aid, max(event_start_time) grt from vehicle join ").append(orgStr)
								.append(" join engine_events on (vi.vehicle_id = engine_events.vehicle_id and rule_id in (")
								.append(attrib[j]).append(")) where ").append(whrJustBeforeClause)
								.append(" group by vi.vehicle_id, rule_id) mx join engine_events on (engine_events.vehicle_id = mx.vid ")
								.append("and engine_events.rule_id = mx.aid and engine_events.event_start_time = mx.grt) ")
								.append(orderClause);						
					}
				}
				else if (j == 3) {
					tquery.append("select vi.vehicle_id v1, event_start_time, rule_id attrid, event_stop_time, engine_events.id, event_begin_name,")
					.append(" event_end_name, addnl_value1 ").append(and[j] == null ? "" : and[j]).append(" from ")
					.append("vehicle join ").append(orgStr).append(" join ")
					.append("(select vehicle_id, rule_id rid, max(engine_events.event_start_time) est from engine_events where rule_id in (")
					.append(attrib[j]).append(") group by vehicle_id, rule_id) latest_engine_events on (latest_engine_events.vehicle_id = vi.vehicle_id) ")
					.append(" join engine_events on (engine_events.vehicle_id = latest_engine_events.vehicle_id " )
					.append("and engine_events.rule_id = latest_engine_events.rid and engine_events.event_start_time = latest_engine_events.est) ")
					;
					orderClause = " order by v1, attribute_id, engine_events.event_start_time ";
				}
				else if (j == 4 || j == 5) {
					tquery.append("select tpr_id v1, 0 as grt, workstation_type_id,instruction_id,override_status,override_workstation_type_id,create_type ")
//		            .append(" from vehicle join ").append(orgStr).append(" on (vi.vehicle_id = vehicle.id) ").append(" join tp_record on (tp_record.vehicle_id = vehicle.id) join tpr_block_status on (tp_record.tpr_id = tpr_block_status.tpr_id) ");
					;
					orderClause = " order by v1, workstation_type_id,instruction_id ";
				}
				if (j == 4 || j == 5) {

					tquery.append(" from ").append(qp.m_fromClause);
					tquery.append("  left outer join tpr_block_status on (tp_record.tpr_id = tpr_block_status.tpr_id) ");
					if (qp.m_whereClause.toString().equals("null") || qp.m_whereClause.toString().equalsIgnoreCase(" where ")) {
						if (j == 5)
							tquery.append(" where ");
					}
					else {
						tquery.append(qp.m_whereClause);
						if (j == 5)
							tquery.append(" and ");
					}
					if (j == 5)
						tquery.append(" tpr_block_status.override_status is null or tpr_block_status.override_status = 0) ");
					tquery.append(orderClause);
				}
				else {
					String whereClause = getWhrClauseVirtual(j == 0 ? "logged_data" : j == 1 ? "current_data" : j == 2 ? "engine_events" :
						j == 3 ? "latest_engine_events" : j ==4 ? "tpr_block_status" : "latest_tpr_block_status", session, searchBox, searchBoxHelper, false);

					tquery.append(" where ").append(j == 0 ||j == 1 ? " attribute_id ": " rule_id ").append(" in (").append(attrib[j]).append(")");
					if (whereClause != null && whereClause.length() > 0)
						tquery.append(" and ").append(whereClause);
					tquery.append(orderClause);
				}
			}

		}	

	}

	private String getWhrClauseVirtual(String baseTable, SessionManager _session, ArrayList<ArrayList<DimConfigInfo>> searchBox,
			SearchBoxHelper searchBoxHelper, boolean doJustBefore) {	
		String whrCl = null;
		StringBuilder whrStr = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		SimpleDateFormat sdfTime = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		SimpleDateFormat indepDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		WorkflowHelper.TableObjectInfo driverTableObjectInfo = WorkflowHelper.getTableInfo(G_OBJ_VEHICLES);
		boolean addWhr = false;
		if (searchBoxHelper == null) {
			
			addWhr = addObjectIdPassedInWhr(_session, driverTableObjectInfo, whrStr, null, addWhr);
			return addWhr ? whrStr.toString() : null;
		}
		//            int maxCol = searchBoxHelper.m_maxCol;
		String topPageContext = searchBoxHelper.m_topPageContext;
		
		boolean seenVehicleStatus = false;
		boolean hasStartTimeIdClause = false;
		boolean hasEndTimeIdClause = false;

		for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
			ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
			int colSize = rowInfo.size();
			for (int j=0;j<colSize;j++) {
				DimConfigInfo dimConfig = rowInfo.get(j);
				if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
					continue;
				boolean exactSearch = dimConfig.m_searchExact;
				boolean is123 = dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
				boolean isTime = "20506".equals(dimConfig.m_dimCalc.m_dimInfo.m_subtype);
				String table = dimConfig.m_dimCalc.m_dimInfo.m_colMap.table;

				boolean isStartDateId = PageHeader.isStartDateId(dimConfig.m_dimCalc.m_dimInfo.m_id);
				if (isStartDateId) { //will be handled separately
					hasStartTimeIdClause = true;
					continue;
				}
				boolean isEndDateId = PageHeader.isEndDateId(dimConfig.m_dimCalc.m_dimInfo.m_id);
				if (isEndDateId) {//will be handled separately
					hasEndTimeIdClause = true;
					continue;
				}

				if (is123 || !(isStartDateId || isEndDateId || "vehicle".equals(table) || baseTable.equals(table))) {
					continue;
				}

				//				boolean isDummy = false;
				int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
				String tempVarName = is123 ? "pv123" : topPageContext+paramId;
				String tempVal = _session.getAttribute(tempVarName);
				seenVehicleStatus = dimConfig.m_dimCalc.m_dimInfo.m_id == 9008 && tempVal != null && !"-1000".equals(tempVal) && tempVal.length() > 0;
				String end = "";
				if ((tempVal != null && !"".equals(tempVal) && !"-1000".equals(tempVal)) || dimConfig.m_numeric_filter){
					String encl ="";
					int type = dimConfig.m_dimCalc.m_dimInfo.m_type;
					String tName = dimConfig.m_dimCalc.m_dimInfo.m_colMap.table;
					String cName = dimConfig.m_dimCalc.m_dimInfo.m_colMap.column;
					if (cName.equals("attribute_id") || cName.equals("rule_id"))
						continue;
					if (addWhr)
						whrStr.append(" and ");
					if (dimConfig.m_numeric_filter) {
						String tempVarNameOperator = tempVarName + "_operator";
						String tempVarNameOperandFirst = tempVarName + "_operand_first";
						String tempVarNameOperandSecond = tempVarName + "_operand_second";

						String paramValOperator = _session.getAttribute(tempVarNameOperator);
						String paramValOperandFirst = _session.getAttribute(tempVarNameOperandFirst);
						String paramValOperandSecond = _session.getAttribute(tempVarNameOperandSecond);

						StringBuilder sb = new StringBuilder();
						boolean doAppend = false;
						if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.useColumnOnlyForName)
							sb.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
						else
							sb.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table + "." + dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
						if(paramValOperator != null && paramValOperator.length() > 0 && tempVarNameOperandFirst != null && tempVarNameOperandFirst.length()
								> 0){
							int operator = Misc.getParamAsInt(paramValOperator);
							//int operandFirst = Misc.getParamAsInt(paramValOperandFirst);
							switch(operator){
							case 1:
								sb.append(" > ").append(paramValOperandFirst);
								doAppend = true;
								break;
							case 2:
								sb.append(" < ").append(paramValOperandFirst);
								doAppend = true;
								break;
							case 3:
								sb.append(" = ").append(paramValOperandFirst);
								doAppend = true;
								break;
							case 4:
								sb.append(" >= ").append(paramValOperandFirst);
								doAppend = true;
								break;
							case 5:
								sb.append(" <= ").append(paramValOperandFirst);
								doAppend = true;
								break;
							case 6:
								sb.append(" <> ").append(paramValOperandFirst);
								doAppend = true;
								break;
							case 7:
								if(paramValOperandSecond != null && paramValOperandSecond.length() > 0){
									sb.append(" BETWEEN ").append(paramValOperandFirst).append(" and ").append(paramValOperandSecond);
									doAppend = true;
									break;
								}
							default:
								if(whrStr.length() > 5 && " and ".equalsIgnoreCase(whrStr.substring(whrStr.length() - 5, whrStr.length())))
									whrStr.setLength(whrStr.length()-5);
							}
							if (doAppend){
								whrStr.append(" ( ").append(sb).append(" ) ");
								addWhr = true;
							}
						}
						continue;
					}

					// to handle comma separated multi text search
					if(dimConfig.m_dimCalc.m_dimInfo.m_id == 9002) {
						boolean multiValuedStringToSearch = false;
						if(tempVal.contains(","))
							multiValuedStringToSearch = true;
						String [] tempValArray = tempVal.split(",");
						if(multiValuedStringToSearch && tempValArray.length > 0){
							whrStr.append(" ( ");
							for (int k = 0; k < tempValArray.length; k++) {
								if(k != 0)
									whrStr.append(" or ");
								if(type == STRING_TYPE){
									whrStr.append(" ( ");
									if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.useColumnOnlyForName)
										whrStr.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
									else
										whrStr.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table + "." + dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
									if (exactSearch)
										whrStr.append(" like '");
									else
										whrStr.append(" like '%");
									encl = exactSearch ? "'" : "%'";
									end = " ";
									whrStr.append(tempValArray[k] + encl);
									whrStr.append(" ) ");
								}
							}
							whrStr.append(" ) ");
							addWhr = true;
							continue;
						}
					}
					addWhr = true;
					if(type != DATE_TYPE) {
						if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.useColumnOnlyForName)
							whrStr.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
						else
							whrStr.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table + "." + dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
					}
					switch(type){
					case LOV_TYPE : whrStr.append(" in (");
					encl = "";
					end = ") ";
					break;
					case STRING_TYPE :
						if("null".equalsIgnoreCase(tempVal)){
							whrStr.append(" is ");
							encl = "";
							end = " ";
						}
						else {
							if (exactSearch)
								whrStr.append(" like '");
							else
								whrStr.append(" like '%");
							encl = exactSearch ? "'" : "%'";
							end = " ";
						}
						break;
					case NUMBER_TYPE : whrStr.append(" = ");
					encl = "";
					end = "";
					break;
					case LOV_NO_VAL_TYPE : whrStr.append(" in (");
					end = ") ";
					encl = "";
					break;
					case INTEGER_TYPE : whrStr.append(" = ");
					encl = "";
					end = " ";
					break;
					case DATE_TYPE :								

						if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.useColumnOnlyForName)
							whrStr.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
						else
							whrStr.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table + "." + dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);

						if(dimConfig.m_forDateApplyGreater)
							whrStr.append(" >= '");
						else
							whrStr.append(" <= '");
						encl = "'";
						end = "";
						break;
					default :
						if (exactSearch)
							whrStr.append(" like '");
						else
							whrStr.append(" like '%");
					encl = exactSearch ? "'" : "%'";
					end = ") ";
					}

					if(type != DATE_TYPE)
						whrStr.append(tempVal + encl);
					else{
						try {
							Date dt = null;
							if (isTime) {
								try {
									dt = sdfTime.parse(tempVal);
								}
								catch (ParseException e2) {
									dt = sdf.parse(tempVal);
								}
							}
							else {
								try {
									dt = sdf.parse(tempVal);
								}
								catch (ParseException e2) {
									dt = sdfTime.parse(tempVal);
								}
							}								
							whrStr.append(indepDateFormat.format(dt) + encl);								
						} 
						catch (ParseException e) {
							e.printStackTrace();
							whrStr.append(" null " + encl);
							//eat it throw e;
						}
					}
				}//has value specified
				if (addWhr)
					whrStr.append(end);
			}//for each col
		}//for each row
		boolean doinglgdLike = "logged_data".equals(baseTable) || "current_data".equals(baseTable);
		//we want overlapping
		if (!doJustBefore) {
			if (hasStartTimeIdClause && !hasEndTimeIdClause) {
				if (addWhr)
					whrStr.append(" and ");
				if (doinglgdLike) {
					whrStr.append(" gps_record_time >= '@user_start' ");
				}
				else {//doing engine_events
					whrStr.append(" (engine_events.event_stop_time is null or engine_events.event_stop_time >= '@user_start') ");
				}
				addWhr = true;
			}
			else if (!hasStartTimeIdClause && hasEndTimeIdClause) {
				if (addWhr)
					whrStr.append(" and ");
				if (doinglgdLike) {
					whrStr.append(" gps_record_time <= '@user_end' ");
				}
				else {
					whrStr.append(" (engine_events.event_start_time <= '@user_end') ");
				}
				addWhr = true;
			}
			else if (hasStartTimeIdClause && hasEndTimeIdClause) {
				if (addWhr)
					whrStr.append(" and ");
				if (doinglgdLike) {
					whrStr.append(" gps_record_time between '@user_start' and '@user_end' ");
				}
				else {
					whrStr.append(" ((engine_events.event_stop_time is null and engine_events.event_start_time <= '@user_end') ")
					.append("or (engine_events.event_stop_time >= '@user_start' and engine_events.event_start_time <= '@user_end')) ");
				}
				addWhr = true;
			}
		}
		else {
			//we want the time just before
			if (hasStartTimeIdClause) {
				if (addWhr)
					whrStr.append(" and ");
				if (doinglgdLike) {
					whrStr.append(" gps_record_time < '@user_start' ");
				}
				else {//doing engine_events
					whrStr.append(" (engine_events.event_stop_time < '@user_start') ");
				}
				addWhr = true;
			}
			else {
				return null;
			}		
		}
		if (!seenVehicleStatus) {
			if (!addWhr)
				whrStr.append(" vehicle.status in (1,2) " );
			else
				whrStr.append(" and vehicle.status in (1,2) ");
			addWhr = true;
		}
		addWhr = addObjectIdPassedInWhr(_session, driverTableObjectInfo, whrStr, null, addWhr);
		whrCl = whrStr.toString();

		return whrCl;
	}
	private void replaceStr(StringBuilder sb,String from,String to){
		String query = sb.toString();
		sb.replace(0, sb.length(), "");
		sb.append(query.replaceAll(from, to));
	}
	public static String helperGetAggregateOp(DimConfigInfo dc, SearchBoxHelper searchBoxHelper, SessionManager session) {
		String aggregateOp = null;
		if (dc.m_aggregate) {//copied .. ProcessForShowResult
			String aggParamName = searchBoxHelper == null ? "p20053": searchBoxHelper.m_topPageContext+"20053";
			int aggDesired =Misc.getParamAsInt(session.getParameter(aggParamName));
			DimInfo aggDim = DimInfo.getDimInfo(20053);
			if (aggDim != null) {
				DimInfo.ValInfo valInfo = aggDim.getValInfo(aggDesired);
				if (valInfo != null) {
					aggregateOp = valInfo.getOtherProperty("op_text");
				}
			}
			if (dc.m_default != null && !"".equals(dc.m_default))
				aggregateOp = dc.m_default;
			if (aggregateOp == null || aggregateOp.length() == 0)
				aggregateOp = "sum";
		}
		return aggregateOp;
	}
	
	public void preProcessForDataToShow(FrontPageInfo fpi, ArrayList<DimConfigInfo> fpiList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SessionManager session, SearchBoxHelper searchBoxHelper) throws Exception {
		DimConfigInfo firstColumnInfo = fpiList.get(0);
		DimConfigInfo secondColumnInfo = fpiList.get(1);
		boolean done = false;
		String topPageContext = searchBoxHelper.m_topPageContext;
			for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
				ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
				int colSize = rowInfo.size();
				for (int j=0;j<colSize;j++) {
					DimConfigInfo dimConfig = rowInfo.get(j);
					if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
						continue;
					DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
					if(dimInfo.m_subsetOf == 20052){
						int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
						String tempVarName =  topPageContext+paramId;
						String tempVal = session.getParameter(tempVarName);
						if(tempVal == null || "".equals(tempVal)){
							break;
						}
						String[] idArr = tempVal.split(",");
						DimInfo dimLov = DimInfo.getDimInfo(dimConfig.m_dimCalc.m_dimInfo.m_id);
						ArrayList valList = dimLov.getValList();
					     for (int k=0,count=valList == null ? 0 : valList.size();k<count;k++) {
					        
					        DimInfo.ValInfo valInfo = (DimInfo.ValInfo) valList.get(k);
					        int id = valInfo.m_id;
					        String nameStr = null;
					        nameStr = valInfo.m_name;
					        dataListToShow.put(id, nameStr);
					     }
						for (int k = 0; k < idArr.length; k++) {
							ValInfo vinfo = dimInfo.getValInfo(Misc.getParamAsInt(idArr[k]));
							if (vinfo != null) {
								done = true;
								dataListToShow.remove(vinfo.m_id);
								dataToShow.put(vinfo.m_id, vinfo.m_name);
							}
						}
						
						break;
					}
					if(done)
						break;
				}
			}
		}
	
	public String getRowLink(String link,Pair<String,Integer> rowLinkHelper, ResultInfo rs) throws Exception{
		if(link != null && rowLinkHelper.first != null){
			boolean check = link.indexOf("?") > 0;
			if(!check)
				link = link + "?";
			link = link + "&" + rowLinkHelper.first + "=" + (rs != null ? rs.getVal(rowLinkHelper.second) : "");
		}
		return link;
	}
	
	public void printMultiFrontPage(Connection conn, String pgContext, SessionManager session, InputTemplate perRowTemplate,  int privIdForOrg, JspWriter out, SearchBoxHelper searchBoxHelper) throws Exception {
		StringBuilder sb = new StringBuilder();
		String frontPageName = session.getParameter("front_page");
		FrontPageInfo fpi = perRowTemplate.getCalculatedFrontPageInfo();
		boolean hasNested = hasNested(fpi.m_frontInfoList);
		Table parentTable = null;
		int reportType = Misc.HTML; //make it parameterizable
		
		boolean doingTransitTable = "tr_transit_report.xml".equalsIgnoreCase(frontPageName);
		boolean doPlainTable = perRowTemplate != null;
		Table table = Table.createTable();
		boolean isCached =  true;
		ProcessShowResult processDataToShowResult = null;
		
		ResultInfo.FormatHelper formatHelper = ResultInfo.getFormatHelper(fpi.m_frontInfoList, session, searchBoxHelper);
		
		try{
			if (isCached){
				boolean doCombinedApproach  = true;
				boolean doDimsLoadFromDBOnly = CopyOfGeneralizedQueryBuilder.checkIfLoadFromDBIfCached(fpi.m_frontInfoList);

				long ts1 = System.currentTimeMillis();
				HashMap<String, Value> loadFromDBVals = null;
				if (doDimsLoadFromDBOnly) {
					int rollupAtJava = fpi.m_doRollupAtJava ? 1 : 0;
					if (processDataToShowResult != null && processDataToShowResult.needsManipulation())
						 rollupAtJava = 1; //only implemented for ma
					 else if (!Misc.g_doRollupAtJava)
						 rollupAtJava = 0;
					ArrayList<Pair<Integer,Long>> vehicleList = null;
					int portNodeId = getPortNodeId(session, fpi.m_frontSearchCriteria, searchBoxHelper);
					vehicleList = VehicleSetup.getVehicleList(conn, session.getCache(), portNodeId);
					
					QueryParts qp = buildQueryParts(fpi.m_frontInfoList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session,
						searchBoxHelper, formatHelper, fpi.m_colIndexLookup, getDriverObjectFromName(fpi.m_driverObjectLocTracker), fpi.m_orderIds, processDataToShowResult, false, Misc.getUndefInt(), rollupAtJava, doDimsLoadFromDBOnly, fpi.m_orgTimingBased);
					String query =  buildQuery(session, qp, vehicleList, doDimsLoadFromDBOnly);
					ts1 = System.currentTimeMillis();
					System.out.println("[CCACHE] Loading Values from DB:"+Thread.currentThread().getId()+ "FP:"+frontPageName);
					loadFromDBVals = CopyOfGeneralizedQueryBuilder.getLoadFromDBIfDoingCached(conn, session, portNodeId, fpi.m_frontInfoList, query);
					long ts2 = System.currentTimeMillis();
					System.out.println("[CCACHE] Loaded Values from DB:"+Thread.currentThread().getId()+ "FP:"+frontPageName+" time(ms):"+(ts2-ts1));
					ts1 = ts2;
				}
				System.out.println("[CCACHE] Begin DB:"+Thread.currentThread().getId()+ "FP:"+frontPageName);
				CurrCacheGrouper groupHelper = new CurrCacheGrouper(fpi.m_frontInfoList, doCombinedApproach ? fpi.m_frontSearchCriteria : null, null, loadFromDBVals);
				groupHelper.addEmptyRow();
				ResultInfo resultInfo = new ResultInfo(fpi.m_frontInfoList, fpi.m_colIndexLookup, null, session, searchBoxHelper,null, fpi.m_colIndexUsingExpr, formatHelper, null, null,groupHelper, null, processDataToShowResult, null, false);
				printTable(resultInfo, fpi, searchBoxHelper, session,table, null, processDataToShowResult, perRowTemplate, null, null);
				long ts2 = System.currentTimeMillis();
				System.out.println("[CCACHE] End DB:"+Thread.currentThread().getId()+ "FP:"+frontPageName+" time(ms):"+(ts2-ts1));
			}
			table.closeTable();
		} catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		int tempReportType = Misc.getParamAsInt(session.getParameter("report_type"),Misc.UNDEF_VALUE);
//		if(tempReportType == Misc.JSON && out != null){
//			JsonStreamer.printMultiJason(table, sb, session, out);
////			JSONArray nestedJson = JasonGenerator.printMultiJason(table, sb, session);
////			JasonGenerator.getJASON(sb.toString());
////			sb.append(nestedJson.toString());
//			out.print(sb);
//		}
		if (tempReportType == Misc.JSON && out != null) {
			JsonStreamer.printMultiJason(table, sb, session, out); 
		} else if (out != null && tempReportType == Misc.CHART) {
			try {
				ChartUtil.loadVirtualShovels(session);
				int portNodeId = getPortNodeId(session, fpi.m_frontSearchCriteria, searchBoxHelper);
				String dataSource ="";
				if(hasNested){
					ChartJasonGenerator.getNestedJason(fpi,table,session,conn,out,portNodeId);
				}else{
					dataSource = ChartJasonGenerator.getJason(fpi,table,session,searchBoxHelper,out,portNodeId);
					// out.print(dataSource);
					fpi.chartInfo.setDataSource(dataSource);
					FusionCharts fChart = new FusionCharts(fpi.chartInfo);
					out.print(fChart.render());
				}
			
			} catch (ChartException e) {
				out.print(e.toString());
			}
		} else if (table.isNullStreamingGenerator()) {
			if (out != null && reportType == Misc.HTML) {
				HtmlGenerator.printHtmlTable(table, sb, session, doPlainTable);
				out.println(sb);
				sb.setLength(0);
			}
		}else if (out != null && reportType == Misc.HTML) {
		}
	}
	
	public static void printMultiFrontPageOld(Connection conn, String pgContext, SessionManager session, InputTemplate template,  int privIdForOrg, JspWriter out) throws Exception {
		User user = session.getUser();
		int portNodeForCustom =  Misc.getUserTrackControlOrg(session); 
		ArrayList<ArrayList<DimConfigInfo>> rows = template.getRows();
		StringBuilder sb = new StringBuilder();
		sb.append("<table cellspacing='3' cellpadding='3' border='0'>");
		out.println(sb);
		sb.setLength(0);
		for (int i=0,is=rows == null ? 0 : rows.size();i<is;i++) {
			//TODO redo with TR etc and make it downloadable somehow
			
			ArrayList<DimConfigInfo> row = rows.get(i);
			for (int j=0,js=row == null ? 0 : row.size(); j<js; j++) {
				DimConfigInfo dci = row.get(j);
				String frontPageName = dci.m_xml;
				FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(conn , user.getUserId(),portNodeForCustom, pgContext, frontPageName, 0, 0);
				if (fPageInfo == null)
					continue;
				sb.append("<tr><td colspan='2'>&nbsp;</td>");
				sb.append("<tr>");
				sb.append("<td class='tmSectionHeader1'>");
				sb.append(dci.m_name);
				sb.append("</td>");
				sb.append("<td class='tn' style=\"margin-left:350px\">");
				
				//sb.append("&nbsp;");
				sb.append("<a href=\"javascript:popCustomizeLink('").append(pgContext).append("' , '").append(frontPageName).append("', ").append(Misc.getUndefInt()).append(")\">Customize </a>");
				sb.append("</td>");
				sb.append("</tr>");
				out.println(sb);
				sb.setLength(0);
				sb.append("<tr>");
				sb.append("<td class='tn' colspan='2'>");
				out.println(sb);
				sb.setLength(0);
				session.rememberSessionVars();
				com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(session, privIdForOrg, pgContext, fPageInfo.m_frontSearchCriteria, null);
				CopyOfGeneralizedQueryBuilder qb = new CopyOfGeneralizedQueryBuilder();
				qb.printPage(conn , fPageInfo, session, searchBoxHelper, out, null, null, null);
				session.setToRememberedVars();
				//print cell specific table;
				sb.append("</td>");
				sb.append("</tr>");
				out.println(sb);
				sb.setLength(0);
			}//for each col
		}//for each row
		sb.append("</table>");
		out.println(sb);
		sb.setLength(0);		
	}
	
	//Current cache 
 //RFID struff
public static String grf_target_plan_monthly_internal = "select seller_id, mode_id, transporter_id, sum(target)/day(last_day('@end_period')) daily_target, sum(target) monthly_target "+
	" from plans join port_nodes leaf on (plans.status=1 and leaf.id = plans.port_node_id) join port_nodes anc on (anc.id in (@pv123) and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
	" join plan_items on (plans.id = plan_items.plan_id) "+
	" where plans.plan_freq = 2 and plans.plan_period_start = '@mon_start' "+
	" group by seller_id, mode_id, transporter_id "
	;
public static String grf_target_plan_daily_internal = "select seller_id, mode_id, transporter_id, sum(target) daily_target "+
" from plans join port_nodes leaf on (plans.status=1 and leaf.id = plans.port_node_id) join port_nodes anc on (anc.id in (@pv123) and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
" join plan_items on (plans.id = plan_items.plan_id) "+
" where plans.plan_freq = 1 and plans.plan_period_start = '@day_start' "+
" group by seller_id, mode_id, transporter_id "
;
public static String grf_desp_mtd_internal = "select do_rr_details.seller seller_id, do_rr_details.type mode_id, tp_record.transporter_id, sum(load_gross-load_tare) desp_tot "+
" from tp_record join do_rr_details on (tp_record.do_id = do_rr_details.id) "+
" where @tp_record.latest_load_gate_in_out < '@day_start' and @tp_record.latest_load_gate_in_out >= '@mon_start' "+
" group by do_rr_details.seller, do_rr_details.type, tp_record.transporter_id "
;
public static String grf_monthly_daily_target_plan = 
"(select mt.seller_id, mt.mode_id, mt.transporter_id, mt.monthly_target, mt.daily_target, (case when md.daily_target is not null then md.daily_target else (mt.monthly_target-mtd_desp.desp_tot)/(day(last_day('@mon_end'))-day('@day_end')+1) end) daily_plan "+
 " from "+
	"("+grf_target_plan_monthly_internal+") mt left outer join "+
	"("+grf_target_plan_daily_internal+") md on (mt.seller_id = md.seller_id and mt.mode_id = md.mode_id and mt.transporter_id = md.transporter_id) left outer join "+
	"("+grf_desp_mtd_internal+") mtd_desp on (mt.seller_id = mtd_desp.seller_id and mt.mode_id = mtd_desp.mode_id and mt.transporter_id = mtd_desp.transporter_id) "+
	") grf_monthly_daily_target_plan "
	;
public static String rfid_handheld_log_unique =
	" select device_id, record_id, record_time,vehicle_id,write_status, epc_id, min(id) from rfid_handheld_log  where @rfid_handheld_log.record_time between '@user_start' and '@user_end' group by "+ 
	" device_id, record_id, record_time,vehicle_id,write_status, epc_id "
	;
public static String rfid_handheld_log_unique_narrow =
	" select device_id, record_id, record_time, min(id) rfid from rfid_handheld_log where @rfid_handheld_log.record_time between '@user_start' and '@user_end' group by "+ 
	" device_id, record_id, record_time "
	;
public static String swm_bin_by_hier_hourly = 
	" (select binlist.id, binlist.name, binlist.totcnt, binlist.label, binlist.cnt, triplist.tc, triplist.tottc, (case when binlist.totcnt = 0 then null else triplist.tottc/binlist.totcnt end)*100 totratio, (case when triplist.tc is null and binlist.cnt > 0 then 0 when binlist.cnt = 0 and triplist.tc > 0 then 1 when binlist.cnt=0 and triplist.tc=0 then null else triplist.tc/binlist.cnt end)*100 ratio"+
	" from  "+
	" ( "+
	" select demo_hierarchy.id, demo_hierarchy.name, hour_table.label "+
	" , sum(case when greatest( "+
	" case when pickup_freq_by_hh_1 is null then -1 else pickup_freq_by_hh_1 end "+
	" ,case when pickup_freq_by_hh_2 is null then -1 else pickup_freq_by_hh_2 end "+
	" ,case when pickup_freq_by_hh_3 is null then -1 else pickup_freq_by_hh_3 end "+
	" ,case when pickup_freq_by_hh_4 is null then -1 else pickup_freq_by_hh_4 end "+
	" ) * 15 "+
	"   <= timestampdiff(minute, '@start_period', hour_table.end_time) "+
	"      then 1 else 0 end) cnt "+
	", count(*) totcnt "+
	" from  "+
	" hour_table cross join "+
	" swm_bins join port_nodes leaf on (leaf.id = swm_bins.port_node_id) join port_nodes anc on (anc.id in (@pv123) and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
    " left outer join swm_bin_details on (swm_bins.id = swm_bin_details.swm_bin_id) "+
	" left outer join swm_bin_hierarchy on (swm_bin_hierarchy.swm_bin_id = swm_bins.id) "+
	" left outer join demo_hierarchy on (demo_hierarchy.id = swm_bin_hierarchy.val) "+
	" where hour_table.start_time between '@start_period' and '@end_period' "+
	 " and swm_bin_hierarchy.level=@hier_level "+
	" group by demo_hierarchy.id, demo_hierarchy.name, hour_table.label "+
	" ) binlist "+
	" left outer join "+
	" ( "+
	" select demo_hierarchy.id, demo_hierarchy.name, hour_table.label "+
	" , sum(case when trip_info_otherLU.gate_in between '@start_period' and hour_table.end_time then 1 else 0 end) tc "+
	", count(*) tottc "+
	" from hour_table,  vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id)  left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc on (anc.id in (@pv123) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)  or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on (vi.vehicle_id = vehicle.id) " +
	" left outer join trip_info_otherLU on (trip_info_otherLU.vehicle_id = vehicle.id) "+
	" left outer join op_station on (trip_info_otherLU.opstation_id = op_station.id) "+
	" left outer join swm_bins on (swm_bins.id = op_station.ref_mines_id) "+
	" left outer join swm_bin_hierarchy on (swm_bin_hierarchy.swm_bin_id = swm_bins.id) "+
	" left outer join demo_hierarchy on (demo_hierarchy.id = swm_bin_hierarchy.val) "+
	" where hour_table.start_time between '@start_period' and '@end_period' "+
	" and trip_info_otherLU.gate_in between '@start_period' and '@end_period' "+
	" and swm_bin_hierarchy.level=@hier_level "+
	" group by  demo_hierarchy.id, demo_hierarchy.name, hour_table.label "+
	" ) triplist on (binlist.id = triplist.id and binlist.label = triplist.label) "+
	" ) g_bin_stat_by_hier "
	;

public static String swm_bin_status_info = 
	" select swm_bins.id swm_bin_id, swm_bins.name, landmarks.lowerX, landmarks.lowerY, ind_data.mxgin, vehicle.name vehicle_name "+
	" , swm_bin_get_clean_status(pickup_freq_by_hh_1*15,pickup_freq_by_hh_2*15, pickup_freq_by_hh_3*15, pickup_freq_by_hh_4*15, totPicks, mxgin, ind_data.start_time, (case when ind_data.user_end < ind_data.end_time then ind_data.user_end else ind_data.end_time end)) bin_status "+
	" , ind_data.item_id, ind_data.period_label "+
	", swm_bin_get_lastCleanDueAt(pickup_freq_by_hh_1*15,pickup_freq_by_hh_2*15, pickup_freq_by_hh_3*15, pickup_freq_by_hh_4*15, ind_data.start_time, (case when ind_data.user_end < ind_data.end_time then ind_data.user_end else ind_data.end_time end)) last_clean_due_at "+
	", swm_bin_get_nextCleanDueAt(pickup_freq_by_hh_1*15,pickup_freq_by_hh_2*15, pickup_freq_by_hh_3*15, pickup_freq_by_hh_4*15, ind_data.start_time, (case when ind_data.user_end < ind_data.end_time then ind_data.user_end else ind_data.end_time end)) next_clean_due_at "+
	", ind_data.start_time "+
	" from "+
	" swm_bins "+ 
	" join port_nodes leaf on (leaf.id = swm_bins.port_node_id) join port_nodes anc on (anc.id in (@pv123) and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+ 
	" left outer join swm_bin_details on (swm_bins.id = swm_bin_details.swm_bin_id) "+ 
	" left outer join landmarks on (landmarks.id = swm_bins.landmark_id) "+
	" left outer join  "+
	" (select @period.label period_label, swm_bins.id swm_bin_id, op_station.id opid, @period.id item_id, @period.start_time, @period.end_time, '@user_end' user_end "+
	" ,max(trip_info_otherLU.gate_in) mxgin "+
	" ,count(*) totPicks "+
	" from @period cross join "+
	" swm_bins  "+
	" join port_nodes leaf on (leaf.id = swm_bins.port_node_id) join port_nodes anc on (anc.id in (@pv123) and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+ 
	" left outer join swm_bin_details on (swm_bins.id = swm_bin_details.swm_bin_id) "+ 
	" left outer join op_station on (op_station.ref_mines_id = swm_bins.id) "+
	" left outer join trip_info_otherLU on (trip_info_otherLU.opstation_id = op_station.id and trip_info_otherLU.gate_in between @period.start_time and @period.end_time and trip_info_otherLU.gate_in < '@user_end') "+
	" where "+
	" @period.start_time between '@start_period' and '@end_period' "+ 
	" and swm_bins.status=1 "+
	" group by "+
	" @period.label, swm_bins.id, op_station.id, @period.id, @period.start_time, @period.end_time "+
	" ) ind_data on (ind_data.swm_bin_id = swm_bins.id) "+
	" left outer join trip_info_otherLU on (trip_info_otherLU.opstation_id = ind_data.opid and trip_info_otherLU.gate_in = ind_data.mxgin) "+
	" left outer join vehicle on (vehicle.id = trip_info_otherLU.vehicle_id) "+
	" order by ind_data.start_time, swm_bins.id "

	;

public static String swm_bin_status_info_internal = "("+swm_bin_status_info +") swm_bin_status_info";
public static String helpGetCurrSwmBinStatusQ(int portNodeId) {
	String q = swm_bin_status_info;
	q = q.replaceAll("@period", "day_table");
	q = q.replaceAll("@pv123", Integer.toString(portNodeId));
	java.util.Date dt = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	q = q.replaceAll("@user_end", sdf.format(dt));
	dt.setHours(0);
	dt.setMinutes(0);
	dt.setSeconds(0);
	q = q.replaceAll("@start_period", sdf.format(dt));
	dt.setHours(23);
	dt.setMinutes(59);
	dt.setSeconds(59);	
	q = q.replaceAll("@end_period", sdf.format(dt));
	return q;
}

}