/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.constant;

import com.ipssi.gen.utils.Misc;

/**
 *
 * @author Vi$ky
 */
public class Type {

    public static class Reader {

        public static final int IN = 0;
        public static final int OUT = 1;
    }

    public static class WorkStationType {

        public static final int GATE_IN_TYPE = 1;
        public static final int REGISTRATION = 2;
        public static final int WEIGH_BRIDGE_IN_TYPE = 3;
        public static final int YARD_IN_TYPE = 4;
        public static final int YARD_OUT_TYPE = 5;
        public static final int WEIGH_BRIDGE_OUT_TYPE = 6;
        public static final int GATE_OUT_TYPE = 7;
        public static final int GPS_REPAIRED = 8;
        public static final int FLY_ASH_IN_TYPE = 9;
        public static final int FLY_ASH_TARE_WT_TYPE = 10;
        public static final int FLY_ASH_GROSS_WT_TYPE = 11;
        public static final int STONE_TARE_WT_TYPE  = 12;
        public static final int STONE_GROSS_WT_TYPE = 13;
        public static final int FIRST_WEIGHTMENT_TYPE  = 14;
        public static final int SECOND_WEIGHTMENT_TYPE  = 15;
        //secl system type
        public static final int SECL_REG = 20;
        public static final int SECL_REG_GATE_IN = 21;
        public static final int SECL_LOAD_GATE_IN = 22;
        public static final int SECL_LOAD_INT_WB_TARE= 23;
        public static final int SECL_LOAD_ROAD_WB_TARE= 24;
        public static final int SECL_LOAD_INT_YARD_IN= 25;
        public static final int SECL_LOAD_INT_YARD_OUT= 26;
        public static final int SECL_LOAD_INT_WB_GROSS= 27;
        public static final int SECL_LOAD_ROAD_WB_GROSS= 28;
        public static final int SECL_LOAD_WASHERY_TARE= 29;
        public static final int SECL_LOAD_WASHERY_GROSS= 30;
        public static final int SECL_LOAD_GATE_OUT = 31;
        
        
        public static final int SECL_UNLOAD_GATE_IN = 32;
        public static final int SECL_UNLOAD_INT_WB_GROSS= 33; 
        public static final int SECL_UNLOAD_INT_YARD_IN= 34;
        public static final int SECL_UNLOAD_INT_YARD_OUT= 35;
        
        public static final int SECL_UNLOAD_INT_WB_TARE= 36;
        public static final int SECL_UNLOAD_GATE_OUT = 37;
        
        public static final int SECL_OTHER_FIRST = 38;
        public static final int SECL_OTHER_SECOND = 39;
        
     // CGPL
        public static final int CGPL_LOAD_GATE_IN = 41;
        public static final int CGPL_LOAD_WB_OUT= 42; 
        public static final int CGPL_LOAD_WB_IN=43;
        public static final int CGPL_LOAD_GATE_OUT= 44;
        
     // PPGCL
        public static final int PPGCL_REGISTRATION = 51;
        public static final int PPGCL_LOAD_GATE_IN = 52;
        public static final int PPGCL_LOAD_WB_OUT= 53; 
        public static final int PPGCL_LOAD_WB_IN=54;
        public static final int PPGCL_LOAD_GATE_OUT= 55;
        
        public static String getString(int key){
        	String retval = null;
        	switch(key){
        	case GATE_IN_TYPE : retval = "Coal Gate In"; break;
            case WEIGH_BRIDGE_IN_TYPE : retval = "Coal WB In"; break;
            case YARD_IN_TYPE : retval = "Coal Yard In"; break;
            case YARD_OUT_TYPE : retval = "Coal Yard Out"; break;
            case WEIGH_BRIDGE_OUT_TYPE : retval = "Coal WB Out"; break;
            case GATE_OUT_TYPE : retval = "Coal Gate Out"; break;
            case REGISTRATION : retval = "Registration"; break;
            case GPS_REPAIRED : retval = "GPS Repair Center"; break;
            case FLY_ASH_IN_TYPE : retval = "FlyAsh Gate In"; break;
            case FLY_ASH_GROSS_WT_TYPE : retval = "FlyAsh WB Gross"; break;
            case FLY_ASH_TARE_WT_TYPE : retval = "FlyAsh WB Tare"; break;
            case STONE_GROSS_WT_TYPE : retval = "Stone Gross"; break;
            case STONE_TARE_WT_TYPE  : retval = "Stone Tare"; break;
            case FIRST_WEIGHTMENT_TYPE  : retval = "First Weighment"; break;
            case SECOND_WEIGHTMENT_TYPE  : retval = "Second Weighment"; break;
            
            
            case SECL_REG  : retval = "Vehicle Registeration"; break;
            case SECL_REG_GATE_IN  : retval = "Registeration And Gate"; break;
            case SECL_LOAD_GATE_IN  : retval = "Load Gate In"; break;
            case SECL_LOAD_INT_WB_TARE  : retval = "Internal Shifting Tare Weighment"; break;
            case SECL_LOAD_INT_WB_GROSS  : retval = "Internal Shifting Gross Weighment"; break;
            case SECL_LOAD_ROAD_WB_TARE  : retval = "Road Sale Tare Weighment"; break;
            case SECL_LOAD_ROAD_WB_GROSS  : retval = "Road Sale Gross Weighment"; break;
            case SECL_LOAD_WASHERY_TARE : retval = "Washery Tare Weighment"; break;
            case SECL_LOAD_WASHERY_GROSS  : retval = "Washery Gross Weighment"; break;
            case SECL_LOAD_GATE_OUT : retval = "Load Gate Out"; break;
            case SECL_UNLOAD_GATE_IN  : retval = "Unload Gate"; break;
            case SECL_UNLOAD_INT_WB_GROSS  : retval = "Siding Weigh Bridge-Gross"; break;
            case SECL_UNLOAD_INT_WB_TARE  : retval = "Siding Weigh Bridge-Tare"; break;
            case SECL_LOAD_INT_YARD_IN : retval = "Load Gate In"; break;
            case SECL_LOAD_INT_YARD_OUT : retval = "Load Gate Out"; break;
            case SECL_UNLOAD_INT_YARD_IN : retval = "Load Gate In"; break;
            case SECL_UNLOAD_INT_YARD_OUT : retval = "Load Gate Out"; break;
            
            case CGPL_LOAD_GATE_IN : retval = "Load Gate In"; break;
            case CGPL_LOAD_WB_OUT : retval = "Load Wb Tare"; break;
            case CGPL_LOAD_WB_IN : retval = "Load WB GROSS"; break;
            case CGPL_LOAD_GATE_OUT : retval = "Load Gate Out"; break;
            
            case PPGCL_REGISTRATION : retval = "Registration"; break;
            case PPGCL_LOAD_GATE_IN : retval = "Load Gate In"; break;
            case PPGCL_LOAD_WB_OUT : retval = "Load Wb Tare"; break;
            case PPGCL_LOAD_WB_IN : retval = "Load WB GROSS"; break;
            case PPGCL_LOAD_GATE_OUT : retval = "Load Gate Out"; break;
            
        	}
        	return retval;
        }
        
    }
    public static class BlockingInstruction {
    	//manual blocking
    	public static final int BLOCK_DUETO_BLACKLIST = 1;
    	public static final int BLOCK_DUETO_NEXT_STEP = 1001;
    	
    	//process and data blocking
    	public static final int BLOCK_DUETO_STEP_JUMP = 2001;
    	public static final int BLOCK_DUETO_MULTIPLE_TPR = 2002;
    	
    	//conditional blocking
    	public static final int BLOCK_DUETO_QC = 3001;
        public static final int BLOCK_DUETO_GPS = 3002;
        public static final int BLOCK_DUETO_DRUNCK = 3003;
        public static final int BLOCK_DUETO_HEADLIGHT = 3004;
        public static final int BLOCK_DUETO_BACKLIGHT = 3005;
        public static final int BLOCK_DUETO_DOC_INCOMPLETE = 3006;
        
        
		//question based blocking
        public static final int BLOCK_DUETO_TAG_NOT_READ= 4001;
		public static final int BLOCK_DUETO_FINGER_NOT_VERIFIED = 4002;
		public static final int BLOCK_DUETO_FINGER_NOT_CAPTURED = 4003;
		public static final int BLOCK_DUETO_VEHICLE_NOT_EXIST = 4004;
		public static final int BLOCK_DUETO_CHALLAN_NOT_EXIST = 4005;
        
		public static final int BLOCK_DUETO_FITNESS_EXPIRED = 4006;
		public static final int BLOCK_DUETO_ROAD_PERMIT_EXPIRED = 4007;
		public static final int BLOCK_DUETO_INSURANCE_EXPIRED = 4008;
		public static final int BLOCK_DUETO_POLUTION_EXPIRED = 4009;
		public static final int BLOCK_DUETO_NOT_INFORMED_GPS_VENDOR = 4010;
		public static final int BLOCK_DUETO_DRIVER_NOT_EXIST = 4011;
		public static final int BLOCK_DUETO_FINGER_NOT_EXIST = 4012;
		public static final int BLOCK_DUETO_DRIVER_BLACKLISTED = 4013;
		public static final int BLOCK_DUETO_NO_TARE_ALLOWED = 4014;
		public static final int BLOCK_DUETO_ACCESS_DENIED = 4015;
		public static final int BLOCK_DUETO_DO_EXPIARED= 4016;
		public static final int BLOCK_DUETO_CARD_NOT_RETURNED = 4017;
		public static final int BLOCK_DUETO_DO_QUOTA = 4018;
		public static final int BLOCK_DUETO_EXIT_WITHOUT_WEIGHMENT = 4019;
		public static final int BLOCK_DUETO_TRIP_BEFORE_GAP = 4020;
		
		public static final int BLOCK_DUETO_UNSAFE_DRIVING = 4021;
		public static final int BLOCK_DUETO_DAMAGED_PLANT_PROPERTY=4022;
		public static final int BLOCK_DUETO_DRIVER_BEHAVIOUR=4023;
		
		public static String getBlockingStr(int id){
        	switch(id){
        	case BLOCK_DUETO_BLACKLIST: return "Vehicle Blacklisted";
        	case BLOCK_DUETO_DOC_INCOMPLETE: return "Document Incomplete";
        	case BLOCK_DUETO_NEXT_STEP: return "Step Block";
        	case BLOCK_DUETO_STEP_JUMP: return "Step Jump";
        	case BLOCK_DUETO_MULTIPLE_TPR : return "Multiple Open TPR";
        	case BLOCK_DUETO_QC: return "QC Not Done";
        	case BLOCK_DUETO_GPS: return "GPS Not Ok";
        	case BLOCK_DUETO_DRUNCK: return "Driver Found Drunck";
        	case BLOCK_DUETO_HEADLIGHT: return "Headlight Not Ok";
        	case BLOCK_DUETO_BACKLIGHT: return "Backlight Not Ok";
        	case BLOCK_DUETO_TAG_NOT_READ: return "Tag Not Working";
        	case BLOCK_DUETO_FINGER_NOT_VERIFIED: return "Finger Not Verified";
        	case BLOCK_DUETO_FINGER_NOT_CAPTURED: return "Finger Not Captured";
        	case BLOCK_DUETO_VEHICLE_NOT_EXIST: return "Vehicle Not Exist";
        	case BLOCK_DUETO_CHALLAN_NOT_EXIST: return "Challan Not Exist";
        	
        	case BLOCK_DUETO_FITNESS_EXPIRED: return "Fitness Expired";
        	case BLOCK_DUETO_ROAD_PERMIT_EXPIRED: return "Road Permit Expired";
        	case BLOCK_DUETO_INSURANCE_EXPIRED: return "Insurance Expired";
        	case BLOCK_DUETO_POLUTION_EXPIRED: return "Polution Expired";
        	case BLOCK_DUETO_NOT_INFORMED_GPS_VENDOR: return "Not Informed GPS Vendor";
        	case BLOCK_DUETO_DRIVER_NOT_EXIST: return "Driver Reg. Not Done";
        	case BLOCK_DUETO_FINGER_NOT_EXIST: return "Driver Finger Not Reg.";
        	case BLOCK_DUETO_DRIVER_BLACKLISTED: return "Driver Blacklisted";
        	case BLOCK_DUETO_NO_TARE_ALLOWED: return "No Tare";
        	case BLOCK_DUETO_ACCESS_DENIED: return "Access Not Allowed";
        	case BLOCK_DUETO_DO_EXPIARED: return "DO Expired or Exhausted";
        	case BLOCK_DUETO_CARD_NOT_RETURNED: return "Card Not Returned";
        	case BLOCK_DUETO_DO_QUOTA: return " DO Quota Full";
        	case BLOCK_DUETO_EXIT_WITHOUT_WEIGHMENT: return " Exit Without Weighment";
        	case BLOCK_DUETO_TRIP_BEFORE_GAP: return " Min Trip Gap Not Completed";
        	case BLOCK_DUETO_UNSAFE_DRIVING: return "Unsafe Driving";
        	case BLOCK_DUETO_DAMAGED_PLANT_PROPERTY: return "Damaged Plant Property";
        	case BLOCK_DUETO_DRIVER_BEHAVIOUR: return "Misbehavior with Staff";
        	
        	default:
        		return "NA";
        	}
        }
    }
    public static class TPRMATERIAL {
    	//(0=coal, 1=stone, 2=flyash, 3=other)
        public static final int COAL = 0;
        public static final int  STONE = 1;
        public static final int  FLYASH = 2;
        public static final int  OTHERS = 3;
        public static final int  COAL_INTERNAL = 4;
        public static final int  COAL_ROAD = 5;
        public static final int  COAL_WASHERY = 6;
        public static final int  COAL_OTHER = 7;
        public static final int  FLYASH_CGPL = 8;
        public static String getStr(int id){
        	switch(id){
        	case COAL: return "COAL";
        	case STONE: return "STONE";
        	case FLYASH: return "FLYASH";
        	case OTHERS: return "OTHERS";
        	case COAL_INTERNAL: return "COAL";
        	case COAL_ROAD: return "COAL";
        	case COAL_WASHERY: return "COAL";
        	case COAL_OTHER: return "OTHER";
        	case FLYASH_CGPL: return "FLYASH";
        	default : return "NA";
        	}
        }
    }
    public static class RFID_CARD_PURPOSE{
    	public static final int INTERNAL = 0;
        public static final int ROAD = 1;
        public static final int WASHERY = 2;
        public static final int OTHER = 3;
        public static final int CGPL_INTERNAL = 4;
        public static String getString(int id){
        	switch(id){
        		case INTERNAL:return "Internal Shifting";
        		case ROAD:return "Road Sale";
        		case WASHERY:return "Washery";
        		case OTHER:return "Other";
        	}
        	return null;
        }
        public static int getMaterialCatByCardPurpose(int id){
        	switch(id){
        	case INTERNAL:return TPRMATERIAL.COAL_INTERNAL;
        	case ROAD:return TPRMATERIAL.COAL_ROAD;
        	case WASHERY:return TPRMATERIAL.COAL_WASHERY;
        	case OTHER:return TPRMATERIAL.COAL_OTHER;
        	case CGPL_INTERNAL:return TPRMATERIAL.FLYASH_CGPL;
        	}
        	return Misc.getUndefInt();
        }
        public static int getCardPurposeByMaterialCat(int materialCat){
        	switch(materialCat){
        	case TPRMATERIAL.COAL_INTERNAL:return INTERNAL;
        	case TPRMATERIAL.COAL_ROAD:return ROAD;
        	case TPRMATERIAL.COAL_WASHERY:return WASHERY;
        	case TPRMATERIAL.COAL_OTHER:return OTHER;
        	case TPRMATERIAL.FLYASH_CGPL:return CGPL_INTERNAL;
        	}
        	return Misc.getUndefInt();
        }
    }
    public static class RFID_CARD_TYPE{
    	public static final int PERMANENT = 0;
    	public static final int TEMPORARY = 1;
        public static final int NO_TAG = 2;
        public static String getString(int id){
        	switch(id){
        		case TEMPORARY:return "Temporary";
        		case PERMANENT:return "Permanent";
        		case NO_TAG:return "No Tag";
        	}
        	return null;
        }
    }
    public static class RFID_AREA_TYPE{
    	public static final int LOAD = 0;
    	public static final int UNLOAD = 1;
    }
    public static class RFID_PRINT_ON_SAVE{
    	public static final int ONLY_TARE = 0;
    	public static final int ONLY_GROSS = 1;
    	public static final int BOTH = 2;
    	public static String getString(int id){
        	switch(id){
        		case ONLY_TARE:return "Print on tare only";
        		case ONLY_GROSS:return "Print on gross only";
        		case BOTH:return "Print on both";
        	}
        	return null;
        }
    }
    public static class MiningAreaType{
    	public static final int MINES=0;
    	public static final int SIDING=1;
    	public static final int WASHERY=2;
    	public static final int SUB_AREA=3;
    	public static final int AREA=4;
    	public static final int ORGANISATION=5;
    	public static String getString(int id){
    		switch(id){
    			case MINES:return "Mines";
    			case SIDING:return "Siding";
    			case WASHERY:return "Washery/Stockyard";
    			case SUB_AREA:return "Sub Area";
    			case AREA:return "Area";
    			case ORGANISATION:return "Organisation";
    		}
    		return null;
    	}
    }
    public static class ANSWER{
    	public static final int YES = 1;
    	public static final int NO = 2;
    	public static final int NC = 3;
    	public static final int NOSELECTED = 4;
    }
    public static class MinesDoDetails{
    	public static class TransPortMode{
    		public static final int byRoad = 1;
        	public static final int byRail = 2;
        	public static String getString(int id){
        		switch (id) {
				case byRoad:
					return "by Road";
				case byRail:
					return "by Rail";
				default:
					return null;
				}
        	}
    	}
    	public static class TypeOfConsumer{
    		public static final int power = 0;
        	public static final int nonPower = 1;
        	public static final int eAuction = 2;
        	public static final int other = 3;
        	public static String getString(int id){
        		switch (id) {
        		case power:
        			return "POWER";
        		case nonPower:
        			return "NON-POWER";
        		case eAuction:
        			return "E-AUCTION";
        		case other:
        			return "OTHER";
        		default:
        			return null;
        		}
        	}
    	}
    }
    public static class RFID_GATE_OPERATIVE_TYPE{
    	public static final int GATE = 0;
    	public static final int YARD = 1;
    	public static String getString(int id){
        	switch(id){
        		case GATE:return "GATE";
        		case YARD:return "YARD";
        	}
        	return null;
        }
    }
}
