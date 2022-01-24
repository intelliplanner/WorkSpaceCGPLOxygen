package com.ipssi.dispatch.helper;

import java.util.ArrayList;


public class Icon {
	
	enum Color{   
		RED(1), GREEN(2), BLUE(3), YELLOW(4);  
		private int value;  
		private Color(int value){  
			this.value=value;  
		}  
	}  
	enum LoadStatus{   
		LOADED(1), UNLOADED(2), LOADWAIT(3), UNLOADWAIT(4);  
		private int value;  
		private LoadStatus(int value){  
			this.value=value;  
		}  
	}
	enum PaneSide{   
		LEFT(1), RIGHT(2);  
		private int value;  
		private PaneSide(int value){  
			this.value=value;  
		}  
	}  
	private int id;
	private Color color;
	private String image;
	private LoadStatus loadStatus;
	private int distancePercentage; // from left
	private int blinkRate;
	private int overlayNumber;
	private PaneSide paneSide;
	
	private ArrayList<Text> hoverProperties = new ArrayList<Text>();
	private ArrayList<Menu> leftList = new ArrayList<Menu>();
	private ArrayList<Menu> rightList = new ArrayList<Menu>();
	
	public String toString() {
		return super.toString()+" color:"+color+" image:"+image+" loadStatus:"+loadStatus+" distancePercentage:"+distancePercentage+" blinkRate:"+blinkRate+" overlayNumber:"+overlayNumber+" paneSide:"+paneSide;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<Text> getHoverProperties() {
		return hoverProperties;
	}

	public void setHoverProperties(ArrayList<Text> hoverProperties) {
		this.hoverProperties = hoverProperties;
	}

	public ArrayList<Menu> getLeftList() {
		return leftList;
	}

	public void setLeftList(ArrayList<Menu> leftList) {
		this.leftList = leftList;
	}

	public ArrayList<Menu> getRightList() {
		return rightList;
	}

	public void setRightList(ArrayList<Menu> rightList) {
		this.rightList = rightList;
	}

	public Color getColor() {
		return color;
	}


	public void setColor(Color color) {
		this.color = color;
	}


	public String getImage() {
		return image;
	}


	public void setImage(String image) {
		this.image = image;
	}


	public LoadStatus getLoadStatus() {
		return loadStatus;
	}


	public void setLoadStatus(LoadStatus loadStatus) {
		this.loadStatus = loadStatus;
	}


	public int getDistancePercentage() {
		return distancePercentage;
	}


	public void setDistancePercentage(int distancePercentage) {
		this.distancePercentage = distancePercentage;
	}


	public int getBlinkRate() {
		return blinkRate;
	}


	public void setBlinkRate(int blinkRate) {
		this.blinkRate = blinkRate;
	}


	public int getOverlayNumber() {
		return overlayNumber;
	}


	public void setOverlayNumber(int overlayNumber) {
		this.overlayNumber = overlayNumber;
	}


	public PaneSide getPaneSide() {
		return paneSide;
	}


	public void setPaneSide(PaneSide paneSide) {
		this.paneSide = paneSide;
	}
	
	public static class Menu {
		public int id;
		public Text label;
		public Text value;
		public String url;
		public String image;
		
		public Menu(Text label,Text value, String url, String image){
			this.label=label;
			this.value = value;
			this.url = url;
			this.image = image;
		}
		public String toString() {
			return super.toString()+" label:"+label+" value:"+value+" url:"+url+" image:"+image;
		}
		
	}

	public static class Text {
		private String label;
		private String value;
		private String labelStyle;
		private String valueStyle;
		private String hoverHelp;
		private String hoverHelpStyle;
		
		public Text(String label,String value, String labelStyle, String valueStyle, String hoverHelp, String hoverHelpStyle){
			this.label=label;
			this.value = value;
			this.labelStyle = labelStyle;
			this.valueStyle = valueStyle;
			this.hoverHelp = hoverHelp;
			this.hoverHelpStyle = hoverHelpStyle;
		}
		public String toString() {
			return super.toString()+" label:"+label+" value:"+value+" labelStyle:"+labelStyle+" valueStyle:"+valueStyle+" hoverHelp:"+hoverHelp+" hoverHelpStyle:"+hoverHelpStyle;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getLabelStyle() {
			return labelStyle;
		}
		public void setLabelStyle(String labelStyle) {
			this.labelStyle = labelStyle;
		}
		public String getValueStyle() {
			return valueStyle;
		}
		public void setValueStyle(String valueStyle) {
			this.valueStyle = valueStyle;
		}
		public String getHoverHelp() {
			return hoverHelp;
		}
		public void setHoverHelp(String hoverHelp) {
			this.hoverHelp = hoverHelp;
		}
		public String getHoverHelpStyle() {
			return hoverHelpStyle;
		}
		public void setHoverHelpStyle(String hoverHelpStyle) {
			this.hoverHelpStyle = hoverHelpStyle;
		}
		
	}
	
}
