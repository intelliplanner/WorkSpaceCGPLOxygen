package com.ipssi.rfid.db;

import java.lang.annotation.Annotation;

import com.ipssi.gen.utils.Misc;


public class Criteria {
	Class<?> base;
	String tableName;
	boolean desc = false;
	public Criteria(Class<?> base){
		if (base.isAnnotationPresent(Table.class)) {
			Annotation annotation = base.getAnnotation(Table.class);
			Table table  = (Table) annotation;
			if(table != null)
				tableName = table.value();
		}
		this.base = base;
	}
	
	public Criteria(Class<?> base, String whrClause, String orderByClause,boolean desc,
			int limit) {
		super();
		if (base.isAnnotationPresent(Table.class)) {
			Annotation annotation = base.getAnnotation(Table.class);
			Table table  = (Table) annotation;
			if(table != null)
				tableName = table.value();
		}
		this.base = base;
		this.desc = desc;
		this.setWhrClause(whrClause);
		this.setOrderByClause(orderByClause);
		this.limit = limit;
	}

	private StringBuilder whrClause = null;
	private StringBuilder orderByClause = null;
	private int limit = Misc.getUndefInt();
	public boolean isDesc() {
		return desc;
	}
	public StringBuilder getWhrClause() {
		return whrClause;
	}
	public StringBuilder getOrderByClause() {
		return orderByClause;
	}
	public void setDesc(boolean desc) {
		this.desc = desc;
	}
	
	public void setWhrClause(String clause) {
		if(clause != null && clause.length() > 0){
		if(whrClause == null){
			whrClause = new StringBuilder();
			whrClause.append(clause);
		}else
			whrClause.append(" and ").append(clause);
		}
	}
	public void setOrderByClause(String clause) {
		if(clause != null && clause.length() > 0){
			if(orderByClause == null){
				orderByClause = new StringBuilder();
				orderByClause.append(clause);
			}else
				orderByClause.append(" , ").append(clause);
			}
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
}
