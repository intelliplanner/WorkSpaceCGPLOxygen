package com.ipssi.reporting.trip;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.jsp.JspWriter;

import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Value;

public class Table {
	private ArrayList<TR> header = null;
	private ArrayList<TR> body = null;
	private HashMap<Integer, Integer> colIndexMap = new HashMap<Integer, Integer>();
	private HTMLStreamingGenerator streamingGenerator = null;
	private int mailClusteringIndex = Misc.getUndefInt();
	private int columnCount = Misc.getUndefInt();
	
	private int addnlHeaderLines = 0;
	private StringBuilder addnlHeader = null;
	/*private ArrayList<Table> tableCluster = null;
	
	public void addNewTableCluster(Table parentTable){
		
	}*/
	
	private Table() { //make it private to force use of create functions
	
	}
	public JspWriter getStreamingOut() {
		return streamingGenerator == null ? null : streamingGenerator.getWriter();
	}
	public Writer getStreamingGenOut() {
		return streamingGenerator == null ? null : streamingGenerator.getGenOut();
	}
	public int getStreamingReportType() {
		return streamingGenerator == null ? Misc.getUndefInt() : streamingGenerator.getReportType();
	}
	public boolean isNullStreamingGenerator() {
		return streamingGenerator == null;
	}
	
	public static Table createTable(Table parentTable, int reportType, SessionManager _session, JspWriter out, boolean doPlainTable, FrontPageInfo fpi, Writer genOut, StringBuilder addnlHeaderLines) throws Exception {
		Table retval = new Table();
		if (out != null || genOut != null) {
			retval.streamingGenerator = reportType == Misc.DOTMATRIX ?
					new TextStreamingGenerator(reportType, _session, out, retval, fpi, genOut , addnlHeaderLines)
					:
						new HTMLStreamingGenerator(reportType, _session, out, retval, doPlainTable, genOut, addnlHeaderLines);
		}
		if (parentTable != null) {
			//TODO
			
		}
		if (retval.streamingGenerator != null) {
			retval.streamingGenerator.callOnTableCreate();
		}
		return retval;
	}
	public static Table createTable(Table parentTable) throws Exception {
		return createTable(parentTable, Misc.HTML, null, null, false, null, null, null);
	}
	public static Table createTable() throws Exception {
		return createTable(null, Misc.HTML, null, null, false, null, null, null);
	}
	public void closeTable() throws Exception {
		if (streamingGenerator != null) {
			streamingGenerator.callOnClose();
		}
	}
	public void setHeader(TR tr) throws Exception {
		if (this.header == null) {
			this.header = new ArrayList<TR>();
			if (streamingGenerator != null)
				streamingGenerator.callOnTHeadCreate();
		}
		if (streamingGenerator != null)
			streamingGenerator.callOnTRCreate();
		if(Misc.isUndef(columnCount) && tr != null && tr.getRowData() != null & tr.getRowData().size() > 0){
			int count = 0;
			for(TD td : tr.getRowData()){
				if(td.getColSpan() > 1){
					count += td.getColSpan();
				}else{
					count++;
				}
			}
			this.columnCount = count;
		}
		this.header.add(tr);
	}	
	public void setBody(int addAfter, TR tr) throws Exception {
		if (this.body == null) {
			this.body = new ArrayList<TR>();
			if (streamingGenerator != null)
				streamingGenerator.callOnTBodyCreate();
		}
		if (streamingGenerator != null)
			streamingGenerator.callOnTRCreate();
		if (this.body.size() == addAfter)
			this.body.add(tr);
		else
			this.body.add(addAfter+1, tr);
	}
	public void setBody(TR tr) throws Exception {
		if (this.body == null) {
			this.body = new ArrayList<TR>();
			if (streamingGenerator != null)
				streamingGenerator.callOnTBodyCreate();
		}
		if (streamingGenerator != null)
			streamingGenerator.callOnTRCreate();

		this.body.add(tr);
	}
	public void setBody(ArrayList<TR> body)
	{
		this.body = body;
	}
	public ArrayList<TR> getHeader(){
		return this.header;
	}
	public void setOneDimentionHeader(){
		TR tr = null;
		ArrayList<TD> rowData = null;
		TR tempTR = null;
        int rowCount = -1;
        int index = 0;
		if(header != null ){
			rowCount = header.size();
			if(rowCount == 1)
				return ;
			tr = header.get(0);
			tempTR = new TR();
			rowData = tr.getRowData();
			if(rowData != null && rowData.size() > 0){
				for(int j=0;j<rowData.size();j++){
					if(rowData.get(j).getColSpan() > 1){
						for(int k=0;k<rowData.get(j).getColSpan();k++){
							tempTR.setRowData(header.get(rowCount-1).getRowData().get(index));
							index++;
						}
					}
					else if(rowData.get(j).getRowSpan() > 1){
						rowData.get(j).setRowSpan(1);
						tempTR.setRowData(rowData.get(j));
					}
					else
						tempTR.setRowData(rowData.get(j));
				}
			}
			header.set(0, tempTR);
			for(int l=1;l<rowCount;l++)
				header.remove(l);
	}
	}
	public ArrayList<TR> getBody(){
		return this.body;
	}
	public int getTableWidth(){
		int retval = body == null ? 0 : body.get(0) == null ? 0 : body.get(0).getRowData() == null ? 0 : body.get(0).getRowData().size()-getSkipColumn();
		return retval;
	}
	public int getSkipColumn(){
		int retval = 0;
		for(TR tr : header){
			for(TD td : tr.getRowData()){
				if(td.getDoIgnore() || td.getHidden())
					retval++;
			}
		}
		return retval;
	}
	public int getColumnIndexById(int id){
		int retval = Misc.getUndefInt();
			if(colIndexMap.get(id) != null)
				retval = colIndexMap.get(id);
		return retval;
	}
	public void setIndexById(int paramid, int index) {
		this.colIndexMap.put(paramid, index);
	}
	public TR getColne(TR tr){
		TR retval = new TR();
		TD tempCol = null;
		if(tr != null){
		for(int j=0;j<tr.getRowData().size();j++){
			tempCol = new TD();
			tempCol.setId(tr.getRowData().get(j).getId());
			tempCol.setClassId(tr.getRowData().get(j).getClassId());
			tempCol.setColSpan(tr.getRowData().get(j).getColSpan());
			tempCol.setRowSpan(tr.getRowData().get(j).getRowSpan());
			tempCol.setHidden(tr.getRowData().get(j).getHidden());
			tempCol.setAlignment(tr.getRowData().get(j).getAlignment());
			tempCol.setContentType(tr.getRowData().get(j).getContentType());
			tempCol.setContent(tr.getRowData().get(j).getContent());
			tempCol.setDisplay(tr.getRowData().get(j).getDisplay());
			tempCol.setLinkAPart(tr.getRowData().get(j).getLinkAPart());
			retval.setRowData(tempCol);
		}
		}
		return tr;
	}
	public TD getColne(TD td){
		TD retval =  new TD();
		if(td != null){
		retval.setId(td.getId());
		retval.setClassId(td.getClassId());
		retval.setColSpan(td.getColSpan());
		retval.setRowSpan(td.getRowSpan());
		retval.setHidden(td.getHidden());
		retval.setAlignment(td.getAlignment());
		retval.setContentType(td.getContentType());
		retval.setContent(td.getContent());
		retval.setDisplay(td.getDisplay());
		retval.setLinkAPart(td.getLinkAPart());
		}
		return td;
	}
	public int getMailClusteringIndex() {
		return mailClusteringIndex;
	}
	public void setMailClusteringIndex(int mailClusteringIndex) {
		this.mailClusteringIndex = mailClusteringIndex;
	}
	public ArrayList<String> getClusterGroupNames() {
		ArrayList<String> retval = null;
		try{
			ArrayList<TR>  body = getBody();
			if(body == null || body.size() <= 0)
				return null;
			
			for(TR tr : body){
				TD td = tr.get(mailClusteringIndex);
				if(td == null || td.getContent() == null || td.getContent().length() <= 0)
					continue;
				String groupParam = td.getContent();
				boolean foundGroup = false;
				for(int i=0,is=retval == null ? 0 : retval.size();i<is;i++){
					if(groupParam.equalsIgnoreCase(retval.get(i))){
						foundGroup = true;
						break;
					}
				}
				if(!foundGroup){
					if(retval == null)
						retval = new ArrayList<String>();
					retval.add(groupParam);
				}
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	
	public int getAddnlHeaderLines() {
		return addnlHeaderLines;
	}
	public void setAddnlHeaderLines(int addnlHeaderLines) {
		this.addnlHeaderLines = addnlHeaderLines;
	}
	public StringBuilder getAddnlHeader() {
		return addnlHeader;
	}
	public void setAddnlHeader(StringBuilder addnlHeader) {
		this.addnlHeader = addnlHeader;
	}
}
