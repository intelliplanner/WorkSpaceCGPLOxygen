package com.ipssi.reporting.customize;

public class DBItem {
   private int row;
   private int col;
   private int rowspan;
   private int colspan;
   private String title;
   private String itemId;
   private String configFile;
   private int componentId; 
   private String addnlParam;
   
   public DBItem(int row, int col, int rowspan, int colspan, String title, String itemId, String configFile, int componentId, String addnlParam) {
	   this.row = row;
	   this.col = col;
	   this.colspan = colspan;
	   this.rowspan = rowspan;
	   this.title = title;
	   this.itemId = itemId;
	   this.configFile = configFile;
	   this.componentId = componentId;
	   this.addnlParam = addnlParam;
   }
public int getRow() {
	return row;
}
public void setRow(int row) {
	this.row = row;
}
public int getCol() {
	return col;
}
public void setCol(int col) {
	this.col = col;
}
public int getRowspan() {
	return rowspan;
}
public void setRowspan(int rowspan) {
	this.rowspan = rowspan;
}
public int getColspan() {
	return colspan;
}
public void setColspan(int colspan) {
	this.colspan = colspan;
}
public String getTitle() {
	return title;
}
public void setTitle(String title) {
	this.title = title;
}
public String getItemId() {
	return itemId;
}
public void setItemId(String itemId) {
	this.itemId = itemId;
}
public String getConfigFile() {
	return configFile;
}
public void setConfigFile(String configFile) {
	this.configFile = configFile;
}
public int getComponentId() {
	return componentId;
}
public void setComponentId(int componentId) {
	this.componentId = componentId;
}
public String getAddnlParam() {
	return addnlParam;
}
public void setAddnlParam(String addnlParam) {
	this.addnlParam = addnlParam;
}
   
}
