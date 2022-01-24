/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package register_relative_information;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author vicky
 */
public class Connection2 {
    
    
      java.sql.Connection conn=null;
    PreparedStatement ps=null;
    ResultSet rs=null;
    
     boolean isValue=false;
    
    private boolean status1;
    private int i;
    
    Connection2() throws Exception
    { 
            try{
                 Class.forName("com.mysql.jdbc.Driver");
                 conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordinsert","root","root");
                   
               }
           catch(ClassNotFoundException | SQLException e){
                  System.err.println(e);
               }
    }
    
   
    
   public ArrayList<HoldingRegistrationData> Report(Date StartDate, Date EndDate) throws SQLException{
         String Start=null;
         String End=null;
         
          SimpleDateFormat formater = new SimpleDateFormat("yyyy/MM/dd");
          java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());
            if(EndDate!=null){
                
                      End= formater.format(EndDate);
                    
                }
            if(EndDate==null){
                     End=sqlDate.toString();
                    
                }
              Start=   formater.format(StartDate);
                           ArrayList<HoldingRegistrationData> dataLists = new ArrayList<HoldingRegistrationData>();
		try {
	String sql1="Select Fname ,Lname ,Father_Name ,Phone_Num ,Mobile_Num ,Email_ID ,DOB,Address,City,State,Pin,Notes,R_ID,image from Registration where Date_Of_Reg between '"+Start+"' and '"+End+"' order by Fname,Lname ";
			                  
			ps=conn.prepareStatement(sql1);
			 rs=ps.executeQuery();
                    
			HoldingRegistrationData data = null;
			while(rs.next()){
                           
			data = new HoldingRegistrationData();
			data.setFname(rs.getString("Fname"));
                            
                        data.setLname(rs.getString("Lname"));
                            
                        data.setFather_Name(rs.getString("Father_Name"));
                        data.setPhone_Num(rs.getLong("Phone_Num"));
                      	data.setMobile_Num(rs.getLong("Mobile_Num"));
			data.setEmail_ID(rs.getString("Email_ID"));
                        data.setDOB(rs.getDate("DOB"));
                        data.setAddress(rs.getString("Address"));
                        data.setCity(rs.getString("State"));
                        data.setState(rs.getString("City"));
                        data.setPin(rs.getLong("Pin"));
                        data.setNotes(rs.getString("Notes"));
                        data.setR_id(rs.getInt("R_ID"));
                        data.setImage(rs.getBytes("image"));
                        dataLists.add(data);
                        }
				
			
		}
catch ( SQLException e) {
    
		}
finally  
{  


if (conn!=null)  
conn.close();  
}  

	return dataLists;
}

    public int DeleteRecord(int id) throws SQLException {
    
        try {
	String sql1="Delete from registration where R_ID = "+id+"";
	                      
                 ps=conn.prepareStatement(sql1);
                 i=ps.executeUpdate();
	     
        }
catch ( SQLException e) {
                    System.out.println("Delete Record "+e);
                    
		}
finally  
{  

if (conn!=null)  
conn.close();  
}  

          return i;
       
    }

    int UpdateRecord(int id, String fname, String lname, String fatname, String mno,String Email_id, String dob, Object city, Object state, String pin, String notes,String Address,byte[] image) throws SQLException {
          try {
              
	String sql1="Update registration set Fname=? ,Lname=? ,Father_Name=? ,Mobile_Num=? ,Email_ID=?"
                + " ,DOB=?,City=?,State=?,Pin=?,Notes=?, Address=? , image=? where R_ID = "+id+"";
	        ps=conn.prepareStatement(sql1);
                 ps.setString(1,fname );
                 ps.setString(2,lname );
                 ps.setString(3,fatname );
                 ps.setString(4,mno );
                 ps.setString(5,Email_id );
                 ps.setString(6,dob);
                 ps.setString(7,city.toString() );
                 ps.setString(8,state.toString() );
                 ps.setString(9,pin );
                 ps.setString(10,notes );
                     ps.setString(11,Address );
                  ps.setBytes(12,image);
                     i=ps.executeUpdate();
	       
        }
catch ( SQLException e) {
                    System.out.println("Update Record "+e);
                    
		}
finally  
{  

if (conn!=null)  
conn.close();  
}  

          return i;
       
    }

    ArrayList<HoldingRegistrationData> Registration_table_Record(java.sql.Date sqlDate) throws SQLException {
      
                           ArrayList<HoldingRegistrationData> Regis_dataList = new ArrayList<HoldingRegistrationData>();
		try {
	String sql1="Select Fname ,Lname ,Father_Name ,Phone_Num ,Mobile_Num ,Email_ID ,DOB,Address,City,State,Pin,Notes from Registration where Date_Of_Reg ='"+sqlDate+"'";
			                  
			ps=conn.prepareStatement(sql1);
			 rs=ps.executeQuery();
                    
			HoldingRegistrationData data = null;
			while(rs.next()){
                           
			data = new HoldingRegistrationData();
			data.setFname(rs.getString("Fname"));
                            
                        data.setLname(rs.getString("Lname"));
                            
                        data.setFather_Name(rs.getString("Father_Name"));
                        data.setPhone_Num(rs.getLong("Phone_Num"));
                      	data.setMobile_Num(rs.getLong("Mobile_Num"));
			data.setEmail_ID(rs.getString("Email_ID"));
                        data.setDOB(rs.getDate("DOB"));
                        data.setAddress(rs.getString("Address"));
                        data.setCity(rs.getString("State"));
                        data.setState(rs.getString("City"));
                        data.setPin(rs.getLong("Pin"));
                        data.setNotes(rs.getString("Notes"));
                        
                        Regis_dataList.add(data);
                        }
				
			
		}
catch ( SQLException e) {
    
		}
finally  
{  


if (conn!=null)  
conn.close();  
}  

	return Regis_dataList;
    }


  int UpdatePassword(String name,String new_pwd,String old_pwd) throws SQLException {
     
          try {
	String sql1="Update userlogin set User_Password = ? where User_Name = '"+name+"' and User_Password= '"+old_pwd+"'";
	                  
                 ps=conn.prepareStatement(sql1);
                 ps.setString(1,new_pwd);
                   i=ps.executeUpdate();
	     
        }
catch ( SQLException e) {
                    System.out.println("Update Record "+e);
                    
		}
finally  
{  

if (conn!=null)  
conn.close();  
}  

          return i;
       
    }

    
    
    
}
