package com.ipssi.reporting.trip;

import java.util.ArrayList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.ipssi.android.InspectionQuestion;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;

public class JasonGenerator {

	public static JSONArray printMultiJason(Table table, StringBuilder sb,SessionManager _session) {
		JSONArray retval = new JSONArray();
		ArrayList<TD> rowData = null;
		ArrayList<TR> header = table.getHeader();
		ArrayList<TR> body = table.getBody();
		for (int k = 0, size = header == null ? 0 : header.size(); k < size; k++) {
			TR tr = header.get(k);
			for (int i = 0, is = tr.getRowData() == null ? 0 : tr.getRowData()
					.size(); i < is; i++) {
				if (tr.getRowData().get(i).getColSpan() > 1)
					continue;
				if (rowData == null)
					rowData = new ArrayList<TD>();
				rowData.add(tr.getRowData().get(i));
			}
		}
		boolean noHeader = (header == null || header.size() == 0) ? true: false;
		ArrayList<String> keyList = new ArrayList<String>();
		int keyListLen = Misc.getUndefInt();
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			JSONObject jsonObject = new JSONObject();
			String key = " ";
			JSONArray value = null;
			for (int col = 0, size = tdArr == null ? 0 : tdArr.size(); col < size; col++) {
				try {
					TD td = tdArr.get(col);
					boolean hasNestedTable = (td != null && td.getNestedTable() != null) ? true: false;
					String colVal = "";
					JSONArray tempColVal = null;
					JSONArray nestedJson = null;
					if (hasNestedTable) {
						StringBuilder nestedSB = new StringBuilder();
						nestedJson = JasonGenerator.printJason(td.getNestedTable(), nestedSB, _session);
						tempColVal = nestedJson;
						colVal = nestedJson.toString();
					} else {
						colVal = td.getContent();
						tempColVal = getTDJasonObject(td, sb);
					}
					if (row % 2 == 0 && col == 0) {
						keyList = new ArrayList<String>();
					}
					if (row % 2 == 0) {
						keyList.add(colVal);
						keyListLen = keyList.size();
						continue;
					}
					if (noHeader && (col % 2 == 0) && keyListLen > 0) {
						key = tempColVal + "  ";
						key = keyList.get((col / 2));
						continue;
					}
					if (noHeader && (col % 2 == 1)) {
						value = tempColVal;
						if (Misc.nbspString.equalsIgnoreCase(key))
							key = "";
						jsonObject.putOpt(key != null ? key.trim() : key,hasNestedTable ? nestedJson : value);
						continue;
					}
					key = rowData.get(col).getContent();
					value = tempColVal;
					if (Misc.nbspString.equalsIgnoreCase(key))
						key = "";
					jsonObject.putOpt(key != null ? key.trim() : key,hasNestedTable ? nestedJson : value);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			retval.put(jsonObject);
			tdArr=null;
		}
		rowData = null;
		header = null;
		body = null;
		return retval;
	}

	public static JSONArray printJason(Table table, StringBuilder sb,SessionManager _session) {
		ArrayList<TD> rowData = null;
		ArrayList<TR> header = table.getHeader();
		ArrayList<TR> body = table.getBody();
		StringBuilder headerSB = null;
		JSONArray retval = new JSONArray();
		if (header == null || header.size() < 1) {
			StringBuilder headerString = new StringBuilder();
			String lineBreak = "";
			boolean isRowComplete = false;
			JSONArray array = new JSONArray();
			JSONObject object = new JSONObject();
			JSONArray innerArray = new JSONArray();
			String key = "";
			for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
				ArrayList<TD> tdArr = body.get(row).getRowData();
				for (int col = 0, colSize = tdArr == null ? 0 : tdArr.size(); col < colSize; col++) {
					TD td = tdArr.get(col);
					if ((col + 1) % 2 == 1) {
						if ((lineBreak.length()==0 || !lineBreak.equalsIgnoreCase(td.getContent())&& td.getContent()!=null&& !isRowComplete)) {
							headerString.append(headerString.length()>0?";":"");
							headerString.append(td.getContent());
						}
						key = td.getContent();
					} else {
						try {
							innerArray = getTDJasonObject(td, null);
							object.put(key, innerArray);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					if (lineBreak.equalsIgnoreCase(td.getContent())) {
						isRowComplete = true;
						array.put(object);
						object = new JSONObject();
					}
					if (row == 0 && col == 0) {
						lineBreak = td.getContent();
					}
				}
			}
			JSONObject headerObject = new JSONObject();
			try {
				headerObject.put("header", headerString);
				array.put(0, headerObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return array;
		}
		int size = header.size();
		TR firstRowHeader = null;
		TR secondRowHeader = null;
		ArrayList<TD> firstRowHeaderTD = null;
		ArrayList<TD> secondRowHeaderTD = null;
		ArrayList<TD> mergedRowHeaderTD = new ArrayList<TD>();
		if (size > 0)
			firstRowHeader = header.get(0);
		if (size > 1)
			secondRowHeader = header.get(1);
		if (firstRowHeader != null && firstRowHeader.getRowData() != null && firstRowHeader.getRowData().size() > 0)
			firstRowHeaderTD = firstRowHeader.getRowData();
		if (secondRowHeader != null && secondRowHeader.getRowData() != null && secondRowHeader.getRowData().size() > 0)
			secondRowHeaderTD = secondRowHeader.getRowData();
		headerSB = new StringBuilder();
		int secIndex = 0;
		if (firstRowHeaderTD != null) {
			for (int i = 0, firstSize = firstRowHeaderTD.size(); i < firstSize; i++) {
				int colSpan = firstRowHeaderTD.get(i).getColSpan();
				if (colSpan > 1 && secondRowHeaderTD != null) {
					colSpan += secIndex;
					for (int j = secIndex, secSize = secondRowHeaderTD.size(); j < colSpan && j < secSize; j++) {
						headerSB.append((headerSB.length() == 0 ? "" : ";")	+ secondRowHeaderTD.get(j).getContent());
						mergedRowHeaderTD.add(secondRowHeaderTD.get(j));
						secIndex++;
					}
				} else {
					mergedRowHeaderTD.add(firstRowHeaderTD.get(i));
					headerSB.append((headerSB.length() == 0 ? "" : ";")+ firstRowHeaderTD.get(i).getContent());
				}
			}
		}
		rowData = mergedRowHeaderTD;
		firstRowHeaderTD = null;
		secondRowHeaderTD = null;
		mergedRowHeaderTD = null;
		firstRowHeader = null;
		secondRowHeader = null;
		boolean hasHeader = false;
		boolean noHeader = (header == null || header.size() == 0) ? true: false;
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			JSONObject jsonObject = new JSONObject();
			String key = "";
			JSONArray value = null;
			int colSize = tdArr == null || rowData == null ? 0 : tdArr.size() < rowData.size() ? tdArr.size() : rowData.size();
			for (int col = 0; col < colSize; col++) {
				try {
					TD td = tdArr.get(col);
					boolean hasNestedTable = (td != null && td.getNestedTable() != null) ? true: false;
					String colVal = "";
					JSONArray tempColVal = null;
					JSONArray nestedJson = null;
					if (hasNestedTable) {
						StringBuilder nestedSB = new StringBuilder();
						nestedJson = JasonGenerator.printJason(td.getNestedTable(), nestedSB, _session);
						tempColVal = nestedJson;
						colVal = nestedJson.toString();
					} else {
						colVal = td.getContent();
						tempColVal = getTDJasonObject(td, sb);
					}
					if (noHeader && (col % 2 == 0)) {
						key = colVal + "";
						continue;
					}
					if (noHeader && (col % 2 == 1)) {
						value = tempColVal;
						if (Misc.nbspString.equalsIgnoreCase(key))
							key = "";
						try {
							if (headerSB != null && !"".equals(headerSB.toString()) && !hasHeader) {
								JSONObject objectHeader = new JSONObject();
								objectHeader.putOpt("header", headerSB.toString());
								retval.put(objectHeader);
								hasHeader = true;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						jsonObject.putOpt(key != null ? key.trim() : key, value);
						continue;
					}
					key = rowData.get(col).getContent();
					value = tempColVal;
					if (Misc.nbspString.equalsIgnoreCase(key))
						key = "";
					try {
						if (headerSB != null && !"".equals(headerSB.toString())	&& !hasHeader) {
							JSONObject objectHeader = new JSONObject();
							objectHeader.putOpt("header", headerSB.toString());
							retval.put(objectHeader);
							hasHeader = true;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					jsonObject.putOpt(key != null ? key.trim() : key, value);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			retval.put(jsonObject);
			tdArr=null;
		}
		rowData = null;
		header = null;
		body = null;
		headerSB= null;
		System.out.println("printJason() sb: "+ retval.toString());
		return retval;
	}

	public static JSONArray getTDJasonObject(TD td, StringBuilder sb) throws JSONException {
		JSONArray retval = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		jsonObject.putOpt("id", td.getId());
		jsonObject.putOpt("content", Misc.nbspString.equalsIgnoreCase(td.getContent()) ? "" : td.getContent());
		String linkVal = td.getLinkAPart() != null ? td.getLinkAPart().replaceAll("\"", "'") : td.getLinkAPart();
		if (linkVal != null)
			linkVal = linkVal.substring(linkVal.indexOf("href='"), linkVal.indexOf(">"));
		jsonObject.putOpt("link", linkVal);
		jsonObject.putOpt("hidden", td.getHidden());
		jsonObject.putOpt("class", td.getClassId());
		retval.put(jsonObject);
		return retval;
	}

	public static String getJsonFromArray(ArrayList<Pair<Integer, String>> list) {
		JSONArray array = new JSONArray();
		for (Pair<Integer, String> pair : list) {
			JSONObject object = new JSONObject();
			try {
				object.put("id", pair.first);
				object.put("name", pair.second);
				array.put(object);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return array.toString();
	}

	public static String getJsonFromTriple(
			Triple<Integer, String, String> triple) {
		return triple.second + ":" + triple.third;
	}
	public static String getJsonFromQuestions(ArrayList<InspectionQuestion> list) {
		JSONArray array = new JSONArray();
		for (InspectionQuestion question : list) {
			JSONObject object = new JSONObject();
			int queryId = question.getQueryId();
			String queryText = question.getQueryText();
			String querySubText = question.getQuerySubText();
			boolean isMandatory = question.isMandatory();
			boolean isPhoto = question.isPhoto();
			try {
				object.put("id", queryId);
				object.put("query", queryText);
				object.put("subQuery", querySubText);
				object.put("isMandatory", isMandatory);
				object.put("isPhoto", isPhoto);
				array.put(object);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return array.toString();
	}
}
