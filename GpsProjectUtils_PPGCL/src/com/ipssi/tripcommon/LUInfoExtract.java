package com.ipssi.tripcommon;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.tripcommon.ExtLUInfoExtract.WBInfo;

public class LUInfoExtract {//be careful adding field here .. this here is just abt rith to not waste any space
	private static final long serialVersionUID = 1L;
	private long waitIn = Misc.getUndefInt();
	private long waitOut = Misc.getUndefInt();
	private int ofOpStationId = Misc.getUndefInt();
	private int prefOrMovingOpId = Misc.getUndefInt();
	private int artOpStationId = Misc.getUndefInt();
    private boolean opComplete = false;
    private byte dirChanged = 0;
    private byte artOpStationMatch = -1;// -1 not found, 0+ deg of support
    private byte artOpStationIdWasActuallyCreated = 0; //in lusequence we do get the nearest ... but decide when we see unload, that is when this will be set to 1
    public void clear() {
    	waitIn = Misc.getUndefInt();
    	waitOut = Misc.getUndefInt();
    	opComplete = false;
        dirChanged = 0;
        ofOpStationId = Misc.getUndefInt();
        prefOrMovingOpId = Misc.getUndefInt();
        artOpStationId = Misc.getUndefInt();
        artOpStationMatch = -1;
        
    }
    public boolean equalsIntermediate(LUInfoExtract rhs) {
    	return equals(rhs);
    }
    public boolean isCalcComplete() {
    	return false;
    }
    public void specialSetArtOpStationId(long meGateIn, LUInfoExtract rhs) {
    	long rhsGateIn = rhs.getGateIn();
    	meGateIn = meGateIn/1000 * 1000;
    	rhsGateIn = rhsGateIn/1000 * 1000;
    	if (meGateIn == rhsGateIn) {
    		this.artOpStationId = rhs.artOpStationId;
    		this.artOpStationMatch = rhs.artOpStationMatch;
    		this.artOpStationIdWasActuallyCreated = rhs.artOpStationIdWasActuallyCreated;
    	}
    	else {
    		this.artOpStationId = Misc.getUndefInt();
    		this.artOpStationMatch = -1;
    		this.artOpStationIdWasActuallyCreated = (byte) 0;
    	}
    }
    public void specialSetArtOpStationId(LUInfoExtract rhs) {
    	specialSetArtOpStationId(this.getGateIn(), rhs);
    }
    
    public void copy(LUInfoExtract rhs) {
    	if (rhs != null) {
    		
    		waitIn = rhs.waitIn;
    		waitOut = rhs.waitOut;
    		opComplete = rhs.opComplete;
    		dirChanged = rhs.dirChanged;
    		ofOpStationId = rhs.ofOpStationId;
    		
    		this.prefOrMovingOpId = rhs.prefOrMovingOpId;
    		this.artOpStationId = rhs.artOpStationId;
    		this.artOpStationMatch = rhs.artOpStationMatch;
    		this.artOpStationIdWasActuallyCreated = rhs.artOpStationIdWasActuallyCreated;
    		//this.artOpStationId = waitInSame && rhs.artOpStationId < 0 ? this.artOpStationId : rhs.artOpStationId;
    		//this.artOpStationMatch = waitInSame && rhs.artOpStationId < 0 ? this.artOpStationMatch : rhs.artOpStationMatch;
    		//this.artOpStationCreated = waitInSame && rhs.artOpStationId < 0 ? this.artOpStationCreated : rhs.artOpStationCreated;
    	}
    }
    
    public String toString() {
		return "Win:"+ExtLUInfoExtract.dbgFormat(waitIn)+" OpComplete:"+opComplete+" WOut:"+ExtLUInfoExtract.dbgFormat(waitOut)+ " ofOpStationId:"+ofOpStationId+" prefOrMoving:"+this.prefOrMovingOpId+" artOpId:"+this.artOpStationId;
    }
    public boolean equals(LUInfoExtract rhs) {
    	return (
    	rhs != null &&
    	((!Misc.isUndef(waitIn) && !Misc.isUndef(rhs.waitIn) && waitIn == rhs.waitIn) || (Misc.isUndef(waitIn) && Misc.isUndef(rhs.waitIn))) &&
    	((!Misc.isUndef(waitOut) && !Misc.isUndef(rhs.waitOut) && waitOut == rhs.waitOut) || (Misc.isUndef(waitOut) && Misc.isUndef(rhs.waitOut))) &&
    	(opComplete == rhs.opComplete) &&
    	(dirChanged == rhs.dirChanged) &&
    	(ofOpStationId == rhs.ofOpStationId) &&
    	(this.prefOrMovingOpId == rhs.prefOrMovingOpId) &&
    	(this.artOpStationId == rhs.artOpStationId)
    	)
    	;    	
    }
    public String getState(){
    	if(!Misc.isUndef(waitOut)){
    		return "waitOut";
    	}
    	return "waitIn";
    }
    public long getLatestEventDateTime(){
    	if(!Misc.isUndef(waitOut))
    		return waitOut;
    	return waitIn;
    }
    public long getEarliestEventDateTime(){
    	if(!Misc.isUndef(waitIn))
    		return waitIn;
    	return waitIn;
    }

	public long getWaitIn() {
		return waitIn;
	}
	public void setWaitIn(long waitIn) {
		this.waitIn = waitIn;
	}
	public long getWaitOut() {
		return waitOut;
	}
	public void setWaitOut(long waitOut) {
		this.waitOut = waitOut;
	}
	public boolean isOpComplete() {
		return opComplete;
	}
	public void setOpComplete(boolean isOpComplete) {
		this.opComplete = isOpComplete;
	}
	public int getDirChanged() {
		return dirChanged;
	}
	public void setDirChanged(int dirChanged) {
		this.dirChanged = (byte) dirChanged;
	}
	public int getOfOpStationId() {
		return ofOpStationId;
	}
	public void setOfOpStationId(int ofOpStationId) {
		this.ofOpStationId = ofOpStationId;
	}
//from ExtLUInfoExtract
	public long getWb1In() {
		return Misc.getUndefInt();
	}
	public void setWb1In(long wb1In) {
	}
	public long getWb2In() {
		return Misc.getUndefInt();
	}
	public void setWb2In(long wb2In) {
	}
	public long getWb3In() {
		return Misc.getUndefInt();
	}
	public void setWb3In(long wb3In) {
	}
	public long getWb1Out() {
		return Misc.getUndefInt();
	}
	public void setWb1Out(long wb1Out) {
	}
	public long getWb2Out() {
		return Misc.getUndefInt();
	}
	public void setWb2Out(long wb2Out) {
	}
	public long getWb3Out() {
		return Misc.getUndefInt();
	}
	public void setWb3Out(long wb3Out) {
		
	}
	public int getWb1Id() {
		return Misc.getUndefInt();
	}
	public void setWb1Id(int wb1Id) {
	}
	public int getWb2Id() {
		return Misc.getUndefInt();
	}
	public void setWb2Id(int wb2Id) {
	}
	public int getWb3Id() {
		return Misc.getUndefInt();
	}
	public void setWb3Id(int wb3Id) {
	}
	public long getGateIn() {
		return getWaitIn();
	}
	public void setGateIn(long gateIn) {
	}
	public long getGateOut() {
		return getWaitOut();
	}
	public void setGateOut(long gateOut) {
	}
	public long getAreaIn() {
		return Misc.getUndefInt();
	}
	public void setAreaIn(long areaIn) {
	}
	public long getAreaOut() {
		return Misc.getUndefInt();
	}
	public void setAreaOut(long areaOut) {
	}
	public int getAreaId() {
		return Misc.getUndefInt();
	}
	public void setAreaId(int areaId) {
	}
	public int getTripId() {
		return Misc.getUndefInt();
	}
	public void setTripId(int tripId) {
	}
	public int getMaterialId() {
		return Misc.getUndefInt();
	}
	public void setMaterialId(int materialId) {
	}
	public ArrayList<Integer> getAlternateMaterialList() {
		return null;
	}
	public void setAlternateMaterialList(ArrayList<Integer>alternateMaterialList) {
	}
	public int getPrefOrMovingOpId() {
		return prefOrMovingOpId;
	}
	public int getPrefOrMovingOpIdExt() {
		return Misc.isUndef(prefOrMovingOpId) ? this.ofOpStationId : this.prefOrMovingOpId;
	}
	public void setPrefOrMovingOpId(int prefOrMovingOpId) {
		this.prefOrMovingOpId = prefOrMovingOpId;
	}
	public int getArtOpStationId() {
		return artOpStationId;
	}
	public void setArtOpStationId(int artOpStationId) {
		this.artOpStationId = artOpStationId;
	}
	public byte getArtOpStationMatch() {
		return artOpStationMatch;
	}
	public void setArtOpStationMatch(byte artOpStationMatch) {
		this.artOpStationMatch = artOpStationMatch;
	}
	public byte getArtOpStationIdWasActuallyCreated() {
		return artOpStationIdWasActuallyCreated;
	}
	public void setArtOpStationIdWasActuallyCreated(
			byte artOpStationIdWasActuallyCreated) {
		this.artOpStationIdWasActuallyCreated = artOpStationIdWasActuallyCreated;
	}
	
	
}
