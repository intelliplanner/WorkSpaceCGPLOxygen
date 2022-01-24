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
import java.util.ArrayList;
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

   

   
    
}