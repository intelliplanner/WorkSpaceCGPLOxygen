package com.ipssi.rfid.ui.secl.controller;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.secl.data.LovDao.LovItemType;
import com.jfoenix.controls.JFXMaskTextField;

import javafx.scene.Node;

public class NodeExtA{
	
	public static enum VehicleEntryType{
		MANUAL,
		AUTO
	}
	private Node node;
	private int privId = Misc.getUndefInt();
	private boolean isField = false;
	private boolean isAutoComplete = false;
	private boolean isMandatory = false;
	private boolean isVehicle = false;
	private VehicleEntryType vehicleEntryType = null;
	private LovItemType autoCompleteSrc = null; 
	private String autoCompleteNameField = null;
	private boolean noFocus = false;
	private boolean isWBReading = false;
	private boolean dateTime = false;
	private String menuTitle = "";
	private String menuTag = "";
	private String mask = "";
	public boolean isDateTime() {
		return dateTime;
	}
	public static NodeExtA getNodeExt(Node node){
		if(node == null)
			return null;
		return new NodeExtA(node);
	}
	private NodeExtA(Node node){
		this.node = node;
		init();
	}
	private void init(){
		if(this.node != null){
			String property = this.node.getAccessibleText();
			String[] fields = property == null ? null : property.split(";");
			for(int i=0,is=fields==null? 0 : fields.length;i<is;i++){
				String[] keyVal = fields[i] == null ? null : fields[i].split(":");
				if(keyVal == null || keyVal.length == 0 || Utils.isNull(keyVal[0]))
					continue;
				String key = keyVal[0].toUpperCase();
				String val = keyVal.length > 1 ? keyVal[1] : null;
				switch (key) {
				case "FIELD":
					this.isField = true;
					break;
				case "MANDATORY":
					this.isMandatory = true;
					break;
				case "VEHICLE" :
					this.isVehicle = true;
					if(!Utils.isNull(val)){
						this.vehicleEntryType = VehicleEntryType.valueOf(val.toUpperCase());
					}
					break;
				case "AUTOCOMPLETE":
					if(!Utils.isNull(val)){
						this.isAutoComplete = true;
						this.autoCompleteSrc = LovItemType.valueOf(val.toUpperCase());
					}
					break;
				case "AUTOCOMPLETE_NAME_FIELD":
					this.autoCompleteNameField = val;
					break;
				case "NOFOCUS":
					this.noFocus = true;
					break;
				case "READING":
					this.isWBReading = true;
					break;
				case "DATETIME":
					this.dateTime = true;
					break;
				default:
					break;
				}
				
			}
			if (node instanceof JFXMaskTextField){
				mask = ((JFXMaskTextField) node).getMask();
			}
		}
	}
	public boolean isWBReading() {
		return isWBReading;
	}
	public boolean isNoFocus() {
		return noFocus;
	}
	public Node getNode() {
		return node;
	}
	public boolean isField() {
		return isField;
	}
	public boolean isAutoComplete() {
		return isAutoComplete;
	}
	public boolean isMandatory() {
		return isMandatory;
	}
	public boolean isVehicle() {
		return isVehicle;
	}
	public VehicleEntryType getVehicleEntryType() {
		return vehicleEntryType;
	}
	public LovItemType getAutoCompleteSrc() {
		return autoCompleteSrc;
	}
	public int getPrivId() {
		return privId;
	}
	public void setPrivId(int privId) {
		this.privId = privId;
	}
	public String getMenuTitle() {
		return menuTitle;
	}
	public void setMenuTitle(String menuTitle) {
		this.menuTitle = menuTitle;
	}
	public String getMenuTag() {
		return menuTag;
	}
	public void setMenuTag(String menuTag) {
		this.menuTag = menuTag;
	}
	public String getAutoCompleteNameField() {
		return autoCompleteNameField;
	}
	public String getMask() {
		return mask;
	}
	
}
