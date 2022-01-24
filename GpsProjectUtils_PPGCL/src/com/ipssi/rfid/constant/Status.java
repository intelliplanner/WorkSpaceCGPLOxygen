package com.ipssi.rfid.constant;

public class Status {

	public static final int DELETED = 0;
	public static final int ACTIVE = 1;
	public static final int INACTIVE = 2;
	public static final int ERROR = 100;
	
    public static class Workstate {

        public static final int IDLE = 0;
        public static final int BUSY = 1;
        public static final int CLEAR = 2;
        
    }

   
    public static class Token{
           public static final int ASSIGNED = 0;
           public static final int PROCESSED = 1;
           public static final int INSPECTION = 2;
           public static final int CLEAR = 3;
     }
     
     public static class TPR{
           public static final int OPEN = 0;
           public static final int PENDING = 1;
           public static final int CLOSE = 2;
     }
     
     public static class VALIDATION{
           public static final int NO_ISSUE = 0;
           public static final int DUPLICATE_CHALLAN = 1;
           public static final int DUPLICATE_LR = 2;
           public static final int DUPLICATE_ALL = 3;
           public static final int EXPIRED = 4;
           public static final int MULTIPLE_CHALLAN_EXIST = 5;
     }

    public static class TPRQuestion {

        public static final int sealOk = 1;
        public static final int tarpaulinOk = 2;
        public static final int numberVisible = 3;
        public static final int tailLightOk = 4;
        public static final int sideMirror = 5;
        public static final int leftSideIndicator = 6;
        public static final int seatBeltWorm = 7;
        public static final int headLightOk = 8;
        public static final int reverseHornOk = 9;
        public static final int breathLyzerOk = 10;
        public static final int drivenByHelper = 11;
        // public static final int helperOk = 12;
	   public static final int paperChallan = 13;
        public static final int markForQc = 14;
	   public static final int gpsOk = 15;
        public static final int qcRequired = 16;
        public static final int gpsRepairRequired = 17;
	   public static final int MarkforQC = 18;
        public static final int LRCollected = 19;
        public static final int ChallanCollected = 20;
        public static final int PermitCollected = 21;
        public static final int ProcessIssue = 22;
        public static final int gpsRepairNeeded = 23;
        public static final int haveYouInformedIIA = 24;
        //public static final int qcRequired = 25;
        public static final int getGpsFixed = 26;
		
	   public static final int cleanFinger = 27;
	   public static final int thankYou = 28;
		public static final int tryAgainFinger = 29;
		public static final int tryOnceAgainFinger = 30;
		public static final int fingerNotMatch = 31;
		public static final int minesAndTransporterFromChallan = 32;
		public static final int hornPlay = 33;
		public static final int pushBrake = 34;
		public static final int brakeLightOn = 35;
		public static final int headLightOn = 36;
		
		public static final int rightSideIndicator = 37	;
		public static final int leftSideIndicatorOn = 38;
		public static final int rightSideIndicatorOn = 39;
		public static final int enterDriverIdAndDriverName = 40;
		public static final int driverAppearsDrunk = 41;
		public static final int goToWB = 42;
		public static final int vehicleBlackListed = 43;
		public static final int fixedLr = 44;
		public static final int registrationNewVehicle = 45;
		public static final int issueRfidTag = 46;
		public static final int challanEntry = 47;
		public static final int updateFingerPrint = 48;
		public static final int getDriverRegistration = 49;
		public static final int paperNotValid = 50;
		public static final int goToRestrationCenter = 51;
		public static final int InformControlRoom = 52;
		public static final int dumpCoal = 53;
		public static final int getQcStamp = 54;
		public static final int getGpsRepairedForGateOut = 55;
		public static final int getGpsRepaired = 56;
		public static final int qcStampDonegoParking = 57;
		public static final int qcDone = 58;
		
		
		public static final int isTagRead = 59;
		public static final int isFingerVerified = 60;
		public static final int isFingerCaptured = 61;
		public static final int isVehicleExist = 62;
		public static final int isChallanExist = 63;
		
		public static final int isFitnessOk = 64;
		public static final int isRoadPermitOk = 65;
		public static final int isInsuranceOk = 66;
		public static final int isPolutionOk = 67;
		
		public static final int saveDetail = 68;
		public static final int startFingerCapturing = 69;
		
		public static final int barrierGps = 70;
		public static final int barrierQc = 71;
		
		public static final int bedOne = 72;
		public static final int bedTwo = 73;
		public static final int bedThree = 74;
		public static final int bedFour = 75;
		public static final int bedFive = 76;
		public static final int bedSix = 77;
		
		public static final int hopperOne = 78;
		public static final int hopperTwo = 79;
		public static final int hopperThree = 80;
		public static final int hopperFour = 81;
		public static final int hopperFive = 82;
		public static final int hopperSix = 83;
		
		public static final int haveYouInformedGpsVendor = 84;
		public static final int isDriverExist = 85;
		public static final int isFingerExist = 86;
		public static final int isDriverBlacklisted = 87;
		
		
		public static final int isNoTareAllowed = 100;
		public static final int isAccessAllowed = 101;
		public static final int isDoValid= 102;
		public static final int isTempCardReturned = 103;
		public static final int isQuotaRemaing = 104;
		public static final int isWeighmentCompleted = 105;
		public static final int isTripGapProper = 106;
		public static final int isUnSafeDriving = 107;
		public static final int isPlantPropertyDamaged = 108;
		public static final int isMisBehaviorByDriver = 109;
    }


  
    
    public static class Voice {

        public static String getVoicePath(int val) {
            String path = "";
            switch (val) {
                case TPRQuestion.sealOk:
                    path = "D:\\testvoice.wav";
                    break;
                case TPRQuestion.tarpaulinOk:
                    path = "D:\\testvoice.wav";
                    break;
                case TPRQuestion.numberVisible:
                    path = "D:\\testvoice.wav";
                    break;
                case TPRQuestion.tailLightOk:
                    path = "D:\\testvoice.wav";
                    break;
                case TPRQuestion.sideMirror:
                    path = "D:\\testvoice.wav";
                    break;
                /*case TPRQuestion.sideIndicator:
                    path = "D:\\testvoice.wav";
                    break;*/
                case TPRQuestion.seatBeltWorm:
                    path = "D:\\testvoice.wav";
                    break;
                case TPRQuestion.headLightOk:
                    path = "D:\\testvoice.wav";
                    break;
                case TPRQuestion.reverseHornOk:
                    path = "D:\\testvoice.wav";
                    break;
                case TPRQuestion.breathLyzerOk:
                    path = "D:\\testvoice.wav";
                    break;
                default:
                    path = "D:\\testvoice.wav";
                    break;
            }
            return path;
        }


    }
    public static class VEHICLE{
    	public static final int EXISTING_RF = 0;
    	public static final int EXISTING_MANUAL = 1;
    	public static final int NEW_MANUAL = 2;
    }
    
    public static class TPR_MERGE_STATUS{
    	public static final int MERGED = 0;
    	public static final int UNMEREGABLE = 1;
    	public static final int CREATE_NEW= 2;
    	public static final int FILL_BOTH_CURRENT= 3;
    	public static final int FILL_RHS_CURRENT= 4;
    }
    
    public static class TPR_REPORTING_STATUS{
    	public static final int REPORTED=1;
    	public static final int DISPATCHED=2;
    	public static final int IN_PLANT=3;
    	public static final int PRE_AUDIT=4;
    	public static final int SAP_READY=5;
    	public static final int SAP_REPORTED=6;
    	public static final int SAP_REJECTED=7;
    	public static final int CANCELLED=8;
    	public static final int NOT_RECIEVED=9;
    	public static final int PRE_DISPATCH_CANCELLED=10;
    }
    
}
