/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package profilemanagement;




import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JTextField;


/**
 *
 * @author rajeev
 */
public class Connection {
    java.sql.Connection conn=null;
    PreparedStatement ps=null;
    ResultSet rs=null;
    private boolean b=false;
     boolean isValue=false;
    private boolean status=false;
     private int i;
    Connection() throws Exception
    { 
            try{
                 Class.forName("com.mysql.jdbc.Driver");
                 conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordinsert","root","root");
                   
               }
           catch(ClassNotFoundException | SQLException e){
                  System.err.println(e);
               }
    }
    
   
            
          public boolean insert(String Fname,String Lname,String Father_Name,String Phone_Num,String Mobile_Num,String Email_ID,String DOB,String Address,String City,String State,String Pin,String Notes,byte[] image) throws SQLException
    {     
           try
       {   
            java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());
           
             //SimpleDateFormat formater = new SimpleDateFormat("yyyy/MM/dd");  
                //String dates=   formater.format(DOB);
     String sql="insert into Registration(Fname,Lname,Father_Name,Phone_Num,Mobile_Num,Email_ID,DOB,Address,City,State,Pin,Date_Of_Reg,Notes,image)" + "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
   
     ps=conn.prepareStatement(sql);                         
     ps.setString(1, Fname);
			        ps.setString(2,Lname);
				ps.setString(3,Father_Name);
				ps.setString(4,Phone_Num);
                                ps.setString(5, Mobile_Num);
			        ps.setString(6,Email_ID);
				ps.setString(7,DOB);
				ps.setString(8,Address);
                                ps.setString(9, City);
			        ps.setString(10,State);
				ps.setString(11,Pin);
				ps.setDate(12,sqlDate);
                                ps.setString(13,Notes);
                                ps.setBytes(14,image);

                                   ps.execute();
         
                     }
       
           catch(SQLException exe)
           {
               System.out.println(exe);
            b=true;
           }

           return b;
       
       }
                
      boolean Checking(JTextField Fname, JTextField Lname, String Fatname1, String Mno1, String DOB2) throws SQLException{
  
    try{
         ps=conn.prepareStatement("select * from Registration where Fname='"+Fname+"' && Lname='"+Lname+"' && Father_name='"+Fatname1+"' && DOB='"+DOB2+"' && Mobile_Num='"+Mno1+"' ");
         ps.execute();
         
       }
    catch(SQLException exec)
    {
      
        isValue=true;
    }


    return isValue;
          

     
          
}

    boolean Login(String username1, char[] password1) throws SQLException {
            
         //To change body of generated methods, choose Tools | Templates.
      // String sql="select User_Email_ID from userlogin where User_Name='"+username1+"' && User_Password='"+password1+"' ";
         try{
         ps=conn.prepareStatement("SELECT * FROM userlogin where User_Name='"+username1+"' AND User_Password='" + new String(password1) + "'");

		 rs=ps.executeQuery();
		status=rs.next();
              
         
       }
    catch(SQLException exec)
    {
       
       status=false;
    }


         return status;
 
    }

    
    public ArrayList<DataHolder> DropState() throws SQLException{
	
	
		ArrayList<DataHolder> dataList1 = new ArrayList<DataHolder>();
		try {
			String sql="Select State from State";
			
			ps=conn.prepareStatement(sql);
			 rs=ps.executeQuery();
			DataHolder data = null;
			while(rs.next()){
				data= new DataHolder();
			
                        data.setState(rs.getString("State"));
				dataList1.add(data);
			}
				
			
		}
catch ( SQLException e) {
    
		}


	return dataList1;
}

   
    
    
    public ArrayList<DataHolder> DropCity(String State) throws SQLException{
	
	
		ArrayList<DataHolder> dataList1 = new ArrayList<DataHolder>();
		try {
			String sql="Select City from City where sid=(select id from State where State='"+State+"' )";
			
			ps=conn.prepareStatement(sql);
			 rs=ps.executeQuery();
			DataHolder data1 = null;
			while(rs.next()){
				data1 = new DataHolder();
			data1.setCity(rs.getString("City"));
                      	dataList1.add(data1);
			}
				
			
		}
catch ( SQLException e) {
    
		}


	return dataList1;
}

   
    public ArrayList<HolderClass> DropHost(String HostName) throws SQLException{
	
	
		ArrayList<HolderClass> dataList= new ArrayList<HolderClass>();
		try {
			String sql="Select Port from Host where Host_Name='"+HostName+"'";
			
			ps=conn.prepareStatement(sql);
			 rs=ps.executeQuery();
			HolderClass data = null;
			while(rs.next()){
				data= new HolderClass();
			
                        data.setPort(rs.getString("Port"));
                         
				dataList.add(data);
			}
				
			
		}
catch ( SQLException e) {
    
		}
  
	return dataList;
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