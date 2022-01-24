
create database recordinsert;

 CREATE TABLE `city` (                                                     
          `cid` int(11) NOT NULL auto_increment,                                  
          `sid` int(11) NOT NULL,                                                 
          `City` varchar(15) default NULL,                                        
          PRIMARY KEY  (`cid`),                                                   
          KEY `sid` (`sid`),                                                      
          CONSTRAINT `city_ibfk_1` FOREIGN KEY (`sid`) REFERENCES `state` (`id`)  
        ) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1                   ;
 CREATE TABLE `employee` (                                
            `id` int(11) NOT NULL auto_increment,                  
            `name` varchar(255) default NULL,                      
            `salary` int(11) default NULL,                         
            PRIMARY KEY  (`id`)                                    
          ) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1  ;
CREATE TABLE `host` (                                    
          `id` int(11) NOT NULL auto_increment,                  
          `Host_Name` varchar(40) default NULL,                  
          `Port` varchar(40) default NULL,                       
          PRIMARY KEY  (`id`)                                    
        ) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1  ;

CREATE TABLE `records` (                                 
           `ID` int(11) NOT NULL auto_increment,                  
           `Fname` varchar(15) NOT NULL,                          
           `Lname` varchar(20) default NULL,                      
           `Father_Name` varchar(30) default NULL,                
           `Mobile_Num` decimal(10,0) default NULL,               
           `Phone_Num` decimal(10,0) default NULL,                
           `Address` varchar(50) default NULL,                    
           `Email_ID` varchar(30) default NULL,                   
           `DOB` date default NULL,                               
           `DOA` date default NULL,                               
           `City` varchar(15) NOT NULL,                           
           `Country` varchar(20) NOT NULL,                        
           `Pin` decimal(6,0) default NULL,                       
           PRIMARY KEY  (`ID`)                                    
         ) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1  
;
CREATE TABLE `registration` (                             
                `R_ID` int(11) NOT NULL auto_increment,                 
                `Fname` varchar(40) default NULL,                       
                `Lname` varchar(40) default NULL,                       
                `Father_Name` varchar(40) default NULL,                 
                `Phone_Num` decimal(10,0) default NULL,                 
                `Mobile_Num` decimal(10,0) default NULL,                
                `Email_ID` varchar(30) default NULL,                    
                `DOB` date default NULL,                                
                `Address` varchar(50) default NULL,                     
                `City` varchar(15) default NULL,                        
                `State` varchar(20) default NULL,                       
                `Pin` decimal(6,0) default NULL,                        
                `Date_Of_Reg` date default NULL,                        
                `Notes` varchar(120) default NULL,                      
                `image` longblob,                                       
                PRIMARY KEY  (`R_ID`)                                   
              ) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1  ;

CREATE TABLE `state` (                                   
          `id` int(11) NOT NULL auto_increment,                  
          `State` varchar(40) default NULL,                      
          PRIMARY KEY  (`id`)                                    
        ) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1  
;
 CREATE TABLE `userlogin` (                               
             `ID` int(11) NOT NULL auto_increment,                  
             `User_Name` varchar(40) default NULL,                  
             `User_Password` varchar(40) default NULL,              
             `User_Email_ID` varchar(40) default NULL,              
             PRIMARY KEY  (`ID`)                                    
           ) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1  ;



