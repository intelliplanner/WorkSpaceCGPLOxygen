package com.ipssi.angular;

import java.util.ArrayList;

public class Report {
	private String title;
	private ArrayList<Tab> tabs;
	public static class Tab{
		private String title;
		private ArrayList<Widget> widgets;
	}
	public static class Widget{
		private String title;
		private String detail;
		private WidgetType type;
		private ArrayList<HeaderColumn> ranges;
		private HeaderColumn currentRange;
	}
	public static class HeaderColumn extends Column{
		private String key;
		private String label;
		private boolean sort;
		private boolean filter;
		private boolean hidden;
	}
	public static class DataColumn extends Column{
		private String content;
		private String img;
	}
	public static class Column{
		private String key;
		private ColumnType type;
	}
	public static enum ColumnType{
		NO_TYPE,
		LOV_TYPE,
	    STRING_TYPE,
	    NUMBER_TYPE,
	    LOV_NO_VAL_TYPE,
		INTEGER_TYPE,
		DATE_TYPE,
		FILE_TYPE,
		IMAGE_TYPE,
		GROUP_TOTAL
	}
	public static enum WidgetType{
		cardFilp,
		cardFlipFilter,
		stackedBarChart,
		donutChar,
		listView,
		pieChrt,
		areaChartListView,
		tableTypeOne,
		tableTypeTwo
	}
}
