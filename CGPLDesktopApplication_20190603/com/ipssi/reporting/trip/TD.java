package com.ipssi.reporting.trip;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Value;

public class TD {
	private int id;
	private int classId = Misc.getUndefInt();
	private boolean hidden = false;
	private String content;
	private String display = null;
	private String linkApart = null;
	private Table nestedTable = null;
	private int align = Misc.getUndefInt(); //-1 left, 0 center, 1 right
	private int rowSpan = 1;
	private int colSpan = 1;
	private int contentType = 2;
	private int width = -1;
	private boolean doGroup = false;
	private boolean ignore = false;
	private boolean noWrap = false;
	private Value optionalValue = null;
	private int optionalDimCofigIndex = -1;
	public String toString() {
		return this.content+" L:"+this.linkApart+" D:"+this.display;
	}
	public TD copy() {
		
		TD td = new TD();
		td.id = this.id;
		td.classId = this.classId;
		td.hidden = this.hidden;
		td.content = this.content;
		td.display = this.display;
		td.linkApart = this.linkApart;
		td.nestedTable = this.nestedTable;
		td.align = this.align;
		td.rowSpan = this.rowSpan;
		td.colSpan = this.colSpan;
		td.contentType = this.contentType;
		td.width = this.width;
		td.doGroup = this.doGroup;
		td.ignore = this.ignore;
		td.noWrap = this.noWrap;
		td.optionalValue = this.optionalValue;
		td.optionalDimCofigIndex = this.optionalDimCofigIndex;
		return td;
	}
	public void setId(int id){
		this.id = id; 
	}
	public int getId(){
		return this.id;
	}
	public void setClassId(int classId){
		
		this.classId = classId;
	}
	public int getClassId(){
		int retval = this.classId;
		if (this.doGroup) {
			if (classId >= 2 && classId <= 6) {//cn,nn,nnGreen, nnYellow, nnRead
				retval += 8;
			}
			
		}
		else {
			if (classId >= 10 && classId <= 14) {//cn,nn,nnGreen, nnYellow, nnRead
				retval -= 8;
			}
		}
		return retval;
	}
	public void setHidden(boolean hidden){
		this.hidden = hidden;
	}
	public boolean getHidden(){
		return this.hidden;
	}
	public void setContent(String content){
		this.content = content;
	}
	public String getContent(){
		return this.content;
	}
	public void setDisplay(String display){
		this.display = display;
	}
	public String getDisplay(){
		return this.display;
	}
	public void setAlignment(int align)
	{
		this.align = align;
	}
	public int getAlignment()
	{
		return this.align;
	}
	public void setRowSpan(int rowSpan)
	{
		this.rowSpan = rowSpan;
	}
	public int getRowSpan()
	{
		return this.rowSpan;
	}
	public void setColSpan(int colSpan)
	{
		this.colSpan = colSpan;
	}
	public int getColSpan()
	{
		return this.colSpan;
	}
	public void setContentType(int contentType){
		this.contentType = contentType;	
	}
	public int getContentType(){
		return this.contentType;
	}
	public void setDoGroup(boolean doGroup){
		this.doGroup = doGroup;	
	}
	public boolean getDoGroup(){
		return this.doGroup;
	}
	public void setDoIgnore(boolean ignore){
		this.ignore = ignore;	
	}
	public boolean getDoIgnore(){
		return this.ignore;
	}
	public int getAlign() {
		return align;
	}
	public void setAlign(int align) {
		this.align = align;
	}
	public boolean isIgnore() {
		return ignore;
	}
	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}
	public boolean isNoWrap() {
		return noWrap;
	}
	public void setNoWrap(boolean noWrap) {
		this.noWrap = noWrap;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public Table getNestedTable() {
		return nestedTable;
	}
	public void setNestedTable(Table nestedTable) {
		this.nestedTable = nestedTable;
	}
	public Value getOptionalValue() {
		return optionalValue;
	}
	public void setOptionalAndRefDciIndexValue(Value optionalValue, int index) {
		this.optionalValue = optionalValue;
		this.optionalDimCofigIndex = index;
	}
	public String getLinkAPart() {
		return linkApart;
	}
	public void setLinkAPart(String linkAPart) {
		this.linkApart = linkAPart;
	}
	public int getOptionalDimCofigIndex() {
		return optionalDimCofigIndex;
	}
	public String getLinkApart() {
		return linkApart;
	}
	public void setLinkApart(String linkApart) {
		this.linkApart = linkApart;
	}

}
