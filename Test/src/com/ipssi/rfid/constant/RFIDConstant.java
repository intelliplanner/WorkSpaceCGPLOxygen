package com.ipssi.rfid.constant;

import java.util.Properties;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.readers.RFIDConfig;
import com.ipssi.rfid.readers.RFIDException;
import com.ipssi.rfid.readers.RFIDMaster;
import com.ipssi.rfid.ui.secl.controller.PropertyManager;
import com.ipssi.rfid.ui.secl.controller.PropertyManager.PropertyType;

public class RFIDConstant {
    public static void setReaderConfiguration() {
        try {
        	Properties prop = PropertyManager.getProperty(PropertyType.RfidReader);
        	RFIDConfig cfg = new RFIDConfig();
            
            cfg.setReaderOneComm(Misc.getParamAsString(prop.getProperty("READER_ONE_COM"), "COM1"));
            cfg.setReaderOnePort(Misc.getParamAsInt(prop.getProperty("READER_ONE_TCP_PORT"), 6000));
            cfg.setReaderOneServer(Misc.getParamAsString(prop.getProperty("READER_ONE_TCP_IP"), "192.168.1.190"));
            cfg.setReaderOneConnectionType(Misc.getParamAsInt(prop.getProperty("READER_ONE_CONN_TYPE"), RFIDConfig.READER_TYPE_TCPIP));
            cfg.setReaderTwoComm(Misc.getParamAsString(prop.getProperty("READER_TWO_COM"), "COM2"));
            cfg.setReaderTwoPort(Misc.getParamAsInt(prop.getProperty("READER_TWO_TCP_PORT"), 6001));
            cfg.setReaderTwoServer(Misc.getParamAsString(prop.getProperty("READER_TWO_TCP_IP"), "192.168.1.191"));
            cfg.setReaderTwoConnectionType(Misc.getParamAsInt(prop.getProperty("READER_TWO_CONN_TYPE"), RFIDConfig.READER_TYPE_TCPIP));
            cfg.setReaderOneValid(Misc.getParamAsInt(prop.getProperty("READER_ONE_PRESENT")) == 1);
            cfg.setReaderTwoValid(Misc.getParamAsInt(prop.getProperty("READER_TWO_PRESENT")) == 1);
            cfg.setReaderDesktopValid(Misc.getParamAsInt(prop.getProperty("READER_DESKTOP_PRESENT")) == 1);
            cfg.setReaderDesktopConnectionType(Misc.getParamAsInt(prop.getProperty("READER_DESKTOP_CONN_TYPE")));
            cfg.setReaderDesktopComm(Misc.getParamAsString(prop.getProperty("READER_DESKTOP_COM"), "COM10"));
            cfg.setReaderDesktopPort(Misc.getParamAsInt(prop.getProperty("READER_DESKTOP_TCP_PORT"), 6002));
            cfg.setReaderDesktopServer(Misc.getParamAsString(prop.getProperty("READER_DESKTOP_TCP_IP"), "192.168.1.192"));
            RFIDMaster.init(cfg);
            RFIDMaster.StartRFIDReaders();
        } catch (RFIDException ex) {
            ex.printStackTrace();
        }
    }
}
