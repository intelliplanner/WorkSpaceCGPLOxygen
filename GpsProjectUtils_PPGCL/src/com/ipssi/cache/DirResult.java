package com.ipssi.cache;

import com.ipssi.geometry.Point;
import com.ipssi.processor.utils.GpsData;

public class DirResult {
    private double loInDir = 0;
    private double hiInDir = 0;
    private double loOutDir = 0;
    private double hiOutDir = 0;
    private Point loAvgInOutPoint = null;
    private Point hiAvgInOutPoint = null;
    private int prevHiEntryIndex = -1;
    private int prevHiExitIndex = -1;
    private byte dirSearchTermBecauseOfThresh = 0;
    private static byte G_LO_DIR_TERM = 0x1;
    private static byte G_HI_DIR_TERM = 0x2;
    public void setLoDirTerm() {
    	dirSearchTermBecauseOfThresh = (byte)( dirSearchTermBecauseOfThresh |G_LO_DIR_TERM | G_HI_DIR_TERM); 
    }
    public void resetLoDirTerm() {
    	dirSearchTermBecauseOfThresh = (byte)( dirSearchTermBecauseOfThresh & ~G_LO_DIR_TERM); 
    }
    public void setHiDirTerm() {
    	dirSearchTermBecauseOfThresh = (byte)( dirSearchTermBecauseOfThresh | G_HI_DIR_TERM); 
    }
    public void resetAllDirTerm() {
    	dirSearchTermBecauseOfThresh = 0;
    }
    public void resetHiDirTerm() {
    	dirSearchTermBecauseOfThresh = (byte)( dirSearchTermBecauseOfThresh & ~G_HI_DIR_TERM); 
    }
    public boolean isLoDirTerm() {
    	return (dirSearchTermBecauseOfThresh & G_LO_DIR_TERM) != 0;
    }
    public boolean isHiDirTerm() {
    	return (dirSearchTermBecauseOfThresh & G_HI_DIR_TERM) != 0;
    }

	public DirResult(double loInDir, double hiInDir, double loOutDir,
			double hiOutDir, Point loAvgInOutPoint, Point hiAvgInOutPoint, int prevHiEntryIndex, int prevHiExitIndex, boolean loTermThresh, boolean hiTermThresh) {
		super();
		this.loInDir = loInDir;
		this.hiInDir = hiInDir;
		this.loOutDir = loOutDir;
		this.hiOutDir = hiOutDir;
		this.loAvgInOutPoint = loAvgInOutPoint;
		this.hiAvgInOutPoint = hiAvgInOutPoint;
		this.prevHiEntryIndex = prevHiEntryIndex;
		this.prevHiExitIndex = prevHiExitIndex;
		if (loTermThresh)
			this.setLoDirTerm();
		if (hiTermThresh)
			this.setHiDirTerm();
	}
	public double getLoInDir() {
		return loInDir;
	}
	public void setLoInDir(double loInDir) {
		this.loInDir = loInDir;
	}
	public double getHiInDir() {
		return hiInDir;
	}
	public void setHiInDir(double hiInDir) {
		this.hiInDir = hiInDir;
	}
	public double getLoOutDir() {
		return loOutDir;
	}
	public void setLoOutDir(double loOutDir) {
		this.loOutDir = loOutDir;
	}
	public double getHiOutDir() {
		return hiOutDir;
	}
	public void setHiOutDir(double hiOutDir) {
		this.hiOutDir = hiOutDir;
	}
	public Point getLoAvgInOutPoint() {
		return loAvgInOutPoint;
	}
	public void setLoAvgInOutPoint(Point loAvgInOutPoint) {
		this.loAvgInOutPoint = loAvgInOutPoint;
	}
	public Point getHiAvgInOutPoint() {
		return hiAvgInOutPoint;
	}
	public void setHiAvgInOutPoint(Point hiAvgInOutPoint) {
		this.hiAvgInOutPoint = hiAvgInOutPoint;
	}
	public int getPrevHiEntryIndex() {
		return prevHiEntryIndex;
	}
	public void setPrevHiEntryIndex(int prevHiEntryIndex) {
		this.prevHiEntryIndex = prevHiEntryIndex;
	}
	public int getPrevHiExitIndex() {
		return prevHiExitIndex;
	}
	public void setPrevHiExitIndex(int prevHiExitIndex) {
		this.prevHiExitIndex = prevHiExitIndex;
	}
}
