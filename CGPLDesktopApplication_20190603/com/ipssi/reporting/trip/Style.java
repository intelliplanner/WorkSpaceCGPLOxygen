package com.ipssi.reporting.trip;

import com.ipssi.gen.utils.Misc;

public class Style {
	private boolean hidden = false;
	private int align = Misc.getUndefInt(); //-1 left, 0 center, 1 right
	private int rowSpan = 1;
	private int colSpan = 1;
	private boolean doGroup = false;
	public void setHidden(boolean hidden){
		this.hidden = hidden;
	}
	public boolean getHidden(){
		return this.hidden;
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
	public void setDoGroup(boolean doGroup){
		this.doGroup = doGroup;	
	}
	public boolean getDoGroup(){
		return this.doGroup;
	}

}
