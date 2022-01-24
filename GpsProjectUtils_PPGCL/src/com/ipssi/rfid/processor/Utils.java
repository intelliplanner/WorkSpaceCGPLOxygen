package com.ipssi.rfid.processor;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ipssi.gen.utils.Misc;

public class Utils {

    public static final int PRESET_VALUE = 0xFFFF;
    public static final int POLYNOMIAL = 0x8408;
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static boolean readSyncThreadStarted = false;
    public static boolean writeSyncThreadStarted = false;
    public static final byte MEM_PASSWORD = 0x00;
    public static final byte MEM_EPC = 0x01;
    public static final byte MEM_TID = 0x02;
    public static final byte MEM_USER = 0x03;
    public static final byte MASK_ENABLE = 0x00;
    public static final byte MASK_DISABLE = 0x01;
    public static final byte INVENTORY_TID = 1;
    public static final byte INVENTORY_EPC = 0;
    public static final byte KILL_PASSWORD_PROTECTION_SETTING = 0x00;
    public static final byte ACCESS_PASSWORD_PROTECTION_SETTING = 0x01;
    public static final byte EPC_MEMORY_PROTECTION_SETTING = 0x02;
    public static final byte TID_MEMORY_PROTECTION_SETTING = 0x03;
    public static final byte USER_MEMORY_PROTECTION_SETTING = 0x04;
    public static final byte TAG_PROTECTED = 0x00;
    public static final byte TAG_UNPROTECTED = 0x01;
    //workflow states
    public static final int WORKSTATE_NOT_INT = 0;
    //public static int AUTO_ALLOW = 1;
    public static final int BLACKLISTED = 2;
    public static final int GPS_NOT_WORKING = 3;
    public static final int INCOMPLETE_WORKSTATE = 4;
    public static final int NO_WB_INFO = 5;
    public static final int GPS_VOILATION = 6;
    public static final int WEIGTH_DIFF = 7;
    public static final int INCOMPLETE_TRIP = 8;
    public static final int INCORRECT_WEIGHT = 9;
    //DB Operation
    public static final int INSERT = 0;
    public static final int UPDATE = 1;
    public static final int DELETE = 2;
    public static final int SELECT = 3;
    //DATE Formats
    public static final String MYSQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    //Data Type
    public static final int DATA_INT = 0;
    public static final int DATA_DOUBLE = 1;
    public static final int DATA_STRING = 2;
    public static final int DATA_DATE = 3;
    //System Variables;
    public static final int SYSTEM_TYPE_GATE = 0;
    public static final int SYSTEM_TYPE_WEIGH_BRIDGE = 1;
    public static final int SYSTEM_TYPE_WEIGH_BRIDGE_SERIAL = 2;
    public static final int SYSTEM_READER_GATE_ENTRY = 0;
    public static final int SYSTEM_READER_GATE_EXIT = 1;
    public static final int SYSTEM_READER_WEIGH_BRIDGE = 2;
    public static final int SYSTEM_READER_GATE_CCL = 3;
    public static final int SYSTEM_REGION_LOAD = 0;
    public static final int SYSTEM_REGION_UNLOAD = 1;
    //Task ID'S
    public static final int TASK_CREATE_CHECKLIST = 0;
    public static final int TASK_CREATE_VEHICLE = 1;
    public static final int TASK_INSERT_VEHICLE = 2;
    public static final int TASK_ASSIGN_TOKEN = 3;
    public static final int TASK_UPDATE_VEHICLE = 4;
    //vehicle parameters
    public static final int MAT_TYPE_COAL = 1;
    public static final int MAT_TYPE_NON_COAL = 0;
    public static final int ACCESS_TYPE_CONTROLLED = 1;
    public static final int ACCESS_TYPE_NON_CONTROLLED = 0;
    public static final int CARD_TYPE_PRIVILAGE = 2;
    public static final int CARD_TYPE_PERMANENT = 1;
    public static final int CARD_TYPE_TEMPORARY = 0;
    //System connection type
    public static final int SYSTEM_READER_CONN_TYPE_SERIAL = 0;
    public static final int SYSTEM_READER_CONN_TYPE_TCPIP = 1;
    //udef value
    //rajeev 20141211 - possible changes start
    public static final int UNDEF_INT = -1111111;
    public static final int UNDEF_VALUE = -1111111;//-1999999999;
	  public static final int UNDEF_SHORT_VALUE = -11111;
	  public static final int UNDEF_BYTE_VALUE = -1;
	  public static final double UNDEF_FLOAT_VALUE = -1e12f;//UNDEF_VALUE * 100000;
	  public static final double UNDEF_FLOAT_VALUE_CMP = -1e11f;//UNDEF_VALUE * 100000;
    public static final long UNDEF_LONG = UNDEF_INT;
    //card status
    public static final int ISSUE_CARD = 1;
    public static final int RETURN_CARD = 0;
    //vehicle Request
    public static final int VEHICLE_REQUEST_CREATE = 0;
    public static final int VEHICLE_REQUEST_INPROCESS = 1;
    public static final int VEHICLE_REQUEST_COMPLETE = 2;
    public static final int VEHICLE_REQUEST_DESTROY = 3;
    //Exception Action
    public static final int ACTION_ALLOW = 0;
    public static final int ACTION_DONTALLOW = 1;
    //Exception Action
    public static final int SYSTEM_OPERATING_MANUAL = 1;
    public static final int SYSTEM_OPERATING_AUTO = 0;
    //waveform
    public static final String WAVEFORM_BLACKLISTED = "blacklisted.wav";
    public static final String WAVEFORM_WEIGHT_NOT_CENTERED = "WB_Not_Centered.wav";
    public static final String WAVEFORM_WEIGHT_DIFF = "Gross_weight_difference.wav";
    public static final String WAVEFORM_RETURN_TAG = "Mines_Gate_Tag_Return.wav";
    public static final String WAVEFORM_NO_WB = "Gross_weight_not_taken.wav";
    public static final String WAVEFORM_PRIVIOUS_GPS_TRIP_NOT_COMPLETED = "Trip_not_Completed.wav";
    public static final String WAVEFORM_GPS_VOILATION = "WB_GPS_Error.wav";
    public static final String WAVEFORM_GPS_NOT_WORKING = "GPS_not_working.wav";
    public static final String WAVEFORM_DIFF_WB = "";
    //query
    public static final int REPORT_QUERY = 0;
    public static final int VEHICLE_QUERY = 1;
    //commands
    public static final String RFID_GET_INVENTORY = "GET";
    public static final String RFID_SET_INVENTORY = "SET";
    public static final String RFID_ERASE_INVENTORY = "ERASE";
    //RFID Commands
    public static final byte COMMAND_INVENTORY = 0x01;
    public static final byte COMMAND_READ_DATA = 0x02;
    public static final byte COMMAND_WRITE_DATA = 0x03;
    public static final byte COMMAND_WRITE_EPC = 0x04;
    public static final byte COMMAND_KILL_TAG = 0x05;
    public static final byte COMMAND_LOCK = 0x06;
    public static final byte COMMAND_BLOCK_ERASE = 0x07;
    public static final byte COMMAND_READ_PROTECT = 0x08;
    public static final byte COMMAND_READ_PROTECT_WITHOUT_EPC = 0x09;
    public static final byte COMMAND_RESET_READ_PROTECT = 0x0A;
    public static final byte COMMAND_CHECK_READ_PROTECT = 0x0B;
    public static final byte COMMAND_EAS_ALARM = 0x0C;
    public static final byte COMMAND_CHECK_EAS_ALARM = 0x0D;
    public static final byte COMMAND_BLOCK_LOCK = 0x0E;
    public static final byte COMMAND_INVENTORY_SINGLE = 0x0F;
    public static final byte COMMAND_BLOCK_WRITE = 0x10;
    //vehicle request related
    public static final int REQUEST_STATUS_INPROCESS = 0;
    public static final int REQUEST_STATUS_AllOW = 1;
    public static final int REQUEST_STATUS_DONTALLOW = 2;
    public static final int REQUEST_STATUS_CARD_NOT_READY = 3;
    public static final int REQUEST_STATUS_CLEAR = 4;
    public static final int REQUEST_STATUS_INSPECTION = 5;
    //Barrier Type
    public static final int BARRIER_TYPE_SINGLE = 0;
    public static final int BARRIER_TYPE_DOUBLE = 1;
    //same is vehicle for same token
    public static final int SAME_TOKEN_THRESHOLD = 20;//minutes
    //SYSTEM Work mode
    public static final int SYSTEM_WORK_MODE_NORMAL = 0;
    public static final int SYSTEM_WORK_MODE_STRICT = 1;
    //Sytem Reader Driver Type
    public static final int SYSTEM_READER_DRIVER_NORMAL = 0;
    public static final int SYSTEM_READER_DRIVER_UHFREADER18 = 1;
    //Encoding 6 types
    public static final int STRING8 = 8;
    public static final int STRING6 = 6;
    public static final int STRING4 = 4;
    //Signal color coding
    public static final int YELLOW = 0;
    public static final int GREEN = 1;
    public static final int RED = 2;
    public static final int RESET = 3;
    //reader connection type
    public static final int READER_CONNECTION_TYPE_TCPIP = 0;
    public static final int READER_CONNECTION_TYPE_SERIAL = 1;
    public static String G_DEFAULT_DATE_FORMAT = "yyyy-MM-dd"; //ALSO BE SURE TO SET VARIABLE IN PROFILE.JS and C++
    public static String G_DEFAULT_DATE_FORMAT_HHMM = G_DEFAULT_DATE_FORMAT + " HH:mm";
    public static SimpleDateFormat defaultDateFormater = new SimpleDateFormat(G_DEFAULT_DATE_FORMAT);
    public static long refDateLong = 1420050600000l;//defaultDateFormater.parse("2015-01-01").getTime();
    private static final Pattern ipv4Pattern = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b");
    
    static {
        try {
        	if(Misc.isUndef(refDateLong))
        		refDateLong = defaultDateFormater.parse("2015-01-01").getTime();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isNull(int val) {
        return (val == UNDEF_INT);
    }

    public static boolean isNull(float val) {
        return val <= UNDEF_FLOAT_VALUE_CMP ;
    }

    public static boolean isNull(double val) {
        return val <= UNDEF_FLOAT_VALUE_CMP;
    }

    public static boolean isNull(String val) {
        return (val == null || val.length() <= 0);
    }
     
    public static boolean validateEmailAddress(String email){
        Pattern emailNamePtrn = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher mtch = emailNamePtrn.matcher(email);
        if(mtch.matches()){
            return true;
        }
        return false;
    }
     
    public static boolean isNumericDigit(String number) {
     String expression = "-?\\d+(\\.\\d+)?";  //match a number with optional '-' and decimal.
     
        boolean isValid = false;
       // String expression = "\"-?\\\\d+(\\\\.\\\\d+)?\"";
        CharSequence inputStr = number;
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    
    
    }

    public static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String ByteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String GetReturnCodeDesc(int cmdRet) //get return code description
    {
        switch (cmdRet) {
            case 0x00:
                return "Operation Successed";
            case 0x01:
                return "Return before Inventory finished";
            case 0x02:
                return "the Inventory-scan-time overflow";
            case 0x03:
                return "More Data";
            case 0x04:
                return "Reader module MCU is Full";
            case 0x05:
                return "Access Password Error";
            case 0x09:
                return "Destroy Password Error";
            case 0x0a:
                return "Destroy Password Error Cannot be Zero";
            case 0x0b:
                return "Tag Not Support the command";
            case 0x0c:
                return "Use the commmand,Access Password Cannot be Zero";
            case 0x0d:
                return "Tag is protected,cannot set it again";
            case 0x0e:
                return "Tag is unprotected,no need to reset it";
            case 0x10:
                return "There is some locked bytes,write fail";
            case 0x11:
                return "can not lock it";
            case 0x12:
                return "is locked,cannot lock it again";
            case 0x13:
                return "Parameter Save Fail,Can Use Before Power";
            case 0x14:
                return "Cannot adjust";
            case 0x15:
                return "Return before Inventory finished";
            case 0x16:
                return "Inventory-Scan-Time overflow";
            case 0x17:
                return "More Data";
            case 0x18:
                return "Reader module MCU is full";
            case 0x19:
                return "Not Support Command Or AccessPassword Cannot be Zero";
            case 0xFA:
                return "Get Tag,Poor Communication,Inoperable";
            case 0xFB:
                return "No Tag Operable";
            case 0xFC:
                return "Tag Return ErrorCode";
            case 0xFD:
                return "Command length wrong";
            case 0xFE:
                return "Illegal command";
            case 0xFF:
                return "Parameter Error";
            case 0x30:
                return "Communication error";
            case 0x31:
                return "CRC checksummat error";
            case 0x32:
                return "Return data length error";
            case 0x33:
                return "Communication busy";
            case 0x34:
                return "Busy,command is being executed";
            case 0x35:
                return "ComPort Opened";
            case 0x36:
                return "ComPort Closed";
            case 0x37:
                return "Invalid Handle";
            case 0x38:
                return "Invalid Port";
            case 0xEE:
                return "Return command error";
            default:
                return "";
        }
    }

    public static String GetErrorCodeDesc(int cmdRet) //get error code description
    {
        switch (cmdRet) {
            case 0x00:
                return "Other error";
            case 0x03:
                return "Memory out or pc not support";
            case 0x04:
                return "Memory Locked and unwritable";
            case 0x0b:
                return "No Power,memory write operation cannot be executed";
            case 0x0f:
                return "Not Special Error,tag not support special errorcode";
            default:
                return "";
        }
    }

    public static byte[] intToByte(int value) {
        ByteBuffer _intShifter = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder());
        _intShifter.clear();
        _intShifter.putInt(value);
        return _intShifter.array();
    }

    public static int byteToInt(byte[] bi) {
        return bi[3] & 0xFF | (bi[2] & 0xFF) << 8
                | (bi[1] & 0xFF) << 16 | (bi[0] & 0xFF) << 24;
    }
    /*public static byte[] intToByte(int i) {
     return new byte[] { (byte) (i >>> 24), (byte) ((i << 8) >>> 24),
     (byte) ((i << 16) >>> 24), (byte) ((i << 24) >>> 24)
     };
     }*/

    public static long uiCrc16Cal(byte[] pucY, int ucX) {
        int ucI, ucJ;
        long uiCrcValue = PRESET_VALUE;
        int b = 0;
        for (ucI = 0; ucI < ucX; ucI++) {
            b = byteToInt(pucY[ucI]);
            //LoggerNew.Write("Input["+pucY[ucI]+"]:"+Long.toBinaryString(b));
            uiCrcValue = uiCrcValue ^ b;
            //LoggerNew.Write("uiCrcValue:"+Long.toBinaryString(uiCrcValue));
            for (ucJ = 0; ucJ < 8; ucJ++) {
                if ((uiCrcValue & 0x0001) != 0) {
                    uiCrcValue = (uiCrcValue >> 1) ^ POLYNOMIAL;
                } else {
                    uiCrcValue = (uiCrcValue >> 1);
                }
                //LoggerNew.Write("uiCrcValue["+ucI+"]["+ucJ+"]:"+Long.toBinaryString(uiCrcValue));
            }
        }
        //LoggerNew.Write("uiCrcValue:"+Long.toBinaryString(uiCrcValue));
        return uiCrcValue;
    }

    public static String getBinaryStrFromByteArray(byte[] data) {
        String retval = null;
        if (data != null && data.length > 0) {
            for (byte b : data) {
                if (retval == null) {
                    retval = new String();
                }
                retval += (Integer.toBinaryString(b & 255 | 256).substring(1));
            }
        }
        return retval;
    }

    public static byte[] longToByte(long value) {
        ByteBuffer _intShifter = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder());
        _intShifter.clear();
        _intShifter.putLong(value);
        return _intShifter.array();
    }

    public static int byteToInt(byte b) {
        return (b & 0xff);
    }

    public static String EncodingAddPadding(String str, int type) {
        int count = 0;
        if (str != null) {
            if (type < str.length()) {
                str = "";
            }
            count = type - str.length();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    str = "0" + str;
                }
            }
        }
        return str;
    }

    public static String Encoding6StringToBinary(String str) {
        return EncodingStringToBinary(str, STRING6);
    }

    public static String Encoding4StringToBinary(String str) {
        return EncodingStringToBinary(str, STRING4);
    }

    public static String EncodingSpecialToBinary(String str) {
        return EncodingStringToBinary(str, 2);
    }

    private static String EncodingStringToBinary(String str, int type) {
        int index = 0;
        String retval = null;
        if (!isNull(str)) {
            for (byte b : str.getBytes()) {
                if (b == 35) {
                    index = 0;
                } else if (b == 64) {
                    index = 1;
                } else if (b == 95) {
                    index = 2;
                } else if (b >= 48 && b <= 57) {
                    index = b - 45;
                } else if (b >= 65 && b <= 90) {
                    index = b - 52;
                } else if (b >= 97 && b <= 122) {
                    index = b - 84;
                }
                if (retval == null) {
                    retval = EncodingAddPadding(Integer.toBinaryString(index), type);
                } else {
                    retval += EncodingAddPadding(Integer.toBinaryString(index), type);
                }
            }
        } else {
            retval = EncodingAddPadding("", type);
        }
        return retval;
    }

    public static String EncodingIntToBinary(int val, int type) {
        String retval = null;
        if (!isNull(val)) {
            retval = EncodingAddPadding(Integer.toBinaryString(val), type);
        } else {
            retval = EncodingAddPadding("", type);
        }
        return retval;
    }

    /**
     * test
     *
     * @param binary string
     * @param integer
     * @return integer
     */
    public static int DecodingBinaryToInt(String binaryStr, int type) {

        int retval = UNDEF_INT;

        if (!isNull(binaryStr)) {
            if (binaryStr.length() < type) {
                binaryStr = EncodingAddPadding(binaryStr, type);
            }
            retval = Integer.parseInt(binaryStr, 2);
        }
        return retval;
    }

    public static String Decoding6BinaryToString(String binaryStr) {
        return DecodingBinaryToString(binaryStr, STRING6);
    }

    public static String Decoding4BinaryToString(String binaryStr) {
        return DecodingBinaryToString(binaryStr, STRING4);
    }

    public static String DecodingBinaryToSpecial(String binaryStr) {
        return DecodingBinaryToString(binaryStr, 2);
    }

    private static String DecodingBinaryToString(String binaryStr, int type) {
        int index = 0;
        String retval = null;
        String[] charArray = null;
        int size = 0;

        if (!isNull(binaryStr)) {
            if (binaryStr.length() < type) {
                binaryStr = EncodingAddPadding(binaryStr, type);
            }
            charArray = StringSplit(binaryStr, type);
            if (charArray != null && charArray.length > 0) {
                size = charArray.length;
                for (int i = 0; i < size; i++) {
                    int b = byteToInt(getByteFromBiary(EncodingAddPadding(charArray[i], STRING8)));
                    if (b == 0) {
                        index = 35;
                    } else if (b == 1) {
                        index = 64;
                    } else if (b == 2) {
                        index = 95;
                    } else if (b >= 3 && b <= 12) {
                        index = b + 45;
                    } else if (b >= 13 && b <= 38) {
                        index = b + 52;
                    }
                    if (retval == null) {
                        retval = "" + (char) (index);
                    } else {
                        retval += (char) (index);
                    }
                }
            }
        }
        return retval;
    }

    public static String[] StringSplit(String str, int size) {
        String[] retval = null;
        int count = 0;
        if (!isNull(str)) {
            count = (str.length() / size);
            if (count > 0) {
                retval = new String[count];
                for (int i = 0; i < count; i++) {
                    retval[i] = str.substring(i * size, ((i + 1) * size));
                }
            }
        }
        return retval;
    }

    public static byte[] GetBytesFromBinaryString(String binary) {

        if (isNull(binary) || (binary.length() % 8 != 0)) {
            return null;
        }
        int count = binary.length() / 8;
        byte[] retval = new byte[count];
        for (int i = 0; i < count; i++) {
            String t = binary.substring(i * 8, ((i + 1) * 8));
            retval[i] = getByteFromBiary(t);
        }
        String check = getBinaryStrFromByteArray(retval);
        return retval;
    }

    public static String standardizeName(String name) {//for vehicle like 0up80ac5566
        if (name == null || name.length() <= 0) {
            return null;
        }
        if (!name.matches("-?\\d+(\\.\\d+)?")) {
            while (!Character.isLetter(name.charAt(0))) {
                name = name.substring(1);
            }
        }
        return (name == null ? null : name.replaceAll("[^A-Za-z0-9_]", "").toUpperCase());
    }
    public static String standardizeNameAN(String name) {//for vehicle like 0up80ac5566
        if (name == null || name.length() <= 0) {
            return "";
        }
        if (!name.matches("-?\\d+(\\.\\d+)?")) {
            while (!Character.isLetter(name.charAt(0))) {
                name = name.substring(1);
            }
        }
        return (name == null ? null : name.replaceAll("[^A-Za-z0-9]", "").toUpperCase());
    }

    public static String getStdString(String val, int noOfChar) {
        return getStdString(val, noOfChar, "@");
    }

    public static String getStdString(String val, int noOfChar, String chr) {
        String retval = null;

        if (val == null) {
            val = "";
        }
        retval = "";
        val = val.trim();
        int dataCount = val.length() <= noOfChar ? val.length() : noOfChar;
        for (int i = 0; i < dataCount; i++) {
            if (Character.isLetterOrDigit(val.charAt(i))) {
                retval += Character.toUpperCase(val.charAt(i));
            }
        }
        int count = retval.length();
        if (retval.length() < noOfChar) {
            for (int j = 0; j < (noOfChar - count); j++) {
                if (chr.equalsIgnoreCase("0")) {
                    retval = chr + retval;
                } else {
                    retval += chr;
                }
            }
        }
        return retval;
    }

    public static byte getByteFromBiary(String binary) {
        if (isNull(binary)) {
            return (byte) 0;
        }
        int i = Integer.parseInt(binary, 2);
        if (i > 127) {
            i = i - 256;
        }
        return (byte) i;
    }

    public static int getDateTimeLong(Date dateTime) {
        if (dateTime == null) {
            return 0;
        }
        return (int) ((dateTime.getTime() - refDateLong) / 1000);
    }

    public static Date getDateTime(long ticks) {
        if (ticks <= 0) {
            return null;
        }
        return (new Date(ticks * 1000 + refDateLong));
    }

    public static String getDate(String strDate, DateFormat inFormat, DateFormat outFormat) {
        Date date = null;
        String myDate = null;
        if (strDate.length() > 0 && strDate != null) {
            try {
                date = inFormat.parse(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date != null) {
                myDate = outFormat.format(date);
            }
        } else {
            myDate = null;
        }
        return myDate;
    }

    public static boolean isPhoneNumberValidate(String phoneNo) {
        //validate phone numbers of format "1234567890"
        boolean isPhoneNumber = false;
        if (phoneNo.matches("\\d{10}")) {
            isPhoneNumber = true;
        }
        return isPhoneNumber;
    }

    public static boolean isAadharNumbervalidate(String aadharNumber) {
        Pattern aadharPattern = Pattern.compile("\\d{12}");
        boolean isValidAadhar = aadharPattern.matcher(aadharNumber).matches();
        if (isValidAadhar) {
            isValidAadhar = VerhoeffAlgorithm.validateVerhoeff(aadharNumber);
        }
        return isValidAadhar;
    }

    public static boolean isNumeric(String number) {
        boolean isValid = false;

        /*Number: A numeric value will have following format: 
         ^[-+]?: Starts with an optional "+" or "-" sign. 
         [0-9]*: May have one or more digits. 
         \\.? : May contain an optional "." (decimal point) character. 
         [0-9]+$ : ends with numeric digit. 
         */

//Initialize reg ex for numeric data.   
        String expression = "^[-+]?[0-9]*\\.?[0-9]+$";
        CharSequence inputStr = number;
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
    
    public static int getBigDate(Date firstDate,Date secondDate){
        int val = Misc.getUndefInt();
        if(firstDate == null || secondDate == null){
                val = 0;
        }
        if(getDateTimeLong(firstDate) > getDateTimeLong(secondDate)){
            val = 1;
        }else{
            val = 2;
        }
      
        return val;
    }
    public static void copy(Object fromObj, Object toObj){
    	if(fromObj == null || toObj == null)
    		return;
    	try{
    		Class<?> base = fromObj.getClass();
    		Class<?> baseto = toObj.getClass();
    		if(!base.isAssignableFrom(baseto))
    			return;
    		if (base.isAssignableFrom(Integer.TYPE)
					||
					base.isAssignableFrom(String.class)
					||
					base.isAssignableFrom(Date.class)
					||
					base.isAssignableFrom(Long.TYPE)
					||
					base.isAssignableFrom(Float.TYPE)
					||
					base.isAssignableFrom(Double.TYPE)
					||
					base.isAssignableFrom(Boolean.TYPE)){
					fromObj = toObj;
					return;
			}
    		for(Field field : base.getDeclaredFields()){
    			field.setAccessible(true);
    			field.set(toObj, field.get(fromObj));
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    public static boolean isIpv4(String str){
    	Matcher m = ipv4Pattern.matcher(str);
    	return m.find();
    }
    public static boolean isMyIp(String myIp){
        if(isNull(myIp))
        	return false;
    	try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if(myIp.equalsIgnoreCase(addr.getHostAddress()))
                    	return true;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static String getConnectionParams(Connection conn){
    	if(conn == null || !(conn instanceof com.mysql.jdbc.Connection))
    		return null;
    	com.mysql.jdbc.Connection myConn = (com.mysql.jdbc.Connection) conn;
    	try {
			return myConn.getCatalog();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
}
