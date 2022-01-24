package com.ipssi.input;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.jsp.JspWriter;

import org.codehaus.jettison.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.ColumnMappingHelper;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.DriverExtendedInfo;
import com.ipssi.gen.utils.FmtI;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.PageHeader;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.PrivInfo;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.User;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.VehicleExtendedInfo;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.miningOpt.NewMU;
import com.ipssi.reporting.trip.CssClassDefinition;
import com.ipssi.reporting.trip.ExcelGenerator;
import com.ipssi.reporting.trip.ExcelGenerator_poi;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder;
import com.ipssi.reporting.trip.HtmlGenerator;
import com.ipssi.reporting.trip.JasonGenerator;
import com.ipssi.reporting.trip.JsonStreamer;
import com.ipssi.reporting.trip.PdfGenerator;
import com.ipssi.reporting.trip.ResultInfo;
import com.ipssi.reporting.trip.TD;
import com.ipssi.reporting.trip.TR;
import com.ipssi.reporting.trip.Table;
import com.ipssi.reporting.trip.XmlGenerator;
import com.ipssi.reporting.trip.ResultInfo.FormatHelper;
import com.ipssi.rfid.beans.DOUpdInfo;
import com.ipssi.secl.DOUpdateHelper;
import com.ipssi.secl.TPRChanges;
import com.ipssi.tracker.drivers.DVUtils;
import com.ipssi.workflow.Constant;
import com.ipssi.workflow.WorkflowDef;
import com.ipssi.workflow.WorkflowHelper;
import com.ipssi.workflow.WorkflowHelper.TableObjectInfo;


public class InputTemplate {
	public static String G_BACKUP_BBLOB_FILE = "/backup_file_data";
	private static int G_VEHICLEOBJECT_ID = 0;
	private static int G_DRIVEROBJECT_ID = 1;
	private static int G_TPTR_ID_COAL = 2;
	private static int G_DORR_ID = 3;
	private static int G_OP_ISSUES_ID = 4;
	private static int G_VIOLATION_ID = 5;
	private static int G_PLAN_ID = 6;
	private static int G_QC_ID = 7;
	private static int G_TPTR_ID_ASH= 8;
	private static int G_TPTR_ID_STONE = 9;
	private static int G_TAT_PROCESS = 10;
	private static int G_TAT_OP_TO_OP = 11;
	private static int G_EXCLUDE_REGIONS = 12;
	private static int G_CRITICALITY_DEF = 13;
	//above must be sequential
	
	public static class QueryHelper {
		public String m_view = null;
		public String m_updateBaseNew = null; //
		public String m_updateExtendedNew = null;
		public String m_updateBase = null;
		public String m_updateExtended = null;		
		public ArrayList<DimInfo> m_dimUpdateBaseNew = null;
		public ArrayList<DimInfo> m_dimUpdateExtendedNew = null;
		public ArrayList<DimInfo> m_dimUpdateBase = null;
		public ArrayList<DimInfo> m_dimUpdateExtended = null;
	}
	
	
	
	private ArrayList<Integer> workflowsUsed = null;
	private ArrayList<ArrayList<DimConfigInfo>> rows = new ArrayList<ArrayList<DimConfigInfo>>();
	private ArrayList<ArrayList<DimConfigInfo>> commonRows = new ArrayList<ArrayList<DimConfigInfo>>();
	private ArrayList<ArrayList<DimConfigInfo>> searchCriteria = new ArrayList<ArrayList<DimConfigInfo>>();
	private void copyHeaderExt(InputTemplate rhs) {
		copyHeader(rhs);
		this.commonRows = (ArrayList<ArrayList<DimConfigInfo>>)rhs.commonRows.clone();
		this.searchCriteria = (ArrayList<ArrayList<DimConfigInfo>>)rhs.searchCriteria.clone();
	}
	public String m_help = null;
	private int objectType = 0; //0 => vehicle, 1 => driver
	public String m_objectIdParamLabel = null;
	public String m_objectIdColName = null;	
    private FrontPageInfo calculatedFrontPageInfo = null; //will be populated if topElem of InputElement has create_fp
	public String m_customField1 = null;
	public String m_customField2 = null;
	public String m_customField3 = null;
	public int m_columnOutput = 1;
	public boolean m_doAsMultiRow = false;
	public int m_createSpec = 0; // 0 -> to add only if no undef. else one adder
	public String m_createPriv = null;
	public int m_searchSpecIfExists = 0;//0 dont show, 1 show 
	public boolean m_showSearchCriteriaInInput = false;
	public String javaScriptFile = null;
	public String onSubmitPreHandler = null;
	public int customPostHandler = Misc.getUndefInt();
	public String insteadPostActionHandler = null;
	private void copyHeader(InputTemplate rhs) {
		this.objectType = rhs.objectType;
		this.m_customField1 = rhs.m_customField1;
		this.m_customField2 = rhs.m_customField2;
		this.m_customField3 = rhs.m_customField3;
		this.m_columnOutput = rhs.m_columnOutput;
		this.m_objectIdColName = rhs.m_objectIdColName;
		this.m_objectIdParamLabel = rhs.m_objectIdParamLabel;
		this.m_help = rhs.m_help;
		this.calculatedFrontPageInfo = rhs.calculatedFrontPageInfo;
		this.m_doAsMultiRow = rhs.m_doAsMultiRow;
		this.m_createSpec = rhs.m_createSpec; // 0 -> to add only if no undef. else one adder
		this.m_createPriv = rhs.m_createPriv;
		this.m_searchSpecIfExists = rhs.m_searchSpecIfExists;//0 dont show, 1 show 
		this.m_showSearchCriteriaInInput = rhs.m_showSearchCriteriaInInput;
		this.javaScriptFile = rhs.javaScriptFile;
		this.onSubmitPreHandler = rhs.onSubmitPreHandler;
		this.customPostHandler =rhs.customPostHandler;
		this.insteadPostActionHandler = rhs.insteadPostActionHandler;
	}
	
	
	private static ConcurrentHashMap<String, Pair<Boolean, InputTemplate>> inputCache = new ConcurrentHashMap<String, Pair<Boolean,InputTemplate>>();
	   //above: key = id generated from getId, Value - if for that specific id, we have looked in db
	public static String hackGetMaterialCatAdjustedPageContext(SessionManager session, String pageContext) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if ("tr_tptr_template".equals(pageContext)) {
				String[] tprIds = session.request.getParameterValues("tpr_id"); 
				if (tprIds != null && tprIds.length > 0) {
					int tprId = Misc.getParamAsInt(tprIds[0]);
					ps = session.getConnection().prepareStatement("select material_cat from tp_record where tpr_id=?");
					ps.setInt(1, tprId);
					rs = ps.executeQuery();
					
					int matCode = rs.next() ? rs.getInt(1) : 0;
					pageContext = matCode == 1 ? "mpl_stone_trip" : matCode == 2 ? "mpl_flyash_trip" : matCode == 3 ? "mpl_other_trip" : pageContext;
					rs = Misc.closeRS(rs);
					ps = Misc.closePS(ps);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return pageContext;
	}
	public static DimConfigInfo getDimConfigInfoInList(ArrayList<ArrayList<DimConfigInfo>> theList, int dimId, String colName) {
		if (colName != null) {
			for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++) {
				ArrayList<DimConfigInfo> row = theList.get(i);
				for (int j=0,js=row == null ? 0 : row.size();j<js;j++) {
					DimConfigInfo dci = row.get(j);
					if (colName != null && colName.equals(dci.m_columnName))
						return dci;
				}
			}
		}
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++) {
			ArrayList<DimConfigInfo> row = theList.get(i);
			for (int j=0,js=row == null ? 0 : row.size();j<js;j++) {
				DimConfigInfo dci = row.get(j);
				if (dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_dimCalc.m_dimInfo.m_id == dimId)
					return dci;
			}
		}
		return null;
	}
	
	public ArrayList<ArrayList<DimConfigInfo>> getRows() {
		return rows;
	}
	public ArrayList<ArrayList<DimConfigInfo>> getSearchCriteria() {
		return searchCriteria;
	}
	
	public static InputTemplate getTemplate(Cache cache, Connection conn, String menuTag, int portNodeId, String fileName, SessionManager session) throws Exception {
		String lookupId = getId(menuTag, portNodeId, fileName);
		if (Misc.isUndef(portNodeId))
			return getAtPortNodeId(cache, conn, menuTag, portNodeId, fileName, session);
		for (MiscInner.PortInfo curr = cache.getPortInfo(portNodeId, conn); curr != null; curr = curr.m_parent) {
			InputTemplate retval = getAtPortNodeId(cache, conn, menuTag, curr.m_id, fileName, session);
			if (retval != null)
				return retval;
		}
		return getAtPortNodeId(cache, conn, menuTag, Misc.getUndefInt(), fileName, session);
	}
	
	public static void makeDirty(Cache cache, Connection conn, String menuTag, int portNodeId, String fileName) {
		if (menuTag == null)
			inputCache.clear();
		else {
			String lookupId = getId(menuTag, portNodeId, fileName);
			inputCache.remove(lookupId);
		}
	}
	
	public QueryHelper generateQuery(SessionManager session) {
		if (objectType == 1) {//driver
			String pgContext = Misc.getParamAsString(session.getParameter("page_context"));
			if("bsnl_drivers_printable".equals(pgContext))
				return helpGenerateQuery("driver_details", "driver_attendance_bsnl", "driver_id");
			else
				return helpGenerateQuery("driver_details", "driver_details_extended", "driver_id");
		}
		else if (objectType == 2) //TPTR
			return helpGenerateQuery("driver_details", "driver_details_extended", "driver_id");
		else //if (objectType == 0)
			return helpGenerateQuery("vehicle", "vehicle_extended", "vehicle_id");
	}
	public void printMultiRowColBlock(JspWriter out, Connection conn, Cache cache, ArrayList<Integer> objectIds, SessionManager session, MiscInner.PairBool needVehicleDriverWrite, QueryHelper queryHelper, boolean readOnly) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<table cellspacing='3' cellpadding='3'>");
		
		for (int i=0,is = objectIds.size();i<is;i++) {
			if (i%this.m_columnOutput == 0) {
				if (i != 0)
					sb.append("</tr>");
				sb.append("<tr>");
			}
		//	if (i%this.m_columnOutput == 1) {
		//		sb.append("<td>");// ("<td width='6'  class='sh'>&nbsp;</td>");
		//		sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		//		sb.append("</td>");
		//	}
			sb.append("<td>");
			printBlock(1,100, sb, objectIds.get(i), conn, cache, session, needVehicleDriverWrite, queryHelper, readOnly);
			sb.append("</td>");
			out.println(sb);
			sb.setLength(0);
		}
		if (objectIds.size() > 0) {
			for (int i=0,is = m_columnOutput-(objectIds.size()%m_columnOutput); i<is; i++) {
				sb.append("<td>&nbsp;</td>");
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		out.println(sb);
		sb.setLength(0);
	}
	
	public void printBlock(StringBuilder sb, int objectId, Connection conn, Cache cache, SessionManager session, MiscInner.PairBool needVehicleDriverWrite, QueryHelper queryHelper, boolean readOnly) throws Exception {
			printBlock(3,Misc.getUndefInt(), sb, objectId, conn, cache, session, needVehicleDriverWrite, queryHelper, readOnly);
	}
	public void printBlock(int cellpadding, int tablePercentWidth, StringBuilder sb, int objectId, Connection conn, Cache cache, SessionManager session, MiscInner.PairBool needVehicleDriverWrite, QueryHelper queryHelper, boolean readOnly) throws Exception {
		//Dimconfig marked as hidden will be printed in hidden - but those determined to be hidden because of m_hideSpecialControl will not be printed
		//Dimconfig marked as read only will not have <input element>
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (!Misc.isUndef(objectId)) {
				ps = conn.prepareStatement(queryHelper.m_view);
				ps.setInt(1, objectId);
				rs = ps.executeQuery();
				if (!rs.next()) {
					rs.close();
					ps.close();
					rs = null;
					ps = null;
				}
			}
			boolean doingNew = Misc.isUndef(objectId);
			needVehicleDriverWrite.first = false;
			needVehicleDriverWrite.second = false;
			
			int maxColCount = -1;
			ArrayList<Integer> toSkipRowIndex = new ArrayList<Integer>();
			FormatHelper formatHelper = this.getFormatHelper(rows, session);
			for (int i=0,is=rows.size();i<is;i++) {
				ArrayList<DimConfigInfo> row = rows.get(i);
				int colCount = 0;
				boolean seenNonHid = false;
				for (int j=0,js = row.size();j<js;j++) {
					DimConfigInfo dimConfig = row.get(j);
					if (dimConfig.m_hidden)
						continue;
					int hideControl = dimConfig.m_hiddenSpecialControl;
					boolean toHide = doingNew ? hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_PRECREATE 
							:
							hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_POSTCREATE
						;
					if (toHide)
						continue;
					seenNonHid = true;
					colCount += 1;
					if (dimConfig.m_dataSpan < 1)
						colCount++;
					else if (dimConfig.m_dataSpan < 1000)
						colCount += dimConfig.m_dataSpan;
					else
						colCount++;
				}
				if (!seenNonHid)
					toSkipRowIndex.add(i);
				if (colCount > maxColCount)
					maxColCount = colCount;
			}
			//now start printing ..
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			StringBuilder hiddenVar = new StringBuilder();
			int colIndex = 0;
			sb.append("<input type='hidden' name='").append(objectType == 0 ? "vehicle_id" : "driver_id").append("' value='").append(objectId).append("'/>");
			sb.append("<table border='0' cellpadding='").append(cellpadding).append("' cellspacing='0'");
			if (!Misc.isUndef(tablePercentWidth)) {
				sb.append(" width='").append(tablePercentWidth).append("%' ");
			}
			ArrayList<MiscInner.Pair> colsForRowSpan = null;
			for (int i=0,is=rows.size();i<is;i++) {
				ArrayList<DimConfigInfo> row = rows.get(i);
				boolean toSkip = false;
				for (int j=0,js = toSkipRowIndex.size();j<js;j++) {
					if (toSkipRowIndex.get(j) == i) {
						toSkip= true;
						break;
					}
				}
				if (toSkip) {
					GeneralizedQueryBuilder.helperReduceColsForRowSpan(colsForRowSpan);
					colIndex += row.size();
					continue;
				}
				sb.append("<tr>");
				MiscInner.Pair labelDatacolsUsed = GeneralizedQueryBuilder.helperGetColsForRowSpans(colsForRowSpan);
				int colsUsed = labelDatacolsUsed.second+labelDatacolsUsed.first;
				for (int j=0,js = row.size();j<js;j++) {
					DimConfigInfo dimConfig = row.get(j);
					colIndex++;
					if (dimConfig.m_hidden)
						continue;
					int hideControl = dimConfig.m_hiddenSpecialControl;
					int readControl = dimConfig.m_readSpecialControl;
					boolean toHide = doingNew ? hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_PRECREATE 
																				:
																				hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_POSTCREATE
																			;
					if (toHide) {
						continue;
					}
					boolean toRead = doingNew ? readControl == DimConfigInfo.G_READHIDE_ALWAYS || readControl == DimConfigInfo.G_READHIDE_PRECREATE 
							:
							readControl == DimConfigInfo.G_READHIDE_ALWAYS || readControl == DimConfigInfo.G_READHIDE_POSTCREATE
						;
					if (dimConfig.m_readOnly)
						toRead = true;
					if (readOnly)
						toRead = true;
					
					String labelClass = dimConfig.m_labelStyleClass == null || dimConfig.m_labelStyleClass.length() == 0 ? "sh" : dimConfig.m_labelStyleClass;
					boolean mergeLabelAndVal = false;
					if (dimConfig.m_name == null || dimConfig.m_name.length() == 0) {
						if (dimConfig.m_disp != null && dimConfig.m_disp.length() != 0)
							mergeLabelAndVal = true;
					}
					int dataColspan = 1;
					if (!mergeLabelAndVal) {
						sb.append("<td valign=\"top\" class='").append(labelClass).append("' ");
						if (dimConfig.m_labelNowrap)
							sb.append("nowrap='1'");
						else if (dimConfig.m_labelWidth > 0)
							sb.append("width='").append(dimConfig.m_labelWidth).append("px'");
						if (dimConfig.m_rowSpan > 1)
							sb.append(" rowspan='").append(dimConfig.m_rowSpan).append("' ");
						sb.append(">");
						sb.append(dimConfig.m_name == null || dimConfig.m_name.length() == 0 ? "&nbsp;" : dimConfig.m_name);
						if ("sh".equals(labelClass) && dimConfig.m_name != null && !dimConfig.m_name.endsWith(":"))
							sb.append(":");
						sb.append("</td>\n");
						
						colsUsed += dataColspan;
					}
					dataColspan = dimConfig.m_dataSpan > 0 && dimConfig.m_dataSpan < 1000 ? 1 : dimConfig.m_dataSpan;
					if (mergeLabelAndVal) {
						dataColspan++;
					}
					if (j == js-1)
						dataColspan = maxColCount-colsUsed;
					
					sb.append("<td  valign=\"top\" ");
					if (dataColspan > 1)
						sb.append("colspan='").append(dataColspan).append("' ");
					colsUsed += dataColspan;
					if (dimConfig.m_nowrap)
						sb.append("nowrap='1'");
					if (dimConfig.m_rowSpan > 1)
						sb.append(" rowspan='").append(dimConfig.m_rowSpan).append("' ");
					String valStyle = dimConfig.m_valStyleClass == null || dimConfig.m_valStyleClass.length() == 0 ? "tn" : dimConfig.m_valStyleClass;
					sb.append(" class='").append(valStyle).append("'>");
					sb.append(hiddenVar);
					hiddenVar.setLength(0);
					if (dimConfig.m_rowSpan > 1) {
						colsForRowSpan = GeneralizedQueryBuilder.helperAddColsForRowSpan(colsForRowSpan, dimConfig.m_rowSpan, dimConfig.m_dataSpan);
					}
					String customPrintHelp = dimConfig.m_customBlock;
					DimInfo dimInfo = dimConfig.m_dimCalc == null ? null : dimConfig.m_dimCalc.m_dimInfo;
					boolean isImg = dimInfo != null && "20515".equals(dimInfo.m_subtype);
					boolean isFile = dimInfo != null && "20516".equals(dimInfo.m_subtype);
					if (isImg || isFile) {
						String width = dimConfig.m_width > 0 ?" width=\""+dimConfig.m_width+"px\" " : "";
						ColumnMappingHelper colHelper = dimInfo.m_colMap;
						String tbl = colHelper.table;
						String col = colHelper.column;
						String idCol = "id"; //TODO
						int id = objectId;
						StringBuilder ref = new StringBuilder();
						ref.append("getFile.do?_table=").append(tbl).append("&_column=").append(col).append("&_id_col=").append(idCol).append("&_id=").append(id);
						if (isImg)
							sb.append("<img src=\"").append(ref).append("\"").append(width).append("/>");
						if (isFile)
							sb.append("<a href=\"").append(ref).append("\"> Download</a>");
					}
					else {
						if (customPrintHelp != null) {
							//DO SOMETHING
						}
						else {
							Value value = new Value();
							
							int attribType = dimInfo == null ? Cache.STRING_TYPE : dimInfo.getAttribType();
							String varName = null;
							boolean printText = true;
							if (attribType != Cache.STRING_TYPE && attribType != Cache.NUMBER_TYPE && attribType != Cache.DATE_TYPE) {
		                          printText = false;
		                     }
							if (dimInfo != null) {
								varName = "v"+dimInfo.m_id;
								
								int defInt = !printText ? PageHeader.getPageDefaultInt( dimConfig, session) : Misc.getUndefInt();
								String defStr = printText ? PageHeader.getPageDefaultString(dimConfig, session) : null;
	
								if (attribType == Cache.INTEGER_TYPE) {
									int val = rs == null ? defInt : Misc.getRsetInt(rs,colIndex);
									value.setValue(val);
								}
								else if (attribType == Cache.NUMBER_TYPE) {
									double val = rs == null ? Misc.getParamAsDouble(defStr) : Misc.getRsetDouble(rs,colIndex);
									value.setValue(val);
								}
								else if (attribType == Cache.STRING_TYPE) {
									String val = rs == null ? defStr : Misc.getRsetString(rs, colIndex);
									value.setValue(val);
								}
								else if (attribType == Cache.DATE_TYPE) {					
									java.sql.Timestamp val = rs == null ? null : rs.getTimestamp(colIndex);
									value.setValue(Misc.sqlToUtilDate(val));
								}
								else {
									int val = rs == null ? defInt : Misc.getRsetInt(rs,colIndex);
									if (attribType == Cache.LOV_TYPE) {
										val = cache.getParentDimValId(conn, dimInfo, val);
									}
									value.setValue(val);
								}
							}//if valid dimInfo
							else {
								value.setValue((String) dimConfig.m_disp);
							}
							if (dimConfig.m_hidden) {
								if (varName != null)
									hiddenVar.append("<input type='hidden' name='").append(varName).append("' ").append(" value='").append(value.toString()).append("'").append("/>");
								sb.append("&nbsp;");
							}
							else {
								if (varName == null) {
									String disp = value.toString();
									if (disp == null || disp.length() == 0)
										sb.append("&nbsp;");
									else
										sb.append(value.toString());
								}
								else {
									String formattedString = null;
									if (!toRead) {
										if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM)
											needVehicleDriverWrite.first = true;
										if (dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM)
											needVehicleDriverWrite.second = true;
									}
									if (toRead || dimInfo.getAttribType() != Cache.LOV_TYPE) {
										if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM) {
											formattedString = cache.getVehicleDisplayInfo(conn, value.m_iVal).first;
										}
										else if (dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM){
											formattedString = cache.getDriverDisplayInfo(conn, value.m_iVal).first;
										}
										else {
											formattedString = value.toString(dimInfo, formatHelper.multScaleFactors.get(colIndex-1), formatHelper.formatters.get(colIndex-1), session, session.getCache(), conn, sdf);
										}
									}
									if (attribType == Cache.NUMBER_TYPE && Misc.isUndef(value.getDoubleVal()))
											formattedString = "";
									else if ((attribType == Cache.LOV_NO_VAL_TYPE || attribType == Cache.INTEGER_TYPE) && Misc.isUndef(value.getIntVal()))
										formattedString = "";
									else if ((attribType == Cache.LOV_TYPE) && Misc.isUndef(value.getIntVal()))
										formattedString = toRead ? "" : null;
									if (toRead) {
										
										sb.append(formattedString);
									}
									else {
										cache.printDimVals(session, conn, session.getUser(), dimInfo, value.m_iVal, null, sb, varName, false,  null, false, Misc.getUndefInt(), dimConfig.m_height < 1 ? 1 :dimConfig.m_height, dimConfig.m_width < 0 ? 20 : dimConfig.m_width
							                  , false, null, false, false, toRead, dimConfig.m_onChange_handler, null,  Misc.getUndefInt(), Misc.getUndefInt()
							                  , null, formattedString, false, 0, dimConfig);
									}
								}
							}
						}//if to get from non custom
					}//non file/img
				}//end of col
				GeneralizedQueryBuilder.helperReduceColsForRowSpan(colsForRowSpan);
				sb.append("</tr>\n");
			}//end of row
			sb.append("</table>");
			if (needVehicleDriverWrite.first || needVehicleDriverWrite.second) {
				sb.append("<script src=\"").append(Misc.G_SCRIPT_BASE).append("autocomplete.js\">").append("</script>");
	
				sb.append("<script>");
				if (needVehicleDriverWrite.first)
					sb.append("var jg_vehicleList = new Array();");
				if (needVehicleDriverWrite.second)
					sb.append("var jg_driverList = new Array();");
				int portToUse = Misc.getUndefInt();
				int pv123forLookup = Misc.getUndefInt();
				int pv123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
				if (!Misc.isUndef(objectId)) {
					pv123 = DVUtils.guessAppropPV123(session.getCache(), conn, pv123, objectType != 0 ? objectId : Misc.getUndefInt(),  objectType == 0 ? objectId : Misc.getUndefInt());
				}
				
				if (needVehicleDriverWrite.first) {
					StringBuilder sb1= DVUtils.getVehicleAutoCompleteObjExt(conn, session.getCache(), pv123, "jg_vehicleList");
					if (sb1 != null) sb.append(sb1).append("\n");
				}
				if (needVehicleDriverWrite.second) {
					StringBuilder sb1= DVUtils.getDriverAutoCompleteObjExt(conn, session.getCache(), pv123, "jg_driverList");
					if (sb1 != null) sb.append(sb1).append("\n");
				}

				sb.append("</script>");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
		}
	}

	//public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper, JspWriter out,ByteArrayOutputStream stream,int reportType,String reportName,int reportId, ServletOutputStream servletStream) throws Exception {
	public FrontPageInfo getCalculatedFrontPageInfo() {
		
		return calculatedFrontPageInfo;
	}
	private void calcFrontPageInfo(Element topElem) {
		//basically when being created first time we want to create only if create_fp is true
		//once calc has been set up, then we want to recalc if the earlier InputTemplate's calcFrontPageInfo is not null
		if ((this.searchCriteria == null || searchCriteria.size() == 0) &&  topElem != null && !"1".equals(topElem.getAttribute("create_fp")))
			return;
		if (topElem == null && this.calculatedFrontPageInfo == null)
			return;
		FrontPageInfo base = new FrontPageInfo();
		
		base.m_frontSearchCriteria = (ArrayList<ArrayList<DimConfigInfo>>) this.searchCriteria.clone();
		base.m_objectIdColName = this.m_objectIdColName;
		base.m_objectIdParamLabel = this.m_objectIdParamLabel;
		base.m_help = this.m_help;
		base.m_driverObjectLocTracker = Integer.toString(this.objectType);
		if (topElem != null) {
			this.m_objectIdParamLabel = base.m_objectIdParamLabel = Misc.getParamAsString(topElem.getAttribute("object_param"), base.m_objectIdParamLabel);
	        this.m_objectIdColName = base.m_objectIdColName = Misc.getParamAsString(topElem.getAttribute("object_col_name"), base.m_objectIdColName);
	        this.m_help = base.m_help = Misc.getParamAsString(topElem.getAttribute("help"), base.m_help);
	        FrontPageInfo.readFrontPageTopLevel(topElem, base);

		}
	 
	
		ArrayList<DimConfigInfo> fpList = new ArrayList<DimConfigInfo>();
		for (int i=0,is = this.rows == null ? 0 : this.rows.size(); i<is;i++) {
			ArrayList<DimConfigInfo> row = rows.get(i);
			for (int j=0,js=row.size(); j<js; j++) {
				fpList.add(row.get(j));
				
			}
		}
		base.m_frontInfoList = fpList;
		base.postProcess(topElem, false);
		
		
		calculatedFrontPageInfo = base;
	}
	public static int getMaxColCountAndSkipRow(ArrayList<ArrayList<DimConfigInfo>> rows, ArrayList<Integer> toSkipRowIndex, boolean doingNew) {
		int maxColCount = 0;
		for (int i=0,is=rows.size();i<is;i++) {
			ArrayList<DimConfigInfo> row = rows.get(i);
			int colCount = 0;
			boolean seenNonHid = false;;
			for (int j=0,js = row.size();j<js;j++) {
				DimConfigInfo dimConfig = row.get(j);
				if (dimConfig.m_hidden)
					continue;
				int hideControl = dimConfig.m_hiddenSpecialControl;
				boolean toHide = doingNew ? hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_PRECREATE 
						:
						hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_POSTCREATE
					;
				if (toHide)
					continue;
				seenNonHid = true;
				colCount += 1;
				if (dimConfig.m_dataSpan < 1)
					colCount++;
				else if (dimConfig.m_dataSpan < 1000)
					colCount += dimConfig.m_dataSpan;
				else
					colCount++;
			}
			if (!seenNonHid)
				toSkipRowIndex.add(i);
			if (colCount > maxColCount)
				maxColCount = colCount;
		}
		return maxColCount;
	}
	
	public void printBlockMulti(StringBuilder sb, ArrayList<Integer> objId, Connection conn, Cache cache, SessionManager session, MiscInner.PairBool needVehicleDriverWrite, QueryHelper queryHelper, boolean readOnly) throws Exception {
		//Dimconfig marked as hidden will be printed in hidden - but those determined to be hidden because of m_hideSpecialControl will not be printed
		//Dimconfig marked as read only will not have <input element>
		int objectId = objId.get(0);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (!Misc.isUndef(objectId)) {
				ps = conn.prepareStatement(queryHelper.m_view);
				ps.setInt(1, objectId);
				rs = ps.executeQuery();
				if (!rs.next()) {
					rs.close();
					ps.close();
					rs = null;
					ps = null;
				}
			}
			boolean doingNew = Misc.isUndef(objectId);
			needVehicleDriverWrite.first = false;
			needVehicleDriverWrite.second = false;
			
			int maxColCount = -1;
			ArrayList<Integer> toSkipRowIndex = new ArrayList<Integer>();
			FormatHelper formatHelper = this.getFormatHelper(rows, session);
			maxColCount = getMaxColCountAndSkipRow(rows, toSkipRowIndex, doingNew);
			//now start printing ..
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			StringBuilder hiddenVar = new StringBuilder();
			int colIndex = 0;
			sb.append("<input type='hidden' name='").append(objectType == 0 ? "vehicle_id" : "driver_id").append("' value='").append(objectId).append("'/>");
			sb.append("<table border='0' cellpadding='3' cellspacing='0'");
			for (int i=0,is=rows.size();i<is;i++) {
				ArrayList<DimConfigInfo> row = rows.get(i);
				boolean toSkip = false;
				for (int j=0,js = toSkipRowIndex.size();j<js;j++) {
					if (toSkipRowIndex.get(j) == i) {
						toSkip= true;
						break;
					}
				}
				if (toSkip) {
					colIndex += row.size();
					continue;
				}
				sb.append("<tr>");
				int colsUsed = 0;
				for (int j=0,js = row.size();j<js;j++) {
					DimConfigInfo dimConfig = row.get(j);
					colIndex++;
					if (dimConfig.m_hidden)
						continue;
					int hideControl = dimConfig.m_hiddenSpecialControl;
					int readControl = dimConfig.m_readSpecialControl;
					boolean toHide = doingNew ? hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_PRECREATE 
																				:
																				hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_POSTCREATE
																			;
					if (toHide) {
						continue;
					}
					boolean toRead = doingNew ? readControl == DimConfigInfo.G_READHIDE_ALWAYS || readControl == DimConfigInfo.G_READHIDE_PRECREATE 
							:
							readControl == DimConfigInfo.G_READHIDE_ALWAYS || readControl == DimConfigInfo.G_READHIDE_POSTCREATE
						;
					if (readOnly)
						toRead = true;
	
					String labelClass = dimConfig.m_labelStyleClass == null || dimConfig.m_labelStyleClass.length() == 0 ? "sh" : dimConfig.m_labelStyleClass;
					sb.append("<td class='").append(labelClass).append("' ");
					if (dimConfig.m_labelNowrap)
						sb.append("nowrap='1'");
					else if (dimConfig.m_labelWidth > 0)
						sb.append("width='").append(dimConfig.m_labelWidth).append("px'");
					sb.append(">");
					sb.append(dimConfig.m_name == null || dimConfig.m_name.length() == 0 ? "&nbsp;" : dimConfig.m_name);
					if ("sh".equals(labelClass) && dimConfig.m_name != null && !dimConfig.m_name.endsWith(":"))
						sb.append(":");
					sb.append("</td>\n");
					colsUsed++;
					int dataColspan = dimConfig.m_dataSpan > 0 && dimConfig.m_dataSpan < 1000 ? 1 : dimConfig.m_dataSpan;
					if (j == js-1)
						dataColspan = maxColCount-colsUsed;
					
					sb.append("<td ");
					if (dataColspan > 1)
						sb.append("colspan='").append(dataColspan).append("' ");
					colsUsed += dataColspan;
					if (dimConfig.m_nowrap)
						sb.append("nowrap='1'");
					sb.append(" class='tn'>");
					sb.append(hiddenVar);
					hiddenVar.setLength(0);
					
					String customPrintHelp = dimConfig.m_customBlock;
					
					if (customPrintHelp != null) {
						//DO SOMETHING
					}
					else {
						Value value = new Value();
						DimInfo dimInfo = dimConfig.m_dimCalc == null ? null : dimConfig.m_dimCalc.m_dimInfo;
						int attribType = dimInfo == null ? Cache.STRING_TYPE : dimInfo.getAttribType();
						String varName = null;
						boolean printText = true;
						if (attribType != Cache.STRING_TYPE && attribType != Cache.NUMBER_TYPE && attribType != Cache.DATE_TYPE) {
	                          printText = false;
	                     }
						if (dimInfo != null) {
							varName = "v"+dimInfo.m_id;
							
							int defInt = !printText ? PageHeader.getPageDefaultInt( dimConfig, session) : Misc.getUndefInt();
							String defStr = printText ? PageHeader.getPageDefaultString(dimConfig, session) : null;

							if (attribType == Cache.INTEGER_TYPE) {
								int val = rs == null ? defInt : Misc.getRsetInt(rs,colIndex);
								value.setValue(val);
							}
							else if (attribType == Cache.NUMBER_TYPE) {
								double val = rs == null ? Misc.getParamAsDouble(defStr) : Misc.getRsetDouble(rs,colIndex);
								value.setValue(val);
							}
							else if (attribType == Cache.STRING_TYPE) {
								String val = rs == null ? defStr : Misc.getRsetString(rs, colIndex);
								value.setValue(val);
							}
							else if (attribType == Cache.DATE_TYPE) {					
								java.sql.Timestamp val = rs == null ? null : rs.getTimestamp(colIndex);
								value.setValue(Misc.sqlToUtilDate(val));
							}
							else {
								int val = rs == null ? defInt : Misc.getRsetInt(rs,colIndex);
								if (attribType == Cache.LOV_TYPE) {
									val = cache.getParentDimValId(conn, dimInfo, val);
								}
								value.setValue(val);
							}
						}//if valid dimInfo
						else {
							value.setValue((String) null);
						}
						if (dimConfig.m_hidden) {
							if (varName != null)
								hiddenVar.append("<input type='hidden' name='").append(varName).append("' ").append(" value='").append(value.toString()).append("'").append("/>");
							sb.append("&nbsp;");
						}
						else {
							if (varName == null) {
								String disp = value.toString();
								if (disp == null || disp.length() == 0)
									sb.append("&nbsp;");
								else
									sb.append(value.toString());
							}
							else {
								String formattedString = null;
								if (!toRead) {
									if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM)
										needVehicleDriverWrite.first = true;
									if (dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM)
										needVehicleDriverWrite.second = true;
								}
								if (toRead || dimInfo.getAttribType() != Cache.LOV_TYPE) {
									if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM) {
										formattedString = cache.getVehicleDisplayInfo(conn, value.m_iVal).first;
									}
									else if (dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM){
										formattedString = cache.getDriverDisplayInfo(conn, value.m_iVal).first;
									}
									else {
										formattedString = value.toString(dimInfo, formatHelper.multScaleFactors.get(colIndex-1), formatHelper.formatters.get(colIndex-1), session, session.getCache(), conn, sdf);
									}
								}
								if (toRead) {
									sb.append(formattedString);
								}
								else {
									cache.printDimVals(session, conn, session.getUser(), dimInfo, value.m_iVal, null, sb, varName, false,  null, false, Misc.getUndefInt(), dimConfig.m_height < 1 ? 1 :dimConfig.m_height, dimConfig.m_width < 0 ? 20 : dimConfig.m_width
						                  , false, null, false, false, toRead, dimConfig.m_onChange_handler, null,  Misc.getUndefInt(), Misc.getUndefInt()
						                  , null, formattedString);
								}
							}
						}
					}//if to get from non custom
				}//end of col
				sb.append("</tr>\n");
			}//end of row
			sb.append("</table>");
			if (needVehicleDriverWrite.first || needVehicleDriverWrite.second) {
				sb.append("<script src=\"").append(Misc.G_SCRIPT_BASE).append("autocomplete.js\">").append("</script>");
	
				sb.append("<script>");
				if (needVehicleDriverWrite.first)
					sb.append("var jg_vehicleList = new Array();");
				if (needVehicleDriverWrite.second)
					sb.append("var jg_driverList = new Array();");
				int portToUse = Misc.getUndefInt();
				int pv123forLookup = Misc.getUndefInt();
				int pv123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
				if (!Misc.isUndef(objectId)) {
					pv123 = DVUtils.guessAppropPV123(session.getCache(), conn, pv123, objectType != 0 ? objectId : Misc.getUndefInt(),  objectType == 0 ? objectId : Misc.getUndefInt());
				}
				
				if (needVehicleDriverWrite.first) {
					StringBuilder sb1= DVUtils.getVehicleAutoCompleteObjExt(conn, session.getCache(), pv123, "jg_vehicleList");
					if (sb1 != null) sb.append(sb1).append("\n");
				}
				if (needVehicleDriverWrite.second) {
					StringBuilder sb1= DVUtils.getDriverAutoCompleteObjExt(conn, session.getCache(), pv123, "jg_driverList");
					if (sb1 != null) sb.append(sb1).append("\n");
				}

				sb.append("</script>");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
		}
	}
	public static void doRecordSrcTimeForVehicle(Connection conn, int vehicleId) {
		PreparedStatement ps = null;
		try {
			WorkflowHelper.TableObjectInfo vehicleTableInfo = WorkflowHelper.getTableInfo(WorkflowHelper.G_OBJ_VEHICLES);
			boolean hasRecordSrc = vehicleTableInfo != null && vehicleTableInfo.hasRecordSrc(conn);
			boolean hasRecordTime = vehicleTableInfo != null && vehicleTableInfo.hasRecordSrcTime(conn);
			int recordId = Misc.getRecordSrcId(conn);
			String q = hasRecordSrc && hasRecordTime ? "update vehicle set record_src="+recordId+", src_record_time = now() where vehicle.id = ?"
					: hasRecordSrc ? "update vehicle set record_src="+recordId+" where vehicle.id = ?"
					: hasRecordTime ? "update vehicle set  src_record_time = now() where vehicle.id = ?"
						: null
					;
			if (q != null) {
				ps = conn.prepareStatement(q);
				ps.setInt(1, vehicleId);
				ps.execute();
				ps = Misc.closePS(ps);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			ps = Misc.closePS(ps);
		}
	}
	public String saveBlock(Connection conn, SessionManager session, QueryHelper queryHelper) throws Exception {
		try {
			String errMsg = null;
			int objectId = Misc.getParamAsInt(session.getParameter(this.objectType == 0 ? "vehicle_id" : "driver_id"));
			boolean doingNew = Misc.isUndef(objectId);
			PreparedStatement psBase = null;
			PreparedStatement psExt = null;
			ArrayList<DimInfo> baseDimList = null;
			ArrayList<DimInfo> extDimList = null;
			PreparedStatement psBaseInsert = null;
			PreparedStatement psExtInsert = null;
			if (doingNew) {
				psBaseInsert = conn.prepareStatement(objectType == 0 ? "insert into vehicle (status) values(1)" : "insert into driver_details (status) values (1)");
				if (queryHelper.m_updateExtendedNew != null) {
					psExtInsert = conn.prepareStatement(objectType == 0 ? "insert into vehicle_extended (vehicle_id) values(?)" : "insert into driver_details_extended (driver_id) values (?)");
					extDimList = queryHelper.m_dimUpdateExtendedNew;
					psExt = conn.prepareStatement(queryHelper.m_updateExtendedNew);
				}
				if (queryHelper.m_updateBaseNew != null) {
					baseDimList = queryHelper.m_dimUpdateBaseNew;
					psBase= conn.prepareStatement(queryHelper.m_updateBaseNew);
				}
				
			}
			else {
				if (queryHelper.m_updateExtended != null) {
					//checkif exits
					PreparedStatement ps = conn.prepareStatement(objectType == 0 ? "select 1 from vehicle_extended where vehicle_id = ?" : "select 1 from driver_details_extended where driver_id= ?");
					ps.setInt(1, objectId);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						
					}
					else {
						psExtInsert = conn.prepareStatement(objectType == 0 ? "insert into vehicle_extended (vehicle_id) values(?)" : "insert into driver_details_extended (driver_id) values (?)");						
					}
					rs.close();
					ps.close();
				}
				if (queryHelper.m_updateExtended != null) {
					extDimList = queryHelper.m_dimUpdateExtended;
					psExt = conn.prepareStatement(queryHelper.m_updateExtended);
				}
				if (queryHelper.m_updateBase != null) {
					baseDimList = queryHelper.m_dimUpdateBase;
					psBase= conn.prepareStatement(queryHelper.m_updateBase);
				}
			}
			//now insert if necessary
			if (psBaseInsert != null) {
				psBaseInsert.executeUpdate();
				ResultSet rs = psBaseInsert.getGeneratedKeys();
				if (rs.next()) {
					objectId = rs.getInt(1);
				}
				rs.close();
				psBaseInsert.close();
				psBaseInsert = null;
			}
			if (psExtInsert != null) {
				psExtInsert.setInt(1, objectId);
				psExtInsert.execute();
				psExtInsert.close();
				psExtInsert = null;
			}
			if (objectType == 0)
				InputTemplate.doRecordSrcTimeForVehicle(conn, objectId);

			//now update
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
			SimpleDateFormat sdfTime = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			hackHandleChangePlantDriver(conn, session, objectType, objectId, baseDimList, extDimList);
			for (int art=0;art<2;art++) {
				PreparedStatement ps = art == 0 ? psBase : psExt;
				ArrayList<DimInfo> dimList = art == 0 ? baseDimList : extDimList;
				if (ps == null)
					continue;
				for (int i=0,is=dimList.size(); i<is;i++) {
					DimInfo dim = dimList.get(i);
					String varName = "v"+dim.m_id;
					String varVal = session.getParameter(varName);
					int attribType = dim.getAttribType();
					if (attribType == Cache.STRING_TYPE) {
						ps.setString(i+1, varVal);
					}
					else if (attribType == Cache.DATE_TYPE) {
						Date dt = Misc.getParamAsDate(varVal, null, sdf, sdfTime);
						ps.setTimestamp(i+1, Misc.utilToSqlDate(dt));
					}
					else if (attribType == Cache.NUMBER_TYPE) {
						Misc.setParamDouble(ps, Misc.getParamAsDouble(varVal), i+1);
					}
					else {
						Misc.setParamInt(ps, Misc.getParamAsInt(varVal), i+1);
					}
				}
				ps.setInt(dimList.size()+1, objectId);
				ps.execute();
				ps.close();
			}
			hackHandleDriverBlock(conn, session, objectType, objectId, baseDimList, extDimList);
			//DO CUSTOM STUFF IF ANY
			if(!Misc.isUndef(objectId)){
				if(objectType == 0){//vehicle
					VehicleExtendedInfo.markDirty(conn, objectId);
				}else{
					DriverExtendedInfo.markDirty(conn, objectId);
				}
			}
				
			return errMsg;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private static void hackHandleChangePlantDriver(Connection conn, SessionManager session, int objectType,  int objectId, ArrayList<DimInfo>baseDimList, ArrayList<DimInfo>extDimList) throws Exception {
		int dimToWatch = objectType == 0 ? 20872 : 20914;
		boolean hasChange = false;
		int newVal = Misc.getUndefInt();
		for (int art=0;art<2;art++) {
			ArrayList<DimInfo> dimList = art == 0 ? baseDimList : extDimList;
			for (int i=0,is=dimList == null ? 0 : dimList.size();i<is;i++) {
				if (dimList.get(i).m_id == dimToWatch) {
					hasChange = true;
					newVal = Misc.getParamAsInt(session.getParameter("v"+dimToWatch));
					break;
				}
			}
			if (hasChange)
				break;
		}
		if (hasChange && !Misc.isUndef(newVal)) {
			/*if (objectType == 0)
				VehicleChangeLog.changePlantFromManageCallBefUpd(conn, objectId, newVal);
			else
				VehicleChangeLog.changeDriverFromManageCallBefUpd(conn, newVal, objectId);*/
		}
	}
	private static void hackHandleDriverBlock(Connection conn, SessionManager session, int objectType,  int objectId, ArrayList<DimInfo>baseDimList, ArrayList<DimInfo>extDimList) throws Exception {
		if (objectType != 1) 
			return;
		boolean hasBlockUpd = false;
		for (int i=0,is=baseDimList==null ? 0 : baseDimList.size();i<is;i++) {
			DimInfo dimInfo = baseDimList.get(i);
			if (dimInfo == null)
				continue;
			int dimId = baseDimList.get(i).m_id;
			if (dimId == 90661 || dimId == 90658 || dimId == 90659 || dimId == 90660) {
				hasBlockUpd = true;
				break;
			}
		}
		if (hasBlockUpd) {
			PreparedStatement ps = null;
			try {
				ps = conn.prepareStatement("insert into driver_blocking_hist(driver_id, block_status, block_from, block_to, block_reason, created_on, created_by) (select id, block_status, block_from, block_to, block_reason, now(), "+session.getUserId()+" from driver_details where id = ?)");
				ps.setInt(1, objectId);
				ps.execute();
				ps = Misc.closePS(ps);
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			finally {
				ps = Misc.closePS(ps);
			}
		}
	}
	
	//internal use functions
	
	private static String getId(String menuTag, int portNodeId, String fileName) {
		if (Misc.isUndef(portNodeId))
			return menuTag+"_"+fileName;
		return portNodeId+"_"+menuTag+"_"+fileName;
	}

	public static InputTemplate getAtPortNodeId(Cache cache, Connection conn, String menuTag, int portNodeId, String fileName, SessionManager session) throws Exception {
		String lookupId = getId(menuTag, portNodeId, fileName);
	    Pair<Boolean, InputTemplate> retval = inputCache.get(lookupId);
	    if (retval != null)
	    	return retval.second;
	    else {
	    	InputTemplate result = null;
	    	if (Misc.isUndef(portNodeId)) {
	    		result = read(conn, fileName);
	    		retval = new Pair<Boolean, InputTemplate>(true, result);
	    		inputCache.put(lookupId, retval);
	    		return retval.second;
	    	}
	    	final String rowq = "select mc.row_index, mc.col_index, mc.column_name, mc.label, mc.default_val from muti_col_template mct join cols_in_template mc on (multi_col_template_id = mct.id) where mct.port_node_id=? and (mct.menu_tag = ? or mct.menu_tag is null) and (mct.template_name = ? or mct.template_name is null) and (mct.template_name is not null or mct.menu_tag is not null) order by mc.row_index, mc.col_index ";
	    	
	    	PreparedStatement ps = conn.prepareStatement(rowq);
	    	ps.setInt(1, portNodeId);
	    	ps.setString(2, menuTag);
	    	ps.setString(3, fileName);
	    	ResultSet rs = ps.executeQuery();
	    	InputTemplate fromFile = null;
	    	HashMap<String, DimConfigInfo> fromFileLookupHelper = null;
	    	int prevRow = -1;
	    	int prevCol = -1;
	    	ArrayList<DimConfigInfo> row = new ArrayList<DimConfigInfo>();
	    	while (rs.next()) {
	    		if (fromFile == null) {
	    			fromFile  = getAtPortNodeId(cache, conn, menuTag, Misc.getUndefInt(), fileName, session);
	    			if (fromFile == null)
	    				break;
	    			fromFileLookupHelper = fromFile.populateLookupHelper();
	    		}
	    		if (result == null) {
	    			result = new InputTemplate();
	    			result.copyHeaderExt(fromFile);
	    		}
	    		int currRow = rs.getInt("row_index");
	    		int currCol = rs.getInt("col_index");
	    		String colName = rs.getString("column_name");
	    		String label = rs.getString("label");
	    		String defaultVal = rs.getString("default_val");
	    		if (currRow != prevRow) {
	    			row = null;
	    			prevRow = currRow;
	    		}
	    		DimConfigInfo entryInFile = fromFile.getDimConfigInfo(colName, fromFileLookupHelper);
	    		if (entryInFile == null) {
	    			continue;
	    		}
	    		if (row == null) {
	    			row = new ArrayList<DimConfigInfo>();
	    			result.rows.add(row);
	    		}
	    		DimConfigInfo toadd = ((DimConfigInfo) entryInFile.clone());
	    		if (defaultVal != null && defaultVal.length() != 0)
	    			toadd.m_default = defaultVal;
	    		toadd.m_name = label;
	    		row.add(toadd);
	    	}
	    	rs.close();
	    	ps.close();
	    	retval = new Pair<Boolean, InputTemplate>(true, result);
	    	//now populate hidden elements
	    	HashMap<String, DimConfigInfo> resultLookupHelper = null;
	    	if (fromFile != null && result != null && result.rows.size() > 0) {
	    		ArrayList<DimConfigInfo> firstRow = result.rows.get(0);
	    		resultLookupHelper = result.populateLookupHelper();
	    		for (int i=0,is=fromFile.rows.size();i<is;i++) {
					ArrayList<DimConfigInfo> fileRow = fromFile.rows.get(i);
					for (int j=0,js = fileRow == null ? 0 : fileRow.size();j<js;j++) {
						DimConfigInfo dimConfig = fileRow.get(j);
						if (!dimConfig.m_hidden)
							continue;
						DimConfigInfo resultConfig = null;
						String name = dimConfig.m_columnName;
						if (name != null && name.length() > 0) {
							resultConfig = result.getDimConfigInfo(name, resultLookupHelper);
						}
						if (resultConfig == null && dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null) {
							name = "d"+dimConfig.m_dimCalc.m_dimInfo.m_id;
							resultConfig = result.getDimConfigInfo(name, resultLookupHelper);
						}
						if (resultConfig == null) {
							resultConfig = (DimConfigInfo) dimConfig.clone();
							firstRow.add(0, resultConfig);
						}
					}
				}
	    	}
	    	//now populate the searchCriteria
	    	if (result != null && fromFile != null) {
		    	result.searchCriteria = new ArrayList<ArrayList<DimConfigInfo>>();
		    	for (int i=0,is=fromFile.searchCriteria == null ? 0 : fromFile.searchCriteria.size(); i<is; i++) {
		    		ArrayList<DimConfigInfo> newRow = null;
		    		ArrayList<DimConfigInfo> r = fromFile.searchCriteria.get(i);
		    		for (int j=0,js=r.size(); j<js; j++) {
		    			DimConfigInfo c = r.get(j);
		    			boolean toAdd = c.m_isMandatory;
		    			if (!toAdd) {
		    				int did = c.m_dimCalc != null && c.m_dimCalc.m_dimInfo != null ? c.m_dimCalc.m_dimInfo.m_id : Misc.getUndefInt();
		    				DimInfo dimInfo = DimInfo.getDimInfo(did);
		    				if (dimInfo != null && dimInfo.m_descDataDimId == 123) {
		    					toAdd = true;
		    				}
		    			}
		    			if (!toAdd) { //check if being viewed
		    				DimConfigInfo resultConfig = null;
							String name = c.m_columnName;
							if (name != null && name.length() > 0) {
								resultConfig = result.getDimConfigInfo(name, resultLookupHelper);
							}
							if (resultConfig == null && c.m_dimCalc != null && c.m_dimCalc.m_dimInfo != null) {
								name = "d"+c.m_dimCalc.m_dimInfo.m_id;
								resultConfig = result.getDimConfigInfo(name, resultLookupHelper);
							}
							if (resultConfig != null) {
								toAdd = true;
							}
		    			}
		    			if (toAdd) {
		    				DimConfigInfo a = (DimConfigInfo) c.clone();
		    				if (newRow == null) {
		    					newRow = new ArrayList<DimConfigInfo>();
		    					result.searchCriteria.add(newRow);
		    				}
		    				newRow.add(a);
		    			}//if to be added
		    		}//for each col
		    	}//for each row
	    	}//result != null
	    	if (result != null)
	    		result.calcFrontPageInfo(null);
	    	inputCache.put(lookupId, retval);
	    }
		 return retval.second;
	}
	
	
	public HashMap<String, DimConfigInfo> populateLookupHelper() {
		HashMap<String, DimConfigInfo> lookupHelper = new HashMap<String, DimConfigInfo>();
		for (int i=0,is=rows.size();i<is;i++) {
			ArrayList<DimConfigInfo> row = rows.get(i);
			for (int j=0,js = row.size();j<js;j++) {
				DimConfigInfo dimConfig = row.get(j);
				boolean added = false;
				String name = dimConfig.m_columnName;
				if (name != null && name.length() > 0) {
					lookupHelper.put(name, dimConfig);
					added = true;
				}
				if (dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null) {
					name = "d"+dimConfig.m_dimCalc.m_dimInfo.m_id;
					lookupHelper.put(name, dimConfig);
					added = true;
				}
				if (!added) {
					lookupHelper.put(dimConfig.m_name, dimConfig);
				}
			}
		}
		return lookupHelper;
	}
	
	private DimConfigInfo getDimConfigInfo(String colName, HashMap<String, DimConfigInfo> lookupHelper) {
		return lookupHelper.get(colName);
	}
	private static InputTemplate read(Connection conn, String fileName) throws Exception {
		InputTemplate retval = new InputTemplate();
	     try {
	    	   FileInputStream inp = null;
	    	   Document frontXML = null;
	    	   try {
		           inp = new FileInputStream(Cache.serverConfigPath+System.getProperty("file.separator")+"web_look"+System.getProperty("file.separator")+"project"+System.getProperty("file.separator")+fileName);
		           MyXMLHelper test = new MyXMLHelper(inp, null);
		           frontXML =  test.load();           
	    	   }
	    	   catch (Exception e2) {
	    		   throw e2;
	    		//   return null;
	    	   }
	    	   finally {
	    		   if (inp != null)
	    			   inp.close();
	    		   inp = null;
	    	   }
	           if (frontXML == null || frontXML.getDocumentElement() == null) {
	               return null;
	           }
	           Element topElem = frontXML.getDocumentElement();
	           retval.m_help = topElem.getAttribute("help");
	           retval.javaScriptFile = topElem.getAttribute("javascript_file");
	           retval.onSubmitPreHandler = topElem.getAttribute("on_submit_pre");
	           retval.insteadPostActionHandler = topElem.getAttribute("form_action");
	           retval.customPostHandler = Misc.getParamAsInt(topElem.getAttribute("post_handler_hint"));
	           retval.objectType = Misc.getParamAsInt(topElem.getAttribute("object_type"),retval.objectType);
	           retval.m_customField1 = Misc.getParamAsString(topElem.getAttribute("custom_field1"), null);
	           retval.m_customField2 = Misc.getParamAsString(topElem.getAttribute("custom_field2"), null);
	           retval.m_customField3 = Misc.getParamAsString(topElem.getAttribute("custom_field3"), null);
	           retval.m_columnOutput = Misc.getParamAsInt(topElem.getAttribute("column_output"),1);
	           retval.m_createSpec = Misc.getParamAsInt(topElem.getAttribute("create_spec"),0);
	           retval.m_createPriv = InputTemplate.getCleanedString(topElem.getAttribute("create_priv"));
	           retval.m_searchSpecIfExists = Misc.getParamAsInt(topElem.getAttribute("search_spec"),0);
	           for (Node n = topElem.getFirstChild(); n != null; n = n.getNextSibling()) {
	        	   if (n.getNodeType() != 1)
	        		   continue;
	        	   Element block = (Element) n;
	        	   boolean doingPage =  ("page".equals(block.getTagName())); 
	        	   if (doingPage) {
	        		   ArrayList<ArrayList<DimConfigInfo>> rows = DimConfigInfo.readRowColInfo(block, false);
	        		   retval.rows = rows;
	        	   }
	        	   boolean search = "search".equals(block.getTagName());
	        	   if (search) {
	        		   retval.searchCriteria = DimConfigInfo.readRowColInfo(block,false);
	        		   retval.m_showSearchCriteriaInInput = "1".equals(block.getAttribute("show_seach_in_inp"));
	        	   }
	        	   
	        	   if ("common".equals(block.getTagName())) {
	        		   retval.commonRows = DimConfigInfo.readRowColInfo(block, false);
	        	   }
	           }
	//           retval.forMultiReport = retval.getQueryForMultiPartsInit(conn, null, topElem, true);
	           retval.calcFrontPageInfo(topElem);
	           if (retval.rows != null && retval.rows.size() == 1)
	        	   retval.m_doAsMultiRow = true;
	           //String multiRowSpec = Misc.getParamAsString(topElem.getAttribute("custom_field1"), null);
	           //if ("0".equals(multiRowSpec)) {
	           //   retval.m_doAsMultiRow = false;
	           //}
	           //else if ("1".equals(multiRowSpec)) {
	           //   retval.m_doAsMultiRow = true;
	           //}
	           //else {
	           //   if (retval.rows.size() == 1)
	           //		   retval.m_doAsMultiRow = true;
	           //}
	           if (retval.searchCriteria != null && retval.searchCriteria.size() > 0)
	        	   retval.calcFrontPageInfo(topElem);
	           return retval;
	     }
	     catch (Exception e) {
	    	 e.printStackTrace();
	    	 throw e;
	     }
	}
	
	
	
	private QueryHelper helpGenerateQuery(String base, String extended, String objectIdStr) {
		QueryHelper retval = new QueryHelper();
		StringBuilder sel = new StringBuilder();
		boolean selAdded = false;
		
		StringBuilder updateBaseNew = new StringBuilder();
		boolean updateBaseNewAdded = false;
		ArrayList<DimInfo>dimUpdateBaseNew = new ArrayList<DimInfo>();
		
		StringBuilder updateExtendedNew = new StringBuilder();
		boolean updateExtendedNewAdded = false;
		ArrayList<DimInfo>dimUpdateExtendedNew = new ArrayList<DimInfo>();
		
		StringBuilder updateBase = new StringBuilder();
		boolean updateBaseAdded = false;
		ArrayList<DimInfo>dimUpdateBase = new ArrayList<DimInfo>();
		
		StringBuilder updateExtended = new StringBuilder();
		boolean updateExtendedAdded = false;
		ArrayList<DimInfo>dimUpdateExtended = new ArrayList<DimInfo>();
		
		//1. generate sel
		sel.append(" select ");
		updateBaseNew.append("update ").append(base).append(" set ");
		updateBase.append("update ").append(base).append(" set ");
		updateExtendedNew.append("update ").append(extended).append(" set ");
		updateExtended.append("update ").append(extended).append(" set ");
		HashMap<Integer, Integer> seenDim = new HashMap<Integer, Integer>();
		for (int i=0,is=rows.size();i<is;i++) {
			ArrayList<DimConfigInfo> row = rows.get(i);
			for (int j=0,js = row.size();j<js;j++) {
				DimConfigInfo dimConfig = row.get(j);
				boolean valid = false;
				if (dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null) {
					ColumnMappingHelper colMap = dimConfig.m_dimCalc.m_dimInfo.m_colMap;
					if (colMap != null && colMap.table != null && colMap.column != null && !colMap.column.equalsIgnoreCase("Dummy")) {
						boolean doingBase = base.equals(colMap.table);
						boolean doingExtended = extended.equals(colMap.table);
						if (doingBase || doingExtended) {
							valid = true;
							if (selAdded) 
								sel.append(",");
							if(!colMap.useColumnOnlyForName)
								sel.append(colMap.table).append(".");
							sel.append(colMap.column);
							selAdded = true;
						}
						int readControl = dimConfig.m_readSpecialControl;
						int hideControl = dimConfig.m_hiddenSpecialControl;
						boolean updateAbleInNew = hideControl != DimConfigInfo.G_READHIDE_ALWAYS && hideControl != DimConfigInfo.G_READHIDE_PRECREATE &&
											readControl != DimConfigInfo.G_READHIDE_ALWAYS && readControl != DimConfigInfo.G_READHIDE_PRECREATE;
						boolean updateAbleInOld = hideControl != DimConfigInfo.G_READHIDE_ALWAYS && hideControl != DimConfigInfo.G_READHIDE_POSTCREATE &&
											readControl != DimConfigInfo.G_READHIDE_ALWAYS && readControl != DimConfigInfo.G_READHIDE_POSTCREATE;
						if (dimConfig.m_readOnly) {
							updateAbleInNew = false;
							updateAbleInOld = false;
						}
						if (doingBase) {
							if (updateAbleInNew) {
								if (updateBaseNewAdded)
									updateBaseNew.append(",");
								updateBaseNewAdded = true;
								updateBaseNew.append(colMap.column).append(" = ?");
								dimUpdateBaseNew.add(dimConfig.m_dimCalc.m_dimInfo);
							}
							if (updateAbleInOld) {
								if (updateBaseAdded)
									updateBase.append(",");
								updateBaseAdded = true;
								updateBase.append(colMap.column).append(" = ?");
								dimUpdateBase.add(dimConfig.m_dimCalc.m_dimInfo);
							}
						}
						else if (doingExtended) {
							if (updateAbleInNew) {
								if (updateExtendedNewAdded)
									updateExtendedNew.append(",");
								updateExtendedNewAdded = true;
								updateExtendedNew.append(colMap.column).append(" = ?");
								dimUpdateExtendedNew.add(dimConfig.m_dimCalc.m_dimInfo);
							}
							if (updateAbleInOld) {
								if (updateExtendedAdded)
									updateExtended.append(",");
								updateExtendedAdded = true;
								updateExtended.append(colMap.column).append(" = ?");
								dimUpdateExtended.add(dimConfig.m_dimCalc.m_dimInfo);
							}
						}//if validExitended
					}//if valid columnMapping helper mapped to vehicle/diver
				}//if proper dimConfig
				if (!valid) {
					if (selAdded)
						sel.append(",");
					sel.append("null");
					selAdded = true;
				}
				
			}//for each col
		}	//for each row
		if (selAdded) {
			sel.append(" from ").append(base).append(" left outer join ").append(extended).append(" on (").append(base).append(".id = ").append(extended).append(".").append(objectIdStr).append(") ")
			.append(" where ").append(base).append(".id = ?");
			retval.m_view = sel.toString();
		}
		if (updateBaseNewAdded) {
			updateBaseNew.append(" where id=?");
			retval.m_updateBaseNew = updateBaseNew.toString();
			retval.m_dimUpdateBaseNew = dimUpdateBaseNew;
		}
		if (updateExtendedNewAdded) {
			updateExtendedNew.append(" where ").append(objectIdStr).append(" = ? ");
			retval.m_updateExtendedNew = updateExtendedNew.toString();
			retval.m_dimUpdateExtendedNew = dimUpdateExtendedNew;
		}
		if (updateBaseAdded) {
			updateBase.append(" where id=?");
			retval.m_updateBase = updateBase.toString();
			retval.m_dimUpdateBase = dimUpdateBase;
		}
		if (updateExtendedAdded) {
			updateExtended.append(" where ").append(objectIdStr).append(" = ? ");
			retval.m_updateExtended = updateExtended.toString();
			retval.m_dimUpdateExtended = dimUpdateExtended;
		}
		
		return retval;
	}
	
	//Web related stuff ... may have to move it to another file ..
	private static ResultInfo.FormatHelper getFormatHelper(ArrayList<ArrayList<DimConfigInfo>> rows,  SessionManager session) {
		FormatHelper retval = new FormatHelper();
		// TODO pv20501
//		int uProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": searchBoxHelper.m_topPageContext+"20501"), 0);
//	    int sProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": searchBoxHelper.m_topPageContext+"20530"), 0);
//	    int fProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": searchBoxHelper.m_topPageContext+"20560"), 0);
		int uProfiler = Misc.getParamAsInt(session.getParameter( "pv20501"), 0);
	    int sProfiler = Misc.getParamAsInt(session.getParameter( "pv20530"), 0);
	    int fProfiler = Misc.getParamAsInt(session.getParameter("pv20560"), 0);
	    retval.m_fFormatSelected = fProfiler;
		Cache cache = session.getCache();
		retval.m_uProfileList = DimConfigInfo.getProfileList(cache.getUnitProfileDef(), uProfiler);
		retval.m_sProfileList = DimConfigInfo.getProfileList(cache.getScaleProfileDef(), sProfiler);
		retval.m_fProfileList = DimConfigInfo.getProfileList(cache.getFormatProfileDef(), fProfiler);
		retval.formatters = new ArrayList<FmtI.AllFmt>();
		retval.multScaleFactors = new ArrayList<Pair<Double,Double>>();
		String lang = "en";
		String country = "IN";
		DimInfo dLocalList = DimInfo.getDimInfo(20560);
		ValInfo dlocalValInfo = dLocalList == null ? null : dLocalList.getValInfo(retval.m_fFormatSelected);
		if (dlocalValInfo != null) {
			lang = dlocalValInfo.getOtherProperty("lang");
			country = dlocalValInfo.getOtherProperty("country");
		}
		
		if (lang == null || lang.length() == 0)
			lang = "en";
		if (country == null | country.length() == 0)
			country = "IN";
		
		Locale locale = new Locale(lang, country);
		for (int j=0,js=rows.size();j<js;j++) {
			ArrayList<DimConfigInfo> colList = rows.get(j);
			for (int i=0,is = colList.size();i<is;i++) {
				DimConfigInfo dc = colList.get(i);
				FmtI.AllFmt toAdd = null;
				Pair<Double, Double> multScale = null;
				if (dc != null && dc.m_dimCalc != null && dc.m_dimCalc.m_dimInfo != null) {
					DimInfo dimInfo = dc.m_dimCalc.m_dimInfo;
					int ty = dimInfo.m_type;
					int subTy = Misc.getParamAsInt(dimInfo.m_subtype);
					DimInfo subTypeDim = DimInfo.getDimInfo(subTy);
					DimConfigInfo unitprofile = DimConfigInfo.getProfile(retval.m_uProfileList, subTy);
					DimConfigInfo scaleprofile = DimConfigInfo.getProfile(retval.m_sProfileList, subTy);
					
					
					if (ty == Cache.NUMBER_TYPE) {
						if (subTypeDim != null && unitprofile != null) {
							double addFactor = 0;
							double mulFactor = 1;
							DimInfo.ValInfo valInfo = subTypeDim.getValInfo(unitprofile.m_p_val);
							if (valInfo != null) {
								addFactor = Misc.getParamAsDouble(valInfo.getOtherProperty("add_factor"));
								mulFactor = Misc.getParamAsDouble(valInfo.getOtherProperty("multi_factor"));
								multScale = new Pair<Double,Double>(mulFactor, addFactor);
							}
						}
						if (subTypeDim != null && scaleprofile != null) {
							double unit = scaleprofile.m_scale;
							int numAfterDec = scaleprofile.m_decimalPrecision;
							FmtI.Number numfmt = new FmtI.Number(locale, unit, numAfterDec);
							toAdd = numfmt;
						}
					}
					else if (ty == Cache.DATE_TYPE) {
						FmtI.Date dtfmt = new FmtI.Date(locale, subTy == 20506);
						toAdd = dtfmt;
					}
					else {
						//do nothing - no formatting
					}				
				}
				retval.formatters.add(toAdd);
				retval.multScaleFactors.add(multScale);
			}
		}
		return retval;
	}

	public int getObjectType() {
		return objectType;
	}

	public void setObjectType(int objectType) {
		this.objectType = objectType;
	}
	public static int updateOrCreateTemplate(Connection conn, String menuTag, String configFile, int portNodeId, ArrayList<ArrayList<Triple<Integer, String, String>>> rows) throws Exception {
		int templateId = getTemplateId(conn, menuTag, configFile, portNodeId);
		if (Misc.isUndef(templateId)) {
			PreparedStatement ps = conn.prepareStatement("insert into muti_col_template (menu_tag, port_node_id,template_name) values (?,?,?)");
			ps.setString(1, menuTag);
			ps.setInt(2, portNodeId);
			ps.setString(3, configFile);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next())
				templateId = rs.getInt(1);
			rs.close();
			ps.close();
		}
		else {
			PreparedStatement ps = conn.prepareStatement("delete from cols_in_template where multi_col_template_id = ?");
			ps.setInt(1, templateId);
			ps.execute();
			ps.close();
		}
		PreparedStatement ps = conn.prepareStatement("insert into cols_in_template (multi_col_template_id, row_index, col_index, column_name, label, default_val) values (?,?,?,?,?,?)");
		for (int i=0,is = rows.size();i<is;i++) {
			ArrayList<Triple<Integer, String, String>> row = rows.get(i);
			for (int j=0,js=row.size();j<js;j++) {
				Triple<Integer, String, String> item = row.get(j);
				ps.setInt(1, templateId);
				ps.setInt(2, i);
				ps.setInt(3, j);
				ps.setString(4, "d"+item.first);
				ps.setString(5, item.second);
				ps.setString(6, item.third);
				ps.addBatch();
			}
		}
		ps.executeBatch();
		ps.close();
		InputTemplate.makeDirty(null, conn, null, Misc.getUndefInt(), null);
		return templateId;
	}
	public static int getTemplateId(Connection conn, String menuTag, String configFile, int portNodeId) throws Exception {
		final String headerQ = "select id from muti_col_template where muti_col_template.port_node_id=? and (muti_col_template.menu_tag = ? or muti_col_template.menu_tag is null) and (template_name=? or template_name is null) and (template_name is not null or menu_tag is not null) order by id desc ";
    	
    	PreparedStatement ps = conn.prepareStatement(headerQ);
    	ps.setInt(1, portNodeId);
    	ps.setString(2, menuTag);
    	ps.setString(3, configFile);
    	ResultSet rs = ps.executeQuery();
    	int retval = Misc.getUndefInt();
    	if (rs.next()) {
    		retval = rs.getInt(1);
    	}
    	rs.close();
    	ps.close();
    	return retval;
	}
	
	public static void deleteTemplate(Connection conn, String menuTag, String configFile, int portNodeId) throws Exception {
    	final String rowq = "delete from cols_in_template using  muti_col_template mct join cols_in_template on (multi_col_template_id = mct.id) where mct.port_node_id=? and mct.menu_tag = ?  ";
    	final String headerQ = "delete from muti_col_template where muti_col_template.port_node_id=? and (muti_col_template.menu_tag = ? or menu_tag is null) and (template_name = ? or template_name is null) and (template_name is not null or menu_tag is not null) ";
    	
    	PreparedStatement ps = conn.prepareStatement(rowq);
    	ps.setInt(1, portNodeId);
    	ps.setString(2, menuTag);
    	ps.executeUpdate();
    	ps.close();
    	ps = conn.prepareStatement(headerQ);
    	ps.setInt(1, portNodeId);
    	ps.setString(2, menuTag);
    	ps.setString(3, configFile);
    	ps.executeUpdate();
    	ps.close();
    	InputTemplate.makeDirty(null, conn, null, Misc.getUndefInt(), null);
	}
//////@#@#@# New extended stuff
	//entry functions:printInputBlockNew
	//                             handleUpsdateOrInsertForWeb
	//
		
	private ArrayList<Integer> getObjectId(SessionManager session) {
		TableObjectInfo tablesFor = WorkflowHelper.g_tablesForObjectId.get(objectType);
		String objectId = tablesFor.getParamName();
		String paramvals[] = session.request.getParameterValues(objectId);
		ArrayList<Integer> retval = new ArrayList<Integer>();
		if (paramvals == null || paramvals.length == 0) {
			int iv = Misc.getParamAsInt(session.getParameter(objectId));
			retval.add(iv);
		}
		else {
			for (int i1=0,i1s=paramvals.length; i1 < i1s; i1++)
				retval.add(Misc.getParamAsInt(paramvals[i1]));
		}
		return retval;
	}
	private ArrayList<Integer> getControllingWorkflowDefs(int type) {
		if (workflowsUsed != null)
			return workflowsUsed;
		ArrayList<Integer> retval = WorkflowHelper.getControllingWorkflowDefs(rows,type,Misc.getUndefInt(),null);
		
		workflowsUsed = retval;
		return retval;
	}
	
	//VERIFICATION related
	//IN UI we will keep old values as hidden and then when NC is selected, edited values if any will be replaced with orig values
	//           if values changed, checkbox automatically set to Not Matching
	//     
	//Three workflows -  1.Non verification initiated - if verification value not set 2 and there are changes
	//                                   2.Verification initiated if changes and verification set to 2 . 
	//                                       Its post approval actions will be to set process status to Verified and to reinitate workflow processing	
	//                                   3.Special audit to start if process status changed to audit
	//In handleUpdate if verification status is 1 and there are no changes then process status will be set to verfied .. so that special audit workflow
	//  can be started
	//
	public  String handleUpsdateOrInsertForWeb(Connection conn, SessionManager session) throws Exception {
		
		//hidden elems are printed as hidden vars ... but when saving only hidden with mandatory are saved back 
		//(assumption being that these may be changed via script) OR if the dimConfig is marked as useTopLevelInInpTemplate
		//then we read the value from common params instead of taking it from left
		//HANDLING of v80452
		//hacks for MPL iv v80452 (verification status) explicity given and equals 3 (not checked) then row ignored
		//if v60283 (reporting status) given explicitly ... then if status change not allowed then row ignored
		long ts1 = System.currentTimeMillis();
		long ts2 = ts1;
		long tsTotSave = 0;
		System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" started:"+ts1);
		
		StringBuilder errMsg = new StringBuilder();
		TableObjectInfo tablesFor =WorkflowHelper.g_tablesForObjectId.get(objectType);
		boolean isAudit = "1".equalsIgnoreCase(session.getParameter("for_audit"));
		String baseTable = tablesFor.getName();
		String updatedByColName = "updated_by";
		if (tablesFor.getId() == WorkflowHelper.G_OBJ_TPRECORD)
			updatedByColName = "user_by";
		String objectIdLabel = tablesFor.getParamName();
		String primaryCol = tablesFor.getPrimaryIdCol();
		ArrayList<String> secondaryTables = tablesFor.getExtendedTables();
		int workflowType = WorkflowDef.WORKFLOW_TYPE_REG;//type 0-regular,1-special,2-both
		boolean isAuditSuccess = true;
		if("1".equalsIgnoreCase(session.getParameter("special_work_flow")))
			workflowType = WorkflowDef.WORKFLOW_TYPE_SPECIAL;
		//check if multiRow ... 
		String xmlDataString = session == null ? null : session.getParameter("XML_DATA");
		System.out.println("xmlDataString :" +xmlDataString);
		Document xmlDoc = xmlDataString != null && xmlDataString.length() != 0 ? MyXMLHelper.loadFromString(xmlDataString) : null;
		Element topElem = xmlDoc == null ? null : xmlDoc.getDocumentElement();
		ArrayList<Element> objectXmlList = new ArrayList<Element>();
		ArrayList<Integer> objectIds = new ArrayList<Integer> ();
		for (Node n=topElem == null ? null : topElem.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element) n;
			int id = Misc.getParamAsInt(e.getAttribute(objectIdLabel));
			objectIds.add(id);
			objectXmlList.add((Element) n);
		}
		
		boolean doingXml = objectXmlList != null && objectXmlList.size() > 0;
		if (!doingXml) {
			int id = Misc.getParamAsInt(session.getParameter(objectIdLabel));
			objectIds.add(id);
		}
		
		
		ts2 = System.currentTimeMillis();
		System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" got XML (ms):+"+(ts2-ts1));
		ts1=ts2;
		ArrayList<Integer> workflowTypeIds = this.getControllingWorkflowDefs(workflowType);
		ArrayList<ArrayList<Integer>> workflowIds = WorkflowHelper.getActiveWorkflowsFor(conn, objectIds, workflowTypeIds);
		ts2 = System.currentTimeMillis();
		System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" got active workflow (ms):+"+(ts2-ts1));
		ts1=ts2;
		long tsSaveStart = ts1;
		ArrayList<Integer> objectIdsWithDimEdited = null;
		
		int specialTPRecordProcessing = Misc.getParamAsInt(session.getParameter("v94609"));
		if (Misc.isUndef(specialTPRecordProcessing) && commonRows != null) {
			DimConfigInfo dci = InputTemplate.getDimConfigInfoInList(commonRows, 94609, null);
			if (dci != null)
				specialTPRecordProcessing = Misc.getParamAsInt(getCleanedString(dci.m_default));
		}
		if (specialTPRecordProcessing > 0) {
			if (specialTPRecordProcessing == 1) {
				TPRManageAddnl.handleEnableForNextWt(conn, objectIds);
			}
			else {
				TPRManageAddnl.handleMakeMeLatest(conn, objectIds);
			}
		}
		else {
			ArrayList<TPRStatusInfo> tprLikeStatusInfo = this.objectType == WorkflowHelper.G_OBJ_TPRECORD ? TPRStatusInfo.getStatusInfo(conn, objectIds) : null;
			//if tprListStatusInfo is found (currently only for TP_Record - then if locked then entire record except status and reporting status become readonly
			ts2 = System.currentTimeMillis();
			System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" got tprlikestatus (ms):+"+(ts2-ts1));
			ts1=ts2;
			ArrayList<ArrayList<ArrayList<Integer>>> readWriteInfo = getReadWrite(conn, session, workflowIds, objectIds, workflowTypeIds, tprLikeStatusInfo);
			
				//1st dim: by ObjectId, 2nd dim by row, 3rd dim by col
				// 0-> none, 1 => read, 2 => read/write
			String portNodeIdCol = tablesFor.getPortNodeCol();
			
			//PreparedStatement psCreateObj = conn.prepareStatement("insert into "+baseTable+ "(created_on,created_by, updated_on, "+updatedByColName+","+portNodeIdCol+",status) values (now(),?,now(),?,?,1)");
			StringBuilder baseCreateStr = new StringBuilder("insert into "+baseTable+ "(created_on,created_by, updated_on, "+updatedByColName+","+portNodeIdCol+",status) values (now(),?,now(),?,?,1)");
			boolean baseTableHasRecordSrc = tablesFor.hasRecordSrc(conn);
			boolean baseTableHasRecorSrcTime = tablesFor.hasRecordSrcTime(conn);
			
			PreparedStatement psUpdatedByObj = conn.prepareStatement(" update "+baseTable+ " set updated_on=now(), "+updatedByColName+"=?"+(baseTableHasRecordSrc ? ", record_src = "+WorkflowHelper.getRecordSrcId(conn) : "")+(baseTableHasRecorSrcTime ? ", src_record_time = now() " : "")+" where "+primaryCol+"= ?");
			SimpleDateFormat withSec = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
			SimpleDateFormat withMin = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			SimpleDateFormat withDate = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
			SimpleDateFormat withStd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			
			
			ArrayList<Integer> dimEditsToCheck = new ArrayList<Integer>();
			dimEditsToCheck.add(93413);
			for (int k=0,ks=doingXml ? objectXmlList.size() : 1; k<ks;k++) {
				
				Element currRow = doingXml ? objectXmlList.get(k) : null;
				if (currRow == null)
					continue;
				boolean doingNew = Misc.isUndef(objectIds.get(k));
				String v80452 = currRow.getAttribute("v80452");//HACK
				if ("3".equals(v80452)) {//HACK ... Not Checked needs to be ignored
					objectIds.set(k, Misc.getUndefInt());
					continue;
				}
				long tsAtEachObj = ts1;
				ArrayList<Pair<String,Integer>> tablesSeen = new ArrayList<Pair<String, Integer>>(); //1st is table seen, 2nd is index in baseExt
				ArrayList<String> baseExtTableNamePerQ = new ArrayList<String>();
				ArrayList<StringBuilder> baseExtTableUpdate = new ArrayList<StringBuilder>();
				ArrayList<StringBuilder> baseExtTableInsert = new ArrayList<StringBuilder>();
				ArrayList<ArrayList<Value>> baseExtTableUpdateParam = new ArrayList<ArrayList<Value>>();
				ArrayList<StringBuilder> nestedTableInsert = new ArrayList<StringBuilder>();
				
				ArrayList<ArrayList<ArrayList<Value>>> nestedTableInsertParam = new ArrayList<ArrayList<ArrayList<Value>>>();
				
				ArrayList<StringBuilder> nestedTableDeletes = new ArrayList<StringBuilder>();
				ArrayList<ArrayList<Integer>> readWriteInfoByRowCol = readWriteInfo.get(k);
				//new for nested table with Id
				ArrayList<StringBuilder> nestedTableUpdate = new ArrayList<StringBuilder>();//allowing unique field named "id" in nested table
				//either column be named "id" or the DimInfo's m_nesteDimBehaviour is not 0
				
				ArrayList<ArrayList<Pair<Boolean, Value>>> nestedTableIdFieldValue = new ArrayList<ArrayList<Pair<Boolean,Value>>>();
				//1 for each nested table, second dim: 1st is whether exist in table, 2nd the id value
				ArrayList<String> nestedTableIdColName = new ArrayList<String>();
				ArrayList<String> nestedTableTableName = new ArrayList<String>();
				ArrayList<Integer> nestedTableIdBehaviour = new ArrayList<Integer>(); 
				boolean foundMasterRowData = false;
				boolean notFoundAllMandatory = false;			
				Value newReportingStatus = null;
				Value newLatest = null;
				ArrayList<Boolean> mandGroupSeen = new ArrayList<Boolean>();
				ArrayList<Boolean> oneOfGroupSeen = new ArrayList<Boolean>();
				boolean checkEditDimBeingEdited = false;
				for (int i=0,is=rows.size();i<is;i++) {
					ArrayList<DimConfigInfo> row = rows.get(i);
					ArrayList<Integer> readWriteInfoCols = readWriteInfoByRowCol.get(i); 
					for (int j=0,js = row.size();j<js;j++) {
						DimConfigInfo dimConfig = row.get(j);
						 
						boolean toRead = readWriteInfoCols.get(j) != 2; 
						if (!toRead) {
							if (dimConfig.m_readOnly)
								toRead = true;
						}
						if (!toRead) {
							int readControl = dimConfig.m_readSpecialControl;
							toRead =  (doingNew ? readControl == DimConfigInfo.G_READHIDE_ALWAYS || readControl == DimConfigInfo.G_READHIDE_PRECREATE 
																						:
																						readControl == DimConfigInfo.G_READHIDE_ALWAYS || readControl == DimConfigInfo.G_READHIDE_POSTCREATE
																						)
																					;
						}
						if (toRead && (!dimConfig.m_isMandatory && dimConfig.m_mandatoryGroup < 0 && (dimConfig.m_oneOfAllGrouping == null || dimConfig.m_oneOfAllGrouping.length() == 0)))
							continue;
						if (dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null) {
							ColumnMappingHelper colMap = dimConfig.m_dimCalc.m_dimInfo.m_colMap;
							if (colMap != null && colMap.table != null && colMap.column != null && !colMap.column.equalsIgnoreCase("Dummy") && !colMap.useColumnOnlyForName && !"20515".equals(dimConfig.m_dimCalc.m_dimInfo.m_subtype) && !"20516".equals(dimConfig.m_dimCalc.m_dimInfo.m_subtype)&& !"20517".equals(dimConfig.m_dimCalc.m_dimInfo.m_subtype)) {
								boolean doingBase = baseTable.equals(colMap.table);
								boolean doingExtended = false;
								String tableName = colMap.table+(isAudit?"_audit":"");;
								boolean isMandatory = dimConfig.m_isMandatory;
								int mandGroup = dimConfig.m_mandatoryGroup;
								String oneOf = dimConfig.m_oneOfAllGrouping;
								ArrayList<Integer> oneOfParsed = null;
								if (oneOf != null && oneOf.length() > 0) {
									oneOfParsed = new ArrayList<Integer>();
									Misc.convertValToVector(oneOf, oneOfParsed);
								}
								if (mandGroup >= 0) {
									for (int t1=mandGroupSeen.size(); t1<=mandGroup;t1++) {
										mandGroupSeen.add(false);
									}
								}
								for (int t1=0,t1s=oneOfParsed == null ? 0 : oneOfParsed.size(); t1<t1s;t1++) {
									int grid = oneOfParsed.get(t1);
									for (int t2=oneOfGroupSeen.size(); t2 <= grid; t2++) {
										oneOfGroupSeen.add(true);
									}
								}
								if (!doingBase) {
									for (int t1=0,t1s=secondaryTables.size(); t1<t1s;t1++) {
										if (secondaryTables.get(t1).equals(tableName)) {
											doingExtended = true;
											break;
										}
									}
								}
								boolean doingPrimary = doingExtended || doingBase;
								if (!doingPrimary) {
									boolean doingLinked = WorkflowHelper.getLinkedTableIfPossible(tablesFor, colMap.table) != null;
									if (doingLinked) //cant do changes in linked
										continue;
								}
								
								DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
								checkEditDimBeingEdited = checkEditDimBeingEdited || Misc.isInList(dimEditsToCheck, dimInfo.m_id);
								if (doingPrimary) {
									int tabIdx = -1;
									for (int t1 = 0,t1s = tablesSeen.size(); t1 < t1s; t1++) {
										if (tablesSeen.get(t1).first.equals(tableName)) {
											tabIdx = t1;
											break;
										}
									}
									if (tabIdx == -1) {
										tabIdx = tablesSeen.size();
										tablesSeen.add(new Pair<String, Integer>(tableName, tabIdx));
										baseExtTableNamePerQ.add(tableName);
										baseExtTableUpdate.add(new StringBuilder());
										baseExtTableInsert.add(new StringBuilder());
										baseExtTableUpdateParam.add(new ArrayList<Value>());
									}
									StringBuilder updStr = baseExtTableUpdate.get(tabIdx);
									StringBuilder insStr = baseExtTableInsert.get(tabIdx);
									ArrayList<Value> updParams = baseExtTableUpdateParam.get(tabIdx); 
									if(!colMap.column.equalsIgnoreCase(objectIdLabel)){//need to varify debug rahul
										if (updStr.length() == 0) {
											updStr.append("update ").append(tableName).append(" set ");
										}
										else {
											updStr.append(",");
										}
										if (insStr.length() == 0) {
											insStr.append("insert into ").append(tableName).append(" ( ");
										}
										else {
											insStr.append(",");
										}
	
										updStr.append(colMap.column).append("=? ");
										insStr.append(colMap.column);
										Pair<Value, Boolean> param = readParameter(currRow, session, dimInfo, withSec, withMin, withDate, withStd, topElem,(isAudit), this.commonRows, dimConfig.useTopLevelInInpTemplate);// && dimConfig.m_isRadio));
										if (param.second) {
											foundMasterRowData = true;
											if (dimInfo.m_id == 60283) {//HACK
												newReportingStatus = param.first; 
											}
											else if (dimInfo.m_id == 90027) {
												newLatest = param.first;
											}
											if (mandGroup >= 0)
												mandGroupSeen.set(mandGroup, true);
										}
										else {
											if (isMandatory) 
												notFoundAllMandatory = true;
											for (int t1=0,t1s=oneOfParsed == null ? 0 : oneOfParsed.size(); t1<t1s; t1++) {
												oneOfGroupSeen.set(oneOfParsed.get(t1), false);
											}
										}
										updParams.add(param.first);
									}
								}
								else {
									int secRsetIndex = 0;
									nestedTableInsert.add(new StringBuilder());
									nestedTableDeletes.add(new StringBuilder());
									nestedTableInsertParam.add(new ArrayList<ArrayList<Value>>());
									nestedTableUpdate.add(new StringBuilder());
									nestedTableIdFieldValue.add(new ArrayList<Pair<Boolean,Value>>());
									
									int sz = nestedTableInsert.size()-1;
									StringBuilder insertQ = nestedTableInsert.get(sz);
									StringBuilder delQ = nestedTableDeletes.get(sz);
									StringBuilder updQ = nestedTableUpdate.get(sz);
									ArrayList<ArrayList<Value>> paramsList =  nestedTableInsertParam.get(sz);
									ArrayList<Pair<Boolean,Value>> idParams = nestedTableIdFieldValue.get(sz); 
									//check if nestedTable has id
									int idParamIdx = -1;
									String idColName = null;
									String nestedName = null;
									int nestedBehaviour = 0;
									ArrayList<Integer> paramIndexes = new ArrayList<Integer>();
									int tempidx = -1;
									
									boolean hackIsSECLCurrAllocationChange = false;
									int hackSECLAllocFieldIndex = -1;
									int hackSECLPropLiftedFieldIndex = -1;
									
									for (int t1=0,t1s=dimConfig.m_nestedCols == null  ? 1 : dimConfig.m_nestedCols.get(0).size(); t1<t1s;t1++) {
										DimConfigInfo nestDC = dimConfig.m_nestedCols == null  ? dimConfig : dimConfig.m_nestedCols.get(0).get(t1);
										int nestDimId = nestDC.m_dimCalc != null && nestDC.m_dimCalc.m_dimInfo != null ? nestDC.m_dimCalc.m_dimInfo.m_id : Misc.getUndefInt();
										paramIndexes.add(-1);
										if (Misc.isUndef(nestDimId))
											continue;
										DimInfo nestDimInfo = DimInfo.getDimInfo(nestDimId);
										ColumnMappingHelper nestColMap = nestDimInfo.m_colMap;
										if (nestColMap == null || nestColMap.table.equalsIgnoreCase("Dummy") || nestColMap.useColumnOnlyForName)
											continue;
										if (!hackIsSECLCurrAllocationChange && "current_do_status".equals(nestColMap.table)) {
											hackIsSECLCurrAllocationChange = true;
										}
										
										
										if ("id".equals(nestColMap.column)) {
											idParamIdx = t1;
											idColName = "id";
											nestedBehaviour = 1;
											nestedName = nestColMap.table;
										}
										else if (nestDimInfo.m_nestedDimIdBehaviour != 0) {
											idParamIdx = t1;
											idColName = nestColMap.column;
											nestedBehaviour = nestDimInfo.m_nestedDimIdBehaviour;
											nestedName = nestColMap.table;
										}
										else {
											tempidx++;
											paramIndexes.set(t1,tempidx);
											if (hackIsSECLCurrAllocationChange) {
												if ("current_allocation".equals(nestColMap.column)) {
													hackSECLAllocFieldIndex = tempidx;
												}
												else if ("proposed_lifted".equals(nestColMap.column)) {
													hackSECLPropLiftedFieldIndex = tempidx;
												}
											}
										}
									}
									nestedTableIdBehaviour.add(nestedBehaviour);
									nestedTableIdColName.add(idColName);
									nestedTableTableName.add(nestedName);
									delQ.append("delete from ").append(tableName);
									
									
									Element tabElem = null;
									ArrayList<Element> subRows = new ArrayList<Element>();
									if (currRow != null) {
										tabElem = MyXMLHelper.getChildElementByTagName(currRow, "d"+dimInfo.m_id);
									}
									if (tabElem == null) {
										String val = session.getParameter("XML_DATA_TABLE_d"+dimInfo.m_id);
										if (val != null)
											val = val.trim();
										if (val != null && val.length() != 0) {
											Document doct = MyXMLHelper.loadFromString(val);
											if (doct != null) {
												tabElem = doct.getDocumentElement();
											}
										}
									}
									if (tabElem != null) {
										for (Node cn = tabElem.getFirstChild(); cn != null; cn = cn.getNextSibling()) {
											if (cn.getNodeType() != 1)
												continue;
											Element ce = (Element) cn;
											subRows.add(ce);
										}
									}
									for (int i2=0,i2s = subRows.size() == 0 ? 1 : subRows.size(); i2 < i2s; i2++) {
										ArrayList<Value> params = new ArrayList<Value>();
										Element subRow = i2 != subRows.size() ? subRows.get(i2) : null;;
										boolean foundValidParam = false;
										
										for (int t1=0,t1s=dimConfig.m_nestedCols == null  ? 1 : dimConfig.m_nestedCols.get(0).size(); t1<t1s;t1++) {
											DimConfigInfo nestDC = dimConfig.m_nestedCols == null  ? dimConfig : dimConfig.m_nestedCols.get(0).get(t1);
											int nestDimId = nestDC.m_dimCalc != null && nestDC.m_dimCalc.m_dimInfo != null ? nestDC.m_dimCalc.m_dimInfo.m_id : Misc.getUndefInt();
											if (Misc.isUndef(nestDimId))
												continue;
											DimInfo nestDimInfo = DimInfo.getDimInfo(nestDimId);
											ColumnMappingHelper nestColMap = nestDimInfo.m_colMap;
											if (nestColMap == null || nestColMap.table.equalsIgnoreCase("Dummy"))
												continue;
											
											String nestedTableName = nestColMap.table+(isAudit?"_audit":"");
											if (nestColMap.useColumnOnlyForName)
												continue;
											if (t1 != idParamIdx) {
												if (i2 == 0) {
													if (insertQ.length() == 0) {
														insertQ.append("insert into ").append(nestedTableName).append("(");
													}
													else {
														insertQ.append(",");
													}
													insertQ.append(nestColMap.column);
												}
												if (idParamIdx >= 0) {
													if (i2 == 0) {
														if (updQ.length() == 0) {
															updQ.append("update ").append(nestedTableName).append(" set ");
														}
														else {
															updQ.append(",");
														}
														updQ.append(nestColMap.column).append(" = ? ");
													}	
												}
											}
											Pair<Value, Boolean> param = readParameter(subRow, session, nestDimInfo, withSec, withMin, withDate, withStd, topElem,isAudit, this.commonRows, nestDC.useTopLevelInInpTemplate);
											if (t1 == idParamIdx) {
												idParams.add(new Pair<Boolean,Value>(false,param.first));
											}
											else {
												params.add(param.first);
											}
										}//for each nested col
										if (InputTemplate.checkIfMandatoryThereNested(dimConfig, params, paramIndexes)) {
											paramsList.add(params);
											foundValidParam = true;
											foundMasterRowData = true;
										}
										else { //we greedily added
											if (idParamIdx >= 0)
												idParams.remove(idParams.size()-1);
										}
									}//for each data sub row
									if (hackIsSECLCurrAllocationChange && (hackSECLAllocFieldIndex >= 0 || hackSECLPropLiftedFieldIndex >= 0)) {
										notFoundAllMandatory = hackValidateSECLAlloc(conn, objectIds.get(k), idParams, paramsList, hackSECLAllocFieldIndex, hackSECLPropLiftedFieldIndex, errMsg);
										
									} 
								}//if doing nested
							}//if valid columnMapping helper mapped 
						}//if proper dimConfig				
					}//for each ui col
				}	//for each ui row
				TPRStatusInfo tprStatusInfo = tprLikeStatusInfo != null ? tprLikeStatusInfo.get(k) : null;
				if (!notFoundAllMandatory && !TPRManageAddnl.isDataChangeAllowed(conn, session, objectIds.get(k), newReportingStatus, newLatest, tprStatusInfo, errMsg)) {
					notFoundAllMandatory = true;
				}
			
				if (!notFoundAllMandatory) {//	check all mandatoryGroups given
					for (int t1=0,t1s = mandGroupSeen.size();t1<t1s;t1++) {
						if (!mandGroupSeen.get(t1)) {
							notFoundAllMandatory = true;
							break;
						}
					}
				}
				if (!notFoundAllMandatory && oneOfGroupSeen.size() > 0) {//	check all mandatoryGroups given
					boolean seenT = false;
					for (int t1=0,t1s = oneOfGroupSeen.size();t1<t1s;t1++) {
						if (oneOfGroupSeen.get(t1)) {
							seenT = true;
							break;
						}
					}
					if (!seenT)
						notFoundAllMandatory = true;
				}
				if (foundMasterRowData && !notFoundAllMandatory) {
					int currId = objectIds.get(k);
					boolean isNew = false;
					
					if (Misc.isUndef(currId)) {
						int pv123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
						int baseIdx = -1;
						for (int x1=0,x1s=baseExtTableNamePerQ.size();x1<x1s;x1++) {
							if (baseTable.equals(baseExtTableNamePerQ.get(x1))) { 
								baseCreateStr = baseExtTableInsert.get(x1);
								baseIdx = x1;
								break;
							}
						}
						ArrayList<Value> vals = baseIdx != -1 ? baseExtTableUpdateParam.get(baseIdx) : null;
						if (vals != null)
							baseCreateStr.append(",");
						baseCreateStr.append("created_on, created_by, updated_on,").append(updatedByColName)
						;
						boolean qhasport = baseCreateStr.indexOf(portNodeIdCol) >= 0;
						boolean qhasstatus = baseCreateStr.indexOf("status") >= 0;
						if (!qhasport) {
							baseCreateStr.append(",").append(portNodeIdCol);
						}
						if (!qhasstatus) {
							baseCreateStr.append(",").append("status");
						}
						if (baseTableHasRecordSrc) {
							baseCreateStr.append(",record_src");
						}
						if (baseTableHasRecorSrcTime) {
							baseCreateStr.append(",src_record_time");
						}
						baseCreateStr.append(") values (");
						//StringBuilder baseCreateStr = new StringBuilder("insert into "+baseTable+ "(created_on,created_by, updated_on, "+updatedByColName+","+portNodeIdCol+",status) values (now(),?,now(),?,?,1)");
						
						if (baseIdx != -1) {
							for (int x1=0,x1s=vals.size(); x1<x1s; x1++) {
								if (x1 != 0)
									baseCreateStr.append(",");
								baseCreateStr.append("?");
							}
						}
						
						
						if (vals != null)
							baseCreateStr.append(",");
						
						baseCreateStr.append("now(),?,now(),?");
						if (!qhasport)
							baseCreateStr.append(",?");
						if (!qhasstatus)
							baseCreateStr.append(",1");
						if (baseTableHasRecordSrc) {
							baseCreateStr.append(",").append(WorkflowHelper.getRecordSrcId(conn));
						}
						if (baseTableHasRecorSrcTime) {
							baseCreateStr.append(",now()");
						}

						baseCreateStr.append(")");
						if (vals == null)
							vals = new ArrayList<Value>();
						vals.add(new Value((int)session.getUserId()));
						vals.add(new Value((int)session.getUserId()));
						if (!qhasport) 
							vals.add(new Value((int)pv123));
						PreparedStatement ps = conn.prepareStatement(baseCreateStr.toString());
						WorkflowHelper.putParams(ps, vals);
						ps.execute();
						ResultSet rs = ps.getGeneratedKeys();
						if (rs.next())
							currId = rs.getInt(1);
						rs.close();
						ps.close();
						objectIds.set(k, currId);
						isNew = true;
					}
					else {
						Misc.setParamInt(psUpdatedByObj,(int)session.getUserId(), 1);
						Misc.setParamInt(psUpdatedByObj, currId,2);
						psUpdatedByObj.execute();
					}
					if (checkEditDimBeingEdited) {
						if (objectIdsWithDimEdited == null)
							objectIdsWithDimEdited = new ArrayList<Integer>();
						objectIdsWithDimEdited.add(currId);
					}
						
					ts2 = System.currentTimeMillis();
					System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" saved base row "+currId+"(ms):"+(ts2-ts1));
					ts1=ts2;
					Value currIdValue = new Value(currId);
					//for inserting extended table data
					for (int t1=0,t1s=tablesSeen.size(); t1 < t1s; t1++) {
						if(tablesSeen.get(t1).first == null || tablesSeen.get(t1).first.length() == 0 || tablesSeen.get(t1).first.equals(baseTable) )
							continue;
						StringBuilder q = new StringBuilder("insert ignore into ").append(tablesSeen.get(t1).first)
								.append(" (").append(objectIdLabel).append(") values (?)");//baseExtTableUpdate.get(t1);
						PreparedStatement ps = conn.prepareStatement(q.toString());
						ArrayList<Value> params = new ArrayList<Value>();
						params.add(currIdValue);
						WorkflowHelper.putParams(ps, params);
						ps.execute();
						ps.close();
						ts2 = System.currentTimeMillis();
						System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" ins ext table  "+tablesSeen.get(t1).first+" ObjectId:"+currId+"(ms):"+(ts2-ts1));
						ts1=ts2;
					}
					for (int t1=0,t1s=tablesSeen.size(); t1 < t1s; t1++) {
						if(baseExtTableUpdate.get(t1) == null || baseExtTableUpdate.get(t1).length() == 0 )
							continue;
						if (isNew && baseTable.equals(baseExtTableNamePerQ.get(t1)))
							continue;
						StringBuilder q = baseExtTableUpdate.get(t1);
						q.append(" where ").append(tablesSeen.get(t1).first.equals(baseTable) ? primaryCol : objectIdLabel).append(" = ? ");
						PreparedStatement ps = conn.prepareStatement(q.toString());
						ArrayList<Value> params = baseExtTableUpdateParam.get(t1);
						params.add(currIdValue);
						WorkflowHelper.putParams(ps, params);
						ps.execute();
						ps.close();
						ts2 = System.currentTimeMillis();
						System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" upd ext table  "+tablesSeen.get(t1).first+" ObjectId:"+currId+"(ms):"+(ts2-ts1));
						ts1=ts2;
						if(isAudit){
							if(params != null){
								for(Value v : params){
									if(v.getIntVal() != 1){
										isAuditSuccess = false;
										break;
									}
								}
							}
						}
					}
					if (!isNew) {
						for (int t1=0,t1s=nestedTableIdBehaviour .size(); t1 < t1s; t1++) {
							//check which are already existing
							if (nestedTableIdBehaviour.get(t1) != 0) {
								PreparedStatement ps = conn.prepareStatement("select "+nestedTableIdColName .get(t1)+" from "+nestedTableTableName.get(t1)+" where "+objectIdLabel+" = ?");
								ps.setInt(1,currId );
								HashMap<Integer, Integer> valsSeen = new HashMap<Integer, Integer>();
								ResultSet rs = ps.executeQuery();
								while (rs.next()) {
									valsSeen.put(Misc.getRsetInt(rs, 1), 1);
								}
								Misc.closeResultSet(rs);
								Misc.closePS(ps);
								ArrayList<Pair<Boolean, Value>> idParamList = nestedTableIdFieldValue .get(t1);
								for (int t2=0,t2s=idParamList == null ? 0 : idParamList.size(); t2<t2s; t2++) {
									Value vv = idParamList.get(t2).second;
									
									if (vv != null && vv.isNotNull() && valsSeen.containsKey(vv.getIntVal())) {
										idParamList.get(t2).first = true;
									}
								}
							}
						}
						//now delete those that are not being provided
						for (int t1=0,t1s=nestedTableIdBehaviour .size(); t1 < t1s; t1++) {
							boolean hasIdParam = nestedTableIdBehaviour .get(t1) != 0;
							StringBuilder q = nestedTableDeletes.get(t1);
							q.append(" where ").append(objectIdLabel).append(" = ? ");
							if (hasIdParam) {
								//delete only those that are not in the list
								ArrayList<Pair<Boolean,Value>> idparams = nestedTableIdFieldValue.get(t1);
								boolean firstAdded = false;
								for (int t2=0,t2s=idparams == null ? 0 : idparams.size(); t2<t2s; t2++) {
									Value v = idparams.get(t2).second;
									if (v != null && v.isNotNull()) {
										if (!firstAdded) 
											q.append(" and ").append(nestedTableIdColName.get(t1)).append(" not in (");
										else
											q.append(",");
										firstAdded = true;
										q.append(v.getIntVal());
									}
								}
								if (firstAdded)
									q.append(")");
							}
							PreparedStatement ps = conn.prepareStatement(q.toString());
							ps.setInt(1, currId);
							ps.execute();
							ps.close();
							ts2 = System.currentTimeMillis();
							System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" del nested table  "+q+" ObjectId:"+currId+"(ms):"+(ts2-ts1));
							ts1=ts2;
						}
					}
					for (int t1=0,t1s=nestedTableIdBehaviour .size(); t1<t1s; t1++) {
						ArrayList<ArrayList<Value>> paramsList =   nestedTableInsertParam.get(t1);
						if (paramsList.size() == 0)
							continue;
						ArrayList<Pair<Boolean, Value>> idparams = nestedTableIdFieldValue.get(t1);
						StringBuilder insQ = nestedTableInsert.get(t1);
						StringBuilder updQ = nestedTableUpdate.get(t1);
						int nestedBehaviour = nestedTableIdBehaviour.get(t1);
						insQ.append(",").append(objectIdLabel);
						if (nestedBehaviour == 2) {
							insQ.append(",").append(nestedTableIdColName.get(t1));
						}
						insQ.append(") values (");
						if (nestedBehaviour > 0) {
							updQ.append(" where ").append(objectIdLabel).append(" = ?").append(" and ").append(nestedTableIdColName.get(t1)).append(" = ?");
						}
						
						for (int t2=0,t2s=paramsList.get(0).size()+1; t2 < t2s; t2++) {
							if (t2 != 0)
								insQ.append(",");
							insQ.append("?");
						}
						if (nestedBehaviour == 2) {
							insQ.append(",?");
						}
						insQ.append(")");
						PreparedStatement ps = conn.prepareStatement(insQ.toString());
						PreparedStatement updPs = nestedBehaviour != 0 ? conn.prepareStatement(updQ.toString()) : null;
						boolean hasUpd = false;
						boolean hasIns = false;
						for (int t2=0, t2s=paramsList.size(); t2<t2s;t2++) {
							Pair<Boolean, Value> idVal = nestedBehaviour != 0 ? idparams.get(t2) : null;
							paramsList.get(t2).add(currIdValue);
							
							if (nestedBehaviour == 0) {
								WorkflowHelper.putParams(ps, paramsList.get(t2));
								hasIns = true;
								ps.addBatch();
							}
							else {
								if (nestedBehaviour == 2 && (idVal.second == null || idVal.second.isNull()))
									continue;
								paramsList.get(t2).add(idVal.second);
								if (idVal.first) {
									WorkflowHelper.putParams(updPs, paramsList.get(t2));
									hasUpd = true;
									updPs.addBatch();
								}
								else {
									WorkflowHelper.putParams(ps, paramsList.get(t2));
									hasIns = true;
									ps.addBatch();
								}
							}
						}
						if (hasIns) {
							System.out.println(ps);
							ps.executeBatch();
						}
						if (hasUpd) {
							System.out.println(updPs);
							updPs.executeBatch();
						}
						updPs = Misc.closePS(updPs);
						ps = Misc.closePS(ps);
						ts2 = System.currentTimeMillis();
						System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" ins/upd nested table  "+insQ+" ObjectId:"+currId+"(ms):"+(ts2-ts1));
						ts1=ts2;
	
					}//nested table
					postProcess(conn,session, currId, isNew, (int) session.getUserId(), newReportingStatus, tprStatusInfo);
					ts2 = System.currentTimeMillis();
					System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" postProcess  "+currId+"(ms):"+(ts2-ts1));
					ts1=ts2;
	
					if (!conn.getAutoCommit())
						conn.commit();
					
				}//if valid row
				else {
					objectIds.set(k, Misc.getUndefInt());
					errMsg.append("Error-Row("+(k+1)+") not having mandatory fields<br>");
				}
				ts2 = System.currentTimeMillis();
				System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" Each obj save "+objectIds.get(k)+" (ms)"+(ts2-tsAtEachObj));
				ts1=ts2;
			}//for each data row
			psUpdatedByObj.close();
		}//end of not doing specialTPRecordProcessing
		ts2 = System.currentTimeMillis();
		System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" All obj save (ms)"+(ts2-tsSaveStart));
		ts1=ts2;
		WorkflowHelper.QueryHelperNew queryHelper = WorkflowHelper.helpGetQueryHelperNew(this.objectType, rows);
		int userId = (int)session.getUserId();
		String userComment = session.getParameter("user_comments");
		WorkflowHelper.doWorkflowCreateUpdateEtc(conn, session, objectIds, objectType, rows, true, workflowTypeIds, workflowIds, queryHelper, userId, userComment,workflowType);
		ts2 = System.currentTimeMillis();
		System.out.println("[INPT Save] Thread:"+Thread.currentThread().getId()+" workflowCreateUpdate  (ms)"+(ts2-ts1));
		ts1=ts2;

		if (tablesFor != null && !Misc.isUndef(tablesFor.getRefDynDimId())) {
			DimInfo refDim = DimInfo.getDimInfo(tablesFor.getRefDynDimId());
			if (refDim != null)
				refDim.makeDirty();
		}
		if(tablesFor != null && tablesFor.getAddnlRefDynDimId()!=null) {
			for (Iterator iterator = tablesFor.getAddnlRefDynDimId().iterator(); iterator.hasNext();) {
				DimInfo refDim = DimInfo.getDimInfo((Integer) iterator.next());
				if (refDim != null)
					refDim.makeDirty();
			}
		}
		
		//special processing for lock status being updated
		if (objectIdsWithDimEdited != null && objectIdsWithDimEdited.size() > 0) {
			ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> dpUpdList = DOUpdateHelper.getDOUpdateInfoOnUpdOfLock(conn,objectIdsWithDimEdited);
			if (dpUpdList != null)
				DOUpdInfo.sendToWB(conn, dpUpdList, true);
			//DOUpdateHelper.saveCommunicationResult(dpUpdList, conn);
			ArrayList<DOUpdInfo> doUpdErrs = null;
			for (int t4=0,t4s = dpUpdList == null ? 0 : dpUpdList.size(); t4 < t4s; t4++) {
				ArrayList<DOUpdInfo> toupd = dpUpdList.get(t4).second;
				for (int t5=0, t5s=toupd == null ? 0 : toupd.size(); t5 < t5s; t5++) {
					if (toupd.get(t5).getResultStatus() != 0) {
						if (doUpdErrs == null)
							doUpdErrs = new ArrayList<DOUpdInfo>();
						doUpdErrs.add(toupd.get(t5));
					}
				}
			}
			if (doUpdErrs != null && doUpdErrs.size() > 0)
				session.setAttributeObj("_doUpdErr", doUpdErrs);
		}

		return  (errMsg == null || errMsg.length() == 0 ? null : errMsg.toString());
	}

	
	public ArrayList<WorkflowInfoAskedByUser> prepForApprovalRejectOnWeb(Connection conn, SessionManager session) throws Exception {
		TableObjectInfo tablesFor =WorkflowHelper.g_tablesForObjectId.get(objectType);
		String[] workflowIdParams = session.request.getParameterValues("workflow_id");
		if (workflowIdParams == null || workflowIdParams.length == 0)
			workflowIdParams = session.request.getParameterValues("v415");
		if (workflowIdParams == null || workflowIdParams.length == 0)
			workflowIdParams = session.request.getParameterValues("v418");
		ArrayList<WorkflowInfoAskedByUser> workflowIds = new ArrayList<WorkflowInfoAskedByUser>();
		boolean doingApproval = session.getParameter("do_approve") != null;
		boolean doingReject = !doingApproval && session.getParameter("do_reject") != null;
		
		StringBuilder errMsg = new StringBuilder();
		for (int i=0,is=workflowIdParams == null ? 0 : workflowIdParams.length; i<is; i++) {
			int workflowId = Misc.getParamAsInt(workflowIdParams[i]);
			if (Misc.isUndef(workflowId))
				continue;
			String comment = getCleanedString(session.request.getParameter("v90119"));
			workflowIds.add(new WorkflowInfoAskedByUser(workflowId,doingApproval, comment ));
		}
		WorkflowInfoAskedByUser.updateWorkflowInfoAskedFromWorkflowId(conn, workflowIds);
		
		return  workflowIds;
	}

	public boolean checkIfSpecialHandlingForFinalApproveDOAllocation(Connection conn, ArrayList<WorkflowInfoAskedByUser> workflowItems) throws Exception {
		boolean retval = false;
		WorkflowDef approvingWorkflow = WorkflowDef.getWorkflowById(conn, 52, false);
		if (approvingWorkflow == null)
			return false;
		for (int i=0,is=workflowItems == null ? 0 : workflowItems.size(); i<is; i++) {
			WorkflowInfoAskedByUser workflowItem = workflowItems.get(i);
			if (workflowItem == null)
				continue;
			if (workflowItem.getWkfType() == approvingWorkflow.getId() && workflowItem.isDoApprove()) {
				//may be 
				if (workflowItem.isForce()) {
					retval = true;
					workflowItem.setForFinalAtDOApproval(true);
				}
				else {//check if it is the last stage in approval
					Pair<Boolean, Integer> isLastCurrLevel = approvingWorkflow.getIfLastApprovalAndLevel(conn, workflowItem.getWorkflowId());
					if (isLastCurrLevel.first) {
						retval = true;
						workflowItem.setForFinalAtDOApproval(true);
					}
				}
			}
		}
		return retval;
	}
	
	public String handleApproveRejectForInpTemplateGeneral(Connection conn, SessionManager session) throws Exception {
		ArrayList<WorkflowInfoAskedByUser> workflowItems = prepForApprovalRejectOnWeb(conn, session);
		
		return handleApproveRejectForInpTemplateGeneral(conn, workflowItems, session);
	}
	
	public String handleApproveRejectForInpTemplateGeneral(Connection conn, ArrayList<WorkflowInfoAskedByUser> workflowItems, SessionManager session) throws Exception {
		WorkflowDef wdef = null;
		ArrayList<DOUpdInfo> updsWithError = new ArrayList<DOUpdInfo>();
		ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> doUpdInfoToSendRemote = null;
		
		int userId = (int) session.getUserId();
		for (int i=0,is=workflowItems == null ? 0 : workflowItems.size(); i<is; i++) {
			WorkflowInfoAskedByUser workflowItem = workflowItems.get(i);
			if (workflowItem == null)
				continue;
			if (wdef == null || wdef.getId() != workflowItem.getWkfType())
				wdef = WorkflowDef.getWorkflowById(conn, workflowItem.getWkfType(), false);
			if (wdef == null || workflowItem.getStillPending() != 1 || (workflowItem.getApprvUserConf() != userId && !workflowItem.isForce()) || Misc.isUndef(workflowItem.getObjectId()))
				continue;
			if (workflowItem.isDoApprove()) {
				ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> doUpdInfoIfNeeded = null;
				if (workflowItem.getWkfType() == 52) {
					if (workflowItem.isForce() || wdef.getIfLastApprovalAndLevel(conn, workflowItem.getWorkflowId()).first) {
						doUpdInfoIfNeeded = DOUpdateHelper.prepDORRAllocationForFinApproval(conn, workflowItem.getObjectId(), session.getUser().getUserId());
						
					}
					
				}					
				TPRChanges tprChanges = null;
				if (wdef.getId() == 53 || wdef.getId() == 54) {
					tprChanges = TPRChanges.getTPRChanges(conn, workflowItem.getObjectId(), null, wdef.getId() == 54);
				}
				wdef.approveWorkflow(conn, session, workflowItem.getWorkflowId(), workflowItem.getObjectId(), (int)userId, workflowItem.getComments(), workflowItem.isForce());
				if (tprChanges != null) {
					tprChanges.applyDOLocal(conn);
				}
				if (!conn.getAutoCommit())
					conn.commit();
				if (doUpdInfoIfNeeded != null && doUpdInfoIfNeeded.size() > 0) {
					doUpdInfoToSendRemote = DOUpdateHelper.mergeDoUpdInfo(doUpdInfoIfNeeded, doUpdInfoToSendRemote);
				}
				if (tprChanges != null) {
					boolean doingAtServer = Misc.getRecordSrcId(conn) == Integer.MAX_VALUE;
					tprChanges.applyRemote(conn, doingAtServer);
				}
			}
			else {
				wdef.rejectWorkflow(conn, session, workflowItem.getWorkflowId(), workflowItem.getObjectId(), (int) userId, workflowItem.getComments());
			}
			if (!conn.getAutoCommit())
				conn.commit();
		}
		if (doUpdInfoToSendRemote != null && doUpdInfoToSendRemote.size() > 0) {
   		    DOUpdInfo.sendToWB(conn, doUpdInfoToSendRemote, false);
   		   // DOUpdateHelper.saveCommunicationResult(doUpdInfoToSendRemote, conn);
			DOUpdateHelper.rememberErroredDoUpdInfo(doUpdInfoToSendRemote, updsWithError);
			session.setAttributeObj("_doUpdErr", updsWithError);
		}
		return  null;
	}

	public String handleApproveRejectForInpTemplateVerificationNotUsed(Connection conn, SessionManager session) throws Exception {
		TableObjectInfo tablesFor =WorkflowHelper.g_tablesForObjectId.get(objectType);
		//check if multiRow ... 
		String xmlDataString = session == null ? null : session.getParameter("XML_DATA");
		Document xmlDoc = xmlDataString != null && xmlDataString.length() != 0 ? MyXMLHelper.loadFromString(xmlDataString) : null;
		Element topElem = xmlDoc == null ? null : xmlDoc.getDocumentElement();
		ArrayList<Element> objectXmlList = new ArrayList<Element>();
		ArrayList<Triple<Integer, Boolean, String>> workflowIds = new ArrayList<Triple<Integer, Boolean, String>> ();
		StringBuilder errMsg = new StringBuilder();
		for (Node n=topElem == null ? null : topElem.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element) n;
			int approvalId = Misc.getParamAsInt(e.getAttribute("v90117"));
			boolean doingApproval  = !Misc.isUndef(approvalId);
			if (!doingApproval) {
				approvalId = Misc.getParamAsInt(e.getAttribute("v90118"));
			}
			if (Misc.isUndef(approvalId))
				continue;
			String comment = getCleanedString(e.getAttribute("v90119"));
			if (comment == null) {
				comment = getCleanedString(topElem.getAttribute("v90119"));
			}
			workflowIds.add(new Triple<Integer, Boolean, String>(approvalId, doingApproval, comment));
		}
		PreparedStatement ps = conn.prepareStatement("select workflow_type_id,status,pending_approval_of,object_id from workflows where id=?");
		WorkflowDef wdef = null;
		for (int i=0,is=workflowIds == null ? 0: workflowIds.size(); i<is; i++) {
			Triple<Integer, Boolean, String> workflowId = workflowIds.get(i);
			ps.setInt(1, workflowId.first);
			ResultSet rs = ps.executeQuery();
			int wkfType = Misc.getUndefInt();
			int apprvUserConf = Misc.getUndefInt();
			int stillPending = Misc.getUndefInt();
			int objectId = Misc.getUndefInt();
			if (rs.next()) {
				wkfType = Misc.getRsetInt(rs, 1);
				stillPending = Misc.getRsetInt(rs, 2);
				apprvUserConf = Misc.getRsetInt(rs,3);
				objectId = Misc.getRsetInt(rs, 4);
			}
			rs = Misc.closeRS(rs);
			if (wdef == null || wdef.getId() != wkfType)
				wdef = WorkflowDef.getWorkflowById(conn, wkfType, false);
			if (wdef == null || stillPending != 1 || apprvUserConf != session.getUserId() || Misc.isUndef(objectId))
				continue;
			if (workflowId.second) {
				wdef.approveWorkflow(conn, session, workflowId.first, objectId, (int)session.getUserId(), workflowId.third);
			}
			else {
				wdef.rejectWorkflow(conn, session, workflowId.first, objectId, (int) session.getUserId(), workflowId.third);
			}
			if (!conn.getAutoCommit())
				conn.commit();

		}
		ps = Misc.closePS(ps);
		return  (errMsg == null || errMsg.length() == 0 ? null : errMsg.toString());
	}

	private static String UPD_INOUT_IF_MISSING = "update tp_record set "+
	" earliest_unload_gate_in_in = (case when earliest_unload_gate_in_in is not null then earliest_unload_gate_in_in else latest_unload_gate_in_out end) "+
	" ,latest_unload_gate_in_out = (case when latest_unload_gate_in_out is not null then latest_unload_gate_in_out else earliest_unload_gate_in_in end) "+
	" ,earliest_unload_wb_in_in = (case when earliest_unload_wb_in_in is not null then earliest_unload_wb_in_in else latest_unload_wb_in_out end) "+
	" ,latest_unload_wb_in_out = (case when latest_unload_wb_in_out is not null then latest_unload_wb_in_out else earliest_unload_wb_in_in end) "+
	" ,earliest_unload_yard_in_in = (case when earliest_unload_yard_in_in is not null then earliest_unload_yard_in_in else latest_unload_yard_in_out end) "+
	" ,latest_unload_yard_in_out = (case when latest_unload_yard_in_out is not null then latest_unload_yard_in_out else earliest_unload_yard_in_in end) "+
	" ,earliest_unload_wb_out_in = (case when earliest_unload_wb_out_in is not null then earliest_unload_wb_out_in else latest_unload_wb_out_out end) "+
	" ,latest_unload_wb_out_out = (case when latest_unload_wb_out_out is not null then latest_unload_wb_out_out else earliest_unload_wb_out_in end) "+
	" ,earliest_unload_gate_out_in = (case when earliest_unload_gate_out_in is not null then earliest_unload_gate_out_in else latest_unload_gate_out_out end) "+
	" ,latest_unload_gate_out_out = (case when latest_unload_gate_out_out is not null then latest_unload_gate_out_out else earliest_unload_gate_out_in end) "+
	" ,earliest_reg_in = (case when earliest_reg_in is not null then earliest_reg_in else latest_reg_out end) "+
	" ,latest_reg_out = (case when latest_reg_out is not null then latest_reg_out else earliest_reg_in end) "+
	" ,earliest_load_gate_in_in = (case when earliest_load_gate_in_in is not null then earliest_load_gate_in_in else latest_load_gate_in_out end) "+
	" ,latest_load_gate_in_out = (case when latest_load_gate_in_out is not null then latest_load_gate_in_out else earliest_load_gate_in_in end) "+
	" ,earliest_load_wb_in_in = (case when earliest_load_wb_in_in is not null then earliest_load_wb_in_in else latest_load_wb_in_out end) "+
	" ,latest_load_wb_in_out = (case when latest_load_wb_in_out is not null then latest_load_wb_in_out else earliest_load_wb_in_in end) "+
	" ,earliest_load_yard_in_in = (case when earliest_load_yard_in_in is not null then earliest_load_yard_in_in else latest_load_yard_in_out end) "+
	" ,latest_load_yard_in_out = (case when latest_load_yard_in_out is not null then latest_load_yard_in_out else earliest_load_yard_in_in end) "+
	" ,earliest_load_wb_out_in = (case when earliest_load_wb_out_in is not null then earliest_load_wb_out_in else latest_load_wb_out_out end) "+
	" ,latest_load_wb_out_out = (case when latest_load_wb_out_out is not null then latest_load_wb_out_out else earliest_load_wb_out_in end) "+
	" ,earliest_load_gate_out_in = (case when earliest_load_gate_out_in is not null then earliest_load_gate_out_in else latest_load_gate_out_out end) "+
	" ,latest_load_gate_out_out = (case when latest_load_gate_out_out is not null then latest_load_gate_out_out else earliest_load_gate_out_in end) "+
	",tpr_status = (case when latest_unload_gate_out_out is not null or latest_load_gate_out_out is not null then 2 else tpr_status end) "+
	" where tpr_id=? ";
	
	
	private static String UPD_TPR_DATES = "update tp_record set "+
	" tpr_create_date = (case when tpr_create_date and created_on is null then now() when tpr_create_date is null then created_on else tpr_create_date end) "+
	", combo_start = (case when material_cat is null or material_cat = 0 then "+
	" least(coalesce(earliest_unload_gate_in_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_unload_gate_in_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_reg_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_reg_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_unload_wb_in_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_unload_wb_in_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_unload_yard_in_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_unload_yard_in_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_unload_yard_out_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_unload_yard_out_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_unload_wb_out_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_unload_wb_out_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_unload_gate_out_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_unload_gate_out_out, cast('2027-01-01' as datetime)) "+
	") "+
	" else "+
	" least(coalesce(earliest_load_gate_in_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_load_gate_in_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_reg_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_reg_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_load_wb_in_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_load_wb_in_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_load_yard_in_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_load_yard_in_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_load_yard_out_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_load_yard_out_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_load_wb_out_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_load_wb_out_out, cast('2027-01-01' as datetime)) "+
	",coalesce(earliest_load_gate_out_in, cast('2027-01-01' as datetime)) "+
	",coalesce(latest_load_gate_out_out, cast('2027-01-01' as datetime)) "+
	") "+
	" end) "+
	", combo_end = (case when material_cat is null or material_cat = 0 then "+
	" greatest(coalesce(earliest_unload_gate_in_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_unload_gate_in_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_reg_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_reg_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_unload_wb_in_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_unload_wb_in_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_unload_yard_in_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_unload_yard_in_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_unload_yard_out_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_unload_yard_out_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_unload_wb_out_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_unload_wb_out_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_unload_gate_out_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_unload_gate_out_out, cast('1990-01-01' as datetime)) "+
	") "+
	" else "+
	" greatest(coalesce(earliest_load_gate_in_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_load_gate_in_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_reg_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_reg_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_load_wb_in_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_load_wb_in_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_load_yard_in_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_load_yard_in_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_load_yard_out_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_load_yard_out_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_load_wb_out_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_load_wb_out_out, cast('1990-01-01' as datetime)) "+
	",coalesce(earliest_load_gate_out_in, cast('1990-01-01' as datetime)) "+
	",coalesce(latest_load_gate_out_out, cast('1990-01-01' as datetime)) "+
	") "+
	" end) "+
	" where tpr_id=? "
	;
	
	private static String COPY_REF_PO = "insert into do_grade_transporter_hist(do_id, grade_id, transporter_id, ref_po_no, ref_po_line_item, updated_on, updated_by) "+
	" ( "+
		"	select refl.do_id, refl.grade_id, refl.transporter_id, refl.ref_po_no, refl.ref_po_line_item, now(),? "+
		"	from do_grade_transporter refl where refl.do_id = ? and refl.ref_po_no is not null and ref_po_line_item is not null "+ 
		"	and not exists(select 1 from do_grade_transporter_hist h where h.do_id = refl.do_id and h.grade_id=refl.transporter_id and h.ref_po_no=refl.ref_po_no and h.ref_po_line_item = refl.ref_po_line_item) "+
		"	)";
	private static String COPY_REF_PO_APPRVD = "insert into do_grade_transporter_hist_apprvd(do_id, grade_id, transporter_id, ref_po_no, ref_po_line_item, updated_on, updated_by) "+
	" ( "+
		"	select refl.do_id, refl.grade_id, refl.transporter_id, refl.ref_po_no, refl.ref_po_line_item, now(),? "+
		"	from do_grade_transporter refl where refl.do_id = ? and refl.ref_po_no is not null and ref_po_line_item is not null "+ 
		"	and not exists(select 1 from do_grade_transporter_hist_apprvd h where h.do_id = refl.do_id and h.grade_id=refl.transporter_id and h.ref_po_no=refl.ref_po_no and h.ref_po_line_item = refl.ref_po_line_item) "+
		"	)";
	public static String G_NEW_OLD_TPR_REPORTING_STATUS = "select tp0.status, tp0.tpr_status, tp0.is_latest, tp0.reporting_status, tp0_apprvd.status, tp0_apprvd.tpr_status, tp0_apprvd.is_latest, tp0_apprvd.reporting_status, tp0.material_cat,tp0.tpr_id from tp_record tp0 left outer join tp_record_apprvd tp0_apprvd on (tp0.tpr_id = tp0_apprvd.tpr_id) ";
	public static class TPRStatusInfo {
		int status = 1;
		int tprStatus = 0;
		int isLatest = 0;
		int reportingStatus = 1;
		int statusOld = 1;
		int tprStatusOld = 0;
		int isLatestOld = 0;
		int reportingStatusOld = 1;
		int materialCat = 0;
		public TPRStatusInfo(int status, int tprStatus, int isLatest, int reportingStatus, int statusOld, int tprStatusOld, int isLatestOld, int reportingStatusOld, int materialCat) {
			this.status = status;
			this.tprStatus = tprStatus;
			this.isLatest = isLatest;
			this.reportingStatus = reportingStatus;
			this.statusOld = statusOld;
			this.tprStatusOld = tprStatusOld;
			this.isLatestOld = isLatestOld;
			this.reportingStatusOld = reportingStatusOld;
			this.materialCat = materialCat;
		}
		public static TPRStatusInfo getStatusInfo(Connection conn, int tprId) throws Exception {
			PreparedStatement ps = conn.prepareStatement(G_NEW_OLD_TPR_REPORTING_STATUS+" where tp0.id in (?)");
			ps.setInt(1, tprId);
			ResultSet rs = ps.executeQuery();
			boolean hasNext = rs.next();
			int colIndex = 1;
			int status = hasNext ? Misc.getRsetInt(rs, colIndex++, 1) : 1;
			int tprStatus = hasNext ? Misc.getRsetInt(rs, colIndex++, 0) : 0;
			int isLatest = hasNext ? Misc.getRsetInt(rs, colIndex++, 0) : 0;
			int reportingStatus = hasNext ? Misc.getRsetInt(rs, colIndex++, 1) : 1;
			int statusOld = hasNext ? Misc.getRsetInt(rs, colIndex++, 1) : 1;
			int tprStatusOld = hasNext ? Misc.getRsetInt(rs, colIndex++, 0) : 0;
			int isLatestOld = hasNext ? Misc.getRsetInt(rs, colIndex++, 0) : 0;
			int reportingStatusOld = hasNext ? Misc.getRsetInt(rs, colIndex++, 1) : 1;
			int materialCat = hasNext ? Misc.getRsetInt(rs, colIndex++, 1) : 1;
			TPRStatusInfo retval = new TPRStatusInfo(status, tprStatus, isLatest, reportingStatus, statusOld, tprStatusOld, isLatestOld, reportingStatusOld, materialCat);
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return retval;
		}
		public static ArrayList<TPRStatusInfo> getStatusInfo(Connection conn, ArrayList<Integer> tprIds) throws Exception {
			StringBuilder sb = new StringBuilder(G_NEW_OLD_TPR_REPORTING_STATUS);
			sb.append(" where tp0.tpr_id in (");
			if(tprIds.size()==0)
				sb.append(Misc.getUndefInt());
			else
			Misc.convertInListToStr(tprIds, sb);
			sb.append(") order by tp0.tpr_id ");
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			ResultSet rs = ps.executeQuery();
			int prevObjIndex = -1;
			ArrayList<TPRStatusInfo> retvalList = new ArrayList<TPRStatusInfo>();
			while (rs.next()) {
				int colIndex = 1;
				int status = Misc.getRsetInt(rs, colIndex++, 1);
				int tprStatus = Misc.getRsetInt(rs, colIndex++, 0);
				int isLatest = Misc.getRsetInt(rs, colIndex++, 0);
				int reportingStatus = Misc.getRsetInt(rs, colIndex++, 1);
				int statusOld = Misc.getRsetInt(rs, colIndex++, 1);
				int tprStatusOld = Misc.getRsetInt(rs, colIndex++, 0);
				int isLatestOld = Misc.getRsetInt(rs, colIndex++, 0);
				int reportingStatusOld = Misc.getRsetInt(rs, colIndex++, 1);
				int materialCat = Misc.getRsetInt(rs, colIndex++, 1);
				int tprId = Misc.getRsetInt(rs, colIndex++);
				if (Misc.isUndef(tprId))
					continue;
				prevObjIndex++;
				for (int js = tprIds.size(); prevObjIndex<js; prevObjIndex++) {
					if (tprIds.get(prevObjIndex) == tprId)
						break;
					retvalList.add(new TPRStatusInfo(1,0,0,1,1,0,0,1,0));
				}
				TPRStatusInfo retval = new TPRStatusInfo(status, tprStatus, isLatest, reportingStatus, statusOld, tprStatusOld, isLatestOld, reportingStatusOld, materialCat);
				retvalList.add(retval);
			}
			for (int js = tprIds.size(); prevObjIndex<js; prevObjIndex++) {
				retvalList.add(new TPRStatusInfo(1,0,0,1,1,0,0,1,0));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return retvalList;
		}
		
		public boolean isLocked() {
			return reportingStatus == 10 || reportingStatus == 9;//cancelled or closed
		}
		public boolean isReportingStatusChangeAllowed(int newReportingStatus, int newMaterialCat) {			
			boolean retval = (newReportingStatus == reportingStatusOld || newReportingStatus == 9 || status == 0)
											; 
		     
			
			if (!retval) {
				if (Misc.isUndef(newMaterialCat))
					newMaterialCat = this.materialCat;
				if (statusOld == 0 || statusOld == 2 || reportingStatusOld == 9 || reportingStatusOld == 10) //if deleted/cancelled/closed ... can only goto till Under Verification
				   retval = newReportingStatus == 1 || newReportingStatus == 2 || newReportingStatus == 3 || newReportingStatus == 4;
				else if (statusOld == 1)
					if (materialCat == 0) {
						retval = (reportingStatusOld <= 4 && newReportingStatus <= 5) //if Under Verification .. then only till GRN Ready
						|| (reportingStatusOld ==5 && (newReportingStatus == 11 || newReportingStatus <= 5)) //if GRN Ready - either mapped or older
						|| (reportingStatusOld == 11 && (newReportingStatus <= 5 || newReportingStatus == 6)) //if mapped new or older or GRN Parked
						|| (reportingStatusOld == 6 && (newReportingStatus == 7 || newReportingStatus == 8)) //if GRN Parked .. Succ or faile
						|| ((reportingStatusOld == 7 || reportingStatusOld == 8) && (newReportingStatus == 4 || newReportingStatus == 5)) //if Posted/Failed either back to ready or under verification
						|| (reportingStatusOld == 7  && (newReportingStatus == 10)) //if Posted then closed
						;
					}
					else {
						retval = (reportingStatusOld <= 4 && (newReportingStatus <= 5 || newReportingStatus == 10)) //if Under Verification .. then only till GRN Ready
								;
					}
			}
			return retval;
		}
	}
	
	public static void changeTPRStatusActionNowCalledInWorkflow(Connection conn, SessionManager session, int objectId, boolean isNew, int userId, int oldStatus, int newStatus) throws Exception {
		//1. If going to 4 or before then cancel Lockdown
		//2. if goinng to 5 or above except 10, start lockdown
		//2. if going to Closed == 10, cancel lockdown flow
		//Cancel/Uncancel handled elsewhere
		ArrayList<Integer> objectIds = new ArrayList<Integer>();
		ArrayList<Integer> workflowTypes = new ArrayList<Integer>();
		objectIds.add(objectId);
		workflowTypes.add(202);
		if (newStatus <= 4 || newStatus == 10 || newStatus == 9) {
			WorkflowDef.rejectWorkflow(conn, session, objectIds, workflowTypes, userId, "Auto: Rejected on change of status to "+newStatus);
		}
		else {
			WorkflowDef.createWorkflow(conn, session, objectIds, workflowTypes, userId, "Auto: Created on change of status to "+newStatus);
		}
		
	}
	private void postProcess(Connection conn,SessionManager session, int objectId, boolean isNew, int userId, Value newReportingStatus, TPRStatusInfo tprStatusInfo) throws Exception {
		PreparedStatement ps = null;
		try {
			if (this.objectType == WorkflowHelper.G_OBJ_TPRECORD) {
				//1. update combo_start, combo_end, tpr_create_date
				if (this.customPostHandler == 1) {
					ps = conn.prepareStatement(UPD_TPR_DATES);
					ps.setInt(1, objectId);
					ps.execute();
					ps = Misc.closePS(ps);
					ps = conn.prepareStatement(UPD_INOUT_IF_MISSING);
					ps.setInt(1, objectId);
					ps.execute();
					ps = Misc.closePS(ps);
				}
				if (tprStatusInfo != null && newReportingStatus != null && newReportingStatus.isNotNull()) {
					//this.changeTPRStatusAction(conn, objectId, isNew, userId, tprStatusInfo.reportingStatusOld, newReportingStatus.getIntVal());
					//instead being called in workflow
				}
				
			}
			if (this.objectType == WorkflowHelper.G_OBJ_DORR) {
				//1. update combo_start, combo_end, tpr_create_date
				if (this.customPostHandler == 1) {
					ps = conn.prepareStatement(COPY_REF_PO);
					Misc.setParamInt(ps, userId, 1);
					ps.setInt(2, objectId);
					ps.execute();
					ps = Misc.closePS(ps);
					ps = conn.prepareStatement(COPY_REF_PO_APPRVD);
					Misc.setParamInt(ps, userId, 1);
					ps.setInt(2, objectId);
					ps.execute();
					ps = Misc.closePS(ps);
				}
				
			}
			try{
			if (this.objectType == WorkflowHelper.G_PLAN_DEFINITION) {
				System.out.println("InputTemplate.postProcess(), Calling optimizer start");
				NewMU nm=NewMU.getManagementUnit(conn,Misc.getParamAsInt(session.getParameter("pv123"),816));
				nm.load(conn, false);
				long ts = System.currentTimeMillis();
				String fromDate=session.getParameter("from_date");
				if(fromDate!=null){
				SimpleDateFormat sdfDate = new SimpleDateFormat("dd/mm/yy :mm:ss");
				Date dt = sdfDate.parse(fromDate);
					if (dt != null)
						ts = dt.getTime();
				}
				nm.optimize(conn,ts,null);
				System.out.println("InputTemplate.postProcess(), Calling optimizer END");
			}
			}catch (Exception e) {
				e.printStackTrace();
				//Eating Exception for Demo purpose , Need to Handle Later.
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			ps = Misc.closePS(ps);
		}
	}
	
	private static boolean hackValidateSECLAlloc(Connection conn, int objectId, ArrayList<Pair<Boolean,Value>>wbList, ArrayList<ArrayList<Value>> valsList, int allocFieldIndex, int propLiftedFieldIndex, StringBuilder errMsg) throws Exception {
		
		PreparedStatement ps = conn.prepareStatement("select mines_do_details.do_number, qty_alloc-(case when qty_already_lifted is null then 0 else qty_already_lifted end), wb_id, current_allocation, lifted_qty from mines_do_details left outer join current_do_status_apprvd on (id = do_id) where id = ?");
		ps.setInt(1, objectId);
		ResultSet rs = ps.executeQuery();
		double totAlloc = 0;
		double totLifted = 0;
		
		double totqty = 0;
		String doNumber = null;
		ArrayList<MiscInner.PairDouble> allocLifted = new ArrayList<MiscInner.PairDouble>();
		for (int i=0,is=valsList.size(); i<is;i++) {
			double allocPerUser = allocFieldIndex >= 0 && valsList.get(i).get(allocFieldIndex) != null ? valsList.get(i).get(allocFieldIndex).getDoubleVal() : Misc.getUndefDouble();
			double liftedPerUser = propLiftedFieldIndex >= 0 && valsList.get(i).get(propLiftedFieldIndex) != null  ? valsList.get(i).get(propLiftedFieldIndex).getDoubleVal() : Misc.getUndefDouble();
			allocLifted.add(new MiscInner.PairDouble(allocPerUser, liftedPerUser));
		}
		while (rs.next()) {
			doNumber = rs.getString(1);
			totqty = rs.getDouble(2);
			int wbId = Misc.getRsetInt(rs, 3);
			double allocPerData = rs.getDouble(4);
			double liftedPerData = rs.getDouble(5);
			double allocPerUser = Misc.getUndefDouble();
			double liftedPerUser = Misc.getUndefDouble();
			MiscInner.PairDouble entry = null;
			for (int t1=0,t1s=wbList.size();t1<t1s;t1++) {
				int wbIdUser = wbList.get(t1).second == null || wbList.get(t1).second.isNull() ? -1 : wbList.get(t1).second.getIntVal();
				if (wbIdUser == wbId) {
					if (allocLifted.size() > t1) {
						entry = allocLifted.get(t1);
						allocPerUser = entry.first;
						liftedPerUser = entry.second;
					}
					break;
				}
			}
			if (Misc.isUndef(allocPerUser))
				allocPerUser = allocPerData;
			if (Misc.isUndef(liftedPerUser))
				liftedPerUser = liftedPerData;
			if (entry == null) {
				entry = new MiscInner.PairDouble(0,0);
				allocLifted.add(entry);
			}
			entry.first = allocPerUser;
			entry.second = liftedPerUser;
		}
		
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		for (int i=0,is=allocLifted.size(); i<is; i++) {
			double alloc = allocLifted.get(i).first;
			double lifted = allocLifted.get(i).second;
			if (alloc < lifted-0.00001) {
				errMsg.append("\n").append("Allocation for WB is less than lifted for DO:").append(doNumber);
				return true;
			}
			totAlloc += alloc;
			totLifted += lifted;
		}
		if ((totAlloc < totLifted-0.00001) || (totAlloc > totqty+0.00001)) {
			errMsg.append("\n").append("Tot Allocation is less than total lifted or more than qty available for DO:").append(doNumber);
			return true;
		}
		return false;
	}

	private static boolean checkIfMandatoryThereNested(DimConfigInfo dimConfig, ArrayList<Value> params, ArrayList<Integer> paramIndexes) {
		boolean retval = true;
		for (int t1=0,t1s=dimConfig.m_nestedCols == null  ? 1 : dimConfig.m_nestedCols.get(0).size(); t1<t1s;t1++) {
			DimConfigInfo nestDC = dimConfig.m_nestedCols == null  ? dimConfig : dimConfig.m_nestedCols.get(0).get(t1);
			int nestDimId = nestDC.m_dimCalc != null && nestDC.m_dimCalc.m_dimInfo != null ? nestDC.m_dimCalc.m_dimInfo.m_id : Misc.getUndefInt();
			if (Misc.isUndef(nestDimId))
				continue;
			DimInfo nestDimInfo = DimInfo.getDimInfo(nestDimId);
			ColumnMappingHelper nestColMap = nestDimInfo.m_colMap;
			if (nestColMap == null || nestColMap.table.equalsIgnoreCase("Dummy"))
				continue;
			if (nestColMap.useColumnOnlyForName)
				continue;
			int paramIdx = paramIndexes.get(t1); 
			if (nestDC.m_isMandatory) {
				Value v = params.get(paramIdx);
				if (v == null || v.isNull())
					return false;
			}
		}
		return true;
	}
	/*private static Pair<Value, Boolean> readParameter(Element row, SessionManager session, DimInfo dimInfo , SimpleDateFormat withSec, SimpleDateFormat withMin, SimpleDateFormat withDate, SimpleDateFormat withStd, Element topElem) {
		return readParameter(row, session, dimInfo, withSec, withMin, withDate, withStd, topElem,false);
	}*/
	public static String getCleanedString(String str) {
		if (str != null) {
			str = str.trim();
			if (str.length() == 0)
				str = null;
		}
		return str;
	}
	private static Pair<Value, Boolean> readParameter(Element row, SessionManager session, DimInfo dimInfo , SimpleDateFormat withSec, SimpleDateFormat withMin, SimpleDateFormat withDate, SimpleDateFormat withStd, Element topElem,boolean isAudit, ArrayList<ArrayList<DimConfigInfo>> commonRows, boolean useTop ) {
		boolean foundValidParam = false;
		String varName = "v"+dimInfo.m_id + (isAudit ? "_audit" : "");
		String val = null;
		String valo = null;
		String varNameo = varName+"_o";
		if (useTop) {
			val = topElem == null ? null : topElem.getAttribute(varName);
			val = getCleanedString(val);
			valo = getCleanedString(topElem == null ? null : topElem.getAttribute(varNameo));
			if (val == null && commonRows != null) {
				DimConfigInfo dci = InputTemplate.getDimConfigInfoInList(commonRows, dimInfo.m_id, null);
				if (dci != null)
					val = dci.m_default;
				val = getCleanedString(val);
			}			
		}
		else {
			valo = getCleanedString(row == null ? null : row.getAttribute(varNameo));
			val = row == null ? session.getParameter(varName) : row.getAttribute(varName);
			val = getCleanedString(val);
			if (val == null && topElem != null) {
				val = topElem.getAttribute(varName);
				val = getCleanedString(val);
				valo = getCleanedString(topElem.getAttribute(varNameo));
			}
			if (val == null && commonRows != null) {
				DimConfigInfo dci = InputTemplate.getDimConfigInfoInList(commonRows, dimInfo.m_id, null);
				if (dci != null)
					val = dci.m_default;
				val = getCleanedString(val);
			}
		}
		/*if(isAudit && (val == null || val.length() <= 0)){
			val = row == null ? session.getParameter("v"+dimInfo.m_id) : row.getAttribute("v"+dimInfo.m_id);
		}*/
		
		Value v = null;
		if(isAudit){
			int iv = Misc.getParamAsInt(val);
			if (!Misc.isUndef(iv) && iv != -1000)
				foundValidParam = true;
			v = new Value(iv);
		}
		else if (dimInfo.m_type == Cache.NUMBER_TYPE) {
			double dv = Misc.getParamAsDouble(val);
			if (!Misc.isUndef(dv))
				foundValidParam = true;
			v = new Value(dv);
		}
		else if (dimInfo.m_type == Cache.STRING_TYPE) {
			if (val != null && val.trim().length() > 0)
				foundValidParam = true;
			v = new Value(val);
		}
		else if (dimInfo.m_type == Cache.DATE_TYPE) {
			Date dt = Misc.getParamAsDate(val, withSec, withMin, withDate, withStd);
			if (dt != null)
				foundValidParam = true;
			
			Date dto = Misc.getParamAsDate(valo, withSec, null, null, null);
			if (dto != null && dt != null) {
				long ts1 = dto.getTime();
				long ts = dt.getTime();
				if (ts1 >= ts && (ts1-ts) < 60000) {
					dt = dto;
				}
			}
			v = new Value(Misc.utilToLong(dt));
		}
		else {
			int iv = Misc.getParamAsInt(val);
			if (iv == -1000)
				iv = Misc.getUndefInt();
			if (!Misc.isUndef(iv) && iv != -1000)
				foundValidParam = true;
			v = new Value(iv);
		}	
		if (!foundValidParam) {
			if (dimInfo.m_id == 60264)
				v = new Value(1);
			//TODO estimateParametersFromOtherValues(row, session, dimInfo, withSec,  withMin, withDate, withStd, topElem, isAudit);
		}
		return new Pair<Value, Boolean> (v, foundValidParam);
	}		
	
	
	
	private ArrayList<ArrayList<ArrayList<Integer>>> getReadWrite(Connection conn, SessionManager session, ArrayList<ArrayList<Integer>> workflowIds, ArrayList<Integer> objectIds, ArrayList<Integer> workflowTypeIds, ArrayList<TPRStatusInfo> tprLikeStatusInfos) throws Exception {
		//1st dim: by ObjectId, 2nd dim by row, 3rd dim by col
		// 0-> none, 1 => read, 2 => read/write
		ArrayList<ArrayList<ArrayList<Integer>>> retval = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<Pair<Integer,ArrayList<Integer>>>> preprocessRows = new ArrayList<ArrayList<Pair<Integer,ArrayList<Integer>>>>();//1st dim: row, 2nd dim: col
		//, 3rd.first = whether globally read/write, 3rd.second = list of index in workflowTypeDim for the dim
		User user = session.getUser();
		for (int i=0,is=rows.size(); i<is;i++) {
			ArrayList<DimConfigInfo> row = rows.get(i);
			ArrayList<Pair<Integer, ArrayList<Integer>>> preprocessRow = new ArrayList<Pair<Integer, ArrayList<Integer>>>();
			preprocessRows.add(preprocessRow);
			for (int j=0,js=row.size(); j<js; j++) {
				DimConfigInfo dimConfig = row.get(j);
				DimInfo dimInfo = dimConfig != null && dimConfig.m_dimCalc != null ? dimConfig.m_dimCalc.m_dimInfo : null;
				String readTag = dimConfig != null ? dimConfig.m_accessPriv : null;
				String writeTag = dimConfig != null ? dimConfig.m_writePriv : null;
				if (Misc.emptyString.equals(readTag))
					readTag = null;
				if (Misc.emptyString.equals(writeTag))
					writeTag = null;
				if (readTag == null && dimInfo != null)
					readTag = dimInfo.m_readTagNew;
				if (writeTag == null && dimInfo != null)
					writeTag = dimInfo.m_writeTagNew;
				if (Misc.emptyString.equals(readTag))
					readTag = null;
				if (Misc.emptyString.equals(writeTag))
					writeTag = null;
				int rw = 2;
				ArrayList<Integer> worflowTypeIndices = null;
				if (writeTag != null && writeTag.length() > 0) {
					if (user.isPrivAvailable(session, writeTag))
						rw = 2;
					else {
						if (readTag != null && readTag.length() > 0) {
							if (user.isPrivAvailable(session, readTag))
								rw = 1;
							else
								rw = 0;
						}
						else {
							rw = 1;
						}
					}
				}
				else {
					if (readTag != null && readTag.length() > 0) {
						if (user.isPrivAvailable(session, readTag))
							rw = 1;
						else
							rw = 0;
					}
					else {
						rw = 2;
					}
				}
				if (dimInfo != null) {
					ArrayList<Integer> workflowTypeIdsUsed = WorkflowDef.getWorkflowForDim(dimInfo.m_id);
					worflowTypeIndices = new ArrayList<Integer>();
					for (int i1=0,i1s= workflowTypeIdsUsed == null ? 0 : workflowTypeIdsUsed.size(); i1<i1s;i1++) {
						int idx = -1;
						int widt = workflowTypeIdsUsed.get(i1);
						for (int i2=0,i2s = workflowTypeIds.size(); i2 <i2s;i2++) {
							if (workflowTypeIds.get(i2) == widt) {
								idx = i2;
								break;
							}
						}
						if (idx >= 0)
							worflowTypeIndices.add(idx);
					}
				}
				preprocessRow.add(new Pair<Integer, ArrayList<Integer>>(rw, worflowTypeIndices));
			}//for each col
		}//for each row .. preprocess
		for (int k=0,ks=workflowIds.size(); k<ks;k++) {
			ArrayList<ArrayList<Integer>> resultEntry = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> workflowIdsForObj = workflowIds.get(k);
			TPRStatusInfo tprStatusInfo = tprLikeStatusInfos == null ? null : tprLikeStatusInfos.get(k);
			boolean lockDown = tprStatusInfo == null ? false : tprStatusInfo.isLocked();
			retval.add(resultEntry);
			for (int i=0,is=rows.size(); i<is;i++) {
				ArrayList<DimConfigInfo> row = rows.get(i);
				ArrayList<Pair<Integer, ArrayList<Integer>>> preprocessRow = preprocessRows.get(i);
				ArrayList<Integer> resultRow = new ArrayList<Integer>();
				resultEntry.add(resultRow);
				for (int j=0,js=row.size(); j<js; j++) {
					Pair<Integer, ArrayList<Integer>> preprocessRowEntry = preprocessRow.get(j);
					int rw = preprocessRowEntry.first;
					ArrayList<Integer> wkfIdxList = preprocessRowEntry.second;
					DimInfo dimInfo = row.get(j).m_dimCalc != null  ? row.get(j).m_dimCalc.m_dimInfo : null;
					if (rw == 2) { //check if there is an active workflow
						if (lockDown && dimInfo != null && dimInfo.m_id != 60283 && dimInfo.m_id != 60264)
							rw = 1;
					}
					if (rw == 2) {
						for (int i1=0,i1s=wkfIdxList == null ? 0 : wkfIdxList.size(); i1 < i1s; i1++) {
							int wkfIdx = wkfIdxList.get(i1);
							if (wkfIdx >= 0) {
								int wkfid = workflowIdsForObj.get(wkfIdx);
								if (!Misc.isUndef(wkfid)) {
									rw = 1;
									break;
								}
							}//
						}//for each workflow type possible for dim
					}//if otherwise writeable ... check if there are any workflow
					resultRow.add(rw);
				}//for each col
			}//for each row
		}//for each object
		return retval;
	}
	
	
	
	private ArrayList<ArrayList<ArrayList<Value>>> readAllTopLevelRowsNotUsed(Connection conn, WorkflowHelper.QueryHelperNew queryHelper, ArrayList<Integer> objectIds, boolean doingCurr) throws Exception {
		//potentially too much memory
		ArrayList<ArrayList<ArrayList<Value>>> retval = new ArrayList<ArrayList<ArrayList<Value>>>();
		for (int i=0,is = objectIds.size(); i<is;i++) {
			retval.add(null);
		}
		String query = queryHelper.getQueryDetailed(objectType, doingCurr, -1, objectIds, false);
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			Pair<Integer, ArrayList<ArrayList<Value>>> vals = WorkflowHelper.readTopLevelQueryResult(rs, rows, queryHelper.dimTypeAndIndexPrimary);
			for (int i1=0,i1s = objectIds.size(); i1<i1s; i1++) {
				if (objectIds.get(i1) != null && vals.first != null && objectIds.get(i1).intValue() == vals.first.intValue()) {
					retval.set(i1, vals.second);
				}
			}
		}
		rs.close();
		ps.close();
		
		return retval;
	}
	
	private ArrayList<ArrayList<ArrayList<Value>>> readAllNestedQueryAtIndex(Connection conn, WorkflowHelper.QueryHelperNew queryHelper, ArrayList<Integer> objectIds, int index, boolean doingCurr) throws Exception {
		ArrayList<ArrayList<ArrayList<Value>>> retval = new ArrayList<ArrayList<ArrayList<Value>>>();
		for (int i=0,is = objectIds.size(); i<is;i++) {
			retval.add(null);
		}
		String query = queryHelper.getQueryDetailed(objectType, doingCurr, index, objectIds, false);
		PreparedStatement ps = conn.prepareStatement(query);
		System.out.println("Nested Query:"+query);
		ResultSet rs = ps.executeQuery();
		boolean toCont = rs.next();
		while (toCont) {
			Triple<Integer, ArrayList<ArrayList<Value>>, Boolean> vals = WorkflowHelper.readNestedQueryResultSpecial(rs, queryHelper.dimTypeAndIndexNested.get(index));
			for (int i1=0,i1s = objectIds.size(); i1<i1s; i1++) {
				if (objectIds.get(i1) != null && vals.first != null && objectIds.get(i1).intValue() == vals.first.intValue()) {
					retval.set(i1, vals.second);
				}
			}
			toCont = vals.third;
		}
		rs.close();
		ps.close();
		
		return retval;
	}
	public ArrayList<Integer> getObjectIdsFromSearch(Connection conn, SessionManager session, SearchBoxHelper searchBoxHelper, FormatHelper formatHelper, ArrayList<Integer> origObjectIds, boolean toIgnGivenIds, boolean glbRead) throws Exception {
		ArrayList<Integer> retval = new ArrayList<Integer>();
		FrontPageInfo fpi = this.getCalculatedFrontPageInfo();
		WorkflowHelper.TableObjectInfo driverObjectInfo = WorkflowHelper.getTableInfo(this.getObjectType());
		if (driverObjectInfo != null && fpi != null && fpi.m_frontSearchCriteria != null && fpi.m_frontSearchCriteria.size() > 0) {
			GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
			GeneralizedQueryBuilder.QueryParts qp = qb.buildQueryParts(fpi.m_frontInfoList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session,
					searchBoxHelper, formatHelper, fpi.m_colIndexLookup, GeneralizedQueryBuilder.getDriverObjectFromName(fpi.m_driverObjectLocTracker), fpi.m_orderIds, null, true, Misc.getUndefInt(),0, false,0);
			if (!toIgnGivenIds && origObjectIds != null && origObjectIds.size() > 0 && origObjectIds.get(0) > 0) {
				if (qp.m_whereClause.length() > 0)
					qp.m_whereClause.append(" and ");
				qp.m_whereClause.append(driverObjectInfo.getName()).append(".").append(driverObjectInfo.getPrimaryIdCol()).append(" in (");
				Misc.convertInListToStr(origObjectIds, qp.m_whereClause);
				qp.m_whereClause.append(")");
			}
			String query = qb.buildQuery(session, qp, null, false);
			query = query.replaceFirst("select ", "select "+driverObjectInfo.getName()+"."+driverObjectInfo.getPrimaryIdCol()+", ");
			System.out.println("############# Inp templates' front page's search"+query);
			PreparedStatement ps = conn.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
				
			while (rs.next()) {
				int objId = rs.getInt(1);
				retval.add(objId);
			}
			rs.close();
			ps.close();
		}
		boolean hasCreatePriv = 	(this.m_createPriv == null || session.getUser().isPrivAvailable(session, this.m_createPriv));
		if (!glbRead && hasCreatePriv && (this.m_createSpec == 1 && retval.size() == 0) || (this.m_createSpec == 2)) {
				retval.add(Misc.getUndefInt());
		}
		
		return retval;
	}
	public static StringBuilder getIgnoredObjectIds(ArrayList<Integer> retval, ArrayList<Integer> origObjectIds, boolean toIgnGivenIds) {
		int retvalSz = retval == null || retval.size() == 0 || retval.get(0) < 0 ? 0 : retval.size();
		int origSz = origObjectIds == null || origObjectIds.size() == 0 || origObjectIds.get(0) < 0 ? 0 : origObjectIds.size();
		StringBuilder ignoredIds = new StringBuilder();
		if (!toIgnGivenIds && origSz != retvalSz && origSz > 0) {
			
			ignoredIds.append("Operation cant be done on following object Ids because conditions not met:");
			boolean added = false;
			for (int i=0,is=origObjectIds.size(); i<is;i++) {
				int oid = origObjectIds.get(i);
				if (oid < 0)
					continue;
				boolean found = false;
				for (int j=0,js=retval.size();j<js;j++) {
					if (retval.get(j) == oid) {
						found = true;
						break;
					}
				}
				if (!found) {
					if (added)
						ignoredIds.append(",");
					added = true;
					ignoredIds.append(oid);
				}
			}
			if (!added) {
				ignoredIds = null;
			}
		}
		return ignoredIds;
	}
	public static String fixForAtParams(String queryReg, SessionManager session, SearchBoxHelper searchBoxHelper) {
		if (queryReg.indexOf("@user") >= 0)
			queryReg = queryReg.replaceAll("@user", Long.toString(session.getUserId()));
		if (queryReg.indexOf("@workflow_type") >= 0) {
			String wkfType = getCleanedString(session.getParameter("pv416"));
			if (wkfType == null && searchBoxHelper != null)
				wkfType = getCleanedString(session.getParameter(searchBoxHelper.m_topPageContext+416));
			if (wkfType != null)
				queryReg = queryReg.replaceAll("@workflow_type", wkfType);
		}
		return queryReg;
	}
	
	public void callPrintInputBlockGeneric(Connection conn, SessionManager session, ArrayList<Integer> objectIds, JspWriter out, ByteArrayOutputStream stream, int reportType,String reportName, int reportId, ServletOutputStream servletStream, boolean toIgnoreSearchParams) throws Exception {
		Table table = Table.createTable();
		StringBuilder sb = new StringBuilder();
		printInputBlockNew(out, conn, session, objectIds, true, table, reportType == Misc.HTML && out != null, toIgnoreSearchParams,true);	
		String tempReportType = session.getParameter("report_type");
		/*if("5".equals(tempReportType) && out != null){
			JSONArray nestedJson = JasonGenerator.printJason(table, sb, session);
			sb.append(nestedJson.toString());
			out.print(sb);
		}*/if(out != null && "5".equals(tempReportType)){
			JsonStreamer.printJason(table, sb, session, out);
		}else if	(out != null && reportType == Misc.HTML){
			HtmlGenerator.printHtmlTable(table, sb, session, !m_doAsMultiRow);
			out.println(sb);
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
			//out.println(db);
		}
	}
	
	public void printInputBlockNew(JspWriter out, Connection conn, SessionManager session, ArrayList<Integer> objectIds, boolean glbRead, Table outputTable, boolean printSearchBox, boolean toIgnoreSearchParams,boolean searchButtonPressed) throws Exception {
		//MOD: If Table is not null then will create table so that it can then be printed and streamed as Excel/PDF

		//hidden elems are printed as hidden vars ... but when saving only hidden with mandatory are saved back 
		//(assumption being that these may be changed via script) OR if the dimConfig is marked as useTopLevelInInpTemplate
		//then we read the value from common params instead of taking it from left
		
		//1. getReadWrite
		//Strategy for printing:
		//Get All top level rows of regular and of approved - THIS MUST BE IN SORTED ORDER OF OBJECTIDs
		//Iterate through regular and approved - if reg id > approved then reg missing else if reg id < apprvd then apprvd missing else both there
		//for the object id in question, get nested rows for each of the nested dim config for bot reg and approved
		// basically we are getting ArrayList<ArrayList<ArrayList<Value>>> - the inner ArrayList - a row
		//, 2n mid list of rows for a given nested col, the outermost - the list of nested cols
		//Give it (regTopRow, approvedTopRow and all the nested Value) to  printSingleDataRow
		// that will print for each of UI row and nested values.
		//   In order to ensure saving of data ... we will have different javascript to collect data - outer table will have an 
		//   attribute of _js = "td" - we will get all rows with such ..
		//   each inner beginning row will have _js="b"
		//   each nested table will have _js="d"+dimInfo.id ...

        //HACKISH for showing selectboxes for 		
		Connection connForRegTop = conn;
		Connection connForApprvdTop = conn;
		boolean newConnCreated = false;
		StringBuilder sb = new StringBuilder();
		int workflowType = WorkflowDef.WORKFLOW_TYPE_ALL;//type 0-regular,1-special,2-both
		long ts1 = System.currentTimeMillis();
		long ts2 = ts1;
		System.out.println("[INPT Get] Thread:"+Thread.currentThread().getId()+" start:"+ts1);
		try {
			boolean toAddForWorkflowSelection = "1".equals(session.getParameter("_use_for_approval"));//may be set to false dimbeing shown has 415 or418  90117 or 90118			
			for (int i=0,is=rows.size(); i<is; i++) {
				ArrayList<DimConfigInfo> row = rows.get(i);
				for (int j=0,js=row.size(); j<js; j++) {
					DimConfigInfo col = row.get(j);
					if (col.m_dimCalc != null && col.m_dimCalc.m_dimInfo != null) {
						int dimid = col.m_dimCalc.m_dimInfo.m_id;
						toAddForWorkflowSelection = toAddForWorkflowSelection && dimid != 415 && dimid != 418;
					}
				}
			}

			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			SimpleDateFormat sdfWithHMMSS = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
			FormatHelper formatHelper = InputTemplate.getFormatHelper(rows, session);
			MiscInner.PairBool needVehicleDriverWrite = new MiscInner.PairBool(false, false);
			ArrayList<FormatHelper> nestedFormatters = new ArrayList<FormatHelper>();
			SearchBoxHelper searchBoxHelper = null;
			ArrayList<Integer> origObjectIds = objectIds;
			boolean toIgnGivenIds = "1".equals(session.getAttribute("ign_vehicle_id"));
			StringBuilder ignoredIds = null;
			boolean addUndef = objectIds == null || objectIds.size() == 0;
			if (!toIgnoreSearchParams && this.searchCriteria != null && this.searchCriteria.size() > 0) {
				System.out.println("[INPT Get] Thread:"+Thread.currentThread().getId()+" getting objectIds");
				String pgContext = session.getParameter("page_context");
				int privIdForOrg = session.getUser().getPrivToCheckForOrg(session, pgContext); //this tells the privilege to use for showing the Org tree
				session.setAttribute("SearchButton", "1", false);
				searchBoxHelper = 
					PageHeader.processSearchBox(session, privIdForOrg, pgContext, searchCriteria, null);
				//Misc.preprocessForSearchBeforeView(session.request, null);
				if (searchButtonPressed) {
					objectIds = getObjectIdsFromSearch(conn, session, searchBoxHelper, formatHelper, origObjectIds, toIgnGivenIds, glbRead);
					addUndef = objectIds == null || objectIds.size() == 0;
					ignoredIds = getIgnoredObjectIds(objectIds, origObjectIds, toIgnGivenIds);
					if (addUndef) {
						if (objectIds == null)
							objectIds = new ArrayList<Integer>();
						objectIds.add(Misc.getUndefInt());
					}
				}
				ts2 = System.currentTimeMillis();
				System.out.println("[INPT Get] Thread:"+Thread.currentThread().getId()+" got objectIds (ms):+"+(ts2-ts1));
				ts1=ts2;
			}
			
			
			//get format helpers for nested columns - 
			for (int i=0,is=rows.size(); i<is; i++) {
				ArrayList<DimConfigInfo> row = rows.get(i);
				for (int j=0,js=row.size(); j<js; j++) {
					DimConfigInfo col = row.get(j);
					if (col.m_dimCalc != null && col.m_dimCalc.m_dimInfo != null) {
						int dimid = col.m_dimCalc.m_dimInfo.m_id;
						toAddForWorkflowSelection = toAddForWorkflowSelection && dimid != 415 && dimid != 418 ;
					}
					if (col.m_nestedCols != null) {
						nestedFormatters.add(InputTemplate.getFormatHelper(col.m_nestedCols, session));
					}
				}
			}
			
			Cache cache = session.getCache();
			TableObjectInfo tableInfo = WorkflowHelper.g_tablesForObjectId.get(this.objectType);
			String selectCheckBoxVarName = tableInfo.getParamName();
			boolean hasCreatePriv = 	(this.m_createPriv == null || session.getUser().isPrivAvailable(session, this.m_createPriv));
			if (this.m_searchSpecIfExists > 0 && out != null && printSearchBox) {
				String pgContext = session.getParameter("page_context");
				int privIdForOrg = session.getUser().getPrivToCheckForOrg(session, pgContext);
				FrontPageInfo fpi = this.getCalculatedFrontPageInfo();
				StringBuilder searchBoxText = new StringBuilder();
				com.ipssi.gen.utils.PageHeader.printSearchBox(session, privIdForOrg, pgContext, fpi.m_frontSearchCriteria, null, searchBoxHelper, searchBoxText, null/*"do_match_only=1"*/, "Go", true, null, null,"modifyOptions", fpi.m_addnlSearchButton, fpi.m_addnlSearchButtonRetainDim);
				out.println(searchBoxText);
				out.println("<br/>");
			}
			if (!searchButtonPressed && this.m_searchSpecIfExists > 0 && this.searchCriteria != null && this.searchCriteria.size() > 0)
				return;
			ArrayList<Integer> workflowTypeIds = WorkflowHelper.getControllingWorkflowDefs(rows,workflowType,objectType,conn);
			ArrayList<ArrayList<Integer>> workflowIds = WorkflowHelper.getActiveWorkflowsFor(conn, objectIds, workflowTypeIds);
			ts2 = System.currentTimeMillis();
			System.out.println("[INPT Get] Thread:"+Thread.currentThread().getId()+" got activeworkflows (ms):+"+(ts2-ts1));
			ts1=ts2;
			
			ArrayList<TPRStatusInfo> tprLikeStatusInfo = this.objectType == WorkflowHelper.G_OBJ_TPRECORD ? TPRStatusInfo.getStatusInfo(connForApprvdTop, objectIds) : null;
			ts2 = System.currentTimeMillis();
			System.out.println("[INPT Get] Thread:"+Thread.currentThread().getId()+" got status info (ms):+"+(ts2-ts1));
			ts1=ts2;
			ArrayList<ArrayList<ArrayList<Integer>>> readWriteInfo = getReadWrite(conn, session, workflowIds, objectIds, workflowTypeIds, tprLikeStatusInfo);		
			WorkflowHelper.QueryHelperNew queryHelper = WorkflowHelper.helpGetQueryHelperNew(this.objectType, rows);
			boolean hasWorkflow = workflowTypeIds != null && workflowTypeIds.size() > 0;
			
			if (commonRows != null && commonRows.size() > 0 && outputTable == null && out != null) {
				//print top level common input selection box
				if (this.m_doAsMultiRow) {
					sb.append("<table border='0' cellpadding='3' cellspacing='0' _js=\"td\">");
				}
				printSingleDataRow(conn, cache, sb, session, null, sdf, needVehicleDriverWrite
						,commonRows, false, false, selectCheckBoxVarName
						, null, null //queryHelper, nestedFormatters
						, null //readwrite
						, Misc.getUndefInt(), null, null
						, null, null
						,true
						,sdfWithHMMSS
						,glbRead
						,0
						,hasWorkflow
						,this.onSubmitPreHandler
						,false
						,0
						,Misc.getUndefInt(),Misc.getUndefInt()
						,false, Misc.getUndefInt(), false
						,outputTable
						,tableInfo
						);
				if (this.m_doAsMultiRow) {
					sb.append("</table>");
				}
				out.println(sb);
				sb.setLength(0);
			}
			if (ignoredIds != null && out != null && outputTable == null)
				out.println("<table><tr><td class='tmTip'>"+ignoredIds+"</td></tr></table>");
			int _doingSelectionMode = "1".equals(session.getParameter("_selectionMode")) ? 1 : "1".equals(session.getParameter("_approveRejectMode")) ? 2 : 0;//0 => now, 1=>Single, 2=>Multi
			
			if (this.m_doAsMultiRow) {
				sb.append("<table  xxxx='xxx'><tr><td class='tn'>")
				.append("<a  style='margin-left:300px' href='#' onclick='getDownloadDataTable(event.srcElement, true, null, null);'>Download Data</a>&nbsp;&nbsp;&nbsp;")
                 .append("<a  class='noprint' href='#'   onclick='javascript:g_sortCellIndex[0] = 0;'>Reset Sort Criteria </a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                 .append("</td></tr><tr><td>");
                 ;
                 if (out != null  && outputTable == null)
                	 out.println(sb);
				sb.setLength(0);
				printTableHeader(sb, rows, selectCheckBoxVarName, session, "td", false, this.m_createSpec > 1 && hasCreatePriv, false, _doingSelectionMode, toAddForWorkflowSelection, outputTable);//at top level we dont allow row add/delete
			}
			else {
				sb.append("<table border='0' cellpadding='3' cellspacing='0' _js=\"td\">");
			}
			sb.append("<tbody>");
			if (out != null  && outputTable == null)
				out.println(sb);
			sb.setLength(0);
			

			
			//if (objectIds.size() > 10) {
			//	connForRegTop = DBConnectionPool.getConnectionFromPoolNonWeb();
			//	connForApprvdTop = DBConnectionPool.getConnectionFromPoolNonWeb();
			//	newConnCreated = true; 
			//}
			
			String queryReg = queryHelper.getQueryDetailed(objectType, true, -1, objectIds, toAddForWorkflowSelection);
			queryReg = fixForAtParams(queryReg, session, searchBoxHelper);
			
			Statement stmtReg = connForRegTop.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			if (newConnCreated)
				stmtReg.setFetchSize(Integer.MIN_VALUE);
			System.out.println("QUERY REG: Thread:"+Thread.currentThread().getId()+ " "+queryReg);
			
			ResultSet rsReg = stmtReg.executeQuery(queryReg);
			ts2 = System.currentTimeMillis();
			System.out.println("[INPT Get] Thread:"+Thread.currentThread().getId()+" got queryReg (ms):+"+(ts2-ts1));
			ts1=ts2;
			String queryApprvd = queryHelper.getQueryDetailed(objectType, false, -1, objectIds, false);
			queryApprvd = fixForAtParams(queryApprvd, session, searchBoxHelper);
			Statement stmtApprvd = connForApprvdTop.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			if (newConnCreated)
				stmtApprvd.setFetchSize(Integer.MIN_VALUE);
			System.out.println("QUERY APPRVD: Thread:"+Thread.currentThread().getId()+" :" +queryApprvd);
			ResultSet rsApprvd = stmtApprvd.executeQuery(queryApprvd);
			ts2 = System.currentTimeMillis();
			System.out.println("[INPT Get] Thread:"+Thread.currentThread().getId()+" got queryAprrvd (ms):+"+(ts2-ts1));
			ts1=ts2;
			boolean regExhausted = false;
			boolean apprvdExhausted = false;
			Pair<Integer, ArrayList<ArrayList<Value>>> regVals = null;
			Pair<Integer, ArrayList<ArrayList<Value>>> apprvdVals = null;
			boolean loopedOnce = false;//TODO make it true if no create priv 
			ArrayList<Integer> tempObjectIds = new ArrayList<Integer>();
			tempObjectIds.add(Misc.getUndefInt());
			int workflowId = Misc.getUndefInt();
			boolean isFirstRow = true;
			while (true&&searchButtonPressed) {
				workflowId = Misc.getUndefInt();
				if (regVals == null && !regExhausted) {
					regExhausted = !rsReg.next();
					if (!regExhausted) {
						regVals = WorkflowHelper.readTopLevelQueryResult(rsReg, rows, queryHelper.dimTypeAndIndexPrimary);
						if (toAddForWorkflowSelection)
							workflowId = Misc.getRsetInt(rsReg, "workflow_id");
					}
				}
				if (apprvdVals == null && !apprvdExhausted) {
					apprvdExhausted = !rsApprvd.next();
					if (!apprvdExhausted) {
						apprvdVals = WorkflowHelper.readTopLevelQueryResult(rsApprvd, rows, queryHelper.dimTypeAndIndexPrimary);
					}
				}
				if (regVals == null && apprvdVals == null && (loopedOnce || addUndef))
					break;
				
				int regObjectId = regVals == null ? Misc.getUndefInt() : regVals.first;
				loopedOnce = loopedOnce || (hasCreatePriv && this.m_createSpec != 0 ?  Misc.isUndef(regObjectId) : true);
				int apprvdObjectId = apprvdVals == null ? Misc.getUndefInt() : apprvdVals.first;
				int objectId = Misc.getUndefInt();
				ArrayList<ArrayList<Value>> regValList = null;
				ArrayList<ArrayList<Value>> apprvdValList = null;
				if (Misc.isUndef(apprvdObjectId) || regObjectId < apprvdObjectId) { //no apprvd
					regValList = regVals == null ? null : regVals.second;
					objectId = regObjectId;
					regVals = null;
				}
				else if (Misc.isUndef(regObjectId) || regObjectId > apprvdObjectId) {//no reg
					apprvdValList = apprvdVals == null ? null : apprvdVals.second;
					objectId = apprvdObjectId;
					apprvdVals = null;
				}
				else {
					regValList = regVals == null ? null : regVals.second;
					objectId = regObjectId;
					regVals = null;
					apprvdValList = apprvdVals == null ? null : apprvdVals.second;
					apprvdVals = null;
				}
				tempObjectIds.set(0, objectId);
				ArrayList<ArrayList<ArrayList<Value>>> nestedReg = new ArrayList<ArrayList<ArrayList<Value>>>();
				ArrayList<ArrayList<ArrayList<Value>>> nestedApprvd = new ArrayList<ArrayList<ArrayList<Value>>>();
				
				for (int i=0,is = queryHelper.selNestedTables.size(); i<is; i++) {
					nestedReg.add(readAllNestedQueryAtIndex(conn, queryHelper, tempObjectIds, i, true).get(0));
					nestedApprvd.add(readAllNestedQueryAtIndex(conn, queryHelper, tempObjectIds, i, false).get(0));
				}
				int objectIdIndex = objectIds.indexOf(objectId);
				if (objectIdIndex < 0)
					continue;
				int putAdderRemoveControl = 0;
				if (hasCreatePriv && this.m_createSpec == 1) {
					putAdderRemoveControl = 1;
				}
				else if (hasCreatePriv && this.m_createSpec > 1) {
					putAdderRemoveControl = Misc.isUndef(objectId) ? 3 : 1;
				}
				int _nameDim = Misc.getParamAsInt(session.getParameter("_name_dimid"));
			    int _addnlDim = Misc.getParamAsInt(session.getParameter("_addnl_selection_dimid")); 
				if (isFirstRow) {
					
				}
				else {
					isFirstRow = false;
					//if (out != null  && outputTable == null) {
					//	sb.append("<tr><td colspan='1000'><hr noshade size='1'></td></tr>");
					//	out.println(sb);
					//	sb.setLength(0);
					//}
				}
				printSingleDataRow(conn, cache, sb, session, formatHelper, sdf, needVehicleDriverWrite
						,rows, m_doAsMultiRow, m_doAsMultiRow || (objectIds.size() > 1), selectCheckBoxVarName
						, queryHelper, nestedFormatters
						, readWriteInfo.get(objectIdIndex)
						, objectId, regValList, apprvdValList
						, nestedReg, nestedApprvd
						,false
						,sdfWithHMMSS	
						,glbRead
						,putAdderRemoveControl
						,hasWorkflow
						,this.onSubmitPreHandler
						,"1".equals(session.getParameter("must_print_diff_if_diff"))
						,_doingSelectionMode
						,_nameDim, _addnlDim
						,toAddForWorkflowSelection, workflowId, !m_doAsMultiRow && objectIds.size() <= 1
						, outputTable
						,tableInfo
						);
				if (out != null  && outputTable == null)
					out.println(sb);
				sb.setLength(0);
			}//reading thru all data
			rsReg.close();
			rsApprvd.close();
			stmtReg.close();
			stmtApprvd.close();
			sb.append("</tbody></table>");
			ts2 = System.currentTimeMillis();
			System.out.println("[INPT Get] Thread:"+Thread.currentThread().getId()+" printedAllRows (ms):+"+(ts2-ts1));
			ts1=ts2;
			if (this.m_doAsMultiRow) {
				sb.append("</td></tr></table>");//the table holding the data table
			}
			if ((needVehicleDriverWrite.first || needVehicleDriverWrite.second)) {
				sb.append("<script src=\"").append(Misc.G_SCRIPT_BASE).append("autocomplete.js\">").append("</script>");
				sb.append("<script>");
				if (needVehicleDriverWrite.first)
					sb.append("var jg_vehicleList = new Array();");
				if (needVehicleDriverWrite.second)
					sb.append("var jg_driverList = new Array();");
				if (false) {
					int portToUse = Misc.getUndefInt();
					int pv123forLookup = Misc.getUndefInt();
					int pv123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
					/*if (!Misc.isUndef(objectId)) {
						pv123 = DVUtils.guessAppropPV123(session.getCache(), conn, pv123, objectType != 0 ? objectId : Misc.getUndefInt(),  objectType == 0 ? objectId : Misc.getUndefInt());
					}
					*/
					if (needVehicleDriverWrite.first) {
						StringBuilder sb1= DVUtils.getVehicleAutoCompleteObjExt(conn, session.getCache(), pv123, "jg_vehicleList");
						if (sb1 != null) sb.append(sb1).append("\n");
					}
					if (needVehicleDriverWrite.second) {
						StringBuilder sb1= DVUtils.getDriverAutoCompleteObjExt(conn, session.getCache(), pv123, "jg_driverList");
						if (sb1 != null) sb.append(sb1).append("\n");
					}
				}
				sb.append("</script>");
			}
			if (out != null  && outputTable == null)
				out.println(sb);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			try {
				if (newConnCreated) {
					DBConnectionPool.returnConnectionToPoolNonWeb(connForRegTop);
				}
			}
			catch (Exception e2) {
				
			}
			try {
				if (newConnCreated) {
					DBConnectionPool.returnConnectionToPoolNonWeb(connForApprvdTop);
				}
			}
			catch (Exception e2) {
				
			}
		}

		//XML_DATA //objectIdCol //v+dimInfo.m_id //TABLE_d+dimInfo.m_id //XML_DATA_TABLE_d+dimInfo.m_id
		
	}

	public static boolean hasNestedColHeader(ArrayList<ArrayList<DimConfigInfo>> rows) {
		for (int i=0,is = rows.size(); i<is ;i++) {
			ArrayList<DimConfigInfo> row = rows.get(i);
			for (int j=0,js = row.size(); j<js;j++) {
				DimConfigInfo col = row.get(j);
				if (col.m_nestedCols != null && col.m_nestedCols.get(0).size() > 1)
					return true;
			}
		}
		return false;
	}

	private static boolean printSingleValue(Connection conn, Cache cache, StringBuilder sb, SessionManager session, FormatHelper formatHelper, SimpleDateFormat sdf, int colIndex, MiscInner.PairBool needVehicleDriverWrite, boolean read, boolean printIfDiff, Value regVal, Value apprvdVal, DimConfigInfo dimConfig, boolean printReadAsHidden, SimpleDateFormat sdfWithHHMMSS, Table outputTable, TD td, int objectId, TableObjectInfo driverTableObjectInfo) throws Exception {
		//return true if apprvdVal needs to be printed and apprvdVal was different from regVal
		//TODO - handle links ... currently hardcoded and working only for 90173
		if (td != null)
			td.setContent(Misc.nbspString);
		boolean isDiff = printIfDiff && WorkflowHelper.isDIfferingVal(regVal, apprvdVal);
		DimInfo dimInfo = dimConfig == null || dimConfig.m_dimCalc == null ? null : dimConfig.m_dimCalc.m_dimInfo;
		if (dimInfo == null) {
			sb.append("&nbsp;");
			return false;
		}
		
		
		boolean isStatusDim = dimInfo != null && dimInfo.m_subsetOf == 9000;
		boolean isImg = dimInfo != null && "20515".equals(dimInfo.m_subtype);
		boolean isNestedImg = dimInfo != null && "20517".equals(dimInfo.m_subtype);
		
		boolean isFile = dimInfo != null && "20516".equals(dimInfo.m_subtype);
		boolean isPlayback = dimInfo != null && dimInfo.m_id == 32161;
		if (isPlayback) {
			String width = dimConfig.m_width > 0 ?" width=\""+dimConfig.m_width+"px\" " : "";
			sb.append("<img  src=\"getFile.do?play_back=1&trip_id=").append(regVal).append("\"").append(width).append("/>").append("</a>");
			
			return false;
		}
		
		if (isImg || isFile) {
			String width = dimConfig.m_width > 0 ?" width=\""+dimConfig.m_width+"px\" " : "";
			ColumnMappingHelper colHelper = dimInfo.m_colMap;
			String tbl = colHelper.table;
			String col = colHelper.column;
			
			String idCol = driverTableObjectInfo == null ? "id" : driverTableObjectInfo.getPrimaryIdCol(); //TODO
			int id = objectId;
			boolean fromBackup = regVal != null && regVal.getIntVal() == 2;
			boolean noData = regVal == null || regVal.getIntVal() == 0;
			StringBuilder ref = new StringBuilder();
			if (fromBackup) {
				ref.append(G_BACKUP_BBLOB_FILE).append("/").append(tbl).append("/").append(objectId).append("_").append(col);
				if (isImg)
					ref.append(".jpg");				
			}
			else {
				ref.append("getFile.do?_table=").append(tbl).append("&_column=").append(col).append("&_id_col=").append(idCol).append("&_id=").append(id);
			}
			if (isImg)
				sb.append("<img class='north' onclick='rotateImage(this)' src=\"").append(ref).append("\"").append(width).append("/>");
			if (isFile)
				sb.append("<a href=\"").append(ref).append("\"> Download</a>");
			return false;
		}
		if (isNestedImg) {
		String width = dimConfig.m_width > 0 ?" width=\""+dimConfig.m_width+"px\" " : "";
		ColumnMappingHelper colHelper = dimInfo.m_colMap;
		String tbl = colHelper.table;
		String col = colHelper.column;
		String idCol="";
		 idCol = dimConfig.m_dimCalc.m_dimInfo.m_colMap.idField; //TODO
		int id = objectId;
		boolean fromBackup = regVal != null && regVal.getIntVal() == 2;
		boolean noData = regVal == null || regVal.getIntVal() == 0;
		StringBuilder ref = new StringBuilder();
		if (fromBackup) {
			ref.append(G_BACKUP_BBLOB_FILE).append("/").append(tbl).append("/").append(objectId).append("_").append(col);
			if (isImg)
				ref.append(".jpg");				
		}
		else {
			ref.append("getFile.do?_table=").append(tbl).append("&_column=").append(col).append("&_id_col=").append(idCol).append("&_id=").append(id);
		}
		if (isImg)
			sb.append("<img class='north' onclick='rotateImage(this)' src=\"").append(ref).append("\"").append(width).append("/>");
		//if (isFile)
			sb.append("<a href=\"").append("getFile.do?_table=").append(tbl).append("&_column=").append(col).append("&_id_col=").append(idCol).append("&_id=").append(id).append("\"> ");
		if (isNestedImg)
			sb.append("<img class='north'  src=\"").append(ref).append("\"").append(width).append("/>").append("</a>");
		
		return false;
	}
		int attribType = dimInfo.m_type;
		String varName = "v"+dimInfo.m_id;
		boolean printText = true;
		if (attribType != Cache.STRING_TYPE && attribType != Cache.NUMBER_TYPE && attribType != Cache.DATE_TYPE) {
              printText = false;
         }
		varName = "v"+dimInfo.m_id;
		int defInt = !printText ? PageHeader.getPageDefaultInt( dimConfig, session) : Misc.getUndefInt();
		String defStr = printText ? PageHeader.getPageDefaultString(dimConfig, session) : null;
		// set val as per attributeType here and may be set for other val type
		String setVal = dimConfig.m_set_val;
		if(setVal != null && !"".equals(setVal.trim()) && attribType == Cache.LOV_TYPE)
			regVal = new Value(Misc.getParamAsInt(setVal));
		else if(true){
			
		}
		if (attribType == Cache.LOV_TYPE) {
			if (regVal != null && regVal.isNotNull()) {
				int ival = cache.getParentDimValId(conn, dimInfo, regVal.getIntVal());
				if (ival != regVal.getIntVal())
					regVal = new Value (ival);
			}
			if (apprvdVal != null && apprvdVal.isNotNull()) {
				int ival = cache.getParentDimValId(conn, dimInfo, apprvdVal.getIntVal());
				if (ival != apprvdVal.getIntVal())
					apprvdVal = new Value (ival);
			}
		}
		if (dimConfig.m_hidden) {			
			sb.append("&nbsp;");
		}
		else {
			StringBuilder strForTD = td == null ? null : new StringBuilder();
			if (varName == null) {
				String disp = regVal == null ? null : regVal.toString();
				if (disp == null || disp.length() == 0)
					sb.append("&nbsp;");
				else {
					sb.append(disp);
					if (strForTD != null)
						strForTD.append(disp);
				}
			}
			else {
				String formattedString = null;
				if (!read) {
					if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM)
						needVehicleDriverWrite.first = true;
					if (dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM)
						needVehicleDriverWrite.second = true;
				}
				if (read || dimInfo.getAttribType() != Cache.LOV_TYPE || td != null) {
					if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM) {
						formattedString = regVal == null ? "" : cache.getVehicleDisplayInfo(conn, regVal.m_iVal).first;
					}
					else if (dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM){
						formattedString = regVal == null ? "" : cache.getDriverDisplayInfo(conn, regVal.m_iVal).first;
					}
					else if (dimInfo.m_subsetOf == 90630) {//po
						formattedString = regVal == null ? "" : cache.getPOLineDisplayInfo(conn, regVal.m_iVal).first;
					}
					else if (dimInfo.m_subsetOf == 70201) {
						formattedString = regVal == null ? "" : cache.getDORRDisplayInfo(conn, regVal.m_iVal).first;
					}
					/*else if (dimConfig.m_isDisplay || dimConfig.m_isAutocomplete){
						formattedString = regVal == null ? "" : cache.getDisplayInfo(conn, regVal.m_iVal, dimInfo).first;
					}*/
					else {
						formattedString = regVal == null || regVal.isNull()? "" : regVal.toString(dimInfo, formatHelper == null ? null : formatHelper.multScaleFactors.get(colIndex-1), formatHelper == null ? null : formatHelper.formatters.get(colIndex-1), session, session.getCache(), conn, sdf);
					}
				}
				if (regVal != null) {
					if (regVal.m_type == Cache.NUMBER_TYPE && Misc.isUndef(regVal.getDoubleVal()))
							formattedString = "";
					else if ((regVal.m_type == Cache.LOV_TYPE || regVal.m_type == Cache.LOV_NO_VAL_TYPE || regVal.m_type == Cache.INTEGER_TYPE) && Misc.isUndef(regVal.getIntVal()))
						formattedString = read ? "" : null;
				}
				
				if (strForTD != null)
					strForTD.append(formattedString);
				if (dimInfo.m_id == 90173 && regVal != null && regVal.isNotNull()) {//Hack for scanned images
					formattedString = "<a target='_newTab' href='vehicle_gen_details.jsp?page_context=tr_tptr_template&amp;page_name=Scanned Challan etc&amp;_glb_read=1&amp;_dir_print=0&amp;template_name=tr_tptr_scan_inp.xml&amp;tpr_id="+objectId+"'>"+regVal.toString()+"</a>";
				}

				if (read) {
					//if (printReadAsHidden) {
					if (dimConfig.m_isSelect) {
						if (regVal != null && regVal.isNotNull())
							sb.append("<input name='").append("v").append(dimInfo.m_id).append("' type='checkbox' value='").append(regVal == null ? Misc.getUndefInt() : regVal.m_iVal).append("' />");
						else
							sb.append("&nbsp;");
					}
					else {
						String regValStr = null;
						if (regVal != null) {
							regValStr = regVal.toString(sdfWithHHMMSS);				
						}
						if (dimInfo.m_id == 80452) {
							regValStr = "3";//we dont want to process read only stuff in verification check
						}
						if (regValStr != null && varName != null && regValStr.length() > 0) {
							sb.append("<input type='hidden' name='").append(varName).append("' id='").append(varName).append("' value='").append(regValStr).append("'/>");
						}
						sb.append(formattedString);
					}
					//}
					
				}
				else {
					if (dimConfig.m_isSelect) {
						if (regVal != null && regVal.isNotNull())
							sb.append("<input name='").append("v").append(dimInfo.m_id).append("' type='checkbox' value='").append(regVal == null ? Misc.getUndefInt() : regVal.m_iVal).append("' />");
						else
							sb.append("&nbsp;");
					}
					else if (dimInfo.m_id == 90173) {//HACK for scanned images linking
						if (regVal != null && regVal.isNotNull())
							sb.append(formattedString);
						else
							sb.append(Misc.nbspString);
					}
					else if (dimInfo.m_id == 80452) {
						boolean yesChecked = regVal != null && regVal.m_iVal == 1;
						boolean noChecked = regVal != null && regVal.m_iVal == 2;
						boolean ncChecked = !yesChecked && !noChecked;
						sb.append("<input type='checkbox' value='1' name='v80452' ").append(yesChecked ? " checked='true' ":"").append("onchange='handle80452(this)'/>Matched<br/><input type='checkbox'  name='v80452' value='2' ").append(noChecked ? " checked='true' ":"").append(" onchange='handle80452(this)'/>Not Matched<br/><input type='checkbox' name='v80452' ").append(ncChecked ? " checked='true' ":"").append("  value='3' onchange='handle80452(this)'/>Not Checked");
					}
					else {
						boolean initZero = dimConfig.m_initZero;
						cache.printDimVals(session, conn, session.getUser(), dimInfo, initZero || regVal == null ? Misc.getUndefInt() : regVal.m_iVal, null, sb, varName, true,  "select", false, Misc.getUndefInt(), dimConfig.m_height < 1 ? 1 :dimConfig.m_height, dimConfig.m_width < 0 ? 20 : dimConfig.m_width
			                  , false, null, false, true, read, isStatusDim && dimConfig.m_onChange_handler == null ? "warnOnStatusChange(this)" : dimConfig.m_onChange_handler, null,  Misc.getUndefInt(), Misc.getUndefInt()
			                  , null, initZero ? "" : formattedString,dimConfig.m_isRadio,1, dimConfig);
						boolean printOrigDateTillSec = dimInfo.m_type == Cache.DATE_TYPE && "20506".equals(dimInfo.m_subtype);
						if (dimConfig.putValAsHiddenTooInInpTemplate || printOrigDateTillSec) {
							sb.append("<input type='hidden' name='").append(varName).append("_o' value='");
							if (regVal != null) {
								if (dimInfo.m_type == Cache.LOV_TYPE)
									sb.append(regVal.m_iVal);
								else if (printOrigDateTillSec)
									sb.append(regVal == null ? "" : regVal.toString(sdfWithHHMMSS));
								else
									sb.append(formattedString);
							}
							sb.append("'/>");
						}
					}
				}
			}
			String apprvdValPart = null;
			if (dimInfo.m_id == 90173)
				isDiff = false;
			if (isDiff) {
				if (!read)
					sb.append("<br/>");
				else 
					sb.append("&nbsp;");
				
				sb.append("(Original:");
				if (strForTD != null)
					strForTD.append("(Original:");
				if (apprvdVal != null) {
					if (varName == null) {
						String disp = apprvdVal.toString();
						if (disp == null || disp.length() == 0)
							sb.append("&nbsp;");
						else {
							sb.append(disp);
							if (strForTD != null)
								strForTD.append(disp);
						}
					}
					else {
						String formattedString = null;
						if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM) {
							formattedString = cache.getVehicleDisplayInfo(conn, apprvdVal.m_iVal).first;
						}
						else if (dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM){
							formattedString = cache.getDriverDisplayInfo(conn, apprvdVal.m_iVal).first;
						}
						else {
							formattedString = apprvdVal.toString(dimInfo, formatHelper.multScaleFactors.get(colIndex-1), formatHelper.formatters.get(colIndex-1), session, session.getCache(), conn, sdf);
						}
						if (apprvdVal.m_type == Cache.NUMBER_TYPE && Misc.isUndef(apprvdVal.getDoubleVal()))
							formattedString = "";
						else if ((regVal.m_type == Cache.LOV_TYPE || regVal.m_type == Cache.LOV_NO_VAL_TYPE || regVal.m_type == Cache.INTEGER_TYPE) && Misc.isUndef(regVal.getIntVal()))
							formattedString = "";
						sb.append(formattedString);
						if (strForTD != null)
							strForTD.append(formattedString);
					}
				}
				sb.append(")");
				if (strForTD != null)
					strForTD.append(")");
				
			}
			if (td != null)
				td.setContent(strForTD == null || strForTD.length() == 0 ? Misc.nbspString : strForTD.toString());
			if (dimInfo.m_id == 90173 && !read) {//HACK for delete/reuploading scan
				if (regVal != null && regVal.isNotNull()) {
					sb.append("&nbsp;<a href='#' onclick='doDeleteScan(").append(objectId).append(")'>Delete</a>");
				}
				if (!Misc.isUndef(objectId))
					sb.append("&nbsp;<a href='scanDocument.jsp?tpr_id=").append(objectId).append("' taget='_newTab'>Rescan</a>");
			}
			
		
		}//if not hidden
		return isDiff;
	}
	private static boolean printNestedTable(Connection conn, Cache cache, StringBuilder sb, SessionManager session, FormatHelper formatHelper, SimpleDateFormat sdf, MiscInner.PairBool needVehicleDriverWrite, boolean printDiff
			, boolean read, int upperDimId
			,ArrayList<ArrayList<DimConfigInfo>> nestedCols //single rowd only
			, ArrayList<ArrayList<Value>> reg, ArrayList<ArrayList<Value>> apprvd
			,SimpleDateFormat sdfWithHHMMSS, boolean putMandatory
			,String upperRowAddValidation
			,Table outputTable, TD td, int objectId
			, TableObjectInfo driverTableObjectInfo
			,DimConfigInfo upperDCI
	) throws Exception {
		boolean isDiff = false;
		if (printDiff) {
			isDiff = WorkflowHelper.isDifferingNested(reg, apprvd);
		}
		StringBuilder hiddenVars = new StringBuilder();
		ArrayList<ArrayList<Value>> mimicMultiRowVals = new ArrayList<ArrayList<Value>>();
		mimicMultiRowVals.add(null);
		for (int art=0,arts = isDiff ? 2 : 1; art<arts;art++) {
			boolean thisRead = art == 0 ? read : true;
			ArrayList<ArrayList<Value>> valList = art == 0 ? reg : apprvd;
			if (art == 1) {
				sb.append("Original:<br/>");
			}
			Table nestedOutputTable = null;
			if (outputTable != null) {
				nestedOutputTable = Table.createTable();
				td.setNestedTable(nestedOutputTable);
			}
			printTableHeader(sb, nestedCols, null, session, "d"+upperDimId, thisRead, true, thisRead ? false : putMandatory, 0,false, nestedOutputTable);
			sb.append("<tbody>");
			for (int i=0,is=valList == null ? 0 : valList.size();i<is;i++) {
				ArrayList<Value> dataRow = valList.get(i);
				mimicMultiRowVals.set(0, dataRow);
				sb.append("<tr>");
				TR tr = null;
				if (nestedOutputTable != null) {
					tr = new TR();
					nestedOutputTable.setBody(tr);
				}
				collectHiddenVars(hiddenVars, sdfWithHHMMSS, nestedCols, mimicMultiRowVals, Misc.getUndefInt(), Misc.getUndefInt());
				
				for (int j=0,js = dataRow.size(); j<js;j++) {
					DimConfigInfo dimConfig = nestedCols.get(0).get(j);
					if (dimConfig.m_hidden) {
					//	sb.append("<td class='cn' style='display:none'>&nbsp;</td>");
						//hidden headers will not be printed
						continue;
					}
					boolean cellRead = thisRead;
					if (dimConfig.m_readOnly)
						cellRead = true;
					sb.append("<td ");
					if (dimConfig.m_nowrap)
						sb.append(" nowrap='1' ");
					TD newTd = null;
					if (tr != null) {
						newTd = new TD();
						tr.setRowData(newTd);
					}
					String classid = "cn";
					if (thisRead) {
						int ty = dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null ? Misc.getUndefInt() : dimConfig.m_dimCalc.m_dimInfo.m_id;
						boolean doNumber = ty == Cache.INTEGER_TYPE || ty == Cache.LOV_NO_VAL_TYPE || ty == Cache.NUMBER_TYPE;
						if (doNumber)
							classid = "nn";
					}
					sb.append(" class='").append(classid).append("' ");
					sb.append(">");
					if (newTd != null) {
						newTd.setClassId(CssClassDefinition.getClassIdByClassName(classid, true));
					}
					if (hiddenVars.length() > 0) {
						sb.append(hiddenVars);
						hiddenVars.setLength(0);
					}
				
					if("20517".equals(dimConfig.m_dimCalc.m_dimInfo.m_subtype))
						printSingleValue(conn, cache, sb, session, formatHelper, sdf, j+1, needVehicleDriverWrite, cellRead, false, dataRow.get(j), null, dimConfig, cellRead && !thisRead, sdfWithHHMMSS, nestedOutputTable, newTd, dataRow.get(0).m_iVal, driverTableObjectInfo);
					else
					printSingleValue(conn, cache, sb, session, formatHelper, sdf, j+1, needVehicleDriverWrite, cellRead, false, dataRow.get(j), null, dimConfig, cellRead && !thisRead, sdfWithHHMMSS, nestedOutputTable, newTd, objectId, driverTableObjectInfo);
					sb.append("</td>");
				}
				if (!thisRead) {
					if (upperDCI == null || upperDCI.m_readSpecialControl != DimConfigInfo.G_READ_NODELETE)
						sb.append("<td class='cn'>").append("<img  title= \"Remove row\" src=\"").append(com.ipssi.gen.utils.Misc.G_IMAGES_BASE).append("cancel.gif\" onclick=\"removeRowHelper(event.srcElement)\"/>");
					else
						sb.append("<td class='cn'>&nbsp;");
						
					
					sb.append("</td>");
				}
				sb.append("</tr>");
			}
			if (!thisRead) {//(valList == null) {
			//print blankRow
				ArrayList<DimConfigInfo> dcList = nestedCols.get(0);
				sb.append("<tr>");
				for (int i=0,is = dcList.size();i<is;i++) {
					DimConfigInfo dimConfig = dcList.get(i);
					if (dimConfig.m_hidden)
						continue;
					boolean stillReadOnly = dimConfig.m_readSpecialControl == DimConfigInfo.G_READHIDE_ALWAYS || dimConfig.m_readSpecialControl == DimConfigInfo.G_READHIDE_PRECREATE;
					sb.append("<td ");
					if (dimConfig.m_nowrap)
						sb.append(" nowrap='1' ");
					String classid = "cn";
						
					sb.append(" class='").append(classid).append("' ");
					sb.append(">");
					printSingleValue(conn, cache, sb, session, formatHelper, sdf, i+1, needVehicleDriverWrite, stillReadOnly, false, null, null, dimConfig, false, sdfWithHHMMSS, null, null, Misc.getUndefInt(), driverTableObjectInfo);
					sb.append("</td>");
					
				}
				StringBuilder validateCmd = new StringBuilder();
				validateCmd.append("onclick='");
				if (upperRowAddValidation != null && upperRowAddValidation.length() > 0) {
					validateCmd.append(upperRowAddValidation).append(";");
				}
				validateCmd.append("validateAndAddRow(event)'");

				sb.append("<td class='cn'>").append("<img  title= \"Add row\" src=\"").append(com.ipssi.gen.utils.Misc.G_IMAGES_BASE).append("green_check.gif\" ").append(validateCmd).append(" />");
				sb.append("</tr>");
			}
			
			sb.append("</tbody>");
			sb.append("</table>");
		}
		
		return isDiff;
	}
	//FormatHelper formatHelper = this.getFormatHelper(rows, session);
	//SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
	
	private static void collectHiddenVars(StringBuilder hiddenVars, SimpleDateFormat sdfWithSec, ArrayList<ArrayList<DimConfigInfo>> rows, ArrayList<ArrayList<Value>> data, int _nameDimId, int _addnlDimId) {
		boolean collectedName = _nameDimId < 0;
		boolean collectedAddnl = _addnlDimId < 0;
		for (int i=0,is=rows.size();i<is;i++) {
			ArrayList<DimConfigInfo> row = rows.get(i);
			for (int j=0,js = row.size();j<js;j++) {
				DimConfigInfo dimConfig = row.get(j);
				boolean toHide = false;
				if (dimConfig.m_hidden)
					toHide = true;
				int hideControl = dimConfig.m_hiddenSpecialControl;
				toHide = toHide || hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_POSTCREATE
						;
				if (!toHide && collectedName && collectedAddnl)
					continue;
				Value reg = data == null ? null : data.get(i).get(j);
				DimInfo dimInfo =dimConfig != null && dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null ? dimConfig.m_dimCalc.m_dimInfo : null;
				if (data == null && dimConfig.m_default != null && dimConfig.m_default.length() > 0) {
					if (dimInfo.m_type == Cache.STRING_TYPE) {
						reg = new Value(dimConfig.m_default);
					}
					else if (dimInfo.m_type == Cache.NUMBER_TYPE) {
						double dv = Misc.getParamAsDouble(dimConfig.m_default);
						if (!Misc.isUndef(dv))
						reg = new Value(dv);
					}
					else if (dimInfo.m_type == Cache.DATE_TYPE) {
						int iv = Misc.getParamAsInt(dimConfig.m_default);
						if (!Misc.isUndef(iv)) {
							long ts = System.currentTimeMillis();
							ts += iv*24*3600;
							if (!"20506".equals(dimInfo.m_subtype)) {
								java.util.Date dt = new java.util.Date(ts);
								dt.setHours(0);
								dt.setMinutes(0);
								dt.setSeconds(0);
								ts = dt.getTime();
							}
							reg = new Value(ts);
						}
					}
					else {
						int iv = Misc.getParamAsInt(dimConfig.m_default);
						if (!Misc.isUndef(iv)) {
							reg = new Value(iv);
						}
					}
				}
				String regStr = null;
				 
				
				if (reg != null && dimInfo != null) {
					String varName = "v"+dimInfo.m_id;
					regStr = reg.toString(sdfWithSec);
					if (regStr != null && regStr.length() != 0) {
						if (!toHide)
							hiddenVars.append("<input type='hidden' name='").append(varName).append("' value='").append(regStr).append("'/>");
						
						if (dimInfo.m_id == _nameDimId || dimInfo.m_id == _addnlDimId) {
							hiddenVars.append("<input type='hidden' name='_v").append(dimInfo.m_id).append("' value='").append(regStr).append("'/>");
							if (dimInfo.m_id == _nameDimId)
								collectedName = true;
							if (dimInfo.m_id == _addnlDimId)
								collectedAddnl = true;
						}
						
					}
					
				}
			}//for each col
		}//for each row
	}
	
	private static Value getDefaultValue(Connection conn, Cache cache, SessionManager session, DimConfigInfo dimConfig) throws Exception {
		Value retval = null;
	    DimInfo dimInfo = dimConfig == null || dimConfig.m_dimCalc == null ? null : dimConfig.m_dimCalc.m_dimInfo;
	    if (dimInfo == null)
	    	return null;
	    if (dimInfo.m_type == Cache.LOV_TYPE || dimInfo.m_type == Cache.INTEGER_TYPE) {
	    	int iv = Misc.getParamAsInt(dimConfig.m_default);
	    	boolean is123 = dimInfo.m_descDataDimId == 123;
	    	
	    	if (Misc.isUndef(iv) && is123) {
	    		String sessionParamName = is123 ? "pv123" : "v"+dimInfo.m_id;
	    		String val = session.getAttribute(sessionParamName);//session.getParameter(sessionParamName); //else will get previously used vals ..
		        iv = Misc.getParamAsInt(val);
	    	}
		    if (is123) {
		    	if (Misc.isUndef(iv))
		    		iv = Misc.G_TOP_LEVEL_PORT;
		        User user = session.getUser();
	          	int privToCheckForPort = Misc.getUndefInt();
	            if (dimConfig.m_accessPriv != null) {
	            	 PrivInfo.TagInfo rwTagInfo = session.getCache().getPrivId(dimConfig.m_accessPriv);
	            	 if (rwTagInfo != null)
	            		privToCheckForPort = rwTagInfo.m_write;
	            }
	            if (privToCheckForPort < 0)
	            	privToCheckForPort =   user.getPrivToCheckForOrg(session, session.getParameter("page_context")); //this tells the privilege to use for showing the Org tree
	             if (privToCheckForPort < 0)
	            	privToCheckForPort = 1;
	             iv = user.getUserSpecificDefaultPort(session, iv, privToCheckForPort, dimInfo);
		    }
		    else if (Misc.isUndef(iv)) {
		        iv = dimInfo.getDefaultInt();
		    }
		    return new Value(iv);
	    }
	    else if (dimInfo.m_type == Cache.NUMBER_TYPE) {
	    	double dv = Misc.getParamAsDouble(dimConfig.m_default);
	    	if (Misc.isUndef(dv)) {
	    		dv = dimInfo.getDefaultDouble();
	    	}
	    	return new Value(dv);
	    }
	    else if (dimInfo.m_type == Cache.STRING_TYPE) {
	    	String str = dimConfig.m_default;
	    	if (str == null) {
	    		str = dimInfo.getDefaultString();
	    	}
	    	return new Value(str);
	    }
	    else if (dimInfo.m_type == Cache.DATE_TYPE) {
	    	double iv = Misc.getParamAsDouble(dimConfig.m_default);
	    	if (Misc.isUndef(iv)) {
	    		iv = dimInfo.getDefaultDouble();
	    	}
	    	if (!Misc.isUndef(iv)) {
	    		java.util.Date dt = new java.util.Date();
	    		Misc.addDays(dt, iv);
	    		return new Value(dt.getTime());
	    	}
	    }
	    return null;
	}
	
	private static void printSingleDataRow(Connection conn, Cache cache, StringBuilder sb, SessionManager session, FormatHelper formatHelper, SimpleDateFormat sdf, MiscInner.PairBool needVehicleDriverWrite
			,ArrayList<ArrayList<DimConfigInfo>> rows, boolean doAsMultiRow, boolean printObjectIdHiddenVar, String objectIdParamName
			, WorkflowHelper.QueryHelperNew queryHelper, ArrayList<FormatHelper> nestedFormatHelper
			,ArrayList<ArrayList<Integer>> readWriteInfo
			, int objectId, ArrayList<ArrayList<Value>> regTopRow, ArrayList<ArrayList<Value>> apprvdTopRow
			, ArrayList<ArrayList<ArrayList<Value>>> regNestedData, ArrayList<ArrayList<ArrayList<Value>>> apprvdNestedData
			, boolean doingCommonRow
			,SimpleDateFormat sdfWithHHMMSS
			,boolean glbRead
			,int putAdderRemoveControl //0 no, 1 blank, 2 delete, 3 adder
			,boolean hasWorkflow
			,String preHandler
			,boolean mustPrintDiff
			,int _doingSelectionMode
			,int _nameDimId
			,int _addnlDimId
			,boolean toAddForWorkflowSelection, int workflowId, boolean doWorkflowIdHidden// true if multi object
			,Table outputTable
			, TableObjectInfo driverTableObjectInfo
			) throws Exception {
			boolean doingNew = Misc.isUndef(objectId);
			boolean isDiff = false;
			ArrayList<Integer> toSkipRowIndex = new ArrayList<Integer>();
			int maxCols = getMaxColCountAndSkipRow(rows, toSkipRowIndex, Misc.isUndef(objectId));
			boolean printedAddnlCheckBox = false;
			TR tr = null;
			if (doAsMultiRow) {
				sb.append("<tr _js=\"").append(doingCommonRow ? "c" : "b").append("\">");
				if (outputTable != null) {
					tr = new TR();
					outputTable.setBody(tr);
				}
				if (_doingSelectionMode > 0) {
					sb.append("<td class='cn'>");
				    if (!Misc.isUndef(objectId)) {
						sb.append("<input type='").append(_doingSelectionMode ==1 ? "radio" : "checkbox").append("' name='").append(_doingSelectionMode == 2 ? "select_": "").append(objectIdParamName).append("' onclick='").append(_doingSelectionMode == 2 ? "" : "autoSelect(this)").append("' ");
						sb.append(" value='").append(objectId).append("'" );
						sb.append("/>");
				    }
				    else {
				    	sb.append("&nbsp;");
				    }
					
					sb.append("</td>");
				}
				if (toAddForWorkflowSelection) {
					sb.append("<td class='cn'>");
					if (!Misc.isUndef(workflowId)) {
						sb.append("<input type='").append(doWorkflowIdHidden ? "hidden" : "checkbox").append("' name='workflow_id' value='").append(workflowId).append("' />");
					}
					else {
						sb.append("&nbsp;");
					}
					sb.append("</td>");
				}
				printedAddnlCheckBox = true;
			}
			int colIndex = 0;
			boolean printedObjectIdStuff = false;
			boolean isFirstRow = true;
			StringBuilder hiddenVars = new StringBuilder();
			//collect hidden vars ... while can be printed in situ - becomes messy for last col that may be hidden
			//so instead we collect upfront and print at top
			//if (!doingNew)
			collectHiddenVars(hiddenVars, sdfWithHHMMSS, rows, regTopRow, _nameDimId, _addnlDimId);
			
			for (int i=0,is=rows.size();i<is;i++) {
				ArrayList<DimConfigInfo> row = rows.get(i);
				boolean toSkip = false;
				for (int j=0,js = toSkipRowIndex.size();j<js;j++) {
					if (toSkipRowIndex.get(j) == i) {
						toSkip= true;
						break;
					}
				}
				if (toSkip) {
					colIndex += row.size();
					continue;
				}
				if (!doAsMultiRow) {
					sb.append("<tr ").append(isFirstRow ? doingCommonRow ? " _js=\"c\" " : " _js=\"b\" " : "").append(">");
					if (outputTable != null) {
						tr = new TR();
						outputTable.setBody(tr);
					}
				}
				isFirstRow = false;
				int colsUsed = 0;
				int rowSpansSeen =0;//  will work only at end and spanning till curr
				
				for (int j=0,js = row.size();j<js;j++) {
					DimConfigInfo dimConfig = row.get(j);
					colIndex++;
					boolean toHide = false;
					boolean mergeLabelAndVal=false;
					MiscInner.Pair tyAndIdx = queryHelper == null ? null : queryHelper.dimTypeAndIndexPrimary.get(i).get(j);
					
					if (dimConfig.m_hidden)
						toHide = true;
					int rowSpan = dimConfig.m_rowSpan > 0  ? dimConfig.m_rowSpan : 1;
					if (i+rowSpan > rows.size())
						rowSpan = rows.size()-i;
					int rwHint = readWriteInfo == null ? 2 : readWriteInfo.get(i).get(j);
					// 0-> none, 1 => read, 2 => read/write
					int hideControl = dimConfig.m_hiddenSpecialControl;
					toHide = toHide || (doingNew ? hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_PRECREATE 
																				:
																				hideControl == DimConfigInfo.G_READHIDE_ALWAYS || hideControl == DimConfigInfo.G_READHIDE_POSTCREATE
																				)
																			;
					toHide = rwHint == 0 || toHide;
					if (toHide) {
						continue;
					}
					boolean toRead = glbRead || rwHint == 1;
					if (!toRead) {
						if (dimConfig.m_readOnly)
							toRead = true;
					}
					if (!toRead) {
						int readControl = dimConfig.m_readSpecialControl;
						toRead =  (doingNew ? readControl == DimConfigInfo.G_READHIDE_ALWAYS || readControl == DimConfigInfo.G_READHIDE_PRECREATE 
																					:
																					readControl == DimConfigInfo.G_READHIDE_ALWAYS || readControl == DimConfigInfo.G_READHIDE_POSTCREATE
																					)
																				;
					}
					if (!doAsMultiRow) {
					
						if (dimConfig.m_name == null || dimConfig.m_name.length() == 0) {
							if (dimConfig.m_disp != null && dimConfig.m_disp.length() != 0)
								mergeLabelAndVal = true;
						}
						if (!mergeLabelAndVal) {
							String labelClass = dimConfig.m_labelStyleClass == null || dimConfig.m_labelStyleClass.length() == 0 ? "sh" : dimConfig.m_labelStyleClass;
							sb.append("<td class='").append(labelClass).append("' ");
							
							
							if (dimConfig.m_labelNowrap)
								sb.append("nowrap='1'");
							else if (dimConfig.m_labelWidth > 0)
								sb.append("width='").append(dimConfig.m_labelWidth).append("px'");
							if (rowSpan > 1) {
								sb.append(" rowspan='").append(rowSpan).append("'");
								rowSpansSeen += 1;
							}
							sb.append(">");
						
							TD td = null;
							if (tr != null) {
								td = new TD();
								tr.setRowData(td);
								td.setClassId(CssClassDefinition.getClassIdByClassName(labelClass, doAsMultiRow));
								if (rowSpan > 1)
									td.setRowSpan(rowSpan);
							}
							if (toAddForWorkflowSelection && !printedAddnlCheckBox) {
								if (!Misc.isUndef(workflowId)) {
									sb.append("<input type='").append(doWorkflowIdHidden ? "hidden" : "checkbox").append("' name='workflow_id' value='").append(workflowId).append("' />&nbsp;");
								}
								printedAddnlCheckBox = true;
							}
							boolean mandatory = dimConfig.m_isMandatory && !toRead;
							String mandPrefix = mandatory ? "<span class='tmRequiredFieldAsterisk'>*</span>" : "";
							String labelText = dimConfig.m_name == null || dimConfig.m_name.length() == 0 ? "&nbsp;" : dimConfig.m_name;
							if ("sh".equals(labelClass) && dimConfig.m_name != null && !dimConfig.m_name.endsWith(":"))
								labelText += ":";
							sb.append(mandPrefix).append(labelText);
							if (td != null) {
								td.setContent(labelText == null || labelText.length() == 0 ? Misc.nbspString : labelText);
							}
							sb.append("</td>\n");
							colsUsed++;
						}
					}
					int dataColspan = //dimConfig.m_dataSpan > 0 && dimConfig.m_dataSpan < 1000 ? 1 : dimConfig.m_dataSpan;
						dimConfig.m_dataSpan > 0 ? dimConfig.m_dataSpan : 1;
					
					if (mergeLabelAndVal) {
						dataColspan++;
					}
					if (j == js-1)
						dataColspan = maxCols-colsUsed-rowSpansSeen;
					
					sb.append("<td ");
					TD td = null;
					if (tr != null) {
						td = new TD();
						tr.setRowData(td);
					}
					if (dimConfig.m_nowrap)
						sb.append(" nowrap='1' ");
					if (!doAsMultiRow) {
						if (dataColspan > 1) 
							sb.append(" colspan='").append(dataColspan).append("' ");
						if (rowSpan > 1) //only works at end of col
							sb.append(" rowspan='").append(rowSpan).append("' ");
						if (td != null) {
							td.setColSpan(dataColspan);
							td.setRowSpan(rowSpan);
							
						}
						colsUsed += dataColspan;
						if (rowSpan > 1)
							rowSpansSeen += 1;
					}
					
					String classid = doAsMultiRow ? "cn" :"tn";
					
					if (tyAndIdx == null) {
						DimInfo dimInfo = dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null ?null : dimConfig.m_dimCalc.m_dimInfo;
						int ty = dimInfo == null ? Cache.STRING_TYPE : dimInfo.m_type;
						tyAndIdx = new MiscInner.Pair(ty, 0);
					}
					
					if (toRead && tyAndIdx.first>= 0 && tyAndIdx.second >= 0) {
						int ty = tyAndIdx.first;	
						boolean doNumber = ty == Cache.INTEGER_TYPE || ty == Cache.LOV_NO_VAL_TYPE || ty == Cache.NUMBER_TYPE;
						if (doNumber && doAsMultiRow) {
							classid = "nn";
							
						}
					}
					
					if(dimConfig.m_disp != null && dimConfig.m_disp.length()>0)
					{
						classid = dimConfig.m_valStyleClass;
					}
					sb.append(" class='").append(classid).append("' ");
					sb.append(">");
					if (td != null) {
						td.setClassId(CssClassDefinition.getClassIdByClassName(classid, doAsMultiRow));
					}
					if (!printedObjectIdStuff && _doingSelectionMode == 0) {
						sb.append("<input type='hidden' name='").append(objectIdParamName).append("' value='").append(objectId).append("'/>");
						printedObjectIdStuff = true;
					}
					if (hiddenVars.length() != 0) {
						sb.append(hiddenVars);
						hiddenVars.setLength(0);
					}
					if (tyAndIdx.first >= 0 && tyAndIdx.second >= 0) {
						Value reg = regTopRow == null ? null : regTopRow.get(i).get(j);
						Value appr = apprvdTopRow == null ? null : apprvdTopRow.get(i).get(j);
						if ((reg == null || reg.isNull()) && doingNew && !toRead) {
							reg = InputTemplate.getDefaultValue(conn, cache, session, dimConfig);
						}
						// put code here for forced set val
						
						printSingleValue(conn, cache, sb, session, formatHelper, sdf, colIndex, needVehicleDriverWrite, toRead, (!doAsMultiRow && !doingNew && hasWorkflow) || mustPrintDiff, reg, appr, dimConfig, false, sdfWithHHMMSS, outputTable, td, objectId,driverTableObjectInfo);
					}
					else if (tyAndIdx.first < 0 && tyAndIdx.second >= 0) {
						int idx = tyAndIdx.second;
						int upperDimId = dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null ? dimConfig.m_dimCalc.m_dimInfo.m_id : 1;
						printNestedTable(conn, cache, sb, session, nestedFormatHelper.get(idx), sdf, needVehicleDriverWrite, !doAsMultiRow
								,toRead, upperDimId, dimConfig.m_nestedCols
								,regNestedData.get(idx), apprvdNestedData.get(idx)
								,sdfWithHHMMSS, dimConfig != null && dimConfig.m_isMandatory			
								,dimConfig.m_onChange_handler
								, outputTable, td, objectId
								, driverTableObjectInfo
								, dimConfig
								);
					}
					else {
						//TODO for disp stuff
						if(dimConfig.m_disp != null && dimConfig.m_disp.length()>0)
						{
							sb.append(dimConfig.m_disp);
							if (td != null)
								td.setContent(dimConfig.m_disp);
						}
						else
						{
							if (td != null)
								td.setContent(Misc.nbspString);
							sb.append("&nbsp;");
						}
					}
					sb.append("</td>");
				}//each col
				if (!doAsMultiRow)
					sb.append("</tr>");
			}//each row
			if (doAsMultiRow) {
				if (putAdderRemoveControl == 1) {
					sb.append("<td class='cn'>&nbsp;</td>");
				}
				else if (putAdderRemoveControl == 2) {
					sb.append("<td class='cn'>").append("<img  title= \"Remove row\" src=\"").append(com.ipssi.gen.utils.Misc.G_IMAGES_BASE).append("cancel.gif\" onclick=\"removeRowHelper(event.srcElement)\"/>");
					sb.append("</td>");
				}
				else if (putAdderRemoveControl == 3) {
					StringBuilder validateCmd = new StringBuilder();
					validateCmd.append("onclick='");
					if (preHandler != null && preHandler.length() > 0) {
						validateCmd.append("javascript:var _row=getParentRow(this);").append(preHandler).append(";");
					}
					validateCmd.append("validateAndAddRow(event)'");
					sb.append("<td class='cn'>").append("<img  title= \"Add row\" src=\"").append(com.ipssi.gen.utils.Misc.G_IMAGES_BASE).append("green_check.gif\" ").append(validateCmd).append(" />");
					sb.append("</td>");
				}
				sb.append("</tr>");
			}
	}
	private static void printTableHeader(StringBuilder sb, ArrayList<ArrayList<DimConfigInfo>> rows, String selectCheckBoxVarName, SessionManager session, String tableId, boolean isRead, boolean putAdder, boolean putMandatory, int _doingSelectionMode, boolean toAddForWorkflowSelection, Table outputTable) throws Exception{
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
		String displayLink = null;
		//ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		Cache cache = session.getCache();
		boolean hasMultiple = false;//hasNestedColHeader(rows); ... nested printed in its own header ... at the most in case of all read only does it make sense to print in col ... too complex
		HtmlGenerator.printTableStart(sb, null, HtmlGenerator.styleWithAdornment, tableId, true, isRead, putMandatory);
		sb.append("<thead>");
		sb.append("<tr class='").append(CssClassDefinition.getHtmlCssClass(hasMultiple ? 0 : 1)).append("'>");
		TR tr = null;
		if (outputTable != null) {
			tr = new TR();
			outputTable.setHeader(tr);
			tr.setClassId(hasMultiple ? 0 : 1);
		}
		if (_doingSelectionMode > 0) {//outputTable will not have any inp element
			sb.append("<td >");
			if (hasMultiple)
				sb.append(" rowspan='2' ");
			sb.append("<input type='").append(_doingSelectionMode ==1 ? "radio" : "checkbox").append("' name='").append(_doingSelectionMode == 2 ? "select_": "").append(selectCheckBoxVarName).append("' onclick='").append(_doingSelectionMode == 2 ? "setSelectAll(this)" : "autoSelect(this)").append("' />");
			sb.append("</td>");
		}
		if (toAddForWorkflowSelection) {
			sb.append("<td >");
			if (hasMultiple)
				sb.append(" rowspan='2' ");
			sb.append("Approve/Reject<br/>");
			sb.append("<input type='checkbox' name='select_workflow_id' onclick='setSelectAll(this)'/>");
			sb.append("</td>");
		}
		
		StringBuilder tempSB = new StringBuilder();
		for (int i=0,is = rows.size(); i<is; i++) {
			ArrayList<DimConfigInfo> row = rows.get(i);
			for (int j=0,js = row.size(); j<js; j++) {
				DimConfigInfo dci = row.get(j); 
				if (dci == null || dci.m_hidden)
					continue;
				TD td = null;
				if (tr != null) {
					td = new TD();
					tr.setRowData(td);
				}
				sb.append("<td ");
			
				boolean isMultiRow = dci.m_nestedCols != null && dci.m_nestedCols.get(0).size() > 1;
				if (hasMultiple && !isMultiRow) {
					sb.append(" rowspan='2' ");
					if (td != null)
						td.setRowSpan(2);
				}
//				if(dci.m_hidden) {
//					sb.append("style='display:none'");
//				}
				DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
				int attribType = dimInfo != null ? dimInfo.getAttribType() : cache.STRING_TYPE;
				boolean doDate = attribType == cache.DATE_TYPE;
				boolean doInterval = dimInfo != null && "20510".equals(dimInfo.m_subtype);
				int numColspan = 1;//dci.m_nestedCols != null ? dci.m_nestedCols.get(0).size() : 1;
				if (numColspan > 1)
					sb.append(" colspan = '").append(numColspan).append("' ");
				String contentType =  doDate ? "date" : doInterval ? "interval" : attribType == Cache.NUMBER_TYPE || attribType == Cache.INTEGER_TYPE  ? "num" : "text";
				sb.append(" dt_type='").append(contentType).append("'");
				if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0) {
					tempSB.setLength(0);
					FrontPageInfo.getAllListMenuDiv(session, dci, tempSB, true, null);
					if (tempSB.length() == 0) {
					//no priv
						sb.append("style='display:none'");
					}
				}
				boolean ignore = dci.m_isSelect || (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0);
				boolean doSortLink = numColspan <= 1 && !ignore;
				String name =  dci.m_name;
				boolean mandatory = dci.m_isMandatory;
				String mandPrefix = mandatory ? "<span style='font-weight:bold'> * </span>" : "";
				if (doSortLink){
					displayLink = "<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>" +(name != null && name.length() != 0 ? name : "&nbsp;")+"</a>";
				}
				else {
					displayLink = name;
				}
				
				if (dci.m_isSelect){
					String actSelectCheckBoxName = selectCheckBoxVarName;
					if (dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null) {
						actSelectCheckBoxName = "v"+dci.m_dimCalc.m_dimInfo;
					}
					displayLink = (dci.m_name != null && dci.m_name.length() > 0 ? dci.m_name+"<br/>" : "")+"<input type='checkbox' name='select_"+actSelectCheckBoxName+"' class='tn' onclick='setSelectAll(this)'/>";
				}
				if (td != null)
					td.setContent(name == null || name.length() == 0 ? Misc.nbspString : name);
				sb.append(">").append(displayLink).append(mandPrefix).append("</td>");
			}//template col
		}//template row
		if (!isRead && putAdder) {
			sb.append("<td ");
			if (hasMultiple)
				sb.append(" rowspan='2' ");
			sb.append(">&nbsp;</td>");
		}
		if (hasMultiple) {
			sb.append("</tr>");
			sb.append("<tr class='").append(CssClassDefinition.getHtmlCssClass(hasMultiple ? 0 : 1)).append("'>");
			if (outputTable != null) {
				tr = new TR();
				outputTable.setHeader(tr);
				tr.setClassId(hasMultiple ? 0 : 1);
			}
			for (int i=0,is = rows.size(); i<is; i++) {
				ArrayList<DimConfigInfo> row = rows.get(i);
				for (int j=0,js = row.size(); j<js; j++) {
					DimConfigInfo upperdci = row.get(j); 
					if (upperdci == null )
						continue;
					boolean isMultiRow = upperdci.m_nestedCols != null && upperdci.m_nestedCols.get(0).size() > 1;
					if (!isMultiRow)
						continue;
					for (int k=0,ks=upperdci.m_nestedCols.get(0).size(); k<ks;k++) {
						DimConfigInfo dci = upperdci.m_nestedCols.get(0).get(k); 
						if (dci == null || dci.m_hidden)
							continue;
						sb.append("<td ");
						//if(dci.m_hidden) {
						//	sb.append("style='display:none'");
						//}
						TD td = null;
						if (tr != null) {
							td = new TD();
							tr.setRowData(td);
						}
						DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
						int attribType = dimInfo != null ? dimInfo.getAttribType() : cache.STRING_TYPE;
						boolean doDate = attribType == cache.DATE_TYPE;
						boolean doInterval = dimInfo != null && "20510".equals(dimInfo.m_subtype);
						int numColspan = dci.m_nestedCols != null ? dci.m_nestedCols.get(0).size() : 1;
						if (numColspan > 1)
							sb.append(" colspan = '").append(numColspan).append("' ");
						String contentType =  doDate ? "date" : doInterval ? "interval" : attribType == Cache.NUMBER_TYPE || attribType == Cache.INTEGER_TYPE  ? "num" : "text";
						sb.append(" dt_type='").append(contentType).append("'");
						if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0) {
							tempSB.setLength(0);
							FrontPageInfo.getAllListMenuDiv(session, dci, tempSB, true, null);
							if (tempSB.length() == 0) {
							//no priv
								sb.append("style='display:none'");
							}
						}
						boolean ignore = dci.m_isSelect || (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0);
						boolean doSortLink = numColspan > 1 || ignore;
						String name =  dci.m_name;
						if (doSortLink){
							displayLink = "<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>" +(name != null && name.length() != 0 ? name : "&nbsp;")+"</a>";
						}
						else {
							displayLink = name;
						}
						
						if (dci.m_isSelect){
							String actSelectCheckBoxName = selectCheckBoxVarName;
							if (dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null) {
								actSelectCheckBoxName = "v"+dci.m_dimCalc.m_dimInfo;
							}
							displayLink = (dci.m_name != null && dci.m_name.length() > 0 ? dci.m_name+"<br/>" : "")+"<input type='checkbox' name='select_"+actSelectCheckBoxName+"' class='tn' onclick='setSelectAll(this)'/>";							
						}
						if (td != null)
							td.setContent(name == null || name.length() == 0 ? Misc.nbspString : name);
						sb.append(">").append(displayLink).append("</td>");
					}//for each nested col
				}//for each template col
			}//for each template row
		}//if doing multiple
		sb.append("</tr>");
		sb.append("</thead>");

	}
	
	
	//specialFilterType
	//SpecialFilterValue
	//ObjectTypeId
	//ObjectIds
	
	
	private static void filterIds(Connection conn, ArrayList<Integer> objectIds,String query){
		if(objectIds == null || objectIds.size() <= 0)
			return;
		StringBuilder sb = null;
		for(Integer i : objectIds){
			if(sb == null){
				sb = new StringBuilder();
			}else{
				sb.append(",");
			}
			sb.append(i);
		}
		objectIds.clear();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement(query.replaceAll("@", sb.toString()));
			rs = ps.executeQuery();
			while(rs.next()){
				objectIds.add(Misc.getRsetInt(rs, 1) );
			}
			rs.close();
			ps.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static void applySpecialFilter(SessionManager session, ArrayList<Integer> objectIds){
         int specialFilterType = Misc.getParamAsInt(session.getParameter("specialFilterType"));
		 //int specialFilterValue = Misc.getParamAsInt(session.getParameter("specialFilterValue"));
		 switch (specialFilterType) {
		case Constant.FILTER.TYPE.RR_FILTER:
				filterIds(session.getConnection(), objectIds,Constant.FILTER.QUERY.RR_FILTER_QUERY);
			break;
		case Constant.FILTER.TYPE.TPR_AUDIT_FILTER:
				filterIds(session.getConnection(), objectIds,Constant.FILTER.QUERY.TPR_AUDIT_FILTER_QUERY);
			break;
		default:
			break;
		}
	}
	//TODO for Flat tables
	static public boolean  todoSpecialActionCases(SessionManager session) {
		//currently for rateFreeze
		String freezeDateStr = session.getParameter("v94969");
		if (freezeDateStr != null)
			freezeDateStr = freezeDateStr.trim();
		if (freezeDateStr != null && freezeDateStr.length() > 0)
			return true;
		return false;
	}
	static public String handleSpecialActionCases(SessionManager session, ArrayList<Integer> objectIds) throws Exception {
		//currently for rateFreeze
		StringBuilder errMsg = null;
		String freezeDateStr = session.getParameter("v94969");
		if (freezeDateStr != null)
			freezeDateStr = freezeDateStr.trim();
		if (freezeDateStr != null && freezeDateStr.length() > 0) {
			java.util.Date freezeDate = Misc.getParamAsDateFull(freezeDateStr);
			if (freezeDate != null && objectIds != null && objectIds.size() > 0) {
				errMsg = doRateFreeze(session.getConnection(), freezeDate, objectIds);
			}
		}
		return errMsg == null || errMsg.length() == 0? null : errMsg.toString();
	}
	
	static private class FreezeRateHelper {
		public java.sql.Timestamp pre;
		public java.sql.Timestamp post;
		public java.sql.Timestamp currStart;
		public java.sql.Timestamp currEnd;
		public int currId;
		public String doNumber;
		public int doId;
		public FreezeRateHelper(int doId, String doNumber, java.sql.Timestamp pre, java.sql.Timestamp post, java.sql.Timestamp currStart, java.sql.Timestamp currEnd, int currId) {
			this.doId = doId;
			this.doNumber = doNumber;
			this.pre = pre;
			this.post = post;
			this.currStart = currStart;
			this.currEnd = currEnd;
			this.currId = currId;
		}
	}
	static private StringBuilder  doRateFreeze(Connection conn, Date freezeDate, ArrayList<Integer> objectIds) throws Exception {
		StringBuilder errSB = new StringBuilder();
		//for each doId .. get imm ending prior to this, imm start after this and covering 
		StringBuilder sb = new StringBuilder();
		sb.append("select mdd.id, mdd.do_number, max(mdhpre.end), min(mdhpost.start), mdhcov.start, mdhcov.end, mdhcov.id from mines_do_details mdd left outer join mines_do_details_hist mdhpre on (mdhpre.do_number = mdd.do_number and mdhpre.end <= ?) ")
		.append(" left outer join mines_do_details_hist mdhpost on (mdhpost.do_number = mdd.do_number and mdhpost.start > ?) ")
		.append(" left outer join mines_do_details_hist mdhcov on (mdhcov.do_number = mdd.do_number and mdhcov.start <= ? and mdhcov.end > ?) ")
		.append(" where mdd.id in (");
		Misc.convertInListToStr(objectIds, sb);
		sb.append(") group by mdd.id, mdd.do_number,mdhcov.start, mdhcov.end, mdhcov.id ");
		PreparedStatement ps = conn.prepareStatement(sb.toString());
		sb.setLength(0);
		java.sql.Timestamp frst =Misc.utilToSqlDate(freezeDate); 
		ps.setTimestamp(1,frst);
		ps.setTimestamp(2,frst);
		ps.setTimestamp(3,frst);
		ps.setTimestamp(4,frst);
		ResultSet rs = ps.executeQuery();
		ArrayList<FreezeRateHelper> freezeRateList = new ArrayList<FreezeRateHelper>();
		int predoId = Misc.getUndefInt();
		while (rs.next()) {
			FreezeRateHelper entry = new FreezeRateHelper(rs.getInt(1), rs.getString(2), rs.getTimestamp(3), rs.getTimestamp(4), rs.getTimestamp(5), rs.getTimestamp(6), Misc.getRsetInt(rs, 7));
			if (entry.doId != predoId) {
				freezeRateList.add(entry);
			}
			predoId = entry.doId;
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		java.util.Date nintyDate = new java.util.Date(90,0,1);
		java.sql.Timestamp nintyTS = Misc.utilToSqlDate(nintyDate);
		//get default dmf and nmet 
		PreparedStatement ps2 = conn.prepareStatement(" insert into mines_do_details_hist(do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person,grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge,dump_charge,stc_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,status,created_on,updated_on,updated_by,port_node_id,created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1,prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code,delivery_point_code,delivery_point,material,royalty_charge,transport_mode,validity_date,prefered_wb_1_qty,prefered_wb_2_qty,prefered_wb_3_qty,prefered_wb_4_qty,min_retry_hours,product_code,type_of_release,release_priority,lock_status,allocation_approval_status,lock_changed_at,do_type,int_field1,int_field2,int_field3,str_field1,str_field2,str_field3,date_field1,date_field2,date_field3,double_field1,double_field2,double_field3,origianl_qty,taxation_type,other_charges,sgst_rate,cgst_rate,igst_rate,state_compensation_cess,other_charges1_pre_tax_permt,other_charges2_pre_tax_permt,other_charges1_post_tax_permt,other_charges2_post_tax_permt,gst_cutover_date,tot_value_paid,gst_autoadjustment_done,tot_value_gst,arv_id,consginee,consginee_address,consginee_state,tot_value,sadak_tax,area_do_number,alert_phone_one,alert_phone_two,alert_phone_three,alert_phone_four,dmf,nmet,start,end) "+ 
		"  ( "+
			"	  select do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person,grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge,dump_charge,stc_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,mines_do_details.status,mines_do_details.created_on,mines_do_details.updated_on,mines_do_details.updated_by,mines_do_details.port_node_id,mines_do_details.created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1,prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code,delivery_point_code,delivery_point,material,royalty_charge,transport_mode,validity_date,prefered_wb_1_qty,prefered_wb_2_qty,prefered_wb_3_qty,prefered_wb_4_qty,mines_do_details.min_retry_hours,product_code,type_of_release,release_priority,lock_status,allocation_approval_status,lock_changed_at,do_type,mines_do_details.int_field1,mines_do_details.int_field2,mines_do_details.int_field3,mines_do_details.str_field1,mines_do_details.str_field2,mines_do_details.str_field3,mines_do_details.date_field1,mines_do_details.date_field2,mines_do_details.date_field3,mines_do_details.double_field1,mines_do_details.double_field2,mines_do_details.double_field3,origianl_qty,taxation_type,other_charges,sgst_rate,cgst_rate,igst_rate,state_compensation_cess,other_charges1_pre_tax_permt,other_charges2_pre_tax_permt,other_charges1_post_tax_permt,other_charges2_post_tax_permt,gst_cutover_date,tot_value_paid,gst_autoadjustment_done,tot_value_gst,arv_id,consginee,consginee_address,consginee_state,tot_value,sadak_tax,area_do_number,alert_phone_one,alert_phone_two,alert_phone_three,alert_phone_four,(case when dmf is not null then dmf when md2.dmf_rate is not null then md2.dmf_rate when md1.dmf_rate is not null then md1.dmf_rate else 30 end) "+
			"	     ,(case when nmet is not null then nmet when md2.nmet_rate is not null then md2.nmet_rate when md1.nmet_rate is not null then md1.nmet_rate else 2 end) "+
			"	     ,?,? "+ 
			"	      from mines_do_details left outer join mines_details md1 on (md1.sn = mines_do_details.source_code) left outer join mines_details md2 on (md1.parent_mines_code = md2.sn) where mines_do_details.id=? "+
			"	  ) "
			);

		for (int i=0,is=freezeRateList.size(); i<is; i++) {
			FreezeRateHelper entry = freezeRateList.get(i);
			//if pre exists and post exist then New entry's start = preSend and end = freeze 
			//if post exists
			java.sql.Timestamp newStartTs =Misc.longToSqlDate(System.currentTimeMillis());
			java.sql.Timestamp newEndTs = frst;
			if (frst.equals(entry.pre)) {
				//do nothing
				if (errSB.length() != 0) {
					errSB.append("<br/>");
				}
				errSB.append("DO:").append(entry.doNumber).append(" already has rate frozen ending at date. Please goto hist and change there ");
				continue;
			}
			if (!Misc.isUndef(entry.currId)) {
	            //NEW entry's start = curr start and end = freeze and old'curr start = freeze
				if (entry.currEnd.equals(frst)) {
					if (errSB.length() != 0) {
						errSB.append("<br/>");
					}
					errSB.append("DO:").append(entry.doNumber).append(" already has rate frozen ending at date. Please goto hist and change there ");
					continue;
				}
				else {
					newStartTs = entry.currStart;
					newEndTs = frst;
					ps = conn.prepareStatement("update mines_do_details_hist set start = ? where id = ?");
					ps.setTimestamp(1, frst);
					ps.setInt(2, entry.currId);
					ps.executeUpdate();
					ps = Misc.closePS(ps);
				}
			}
			else if (entry.pre != null && entry.post != null) {//shouldnt happen
				//NEW:  start - pre, end = freeze. Post record - start = freeze
				newStartTs = entry.pre;
				newEndTs = frst;
				ps = conn.prepareStatement("update mines_do_details_hist set start = ? where do_number=? and start = ?");
				ps.setTimestamp(1, frst);
				ps.setString(2, entry.doNumber);
				ps.setTimestamp(3, entry.post);
				ps.executeUpdate();
				ps = Misc.closePS(ps);
			}
			else if (entry.pre != null) {
				//NEW:  start - pre, end = freeze. 
				newStartTs = entry.pre;
				newEndTs = frst;
			}
			else if (entry.post != null) {
				//NEW:  start - pre, end = freeze. Post record - start = freeze
				newStartTs = nintyTS;
				newEndTs = frst;
				ps = conn.prepareStatement("update mines_do_details_hist set start = ? where do_number=? and start = ?");
				ps.setTimestamp(1, frst);
				ps.setString(2, entry.doNumber);
				ps.setTimestamp(3, entry.post);
				ps.executeUpdate();
				ps = Misc.closePS(ps);
			}
			else {
				newStartTs = nintyTS;
				newEndTs = frst;
			
			}
			//get default dmf/nmet
			if (newStartTs.equals(newEndTs))
				continue;
			ps2.setTimestamp(1, newStartTs);
			ps2.setTimestamp(2, newEndTs);
			ps2.setInt(3, entry.doId);
			ps2.addBatch();
		}
		ps2.executeBatch();
		ps2 = Misc.closePS(ps2);
		return errSB;
	}
	
			
}

