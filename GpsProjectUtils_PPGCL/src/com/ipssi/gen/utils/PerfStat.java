package com.ipssi.gen.utils;

public class PerfStat {
	
	private  static boolean enable = false;
	public static synchronized boolean isEnabled() {
		return enable;
	}
	public static synchronized void setEnable(boolean val) {
		enable = val;
	}
	public static synchronized  void addMsgHandler(long delta) {
		if (enable) {
			msgHandlerOverAll += delta;
			msgHanderOverAllCount++;
		}
	}
	public static synchronized  void addParse(long delta) {
		if (enable) {
			parseOverAll += delta;
			parseOverAllCount++;
		}
	}
	public static synchronized  void addProcess(long delta) {
		if (enable) {
			processOverAll += delta;
			processOverAllCount++;
		}
	}
	final public static int  UPDATE_CURRENT = 0;
	final public static int  INSERT_CURRENT = 1;
	final public static int  UPDATE_DATA = 2; //needed
	final public static int  UPDATE_DATADIST = 3; //needed
	final public static int  UPDATE_DATASOURCE = 4; //not needed
	final public static int  INSERT_DATA = 5; //needed
	final public static int  UPDATE_LOGGED_DATA_DETAILS = 6; //needed
	final public static int  UPDATE_LOGGED_DATA_DETAILS_DIST = 7; //needed
	final public static int  UPDATE_LOGDATA = 8;// needed
	final public static int UPDATEBE_ = 9; //not needed
	public static synchronized  void addDBUpdIns(long delta, int forWhat) {
		if (enable) {
			dbUpdInsOverAll += delta;
			dbUpdInsOverAllCount++;
			switch (forWhat) {
			case  UPDATE_CURRENT:
				updateCurrent += delta;
				updateCurrentCount++;
				break;
			case  INSERT_CURRENT:
				insertCurrent += delta;
				insertCurrentCount++;
				break;
			case  UPDATE_DATA:
				updateData += delta;
				updateDataCount++;
				break;
			case  UPDATE_DATADIST:
				updateDataDist += delta;
				updateDataDistCount++;
				break;
			case  UPDATE_DATASOURCE:
				updateDataSource += delta;
				updateDataSourceCount++;
				break;
			case  INSERT_DATA:
				insertData += delta;
				insertDataCount++;
				break;
			case  UPDATE_LOGGED_DATA_DETAILS:
				updateLoggedDataDetails += delta;
				updateLoggedDataDetailsCount++;
				break;
			case  UPDATE_LOGGED_DATA_DETAILS_DIST:
				updateLoggedDataDetailsDist += delta;
				updateLoggedDataDetailsDistCount++;
				break;
			case  UPDATE_LOGDATA:
				logDataUpdate += delta;
				logDataUpdateCount++;
				break;
			case UPDATEBE_:
				updateBe += delta;
				updateBeCount++;
			default:
				break;
			}
			
					
		}
	}
	public static synchronized  void addUpdateBeOfPeriod(long delta) {
		if (enable) {
			updateBeOfPeriodOverAll += delta;
			updateBeOfPeriodOverAllCount++;
		}
	}
	public static synchronized  void addVDFFindAndAdd(long delta) {
		if (enable) {
			newVehicleDataInsOverAll += delta;
			newVehicleDataInsOverAllCount++;
		}
	}
	public static synchronized  void addVDFGet(long delta) {
		if (enable) {
			newVehicleDataGetOverAll += delta;
			newVehicleDataGetOverAllCount++;
		}
	}
	public static synchronized  void addVDFQuery(long delta) {
		if (enable) {
			newVehicleDataQueryPrepOverAll += delta;
			newVehicleDataQueryPrepOverAllCount++;
		}
	}
	public static synchronized  void addVDFSync(long delta) {
		if (enable) {
			syncWaitOverAll += delta;
			syncWaitOverAllCount++;
		}
	}
	public static synchronized  void addRecoPrepMsg(long delta) {
		if (enable) {
			recoPrepOverAll += delta;
			recoPrepOverAllCount++;
		}
	}
	public static synchronized  void addRecoDB(long delta) {
		if (enable) {
			recoDBOverAll += delta;
			recoDBOverAllCount++;
		}
	}
	public static StringBuilder getStats() {
		StringBuilder sb = new StringBuilder();
		
		msgHanderOverAllCount = msgHanderOverAllCount == 0 ? 1 : msgHanderOverAllCount;
		parseOverAllCount = parseOverAllCount == 0 ? 1 : parseOverAllCount;
		processOverAllCount = processOverAllCount == 0 ? 1 : processOverAllCount;
		dbUpdInsOverAllCount = dbUpdInsOverAllCount == 0 ? 1 : dbUpdInsOverAllCount;
		updateBeOfPeriodOverAllCount = updateBeOfPeriodOverAllCount == 0 ? 1 : updateBeOfPeriodOverAllCount;
		newVehicleDataInsOverAllCount = newVehicleDataInsOverAllCount == 0 ? 1 : newVehicleDataInsOverAllCount;
		newVehicleDataGetOverAllCount = newVehicleDataGetOverAllCount == 0 ? 1 : newVehicleDataGetOverAllCount;
		newVehicleDataQueryPrepOverAllCount = newVehicleDataQueryPrepOverAllCount == 0 ? 1 : newVehicleDataQueryPrepOverAllCount;
		syncWaitOverAllCount = syncWaitOverAllCount == 0 ? 1 : syncWaitOverAllCount;
		recoPrepOverAllCount = recoPrepOverAllCount == 0 ? 1 : recoPrepOverAllCount;
		recoDBOverAllCount = recoDBOverAllCount == 0 ? 1 : recoDBOverAllCount;
		
		updateCurrentCount = updateCurrentCount == 0 ? 1 : updateCurrentCount;
		insertCurrentCount = insertCurrentCount == 0 ? 1 : insertCurrentCount;
		updateDataCount = updateDataCount == 0 ? 1 : updateDataCount;
		updateDataDistCount = updateDataDistCount == 0 ? 1 : updateDataDistCount;
		updateDataSourceCount = updateDataSourceCount == 0 ? 1 : updateDataSourceCount;
		insertDataCount = insertDataCount == 0 ? 1 : insertDataCount;
		updateLoggedDataDetailsCount = updateLoggedDataDetailsCount == 0 ? 1 : updateLoggedDataDetailsCount;
		updateLoggedDataDetailsDistCount = updateLoggedDataDetailsDistCount == 0 ? 1 : updateLoggedDataDetailsDistCount;
		logDataUpdateCount = logDataUpdateCount == 0 ? 1 : logDataUpdateCount;
		updateBeCount = updateBeCount == 0 ? 1 : updateBeCount;

		sb.append("Message Handler Overall: ").append(" Avg (ms):").append((double)msgHandlerOverAll/(double) (msgHanderOverAllCount <= 0 ? 1 : msgHanderOverAllCount)/1000000.0)
        .append(" ms:").append((double)msgHandlerOverAll/1000000.0)
        .append(" cnt:").append(msgHanderOverAllCount).append("\n");
		sb.append("Message Parsing Overall: ").append(" Avg (ms):").append((double)parseOverAll/(double) (parseOverAllCount <= 0 ? 1 : parseOverAllCount)/1000000.0)
        .append(" ms:").append((double)parseOverAll/1000000.0)
        .append(" cnt:").append(parseOverAllCount).append("\n");
		sb.append("Point DB Insert/Update Overall: ").append(" Avg (ms):").append((double)dbUpdInsOverAll/(double) (dbUpdInsOverAllCount <=0 ? 1 : dbUpdInsOverAllCount) /1000000.0)
        .append(" ms:").append((double)dbUpdInsOverAll/1000000.0)
        .append(" cnt:").append(dbUpdInsOverAllCount).append("\n");
		sb.append("updateBeOfPeriod?? Insert/Update Overall: ").append(" Avg (ms):").append((double)updateBeOfPeriodOverAll/(double) (updateBeOfPeriodOverAllCount <= 0 ? 1 : updateBeOfPeriodOverAllCount)/1000000.0)
        .append(" ms:").append((double)updateBeOfPeriodOverAll/1000000.0)
        .append(" cnt:").append(updateBeOfPeriodOverAllCount).append("\n");
		sb.append("VDF Structure Insert Overall: ").append(" Avg (ms):").append((double)newVehicleDataInsOverAll/(double) (newVehicleDataInsOverAllCount <= 0 ? 1 : newVehicleDataInsOverAllCount)/1000000.0)
        .append(" ms:").append((double)newVehicleDataInsOverAll/1000000.0)
        .append(" cnt:").append(newVehicleDataInsOverAllCount).append("\n");
		sb.append("VDF Structure Get (Incl Q) Overall: ").append(" Avg (ms):").append((double)newVehicleDataGetOverAll/(double) (newVehicleDataGetOverAllCount <= 0 ? 1 : newVehicleDataGetOverAllCount)/1000000.0)
        .append(" ms:").append((double)newVehicleDataGetOverAll/1000000.0)
        .append(" cnt:").append(newVehicleDataGetOverAllCount).append("\n");
		sb.append("VDF Structure Get Q+Prep Overall: ").append(" Avg (ms):").append((double)newVehicleDataQueryPrepOverAll/(double) (newVehicleDataQueryPrepOverAllCount <= 0 ? 1 : newVehicleDataQueryPrepOverAllCount)/1000000.0)
        .append(" ms:").append((double)newVehicleDataQueryPrepOverAll/1000000.0)
        .append(" cnt:").append(newVehicleDataQueryPrepOverAllCount).append("\n");
		sb.append("VDF/VDP Sync Wait Overall: ").append(" Avg (ms):").append((double)syncWaitOverAll/(double) (syncWaitOverAllCount <= 0 ? 1 : syncWaitOverAllCount)/1000000.0)
        .append(" ms:").append((double)syncWaitOverAll/1000000.0)
        .append(" cnt:").append(syncWaitOverAllCount).append("\n");
		sb.append("Recovery Read put array Overall: ").append(" Avg (ms):").append((double)recoPrepOverAll/(double) (recoPrepOverAllCount <= 0 ? 1: recoPrepOverAllCount) /1000000.0)
        .append(" ms:").append((double)recoPrepOverAll/1000000.0)
        .append(" cnt:").append(recoPrepOverAllCount).append("\n");
		sb.append("Recovery delete Id: ").append(" Avg (ms):").append((double)recoDBOverAll/(double) (recoDBOverAllCount <= 0 ? 1 : recoDBOverAllCount)/1000000.0)
        .append(" ms:").append((double)recoDBOverAll/1000000.0)
        .append(" cnt:").append(recoDBOverAllCount).append("\n");

		sb.append("DB Update Current: ").append(" Avg (ms):").append((double)updateCurrent/(double) updateCurrentCount/1000000.0)
        .append(" ms:").append((double)updateCurrent/1000000.0)
        .append(" cnt:").append(updateCurrentCount).append("\n");

		sb.append("DB Insert Current: ").append(" Avg (ms):").append((double)insertCurrent/(double) insertCurrentCount/1000000.0)
        .append(" ms:").append((double)insertCurrent/1000000.0)
        .append(" cnt:").append(insertCurrentCount).append("\n");

		sb.append("DB Update Data: ").append(" Avg (ms):").append((double)updateData/(double) updateDataCount/1000000.0)
        .append(" ms:").append((double)updateData/1000000.0)
        .append(" cnt:").append(updateDataCount).append("\n");

		sb.append("DB Update Data Dist: ").append(" Avg (ms):").append((double)updateDataDist/(double) updateDataDistCount/1000000.0)
        .append(" ms:").append((double)updateDataDist/1000000.0)
        .append(" cnt:").append(updateDataDistCount).append("\n");

		sb.append("DB Update Data Source: ").append(" Avg (ms):").append((double)updateDataSource/(double) updateDataSourceCount/1000000.0)
        .append(" ms:").append((double)updateDataSource/1000000.0)
        .append(" cnt:").append(updateDataSourceCount).append("\n");

		sb.append("DB Insert Data: ").append(" Avg (ms):").append((double)insertData/(double) insertDataCount/1000000.0)
        .append(" ms:").append((double)insertData/1000000.0)
        .append(" cnt:").append(insertDataCount).append("\n");

		sb.append("DB Update LoggedData Details: ").append(" Avg (ms):").append((double)updateLoggedDataDetails/(double) updateLoggedDataDetailsCount/1000000.0)
        .append(" ms:").append((double)updateLoggedDataDetails/1000000.0)
        .append(" cnt:").append(updateLoggedDataDetailsCount).append("\n");

		sb.append("DB Update LoggedData Dist Details: ").append(" Avg (ms):").append((double)updateLoggedDataDetailsDist/(double) updateLoggedDataDetailsDistCount/1000000.0)
        .append(" ms:").append((double)updateLoggedDataDetailsDist/1000000.0)
        .append(" cnt:").append(updateLoggedDataDetailsDistCount).append("\n");

		sb.append("DB Log Data Update (last)??: ").append(" Avg (ms):").append((double)logDataUpdate/(double) logDataUpdateCount/1000000.0)
        .append(" ms:").append((double)logDataUpdate/1000000.0)
        .append(" cnt:").append(logDataUpdateCount).append("\n");

		sb.append("DB UpdateBe??: ").append(" Avg (ms):").append((double)updateBe/(double) updateBeCount/1000000.0)
        .append(" ms:").append((double)updateBe/1000000.0)
        .append(" cnt:").append(updateBeCount).append("\n");

		return sb;
	}
	synchronized public static void  init() {
		msgHandlerOverAll = 0;
		msgHanderOverAllCount = 0;
		parseOverAll = 0;
		parseOverAllCount = 0;
		processOverAll = 0;
		processOverAllCount = 0;
		dbUpdInsOverAll = 0;
		dbUpdInsOverAllCount = 0;
		updateBeOfPeriodOverAll = 0;
		updateBeOfPeriodOverAllCount = 0;
		newVehicleDataInsOverAll = 0;
		newVehicleDataInsOverAllCount = 0;
		newVehicleDataGetOverAll = 0;
		newVehicleDataGetOverAllCount = 0;
		newVehicleDataQueryPrepOverAll = 0;
		newVehicleDataQueryPrepOverAllCount = 0;
		syncWaitOverAll = 0;
		syncWaitOverAllCount = 0;
		recoPrepOverAll = 0;
		recoPrepOverAllCount = 0;
		recoDBOverAll = 0;
		recoDBOverAllCount = 0;
		
		updateCurrent = 0;
		insertCurrent = 0;
		updateData = 0;
		updateDataDist = 0;
		updateDataSource = 0;
		insertData = 0;
		updateLoggedDataDetails = 0;
		updateLoggedDataDetailsDist = 0;
		logDataUpdate = 0;
		updateBe = 0;
		updateBeCount = 0;
		
		updateCurrentCount = 0;
		insertCurrentCount = 0;
		updateDataCount = 0;
		updateDataDistCount = 0;
		updateDataSourceCount = 0;
		insertDataCount = 0;
		updateLoggedDataDetailsCount = 0;
		updateLoggedDataDetailsDistCount = 0;
		logDataUpdateCount = 0;
		


			
	}
	public static long msgHandlerOverAll = 0;
	public static int msgHanderOverAllCount = 0;
	public static long parseOverAll = 0;
	public static int parseOverAllCount = 0;
	public static long processOverAll = 0;
	public static int processOverAllCount = 0;
	public static long dbUpdInsOverAll = 0;
	public static int dbUpdInsOverAllCount = 0;
	public static long updateBeOfPeriodOverAll = 0;
	public static int updateBeOfPeriodOverAllCount = 0;
	public static long newVehicleDataInsOverAll = 0;
	public static int newVehicleDataInsOverAllCount = 0;
	public static long newVehicleDataGetOverAll = 0;
	public static int newVehicleDataGetOverAllCount = 0;
	public static long newVehicleDataQueryPrepOverAll = 0;
	public static int newVehicleDataQueryPrepOverAllCount = 0;
	public static long syncWaitOverAll = 0;
	public static int syncWaitOverAllCount = 0;
	public static long recoPrepOverAll = 0;
	public static int recoPrepOverAllCount = 0;
	public static long recoDBOverAll = 0;
	public static int recoDBOverAllCount = 0;

	public static long updateCurrent = 0;
	public static long insertCurrent = 0;
	public static long updateData = 0;
	public static long updateDataDist = 0;
	public static long updateDataSource = 0;
	public static long insertData = 0;
	public static long updateLoggedDataDetails = 0;
	public static long updateLoggedDataDetailsDist = 0;
	public static long logDataUpdate = 0;
	public static long updateBe = 0;
	public static int updateBeCount = 0;
	
	public static int updateCurrentCount = 0;
	public static int insertCurrentCount = 0;
	public static int updateDataCount = 0;
	public static int updateDataDistCount = 0;
	public static int updateDataSourceCount = 0;
	public static int insertDataCount = 0;
	public static int updateLoggedDataDetailsCount = 0;
	public static int updateLoggedDataDetailsDistCount = 0;
	public static int logDataUpdateCount = 0;
	
}
