package collection.map;
import java.util.HashMap;


public class TestHashmap {

	/**
	 * @param args
	 */
	private static HashMap<String, Integer> seclDOReleaseTypeMap = new HashMap<String, Integer>();
    private static HashMap<String, Integer> seclDOPriorityTypeMap = new HashMap<String, Integer>();
    private static HashMap<String, String> seclGradeCodeMasterMap = new HashMap<String, String>();
    public  static HashMap<Integer, String> seclMinesDetailsMap = new HashMap<Integer, String>();
    
    private static final String ISSUE_NO_EXIST= "Select 1 from mines_do_details where do_release_no = ?";
    private static final String DO_NO_EXIST= "Select 1 from mines_do_details where do_number = ?";
    private static final String FETCH_SOURCE_CODE = "Select name from mines_details where name = ?";
    private static final String FETCH_SAP_CODE = "Select id from customer_details where sap_code = ?";
    private static final String FETCH_SAP_ID = "Select id from customer_details where sap_code = ?";
  
    static{
        seclGradeCodeMasterMap.put("G-1", "G1");
        seclGradeCodeMasterMap.put("G-2", "G2");
        seclGradeCodeMasterMap.put("G-3", "G3");
        seclGradeCodeMasterMap.put("G-4", "G4");
        seclGradeCodeMasterMap.put("G-5", "G5");
        seclGradeCodeMasterMap.put("G-6", "G6");
        seclGradeCodeMasterMap.put("G-7", "G7");
        seclGradeCodeMasterMap.put("G-8", "G8");
        seclGradeCodeMasterMap.put("G-9", "G9");
        seclGradeCodeMasterMap.put("G-10", "G10");
        seclGradeCodeMasterMap.put("G-11", "G11");
        seclGradeCodeMasterMap.put("ROM", "ROM");
        
        seclDOPriorityTypeMap.put("POWER", 1);
        seclDOPriorityTypeMap.put("CPP (CAPTIVE POWER PLANT)", 2);
        seclDOPriorityTypeMap.put("CAPTIVE POWER PLANT", 2);
        seclDOPriorityTypeMap.put("RRM (RE ROLLING MILLS)", 3);
        seclDOPriorityTypeMap.put("RE ROLLING MILLS", 3);
        seclDOPriorityTypeMap.put("RE-ROLLING MILLS", 3);
        seclDOPriorityTypeMap.put("CEMENT", 4);
        seclDOPriorityTypeMap.put("SPONGE", 5);
        seclDOPriorityTypeMap.put("PAPER", 6);
        seclDOPriorityTypeMap.put("BRICK", 7);
        seclDOPriorityTypeMap.put("STATE AGENCY", 8);
        seclDOPriorityTypeMap.put("ALUMINIUM", 9);
        seclDOPriorityTypeMap.put("AS IS WHERE IS BASIS", 10);
        seclDOPriorityTypeMap.put("LINKAGE AUCTION", 11);
        seclDOPriorityTypeMap.put("E-AUCTION", 12);
        seclDOPriorityTypeMap.put("EAUCTION", 12);
        seclDOPriorityTypeMap.put("E- AUCTION", 12);
        seclDOPriorityTypeMap.put("F EAUCTION", 13);
        seclDOPriorityTypeMap.put("FEAUCTION", 13);
        seclDOPriorityTypeMap.put("F E-AUCTION", 13);
        seclDOPriorityTypeMap.put("SF- AUCTION", 14);
        seclDOPriorityTypeMap.put("S F-AUCTION", 14);
        seclDOPriorityTypeMap.put("SFAUCTION", 14);
        seclDOPriorityTypeMap.put("SF-AUCTION", 14);
        seclDOPriorityTypeMap.put("X- AUCTION", 15);
        seclDOPriorityTypeMap.put("X-AUCTION", 15);
        seclDOPriorityTypeMap.put("XAUCTION", 15);
        
        seclDOReleaseTypeMap.put("POWER", 1);
        seclDOReleaseTypeMap.put("NON POWER", 2);
        seclDOReleaseTypeMap.put("FUEL SUPPLY AGREEMENT", 2);
        seclDOReleaseTypeMap.put("AIWI", 3);
        seclDOReleaseTypeMap.put("LINKAGE AUCTION", 4);
        seclDOReleaseTypeMap.put("FSA THROUGH LINKAGE AUCTION", 4);
        seclDOReleaseTypeMap.put("EAUCTION", 5);
        seclDOReleaseTypeMap.put("E- AUCTION", 5);
        seclDOReleaseTypeMap.put("FEAUCTION", 6);
        seclDOReleaseTypeMap.put("F EAUCTION", 6);
        seclDOReleaseTypeMap.put("FE-AUCTION", 6);
        seclDOReleaseTypeMap.put("F E-AUCTION", 6);
        seclDOReleaseTypeMap.put("S F-AUCTION", 7);
        seclDOReleaseTypeMap.put("SFAUCTION", 7);
        seclDOReleaseTypeMap.put("SF-AUCTION", 7);
        seclDOReleaseTypeMap.put("X- AUCTION", 8);
        seclDOReleaseTypeMap.put("X-AUCTION", 8);
        seclDOReleaseTypeMap.put("X AUCTION", 8);
        seclDOReleaseTypeMap.put("XAUCTION", 8);
    }
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//	System.out.println(seclDOReleaseTypeMap.get("XAUCTION"));
//	String doNo = "201701/4909/08035";
//	Pattern pattern = Pattern.compile(".*/([^']*)/.*");
//    String mydata = "some string with /the data i want/ inside";
//
//    Matcher matcher = pattern.matcher(mydata);
//    if(matcher.matches()) {
//        System.out.println(matcher.group(1));
//    }
//    
//	String s[] = doNo.split("/");
//	String unitCode = s.length == 3 ? s[1] : "hello";  
//    System.out.println(unitCode);
		
		String strNew = "234567890";
		System.out.println(strNew.substring(0, 2));
		
	}

		
	

}
