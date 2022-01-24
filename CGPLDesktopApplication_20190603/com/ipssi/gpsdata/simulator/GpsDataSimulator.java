package com.ipssi.gpsdata.simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.processor.utils.ChannelTypeEnum;

public class GpsDataSimulator implements Runnable{
	long start = Misc.getUndefInt();
	volatile int delay = 10;
	volatile int dataGap = 1;
	private long lastRunOn = Misc.getUndefInt();
	private Thread mThread = null;
	private static GpsDataSimulator gpsDataSimulator = null;
	private GpsDataSimulator(long startFrom, int delaySec, int dataGapMin){
		if(!Misc.isUndef(startFrom))
			this.start = startFrom;
		this.delay = delaySec;
		this.dataGap = dataGapMin;
	}
	
	public static GpsDataSimulator getGpsDataSimulator(long startFrom, int delaySec, int dataGapMin){
		if(gpsDataSimulator == null)
			gpsDataSimulator = new GpsDataSimulator(startFrom, delaySec, dataGapMin);
		return gpsDataSimulator;
	}
	
	public void changeParams(long startFrom, int delaySec, int dataGapMin){
		synchronized (SDF) {
			if(!Misc.isUndef(startFrom))
				this.start = startFrom;
			this.delay = delaySec;
			this.dataGap = dataGapMin;
		}
	}

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private void sendGpsDatatoGpsQueue(){
		System.out.println("sendGpsDatatoGpsQueue: called");
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement("select vehicle_id,cast(longitude as DECIMAL(9,6)) longitude, cast(latitude as DECIMAL(9,6))  latitude, attribute_id, attribute_value, gps_record_time, source, name,updated_on,speed,gps_id from logged_data_init " +
					"where attribute_id = 0 and  (? is null or updated_on >= ?) and (? is null or updated_on < ?)" +
					" order by updated_on ");
			ps.setTimestamp(1, Misc.isUndef(start) ? null : new Timestamp(start));
			ps.setTimestamp(2, Misc.isUndef(start) ? null : new Timestamp(start));
			ps.setTimestamp(3, Misc.isUndef(start) ? null : new Timestamp(start+dataGap*60*1000));
			ps.setTimestamp(4, Misc.isUndef(start) ? null : new Timestamp(start+dataGap*60*1000));
			
			rs = ps.executeQuery();

			while(rs.next()){
				int vehicleId = Misc.getRsetInt(rs, "vehicle_id");
				long updatedOn = Misc.getDateInLong(rs, "updated_on");
				int source = Misc.getRsetInt(rs, "source");
				ChannelTypeEnum channel=com.ipssi.processor.utils.ChannelTypeEnum.getChannelType(source);
				double speed = Misc.getRsetDouble(rs, "speed");
				long gpsRecordTime = Misc.getDateInLong(rs, "gps_record_time");
				double lon = Misc.getRsetDouble(rs, "longitude");
				double lat = Misc.getRsetDouble(rs, "latitude");
				int attributeId= Misc.getRsetInt(rs, "attribute_id");
				double attributeValue = Misc.getRsetDouble(rs, "attribute_value");
				StringBuilder gpsDataString = null;
				if (!Misc.isUndef(vehicleId)) {
					start = updatedOn;
					gpsDataString = new StringBuilder();
					gpsDataString.append(channel);
					gpsDataString.append(",").append(vehicleId);
					// deliberately not sending Cardinal Directions...will have to change this while moving into a different geography
					gpsDataString.append(",").append(lat);
					gpsDataString.append(",").append(lon);
					gpsDataString.append(",").append(SDF.format(gpsRecordTime));
					gpsDataString.append(",").append(SDF.format(updatedOn));
					//Speed,Orientation,Odo
					gpsDataString.append(",").append(speed);
					gpsDataString.append(",").append("null");
					gpsDataString.append(",").append("null");
					gpsDataString.append(",").append("null");
					gpsDataString.append(",").append("null");
					System.out.println("[GPS][DATA][SIM]"+gpsDataString.toString());
					DataProcessorQueueSender.send(gpsDataString.toString(), vehicleId);
					
				} 
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			destroyIt = false;
		}finally{
			try {
				Misc.closeRS(rs);
				Misc.closePS(ps);
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(runThis){
			try{
				lastRunOn = System.currentTimeMillis();
				sendGpsDatatoGpsQueue();
			}catch(Exception ex){
				ex.printStackTrace();
			}finally{
				try{
					long temp = System.currentTimeMillis();
					
					long currentGap = (lastRunOn - temp)/1000;
					if(currentGap < delay)
					Thread.sleep((delay-currentGap)*1000);
					lastRunOn = System.currentTimeMillis();
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}
	}
	volatile boolean runThis = false;
	public void start() {
		if(runThis)
			return;
		//stop();
		System.out.println("starting GpsDataSimulator");
		mThread = new Thread(this);
		mThread.setName("GpsDataSimulator");
		System.out.println("started GpsDataSimulator");
		runThis = true;
		mThread.start();
	}

	public void stop() {
		System.out.println("stopping GpsDataSimulator....");
		runThis = false;
		try {
			if (mThread != null) {
				mThread.interrupt();
				mThread = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("stopped GpsDataSimulator....");
	}
	
	public boolean isRunning(){
		return runThis;
	}
}
