package com.ipssi.rfid.db;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ipssi.gen.utils.Misc;

public class DatabaseColumn {
	public static final int INTEGER = 0;
	public static final int LONG = 1;
	public static final int DOUBLE = 2;
	public static final int STRING = 3;
	public static final int DATE = 4;
	public static final int BOOLEAN = 5;
	public static final int OBJECT = 6;
	private int type;
	private String colName;
	private Object colVal;
	private boolean isKey = false;
	private boolean isAuto = false;
	private boolean isGeneratedTime = false;
	private Field field = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public DatabaseColumn(int type,String colName,Object obj){
		this.type = type;
		this.colName = colName;
		this.colVal = obj;
	}
	public int getType() {
		return type;
	}
	public String getColName() {
		return colName;
	}
	public Object getColVal() {
		Object obj = "";
		switch(type){
		case BOOLEAN : obj = ((Boolean)colVal) ? 1 : 0;break;
		case STRING : 
			obj = !isNull()  ? ("'"+colVal+"'".replaceAll(";", "")) : "null";break;
		case DATE : 
			obj =  isGeneratedTime ? "now()" : !isNull()  ? ("'" + sdf.format((Date) colVal) +"'") : "null";break;
		default:obj=isNull() ? "null" : colVal;break;
		}
		return obj;
	}
	public void setType(int type) {
		this.type = type;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public void setColVal(Object colVal) {
		this.colVal = colVal;
	}
	public boolean isKey() {
		return isKey;
	}
	public boolean isAuto() {
		return isAuto;
	}
	public void setKey(boolean isKey) {
		this.isKey = isKey;
	}
	public void setAuto(boolean isAuto) {
		this.isAuto = isAuto;
	}
	public boolean isNull() {
		boolean retval = true;
		switch(type){
		case INTEGER : retval = Misc.isUndef((Integer)colVal); 
		break;
		case LONG : retval = Misc.isUndef((Long)colVal); 
		break;
		case DOUBLE : retval = Misc.isUndef((Double)colVal); 
		break;
		case STRING : retval = (colVal == null || ((String)colVal).length() <= 0) ;
		break;
		case DATE : retval = (colVal == null) ;
		break;
		case BOOLEAN : retval = (colVal == null);
		break;
		}
		return retval;
	}
	public void setValue(Object parent){
		try{
			if(!isNull()){
				switch(type){
				case INTEGER :   
				case LONG :  
				case DOUBLE : 
				case STRING : field.set(parent, colVal);break;
				case DATE :
						field.set(parent,  new Date(((java.sql.Date)colVal).getTime()));
					break;
				case BOOLEAN : field.set(parent, ((Integer)colVal) == 1);break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void setValue(Object parent,ResultSet rs){
		try{
			switch(type){
			case INTEGER : field.set(parent, Misc.getRsetInt(rs, colName)) ;break;  
			case LONG :  field.set(parent, Misc.getRsetLong(rs, colName)) ;break;
			case DOUBLE : field.set(parent, Misc.getRsetDouble(rs, colName)) ;break;
			case STRING : field.set(parent, Misc.getRsetString(rs, colName,null));break;
			case DATE : colVal = rs.getTimestamp(colName);
				if(!isNull())
					field.set(parent,  new Date(((java.sql.Timestamp)colVal).getTime()));
				break;
			case BOOLEAN : colVal = Misc.getRsetInt(rs, colName);
				if(!isNull())
					field.set(parent, ((Integer)colVal) == 1);break;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void setStatement(Object obj, PreparedStatement ps, int pos){
		try{
			field.setAccessible(true);
			switch(type){
			case INTEGER : 
				Misc.setParamInt(ps, (Integer)field.get(obj), pos);
				break;  
			case LONG :  
				Misc.setParamLong(ps, (Long)field.get(obj), pos);
				break; 
			case DOUBLE :
				Misc.setParamDouble(ps, (Double)field.get(obj), pos);
				break;
			case STRING : 
				ps.setString(pos, (String)field.get(obj));
				break;
			case DATE :
				ps.setTimestamp(pos, (java.sql.Timestamp)field.get(obj));
				break;
			case BOOLEAN :
				Misc.setParamInt(ps, (Integer)field.get(obj), pos);
				break;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	public boolean isGeneratedTime() {
		return isGeneratedTime;
	}
	public void setGeneratedTime(boolean isGeneratedTime) {
		this.isGeneratedTime = isGeneratedTime;
	}
}
