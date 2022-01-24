package com.ipssi.processor.utils;

import static com.ipssi.gen.utils.Common.isNull;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

import java.sql.ResultSet;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.geometry.Point;
import static com.ipssi.processor.utils.ChannelTypeEnum.*;
import com.ipssi.geometry.Point;
import com.ipssi.map.utils.ApplicationConstants;
import com.ipssi.mapguideutils.NameLocationLookUp;
import com.ipssi.modeler.ModelSpec;
import com.ipssi.modeler.ModelState;

public class GpsData implements Serializable, Comparable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	//private transient Point point;
	private static final double mergeDistThresh =0.012;//0.021;// 0.012;
	private static final double mergeSpeedThresh =1.90;//0.021;// 0.012;
	
	private static final double mergeValThresh = 0.0001;
	
	private double x = Misc.getUndefDouble(); 
	private double y = Misc.getUndefDouble();
	public static double g_degToRadFactor = Math.PI/180.0;
	private static final String specialZeroCoordinatesIndicator = "#%#% ";
	
	public String toSqlString() {
		return ("Point(" + this.x + " " + this.y + ")");
	}
	
	public Point getPoint() {
		return new Point(x,y);
	}
	
	public void setPoint(double x ,double y) {
		this.x = x;
		this.y = y;
	}
	
	public void setPoint(Point x) {
		if (x != null) {
			this.x = x.getX();
			this.y = x.getY();;
		}
	}
	public void add(double rhsX, double rhsY) {
		x += rhsX;
		y += rhsY;
	}
	
//	public Point toCartesian(){ //TODO check longitude is x, latitude is y
//		Point p = new Point();
//		p.setX(ApplicationConstants.RADIUS * (Math.sin(Math.toRadians(x))) * (Math.cos(Math.toRadians(y))));
//		p.setY(ApplicationConstants.RADIUS * (Math.sin(Math.toRadians(x))) * (Math.sin(Math.toRadians(y))));
//		return p;
//	}

	public double squaredDistance(double rhsX, double rhsY) {
		double val = Math.pow(this.x - rhsX, 2) +  Math.pow(this.y - rhsY, 2); 
		return val;
	}
	
	public double distance(double rhsX, double rhsY) {
	    //return Math.sqrt(squaredDistance(endPoint));
		return fastGeoDistance(rhsX, rhsY);
	}
	public double getLongitude() {
		return x;
	}
	
	public double getLatitude() {
		return y;
	}
	public double fastGeoDistance(GpsData rhs) {
		return fastGeoDistance(rhs.getLongitude(), rhs.getLatitude());
	}
	
 	public double fastGeoDistance(double rhsX, double rhsY) {		  
	      double horizCircleFactor = Math.cos((getLatitude()+rhsY)/2*g_degToRadFactor);
	      double deltaLon = (rhsX-getLongitude())*g_degToRadFactor*horizCircleFactor;
	      double deltaLat = (rhsY-getLatitude())*g_degToRadFactor;
	      double dist = ApplicationConstants.RADIUS*Math.sqrt(deltaLon*deltaLon+deltaLat*deltaLat);
	      return dist;
		
	}
	
//	public boolean equals(Point rhs) {
//		return rhs != null && Misc.isEqual(y, rhs.getY()) && Misc.isEqual(x, rhs.getX());
//	}
	
	
	private long gps_Record_Time;
	private long gpsRecvTime;
	private int dimId = 0;
	private double value;
	//private ChannelTypeEnum sourceChannel; //instead veing masked
	//private String name;
	private int gpsRecordingId = Misc.getUndefInt();
	private double speed = Misc.getUndefDouble();
	private double orientation = Misc.getUndefDouble();
	private transient  ModelState modelState = null;
	private transient String strData = null;
//	private transient int minGpsRecordTimeDeltaSec = 0;//one of these needs to be non-zero
//	private transient int minGpsReceiveTimeDeltaSec = 0;//one of these needsto be non-zero
	
	//WIP not sure needed private long multiGpsRecTime = 0; //see handling notes at end
	//WIP not sure needed private long multiGpsRecvTime =0; //see handling notes at end;
	//private boolean zeroCoord = false; //being masked
	//private boolean isUZName = false; //being masked
	private short stateMask = 0; //BACKWARD COMPATABILITY ISSUES ... speciallist, LUItem
	//0   1   2   3         4       5       6       7
	//                FW     UZ    ZC    DC    CC   //FW has forward data
	private static final short MASK_CC = 0x1;
	private static final short MASK_DC = 0x2;
	private static final short MASK_ALLCC = 0x3;
	private static final short MASK_ZC = 0x4;
	private static final short MASK_UZ = 0x8;
	private static final short MASK_FW = 0x10;
	private static final short MASK_RPDONE = 0x20;
	private static final short MASK_TPDONE = 0x40;
	//private static final short MASK_LEFTUPDATED = 0x80;
	//private static final short MASK_RIGHTUPDATED = 0x80;
	
	private short ifUZDistTimes10 = 0;
	
	public GpsData() {
	}

	/**
	 * 
	 * @param gpsData
	 */
	public GpsData(GpsData gpsData) {
//		this.point = gpsData.getPoint();
		this.gps_Record_Time = gpsData.getGps_Record_Time(); //because date might be manipulated
		this.dimId = gpsData.getDimId();
		this.value = gpsData.getValue();
		setSourceChannel(gpsData.getSourceChannel());
		//this.name = gpsData.name;
		this.gpsRecvTime = gpsData.getGpsRecvTime();
		this.speed = gpsData.speed;
		this.x = gpsData.x;
		this.orientation = gpsData.orientation;
		this.y = gpsData.y;
		setZeroCoord(gpsData.isZeroCoord());
		setUZName(gpsData.isUZName());
		setFWPoint(gpsData.isFWPoint());
		this.ifUZDistTimes10 = gpsData.ifUZDistTimes10;
	}
	
	public GpsData copy(GpsData gpsData) {
		this.gps_Record_Time = gpsData.getGps_Record_Time(); //because date might be manipulated
		this.dimId = gpsData.getDimId();
		this.value = gpsData.getValue();
		setSourceChannel(gpsData.getSourceChannel());
		//this.name = gpsData.name;
		this.gpsRecvTime = gpsData.getGpsRecvTime();
		this.speed = gpsData.speed;
		this.x = gpsData.x;
		this.orientation = gpsData.orientation;
		this.y = gpsData.y;
		setZeroCoord(gpsData.isZeroCoord());
		setUZName(gpsData.isUZName());
		setFWPoint(gpsData.isFWPoint());
		this.ifUZDistTimes10 = gpsData.ifUZDistTimes10;
		return this;
	}
	public GpsData(Date dt) {
		gps_Record_Time = dt.getTime();		
	}
	public GpsData(long dt) {
		gps_Record_Time = dt;		
	}
	
	public boolean isValidPoint() {
//		if(point == null)
//			point = new Point(x,y);
		return !Misc.isUndef(x) && !Misc.isUndef(y); 
	}
	public double getSpeed() {
		return speed;
	}
	
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public double getOrientation() {
		return orientation;
	}
	
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}
	
	public int getDimId() {
		return dimId;
	}
	
	public int getGpsRecordingId() {
		return gpsRecordingId;
	}
	
	public void setGpsRecordingId(int recordingId) {
		this.gpsRecordingId = recordingId;
	}
	
	
	public void setLongitude(double x) {
		this.x = x;
	}

	public void setLatitude(double y) {
		this.y = y;
	}
	//public String getNameForDP() {
	//	return name;
	//}
	public String getName(Connection conn, int vehicleId, CacheTrack.VehicleSetup vehSetup) {
		return calcName(conn, vehicleId, vehSetup);
	}
	public String calcName(Connection conn, int vehicleId, CacheTrack.VehicleSetup vehSetup) {
		//VehiclePosCache vehicleInfo = ;
		//VehicleSetup vehicleSetup =
		
		String gpsDataname = null;
		if (false) 
			return "HACK_UNKNOWN";
		try {
			//if (name != null && name.length() > 0) {
			//	return name;
			//}
			//gpsDataname ="HACK_UNKNOWN";
			if (false) {//false in prod DEBUG13 
			try {
				PreparedStatement ps = conn.prepareStatement("select name from logged_data where vehicle_id=? and attribute_id=0 and gps_record_time <= ?  order by gps_record_time desc limit 1");
				ps.setInt(1, vehicleId);
				ps.setTimestamp(2, Misc.utilToSqlDate(this.getGps_Record_Time()));
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					gpsDataname = rs.getString(1);
				}
				rs.close();
				ps.close();
				
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			}
			else {
			gpsDataname = NameLocationLookUp.fetchLocationName(conn, vehSetup, getPoint(), vehSetup.getDistCalcControl(conn));
			}
			updateWithNameHack(gpsDataname);
			if (this.isZeroCoord())
				gpsDataname = this.specialZeroCoordinatesIndicator+gpsDataname;
			
			//name = gpsDataname;
		} catch (Exception e) {	
			e.printStackTrace();
		}
		return gpsDataname;
	}

	public void updateWithNameHack(String name) {
		if (!isZeroCoord() && name != null && name.startsWith(specialZeroCoordinatesIndicator))
			setZeroCoord(true);

		if (name != null && name.startsWith("[UZ]"))  {
			setUZName(true);
			int kmidx = name.indexOf("KM");
			double distance = 0;
			if (kmidx > 0) {
				int commaIdx = name.indexOf(",");
				if (commaIdx < kmidx)
					distance = Misc.getParamAsDouble(name.substring(name.indexOf(",") + 1, kmidx));
			}
			if (distance < Short.MAX_VALUE/10)
				this.ifUZDistTimes10 = (short) (distance*10);
			else
				this.ifUZDistTimes10 = Short.MAX_VALUE;
		}
	}
	public void setName(String name) {
		updateWithNameHack(name);
	}
	//public void setName(String name) {
	//	if (!zeroCoord && name != null && name.startsWith(specialZeroCoordinatesIndicator))
	//		zeroCoord = true;
	//	this.name = name;
	//	updateUZHack(name);
	//}

	public long getGpsRecvTime() {
		return gpsRecvTime;
	}
	public void setGpsRecvTime(Date dt) {
		if (dt == null)
			dt = com.ipssi.gen.utils.Misc.getCurrentTime();
		this.gpsRecvTime = dt.getTime();
	}
	public void setGpsRecvTime(long dt) {
		if (Misc.isUndef(dt))
			dt = System.currentTimeMillis();
		this.gpsRecvTime = dt;
	}
	
	/**
	 * @return the gps_Record_Time
	 */
	public long getGps_Record_Time() {
		return gps_Record_Time;
	}
	
	public ChannelTypeEnum mergeChannel(GpsData otherInfo) {
		boolean amCC  = isCCChannel();
		boolean amDC = isDCChannel();
		boolean rhsCC = otherInfo.isCCChannel();
		boolean rhsDC = otherInfo.isDCChannel();
		if (rhsDC && ! amDC)
			stateMask |= MASK_DC;
		if (rhsCC && !amCC)
			stateMask |= MASK_CC;
		return getSourceChannel();				
	}

	/**
	 * @param gps_Record_Time
	 *            the gps_Record_Time to set
	 */
	public void setGps_Record_Time(Date gps_Record_Time) {
		this.gps_Record_Time = gps_Record_Time.getTime();
	}
	public void setGps_Record_Time(long gps_Record_Time) {
		this.gps_Record_Time = gps_Record_Time;
	}

	@Override
	public String toString() {
		return " Source: "+this.getSourceChannel().ordinal()+" Pos: ("+x+","+y+") time: " + new Date(this.gps_Record_Time).toString() +  " recv: " + new Date(this.gpsRecvTime).toString() 
				 +" Speed:"+this.getSpeed()+" Dim:("+ dimId+","+value+")";
	}

	/**
	 * @return the sourceChannel
	 */
	public ChannelTypeEnum getSourceChannel() {
		int ch = stateMask & MASK_ALLCC;
		return ch == 1 ? ChannelTypeEnum.CURRENT : ch == 2 ? ChannelTypeEnum.DATA : ch == 3 ? ChannelTypeEnum.BOTH : ChannelTypeEnum.UNKNOWN;
	}
	
	public boolean isDCChannel() {
		return (stateMask & MASK_DC) != 0;
	}
	
	public boolean isCCChannel() {
		return (stateMask & MASK_CC) != 0;
	}

	/**
	 * @param sourceChannel
	 *            the sourceChannel to set
	 */
	public void setSourceChannel(ChannelTypeEnum sourceChannel) {
		stateMask &= ~MASK_ALLCC;
		if (sourceChannel == ChannelTypeEnum.CURRENT)
			stateMask |= MASK_CC;
		else if (sourceChannel == ChannelTypeEnum.DATA)
			stateMask |= MASK_DC;
		else if (sourceChannel == ChannelTypeEnum.BOTH)
			stateMask |= MASK_ALLCC;
	}

	/**
	 * @param point
	 *            the point to set
	 */
//	public void setPoint(Point point) {
//		this.point = point;
//	}

	public void setModelState(ModelState modelState) {
		this.modelState = modelState;
	}

	public ModelState getModelState(ModelSpec modelSpec) {
		if (modelState == null && modelSpec != null) {
			
		}
		return modelState;
	}

	/**
	 * 
	 */
	public int compareTo(Object obj) {		
		GpsData p = (GpsData)obj;
		return this.gps_Record_Time < p.gps_Record_Time ? -1 : this.gps_Record_Time > p.gps_Record_Time ? 1 : 0;		
	}

	
	/**
	 * @return the dimensionInfo
	 */
	

	/**
	 * @param dimensionInfo
	 *            the dimensionInfo to set
	 */

	/**
	 * 
	 */
	public void setDimensionInfo(int dimId, double value) {
		this.dimId = dimId;
		this.value = value;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj instanceof GpsData) {
			GpsData gpsData = (GpsData) obj;
			if (gpsData != null) {// && Misc.isEqual(y, gpsData.y) && Misc.isEqual(x, gpsData.x)) {
				return this.gps_Record_Time == gpsData.getGps_Record_Time();
//				if (!isNull(this.gps_Record_Time)) {
//					return this.gps_Record_Time.equals(gpsData.getGps_Record_Time());
//				} else {
//					return this.gps_Record_Time == gpsData.getGps_Record_Time();
//				}
			}
		}
		return false;
	}

	public void setStrData(String strData) {
		this.strData = strData;
	}

	public String getStrData() {
		return strData;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public void setDimId(int dimId) {
		this.dimId = dimId;
	}
/*
	public boolean isLeftAded() {
		return (stateMask | MASK_LEFTADDED) != 0;
	}

	public void setLeftAdded(boolean leftAdded) {
		stateMask &= ~MASK_LEFTADDED;
		if (leftAdded)
			stateMask |= MASK_LEFTADDED;
	}

	public boolean isRightAded() {
		return (stateMask | MASK_RIGHTADDED) != 0;
	}

	public void setRightAdded(boolean rightAdded) {
		stateMask &= ~MASK_RIGHTADDED;
		if (rightAdded)
			stateMask |= MASK_RIGHTADDED;
	}

	public boolean isLeftUpdated() {
		return (stateMask | MASK_LEFTUPDATED) != 0;
	}

	public void setLeftUpdated(boolean leftUpdated) {
		stateMask &= ~MASK_LEFTUPDATED;
		if (leftUpdated)
			stateMask |= MASK_LEFTUPDATED;
	}

	public boolean isRightUpdated() {
		return (stateMask | MASK_RIGHTUPDATED) != 0;
	}

	public void setRightUpdated(boolean rightUpdated) {
		stateMask &= ~MASK_RIGHTUPDATED;
		if (rightUpdated)
			stateMask |= MASK_RIGHTUPDATED;
	}
	*/
	public boolean isZeroCoord() {
		return (stateMask & MASK_ZC) != 0;
	}

	public void setZeroCoord(boolean zeroCoord) {
		stateMask &= ~MASK_ZC;
		if (zeroCoord)
			stateMask |= MASK_ZC;
	}

	public void setUZName(boolean uzName) {
		stateMask &= ~MASK_UZ;
		if (uzName)
			stateMask |= MASK_UZ;
	}
	
	public void setFWPoint(boolean fwPoint) {
		stateMask &= ~MASK_FW;
		if (fwPoint)
			stateMask |= MASK_FW;
	}

	public boolean isFWPoint() {
		return (stateMask & MASK_FW) != 0;
	}

	public void setRPDone(boolean done) {
		stateMask &= ~MASK_RPDONE;
		if (done)
			stateMask |= MASK_RPDONE;
	}

	public boolean isRPDone() {
		return (stateMask & MASK_RPDONE) != 0;
	}

	public void setTPDone(boolean done) {
		stateMask &= ~MASK_TPDONE;
		if (done)
			stateMask |= MASK_TPDONE;
	}

	public boolean isTPDone() {
		return (stateMask & MASK_TPDONE) != 0;
	}

	
	public boolean isUZName() {
		return (stateMask & MASK_UZ) != 0 ;
	}

	public short getIfUZDistTimes10() {
		return ifUZDistTimes10;
	}
	
	public  boolean isMergeable(GpsData data, boolean gpsIdDelta, boolean gpsIdSensorBased) {
		if ( data.isZeroCoord() != this.isZeroCoord()|| data.getDimId() != this.getDimId())
			return false;
		if (data.getDimId() == 0) {
			if (this.getSpeed() > mergeSpeedThresh || Misc.isEqual(this.getSpeed(), mergeSpeedThresh) || data.getSpeed() > mergeSpeedThresh || Misc.isEqual(data.getSpeed(), mergeSpeedThresh))
				return false;
			
			if (this.getGpsRecordingId() >= 0 && data.getGpsRecordingId() >= 0) {
				int diffid = gpsIdDelta ? this.getGpsRecordingId() : Math.abs(this.getGpsRecordingId()-data.getGpsRecordingId());
				if (gpsIdDelta && diffid == 0)
					diffid = data.getGpsRecordingId();
				int mergeDistThreshM = gpsIdSensorBased ? 0 : (int)(mergeDistThresh*1000);
				if (diffid > mergeDistThreshM)
					return false;
			}
			double d = this.distance(data.getLongitude(), data.getLatitude());
			return (d <mergeDistThresh || Misc.isEqual(d, mergeDistThresh));
		}
		else if (this.getDimId() == Misc.STRIKE_DIM_ID) {
			return false;
		}
		else {
			return Misc.isEqual(this.getValue(), data.getValue());
		}
	}
	
	public boolean isDifferent(GpsData data) {
		return getDimId() != data.getDimId() || !Misc.isEqual(getValue(), data.getValue()) || !Misc.isEqual(getLatitude(), data.getLatitude()) || !Misc.isEqual(getLongitude(), data.getLongitude());
	}
	public boolean isDifferentInclZero(GpsData data) {
		return this.isZeroCoord() != data.isZeroCoord() || getDimId() != data.getDimId() || !Misc.isEqual(getValue(), data.getValue()) || !Misc.isEqual(getLatitude(), data.getLatitude()) || !Misc.isEqual(getLongitude(), data.getLongitude());
	}

	public static void main(String[] args) {
		Point p = new Point(82.537201, 22.256901);
		Point p2 = new Point(82.537697, 22.256701);
		Point p3 = new Point(82.536903,22.255699);
		double d1= p.fastGeoDistance(p2);
		double d2= p.fastGeoDistance(p3);
		double x= 1;
	}

	//#@#@# - encoding Multi record packing: 3 bits for count and bit for second difference
	//kept in little endian format - 0 at right most part, 1 at second part etc
	/** WIP - note sure if it is needed

	
	public Pair<GpsData, GpsData> add(GpsData data) {
		//Pair.1st: The current point if it is not added
		//Pair.2nd: The pt that is split off from current after the possibility of adding
		//Finally in the fast list we will have this, pair.1st, pair.2nd
		
		GpsData first = null;
		GpsData second =  null;
		boolean isMergeAble = isMergeAble(data);
		EncodedInfo encoding = EncodedInfo.decode(data);
		long baseRecSec = gps_Record_Time/1000;
		long baseRecvSec= gpsRecvTime/1000;
		long dataRecSec = data.gps_Record_Time/1000;
		long dataRecvSec= data.gpsRecvTime/1000;
		encoding.setRecRecvIndex(dataRecSec, dataRecvSec, baseRecSec, baseRecvSec);
		if (isMergeAble) {
			
		}
		if (isMergeAble) {
		
		}
		
		if (!isMergeAble) {
			
		}
		if (isMergeAble) {//see if there is enough space to merge
			
		}
	
	}
	private static class EncodedInfo {//not a good design trying to avoid too many classes and object creation
		public static final int BLOCK_COUNT =5;
		public static final long COMBO_MASK = 0xFFF;
		public static final long DELTA_MASK = 0x1FF;
		public static final long COUNT_MASK = 0x7;

		int rec_cnt[];
		int rec_delta[];
		int recv_cnt[];
		int recv_delta[];
		int recTimeBlockIndex = -1;
		int recvTimeBlockIndex = -1;
		
		public EncodedInfo() {
			rec_cnt = new int[BLOCK_COUNT];
			rec_delta = new int[BLOCK_COUNT];
			recv_cnt = new int[BLOCK_COUNT];
			recv_delta = new int[BLOCK_COUNT];

		}
		public void setRecRecvIndex(long recSecond, long recvSecond, long baseRecSecond, long baseRecvSecond){
			int gapRec = (int)(recSecond - baseRecSecond);
			int gapRecv = (int)(recvSecond- baseRecvSecond);
			for (int art=0;art<2;art++) {
				int cnt[] = art == 0 ? rec_cnt: recv_cnt;
				int delta[] = art == 0 ? rec_delta : recv_delta;
				int gap = art == 0 ? gapRec : gapRecv;
				int i = 0;
				int totNow = 0;
				for ( i = -1;i<BLOCK_COUNT && totNow < gap;) {
					i++;
					if (cnt[i] == 0 && delta[i] == 0)
						break;
					totNow += cnt[i]*delta[i];
				}
				if (art == 0)
					recTimeBlockIndex = i;
				else
					recvTimeBlockIndex = i;
			}
		}
		public static EncodedInfo decode(GpsData data) {
			EncodedInfo retval = new EncodedInfo();
			decode(data.multiGpsRecTime,retval.rec_cnt,retval.rec_delta);
			decode(data.multiGpsRecvTime,retval.recv_cnt,retval.recv_delta);
			return retval;
		}
		public void encode(GpsData data) {
			data.multiGpsRecTime = encode(rec_cnt, rec_delta);
			data.multiGpsRecvTime = encode(recv_cnt, recv_delta);
		}
		
		private static void decode(long param, int cnt[], int delta[]) {
			for (int i=0;i<BLOCK_COUNT;i++){
				cnt[i]=0;
				delta[i]= 0;
			}
			for (int i=0;i<BLOCK_COUNT;i++){
				int deltaR = (int) (param & DELTA_MASK);
				param = param >> 9;
				int cntR = (int) (param& COUNT_MASK);
				param = param >> 3;
				cnt[i] = cntR;
				delta[i] = deltaR;
				if (deltaR == 0 && cntR == 0)
					break;
			}
		}
		
		
		private static long encode(int cnt[], int delta[]) {
			long result = 0;
			for (int i=BLOCK_COUNT-1;i>=0;i--) {
				int cntR = cnt[i];
				int deltaR = delta[i];
				if (cntR == 0 && deltaR == 0)
					continue;
				result = result <<3;
				result = result | cntR;
				result = result << 9;
				result = result | deltaR;
			}
			return result;
		}
	}
	public static class DetailedPos {
		
		
		public static final long BLOCK_0_CNT = 0xE000000000000000L;
		public static final long BLOCK_0_DELTA = 0x1FF0000000000000L;
		public static final long BLOCK_1_CNT = BLOCK_0_CNT >> 12;
		public static final long BLOCK_1_DELTA = BLOCK_0_DELTA >> 12;
		public static final long BLOCK_2_CNT = BLOCK_1_CNT >> 12;
		public static final long BLOCK_2_DELTA = BLOCK_1_DELTA >> 12;
		public static final long BLOCK_3_CNT = BLOCK_2_CNT >> 12;
		public static final long BLOCK_3_DELTA = BLOCK_2_DELTA >> 12;
		public static final long BLOCK_4_CNT = BLOCK_3_CNT >> 12;
		public static final long BLOCK_4_DELTA = BLOCK_3_DELTA >> 12;
		
		private int recBlockIndex = -1;
		private int recvBlockIndex = -1;
		private int recBlockCount = 0;
		private int recvBlockCount = 0;
		private int recBlockDelta = 0;
		private int recvBlockDelta = 0;
		public DetailedPos(int recBlockIndex, int recvBlockIndex,
				int recBlockCount, int recvBlockCount, int recBlockDelta,
				int recvBlockDelta) {
			super();
			this.recBlockIndex = recBlockIndex;
			this.recvBlockIndex = recvBlockIndex;
			this.recBlockCount = recBlockCount;
			this.recvBlockCount = recvBlockCount;
			this.recBlockDelta = recBlockDelta;
			this.recvBlockDelta = recvBlockDelta;
		}
		public int getRecBlockIndex() {
			return recBlockIndex;
		}
		public void setRecBlockIndex(int recBlockIndex) {
			this.recBlockIndex = recBlockIndex;
		}
		public int getRecvBlockIndex() {
			return recvBlockIndex;
		}
		public void setRecvBlockIndex(int recvBlockIndex) {
			this.recvBlockIndex = recvBlockIndex;
		}
		public int getRecBlockCount() {
			return recBlockCount;
		}
		public void setRecBlockCount(int recBlockCount) {
			this.recBlockCount = recBlockCount;
		}
		public int getRecvBlockCount() {
			return recvBlockCount;
		}
		public void setRecvBlockCount(int recvBlockCount) {
			this.recvBlockCount = recvBlockCount;
		}
		public int getRecBlockDelta() {
			return recBlockDelta;
		}
		public void setRecBlockDelta(int recBlockDelta) {
			this.recBlockDelta = recBlockDelta;
		}
		public int getRecvBlockDelta() {
			return recvBlockDelta;
		}
		public void setRecvBlockDelta(int recvBlockDelta) {
			this.recvBlockDelta = recvBlockDelta;
		}
		
	}
*/		 
}// end of class
