package com.ipssi.input;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;

public class Customization {
	/*

	public static void printCustomDimConfig(Connection conn, DimConfigInfo dimConfig, int objectId, InputTemplate inputTemplate, String customBlock, boolean toRead) throws Exception {
		if ("vehicle_access_group".equals(customBlock)) {
			printAccessGroup(conn, dimConfig, objectId, inputTemplate, customBlock, toRead);	
		}
		if ("training".equals(customBlock)) {
			printDriverTraining(conn, dimConfig, objectId, inputTemplate, customBlock, toRead);				
		}
		if ("skills".equals(customBlock)) {
			printDriverSkills(conn, dimConfig, objectId, inputTemplate, customBlock, toRead);	

		}
	}
	
	private static void printDriverTraining(Connection conn, DimConfigInfo dimConfig, int objectId, InputTemplate inputTemplate, String customBlock, boolean toRead) throws Exception {
		
	}
	private static void printAccessGroup(Connection conn, DimConfigInfo dimConfig, int objectId, InputTemplate inputTemplate, String customBlock, boolean toRead) throws Exception {
		
	}
	private static void printDriverSkills(Connection conn, DimConfigInfo dimConfig, int objectId, InputTemplate inputTemplate, String customBlock, boolean toRead) throws Exception {
	
	}
	
	private static void genericPrinter(Connection conn, SessionManager session, String tableId, StringBuilder sb, ArrayList<String> header, ArrayList<DimInfo> dimList, ResultSet rs, boolean doRead) throws Exception {
		sb.append("<table  id='").append(tableId).append("'  border='1' cellspacing='0' cellpadding='1' bordercolor='#003366'>");
		if (header != null && header.size() > 0) {
			sb.append("<THEAD><TR>\n");
			for (int i=0,is = header.size()+(doRead ? 0 : 1);i<is;i++) {
				sb.append("<td class='tshc'>").append(i < header.size() ? header.get(i):"&nbsp;").append("</td>");
			}
			sb.append("\n</tr></thead>");
		}
		ResultInfo.Value value = new ResultInfo.Value();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		
		while (rs.next()) {
			for (int i=0,is = dimList.size()+(doRead ? 0 : 1);i<is;i++) {
				if (i<dimList.size()) {
					DimInfo dimInfo = dimList.get(i);
					int attribType = dimInfo.getAttribType();
					if (attribType == Cache.STRING_TYPE)
						value.setValue(rs.getString(i+1));
					else if (attribType == Cache.DATE_TYPE)
						value.setValue(Misc.sqlToUtilDate(rs.getTimestamp(i+1)));
					else if (attribType == Cache.NUMBER_TYPE)
						value.setValue( Misc.getRsetDouble(rs, i+1));
					else
						value.setValue( Misc.getRsetInt(rs, i+1));
					String fmtVal = null;
					if (doRead || attribType == Cache.STRING_TYPE || attribType == Cache.INTEGER_TYPE || attribType == Cache.DATE_TYPE || attribType == Cache.NUMBER_TYPE) {
						fmtVal = value.toString(dimInfo, null, null, session, session.getCache(), conn, sdf);
					}
					sb.append("<td class='cn'>");
					
					sb.append("</td>");
				}
				else {
				}
			}
		}
		if (!doRead) {
			
		}
	
		boolean accessToPrinted = false;

		for(int ctr = 0, ctrs = vehCust == null ? 0 : vehCust.size(); ctr < ctrs; ctr++){
		   accessToPrinted = true;
		   InnerMap imap = (InnerMap)vehCust.get(ctr);
	%>
	      <tr>
	        <td nowrap="nowrap" class='cn'><%					tempBuf.setLength(0);
		_cache.printDimVals(_dbConnection, _user, portDimInfo, imap.getId(), null, tempBuf, "accessTo", false,  null, 
				false, privIdForOrg, 1, 20, false, null, false, true, true, "handleChangeAccessToOrg(this)",
				null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo);
					          out.println(tempBuf);
	%>        </td>
	        <td class='cn'><img  <%= !imap.getCanRemove() ? "style='display:none'": ""%> title= "Remove row" src="<%= com.ipssi.gen.utils.Misc.G_IMAGES_BASE %>cancel.gif" onclick="removeRowAccessTo(this)"/>
	            <%
																if (ctr == vehCust.size()-1) {
																%>
	            <img title='Add  Another Row' src='<%= com.ipssi.gen.utils.Misc.G_IMAGES_BASE %>add.gif' onclick='addRowSpecial(this)' />
	            <%
																}
															%>        </td>
	      </tr>
	      <%
	                          } //end of for loop for list of customer access groups available for the vehicle         
	%>
	      <tr <%= accessToPrinted ? "style='display:none'" : "" %> >
	        <td nowrap="nowrap" class='cn'><%					tempBuf.setLength(0);
		_cache.printDimVals(_dbConnection, _user, portDimInfo, Misc.getUndefInt(), null, tempBuf, "accessTo", false,  null, 
				false, privIdForOrg, 1, 20, false, null, false, true, false, "handleChangeAccessToOrg(this)",
				null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo);
					          out.println(tempBuf);
	%>        </td>
	        <td class='cn'><img title='Add another row' src='<%= com.ipssi.gen.utils.Misc.G_IMAGES_BASE %>add.gif'  onclick='addRowAccessToRegular(this, &quot;accessTo&quot;)'/></td>
	      </tr>
	    </table>
	}
	*/
}
