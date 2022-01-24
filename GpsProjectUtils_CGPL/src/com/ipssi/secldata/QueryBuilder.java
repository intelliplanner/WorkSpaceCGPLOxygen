package com.ipssi.secldata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class QueryBuilder {
	public static void main(String[] args) {
		String query = QueryBuilder.getQuery("2,0,null,ram,null,1,1");
		System.out.println("Generated Query For Insert :"+query);
		query = QueryBuilder.getQuery("2,1,9,shyam,2014-01-24 13:56:00,1,1");
		System.out.println("Generated Query For Update :"+query);
		query = QueryBuilder.getQuery("2,2,9,shyam,2014-01-24 13:56:00,1,1");
		System.out.println("Generated Query For Delete :"+query);
	}
	public static <T> boolean contains( final T[] array, final T v ) {
	    for ( final T e : array )
	        if ( e == v || v != null && v.equals( e ) )
	            return true;

	    return false;
	}
	// private String queryString;
	public static String getLogQuery(String dataString){
		String queryString = null;
        Pair<Pair<String,String>, ArrayList> dataSchema = null;
     //   dataString = dataString.substring(4);//senderid,objectid
        String[] dataStringArray = dataString.split(",");
        try
        {
            dataSchema = LoadXML.getTableFieldInfo(Misc.getParamAsInt(dataStringArray[0]));
            if(dataSchema != null && dataSchema.first != null && dataSchema.first.first != null && dataSchema.first.first.length() > 0 && dataSchema.first.first.equalsIgnoreCase("token")){
            	dataSchema.first.first = "rfid_log";
            	queryString = getInsertQuery(dataSchema,dataString);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return queryString;
	}
    public static String getQuery(String dataString)//dataString ="tableId,actionId,datapoints comma separated";
    {
        String queryString = null;
        Pair<Pair<String,String>, ArrayList> dataSchema = null;
     //   dataString = dataString.substring(4);//senderid,objectid
        String[] dataStringArray = dataString.split(",");
        try
        {
            dataSchema = LoadXML.getTableFieldInfo(Misc.getParamAsInt(dataStringArray[0]));
            if (Misc.getParamAsInt(dataStringArray[1]) == 0)
            {
                queryString = getInsertQuery(dataSchema,dataString);
            }
            else if (Misc.getParamAsInt(dataStringArray[1]) == 1)
            {
                queryString = getUpdateQuery(dataSchema,dataString);
            }
            else if (Misc.getParamAsInt(dataStringArray[1]) == 2)
            {
                queryString = getDeletedQuery(dataSchema, dataString);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return queryString;

    }
    static String getInsertQuery(Pair<Pair<String,String>,ArrayList> dataschema,String dataString)
    {//Pair<Pair<String,String>, ArrayList>
        //Dictionary<tableId, Pair<Pair<tableName,primaryKey>,ArrayList(columnName-type)>>
        String query = null;

        StringBuilder insertQueryPart = new StringBuilder("insert into ");
        StringBuilder insertValuePart = new StringBuilder(" values (");
        try
        {
            Pair<Pair<String, String>, ArrayList> data = dataschema;
            Pair<String, String> tableNameAndPK = data.first;
            ArrayList list = data.second;
            int listSize = list.size();
            String[] dataStringArray = dataString.split(",");
            insertQueryPart.append(" " + tableNameAndPK.first + "( ");
            for (int i = 0; i < listSize; i++)
            {
                Pair<String, String> pair = (Pair<String, String>)list.get(i);
                String columnName = pair.first;
                String columnType = pair.second;
                //columnType -> 0 --int,1 -- double ,2 -- String, 3 --date
                if ((listSize-1) == i)
                {
                    insertQueryPart.append(columnName + ")");
                    convertDataAsColumnType(insertValuePart, columnType, dataStringArray[i + 2]);
                    insertValuePart.append(" )");
                }
                else
                {
                    insertQueryPart.append(columnName + ",");
                    convertDataAsColumnType(insertValuePart, columnType, dataStringArray[i + 2]);
                    insertValuePart.append(" ,");
                }    

            }
            query = insertQueryPart.toString() + " " + insertValuePart.toString();

        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return query;
    }
    static String getUpdateQuery(Pair<Pair<String, String>, ArrayList> dataschema,String dataString)
    {
        String query = null;
        StringBuilder updatePart = new StringBuilder("update ");
        StringBuilder wherePart = new StringBuilder(" where ");
        try
        {
            Pair<Pair<String, String>, ArrayList> data = dataschema;
            Pair<String, String> tableNameAndPK = data.first;
            String tableName = tableNameAndPK.first;
            String pkString = tableNameAndPK.second;
            int pkStringcount = pkString.length();
            if (pkStringcount > 0)
            {
            
            String[] pkStringArray = pkString.split(",");
            ArrayList list = data.second;
            int listSize = list.size();
            String[] dataStringArray = dataString.split(",");
            updatePart.append(" " + tableName + " set ");
            
            for (int i = 0; i < listSize; i++)
            {
                Pair<String, String> pair = (Pair<String, String>)list.get(i);
                String columnName = pair.first;
                String columnType = pair.second;
                //columnType -> 0 --int,1 -- double ,2 -- String, 3 --date

                if (contains(pkStringArray,columnName))
                {
                    wherePart.append(columnName + '=');
                    convertDataAsColumnType(wherePart, columnType, dataStringArray[i + 2]);
                    wherePart.append(" and ");
                }
                else
                {
                    updatePart.append(columnName + "=");
                    convertDataAsColumnType(updatePart, columnType, dataStringArray[i + 2]);
                    
                    updatePart.append(" ,");

                }

            }
            wherePart.append( "1=1");
            query = updatePart.toString();
            query = query.substring(0, query.length()- 1);//Remove(query.length() - 1);
            query = query + wherePart.toString();
        }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return query;
    }
    static String getDeletedQuery(Pair<Pair<String, String>, ArrayList> dataschema, String dataString)
    {
        String query = null;
        StringBuilder deletePart = new StringBuilder("delete from ");
        StringBuilder wherePart = new StringBuilder(" where ");

        try
        {
            Pair<Pair<String, String>, ArrayList> data = dataschema;
            Pair<String, String> tableNameAndPK = data.first;
            String tableName = tableNameAndPK.first;
            String pkString = tableNameAndPK.second;
            int pkStringcount = pkString.length();
            if (pkStringcount > 0)
            {
            
            String[] pkStringArray = pkString.split(",");
            ArrayList list = data.second;
            int listSize = list.size();
            String[] dataStringArray = dataString.split(",");
            deletePart.append(" " + tableName + "   ");
            
            for (int i = 0; i < listSize; i++)
            {
                Pair<String, String> pair = (Pair<String, String>)list.get(i);
                String columnName = pair.first;
                String columnType = pair.second;
                //columnType -> 0 --int,1 -- double ,2 -- String, 3 --date

                if (contains(pkStringArray,columnName))
                {
                    wherePart.append(columnName + '=');
                    convertDataAsColumnType(wherePart, columnType, dataStringArray[i + 2]);
                    wherePart.append(" and ");
                }
                

            }
            wherePart.append("1=1");
            query = deletePart.toString() + wherePart.toString();
        }
        }
        catch (Exception e)
        {
         e.printStackTrace();
        }
        return query;
    }
    static void convertDataAsColumnType(StringBuilder insertValuePart,String columnType,String columnValue)
    {
        int colType = Misc.getParamAsInt(columnType);
        switch (colType)
        {
            case 0:
            	if(columnValue!= null && columnValue.length() > 0 && !"null".equalsIgnoreCase(columnValue)){
                int colIntVal = Misc.getParamAsInt(columnValue);
                insertValuePart.append(colIntVal);
               }else{
            	   insertValuePart.append(null+"");
               }
               
                break;
            case 1 :
            	if(columnValue!= null && columnValue.length() > 0 && !"null".equalsIgnoreCase(columnValue)){
                double colDouVal = Misc.getParamAsDouble(columnValue);
                insertValuePart.append(colDouVal);
            	}else{
            		 insertValuePart.append(null+"");
            	}
                break;
            case 2 :
            	if(columnValue!= null && columnValue.length() > 0 && !"null".equalsIgnoreCase(columnValue)){
                String colStrVal = "'"+columnValue+"'";
                insertValuePart.append(colStrVal);
            	}else{
            		insertValuePart.append(null+"");
            	}
                break;
            case 3:
            	try{
            	if(columnValue!= null && columnValue.length() > 0 && !"null".equalsIgnoreCase(columnValue) && !"0".equalsIgnoreCase(columnValue)  && !"-1111111".equalsIgnoreCase(columnValue)){
                 String colDateVal = "'"+columnValue+"'";
                 insertValuePart.append(colDateVal);
            	}else{
            		insertValuePart.append(null+"");
            	}
            	}catch (Exception e) {
            		insertValuePart.append(null+"");
            		e.printStackTrace();
				}
                break;

        }
    }
    public static String executeQuery(String query,Connection conn){
    	String retval = 0+"";
    	PreparedStatement ps = null;
    	try{
         ps = conn.prepareStatement(query);
         ps.execute();
         retval = 1+"";
    	}catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if (ps != null) {
					ps.close();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retval;
    }    
}
