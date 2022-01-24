package com.ipssi.gen.utils;

import java.io.Serializable;

import org.w3c.dom.Element;

public class ChartInfo implements Serializable,Cloneable  {
	
	
	public Object clone()throws CloneNotSupportedException{  
		return super.clone();  
		}  
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id = null;
	private String name = null;
	private String chartCategory = null;
	private String type = null;
	private String width = null;
	private String height = null;
	private String renderAt = null;
	private String dataFormat = null;
	private String dataSource = null;
	private String caption=null;
	private String subCaption=null;
	private String xAxisName=null;
	private String yAxisName=null;
	private String exportEnabled=null;
	private String avgDisplayStr=null;
	private String categorySeries=null;
	private String categoryAxis=null;
	private String categoryValue=null;
	private String categoryCompareWithValue=null;
	private String categoryCompareWithSeries=null;
	private String stackedDualYAxisValue=null;
	private Element chartNode=null;
	private String stackedDualYAxisSeries=null;
	private String stackedDualYAxisName=null;
	private String pYAxisName;
	private String sYAxisName;
	private String numberPrefix;
	private String numbersuffix;
	private String sNumberSuffix;
	private String paletteColors;
	private String mlvlPIECoreCategoryLabel;
	private String subCategorySeries;
	private String plotTooltext;
	private int maxXAxisPoints;
	private int refreshinterval;
	private int doCumulative;
	
	private String showXAxisLine;
	private String showLabels; 
	private String showLegend;
	private String captionFontSize;
	private String showBorder;
	private String showYAxisValues;
	private String yAxisMinValue ;
	private String yAxisMaxValue;
	private String rotateYAxisName;
	public String getShowXAxisLine() {
		return showXAxisLine;
	}


	public void setShowXAxisLine(String showXAxisLine) {
		this.showXAxisLine = showXAxisLine;
	}


	public String getShowLabels() {
		return showLabels;
	}


	public void setShowLabels(String showLabels) {
		this.showLabels = showLabels;
	}


	public String getShowLegend() {
		return showLegend;
	}


	public void setShowLegend(String showLegend) {
		this.showLegend = showLegend;
	}


	public String getCaptionFontSize() {
		return captionFontSize;
	}


	public void setCaptionFontSize(String captionFontSize) {
		this.captionFontSize = captionFontSize;
	}


	public String getShowBorder() {
		return showBorder;
	}


	public void setShowBorder(String showBorder) {
		this.showBorder = showBorder;
	}


	public void setHeight(String height) {
		this.height = height;
	}
	public void setWidth(String width) {
		this.width = width;
	}

	public void setXAxisName(String axisName) {
		xAxisName = axisName;
	}


	public void setYAxisName(String axisName) {
		yAxisName = axisName;
	}


	public void setExportEnabled(String exportEnabled) {
		this.exportEnabled = exportEnabled;
	}


	public String getAttributeByName(String attrName) {
		return Misc.getParamAsString(chartNode!=null?chartNode.getAttribute(attrName):"","");
	}
	
	
	public int getRefreshinterval() {
		return refreshinterval;
	}


	public String getStackedDualYAxisLine() {
		return stackedDualYAxisValue;
	}

	public String getChartCategory() {
		return chartCategory;
	}

	public String getAvgDisplayStr() {
		return avgDisplayStr;
	}
	
	public int getMaxXAxisPoints() {
		return maxXAxisPoints;
	}
	public ChartInfo(String id,String name,String type, String width, String height, String renderAt, String dataFormat, String dataSource){
		this.id = id;
		this.type = type;
		this.width = width;
		this.height = height;
		this.renderAt = renderAt;
		this.dataFormat = dataFormat;
		this.dataSource = dataSource;
	}
	public ChartInfo(){
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	public String getWidth() {
		return width;
	}
	public String getHeight() {
		return height;
	}
	public String getRenderAt() {
		return renderAt;
	}
	
	public void setRenderAt(String renderAt) {
		this.renderAt = renderAt;
	}


	public String getExportEnabled() {
		return exportEnabled;
	}
	public String getDataFormat() {
		return dataFormat;
	}
	public String getDataSource() {
		return dataSource;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String s) {
		this.caption=s;
	}
	public String getSubCaption() {
		return subCaption;
	}
	public String getXAxisName() {
		return xAxisName;
	}
	public String getYAxisName() {
		return yAxisName;
	}
	public ChartInfo(Element chartNode) {
		if(chartNode!=null){
		this.chartNode=chartNode;
		this.id = Misc.getParamAsString(chartNode.getAttribute("id"),"chart_id");
		this.name = Misc.getParamAsString(chartNode.getAttribute("name"),"Basic Chart");
		this.type = Misc.getParamAsString(chartNode.getAttribute("type"),"column2d");
		this.width = Misc.getParamAsString(chartNode.getAttribute("width"),"400");
		this.height = Misc.getParamAsString(chartNode.getAttribute("height"),"600");
		this.renderAt = Misc.getParamAsString(chartNode.getAttribute("container"),"chart");
		this.dataFormat = Misc.getParamAsString(chartNode.getAttribute("data_format"),"json");
		this.caption = Misc.getParamAsString(chartNode.getAttribute("caption"),"caption");
		this.subCaption = Misc.getParamAsString(chartNode.getAttribute("subCaption"),"");
		this.xAxisName = Misc.getParamAsString(chartNode.getAttribute("xaxisname"),"");
		this.yAxisName = Misc.getParamAsString(chartNode.getAttribute("yaxisname"),"");
		this.exportEnabled = Misc.getParamAsString(chartNode.getAttribute("exportEnabled"),"0");
		this.avgDisplayStr = Misc.getParamAsString(chartNode.getAttribute("avgdisplayvalue"),"Average");
		this.categorySeries = Misc.getParamAsString(chartNode.getAttribute("category_series"),"");
		this.categoryAxis = Misc.getParamAsString(chartNode.getAttribute("category_axis"),"");
		this.categoryValue = Misc.getParamAsString(chartNode.getAttribute("category_value"),"");
		this.categoryCompareWithValue = Misc.getParamAsString(chartNode.getAttribute("category_compare_with_value"),"");
		this.categoryCompareWithSeries = Misc.getParamAsString(chartNode.getAttribute("category_compare_with_series"),"");
		this.chartCategory = Misc.getParamAsString(chartNode.getAttribute("chart_category"),"");
		this.stackedDualYAxisValue = Misc.getParamAsString(chartNode.getAttribute("stacked_dual_y_axis_value"),"");
		this.stackedDualYAxisSeries = Misc.getParamAsString(chartNode.getAttribute("stacked_dual_y_axis_series"),"");
		this.pYAxisName = Misc.getParamAsString(chartNode.getAttribute("pyaxisname"),"pyaxisname");
		this.sYAxisName = Misc.getParamAsString(chartNode.getAttribute("syaxisname"),"syaxisname");
		this.numberPrefix = Misc.getParamAsString(chartNode.getAttribute("numberprefix"),"");
		this.numbersuffix = Misc.getParamAsString(chartNode.getAttribute("numbersuffix"),"");
		this.sNumberSuffix = Misc.getParamAsString(chartNode.getAttribute("snumbersuffix"),"");
		this.paletteColors = Misc.getParamAsString(chartNode.getAttribute("palettecolors"),"");
		this.mlvlPIECoreCategoryLabel = Misc.getParamAsString(chartNode.getAttribute("mlvl_pie_core_category_label"),"");
		this.subCategorySeries = Misc.getParamAsString(chartNode.getAttribute("sub_category_series"),"");
		this.plotTooltext= Misc.getParamAsString(chartNode.getAttribute("plotTooltext"),"");
		this.maxXAxisPoints= Misc.getParamAsInt(chartNode.getAttribute("max_x_axis_points"),12);
		this.refreshinterval= Misc.getParamAsInt(chartNode.getAttribute("refreshinterval"),5);
		this.doCumulative= Misc.getParamAsInt(chartNode.getAttribute("do_cumulative"),0);
		this.showXAxisLine= Misc.getParamAsString(chartNode.getAttribute("showXAxisLine"),"");
		this.showLabels= Misc.getParamAsString(chartNode.getAttribute("showLabels"),""); 
		this.showLegend= Misc.getParamAsString(chartNode.getAttribute("showLegend"),"");
		this.captionFontSize= Misc.getParamAsString(chartNode.getAttribute("captionFontSize"),"");
		this.showBorder= Misc.getParamAsString(chartNode.getAttribute("showBorder"),"");
		
		this.showYAxisValues= Misc.getParamAsString(chartNode.getAttribute("showYAxisValues"),"");
		this.yAxisMinValue = Misc.getParamAsString(chartNode.getAttribute("yAxisMinValue"),"");
		this.yAxisMaxValue= Misc.getParamAsString(chartNode.getAttribute("yAxisMaxValue"),"");
		this.rotateYAxisName= Misc.getParamAsString(chartNode.getAttribute("rotateYAxisName"),"");
		}
	}
	
	public String getShowYAxisValues() {
		return showYAxisValues;
	}


	public void setShowYAxisValues(String showYAxisValues) {
		this.showYAxisValues = showYAxisValues;
	}


	public String getYAxisMinValue() {
		return yAxisMinValue;
	}


	public void setYAxisMinValue(String axisMinValue) {
		yAxisMinValue = axisMinValue;
	}


	public String getYAxisMaxValue() {
		return yAxisMaxValue;
	}


	public void setYAxisMaxValue(String axisMaxValue) {
		yAxisMaxValue = axisMaxValue;
	}


	public String getRotateYAxisName() {
		return rotateYAxisName;
	}


	public void setRotateYAxisName(String rotateYAxisName) {
		this.rotateYAxisName = rotateYAxisName;
	}


	public int getDoCumulative() {
		return doCumulative;
	}


	public String getPlotTooltext() {
		return plotTooltext;
	}
	public String getSubCategorySeries() {
		return subCategorySeries;
	}
	public String getMlvlPIECoreCategoryLabel() {
		return mlvlPIECoreCategoryLabel;
	}
	public String getPYAxisName() {
		return pYAxisName;
	}
	
	public String getSYAxisName() {
		return sYAxisName;
	}
	public String getNumberPrefix() {
		return numberPrefix;
	}
	public String getNumbersuffix() {
		return numbersuffix;
	}
	public String getSNumberSuffix() {
		return sNumberSuffix;
	}
	public String getPaletteColors() {
		return paletteColors;
	}
	public String getStackedDualYAxisSeries() {
		return stackedDualYAxisSeries;
	}
	public String getCategorySeries() {
		return categorySeries;
	}
	public String getCategoryAxis() {
		return categoryAxis;
	}
	public String getCategoryValue() {
		return categoryValue;
	}
	public String getCategoryCompareWithValue() {
		return categoryCompareWithValue;
	}
	public String getCategoryCompareWithSeries() {
		return categoryCompareWithSeries;
	}
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
}