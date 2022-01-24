package com.ipssi.rfid.readers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.RFIDHolder;

public class RFIDMaster {

    public static RFIDReaderTCPClient[] tcpReaderlist;
    public static boolean isRunning = false;
    private static RFIDReaderSerialClient desktopReader;
    private static RFIDReaderSerialClient[] serialReaderlist;
    public static int READER_ONE_COMPORT = 1;
    public static int READER_TWO_COMPORT = 2;
    private static RFIDConfig cfg = null;
    private static ConnectionStatusI connectionStatusHandler = null;
    public static RFIDConfig getConfig(){
    	return cfg ;
    }
    
    public static void init(RFIDConfig mConfig) {
        cfg = mConfig;
    }
    public static void setConnectionStatusHandler(ConnectionStatusI mConnectionStatusHandler) {
    	connectionStatusHandler = mConnectionStatusHandler;
    }

    private static void load() throws RFIDException {
        if (cfg == null) {
            throw new RFIDException("Readers not initialize");
        } else {
            RFIDReaderTCPClient reader1, reader2, reader3;
            RFIDReaderSerialClient serialReader1, serialReader2, serialReader3;
            serialReader1 = new RFIDReaderSerialClient(cfg.getReaderOneComm(),0,connectionStatusHandler);
            serialReader2 = new RFIDReaderSerialClient(cfg.getReaderTwoComm(),1,connectionStatusHandler);
            serialReader3 = new RFIDReaderSerialClient(cfg.getReaderDesktopComm(),2,connectionStatusHandler);
            reader1 = new RFIDReaderTCPClient(cfg.getReaderOneServer(), cfg.getReaderOnePort(),0,connectionStatusHandler);
            reader2 = new RFIDReaderTCPClient(cfg.getReaderTwoServer(), cfg.getReaderTwoPort(),1,connectionStatusHandler);
            reader3 = new RFIDReaderTCPClient(cfg.getReaderDesktopServer(), cfg.getReaderDesktopPort(),2,connectionStatusHandler);
            tcpReaderlist = new RFIDReaderTCPClient[3];
            serialReaderlist = new RFIDReaderSerialClient[3];
            
            tcpReaderlist[0] = reader1;
            tcpReaderlist[1] = reader2;
            tcpReaderlist[2] = reader3;
            serialReaderlist[0] = serialReader1;
            serialReaderlist[1] = serialReader2;
            serialReaderlist[2] = serialReader3;
        }
    }

    private static void loadDesktopReader() throws RFIDException {
        if (cfg == null) {
            throw new RFIDException("Readers not initialize.");
        } else if (desktopReader == null && cfg.isReaderDesktopValid()) {
            desktopReader = new RFIDReaderSerialClient(cfg.getReaderDesktopComm(),2,connectionStatusHandler);
            desktopReader.startMonitor(20*1000);
            if (desktopReader.checkConnection()) {
                
            }
            /*if (desktopReader != null && !desktopReader.checkConnection()) {
                desktopReader = null;
            }*/
        }

    }

    /*public static RFIDReaderSerialClient getDesktopReader() throws RFIDException {
        if (desktopReader == null) {
            loadDesktopReader();
        }
        return desktopReader;
    }*/
    public static ReaderI getDesktopReader() throws RFIDException {
        return getReader(2);
    }

    public static ReaderI getReader(int id) throws RFIDException {
    	ReaderI retval = null;
    	if (tcpReaderlist == null || serialReaderlist == null) {
            load();
        }
        int connectionType = id == 0 ? cfg.getReaderOneConnectionType() : id==1 ? cfg.getReaderTwoConnectionType() : cfg.getReaderDesktopConnectionType();
        if (connectionType == RFIDConfig.READER_TYPE_SERIAL) {
            retval = serialReaderlist[id]; 
        } else {
        	retval = tcpReaderlist[id];
        }
//        if(retval != null && !retval.isAlive())
//        	retval = null;
        return retval != null && !retval.isAlive() ? null : retval;
    }

    public static boolean StartRFIDReaders() throws RFIDException {
        boolean retval = false;
        if (tcpReaderlist == null || serialReaderlist == null) {
            load();
        }
        try {
            if (cfg.isReaderOneValid()) {
                if (cfg.getReaderOneConnectionType() == RFIDConfig.READER_TYPE_TCPIP) {
                    serialReaderlist[0] = null;
                    if (tcpReaderlist[0] != null) {
                        /*if (IsConnectedToReader(cfg.getReaderOneServer()))
                         {
                         if (!tcpReaderlist[0].open())
                         tcpReaderlist[0] = null;
                         }*/
                        if (!tcpReaderlist[0].open()) {
                        	//tcpReaderlist[0] = null;
                        }
                        tcpReaderlist[0].startMonitor(10*1000);
                    }
                } else {
                    tcpReaderlist[0] = null;
                    if (serialReaderlist[0] != null) {
                    	if (!serialReaderlist[0].open()) {
                            //serialReaderlist[0] = null;
                        }
                    	serialReaderlist[0].startMonitor(10*1000);
                    }
                }
            }
            if (cfg.isReaderTwoValid()) {
                if (cfg.getReaderTwoConnectionType() == RFIDConfig.READER_TYPE_TCPIP) {
                    serialReaderlist[1] = null;
                    if (tcpReaderlist[1] != null) {
                        /*if (IsConnectedToReader(cfg.getReaderTwoServer()))
                         {
                         if (!tcpReaderlist[1].open())
                         tcpReaderlist[1] = null;
                         }
                         else
                         tcpReaderlist[1] = null;*/
                        if (!tcpReaderlist[1].open()) {
                        //    tcpReaderlist[1] = null;
                        }
                        tcpReaderlist[1].startMonitor(10*1000);
                    }
                } else {
                    tcpReaderlist[1] = null;
                    if (serialReaderlist[1] != null) {
                        if (!serialReaderlist[1].open()) {
                          //  serialReaderlist[1] = null;
                        }
                        serialReaderlist[1].startMonitor(10*1000);
                    }
                }
            }
            if(cfg.isReaderDesktopValid()){
            	//getDesktopReader();
                if (cfg.getReaderDesktopConnectionType() == RFIDConfig.READER_TYPE_TCPIP) {
                    serialReaderlist[2] = null;
                    if (tcpReaderlist[2] != null) {
                        /*if (IsConnectedToReader(cfg.getReaderTwoServer()))
                         {
                         if (!tcpReaderlist[1].open())
                         tcpReaderlist[1] = null;
                         }
                         else
                         tcpReaderlist[1] = null;*/
                        if (!tcpReaderlist[2].open()) {
                        //    tcpReaderlist[1] = null;
                        }
                        tcpReaderlist[2].startMonitor(10*1000);

                    }
                } else {
                    tcpReaderlist[2] = null;
                    if (serialReaderlist[2] != null) {
                        if (!serialReaderlist[2].open()) {
                          //  serialReaderlist[1] = null;
                        }
                        serialReaderlist[2].startMonitor(10*1000);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retval;
    }

    public static void StopRFIDReaders() {
        if (tcpReaderlist != null) {
            if (tcpReaderlist[0] != null) {
                tcpReaderlist[0].close();
            }
            if (tcpReaderlist[1] != null) {
                tcpReaderlist[1].close();
            }
            if (tcpReaderlist[2] != null) {
                tcpReaderlist[2].close();
            }
        }
        if (desktopReader != null) {
            desktopReader.close();
        }
    }
    public static boolean isConnectedToReader(String readerIp) {
    	return isConnectedToServer(readerIp,1000);
    }
    public static boolean isConnectedToServer(String serverIp, int timeout) {
        boolean pingable = false;
        try {
            InetAddress inet = InetAddress.getByName(serverIp);
            pingable = inet.isReachable(timeout);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return pingable;
    }
    public static boolean ishostAvailable(String server,int port){ 
    	boolean available = true;
    	Socket s = null;
    	try {               
    		s = new Socket(server, port);   
    		s.close();
    	} 
    	catch (UnknownHostException e) { // unknown host 
    		available = false;
    		s = null;
    	} 
    	catch (IOException e) { // io exception, service probably not running 
    		available = false;
    		s = null;
    	} 
    	catch (NullPointerException e) {
    		available = false;
    		s=null;
    	}
    	return available;   
    } 
    public static void main(String[] arg) {
        try {
        	RFIDMaster.init(new RFIDConfig());
        	RFIDHolder hh1 = new RFIDHolder(23432,"jh10f0810", 1, "2016-03-18 08:30", "ch2201", "lr2201", 1, 1,true);
			RFIDHolder hh2 = new RFIDHolder(23453,"wb397751", 1, "2016-03-18 15:30", "ch2202", "lr2202", 1, 1,true);
			RFIDHolder hh3 = new RFIDHolder(23498,"jh10ag2098", 1, "2016-03-18 18:30", "ch2203", "lr2203", 1, 1,true);
			RFIDMaster.getReader(0).writeCardG2(hh1.createTag(0), 10);
			RFIDMaster.getReader(0).writeCardG2(hh2.createTag(0), 10);
			RFIDMaster.getReader(0).writeCardG2(hh3.createTag(0), 10);
        
        
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
