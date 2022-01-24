package com.ipssi.segAnalysis;

public class Junk {
	/*
}
	public static int TO_INIT = 0;
	public static int REBOOTING = 1;
	public static int CHECKING_SIMREADY = 1;
	public static int SIM_READY = 2;
	public static int CHECKING_SIMREG = 2;	
	public static int SIMREG_DONE = 3;
	public static int PREPARING_TCP_STACK = 3;
	public static int TCP_STACK_PREPARED = 3;
	public static int MAKING_CONNECTION = 4;
	public static int MADE_CONNECTION = 5;
	public static int SENDING_DATA = 6;	
	public static int SENT_DATA = 7;
	public static int WAITING_FOR_DATA = 8;
	public static int DATA_RECEIVED = 9;
	
	int waitForSerialTS = 0;
	int myState = TO_INIT;
	int checkSimAttempt = 0;
	int currTS = 0;
	byte currBuf[];
	int currBufLen = 0;
	public boolean sendData() {
		while (true) {
			
			boolean succ = openPort();		
			if (!succ) {
				myState = REBOOTING;
				reboot();
				return (addJob(me, 1000));
			}
			if (myState == REBOOTING) {
				myState = TO_INIT;
				checkSimAttempt = 0;
				waitForSerialTS = 0;
			}
			if (waitForSerialTS > 0) {
				if (!port.dataAvailable()) {
					return addJob(me, GSM_SERIAL_READ_DELTA);
				}
				else {
					currBufLen = readAfter(buf, currBufLen);
					waitForSerialTS = 0;
				}
			}
			switch (myState) {
				case TO_INIT: {
				    if (checkSimAttempt == MAX) {
				    	myState = REBOOTING;
				    	return addJob(me, 1000);
				    }
				    checkSimAttempt++;
				    myState = CHECKING_SIMREADY;
				    waitForSerialTS = GSM_SERIAL_READ_DELTA;
				    sendATCommand(...)
				    return addJob(me, waitForSerialTS);
				case CHECKING_SIMREADY:
				    if (!allDataSeen()) {
				    	waitForSerialTS = GSM_SERIAL_READ_DELTA;;
				    	return addJob(me, waitForSerialTS);
				    }
			    	if (succ) {
			    		myState = SIM_READY;
			    		//optionally return addJob(me, YIELD_MODE); .. if so then while loop not ready
			    		
			    	}
			    	else {
			    		myState = TO_INIT; 
				    	return addJob(me, 1000);
			    	}
			    	break;
				case SIM_READY: {
					myState = CHECKING_SIMREG;
					tsAtBegOfCheckingSimReg = now();
					waitForSerialTS = GSM_SERIAL_READ_DELTA;
					sendATCommand();
					return addJob(me, waitForSerialTS);
				case CHECKING_SIMREG:
					if (!allDataReceived) {
						waitForSerialTS = GSM_SERIAL_READ_DELTA;
						return addJob(me, waitForSerialTS);
					}
					if (succ) {
						myState = SIM_REG;
						//optionally return addJob(me, yieldMode);
						
					}
					else {
						if (now-tsAtBegOfCheckingSimReg > thresh) {
							if (manualRegTriedOnce) {
								myState = REBOOTING;
								reboot();
								return addJob(me, 1000); //technically if sim reg fails we are supposed to wait for 2 min,4 min ,8 min etc else network provider may decide to blacklist
							}
							else {
								myState = TRYING_MANUAL_REG_MODE;
								
								sendATCommand();
								waitForSerialTS = GSM_SERIAL_READ_DELTA;
								return addJob(me, waitForSerialTS);
							}
						}
						else {
							sendATCommand();
							waitForSerialTS = GSM_SERIAL_READ_DELTA;
							return addJob(me, waitForSerialTS);
						}
					}
				    
				    checkSimAttempt++;
				    myState = CHECKING_SIMREADY;
				    waitForSerialTS = GSM_SERIAL_READ_DELTA;
				    sendATCommand(...)
				    return addJob(me, waitForSerialTS);
				case CHECKING_SIMREADY:
				    if (!allDataSeen()) {
				    	waitForSerialTS = GSM_SERIAL_READ_DELTA;;
				    	return addJob(me, waitForSerialTS);
				    }
			    	if (succ) {
			    		myState = SIM_READY;
			    		//optionally return addJob(me, YIELD_MODE); .. if so then while loop not ready
			    		
			    	}
			    	else {
			    		myState = TO_INIT; 
				    	return addJob(me, 1000);
			    	}
			    	break;
	}
	*/
}
